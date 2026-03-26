/*
 * DifferentialBackup.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.backup;

import com.example.config.BackupConfig;
import com.example.database.DatabaseConnector;

/**
 * DifferentialBackup.java
 *
 * @author Nguyen
 */
public class DifferentialBackup implements BackupStrategy {
    private String lastFullBackupPath;

    public DifferentialBackup() {
        // Will be set based on last full backup
    }

    public DifferentialBackup(String lastFullBackupPath) {
        this.lastFullBackupPath = lastFullBackupPath;
    }

    @Override
    public void executeBackup(DatabaseConnector connector, BackupConfig config, String outputPath) throws Exception {
        System.out.println("Performing differential backup...");
        connector.performDifferentialBackup(
                config.getHost(),
                config.getPort(),
                config.getUsername(),
                config.getPassword(),
                config.getDatabase(),
                outputPath,
                lastFullBackupPath
        );
        System.out.println("Differential backup completed successfully");
    }
}
