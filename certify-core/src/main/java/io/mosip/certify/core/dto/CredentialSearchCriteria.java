package io.mosip.certify.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for searching credential status records.
 * Maps to the OpenAPI CredentialSearchCriteria schema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CredentialSearchCriteria {
    private String credentialId;
    private String issuerId;
    private Map<String, String> indexedAttributesEquals;
}
