package ai.automation.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class Helper {




    public static String randromNumberByLength(int length) {

        String numberSet = "0123456789";

        if (length <= 0) {
            return null;
        }

        StringBuilder result = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = getRandomNumber(numberSet.length());
            result.append(numberSet.charAt(randomIndex));
        }

        return result.toString();
    }

    public static int getRandomNumber(int range) {

        SecureRandom random = new SecureRandom();
        return random.nextInt(range);
    }

    public static List<String> findJavaFiles(String projectPath) {

        List<String> javaFiles = new ArrayList<>();

        File projectDir = new File(projectPath);

        if (projectDir.exists() && projectDir.isDirectory()) {
            findJavaFilesRecursive(projectDir, javaFiles);
        }

        return javaFiles;
    }

    public static List<String> findFeatureFiles(String projectPath) {

        List<String> featureFiles = new ArrayList<>();

        File projectDir = new File(projectPath);

        if (projectDir.exists() && projectDir.isDirectory()) {
            findFeatureFilesRecursive(projectDir, featureFiles);
        }

        return featureFiles;
    }

    private static void findJavaFilesRecursive(
            File directory,
            List<String> javaFiles) {

        File[] files = directory.listFiles();

        if (files != null) {

            for (File file : files) {

                if (file.isDirectory()) {

                    findJavaFilesRecursive(file, javaFiles);

                } else if (file.getName().endsWith(".java")) {

                    javaFiles.add(file.getAbsolutePath());
                }
            }
        }
    }
    private static void findFeatureFilesRecursive(
            File directory,
            List<String> featureFiles) {

        File[] files = directory.listFiles();

        if (files != null) {

            for (File file : files) {

                if (file.isDirectory()) {

                    findFeatureFilesRecursive(file, featureFiles);

                } else if (file.getName().endsWith(".feature")) {

                    featureFiles.add(file.getAbsolutePath());
                }
            }
        }
    }
    public static Boolean isFeatureFilePresent(String fileName, String featuresPath) {

        String projectPath = featuresPath; // Replace with your project path

        List<String> featureFiles = findFeatureFiles(projectPath);

        for (String featureFile : featureFiles) {

            if (featureFile.contains(fileName)) {
                return true;
            }
        }

        return false;
    }

    public static void createFile(String filePath, String content)
            throws IOException {

        Path path = Path.of(filePath);

        // Ensure parent directories exist
        if (path.getParent() != null) {

            Files.createDirectories(path.getParent());

            // Create the file if it does not exist
            if (Files.notExists(path)) {
                Files.createFile(path);
            }
        }

        // Write content if supplied
        if (content != null) {

            // Write (replace existing content)
            Files.writeString(
                    path,
                    content,
                    StandardOpenOption.TRUNCATE_EXISTING);

            // Force OS to sync the file to disk
            try (FileChannel channel =
                         FileChannel.open(
                                 path,
                                 StandardOpenOption.WRITE)) {

                channel.force(true); // flushes OS buffers
            }
        }
    }
    public static Boolean isJavaFilePresent(String fileName, String packagePath) {

        String projectPath = packagePath; // Replace with your project path

        List<String> javaFiles = findJavaFiles(projectPath);

        for (String javaFile : javaFiles) {

            if (javaFile.contains(fileName)) {
                return true;
            }
        }

        return false;
    }

    public static String readFile(String fileName) {
        try (InputStream inputStream =
                     Helper.class.getClassLoader().getResourceAsStream(fileName)) {

            if (inputStream == null) {
                throw new RuntimeException("File not found: " + fileName);
            }

            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + fileName, e);
        }
    }
}
