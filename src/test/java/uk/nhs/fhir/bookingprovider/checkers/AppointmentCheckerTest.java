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
package uk.nhs.fhir.bookingprovider.checkers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.JsonParser;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import org.hl7.fhir.dstu3.model.Appointment;
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
public class AppointmentCheckerTest {
    static FhirContext ctx;
    static JsonParser parser;

    public AppointmentCheckerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        ctx = FhirContext.forDstu3();
        parser = (JsonParser) ctx.newJsonParser();
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
     * Test of CheckThis method, of class AppointmentChecker.
     */
    @Test
    public void testCheckThis() {
        System.out.println("checkThis");
        String apptString = getFileContents("goodAppt.json");
        Appointment appointment = parser.parseResource(Appointment.class,
                apptString);
        AppointmentChecker instance = new AppointmentChecker(ctx);
        ArrayList<Fault> expResult = new ArrayList<>();
        ArrayList<Fault> result = instance.checkThis(appointment);
        assertEquals(expResult, result);
    }

    @Test
    public void testcheckLanguage() {
        System.out.println("checkLanguage");
        String apptString = getFileContents("goodAppt.json");
        Appointment appointment = parser.parseResource(Appointment.class,
                apptString);
        AppointmentChecker instance = new AppointmentChecker(ctx);
        int expResult = 0;
        ArrayList<Fault> result = instance.checkLanguage(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testcheckLanguageBAD() {
        System.out.println("checkLanguageBAD");
        String apptString = getFileContents("badAppt_Lang.json");
        Appointment appointment = parser.parseResource(Appointment.class,
                apptString);
        appointment.setLanguage("fr");
        AppointmentChecker instance = new AppointmentChecker(ctx);
        int expResult = 1;
        ArrayList<Fault> result = instance.checkLanguage(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testcheckStatus() {
        System.out.println("checkStatus");
        String apptString = getFileContents("goodAppt.json");
        Appointment appointment = parser.parseResource(Appointment.class,
                apptString);
        AppointmentChecker instance = new AppointmentChecker(ctx);
        int expResult = 0;
        ArrayList<Fault> result = instance.checkStatus(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testcheckStatusBAD() {
        System.out.println("checkStatusBAD");
        String apptString = getFileContents("badAppt_Status.json");
        Appointment appointment = parser.parseResource(Appointment.class,
                apptString);
        AppointmentChecker instance = new AppointmentChecker(ctx);
        int expResult = 1;
        ArrayList<Fault> result = instance.checkStatus(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testcheckSlot() {
        System.out.println("checkSlot");
        String apptString = getFileContents("goodAppt.json");
        Appointment appointment = parser.parseResource(Appointment.class,
                apptString);
        AppointmentChecker instance = new AppointmentChecker(ctx);
        int expResult = 0;
        ArrayList<Fault> result = instance.checkSlot(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testcheckSlotBAD1() {
        System.out.println("checkSlotBAD1");
        String apptString = getFileContents("baddAppt_TwoSlots.json");
        Appointment appointment = parser.parseResource(Appointment.class,
                apptString);

        AppointmentChecker instance = new AppointmentChecker(ctx);
        int expResult = 1;
        ArrayList<Fault> result = instance.checkSlot(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testcheckSlotBAD2() {
        System.out.println("checkSlotBAD2");
        String apptString = getFileContents("baddAppt_SlotType.json");
        Appointment appointment = parser.parseResource(Appointment.class,
                apptString);
        AppointmentChecker instance = new AppointmentChecker(ctx);
        int expResult = 1;
        ArrayList<Fault> result = instance.checkSlot(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testcheckCreated() {
        System.out.println("checkCreated");
        String apptString = getFileContents("goodAppt.json");
        Appointment appointment = parser.parseResource(Appointment.class,
                apptString);
        AppointmentChecker instance = new AppointmentChecker(ctx);
        int expResult = 0;
        ArrayList<Fault> result = instance.checkCreated(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testcheckCreatedBAD() {
        System.out.println("checkCreatedBAD");
        String apptString = getFileContents("badAppt_Created.json");
        Appointment appointment = parser.parseResource(Appointment.class,
                apptString);
        AppointmentChecker instance = new AppointmentChecker(ctx);
        int expResult = 1;
        ArrayList<Fault> result = instance.checkCreated(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testcheckParticipant() {
        System.out.println("checkParticipant");

        String apptString = getFileContents("goodAppt.json");
        Appointment appointment = parser.parseResource(Appointment.class,
                apptString);
        AppointmentChecker instance = new AppointmentChecker(ctx);
        int expResult = 0;
        ArrayList<Fault> result = instance.checkParticipant(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testcheckParticipantBAD() {
        System.out.println("checkParticipantBAD");
        String apptString = getFileContents("badAppt_MoreParticipants.json");
        Appointment appointment = parser.parseResource(Appointment.class,
                apptString);
        AppointmentChecker instance = new AppointmentChecker(ctx);
        int expResult = 2;
        ArrayList<Fault> result = instance.checkParticipant(appointment);
        assertEquals(expResult, result.size());
    }

    /**
     * This checks that an Appointment with good supportingInformation link to a
     * contained DocumentReference resource is flagged as good.
     */
    @Test
    public void testchecksupportingInfo() {
        System.out.println("checksupportingInfo");
        String apptString = getFileContents("goodAppt.json");
        Appointment appointment = parser.parseResource(Appointment.class,
                apptString);
        AppointmentChecker instance = new AppointmentChecker(ctx);
        int expResult = 0;
        ArrayList<Fault> result = instance.checksupportingInfo(appointment);
        assertEquals(expResult, result.size());
    }

    /**
     * This checks that an Appointment where the supportingINformation is a
     * local canonical reference (e.g. /DocumentReference/123) it's marked as
     * bad.
     */
    @Test
    public void testchecksupportingInfoBAD1() {
        System.out.println("checksupportingInfoBAD1");
        String apptString = getFileContents("badAppt_SupInfo.json");
        Appointment appointment = parser.parseResource(Appointment.class,
                apptString);
        AppointmentChecker instance = new AppointmentChecker(ctx);
        int expResult = 1;
        ArrayList<Fault> result = instance.checksupportingInfo(appointment);
        assertEquals(expResult, result.size());
    }

    /**
     * This checks that if there's no supportingInformation items (e.g.
     * "supportingInformation": [ ], it's flagged as bad.
     * 
     */
    @Test
    public void testchecksupportingInfoBAD2() {
        System.out.println("checksupportingInfoBAD2");
        String apptString = getFileContents("badAppt_NoSupInfo.json");
        Appointment appointment = parser.parseResource(Appointment.class,
                apptString);
        AppointmentChecker instance = new AppointmentChecker(ctx);
        int expResult = 1;
        ArrayList<Fault> result = instance.checksupportingInfo(appointment);
        assertEquals(expResult, result.size());
    }

    /**
     * This checks that cases where the id in the contained DocumentReference
     * resource isn't reflected in supportingInformation (with a hash prefix)
     * are flagged up as bad.
     */
    @Test
    public void testchecksupportingInfoBAD3() {
        System.out.println("checksupportingInfoBAD1");
        String apptString = getFileContents("badAppt_SupInfo2.json");
        Appointment appointment = parser.parseResource(Appointment.class,
                apptString);
        AppointmentChecker instance = new AppointmentChecker(ctx);
        int expResult = 1;
        ArrayList<Fault> result = instance.checksupportingInfo(appointment);
        assertEquals(expResult, result.size());
    }

    /**
     * This checks that if there's no supportingInformation element at all, it's
     * flagged as bad.
     * 
     */
    @Test
    public void testchecksupportingInfoBAD4() {
        System.out.println("checksupportingInfoBAD2");
        String apptString = getFileContents("badAppt_NoSupInfoElement.json");
        Appointment appointment = parser.parseResource(Appointment.class,
                apptString);
        AppointmentChecker instance = new AppointmentChecker(ctx);
        int expResult = 1;
        ArrayList<Fault> result = instance.checksupportingInfo(appointment);
        assertEquals(expResult, result.size());
    }


    @Test
    public void testcheckPatientLink() {
        System.out.println("checkPatientLink");
        String apptString = getFileContents("goodAppt.json");
        IParser newJsonParser = ctx.newJsonParser();
        Appointment appointment = (Appointment) newJsonParser.parseResource(apptString);
        AppointmentChecker instance = new AppointmentChecker(ctx);
        int expResult = 0;
        ArrayList<Fault> result = instance.checkPatientLink(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testcheckPatientLinkBAD() {
        System.out.println("checkPatientLinkBAD");
        String apptString = getFileContents("badAppt1.json");
        IParser newJsonParser = ctx.newJsonParser();
        Appointment appointment = (Appointment) newJsonParser.parseResource(apptString);
        AppointmentChecker instance = new AppointmentChecker(ctx);
        int expResult = 1;
        ArrayList<Fault> result = instance.checkPatientLink(appointment);
        assertEquals(expResult, result.size());
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
     * Test of validateAppointment method, of class AppointmentChecker.
     */
    @Test
    public void testValidateAppointment() {
        System.out.println("validateAppointment");
        String apptString = getFileContents("invalidAppt.json");
        Appointment appointment = parser.parseResource(Appointment.class, apptString);
        AppointmentChecker instance = new AppointmentChecker(ctx);
        ArrayList<Fault> result = instance.validateAppointment(appointment);
        assertEquals(3, result.size());
    }
}
