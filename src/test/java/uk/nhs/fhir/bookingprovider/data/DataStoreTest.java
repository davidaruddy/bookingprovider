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
package uk.nhs.fhir.bookingprovider.data;

import uk.nhs.fhir.bookingprovider.data.DataStore;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.Appointment.AppointmentParticipantComponent;
import org.hl7.fhir.dstu3.model.Attachment;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.DocumentReference;
import org.hl7.fhir.dstu3.model.DocumentReference.DocumentReferenceContentComponent;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.HealthcareService;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.Schedule;
import org.hl7.fhir.dstu3.model.Slot;
import org.hl7.fhir.dstu3.model.Slot.SlotStatus;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.fhir.dstu3.model.codesystems.NarrativeStatus;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
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
public class DataStoreTest {

    private static final Logger LOG = Logger.getLogger(DataStoreTest.class.getName());

    public DataStoreTest() {
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
     * Test of getSlotByID method, of class DataStore.
     */
    @Test
    public void testGetSlotByID() {
        System.out.println("getSlotByID");
        String id = "slot055";
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        // Set start time to 09:00 tomorrow...
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DATE, 1);

        Date expResult = cal.getTime();
        Slot result = instance.getSlotByID(id);
        assertEquals(expResult, result.getStart());
    }

    /**
     * Test of getSlotByID method, of class DataStore.
     */
    @Test
    public void testGetSlotByID1() {
        System.out.println("getSlotByID");
        String id = "slot055";
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        // Set start time to 09:00 tomorrow...
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 15);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DATE, 1);

        Date expResult = cal.getTime();
        Slot result = instance.getSlotByID(id);
        assertEquals(expResult, result.getEnd());
    }

    /**
     * Test of getSlotByID method, of class DataStore.
     */
    @Test
    public void testGetSlotByID2() {
        System.out.println("getSlotByID");
        String id = "slot055";
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        Slot result = instance.getSlotByID(id);
        String expResult = "/Schedule/sched2222";
        assertEquals(expResult, result.getSchedule().getReference());
    }

    /**
     * Test of getSlotsByHealthcareService method, of class DataStore.
     */
    @Test
    public void testGetSlotsByHealthcareService() {
        System.out.println("getSlotsByHealthcareService");
        String HCS = "918999198999";
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        int expResult = 20;
        ArrayList result = instance.getSlotsByHealthcareService(HCS);
        assertEquals(expResult, result.size());
    }

    /**
     * Test of MakePractitionerRoles method, of class DataStore.
     */
    @Test
    public void testMakePractitionerRoles() {
        System.out.println("MakePractitionerRoles");
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        ArrayList result = instance.makePractitionerRoles();
        assertEquals(result.size(), 1);
        PractitionerRole pr = (PractitionerRole) result.get(0);
        assertEquals(pr.getId(), "R0260");
    }

    /**
     * Test of MakePractitioners method, of class DataStore.
     */
    @Test
    public void testMakePractitioners() {
        System.out.println("MakePractitioners");
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        ArrayList result = instance.makePractitioners();
        assertEquals(result.size(), 1);
    }

    /**
     * Test of MakePractitioners method, of class DataStore.
     */
    @Test
    public void testMakePractitioners1() {
        System.out.println("MakePractitioners");
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        ArrayList result = instance.makePractitioners();
        Practitioner pr = (Practitioner) result.get(0);
        assertEquals(pr.getNameFirstRep().getFamily(), "Webber");
    }

    /**
     * Test of MakeOrganisations method, of class DataStore.
     */
    @Test
    public void testMakeOrganisations() {
        System.out.println("MakeOrganisations");
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        ArrayList result = instance.makeOrganisations();
        assertEquals(result.size(), 1);
    }

    /**
     * Test of MakeLocations method, of class DataStore.
     */
    @Test
    public void testMakeLocations() {
        System.out.println("MakeLocations");
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        ArrayList result = instance.makeLocations();
        assertEquals(result.size(), 2);
    }

    /**
     * Test of MakeLocations method, of class DataStore.
     */
    @Test
    public void testMakeLocations1() {
        System.out.println("MakeLocations");
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        ArrayList result = instance.makeLocations();
        Location loc = (Location) result.get(0);
        assertEquals(loc.getName(), "Location One");
    }

    /**
     * Test of MakeLocations method, of class DataStore.
     */
    @Test
    public void testMakeLocations2() {
        System.out.println("MakeLocations");
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        ArrayList result = instance.makeLocations();
        Location loc = (Location) result.get(1);
        assertEquals(loc.getName(), "Location Two");
    }

    /**
     * Test of MakeHealthcareServices method, of class DataStore.
     */
    @Test
    public void testMakeHealthcareServices() {
        System.out.println("MakeHealthcareServices");
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        ArrayList result = instance.makeHealthcareServices();
        assertEquals(2, result.size());
    }

    /**
     * Test of MakeHealthcareServices method, of class DataStore.
     */
    @Test
    public void testMakeHealthcareServices1() {
        System.out.println("MakeHealthcareServices");
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        ArrayList result = instance.makeHealthcareServices();
        HealthcareService hs1 = (HealthcareService) result.get(0);
        assertEquals("Service One", hs1.getName());
    }

    /**
     * Test of MakeHealthcareServices method, of class DataStore.
     */
    @Test
    public void testMakeHealthcareServices2() {
        System.out.println("MakeHealthcareServices");
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        ArrayList result = instance.makeHealthcareServices();
        HealthcareService hs2 = (HealthcareService) result.get(1);
        assertEquals("Service Two", hs2.getName());
    }

    /**
     * Test of MakeSchedules method, of class DataStore.
     */
    @Test
    public void testMakeSchedules() {
        System.out.println("MakeSchedules");
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        ArrayList result = instance.makeSchedules();
        assertEquals(2, result.size());
    }

    /**
     * Test of MakeSchedules method, of class DataStore.
     */
    @Test
    public void testMakeSchedules1() {
        System.out.println("MakeSchedules");
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        ArrayList result = instance.makeSchedules();
        Schedule sch1 = (Schedule) result.get(0);
        assertEquals("/HealthcareService/918999198999", sch1.getActorFirstRep().getReference());
    }

    /**
     * Test of MakeSchedules method, of class DataStore.
     */
    @Test
    public void testMakeSchedules2() {
        System.out.println("MakeSchedules");
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        ArrayList result = instance.makeSchedules();
        Schedule sch2 = (Schedule) result.get(1);
        assertEquals("/HealthcareService/118111118111", sch2.getActorFirstRep().getReference());
    }

    /**
     * Test of MakeSlots method, of class DataStore.
     */
    @Test
    public void testMakeSlots() {
        System.out.println("MakeSlots");
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        int expResult = 40;
        ArrayList result = instance.makeSlots();
        assertEquals(expResult, result.size());
    }

    /**
     * Test of addAppointment method, of class DataStore.
     */
    @Test
    public void testAddAppointment() {
        System.out.println("addAppointment");
        Appointment newAppt = makeAppointment(true);
        // Here we need to build a valid appointment

        DataStore instance = DataStore.getInstance();
        instance.initialize();
        int expResult = 36;
        String result = instance.addAppointment(newAppt);
        assertEquals(expResult, result.length());

        // Now we do some additional checks on the created Appt...
        Appointment retrieved = instance.getAppointment("Appointment/" + result);
        Appointment.AppointmentStatus theStatus = retrieved.getStatus();
        Appointment.AppointmentStatus expStatus = Appointment.AppointmentStatus.BOOKED;
        assertEquals(expStatus, theStatus);

        ArrayList contained = (ArrayList) retrieved.getContained();
        assertEquals(2, contained.size());
    }

    /**
     * Helper method to make an Appointment we'll use for testing...
     *
     * @param valid
     * @return
     */
    private Appointment makeAppointment(boolean valid) {
        Appointment newAppt = new Appointment();
        Meta met = new Meta();
        met.addProfile("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Appointment-1");
        newAppt.setMeta(met);

        // Now we add a contained Patient
        Patient pat = new Patient();
        pat.setId("P1");

        Identifier patNHSNumber = new Identifier();
        patNHSNumber.setUse(Identifier.IdentifierUse.OFFICIAL);
        patNHSNumber.setSystem("https://fhir.nhs.uk/Id/nhs-number");
        patNHSNumber.setValue("1231231234");
        pat.addIdentifier(patNHSNumber);

        HumanName name = new HumanName();
        name.setText("Mr Fred Smith");
        name.addPrefix("Mr");
        name.addGiven("Fred");
        name.setFamily("Smith");
        pat.addName(name);

        ContactPoint telecom = new ContactPoint();
        telecom.setUse(ContactPoint.ContactPointUse.HOME);
        telecom.setSystem(ContactPoint.ContactPointSystem.PHONE);
        telecom.setValue("01234 567 890");
        telecom.setRank(0);
        pat.addTelecom(telecom);

        pat.setGender(Enumerations.AdministrativeGender.MALE);
        pat.setBirthDate(new Date(65, 4, 21));

        Address addr = new Address();
        addr.setUse(Address.AddressUse.HOME);
        addr.addLine("123 High Street");
        addr.addLine("Leeds");
        addr.setCity("Leeds");
        addr.setPostalCode("LS1 4HR");
        pat.addAddress(addr);

        // We log out the contained Patient resource
        //LOG.info("Here's the Patient we'll contain: " + ResourceToString(pat));
        // And add it as another contained resource
        newAppt.addContained(pat);

        newAppt.setStatus(Appointment.AppointmentStatus.BOOKED);
        newAppt.setDescription("Reason for call here");

        Reference slotRef = new Reference();
        slotRef.setReference("/Slot/slot004");
        newAppt.addSlot(slotRef);

        newAppt.setCreated(new Date());

        AppointmentParticipantComponent patParticipant = new Appointment.AppointmentParticipantComponent();

        Reference patReference = new Reference();
        patReference.setReference("#P1");
        patReference.setIdentifier(patNHSNumber);
        patReference.setDisplay(name.getText());

        patParticipant.setActor(patReference);
        CodeableConcept partTypeConcept = new CodeableConcept();
        Coding patTypeCoding = new Coding();
        patTypeCoding.setCode("");
        partTypeConcept.addCoding(patTypeCoding);

        newAppt.addParticipant(patParticipant);

        // Now we add a contained DocumentReference...
        DocumentReference cdaRef = new DocumentReference();
        cdaRef.setId(UUID.randomUUID().toString());
        Identifier ident = new Identifier();
        ident.setSystem("uuid");
        ident.setValue("03e1c963-3da0-4330-9192-d4b1a5b349a9");
        cdaRef.addIdentifier(ident);
        cdaRef.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);

        cdaRef.setSubject(patReference);

        Coding typeCoding = new Coding();
        typeCoding.setSystem("urn:oid:2.16.840.1.113883.2.1.3.2.4.18.17");
        typeCoding.setCode("POCD_MT200001GB02");
        typeCoding.setDisplay("Integrated Urgent Care Report");
        CodeableConcept typeConcept = new CodeableConcept();
        typeConcept.addCoding(typeCoding);
        cdaRef.setType(typeConcept);
        cdaRef.setIndexed(new Date());
        Attachment attachment = new Attachment();
        attachment.setContentType("application/hl7-v3+xml");
        attachment.setLanguage("en");
        DocumentReferenceContentComponent drcc = new DocumentReference.DocumentReferenceContentComponent(attachment);
        cdaRef.addContent(drcc);

        // We log out the cotained CDA DocumentReference resource
        //LOG.info("Here's the DocumentReference we'll contain: " + ResourceToString(cdaRef));
        // And add it as a Contained resource
        newAppt.addContained(cdaRef);

        ArrayList containedResourceList = (ArrayList) newAppt.getContained();
        //LOG.info("Appointment has: " + containedResourceList.size() + " contained Resources");
        //for (int x = 0; x < containedResourceList.size(); x++) {
            //LOG.info(ResourceToString((Resource) containedResourceList.get(x)));
        //}

        //LOG.info("Here's the complete Appointment: " + ResourceToString(newAppt));

        return newAppt;
    }

    /**
     * Helper function to serialise a Resource
     *
     * @param input
     * @return
     */
    private String ResourceToString(Resource input) {
        FhirContext ctx = FhirContext.forDstu3();
        return ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(input);
    }

    /**
     * Test of getAppointment method, of class DataStore.
     */
    @Test
    public void testGetAppointments() {
        System.out.println("getAppointment");
        Appointment newAppt = makeAppointment(true);
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        instance.addAppointment(newAppt);
        instance.addAppointment(newAppt);
        instance.addAppointment(newAppt);

        ArrayList<Appointment> result = instance.getAppointments();
        assertEquals(3, result.size());
    }

    /**
     * Test of setSlotBooked method, of class DataStore.
     */
    @Test
    public void testSetSlotBooked() {
        System.out.println("setSlotBooked");
        String id = "";
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        instance.setSlotBooked("slot010");
        Slot sl = instance.getSlotByID("slot010");
        assertEquals(SlotStatus.BUSY, sl.getStatus());
    }

    /**
     * Test of getAppointments method, of class DataStore.
     */
    @Test
    public void testGetAppointment1() {
        System.out.println("addAppointment");
        Appointment newAppt = makeAppointment(true);
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        String result = instance.addAppointment(newAppt);
        //LOG.info("Created Appointment: " + result);
        Appointment appointment = instance.getAppointment("Appointment/" + result);
        //LOG.info("Retrieved Appointment: " + appointment.getText());
        assertEquals(result, appointment.getId());
    }

    /**
     * Test of getAppointments method, of class DataStore.
     */
    @Test
    public void testGetAppointment2() {
        System.out.println("addAppointment");
        Appointment newAppt = makeAppointment(true);
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        String result = instance.addAppointment(newAppt);
        //LOG.info("Created Appointment: " + result);
        Appointment appointment = instance.getAppointment("Appointment/" + result);
        //LOG.info("Retrieved Appointment: " + appointment.getText());
        List<UriType> profile = appointment.getMeta().getProfile();
        UriType prof1 = profile.get(0);
        String correctProfile = "https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Appointment-1";
        assertTrue(prof1.equals(correctProfile));
    }

    /**
     * Test of getAppointment method, of class DataStore.
     */
    @Test
    public void testGetAppointment() {
        System.out.println("getAppointment");
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        String identifier = instance.addAppointment(makeAppointment(true));
        Appointment result = instance.getAppointment("Appointment/" + identifier);
        assertEquals(identifier, result.getId());
    }

    /**
     * Test of getSlots method, of class DataStore.
     */
    @Test
    public void testGetSlots() {
        System.out.println("getSlots");
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        int expResult = 40;
        int result = instance.getSlots().size();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSchedule method, of class DataStore.
     */
    @Test
    public void testGetSchedule() {
        System.out.println("getSchedule");
        String schedName = "sched2222";
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        Schedule sched = instance.getSchedule("/Schedule/" + schedName);
        String result = sched.getId();
        assertEquals(schedName, result);
    }

    /**
     * Test of getSchedule method, of class DataStore.
     */
    @Test
    public void testGetSchedule1() {
        System.out.println("getSchedule");
        String schedName = "sched2222";
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        Schedule sched = instance.getSchedule("/Schedule/" + "othername");
        assertNull(sched);
    }

    /**
     * Test of getHealthcareService method, of class DataStore.
     */
    @Test
    public void testGetHealthcareService() {
        System.out.println("getHealthcareService");
        String identifier = "918999198999";
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        HealthcareService result = instance.getHealthcareService(identifier);
        assertEquals(identifier, result.getId());
    }

    /**
     * Test of getInstance method, of class DataStore.
     */
    @Test
    public void testGetInstance() {
        System.out.println("getInstance");
        DataStore result = DataStore.getInstance();
        assertNotNull(result);
    }

    /**
     * Test of initialize method, of class DataStore.
     */
    @Test
    public void testInitialize() {
        System.out.println("initialize");
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        assertEquals(40, instance.getSlots().size());
    }

    /**
     * Test of getFreeSlots method, of class DataStore.
     */
    @Test
    public void testGetFreeSlots() {
        System.out.println("getFreeSlots");
        DataStore instance = DataStore.getInstance();
        instance.setSlotBooked("slot001");
        int expResult = 39;
        ArrayList<Slot> result = instance.getFreeSlots();
        assertEquals(expResult, result.size());
    }

    /**
     * Test of getPractitioner method, of class DataStore.
     */
    @Test
    public void testGetPractitioner() {
        System.out.println("getPractitioner");
        DataStore instance = DataStore.getInstance();
        String expResult = "ABCD123456";
        Practitioner result = instance.getPractitioner();
        assertEquals(expResult, result.getId());
    }

    /**
     * Test of getPractitionerRole method, of class DataStore.
     */
    @Test
    public void testGetPractitionerRole() {
        System.out.println("getPractitionerRole");
        DataStore instance = DataStore.getInstance();
        String expResult = "R0260";
        PractitionerRole result = instance.getPractitionerRole();
        assertEquals(expResult, result.getId());
    }

    /**
     * Test of getOrganization method, of class DataStore.
     */
    @Test
    public void testGetOrganization() {
        System.out.println("getOrganization");
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        String expResult = "A91545";
        Organization result = instance.getOrganization();
        assertEquals(expResult, result.getId());
    }

    /**
     * Test of getFreeSlotsByHCS method, of class DataStore.
     */
    @Test
    public void testGetFreeSlotsByHCS() {
        System.out.println("getFreeSlotsByHCS");
        String hcsID = "918999198999";
        String status = "free";
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        int expResult = 20;
        ArrayList result = instance.getFreeSlotsByHCS(hcsID, status);
        assertEquals(expResult, result.size());
    }

    /**
     * Test of getFreeSlotsByHCS method, of class DataStore.
     */
    @Test
    public void testGetFreeSlotsByHCS2() {
        System.out.println("getFreeSlotsByHCS");
        String hcsID = "118111118111";
        String status = "free";
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        instance.setSlotBooked("slot053");
        int expResult = 19;
        ArrayList result = instance.getFreeSlotsByHCS(hcsID, status);
        assertEquals(expResult, result.size());
    }
    
    

    /**
     * Test of getLocation method, of class DataStore.
     */
    @Test
    public void testGetLocation() {
        System.out.println("getLocation");
        String locID = "loc1111";
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        String expResult = "Location One";
        Location result = (Location) instance.getLocation(locID);
        assertEquals(expResult, result.getName());
    }

        /**
     * Test of getLocation method, of class DataStore.
     */
    @Test
    public void testGetLocation2() {
        System.out.println("getLocation");
        String locID = "nothingmatching";
        DataStore instance = DataStore.getInstance();
        instance.initialize();
        Location result = (Location) instance.getLocation(locID);
        assertNull(result);
    }

}
