/*
 * Copyright 2019 dev.
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
package uk.nhs.fhir.bookingprovider.logging;

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
public class ExternalLoggerTest {
    
    public ExternalLoggerTest() {
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
     * Test of GetInstance method, of class ExternalLogger.
     */
    @Test
    public void testGetInstance() {
        System.out.println("GetInstance");
        ExternalLogger expResult = null;
        ExternalLogger result = ExternalLogger.GetInstance();
        assertNotEquals(expResult, result);
    }

    /**
     * Test of log method, of class ExternalLogger.
     */
    @Test
    public void testLog() {
        System.out.println("log");
        String message = "Just running unit tests - please ignore me";
        ExternalLogger instance = null;
        instance = ExternalLogger.GetInstance();
        boolean expResult = true;
        boolean result = instance.log(message);
        assertEquals(expResult, result);
    }
}
