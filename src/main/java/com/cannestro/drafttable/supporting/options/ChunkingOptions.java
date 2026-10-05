package com.cannestro.drafttable.supporting.options;

import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

import static java.util.Objects.isNull;


/**
 * @author Victor Cannestro
 */
@Builder
public record ChunkingOptions(Integer limitPerChunk,
                              Integer targetMinimumChunks,
                              @NonNull File parentDirectory,
                              String chunkRootName,
                              @NonNull SupportedExtension extension) {

    public static final String DEFAULT_NAME = "chunk";
    public static final String CHUNK_FORMAT = "%s%s%s_%d.%s";


    public ChunkingOptions {
        if (!isNull(limitPerChunk) && limitPerChunk < 1) {
            throw new IllegalArgumentException("The limit per chunk must be a positive integer.");
        }
        if (isNull(targetMinimumChunks)) {
            targetMinimumChunks = 1;
        } else if (targetMinimumChunks < 1) {
            throw new IllegalArgumentException("The minimum chunk size must be a positive integer.");
        }
        if (StringUtils.isBlank(chunkRootName)) {
            chunkRootName = DEFAULT_NAME;
        }
    }

    public String constructFilenameForChunk(int chunkId) {
        if (chunkId < 0) {
            throw new IllegalArgumentException("The chunk ID must be a non-negative integer: " + chunkId);
        }
        return String.format(CHUNK_FORMAT,
                parentDirectory().getAbsolutePath(),
                File.separator,
                chunkRootName(),
                chunkId,
                extension()
        );
    }

}
