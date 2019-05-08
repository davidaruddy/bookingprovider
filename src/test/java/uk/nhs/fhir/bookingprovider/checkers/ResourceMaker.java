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
import ca.uhn.fhir.model.primitive.IdDt;
import java.util.ArrayList;
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
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;

/**
 *
 * @author tim.coates@nhs.net
 */
public class ResourceMaker {

    private static final Logger LOG = Logger.getLogger(ResourceMaker.class.getName());

    /**
     * Simple constructor...
     */
    public ResourceMaker() {
    }

    /**
     * Method to create a valid Patient resource.
     *
     * TODO: Maybe add Meta details?
     *
     * @param refValue
     * @return
     */
    public Patient makePatient(String refValue) {
        // Now we add a contained Patient
        Patient pat = new Patient();
        pat.setMeta(new Meta().addProfile("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Patient-1"));
        pat.setId(refValue);
        Identifier patNHSNumber = new Identifier();

        Coding verStatusCoding = new Coding("https://fhir.hl7.org.uk/STU3/CodeSystem/CareConnect-NHSNumberVerificationStatus-1", "01", "Number present and verified");
        CodeableConcept verStatus = new CodeableConcept();
        verStatus.addCoding(verStatusCoding);
        Extension verifiedExtension = new Extension("https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-NHSNumberVerificationStatus-1");
        verifiedExtension.setValue(verStatus);
        patNHSNumber.addExtension(verifiedExtension);

        patNHSNumber.setUse(Identifier.IdentifierUse.OFFICIAL);
        patNHSNumber.setSystem("https://fhir.nhs.uk/Id/nhs-number");
        patNHSNumber.setValue("1231231234");
        pat.addIdentifier(patNHSNumber);
        HumanName name = new HumanName();
        name.setText("Mr Fred Smith");
        name.addPrefix("Mr");
        name.addGiven("Fred");
        name.setFamily("Smith");
        name.setUse(HumanName.NameUse.OFFICIAL);
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
        return pat;
    }

    /**
     * Method to create a valid Patient resource.
     *
     * TODO: Maybe add Meta details?
     *
     * @param refValue
     * @return
     */
    public Patient makePatient() {
        // Now we add a contained Patient
        Patient pat = new Patient();
        pat.setMeta(new Meta().addProfile("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Patient-1"));
        Identifier patNHSNumber = new Identifier();

        Coding verStatusCoding = new Coding("https://fhir.hl7.org.uk/STU3/ValueSet/CareConnect-NHSNumberVerificationStatus-1", "01", "Number present and verified");
        CodeableConcept verStatus = new CodeableConcept();
        verStatus.addCoding(verStatusCoding);
        Extension verifiedExtension = new Extension("https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-NHSNumberVerificationStatus-1");
        verifiedExtension.setValue(verStatus);
        patNHSNumber.addExtension(verifiedExtension);

        patNHSNumber.setUse(Identifier.IdentifierUse.OFFICIAL);
        patNHSNumber.setSystem("https://fhir.nhs.uk/Id/nhs-number");
        patNHSNumber.setValue("1231231234");
        pat.addIdentifier(patNHSNumber);
        HumanName name = new HumanName();
        name.setText("Mr Fred Smith");
        name.addPrefix("Mr");
        name.addGiven("Fred");
        name.setFamily("Smith");
        name.setUse(HumanName.NameUse.OFFICIAL);
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
        return pat;
    }
    
    /**
     * Method to break a valid Patient to run more tests on him.
     *
     * @param good
     * @param what
     * @return
     */
    public Patient BreakHim(Patient good, PatientCheckerTest.BREAKPOINT what) {
        switch (what) {
            case ID:
                good.setId(new IdDt());
                break;
            case FUTUREDOB:
                good.setBirthDate(new Date(120, 5, 5));
                break;
            case NONAME:
                good.setName((List<HumanName>) new ArrayList<HumanName>());
                break;
            case UNVERIFIED:
                good = setVerifiedStatus(good, false);
            default:
        }
        return good;
    }

    /**
     * Helper function to serialise a Resource
     *
     * @param input
     * @return
     */
    private String ResourceToString(Resource input) {
        FhirContext ctx = FhirContext.forDstu3();
        //return ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(input);
        return ctx.newXmlParser().setPrettyPrint(false).encodeResourceToString(input);
    }

