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

import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.ResponseDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.SigningKeyNotFoundException;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import uk.nhs.fhir.bookingprovider.azure.AzureAD;
import uk.nhs.fhir.bookingprovider.logging.ExternalLogger;

/**
 * See https://hapifhir.io/doc_rest_server_interceptor.html for more details
 * @author tim.coates@nhs.net
 */
public class RequestInterceptor extends InterceptorAdapter {

    private static final Logger LOG = Logger.getLogger(RequestInterceptor.class.getName());
    private final String JWKURL;
    private final String ISSUER;
    AzureAD adWrangler;
    ExternalLogger ourLogger;

    /**
     * Properties file in which we store the URLs used for checking tokens.
     */
    Properties jwtURLs;

    /**
     * Constructor, just tells us to load the properties file holding all of the
     * App IDs, and the properties file which configures the AzureAD endpoints.
     *
     */
    public RequestInterceptor(ExternalLogger newLogger) {
        loadJWTURLs();
        JWKURL = jwtURLs.getProperty("JWKURL");
        ISSUER = jwtURLs.getProperty("ISSUER");
        adWrangler = new AzureAD();
        ourLogger = newLogger;
    }

    /**
     * Override the incomingRequestPreProcessed method, which is called for each
     * incoming request before any processing is done.
     * 
     * This is where we do the auth checking.
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
        String clientName = null;
        String requestid = UUID.randomUUID().toString();

        if (authHeader != null) {
            LOG.info("JWT: " + authHeader);
            String requestURI = theRequest.getRequestURL().toString();
            LOG.info("Request received " + requestURI + " " + theRequest.getQueryString());
            if (authHeader.toLowerCase().startsWith("bearer")) {
                String tokenValue = authHeader.substring(6, authHeader.length()).trim();
                clientName = validateToken(tokenValue, requestURI);
                if(clientName == null) {
                    throw new AuthenticationException("Authorization header not validated");
                } else {
                    theRequest.setAttribute("uk.nhs.fhir.bookingprovider.requestid", clientName  + " " + requestid);
                    ourLogger.log(clientName + " presented a valid JWT " + requestid);
                    return true;
                }
            } else {
                throw new AuthenticationException("Authorization header doesn't begin with 'Bearer'");
            }
        } else {
            throw new AuthenticationException("No 'Authorization' header received");
        }
    }

    /**
     * Method to validate the JWT we've been passed.
     *
     * @param token
     * @return
     */
    public final String validateToken(final String token, final String reqURI) {
        String clientName = null;
        try {
            DecodedJWT actualJWT = JWT.decode(token);
            JwkProvider JWKSProvider = new UrlJwkProvider(new URL(JWKURL));
            String keyUsedID = actualJWT.getKeyId();
            Jwk jwk = JWKSProvider.get(keyUsedID);
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            algorithm.verify(actualJWT);
            // Here we know it was signed properly

            // Check it's current etc
            checkTimes(actualJWT);

            // Check who issued it
            checkIssuer(actualJWT);

            // Check client is a mamber of the right Groups.
            if (!checkGroups(actualJWT)) {
                LOG.severe("Client was not a member of the required Groups.");
                throw new ForbiddenOperationException("Client is not a member of the required Groups.");
            }

            // Log the client's ID
            clientName = logAppID(actualJWT);

            // Check the token is intended for this server
            if (!checkAudience(actualJWT, reqURI)) {
                LOG.severe("Token was not intended for: " + reqURI);
                return null;
            }

        }
        catch(SigningKeyNotFoundException ex) {
            // Signing Key used not found, has it been rotated out of use?
            LOG.severe(ex.getMessage());
            throw new UnprocessableEntityException("SigningKeyNotFoundException: " + ex.getMessage());
        }
        catch (JwkException ex) {
            LOG.severe(ex.getMessage());
            throw new UnprocessableEntityException("JwkException: " + ex.getMessage());
        }
        catch (MalformedURLException ex) {
            LOG.severe(ex.getMessage());
            throw new UnprocessableEntityException("MalformedURLException: " + ex.getMessage());
        }
        
        return clientName;
    }

    /**
     * Method to check the various times; Not Before, Issued At and Expiry.
     * NB: We allow 7 minutes 30 seconds of grace on both not before and expiry
     * times to accommodate time drift.
     *
     * @param theJWT The Decoded JWT as per:
     * https://static.javadoc.io/com.auth0/java-jwt/3.3.0/com/auth0/jwt/interfaces/DecodedJWT.html
     *
     * @return Whether or not this token is current.
     *
     */
    private void checkTimes(DecodedJWT theJWT) {

        // Check it hasn't yet expired
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -450);
        if (theJWT.getExpiresAt().before(calendar.getTime())) {
            Calendar expiresAt = Calendar.getInstance();
            expiresAt.setTime(theJWT.getExpiresAt());
            throw new AuthenticationException("Access Token has expired");
        }

