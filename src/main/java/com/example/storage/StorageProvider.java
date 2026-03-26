/*
 * StorageProvider.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.storage;

/**
 * StorageProvider.java
 *
 * @author Nguyen
 */
public interface StorageProvider {
    void storeBackup(String sourceFilePath, String storagePath, String backupFileName) throws Exception;

    void retrieveBackup(String storagePath, String backupFileName, String destinationPath) throws Exception;

    boolean deleteBackup(String storagePath, String backupFileName) throws Exception;

    boolean backupExists(String storagePath, String backupFileName) throws Exception;
}
