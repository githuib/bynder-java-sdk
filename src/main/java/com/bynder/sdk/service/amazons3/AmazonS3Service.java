/*
 * Copyright (c) 2017 Bynder B.V. All rights reserved.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */
package com.bynder.sdk.service.amazons3;

import com.bynder.sdk.model.upload.MultipartParameters;
import com.bynder.sdk.model.upload.UploadRequest;
import com.bynder.sdk.service.asset.AssetService;
import com.bynder.sdk.util.Indexed;
import io.reactivex.Completable;
import io.reactivex.Observable;
import retrofit2.Response;

/**
 * Interface to upload file parts to Amazon S3.
 */
public interface AmazonS3Service {

    /**
     * Uploads a file part to Amazon S3.
     *
     * @param filename Name of the file to be uploaded.
     * @param chunkNumber Number of the chunk to be uploaded.
     * @param fileContent Content of the file to be uploaded.
     * @param numberOfChunks Total number of chunks.
     * @param multipartParams AWS request parameters.
     * @return {@link Observable} with the request {@link Response} information.
     */
    Completable uploadPartToAmazon(
            final Indexed<byte[]> chunk,
            final String filename,
            final int numberOfChunks,
            final MultipartParameters multipartParams
    );

    /**
     * Builder class used to create a new instance of {@link AssetService}.
     */
    class Builder {

        private Builder() {
        }

        public static AmazonS3Service create(final String bucket) {
            return new AmazonS3ServiceImpl(bucket);
        }
    }
}
