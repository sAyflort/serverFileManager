package ru.commons;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class FileSplit {
    private static final int MB_20_SHORT = 19_999_000;

    public void split(Path path, Consumer<byte[]> filePartConsumer) {
        byte[] filePart = new byte[MB_20_SHORT];
        try (FileInputStream fileInputStream = new FileInputStream(path.toFile())) {
            int number_parts = (int) (Files.size(path)/MB_20_SHORT + 1);
            int count = 0;
            while (fileInputStream.read(filePart) != -1) {
                count++;
                if(count == number_parts) {
                    byte[] lastPart = new byte[(int) (Files.size(path)-MB_20_SHORT*(number_parts-1))];
                    System.arraycopy(filePart, 0, lastPart, 0, lastPart.length);
                    filePartConsumer.accept(lastPart);
                } else {
                    filePartConsumer.accept(filePart);
                }
            }
        } catch (IOException e) {
            e.getMessage();
        }
    }
}
