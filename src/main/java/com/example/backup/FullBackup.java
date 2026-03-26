/*
 * FullBackup.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.backup;

import com.example.config.BackupConfig;
import com.example.database.DatabaseConnector;

/**
 * FullBackup.java
 *
 * @author Nguyen
 */
public class FullBackup implements BackupStrategy {
    @Override
    public void executeBackup(DatabaseConnector connector, BackupConfig config, String outputPath) throws Exception {
        System.out.println("Performing full backup...");
        connector.performBackup(
                config.getHost(),
                config.getPort(),
                config.getUsername(),
                config.getPassword(),
                config.getDatabase(),
                outputPath
        );
        System.out.println("Full backup completed successfully");
    }
}
