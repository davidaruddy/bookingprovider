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
package uk.nhs.fhir.bookingprovider.checkers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.Appointment.AppointmentParticipantComponent;
import org.hl7.fhir.dstu3.model.DocumentReference;
import org.hl7.fhir.dstu3.model.Enumeration;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.UriType;

/**
 * Class that performs all of the business rule checks against an appointment
 * object.
 *
 * @author tim.coates@nhs.net
 */
public class AppointmentChecker {

    /**
     * The Logger object we use for all logging in this class.
     */
    private static final Logger LOG = Logger.getLogger(
            AppointmentChecker.class.getName()
    );

    /**
     * The root where we store profiles.
     */
    private static final String PROFILEROOT
            = "https://fhir.hl7.org.uk/STU3/StructureDefinition/";

    /**
     * The name of this specific profile.
     */
    private static final String PROFILENAME
            = PROFILEROOT + "CareConnect-Appointment-1";

    /**
     * The system used to indicate an identifier is an NHS Number.
     */
    String NHSNUMSYSTEM = "https://fhir.nhs.uk/Id/nhs-number";

    /**
     * Error strings live here.
     */
    private static final String NULLPROFILE
            = "Appointment has a null profile.";
    private static final String OTHERPROFILES
            = "Appointment has OTHER profile(s).";
    private static final String WRONGPROFILE
            = "Appointment does NOT have correct profile.";
    private static final String NULLLANG
            = "Appointment has a language of 'null'";
    private static final String BADLANG
            = "Appointment language not 'en' / 'en-GB'";
    private static final String BADSTATUS
            = "Appointment must have a status of Booked";
    private static final String NOSTATUS
            = "Appointment does not have a 'Status'";
    private static final String MULTISLOTERR
            = "Appointment references multiple Slots";
    private static final String NULLSLOTERR
            = "Slot reference is null";
    private static final String SLOTREFNULLERR
            = "Reference value of Slot is null";
    private static final String BADSLOTREFERR
            = "Slot reference doesn't contain 'Slot/' so invalid.";
    private static final String NOSLOTERR
            = "Appointment does not reference a Slot";
    private static final String CREATENULLERR
            = "Appointment created date is set to null";
    private static final String CREATEFUTUREERR
            = "Appointment created date is in the future";
    private static final String CREATEPASTERR
            = "Appointment created date appears to be in the past.";
    private static final String NOCREATEERR
            = "Appointment has no 'created' date";
    private static final String NOPARTSERR
            = "Appointment has no participants, therefore no Patient?";
    private static final String MULTIPARTSERR
            = "Appointment has multiple participants, may be confusing?";
    private static final String PARTACTORNULLERR = "Participant actor is null";
    private static final String NOTNHSNUMERR
            = "Participant identifier does not seem to be an NHS Number?";
    private final String PARTSYSERR
            = "Appointment has a participant | actor | Identifier "
            + "with System other than " + NHSNUMSYSTEM;
    private static final String PARTOFFICIALERR
            = "Appointment has a participant | actor | Identifier with Use "
            + "other than OFFICIAL";
    private static final String NOPARTERR
            = "Appointment has no Participant (Patient!).";
    private static final String INVALIDSUPINFOERR
            = "supportingInformation is invalid.";
    private static final String BADSUPINFOREF
            = "SupportingInformation does not point to contained DocumentReference resource";
    private static final String MULTISUPINFOERR
            = "Multiple supportingInformation references, causing confusion.";
    private static final String NOSUPINFOERR
            = "Appointment doesn't have a supportingInformation (pointing to a"
            + " contained DocumentReference resource).";

    private static final String PATMISMATCHERR
            = "No linked Participant Patient in Contained resources.";
    private static final String VALIDATIONERROR
            = "ERROR received when validating the resource, use: https://data.developer.nhs.uk/ccri/term/validate";
    private static final String VALIDATIONFATAL
            = "FATAL received when validating the resource, use: https://data.developer.nhs.uk/ccri/term/validate";
    private static final String VALIDATIONWARNING
            = "WARNING received when validating the resource, use: https://data.developer.nhs.uk/ccri/term/validate";

    String localDocRefReference;
    FhirContext ctx;
    IGenericClient client;

