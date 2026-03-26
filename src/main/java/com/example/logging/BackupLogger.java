/*
 * BackupLogger.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.logging;

import ch.qos.logback.classic.Logger;
import com.example.config.BackupConfig;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BackupLogger.java
 *
 * @author Nguyen
 */
public class BackupLogger {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(BackupLogger.class);
    private static final String LOG_FILE = "logs/backup.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ConcurrentHashMap<String, BackupLogEntry> activeBackups;

    public BackupLogger() {
        this.activeBackups = new ConcurrentHashMap<>();
        initializeLogDirectory();
    }

    private void initializeLogDirectory() {
        File logDir = new File("logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }

    public void logBackupStart(String backupId, BackupConfig config) {
        BackupLogEntry entry = new BackupLogEntry();
        entry.setBackupId(backupId);
        entry.setStartTime(LocalDateTime.now());
        entry.setDatabase(config.getDatabase());
        entry.setDbType(config.getDbType());
        entry.setBackupType(config.getBackupType());

        activeBackups.put(backupId, entry);

        String message = String.format("[START] Backup ID: %s, Database: %s, Type: %s, DB: %s",
                backupId, config.getDbType(), config.getBackupType(), config.getDatabase());

        logger.info(message);
        writeToFile(message);
    }

    public void logBackupSuccess(String backupId, LocalDateTime startTime, LocalDateTime endTime) {
        BackupLogEntry entry = activeBackups.remove(backupId);
        long duration = java.time.Duration.between(startTime, endTime).getSeconds();

        String message = String.format("[SUCCESS] Backup ID: %s, Duration: %d seconds, Completed: %s",
                backupId, duration, endTime.format(formatter));

        logger.info(message);
        writeToFile(message);

        // Also write to a success log
        writeToSuccessFile(String.format("%s|%s|%d|%s",
                backupId, startTime.format(formatter), duration, endTime.format(formatter)));
    }

    public void logBackupError(String backupId, Exception error) {
        BackupLogEntry entry = activeBackups.remove(backupId);
        LocalDateTime errorTime = LocalDateTime.now();

        String message = String.format("[ERROR] Backup ID: %s, Error: %s, Time: %s",
                backupId, error.getMessage(), errorTime.format(formatter));

        logger.error(message, error);
        writeToFile(message);
        writeToErrorFile(String.format("%s|%s|%s|%s",
                backupId, errorTime.format(formatter), error.getClass().getSimpleName(), error.getMessage()));
    }

    public void logRestoreStart(String restoreId, String backupFile, String database) {
        String message = String.format("[RESTORE START] Restore ID: %s, Backup: %s, Database: %s",
                restoreId, backupFile, database);

        logger.info(message);
        writeToFile(message);
    }

    public void logRestoreSuccess(String restoreId, LocalDateTime startTime, LocalDateTime endTime) {
        long duration = java.time.Duration.between(startTime, endTime).getSeconds();

        String message = String.format("[RESTORE SUCCESS] Restore ID: %s, Duration: %d seconds",
                restoreId, duration);

        logger.info(message);
        writeToFile(message);
    }

    public void logRestoreError(String restoreId, Exception error) {
        String message = String.format("[RESTORE ERROR] Restore ID: %s, Error: %s",
                restoreId, error.getMessage());

        logger.error(message, error);
        writeToFile(message);
    }

    public void logConnectionTest(String dbType, String host, int port, String database, boolean success) {
        String status = success ? "SUCCESS" : "FAILED";
        String message = String.format("[CONNECTION TEST] DB: %s, Host: %s:%d, Database: %s, Status: %s",
                dbType, host, port, database, status);

        if (success) {
            logger.info(message);
        } else {
            logger.error(message);
        }
        writeToFile(message);
    }

    private synchronized void writeToFile(String message) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            String timestamp = LocalDateTime.now().format(formatter);
            out.println(timestamp + " - " + message);

        } catch (IOException e) {
            logger.error("Failed to write to log file", e);
        }
    }

    private void writeToSuccessFile(String entry) {
        try (FileWriter fw = new FileWriter("logs/success.log", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(entry);
        } catch (IOException e) {
            logger.error("Failed to write to success log", e);
        }
    }

    private void writeToErrorFile(String entry) {
        try (FileWriter fw = new FileWriter("logs/error.log", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(entry);
        } catch (IOException e) {
            logger.error("Failed to write to error log", e);
        }
    }

    public void generateReport(LocalDateTime from, LocalDateTime to) throws IOException {
        String reportFile = String.format("logs/report_%s.txt",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

        try (FileWriter fw = new FileWriter(reportFile);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            out.println("=== Backup Report ===");
            out.println("Period: " + from.format(formatter) + " to " + to.format(formatter));
            out.println();

            // Parse log files and generate statistics
            // This is a simplified version
            out.println("Total backups: " + countBackups(from, to));
            out.println("Successful backups: " + countSuccessfulBackups(from, to));
            out.println("Failed backups: " + countFailedBackups(from, to));
        }
    }

    private int countBackups(LocalDateTime from, LocalDateTime to) {
        // Implementation to count backups from log files
        return 0; // Placeholder
    }

    private int countSuccessfulBackups(LocalDateTime from, LocalDateTime to) {
        // Implementation to count successful backups
        return 0; // Placeholder
    }

    private int countFailedBackups(LocalDateTime from, LocalDateTime to) {
        // Implementation to count failed backups
        return 0; // Placeholder
    }

    private static class BackupLogEntry {
        private String backupId;
        private LocalDateTime startTime;
        private String database;
        private String dbType;
        private String backupType;

        // Getters and setters
        public String getBackupId() {
            return backupId;
        }

        public void setBackupId(String backupId) {
            this.backupId = backupId;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getDbType() {
            return dbType;
        }

        public void setDbType(String dbType) {
            this.dbType = dbType;
        }

        public String getBackupType() {
            return backupType;
        }

        public void setBackupType(String backupType) {
            this.backupType = backupType;
        }
    }
}
