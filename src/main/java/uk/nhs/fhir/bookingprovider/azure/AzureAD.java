/*
 * Copyright 2019 NHS Digital.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.nhs.fhir.bookingprovider.azure;

import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author tim.coates@nhs.net
 */
public class AzureAD {

    /**
     * The Logger object we use acros this class.
     */
    private static final Logger LOG = Logger.getLogger(AzureAD.class.getName());

    /**
     * The root URL for making queries to Azure.
     */
    private final String rootURL = "https://graph.windows.net/";

    /**
     * The tenant ID (currently mine).
     */
    private final String tenant = "e52111c7-4048-4f34-aea9-6326afa44a8d";

    /**
     * Method to retrieve the name of an Azure AD Group by calling Azure.
     *
     * @param groupID The GUID (objectId) of a group.
     * @return The name (displayName) of that Group
     */
    public final String getGroupName(final String groupID) {

        String groupName = null;
        StringBuilder makeURL = new StringBuilder();
        makeURL.append(rootURL);
        makeURL.append(tenant);
        makeURL.append("/groups?api-version=1.6");
        String url = makeURL.toString();

        String token = getToken();

        if (token != null) {

            try {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("cache-control", "no-cache")
                        .addHeader("Authorization", "Bearer " + token)
                        .build();

                Response response = client.newCall(request).execute();
                String responseStr = response.body().string();
                groupName = groupNameFromJSON(groupID, responseStr);
            }
            catch (IOException ex) {
                LOG.severe(ex.getMessage());
            }
        }
        return groupName;
    }

    /**
     * Method to retrieve the description of an Azure AD Group by calling Azure.
     *
     * @param groupID The GUID (objectId) of a group.
     * @return The name (description) of that Group
     */
    public final String getGroupDesc(final String groupID) {

        String groupDescription = null;
        StringBuilder makeURL = new StringBuilder();
        makeURL.append(rootURL);
        makeURL.append(tenant);
        makeURL.append("/groups?api-version=1.6");
        String url = makeURL.toString();
        String token = getToken();

        if (token != null) {

            try {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("cache-control", "no-cache")
                        .addHeader("Authorization", "Bearer " + token)
                        .build();

                Response response = client.newCall(request).execute();
                String responseStr = response.body().string();
                groupDescription = groupDescFromJSON(groupID, responseStr);
            }
            catch (IOException ex) {
                LOG.severe(ex.getMessage());
            }
        }
        return groupDescription;
    }

    /**
     * Method to retrieve the name of an Application by calling Azure.
     *
     * @param appID The GUID (appID) of a registered application.
     * @return The displayName of the application if found.
     */
    public final String getAppName(final String appID) {
        String groupName = null;
        StringBuilder makeURL = new StringBuilder();
        makeURL.append(rootURL);
        makeURL.append(tenant);
        makeURL.append("/applications?api-version=1.6");
        String url = makeURL.toString();

        String token = getToken();

        if (token != null) {

            try {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("cache-control", "no-cache")
                        .addHeader("Authorization", "Bearer " + token)
                        .build();

                Response response = client.newCall(request).execute();
                String responseStr = response.body().string();
                groupName = appNameFromJSON(appID, responseStr);
            }
            catch (IOException ex) {
                LOG.severe(ex.getMessage());
            }
        }
        return groupName;
    }

    /**
     * Method to get an access token from Azure to query Azure AD.
     *
     * @return The access_token as a String
     */
    public final String getToken() {

        String token = null;

        String clientId = "92d85f9d-0666-49bc-a31c-12b45b04a7de";
        String clientSecret = "d/pj5x2kQsCnAZQTEDJ5tE4upeN6hLR1EE+aVnraAJg=";
        clientSecret = "y7rCysA8anvYshLckwwFNs8qnC6JPyCerE7CUAAnGgo=";
        String resource = "https%3A%2F%2Fgraph.windows.net";
        String mediatype = "application/x-www-form-urlencoded";
        String tenantName = "timcoatesgmail";
        String url = "https://login.microsoftonline.com/";
        url = url + tenantName;
        url = url + ".onmicrosoft.com/oauth2/token";

        try {
            OkHttpClient client = new OkHttpClient();

            StringBuilder fields = new StringBuilder();
            fields.append("grant_type=client_credentials");
            fields.append("&client_id=");
            fields.append(clientId);
            fields.append("&client_secret=");
            fields.append(clientSecret);
            fields.append("&resource=");
            fields.append(resource);

            MediaType mediaType = MediaType.parse(mediatype);
            RequestBody body = RequestBody.create(mediaType, fields.toString());
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", mediatype)
                    .addHeader("cache-control", "no-cache")
                    .build();

            Response response = client.newCall(request).execute();
            String responseStr = response.body().string();
            Gson gson = new Gson();
            TokenResponse responseObject;
            responseObject = gson.fromJson(responseStr, TokenResponse.class);
            token = responseObject.getAccess_token();
            //LOG.info(token);
            return token;
        }
        catch (IOException ex) {
            LOG.severe(ex.getMessage());
        }
        return token;
    }

