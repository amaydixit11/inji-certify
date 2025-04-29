package io.mosip.certify.core.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CredentialStatusRequestDto {
    private String credentialId;
    private boolean revoked;
    private Map<String, Object> additionalContext;
}
