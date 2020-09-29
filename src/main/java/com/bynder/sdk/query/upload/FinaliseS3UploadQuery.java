/*
 * Copyright (c) 2017 Bynder B.V. All rights reserved.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */
package com.bynder.sdk.query.upload;

import com.bynder.sdk.model.upload.UploadRequest;
import com.bynder.sdk.query.decoder.ApiField;

/**
 * Query with the information to finalise a completely uploaded file.
 */
public class FinaliseS3UploadQuery {

    /**
     * Upload id for the file being uploaded.
     */
    @ApiField(name = "id")
    private final String uploadId;

    /**
     * Target id in the authorisation information.
     */
    @ApiField(name = "targetid")
    private final String targetId;

    /**
     * Base location of the uploaded file.
     */
    @ApiField(name = "s3_filename")
    private final String s3Filename;

    /**
     * Total number of chunks uploaded.
     */
    @ApiField
    private final int chunks;

    public FinaliseS3UploadQuery(final UploadRequest uploadRequest, final int chunks) {
        this.uploadId = uploadRequest.getS3File().getUploadId();
        this.targetId = uploadRequest.getS3File().getTargetId();
        this.s3Filename = uploadRequest.getS3Filename();
        this.chunks = chunks;
    }

    public String getUploadId() {
        return uploadId;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getS3Filename() {
        return s3Filename;
    }

    public int getChunks() {
        return chunks;
    }
}
