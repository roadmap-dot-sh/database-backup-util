package com.example;

import com.example.config.BackupConfig;
import com.example.database.DatabaseConnector;
import com.example.database.DatabaseConnectorFactory;
import com.example.restore.RestoreManager;
import com.example.scheduler.BackupScheduler;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "db-backup",
        description = "Database Backup CLI Utility",
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        subcommands = {
                Main.BackupCommand.class,
                Main.RestoreCommand.class,
                Main.ScheduleCommand.class,
                Main.TestConnectionCommand.class
        }
)
public class Main implements Callable<Integer> {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("Database Backup CLI Utility v1.0.0");
        System.out.println("Use --help for available commands");
        return 0;
    }

    @CommandLine.Command(name = "backup", description = "Perform database backup")
    static class BackupCommand implements Callable<Integer> {
        @CommandLine.Option(names = {"--db-type"}, required = true,


                description = "Database type: mysql, postgresql, mongodb, sqlite")
        private String dbType;

        @CommandLine.Option(names = {"--host"}, description = "Database host")
        private String host;

        @CommandLine.Option(names = {"--port"}, description = "Database port")
        private Integer port;

        @CommandLine.Option(names = {"--username"}, description = "Database username")
        private String username;

        @CommandLine.Option(names = {"--password"}, description = "Database password",
                interactive = true)
        private String password;

        @CommandLine.Option(names = {"--database"}, required = true,
                description = "Database name")
        private String database;

        @CommandLine.Option(names = {"--backup-type"}, defaultValue = "full",
                description = "Backup type: full, incremental, differential")
        private String backupType;

        @CommandLine.Option(names = {"--compress"}, defaultValue = "true",
                description = "Compress backup file")
        private boolean compress;

        @CommandLine.Option(names = {"--storage"}, defaultValue = "local",
                description = "Storage type: local, s3, gcs, azure")
        private String storage;

        @CommandLine.Option(names = {"--storage-path"}, required = true,
                description = "Storage path (local directory or bucket name)")
        private String storagePath;

        @CommandLine.Option(names = {"--slack-webhook"}, description = "Slack webhook URL for notifications")
        private String slackWebhook;

        @Override
        public Integer call() throws Exception {
            BackupManager backupManager = new BackupManager();
            BackupConfig config = new BackupConfig();
            config.setDbType(dbType);
            config.setHost(host);
            config.setPort(port);
            config.setUsername(username);
            config.setPassword(password);
            config.setDatabase(database);
            config.setBackupType(backupType);
            config.setCompress(compress);
            config.setStorage(storage);
            config.setStoragePath(storagePath);
            config.setSlackWebhook(slackWebhook);

            boolean success = backupManager.performBackup(config);
            return success ? 0 : 1;
        }
    }

    @CommandLine.Command(name = "restore", description = "Restore database from backup")
    static class RestoreCommand implements Callable<Integer> {

        @CommandLine.Option(names = {"--db-type"}, required = true,
                description = "Database type: mysql, postgresql, mongodb, sqlite")
        private String dbType;

        @CommandLine.Option(names = {"--host"}, description = "Database host")
        private String host;

        @CommandLine.Option(names = {"--port"}, description = "Database port")
        private Integer port;

        @CommandLine.Option(names = {"--username"}, description = "Database username")
        private String username;

        @CommandLine.Option(names = {"--password"}, description = "Database password",
                interactive = true)
        private String password;

        @CommandLine.Option(names = {"--database"}, required = true,
                description = "Database name")
        private String database;

        @CommandLine.Option(names = {"--backup-file"}, required = true,
                description = "Backup file path")
        private String backupFile;

        @CommandLine.Option(names = {"--tables"}, description = "Specific tables to restore (comma-separated)")
        private String tables;

        @Override
        public Integer call() throws Exception {
            RestoreManager restoreManager = new RestoreManager();
            boolean success = restoreManager.restoreBackup(
                    dbType, host, port, username, password, database,
                    backupFile, tables != null ? tables.split(",") : null
            );
            return success ? 0 : 1;
        }
    }

    @CommandLine.Command(name = "schedule", description = "Schedule automated backups")
    static class ScheduleCommand implements Callable<Integer> {

        @CommandLine.Option(names = {"--cron"}, required = true,
                description = "Cron expression for scheduling")
        private String cronExpression;

        @CommandLine.Option(names = {"--config-file"}, required = true,
                description = "Backup configuration file")
        private String configFile;

        @Override
        public Integer call() throws Exception {
            BackupScheduler scheduler = new BackupScheduler();
            scheduler.scheduleBackup(cronExpression, configFile);
            System.out.println("Backup scheduled with cron: " + cronExpression);
            return 0;
        }
    }

    @CommandLine.Command(name = "test-connection", description = "Test database connection")
    static class TestConnectionCommand implements Callable<Integer> {

        @CommandLine.Option(names = {"--db-type"}, required = true,
                description = "Database type: mysql, postgresql, mongodb, sqlite")
        private String dbType;

        @CommandLine.Option(names = {"--host"}, description = "Database host")
        private String host;

        @CommandLine.Option(names = {"--port"}, description = "Database port")
        private Integer port;

        @CommandLine.Option(names = {"--username"}, description = "Database username")
        private String username;

        @CommandLine.Option(names = {"--password"}, description = "Database password",
                interactive = true)
        private String password;

        @CommandLine.Option(names = {"--database"}, required = true,
                description = "Database name")
        private String database;

        @Override
        public Integer call() throws Exception {
            DatabaseConnector connector = DatabaseConnectorFactory.create(dbType);
            boolean connected = connector.testConnection(host, port, username, password, database);

            if (connected) {
                System.out.println("✓ Connection successful!");
                return 0;
            } else {
                System.err.println("✗ Connection failed!");
                return 1;
            }
        }
    }
}