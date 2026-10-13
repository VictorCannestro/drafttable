package com.cannestro.drafttable.supporting.utils;

import org.jspecify.annotations.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.URI;
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
public final class FileHelper {

    private FileHelper() {}


    /**
     * <p><b>Requires</b>: The URI must not be null and represent a URL </p>
     * <p><b>Guarantees</b>: The resource located at the remote URI will be copied byte-for-byte to a local file. The
     * destination file will be located at {@code X/Y/Z} where:
     * <items>
     *     <li> {@code X} = Absolute path of the "java.io.tmpdir" </li>
     *     <li> {@code Y} = UUID </li>
     *     <li> {@code Z} = The text after the last forward or backslash of the path of the URI </li>
     * </items> </p>
     *
     * @param fileUri e.g., {@code URI.create("https://people.sc.fsu.edu/~jburkardt/data/csv/faithful.csv")}
     * @param connectionTimeoutInMillis A non-negative integer
     * @param readTimeoutInMillis A non-negative integer
     * @return A local {@code File}
     */
    public static File copyToTempDirectory(@NonNull URI fileUri,
                                           int connectionTimeoutInMillis,
                                           int readTimeoutInMillis) {
        if (connectionTimeoutInMillis < 0 || readTimeoutInMillis < 0) {
            throw new IllegalArgumentException("Time parameters must be non-negative integers");
        }
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
                    FilenameUtils.getName(fileUri.getPath())
            );
            File temp = new File(filePath);
            copyURLToFile(NetHelper.url(fileUri), temp, connectionTimeoutInMillis, readTimeoutInMillis);
            return temp;
        } catch (IOException e) {
            throw new IllegalArgumentException("An IO error occurred during copying.", e);
        }
    }

    /**
     * <p><b>Requires</b>: The URI must not be null and represent a URL </p>
     * <p><b>Guarantees</b>: The resource located at the remote URI will be copied byte-for-byte to a local file. The
     * destination file will be located at {@code X/Y/Z} where:
     * <items>
     *     <li> {@code X} = Absolute path of the "java.io.tmpdir" </li>
     *     <li> {@code Y} = UUID </li>
     *     <li> {@code Z} = The text after the last forward or backslash of the path of the URI </li>
     * </items> </p>
     *
     * @param fileUri e.g., {@code URI.create("https://people.sc.fsu.edu/~jburkardt/data/csv/faithful.csv")}
     * @return A local {@code File}
     * @apiNote If no connection could be established to the source URI, then timeout will occur in 10 minutes. If no
     * data could be read from the source URI, then timeout will occur in 10 minutes.
     */
    public static File opinionatedCopyToTempDirectory(@NonNull URI fileUri) {
        final int defaultInMinutes = 10;
        return copyToTempDirectory(
                fileUri,
                (int) TimeUnit.of(ChronoUnit.MINUTES).toMillis(defaultInMinutes),
                (int) TimeUnit.of(ChronoUnit.MINUTES).toMillis(defaultInMinutes)
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

    /**
     * <p> Attempts to create a {@code BufferedReader} from the target resource using the following, in order,
     * short-circuiting upon success:
     * <ol>
     *     <li> {@code FileInputStream} </li>
     *     <li> {@code ContextClassLoader} </li>
     *     <li> File tree walk starting at {@code resourceFilePath} </li>
     * </ol> </p>
     *
     * @param resourceFilePath The system-dependent file name
     * @param charset To inform the {@code InputStreamReader}
     * @return A {@code Reader}
     * @throws IOException if an I/ O error occurs opening the file
     */
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

    /**
     * Walks the file tree rooted at the given starting {@code filePath} to find the target path. Will only return the
     * target file ifi it is a regular file with opaque content.
     *
     * @param filePath A non-null string
     * @return A {@code Path}
     * @throws IllegalArgumentException if the Path to the given URI could not be located
     */
    public static Path walkFileTreeToFind(@NonNull String filePath) {
        try (Stream<Path> paths = Files.walk(Paths.get(filePath))) {
            return paths.filter(Files::isRegularFile).findAny().orElseThrow();
        } catch (IOException e) {
            log.error("Could not locate {}", filePath);
            throw new IllegalArgumentException("Could not locate the Path to the given URI");
        }
    }

    /**
     * Deletes the {@code file} and it's parent directory
     *
     * @param file Any non-null {@code File}
     */
    public static void cleanUpTemporaryFiles(@NonNull File file) {
        deleteFileIfPresent(file.getPath());
        deleteFileIfPresent(file.getParentFile().getPath());
    }

}
