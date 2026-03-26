/*
 * LocalStorage.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.storage;

import java.io.*;
import java.nio.file.*;

/**
 * LocalStorage.java
 *
 * @author Nguyen
 */
public class LocalStorage implements StorageProvider {
    @Override
    public void storeBackup(String sourceFilePath, String storagePath, String backupFileName) throws Exception {
        Path source = Paths.get(sourceFilePath);
        Path target = Paths.get(storagePath, backupFileName);

        // Create storage directory if it doesn't exist
        Files.createDirectories(target.getParent());

        // Copy file
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Backup stored locally at: " + target);
    }

    @Override
    public void retrieveBackup(String storagePath, String backupFileName, String destinationPath) throws Exception {
        Path source = Paths.get(storagePath, backupFileName);
        Path target = Paths.get(destinationPath, backupFileName);

        if (!Files.exists(source)) {
            throw new FileNotFoundException("Backup file not found: " + source);
        }

        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public boolean deleteBackup(String storagePath, String backupFileName) throws Exception {
        Path file = Paths.get(storagePath, backupFileName);
        return Files.deleteIfExists(file);
    }

    @Override
    public boolean backupExists(String storagePath, String backupFileName) throws Exception {
        Path file = Paths.get(storagePath, backupFileName);
        return Files.exists(file);
    }

    public void listBackups(String storagePath) throws IOException {
        Path dir = Paths.get(storagePath);
        if (Files.exists(dir) && Files.isDirectory(dir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path entry : stream) {
                    System.out.println(entry.getFileName());
                }
            }
        }
    }
}
