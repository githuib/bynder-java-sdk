package com.bynder.sdk.service.upload;

import com.bynder.sdk.api.BynderApi;
import com.bynder.sdk.exception.BynderUploadException;
import com.bynder.sdk.model.upload.FinaliseS3Response;
import com.bynder.sdk.model.upload.SaveMediaResponse;
import com.bynder.sdk.model.upload.UploadRequest;
import com.bynder.sdk.query.decoder.QueryDecoder;
import com.bynder.sdk.query.upload.*;
import com.bynder.sdk.service.amazons3.AmazonS3Service;
import com.bynder.sdk.util.Indexed;
import com.bynder.sdk.util.Utils;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Response;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class S3FileUploader extends BaseFileUploader {

    /**
     * Max polling iterations to wait for the file to be converted.
     */
    private static final int MAX_POLLING_ITERATIONS = 60;

    /**
     * Idle time between polling iterations.
     */
    private static final int POLLING_IDLE_TIME = 2000;

    private final AmazonS3Service amazonS3Service;
    private final UploadRequest uploadRequest;

    /**
     * Creates a new instance of the class.
     *
     * @param bynderApi    Instance to handle the HTTP communication with the Bynder API.
     * @param queryDecoder Query decoder.
     * @param uploadQuery  Upload query with the information to upload the file.
     */
    public S3FileUploader(BynderApi bynderApi, QueryDecoder queryDecoder, UploadQuery uploadQuery) {
        super(bynderApi, queryDecoder, uploadQuery);

        amazonS3Service = AmazonS3Service.Builder.create(
                getClosestS3Endpoint().blockingGet()
        );
        uploadRequest = initUpload().blockingGet();
    }

    @Override
    public Single<SaveMediaResponse> uploadFile()
            throws IOException {
        return uploadFileWithProgress().ignoreElements() // upload file chunks
                .andThen(finalizeUpload()) // finalise when all chunks are uploaded
                .flatMap(this::pollImageConversion) // wait until the file is processed
                .flatMap(this::saveMedia); // save in asset bank
    }

    @Override
    public Observable<Indexed<byte[]>> uploadFileWithProgress()
            throws FileNotFoundException {

        return Utils.mapWithIndex(readChunks(), 1) // read file into chunks
                .flatMapSingle(this::uploadChunk); // upload chunks through API
    }

//    /**
//     * Check {@link BynderApi#saveMedia} for more information.
//     */
//    private Observable<Response<SaveMediaResponse>> saveMedia(
//            final String importId,
//            final SaveMediaQuery saveMediaQuery
//    ) {
//        return bynderApi.saveMedia(importId, queryDecoder.decode(saveMediaQuery));
//    }

    /**
     * Calls the {@link AmazonS3Service} to upload the chunk to Amazon and after registers the
     * uploaded chunk in Bynder.
     *
     * @param chunk Upload process data of the file being uploaded.
     * @return {@link Observable} with Integer indicating the number of bytes that were uploaded
     * in the current chunk.
     */
    private Single<Indexed<byte[]>> uploadChunk(
            final Indexed<byte[]> chunk
    ) {
        int chunkNumber = chunk.getIndex();

        return amazonS3Service.uploadPartToAmazon(
                chunk,
                uploadQuery.getFilename(),
                numberOfChunks,
                uploadRequest.getMultipartParams()
        ).andThen(registerChunk(
                new RegisterChunkQuery(
                        chunkNumber,
                        uploadRequest.getS3File().getUploadId(),
                        uploadRequest.getS3File().getTargetId(),
                        String.format(
                                "%s/p%s",
                                uploadRequest.getS3Filename(),
                                chunkNumber
                        )
                )
        )).toSingle(() -> chunk);
    }

    /**
     * Check {@link BynderApi#getClosestS3Endpoint} for more information.
     */
    private Single<String> getClosestS3Endpoint() {
        return Utils.handleRequest(bynderApi.getClosestS3Endpoint()).map(Response::body);
    }

    /**
     * Check {@link BynderApi#initUpload} for more information.
     */
    private Single<UploadRequest> initUpload() {
        return Utils.handleRequest(bynderApi.initUpload(
                queryDecoder.decode(new RequestUploadQuery(uploadQuery.getFilename()))
        )).map(Response::body);
    }

    /**
     * Check {@link BynderApi#registerChunk} for more information.
     */
    private Completable registerChunk(final RegisterChunkQuery registerChunkQuery) {
        return Utils.handleRequest(bynderApi.registerChunk(
                queryDecoder.decode(registerChunkQuery)
        )).ignoreElement();
    }

    /**
     * Check {@link BynderApi#finaliseS3Upload} for more information.
     */
    private Single<String> finalizeUpload() {
        return Utils.handleRequest(bynderApi.finaliseS3Upload(
                queryDecoder.decode(new FinaliseS3UploadQuery(uploadRequest, numberOfChunks))
        )).map(Response::body).map(FinaliseS3Response::getImportId);
    }

//    /**
//     * Method to check if file has finished converting within expected timeout.
//     *
//     * @param importId Import id of the upload.
//     * @return {@link Observable} with a Boolean indicating whether the file finished converting
//     * successfully.
//     */
//    private Single<String> checkProcessingFinished(final String importId) {
//        return pollProcessingStatus(importId)
//                .retryWhen(f -> f
//                        .take(MAX_POLLING_ITERATIONS)
//                        .delay(POLLING_IDLE_TIME, TimeUnit.MILLISECONDS)
//                );
//    }

    /**
     * Check {@link BynderApi#getPollStatus} for more information.
     */
    private Single<String> pollImageConversion(final String importId) {
        return Single.create(emitter -> Utils.handleRequest(bynderApi.getPollStatus(
                queryDecoder.decode(new PollStatusQuery(importId.split(",")))
        )).map(Response::body).retryWhen(f -> f
                .take(MAX_POLLING_ITERATIONS)
                .delay(POLLING_IDLE_TIME, TimeUnit.MILLISECONDS)
        ).subscribe(
                pollStatus -> {
                    if (pollStatus.getItemsFailed().contains(importId)) {
                        // Processing failed.
                        emitter.onError(new BynderUploadException("Upload failed."));
                    } else if (pollStatus.getItemsDone().contains(importId)) {
                        // Processing is done.
                        emitter.onSuccess(importId);
                    }
                },
                emitter::onError
        ));
    }

}
