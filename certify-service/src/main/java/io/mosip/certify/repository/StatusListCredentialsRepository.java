package io.mosip.certify.repository;

import io.mosip.certify.entity.StatusListCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusListCredentialsRepository extends JpaRepository<StatusListCredential, String> {
    List<StatusListCredential> findByListPurpose(String purpose);
    List<StatusListCredential> findByListPurposeAndAvailableSlots(String purpose);
    List<StatusListCredential> findByIssuerIdAndStatusPurposeAndStatus();
}