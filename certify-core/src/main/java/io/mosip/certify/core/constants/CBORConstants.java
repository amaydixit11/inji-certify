package io.mosip.certify.core.constants;

/**
 * Constants for CBOR (Concise Binary Object Representation) handling
 * Supporting ISO 18013-7 CBOR transport encoding
 */
public class CBORConstants {

    // CBOR Tags (as per ISO 18013-5 and ISO 18013-7)
    public static final int CBOR_TAG_ENCODED_CBOR = 24;  // Tag for encoded CBOR data
    public static final int CBOR_TAG_FULL_DATE = 1004;   // Tag for full date strings
    public static final int CBOR_TAG_COSE_SIGN1 = 18;    // Tag for COSE_Sign1 structures

    // CBOR Major Types
    public static final int CBOR_MAJOR_TYPE_0 = 0;  // Unsigned integer
    public static final int CBOR_MAJOR_TYPE_1 = 1;  // Negative integer
    public static final int CBOR_MAJOR_TYPE_2 = 2;  // Byte string
    public static final int CBOR_MAJOR_TYPE_3 = 3;  // Text string
    public static final int CBOR_MAJOR_TYPE_4 = 4;  // Array
    public static final int CBOR_MAJOR_TYPE_5 = 5;  // Map
    public static final int CBOR_MAJOR_TYPE_6 = 6;  // Tag
    public static final int CBOR_MAJOR_TYPE_7 = 7;  // Float/Simple/Break

    // CBOR Encoding Options
    public static final String CBOR_ENCODING_UTF8 = "UTF-8";
    public static final String CBOR_ENCODING_HEX = "hex";
    public static final String CBOR_ENCODING_BASE64 = "base64";
    public static final String CBOR_ENCODING_BASE64URL = "base64url";

    // CBOR Content Types
    public static final String CBOR_CONTENT_TYPE = "application/cbor";
    public static final String CBOR_DIAGNOSTIC_CONTENT_TYPE = "application/cbor-diagnostic";

    // CBOR Special Values
    public static final byte CBOR_FALSE = (byte) 0xF4;
    public static final byte CBOR_TRUE = (byte) 0xF5;
    public static final byte CBOR_NULL = (byte) 0xF6;
    public static final byte CBOR_UNDEFINED = (byte) 0xF7;

    // CBOR Array/Map Lengths
    public static final int CBOR_INDEFINITE_LENGTH = -1;

    // Error Messages
    public static final String CBOR_ENCODING_ERROR = "Error encoding to CBOR";
    public static final String CBOR_DECODING_ERROR = "Error decoding from CBOR";
    public static final String CBOR_TAG_ERROR = "Error handling CBOR tag";
    public static final String CBOR_INVALID_FORMAT = "Invalid CBOR format";
    public static final String CBOR_UNSUPPORTED_TYPE = "Unsupported CBOR type";

    private CBORConstants() {
        // Private constructor to prevent instantiation
    }
}