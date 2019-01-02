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
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

/**
 * This is the actual Servlet, which hosts a set of ResourceProviders for each
 * Resource type that we're handling.
 *
 * Serves out FHIR base at localhost:443/poc/ See for example
 * /poc/metadata
 *
 * @author tim.coates@nhs.net
 */
@WebServlet(urlPatterns = {"/poc/*"}, displayName = "FHIR Booking POC")
public class RestfulServlet extends RestfulServer {

    /**
     * Constructor, just sets the base URL (to a static fixed value for now)
     */
    public RestfulServlet() {
        //String serverBaseUrl = "http://localhost:443/poc";
        //setServerAddressStrategy(new HardcodedServerAddressStrategy(serverBaseUrl));
    }

    /**
     * The logger we use across this class. *
     */
    private static final Logger LOG = Logger.getLogger(RestfulServlet.class.getName());

    /**
     * The Class that holds all of our resources...
     */
    DataStore data;

    /**
     * The HAPI Fhir context. *
     */
    private final FhirContext ctx = FhirContext.forDstu3();

    /**
     * An AppointmentChecker object that we'll use to validate any incoming
     * appointments
     */
    AppointmentChecker checker;

    /**
     * This handles requests to URL: http://localhost:443/poc/reset where
     * it resets the in-memory data store, so creates all new Slots as free and
     * removes any booked appointments.
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOG.info("Requested URI: " + request.getRequestURI());

        // Special case processing for the Reset page
        if (request.getRequestURI().equals("/poc/reset")) {
            LOG.info("Resetting the data store...");
            data.initialize();
            response.setStatus(200);
            response.setContentType("text/html");
            PrintWriter outputStream = response.getWriter();
            outputStream.append("<html lang='en'><head><meta http-equiv='refresh' content='2; url=/poc/index'><title>Reset done</title></head><body><h1>Data reset to initial state</h1></body></html>");
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
            LOG.info("Index page requested");
            int apptCount = data.getAppointments().size();
            int slotCount = data.getSlots().size();
            int freeSlotCount = data.getFreeSlots().size();
            response.setStatus(200);
            response.setContentType("text/html");
            PrintWriter outputStream = response.getWriter();
            outputStream.append(getIndexPage(slotCount, freeSlotCount, apptCount));
            return;
        }

        // Special case processing for the 'home' page
        if (request.getRequestURI().equals("/poc/model")) {
            LOG.info("Model POST request requested");
            response.setStatus(200);
            response.setContentType("application/json");
            PrintWriter outputStream = response.getWriter();
            outputStream.append(getRequest());
            return;
        }

        // Special case processing for the 'home' page
        if (request.getRequestURI().equals("/poc/modelXML")) {
            LOG.info("Model POST request requested");
            response.setStatus(200);
            response.setContentType("application/xml");
            PrintWriter outputStream = response.getWriter();
            outputStream.append(getRequestXML());
            return;
        }

        // If we haven't returned yet, get superclass to process this.
        super.doGet(request, response);
    }

    /**
     * Here is where the Servlet is first initialised.
     *
     * @throws ServletException
     */
    @Override
    protected final void initialize() throws ServletException {
        LOG.info("Initialising servlet");
        checker = new AppointmentChecker();
        data = null;
        data = DataStore.getInstance();

        // Create an interceptor to validate incoming requests
        RequestInterceptor requestInterceptor = new RequestInterceptor();
        // Now register the validating interceptor
        registerInterceptor(requestInterceptor);
        List<IResourceProvider> rpList = new ArrayList<>();

        // This list is all of the STU3 resource type on fhir.nhs.uk
        rpList.add(new AppointmentResourceProvider(ctx, data, checker));
        rpList.add(new SlotResourceProvider(ctx, data));

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
     * Separate method to build the index page, to move the code out of doGet()
     * 
     * Values are passed in to allow it to show the number of Slots etc.
     *
     * @param slotCount The total number of Slots.
     * @param freeSlotCount The number of Slots shown as free
     * @param apptCount The number of booked appointments.
     * @return A String representing the entire html page.
     */
    public final String getIndexPage(final int slotCount, final int freeSlotCount, final int apptCount) {
        StringBuilder outputStream = new StringBuilder();
        outputStream.append("<html lang='en'>\n <head>\n  <title>Booking POC</title>\n </head>\n <body style='font-family:verdana;'>\n  <h1>UEC Appointment Booking Provider Demonstrator</h1>");
        outputStream.append("  <h2>Internal links</h2>");
        outputStream.append("  <p>");
        outputStream.append("<a href='/poc/Slot/'>Slots</a> - (Requires a JWT) This requests all Slots held in the internal data store.<br />");
        outputStream.append("<a href='/poc/Slot?schedule.actor:healthcareservice=918999198999&_include:recurse=Slot:schedule&_include=Schedule:actor:Practitioner&_include=Schedule:actor:PractitionerRole&_include=Schedule:actor:healthcareservice&_format=json'>Slots</a> - (Requires a JWT) This requests Slots from a given HealthcareService (an example of the full query).<br />");
        outputStream.append("<a href='/poc/Appointment'>Appointments</a> - (Requires a JWT) This lists any Appointments in the internal data store.<br />");
        outputStream.append("<a href='/poc/reset'>Reset</a> - This resets the internal data store to initial state.<br />");
        outputStream.append("  </p>");
        outputStream.append("  <h2>Useful external links</h2>");
        outputStream.append("  <p>");
        outputStream.append("<a href='https://developer.nhs.uk/scheduling-specification-versions/' target='new'>Standard</a> - The Care Connect Scheduling service standard.<br />");
        outputStream.append("<a href='https://nhsconnect.github.io/FHIR-A2SI-APPOINTMENTS-API/index.html' target='new'>Spec</a> - The FHIR specification.<br />");
        outputStream.append("  </p>");
        outputStream.append("  <h2>Data</h2>");
        outputStream.append("  <p>");
        outputStream.append(slotCount + " Slots of which " + freeSlotCount + " are Free.<br />");
        outputStream.append(apptCount + " Appointments<br />");
        outputStream.append("  </p>");

        outputStream.append("  <h2>Get Token</h2>");
        outputStream.append("  <p>\n   <form action='https://login.microsoftonline.com/e52111c7-4048-4f34-aea9-6326afa44a8d/oauth2/v2.0/token' method='post'>");
        outputStream.append("<table>\n");
        outputStream.append(" <tr>\n  <td>grant_type:</td><td><input type='text' size='22' name='grant_type' value='client_credentials' readonly='true'></td>\n</tr>\n");
        outputStream.append(" <tr>\n  <td>scope:</td><td><input type='text' size='55' name='scope' value='http://appointments.directoryofservices.nhs.uk:443/poc/.default'></td>\n</tr>\n");
        outputStream.append(" <tr>\n  <td>client_id:</td><td><input type='text' size='36' name='client_id' value='' placeholder='Value issued by NHS Digital'></td>\n</tr>\n");
        outputStream.append(" <tr>\n  <td>client_secret:</td><td><input type='text' size='50' name='client_secret' value='' placeholder='Value issued by NHS Digital'></td>\n</tr>\n");
        outputStream.append("</table>\n");
        outputStream.append("<input type='submit' />");
        outputStream.append("   </form></p>");
        outputStream.append("  <h2>Appointment sample POST Payloads</h2>");
        outputStream.append("  <p>");
        outputStream.append("<a href='/poc/model'>View</a> JSON example<br />");
        outputStream.append("<a href='/poc/modelXML'>View</a> Annotated XML Example<br />");
        outputStream.append("  </p>");

        outputStream.append("  <p>");
        String versionInfo = getVersionInfo();
        versionInfo = versionInfo.replace("\n", "<br />");
        outputStream.append(versionInfo);
        outputStream.append("  </p>");

        outputStream.append(" </body>\n</html>\n");
        return outputStream.toString();
    }

    /**
     * Method to generate a model POST request payload
     *
     * @return
     */
    public final String getRequest() {
        StringBuilder outputStream = new StringBuilder();
        outputStream.append("{\n"
                + "    \"resourceType\": \"Appointment\",\n"
                + "    \"meta\": {\n"
                + "        \"profile\": \"https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Appointment-1\"\n"
                + "    },\n"
                + "    \"language\": \"en\",\n"
                + "    \"text\": \"<div>Appointment</div>\",\n"
                + "    \"contained\": [\n"
                + "        {\n"
                + "            \"resourceType\": \"DocumentReference\",\n"
                + "            \"id\": \"123\",\n"
                + "            \"identifier\": {\n"
                + "                \"system\": \"uuid\",\n"
                + "                \"value\": \"A709A442-3CF4-476E-8377-376500E829C9\"\n"
                + "            },\n"
                + "            \"status\": \"current\",\n"
                + "            \"type\": {\n"
                + "                \"coding\": [\n"
                + "                    {\n"
                + "                        \"system\": \"urn:oid:2.16.840.1.113883.2.1.3.2.4.18.17\",\n"
                + "                        \"code\": \"POCD_MT200001GB02\",\n"
                + "                        \"display\": \"Integrated Urgent Care Report\"\n"
                + "                    }\n"
                + "                ]\n"
                + "            },\n"
                + "            \"indexed\": \"2018-12-20T09:43:41+11:00\",\n"
                + "            \"content\": [\n"
                + "                {\n"
                + "                    \"attachment\": {\n"
                + "                        \"contentType\": \"application/hl7-v3+xml\",\n"
                + "                        \"language\": \"en\"\n"
                + "                    }\n"
                + "                }\n"
                + "            ]\n"
                + "        },\n"
                + "        {\n"
                + "            \"resourceType\": \"Patient\",\n"
                + "            \"meta\": {\n"
                + "            \"profile\": \"https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Patient-1\"\n"
                + "        },\n"
                + "            \"id\": \"P1\",\n"
                + "            \"identifier\": [\n"
                + "                {\n"
                + "                    \"extension\": [\n"
                + "                        {\n"
                + "                            \"url\": \"https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-NHSNumberVerificationStatus-1\",\n"
                + "                            \"valueCodeableConcept\": {\n"
                + "                                \"coding\": [\n"
                + "                                    {\n"
                + "                                        \"system\": \"https://fhir.hl7.org.uk/STU3/ValueSet/CareConnect-NHSNumberVerificationStatus-1\",\n"
                + "                                        \"code\": \"01\",\n"
                + "                                        \"display\": \"Number present and verified\"\n"
                + "                                    }\n"
                + "                                ]\n"
                + "                            }\n"
                + "                        }\n"
                + "                    ],\n"
                + "                    \"use\": \"official\",\n"
                + "                    \"system\": \"https://fhir.nhs.uk/Id/nhs-number\",\n"
                + "                    \"value\": \"1231231234\"\n"
                + "                }\n"
                + "            ],\n"
                + "            \"name\": [\n"
                + "                {\n"
                + "                  \"use\": \"official\",\n"
                + "                    \"prefix\": \"Mr\",\n"
                + "                    \"given\": \"John\",\n"
                + "                    \"family\": \"Smith\"\n"
                + "                }\n"
                + "            ],\n"
                + "            \"telecom\": [\n"
                + "                {\n"
                + "                    \"system\": \"phone\",\n"
                + "                    \"value\": \"01234 567 890\",\n"
                + "                    \"use\": \"home\",\n"
                + "                    \"rank\": 0\n"
                + "                }\n"
                + "            ],\n"
                + "            \"gender\": \"male\",\n"
                + "            \"birthDate\": \"1974-12-25\",\n"
                + "            \"address\": [\n"
                + "                {\n"
                + "                    \"use\": \"home\",\n"
                + "                    \"text\": \"123 High Street, Leeds LS1 4HR\",\n"
                + "                    \"line\": [\n"
                + "                        \"123 High Street\",\n"
                + "                        \"Leeds\"\n"
                + "                    ],\n"
                + "                    \"city\": \"Leeds\",\n"
                + "                    \"postalCode\": \"LS1 4HR\"\n"
                + "                }\n"
                + "            ]\n"
                + "        }\n"
                + "    ],\n"
                + "    \"status\": \"booked\",\n"
                + "    \"supportingInformation\": [\n"
                + "      {\n"
                + "            \"reference\": \"#123\"\n"
                + "        }\n"
                + "    ],\n"
                + "    \"description\": \"Reason for calling\",\n"
                + "    \"slot\": [\n"
                + "        {\n"
                + "            \"reference\": \"Slot/slot001\"\n"
                + "        }\n"
                + "    ],\n"
                + "    \"created\": \"2018-12-11T13:42:18.124Z\" ,\n"
                + "    \n"
                + "    \"participant\": [\n"
                + "        {\n"
                + "            \"actor\": {\n"
                + "                \"reference\": \"#P1\",\n"
                + "                \"identifier\": {\n"
                + "                    \"use\": \"official\",\n"
                + "                    \"system\": \"https://fhir.nhs.uk/Id/nhs-number\",\n"
                + "                    \"value\": \"1234554321\"\n"
                + "                },\n"
                + "                \"display\": \"Peter James Chalmers\"\n"
                + "            }\n"
                + "        }\n"
                + "    ]\n"
                + "}");
        return outputStream.toString();
    }

    public final String getRequestXML() {
        StringBuilder outputStream = new StringBuilder();
        outputStream.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<Appointment xmlns=\"http://hl7.org/fhir\">\n"
                + "  <meta>\n"
                + "    <profile value=\"https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Appointment-1\"/>\n"
                + "  </meta>\n"
                + "  <language value=\"en\"/>\n"
                + "<!-- Appointment resource contains two resources -->\n"
                + "  <contained>\n"
                + "\n<!-- A DocumentReference, which links this to the CDA Document -->\n"
                + "    <DocumentReference xmlns=\"http://hl7.org/fhir\">\n"
                + "      <id value=\"123\"/>\n"
                + "      <identifier>\n"
                + "        <system value=\"uuid\"/>\n"
                + "\n<!-- This is the root ID of the CDA document -->\n"
                + "        <value value=\"A709A442-3CF4-476E-8377-376500E829C9\"/>\n"
                + "      </identifier>\n"
                + "      <status value=\"current\"/>\n"
                + "      <type>\n"
                + "        <coding>\n"
                + "          <system value=\"urn:oid:2.16.840.1.113883.2.1.3.2.4.18.17\"/>\n"
                + "          <code value=\"POCD_MT200001GB02\"/>\n"
                + "          <display value=\"Integrated Urgent Care Report\"/>\n"
                + "        </coding>\n"
                + "      </type>\n"
                + "      <indexed value=\"2018-12-20T09:43:41+11:00\"/>\n"
                + "      <content>\n"
                + "        <attachment>\n"
                + "          <contentType value=\"application/hl7-v3+xml\"/>\n"
                + "          <language value=\"en\"/>\n"
                + "        </attachment>\n"
                + "      </content>\n"
                + "    </DocumentReference>\n"
                + "  </contained>\n"
                + "\n<!-- In theory, both of the contained resources could (should?) be within one <contained> tag. -->\n"
                + "  <contained>\n"
                + "\n<!-- A Patient which gives details of the Patient the Appointment is for -->\n"
                + "    <Patient xmlns=\"http://hl7.org/fhir\">\n"
                + "      <id value=\"P1\"/>\n"
                + "      <meta>\n"
                + "        <profile value=\"https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Patient-1\"/>\n"
                + "      </meta>\n"
                + "\n<!-- Here's the NHS Number (with the Care Connect Extension). -->\n"
                + "      <identifier>\n"
                + "        <extension url=\"https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-NHSNumberVerificationStatus-1\">\n"
                + "          <valueCodeableConcept>\n"
                + "            <coding>\n"
                + "              <system value=\"https://fhir.hl7.org.uk/STU3/ValueSet/CareConnect-NHSNumberVerificationStatus-1\"/>\n"
                + "              <code value=\"01\"/>\n"
                + "              <display value=\"Number present and verified\"/>\n"
                + "            </coding>\n"
                + "          </valueCodeableConcept>\n"
                + "        </extension>\n"
                + "        <use value=\"official\"/>\n"
                + "        <system value=\"https://fhir.nhs.uk/Id/nhs-number\"/>\n"
                + "        <value value=\"1231231234\"/>\n"
                + "      </identifier>\n"
                + "      <name>\n"
                + "        <use value=\"official\"/>\n"
                + "        <family value=\"Smith\"/>\n"
                + "        <given value=\"John\"/>\n"
                + "        <prefix value=\"Mr\"/>\n"
                + "      </name>\n"
                + "      <telecom>\n"
                + "        <system value=\"phone\"/>\n"
                + "        <value value=\"01234 567 890\"/>\n"
                + "        <use value=\"home\"/>\n"
                + "        <rank value=\"0\"/>\n"
                + "      </telecom>\n"
                + "      <gender value=\"male\"/>\n"
                + "      <birthDate value=\"1974-12-25\"/>\n"
                + "      <address>\n"
                + "        <use value=\"home\"/>\n"
                + "        <text value=\"123 High Street, Leeds LS1 4HR\"/>\n"
                + "        <line value=\"123 High Street\"/>\n"
                + "        <line value=\"Leeds\"/>\n"
                + "        <city value=\"Leeds\"/>\n"
                + "        <postalCode value=\"LS1 4HR\"/>\n"
                + "      </address>\n"
                + "    </Patient>\n"
                + "  </contained>\n"
                + "\n<!-- End of the contained resources -->\n"
                + "  <status value=\"booked\"/>\n"
                + "  <description value=\"Reason for calling\"/>\n"
                + "\n<!-- supportingInformation points to the contained DocumentReference resource (reference prefixed with # indicates contained resource) identified with #123. -->\n"
                + "  <supportingInformation>\n"
                + "    <reference value=\"#123\"/>\n"
                + "  </supportingInformation>\n"
                + "\n<!-- Slot points to a Slot as referenced (by the Provider) when Get Slots was called. -->\n"
                + "  <slot>\n"
                + "    <reference value=\"Slot/slot001\"/>\n"
                + "  </slot>\n"
                + "  <created value=\"2018-12-11T13:42:18.124Z\"/>\n"
                + "\n<!-- participant | actor points to the contained Patient resource identified with P1 -->\n"
                + "  <participant>\n"
                + "    <actor>\n"
                + "      <reference value=\"#P1\"/>\n"
                + "      <identifier>\n"
                + "        <use value=\"official\"/>\n"
                + "        <system value=\"https://fhir.nhs.uk/Id/nhs-number\"/>\n"
                + "        <value value=\"1234554321\"/>\n"
                + "      </identifier>\n"
                + "      <display value=\"Peter James Chalmers\"/>\n"
                + "    </actor>\n"
                + "  </participant>\n"
                + "</Appointment>");
        return outputStream.toString();
    }

    /**
     * Method to get the contents of version.txt which is populated by Maven at
     * build time.
     *
     * @return
     */
    public final String getVersionInfo() {
        StringBuilder result = new StringBuilder("");
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("version.txt").getFile());

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
