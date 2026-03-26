/*
 * StorageProviderFactory.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.storage;

/**
 * StorageProviderFactory.java
 *
 * @author Nguyen
 */
public class StorageProviderFactory {
    public static StorageProvider create(String storageType) throws Exception {
        switch (storageType.toLowerCase()) {
            case "local":
                return new LocalStorage();
            case "s3":
                // These would come from environment variables or config
                String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
                String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
                String region = System.getenv("AWS_REGION");
                return new S3Storage(accessKey, secretKey, region);
            case "gcs":
                String projectId = System.getenv("GCP_PROJECT_ID");
                String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
                return new GCSStorage(projectId, credentialsPath);
            case "azure":
                String connectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
                String containerName = System.getenv("AZURE_CONTAINER_NAME");
                return new AzureStorage(connectionString, containerName);
            default:
                throw new IllegalArgumentException("Unknown storage type: " + storageType);
        }
    }
}
