/*
 * Copyright 2019 NHS Digital.
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
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenParam;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.Slot;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.nhs.fhir.bookingprovider.MockRequest;
import uk.nhs.fhir.bookingprovider.MockResponse;
import uk.nhs.fhir.bookingprovider.data.DataStore;
import uk.nhs.fhir.bookingprovider.logging.ExternalLogger;

/**
 *
 * @author tim.coates@nhs.net
 */
public class SlotResourceProviderTest {

    FhirContext ctx;
    static ExternalLogger ourLogger;

    static HttpServletRequest myRequestMock;
    static HttpServletResponse responseMock;

    public SlotResourceProviderTest() {
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
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getResourceType method, of class SlotResourceProvider.
     */
    @Test
    public void testGetResourceType() {
        System.out.println("getResourceType");
        ctx = FhirContext.forDstu3();
        DataStore newData = DataStore.getInstance();
        newData.initialize();
        SlotResourceProvider instance = new SlotResourceProvider(ctx, newData, ourLogger);
        Class<Slot> expResult = Slot.class;
        Class<Slot> result = instance.getResourceType();
        assertEquals(expResult, result);
    }

    /**
     * Test of getResourceById method, of class SlotResourceProvider.
     */
    @Test
    public void testGetResourceById() {
        System.out.println("getResourceById");
        String idName = "slot001";
        IdType theId = new IdType(idName);
        ctx = FhirContext.forDstu3();
        DataStore newData = DataStore.getInstance();
        newData.initialize();
        SlotResourceProvider instance = new SlotResourceProvider(ctx, newData, ourLogger);
        Slot result = instance.getResourceById(theId, myRequestMock);
        assertEquals(idName, result.getId());
    }

    /**
     * Test of searchSlots method, of class SlotResourceProvider.
     */
    @Test
    public void testSearchSlots_0args() {
        System.out.println("searchSlots");
        ctx = FhirContext.forDstu3();
        DataStore newData = DataStore.getInstance();
        newData.initialize();
        SlotResourceProvider instance = new SlotResourceProvider(ctx, newData, ourLogger);
        int expResult = 40;
        List<Slot> result = instance.searchSlots(myRequestMock);
        assertEquals(expResult, result.size());
    }

    /**
     * Test of searchSlots method, of class SlotResourceProvider.
     */
    @Test
    public void testSearchSlots_4args() {
        System.out.println("searchSlots");
        TokenParam theHealthcareService = new TokenParam("918999198999");
        TokenParam statusToken = new TokenParam("free");
        // Set start time to 00:00 tomorrow...
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DATE, 1);
        Date theLowerBound = cal.getTime();
        cal.add(Calendar.DATE, 1);
        Date theUpperBound = cal.getTime();
        DateRangeParam startRange = new DateRangeParam(theLowerBound, theUpperBound);
        Set<Include> theIncludes = new HashSet<Include>();
        theIncludes.add(new Include("Slot:schedule"));
        ctx = FhirContext.forDstu3();
        DataStore newData = DataStore.getInstance();
        newData.initialize();
        SlotResourceProvider instance = new SlotResourceProvider(ctx, newData, ourLogger);
        int expResult = 21;
        List<IResource> result = instance.searchSlots(theHealthcareService, statusToken, startRange, theIncludes, myRequestMock);
        //int Schedcount = 0;
        for (int i = 0; i < result.size(); i++) {
            Resource res = (Resource) result.get(i);
            System.out.println(res.getResourceType().toString() + " - " + res.getId());
        //    if(res.getResourceType().toString().equals("Schedule")) {
        //        Schedcount++;
        //    }
        }
        //assertEquals(1, Schedcount);
        assertEquals(expResult, result.size());
    }