    /**
     * Method to create a DocumentReference to be contained in an Appointment
     * resource.
     *
     * @return
     */
    private DocumentReference makeDocRef(String ID) {
        DocumentReference docRef = new DocumentReference();
        docRef.setId(ID);
        docRef.addIdentifier(new Identifier().setSystem("uuid").setValue(UUID.randomUUID().toString()));
        docRef.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);
        docRef.setType(new CodeableConcept().addCoding(new Coding("urn:oid:2.16.840.1.113883.2.1.3.2.4.18.17", "POCD_MT200001GB02", "Integrated Urgent Care Report")));
        docRef.setIndexed(new Date());
        docRef.addContent(new DocumentReference.DocumentReferenceContentComponent(new Attachment().setContentType("application/hl7-v3+xml").setLanguage("en")));
        return docRef;
    }

    /**
     * Method to create a sample Appointment resource.
     *
     * @return
     */
    public Appointment makeAppointment() {
        Appointment appt = new Appointment();
        Meta meta = new Meta();
        meta.addProfile("https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Appointment-1");
        appt.setMeta(meta);
        appt.setLanguage("en");
        appt.setStatus(Appointment.AppointmentStatus.BOOKED);
        Reference slotRef = new Reference();
        slotRef.setReference("/Slot/76abb688-01cd-4985-bf11-425d051fb4a6");
        appt.addSlot(slotRef);
        appt.setCreated(new Date());
        //String PatientReferenceValue = "P1";
        
        Patient patient = makePatient();
        
        AppointmentParticipantComponent thePatParticipant = new AppointmentParticipantComponent();
        
        Reference patRef = new Reference();
        patRef.setResource(patient);
        patRef.setIdentifier(new Identifier().setSystem("https://fhir.nhs.uk/Id/nhs-number").setValue("1234512345").setUse(Identifier.IdentifierUse.OFFICIAL));
        thePatParticipant.setActor(patRef);
        appt.addParticipant(thePatParticipant);
        //appt.addContained(patient);
        
        ArrayList<Resource> containedResources = new ArrayList();

        containedResources.add(patient);

        String DocRefReferenceValue = "78a39984-298d-4015-b48a-585aa0650005";
        DocumentReference DocRef = makeDocRef(DocRefReferenceValue);
        //LOG.info("DocumentReference: " + ResourceToString(DocRef));

        /// - ///
        Reference docRefReference = new Reference();
        docRefReference.setReference("#" + DocRefReferenceValue);
        appt.addSupportingInformation(docRefReference);

        containedResources.add(DocRef);
        appt.setContained(containedResources);
        LOG.info("Appointment now has: " + appt.getContained().size() + " contained resources");
        LOG.info("Appointment: " + ResourceToString(appt));
        return appt;
    }

    /**
     * Method to set the NHS Number verification status of a Patient.
     *
     * @param inputPatient The incoming Patient
     * @param status True / False whether to set t oVerified or Not
     * @return Returns the modified Patient
     */
    private Patient setVerifiedStatus(Patient inputPatient, boolean status) {
        Identifier patNHSNumber = new Identifier();

        Coding verStatusCoding;
        if (status) {
            verStatusCoding = new Coding("https://fhir.hl7.org.uk/STU3/ValueSet/CareConnect-NHSNumberVerificationStatus-1", "01", "Number present and verified");
        } else {
            verStatusCoding = new Coding("https://fhir.hl7.org.uk/STU3/ValueSet/CareConnect-NHSNumberVerificationStatus-1", "04", "Trace attempted - No match or multiple match found");
        }
        CodeableConcept verStatus = new CodeableConcept();
        verStatus.addCoding(verStatusCoding);
        Extension verifiedExtension = new Extension("https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-NHSNumberVerificationStatus-1");
        verifiedExtension.setValue(verStatus);
        patNHSNumber.addExtension(verifiedExtension);

        patNHSNumber.setUse(Identifier.IdentifierUse.OFFICIAL);
        patNHSNumber.setSystem("https://fhir.nhs.uk/Id/nhs-number");
        patNHSNumber.setValue("1231231234");
        ArrayList<Identifier> identList = new ArrayList<Identifier>();
        identList.add(patNHSNumber);
        inputPatient.setIdentifier(identList);
        return inputPatient;
    }

    public Appointment setAppointmentSlot(Appointment incoming, String slotref) {
        Reference slotRef = new Reference();
        slotRef.setReference("/Slot/slot003");
        ArrayList<Reference> slotList = new ArrayList<Reference>();
        slotList.add(slotRef);
        incoming.setSlot(slotList);
        return incoming;
    }

}
