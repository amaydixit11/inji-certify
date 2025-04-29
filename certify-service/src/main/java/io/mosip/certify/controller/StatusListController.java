package io.mosip.certify.controller;

import io.mosip.certify.core.constants.ErrorConstants;
import io.mosip.certify.core.dto.CredentialStatusRequestDto;
import io.mosip.certify.core.dto.CredentialStatusResponseDto;
import io.mosip.certify.core.exception.CertifyException;
import io.mosip.certify.entity.Ledger;
import io.mosip.certify.repository.LedgerRepository;
import io.mosip.certify.services.StatusListUpdaterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/credentials/status")
@Slf4j
public class StatusListController {

    @Autowired
    private StatusListUpdaterService statusListUpdaterService;

    @Autowired
    private LedgerRepository ledgerRepository;

    @PostMapping
    public ResponseEntity<CredentialStatusResponseDto> updateCredentialStatus(
            @RequestBody CredentialStatusRequestDto requestDto) {

        log.info("Credential status update request received for credential: {}", requestDto.getCredentialId());

        Ledger ledgerEntry = ledgerRepository.findByCredentialId(requestDto.getCredentialId())
                .orElseThrow(() -> new CertifyException(ErrorConstants.CREDENTIAL_NOT_FOUND));

        if (ledgerEntry.getStatusListCredentialUrl() != null && ledgerEntry.getStatusListIndex() != null) {
            statusListUpdaterService.updateStatus(
                    ledgerEntry.getStatusListCredentialUrl(),
                    ledgerEntry.getStatusListIndex(),
                    requestDto.isRevoked());
        }

        ledgerEntry.setCredentialStatus(requestDto.isRevoked() ? "revoked" : "valid");
        ledgerRepository.save(ledgerEntry);

        CredentialStatusResponseDto responseDto = new CredentialStatusResponseDto();
        responseDto.setCredentialId(requestDto.getCredentialId());
        responseDto.setStatus(ledgerEntry.getCredentialStatus());
        responseDto.setUpdatedAt(LocalDateTime.now());

        return ResponseEntity.ok(responseDto);
    }
}