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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import uk.nhs.fhir.bookingprovider.ResourceProvider.AppointmentResourceProvider;
import uk.nhs.fhir.bookingprovider.ResourceProvider.SlotResourceProvider;
import uk.nhs.fhir.bookingprovider.checkers.AppointmentChecker;
import uk.nhs.fhir.bookingprovider.data.DataStore;
import uk.nhs.fhir.bookingprovider.logging.ExternalLogger;

/**
 * This is the actual Servlet, which hosts a set of ResourceProviders, one for
 * each Resource type that we're handling.
 *
 * Serves out FHIR base at localhost:443/poc/ See for example /poc/metadata
 *
 * @author tim.coates@nhs.net
 */
@WebServlet(urlPatterns = {"/poc/*"}, displayName = "FHIR Booking POC")
public class RestfulServlet extends RestfulServer {

    String environment = "Developing";

    /**
     * The object we use to intercept requests, to check supplied JWTs.
     */
    RequestInterceptor requestInterceptor = null;

    /**
     * The logger we use across this class. *
     */
    private static final Logger LOG
            = Logger.getLogger(RestfulServlet.class.getName());



    /**
     * Constructor, just sets the base URL (to a static fixed value for now).
     */
    public RestfulServlet() {
        InputStream input = null;
        String propsName = "server.properties";

        String base = "http://appointments.directoryofservices.nhs.uk:443/poc";
        try {
            Properties serverProperties = new Properties();
            ClassLoader classLoader = getClass().getClassLoader();
            input = classLoader.getResource(propsName).openStream();
            serverProperties.load(input);
            String baseurl = serverProperties.getProperty("baseurl");
            environment = serverProperties.getProperty("environment");
            if(baseurl!= null) {
                LOG.info("Loaded baseurl from: " + propsName + " " + baseurl);
                base = baseurl;
            }
        } catch (IOException ex) {
            LOG.severe("Error reading " + propsName + " " + ex.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LOG.severe("Error closing: " + propsName  + " " + e.getMessage());
                }
            }
        }
        setServerAddressStrategy(new HardcodedServerAddressStrategy(base));
        setServerConformanceProvider(new CapabilityStatementBooster());
    }

    /**
     * This object is shared quite widely by being passed into various
     * constructors, it is used to share key information.
     *
     */
    private ExternalLogger ourLogger;

    /**
     * The Class that holds all of our resources. Again this instance is shared
     * across various other objects.
     */
    private DataStore data;

    /**
     * The HAPI Fhir context see
     * https://hapifhir.io/doc_intro.html#_toc_introducing_the_fhir_context
     * for details of how this is used.
     *
     */
    private final FhirContext ctx = FhirContext.forDstu3();

    /**
     * An AppointmentChecker object that we'll use to validate any incoming
     * appointments.
     */
    private AppointmentChecker checker;

    /**
     * This handles requests to URL: /poc/reset where it resets the in-memory
     * data store, so creates all new Slots as free and removes any booked
     * appointments.
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected final void doGet(final HttpServletRequest request,
            final HttpServletResponse response)
            throws ServletException, IOException {
        //LOG.info("Requested URI: " + request.getRequestURI());

        // Special case processing for the Reset page
        if (request.getRequestURI().equals("/poc/reset")) {
            LOG.info("Resetting the data store...");
            data.initialize();
            LOG.info("Flushing cached Groups and Applications from Azure");
            requestInterceptor.flushAzureCache();

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/html");
            PrintWriter outputStream = response.getWriter();
            String page = getFileContents("reset.html");
            outputStream.append(page);
            return;
        }

        // Special case processing for the 'home' page
        if (request.getRequestURI().equals("/poc/index")
                || request.getRequestURI().equals("/poc/index.html")
                || request.getRequestURI().equals("/poc/index.htm")
                || request.getRequestURI().equals("/poc/")
                || request.getRequestURI().equals("/index")
                || request.getRequestURI().equals("/index.html")
                || request.getRequestURI().equals("/index.htm")) {
            //LOG.info("Index page requested");
            int appts = data.getAppointments().size();
            int slots = data.getSlots().size();
            int freeSlots = data.getFreeSlots().size();
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/html");
            PrintWriter outputStream = response.getWriter();
            outputStream.append(getIndexPage(slots, freeSlots, appts));
            return;
        }

        // Special case processing for the 'home' page
        if (request.getRequestURI().equals("/poc/model")) {
            LOG.info("Model POST request requested");
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            PrintWriter outputStream = response.getWriter();
            outputStream.append(getRequest());
            return;
        }

        // Special case processing for the 'home' page
        if (request.getRequestURI().equals("/poc/modelXML")) {
            LOG.info("Model POST request requested");
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/xml");
            PrintWriter outputStream = response.getWriter();
            outputStream.append(getRequestXML());
            return;
        }

        // If we haven't returned yet, get superclass to process this.
        super.doGet(request, response);
    }

    /**
     * Here is where the Servlet is first initialised by the Application server
     * (e.g. Tomcat or Jetty) it's being hosted by.
     *
     * @throws ServletException
     */
    @Override
    protected final void initialize() throws ServletException {
        LOG.info("Initialising servlet");

        /**
         * Here we instantiate some globally used objects:
         **/

        // An object that checks an Appointment FHIR Resource.
        checker = new AppointmentChecker(ctx);

        // An object holding the data (typically in memory) resources/objects.
        data = DataStore.getInstance();

        // An object that logs things out over some external channel (MS Teams?)
        ourLogger = ExternalLogger.GetInstance(environment);

        // Create an interceptor to validate incoming requests
        requestInterceptor = new RequestInterceptor(ourLogger);
        // Now register the validating interceptor
        registerInterceptor(requestInterceptor);

        /**
         * Now we're going to add ResourceProviders see
         * https://hapifhir.io/doc_rest_server.html#_toc_defining_resource_providers
         */
        List<IResourceProvider> rpList = new ArrayList<>();

        // We pass our Global objects from above into both our Resource Providers.
        rpList.add(new AppointmentResourceProvider(ctx, data, checker, ourLogger));
        rpList.add(new SlotResourceProvider(ctx, data, ourLogger));

        // And add the list to this Servlet
        setResourceProviders(rpList);
        LOG.info("Created server to handle the configured resources.");
    }

    /**
     * Method to return a list of resources we're here to handle. This is only
     * currently used in the unit tests.
     *
     * @return A list of (class names of) the Resource types we can handle.
     */
    public final List<String> getResources() {
        List<String> resourceNames = new ArrayList<>();
        Collection<IResourceProvider> rpList = this.getResourceProviders();

        for (IResourceProvider item : rpList) {
            resourceNames.add(item.getResourceType().getCanonicalName());
        }
        return resourceNames;
    }

    /**
     * Separate method to build the index page, to move the code out of doGet().
     *
     * Values are passed in to allow it to show the number of Slots etc.
     *
     * @param slotCount The total number of Slots.
     * @param freeSlotCount The number of Slots shown as free.
     * @param apptCount The number of booked appointments.
     * @return A String representing the entire html page.
     */
    public final String getIndexPage(final int slotCount,
            final int freeSlotCount,
            final int apptCount) {
        String indexFile = getFileContents("index.html");
        String slots = Integer.toString(slotCount);
        String freeSLots = Integer.toString(freeSlotCount);
        String appts = Integer.toString(apptCount);
        indexFile = indexFile.replace("{{SLOTS}}", slots);
        indexFile = indexFile.replace("{{FREESLOTS}}", freeSLots);
        indexFile = indexFile.replace("{{APPOINTMENTS}}", appts);

        String versionInfo = getFileContents("version.txt");
        versionInfo = versionInfo.replace("\n", "<br />");

        indexFile = indexFile.replace("{{VERSION}}", versionInfo);
        return indexFile;
    }

    /**
     * Method to generate a model POST request payload.
     *
     * @return A String holding a sample JSON request body to be used to book an
     * Appointment.
     *
     * The string is loaded from a Resource file in
     * bookingprovider/src/main/resources/request.json
     *
     */
    public final String getRequest() {
        return getFileContents("request.json");
    }

    /**
     * Method to return the perfect POST payload in XML format for booking an
     * Appointment. This is loaded from
     * bookingprovider/src/main/resources/request.xml
     *
     * @return XML Payload
     */
    public final String getRequestXML() {
        return getFileContents("request.xml");
    }

    /**
     * Method to get the contents of files in src/main/resources
     *
     * Used to get the JSON and XML model requests. Used to load version.txt
     * which is populated by Maven at build time.
     *
     * @param filename The name of the file requested to be read.
     * @return The contents of the file, or an empty String.
     */
    public final String getFileContents(final String filename) {
        StringBuilder result = new StringBuilder("");
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(filename).getFile());

        try (Scanner scanner = new Scanner(file)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }
            scanner.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();
    }
}
