package io.mosip.certify.core.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CredentialStatusResponseDto {
    private String credentialId;
    private String status;
    private LocalDateTime updatedAt;
}