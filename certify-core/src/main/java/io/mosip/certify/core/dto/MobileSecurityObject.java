/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.certify.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * Mobile Security Object (MSO) as defined in ISO 18013-5
 * This is the structure that gets signed to create the mDoc credential
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MobileSecurityObject {

    /**
     * REQUIRED. MSO version, fixed to "1.0"
     */
    @JsonProperty("version")
    private String version;

    /**
     * REQUIRED. Digest algorithm identifier
     */
    @JsonProperty("digestAlgorithm")
    private String digestAlgorithm;

    /**
     * REQUIRED. Map of namespace to digest values
     * Key: namespace string
     * Value: Map of digestID to digest bytes
     */
    @JsonProperty("valueDigests")
    private Map<String, Map<Integer, byte[]>> valueDigests;

    /**
     * OPTIONAL. Device key information for holder binding
     */
    @JsonProperty("deviceKeyInfo")
    private DeviceKeyInfo deviceKeyInfo;

    /**
     * REQUIRED. Document type identifier
     */
    @JsonProperty("docType")
    private String docType;

    /**
     * REQUIRED. Validity information for the credential
     */
    @JsonProperty("validityInfo")
    private ValidityInfo validityInfo;

    /**
     * Device Key Information structure
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceKeyInfo {

        /**
         * Device public key for holder binding
         */
        @JsonProperty("deviceKey")
        private Map<String, Object> deviceKey;

        /**
         * Key authorizations
         */
        @JsonProperty("keyAuthorizations")
        private Map<String, Object> keyAuthorizations;

        /**
         * Additional key information
         */
        @JsonProperty("keyInfo")
        private Map<String, Object> keyInfo;
    }

    /**
     * Validity Information structure
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidityInfo {

        /**
         * REQUIRED. Date from which the credential is valid
         */
        @JsonProperty("validFrom")
        private String validFrom;

        /**
         * REQUIRED. Date until which the credential is valid
         */
        @JsonProperty("validUntil")
        private String validUntil;

        /**
         * OPTIONAL. Expected update date for the credential
         */
        @JsonProperty("expectedUpdate")
        private String expectedUpdate;
    }
}