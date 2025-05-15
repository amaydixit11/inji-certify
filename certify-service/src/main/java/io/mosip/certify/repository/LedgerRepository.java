package io.mosip.certify.repository;

import io.mosip.certify.entity.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LedgerRepository extends JpaRepository<Ledger, String> {
    Optional<Ledger> findByCredentialId(String credentialId);
    Optional<Ledger> findByCredentialIdAndIssuerId(String credentialId, String issuerId);
    List<Ledger> findByIssuerId(String issuerId);
    List<Ledger> findByStatusListCredentialUrl(String statusListUrl);
    long countByStatusListCredentialUrl(String statusListUrl);
}