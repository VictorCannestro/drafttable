package com.cannestro.drafttable.supporting.utils;

import org.jspecify.annotations.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.apache.commons.io.FileUtils.copyURLToFile;


/**
 * @author Victor Cannestro
 */
@Slf4j
public class FileHelper {

    private FileHelper() {}


    public static File copyToTempDirectory(@NonNull URL fileUrl,
                                           int connectionTimeoutInMillis,
                                           int readTimeoutInMillis) {
        try {
            Path path = Paths.get(
                    new File(System.getProperty("java.io.tmpdir")).getAbsolutePath(),
                    UUID.randomUUID().toString()
            );
            String tempDirectory = Files.createDirectories(path).toFile().getPath();
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
            throw new IllegalArgumentException("An IO error occurred during copying.", e);
        }
    }

    public static File copyToTempDirectory(@NonNull URL fileUrl) {
        return copyToTempDirectory(
                fileUrl,
                (int) TimeUnit.of(ChronoUnit.MINUTES).toMillis(10),
                (int) TimeUnit.of(ChronoUnit.MINUTES).toMillis(10)
        );
    }

    /**
     * Creates an empty file or updates the last updated timestamp on the same as in the UNIX command of the same name
     *
     * @param file The destination file containing the filepath, for example {@code ./src/main/resources/csv/export_file.csv}
     */
    public static void touchFile(@NonNull File file) {
        try {
            log.debug("Attempting to create or modify the resource at {}", file.getAbsolutePath());
            Path path = file.toPath();
            if (!Files.exists(path)) {
                if (!isNull(path.getParent())) {
                    Files.createDirectories(path.getParent());
                }
                Files.createFile(path);
            } else {
                Files.setLastModifiedTime(path, FileTime.from(Instant.now()));
            }
        } catch (IOException e) {
            log.error("Could not create or modify the resource at {}", file.getAbsolutePath());
        }
        log.debug("Successfully created or modified the resource at {}", file.getAbsolutePath());
    }

    /**
     * Deletes the specified file, if present, or directory, if present and empty -- based on the UNIX command of the
     * same name.
     *
     * @param filePath The destination filepath, for example {@code ./src/main/resources/csv/export_file.csv}
     */
    public static void deleteFileIfPresent(@NonNull String filePath) {
        try {
            log.debug("Attempting to delete the resource at {}", filePath);
            if (Files.deleteIfExists(Paths.get(filePath))) {
                log.debug("Successfully deleted the resource at {}", filePath);
            } else {
                log.debug("Resource not found at {}", filePath);
            }
        } catch (IOException e) {
            log.error("Could not delete the resource at {}", filePath);
        }
    }

    public static Reader createReaderFromResource(@NonNull String resourceFilePath, Charset charset) throws IOException {
        try {
            Reader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(resourceFilePath),
                    charset
            ));
            log.debug("Successfully loaded {} using the FileInputStream.", resourceFilePath);
            return reader;
        } catch (FileNotFoundException e) {
            log.debug("Could not find {} through FileInputStream. Attempting to search for the resource.", resourceFilePath);
        }
        try {
            Reader reader = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceFilePath)),
                    charset
            ));
            log.debug("Successfully loaded {} using the ContextClassLoader.", resourceFilePath);
            return reader;
        } catch (NullPointerException e) {
            log.debug("Could not load {} using the ContextClassLoader. Attempting to search elsewhere.", resourceFilePath);
        }
        return Files.newBufferedReader(walkFileTreeToFind(resourceFilePath), charset);
    }

    public static Path walkFileTreeToFind(@NonNull String filePath) {
        try (Stream<Path> paths = Files.walk(Paths.get(filePath))) {
            return paths.filter(Files::isRegularFile).findAny().orElseThrow();
        } catch (IOException e) {
            log.error("Could not locate {}", filePath);
            throw new IllegalArgumentException("Could not locate the Path to the given URI");
        }
    }

    public static void cleanUpTemporaryFiles(@NonNull File file) {
        deleteFileIfPresent(file.getPath());
        deleteFileIfPresent(file.getParentFile().getPath());
    }

}
