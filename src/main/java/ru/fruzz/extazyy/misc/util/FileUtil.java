package ru.fruzz.extazyy.misc.util;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;


public class FileUtil {


    public static String readInputStream(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
        } catch (Exception e) {

            e.printStackTrace();
        }

        return stringBuilder.toString();
    }

    public static void copyFile(Path sourcePath, Path destinationPath) throws IOException {
        Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
    }


    public static void deleteFileOrDirectory(Path path) throws IOException {
        Files.delete(path);
    }


    public static List<String> readLinesFromFile(Path filePath) throws IOException {
        return Files.readAllLines(filePath);
    }

    public static void moveFile(Path sourcePath, Path destinationPath) throws IOException {
        Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
    }
}

