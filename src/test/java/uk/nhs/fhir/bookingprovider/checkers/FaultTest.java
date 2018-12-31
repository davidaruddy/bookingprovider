/*
 * Copyright 2018 tim.coates@nhs.net.
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
import uk.nhs.fhir.bookingprovider.checkers.Fault;
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
public class FaultTest {

    public FaultTest() {
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
     * Test of getDescription method, of class Fault.
     */
    @Test
    public void testGetDescription() {
        System.out.println("getDescription");
        String expResult = "This description which can be very very long.";
        Fault instance = new Fault(expResult, Severity.MINOR);
        String result = instance.getDescription();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSev method, of class Fault.
     */
    @Test
    public void testGetSev() {
        System.out.println("getSev");
        String expResult = "Anything";
        Fault instance = new Fault(expResult, Severity.CRITICAL);
        Severity result = instance.getSev();
        assertEquals(Severity.CRITICAL, result);
    }

    /**
     * Test of toString method, of class Fault.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        Fault instance = new Fault("Some Fault", Severity.CRITICAL);
        String expResult = "CRITICAL Some Fault";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

}
