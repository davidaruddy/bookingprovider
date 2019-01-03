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

/**
 * Describes the severity of a Fault.
 *
 * @author tim.coates@nhs.net
 */
public enum Severity {

    /**
     * A Trivial problem, which might not even cause trouble, but is pointed out
     * for clarification.
     */
    TRIVIAL,
    /**
     * A minor problem which should be addressed.
     */
    MINOR,
    /**
     * A Major problem which is expected to cause significant difficulties.
     */
    MAJOR,
    /**
     * A Critical problem which would prevent proper behaviour.
     */
    CRITICAL
}
