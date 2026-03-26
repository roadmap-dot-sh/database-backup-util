/*
 * DatabaseConnector.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.database;

import java.sql.Connection;
import java.util.Map;

/**
 * DatabaseConnector.java
 *
 * @author Nguyen
 */
public interface DatabaseConnector {
    Connection getConnection(String host, Integer port, String username,
                             String password, String database) throws Exception;

    boolean testConnection(String host, Integer port, String username,
                           String password, String database);

    void performBackup(String host, Integer port, String username,
                       String password, String database, String outputPath) throws Exception;

    void performIncrementalBackup(String host, Integer port, String username,
                                  String password, String database, String outputPath,
                                  String lastBackupTime) throws Exception;

    void performDifferentialBackup(String host, Integer port, String username,
                                   String password, String database, String outputPath,
                                   String lastFullBackup) throws Exception;

    void restoreBackup(String host, Integer port, String username,
                       String password, String database, String backupFile,
                       String[] tables) throws Exception;
}
