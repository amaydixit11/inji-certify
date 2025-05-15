package io.mosip.certify.core.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for representing Status List Verifiable Credentials.
 * Maps to the OpenAPI StatusListCredentialDto schema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusListCredentialDto {
    private Object context; // Can be String or List<String>
    private String id;
    private String[] type;
    private Object issuer; // Can be String or Object with id
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private CredentialSubject credentialSubject;
    private Proof proof;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CredentialSubject {
        private String id;
        private String type;
        private String statusPurpose;
        private String encodedList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Proof {
        private String type;
        private LocalDateTime created;
        private String verificationMethod;
        private String proofPurpose;
        private String proofValue;
    }
}