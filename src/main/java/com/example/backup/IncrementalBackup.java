/*
 * IncrementalBackup.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.backup;

import com.example.config.BackupConfig;
import com.example.database.DatabaseConnector;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * IncrementalBackup.java
 *
 * @author Nguyen
 */
public class IncrementalBackup implements BackupStrategy {
    private String lastBackupTime;

    public IncrementalBackup() {
        // Default to last 24 hours if not specified
        this.lastBackupTime = LocalDateTime.now().minusHours(24)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public IncrementalBackup(String lastBackupTime) {
        this.lastBackupTime = lastBackupTime;
    }

    @Override
    public void executeBackup(DatabaseConnector connector, BackupConfig config, String outputPath) throws Exception {
        System.out.println("Performing incremental backup since: " + lastBackupTime);
        connector.performIncrementalBackup(
                config.getHost(),
                config.getPort(),
                config.getUsername(),
                config.getPassword(),
                config.getDatabase(),
                outputPath,
                lastBackupTime
        );
        System.out.println("Incremental backup completed successfully");
    }
}
