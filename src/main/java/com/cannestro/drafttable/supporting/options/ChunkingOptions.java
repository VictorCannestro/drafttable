package com.cannestro.drafttable.supporting.options;

import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

import static java.util.Objects.isNull;


@Builder
public record ChunkingOptions(Integer limitPerChunk,
                              Integer targetMinimumChunks,
                              String filenameWithoutExtension,
                              @NonNull File parentDirectory) {

    public static final String DEFAULT_NAME = "chunk";


    public ChunkingOptions {
        if (!isNull(limitPerChunk) && limitPerChunk < 1) {
            throw new IllegalArgumentException("The limit per chunk must be a positive integer.");
        }
        if (isNull(targetMinimumChunks)) {
            targetMinimumChunks = 1;
        } else if (targetMinimumChunks < 1) {
            throw new IllegalArgumentException("The minimum chunk size must be a positive integer.");
        }
        if (StringUtils.isBlank(filenameWithoutExtension)) {
            filenameWithoutExtension = DEFAULT_NAME;
        }
    }



}
