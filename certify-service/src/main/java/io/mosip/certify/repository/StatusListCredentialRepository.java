package io.mosip.certify.repository;

import io.mosip.certify.entity.StatusListCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatusListCredentialRepository extends JpaRepository<StatusListCredentials, String> {
    List<StatusListCredentials> findByListPurpose(String purpose);
    List<StatusListCredentials> findByListPurposeAndAvailableSlots(String purpose);
}