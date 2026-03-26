/*
 * MongoDBConnector.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.sql.Connection;

/**
 * MongoDBConnector.java
 *
 * @author Nguyen
 */
public class MongoDBConnector implements DatabaseConnector {
    @Override
    public Connection getConnection(String host, Integer port, String username, String password, String database) throws Exception {
        throw new UnsupportedOperationException("MongoDB doesn't use JDBC Connection");
    }

    public MongoClient getMongoClient(String host, Integer port, String username, String password) {
        String connectionString = String.format("mongodb://%s:%s@%s:%d",
                username, password, host, port);
        return MongoClients.create(connectionString);
    }

    @Override
    public boolean testConnection(String host, Integer port, String username, String password, String database) {
        try {
            try (MongoClient client = getMongoClient(host, port, username, password)) {
                MongoDatabase db = client.getDatabase(database);
                db.runCommand(new Document("ping", 1));
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void performBackup(String host, Integer port, String username, String password, String database, String outputPath) throws Exception {
        // Use mongodump for backup
        String command = String.format(
                "mongodump --host %s --port %d --username %s --password %s --db %s --out %s",
                host, port, username, password, database, outputPath
        );

        Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Backup failed with exit code: " + exitCode);
        }

        // Create a single archive file
        String archivePath = outputPath + ".archive";
        command = String.format("tar -czf %s -C %s .", archivePath, outputPath);
        process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
        exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Archive creation failed");
        }

        // Clean up the directory
        command = String.format("rm -rf %s", outputPath);
        Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
    }

    @Override
    public void performIncrementalBackup(String host, Integer port, String username, String password, String database, String outputPath, String lastBackupTime) throws Exception {
        // MongoDB incremental backup using oplog
        String command = String.format(
                "mongodump --host %s --port %d --username %s --password %s --db %s " +
                        "--oplog --out %s --oplogLimit %s",
                host, port, username, password, database, outputPath, lastBackupTime
        );

        Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Incremental backup failed with exit code: " + exitCode);
        }
    }

    @Override
    public void performDifferentialBackup(String host, Integer port, String username, String password, String database, String outputPath, String lastFullBackup) throws Exception {
        // For MongoDB, differential backup can use the last full backup timestamp
        String command = String.format(
                "mongodump --host %s --port %d --username %s --password %s --db %s " +
                        "--query '{ \"_id\": { $gt: ObjectId(\"%s\") } }' --out %s",
                host, port, username, password, database, lastFullBackup, outputPath
        );

        Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Differential backup failed with exit code: " + exitCode);
        }
    }

    @Override
    public void restoreBackup(String host, Integer port, String username,
                              String password, String database, String backupFile,
                              String[] collections) throws Exception {

        // Extract the backup first
        String extractPath = "/tmp/mongo_restore_" + System.currentTimeMillis();
        String command = String.format("tar -xzf %s -C %s", backupFile, extractPath);
        Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Failed to extract backup");
        }

        // Restore using mongorestore
        String collectionFilter = "";
        if (collections != null && collections.length > 0) {
            for (String collection : collections) {
                collectionFilter += " --collection " + collection;
            }
        }

        command = String.format(
                "mongorestore --host %s --port %d --username %s --password %s " +
                        "--db %s%s %s/%s",
                host, port, username, password, database, collectionFilter, extractPath, database
        );

        process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
        exitCode = process.waitFor();

        // Clean up
        command = String.format("rm -rf %s", extractPath);
        Runtime.getRuntime().exec(new String[]{"bash", "-c", command});

        if (exitCode != 0) {
            throw new Exception("Restore failed with exit code: " + exitCode);
        }
    }
}
