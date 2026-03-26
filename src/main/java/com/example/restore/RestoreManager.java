/*
 * RestoreManager.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.restore;

import com.example.backup.BackupCompressor;
import com.example.database.DatabaseConnector;
import com.example.database.DatabaseConnectorFactory;
import com.example.logging.BackupLogger;

import java.io.File;
import java.time.LocalDateTime;

/**
 * RestoreManager.java
 *
 * @author Nguyen
 */
public class RestoreManager {
    private BackupLogger logger;

    public RestoreManager() {
        this.logger = new BackupLogger();
    }

    public boolean restoreBackup(String dbType, String host, Integer port,
                                 String username, String password, String database,
                                 String backupFile, String[] tables) {
        String restoreId = "RESTORE_" + System.currentTimeMillis();
        LocalDateTime startTime = LocalDateTime.now();

        try {
            logger.logRestoreStart(restoreId, backupFile, database);

            // Check if backup file exists
            File backup = new File(backupFile);
            if (!backup.exists()) {
                throw new Exception("Backup file not found: " + backupFile);
            }

            // Check if file is compressed
            String actualBackupFile = backupFile;
            boolean isCompressed = backupFile.endsWith(".gz") || backupFile.endsWith(".zip") ||
                    backupFile.endsWith(".tar.gz");

            if (isCompressed) {
                // Decompress the backup file
                BackupCompressor compressor = new BackupCompressor();
                String tempDir = "/tmp/restore_" + restoreId;
                new File(tempDir).mkdirs();

                compressor.decompress(backupFile, tempDir);

                // Find the actual backup file in the extracted directory
                File[] files = new File(tempDir).listFiles();
                if (files != null && files.length > 0) {
                    actualBackupFile = files[0].getAbsolutePath();
                } else {
                    throw new Exception("No files found in compressed backup");
                }
            }

            // Get database connector
            DatabaseConnector connector = DatabaseConnectorFactory.create(dbType);

            // Test connection
            if (!connector.testConnection(host, port, username, password, database)) {
                throw new Exception("Failed to connect to database");
            }

            // Perform restore
            connector.restoreBackup(host, port, username, password, database,
                    actualBackupFile, tables);

            // Clean up temporary files
            if (isCompressed) {
                String tempDir = "/tmp/restore_" + restoreId;
                deleteDirectory(new File(tempDir));
            }

            LocalDateTime endTime = LocalDateTime.now();
            logger.logRestoreSuccess(restoreId, startTime, endTime);

            System.out.println("Restore completed successfully!");
            return true;

        } catch (Exception e) {
            logger.logRestoreError(restoreId, e);
            System.err.println("Restore failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean validateBackupFile(String backupFile, String dbType) {
        try {
            File file = new File(backupFile);
            if (!file.exists()) {
                System.err.println("Backup file does not exist");
                return false;
            }

            if (file.length() == 0) {
                System.err.println("Backup file is empty");
                return false;
            }

            // Additional validation based on database type
            switch (dbType.toLowerCase()) {
                case "mysql":
                case "postgresql":
                    // Check if it's a valid SQL dump
                    try (java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.FileReader(backupFile))) {
                        String firstLine = reader.readLine();
                        if (firstLine != null && (firstLine.contains("-- MySQL") ||
                                firstLine.contains("-- PostgreSQL"))) {
                            return true;
                        }
                    }
                    break;
                case "mongodb":
                    if (backupFile.endsWith(".archive") || backupFile.endsWith(".gz")) {
                        return true;
                    }
                    break;
                case "sqlite":
                    if (backupFile.endsWith(".db") || backupFile.endsWith(".sqlite") ||
                            backupFile.endsWith(".sql")) {
                        return true;
                    }
                    break;
            }

            return true; // Assume valid if we can't determine
        } catch (Exception e) {
            System.err.println("Error validating backup file: " + e.getMessage());
            return false;
        }
    }

    public BackupInfo getBackupInfo(String backupFile) {
        BackupInfo info = new BackupInfo();
        File file = new File(backupFile);

        info.setFileName(file.getName());
        info.setSize(file.length());
        info.setLastModified(file.lastModified());

        // Try to extract more info from the file
        try {
            if (backupFile.endsWith(".gz") || backupFile.endsWith(".zip") ||
                    backupFile.endsWith(".tar.gz")) {
                info.setCompressed(true);

                // Get compression ratio
                long originalSize = estimateOriginalSize(backupFile);
                if (originalSize > 0) {
                    info.setOriginalSize(originalSize);
                    info.setCompressionRatio((double) file.length() / originalSize);
                }
            } else {
                info.setCompressed(false);
            }

            // Try to parse filename for metadata
            String[] parts = file.getName().split("_");
            if (parts.length >= 3) {
                info.setDatabase(parts[0]);
                info.setTimestamp(parts[1]);
            }

        } catch (Exception e) {
            // Ignore parsing errors
        }

        return info;
    }

    private long estimateOriginalSize(String compressedFile) {
        // Simple estimation based on compression ratios
        File file = new File(compressedFile);
        if (compressedFile.endsWith(".gz")) {
            return (long) (file.length() * 4); // Approximate 4x expansion
        } else if (compressedFile.endsWith(".zip")) {
            return (long) (file.length() * 3);
        } else if (compressedFile.endsWith(".tar.gz")) {
            return (long) (file.length() * 5);
        }
        return file.length();
    }

    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    public static class BackupInfo {
        private String fileName;
        private long size;
        private long originalSize;
        private long lastModified;
        private boolean compressed;
        private double compressionRatio;
        private String database;
        private String timestamp;

        // Getters and setters
        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public long getOriginalSize() {
            return originalSize;
        }

        public void setOriginalSize(long originalSize) {
            this.originalSize = originalSize;
        }

        public long getLastModified() {
            return lastModified;
        }

        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }

        public boolean isCompressed() {
            return compressed;
        }

        public void setCompressed(boolean compressed) {
            this.compressed = compressed;
        }

        public double getCompressionRatio() {
            return compressionRatio;
        }

        public void setCompressionRatio(double compressionRatio) {
            this.compressionRatio = compressionRatio;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return String.format("BackupInfo{fileName='%s', size=%d, compressed=%s, database='%s'}",
                    fileName, size, compressed, database);
        }
    }
}
