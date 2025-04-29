package io.mosip.certify.services;

import io.mosip.certify.entity.StatusListCredentials;
import io.mosip.certify.entity.Ledger;
import io.mosip.certify.exception.RevocationException;
import io.mosip.certify.repository.LedgerRepository;
import io.mosip.certify.repository.StatusListCredentialRepository;
import io.mosip.certify.utils.BitStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Service
public class BitStringStatusListService {

    private static final int MINIMUM_BITSTRING_SIZE = 131072; // 16 KB
    private static final int STATUS_SIZE = 1; // Default status size as per spec

    @Autowired
    private StatusListCredentialRepository statusListCredentialRepository;

    @Autowired
    private LedgerRepository ledgerRepository;


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
        Optional<StatusListCredentials> statusListOptional =
                statusListCredentialRepository.findById(statusListCredentialUrl);

        if (statusListOptional.isEmpty()) {
            throw new RuntimeException("Status List Credential not found");
        }

        StatusListCredentials statusList = statusListOptional.get();


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
    public void updateStatusListBitstring(Ledger credential) throws RevocationException {
        log.info("Starting to update status list bitstring for credential: {}", credential.getCredentialId());
        try {
            String statusListCredentialUrl = credential.getStatusListCredentialUrl();
            String statusPurpose = credential.getStatusPurpose();
            long statusListIndex = credential.getStatusListIndex();

            log.info("Status list details - URL: {}, Purpose: {}, Index: {}",
                    statusListCredentialUrl, statusPurpose, statusListIndex);

            // 1. Get the status list credential from the URL
            log.info("Fetching status list credential from repository");
            Optional<StatusListCredentials> statusListOptional =
                    statusListCredentialRepository.findById(statusListCredentialUrl);

            if (statusListOptional.isEmpty()) {
                log.error("Status List Credential not found for URL: {}", statusListCredentialUrl);
                throw new RevocationException("Status List Credential not found for URL: " + statusListCredentialUrl);
            }

            StatusListCredentials statusList = statusListOptional.get();
            log.info("Retrieved status list credential: {}", statusList.getId());

            // 2. Get the compressed bitstring
            String encodedList = statusList.getEncodedList();
            log.info("Retrieved encoded list of size: {}",
                    encodedList != null ? encodedList.length() : "null");

            // 3. Decompress the bitstring to a mutable format
            log.info("Decompressing bitstring to byte array");
            byte[] bitstring = BitStringUtils.toByteArray(BitStringUtils.expandCompressedList(encodedList), MINIMUM_BITSTRING_SIZE);
            log.info("Decompressed bitstring to byte array of size: {}", bitstring.length);

            // 4. Set the bit at the credential's index to 1 (revoked)
            log.info("Setting bit at index {} to 1 (revoked)", statusListIndex);
            BitStringUtils.setBitAtIndex(bitstring, statusListIndex, (byte) 1);
            log.info("Bit successfully set at index {}", statusListIndex);

            // 5. Compress the updated bitstring
            log.info("Compressing updated bitstring");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOS = new GZIPOutputStream(baos)) {
                gzipOS.write(bitstring);
                log.info("Wrote {} bytes to GZIP output stream", bitstring.length);
            }
            String updatedEncodedList = Base64.getUrlEncoder().withoutPadding().encodeToString(baos.toByteArray());
            log.info("Compressed updated bitstring to encoded list of size: {}", updatedEncodedList.length());

            // 6. Update the status list credential with the new bitstring
            log.info("Updating status list credential with new encoded list");
            statusList.setEncodedList(updatedEncodedList);

            // 7. Update the timestamp and other metadata
            // statusList.setLastUpdated(LocalDateTime.now());
            log.info("Metadata update step (currently commented out)");

            // 8. Generate cryptographic proof for the updated status list
            // This would typically involve creating a digital signature or other cryptographic proof
            // For now, we'll leave this as a placeholder
            // statusList.setProof(generateProof(statusList));
            log.info("Proof generation step (currently commented out)");

            // 9. Save the updated status list credential
            log.info("Saving updated status list credential to repository");
            statusListCredentialRepository.save(statusList);
            log.info("Successfully updated status list bitstring for credential ID: {}", credential.getCredentialId());
        } catch (Exception e) {
            log.error("Failed to update status list bitstring for credential ID: {}", credential.getCredentialId(), e);
            throw new RevocationException("Failed to update status list bitstring: " + e.getMessage(), e);
        }
    }
}