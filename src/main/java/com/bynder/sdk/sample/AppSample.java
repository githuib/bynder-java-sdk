/*
 * Copyright (c) 2019 Bynder B.V. All rights reserved.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */
package com.bynder.sdk.sample;

import com.bynder.sdk.configuration.Configuration;
import com.bynder.sdk.configuration.OAuthSettings;
import com.bynder.sdk.model.Brand;
import com.bynder.sdk.model.Derivative;
import com.bynder.sdk.model.Media;
import com.bynder.sdk.model.MediaType;
import com.bynder.sdk.model.oauth.RefreshTokenCallback;
import com.bynder.sdk.query.MediaQuery;
import com.bynder.sdk.query.OrderBy;
import com.bynder.sdk.service.BynderClient;
import com.bynder.sdk.service.asset.AssetService;
import com.bynder.sdk.service.oauth.OAuthService;
import com.bynder.sdk.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * Sample class to display some of the SDK functionality.
 */
public class AppSample {

    private static final List<String> OAUTH_SCOPES = Arrays.asList("offline", "asset:read", "asset:write");

    private final Logger logger;
    private final Properties config;

    public static void main(final String[] args)
            throws IOException, URISyntaxException {
        new AppSample().queryBynder();
    }

    private AppSample()
            throws IOException, URISyntaxException {
        logger = LoggerFactory.getLogger(AppSample.class);
        config = Utils.loadConfig("app");
    }

    private void queryBynder()
            throws IOException, URISyntaxException {
        BynderClient client = authenticate();
        AssetService assetService = client.getAssetService();

        // Call the API to request for the account information
        for (Derivative derivative : client.getDerivatives().blockingSingle().body()) {
            logger.info(derivative.getPrefix());
        }

        // Call the API to request for brands
        for (Brand brand : assetService.getBrands().blockingSingle().body()) {
            logger.info(brand.getName());
        }

        // Call the API to request for media assets
        for (Media media : assetService.getMediaList(
                new MediaQuery()
                        .setType(MediaType.IMAGE)
                        .setOrderBy(OrderBy.NAME_DESC)
                        .setLimit(10)
                        .setPage(1)
        ).blockingSingle().body()) {
            logger.info(media.getName());
        }
    }

    private BynderClient authenticate()
            throws IOException, URISyntaxException {
        if (config.containsKey("PERMANENT_TOKEN")) {
            return authenticateWithPermanentToken();
        } else {
            return authenticateWithOAuth2();
        }
    }

    private BynderClient authenticateWithPermanentToken()
            throws MalformedURLException {
        // Initialize BynderClient with a permanent token
        return BynderClient.Builder.create(
                new Configuration.Builder(new URL(config.getProperty("BASE_URL")))
                        .setPermanentToken(config.getProperty("PERMANENT_TOKEN"))
                        .build()
        );
    }

    private BynderClient authenticateWithOAuth2()
            throws IOException, URISyntaxException {
        // Optional: define callback function to be triggered after access token is auto
        // refreshed
        RefreshTokenCallback callback = token -> {
            logger.info("Auto refresh triggered!");
            logger.info(String.format("Refresh token used: %s", token.getRefreshToken()));
            logger.info(String.format("New access token: %s", token.getAccessToken()));
            logger.info(String.format("New access token expiration date: %s", token.getAccessTokenExpiration()));
        };

        // Initialize BynderClient with oauth settings to perform OAuth 2.0
        // authorization flow
        BynderClient client = BynderClient.Builder.create(
                new Configuration.Builder(new URL(config.getProperty("BASE_URL")))
                        .setOAuthSettings(new OAuthSettings(
                                config.getProperty("CLIENT_ID"),
                                config.getProperty("CLIENT_SECRET"),
                                new URI(config.getProperty("REDIRECT_URI")),
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

        return client;
    }

}
