package io.mosip.certify.services;

import io.mosip.certify.api.dto.VCResult;
import io.mosip.certify.core.constants.Constants;
import io.mosip.certify.core.constants.ErrorConstants;
import io.mosip.certify.core.exception.CertifyException;
import io.mosip.certify.entity.StatusListCredentials;
import io.mosip.certify.repository.StatusListCredentialRepository;
import io.mosip.certify.vcsigners.VCSigner;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.mapping.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class StatusListUpdaterService {

    @Autowired
    private StatusListCredentialRepository statusListRepository;

    @Autowired
    private VCSigner vcSigner;

    /**
     * Updates the status (e.g., revoked) of a credential in a Status List
     *
     * @param listUrl The URL of the Status List VC to update
     * @param listIndex The index position in the list
     * @param newStatus The new status value (true for revoked)
     * @return Updated Status List VC
     */
    public java.util.Map<String, Object> updateStatus(String listUrl, long listIndex, boolean newStatus) {
        // 1. Get current Status List VC by URL
        StatusListCredentials statusListCredential = statusListRepository.findById(listUrl)
                .orElseThrow(() -> new CertifyException(ErrorConstants.STATUS_LIST_NOT_FOUND));

        // 2. Extract the encoded list from the VC document
        JSONObject vcDoc = new JSONObject(statusListCredential.getVcDocument());
        JSONObject credentialSubject = vcDoc.getJSONObject("credentialSubject");
        String encodedList = credentialSubject.getString("encodedList");

        // 3. Decode the list, update the bit at the specified index, and re-encode
        byte[] decodedList = Base64.getDecoder().decode(encodedList);
        BitSet bitSet = BitSet.valueOf(decodedList);
        bitSet.set((int) listIndex, newStatus);
        byte[] updatedBytes = new byte[(int) Math.ceil(statusListCredential.getLength() / 8.0)];
        byte[] bitSetBytes = bitSet.toByteArray();
        System.arraycopy(bitSetBytes, 0, updatedBytes, 0, Math.min(bitSetBytes.length, updatedBytes.length));
        String updatedEncodedList = Base64.getEncoder().encodeToString(updatedBytes);

        // 4. Update the VC document with the new encoded list
        credentialSubject.put("encodedList", updatedEncodedList);

        // 5. Sign the updated Status List VC
        Map<String, String> signerSettings = new HashMap<>();
        signerSettings.put(Constants.APPLICATION_ID, Constants.CERTIFY_VC_SIGN_ED25519);
        signerSettings.put(Constants.REFERENCE_ID, Constants.ED25519_REF_ID);

        VCResult<?> result = vcSigner.attachSignature(vcDoc.toString(), signerSettings);

        // 6. Save the updated Status List VC
        statusListCredential.setVcDocument(result.getCredential().toString());
        statusListCredential.setUpdTimes(LocalDateTime.now(ZoneOffset.UTC));
        statusListRepository.save(statusListCredential);

        return (Map<String, Object>) result.getCredential();
    }

    /**
     * Finds or creates a usable Status List VC for a given purpose
     *
     * @param purpose The intended purpose (e.g., "revocation")
     * @param issuer The issuer URI
     * @return A StatusListCredential with available slots
     */
    public StatusListCredentials findUsableStatusList(String purpose, String issuer) {
        // Find existing status list with available slots
        List<StatusListCredentials> availableLists = statusListRepository
                .findByListPurposeAndAvailableSlots(purpose);

        if (!availableLists.isEmpty()) {
            return availableLists.get(0);
        }

        // No available list found, create a new one
        return createNewStatusList(purpose, issuer);
    }

    private StatusListCredentials createNewStatusList(String purpose, String issuer) {
        // Implement status list creation
        // This would generate a new Status List VC with appropriate size
        // Sign it and save to repository
        // Return the new status list credential

        // Implementation details would depend on your specific requirements
        // for list size, ID generation, etc.
    }
}