package com.bynder.sdk.service.upload;

import com.bynder.sdk.api.BynderApi;
import com.bynder.sdk.model.upload.SaveMediaResponse;
import com.bynder.sdk.query.decoder.QueryDecoder;
import com.bynder.sdk.query.upload.SaveMediaQuery;
import com.bynder.sdk.query.upload.UploadQuery;
import com.bynder.sdk.util.Indexed;
import com.bynder.sdk.util.Utils;
import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Response;

import java.io.*;
import java.util.Arrays;

public abstract class BaseFileUploader {

    static final int MAX_CHUNK_SIZE = 1024 * 1024 * 5;

    /**
     * Instance of {@link BynderApi} which handles the HTTP communication.
     */
    BynderApi bynderApi;

    /**
     * Instance of {@link QueryDecoder} to decode query objects into API parameters.
     */
    QueryDecoder queryDecoder;

    UploadQuery uploadQuery;
    File file;
    int numberOfChunks;

    /**
     * Creates a new instance of the class.
     *
     * @param bynderApi Instance to handle the HTTP communication with the Bynder API.
     * @param queryDecoder Query decoder.
     * @param uploadQuery Upload query with the information to upload the file.
     */
    BaseFileUploader(
            final BynderApi bynderApi,
            final QueryDecoder queryDecoder,
            final UploadQuery uploadQuery
    ) {
        this.bynderApi = bynderApi;
        this.queryDecoder = queryDecoder;
        this.uploadQuery = uploadQuery;
        this.file = new File(uploadQuery.getFilepath());
        this.numberOfChunks = (int) ((file.length() + MAX_CHUNK_SIZE - 1) / MAX_CHUNK_SIZE);
    }

    abstract Single<SaveMediaResponse> uploadFile()
            throws IOException;

    abstract Observable<Indexed<byte[]>> uploadFileWithProgress()
            throws FileNotFoundException;

    /**
     * Read the file into chunks and emits them through an Observable.
     *
     * @return Observable emitting file chunks
     * @throws FileNotFoundException when the file could not be found on the local OS
     */
    Observable<byte[]> readChunks()
            throws FileNotFoundException {
        InputStream is = new FileInputStream(file);

        return Observable.generate(emitter -> {
            byte[] buffer = new byte[MAX_CHUNK_SIZE];
            int chunkSize = is.read(buffer);
            if (chunkSize == -1) {
                // Reading chunks completed.
                is.close();
                emitter.onComplete();
            } else if (chunkSize < MAX_CHUNK_SIZE) {
                // The last chunk could be smaller than the max size.
                emitter.onNext(Arrays.copyOf(buffer, chunkSize));
            } else {
                emitter.onNext(buffer);
            }
        });
    }

    /**
     * Calls {@link BynderApi#saveMedia} to save the completely uploaded file in
     * Bynder.
     *
     * @return {@link Observable} with the {@link SaveMediaResponse} information.
     */
    Single<SaveMediaResponse> saveMedia(String uploadId) {
        SaveMediaQuery saveMediaQuery = new SaveMediaQuery()
                .setAudit(uploadQuery.isAudit())
                .setMetaproperties(uploadQuery.getMetaproperties());

        if (uploadQuery.getMediaId() != null) {
            // The uploaded file will be attached to an existing asset.
            saveMediaQuery
                    .setMediaId(uploadQuery.getMediaId());
        } else {
            // A new asset will be created for the upoaded file.
            saveMediaQuery
                    .setBrandId(uploadQuery.getBrandId())
                    .setName(uploadQuery.getFilename());
        }

        return Utils.handleRequest(bynderApi.saveMedia(
                uploadId,
                queryDecoder.decode(saveMediaQuery)
        )).map(Response::body);
    }

}