    /**
     * Constructor which takes in the FhirContext from the Servlet, to allow us
     * to create a Fhir client, to POST the resource to the CCRI validator.
     *
     * @param mainContext A HAPI FhirContext object.
     */
    public AppointmentChecker(FhirContext mainContext) {
        this.ctx = mainContext;
        String serverBase = "https://data.developer.nhs.uk/ccri-fhir/STU3";
        client = ctx.newRestfulGenericClient(serverBase);
    }

    /**
     * Private Constructor to ensure we always get the Context.
     */
    private AppointmentChecker() {
    }

    /**
     * Checks an Appointment object passed in for conformance to a number of
     * business rules.
     *
     * Currently the only public method defined.
     *
     * @param appointment The object to be checked.
     * @return A List of any faults found, hopefully of zero length.
     */
    public final ArrayList<Fault> checkThis(final Appointment appointment) {
        ArrayList<Fault> results = new ArrayList<Fault>();

        // First check no ID was sent
        if (appointment.hasId()) {
            results.add(
                    new Fault("Appointment has an ID - if POST this is incorrect (ID will be set by server).",
                            Severity.MINOR)
            );
        }

        // Now check profile
        Meta meta = appointment.getMeta();
        if (meta == null) {
            results.add(
                    new Fault("Appointment has no Meta element.",
                            Severity.MAJOR)
            );
        } else {
            results.addAll(
                    followsProfile(meta, PROFILENAME));
        }

        // Check for the correct language.
        results.addAll(checkLanguage(appointment));

        // Check status
        results.addAll(checkStatus(appointment));

        // Check Slot
        results.addAll(checkSlot(appointment));

        // Check created
        results.addAll(checkCreated(appointment));

        // Check participant
        results.addAll(checkParticipant(appointment));

        // Check supportingInformation is a valid Reference...
        localDocRefReference = null;
        results.addAll(checksupportingInfo(appointment));

        // Check that participant actor links to a contained resource.
        results.addAll(checkPatientLink(appointment));
        
        // Send the resource for validation by CCRI
        results.addAll(validateAppointment(appointment));

        // Finally check contained resources...
        boolean hasDocRef = false;
        boolean hasPatient = false;
        if (appointment.hasContained()) {
            List<Resource> containedList = appointment.getContained();
            if (containedList.size() < 2) {
                results.add(
                        new Fault("Appointment expected to have 2 contained resources (Patient and DocumentReference).",
                                Severity.MAJOR)
                );
            } else {
                if (containedList.size() > 2) {
                    results.add(
                            new Fault("Appointment has more than 2 (actually " + containedList.size() + ")contained resources?",
                                    Severity.MINOR)
                    );
                }
                for (Resource resource : containedList) {
                    if (resource.getResourceType() == ResourceType.DocumentReference || resource.getResourceType() == ResourceType.Patient) {

                        // Here DocumentReference
                        if (resource.getResourceType() == ResourceType.DocumentReference) {
                            hasDocRef = true;
                            DocumentReference docRef = (DocumentReference) resource;
                            if (docRef.hasId()) {
                                String docRefID = "#" + docRef.getId();
                                //String docRefID = docRef.getId();
                                LOG.info("Checking that " + localDocRefReference + " equals " + docRefID);
                                // TODO: Add check that supportingInformation refers to the embedded DocumentReference
                                //if (!localDocRefReference.toLowerCase().trim().equals(docRefID.toLowerCase().trim())) {
                                //    results.add(new Fault("supportingInformation reference: " + localDocRefReference + " does NOT point to contained DocumentReference resource: " + docRefID, Severity.CRITICAL));
                                //}
                            }
                            if (docRef.hasIdentifier()) {
                                List<Identifier> identList = docRef.getIdentifier();
                                if (identList.isEmpty()) {
                                    results.add(
                                            new Fault("Contained Document Reference has zero Identifiers",
                                                    Severity.MAJOR)
                                    );
                                } else {
                                    if (identList.size() > 1) {
                                        results.add(
                                                new Fault("Contained DocumentReference has multiple identifiers, risks confusion.",
                                                        Severity.MINOR)
                                        );
                                    } else {
                                        Identifier docRefIdentifier = identList.get(0);
                                        if (docRefIdentifier == null) {
                                            results.add(
                                                    new Fault("Contained DocumentReference has a null identifier.",
                                                            Severity.MAJOR)
                                            );
                                        } else {
                                            // Check System
                                            if (docRefIdentifier.hasSystem()) {
                                                String system = docRefIdentifier.getSystem();
                                                if (system == null) {
                                                    results.add(
                                                            new Fault("Contained DocumentReference has an identifier with a NULL System.",
                                                                    Severity.MAJOR)
                                                    );
                                                } else {
                                                    if (system.equals("")) {
                                                        results.add(
                                                                new Fault("Contained DocumentReference has an identifier with an empty System.",
                                                                        Severity.MAJOR)
                                                        );
                                                    } else {
                                                        if (!system.equals("https://tools.ietf.org/html/rfc4122")) {
                                                            results.add(
                                                                    new Fault("Contained DocumentReference has an Identifier with System not set to 'https://tools.ietf.org/html/rfc4122'.",
                                                                            Severity.MAJOR)
                                                            );
                                                        }
                                                    }
                                                }
                                            } else {
                                                results.add(
                                                        new Fault("Contained DocumentReference has an identifier with no System defined.",
                                                                Severity.MAJOR)
                                                );
                                            }

                                            // Check Value...
                                            if (docRefIdentifier.hasValue()) {
                                                String value = docRefIdentifier.getValue();
                                                if (value == null) {
                                                    results.add(
                                                            new Fault("Contained DocumentReference has an identifier with a null Value.",
                                                                    Severity.MAJOR)
                                                    );
                                                } else {
                                                    if (value.equals("")) {
                                                        results.add(
                                                                new Fault("Contained DocumentReference has an identifier with an empty Value.",
                                                                        Severity.MAJOR)
                                                        );
                                                    } else {
                                                        if (value.length() < 5) {
                                                            results.add(
                                                                    new Fault("Contained DocumentReference has an identifier with a short (< 5) Value.",
                                                                            Severity.MAJOR)
                                                            );
                                                        }
                                                        if (value.length() > 36) {
                                                            results.add(
                                                                    new Fault("Contained DocumentReference has an identifier with a long (> 36) Value.",
                                                                            Severity.MAJOR)
                                                            );
                                                        }
                                                    }
                                                }
                                            } else {
                                                results.add(
                                                        new Fault("Contained DocumentReference has an identifier with no Value defined.",
                                                                Severity.MAJOR)
                                                );
                                            }
                                        }
                                    }
                                }
                            } else {
                                results.add(
                                        new Fault("Contained DocumentReference has no Identifier (to point to CDA)",
                                                Severity.MAJOR)
                                );
                            }
                        }

                        // Here Patient
                        if (resource.getResourceType() == ResourceType.Patient) {
                            hasPatient = true;
                            Patient patient = (Patient) resource;
                            PatientChecker checker = new PatientChecker();
                            results.addAll(checker.checkThis(patient));
                        }
                    } else {
                        results.add(
                                new Fault("One of the contained resources is not of the expected types.",
                                        Severity.MAJOR)
                        );
                    }
                }
            }
        } else {
            results.add(
                    new Fault("Appointment has no contained resources",
                            Severity.CRITICAL)
            );
        }
        if (!hasDocRef) {
            results.add(
                    new Fault("Appointment has no contained DocumentReference.",
                            Severity.CRITICAL)
            );
        }
        if (!hasPatient) {
            results.add(
                    new Fault("Appointment has no contained Patient.",
                            Severity.CRITICAL)
            );
        }
        return results;
    }

