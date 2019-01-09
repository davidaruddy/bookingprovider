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
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
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
 * Handles all RESTful FHIR requests for Slot resources.
 *
 * All resource providers must implement IResourceProvider.
 */
public class SlotResourceProvider implements IResourceProvider {

    /**
     * The logger we'll use throughout this class.
     */
    private static final Logger LOG
            = Logger.getLogger(SlotResourceProvider.class.getName());

    /**
     * FHIR Context.
     */
    private FhirContext myContext;

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
    public SlotResourceProvider(final FhirContext ctx,
            final DataStore newData) {
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
     * operation.You may have many different method annotated with this
     * annotation, to support many different search criteria. This example
     * searches by HealthcareService and Status.
     *
     * @param theHealthcareService The Service that Slots are being filtered to.
     * @param statusToken The status filter we are requested for.
     * @param theIncludes Set of Resource types to be included in the response.
     * @return This method returns a list of Slots. This list may contain
     * multiple matching resources, or it may also be empty.
     */
    @Search()
    public List<IResource> searchSlots(
            @Description(shortDefinition = "This is the HealthcareService for "
                    + "which Slots are being requested - set this to the ASID "
                    + "of the Provider service")
            @RequiredParam(name = "schedule.actor:healthcareservice") TokenParam theHealthcareService,
            @OptionalParam(name = Slot.SP_STATUS) TokenParam statusToken,
            @OptionalParam(name = Slot.SP_START) DateRangeParam startRange,
            @IncludeParam(allow = {
        "Slot:schedule",
        "Schedule:actor:HealthcareService",
        "Schedule:actor:Practitioner",
        "Schedule:actor:PractitionerRole",
        "HealthcareService.providedBy"}) Set<Include> theIncludes) {

        boolean incSchedule = false;
        boolean incHealthcareService = false;
        boolean incPractitionerRole = false;
        boolean incPractitioner = false;
        boolean incProvider = false;
        DateParam lowerBound = null;
        DateParam upperBound = null;

        LOG.info("Slot search being handled for provider: "
                + theHealthcareService.getValue().toString());

        if (startRange != null) {
            lowerBound = startRange.getLowerBound();
            upperBound = startRange.getUpperBound();

            LOG.info("Date range is from: " + startRange.getLowerBoundAsInstant());
            LOG.info("to: " + startRange.getUpperBoundAsInstant());

        }

        // Here we process the array of Includes we've been asked for...
        Iterator<Include> itr = theIncludes.iterator();
        while (itr.hasNext()) {
            String inc = itr.next().getValue();
            LOG.info("Include: " + inc);

            // Decide what this include is and set a boolean for each one we support.
            switch(inc) {
                case "Slot:schedule":
                    incSchedule = true;
                    break;
                    
                case "Schedule:actor:HealthcareService":
                    incHealthcareService = true;
                    break;
                    
                case "Schedule:actor:Practitioner":
                    incPractitioner = true;
                    break;
                    
                case "Schedule:actor:PractitionerRole":
                    incPractitionerRole = true;
                    break;
                    
                case "HealthcareService.providedBy":
                    incProvider = true;
                    break;
                    
                default:
                    LOG.info("Unexpected include sent: " + inc);
            }
        }

        ArrayList slots;
        // Here we filter for free or busy if requested (ignoring other statuses)
        if (statusToken == null) {
            slots = data.getSlotsByHealthcareService(
                    theHealthcareService.getValue()
            );
        } else {
            if (statusToken.getValue().equals("free")
                    || statusToken.getValue().equals("busy")) {
                slots = data.getFreeSlotsByHCS(theHealthcareService.getValue(),
                        statusToken.getValue());
            } else {
                String statusErr
                        = "Slot.status values only 'free' or 'busy' supported.";
                throw new UnprocessableEntityException(statusErr);
            }
        }

        // Now we copy items that fit the start date filter into filteredSlots.
        ArrayList filteredSlots = new ArrayList();

        if (startRange != null) {
            for (Object sl : slots) {
                boolean lowerOkay = false;
                boolean upperOkay = false;
                Slot thisSlot = (Slot) sl;
                if (lowerBound != null) {
                    switch (lowerBound.getPrefix()) {
                        case APPROXIMATE:
                        case EQUAL:
                            if (thisSlot.getStart().equals(lowerBound.getValue())) {
                                lowerOkay = true;
                            }
                            break;
                        case ENDS_BEFORE:
                        case NOT_EQUAL:
                        case STARTS_AFTER:
                            String endsBeforeErrMsg = "ENDS_BEFORE, NOT_EQUAL, STARTS_AFTER not currently supported";
                            throw new UnprocessableEntityException(endsBeforeErrMsg);

                        case GREATERTHAN:
                            switch (lowerBound.getPrecision()) {
                                case MILLI:
                                    if (thisSlot.getStart().after(lowerBound.getValue())) {
                                        lowerOkay = true;
                                    }
                                    break;
                                default:
                                    String notMillisErrMsg = "Currently requires dates to be accurate to milliseconds";
                                    throw new UnprocessableEntityException(notMillisErrMsg);
                            }
                            break;
                        case GREATERTHAN_OR_EQUALS:
                            switch (lowerBound.getPrecision()) {
                                case MILLI:
                                    if (thisSlot.getStart().after(lowerBound.getValue())
                                            || thisSlot.getStart().equals(lowerBound.getValue())) {
                                        lowerOkay = true;
                                    }
                                    break;
                                default:
                                    String notMillisErrMsg = "Currently requires dates to be accurate to milliseconds";
                                    throw new UnprocessableEntityException(notMillisErrMsg);
                            }
                            break;
                        case LESSTHAN:
                            switch (lowerBound.getPrecision()) {
                                case MILLI:
                                    if (thisSlot.getStart().before(lowerBound.getValue())) {
                                        lowerOkay = true;
                                    }
                                    break;
                                default:
                                    String notMillisErrMsg = "Currently requires dates to be accurate to milliseconds";
                                    throw new UnprocessableEntityException(notMillisErrMsg);
                            }
                            break;
                        case LESSTHAN_OR_EQUALS:
                            switch (lowerBound.getPrecision()) {
                                case MILLI:
                                    if (thisSlot.getStart().before(lowerBound.getValue())
                                            || thisSlot.getStart().equals(lowerBound.getValue())) {
                                        lowerOkay = true;
                                    }
                                    break;
                                default:
                                    String notMillisErrMsg = "Currently requires dates to be accurate to milliseconds";
                                    throw new UnprocessableEntityException(notMillisErrMsg);
                            }
                            break;
                    }
                } else {
                    lowerOkay = true;
                }
                if (upperBound != null) {
                    switch (upperBound.getPrefix()) {
                        case APPROXIMATE:
                        case EQUAL:
                            if (thisSlot.getStart().equals(upperBound.getValue())) {
                                upperOkay = true;
                            }
                            break;
                        case ENDS_BEFORE:
                        case NOT_EQUAL:
                        case STARTS_AFTER:
                            String endsBeforeErrMsg = "ENDS_BEFORE, NOT_EQUAL, STARTS_AFTER not currently supported";
                            throw new UnprocessableEntityException(endsBeforeErrMsg);

                        case GREATERTHAN:
                            switch (upperBound.getPrecision()) {
                                case MILLI:
                                    if (thisSlot.getStart().after(upperBound.getValue())) {
                                        upperOkay = true;
                                    }
                                    break;
                                default:
                                    String notMillisErrMsg = "Currently requires dates to be accurate to milliseconds";
                                    throw new UnprocessableEntityException(notMillisErrMsg);
                            }
                            break;
                        case GREATERTHAN_OR_EQUALS:
                            switch (upperBound.getPrecision()) {
                                case MILLI:
                                    if (thisSlot.getStart().after(upperBound.getValue())
                                            || thisSlot.getStart().equals(upperBound.getValue())) {
                                        upperOkay = true;
                                    }
                                    break;
                                default:
                                    String notMillisErrMsg = "Currently requires dates to be accurate to milliseconds";
                                    throw new UnprocessableEntityException(notMillisErrMsg);
                            }
                            break;
                        case LESSTHAN:
                            switch (lowerBound.getPrecision()) {
                                case MILLI:
                                    if (thisSlot.getStart().before(upperBound.getValue())) {
                                        upperOkay = true;
                                    }
                                    break;
                                default:
                                    String notMillisErrMsg = "Currently requires dates to be accurate to milliseconds";
                                    throw new UnprocessableEntityException(notMillisErrMsg);
                            }
                            break;
                        case LESSTHAN_OR_EQUALS:
                            switch (upperBound.getPrecision()) {
                                case MILLI:
                                    if (thisSlot.getStart().before(upperBound.getValue())
                                            || thisSlot.getStart().equals(upperBound.getValue())) {
                                        upperOkay = true;
                                    }
                                    break;
                                default:
                                    String notMillisErrMsg = "Currently requires dates to be accurate to milliseconds";
                                    throw new UnprocessableEntityException(notMillisErrMsg);
                            }
                            break;
                    }
                } else {
                    upperOkay = true;
                }
                if (upperOkay && lowerOkay) {
                    filteredSlots.add(thisSlot);
                }
            }
        } else {
            filteredSlots.addAll(slots);
        }

        if (incSchedule) {
            // Now iterate through the Slots and get a list of Schedules...
            ArrayList<String> schedNames = new ArrayList<>();
            for (Object sl : filteredSlots) {
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
                    filteredSlots.add(sch);
                }
            }
        }

        // Check whether they've asked for the HealthcareService to be included
        if (incHealthcareService) {
            if (filteredSlots.size() > 0) {
                LOG.info("Asked to add HealthcareService");
                HealthcareService healthcareService
                        = data.getHealthcareService(
                                theHealthcareService.getValue());
                if (healthcareService != null) {
                    LOG.info("Adding HealthcareService");
                    filteredSlots.add(healthcareService);
                }
            }
        }

        // Check whether they've asked for the Practitioner to be included
        if (incPractitioner) {
            LOG.info("Asked to add Practitioner");
            if (filteredSlots.size() > 0) {
                LOG.info("Adding Practitioner");
                filteredSlots.add(data.getPractitioner());
            }
        }

        // Check whether they've asked for the PractitionerRole to be included
        if (incPractitionerRole) {
            LOG.info("Asked to add PractitionerRole");
            if (filteredSlots.size() > 0) {
                LOG.info("Adding PractitionerRole");
                filteredSlots.add(data.getPractitionerRole());
            }
        }

        // Check whether they've asked for the Organization to be included
        if (incProvider) {
            LOG.info("Asked to add the Organization");
            if (filteredSlots.size() > 0) {
                LOG.info("Adding the Organization");
                filteredSlots.add(data.getOrganization());
            }
        }
        LOG.info("Returned " + filteredSlots.size() + " slots.");
        return filteredSlots;
    }

}
