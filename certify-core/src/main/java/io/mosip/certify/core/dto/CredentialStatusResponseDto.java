package io.mosip.certify.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for returning credential status information.
 * Maps to the OpenAPI CredentialStatusResponseDto schema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CredentialStatusResponseDto {
    private String credentialId;
    private String issuerId;
    private String statusListCredentialUrl;
    private Long statusListIndex;
    private String statusPurpose;
    private String credentialStatus;
    private LocalDateTime issueDate;
    private LocalDateTime expirationDate;
    private String credentialType;
}
