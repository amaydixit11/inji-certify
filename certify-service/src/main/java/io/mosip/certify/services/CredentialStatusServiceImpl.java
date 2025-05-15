package io.mosip.certify.services;

import io.mosip.certify.core.constants.ErrorConstants;
import io.mosip.certify.core.dto.CredentialSearchCriteria;
import io.mosip.certify.core.dto.CredentialStatusResponseDto;
import io.mosip.certify.core.dto.UpdateCredentialStatusRequestDto;
import io.mosip.certify.core.exception.CertifyException;
import io.mosip.certify.entity.Ledger;
import io.mosip.certify.repository.LedgerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the CredentialStatusService interface.
 */
@Service
@Slf4j
public class CredentialStatusServiceImpl implements CredentialStatusService {

    @Autowired
    private LedgerRepository ledgerRepository;

    @Autowired
    private StatusListCredentialService statusListCredentialService;

    @Override
    public CredentialStatusResponseDto getCredentialStatusById(String credentialId) throws CertifyException {
        log.info("Getting credential status for credentialId: {}", credentialId);

        Ledger ledger = ledgerRepository.findByCredentialId(credentialId)
                .orElseThrow(() -> new CertifyException(ErrorConstants.CREDENTIAL_NOT_FOUND));

        return mapLedgerToDto(ledger);
    }

