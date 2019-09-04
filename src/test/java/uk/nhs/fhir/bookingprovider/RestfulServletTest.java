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

import uk.nhs.fhir.bookingprovider.RestfulServlet;
import java.util.List;
import javax.servlet.ServletException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Class to test (not very much) our underlying RestfulServlet class.
 *
 * Because the Servlet doesn't itself do much, there's very little to test.
 *
 * @author tim.coates@nhs.net
 */
public class RestfulServletTest {

    public RestfulServletTest() {
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
     * Test of initialize method, of class RestfulServlet.
     *
     * @throws javax.servlet.ServletException
     */
    @Test
    public void testInitialize() throws ServletException {
        System.out.println("initialize");
        RestfulServlet instance = new RestfulServlet();
        instance.initialize();
        assertNotNull(instance);
    }

    /**
     * Test of getResources method, of class RestfulServlet.
     *
     * @throws javax.servlet.ServletException
     */
    @Test
    public void testGetResources() throws ServletException {
        System.out.println("getResources");
        RestfulServlet instance = new RestfulServlet();
        instance.initialize();
        List<String> result = instance.getResources();
        assertEquals(result.size(), 2);
    }

}
