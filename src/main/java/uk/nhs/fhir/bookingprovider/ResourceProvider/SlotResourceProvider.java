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
package uk.nhs.fhir.bookingprovider.ResourceProvider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.hl7.fhir.dstu3.model.HealthcareService;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Schedule;
import org.hl7.fhir.dstu3.model.Slot;
import uk.nhs.fhir.bookingprovider.data.DataStore;

/**
 *
 * @author tim.coates@nhs.net
 */
/**
 * All resource providers must implement IResourceProvider
 */
public class SlotResourceProvider implements IResourceProvider {

    /**
     * The logger we'll use throughout this class.
     */
    private static final Logger LOG = Logger.getLogger(SlotResourceProvider.class.getName());

    /**
     * FHIR Context
     */
    FhirContext myContext;

    /**
     * The in memory data store where we cache Slots and other objects.
     */
    private DataStore data;

    /**
     * Constructor that we pass in shared objects to.
     *
     * @param ctx The overall FHIR Context we're using.
     * @param newData The shared in memory data store we're using.
     */
    public SlotResourceProvider(FhirContext ctx, DataStore newData) {
        myContext = ctx;
        data = newData;
    }

    /**
     * The getResourceType method comes from IResourceProvider, and must be
     * overridden to indicate what type of resource this provider supplies.
     */
    @Override
    public Class<Slot> getResourceType() {
        return Slot.class;
    }

    /**
     * The "@Read" annotation indicates that this method supports the read
     * operation. Read operations should return a single resource instance.
     *
     * @param theId The read operation takes one parameter, which must be of
     * type IdDt and must be annotated with the "@Read.IdParam" annotation.
     * @return Returns a resource matching this identifier, or null if none
     * exists.
     */
    @Read()
    public Slot getResourceById(@IdParam IdType theId) {
        LOG.info("Request for Slot: " + theId.getIdPart());
        Slot mySlot = data.getSlotByID(theId.getIdPart());
        mySlot.addIdentifier();
        return mySlot;
    }

    /**
     * The "@Search" annotation indicates that this method supports the search
     * operation. You may have many different method annotated with this
     * annotation, to support many different search criteria. This example
     * searches by family name.
     *
     * @return This method returns a list of all Slots.
     */
    @Search()
    public List<Slot> searchSlots() {
        ArrayList<Slot> slots = data.getSlots();
        LOG.info("Returned " + slots.size() + " slots.");
        return slots;
    }

    /**
     * The "@Search" annotation indicates that this method supports the search
     * operation. You may have many different method annotated with this
     * annotation, to support many different search criteria. This example
     * searches by family name.
     *
     * @param theHealthcareService The Service that Slots are being filtered to.
     * @param theIncludes Set of Resource types to be included in the response.
     * @return This method returns a list of Slots. This list may contain
     * multiple matching resources, or it may also be empty.
     */
    @Search()
    public List<IResource> searchSlots(
            @Description(shortDefinition = "This is the HealthcareService for which Slots are being requested - set this to the ASID of the Provider service")
            @RequiredParam(name = "schedule.actor:healthcareservice") TokenParam theHealthcareService,
            @OptionalParam(name = "Slot.status") TokenParam statusToken,
            @IncludeParam(allow = {
                "Slot:schedule",
                "Schedule:actor:healthcareservice",
                "Schedule:actor:Practitioner",
                "Schedule:actor:PractitionerRole",
                "HealthcareService.providedBy"}) Set<Include> theIncludes) {

        boolean incSchedule = false;
        boolean incHealthcareService = false;
        boolean incPractitionerRole = false;
        boolean incPractitioner = false;
        boolean incProvider = false;

        LOG.info("Slot search being handled for provider: " +
                theHealthcareService.getValue().toString());

        Iterator<Include> itr = theIncludes.iterator();
        while (itr.hasNext()) {
            String inc = itr.next().getValue();
            LOG.info("Include: " + inc);

            if (inc.equals("Slot:schedule")) {
                incSchedule = true;
                LOG.info("Will include Schedules");
            }
            if (inc.equals("Schedule:actor:healthcareservice")) {
                incHealthcareService = true;
            }
            if (inc.equals("Schedule:actor:Practitioner")) {
                incPractitioner = true;
            }
            if (inc.equals("Schedule:actor:PractitionerRole")) {
                incPractitionerRole = true;
            }
            if (inc.equals("HealthcareService.providedBy")) {
                incProvider = true;
            }
        }

        ArrayList slots;
        // Here we filter for free or busy if requested (ignoring other statuses)
        if (statusToken == null) {
            slots = data.getSlotsByHealthcareService(theHealthcareService.getValue());
        } else {
            if (statusToken.getValue().equals("free") || statusToken.getValue().equals("busy")) {
                slots = data.getFreeSlotsByHealthcareService(theHealthcareService.getValue(), statusToken.getValue());
            } else {
                throw new UnprocessableEntityException("Slot.status only supported with values of 'free' or 'busy'.");
            }
        }
        if (incSchedule) {
            // Now iterate through the Slots and get a list of Schedules...
            ArrayList<String> schedNames = new ArrayList<>();
            for (Object sl : slots) {
                Slot thisSlot = (Slot) sl;
                String reference = thisSlot.getSchedule().getReference();
                
                // Don't add the resource if we've already got it!!
                if (!schedNames.contains(reference)) {
                    LOG.info("Will add Schedule: " + reference);
                    schedNames.add(reference);
                }
            }
            for (String schedName : schedNames) {
                Schedule sch = data.getSchedule(schedName);
                if (sch == null) {
                    LOG.info("Null returned when getting: " + schedName);
                } else {
                    LOG.info("Got schedule: " + schedName);
                    slots.add(sch);
                }
            }
        }

        // Check whether they've asked for the HealthcareService to be included
        if (incHealthcareService) {
            if (slots.size() > 0) {
                LOG.info("Asked to add HealthcareService");
                HealthcareService healthcareService = data.getHealthcareService(theHealthcareService.getValue());
                if (healthcareService != null) {
                    LOG.info("Adding HealthcareService");
                    slots.add(healthcareService);
                }
            }
        }

        // Check whether they've asked for the Practitioner to be included
        if (incPractitioner) {
            LOG.info("Asked to add Practitioner");
            if (slots.size() > 0) {
                LOG.info("Adding Practitioner");
                slots.add(data.getPractitioner());
            }
        }

        // Check whether they've asked for the PractitionerRole to be included
        if (incPractitionerRole) {
            LOG.info("Asked to add PractitionerRole");
            if (slots.size() > 0) {
                LOG.info("Adding PractitionerRole");
                slots.add(data.getPractitionerRole());
            }
        }

        // Check whether they've asked for the Organization to be included
        if (incProvider) {
            LOG.info("Asked to add the Organization");
            if (slots.size() > 0) {
                LOG.info("Adding the Organization");
                slots.add(data.getOrganization());
            }
        }
        LOG.info("Returned " + slots.size() + " slots.");
        return slots;
    }

}
