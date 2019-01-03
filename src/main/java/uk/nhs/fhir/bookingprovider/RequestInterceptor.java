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
    private static final String JWKURL =
            "https://login.microsoftonline.com/common/discovery/keys";
    private static final String ISSUER =
            "https://sts.windows.net/e52111c7-4048-4f34-aea9-6326afa44a8d/";
    
    

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
            JwkProvider JWKSProvider = new UrlJwkProvider(new URL(JWKURL));
            String keyUsedID = actualJWT.getKeyId();
            Jwk jwk = JWKSProvider.get(keyUsedID);
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            algorithm.verify(actualJWT);
            // Here we know it was signed properly

            // Check it's current etc
            if(!checkTimes(actualJWT)) {
                LOG.severe("Times didn't check out properly.");
                return false;
            }
            
            // Check who issued it
            if(!checkIssuer(actualJWT)) {
                LOG.severe("Issuer of JWT was not trusted.");
                return false;
            }
            
            // Check client is a mamber of the right Groups.
            if(!checkGroups(actualJWT)) {
                LOG.severe("Client was not a member of the required Groups.");
                return false;
            }
            
            // Log the client's ID
            logAppID(actualJWT);;

            // Check the token is intended for this server
            if(!checkAudience(actualJWT, reqURI)) {
                LOG.severe("Token was not intended for: " + reqURI);
                return false;
            }
            
        } catch (JwkException ex) {
            Logger.getLogger(RequestInterceptor.class.getName()).log(Level.SEVERE, null, ex);
            throw new UnprocessableEntityException("JwkException: " + ex.getMessage());
        } catch (MalformedURLException ex) {
            Logger.getLogger(RequestInterceptor.class.getName()).log(Level.SEVERE, null, ex);
            throw new UnprocessableEntityException("MalformedURLException: " + ex.getMessage());
        }
        return true;
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

    /**
     * Method to check the various times; Not Before, Issued At and Expiry.
     * 
     * @param theJWT The Decoded JWT as per:
     * https://static.javadoc.io/com.auth0/java-jwt/3.3.0/com/auth0/jwt/interfaces/DecodedJWT.html
     * 
     * @return Whether or not this token is current.
     * 
     */
    private boolean checkTimes(DecodedJWT theJWT) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");

        // Check it hasn't yet expired
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -450);
        if (theJWT.getExpiresAt().before(calendar.getTime())) {
            Calendar expiresAt = Calendar.getInstance();
            expiresAt.setTime(theJWT.getExpiresAt());
            return false;
        }

        // Check it's ready to be used
        calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 450);
        if (theJWT.getNotBefore().after(calendar.getTime())) {
            Calendar notBefore = Calendar.getInstance();
            notBefore.setTime(theJWT.getNotBefore());
            return false;
        }
        return true;
    }
    
    /**
     * Method to check that the issuer claim (iss) in the supplied JWT matches
     * the expected value.
     * 
     * @param theJWT A Decoded JWT
     * @return Indication of whether to trust or not.
     */
    private boolean checkIssuer(DecodedJWT theJWT) {
        String issuer = theJWT.getIssuer();
        return issuer.equals(ISSUER);
    }
    
    /**
     * Method to check whether the client is a member of the two required
     * groups.
     * 
     * TODO: This should differentiate between the ability to read Slots and
     *       to book an appointment.
     * 
     * @param theJWT The incoming JWT
     * @return 
     */
    private boolean checkGroups(DecodedJWT theJWT) {
        
        boolean canReadSlots = false;
        boolean canBookAppts = false;
        
        // urn:nhs:names:services:careconnect:fhir:rest:create:appointment
        String createAppointments = "dacb82c5-aea8-4509-887f-281324062dfd";
        
        // urn:nhs:names:services:careconnect:fhir:rest:read:slot
        String readSlots = "ab412fe9-3f68-4368-9810-9dc24d1659b1";
        
        String[] groups = theJWT.getClaim("groups").asArray(String.class);
        if(groups == null) {
            LOG.info("groups claim returned null !");
            return false;
        }
        for(int i = 0; i < groups.length; i++) {
            LOG.info("Found group: " + groups[i]);
            if(groups[i].equals(createAppointments)) {
                canBookAppts = true;
            }
            if(groups[i].equals(readSlots)) {
                canReadSlots = true;
            }
        }
        return (canBookAppts && canReadSlots);
    }
    
    /**
     * Method to check the token is intended for 'us'
     * 
     * @param theJWT
     * @return 
     */
    private boolean checkAudience(DecodedJWT theJWT, String URI) {
        List<String> audienceList = theJWT.getAudience();
        boolean correctAudience = false;
        for (String audience : audienceList) {
            LOG.info("Audience: " + audience);
            if (URI.startsWith(audience)) {
                correctAudience = true;
            }
            
            /*
            if (correctAudience) {
                return true;
            } else {
                //throw new UnprocessableEntityException("The supplied JWT was not intended for: " + URI);
            }
            */
        }
        return true;
    }
    
    /**
     * Method to simply log out which client made the request.
     * 
     * TODO: This really SHOULd do a lookup into Azure AD and determine and log
     * the name as well as the GUID.
     * 
     * @param theJWT The incoming JWT 
     */
    private void logAppID(DecodedJWT theJWT) {
        String clientID = theJWT.getClaim("appid").asString();
        LOG.log(Level.INFO, "JWT was for: {0}", clientID);
    }
}
