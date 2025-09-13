package com.cannestro.drafttable.supporting.utils;

import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;


/**
 * @author Victor Cannestro
 */
@Slf4j
public class FileUtils {

    private FileUtils() {}


    /**
     * Creates an empty file or updates the last updated timestamp on the same as in the UNIX command of the same name
     *
     * @param filePath The destination filepath, for example {@code ./src/main/resources/csv/export_file.csv}
     */
    public static void touchFile(String filePath) {
        try {
            log.debug("Attempting to create or modify the resource at {}", filePath);
            Files.touch(new File(filePath));
        } catch (IOException e) {
            log.error("Could not create or modify the resource at {}", filePath);
            throw new RuntimeException(e);
        }
        log.debug("Successfully created or modified the resource at {}", filePath);
    }

    /**
     * Deletes the specified file -- based on the UNIX command of the same name
     *
     * @param filePath The destination filepath, for example {@code ./src/main/resources/csv/export_file.csv}
     */
    public static void deleteFileIfPresent(String filePath) {
        try {
            log.debug("Attempting to delete the resource at {}", filePath);
            if(java.nio.file.Files.deleteIfExists(Paths.get(filePath))) {
                log.debug("Successfully deleted the resource at {}", filePath);
            } else {
                log.debug("Resource not found at {}", filePath);
            }
        } catch (IOException e) {
            log.error("Could not delete the resource at {}", filePath);
            throw new RuntimeException(e);
        }
    }

    /**
     * <p><b>Requires</b>: The desired resource is located within the resources directory or a sub-directory. This
     *                     method will first attempt to locate the resource file on the class path relative to the
     *                     {@code build/resources/}  directory. If unsuccessful, it will attempt to locate the resource
     *                     file within the {@code src/main/resources/} directory. </p>
     * <br>
     * <p><b>Guarantees</b>: A Path to the desired resource file will be returned.</p>
     *
     * @param resourceFilePath A filepath relative to the resources directory
     * @return A Path to the resource
     */
    public static Path searchForResource(String resourceFilePath) {
        try {
            return Paths.get(Objects.requireNonNull(
                    Thread.currentThread().getContextClassLoader().getResource("./" + resourceFilePath)
            ).toURI());
        } catch (URISyntaxException | NullPointerException e) {
            log.debug("An exception occurred loading {} from the ContextClassLoader and converting to a Path", resourceFilePath);
        }

        try {
            log.debug("Attempting to walk the ./src/main directory to find the file");
            return walkFileTreeToFind("./src/main/resources/" + resourceFilePath);
        } catch (IllegalArgumentException ex) {
            log.debug("Attempting to walk the ./src/test directory to find the file");
            return walkFileTreeToFind("./src/test/resources/" + resourceFilePath);
        }
    }

    public static Path walkFileTreeToFind(String filePath) {
        try(Stream<Path> paths = java.nio.file.Files.walk(Paths.get(filePath))) {
            return paths.filter(java.nio.file.Files::isRegularFile).findAny().orElseThrow();
        } catch (IOException e) {
            log.error("Could not locate {}", filePath);
            throw new IllegalArgumentException("Could not locate the Path to the given URI");
        }
    }

    public static Reader createReaderFromResource(String resourceFilePath) throws IOException {
        try {
            return new BufferedReader(new InputStreamReader(Objects.requireNonNull(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceFilePath)
            )));
        } catch (NullPointerException e) {
            log.debug("Could not load {}. Now attempting to search for the resource", resourceFilePath);
            return java.nio.file.Files.newBufferedReader(searchForResource(resourceFilePath));
        }
    }

}
