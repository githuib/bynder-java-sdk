package com.bynder.sdk.exception;

import retrofit2.Response;

/**
 * Exception thrown when an error occurs during file upload.
 */
public class BynderRequestError extends Exception {

    private static final long serialVersionUID = 1L;

    private final Response<?> response;

    /**
     * Creates a new instance of the class.
     *
     * @param message Message explaining the exception.
     */
    public BynderRequestError(final Response<?> response) {
        super(response.message());
        this.response = response;
    }

    public Response<?> getResponse() {
        return response;
    }

}

