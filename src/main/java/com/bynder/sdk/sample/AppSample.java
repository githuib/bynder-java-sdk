/*
 * Copyright (c) 2019 Bynder B.V. All rights reserved.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */
package com.bynder.sdk.sample;

import com.bynder.sdk.configuration.Configuration;
import com.bynder.sdk.configuration.OAuthSettings;
import com.bynder.sdk.exception.BynderRequestError;
import com.bynder.sdk.model.Brand;
import com.bynder.sdk.model.Derivative;
import com.bynder.sdk.model.Media;
import com.bynder.sdk.model.MediaType;
import com.bynder.sdk.model.oauth.RefreshTokenCallback;
import com.bynder.sdk.model.oauth.Token;
import com.bynder.sdk.model.upload.SaveMediaResponse;
import com.bynder.sdk.query.MediaQuery;
import com.bynder.sdk.query.OrderBy;
import com.bynder.sdk.query.upload.UploadQuery;
import com.bynder.sdk.service.BynderClient;
import com.bynder.sdk.service.asset.AssetService;
import com.bynder.sdk.service.oauth.OAuthService;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

import java.awt.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Sample class to display some of the SDK functionality.
 */
public class AppSample {

    private static final List<String> OAUTH_SCOPES = Arrays.asList("offline", "asset:read", "asset:write");
    private static final String UPLOAD_PATH = "/Users/huibpiguillet/Downloads/rhino.jpg";

    private static final Logger LOG = LoggerFactory.getLogger(AppSample.class);
    private static final AppProperties appProperties = new AppProperties();
    private static BynderClient client;

    public static void main(final String[] args) {
        try {
            authenticateWithOAuth2();
//            authenticateWithPermanentToken();
//            authenticateWithDummy();

            queryBynder();
        } catch (Throwable e) {
            LOG.error("Something went wrong.", e);
        }
    }

    private static void queryBynder() throws IOException {
        AssetService assetService = client.getAssetService();

//        // Call the API to request for the account information
//        List<Derivative> derivatives = client.getDerivatives().blockingSingle().body();
//        for (Derivative derivative : derivatives) {
//            LOG.info(derivative.getPrefix());
//        }
//
//        // Call the API to request for brands
//        List<Brand> brands = assetService.getBrands().blockingSingle().body();
//        for (Brand brand : brands) {
//            LOG.info(brand.getName());
//        }

//        // Call the API to request for media assets
//        List<Media> mediaList = assetService.getMediaList(
//                new MediaQuery().setType(MediaType.IMAGE).setOrderBy(OrderBy.NAME_DESC).setLimit(10)
//                        .setPage(1)).blockingSingle().body();
//        for (Media media : mediaList) {
//            LOG.info(media.getName());
//        }

        List<Brand> brands = assetService.getBrands().blockingSingle().body();
        if (brands.isEmpty()) {
            return;
        }

        SaveMediaResponse saveMediaResponse = assetService.uploadFile(
                new UploadQuery(UPLOAD_PATH, brands.get(0).getId())
        ).blockingGet();

        LOG.info(saveMediaResponse.toString());
    }

    private static void authenticateWithDummy() throws MalformedURLException {
        // Initialize BynderClient with a permanent token
        client = BynderClient.Builder.create(
                new Configuration.Builder(new URL(appProperties.getProperty("BASE_URL")))
                        .build()
        );
    }

    private static void authenticateWithPermanentToken() throws MalformedURLException {
        // Initialize BynderClient with a permanent token
        client = BynderClient.Builder.create(
                new Configuration.Builder(new URL(appProperties.getProperty("BASE_URL")))
                        .setPermanentToken(appProperties.getProperty("PERMANENT_TOKEN"))
                        .build()
        );
    }

    private static void authenticateWithOAuth2() throws IOException, URISyntaxException {
        // Optional: define callback function to be triggered after access token is auto
        // refreshed
        RefreshTokenCallback callback = token -> {
            LOG.info("Auto refresh triggered!");
            LOG.info(String.format("Refresh token used: %s", token.getRefreshToken()));
            LOG.info(String.format("New access token: %s", token.getAccessToken()));
            LOG.info(String.format("New access token expiration date: %s", token.getAccessTokenExpiration()));
        };

        // Initialize BynderClient with oauth settings to perform OAuth 2.0
        // authorization flow
        client = BynderClient.Builder.create(
                new Configuration.Builder(new URL(appProperties.getProperty("BASE_URL")))
                        .setOAuthSettings(new OAuthSettings(
                                appProperties.getProperty("CLIENT_ID"),
                                appProperties.getProperty("CLIENT_SECRET"),
                                new URI(appProperties.getProperty("REDIRECT_URI")),
                                callback
                        ))
                        .build()
        );

        // Initialize OAuthService
        OAuthService oauthService = client.getOAuthService();

        URL authorizationUrl = oauthService.getAuthorizationUrl("state example", OAUTH_SCOPES);

        // Open browser with authorization URL
        Desktop desktop = Desktop.getDesktop();
        desktop.browse(authorizationUrl.toURI());

        // Ask for the code returned in the redirect URI
        System.out.println("Insert the code: ");
        Scanner scanner = new Scanner(System.in);
        String code = scanner.nextLine();
        scanner.close();

        // Get the access token
        oauthService.getAccessToken(code, OAUTH_SCOPES).blockingSingle();
    }

}