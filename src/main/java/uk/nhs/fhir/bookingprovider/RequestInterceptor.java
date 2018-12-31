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
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author tim.coates@nhs.net
 */
public class RequestInterceptor extends InterceptorAdapter {

    private static final Logger LOG = Logger.getLogger(RequestInterceptor.class.getName());

    /**
     * Override the incomingRequestPreProcessed method, which is called for each
     * incoming request before any processing is done.
     *
     * @param theRequest The request we've received.
     * @param theResponse The expected response.
     * @return Returns true to allow the request to be processed, or false to
     * stop here.
     *
     * When returning False, it provides a custom error response.
     *
     */
    @Override
    public boolean incomingRequestPreProcessed(HttpServletRequest theRequest, HttpServletResponse theResponse) {

        String authHeader = theRequest.getHeader("Authorization");

        if (authHeader != null) {
            LOG.info("JWT: " + authHeader);
            String requestURI = theRequest.getRequestURL().toString();
            LOG.info("Request received " + requestURI + " " + theRequest.getQueryString());
            if (authHeader.toLowerCase().startsWith("bearer")) {
                String tokenValue = authHeader.substring(6, authHeader.length()).trim();
                if (validateToken(tokenValue, requestURI)) {
                    return true;
                } else {
                    BuildResponse(theResponse, "Authorization header not validated");
                    return false;
                }
            } else {
                BuildResponse(theResponse, "Authorization header doesn't begin with 'Bearer'");
                return false;
            }
        } else {
            BuildResponse(theResponse, "No 'Authorization' header received");
            return false;
        }
    }

    /**
     * Method to validate the JWT we've been passed.
     *
     * @param token
     * @return
     */
    public final boolean validateToken(final String token, final String reqURI) {
        try {
            DecodedJWT actualJWT = JWT.decode(token);
            JwkProvider JWKSProvider = new UrlJwkProvider(new URL("https://login.microsoftonline.com/common/discovery/keys"));
            String keyUsedID = actualJWT.getKeyId();
            Jwk jwk = JWKSProvider.get(keyUsedID);
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            algorithm.verify(actualJWT);
            // Here we know it was signed properly

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");

            // Check it hasn't yet expired
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, -450);
            if (actualJWT.getExpiresAt().before(calendar.getTime())) {
                Calendar expiresAt = Calendar.getInstance();
                expiresAt.setTime(actualJWT.getExpiresAt());
                throw new UnprocessableEntityException("The supplied JWT has expired (exp = " + sdf.format(expiresAt.getTime()) + ") is before Now = " + sdf.format(calendar.getTime()));
            }

            // Check it's ready to be used
            calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 450);
            if (actualJWT.getNotBefore().after(calendar.getTime())) {
                Calendar notBefore = Calendar.getInstance();
                notBefore.setTime(actualJWT.getNotBefore());
                throw new UnprocessableEntityException("The supplied JWT is not yet ready to be used (nbf = " + sdf.format(notBefore.getTime()) + ") is after Now = " + sdf.format(calendar.getTime()));
            }
            // Here we know it's current
            List<String> audienceList = actualJWT.getAudience();
            boolean correctAudience = false;
            for (String audience : audienceList) {
                LOG.info("Audience: " + audience);
                if (reqURI.startsWith(audience)) {
                    correctAudience = true;
                }
                if (correctAudience) {
                    return true;
                } else {
                    throw new UnprocessableEntityException("The supplied JWT was not intended for: " + reqURI);
                }
            }

        } catch (JwkException ex) {
            Logger.getLogger(RequestInterceptor.class.getName()).log(Level.SEVERE, null, ex);
            throw new UnprocessableEntityException("JwkException: " + ex.getMessage());
        } catch (MalformedURLException ex) {
            Logger.getLogger(RequestInterceptor.class.getName()).log(Level.SEVERE, null, ex);
            throw new UnprocessableEntityException("MalformedURLException: " + ex.getMessage());
        }
        return false;
    }

    /**
     * Method to build a suitable http response, and return it.
     *
     * We need to do this, as the intercept is so early, that we can't expect
     * HAPI to generate a OperationOutcome etc if we throw an Exception here.
     *
     * @param response The HttpServletResponse object
     * @param text The error text we want it to display
     */
    public final void BuildResponse(HttpServletResponse response, String text) {

        PrintWriter out = null;
        try {
            response.setContentType("text/html;charset=UTF-8");
            // Allocate a output writer to write the response message into the network socket
            out = response.getWriter();
            // Write the response message, in an HTML page
            try {
                out.println("<!DOCTYPE html>");
                out.println("<html><head>");
                out.println("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
                out.println("<title>JWT Failure</title></head>");
                out.println("<body>");
                out.println("<h1>JWT Failure</h1>");  // says Hello
                // Echo client's request information
                out.println("<p>Cause: <strong>" + text + "</strong></p>");
                out.println("</body>");
                out.println("</html>");
            } finally {
                out.close();  // Always close the output writer
            }
        } catch (IOException ex) {
            Logger.getLogger(RequestInterceptor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
        }

    }
}
