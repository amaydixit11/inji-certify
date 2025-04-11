package io.mosip.certify.services;

import io.mosip.certify.entity.StatusListCredential;
import io.mosip.certify.entity.LedgerIssuanceTable;
import io.mosip.certify.exception.RevocationException;
import io.mosip.certify.repository.StatusListCredentialRepository;
import io.mosip.certify.repository.LedgerIssuanceTableRepository;
import io.mosip.certify.utils.BitStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Service
public class BitStringStatusListService {

    private static final int MINIMUM_BITSTRING_SIZE = 16 * 1024; // 16 KB
    private static final int STATUS_SIZE = 1; // Default status size as per spec

    @Autowired
    private StatusListCredentialRepository statusListCredentialRepository;

    @Autowired
    private LedgerIssuanceTableRepository ledgerIssuanceTableRepository;


    /**
     * Validate Credential Status as per Section 3.2 Validate Algorithm
     *
     * @param statusListCredentialUrl URL of the status list credential
     * @param statusListIndex Index of the credential in the status list
     * @param statusPurpose Purpose of the status (e.g., "revocation")
     * @return Validation result
     */
    public boolean validateCredentialStatus(String statusListCredentialUrl, long statusListIndex, String statusPurpose) {
        // Retrieve status list credential
        Optional<StatusListCredential> statusListOptional =
                statusListCredentialRepository.findById(statusListCredentialUrl);

        if (statusListOptional.isEmpty()) {
            throw new RuntimeException("Status List Credential not found");
        }

        StatusListCredential statusList = statusListOptional.get();

        // Verify status purpose matches
        if (!statusPurpose.equals(statusList.getStatusPurpose())) {
            throw new RuntimeException("Status Purpose Mismatch");
        }

        // Expand compressed bitstring
        byte[] uncompressedBitstring = decompressAndDecodebitstring(statusList.getEncodedList());

        // Validate list length (minimum 131,072 entries)
        if (uncompressedBitstring.length / STATUS_SIZE < 131_072) {
            throw new RuntimeException("Status List Length Too Short");
        }

        // Check credential status
        int index = (int) (statusListIndex * STATUS_SIZE);
        if (index >= uncompressedBitstring.length) {
            throw new RuntimeException("Status List Index Out of Range");
        }

        // Return true if bit is 0 (valid), false if bit is 1 (revoked/invalid)
        return uncompressedBitstring[index] == 0;
    }


    /**
     * Decompress bitstring from Base64url and GZIP
     *
     * @param compressedBitstring Compressed and Base64url encoded bitstring
     * @return Uncompressed bitstring
     */
    private byte[] decompressAndDecodebitstring(String compressedBitstring) {
        try {
            byte[] compressedBytes = Base64.getUrlDecoder().decode(compressedBitstring);
            ByteArrayInputStream bais = new ByteArrayInputStream(compressedBytes);
            GZIPInputStream gzipIS = new GZIPInputStream(bais);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIS.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }

            gzipIS.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error decompressing bitstring", e);
            throw new RuntimeException("Bitstring Decompression Failed", e);
        }
    }

    /**
     * Updates the bitstring in the status list credential for the given credential
     * @param credential The credential being revoked
     * @throws RevocationException if there's an error updating the bitstring
     */
    public void updateStatusListBitstring(LedgerIssuanceTable credential) throws RevocationException {
        try {
            String statusListCredentialUrl = credential.getStatusListCredential();
            String statusPurpose = credential.getStatusPurpose();
            long statusListIndex = credential.getStatusListIndex();

            // 1. Get the status list credential from the URL
            Optional<StatusListCredential> statusListOptional =
                    statusListCredentialRepository.findById(statusListCredentialUrl);

            if (statusListOptional.isEmpty()) {
                throw new RevocationException("Status List Credential not found for URL: " + statusListCredentialUrl);
            }

            StatusListCredential statusList = statusListOptional.get();

            // 2. Get the compressed bitstring
            String encodedList = statusList.getEncodedList();

            // 3. Decompress the bitstring to a mutable format
            byte[] bitstring = BitStringUtils.toByteArray(BitStringUtils.expandCompressedList(encodedList), MINIMUM_BITSTRING_SIZE);

            // 4. Set the bit at the credential's index to 1 (revoked)
            BitStringUtils.setBitAtIndex(bitstring, statusListIndex, (byte) 1);

            // 5. Compress the updated bitstring
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOS = new GZIPOutputStream(baos)) {
                gzipOS.write(bitstring);
            }
            String updatedEncodedList = Base64.getUrlEncoder().withoutPadding().encodeToString(baos.toByteArray());

            // 6. Update the status list credential with the new bitstring
            statusList.setEncodedList(updatedEncodedList);

            // 7. Update the timestamp and other metadata
            // statusList.setLastUpdated(LocalDateTime.now());

            // 8. Generate cryptographic proof for the updated status list
            // This would typically involve creating a digital signature or other cryptographic proof
            // For now, we'll leave this as a placeholder
            // statusList.setProof(generateProof(statusList));

            // 9. Save the updated status list credential
            statusListCredentialRepository.save(statusList);

            log.info("Updated status list bitstring for credential ID: {}", credential.getCredentialId());
        } catch (Exception e) {
            log.error("Failed to update status list bitstring", e);
            throw new RevocationException("Failed to update status list bitstring: " + e.getMessage(), e);
        }
    }
}