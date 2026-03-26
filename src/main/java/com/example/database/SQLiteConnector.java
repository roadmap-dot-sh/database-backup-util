/*
 * SQLiteConnector.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * SQLiteConnector.java
 *
 * @author Nguyen
 */
public class SQLiteConnector implements DatabaseConnector {
    @Override
    public Connection getConnection(String host, Integer port, String username, String password, String database) throws Exception {
        // SQLite uses file-based database, host/port are ignored
        String url = "jdbc:sqlite:" + database;
        return DriverManager.getConnection(url);
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
        // For SQLite, we can use the .backup command or simply copy the file
        try (Connection conn = getConnection(host, port, username, password, database);
             Statement stmt = conn.createStatement()) {

            // Use SQLite online backup API
            stmt.execute("backup to " + outputPath);

            // Alternative: Use file copy if the database is not in WAL mode
            // Files.copy(Paths.get(database), Paths.get(outputPath), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    public void performIncrementalBackup(String host, Integer port, String username, String password, String database, String outputPath, String lastBackupTime) throws Exception {
        // SQLite incremental backup using WAL files
        String walFile = database + "-wal";
        File wal = new File(walFile);

        if (wal.exists()) {
            // Check if WAL file was modified after last backup
            if (wal.lastModified() > Long.parseLong(lastBackupTime)) {
                // Backup the WAL file
                try (FileInputStream fis = new FileInputStream(wal);
                     FileOutputStream fos = new FileOutputStream(outputPath)) {
                    byte[] buffer = new byte[8192];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }
            } else {
                // No changes, create empty backup
                new FileOutputStream(outputPath).close();
            }
        } else {
            // No WAL file, perform full backup
            performBackup(host, port, username, password, database, outputPath);
        }
    }

    @Override
    public void performDifferentialBackup(String host, Integer port, String username, String password, String database, String outputPath, String lastFullBackup) throws Exception {
        // For SQLite, differential backup can be the changes since last full backup
        // This can be implemented using SQLite's backup API with a filter
        try (Connection source = getConnection(host, port, username, password, database);
             Connection dest = DriverManager.getConnection("jdbc:sqlite:" + outputPath)) {

            // Create backup with only recent changes
            // This is a simplified version; in practice, you'd need to track changes
            performBackup(host, port, username, password, database, outputPath);
        }
    }

    @Override
    public void restoreBackup(String host, Integer port, String username, String password, String database, String backupFile, String[] tables) throws Exception {
        if (tables != null && tables.length > 0) {
            // Selective restore for SQLite
            try (Connection backupConn = DriverManager.getConnection("jdbc:sqlite:" + backupFile);
                 Connection targetConn = getConnection(host, port, username, password, database);
                 Statement stmt = targetConn.createStatement()) {

                // Disable foreign keys temporarily
                stmt.execute("PRAGMA foreign_keys=OFF");

                for (String table : tables) {
                    // Drop existing table if exists
                    stmt.execute("DROP TABLE IF EXISTS " + table);

                    // Get schema from backup
                    ResultSet rs = backupConn.getMetaData().getTables(null, null, table, null);
                    if (rs.next()) {
                        // Create table in target
                        ResultSet schemaRs = backupConn.getMetaData().getColumns(null, null, table, null);
                        // Simplified - you'd need to reconstruct CREATE TABLE statement

                        // Copy data
                        ResultSet dataRs = backupConn.createStatement().executeQuery("SELECT * FROM " + table);
                        // Insert data into target
                    }
                }

                stmt.execute("PRAGMA foreign_keys=ON");
            }
        } else {
            // Full restore - just copy the backup file
            try (FileInputStream fis = new FileInputStream(backupFile);
                 FileOutputStream fos = new FileOutputStream(database)) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            }
        }
    }
}
