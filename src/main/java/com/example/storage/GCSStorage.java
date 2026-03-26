/*
 * GCSStorage.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.storage;

import com.google.cloud.storage.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * GCSStorage.java
 *
 * @author Nguyen
 */
public class GCSStorage implements StorageProvider {
    private Storage storage;
    private String projectId;

    public GCSStorage(String projectId, String credentialsPath) throws Exception {
        this.projectId = projectId;
        this.storage = StorageOptions.newBuilder()
                .setProjectId(projectId)
                .build()
                .getService();
    }

    @Override
    public void storeBackup(String sourceFilePath, String bucketName, String backupFileName) throws Exception {
        Bucket bucket = storage.get(bucketName);
        if (bucket == null) {
            bucket = storage.create(BucketInfo.newBuilder(bucketName).build());
        }

        Path filePath = Paths.get(sourceFilePath);
        BlobId blobId = BlobId.of(bucketName, backupFileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("application/octet-stream")
                .build();

        storage.create(blobInfo, Files.readAllBytes(filePath));
        System.out.println("Backup stored in GCS: gs://" + bucketName + "/" + backupFileName);
    }

    @Override
    public void retrieveBackup(String bucketName, String backupFileName, String destinationPath) throws Exception {
        Blob blob = storage.get(BlobId.of(bucketName, backupFileName));
        if (blob == null) {
            throw new Exception("Backup not found: " + backupFileName);
        }

        File destination = new File(destinationPath, backupFileName);
        blob.downloadTo(Paths.get(destination.getAbsolutePath()));
        System.out.println("Backup retrieved from GCS: " + destination.getAbsolutePath());
    }

    @Override
    public boolean deleteBackup(String bucketName, String backupFileName) throws Exception {
        return storage.delete(BlobId.of(bucketName, backupFileName));
    }

    @Override
    public boolean backupExists(String bucketName, String backupFileName) throws Exception {
        Blob blob = storage.get(BlobId.of(bucketName, backupFileName));
        return blob != null && blob.exists();
    }
}
