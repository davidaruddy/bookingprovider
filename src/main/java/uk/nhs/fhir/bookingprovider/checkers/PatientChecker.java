/*
 * Copyright 2018 tim.coates@nhs.net.
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.StringType;

/**
 * Class for checking Patient resource meets all requirements. NB: It'll be a
 * contained resource, so no need for fully meeting the CareConnect profile.
 *
 * @author tim.coates@nhs.net
 */
public class PatientChecker {

    /**
     * Where we expect profiles to be held.
     */
    private static final String PROFILEROOT
            = "https://fhir.hl7.org.uk/STU3/StructureDefinition/";

    /**
     * The specific profile we'll check for.
     */
    private static final String PROFILENAME
            = PROFILEROOT + "CareConnect-Patient-1";

    /**
     * The System we expect to be used to indicate that an Identifier is an NHS
     * Number.
     */
    private static final String NHSNOSYSTEM
            = "https://fhir.nhs.uk/Id/nhs-number";

    /**
     * The extension defined to specify the Verification status of a given NHS
     * Number.
     */
    private static final String VERIFYEXTENSION
            = PROFILEROOT + "Extension-CareConnect-NHSNumberVerificationStatus-1";

    /**
     * This is the main guts of this class, used to check a Patient object meets
     * a set of business rules specific to the CareConnect booking API.
     *
     * @param patient The Patient resource to be checked.
     * @return A List (hopefully of zero length) of faults.
     */
    public final ArrayList<Fault> checkThis(Patient patient) {
        ArrayList results = new ArrayList<Fault>();

        // Check we have a suitable ID
        String theID = patient.getId();
        if (theID == null) {
            results.add(
                    new Fault("Patient resource has no ID",
                            Severity.CRITICAL)
            );
        } else {
            if (theID.equals("")) {
                results.add(
                        new Fault("Patient resource ID is set to an empty String",
                                Severity.CRITICAL)
                );
            } else {
                if (theID.contains("/")) {
                    results.add(
                            new Fault("Patient resource ID contains / should be a local only identifier.",
                                    Severity.MAJOR)
                    );
                }
            }
        }

        if (patient.hasMeta()) {
            Meta met = patient.getMeta();
            if (met == null) {
                results.add(
                        new Fault("Patient resources has null Meta element.",
                                Severity.MAJOR)
                );
            } else {
                if (!met.hasProfile(PROFILENAME)) {
                    results.add(
                            new Fault("Patient resource does not claim to conform to: " + PROFILENAME,
                                    Severity.MAJOR)
                    );
                }
                if (met.getProfile().size() > 1) {
                    results.add(
                            new Fault("Patient resource claims to conform to multiple profiles.",
                                    Severity.MINOR)
                    );
                }
            }
        } else {
            results.add(
                    new Fault("Patient resource has no Meta element.",
                            Severity.MAJOR)
            );
        }

        // Check we have an NHS Number Identifier
        ArrayList<Identifier> theIdentifiers = (ArrayList) patient.getIdentifier();
        if (theIdentifiers.isEmpty()) {
            results.add(
                    new Fault("Patient resource has no Identifiers",
                            Severity.CRITICAL)
            );
        } else {
            if (theIdentifiers.size() > 1) {
                results.add(
                        new Fault("Patient resource has more than one Identifier, could lead to confusion.",
                                Severity.TRIVIAL)
                );
            }
            // Check structure and that we have only ONE NHS Number Identifier
            int nhsNumberCount = 0;
            for (Identifier thisIdentifier : theIdentifiers) {
                Identifier.IdentifierUse identifierUse = thisIdentifier.getUse();
                if (identifierUse == null) {
                    results.add(
                            new Fault("Patient has an Identifier with a 'Use' set to null?",
                                    Severity.MAJOR)
                    );
                } else {
                    switch (identifierUse) {
                        case OFFICIAL:
                            String identifierSystem = thisIdentifier.getSystem();
                            if (identifierSystem == null) {
                                results.add(
                                        new Fault("Patient has an Identifier with a 'Use' set to OFFICIAL and 'System' set to null.",
                                                Severity.MAJOR)
                                );
                            } else {
                                if (identifierSystem.equals("")) {
                                    results.add(
                                            new Fault("Patient has an Identifier with a 'Use' set to OFFICIAL and 'System' set to an empty string.",
                                                    Severity.MAJOR)
                                    );
                                } else {
                                    if (identifierSystem.equals(NHSNOSYSTEM)) {
                                        // Looks like this is the NHS Number...

                                        // Here we check for the necessary Extension...
                                        ArrayList<Extension> extensionList = (ArrayList<Extension>) thisIdentifier.getExtension();
                                        if (extensionList.size() != 1) {
                                            results.add(
                                                    new Fault("Unexpected multiple Extensions in Identifier.",
                                                            Severity.MINOR)
                                            );
                                        } else {
                                            Extension theExt = extensionList.get(0);
                                            if (!theExt.getUrl().equals(VERIFYEXTENSION)) {
                                                results.add(
                                                        new Fault("Extension has incorrect URL",
                                                                Severity.MAJOR)
                                                );
                                            }
                                            CodeableConcept extConcept = (CodeableConcept) theExt.getValue();
                                            Coding coding = extConcept.getCoding().get(0);
                                            if (!coding.getCode().equals("01")) {
                                                results.add(
                                                        new Fault("Patient NHS Number is not Verified",
                                                                Severity.MINOR)
                                                );
                                            }
                                            if (!coding.getDisplay().equals("Number present and verified")) {
                                                results.add(
                                                        new Fault("Patient NHS Number is not Verified",
                                                                Severity.MINOR)
                                                );
                                            }
                                            if (!coding.getSystem().equals("https://fhir.hl7.org.uk/STU3/ValueSet/CareConnect-NHSNumberVerificationStatus-1")) {
                                                results.add(
                                                        new Fault("Patient NHS Number is not Verified",
                                                                Severity.MINOR)
                                                );
                                            }
                                        }

                                        String identifierValue = thisIdentifier.getValue();
                                        if (identifierValue == null) {
                                            results.add(
                                                    new Fault("Patient has an Identifier with a 'Use' set to OFFICIAL 'System' is set correct, but value set to null.",
                                                            Severity.MAJOR)
                                            );
                                        } else {
                                            if (identifierValue.equals("")) {
                                                results.add(
                                                        new Fault("Patient has an Identifier with a 'Use' set to OFFICIAL 'System' is set correct, but value is empty.",
                                                                Severity.MAJOR)
                                                );
                                            } else {
                                                if (identifierValue.matches("\\d{10}")) {
                                                    nhsNumberCount++;
                                                } else {
                                                    results.add(
                                                            new Fault("Patient has an Identifier with a 'Use' set to OFFICIAL 'System' is set correct, but value doesn't seem to be 10 digits.",
                                                                    Severity.MAJOR)
                                                    );
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case USUAL:
                            break;
                        case SECONDARY:
                            break;
                        case TEMP:
                            break;
                        case NULL:
                            break;
                        default:
                            results.add(new Fault("Patient has an Identifier with no 'Use' defined.", Severity.MINOR));
                    }
                }
            }
            if (nhsNumberCount > 1) {
                results.add(new Fault("Patient seems to have multiple NHS Numbers in Identifiers.", Severity.CRITICAL));
            } else {
                if (nhsNumberCount == 0) {
                    results.add(new Fault("Patient has no NHS Number in Identifiers.", Severity.CRITICAL));
                }
            }
        }

        // Check Name...
        ArrayList<HumanName> theNames = (ArrayList<HumanName>) patient.getName();
        int officialNamesCount = 0;
        if (theNames.isEmpty()) {
            results.add(new Fault("Patient has no Name.", Severity.CRITICAL));
        } else {
            if (theNames.size() > 1) {
                results.add(
                        new Fault("Patient has multiple Names defined, leading to potential confusion.",
                                Severity.MINOR)
                );
            }
            for (HumanName thisName : theNames) {
                HumanName.NameUse nameUse = thisName.getUse();
                if (nameUse == null) {
                    results.add(
                            new Fault("Patient has a Name with no 'Use' defined for it.",
                                    Severity.MAJOR)
                    );
                } else {
                    switch (nameUse) {
                        case OFFICIAL:
                            officialNamesCount++;
                            break;
                        case USUAL:
                        case TEMP:
                        case NICKNAME:
                        case ANONYMOUS:
                        case OLD:
                        case MAIDEN:
                        case NULL:
                        default:
                    }
                }
                String famName = thisName.getFamily();
                if (famName == null) {
                    if (nameUse == HumanName.NameUse.OFFICIAL) {
                        results.add(
                                new Fault("Patient has an official name with Family name of null.",
                                        Severity.CRITICAL)
                        );
                    } else {
                        results.add(
                                new Fault("Patient has a name with Family name of null.",
                                        Severity.MINOR)
                        );
                    }
                } else {
                    if (famName.equals("")) {
                        if (nameUse == HumanName.NameUse.OFFICIAL) {
                            results.add(
                                    new Fault("Patient has an official name with empty Family name.",
                                            Severity.CRITICAL)
                            );
                        } else {
                            results.add(
                                    new Fault("Patient has a name with empty Family name.",
                                            Severity.MINOR)
                            );
                        }
                    } else {
                        if (famName.length() < 3) {
                            if (nameUse == HumanName.NameUse.OFFICIAL) {
                                results.add(
                                        new Fault("Patient has an official name with Family name < 3 characters.",
                                                Severity.MINOR)
                                );
                            } else {
                                results.add(
                                        new Fault("Patient has a name with Family name < 3 characters.",
                                                Severity.MINOR)
                                );
                            }
                        }
                        if (famName.length() > 12) {
                            if (nameUse == HumanName.NameUse.OFFICIAL) {
                                results.add(
                                        new Fault("Patient has an official name with Family name > 12 characters.",
                                                Severity.MINOR)
                                );
                            } else {
                                results.add(
                                        new Fault("Patient has a name with Family name > 12 characters.",
                                                Severity.MINOR)
                                );
                            }
                        }
                    }
                }
                List<StringType> givenNameList = thisName.getGiven();
                for (StringType thisGiven : givenNameList) {
                    if (thisGiven == null) {
                        results.add(
                                new Fault("Patient has a given name of null.",
                                        Severity.MAJOR)
                        );
                    } else {
                        if (thisGiven.toString().equals("")) {
                            results.add(
                                    new Fault("Patient has an empty given name.",
                                            Severity.MAJOR)
                            );
                        } else {
                            if (thisGiven.toString().length() < 3) {
                                results.add(
                                        new Fault("Patient has a given name of < 3 characters.",
                                                Severity.MINOR)
                                );
                            }
                            if (thisGiven.toString().length() > 12) {
                                results.add(
                                        new Fault("Patient has a given name of > 12 characters.",
                                                Severity.MINOR)
                                );
                            }
                        }
                    }
                }
            }
        }
        if (officialNamesCount == 0) {
            results.add(
                    new Fault("Patient has NO official name set.",
                            Severity.CRITICAL)
            );
        }

        // Check telecom...
        ArrayList<ContactPoint> telecomList = (ArrayList<ContactPoint>) patient.getTelecom();
        int telecomCount = 0;
        for (ContactPoint thisContact : telecomList) {
            if (!thisContact.hasSystem()) {
                results.add(
                        new Fault("Patient has a contact with no System set.",
                                Severity.MAJOR)
                );
            } else {
                if (thisContact.getSystem().equals(ContactPoint.ContactPointSystem.PHONE)) {
                    telecomCount++;
                }
            }
            if (!thisContact.hasValue()) {
                results.add(
                        new Fault("Patient has a contact with no Value set.",
                                Severity.MAJOR)
                );
            } else {
                switch (thisContact.getSystem()) {
                    case PHONE:
                    case SMS:
                        if (thisContact.getValue().matches("((\\+44\\s?\\(0\\)\\s?\\d{2,4})|(\\+44\\s?(01|02|03|07|08)\\d{2,3})|(\\+44\\s?(1|2|3|7|8)\\d{2,3})|(\\(\\+44\\)\\s?\\d{3,4})|(\\(\\d{5}\\))|((01|02|03|07|08)\\d{2,3})|(\\d{5}))(\\s|-|.)(((\\d{3,4})(\\s|-)(\\d{3,4}))|((\\d{6,7})))") == false) {
                            results.add(
                                    new Fault("Patient has a phone contact but Value does not seem to be a UK phone number.",
                                            Severity.MAJOR)
                            );
                        }
                        break;

                    case EMAIL:
                        if (thisContact.getValue().matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])") == false) {
                            results.add(
                                    new Fault("Patient has an email contact but Value does not seem to be a valid email address.",
                                            Severity.MAJOR)
                            );
                        }
                        break;
                }
            }
        }
        if (telecomCount == 0) {
            results.add(
                    new Fault("Patient has no telephone Contacts.",
                            Severity.MAJOR)
            );
        }

        // Check Gender
        Enumerations.AdministrativeGender gender = patient.getGender();
        if (gender == null) {
            results.add(
                    new Fault("Patient has no Gender set.",
                            Severity.MAJOR)
            );
        } else {
            if (gender.equals(Enumerations.AdministrativeGender.NULL)
                    || gender.equals(Enumerations.AdministrativeGender.OTHER)
                    || gender.equals(Enumerations.AdministrativeGender.UNKNOWN)) {
                results.add(
                        new Fault("Patient has unexpected Gender set, ensure this was intended.",
                                Severity.MINOR)
                );
            }
        }

        // Check DOB
        Date birthDate = patient.getBirthDate();
        if (birthDate == null) {
            results.add(
                    new Fault("Patient has null DOB.",
                            Severity.MAJOR)
            );
        } else {
            if (birthDate.after(new Date())) {
                results.add(
                        new Fault("Patient has DOB in the future.",
                                Severity.CRITICAL)
                );
            }
            if (birthDate.before(new Date(0, 0, 0))) {
                results.add(
                        new Fault("Patient has DOB before 1900.",
                                Severity.MAJOR)
                );
            }
        }

        // Check Address
        ArrayList<Address> addressList = (ArrayList<Address>) patient.getAddress();
        if (addressList.size() < 1) {
            results.add(
                    new Fault("Patient has no Address.",
                            Severity.MAJOR)
            );
        }
        if (addressList.size() > 1) {
            results.add(
                    new Fault("Patient has multiple Addresses.",
                            Severity.MINOR)
            );
        }
        for (Address thisAddress : addressList) {
            if (thisAddress.hasUse() == false) {
                if (addressList.size() > 1) {
                    results.add(
                            new Fault("Patient has multiple Addresses and Use not specified.",
                                    Severity.MAJOR)
                    );
                } else {
                    results.add(
                            new Fault("Patient Address Use not specified.",
                                    Severity.MINOR)
                    );
                }
            } else {
                if (thisAddress.getUse().equals(Address.AddressUse.NULL)
                        || thisAddress.getUse().equals(Address.AddressUse.OLD)
                        || thisAddress.getUse().equals(Address.AddressUse.TEMP)) {
                    if (addressList.size() > 1) {
                        results.add(
                                new Fault("Patient has multiple Addresses including one or more ambiguous ones.",
                                        Severity.MAJOR)
                        );
                    } else {
                        results.add(
                                new Fault("Patient Address Use value was unexpected - check.",
                                        Severity.MINOR)
                        );
                    }
                }
            }
            if (thisAddress.hasPostalCode() == false) {
                results.add(
                        new Fault("Patient Address has no Postcode.",
                                Severity.CRITICAL)
                );
            } else {
                if (thisAddress.getPostalCode().matches("([Gg][Ii][Rr] 0[Aa]{2})|((([A-Za-z][0-9]{1,2})|(([A-Za-z][A-Ha-hJ-Yj-y][0-9]{1,2})|(([A-Za-z][0-9][A-Za-z])|([A-Za-z][A-Ha-hJ-Yj-y][0-9][A-Za-z]?))))\\s?[0-9][A-Za-z]{2})") == false) {
                    results.add(
                            new Fault("Patient Address Postcode does not appear to be correct.",
                                    Severity.MAJOR)
                    );
                }
            }
        }
        return results;
    }
}
