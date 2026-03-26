/*
 * S3Storage.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.storage;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import java.io.File;

/**
 * S3Storage.java
 *
 * @author Nguyen
 */
public class S3Storage implements StorageProvider {

    private AmazonS3 s3Client;
    private String accessKey;
    private String secretKey;
    private String region;

    public S3Storage(String accessKey, String secretKey, String region) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }

    @Override
    public void storeBackup(String sourceFilePath, String bucketName, String backupFileName) throws Exception {
        File file = new File(sourceFilePath);
        PutObjectRequest request = new PutObjectRequest(bucketName, backupFileName, file);

        // Add metadata
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/octet-stream");
        metadata.setContentLength(file.length());
        metadata.addUserMetadata("backup-date", String.valueOf(System.currentTimeMillis()));
        request.setMetadata(metadata);

        s3Client.putObject(request);
        System.out.println("Backup stored in S3: s3://" + bucketName + "/" + backupFileName);
    }

    @Override
    public void retrieveBackup(String bucketName, String backupFileName, String destinationPath) throws Exception {
        File destination = new File(destinationPath, backupFileName);
        GetObjectRequest request = new GetObjectRequest(bucketName, backupFileName);
        s3Client.getObject(request, destination);
        System.out.println("Backup retrieved from S3: " + destination.getAbsolutePath());
    }

    @Override
    public boolean deleteBackup(String bucketName, String backupFileName) throws Exception {
        s3Client.deleteObject(bucketName, backupFileName);
        return !s3Client.doesObjectExist(bucketName, backupFileName);
    }

    @Override
    public boolean backupExists(String bucketName, String backupFileName) throws Exception {
        return s3Client.doesObjectExist(bucketName, backupFileName);
    }

    public void listBackups(String bucketName) {
        ObjectListing listing = s3Client.listObjects(bucketName);
        for (S3ObjectSummary summary : listing.getObjectSummaries()) {
            System.out.println(summary.getKey() + " (" + summary.getSize() + " bytes)");
        }
    }
}
