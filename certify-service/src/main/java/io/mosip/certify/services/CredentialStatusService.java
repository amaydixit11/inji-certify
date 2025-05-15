package io.mosip.certify.services;

import io.mosip.certify.core.dto.CredentialSearchCriteria;
import io.mosip.certify.core.dto.CredentialStatusResponseDto;
import io.mosip.certify.core.dto.UpdateCredentialStatusRequestDto;
import io.mosip.certify.core.exception.CertifyException;

import java.util.List;

/**
 * Service interface for credential status operations.
 */
public interface CredentialStatusService {

    /**
     * Get credential status by ID
     *
     * @param credentialId The credential ID
     * @return Credential status response DTO
     * @throws CertifyException if credential not found or other errors occur
     */
    CredentialStatusResponseDto getCredentialStatusById(String credentialId) throws CertifyException;

    /**
     * Get credential status records by issuer ID
     *
     * @param issuerId The issuer ID
     * @return List of credential status response DTOs
     */
    List<CredentialStatusResponseDto> getCredentialStatusByIssuerId(String issuerId);

    /**
     * Search for credential status records based on search criteria
     *
     * @param criteria The search criteria
     * @return List of credential status response DTOs
     * @throws IllegalArgumentException if search criteria is invalid
     */
    List<CredentialStatusResponseDto> searchCredentialStatus(CredentialSearchCriteria criteria) throws IllegalArgumentException;

    /**
     * Update credential status
     *
     * @param requestDto The update request DTO
     * @return Updated credential status response DTO
     * @throws CertifyException if credential not found or other errors occur
     */
    CredentialStatusResponseDto updateCredentialStatus(UpdateCredentialStatusRequestDto requestDto) throws CertifyException;
}