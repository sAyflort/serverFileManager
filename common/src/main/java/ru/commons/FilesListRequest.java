package ru.commons;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FilesListRequest implements Request{
    private List<File> fileList;
    private String path;

    public FilesListRequest(String path) {
        Path serverPath = Paths.get(path);
        this.path = path;
        try {
            List<File> pathList = Files.list(serverPath)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
            fileList = pathList;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<File> getFileList() {
        return fileList;
    }

    public String getPath() {
        return path;
    }
}
