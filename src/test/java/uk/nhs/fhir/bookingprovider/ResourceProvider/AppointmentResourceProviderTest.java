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
package uk.nhs.fhir.bookingprovider.ResourceProvider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.XmlParser;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.Appointment.AppointmentStatus;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.nhs.fhir.bookingprovider.MockRequest;
import uk.nhs.fhir.bookingprovider.MockResponse;
import uk.nhs.fhir.bookingprovider.checkers.AppointmentChecker;
import uk.nhs.fhir.bookingprovider.data.DataStore;
import uk.nhs.fhir.bookingprovider.logging.ExternalLogger;

/**
 *
 * @author tim.coates@nhs.net
 */
public class AppointmentResourceProviderTest {

    FhirContext ctx;
    JsonParser parser;
    DataStore newData;
    AppointmentChecker checker;

    static HttpServletRequest myRequestMock;
    static HttpServletResponse responseMock;


    private static final Logger LOG = Logger.getLogger(AppointmentResourceProviderTest.class.getName());
    static ExternalLogger ourLogger;

    public AppointmentResourceProviderTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        ourLogger = ExternalLogger.GetInstance();
        myRequestMock = new MockRequest("", "");
        responseMock = new MockResponse();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        ctx = FhirContext.forDstu3();
        newData = DataStore.getInstance();
        parser = (JsonParser) ctx.newJsonParser();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getResourceType method, of class AppointmentResourceProvider.
     */
    @Test
    public void testGetResourceType() {
        System.out.println("getResourceType");
        checker = new AppointmentChecker(ctx);
        AppointmentResourceProvider instance = new AppointmentResourceProvider(ctx, newData, checker, ourLogger);
        Class<Appointment> expResult = Appointment.class;
        Class<Appointment> result = instance.getResourceType();
        assertEquals(expResult, result);
    }

    /**
     * Test of createAppointment method, of class AppointmentResourceProvider.
     */
    @Test
    public void testCreateAppointment() {
        System.out.println("createAppointment");

        String apptString = getFileContents("goodAppt_1.json");
        Appointment newAppointment = parser.parseResource(Appointment.class, apptString);
        newData.initialize();
        checker = new AppointmentChecker(ctx);
        AppointmentResourceProvider instance = new AppointmentResourceProvider(ctx, newData, checker, ourLogger);
        Class<Appointment> expResult = Appointment.class;
        MethodOutcome appt = instance.createAppointment(newAppointment, myRequestMock, responseMock);
        IBaseResource result = appt.getResource();
        assertEquals(expResult, result.getClass());
    }

    /**
     * Temporary test used to convert a JSON Appointment to XML representation
     */
    @Test
    public void JSONtoXML() {

        String JSONAppt = "{\n"
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
                + "}";
        String XMLAppt = "";
        XmlParser myXmlParser = (XmlParser) ctx.newXmlParser();
        JsonParser myJsonParser = (JsonParser) ctx.newJsonParser();
        Appointment apptObject = (Appointment) myJsonParser.parseResource(JSONAppt);
        XMLAppt = myXmlParser.encodeResourceToString(apptObject);
        LOG.info(XMLAppt);
        assertTrue(true);
    }

    /**
     * Helper function to serialise a Resource
     *
     * @param input
     * @return
     */
    private String ResourceToString(Resource input) {
        FhirContext ctx = FhirContext.forDstu3();
        return ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(input);
    }


