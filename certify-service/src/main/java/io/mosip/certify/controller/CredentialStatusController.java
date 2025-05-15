package io.mosip.certify.controller;

import io.mosip.certify.core.constants.ErrorConstants;
import io.mosip.certify.core.dto.CredentialSearchCriteria;
import io.mosip.certify.core.dto.CredentialStatusResponseDto;
import io.mosip.certify.core.dto.UpdateCredentialStatusRequestDto;
import io.mosip.certify.core.exception.CertifyException;
import io.mosip.certify.services.CredentialStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing credential status operations.
 * Implements the Credential Status endpoints defined in the API spec.
 */
@RestController
@RequestMapping("/api/v1")
@Slf4j
public class CredentialStatusController {

    @Autowired
    private CredentialStatusService credentialStatusService;

    /**
     * Get credential status by credentialId
     *
     * @param credentialId The system identifier for the credential status record
     * @return Credential status details
     */
    @GetMapping("/credential-status/{credentialId}")
    public ResponseEntity<CredentialStatusResponseDto> getStatusById(@PathVariable String credentialId) {
        log.info("Retrieving credential status for credentialId: {}", credentialId);
        try {
            CredentialStatusResponseDto response = credentialStatusService.getCredentialStatusById(credentialId);
            return ResponseEntity.ok(response);
        } catch (CertifyException e) {
            if (e.getErrorCode().equals(ErrorConstants.CREDENTIAL_NOT_FOUND)) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    /**
     * Get credential status records by issuerId
     *
     * @param issuerId The identifier of the credential issuer
     * @return List of credential status records
     */
    @GetMapping("/credential-status/by-issuer/{issuerId}")
    public ResponseEntity<List<CredentialStatusResponseDto>> getStatusByIssuerId(@PathVariable String issuerId) {
        log.info("Retrieving credential status records for issuerId: {}", issuerId);
        List<CredentialStatusResponseDto> records = credentialStatusService.getCredentialStatusByIssuerId(issuerId);
        if (records.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(records);
    }

    /**
     * Search for credential status records
     *
     * @param criteria The search criteria
     * @return List of matching credential status records
     */
    @PostMapping("/credential-status/search")
    public ResponseEntity<List<CredentialStatusResponseDto>> searchCredentialStatus(
            @RequestBody CredentialSearchCriteria criteria) {
        log.info("Searching credential status records");
        try {
            List<CredentialStatusResponseDto> results = credentialStatusService.searchCredentialStatus(criteria);
            if (results.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(results);
        } catch (IllegalArgumentException e) {
            log.error("Invalid search criteria provided", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update credential status
     *
     * @param requestDto The update request
     * @return Updated credential status record
     */
    @PostMapping("/credentials/status")
    public ResponseEntity<CredentialStatusResponseDto> updateCredentialStatus(
            @RequestBody UpdateCredentialStatusRequestDto requestDto) {
        log.info("Credential status update request received for credential: {}", requestDto.getCredentialId());

        try {
            CredentialStatusResponseDto response = credentialStatusService.updateCredentialStatus(requestDto);
            return ResponseEntity.ok(response);
        } catch (CertifyException e) {
            log.error("Error updating credential status", e);
            return switch (e.getErrorCode()) {
                case ErrorConstants.CREDENTIAL_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                case ErrorConstants.STATUS_UPDATE_FORBIDDEN -> ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                case ErrorConstants.STATUS_UPDATE_CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).build();
                case ErrorConstants.STATUS_UPDATE_ERROR -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                default -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            };
        }
    }
}