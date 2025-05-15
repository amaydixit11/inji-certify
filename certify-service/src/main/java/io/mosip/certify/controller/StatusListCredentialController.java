package io.mosip.certify.controller;

import io.mosip.certify.core.constants.ErrorConstants;
import io.mosip.certify.core.dto.StatusListCredentialDto;
import io.mosip.certify.core.exception.CertifyException;
import io.mosip.certify.services.StatusListCredentialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controller for managing Status List Credential operations.
 * Implements the Status List Credential endpoints defined in the API spec.
 */
@RestController
@RequestMapping("/api/v1/status-list-credentials")
@Slf4j
public class StatusListCredentialController {

    @Autowired
    private StatusListCredentialService statusListCredentialService;

    /**
     * Get Status List Credential by ID
     *
     * @param id The unique ID (URL/URN/DID) of the Status List VC
     * @param timestamp Optional timestamp for time-based retrieval (if supported)
     * @return Status List Credential
     */
    @GetMapping("/{id}")
    public ResponseEntity<StatusListCredentialDto> getStatusListCredentialById(
            @PathVariable String id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp) {

        log.info("Retrieving Status List Credential for id: {}", id);

        try {
            StatusListCredentialDto credential = statusListCredentialService.getStatusListCredential(id, timestamp);

            // Set cache-related headers
            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl("max-age=3600");
            headers.setETag("\"" + credential.getId() + "\"");
            if (credential.getValidFrom() != null) {
                headers.setLastModified(credential.getValidFrom().toEpochSecond(java.time.ZoneOffset.UTC) * 1000);
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(credential);
        } catch (IllegalArgumentException e) {
            log.error("Invalid timestamp format", e);
            return ResponseEntity.badRequest().build();
        } catch (CertifyException e) {
            if (e.getErrorCode().equals(ErrorConstants.STATUS_RETRIEVAL_ERROR)) {
                log.error("Status List Credential not found or retrieval error", e);
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }
}