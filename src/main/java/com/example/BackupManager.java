/*
 * BackupManager.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example;

import com.example.backup.BackupCompressor;
import com.example.backup.BackupStrategy;
import com.example.backup.BackupStrategyFactory;
import com.example.config.BackupConfig;
import com.example.database.DatabaseConnector;
import com.example.database.DatabaseConnectorFactory;
import com.example.logging.BackupLogger;
import com.example.notification.SlackNotifier;
import com.example.storage.StorageProvider;
import com.example.storage.StorageProviderFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * BackupManager.java
 *
 * @author Nguyen
 */
public class BackupManager {
    private BackupLogger logger;

    public BackupManager() {
        this.logger = new BackupLogger();
    }

    public boolean performBackup(BackupConfig config) {
        LocalDateTime startTime = LocalDateTime.now();
        String backupId = generateBackupId();

        try {
            logger.logBackupStart(backupId, config);

            // Test database connection
            DatabaseConnector connector = DatabaseConnectorFactory.create(config.getDbType());
            if (!connector.testConnection(config.getHost(), config.getPort(),
                    config.getUsername(), config.getPassword(), config.getDatabase())) {
                throw new Exception("Failed to connect to database");
            }

            // Create backup file path
            String backupFileName = generateBackupFileName(config, backupId);
            String tempBackupPath = "/tmp/" + backupFileName;

            // Perform backup based on type
            BackupStrategy backupStrategy = BackupStrategyFactory.create(config.getBackupType());
            backupStrategy.executeBackup(connector, config, tempBackupPath);

            // Compress backup if needed
            String finalBackupPath = tempBackupPath;
            if (config.isCompress()) {
                BackupCompressor compressor = new BackupCompressor();
                finalBackupPath = compressor.compress(tempBackupPath);
                new File(tempBackupPath).delete(); // Remove uncompressed file
            }

            // Store backup
            StorageProvider storage = StorageProviderFactory.create(config.getStorage());
            storage.storeBackup(finalBackupPath, config.getStoragePath(), backupFileName);

            // Clean up temp file
            new File(finalBackupPath).delete();

            LocalDateTime endTime = LocalDateTime.now();
            logger.logBackupSuccess(backupId, startTime, endTime);

            // Send notification
            if (config.getSlackWebhook() != null && !config.getSlackWebhook().isEmpty()) {
                SlackNotifier notifier = new SlackNotifier(config.getSlackWebhook());
                notifier.sendBackupSuccessNotification(backupId, config, startTime, endTime);
            }

            return true;
        } catch (Exception e) {
            logger.logBackupError(backupId, e);

            // Send error notification
            if (config.getSlackWebhook() != null && !config.getSlackWebhook().isEmpty()) {
                SlackNotifier notifier = new SlackNotifier(config.getSlackWebhook());
                notifier.sendBackupFailureNotification(backupId, config, e);
            }

            return false;
        }
    }

    private String generateBackupId() {
        return "BACKUP_" + System.currentTimeMillis();
    }

    private String generateBackupFileName(BackupConfig config, String backupId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_%s_%s", config.getDatabase(), timestamp, backupId);
    }
}
