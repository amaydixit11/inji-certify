-- This Source Code Form is subject to the terms of the Mozilla Public
-- License, v. 2.0. If a copy of the MPL was not distributed with this
-- file, You can obtain one at https://mozilla.org/MPL/2.0/.
-- -------------------------------------------------------------------------------------------------
-- Database Name: inji_certify
-- Table Name : ledger
-- Purpose    : Stores status information for all verifiable credentials following Bitstring status list standards
--
--
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- ------------------------------------------------------------------------------------------
CREATE TABLE certify.ledger(
    credential_id VARCHAR(255) PRIMARY KEY,   -- Unique ID of the credential whose status is being tracked
    issuer_id VARCHAR(255) NOT NULL,          -- Issuer of the credential being tracked
    status_list_credential_url VARCHAR(255),  -- This column holds the vc.id of the corresponding status list credential
    status_list_index BIGINT,                 -- Index within the status list for this credential
    status_purpose VARCHAR(100),             -- Purpose of the status (e.g., 'revocation', 'suspension')
    credential_status VARCHAR(50) NOT NULL,   -- Current status (e.g., 'valid', 'revoked', 'suspended')
    issue_date TIMESTAMPTZ NOT NULL,          -- Issuance date of the tracked credential
    expiration_date TIMESTAMPTZ,              -- Expiration date of the tracked credential, if any
    credential_type VARCHAR(100) NOT NULL,    -- Type of the tracked credential (e.g., 'VerifiableId')
    indexed_attributes JSONB,                 -- Optional searchable attributes extracted from the tracked credential
    CONSTRAINT fk_status_list_credential_vc   -- Constraint name kept
        FOREIGN KEY(status_list_credential_url)
        REFERENCES status_list_credentials(id)  -- References the 'id' column in status_list_credentials
        ON DELETE RESTRICT                     -- Prevent deletion of status list if credentials refer to it
        ON UPDATE CASCADE                      -- If the status list ID changes, update references here
);
COMMENT ON TABLE ledger IS 'Tracks the status of individual Verifiable Credentials,  linking them to a Status List entry.';
COMMENT ON COLUMN ledger.credential_id IS 'Unique identifier of the credential whose status is being tracked.';
COMMENT ON COLUMN ledger.issuer_id IS 'Identifier of the issuer of the credential being tracked.';
COMMENT ON COLUMN ledger.status_list_credential_url IS 'Reference to the ID (URL/DID/URN) of the Status List VC used for this credential''s status. Foreign Key to status_list_credentials.id.';
COMMENT ON COLUMN ledger.status_list_index IS 'The zero-based index within the referenced Status List corresponding to this credential.';
COMMENT ON COLUMN ledger.status_purpose IS 'The purpose associated with the status entry (e.g., revocation, suspension). Typically from credentialStatus.statusPurpose.';
COMMENT ON COLUMN ledger.credential_status IS 'The current derived status of the credential (e.g., valid, revoked, suspended).';
COMMENT ON COLUMN ledger.issue_date IS 'Issuance date of the credential being tracked.';
COMMENT ON COLUMN ledger.expiration_date IS 'Expiration date of the credential being tracked, if applicable.';
COMMENT ON COLUMN ledger.credential_type IS 'The type(s) of the credential being tracked (e.g., VerifiableId, ProofOfEnrollment).';
COMMENT ON COLUMN ledger.indexed_attributes IS 'Stores specific attributes extracted from the tracked credential for optimized searching (e.g., subject ID, specific claim values).';
-- Indexes (Corrected table name from credential_status to ledger)
CREATE INDEX IF NOT EXISTS idx_ledger_issuer ON ledger(issuer_id);
CREATE INDEX IF NOT EXISTS idx_ledger_status_list_url ON ledger(status_list_credential_url); -- Index on FK for joins/lookups
CREATE INDEX IF NOT EXISTS idx_ledger_credential_status ON ledger(credential_status); -- Index for querying by status
CREATE INDEX IF NOT EXISTS idx_gin_ledger_indexed_attrs ON ledger USING GIN (indexed_attributes); -- Index for searching within JSON attributes
ance_table.revocation_proof IS 'Cryptographic proof or hash representing the integrity of the revocation action';