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
import uk.nhs.fhir.bookingprovider.logging.ExternalLogger;

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
     * Edit: Now token from: 14:19 on 15th April 2019
     * Expires: Mon Apr 15 2019 15:18:58 GMT+0100
     * 
     * Edit: Now token from: Tue May 07 2019 14:15:00 GMT+0100
     * Expires: Tue May 07 2019 15:20:00 GMT+0100
     * 
     * NB: Bear in mind that we allow 450 seconds (7:30) grace period for both
     * the issued and expires times.
     * Should be rejected as not valid after that
     * time.
     * 
     */
    @Test(expected = AuthenticationException.class)
    public void testValidateExpiredToken() {
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Im5iQ3dXMTF3M1hrQi14VWFYd0tSU0xqTUhHUSIsImtpZCI6Im5iQ3dXMTF3M1hrQi14VWFYd0tSU0xqTUhHUSJ9.eyJhdWQiOiJodHRwOi8vYXBwb2ludG1lbnRzLmRpcmVjdG9yeW9mc2VydmljZXMubmhzLnVrOjQ0My9wb2MiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9lNTIxMTFjNy00MDQ4LTRmMzQtYWVhOS02MzI2YWZhNDRhOGQvIiwiaWF0IjoxNTQ3MjEyMzg3LCJuYmYiOjE1NDcyMTIzODcsImV4cCI6MTU0NzIxNjI4NywiYWlvIjoiNDJKZ1lBajU2emtyMkQvbWN6YWY0TnA3QnpjMEFBQT0iLCJhcHBpZCI6IjBmN2JjMDhiLTMzOTUtNGI0Yi1iMjNiLWY3OTBmYzYyYmY5MSIsImFwcGlkYWNyIjoiMSIsImdyb3VwcyI6WyJhYjQxMmZlOS0zZjY4LTQzNjgtOTgxMC05ZGMyNGQxNjU5YjEiLCJkYWNiODJjNS1hZWE4LTQ1MDktODg3Zi0yODEzMjQwNjJkZmQiXSwiaWRwIjoiaHR0cHM6Ly9zdHMud2luZG93cy5uZXQvZTUyMTExYzctNDA0OC00ZjM0LWFlYTktNjMyNmFmYTQ0YThkLyIsIm9pZCI6IjU3MmQ2OGQ0LTExOTctNGE4Ny05MzJjLTAwM2Q4ZTRhN2RhOCIsInN1YiI6IjU3MmQ2OGQ0LTExOTctNGE4Ny05MzJjLTAwM2Q4ZTRhN2RhOCIsInRpZCI6ImU1MjExMWM3LTQwNDgtNGYzNC1hZWE5LTYzMjZhZmE0NGE4ZCIsInV0aSI6ImVidnpqYWRVU1VhYjhwYy0zcTZWQUEiLCJ2ZXIiOiIxLjAifQ.DsOrk8pvedRskimZf1VrTtvV2dUGfpVwIb-A8JRM8r-N9-vbko7YleLlKJJkHEJsss61lKGjuZJWSW1zSlWTn2JIE8HLJk6hUmSoh4fGyqceP3w3II93XHbfzhdMIRBDxVAkiGuI1QsrTDoK3_JJ2fUaMjpiFNIbuJ9PuGLHxTj_M4FGqWLh8kgFKiJdmae46-EQnW5yuaePmiFdpRmF6Nw6_1qQGYxlgCX5FjSGtpFbKY9LdhVgeQxO4aEx_P2zroLPyziNZgyBplsCMU0NFjqRvouWr-tJHpvICWKgeTCzmZieCaaE3DuBnyargb77bTqEKoY79vb_9euk_FPV8g";
        token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ii1zeE1KTUxDSURXTVRQdlp5SjZ0eC1DRHh3MCIsImtpZCI6Ii1zeE1KTUxDSURXTVRQdlp5SjZ0eC1DRHh3MCJ9.eyJhdWQiOiJodHRwOi8vYXBwb2ludG1lbnRzLmRpcmVjdG9yeW9mc2VydmljZXMubmhzLnVrOjQ0My9wb2MiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9lNTIxMTFjNy00MDQ4LTRmMzQtYWVhOS02MzI2YWZhNDRhOGQvIiwiaWF0IjoxNTUxNDI4NzU3LCJuYmYiOjE1NTE0Mjg3NTcsImV4cCI6MTU1MTQzMjY1NywiYWlvIjoiNDJKZ1lMQXpPWnl0ZnVSWnNjZm5ONi9FZWNvUEFnQT0iLCJhcHBpZCI6IjBmN2JjMDhiLTMzOTUtNGI0Yi1iMjNiLWY3OTBmYzYyYmY5MSIsImFwcGlkYWNyIjoiMSIsImdyb3VwcyI6WyJhYjQxMmZlOS0zZjY4LTQzNjgtOTgxMC05ZGMyNGQxNjU5YjEiLCJkYWNiODJjNS1hZWE4LTQ1MDktODg3Zi0yODEzMjQwNjJkZmQiXSwiaWRwIjoiaHR0cHM6Ly9zdHMud2luZG93cy5uZXQvZTUyMTExYzctNDA0OC00ZjM0LWFlYTktNjMyNmFmYTQ0YThkLyIsIm9pZCI6IjU3MmQ2OGQ0LTExOTctNGE4Ny05MzJjLTAwM2Q4ZTRhN2RhOCIsInN1YiI6IjU3MmQ2OGQ0LTExOTctNGE4Ny05MzJjLTAwM2Q4ZTRhN2RhOCIsInRpZCI6ImU1MjExMWM3LTQwNDgtNGYzNC1hZWE5LTYzMjZhZmE0NGE4ZCIsInV0aSI6IjYyNFU4M1dXSVVPUDlZR3l6eE1NQUEiLCJ2ZXIiOiIxLjAifQ.W43Uk4X2G8mrtNDjOPHQkE-cp3kJSETPJF-93fBx9xoc7aiAZthvEHT6z7FMumQM7_7p-yY2pNhr195DgLE5pvruwAIvWz9Ymp1_BW2eD6QjPVK0Y_fyf1RBrsucSz5XWbpsZZNFqgtFSXUb9Rwj9HuvoRpdOgxu1GfkAZ8hKb8VfFo6UT98RaAeq6FyTUr-OVD_1E6FhmcHWy2f17sx7kIApkTv8iUpEiYEy2_qeDZ4WbnXhSMBt786njFlGIaH-xgu3XCGStF3YnZYQFswu_1iVW6-OibwJhGuLjIJd9VzEmQw8olG9o2yH120dFhDcB2PkWyGVDCz7ngJM_Q8fg";
        token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ik4tbEMwbi05REFMcXdodUhZbkhRNjNHZUNYYyIsImtpZCI6Ik4tbEMwbi05REFMcXdodUhZbkhRNjNHZUNYYyJ9.eyJhdWQiOiJodHRwOi8vYXBwb2ludG1lbnRzLmRpcmVjdG9yeW9mc2VydmljZXMubmhzLnVrOjQ0My9wb2MiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9lNTIxMTFjNy00MDQ4LTRmMzQtYWVhOS02MzI2YWZhNDRhOGQvIiwiaWF0IjoxNTU1MzM0MDM4LCJuYmYiOjE1NTUzMzQwMzgsImV4cCI6MTU1NTMzNzkzOCwiYWlvIjoiNDJaZ1lEaTdubW5yZ21PbWlVeDdiaWxhT25GdUFnQT0iLCJhcHBpZCI6IjBmN2JjMDhiLTMzOTUtNGI0Yi1iMjNiLWY3OTBmYzYyYmY5MSIsImFwcGlkYWNyIjoiMSIsImdyb3VwcyI6WyJhYjQxMmZlOS0zZjY4LTQzNjgtOTgxMC05ZGMyNGQxNjU5YjEiLCJkYWNiODJjNS1hZWE4LTQ1MDktODg3Zi0yODEzMjQwNjJkZmQiXSwiaWRwIjoiaHR0cHM6Ly9zdHMud2luZG93cy5uZXQvZTUyMTExYzctNDA0OC00ZjM0LWFlYTktNjMyNmFmYTQ0YThkLyIsIm9pZCI6IjU3MmQ2OGQ0LTExOTctNGE4Ny05MzJjLTAwM2Q4ZTRhN2RhOCIsInN1YiI6IjU3MmQ2OGQ0LTExOTctNGE4Ny05MzJjLTAwM2Q4ZTRhN2RhOCIsInRpZCI6ImU1MjExMWM3LTQwNDgtNGYzNC1hZWE5LTYzMjZhZmE0NGE4ZCIsInV0aSI6IjRpcnVua1F5T0VhdGdEQUVmMHRBQUEiLCJ2ZXIiOiIxLjAifQ.rUAC8PTLJJJs5gMLrzQYNJSBvZXWRp2FGci2iNxW9tZn9gIpQAUe32pBl6DgDaF9b4Xn6_KFYcjk1gunseOjcwyyWf64HfF0GgycoqOiHHjjIcrWZjwr_zXGJAy9FiN3EDpPDuQUlXlLEyTTSNKQQDWuKGk6VL1RlbFHbbA27lFDE_Kmg-1BVZ_2zYFsGSQEOpt04PpQfwjPzqUIGCna8vq2qlI1GHW-UAdCCEa4OAvrJd34MwwL0eC-rpJgOeErRd0VMzz40xhpoRvRWXXAH-IfqXO1Nf0-0RVrY20P0JGBm5MHg3eXgOuxrgPQGxH5enS2S0nM25fK4o2P2h_6pg";
        token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6IkhCeGw5bUFlNmd4YXZDa2NvT1UyVEhzRE5hMCIsImtpZCI6IkhCeGw5bUFlNmd4YXZDa2NvT1UyVEhzRE5hMCJ9.eyJhdWQiOiJodHRwOi8vYXBwb2ludG1lbnRzLmRpcmVjdG9yeW9mc2VydmljZXMubmhzLnVrOjQ0My9wb2MiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9lNTIxMTFjNy00MDQ4LTRmMzQtYWVhOS02MzI2YWZhNDRhOGQvIiwiaWF0IjoxNTU3MjM0OTAwLCJuYmYiOjE1NTcyMzQ5MDAsImV4cCI6MTU1NzIzODgwMCwiYWlvIjoiNDJaZ1lMaTRjb2ZXYllFTkhwTUsxN2NKUlhkK0JRQT0iLCJhcHBpZCI6IjBmN2JjMDhiLTMzOTUtNGI0Yi1iMjNiLWY3OTBmYzYyYmY5MSIsImFwcGlkYWNyIjoiMSIsImdyb3VwcyI6WyJhYjQxMmZlOS0zZjY4LTQzNjgtOTgxMC05ZGMyNGQxNjU5YjEiLCJkYWNiODJjNS1hZWE4LTQ1MDktODg3Zi0yODEzMjQwNjJkZmQiXSwiaWRwIjoiaHR0cHM6Ly9zdHMud2luZG93cy5uZXQvZTUyMTExYzctNDA0OC00ZjM0LWFlYTktNjMyNmFmYTQ0YThkLyIsIm9pZCI6IjU3MmQ2OGQ0LTExOTctNGE4Ny05MzJjLTAwM2Q4ZTRhN2RhOCIsInN1YiI6IjU3MmQ2OGQ0LTExOTctNGE4Ny05MzJjLTAwM2Q4ZTRhN2RhOCIsInRpZCI6ImU1MjExMWM3LTQwNDgtNGYzNC1hZWE5LTYzMjZhZmE0NGE4ZCIsInV0aSI6ImV1UlBhWEpudEUyMmVtMG1FYVZUQUEiLCJ2ZXIiOiIxLjAifQ.SM8jeJ1o9pq8gzGkZ7qfggYQbOHNlDx7sRPU2eOOll-D0gRyykttohEe4sdydZiFBli66VMbmXz9XYuESHXX7T0QTQkZBn158xdZNxuM8U08bpVFjwD_bAXzkM-RsLv3Fq-CezEvntwAI2HZ-Vmfmecx7O6zXiqJioVUnIlNK-UN8JliS2JwjGWkpcE3HU1cwPu2j_gbUNxy1cJODBK051CPlc3MNOoys4Zq8XjUgnFfw9_ce48sg9r_RL6ZfsQGV45c7vVnw-JvkeQny8lYTNIWFXb8Pf59R2iIs70a7V2aHHbpAau9Rc0JwhpjJ9x2-1UX5kH6lGzuxgXPewEZvQ";
        token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6InU0T2ZORlBId0VCb3NIanRyYXVPYlY4NExuWSIsImtpZCI6InU0T2ZORlBId0VCb3NIanRyYXVPYlY4NExuWSJ9.eyJhdWQiOiJodHRwOi8vYXBwb2ludG1lbnRzLmRpcmVjdG9yeW9mc2VydmljZXMubmhzLnVrOjQ0My9wb2MiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9lNTIxMTFjNy00MDQ4LTRmMzQtYWVhOS02MzI2YWZhNDRhOGQvIiwiaWF0IjoxNTY0NDE1NzM5LCJuYmYiOjE1NjQ0MTU3MzksImV4cCI6MTU2NDQxOTYzOSwiYWlvIjoiNDJGZ1lEZ2Q5bmthejJYRzBFWkJ0cXp0VWw2dkFRPT0iLCJhcHBpZCI6IjBmN2JjMDhiLTMzOTUtNGI0Yi1iMjNiLWY3OTBmYzYyYmY5MSIsImFwcGlkYWNyIjoiMSIsImdyb3VwcyI6WyJhYjQxMmZlOS0zZjY4LTQzNjgtOTgxMC05ZGMyNGQxNjU5YjEiLCJkYWNiODJjNS1hZWE4LTQ1MDktODg3Zi0yODEzMjQwNjJkZmQiXSwiaWRwIjoiaHR0cHM6Ly9zdHMud2luZG93cy5uZXQvZTUyMTExYzctNDA0OC00ZjM0LWFlYTktNjMyNmFmYTQ0YThkLyIsIm9pZCI6IjU3MmQ2OGQ0LTExOTctNGE4Ny05MzJjLTAwM2Q4ZTRhN2RhOCIsInN1YiI6IjU3MmQ2OGQ0LTExOTctNGE4Ny05MzJjLTAwM2Q4ZTRhN2RhOCIsInRpZCI6ImU1MjExMWM3LTQwNDgtNGYzNC1hZWE5LTYzMjZhZmE0NGE4ZCIsInV0aSI6InVSNllQa0gzYUVLXzZJbTNLQXBIQUEiLCJ2ZXIiOiIxLjAifQ.GUcWRlBDNUwYC3rljRlaTt8z087JI8whV-fri9qAAtAT9PCHpWWc478Uhd0IG2GcMcdoGuHc3P54pnMbHhs8YTdbdo5cUsfiYoi-ta2-0mca5zI5RntnjsdRVKl36GxlPUozJW-GF0hRsmAvo76qkByFLKjjNoazr6syHwYKxk4mt0IdFe2apWWpPKL6pXnjQSqfp6TXoVboAei-9lpjxTe6czSn__tSJ7cPeT64QAjFMpuvsTOfzJqNehsaHVaJ7P0QDWZh3ix_WfxOKH8FYDh4gID5rWGLhjsLD3x3RSG_hYucqcbPsVzZ3yeISvSlum0OM30vF_YUPIhz50YWoA";
        System.out.println("validateToken");
        String reqURI = "http://appointments.directoryofservices.nhs.uk/poc";
        ExternalLogger ourLogger = ExternalLogger.GetInstance();
        RequestInterceptor instance = new RequestInterceptor(ourLogger);
        String result = instance.validateToken(token, reqURI);
    }

    /**
     * Method to get an access_token as TestClient
     *
     * @return
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

}
