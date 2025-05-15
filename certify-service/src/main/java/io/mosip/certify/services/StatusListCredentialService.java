package io.mosip.certify.services;

import io.mosip.certify.core.dto.StatusListCredentialDto;
import io.mosip.certify.core.exception.CertifyException;

import java.time.LocalDateTime;

/**
 * Service interface for Status List Credential operations.
 */
public interface StatusListCredentialService {

    /**
     * Get Status List Credential by ID, optionally at a specific timestamp
     *
     * @param id The unique ID (URL/URN/DID) of the Status List VC
     * @param timestamp Optional timestamp for time-based retrieval (if supported)
     * @return Status List Credential DTO
     * @throws CertifyException if credential not found or other errors occur
     * @throws IllegalArgumentException if timestamp format is invalid
     */
    StatusListCredentialDto getStatusListCredential(String id, LocalDateTime timestamp) throws CertifyException, IllegalArgumentException;

    /**
     * Update a specific bit in a Status List Credential
     *
     * @param statusListCredentialId The ID of the Status List Credential
     * @param statusListIndex The index in the status list
     * @param status The new status value (true/false)
     * @return The updated Status List Credential
     * @throws CertifyException if credential not found or other errors occur
     */
    StatusListCredentialDto updateStatusListCredential(String statusListCredentialId, Long statusListIndex, Boolean status) throws CertifyException;
}