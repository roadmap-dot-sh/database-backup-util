/*
 * BackupStrategyFactory.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.backup;

/**
 * BackupStrategyFactory.java
 *
 * @author Nguyen
 */
public class BackupStrategyFactory {
    public static BackupStrategy create(String backupType) {
        switch (backupType.toLowerCase()) {
            case "full":
                return new FullBackup();
            case "incremental":
                return new IncrementalBackup();
            case "differential":
                return new DifferentialBackup();
            default:
                throw new IllegalArgumentException("Unknown backup type: " + backupType);
        }
    }
}
