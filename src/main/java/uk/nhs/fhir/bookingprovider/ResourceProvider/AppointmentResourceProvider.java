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
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.Appointment.AppointmentStatus;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Slot;
import uk.nhs.fhir.bookingprovider.checkers.AppointmentChecker;
import uk.nhs.fhir.bookingprovider.checkers.Fault;
import uk.nhs.fhir.bookingprovider.data.DataStore;
import uk.nhs.fhir.bookingprovider.logging.ExternalLogger;

/**
 *
 * @author tim.coates@nhs.net
 */

/**
 * Resource provider which handles any requests for objects of type Appointment
 * resource.
 *
 * All resource providers must implement IResourceProvider.
 */
public class AppointmentResourceProvider implements IResourceProvider {

    /**
     * The logger we'll use throughout this Class.
     */
    private static final Logger LOG =
            Logger.getLogger(AppointmentResourceProvider.class.getName());

    private ExternalLogger ourLogger;

    /**
     * FHIR Context we're operating within.
     */
    private FhirContext myContext;

    /**
     * DataStore where we hold all resources in memory.
     */
    private DataStore myData;

    /**
     * Object we're going to use to validate Appointment Objects.
     */
    private AppointmentChecker myChecker;

    /**
     * Constructor that we pass in any shared objects to.
     *
     * @param ctx The overall FHIR context we're using.
     * @param newData Our DataStore, the in memory object we use to cache Slots
     *                and other objects.
     * @param newChecker The object we'll use to check Appointments conform.
     */
    public AppointmentResourceProvider(final FhirContext ctx,
            final DataStore newData,
            final AppointmentChecker newChecker,
            final ExternalLogger newLogger) {

        /**
         * Local handle to a FHIR Context.
         */
        myContext = ctx;

        /**
         * Local handle to the DataStore.
         */
        myData = newData;

        /**
         * Checker object we'll use.
         */
        myChecker = newChecker;

        /**
         * Logger we use to log results out
         */
        ourLogger = newLogger;

        LOG.info("New AppointmentResourceProvider created");
    }

    /**
     * The getResourceType method comes from IResourceProvider, and must be
     * overridden to indicate what type of resource this provider supplies.
     */
    @Override
    public Class<Appointment> getResourceType() {
        return Appointment.class;
    }

