package ru.commons;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileInfo implements Serializable {
    public enum FileType {
        FILE("F"), DIRECTORY("D");

        private String name;

        public String getName() {
            return name;
        }

        FileType(String name) {
            this.name = name;
        }
    }

    private String fileName;
    private FileType type;
    private long size;
    private byte[] bytes;
    private String filePath;

    public String getFileName() {
        return fileName;
    }

    public FileType getType() {
        return type;
    }

    public long getSize() {
        return size;
    }

    public String getFilePath() {
        return filePath;
    }

    public byte[] getFile() {
        return bytes;
    }

    public FileInfo(Path path) {
        try {
            this.fileName = path.getFileName().toString();
            this.type = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
            this.size = type == FileType.DIRECTORY ? -1l : Files.size(path);
            this.filePath = path.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file info from path");
        }
    }

    public FileInfo(FileInfo fileInfo, byte[] bytes) {
        this.fileName = fileInfo.getFileName();
        this.type = fileInfo.getType();
        this.size = fileInfo.getSize();
        this.filePath = fileInfo.getFilePath();
        this.bytes = bytes;
    }

    public FileInfo(String fileName, FileType type, long size, byte[] file) {
        this.fileName = fileName;
        this.type = type;
        this.size = size;
        this.bytes = file;
    }

    public FileInfo(File file) {
        this.fileName = file.getName();
        this.type = file.isDirectory() ? FileType.DIRECTORY : FileType.FILE;
        this.size = type == FileType.DIRECTORY ? -1l : file.length();
        this.filePath = file.getAbsolutePath();
    }

}
