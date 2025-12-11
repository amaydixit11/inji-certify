package io.mosip.certify.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Maps credential configurations to their associated authorization servers
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CredentialConfigurationMapping implements Serializable {
    private static final long serialVersionUID = 1L;

    private String credentialConfigurationId;
    private String authorizationServerId;
    private String authorizationServerUrl;
}