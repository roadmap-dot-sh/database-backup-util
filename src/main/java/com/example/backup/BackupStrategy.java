/*
 * BackupStrategy.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.backup;

import com.example.config.BackupConfig;
import com.example.database.DatabaseConnector;

/**
 * BackupStrategy.java
 *
 * @author Nguyen
 */
public interface BackupStrategy {
    void executeBackup(DatabaseConnector connector, BackupConfig config, String outputPath) throws Exception;
}
