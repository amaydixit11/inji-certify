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

/**
 * IssuerSignedItem as defined in ISO 18013-5
 * Represents individual data elements within the mDoc credential
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssuerSignedItem {

    public Integer getDigestID() {
        return digestID;
    }

    public void setDigestID(Integer digestID) {
        this.digestID = digestID;
    }

    public byte[] getRandom() {
        return random;
    }

    public String getElementIdentifier() {
        return elementIdentifier;
    }

    public void setElementIdentifier(String elementIdentifier) {
        this.elementIdentifier = elementIdentifier;
    }

    public Object getElementValue() {
        return elementValue;
    }

    public void setElementValue(Object elementValue) {
        this.elementValue = elementValue;
    }

    /**
     * REQUIRED. Unique identifier for this data element within the namespace
     * Used to reference this item in the Mobile Security Object
     */
    @JsonProperty("digestID")
    private Integer digestID;

    /**
     * REQUIRED. 24-byte random value for salt
     */
    @JsonProperty("random")
    private byte[] random;

    /**
     * REQUIRED. Element identifier/name
     */
    @JsonProperty("elementIdentifier")
    private String elementIdentifier;

    /**
     * REQUIRED. The actual data value
     */
    @JsonProperty("elementValue")
    private Object elementValue;

    /**
     * Constructor with basic parameters
     * @param digestID Unique digest identifier
     * @param elementIdentifier Element name/identifier
     * @param elementValue The data value
     */
    public IssuerSignedItem(Integer digestID, String elementIdentifier, Object elementValue) {
        this.digestID = digestID;
        this.elementIdentifier = elementIdentifier;
        this.elementValue = elementValue;
    }

    /**
     * Set random salt bytes
     * @param random 24-byte random salt
     */
    public void setRandom(byte[] random) {
        if (random != null && random.length != 24) {
            throw new IllegalArgumentException("Random salt must be exactly 24 bytes");
        }
        this.random = random;
    }
}