        // Check it's ready to be used
        calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 450);
        if (theJWT.getNotBefore().after(calendar.getTime())) {
            Calendar notBefore = Calendar.getInstance();
            notBefore.setTime(theJWT.getNotBefore());
            throw new AuthenticationException("Access Token is not yet valid");
        }
    }

    /**
     * Method to check that the issuer claim (iss) in the supplied JWT matches
     * the expected value.
     *
     * @param theJWT A Decoded JWT
     * @return Indication of whether to trust or not.
     */
    private void checkIssuer(DecodedJWT theJWT) {
        String issuer = theJWT.getIssuer();
        if (!issuer.equals(ISSUER)) {
            throw new AuthenticationException("JWT not issued by expected party");
        }
    }

    /**
     * Method to check whether the client is a member of the two required
     * groups.
     *
     * TODO: This should differentiate between the ability to read Slots and to
     * book an appointment.
     *
     * @param theJWT The incoming JWT
     * @return
     */
    private boolean checkGroups(DecodedJWT theJWT) {

        boolean canReadSlots = false;
        boolean canBookAppts = false;

        String createAppointments = "urn:nhs:names:services:careconnect:fhir:rest:create:appointment";
        String readSlots = "urn:nhs:names:services:careconnect:fhir:rest:read:slot";

        String[] groups = theJWT.getClaim("groups").asArray(String.class);
        if (groups == null) {
            throw new ForbiddenOperationException("The token's groups claim was null");
        }
        for (String group : groups) {
            String groupName = adWrangler.getGroupName(group);
            LOG.info("Found group: " + groupName);
            if (groupName != null) {
                if (groupName.equals(createAppointments)) {
                    canBookAppts = true;
                }
                if (groupName.equals(readSlots)) {
                    canReadSlots = true;
                }
            }
        }
        if (canBookAppts && canReadSlots) {
            LOG.info("Both Groups found, this request should be permitted.");
        } else {
            LOG.info("Groups not found, this request should be BLOCKED.");
        }
        return (canBookAppts && canReadSlots);
    }

    /**
     * Method to check the token is intended for 'us'
     *
     * @param theJWT
     * @return Validates that the URL called matches that of the audience in the
     * supplied JWT.
     */
    private boolean checkAudience(DecodedJWT theJWT, String URI) {
        LOG.info("Checking JWT was intended for: " + URI);
        List<String> audienceList = theJWT.getAudience();
        boolean correctAudience = false;
        for (String audience : audienceList) {
            LOG.info("Audience: " + audience);

// Necessary hack here, as ECS strips off the port number on the way in.
            if (audience.equals("http://appointments.directoryofservices.nhs.uk:443/poc")) {
                audience = "http://appointments.directoryofservices.nhs.uk/poc";
            }
            if (audience.equals("http://a2sibookingprovidertest.directoryofservices.nhs.uk:443/poc")) {
                audience = "http://a2sibookingprovidertest.directoryofservices.nhs.uk/poc";
            }

            if (URI.startsWith(audience)) {
                correctAudience = true;
            }
        }
        if (correctAudience) {
            LOG.info("Allowing as correct audience");
            return true;
        }
        return false;
    }

    /**
     * Method to simply log out which client made the request.
     *
     * Does a lookup into Azure AD and determine and log the name as well as the GUID.
     *
     * @param theJWT The incoming JWT
     */
    private String logAppID(DecodedJWT theJWT) {
        String clientID = theJWT.getClaim("appid").asString();
        String appName = adWrangler.getAppName(clientID);
        LOG.info("\"JWT was issued to: " + appName + " (" + clientID + ")");
        return appName;
    }

    /**
     * Method to load the URLs used for JWT handling, from jwt.properties file.
     *
     */
    private void loadJWTURLs() {
        InputStream input = null;
        try {
            jwtURLs = new Properties();
            ClassLoader classLoader = getClass().getClassLoader();
            input = classLoader.getResource("jwt.properties").openStream();
            jwtURLs.load(input);
        }
        catch (IOException ex) {
            LOG.severe("Error reading appid.properties file " + ex.getMessage());
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException e) {
                    LOG.severe("Error closing appid.properties file: " + e.getMessage());
                }
            }
        }
    }

    /**
     * TElls our resident Azure handler to flush any cached results it has.
     */
    public void flushAzureCache() {
        adWrangler.flushCaches();
    }
    
    /**
     * Intercepts all outbound (non-error) responses.
     * 
     * This is where we are able to add the eTAG to support checks for versions
     * as described at: http://hl7.org/fhir/stu3/http.html#concurrency
     * 
     * 
     * @param theRequestDetails
     * @param theResponseDetails
     * @param theServletRequest
     * @param theServletResponse
     * @return Returns true to continue with normal processing, or false to
     *          break (e.g. if this is handling the response)
     * @throws AuthenticationException 
     */
    @Override
    public boolean outgoingResponse(RequestDetails theRequestDetails,
                         ResponseDetails theResponseDetails,
                         HttpServletRequest theServletRequest,
                         HttpServletResponse theServletResponse)
                  throws AuthenticationException {
        
        // If the request was related to Appointment
        if(theRequestDetails.getResourceName().equals("Appointment")) {
            LOG.info("Adding ETag - Was an Appointment request");
            if(theRequestDetails.getRestOperationType() == RestOperationTypeEnum.READ) {
                LOG.info("Adding ETag - And it was a READ");
                String version = theResponseDetails.getResponseResource().getIdElement().getVersionIdPart();
                if(! version.equals("")) {
                    LOG.info("Adding ETag - Resource had a version");
                    String ETag = "W/\"" + version + "\"";
                    LOG.info("Adding ETag - Adding the ETag header: " + ETag + " to the response.");
                    theServletResponse.addHeader("ETag", ETag);
                }
            }
        }        
        return true;
    }
}