    /**
     * Method to check a Meta section of a resource and see whether it claims to
     * follow a given profile.
     *
     * @param meta The Meta object
     * @param profileName The full URL of the profile being checked for.
     * @return Boolean response.
     */
    private ArrayList<Fault> followsProfile(final Meta meta, final String profileName) {
        ArrayList<Fault> results = new ArrayList<>();
        boolean profileFound = false;

        List<UriType> profileList = meta.getProfile();
        if (profileList != null) {
            for (UriType profile : profileList) {
                if (profile == null) {
                    results.add(new Fault(NULLPROFILE, Severity.MAJOR));
                } else {
                    if (!profile.equals(profileName)) {
                        results.add(new Fault(OTHERPROFILES, Severity.TRIVIAL));
                    } else {
                        profileFound = true;
                    }
                }
            }
            if (profileFound == false) {
                results.add(new Fault(WRONGPROFILE, Severity.CRITICAL));
            }
        }
        return results;
    }

    /**
     * Method to check the resource has the expected Language set.
     *
     * @param appointment Appointment resource to be checked.
     * @return An ArrayList of problems found.
     */
    public ArrayList<Fault> checkLanguage(final Appointment appointment) {
        ArrayList<Fault> results = new ArrayList<>();

        if (appointment.hasLanguage()) {
            String language = appointment.getLanguage();
            if (language == null) {
                results.add(
                        new Fault(NULLLANG, Severity.MAJOR)
                );
            } else {
                if (!language.equals("en") && !language.equals("en-GB")) {
                    results.add(
                            new Fault(BADLANG, Severity.MAJOR)
                    );
                }
            }
        }
        return results;
    }

