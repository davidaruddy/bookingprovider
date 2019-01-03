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
 * @author tim.coates
 */
public class DataStore {

    private static DataStore instance = null;

    private static final Logger LOG = Logger.getLogger(DataStore.class.getName());

    ArrayList<PractitionerRole> PractitionerRoles = null;
    ArrayList<Practitioner> Practitioners = null;
    ArrayList<Organization> Organizations = null;
    ArrayList<Object> Locations = null;
    ArrayList<HealthcareService> HealthcareServices = null;
    ArrayList<Schedule> Schedules = null;
    ArrayList<Slot> Slots = null;
    ArrayList<Appointment> Appointments = null;

    /**
     * Private Constructor
     *
     */
    private DataStore() {
        LOG.info("New datastore being created and populated...");
    }

    /**
     * Method to get the singleton instance.
     *
     * @return
     */
    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
            instance.initialize();
        }
        return instance;
    }

    /**
     * Method to get a single Slot by ID
     *
     * @param id
     * @return
     */
    public Slot getSlotByID(String id) {
        // First we extract just the ID part from any id we've been sent...
        String[] words = id.split("/");
        id = words[words.length - 1];

        for (int x = 0; x < Slots.size(); x++) {
            Slot sl = (Slot) Slots.get(x);
            String slotId = sl.getId();
            if (slotId.equals(id)) {
                return sl;
            }
        }
        return null;
    }

    /**
     * Method to get a set of Slots that have a given HealthcareService
     *
     * @param HCS
     * @return
     */
    public ArrayList getSlotsByHealthcareService(String HCS) {

        ArrayList<Slot> result = new ArrayList();

        // First find out which schedules are run by the selected HealthcareService...
        ArrayList<String> scheds = new ArrayList();
        for (int i = 0; i < Schedules.size(); i++) {
            Schedule sch = (Schedule) Schedules.get(i);
            String schedID = sch.getId();
            List<Reference> actors = sch.getActor();
            for (int j = 0; j < actors.size(); j++) {
                String actor = actors.get(j).getReference();
                if (actor.equals("/HealthcareService/" + HCS)) {
                    scheds.add("/Schedule/" + schedID);
                }
            }
        }

        // Now we step through all Slots, and see whether they have a matching Schedule...
        for (int x = 0; x < Slots.size(); x++) {
            Slot sl = (Slot) Slots.get(x);
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
     * Method to get a set of FREE Slots that have a given HealthcareService
     *
     * @param HCS
     * @return
     */
    public ArrayList getFreeSlotsByHealthcareService(String HCS, String status) {

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

        // First find out which schedules are run by the selected HealthcareService...
        ArrayList<String> scheds = new ArrayList();
        for (Schedule Schedule : Schedules) {
            Schedule sch = (Schedule) Schedule;
            String schedID = sch.getId();
            List<Reference> actors = sch.getActor();
            for (Reference actor1 : actors) {
                String actor = actor1.getReference();
                if (actor.equals("/HealthcareService/" + HCS)) {
                    scheds.add("/Schedule/" + schedID);
                }
            }
        }

        // Now we step through all Slots, and see whether they have a matching Schedule...
        for (Slot Slot : Slots) {
            Slot sl = (Slot) Slot;
            String schedule = sl.getSchedule().getReference();
            for (String sched : scheds) {
                if (sched.equals(schedule)) {
                    if (sl.getStatus() == stat) {
                        result.add(sl);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Method to create a list of one PractitionerRole resources: - R0260
     *
     * @return
     */
    public ArrayList MakePractitionerRoles() {
        ArrayList<Object> PractitionerRoles = new ArrayList();

        PractitionerRole PR = new PractitionerRole();
        Meta met = new Meta();
        met.addProfile("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-PractitionerRole-1");
        PR.setMeta(met);
        PR.setId(new IdDt("R0260"));
        CodeableConcept code = new CodeableConcept();
        Coding codeCoding = new Coding();
        codeCoding.setSystem("https://fhir.hl7.org.uk/STU3/CodeSystem/CareConnect-SDSJobRoleName-1");
        codeCoding.setCode("R0260");
        codeCoding.setDisplay("General Medical Practitioner");
        code.addCoding(codeCoding);
        PR.addCode(code);

        PractitionerRoles.add(PR);

        return PractitionerRoles;
    }

    /**
     * Method to create a list of one Practitioner resources: - Dr Libbie Webber
     * - ABCD123456 - SDS User ID ABCD123456 - SDS Role ID R0260
     *
     * @return
     */
    public ArrayList MakePractitioners() {
        ArrayList<Object> Practitioners = new ArrayList();

        Practitioner pract = new Practitioner();

        Meta met = new Meta();
        met.addProfile("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Practitioner-1");
        pract.setMeta(met);
        pract.setId(new IdDt("ABCD123456"));

        Identifier newSDSUserIdentifier = new Identifier();
        newSDSUserIdentifier.setSystem("https://fhir.nhs.uk/Id/sds-user-id");
        newSDSUserIdentifier.setValue("ABCD123456");
        pract.addIdentifier(newSDSUserIdentifier);

        Identifier newSDSRoleIdentifier = new Identifier();
        newSDSRoleIdentifier.setSystem("https://fhir.nhs.uk/Id/sds-role-profile-id");
        newSDSRoleIdentifier.setValue("R0260");
        pract.addIdentifier(newSDSRoleIdentifier);

        HumanName name = new HumanName();
        name.addPrefix("Dr");
        name.addGiven("Libbie");
        name.setFamily("Webber");
        pract.addName(name);

        Practitioners.add(pract);

        return Practitioners;
    }

    /**
     * Method to create a list of one Organisation resources: - A91545 - Name:
     * Our Provider Organisation - ODS Code: A91545
     *
     * @return
     */
    public ArrayList MakeOrganisations() {
        ArrayList<Object> Organisations = new ArrayList();

        Organization org = new Organization();

        Meta met = new Meta();
        met.addProfile("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Organization-1");
        org.setMeta(met);
        org.setId(new IdDt("A91545"));

        Identifier newODSID = new Identifier();
        newODSID.setSystem("https://fhir.nhs.uk/Id/ods-organization-code");
        newODSID.setValue("A91545");
        org.addIdentifier(newODSID);

        org.setName("Our Provider Organisation");

        Organisations.add(org);
        return Organisations;
    }

    /**
     * Method to create a list of two Location resources: - Location One -
     * loc1111
     *
     * - Location Two - loc2222
     *
     * @return
     */
    public ArrayList MakeLocations() {
        ArrayList<Object> Locations = new ArrayList();

        Location locn1 = new Location();
        Location locn2 = new Location();
        Meta met = new Meta();
        met.addProfile("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Location-1");
        locn1.setMeta(met);
        locn2.setMeta(met);

        locn1.setId(new IdDt("loc1111"));
        locn1.setName("Location One");
        Locations.add(locn1);

        locn2.setId(new IdDt("loc2222"));
        locn2.setName("Location Two");
        Locations.add(locn2);

        return Locations;
    }

    /**
     * Method to create a list of two HealthcareService resources: - Service One
     * - 918999198999 - Location: /Location/loc1111 - Organisation:
     * /Organization/A91545
     *
     * - Service Two - 118111118111 - Location: /Location/loc2222 -
     * Organisation: /Organization/A91545
     *
     * @return
     */
    public ArrayList MakeHealthcareServices() {
        ArrayList<Object> HealthcareServices = new ArrayList();
        HealthcareService hcs1 = new HealthcareService();
        HealthcareService hcs2 = new HealthcareService();
        Meta met = new Meta();
        met.addProfile("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-HealthcareService-1");
        hcs1.setMeta(met);
        hcs2.setMeta(met);

        hcs1.setId(new IdDt("918999198999"));
        hcs2.setId(new IdDt("118111118111"));

        hcs1.setName("Service One");
        hcs2.setName("Service Two");

        Reference providerRef = new Reference();
        providerRef.setReference("/Organization/A91545");

        hcs1.setProvidedBy(providerRef);
        hcs2.setProvidedBy(providerRef);

        Reference locRef = new Reference();

        locRef.setReference("/Location/loc1111");
        hcs1.addLocation(locRef);

        locRef.setReference("/Location/loc2222");
        hcs2.addLocation(locRef);

        HealthcareServices.add(hcs1);
        HealthcareServices.add(hcs2);

        return HealthcareServices;
    }

    /**
     * Method to create a list of two Schedule resources: - sched1111 -
     * Practitioner: /Practitioner/ABCD123456 - HealthcareService:
     * /HealthcareService/918999198999
     *
     * - sched2222 - Practitioner: /Practitioner/ABCD123456 - HealthcareService:
     * /HealthcareService/118111118111
     *
     * @return
     */
    public ArrayList MakeSchedules() {
        ArrayList<Object> Schedules = new ArrayList();

        Schedule sched1 = new Schedule();
        Schedule sched2 = new Schedule();
        Meta met = new Meta();
        met.addProfile("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Schedule-1");
        sched1.setMeta(met);
        sched2.setMeta(met);

        Reference hcsRef = new Reference();
        Reference practRef = new Reference();
        practRef.setReference("/Practitioner/ABCD123456");

        sched1.setId(new IdDt("sched1111"));
        hcsRef.setReference("/HealthcareService/918999198999");
        sched1.addActor(hcsRef);
        sched1.addActor(practRef);
        Schedules.add(sched1);

        sched2.setId(new IdDt("sched2222"));
        hcsRef = new Reference();
        hcsRef.setReference("/HealthcareService/118111118111");
        sched2.addActor(hcsRef);
        sched2.addActor(practRef);
        Schedules.add(sched2);

        return Schedules;
    }

    public ArrayList MakeSlots() {
        ArrayList<Object> Slots = new ArrayList();

        Slot slot;

        Meta met = new Meta();
        met.addProfile("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Slot-1");

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
            Slots.add(slot);
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
            Slots.add(slot);
        }

        return Slots;
    }

    /**
     * Method to save a POSTed appointment into our memory backed data store
     *
     * @param newAppt
     * @return
     */
    public String addAppointment(Appointment newAppt) {
        String newID = UUID.randomUUID().toString();
        newAppt.setId(newID);
        Appointments.add(newAppt);
        //LOG.info("Appointment: " + newID + " added");
        return newID;
    }

    /**
     * Method to get a specific Appointment by Id
     *
     * @param identifier
     * @return
     */
    public Appointment getAppointment(String identifier) {
        LOG.info("Request for appointment: " + identifier);
        Appointment appt;
        for (int i = 0; i < Appointments.size(); i++) {
            appt = (Appointment) Appointments.get(i);
            String thisone = "Appointment/" + appt.getId();
            //LOG.info("Trying appointment: [" + thisone + "] comparing to [" + identifier + "]");
            if (thisone.equals(identifier)) {
                LOG.info("Found it");
                return appt;
            }
        }
        return null;
    }

    /**
     * Method to set the status of a given Slot to Booked
     *
     * @param id
     */
    public void setSlotBooked(String id) {

        // First we extract just the ID part from any id we've been sent...
        String[] words = id.split("/");
        id = words[words.length - 1];

        LOG.info("Setting Slot " + id + " to 'BUSY'");

        for (int x = 0; x < Slots.size(); x++) {
            Slot sl = (Slot) Slots.get(x);
            String slotId = sl.getId();
            if (slotId.equals(id)) {
                LOG.info("Slot found: " + sl.toString());
                sl.setStatus(Slot.SlotStatus.BUSY);
                Slots.remove(x);
                LOG.info("Slot removed: " + sl.toString());
                Slots.add(sl);
                LOG.info("Slot added: " + sl.toString());
                return;
            }
        }
        return;
    }

    public ArrayList<Appointment> getAppointments() {
        //LOG.info("Returning a set of: " + Appointments.size() + " appointments.");
        return Appointments;
    }

    public ArrayList<Slot> getSlots() {
        return Slots;
    }

    public Schedule getSchedule(String schedName) {
        LOG.info("getSchedule() looking for: " + schedName);
        for (Schedule sched : Schedules) {
            String thisID = "/Schedule/" + sched.getId();
            LOG.info("How about: " + thisID);
            if (thisID.equals(schedName)) {
                return sched;
            }
        }
        return null;
    }

    /**
     * Method to return a specific HealthcareService
     *
     * @param identifier ID of the one we want.
     *
     * @return Either an instance of a HealthcareService resource or null.
     */
    public HealthcareService getHealthcareService(String identifier) {
        LOG.info("Looking for: " + identifier);
        for (HealthcareService healthcareService : HealthcareServices) {
            if (healthcareService.getId().equals(identifier)) {
                return healthcareService;
            } else {
                LOG.info("Not: " + healthcareService.getId());
            }
        }
        return null;
    }

    public void initialize() {
        PractitionerRoles = MakePractitionerRoles();
        Practitioners = MakePractitioners();
        Organizations = MakeOrganisations();
        Locations = MakeLocations();
        HealthcareServices = MakeHealthcareServices();
        Schedules = MakeSchedules();
        Slots = MakeSlots();
        Appointments = new ArrayList();
        LOG.info("Reinitiated with a set of: " + Appointments.size() + " appointments.");
    }

    public ArrayList<Slot> getFreeSlots() {
        ArrayList<Slot> freeSlots = new ArrayList<>();

        for (Slot n : Slots) {
            if (n.getStatus() == SlotStatus.FREE) {
                freeSlots.add(n);
            }
        }
        return freeSlots;
    }

    /**
     * Method to get our one and only Practitioner
     *
     * @return
     */
    public Practitioner getPractitioner() {
        return Practitioners.get(0);
    }

    /**
     * Method to get our one and only PractitionerRole
     *
     * @return
     */
    public PractitionerRole getPractitionerRole() {
        return PractitionerRoles.get(0);
    }

    public Organization getOrganization() {
        return Organizations.get(0);
    }

}
