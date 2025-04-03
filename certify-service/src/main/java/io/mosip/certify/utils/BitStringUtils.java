package io.mosip.certify.utils;

import io.mosip.certify.entity.LedgerIssuanceTable;
import io.mosip.certify.entity.StatusListCredential;
import io.mosip.certify.exception.RevocationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class BitStringUtils {
    private static final int DEFAULT_LIST_SIZE = 131_072; // 16 KB
    private static final int DEFAULT_STATUS_SIZE = 1;

    /**
     * Set a specific bit in a byte array
     * @param bitstring The byte array containing the bitstring
     * @param index The index of the bit to set
     * @param value The value to set (0 or 1)
     */
    public static void setBitAtIndex(byte[] bitstring, long index, byte value) {
        int byteIndex = (int) (index / 8);
        int bitPosition = (int) (index % 8);

        if (byteIndex >= bitstring.length) {
            throw new IndexOutOfBoundsException("Index out of bounds for bitstring");
        }

        if (value == 1) {
            // Set the bit to 1
            bitstring[byteIndex] |= (1 << (7 - bitPosition));
        } else {
            // Set the bit to 0
            bitstring[byteIndex] &= ~(1 << (7 - bitPosition));
        }
    }

    /**
     * Check the value of a bit at a specific index
     * @param bitstring The byte array containing the bitstring
     * @param index The index of the bit to check
     * @return true if the bit is 1, false if the bit is 0
     */
    public static boolean checkBitAtIndex(byte[] bitstring, long index) {
        int byteIndex = (int) (index / 8);
        int bitPosition = (int) (index % 8);

        if (byteIndex >= bitstring.length) {
            throw new IndexOutOfBoundsException("Index out of bounds for bitstring");
        }

        return ((bitstring[byteIndex] & (1 << (7 - bitPosition))) != 0);
    }

    /**
     * Decompress GZIP compressed byte array
     */
    public static byte[] decompressGzip(byte[] compressedBytes) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressedBytes);
             GZIPInputStream gzipIs = new GZIPInputStream(bais);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIs.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        }
    }

    /**
     * Compress bitstring using GZIP and Base64 encode
     */
    public static String compressAndEncodeList(byte[] bitstring) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzipOs = new GZIPOutputStream(baos)) {
            gzipOs.write(bitstring);
            gzipOs.close();
            return Base64.getUrlEncoder().withoutPadding().encodeToString(baos.toByteArray());
        }
    }

    /**
     * Expand compressed and encoded list
     */
    public static byte[] expandCompressedList(String compressedEncodedList) throws Exception {
        try {
            byte[] compressedBytes = Base64.getUrlDecoder().decode(compressedEncodedList);
            return BitStringUtils.decompressGzip(compressedBytes);
        } catch (Exception e) {
            throw new Exception("Error expanding compressed list", e);
        }
    }

    /**
     * Initialize a new bitstring with minimum size according to the specification
     * @return A new bitstring initialized to all zeros
     */
    public static byte[] initializeEmptyBitstring() {
        // Minimum size of 16KB = 16 * 1024 * 8 bits = 16 * 1024 bytes
        return new byte[16 * 1024];
    }

    /**
     * Generate bitstring from issued credentials
     */
    public byte[] generateBitstring(List<LedgerIssuanceTable> issuedCredentials) {
        byte[] bitstring = new byte[DEFAULT_LIST_SIZE / 8]; // Convert bits to bytes

        for (LedgerIssuanceTable entry : issuedCredentials) {
            BitStringUtils.setBitAtIndex(
                    bitstring,
                    entry.getStatusListIndex(),
                    entry.getCredentialStatus().equals("revoked") ? (byte)1 : (byte)0
            );
        }

        return bitstring;
    }

    /**
     * Generate a status list bitstring according to the W3C specification
     * Implements Section 3.3 Bitstring Generation Algorithm
     *
     * @param issuedCredentials List of issued credentials
     * @return Compressed bitstring
     * @throws IOException if compression fails
     */
    public String generateBitstringForCredentials(List<LedgerIssuanceTable> issuedCredentials) throws IOException {
        // 1. Initialize bitstring with minimum size of 16KB
        byte[] bitstring = BitStringUtils.initializeEmptyBitstring();

        // 2. Set appropriate status for each credential in the list
        for (LedgerIssuanceTable credential : issuedCredentials) {
            long index = credential.getStatusListIndex();
            boolean isRevoked = "revoked".equals(credential.getCredentialStatus());
            BitStringUtils.setBitAtIndex(bitstring, index, isRevoked ? (byte) 1 : (byte) 0);
        }

        // 3. Generate a compressed bitstring using GZIP and Base64url encoding
        return BitStringUtils.compressAndEncodeList(bitstring);
    }


}