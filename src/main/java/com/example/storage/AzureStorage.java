/*
 * AzureStorage.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.storage;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.specialized.BlockBlobClient;

import java.io.File;
import java.io.FileInputStream;

/**
 * AzureStorage.java
 *
 * @author Nguyen
 */
public class AzureStorage implements StorageProvider {
    private BlobContainerClient containerClient;
    private String connectionString;
    private String containerName;

    public AzureStorage(String connectionString, String containerName) {
        this.connectionString = connectionString;
        this.containerName = containerName;

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);

        // Create container if it doesn't exist
        if (!containerClient.exists()) {
            containerClient.create();
        }
    }

    @Override
    public void storeBackup(String sourceFilePath, String containerName, String backupFileName) throws Exception {
        BlobClient blobClient = containerClient.getBlobClient(backupFileName);
        BlockBlobClient blockBlobClient = blobClient.getBlockBlobClient();

        try (FileInputStream fis = new FileInputStream(sourceFilePath)) {
            blockBlobClient.upload(fis, new File(sourceFilePath).length());
        }

        System.out.println("Backup stored in Azure Blob Storage: " + containerName + "/" + backupFileName);
    }

    @Override
    public void retrieveBackup(String containerName, String backupFileName, String destinationPath) throws Exception {
        BlobClient blobClient = containerClient.getBlobClient(backupFileName);

        if (!blobClient.exists()) {
            throw new Exception("Backup not found: " + backupFileName);
        }

        File destination = new File(destinationPath, backupFileName);
        blobClient.downloadToFile(destination.getAbsolutePath());
        System.out.println("Backup retrieved from Azure: " + destination.getAbsolutePath());
    }

    @Override
    public boolean deleteBackup(String containerName, String backupFileName) throws Exception {
        BlobClient blobClient = containerClient.getBlobClient(backupFileName);

        if (blobClient.exists()) {
            blobClient.delete();
            return true;
        }
        return false;
    }

    @Override
    public boolean backupExists(String containerName, String backupFileName) throws Exception {
        BlobClient blobClient = containerClient.getBlobClient(backupFileName);
        return blobClient.exists();
    }
}
