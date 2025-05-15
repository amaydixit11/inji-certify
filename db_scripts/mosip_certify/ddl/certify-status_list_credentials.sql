-- -------------------------------------------------------------------------------------------------
-- Database Name: inji_certify
-- Table Name : status_list_credential
-- Purpose : Stores BitString status lists for credential status tracking following the standards
--
-- Modified Date Modified By Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- ------------------------------------------------------------------------------------------
CREATE TABLE certify.status_list_credentials (
    id VARCHAR(255) PRIMARY KEY,           -- The unique ID (URL/DID/URN) extracted from the VC's 'id' field.
    vc_document JSONB NOT NULL,             -- Stores the entire Verifiable Credential JSON document.
    status_list_type VARCHAR(100) NOT NULL, -- Type of the status list (e.g., 'StatusList2021Credential')
    list_purpose VARCHAR(100),              -- Intended purpose of this list within the system (e.g., 'revocation', 'suspension', 'general'). NULLABLE.
    length BIGINT --- length of status list
    crd_times TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL, -- When this VC was added/refreshed in the system
    upd_times TIMESTAMPTZ                     -- When this VC record was last updated in the system
);
COMMENT ON TABLE status_list_credentials IS 'Stores full Status List Verifiable Credentials, including their type and intended purpose within the system.';
COMMENT ON COLUMN status_list_credentials.id IS 'Unique identifier (URL/DID/URN) of the Status List VC (extracted from vc_document.id). Primary Key.';
COMMENT ON COLUMN status_list_credentials.vc_document IS 'The complete JSON document of the Status List Verifiable Credential.';
COMMENT ON COLUMN status_list_credentials.status_list_type IS 'The type of the Status List credential, often found in vc_document.type (e.g., StatusList2021Credential).';
COMMENT ON COLUMN status_list_credentials.list_purpose IS 'The intended purpose assigned to this entire Status List within the system (e.g., revocation, suspension, general). This may be based on convention or system policy, distinct from the credentialStatus.statusPurpose used by individual credentials.';
COMMENT ON COLUMN status_list_credentials.crd_times IS 'Timestamp when this Status List VC was first added/fetched into the local system.';
COMMENT ON COLUMN status_list_credentials.upd_times IS 'Timestamp when this Status List VC record was last updated.';
CREATE INDEX IF NOT EXISTS idx_slc_status_list_type ON status_list_credentials(status_list_type);
CREATE INDEX IF NOT EXISTS idx_slc_list_purpose ON status_list_credentials(list_purpose); fy.status_list_credential.ttl IS 'Time to live in milliseconds before a refresh should be attempted';