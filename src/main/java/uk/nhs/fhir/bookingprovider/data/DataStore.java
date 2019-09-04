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
package uk.nhs.fhir.bookingprovider.data;

import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.HealthcareService;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Schedule;
import org.hl7.fhir.dstu3.model.Slot;
import org.hl7.fhir.dstu3.model.Slot.SlotStatus;

/**
 * Singleton instance of an in memory data store.
 *
 * @author tim.coates@nhs.net
 */
public final class DataStore {

    /**
     * This is our Singleton instance.
     */
    private static DataStore instance = null;

    /**
     * Where all the profiles are pulled from.
     */
    private static final String PROFILEROOT
            = "https://fhir.hl7.org.uk/STU3/StructureDefinition/";

    /**
     * Where any Code Systems are pulled from.
     */
    private static final String CODESYSTEMROOT
            = "https://fhir.hl7.org.uk/STU3/CodeSystem/";

    /**
     * Logger we use throughout.
     */
    private static final Logger LOG
            = Logger.getLogger(DataStore.class.getName());
    /**
     * A List of PractitionerRole resources.
     */
    private ArrayList<PractitionerRole> practitionerRoles;
    /**
     * A List of Practitioner resources.
     */
    private ArrayList<Practitioner> practitioners;
    /**
     * A List of Organization resources.
     */
    private ArrayList<Organization> organizations;
    /**
     * A List of Location resources.
     */
    private ArrayList<Object> locations;
    /**
     * A List of HealthcareService resources.
     */
    private ArrayList<HealthcareService> healthcareServices;
    /**
     * A List of Schedule resources.
     */
    private ArrayList<Schedule> schedules;
    /**
     * A List of Slot resources.
     */
    private ArrayList<Slot> slots;
    /**
     * And finally a List of Appointment resources.
     */
    private ArrayList<Appointment> appointments;

    /**
     * Private Constructor to prevent unexpected instantiation (forces singleton
     * pattern).
     *
     */
    private DataStore() {
        this.practitionerRoles = null;
        this.practitioners = null;
        this.organizations = null;
        this.locations = null;
        this.healthcareServices = null;
        this.schedules = null;
        this.slots = null;
        this.appointments = null;
        LOG.info("New datastore being created and populated...");
    }

