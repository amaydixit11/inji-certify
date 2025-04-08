package io.mosip.certify.services;

import io.mosip.certify.core.dto.*;
import io.mosip.certify.exception.CredentialIssuanceException;
import io.mosip.certify.exception.CredentialNotFoundException;
import io.mosip.certify.exception.RevocationException;

public interface RevocationService {

    /**
     * Fetch credential status information based on filters.
     *
     * @param request The credential fetch request containing filters.
     * @return The credential status information.
     * @throws CredentialNotFoundException if the credential cannot be found.
     */
    CredentialFetchResponse fetchCredential(CredentialFetchRequest request) throws CredentialNotFoundException;

    /**
     * Revoke a credential.
     *
     * @param request The revocation request with credential ID and reason.
     * @return Success message if revocation is successful.
     * @throws RevocationException if revocation fails.
     */
    CredentialRevocationResponse revokeCredential(CredentialRevocationRequest request) throws RevocationException, CredentialNotFoundException;
}
