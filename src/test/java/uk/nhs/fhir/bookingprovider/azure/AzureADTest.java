/*
 * Copyright 2019 tim.coates@nhs.net.
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
package uk.nhs.fhir.bookingprovider.azure;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
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
public class AzureADTest {
    
    public AzureADTest() {
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
     * Test of getToken method, of class AzureAD.
     */
    @Test
    public void testGetToken() {
        System.out.println("getToken");
        AzureAD instance = new AzureAD();
        String result = instance.getToken();
        assertNotNull(result);
    }

    /**
     * Test of getGroupName method, of class AzureAD.
     */
    @Test
    public void testGetGroupName() {
        System.out.println("getGroupName");
        String groupid = "ab412fe9-3f68-4368-9810-9dc24d1659b1";
        AzureAD instance = new AzureAD();
        String expResult = "urn:nhs:names:services:careconnect:fhir:rest:read:slot";
        String result = instance.getGroupName(groupid);
        assertEquals(expResult, result);
    }

    /**
     * Test of groupNameFromJSON method, of class AzureAD.
     */
    @Test
    public void testGroupNameFromJSON() {
        System.out.println("groupNameFromJSON");
        String json = getFileContents("testGroupsResponse.json");
        String groupID = "6f2ab894-dde8-43a4-b243-4c5e6d1f0093";
        AzureAD instance = new AzureAD();
        String expResult = "urn:nhs:names:services:test:first:test:group:name";
        String result = instance.groupNameFrmJSON(groupID, json);
        assertEquals(expResult, result);
    }

    /**
     * Test of groupDescFromJSON method, of class AzureAD.
     */
    @Test
    public void testGroupDescFromJSON() {
        System.out.println("groupDescFromJSON");
        String json = getFileContents("testGroupsResponse.json");
        String groupID = "2b5c867b-f1bf-4a90-8b72-bc1a044131bc";
        AzureAD instance = new AzureAD();
        String expResult = "Second test group";
        String result = instance.groupDescFrmJSON(groupID, json);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getGroupDesc method, of class AzureAD.
     */
    @Test
    public void testGetGroupDesc() {
        System.out.println("getGroupDesc");
        String groupID = "dacb82c5-aea8-4509-887f-281324062dfd";
        AzureAD instance = new AzureAD();
        String expResult = "Group of applications that are permitted to book Appointments in CareConnect FHIR servers";
        String result = instance.getGroupDesc(groupID);
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppName method, of class AzureAD.
     */
    @Test
    public void testGetAppName() {
        System.out.println("getAppName");
        String appID = "92d85f9d-0666-49bc-a31c-12b45b04a7de";
        AzureAD instance = new AzureAD();
        String expResult = "ConsumerDemo";
        String result = instance.getAppName(appID);
        assertEquals(expResult, result);
    }

    /**
     * Test of appNameFromJSON method, of class AzureAD.
     */
    @Test
    public void testAppNameFromJSON() {
        System.out.println("appNameFromJSON");
        String appID = "0892ae7c-7add-4d9e-bed1-0f6a6851bc17e";
        String json = getFileContents("testAppsResponse.json");
        AzureAD instance = new AzureAD();
        String expResult = "First Test App";
        String result = instance.appNameFromJSON(appID, json);
        assertEquals(expResult, result);
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


}
