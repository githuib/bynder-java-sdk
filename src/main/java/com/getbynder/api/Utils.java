package com.getbynder.api;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.getbynder.api.domain.ImageAsset;
import com.getbynder.api.domain.UserAccessData;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

/**
 *
 * @author daniel.sequeira
 */
public final class Utils {

    private Utils() {
        //prevent instantiation
    }

    public static URI createLoginURI(final URL url, final String relativePath, final List<BasicNameValuePair> params) throws URISyntaxException {

        URIBuilder builder = new URIBuilder();

        builder.setScheme(url.getProtocol()).setHost(url.getHost()).setPath(url.getPath().concat(relativePath));

        for(BasicNameValuePair pair : params) {
            builder.setParameter(pair.getName(), pair.getValue());
        }

        return builder.build();
    }

    public static String getOAuthHeaderFromUrl(final URL url) {

        String query = url.getQuery();

        List<BasicNameValuePair> queryPairs = new ArrayList<>();

        String[] pairs = query.split("&");

        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            //the value needs to be decoded because the method OAuth.toHeaderElement will encode it again
            queryPairs.add(new BasicNameValuePair(pair.substring(0, idx), OAuth.percentDecode(pair.substring(idx + 1))));
        }

        StringBuilder oauthHeader = new StringBuilder("OAuth ");

        for(BasicNameValuePair nvPair : queryPairs) {
            oauthHeader.append(OAuth.toHeaderElement(nvPair.getName(), nvPair.getValue())).append(",");
        }

        oauthHeader.deleteCharAt(oauthHeader.length()-1);

        return oauthHeader.toString();
    }

    public static String createOAuthHeader(final String consumerKey, final String consumerSecret, final UserAccessData userAccessData, final String url) throws OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException, MalformedURLException {

        // create a consumer object and configure it with the access token and token secret obtained from the service provider
        OAuthConsumer consumer = new DefaultOAuthConsumer(consumerKey, consumerSecret);
        consumer.setTokenWithSecret(userAccessData.getTokenKey(), userAccessData.getTokenSecret());

        // sign the request and return the encoded url
        String inputUrl = consumer.sign(url);

        return getOAuthHeaderFromUrl(new URL(inputUrl));
    }

    public static List<ImageAsset> createImageAssetListFromJSONArray(final JSONArray responseJsonArray) {

        List<ImageAsset> bynderImageAssets = new ArrayList<>();

        String id = "";
        String title = "";
        String description = "";
        String url = "";
        String thumbnailUrl = "";

        for(int i = 0; i < responseJsonArray.length(); i++){
            JSONObject responseJsonObj = responseJsonArray.getJSONObject(i);

            id = responseJsonObj.get("id").toString();
            title = responseJsonObj.get("name").toString();
            description = responseJsonObj.get("description").toString();

            responseJsonObj = new JSONObject(responseJsonObj.getJSONObject("thumbnails").toString());

            url = responseJsonObj.get("webimage").toString();
            thumbnailUrl = responseJsonObj.get("thul").toString();

            bynderImageAssets.add(new ImageAsset(id, title, description, url, thumbnailUrl));
        }

        return bynderImageAssets;
    }
}
