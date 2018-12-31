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
package uk.nhs.fhir.bookingprovider.checkers;

import uk.nhs.fhir.bookingprovider.checkers.Fault;
import uk.nhs.fhir.bookingprovider.checkers.AppointmentChecker;
import java.util.ArrayList;
import org.hl7.fhir.dstu3.model.Appointment;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dev
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
        System.out.println("CheckThis");
        ResourceMaker maker = new ResourceMaker();
        Appointment appointment = maker.makeAppointment();
        AppointmentChecker instance = new AppointmentChecker();
        ArrayList<Fault> expResult = new ArrayList<>();
        ArrayList<Fault> result = instance.checkThis(appointment);
        assertEquals(expResult, result);
    }

}
