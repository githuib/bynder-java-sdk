package com.bynder.sdk.service.upload;

import com.bynder.sdk.api.BynderApi;
import com.bynder.sdk.model.upload.SaveMediaResponse;
import com.bynder.sdk.model.upload.UploadProgress;
import com.bynder.sdk.query.decoder.QueryDecoder;
import com.bynder.sdk.query.upload.FinaliseUploadParams;
import com.bynder.sdk.query.upload.UploadIntent;
import com.bynder.sdk.query.upload.UploadQuery;
import com.bynder.sdk.util.Indexed;
import com.bynder.sdk.util.Utils;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.*;
import okio.ByteString;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class FilesServiceFileUploader extends BaseFileUploader {

    /**
     * Creates a new instance of the class.
     *
     * @param bynderApi    Instance to handle the HTTP communication with the Bynder API.
     * @param queryDecoder Query decoder.
     * @param uploadQuery  Upload query with the information to upload the file.
     */
    public FilesServiceFileUploader(BynderApi bynderApi, QueryDecoder queryDecoder, UploadQuery uploadQuery) {
        super(bynderApi, queryDecoder, uploadQuery);
    }

    /**
     * Uploads a file with the information specified in the query parameter.
     *
     * @return {@link Observable} with the {@link SaveMediaResponse} information.
     */
    public Single<SaveMediaResponse> uploadFile()
            throws IOException {
        return uploadFileWithProgress().ignoreElements() // upload file chunks
                .andThen(finalizeUpload()) // finalise when all chunks are uploaded
                .map(correlationId -> new SaveMediaResponse());
//                .flatMap(saveMedia(String.valueOf(uploadQuery.getFileId()))); // save in asset bank
    }

    /**
     * Uploads a file with the information specified in the query parameter
     * while providing information on the progress of the upload via the Observable returned.
     *
     * @return {@link Observable} with the {@link UploadProgress} information.
     */
    public Observable<Indexed<byte[]>> uploadFileWithProgress()
            throws FileNotFoundException {
        return Utils.mapWithIndex(readChunks()) // read file into chunks
                .flatMapSingle(this::uploadChunk); // upload chunks through API
    }

    /**
     * Check {@link BynderApi#uploadChunk(String, UUID, int, RequestBody)} for more information.
     *
     * @param chunk
     * @return
     */
    private Single<Indexed<byte[]>> uploadChunk(Indexed<byte[]> chunk) {
        int chunkNumber = chunk.getIndex();
        byte[] bytes = chunk.getValue();

        return Utils.handleRequest(bynderApi.uploadChunk(
                Utils.sha256Hex(bytes),
                uploadQuery.getFileId(),
                chunkNumber,
                RequestBody.create(
                        MediaType.parse("application/octet-stream"),
                        bytes
                )
        )).ignoreElement().toSingle(() -> chunk);
    }

    /**
     * Check {@link BynderApi#finaliseUpload(UUID, Map)} for more information.
     *
     * @return
     * @throws IOException
     */
    private Single<String> finalizeUpload()
            throws IOException {
        return Utils.handleRequest(bynderApi.finaliseUpload(
                uploadQuery.getFileId(),
                queryDecoder.decode(new FinaliseUploadParams(
                        numberOfChunks,
                        uploadQuery.getFilename(),
                        file.length(),
                        Utils.sha256Hex(file),
                        UploadIntent.CREATE_ASSET
                ))
        )).map(response -> response.headers().get("X-API-Correlation-ID"));
    }

    private void uihihiu(final String correlationId) {
        Request req = new Request.Builder().url("/ws").build();
        OkHttpClient client = new OkHttpClient.Builder().build();
        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
            }
        };
        client.newWebSocket(req, listener);
    }

}
