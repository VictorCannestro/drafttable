package com.cannestro.drafttable.supporting.utils;

import com.google.common.io.Files;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.nio.file.Files.createDirectories;
import static org.apache.commons.io.FileUtils.copyURLToFile;
import static org.apache.commons.io.FileUtils.getTempDirectory;


/**
 * @author Victor Cannestro
 */
@Slf4j
public class FileUtils {

    private FileUtils() {}


    public static URL url(String fileUrl) {
        try {
            return new URI(fileUrl).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Either no legal protocol could be found in a specification string or the string could not be parsed.", e);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("The String could not be parsed as a URI reference.", e);
        }
    }

    public static File copyFromURL(URL fileUrl,
                                   int connectionTimeoutInMillis,
                                   int readTimeoutInMillis) {
        try {
            Path path = Paths.get(getTempDirectory().getAbsolutePath(), UUID.randomUUID().toString());
            String tempDirectory = createDirectories(path).toFile().getPath();
            String filePath = String.format(
                    "%s%s%s",
                    tempDirectory,
                    File.separator,
                    FilenameUtils.getName(fileUrl.getPath())
            );
            File temp = new File(filePath);
            copyURLToFile(fileUrl, temp, connectionTimeoutInMillis, readTimeoutInMillis);
            return temp;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static File copyFromURL(URL fileUrl) {
        return copyFromURL(
                fileUrl,
                (int) TimeUnit.of(ChronoUnit.MINUTES).toMillis(10),
                (int) TimeUnit.of(ChronoUnit.MINUTES).toMillis(10)
        );
    }

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
        return walkFileTreeToFind(resourceFilePath);
    }

    public static Path walkFileTreeToFind(String filePath) {
        try(Stream<Path> paths = java.nio.file.Files.walk(Paths.get(filePath))) {
            return paths.filter(java.nio.file.Files::isRegularFile).findAny().orElseThrow();
        } catch (IOException e) {
            log.error("Could not locate {}", filePath);
            throw new IllegalArgumentException("Could not locate the Path to the given URI");
        }
    }

    public static Reader createReaderFromResource(@NonNull String resourceFilePath) throws IOException {
        try {
            Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(resourceFilePath), StandardCharsets.UTF_8));
            log.debug("Successfully loaded {} using the FileInputStream.", resourceFilePath);
            return reader;
        } catch (FileNotFoundException e) {
            log.debug("Could not find {} through FileInputStream. Attempting to search for the resource.", resourceFilePath);
        }
        try {
            Reader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceFilePath)
            )));
            log.debug("Successfully loaded {} using the ContextClassLoader.", resourceFilePath);
            return reader;
        } catch (NullPointerException e) {
            log.debug("Could not load {} using the ContextClassLoader. Attempting to search elsewhere.", resourceFilePath);
        }
        return java.nio.file.Files.newBufferedReader(searchForResource(resourceFilePath));
    }

}
