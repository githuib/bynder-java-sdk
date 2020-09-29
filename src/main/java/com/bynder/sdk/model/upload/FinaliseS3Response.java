/*
 * Copyright (c) 2017 Bynder B.V. All rights reserved.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */
package com.bynder.sdk.model.upload;

import com.bynder.sdk.api.BynderApi;

import java.util.Map;

/**
 * Model returned by {@link BynderApi#finaliseS3Upload(Map)}.
 */
public class FinaliseS3Response {

    /**
     * Import id of the upload. Needed to poll and save media.
     */
    private String importId;

    public String getImportId() {
        return importId;
    }
}
