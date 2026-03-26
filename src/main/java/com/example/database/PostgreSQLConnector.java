/*
 * PostgreSQLConnector.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.database;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * PostgreSQLConnector.java
 *
 * @author Nguyen
 */
public class PostgreSQLConnector implements DatabaseConnector {
    @Override
    public Connection getConnection(String host, Integer port, String username, String password, String database) throws Exception {
        String url = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public boolean testConnection(String host, Integer port, String username, String password, String database) {
        try (Connection conn = getConnection(host, port, username, password, database)) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void performBackup(String host, Integer port, String username, String password, String database, String outputPath) throws Exception {
        // Use pg_dump for PostgreSQL backup
        String command = String.format(
                "PGPASSWORD='%s' pg_dump -h %s -p %d -U %s -F c -b -v -f %s %s",
                password, host, port, username, outputPath, database
        );

        Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Backup failed with exit code: " + exitCode);
        }
    }

    @Override
    public void performIncrementalBackup(String host, Integer port, String username, String password, String database, String outputPath, String lastBackupTime) throws Exception {
        // PostgreSQL incremental backup using WAL archives
        String command = String.format(
                "PGPASSWORD='%s' pg_basebackup -h %s -p %d -U %s -D %s -X stream -P",
                password, host, port, username, outputPath
        );

        Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Incremental backup failed with exit code: " + exitCode);
        }
    }

    @Override
    public void performDifferentialBackup(String host, Integer port, String username, String password, String database, String outputPath, String lastFullBackup) throws Exception {
        // For PostgreSQL, differential backup using pg_dump with --section=data
        String command = String.format(
                "PGPASSWORD='%s' pg_dump -h %s -p %d -U %s --section=data -F c -f %s %s",
                password, host, port, username, outputPath, database
        );

        Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Differential backup failed with exit code: " + exitCode);
        }
    }

    @Override
    public void restoreBackup(String host, Integer port, String username, String password, String database, String backupFile, String[] tables) throws Exception {
        String command = String.format(
                "PGPASSWORD='%s' pg_restore -h %s -p %d -U %s -d %s -v %s",
                password, host, port, username, database, backupFile
        );

        if (tables != null && tables.length > 0) {
            StringBuilder tableArgs = new StringBuilder();
            for (String table : tables) {
                tableArgs.append(" -t ").append(table);
            }
            command = String.format(
                    "PGPASSWORD='%s' pg_restore -h %s -p %d -U %s -d %s%s -v %s",
                    password, host, port, username, database, tableArgs.toString(), backupFile
            );
        }

        Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Restore failed with exit code: " + exitCode);
        }
    }
}