    /**
     * Test of searchSlots method, of class SlotResourceProvider. This one has a
     * 'tighter' time constraint applied.
     */
    @Test
    public void testSearchSlots_4argsFiltered1() {
        System.out.println("searchSlots");
        TokenParam theHealthcareService = new TokenParam("918999198999");
        TokenParam statusToken = new TokenParam("free");
        // Set start time to 09:00 tomorrow...
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DATE, 1);
        Date theLowerBound = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 10);
        Date theUpperBound = cal.getTime();
        DateRangeParam startRange = new DateRangeParam(theLowerBound, theUpperBound);
        Set<Include> theIncludes = new HashSet<Include>();
        theIncludes.add(new Include("Slot:schedule"));
        ctx = FhirContext.forDstu3();
        DataStore newData = DataStore.getInstance();
        newData.initialize();
        SlotResourceProvider instance = new SlotResourceProvider(ctx, newData, ourLogger);
        int expResult = 6;
        List<IResource> result = instance.searchSlots(theHealthcareService, statusToken, startRange, theIncludes, myRequestMock);
        for (int i = 0; i < result.size(); i++) {
            Resource res = (Resource) result.get(i);
            System.out.println(res.getResourceType().toString() + " - " + res.getId());
        }
        assertEquals(expResult, result.size());
    }

    /**
     * Test of searchSlots method, of class SlotResourceProvider. This one has
     * even 'tighter' time constraint applied, but also asks for the
     * HealthcareService to be included in the resulting Bundle.
     */
    @Test
    public void testSearchSlots_4argsFiltered2() {
        System.out.println("searchSlots");
        TokenParam theHealthcareService = new TokenParam("918999198999");
        TokenParam statusToken = new TokenParam("free");
        // Set start time to 09:00 tomorrow...
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DATE, 1);
        Date theLowerBound = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 15);
        Date theUpperBound = cal.getTime();
        DateRangeParam startRange = new DateRangeParam(theLowerBound, theUpperBound);
        Set<Include> theIncludes = new HashSet<Include>();
        theIncludes.add(new Include("Slot:schedule"));
        theIncludes.add(new Include("Schedule:actor:HealthcareService"));
        ctx = FhirContext.forDstu3();
        DataStore newData = DataStore.getInstance();
        newData.initialize();
        SlotResourceProvider instance = new SlotResourceProvider(ctx, newData, ourLogger);
        int expResult = 4;
        List<IResource> result = instance.searchSlots(theHealthcareService, statusToken, startRange, theIncludes, myRequestMock);
        assertEquals(expResult, result.size());
    }
    
    /**
     * Test of searchSlots method, of class SlotResourceProvider. This one has
     * even 'tighter' time constraint applied, but also asks for the
     * HealthcareService to be included in the resulting Bundle.
     */
    @Test
    public void testSearchSlots_4argsFiltered3() {
        System.out.println("searchSlots");
        TokenParam theHealthcareService = new TokenParam("918999198999");
        TokenParam statusToken = new TokenParam("free");
        // Set start time to 09:00 tomorrow...
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DATE, 1);
        Date theLowerBound = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 15);
        Date theUpperBound = cal.getTime();
        DateRangeParam startRange = new DateRangeParam(theLowerBound, theUpperBound);
        Set<Include> theIncludes = new HashSet<Include>();
        theIncludes.add(new Include("Slot:schedule"));
        theIncludes.add(new Include("Schedule:actor:HealthcareService"));
        theIncludes.add(new Include("HealthcareService.location"));
        ctx = FhirContext.forDstu3();
        DataStore newData = DataStore.getInstance();
        newData.initialize();
        SlotResourceProvider instance = new SlotResourceProvider(ctx, newData, ourLogger);
        int expResult = 5;
        List<IResource> result = instance.searchSlots(theHealthcareService, statusToken, startRange, theIncludes, myRequestMock);
        assertEquals(expResult, result.size());
    }

    /**
     * Test of searchSlots method, of class SlotResourceProvider. This one
     * doesn't specify the HealthcareService.
     */
    @Test
    public void testSearchSlots_4argsFiltered4() {
        System.out.println("searchSlots");
        TokenParam statusToken = new TokenParam("free");
        // Set start time to 09:00 tomorrow...
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DATE, 1);
        Date theLowerBound = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 15);
        Date theUpperBound = cal.getTime();
        DateRangeParam startRange = new DateRangeParam(theLowerBound, theUpperBound);
        Set<Include> theIncludes = new HashSet<Include>();
        theIncludes.add(new Include("Slot:schedule"));
        theIncludes.add(new Include("Schedule:actor:HealthcareService"));
        theIncludes.add(new Include("HealthcareService.location"));
        ctx = FhirContext.forDstu3();
        DataStore newData = DataStore.getInstance();
        newData.initialize();
        SlotResourceProvider instance = new SlotResourceProvider(ctx, newData, ourLogger);
        int expResult = 10;
        List<IResource> result = instance.searchSlots(statusToken, startRange, theIncludes, myRequestMock);
        assertEquals(expResult, result.size());
    }

}
