/*
 * Copyright 2018 NHS Digital.
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
package uk.nhs.fhir.bookingprovider;

import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tim.coates@nhs.net
 */
public class RequestInterceptorTest {

    private static final Logger LOG = Logger.getLogger(RequestInterceptorTest.class.getName());

    public RequestInterceptorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of validateToken method, of class RequestInterceptor.
     */
    @Test
    public void testValidateToken() {
        System.out.println("validateToken");
        String token = getToken();
        String reqURI = "http://appointments.directoryofservices.nhs.uk/poc";
        RequestInterceptor instance = new RequestInterceptor();
        boolean expResult = true;
        boolean result = instance.validateToken(token, reqURI);
        assertEquals(expResult, result);
    }

    /**
     * Test of validateToken method, of class RequestInterceptor.
     */
    @Test
    public void testValidateTokenExtraGroups() {
        System.out.println("testValidateTokenExtraGroups");
        String token = getTokenExtraGroup();
        String reqURI = "http://appointments.directoryofservices.nhs.uk/poc";
        RequestInterceptor instance = new RequestInterceptor();
        boolean expResult = true;
        boolean result = instance.validateToken(token, reqURI);
        assertEquals(expResult, result);
    }

    /**
     * Test of validateToken method, of class RequestInterceptor.
     */
    @Test
    public void testValidateBadToken() {
        System.out.println("validateToken");
        String token = getToken();
        String reqURI = "http://localhost:443/poc";
        RequestInterceptor instance = new RequestInterceptor();
        String expResult = "The supplied JWT was not intended for: " + reqURI;
        try {
            boolean result = instance.validateToken(token, reqURI);
        }
        catch (UnprocessableEntityException ex) {
            assertEquals(expResult, ex.getMessage());
        }

    }

    private String getToken() {
        String token = null;

        try {

            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "client_id=0f7bc08b-3395-4b4b-b23b-f790fc62bf91&client_secret=mVBtuzRIwYhuDkSSFZIbp3Qmx7OWiL35BXreUpnBVb8%3D&grant_type=client_credentials&scope=http%3A%2F%2Fappointments.directoryofservices.nhs.uk%3A443%2Fpoc%2F.default");
            Request request = new Request.Builder()
                    .url("https://login.microsoftonline.com/e52111c7-4048-4f34-aea9-6326afa44a8d/oauth2/v2.0/token")
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("cache-control", "no-cache")
                    .build();

            Response response = client.newCall(request).execute();
            String responseStr = response.body().string();
            Gson gson = new Gson();
            TokenResponse responseObject = gson.fromJson(responseStr, TokenResponse.class);
            token = responseObject.access_token;
            //LOG.info(responseObject.access_token);

            return token;
        }
        catch (IOException ex) {
            LOG.severe(ex.getMessage());
        }
        return token;
    }

    private String getTokenExtraGroup() {
        String token = null;

        try {

            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "client_id=92d85f9d-0666-49bc-a31c-12b45b04a7de&client_secret=y7rCysA8anvYshLckwwFNs8qnC6JPyCerE7CUAAnGgo%3D&grant_type=client_credentials&scope=http%3A%2F%2Fappointments.directoryofservices.nhs.uk%3A443%2Fpoc%2F.default");
            Request request = new Request.Builder()
                    .url("https://login.microsoftonline.com/e52111c7-4048-4f34-aea9-6326afa44a8d/oauth2/v2.0/token")
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("cache-control", "no-cache")
                    .build();

            Response response = client.newCall(request).execute();
            String responseStr = response.body().string();
            Gson gson = new Gson();
            TokenResponse responseObject = gson.fromJson(responseStr, TokenResponse.class);
            token = responseObject.access_token;
            //LOG.info(responseObject.access_token);

            return token;
        }
        catch (IOException ex) {
            LOG.severe(ex.getMessage());
        }
        return token;
    }

    private class TokenResponse {

        private String token_type;

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

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }
        private long expires_in;
        private long ext_expires_in;
        private String access_token;
    }

    /**
     * Test of incomingRequestPreProcessed method, of class RequestInterceptor.
     */
    @Test
    public void testIncomingRequestPreProcessed() {
        System.out.println("incomingRequestPreProcessed");
        RequestInterceptor instance = new RequestInterceptor();
        String queryString = "http://appointments.directoryofservices.nhs.uk/poc";
        String token = getToken();
        String authheader = "Bearer " + token;
        HttpServletRequest myRequestMock = new MockRequest(queryString, authheader);
        HttpServletResponse responseMock = new MockResponse();

        boolean expResult = true;
        boolean result = instance.incomingRequestPreProcessed(myRequestMock, responseMock);
        assertEquals(expResult, result);

    }

}