    @Override
    public List<CredentialStatusResponseDto> getCredentialStatusByIssuerId(String issuerId) {
        log.info("Getting credential status records for issuerId: {}", issuerId);

        List<Ledger> ledgers = ledgerRepository.findByIssuerId(issuerId);

        return ledgers.stream()
                .map(this::mapLedgerToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CredentialStatusResponseDto> searchCredentialStatus(CredentialSearchCriteria criteria) throws IllegalArgumentException {
        log.info("Searching credential status records with criteria");

        if (criteria == null) {
            throw new IllegalArgumentException("Search criteria cannot be null");
        }

        // Start with all records if no filters provided
        List<Ledger> results = new ArrayList<>();

        // Apply filters based on provided criteria
        if (criteria.getCredentialId() != null && criteria.getIssuerId() != null) {
            Optional<Ledger> ledger = ledgerRepository.findByCredentialIdAndIssuerId(criteria.getCredentialId(), criteria.getIssuerId());
            ledger.ifPresent(results::add);
        } else if (criteria.getCredentialId() != null) {
            ledgerRepository.findByCredentialId(criteria.getCredentialId()).ifPresent(results::add);
        } else if (criteria.getIssuerId() != null) {
            results.addAll(ledgerRepository.findByIssuerId(criteria.getIssuerId()));
        } else {
            // Basic search with no filters
            results.addAll(ledgerRepository.findAll());
        }

        // Apply indexed attributes filter if provided
        if (criteria.getIndexedAttributesEquals() != null && !criteria.getIndexedAttributesEquals().isEmpty()) {
            // This is a simplified implementation - actual implementation would depend on
            // how indexed attributes are stored and queried in your database
            results = results.stream()
                    .filter(ledger -> matchesIndexedAttributes(ledger, criteria.getIndexedAttributesEquals()))
                    .collect(Collectors.toList());
        }

        return results.stream()
                .map(this::mapLedgerToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CredentialStatusResponseDto updateCredentialStatus(UpdateCredentialStatusRequestDto requestDto) throws CertifyException {
        log.info("Updating credential status for credentialId: {}", requestDto.getCredentialId());

        if (requestDto.getCredentialId() == null || requestDto.getStatus() == null) {
            throw new CertifyException(ErrorConstants.INVALID_INPUT);
        }

        // Find the ledger entry
        Ledger ledger = ledgerRepository.findByCredentialId(requestDto.getCredentialId())
                .orElseThrow(() -> new CertifyException(ErrorConstants.CREDENTIAL_NOT_FOUND));

        // Determine new status value based on input and possibly the statusPurpose
        String newStatus = deriveNewStatus(requestDto.getStatus(),
                requestDto.getCredentialStatus() != null ? requestDto.getCredentialStatus().getStatusPurpose() : null,
                ledger.getStatusPurpose());

        // Handle status list updates if applicable
        UpdateCredentialStatusRequestDto.CredentialStatusMechanism credStatusMech = requestDto.getCredentialStatus();

        String statusListCredentialUrl = null;
        Long statusListIndex = null;

        // Determine which status list/index to use - from request or existing record
        if (credStatusMech != null && credStatusMech.getStatusListCredential() != null && credStatusMech.getStatusListIndex() != null) {
            // Use values from request
            statusListCredentialUrl = credStatusMech.getStatusListCredential();
            statusListIndex = credStatusMech.getStatusListIndex();
        } else if (ledger.getStatusListCredentialUrl() != null && ledger.getStatusListIndex() != null) {
            // Use existing values
            statusListCredentialUrl = ledger.getStatusListCredentialUrl();
            statusListIndex = ledger.getStatusListIndex();
        }

        // Update status list if applicable
        if (statusListCredentialUrl != null && statusListIndex != null) {
            try {
                statusListCredentialService.updateStatusListCredential(
                        statusListCredentialUrl,
                        statusListIndex,
                        requestDto.getStatus());
            } catch (CertifyException e) {
                log.error("Failed to update status in Status List Credential", e);
                throw new CertifyException(ErrorConstants.STATUS_UPDATE_CONFLICT);

            }

        }

        // Update the ledger entry
        ledger.setCredentialStatus(newStatus);

        // Update status list info if provided in the request
        if (credStatusMech != null) {
            if (credStatusMech.getStatusPurpose() != null) {
                ledger.setStatusPurpose(credStatusMech.getStatusPurpose());
            }
            if (credStatusMech.getStatusListCredential() != null) {
                ledger.setStatusListCredentialUrl(credStatusMech.getStatusListCredential());
            }
            if (credStatusMech.getStatusListIndex() != null) {
                ledger.setStatusListIndex(credStatusMech.getStatusListIndex());
            }
        }

        // Save updates
        ledger = ledgerRepository.save(ledger);

        return mapLedgerToDto(ledger);
    }

    /**
     * Map a Ledger entity to a CredentialStatusResponseDto
     */
    private CredentialStatusResponseDto mapLedgerToDto(Ledger ledger) {
        CredentialStatusResponseDto dto = new CredentialStatusResponseDto();
        dto.setCredentialId(ledger.getCredentialId());
        dto.setIssuerId(ledger.getIssuerId());
        dto.setCredentialStatus(ledger.getCredentialStatus());
        dto.setIssueDate(ledger.getIssueDate());
        dto.setExpirationDate(ledger.getExpirationDate());
        dto.setCredentialType(ledger.getCredentialType());
        dto.setStatusListCredentialUrl(ledger.getStatusListCredentialUrl());
        dto.setStatusListIndex(ledger.getStatusListIndex());
        dto.setStatusPurpose(ledger.getStatusPurpose());
        return dto;
    }

    /**
     * Determine the string status value based on boolean status and purpose
     */
    private String deriveNewStatus(Boolean status, String requestPurpose, String existingPurpose) {
        String purpose = requestPurpose != null ? requestPurpose : existingPurpose;

        if (purpose == null) {
            // Default interpretation if no purpose specified
            return Boolean.TRUE.equals(status) ? "revoked" : "valid";
        }

        // Interpret based on purpose
        switch (purpose.toLowerCase()) {
            case "revocation":
                return Boolean.TRUE.equals(status) ? "revoked" : "valid";
            case "suspension":
                return Boolean.TRUE.equals(status) ? "suspended" : "valid";
            default:
                return Boolean.TRUE.equals(status) ? "statusAsserted" : "statusNotAsserted";
        }
    }

    /**
     * Check if a ledger entry matches the indexed attributes filter
     * This is a simplified implementation - actual implementation would depend on
     * how indexed attributes are stored in your system
     */
    private boolean matchesIndexedAttributes(Ledger ledger, java.util.Map<String, String> indexedAttributes) {
        // In a real implementation, you would typically:
        // 1. Have a separate table or JSON column for indexed attributes
        // 2. Have a more sophisticated query mechanism

        // This is just a placeholder that always returns true
        // Replace with actual implementation based on your data model
        return true;
    }
}