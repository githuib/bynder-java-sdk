package com.bynder.sdk.query.upload;

import com.bynder.sdk.query.decoder.ApiField;

public class FinaliseUploadParams {

    @ApiField
    private final int chunksCount;

    @ApiField
    private final String fileName;

    @ApiField
    private final long fileSize;

    @ApiField
    private final String sha256;

    @ApiField
    private final UploadIntent intent; // or does this default to upload_via_api anyway?

    public FinaliseUploadParams(
            final int chunksCount,
            final String fileName,
            final long fileSize,
            final String sha256,
            final UploadIntent intent
    ) {
        this.chunksCount = chunksCount;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.sha256 = sha256;
        this.intent = intent;
    }

}
