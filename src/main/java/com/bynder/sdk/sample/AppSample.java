/*
 * Copyright (c) 2019 Bynder B.V. All rights reserved.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */
package com.bynder.sdk.sample;

import com.bynder.sdk.configuration.Configuration;
import com.bynder.sdk.configuration.OAuthSettings;
import com.bynder.sdk.model.*;
import com.bynder.sdk.model.oauth.Token;
import com.bynder.sdk.query.MediaDeleteQuery;
import com.bynder.sdk.query.MediaInfoQuery;
import com.bynder.sdk.query.MediaQuery;
import com.bynder.sdk.query.OrderBy;
import com.bynder.sdk.query.collection.CollectionOrderType;
import com.bynder.sdk.query.collection.CollectionQuery;
import com.bynder.sdk.query.upload.ExistingAssetUploadQuery;
import com.bynder.sdk.query.upload.NewAssetUploadQuery;
import com.bynder.sdk.service.BynderClient;
import com.bynder.sdk.service.asset.AssetService;
import com.bynder.sdk.service.collection.CollectionService;
import com.bynder.sdk.service.oauth.OAuthService;
import com.bynder.sdk.util.RXUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AppSample {

    public static void main(final String[] args)
            throws IOException, URISyntaxException {
        AppSample app = new AppSample(
<<<<<<< Updated upstream
                "https://example.com",
                "OAuth2 client ID",
                "Oauth2 client secret",
                "https://redirect_url/",
                token -> { // OAuth2 refresh token callback
                    LOG.info("Auto refresh triggered!");
                    LOG.info(String.format("Refresh token used: %s", token.getRefreshToken()));
                    LOG.info(String.format("New access token: %s", token.getAccessToken()));
                    LOG.info(String.format("New access token expiration date: %s", token.getAccessTokenExpiration()));
                }
=======
//                "https://example.com",
                "https://arrakis.bynder-stage.com",
//                "https://lorraine.getbynder.com",
                new OAuthSettings.Builder(
//                        "OAuth2 client ID",
//                        "Oauth2 client secret"
//                "e6e8d179-3450-42f1-9b41-9b3886e49eb3", // arrakis normal
//                "0a453934-e95f-48c0-94b9-cd809b796b95",
                    "0e4b1ef0-e62a-457c-8b19-55437044835d", // arrakis client creds
                    "724f6386-6f00-4283-b8d7-8e8975bf2415"
//                        "ea6e81fb-8fd1-488e-a92c-5d8aed74d8bb", // lorraine client creds
//                        "575c29c2-06b9-492d-91fe-33cdb64c995f"
//                        "26fac8fe-a39c-4fa5-8420-0b110ed99771", // lorraine normal
//                        "8cf11973-653c-4295-9bc0-b0979f47a8c7"
                )
//                        .setRedirectUri("https://redirect_url/") // Leave out for authentication with client credentials
                        .setScopes(OAUTH_SCOPES) // List of scopes to request to be granted to the access token.
//                        .setRefreshTokenCallback(token -> { // Optional callback method to be triggered when token is refreshed.
//                            LOG.info("Auto refresh triggered!");
//                            LOG.info(String.format("Refresh token used: %s", token.getRefreshToken()));
//                            LOG.info(String.format("New access token: %s", token.getAccessToken()));
//                            LOG.info(String.format("New access token expiration date: %s", token.getAccessTokenExpiration()));
//                        })
//                )
//                        .setRedirectUri("https://example.com/")
//                token -> { // OAuth2 refresh token callback
//                    LOG.info("Auto refresh triggered!");
//                    LOG.info(String.format("Refresh token used: %s", token.getRefreshToken()));
//                    LOG.info(String.format("New access token: %s", token.getAccessToken()));
//                    LOG.info(String.format("New access token expiration date: %s", token.getAccessTokenExpiration()));
//                }
                        .build()
>>>>>>> Stashed changes
        );
        app.listItems();
//        app.uploadFile("/path/to/file.ext");
    }

    private static final Logger LOG = LoggerFactory.getLogger(AppSample.class);
    private static final List<String> OAUTH_SCOPES = Arrays.asList("offline", "asset:read", "asset:write", "collection:read");

    private final BynderClient bynderClient;
    private final AssetService assetService;
    private final CollectionService collectionService;

    private AppSample(final Configuration configuration)
            throws IOException, URISyntaxException {
        bynderClient = BynderClient.Builder.create(configuration);
        assetService = bynderClient.getAssetService();
        collectionService = bynderClient.getCollectionService();
        authenticateWithOAuth2();
    }

    public AppSample(final String baseUrl, final OAuthSettings oAuthSettings)
            throws IOException, URISyntaxException {
        this(new Configuration.Builder(new URL(baseUrl))
                .setOAuthSettings(oAuthSettings)
                .build());
    }

    public AppSample(
            final String baseUrl,
            final String oAuthClientId,
            final String oAuthClientSecret,
            final String oAuthRedirectUri
    )
            throws IOException, URISyntaxException {
        this(
                baseUrl,
                new OAuthSettings(oAuthClientId, oAuthClientSecret, new URI(oAuthRedirectUri))
        );
    }

    public AppSample(
            final String baseUrl,
            final String oAuthClientId,
            final String oAuthClientSecret,
            final String oAuthRedirectUri,
            final Consumer<Token> oAuthRefreshTokenCallback
    )
            throws IOException, URISyntaxException {
        this(
                baseUrl,
                new OAuthSettings(
                        oAuthClientId,
                        oAuthClientSecret,
                        new URI(oAuthRedirectUri),
                        oAuthRefreshTokenCallback::accept
                )
        );
    }


    private void logError(final Throwable e) {
        LOG.error(e.getMessage());
    }

    public void listItems() {
        RXUtils.handleResponseBody(
                bynderClient.getDerivatives()
        ).subscribe(
                derivatives -> LOG.info("Derivatives: " + derivatives.stream().map(Derivative::getPrefix).collect(Collectors.toList())),
                this::logError
        ).dispose();

        RXUtils.handleResponseBody(
                assetService.getBrands()
        ).subscribe(
                brands -> LOG.info("Brands: " + brands.stream().map(Brand::getName).collect(Collectors.toList())),
                this::logError
        ).dispose();

        RXUtils.handleResponseBody(
                assetService.getMediaList(new MediaQuery()
                        .setType(MediaType.IMAGE)
                        .setOrderBy(OrderBy.DATE_CREATED_DESC)
                        .setLimit(10)
                        .setPage(1)
                )
        ).subscribe(
                assets -> LOG.info("Assets: " + assets.stream().map(Media::getName).collect(Collectors.toList())),
                this::logError
        ).dispose();

        RXUtils.handleResponseBody(
                collectionService.getCollections(new CollectionQuery()
                        .setKeyword("")
                        .setOrderBy(CollectionOrderType.DATE_CREATED_DESC)
                        .setLimit(10)
                        .setPage(1)
                )
        ).subscribe(
                collections -> LOG.info("Collections: " + collections.stream().map(Collection::getName).collect(Collectors.toList())),
                this::logError
        ).dispose();
    }

    public void uploadFile(final String uploadPath) {
        RXUtils.handleResponseBody(assetService.getBrands()).flatMap(brands ->
                assetService.uploadFile(
                        new NewAssetUploadQuery(uploadPath, brands.get(0).getId())
                )
        ).flatMap(saveMediaResponse -> {
            LOG.info("New asset successfully created: " + saveMediaResponse.getMediaId());
            return RXUtils.handleResponseBody(
                    assetService.getMediaInfo(new MediaInfoQuery(saveMediaResponse.getMediaId()).setVersions(true))
            ).retryWhen(f ->
                    f.take(5).delay(1000, TimeUnit.MILLISECONDS)
            ).doOnError(e ->
                    LOG.error("New asset could not be fetched after trying 5 times.")
            );
        }).flatMap(media -> {
            LOG.info("New asset could be fetched: " + media.getId() + " " + media.getName());
            return assetService.uploadFile(
                    new ExistingAssetUploadQuery(uploadPath, media.getId())
            );
        }).flatMap(saveMediaResponse -> {
            LOG.info("New asset version successfully created: " + saveMediaResponse.getMediaId());
            return RXUtils.handleResponseBody(
                    assetService.getMediaInfo(new MediaInfoQuery(saveMediaResponse.getMediaId()).setVersions(true))
            ).retryWhen(f ->
                    f.take(5).delay(1000, TimeUnit.MILLISECONDS)
            ).doOnError(e ->
                    LOG.error("New asset version could not be fetched after trying 5 times.")
            );
        }).flatMapCompletable(media -> {
            LOG.info("New asset version could be fetched: " + media.getId() + " " + media.getName());
            return RXUtils.handleResponse(
                    assetService.deleteMedia(new MediaDeleteQuery(media.getId()))
            );
        }).blockingAwait();
    }

    private void authenticateWithOAuth2()
            throws IOException, URISyntaxException {
        OAuthService oauthService = bynderClient.getOAuthService();

        // Open browser with authorization URL
        Desktop.getDesktop().browse(
                oauthService.getAuthorizationUrl("state example", OAUTH_SCOPES).toURI()
        );

        // Ask for the code returned in the redirect URI
        System.out.println("Insert the code: ");
        Scanner scanner = new Scanner(System.in);
        String code = scanner.nextLine();
        scanner.close();

        // Get the access token
        Token token = oauthService.getAccessToken(code, OAUTH_SCOPES).blockingSingle();
        LOG.info("OAuth token: " + token.getAccessToken());
    }

}