    /**
     * Method to verify that the Appointment being posted in is set to Booked.
     *
     * @param appointment The Appointment being tested.
     * @return A List of problems found, ideally of zero length.
     */
    public ArrayList<Fault> checkStatus(final Appointment appointment) {
        ArrayList<Fault> results = new ArrayList<>();

        if (appointment.hasStatus()) {
            Appointment.AppointmentStatus status = appointment.getStatus();
            switch (status) {
                case BOOKED:
                    break;
                default:
                    results.add(new Fault(BADSTATUS, Severity.MAJOR));
            }
        } else {
            results.add(new Fault(NOSTATUS, Severity.MAJOR));
        }
        return results;
    }

    /**
     * Method to check the Slot referenced in the posted appointment.
     *
     * @param appointment The appointment resource being checked.
     * @return A List of the problems found.
     */
    public ArrayList<Fault> checkSlot(final Appointment appointment) {
        ArrayList<Fault> results = new ArrayList<>();

        if (appointment.hasSlot()) {
            List<Reference> slotList = appointment.getSlot();
            if (slotList.size() != 1) {
                results.add(
                        new Fault(MULTISLOTERR, Severity.MAJOR)
                );
            } else {
                Reference slot = slotList.get(0);
                if (slot == null) {
                    results.add(
                            new Fault(NULLSLOTERR, Severity.MAJOR)
                    );
                } else {
                    String slotRef = slot.getReference();
                    if (slotRef == null) {
                        results.add(
                                new Fault(SLOTREFNULLERR, Severity.MAJOR)
                        );
                    } else {
                        if (!slotRef.contains("Slot/")) {
                            results.add(
                                    new Fault(BADSLOTREFERR, Severity.CRITICAL)
                            );
                        }
                    }
                }
            }
        } else {
            results.add(new Fault(NOSLOTERR, Severity.CRITICAL));
        }
        return results;
    }

    /**
     * Check a few aspects of the Created value.
     *
     * @param appointment The appointment resource being checked.
     * @return a List of Faults found.
     */
    public ArrayList<Fault> checkCreated(final Appointment appointment) {
        ArrayList<Fault> results = new ArrayList<>();
        if (appointment.hasCreated()) {
            Date created = appointment.getCreated();
            if (created == null) {
                results.add(new Fault(CREATENULLERR, Severity.MAJOR));
            } else {
                if (created.after(new Date())) {
                    results.add(new Fault(CREATEFUTUREERR, Severity.MAJOR));
                } else {
                    if (created.before(new Date(118, 11, 9))) {
                        results.add(new Fault(CREATEPASTERR, Severity.MAJOR));
                    }
                }
            }
        } else {
            results.add(new Fault(NOCREATEERR, Severity.MAJOR));
        }
        return results;
    }

    /**
     * Method to check the participant section of the Appointment resource we've
     * been asked to book.
     *
     * @param appointment The resource being tested.
     * @return A List of (ideally zero) Faults found.
     */
    public ArrayList<Fault> checkParticipant(final Appointment appointment) {
        ArrayList<Fault> results = new ArrayList<>();
        String localPatientReference = null;

        if (appointment.hasParticipant()) {
            List<AppointmentParticipantComponent> participantList = appointment.getParticipant();
            if (participantList.size() == 0) {
                results.add(
                        new Fault(NOPARTSERR, Severity.MINOR)
                );
            } else {
                if (participantList.size() > 1) {
                    results.add(
                            new Fault(MULTIPARTSERR, Severity.MINOR)
                    );
                }
                for (AppointmentParticipantComponent participant : participantList) {
                    Reference actor = participant.getActor();
                    if (actor == null) {
                        results.add(
                                new Fault(PARTACTORNULLERR, Severity.MAJOR)
                        );
                    } else {
                        localPatientReference = actor.getReference();
                        Identifier identifier = actor.getIdentifier();
                        if (identifier.getUse() == Identifier.IdentifierUse.OFFICIAL) {
                            if (identifier.getSystem().equals(NHSNUMSYSTEM)) {
                                String value = identifier.getValue();
                                if (!value.matches("\\d{10}")) {
                                    results.add(
                                            new Fault(NOTNHSNUMERR,
                                                    Severity.MAJOR)
                                    );
                                } else {
                                    // TODO: Add more checks in here?
                                }
                            } else {
                                results.add(
                                        new Fault(PARTSYSERR, Severity.MAJOR)
                                );
                            }
                        } else {
                            results.add(
                                    new Fault(PARTOFFICIALERR, Severity.MAJOR)
                            );
                        }
                    }
                }
            }
        } else {
            results.add(
                    new Fault(NOPARTERR, Severity.MAJOR)
            );
        }

        if (localPatientReference == null) {
            results.add(
                    new Fault(NOPARTERR, Severity.CRITICAL)
            );
        }
        return results;
    }

