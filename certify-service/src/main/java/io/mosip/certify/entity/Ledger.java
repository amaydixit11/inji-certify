package io.mosip.certify.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "ledger")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ledger {
    @Id
    @Column(name = "credential_id", nullable = false)
    private String credentialId;

    @Column(name = "issuer_id", nullable = false)
    private String issuerId;

    @Column(name = "status_list_credential_url")
    private String statusListCredentialUrl;

    @Column(name = "status_list_index", nullable = false)
    private Long statusListIndex;

    @Column(name = "status_purpose", nullable = false)
    private String statusPurpose;

    @Column(name = "credential_status", nullable = false)
    private String credentialStatus = "valid";

    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Column(name = "credential_type", nullable = false)
    private String credentialType;

    @Column(name = "indexed_attributes")
    @JdbcTypeCode(SqlTypes.JSON)
    private Object indexedAttributes;
}
