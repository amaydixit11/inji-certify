package io.mosip.certify.services;

import io.mosip.certify.core.dto.*;
import io.mosip.certify.entity.Ledger;
import io.mosip.certify.exception.CredentialNotFoundException;
import io.mosip.certify.exception.RevocationException;
import io.mosip.certify.repository.LedgerRepository;
import io.mosip.certify.repository.StatusListCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RevocationServiceImpl implements RevocationService {

    @Autowired
    private CertifyIssuanceServiceImpl certifyIssuanceService;

    @Autowired
    private LedgerRepository ledgerRepository;

    @Autowired
    private StatusListCredentialRepository statusListCredentialRepository;

    @Autowired
    private BitStringStatusListService bitStringStatusListService;

    /**
     * Fetch credential status information based on filters
     * @param request The credential fetch request containing filters
     * @return The credential status information
     * @throws CredentialNotFoundException if the credential cannot be found
     */
    @Override
    public CredentialFetchResponse fetchCredential(CredentialFetchRequest request) throws CredentialNotFoundException {
        try {
            // Build query dynamically based on provided filters
            List<Ledger> credentials = findCredentialsByFilters(request);

            if (credentials.isEmpty()) {
                throw new CredentialNotFoundException("No credentials found matching the provided filters");
            }

            // Return the first matching credential
            Ledger credential = credentials.get(0);

            CredentialFetchResponse response = new CredentialFetchResponse();
            response.setStatusPurpose(credential.getStatusPurpose());
            response.setStatusListIndex(credential.getStatusListIndex().toString());
            response.setStatusListCredential(credential.getStatusListCredentialUrl());

            return response;
        } catch (CredentialNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CredentialNotFoundException("Error fetching credential: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to find credentials based on filters
     * @param request The credential fetch request containing filters
     * @return List of matching credentials
     */
    private List<Ledger> findCredentialsByFilters(CredentialFetchRequest request) {
        if (request.getCredentialId() != null && !request.getCredentialId().isEmpty()) {
            // If credential ID is provided, use that as the primary search parameter
            Optional<Ledger> credentialOpt = ledgerRepository.findByCredentialId(request.getCredentialId());
            return credentialOpt.map(Collections::singletonList).orElseGet(Collections::emptyList);
        }

        // Build dynamic query using specifications or criteria
        List<Ledger> results = new ArrayList<>();

        return results;
    }
    /**
     * Revoke a credential
     * @param request The revocation request with credential ID and reason
     * @return Success message if revocation is successful
     * @throws RevocationException if revocation fails
     */
    @Override
    @Transactional
    public CredentialRevocationResponse revokeCredential(CredentialRevocationRequest request) throws RevocationException, CredentialNotFoundException {
        try {
            // Find the credential to revoke
            Optional<Ledger> credentialOptional = ledgerRepository.findByCredentialId(request.getCredentialId());

            if (!credentialOptional.isPresent()) {
                throw new CredentialNotFoundException("Credential not found with ID: " + request.getCredentialId());
            }

            Ledger credential = credentialOptional.get();

            // Check if already revoked
            if ("revoked".equals(credential.getCredentialStatus())) {
                throw new RevocationException("Credential already revoked: " + request.getCredentialId());
            }

            // Update the credential status
            credential.setCredentialStatus("revoked");
            credential.setStatusPurpose("revocation");
            // Update the status list credential
            bitStringStatusListService.updateStatusListBitstring(credential);

            // Save the updated credential status
            ledgerRepository.save(credential);

            CredentialRevocationResponse response = new CredentialRevocationResponse();
            response.setMessage("Credential successfully revoked");
            return response;
        } catch (CredentialNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RevocationException("Error revoking credential: " + e.getMessage(), e);
        }
    }

}


