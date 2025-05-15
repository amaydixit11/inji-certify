package io.mosip.certify.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "status_list_credentials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatusListCredential {
    @Id
    private String id;

    @Column(name = "vc_document", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Object vcDocument;

    @Column(name = "status_list_type", nullable = false)
    private String statusListType;

    @Column(name = "list_purpose")
    private String listPurpose;

    @Column(name = "length", nullable = false)
    private Long length;

    @Column(name = "crd_times", nullable = false)
    private LocalDateTime crdTimes;

    @Column(name = "upd_times")
    private LocalDateTime updTimes;
}