    /**
     * Method to get the singleton instance.
     *
     * @return Gets the only instance of our Singleton class.
     */
    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
            instance.initialize();
        }
        return instance;
    }

    /**
     * Method to get a single Slot by ID.
     *
     * @param id The id of the requested Slot (eg slot001).
     * @return The Slot resource.
     */
    public Slot getSlotByID(final String id) {
        // First we extract just the ID part from any id we've been sent...
        String[] words = id.split("/");
        String idPart = words[words.length - 1];

        for (Slot slot : slots) {
            Slot sl = (Slot) slot;
            String slotId = sl.getId();
            if (slotId.equals(idPart)) {
                return sl;
            }
        }
        return null;
    }

    /**
     * Method to get a set of Slots that have a given HealthcareService.
     *
     * @param hcsID The HealthcareService id.
     * @return A List of Slots which are provided by the specified
     * HealthcareService.
     */
    public ArrayList getSlotsByHealthcareService(final String hcsID) {

        ArrayList<Slot> result = new ArrayList();

        // First find out which schedules are run by this HealthcareService.
        ArrayList<String> scheds = new ArrayList();
        for (int i = 0; i < schedules.size(); i++) {
            Schedule sch = (Schedule) schedules.get(i);
            String schedID = sch.getId();
            List<Reference> actors = sch.getActor();
            for (Reference actor1 : actors) {
                String actor = actor1.getReference();
                if (actor.equals("/HealthcareService/" + hcsID)) {
                    scheds.add("/Schedule/" + schedID);
                }
            }
        }

        // Now step through Slots, and see whether they have that Schedule.
        for (Slot slot : slots) {
            Slot sl = (Slot) slot;
            String schedule = sl.getSchedule().getReference();
            for (String sched : scheds) {
                if (sched.equals(schedule)) {
                    result.add(sl);
                }
            }
        }
        return result;
    }

    /**
     * Method to get a set of Slots with a given status (free/busy) that are
     * provided by a given HealthcareService.
     *
     * @param hcsID The Id of a HealthcareService.
     * @param status The status being searched for.
     * @return A List of FREE Slot resources which are provided by the specified
     * HealthcareService.
     */
    public ArrayList getFreeSlotsByHCS(final String hcsID,
            final String status) {

        SlotStatus stat;
        switch (status) {
            case "free":
                stat = SlotStatus.FREE;
                break;

            case "busy":
                stat = SlotStatus.BUSY;
                break;

            default:
                throw new UnprocessableEntityException(new OperationOutcome());
        }
        ArrayList<Slot> result = new ArrayList();

        // First find out which schedules are run by this HealthcareService.
        ArrayList<String> scheds = new ArrayList();
        for (Schedule thisSchedule : schedules) {
            String schedID = thisSchedule.getId();
            List<Reference> actors = thisSchedule.getActor();
            for (Reference actor1 : actors) {
                String actor = actor1.getReference();
                if (actor.equals("/HealthcareService/" + hcsID)) {
                    scheds.add("/Schedule/" + schedID);
                }
            }
        }

        // Now step through all Slots, and see whether they have that Schedule.
        for (Slot slot : slots) {
            String schedule = slot.getSchedule().getReference();
            for (String sched : scheds) {
                if (sched.equals(schedule)) {
                    if (slot.getStatus() == stat) {
                        result.add(slot);
                    } else {
                        LOG.info("Excluding Slot: " + slot.getId() + " as not free.");
                    }
                }
            }
        }
        return result;
    }

    /**
     * Method to create a list of one PractitionerRole resources: - R0260.
     *
     * @return A List of (one) PractitionerRole resources.
     */
    public ArrayList makePractitionerRoles() {
        ArrayList<Object> practRoles = new ArrayList();

        PractitionerRole practRole = new PractitionerRole();
        Meta met = new Meta();
        String profileName = PROFILEROOT + "CareConnect-PractitionerRole-1";
        met.addProfile(profileName);
        practRole.setMeta(met);
        practRole.setId(new IdDt("R0260"));
        CodeableConcept code = new CodeableConcept();
        Coding codeCoding = new Coding();
        String codeSystemName = CODESYSTEMROOT + "CareConnect-SDSJobRoleName-1";
        codeCoding.setSystem(codeSystemName);
        codeCoding.setCode("R0260");
        codeCoding.setDisplay("General Medical Practitioner");
        code.addCoding(codeCoding);
        practRole.addCode(code);

        practRoles.add(practRole);

        return practRoles;
    }

    /**
     * Method to create a list of one Practitioner resources: - Dr Libbie Webber
     * - ABCD123456 - SDS User ID ABCD123456 - SDS Role ID R0260.
     *
     * @return A List of (one) Practitioners.
     */
    public ArrayList makePractitioners() {
        ArrayList<Object> practs = new ArrayList();

        Practitioner pract = new Practitioner();

        Meta met = new Meta();
        String profileName = PROFILEROOT + "CareConnect-Practitioner-1";
        met.addProfile(profileName);
        pract.setMeta(met);
        pract.setId(new IdDt("ABCD123456"));

        Identifier newSDSUserIdentifier = new Identifier();
        newSDSUserIdentifier.setSystem("https://fhir.nhs.uk/Id/sds-user-id");
        newSDSUserIdentifier.setValue("ABCD123456");
        pract.addIdentifier(newSDSUserIdentifier);

        Identifier newSDSRoleId = new Identifier();
        newSDSRoleId.setSystem("https://fhir.nhs.uk/Id/sds-role-profile-id");
        newSDSRoleId.setValue("R0260");
        pract.addIdentifier(newSDSRoleId);

        HumanName name = new HumanName();
        name.addPrefix("Dr");
        name.addGiven("Libbie");
        name.setFamily("Webber");
        pract.addName(name);

        practs.add(pract);

        return practs;
    }

    /**
     * Method to create a list of one Organisation resources: - A91545 - Name:
     * Our Provider Organisation - ODS Code: A91545.
     *
     * @return A List of Organisations.
     */
    public ArrayList makeOrganisations() {
        ArrayList<Object> orgs = new ArrayList();

        Organization org = new Organization();

        Meta met = new Meta();
        String profileName = PROFILEROOT + "CareConnect-Organization-1";
        met.addProfile(profileName);
        org.setMeta(met);
        org.setId(new IdDt("A91545"));

        Identifier newODSID = new Identifier();
        newODSID.setSystem("https://fhir.nhs.uk/Id/ods-organization-code");
        newODSID.setValue("A91545");
        org.addIdentifier(newODSID);

        org.setName("Our Provider Organisation");

        orgs.add(org);
        return orgs;
    }

    /**
     * Method to create a list of two Location resources: - Location One -
     * loc1111. - Location Two - loc2222.
     *
     * @return A List of Locations.
     */
    public ArrayList makeLocations() {
        ArrayList<Object> locs = new ArrayList();

        Location locn1 = new Location();
        Location locn2 = new Location();
        Meta met = new Meta();
        String profileName = PROFILEROOT + "CareConnect-Location-1";
        met.addProfile(profileName);
        locn1.setMeta(met);
        locn2.setMeta(met);

        locn1.setId(new IdDt("loc1111"));
        locn1.setName("Location One");
        locs.add(locn1);

        locn2.setId(new IdDt("loc2222"));
        locn2.setName("Location Two");
        locs.add(locn2);

        return locs;
    }

    /**
     * Method to create a list of two HealthcareService resources: Service One -
     * 918999198999 - Location: /Location/loc1111 - Organisation:
     * /Organization/A91545
     *
     * Service Two - 118111118111 - Location: /Location/loc2222 - Organisation:
     * /Organization/A91545
     *
     * @return A List of two HealthcareService resources.
     */
    public ArrayList makeHealthcareServices() {
        ArrayList<Object> hcServices = new ArrayList();
        HealthcareService hcs1 = new HealthcareService();
        HealthcareService hcs2 = new HealthcareService();
        Meta met = new Meta();
        String profileName = PROFILEROOT + "CareConnect-HealthcareService-1";
        met.addProfile(profileName);

        Reference providerRef = new Reference();
        providerRef.setReference("/Organization/A91545");

        Reference locRef = new Reference();

        hcs1.setMeta(met);
        hcs1.setId(new IdDt("918999198999"));
        hcs1.setName("Service One");
        hcs1.setProvidedBy(providerRef);
        locRef.setReference("/Location/loc1111");
        hcs1.addLocation(locRef);

        Identifier newId1 = new Identifier();
        newId1.setSystem("https://system.supplier.co.uk/My/Services");
        newId1.setValue("357");
        hcs1.addIdentifier(newId1);

        hcServices.add(hcs1);

        hcs2.setMeta(met);
        hcs2.setId(new IdDt("118111118111"));
        hcs2.setName("Service Two");
        hcs2.setProvidedBy(providerRef);
        locRef.setReference("/Location/loc2222");
        hcs2.addLocation(locRef);

        Identifier newId2 = new Identifier();
        newId2.setSystem("https://system.supplier.co.uk/My/Services");
        newId2.setValue("457");
        hcs2.addIdentifier(newId2);

        hcServices.add(hcs2);

        return hcServices;
    }

    /**
     * Method to create a list of two Schedule resources: - sched1111 -
     * Practitioner: /Practitioner/ABCD123456 - HealthcareService:
     * /HealthcareService/918999198999
     *
     * - sched2222 - Practitioner: /Practitioner/ABCD123456 - HealthcareService:
     * /HealthcareService/118111118111
     *
     * @return A List of Schedules.
     */
    public ArrayList makeSchedules() {
        ArrayList<Object> schedls = new ArrayList();

        Schedule sched1 = new Schedule();
        Schedule sched2 = new Schedule();
        Meta met = new Meta();
        String profileName = PROFILEROOT + "CareConnect-Schedule-1";
        met.addProfile(profileName);
        sched1.setMeta(met);
        sched2.setMeta(met);

        Reference hcsRef = new Reference();
        Reference practRef = new Reference();
        practRef.setReference("/Practitioner/ABCD123456");

        sched1.setId(new IdDt("sched1111"));
        hcsRef.setReference("/HealthcareService/918999198999");
        sched1.addActor(hcsRef);
        sched1.addActor(practRef);

        Identifier newId1 = new Identifier();
        newId1.setSystem("https://system.supplier.co.uk/MyDiary/Numbering");
        newId1.setValue("1015432");
        sched1.addIdentifier(newId1);
        schedls.add(sched1);

        sched2.setId(new IdDt("sched2222"));
        hcsRef = new Reference();
        hcsRef.setReference("/HealthcareService/118111118111");
        sched2.addActor(hcsRef);
        sched2.addActor(practRef);

        Identifier newId2 = new Identifier();
        newId2.setSystem("https://system.supplier.co.uk/MyDiary/Numbering");
        newId2.setValue("6543189");
        sched2.addIdentifier(newId2);
        schedls.add(sched2);

        return schedls;
    }

    /**
     * Method used to initially create a set of Slots for test use.
     *
     * @return An ArrayList of free Slots
     */
    public ArrayList makeSlots() {
        ArrayList<Object> slotList = new ArrayList();

        Slot slot;

        Meta met = new Meta();
        String profileName = PROFILEROOT + "CareConnect-Slot-1";
        met.addProfile(profileName);

        Reference schedRef = new Reference();

        schedRef.setReference("/Schedule/sched1111");

        // Set start time to 09:00 tomorrow...
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DATE, 1);

        for (int i = 0; i < 20; i++) {
            slot = new Slot();
            slot.setMeta(met);
            slot.setStatus(Slot.SlotStatus.FREE);
            slot.setSchedule(schedRef);

            slot.setId(new IdDt("slot" + String.format("%03d", i + 1)));
            slot.setStart(cal.getTime());
            // Set slot end 15 minutes after start
            cal.add(Calendar.MINUTE, 15);
            slot.setEnd(cal.getTime());
            slotList.add(slot);
        }

        schedRef = new Reference();
        schedRef.setReference("/Schedule/sched2222");

        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        for (int i = 0; i < 20; i++) {
            slot = new Slot();
            slot.setMeta(met);
            slot.setStatus(Slot.SlotStatus.FREE);
            slot.setSchedule(schedRef);

            slot.setId(new IdDt("slot" + String.format("%03d", i + 51)));
            slot.setStart(cal.getTime());
            // Set slot end 15 minutes after start
            cal.add(Calendar.MINUTE, 15);
            slot.setEnd(cal.getTime());
            slotList.add(slot);
        }

        return slotList;
    }

    /**
     * Method to save a POSTed appointment into our memory backed data store.
     *
     * @param newAppt The new Appointment to save.
     * @return The ID (a random UUID) assigned to the new appointment.
     */
    public IdDt addAppointment(final Appointment newAppt) {
        String newIDValue = UUID.randomUUID().toString();
        IdDt newID = new IdDt("Appointment", newIDValue, "1");
        newAppt.setId(newID);
        appointments.add(newAppt);
        return newID;
    }

    /**
     * Method to get a specific Appointment by Id.
     *
     * @param identifier of the appointment being requested.
     * @return The Appointment resource if found or null;
     */
    public Appointment getAppointment(final String identifier) {
        LOG.info("Request for appointment: " + identifier);
        Appointment appt;
        for (int i = 0; i < appointments.size(); i++) {
            appt = (Appointment) appointments.get(i);
            String thisone = "Appointment/" + appt.getIdElement().getIdPart();
            if (thisone.equals(identifier)) {
                LOG.info("Found it");
                return appt;
            }
        }
        return null;
    }

    /**
     * Method to set the status of a given Slot to Booked.
     *
     * @param id The id of the Slot to set as booked.
     */
    public void setSlotBooked(final String id) {

        // First we extract just the ID part from any id we've been sent...
        String[] words = id.split("/");
        String idPart = words[words.length - 1];

        LOG.info("Setting Slot " + idPart + " to 'BUSY'");

        for (int x = 0; x < slots.size(); x++) {
            Slot sl = (Slot) slots.get(x);
            String slotId = sl.getId();
            if (slotId.equals(idPart)) {
                LOG.info("Slot found: " + sl.toString());
                sl.setStatus(Slot.SlotStatus.BUSY);
                slots.remove(x);
                LOG.info("Slot removed: " + sl.toString());
                slots.add(sl);
                LOG.info("Slot added: " + sl.toString());
                return;
            }
        }
    }

    /**
     * Method used to access the private ArrayList of Slots, called from the
     * index page, to show current number of appointments.
     *
     * @return An ArrayList of the appointments that have been booked.
     */
    public ArrayList<Appointment> getAppointments() {
        return appointments;
    }

    /**
     * Method to access the private ArrayList of Slots. Called from the index
     * page to show the number of slots in the demonstrator.
     *
     * @return An ArrayList of all slots.
     */
    public ArrayList getSlots() {
        return slots;
    }

    /**
     * Method to retrieve a given Schedule object by name.
     *
     * @param schedName The name of the requested Schedule
     * @return The Schedule or null.
     */
    public Schedule getSchedule(final String schedName) {
        LOG.info("getSchedule() looking for: " + schedName);
        for (Schedule sched : schedules) {
            String thisID = "/Schedule/" + sched.getId();
            LOG.info("How about: " + thisID);
            if (thisID.equals(schedName)) {
                return sched;
            }
        }
        return null;
    }

    /**
     * Method to return a specific HealthcareService.
     *
     * @param identifier ID of the one we want.
     *
     * @return Either an instance of a HealthcareService resource or null.
     */
    public HealthcareService getHealthcareService(final String identifier) {
        LOG.info("Looking for: " + identifier);
        for (HealthcareService healthcareService : healthcareServices) {
            if (healthcareService.getId().equals(identifier)) {
                return healthcareService;
            } else {
                LOG.info("Not: " + healthcareService.getId());
            }
        }
        return null;
    }

    /**
     * Method used to set all the internal data structures ready for testing.
     * Sets all Slots to free and removes all Appointments.
     *
     */
    public void initialize() {
        practitionerRoles = makePractitionerRoles();
        practitioners = makePractitioners();
        organizations = makeOrganisations();
        locations = makeLocations();
        healthcareServices = makeHealthcareServices();
        schedules = makeSchedules();
        slots = makeSlots();
        appointments = new ArrayList();
        LOG.info("Reinitiated with a set of: "
                + appointments.size()
                + " appointments.");
    }

    /**
     * Method to get the ArrayList of free Slots. Called from the index page to
     * show the current status.
     *
     * @return An ArrayList of only the free Slots.
     */
    public ArrayList getFreeSlots() {
        ArrayList<Slot> freeSlots = new ArrayList<>();

        for (Slot n : slots) {
            if (n.getStatus() == SlotStatus.FREE) {
                freeSlots.add(n);
            }
        }
        return freeSlots;
    }

    /**
     * Method to get our one and only Practitioner
     *
     * @return The Practitioner resource.
     */
    public Practitioner getPractitioner() {
        return practitioners.get(0);
    }

    /**
     * Method to get our one and only PractitionerRole
     *
     * @return The PractitionerRole resource.
     */
    public PractitionerRole getPractitionerRole() {
        return practitionerRoles.get(0);
    }

    /**
     * Method to get our only Organisation resource.
     *
     * @return The Organisation or null.
     */
    public Organization getOrganization() {
        return organizations.get(0);
    }

    /**
     * Method to get a specific Location resource based on it's id.
     * @param locID The id of the requested Location.
     * @return The matching resource as an object.
     */
    public Object getLocation(String locID) {
        for(int i = 0; i < locations.size(); i++) {
            Location locn = (Location) locations.get(i);
            if(locn.getId().equals(locID))
            {
                return locn;
            }
        }
        return null;
    }

    /**
     * Method to update an Appointment to Cancelled or EnteredInError
     * 
     * @param identifier The identifier of the Slot we're manipulating
     * @param proposedStatus The status we've been asked to change it to
     */
    public void setAppointmentStatus(String identifier, Appointment.AppointmentStatus proposedStatus) {
        LOG.info("Trying to update: " + identifier);
        for (int i = 0; i < appointments.size(); i++) {
            Appointment appt = (Appointment) appointments.get(i);
            String thisone = "Appointment/" + appt.getId();
            LOG.info("Checking: " + thisone);
            if (thisone.equals(identifier)) {
                LOG.info("Found it");
                appt.setStatus(proposedStatus);
                //IIdType oldId = appt.getIdElement();
                //oldId.setParts(oldId.getBaseUrl(), oldId.getResourceType(), oldId.getIdPart(), Long.toString(oldId.getVersionIdPartAsLong() + 1));
                //appt.setId(oldId);
                //appointments.set(i, appt);
                break;
            }
        }
    }

    /**
     * Sets a slot back to free from booked.
     * @param id  The ID of the Slot we're manipulating.
     */
    public void setSlotFree(final String id) {
        // First we extract just the ID part from any id we've been sent...
        LOG.info("Setting Slot " + id + " back to free");
        String[] words = id.split("/");
        String idPart = words[words.length - 1];

        LOG.info("Setting Slot " + idPart + " to 'FREE'");

        for (int x = 0; x < slots.size(); x++) {
            Slot sl = (Slot) slots.get(x);
            String slotId = sl.getId();
            LOG.info("Trying " + slotId);
            if (slotId.equals(idPart)) {
                LOG.info("Slot found: " + sl.toString());
                sl.setStatus(Slot.SlotStatus.FREE);
                slots.remove(x);
                LOG.info("Slot removed: " + sl.toString());
                slots.add(sl);
                LOG.info("Slot added: " + sl.toString());
                return;
            }
        }
    }
}