    /**
     * Pulls out the matching displayName field for a Group found based on the
     * supplied objectID value.
     *
     * @param groupID The objectID of a Group in a response from Azure.
     * @param json The entire JSON Response from Azure.
     * @return The object's displayName
     */
    public final String groupNameFromJSON(final String groupID, final String json) {
        String name = null;
        Gson gson = new Gson();
        GroupResponse responseObject;
        responseObject = gson.fromJson(json, GroupResponse.class);
        for (GroupResponse.GroupResponseValue value : responseObject.value) {
            if (value.objectId.equals(groupID)) {
                name = value.displayName;
            }
        }
        return name;
    }

    /**
     * Pulls out the matching description field for a Group found based on the
     * supplied objectID value.
     *
     * @param groupID The objectID of a Group in a response from Azure.
     * @param json The entire JSON Response from Azure.
     * @return The object's description
     */
    public final String groupDescFromJSON(final String groupID, final String json) {
        String description = null;
        Gson gson = new Gson();
        GroupResponse responseObject;
        responseObject = gson.fromJson(json, GroupResponse.class);
        for (GroupResponse.GroupResponseValue value : responseObject.value) {
            if (value.objectId.equals(groupID)) {
                description = value.description;
            }
        }

        return description;
    }

    /**
     * Pulls out the matching displayName field for an App found based on the
     * supplied objectID value.
     *
     * @param groupID The objectID of an App registered in AzureAD.
     * @param json The entire JSON Response from Azure.
     * @return The object's displayName
     */
    public final String appNameFromJSON(final String appID, final String json) {
        String name = null;
        Gson gson = new Gson();
        AppsResponse responseObject;
        responseObject = gson.fromJson(json, AppsResponse.class);
        for (AppsResponse.AppResponseValue value : responseObject.value) {
            if (value.appId.equals(appID)) {
                name = value.displayName;
            }
        }
        return name;
    }

    /**
     * Class to represent what comes back in the Token response, to allow GSON
     * to deserialise the token into a POJO.
     */
    private class TokenResponse {

        private String token_type;

        private long expires_in;
        private long ext_expires_in;
        private long expires_on;
        private long not_before;
        private String resource;
        private String access_token;

        public String getToken_type() {
            return token_type;
        }

        public void setToken_type(String token_type) {
            this.token_type = token_type;
        }

        public long getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(long expires_in) {
            this.expires_in = expires_in;
        }

        public long getExt_expires_in() {
            return ext_expires_in;
        }

        public void setExt_expires_in(long ext_expires_in) {
            this.ext_expires_in = ext_expires_in;
        }

        public long getExpires_on() {
            return expires_on;
        }

        public void setExpires_on(long expires_on) {
            this.expires_on = expires_on;
        }

        public long getNot_before() {
            return not_before;
        }

        public void setNot_before(long not_before) {
            this.not_before = not_before;
        }

        public String getResource() {
            return resource;
        }

        public void setResource(String resource) {
            this.resource = resource;
        }

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

    }

    /**
     * Inner class to represent the response.
     */
    private class GroupResponse {

        String odata_metadata;
        GroupResponseValue[] value;

        /**
         * Nested inner class to represent the groups returned in an array.
         */
        private class GroupResponseValue {

            String odata_type;
            String objectType;
            String objectId;
            String deletionTimestamp;
            String description;
            String dirSyncEnabled;
            String displayName;
            String lastDirSyncTime;
            String mail;
            String mailNickname;
            boolean mailEnabled;
            String onPremisesDomainName;
            String onPremisesNetBiosName;
            String onPremisesSamAccountName;
            String onPremisesSecurityIdentifier;
            String[] provisioningErrors;
            String[] proxyAddresses;
            boolean securityEnabled;
        }
    }

    /**
     * Inner class to represent the response.
     */
    private class AppsResponse {

        String odata_metadata;
        AppResponseValue[] value;

        /**
         * Nested inner class to represent the value array we get back.
         */
        private class AppResponseValue {

            String odata_type;
            String objectType;
            String objectId;
            String deletionTimestamp;
            boolean acceptMappedClaims;
            Object[] addIns;
            String appId;
            Object[] appRoles;
            boolean availableToOtherTenants;
            String displayName;
            String errorUrl;
            String groupMembershipClaims;
            String homepage;
            String[] identifierUris;
            Object informationalUrls;
            boolean isDeviceOnlyAuthSupported;
            String[] keyCredentials;
            String[] knownClientApplications;
            String logoutUrl;
            String logo_odata_mediaEditLink;
            String logoUrl;
            String mainLogo_odata_mediaEditLink;
            boolean oauth2AllowIdTokenImplicitFlow;
            boolean oauth2AllowImplicitFlow;
            boolean oauth2AllowUrlPathMatching;
            Object[] oauth2Permissions;
            boolean oauth2RequirePostResponse;
            Object optionalClaims;
            String[] orgRestrictions;
            Object parentalControlSettings;
            Object[] passwordCredentials;
            boolean publicClient;
            String publisherDomain;
            String recordConsentConditions;
            String[] replyUrls;
            Object[] requiredResourceAccess;
            String samlMetadataUrl;
            String signInAudience;
            String tokenEncryptionKeyId;
        }
    }
}
