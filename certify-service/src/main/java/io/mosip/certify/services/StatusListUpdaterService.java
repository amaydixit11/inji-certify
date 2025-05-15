package io.mosip.certify.services;

import io.mosip.certify.api.dto.VCResult;
import io.mosip.certify.core.constants.Constants;
import io.mosip.certify.core.constants.ErrorConstants;
import io.mosip.certify.core.exception.CertifyException;
import io.mosip.certify.entity.StatusListCredential;
import io.mosip.certify.repository.StatusListCredentialsRepository;
import io.mosip.certify.vcsigners.VCSigner;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.mapping.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
@Service
public class StatusListUpdaterService {
    private static final int MINIMUM_BITSTRING_SIZE = 131072;

    @Autowired
    private StatusListCredentialsRepository statusListCredentialsRepository;

    @Autowired
    private VCSigner vcSigner;

    @Value("${mosip.certify.domain.url}")
    private String domainUrl;

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
        StatusListCredential statusListCredential = statusListCredentialsRepository.findById(listUrl)
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
        statusListCredentialsRepository.save(statusListCredential);

        return (Map<String, Object>) result.getCredential();
    }

    /**
     * Find a usable status list for the given purpose and issuer
     */
    public StatusListCredential findUsableStatusList(String statusPurpose, String issuerId) {
        Optional<StatusListCredential> existingList = statusListCredentialsRepository
                .findByIssuerIdAndStatusPurposeAndStatus(issuerId, statusPurpose, "ACTIVE");

        if (existingList.isPresent()) {
            return existingList.get();
        }

        // No suitable list found, create a new one
        return createNewStatusList(issuerId, statusPurpose);
    }

    /**
     * Creates a new status list credential
     */
    @Transactional
    public StatusListCredential createNewStatusList(String issuerId, String statusPurpose) {
        // Generate a unique ID for the status list
        String statusListId = domainUrl + "/status-list-credentials/" + UUID.randomUUID().toString();

        try {
            // Create empty bitstring (all zeros)
            String encodedList = bitStringStatusListService.createEmptyBitString(STATUS_SIZE);

            // Create the status list VC structure
            Map<String, Object> vcData = new HashMap<>();
            vcData.put("@context", Arrays.asList(
                    "https://www.w3.org/2018/credentials/v1",
                    "https://w3id.org/vc/status-list/2021/v1"
            ));
            vcData.put("id", statusListId);
            vcData.put("type", Arrays.asList("VerifiableCredential", "StatusList2021Credential"));
            vcData.put("issuer", issuerId);
            vcData.put("validFrom", ZonedDateTime.now(ZoneOffset.UTC).toString());

            Map<String, Object> credentialSubject = new HashMap<>();
            credentialSubject.put("id", statusListId + "#list");
            credentialSubject.put("type", "StatusList2021");
            credentialSubject.put("statusPurpose", statusPurpose);
            credentialSubject.put("encodedList", encodedList);

            vcData.put("credentialSubject", credentialSubject);

            // Sign the status list VC
            Map<String, String> signerSettings = new HashMap<>();
            // Use appropriate keys from keyChooser
            signerSettings.put(Constants.APPLICATION_ID, keyChooser.get(vcSignAlgorithm).getFirst());
            signerSettings.put(Constants.REFERENCE_ID, keyChooser.get(vcSignAlgorithm).getLast());

            // Convert vcData to JSON string
            String unsignedVC = new JSONObject(vcData).toString();
            VCResult<?> vcResult = vcSigner.attachSignature(unsignedVC, signerSettings);

            if (vcResult == null || vcResult.getCredential() == null) {
                throw new CertifyException(ErrorConstants.VC_SIGNATURE_FAILED);
            }

            // Create and save the status list entity
            StatusListCredential statusList = new StatusListCredential();
            statusList.setId(statusListId);
            statusList.setIssuerId(issuerId);
            statusList.setStatusPurpose(statusPurpose);
            statusList.setEncodedList(encodedList);
            statusList.setStatus("ACTIVE");
            statusList.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
            statusList.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
            statusList.setVcDocument(vcResult.getCredential().toString());

            return statusListCredentialsRepository.save(statusList);

        } catch (Exception e) {
            log.error("Failed to create status list credential", e);
            throw new CertifyException(ErrorConstants.STATUS_LIST_CREATION_FAILED);
        }
    }
}