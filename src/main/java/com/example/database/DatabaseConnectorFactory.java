/*
 * DatabaseConnectorFactory.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.database;

/**
 * DatabaseConnectorFactory.java
 *
 * @author Nguyen
 */
public class DatabaseConnectorFactory {
    public static DatabaseConnector create(String dbType) {
        switch (dbType.toLowerCase()) {
            case "mysql":
                return new MySQLConnector();
            case "postgresql":
                return new PostgreSQLConnector();
            case "mongodb":
                return new MongoDBConnector();
            case "sqlite":
                return new SQLiteConnector();
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }
}
