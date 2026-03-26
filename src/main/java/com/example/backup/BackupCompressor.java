/*
 * BackupCompressor.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.backup;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * BackupCompressor.java
 *
 * @author Nguyen
 */
public class BackupCompressor {
    public enum CompressionType {
        GZIP,
        ZIP,
        TAR_GZ
    }

    private CompressionType compressionType;

    public BackupCompressor() {
        this(CompressionType.GZIP);
    }

    public BackupCompressor(CompressionType compressionType) {
        this.compressionType = compressionType;
    }

    public String compress(String filePath) throws IOException {
        File inputFile = new File(filePath);
        if (!inputFile.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        String compressedFilePath = getCompressedFilePath(filePath);

        switch (compressionType) {
            case GZIP:
                compressGzip(filePath, compressedFilePath);
                break;
            case ZIP:
                compressZip(filePath, compressedFilePath);
                break;
            case TAR_GZ:
                compressTarGz(filePath, compressedFilePath);
                break;
        }

        return compressedFilePath;
    }

    public String compressDirectory(String directoryPath) throws IOException {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IOException("Directory not found: " + directoryPath);
        }

        String compressedFilePath = directoryPath + ".tar.gz";
        compressTarGzDirectory(directoryPath, compressedFilePath);

        return compressedFilePath;
    }

    private void compressGzip(String inputPath, String outputPath) throws IOException {
        try (FileInputStream fis = new FileInputStream(inputPath);
             FileOutputStream fos = new FileOutputStream(outputPath);
             GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {

            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                gzipOS.write(buffer, 0, length);
            }
        }
    }

    private void compressZip(String inputPath, String outputPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputPath);
             ZipOutputStream zipOut = new ZipOutputStream(fos);
             FileInputStream fis = new FileInputStream(inputPath)) {

            File fileToZip = new File(inputPath);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[8192];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }

            zipOut.closeEntry();
        }
    }

    private void compressTarGz(String inputPath, String outputPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputPath);
             GzipCompressorOutputStream gzipOut = new GzipCompressorOutputStream(fos);
             TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzipOut)) {

            tarOut.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
            tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

            File inputFile = new File(inputPath);
            addFileToTar(tarOut, inputFile, "");
        }
    }

    private void compressTarGzDirectory(String directoryPath, String outputPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputPath);
             GzipCompressorOutputStream gzipOut = new GzipCompressorOutputStream(fos);
             TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzipOut)) {

            tarOut.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
            tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

            File directory = new File(directoryPath);
            addDirectoryToTar(tarOut, directory, "");
        }
    }

    private void addFileToTar(TarArchiveOutputStream tarOut, File file, String basePath) throws IOException {
        String entryName = basePath + file.getName();
        TarArchiveEntry tarEntry = new TarArchiveEntry(file, entryName);
        tarOut.putArchiveEntry(tarEntry);

        if (file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                IOUtils.copy(fis, tarOut);
            }
            tarOut.closeArchiveEntry();
        } else {
            tarOut.closeArchiveEntry();
        }
    }

    private void addDirectoryToTar(TarArchiveOutputStream tarOut, File dir, String basePath) throws IOException {
        String entryName = basePath + dir.getName() + "/";
        TarArchiveEntry tarEntry = new TarArchiveEntry(entryName);
        tarEntry.setModTime(dir.lastModified());
        tarOut.putArchiveEntry(tarEntry);
        tarOut.closeArchiveEntry();

        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    addDirectoryToTar(tarOut, child, entryName);
                } else {
                    addFileToTar(tarOut, child, entryName);
                }
            }
        }
    }

    public void decompress(String compressedFilePath, String outputPath) throws IOException {
        if (compressedFilePath.endsWith(".gz")) {
            decompressGzip(compressedFilePath, outputPath);
        } else if (compressedFilePath.endsWith(".zip")) {
            decompressZip(compressedFilePath, outputPath);
        } else if (compressedFilePath.endsWith(".tar.gz")) {
            decompressTarGz(compressedFilePath, outputPath);
        } else {
            throw new IOException("Unsupported compression format");
        }
    }

    private void decompressGzip(String inputPath, String outputPath) throws IOException {
        try (FileInputStream fis = new FileInputStream(inputPath);
             GZIPInputStream gzipIS = new GZIPInputStream(fis);
             FileOutputStream fos = new FileOutputStream(outputPath)) {

            byte[] buffer = new byte[8192];
            int length;
            while ((length = gzipIS.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }

    private void decompressZip(String inputPath, String outputPath) throws IOException {
        try (java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(inputPath)) {
            java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                java.util.zip.ZipEntry entry = entries.nextElement();
                File entryDestination = new File(outputPath, entry.getName());

                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    try (InputStream in = zipFile.getInputStream(entry);
                         OutputStream out = new FileOutputStream(entryDestination)) {
                        IOUtils.copy(in, out);
                    }
                }
            }
        }
    }

    private void decompressTarGz(String inputPath, String outputPath) throws IOException {
        try (FileInputStream fis = new FileInputStream(inputPath);
             GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(fis);
             TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {

            TarArchiveEntry entry;
            while ((entry = tarIn.getNextTarEntry()) != null) {
                File outputFile = new File(outputPath, entry.getName());

                if (entry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    outputFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        IOUtils.copy(tarIn, fos);
                    }
                }
            }
        }
    }

    private String getCompressedFilePath(String filePath) {
        switch (compressionType) {
            case GZIP:
                return filePath + ".gz";
            case ZIP:
                return filePath.replaceAll("\\.(sql|dump|json)$", "") + ".zip";
            case TAR_GZ:
                return filePath + ".tar.gz";
            default:
                return filePath + ".gz";
        }
    }

    public long getCompressedSize(String compressedFilePath) throws IOException {
        Path path = Paths.get(compressedFilePath);
        return Files.size(path);
    }

    public double getCompressionRatio(String originalFilePath, String compressedFilePath) throws IOException {
        long originalSize = Files.size(Paths.get(originalFilePath));
        long compressedSize = Files.size(Paths.get(compressedFilePath));
        return (double) compressedSize / originalSize;
    }
}
