/*
 * Copyright 2018 dev.
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

import uk.nhs.fhir.bookingprovider.ResourceProvider.AppointmentResourceProvider;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.XmlParser;
import ca.uhn.fhir.parser.JsonParser;
import java.util.logging.Logger;
import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.nhs.fhir.bookingprovider.checkers.AppointmentChecker;
import uk.nhs.fhir.bookingprovider.checkers.ResourceMaker;
import uk.nhs.fhir.bookingprovider.data.DataStore;

/**
 *
 * @author dev
 */
public class AppointmentResourceProviderTest {

    FhirContext ctx;
    DataStore newData;
    AppointmentChecker checker;

    private static final Logger LOG = Logger.getLogger(AppointmentResourceProviderTest.class.getName());

    public AppointmentResourceProviderTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        ctx = FhirContext.forDstu3();
        newData = DataStore.getInstance();
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
        checker = new AppointmentChecker();
        AppointmentResourceProvider instance = new AppointmentResourceProvider(ctx, newData, checker);
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
        ResourceMaker maker = new ResourceMaker();
        Appointment newAppointment = maker.makeAppointment();
        newAppointment = maker.setAppointmentSlot(newAppointment, "/Slot/slot003");
        checker = new AppointmentChecker();
        AppointmentResourceProvider instance = new AppointmentResourceProvider(ctx, newData, checker);
        Class<Appointment> expResult = Appointment.class;
        IBaseResource result = instance.createAppointment(newAppointment).getResource();
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
}
