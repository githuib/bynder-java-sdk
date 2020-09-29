/*
 * Copyright (c) 2017 Bynder B.V. All rights reserved.
 * <p>
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */
package com.bynder.sdk.service.upload;

import com.bynder.sdk.api.BynderApi;
import com.bynder.sdk.model.upload.SaveMediaResponse;
import com.bynder.sdk.model.upload.UploadProgress;
import com.bynder.sdk.query.decoder.QueryDecoder;
import com.bynder.sdk.query.upload.UploadQuery;
import com.bynder.sdk.util.Indexed;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Class used to upload files to Bynder.
 */
public class FileUploader {

    /**
     * Instance of {@link BynderApi} which handles the HTTP communication.
     */
    BynderApi bynderApi;

    /**
     * Instance of {@link QueryDecoder} to decode query objects into API parameters.
     */
    QueryDecoder queryDecoder;

    private BaseFileUploader instance(
            final UploadQuery uploadQuery
    ) {
        if (true) {
            return new FilesServiceFileUploader(bynderApi, queryDecoder, uploadQuery);
        } else {
            return new S3FileUploader(bynderApi, queryDecoder, uploadQuery);
        }
    }

    public FileUploader(
            final BynderApi bynderApi,
            final QueryDecoder queryDecoder
    ) {
        this.bynderApi = bynderApi;
        this.queryDecoder = queryDecoder;
    }

    /**
     * Uploads a file with the information specified in the query parameter.
     *
     * @return {@link Observable} with the {@link SaveMediaResponse} information.
     */
    public Single<SaveMediaResponse> uploadFile(final UploadQuery uploadQuery)
            throws IOException {
        return instance(uploadQuery).uploadFile();
    }


    /**
     * Uploads a file with the information specified in the query parameter
     * while providing information on the progress of the upload via the Observable returned.
     *
     * @return {@link Observable} with the {@link UploadProgress} information.
     */

    public Observable<Indexed<byte[]>> uploadFileWithProgress(final UploadQuery uploadQuery)
            throws FileNotFoundException {
        return instance(uploadQuery).uploadFileWithProgress();
    }

}
