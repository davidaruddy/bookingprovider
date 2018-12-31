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

/**
 * Class to represent a fault found when checking a resource class.
 *
 * @author tim.coates@nhs.net
 */
public class Fault {

    /**
     * Description of the problem and/or how it will impact on the service.
     */
    private final String description;

    /**
     * An indication of how bad the problem is considered to be.
     */
    private final Severity sev;

    /**
     * Constructor to create a new Fault object.
     *
     * @param newDescription The human readable description of the fault.
     * @param newSeverity The expected severity of this problem.
     */
    public Fault(final String newDescription, final Severity newSeverity) {
        this.description = newDescription;
        this.sev = newSeverity;
    }

    /**
     * Gets just the description of this fault.
     *
     * @return The user friendly description.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Gets just the severity of this fault.
     *
     * @return The enumeration severity.
     */
    public final Severity getSev() {
        return sev;
    }

    /**
     * Method used when outputting the Fault class.
     *
     * @return
     */
    @Override
    public final String toString() {
        String output;
        switch (sev) {
            case CRITICAL:
                output = "CRITICAL ";
                break;
            case MAJOR:
                output = "MAJOR ";
                break;
            case MINOR:
                output = "MINOR ";
                break;
            case TRIVIAL:
                output = "TRIVIAL ";
                break;
            default:
                output = "UNKNOWN SEVERITY ";
        }
        return output + description;
    }

}