    /**
     * Method to book (create a new) Appointment resource.
     *
     * @param newAppt The new Appointment resource.
     * @return Returns the results of trying to create a new Appointment object.
     */
    @Create
    public MethodOutcome createAppointment(@ResourceParam Appointment newAppt,
        HttpServletRequest theRequest,
        HttpServletResponse theResponse) {
        LOG.info("createAppointment() called");
        if(theRequest.getQueryString() != null) {
            ourLogger.log("Request: " + theRequest.getAttribute("uk.nhs.fhir.bookingprovider.requestid") + " creating Appointment: " + theRequest.getRequestURL() + "?" + theRequest.getQueryString());
        } else {
            ourLogger.log("Request: " + theRequest.getAttribute("uk.nhs.fhir.bookingprovider.requestid") + " creating Appointment: " + theRequest.getRequestURL());
        }

        ArrayList<Fault> faults = myChecker.checkThis(newAppt);
        if (!faults.isEmpty()) {
            for (Fault item : faults) {
                LOG.severe(item.toString());
            }
            String faultMsg = "";
            for (int x = 0; x < faults.size(); x++) {
                if (x == 10) {
                    break;
                }
                faultMsg = faultMsg + faults.get(0).toString() + "\n";
            }
            throw new UnprocessableEntityException("Validation found: "
                    + faults.size()
                    + " problems (max 10 described here):\n"
                    + faultMsg);
        }

        if (newAppt == null) {
            throw new UnprocessableEntityException("No Appointment");
        } else {
            //LOG.info("Appointment was not null");
        }
        /*
         * First we might want to do business validation. The
         * UnprocessableEntityException results in an HTTP 422, which is
         * appropriate for a business rule failure.
         */
        ArrayList<Reference> slots = (ArrayList) newAppt.getSlot();
        Reference slotReference = slots.get(0);
        String slotRef = slotReference.getReference();

        Slot theSlot = myData.getSlotByID(slotRef);
        if (theSlot == null) {
            String notFoundErr = "Specified slot was not found on this server";
            throw new UnprocessableEntityException(notFoundErr);
        } else {
            LOG.info("Got a Slot back from DataStore");
        }

        if (theSlot.getStatus() != Slot.SlotStatus.FREE) {
            String notFreeErr = "The specified Slot: "
                    + slotRef
                    + " is not currently free.";
            LOG.info(notFreeErr);
            throw new UnprocessableEntityException(notFreeErr);
        } else {
            LOG.info("Slot " + slotRef + " is currently free");
        }

        // Save this Appointment to the database...
        IdDt result = myData.addAppointment(newAppt);
        if (result == null) {
            throw new UnprocessableEntityException("Couldn't save Appointment");
        } else {
            LOG.info("Setting Slot " + slotRef + " to BUSY");
            myData.setSlotBooked(slotRef);
            newAppt.setId(result);
        }

        // This method returns a MethodOutcome object which contains
        // the ID (composed of the type Patient, the logical ID 3746, and the
        // version ID 1)
        MethodOutcome retVal = new MethodOutcome();
        retVal.setId(result);

        retVal.setResource(newAppt);
        retVal.setId(result);
        ourLogger.log("Response for: " + theRequest.getAttribute("uk.nhs.fhir.bookingprovider.requestid") + " created Appointment: " + result);
        return retVal;
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
    public Appointment getResourceById(@IdParam IdType theId,
        HttpServletRequest theRequest,
        HttpServletResponse theResponse) {
        if(theRequest.getQueryString() != null) {
            ourLogger.log("Request: " + theRequest.getAttribute("uk.nhs.fhir.bookingprovider.requestid") + " getting Appointment: " + theRequest.getRequestURL() + "?" + theRequest.getQueryString());
        } else {
            ourLogger.log("Request: " + theRequest.getAttribute("uk.nhs.fhir.bookingprovider.requestid") + " getting Appointment: " + theRequest.getRequestURL());
        }
        Appointment myAppt = myData.getAppointment(theId.toString());
        ourLogger.log("Response: " + theRequest.getAttribute("uk.nhs.fhir.bookingprovider.requestid") + " got Appointment: " + myAppt.getId());
        return myAppt;
    }

    /**
     * This returns ALL Appointments.
     *
     * @return This method returns a list of Patients. This list may contain
     * multiple matching resources, or it may also be empty.
     */
    @Search()
    public List<Appointment> getAppointment(
        HttpServletRequest theRequest,
        HttpServletResponse theResponse) {
        LOG.info("Asked for all appointments");
        ourLogger.log("Request: " + theRequest.getAttribute("uk.nhs.fhir.bookingprovider.requestid") + " to get all Appointments");
        ArrayList<Appointment> appointments = myData.getAppointments();
        ourLogger.log("Response: " + theRequest.getAttribute("uk.nhs.fhir.bookingprovider.requestid") + " got: " + appointments.size() + " appointments");
        return appointments;
    }

    /**
     * The update method should ONLY to be used to change an appointment from
     * booked to either cancelled or entered in error.
     *
     * @param theId
     * @param newAppt
     * @return
     */
    @Update()
    public MethodOutcome updateAppointment(@IdParam IdType theId,
            @ResourceParam Appointment newAppt,
            HttpServletRequest theRequest,
            HttpServletResponse theResponse) {
        JsonParser jp = (JsonParser) FhirContext.forDstu3().newJsonParser();
        LOG.info("Got resource:");
        LOG.info(jp.encodeResourceToString(newAppt));
        ourLogger.log("Request: " + theRequest.getAttribute("uk.nhs.fhir.bookingprovider.requestid") + " updating Appointment: " + theRequest.getRequestURL());
        MethodOutcome retVal = new MethodOutcome();
        String identifier = "Appointment/" + theId.getIdPart();
        String resourceID = newAppt.getId();
        LOG.info("updateAppointment() called for ID: " + identifier);
        AppointmentStatus proposedStatus = newAppt.getStatus();

        // Check what status they're changing it to...
        if(proposedStatus != AppointmentStatus.CANCELLED &&
                proposedStatus != AppointmentStatus.ENTEREDINERROR) {
            throw new UnprocessableEntityException("Status not accepted.");
        }

        // Now check the Appointment exists...
        Appointment currentAppt = myData.getAppointment(identifier);
        if(currentAppt == null) {
            throw new UnprocessableEntityException("Appointment " + identifier + " not found.");
        }

        // Now check they're referring to the same Slot
        if(newAppt.getSlot().size() != 1) {
            if(newAppt.getSlot().size() == 0) {
                throw new UnprocessableEntityException("Appointment does not refer to a Slot.");
            } else {
                throw new UnprocessableEntityException("Appointment refers to multiple Slots.");
            }
        }
        String slotId = newAppt.getSlotFirstRep().getReference();
        LOG.info("New Appt slot ID: " + slotId);
        String currentSlotId = currentAppt.getSlotFirstRep().getReference();
        LOG.info("Booked Appt slot ID: " + currentSlotId);
        if( ! slotId.equals(currentSlotId)) {
            throw new UnprocessableEntityException("Appointment refers to a different Slot.");
        }

        // Update the Appointment
        myData.setAppointmentStatus(identifier, proposedStatus);
        switch(proposedStatus) {
            case CANCELLED:
                ourLogger.log("Request: " + theRequest.getAttribute("uk.nhs.fhir.bookingprovider.requestid") + " Appointment: " + identifier + " updated to 'cancelled'.");
                break;
            case ENTEREDINERROR:
                ourLogger.log("Request: " + theRequest.getAttribute("uk.nhs.fhir.bookingprovider.requestid") + " Appointment: " + identifier + " updated to 'enteredinerror'.");
                break;
        }
        LOG.info("Appointment updated");

        // Update the Slot
        myData.setSlotFree(slotId);
        ourLogger.log("Request: " + theRequest.getAttribute("uk.nhs.fhir.bookingprovider.requestid") + " Slot: " + slotId + " set back to free.");
        LOG.info("Slot set back to free");

        String newVersionString = "2";
        retVal.setResource(myData.getAppointment(identifier));
        retVal.setId(new IdType("Appointment", identifier, newVersionString));

        return retVal;
    }
}
