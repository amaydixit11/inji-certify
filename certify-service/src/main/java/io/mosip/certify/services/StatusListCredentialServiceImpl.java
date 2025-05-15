//package io.mosip.certify.services;
//
//import io.mosip.certify.core.constants.ErrorConstants;
//import io.mosip.certify.core.dto.StatusListCredentialDto;
//import io.mosip.certify.core.exception.CertifyException;
//import io.mosip.certify.entity.StatusListCredential;
//import io.mosip.certify.repository.StatusListCredentialsRepository;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
///**
// * Implementation of the StatusListCredentialService interface.
// */
//@Service
//@Slf4j
//public class StatusListCredentialServiceImpl implements StatusListCredentialService {
//
//    @Autowired
//    private StatusListCredentialsRepository statusListCredentialRepository;
//
//    @Override
//    public StatusListCredentialDto getStatusListCredential(String id, LocalDateTime timestamp) throws CertifyException {
//        log.info("Getting Status List Credential for id: {}", id);
//
//        StatusListCredential credential;
//
//        if (timestamp != null) {
//            // Time-based retrieval (if your system supports versioning)
//            Optional<StatusListCredential> versionedCredential = statusListCredentialRepository.findByIdAndTimestamp(id, timestamp);
//            credential = versionedCredential.orElseThrow(() ->
//                    new CertifyException(ErrorConstants.STATUS_RETRIEVAL_ERROR, "Status List Credential not found for id: " + id + " at timestamp: " + timestamp));
//        } else {
//            // Latest version retrieval
//            credential = statusListCredentialRepository.findById(id)
//                    .orElseThrow(() -> new CertifyException(ErrorConstants.STATUS_RETRIEVAL_ERROR, "Status List Credential not found for id: " + id));
//        }
//
//        return mapEntityToDto(credential);
//    }
//
//    @Override
//    @Transactional
//    public StatusListCredentialDto updateStatusListCredential(String statusListCredentialId, Long statusListIndex, Boolean status) throws CertifyException {
//        log.info("Updating Status List Credential: {}, index: {}, status: {}", statusListCredentialId, statusListIndex, status);
//
////        // Retrieve the status list credential
////        StatusListCredential credential = statusListCredentialRepository.findById(statusListCredentialId)
////                .orElseThrow(() -> new CertifyException(ErrorConstants.STATUS_RETRIEVAL_ERROR, "Status List Credential not found for id: " + statusListCredentialId));
////
////        // Decode the encoded list
////        byte[] bitString = BitStringUtils.decodeBase64UrlToBitString(credential.getEncodedList());
////
////        // Validate the index
////        if (statusListIndex < 0 || statusListIndex >= (bitString.length * 8)) {
////            throw new CertifyException(ErrorConstants.STATUS_UPDATE_ERROR, "Index out of bounds: " + statusListIndex);
////        }
////
////        // Update the bit at the specified index
////        BitStringUtils.setBit(bitString, statusListIndex.intValue(), status);
////
////        // Encode the updated bit string
////        String updatedEncodedList = BitStringUtils.encodeBitStringToBase64Url(bitString);
////        credential.setEncodedList(updatedEncodedList);
////
////        // Update last modified timestamp and possibly increment version
////        credential.setLastModified(LocalDateTime.now());
////
////        // If your system uses signatures/proofs, regenerate the proof here
////        updateCredentialProof(credential);
////
////        // Save the updated credential
////        credential = statusListCredentialRepository.save(credential);
////
////        return mapEntityToDto(credential);
//    }
//
//    /**
//     * Map a StatusListCredential entity to a StatusListCredentialDto
//     */
//    private StatusListCredentialDto mapEntityToDto(StatusListCredential entity) {
//        StatusListCredentialDto dto = new StatusListCredentialDto();
////        dto.setId(entity.getId());
////        dto.setContext(entity.getContext());
////        dto.setType(entity.getType());
////        dto.setIssuer(entity.getIssuer());
////        dto.setValidFrom(entity.getValidFrom());
////        dto.setValidUntil(entity.getValidUntil());
////
////        // Map credential subject
////        StatusListCredentialDto.CredentialSubject subject = new StatusListCredentialDto.CredentialSubject();
////        subject.setId(entity.getSubjectId());
////        subject.setType(entity.getSubjectType());
////        subject.setStatusPurpose(entity.getStatusPurpose());
////        subject.setEncodedList(entity.getEncodedList());
////        dto.setCredentialSubject(subject);
////
////        // Map proof if available
////        if (entity.getProofType() != null) {
////            StatusListCredentialDto.Proof proof = new StatusListCredentialDto.Proof();
////            proof.setType(entity.getProofType());
////            proof.setCreated(entity.getProofCreated());
////            proof.setVerificationMethod(entity.getVerificationMethod());
////            proof.setProofPurpose(entity.getProofPurpose());
////            proof.setProofValue(entity.getProofValue());
////            dto.setProof(proof);
////        }
//
//        return dto;
//    }
//
//    /**
//     * Update or regenerate the cryptographic proof for a StatusListCredential.
//     * This method would typically involve:
//     * 1. Preparing the credential data for signing
//     * 2. Using a key or service to create a signature
//     * 3. Updating the proof fields in the credential
//     */
//    private void updateCredentialProof(StatusListCredential credential) {
//        // This is a placeholder implementation
//        // In a real-world scenario, you would:
//        // 1. Use a signing service or key management system to access the appropriate signing key
//        // 2. Generate a canonical representation of the credential (minus the proof)
//        // 3. Create a digital signature using the appropriate algorithm
//        // 4. Update the proof fields with the new signature and metadata
//
//        // For now we just update the timestamp
////        credential.setProofCreated(LocalDateTime.now());
//
//        // In a real implementation, you would do something like:
//        // String canonicalizedData = canonicalize(credential);
//        // String signature = signingService.sign(canonicalizedData, keyId);
//        // credential.setProofValue(signature);
//    }
//}