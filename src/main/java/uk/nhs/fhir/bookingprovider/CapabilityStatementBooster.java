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
package uk.nhs.fhir.bookingprovider;

import ca.uhn.fhir.rest.annotation.Metadata;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import org.hl7.fhir.dstu3.hapi.rest.server.ServerCapabilityStatementProvider;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import javax.servlet.http.HttpServletRequest;
import org.hl7.fhir.dstu3.model.Reference;

/**
 * Class to boost up our Capability Statement, ensuring things like the supplier
 * etc are correct.
 *
 * Loads most values from server.properties which is located in
 * bookingprovider/src/main/resources/server.properties
 *
 * @author tim.coates@nhs.net
 */
public class CapabilityStatementBooster extends ServerCapabilityStatementProvider {

    /**
     * Basic constructor.
     */
    public CapabilityStatementBooster() {
        setCache(false);
    }

    /**
     * Overridden method to produce CapabilityStatement. Relies on
     * super.getServerConformance() to do the hard work, but then augments the
     * result by setting values mainly from the server.properties file using the
     * function getProperties() which is below. In particular it specifies which
     * profiles we expect.
     *
     * @param theRequest The inbound http request
     * @return The 'improved' CapabilityStatement for our server.
     */
    @Metadata
    public CapabilityStatement getServerConformance(HttpServletRequest theRequest) {
        CapabilityStatement response = super.getServerConformance(theRequest);

        Properties props = getProperties();

        response.setPublisher(props.getProperty("capability.publisher"));
        response.getSoftware().setName(props.getProperty("capability.softwarename"));
        response.getSoftware().setVersion(props.getProperty("capability.softwareversion"));
        response.getImplementation().setDescription(props.getProperty("capability.implementationdescription"));

        // Here we need to update the profiles of Appointment and Slot
        Reference apptProfRef = new Reference();
        apptProfRef.setReference(
                props.getProperty("capability.profile.appointment"));

        Reference slotProfRef = new Reference();
        slotProfRef.setReference(
                props.getProperty("capability.profile.slot"));

        Reference patProfRef = new Reference();
        patProfRef.setReference(
                props.getProperty("capability.profile.patient"));

        Reference practProfileRef = new Reference();
        practProfileRef.setReference(
                props.getProperty("capability.profile.practitioner"));

        Reference practRoleProfileRef = new Reference();
        practRoleProfileRef.setReference(
                props.getProperty("capability.profile.practitionerrole"));

        Reference orgProfileRef = new Reference();
        orgProfileRef.setReference(
                props.getProperty("capability.profile.organization"));

        Reference hcsProfileRef = new Reference();
        hcsProfileRef.setReference(
                props.getProperty("capability.profile.healthcareservice"));

        Reference locProfileRef = new Reference();
        locProfileRef.setReference(
                props.getProperty("capability.profile.location"));

        Reference schedProfileRef = new Reference();
        schedProfileRef.setReference(
                props.getProperty("capability.profile.schedule"));

        List<CapabilityStatementRestComponent> restItems = response.getRest();
        CapabilityStatementRestComponent restItem = restItems.get(0);

        List<CapabilityStatementRestResourceComponent> resourceList = restItem.getResource();

        // Iterate through the resources we do handle and set the profiles.
        for (CapabilityStatementRestResourceComponent resourceItem : resourceList) {

            switch (resourceItem.getType()) {
                case "Appointment":
                    resourceItem.setProfile(apptProfRef);
                    break;

                case "Slot":
                    resourceItem.setProfile(slotProfRef);
                    break;
            }
        }

        /**
         * Now we add in the following resources, just so we can specify the
         * profiles we expect to use.
         *
         * Patient Practitioner PractitionerRole Organization HealthcareService
         * Location Schedule
         */
        CapabilityStatementRestResourceComponent patientRestItem = new CapabilityStatementRestResourceComponent();
        patientRestItem.setType("Patient").setProfile(patProfRef);
        resourceList.add(patientRestItem);

        CapabilityStatementRestResourceComponent practRestItem = new CapabilityStatementRestResourceComponent();
        practRestItem.setType("Practitioner").setProfile(practProfileRef);
        resourceList.add(practRestItem);

        CapabilityStatementRestResourceComponent practRoleRestItem = new CapabilityStatementRestResourceComponent();
        practRoleRestItem.setType("PractitionerRole").setProfile(practRoleProfileRef);
        resourceList.add(practRoleRestItem);

        CapabilityStatementRestResourceComponent orgRoleRestItem = new CapabilityStatementRestResourceComponent();
        orgRoleRestItem.setType("Organization").setProfile(orgProfileRef);
        resourceList.add(orgRoleRestItem);

        CapabilityStatementRestResourceComponent hcsRoleRestItem = new CapabilityStatementRestResourceComponent();
        hcsRoleRestItem.setType("HealthcareService").setProfile(hcsProfileRef);
        resourceList.add(hcsRoleRestItem);

        CapabilityStatementRestResourceComponent locRoleRestItem = new CapabilityStatementRestResourceComponent();
        locRoleRestItem.setType("Location").setProfile(locProfileRef);
        resourceList.add(locRoleRestItem);

        CapabilityStatementRestResourceComponent schedRoleRestItem = new CapabilityStatementRestResourceComponent();
        schedRoleRestItem.setType("Schedule").setProfile(schedProfileRef);
        resourceList.add(schedRoleRestItem);

        // Now put the updated list back in
        response.setRest(restItems);

        return response;
    }

    /**
     * Method to get the properties object with data from server.properties
     * file.
     *
     * @return A properties Object or null
     */
    private Properties getProperties() {
        Properties serverProperties = new Properties();
        InputStream input = null;
        String propsName = "server.properties";
        try {

            ClassLoader classLoader = getClass().getClassLoader();
            input = classLoader.getResource(propsName).openStream();
            serverProperties.load(input);
        }
        catch (IOException ex) {
            System.err.println("Error reading file " + propsName + " " + ex.getMessage());
            serverProperties = null;
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException e) {
                    System.err.println("Error closing file: " + propsName + " " + e.getMessage());
                }
            }
        }
        return serverProperties;
    }
}
