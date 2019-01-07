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

import uk.nhs.fhir.bookingprovider.checkers.Fault;
import uk.nhs.fhir.bookingprovider.checkers.AppointmentChecker;
import java.util.ArrayList;
import java.util.Date;
import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.Reference;
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

    public AppointmentCheckerTest() {
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
     * Test of CheckThis method, of class AppointmentChecker.
     */
    @Test
    public void testCheckThis() {
        System.out.println("checkThis");
        ResourceMaker maker = new ResourceMaker();
        Appointment appointment = maker.makeAppointment();
        AppointmentChecker instance = new AppointmentChecker();
        ArrayList<Fault> expResult = new ArrayList<>();
        ArrayList<Fault> result = instance.checkThis(appointment);
        assertEquals(expResult, result);
    }

    @Test
    public void testcheckLanguage() {
        System.out.println("checkLanguage");
        ResourceMaker maker = new ResourceMaker();
        Appointment appointment = maker.makeAppointment();
        AppointmentChecker instance = new AppointmentChecker();
        int expResult = 0;
        ArrayList<Fault> result = instance.checkLanguage(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testcheckLanguageBAD() {
        System.out.println("checkLanguageBAD");
        ResourceMaker maker = new ResourceMaker();
        Appointment appointment = maker.makeAppointment();
        appointment.setLanguage("fr");
        AppointmentChecker instance = new AppointmentChecker();
        int expResult = 1;
        ArrayList<Fault> result = instance.checkLanguage(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testcheckStatus() {
        System.out.println("checkStatus");
        ResourceMaker maker = new ResourceMaker();
        Appointment appointment = maker.makeAppointment();
        AppointmentChecker instance = new AppointmentChecker();
        int expResult = 0;
        ArrayList<Fault> result = instance.checkStatus(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testcheckStatusBAD() {
        System.out.println("checkStatusBAD");
        ResourceMaker maker = new ResourceMaker();
        Appointment appointment = maker.makeAppointment();
        appointment.setStatus(Appointment.AppointmentStatus.NOSHOW);
        AppointmentChecker instance = new AppointmentChecker();
        int expResult = 1;
        ArrayList<Fault> result = instance.checkStatus(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testcheckSlot() {
        System.out.println("checkSlot");
        ResourceMaker maker = new ResourceMaker();
        Appointment appointment = maker.makeAppointment();
        AppointmentChecker instance = new AppointmentChecker();
        int expResult = 0;
        ArrayList<Fault> result = instance.checkSlot(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testcheckSlotBAD1() {
        System.out.println("checkSlotBAD1");
        ResourceMaker maker = new ResourceMaker();
        Appointment appointment = maker.makeAppointment();
        appointment.getSlot().add(new Reference("BadReference"));
        AppointmentChecker instance = new AppointmentChecker();
        int expResult = 1;
        ArrayList<Fault> result = instance.checkSlot(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testcheckSlotBAD2() {
        System.out.println("checkSlotBAD2");
        ResourceMaker maker = new ResourceMaker();
        Appointment appointment = maker.makeAppointment();
        appointment.getSlot().set(0, new Reference("/Organization/Org1"));
        AppointmentChecker instance = new AppointmentChecker();
        int expResult = 1;
        ArrayList<Fault> result = instance.checkSlot(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testcheckCreated() {
        System.out.println("checkCreated");
        ResourceMaker maker = new ResourceMaker();
        Appointment appointment = maker.makeAppointment();
        AppointmentChecker instance = new AppointmentChecker();
        int expResult = 0;
        ArrayList<Fault> result = instance.checkCreated(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testcheckCreatedBAD() {
        System.out.println("checkCreatedBAD");
        ResourceMaker maker = new ResourceMaker();
        Appointment appointment = maker.makeAppointment();
        appointment.setCreated(new Date(4, 11, 9));
        AppointmentChecker instance = new AppointmentChecker();
        int expResult = 1;
        ArrayList<Fault> result = instance.checkCreated(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testcheckParticipant() {
        System.out.println("checkParticipant");
        ResourceMaker maker = new ResourceMaker();
        Appointment appointment = maker.makeAppointment();
        AppointmentChecker instance = new AppointmentChecker();
        int expResult = 0;
        ArrayList<Fault> result = instance.checkParticipant(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testcheckParticipantBAD() {
        System.out.println("checkParticipantBAD");
        ResourceMaker maker = new ResourceMaker();
        Appointment appointment = maker.makeAppointment();
        Appointment.AppointmentParticipantComponent t = new Appointment.AppointmentParticipantComponent();
        Reference actorRef = new Reference("AnyBadReference");
        t.setActor(actorRef);
        appointment.addParticipant(t);
        AppointmentChecker instance = new AppointmentChecker();
        int expResult = 2;
        ArrayList<Fault> result = instance.checkParticipant(appointment);
        assertEquals(expResult, result.size());
    }
    
    @Test
    public void testchecksupportingInfo() {
        System.out.println("checksupportingInfo");
        ResourceMaker maker = new ResourceMaker();
        Appointment appointment = maker.makeAppointment();
        AppointmentChecker instance = new AppointmentChecker();
        int expResult = 0;
        ArrayList<Fault> result = instance.checksupportingInfo(appointment);
        assertEquals(expResult, result.size());
    }

    @Test
    public void testchecksupportingInfoBAD1() {
        System.out.println("checksupportingInfoBAD1");
        ResourceMaker maker = new ResourceMaker();
        Appointment appointment = maker.makeAppointment();
        Reference newRef = new Reference("BadReference/Nothing");
        appointment.addSupportingInformation(newRef);
        AppointmentChecker instance = new AppointmentChecker();
        int expResult = 1;
        ArrayList<Fault> result = instance.checksupportingInfo(appointment);
        assertEquals(expResult, result.size());
    }
    
    @Test
    public void testchecksupportingInfoBAD2() {
        System.out.println("checksupportingInfoBAD2");
        ResourceMaker maker = new ResourceMaker();
        Appointment appointment = maker.makeAppointment();
        ArrayList<Reference> theSupportingInformation = new ArrayList<>();
        appointment.setSupportingInformation(theSupportingInformation);
        AppointmentChecker instance = new AppointmentChecker();
        int expResult = 1;
        ArrayList<Fault> result = instance.checksupportingInfo(appointment);
        assertEquals(expResult, result.size());
    }
}
