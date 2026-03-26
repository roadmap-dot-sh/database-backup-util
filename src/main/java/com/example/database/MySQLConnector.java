/*
 * MySQLConnector.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.database;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * MySQLConnector.java
 *
 * @author Nguyen
 */
public class MySQLConnector implements DatabaseConnector {
    @Override
    public Connection getConnection(String host, Integer port, String username, String password, String database) throws Exception {

        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC",
                host, port, database);

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
        // User mysqldump for backup
        String command = String.format(
                "mysqldump -h %s -P %d -u %s -p%s %s > %s",
                host, port, username, password, database, outputPath
        );

        Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Backup failed with exit code: " + exitCode);
        }
    }

    @Override
    public void performIncrementalBackup(String host, Integer port, String username, String password, String database, String outputPath, String lastBackupTime) throws Exception {
        // MySQL incremental backup using binary logs
        String command = String.format(
                "mysqlbinlog --start-datetime=\"%s\" --result-file=%s " +
                        "--host=%s --port=%d --user=%s --password=%s",
                lastBackupTime, outputPath, host, port, username, password
        );

        Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Incremental backup failed with exit code: " + exitCode);
        }
    }

    @Override
    public void performDifferentialBackup(String host, Integer port, String username, String password, String database, String outputPath, String lastFullBackup) throws Exception {
        // For MySQL, differential backup can be implemented using binary logs
        // This is a simplified version
        performBackup(host, port, username, password, database, outputPath);
    }

    @Override
    public void restoreBackup(String host, Integer port, String username, String password, String database, String backupFile, String[] tables) throws Exception {
        String command;
        if (tables != null && tables.length > 0) {
            // Restore specific tables
            StringBuilder tableList = new StringBuilder();
            for (String table : tables) {
                tableList.append(table).append(" ");
            }
            command = String.format(
                    "mysql -h %s -P %d -u %s -p%s %s < %s",
                    host, port, username, password, database, backupFile
            );
        } else {
            // Restore entire database
            command = String.format(
                    "mysql -h %s -P %d -u %s -p%s %s < %s",
                    host, port, username, password, database, backupFile
            );
        }

        Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Restore failed with exit code: " + exitCode);
        }
    }
}
