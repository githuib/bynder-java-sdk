/*
 * Copyright (c) 2019 Bynder B.V. All rights reserved.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */
package com.bynder.sdk.api;

import com.bynder.sdk.configuration.Configuration;
import com.bynder.sdk.configuration.HttpConnectionSettings;
import com.bynder.sdk.exception.BynderRuntimeException;
import com.bynder.sdk.model.oauth.Token;
import com.bynder.sdk.service.BynderClient;
import com.bynder.sdk.service.oauth.OAuthService;
import com.bynder.sdk.util.BooleanTypeAdapter;
import com.bynder.sdk.util.StringConverterFactory;
import com.bynder.sdk.util.Utils;
import com.google.gson.GsonBuilder;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Factory to create API clients.
 */
public class ApiFactory {

    /**
     * Prevents the instantiation of the class.
     */
    private ApiFactory() {}

    /**
     * Creates an implementation of the Bynder API endpoints defined in the {@link BynderApi}
     * interface.
     *
     * @param configuration {@link Configuration} settings for the HTTP communication with Bynder.
     * @return Implementation instance of the {@link BynderApi} interface.
     */
    public static BynderApi createBynderClient(final Configuration configuration) {
        GsonBuilder gsonBuilder = new GsonBuilder().registerTypeAdapter(Boolean.class, new BooleanTypeAdapter());

        return new Retrofit.Builder()
                .baseUrl(configuration.getBaseUrl().toString())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(new StringConverterFactory())
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                .client(createOkHttpClient(configuration))
                .build()
                .create(BynderApi.class);
    }

    /**
     * Creates an implementation of the Bynder OAuth2 endpoints defined in the {@link OAuthApi}
     * interface.
     *
     * @param bucket AWS bucket URL.
     * @return Implementation instance of the {@link OAuthApi} interface.
     */
    public static AmazonS3Api createAmazonS3Client(final String bucket) {

        return new Retrofit.Builder()
                .baseUrl(bucket)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(AmazonS3Api.class);
    }

    /**
     * Creates an implementation of the Amazon S3 endpoints defined in the {@link AmazonS3Api}
     * interface.
     *
     * @param baseUrl Bynder portal base URL.
     * @return Implementation instance of the {@link OAuthApi} interface.
     */
    public static OAuthApi createOAuthClient(final String baseUrl) {

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OAuthApi.class);
    }

    /**
     * Creates an instance of {@link OkHttpClient}.
     *
     * @param configuration Configuration settings for the HTTP communication with Bynder.
     * @return {@link OkHttpClient} instance used for API requests.
     */
    private static OkHttpClient createOkHttpClient(final Configuration configuration) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();

        if (configuration.getPermanentToken() == null) {
            setOAuthInterceptor(httpClientBuilder, configuration);
        } else {
            setPermanentTokenInterceptor(httpClientBuilder, configuration);
        }
//        httpClientBuilder.addInterceptor(new Interceptor() {
//            @Override
//            public Response intercept(final Chain chain) throws IOException {
//                return chain.proceed(chain.request().newBuilder().build());
//            }
//        });

        setHttpConnectionSettings(httpClientBuilder, configuration.getHttpConnectionSettings());

        return httpClientBuilder.build();
    }

    /**
     * Sets the OAuth interceptor for the HTTP client. This interceptor will handle adding the
     * access token to the request header and refreshing it when it expires.
     *
     * @param httpClientBuilder Builder instance of the HTTP client.
     * @param configuration {@link Configuration} settings for the HTTP communication with Bynder.
     */
    private static void setOAuthInterceptor(final Builder httpClientBuilder,
        final Configuration configuration) {
        httpClientBuilder.addInterceptor(chain -> {
            if (configuration.getOAuthSettings().getToken() == null) {
                throw new BynderRuntimeException("Token is not defined in Configuration");
            }

            // check if access token is expiring in the next 15 seconds
            if (Utils.isDateExpiring(configuration.getOAuthSettings().getToken().getAccessTokenExpiration(), 15)) {
                // refresh the access token
                OAuthService oAuthService = BynderClient.Builder.create(configuration).getOAuthService();
                Token token = oAuthService.refreshAccessToken().blockingSingle();

                // trigger callback method
                configuration.getOAuthSettings().callback(token);
            }

            Request.Builder requestBuilder = chain.request().newBuilder().header(
                    "Authorization",
                    String.format("%s %s", "Bearer", configuration.getOAuthSettings().getToken().getAccessToken())
            );

            return chain.proceed(requestBuilder.build());
        });
    }

    /**
     * Sets the permanent token interceptor for the HTTP client. This interceptor will handle adding
     * the permanent toekn to the request header.
     *
     * @param httpClientBuilder Builder instance of the HTTP client.
     * @param configuration {@link Configuration} settings for the HTTP communication with Bynder.
    */
    private static void setPermanentTokenInterceptor(final Builder httpClientBuilder,
        final Configuration configuration) {
        httpClientBuilder.addInterceptor(chain -> {

            Request.Builder requestBuilder = chain.request().newBuilder().header(
                    "Authorization",
                    String.format("%s %s", "Bearer", configuration.getPermanentToken())
            );

            return chain.proceed(requestBuilder.build());
        });
    }

    /**
     * Sets the HTTP connection settings for the HTTP client.
     *
     * @param httpClientBuilder Builder instance of the HTTP client.
     * @param httpConnectionSettings HTTP connection settings for the HTTP communication with
     * Bynder.
     */
    private static void setHttpConnectionSettings(
            final Builder httpClientBuilder,
            final HttpConnectionSettings httpConnectionSettings
    ) {
        if (httpConnectionSettings.isLoggingInterceptorEnabled()) {
            httpClientBuilder.addInterceptor(
                    new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            );
        }

        if (httpConnectionSettings.getCustomInterceptor() != null) {
            httpClientBuilder.addInterceptor(httpConnectionSettings.getCustomInterceptor());
        }

        httpClientBuilder
                .retryOnConnectionFailure(httpConnectionSettings.isRetryOnConnectionFailure())
                .readTimeout(httpConnectionSettings.getReadTimeoutSeconds(), TimeUnit.SECONDS)
                .connectTimeout(httpConnectionSettings.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
                .writeTimeout(httpConnectionSettings.getConnectTimeoutSeconds(), TimeUnit.SECONDS);

        if (httpConnectionSettings.getSslContext() != null
                && httpConnectionSettings.getTrustManager() != null) {
            httpClientBuilder.sslSocketFactory(
                    httpConnectionSettings.getSslContext().getSocketFactory(),
                    httpConnectionSettings.getTrustManager()
            );
        }
    }
}