    /**
     * Method to check the supportingInformation is valid.
     *
     * @param appointment The appointment resource we've been asked to save.
     * @return A List of faults found.
     */
    public ArrayList<Fault> checksupportingInfo(final Appointment appointment) {
        ArrayList<Fault> results = new ArrayList<>();

        if (appointment.hasSupportingInformation()) {
            ArrayList<Reference> supportingInformationList = (ArrayList<Reference>) appointment.getSupportingInformation();
            if (supportingInformationList.isEmpty() || supportingInformationList == null) {
                results.add(
                        new Fault(INVALIDSUPINFOERR, Severity.MAJOR)
                );
            } else {
                if (supportingInformationList.size() == 1) {
                    localDocRefReference = supportingInformationList.get(0).getReference();
                    List<Resource> containedList = appointment.getContained();
                    boolean matched = false;
                    for (Resource res : containedList) {
                        if (res.getId().equals(localDocRefReference)) {
                            matched = true;
                        }
                    }
                    if (matched == false) {
                        results.add(new Fault(BADSUPINFOREF, Severity.MINOR));
                    }
                } else {
                    results.add(new Fault(MULTISUPINFOERR, Severity.MINOR));
                }
            }
        } else {
            results.add(new Fault(NOSUPINFOERR, Severity.MAJOR));
        }
        return results;
    }

    /**
     *
     * @param appointment
     * @return
     */
    public ArrayList<Fault> checkPatientLink(final Appointment appointment) {
        ArrayList<Fault> results = new ArrayList<>();
        boolean matched = false;

        JsonParser jp = (JsonParser) FhirContext.forDstu3().newJsonParser();

        //System.out.println("Appointment\n" + jp.encodeResourceToString(appointment) + "\n");
        List<AppointmentParticipantComponent> participants
                = appointment.getParticipant();
        //System.out.println("Found: " + participants.size() + " participants.");

        List<Resource> containList = appointment.getContained();

        for (AppointmentParticipantComponent participant : participants) {
            String participantRef = participant.getActor().getReference();
            LOG.info("Info " + participantRef);

            for (Resource res : containList) {
                String containedID = res.getId();
                LOG.info("Contained ID: " + containedID);
                if (participantRef.equals(containedID)) {
                    matched = true;
                }
            }

        }

        if (!matched) {
            results.add(new Fault(PATMISMATCHERR, Severity.MAJOR));
        }
        return results;
    }

    /**
     * Method to post the resource to the CCRI validator endpoint.
     *
     * @param appointment The resource we're validating.
     * @return An ArrayList of Faults we've identified.
     */
    public ArrayList<Fault> validateAppointment(final Appointment appointment) {
        ArrayList<Fault> results = new ArrayList<>();

        // Perform a validation
        MethodOutcome outcome = client
                .validate()
                .resource(appointment)
                .execute();

        OperationOutcome oo = (OperationOutcome) outcome.getOperationOutcome();
        LOG.info("Validation complete, returned: " + oo.getIssue().size() + " issues.");

        for (OperationOutcomeIssueComponent nextIssue : oo.getIssue()) {
            Enumeration<IssueSeverity> severityElement = nextIssue.getSeverityElement();
            String valueAsString = severityElement.getValueAsString();
            if (valueAsString.equals("error")) {
                results.add(new Fault(VALIDATIONERROR, Severity.MAJOR));
            }
            if (valueAsString.equals("fatal")) {
                results.add(new Fault(VALIDATIONFATAL, Severity.CRITICAL));
            }
            if (valueAsString.equals("warning")) {
                results.add(new Fault(VALIDATIONWARNING, Severity.CRITICAL));
            }
        }
        return results;
    }
}