    /**
     * Method to get the contents of files in src/test/resources
     *
     * Used to get sample JSON responses.
     *
     * @param filename The name of the file requested to be read.
     * @return The contents of the file, or an empty String.
     */
    public final String getFileContents(String filename) {
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

    /**
     * Test of getAppointment method, of class AppointmentResourceProvider.
     * Gets ALL Appointments
     */
    @Test
    public void testGetAppointment() {
        System.out.println("getAppointment");
        newData.initialize();
        checker = new AppointmentChecker(ctx);
        AppointmentResourceProvider instance = new AppointmentResourceProvider(ctx, newData, checker, ourLogger);
        List<Appointment> result = instance.getAppointment(myRequestMock, responseMock);
        assertEquals(0, result.size());
        String apptString = getFileContents("goodAppt_1.json");
        Appointment newAppointment = parser.parseResource(Appointment.class, apptString);
        MethodOutcome appt = instance.createAppointment(newAppointment, myRequestMock, responseMock);
        result = instance.getAppointment(myRequestMock, responseMock);
        assertEquals(1, result.size());
    }

    /**
     * Test of updateAppointment method, of class AppointmentResourceProvider.
     */
    @Test
    public void testUpdateAppointment() {
        System.out.println("updateAppointment");
        newData.initialize();
        checker = new AppointmentChecker(ctx);
        String apptString = getFileContents("goodAppt_1.json");
        Appointment newAppointment = parser.parseResource(Appointment.class, apptString);
        AppointmentResourceProvider instance = new AppointmentResourceProvider(ctx, newData, checker, ourLogger);
        MethodOutcome outcome = instance.createAppointment(newAppointment, myRequestMock, responseMock);
        // So now we've created an Appointment from goodAppt_1.json
        
        Appointment savedAppt = (Appointment) outcome.getResource();
        IdType newId = new IdType("Appointment/" + outcome.getResource().getIdElement());
        savedAppt.setStatus(AppointmentStatus.CANCELLED);
        
        MethodOutcome result2 = instance.updateAppointment(newId, savedAppt, myRequestMock, responseMock);
        Appointment updated = (Appointment) result2.getResource();
        
        assertEquals(updated.getSlotFirstRep(), newAppointment.getSlotFirstRep());
    }

    /**
     * Test of updateAppointment method, of class AppointmentResourceProvider.
     *
     * This test that we can't ask for a non-existent appointment to be cancelled.
     */
    @Test(expected = UnprocessableEntityException.class)
    public void testUpdateAppointmentBADID() {
        System.out.println("updateAppointment");
        newData.initialize();
        checker = new AppointmentChecker(ctx);
        String apptString = getFileContents("goodAppt_1.json");
        Appointment newAppointment = parser.parseResource(Appointment.class, apptString);
        AppointmentResourceProvider instance = new AppointmentResourceProvider(ctx, newData, checker, ourLogger);
        MethodOutcome appt = instance.createAppointment(newAppointment, myRequestMock, responseMock);
        IdType newId = new IdType("Appointment/f04cf6dd-a30e-4e99-ab50-804c6b5ce38a");
        newAppointment.setId(appt.getResource().getIdElement());
        newAppointment.setStatus(AppointmentStatus.CANCELLED);
        instance.updateAppointment(newId, newAppointment, myRequestMock, responseMock);
    }



    /**
     * Test of updateAppointment method, of class AppointmentResourceProvider.
     *
     * This tests that we can't set the status to an arbitrary value...
     */
    @Test (expected = UnprocessableEntityException.class)
    public void testUpdateAppointmentBadStatusFULFILLED() {
        System.out.println("updateAppointment");
        newData.initialize();
        checker = new AppointmentChecker(ctx);
        String apptString = getFileContents("goodAppt_1.json");
        Appointment newAppointment = parser.parseResource(Appointment.class, apptString);
        AppointmentResourceProvider instance = new AppointmentResourceProvider(ctx, newData, checker, ourLogger);
        MethodOutcome appt = instance.createAppointment(newAppointment, myRequestMock, responseMock);
        IdType newId = new IdType("Appointment/" + appt.getResource().getIdElement());
        newAppointment.setStatus(AppointmentStatus.FULFILLED);
        instance.updateAppointment(newId, newAppointment, myRequestMock, responseMock);
    }

    /**
     * Test of updateAppointment method, of class AppointmentResourceProvider.
     *
     * This tests that we can't set the status to an arbitrary value...
     */
    @Test (expected = UnprocessableEntityException.class)
    public void testUpdateAppointmentBadStatusARRIVED() {
        System.out.println("updateAppointment");
        newData.initialize();
        checker = new AppointmentChecker(ctx);
        String apptString = getFileContents("goodAppt_1.json");
        Appointment newAppointment = parser.parseResource(Appointment.class, apptString);
        AppointmentResourceProvider instance = new AppointmentResourceProvider(ctx, newData, checker, ourLogger);
        MethodOutcome appt = instance.createAppointment(newAppointment, myRequestMock, responseMock);
        IdType newId = new IdType("Appointment/" + appt.getResource().getIdElement());
        newAppointment.setStatus(AppointmentStatus.ARRIVED);
        instance.updateAppointment(newId, newAppointment, myRequestMock, responseMock);
    }

    /**
     * Test of updateAppointment method, of class AppointmentResourceProvider.
     *
     * This tests that we can't set the status to an arbitrary value...
     */
    @Test (expected = UnprocessableEntityException.class)
    public void testUpdateAppointmentBadStatusBOOKED() {
        System.out.println("updateAppointment");
        newData.initialize();
        checker = new AppointmentChecker(ctx);
        String apptString = getFileContents("goodAppt_1.json");
        Appointment newAppointment = parser.parseResource(Appointment.class, apptString);
        AppointmentResourceProvider instance = new AppointmentResourceProvider(ctx, newData, checker, ourLogger);
        MethodOutcome appt = instance.createAppointment(newAppointment, myRequestMock, responseMock);
        IdType newId = new IdType("Appointment/" + appt.getResource().getIdElement());
        newAppointment.setStatus(AppointmentStatus.BOOKED);
        instance.updateAppointment(newId, newAppointment, myRequestMock, responseMock);
    }

    /**
     * Test of updateAppointment method, of class AppointmentResourceProvider.
     *
     * This tests that we can't set the status to an arbitrary value...
     */
    @Test (expected = UnprocessableEntityException.class)
    public void testUpdateAppointmentBadStatusNOSHOW() {
        System.out.println("updateAppointment");
        newData.initialize();
        checker = new AppointmentChecker(ctx);
        String apptString = getFileContents("goodAppt_1.json");
        Appointment newAppointment = parser.parseResource(Appointment.class, apptString);
        AppointmentResourceProvider instance = new AppointmentResourceProvider(ctx, newData, checker, ourLogger);
        MethodOutcome appt = instance.createAppointment(newAppointment, myRequestMock, responseMock);
        IdType newId = new IdType("Appointment/" + appt.getResource().getIdElement());
        newAppointment.setStatus(AppointmentStatus.NOSHOW);
        instance.updateAppointment(newId, newAppointment, myRequestMock, responseMock);
    }

    /**
     * Test of updateAppointment method, of class AppointmentResourceProvider.
     *
     * This tests that we can't set the status to an arbitrary value...
     */
    @Test (expected = UnprocessableEntityException.class)
    public void testUpdateAppointmentBadStatusNULL() {
        System.out.println("updateAppointment");
        newData.initialize();
        checker = new AppointmentChecker(ctx);
        String apptString = getFileContents("goodAppt_1.json");
        Appointment newAppointment = parser.parseResource(Appointment.class, apptString);
        AppointmentResourceProvider instance = new AppointmentResourceProvider(ctx, newData, checker, ourLogger);
        MethodOutcome appt = instance.createAppointment(newAppointment, myRequestMock, responseMock);
        IdType newId = new IdType("Appointment/" + appt.getResource().getIdElement());
        newAppointment.setStatus(AppointmentStatus.NULL);
        instance.updateAppointment(newId, newAppointment, myRequestMock, responseMock);
    }

    /**
     * Test of updateAppointment method, of class AppointmentResourceProvider.
     *
     * This tests that we can't set the status to an arbitrary value...
     */
    @Test (expected = UnprocessableEntityException.class)
    public void testUpdateAppointmentBadStatusPENDING() {
        System.out.println("updateAppointment");
        newData.initialize();
        checker = new AppointmentChecker(ctx);
        String apptString = getFileContents("goodAppt_1.json");
        Appointment newAppointment = parser.parseResource(Appointment.class, apptString);
        AppointmentResourceProvider instance = new AppointmentResourceProvider(ctx, newData, checker, ourLogger);
        MethodOutcome appt = instance.createAppointment(newAppointment, myRequestMock, responseMock);
        IdType newId = new IdType("Appointment/" + appt.getResource().getIdElement());
        newAppointment.setStatus(AppointmentStatus.PENDING);
        instance.updateAppointment(newId, newAppointment, myRequestMock, responseMock);
    }

    /**
     * Test of updateAppointment method, of class AppointmentResourceProvider.
     *
     * This tests that we can't set the status to an arbitrary value...
     */
    @Test (expected = UnprocessableEntityException.class)
    public void testUpdateAppointmentBadStatusPROPOSED() {
        System.out.println("updateAppointment");
        newData.initialize();
        checker = new AppointmentChecker(ctx);
        String apptString = getFileContents("goodAppt_1.json");
        Appointment newAppointment = parser.parseResource(Appointment.class, apptString);
        AppointmentResourceProvider instance = new AppointmentResourceProvider(ctx, newData, checker, ourLogger);
        MethodOutcome appt = instance.createAppointment(newAppointment, myRequestMock, responseMock);
        IdType newId = new IdType("Appointment/" + appt.getResource().getIdElement());
        newAppointment.setStatus(AppointmentStatus.PROPOSED);
        instance.updateAppointment(newId, newAppointment, myRequestMock, responseMock);
    }}
