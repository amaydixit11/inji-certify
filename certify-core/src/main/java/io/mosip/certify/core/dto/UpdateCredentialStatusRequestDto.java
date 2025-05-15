package io.mosip.certify.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for receiving credential status update requests.
 * Maps to the OpenAPI UpdateCredentialStatusRequestDto schema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateCredentialStatusRequestDto {

    private String credentialId;
    private CredentialStatusMechanism credentialStatus;
    private Boolean status;
    private String indexAllocator;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CredentialStatusMechanism {
        private String id;
        private String type;
        private String statusPurpose;
        private Long statusListIndex;
        private String statusListCredential;
    }
}
