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

import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
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
import uk.nhs.fhir.bookingprovider.logging.ExternalLogger;

/**
 * Class which holds all of the tests for our RequestInterceptor.
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
        ExternalLogger ourLogger = ExternalLogger.GetInstance();
        String reqURI = "http://appointments.directoryofservices.nhs.uk/poc";
        RequestInterceptor instance = new RequestInterceptor(ourLogger);
        String expResult = "TestClient";
        String result = instance.validateToken(token, reqURI);
        assertTrue(result.equals(expResult));
    }

    /**
     * Test of validateToken method, of class RequestInterceptor. Checks that
     * having Groups that can't be resolved is tolerated.
     */
    @Test
    public void testValidateTokenExtraGroups() {
        System.out.println("testValidateTokenExtraGroups");
        String token = getTokenExtraGroup();
        ExternalLogger ourLogger = ExternalLogger.GetInstance();
        String reqURI = "http://appointments.directoryofservices.nhs.uk/poc";
        RequestInterceptor instance = new RequestInterceptor(ourLogger);
        String expResult = "ConsumerDemo";
        String result = instance.validateToken(token, reqURI);
        assertTrue(result.equals(expResult));
    }

    /**
     * Test of validateToken method, of class RequestInterceptor.
     */
    @Test
    public void testValidateBadToken() {
        System.out.println("validateToken");
        String token = getToken();
        String reqURI = "http://localhost:443/poc";
        ExternalLogger ourLogger = ExternalLogger.GetInstance();
        RequestInterceptor instance = new RequestInterceptor(ourLogger);
        String expResult = "The supplied JWT was not intended for: " + reqURI;
        try {
            String result = instance.validateToken(token, reqURI);
        }
        catch (UnprocessableEntityException ex) {
            assertEquals(expResult, ex.getMessage());
        }

    }

    /**
     * Test using a token Generated:
     * 13:18 on 11th January 2019
     * Expires: Fri Jan 11 2019 14:18:07 GMT+0000
     *
     * NB: Bear in mind that we allow 450 seconds (7:30) grace period for both
     * the issued and expires times.
     * Should be rejected as not valid after that
     * time.
     *
     */
    @Test(expected = AuthenticationException.class)
    public void testValidateExpiredToken() {
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImllX3FXQ1hoWHh0MXpJRXN1NGM3YWNRVkduNCIsImtpZCI6ImllX3FXQ1hoWHh0MXpJRXN1NGM3YWNRVkduNCJ9.eyJhdWQiOiJodHRwOi8vYXBwb2ludG1lbnRzLmRpcmVjdG9yeW9mc2VydmljZXMubmhzLnVrOjQ0My9wb2MiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9lNTIxMTFjNy00MDQ4LTRmMzQtYWVhOS02MzI2YWZhNDRhOGQvIiwiaWF0IjoxNTY2NTQ5NDM0LCJuYmYiOjE1NjY1NDk0MzQsImV4cCI6MTU2NjU1MzMzNCwiYWlvIjoiNDJGZ1lMZ3crWVl5UjlUTGs3TlBsYmwwUzdwRUF3QT0iLCJhcHBpZCI6IjBmN2JjMDhiLTMzOTUtNGI0Yi1iMjNiLWY3OTBmYzYyYmY5MSIsImFwcGlkYWNyIjoiMSIsImdyb3VwcyI6WyJhYjQxMmZlOS0zZjY4LTQzNjgtOTgxMC05ZGMyNGQxNjU5YjEiLCJkYWNiODJjNS1hZWE4LTQ1MDktODg3Zi0yODEzMjQwNjJkZmQiXSwiaWRwIjoiaHR0cHM6Ly9zdHMud2luZG93cy5uZXQvZTUyMTExYzctNDA0OC00ZjM0LWFlYTktNjMyNmFmYTQ0YThkLyIsIm9pZCI6IjU3MmQ2OGQ0LTExOTctNGE4Ny05MzJjLTAwM2Q4ZTRhN2RhOCIsInN1YiI6IjU3MmQ2OGQ0LTExOTctNGE4Ny05MzJjLTAwM2Q4ZTRhN2RhOCIsInRpZCI6ImU1MjExMWM3LTQwNDgtNGYzNC1hZWE5LTYzMjZhZmE0NGE4ZCIsInV0aSI6Ii1QVW1Lb1BYQVVlUXQxcHFnaVkxQUEiLCJ2ZXIiOiIxLjAifQ.mr525t8S32jwNgOEj1q_FFtlEBcY5sxYfGJUXuV0aJkQlLzaABo_RtxSE5-lQ008Z3a_a8_3PWy38g4olz_Db9XRyz2dQP3cR8vJCs3xyeDl41u87VvfCSwW6Z3vgW7Cj1aWMDz3g_4ywY3t9fFMG23YC9de37bXgsJSEumApK28owMzWDBB8Ii8tziP8pVj69wSMTIX_IOWC7o42jzTynzm5nZpMUhGJT2_T_x_R2hrZ4Hp4UUSv2ZTIVFtArEpZib6Owg1jH3wGnASAHgd-XBRFnWp1V-1PPtWdyC2m9TC8Rz9niSJYVnZvQRnMFC3XpeenVyweYueOccLkyvdIQ";
        System.out.println("validateToken");
        String reqURI = "http://appointments.directoryofservices.nhs.uk/poc";
        ExternalLogger ourLogger = ExternalLogger.GetInstance();
        RequestInterceptor instance = new RequestInterceptor(ourLogger);
        /**
         * This WILL break, as the public key which should be retrieved and used
         * to validate the old token is cycled out of use.
         *
         * To fix, it, it's necessary to request a new Token, overwrite the
         * value of the variable "token" above with the token, and then wait
         * approximately an hour, allowing for the 450 second grace period we've
         * built in.
         *
        **/
        instance.validateToken(token, reqURI);
    }

    /**
     * Method to get an access_token as TestClient
     *
     * @return An access token
     */
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
            return token;
        }
        catch (IOException ex) {
            LOG.severe(ex.getMessage());
        }
        return token;
    }

    /**
     * Method to get a different token.
     *
     * @return An access token.
     */
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
            return token;
        }
        catch (IOException ex) {
            LOG.severe(ex.getMessage());
        }
        return token;
    }

    /**
     * Test of incomingRequestPreProcessed method, of class RequestInterceptor.
     */
    @Test
    public void testIncomingRequestPreProcessed() {
        System.out.println("incomingRequestPreProcessed");
        ExternalLogger ourLogger = ExternalLogger.GetInstance();
        RequestInterceptor instance = new RequestInterceptor(ourLogger);
        String queryString = "http://appointments.directoryofservices.nhs.uk/poc";
        String token = getToken();
        String authheader = "Bearer " + token;
        HttpServletRequest myRequestMock = new MockRequest(queryString, authheader);
        HttpServletResponse responseMock = new MockResponse();

        boolean expResult = true;
        boolean result = instance.incomingRequestPreProcessed(myRequestMock, responseMock);
        assertEquals(expResult, result);

    }

    /**
     * Inner class which we define to allow GSON to deserialise JSON into a
     * POJO Object
     *
     */
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
}
