/*
 * Copyright (c) 2017 Bynder B.V. All rights reserved.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */
package com.bynder.sdk.util;

import com.bynder.sdk.exception.BynderRequestError;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Response;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

/**
 * Utils class that provides methods to help handling API requests and responses.
 */
public final class Utils {

    /**
     * Prevents the instantiation of the class.
     */
    private Utils() {
    }

    /**
     * Encodes a string into application/x-www-form-urlencoded format using a UTF-8 as encoding
     * scheme.
     *
     * @param value String to be encoded.
     * @return The encoded string value.
     * @throws UnsupportedEncodingException If the encoding scheme for the parameter is not
     * supported.
     */
    public static String encodeParameterValue(final String value)
        throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
    }

    /**
     * Checks if a date is expiring in a specified number of seconds.
     *
     * @param date String representing the date.
     * @param seconds Number of seconds desired for the check.
     * @return True if the date will expire in the number of seconds passed as parameter.
     */
    public static boolean isDateExpiring(final Date date, final int seconds) {
        Calendar expirationDate = Calendar.getInstance();
        expirationDate.setTime(date);
        expirationDate.add(Calendar.SECOND, -seconds);
        return expirationDate.before(Calendar.getInstance());
    }

    /**
     * Takes a SHA-256 hash of a byte array.
     *
     * @param bytes byte array to hash
     * @return SHA-256 hash of the content
     */
    public static String sha256Hex(byte[] bytes) {
        MessageDigest hasher;
        try {
            hasher = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // Should never happen
            return null;
        }
        StringBuilder hexString = new StringBuilder();
        for (byte b : hasher.digest(bytes)) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Takes a SHA-256 hash of a file.
     *
     * @param file file to hash
     * @return SHA-256 hash of the file content
     * @throws IOException when the file could not be read
     */
    public static String sha256Hex(File file)
            throws IOException {
        return sha256Hex(Files.readAllBytes(file.toPath()));
    }

    public static <T> Observable<Indexed<T>> mapWithIndex(Observable<T> observable) {
        return mapWithIndex(observable, 0);
    }

    public static <T> Observable<Indexed<T>> mapWithIndex(Observable<T> observable, int startFrom) {
        return observable.zipWith(
                Observable.range(startFrom, Integer.MAX_VALUE),
                Indexed::new
        );
    }

//    public static <T> Flowable<Indexed<T>> mapWithIndex(Flowable<T> flowable) {
//        return mapWithIndex(flowable, 0);
//    }
//
//    public static <T> Flowable<Indexed<T>> mapWithIndex(Flowable<T> flowable, int startFrom) {
//        return flowable.zipWith(Flowable.range(startFrom, Integer.MAX_VALUE), Indexed::new);
//    }

    public static <T> Single<Response<T>> handleRequest(Single<Response<T>> request) {
        return Single.create(emitter -> request.subscribe(response -> {
            if (!response.isSuccessful()) {
                emitter.onError(new BynderRequestError(response));
            }
            emitter.onSuccess(response);
        }, emitter::onError));
    }

    public static <T> Single<Response<T>> handleRequest(Observable<Response<T>> request) {
        return handleRequest(request.singleOrError());
    }

}
