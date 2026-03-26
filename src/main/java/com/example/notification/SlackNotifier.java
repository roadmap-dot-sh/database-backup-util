/*
 * SlackNotifier.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.notification;

import com.example.config.BackupConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * SlackNotifier.java
 *
 * @author Nguyen
 */
public class SlackNotifier {
    private String webhookUrl;
    private ObjectMapper objectMapper;

    public SlackNotifier(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.objectMapper = new ObjectMapper();
    }

    public void sendBackupSuccessNotification(String backupId, BackupConfig config,
                                              LocalDateTime startTime, LocalDateTime endTime) {
        long duration = java.time.Duration.between(startTime, endTime).getSeconds();

        String message = String.format(
                "✅ *Backup Successful!*\n" +
                        "• *Backup ID:* %s\n" +
                        "• *Database:* %s (%s)\n" +
                        "• *Type:* %s\n" +
                        "• *Duration:* %d seconds\n" +
                        "• *Storage:* %s\n" +
                        "• *Compressed:* %s",
                backupId,
                config.getDatabase(),
                config.getDbType(),
                config.getBackupType(),
                duration,
                config.getStorage(),
                config.isCompress() ? "Yes" : "No"
        );

        sendSlackMessage(message, "good");
    }

    public void sendBackupFailureNotification(String backupId, BackupConfig config, Exception error) {
        String message = String.format(
                "❌ *Backup Failed!*\n" +
                        "• *Backup ID:* %s\n" +
                        "• *Database:* %s (%s)\n" +
                        "• *Type:* %s\n" +
                        "• *Error:* %s\n" +
                        "• *Time:* %s",
                backupId,
                config.getDatabase(),
                config.getDbType(),
                config.getBackupType(),
                error.getMessage(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        sendSlackMessage(message, "danger");
    }

    public void sendRestoreNotification(String restoreId, String database, boolean success, String details) {
        String status = success ? "✅ Successful" : "❌ Failed";
        String color = success ? "good" : "danger";

        String message = String.format(
                "*Restore %s*\n" +
                        "• *Restore ID:* %s\n" +
                        "• *Database:* %s\n" +
                        "• *Details:* %s",
                status,
                restoreId,
                database,
                details
        );

        sendSlackMessage(message, color);
    }

    public void sendHealthCheckNotification(String status, String details) {
        String color = "good";
        if (status.equalsIgnoreCase("warning")) {
            color = "warning";
        } else if (status.equalsIgnoreCase("critical")) {
            color = "danger";
        }

        String message = String.format(
                "*System Health Check: %s*\n%s",
                status.toUpperCase(),
                details
        );

        sendSlackMessage(message, color);
    }

    private void sendSlackMessage(String message, String color) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            ObjectNode payload = objectMapper.createObjectNode();
            ObjectNode attachment = objectMapper.createObjectNode();
            attachment.put("color", color);
            attachment.put("text", message);
            attachment.put("mrkdwn_in", objectMapper.createArrayNode().add("text"));

            payload.set("attachments", objectMapper.createArrayNode().add(attachment));

            try (OutputStream os = connection.getOutputStream()) {
                os.write(objectMapper.writeValueAsBytes(payload));
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.err.println("Failed to send Slack notification: HTTP " + responseCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            System.err.println("Error sending Slack notification: " + e.getMessage());
        }
    }

    public void sendCustomMessage(String title, String message, String color) {
        String formattedMessage = String.format("*%s*\n%s", title, message);
        sendSlackMessage(formattedMessage, color);
    }
}
