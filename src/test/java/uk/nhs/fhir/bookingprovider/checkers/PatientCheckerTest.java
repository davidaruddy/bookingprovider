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

import uk.nhs.fhir.bookingprovider.checkers.Severity;
import uk.nhs.fhir.bookingprovider.checkers.PatientChecker;
import uk.nhs.fhir.bookingprovider.checkers.Fault;
import java.util.ArrayList;
import org.hl7.fhir.dstu3.model.Patient;
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
public class PatientCheckerTest {

    public enum BREAKPOINT {

        ID, NOIDENTIFIER, NONHSNUMBER, MULTINHSNUMBER, NONAME, NULLFAMILYNAME, NOOFFICIALNAME, FUTUREDOB, NOPOSTCODE, UNVERIFIED
    };
    ResourceMaker maker;

    public PatientCheckerTest() {
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
     * Test of CheckThis method, of class PatientChecker.
     */
    @Test
    public void testCheck() {
        System.out.println("CheckThis");
        maker = new ResourceMaker();
        Patient patient = maker.makePatient("P1");
        PatientChecker instance = new PatientChecker();
        ArrayList<Fault> expResult = new ArrayList<>();
        ArrayList<Fault> result = instance.checkThis(patient);
        assertEquals(expResult, result);
    }

    /**
     * Test of CheckThis method, of class PatientChecker.
     */
    @Test
    public void testCheck_No_ID() {
        System.out.println("CheckThis");
        maker = new ResourceMaker();
        Patient patient = maker.makePatient("P2");
        patient = maker.BreakHim(patient, BREAKPOINT.ID);
        PatientChecker instance = new PatientChecker();
        ArrayList<Fault> expResult = new ArrayList<>();
        expResult.add(new Fault("Patient resource has no ID", Severity.CRITICAL));
        ArrayList<Fault> result = instance.checkThis(patient);
        assertEquals(expResult.get(0).toString(), result.get(0).toString());
    }

    /**
     * Test of CheckThis method, of class PatientChecker.
     */
    @Test
    public void testCheck_Future_DOB() {
        System.out.println("CheckThis");
        maker = new ResourceMaker();
        Patient patient = maker.makePatient("P3");
        patient = maker.BreakHim(patient, BREAKPOINT.FUTUREDOB);
        PatientChecker instance = new PatientChecker();
        ArrayList<Fault> expResult = new ArrayList<>();
        expResult.add(new Fault("Patient has DOB in the future.", Severity.CRITICAL));
        ArrayList<Fault> result = instance.checkThis(patient);
        assertEquals(expResult.get(0).toString(), result.get(0).toString());
    }

    /**
     * Test of CheckThis method, of class PatientChecker.
     */
    @Test
    public void testCheck_No_Name() {
        System.out.println("CheckThis");
        maker = new ResourceMaker();
        Patient patient = maker.makePatient("P4");
        patient = maker.BreakHim(patient, BREAKPOINT.NONAME);
        PatientChecker instance = new PatientChecker();
        ArrayList<Fault> expResult = new ArrayList<>();
        expResult.add(new Fault("Patient has no Name.", Severity.CRITICAL));
        ArrayList<Fault> result = instance.checkThis(patient);
        assertEquals(expResult.get(0).toString(), result.get(0).toString());
    }

}
