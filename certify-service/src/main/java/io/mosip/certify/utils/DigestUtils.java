/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.certify.utils;

import io.mosip.certify.core.exception.CertifyException;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for digest calculation and cryptographic operations for mDoc.
 * Provides SHA-256 hashing and secure random salt generation as per ISO 18013-5.
 */
@Slf4j
public class DigestUtils {

    private static final String SHA256_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 24; // 24 bytes as per ISO 18013-5
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Calculate SHA-256 digest of input data
     *
     * @param data The input data to hash
     * @return SHA-256 hash bytes
     * @throws CertifyException if hashing fails
     */
    public static byte[] calculateSHA256(byte[] data) throws CertifyException {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA256_ALGORITHM);
            return digest.digest(data);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available: {}", e.getMessage());
            throw new CertifyException("SHA-256 hashing failed: {}", e.getMessage());
        }
    }

    /**
     * Calculate SHA-256 digest of UTF-8 encoded string
     *
     * @param data The string to hash
     * @return SHA-256 hash bytes
     * @throws CertifyException if hashing fails
     */
    public static byte[] calculateSHA256(String data) throws CertifyException {
        return calculateSHA256(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * Calculate SHA-256 digest and return as Base64 encoded string
     *
     * @param data The input data to hash
     * @return Base64 encoded SHA-256 hash
     * @throws CertifyException if hashing fails
     */
    public static String calculateSHA256Base64(byte[] data) throws CertifyException {
        byte[] hash = calculateSHA256(data);
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Calculate SHA-256 digest of string and return as Base64 encoded string
     *
     * @param data The string to hash
     * @return Base64 encoded SHA-256 hash
     * @throws CertifyException if hashing fails
     */
    public static String calculateSHA256Base64(String data) throws CertifyException {
        return calculateSHA256Base64(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * Generate cryptographically secure random salt for mDoc
     * Salt length is 24 bytes as per ISO 18013-5 specification
     *
     * @return 24-byte random salt
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        return salt;
    }

    /**
     * Generate cryptographically secure random salt as Base64 string
     *
     * @return Base64 encoded 24-byte random salt
     */
    public static String generateSaltBase64() {
        return Base64.getEncoder().encodeToString(generateSalt());
    }

    /**
     * Generate cryptographically secure random salt with custom length
     *
     * @param length The desired salt length in bytes
     * @return Random salt of specified length
     */
    public static byte[] generateSalt(int length) {
        byte[] salt = new byte[length];
        SECURE_RANDOM.nextBytes(salt);
        return salt;
    }

    /**
     * Calculate digest with salt for mDoc value digest calculation
     * This combines salt + value and calculates SHA-256
     *
     * @param salt The random salt bytes
     * @param value The value to hash
     * @return SHA-256 hash of (salt || value)
     * @throws CertifyException if hashing fails
     */
    public static byte[] calculateValueDigest(byte[] salt, byte[] value) throws CertifyException {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA256_ALGORITHM);
            digest.update(salt);
            digest.update(value);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available: {}", e.getMessage());
            throw new CertifyException("Value digest calculation failed: {}", e.getMessage());
        }
    }

    /**
     * Calculate digest with salt for mDoc value digest calculation
     * This combines salt + CBOR encoded value and calculates SHA-256
     *
     * @param salt The random salt bytes
     * @param value The value to encode and hash
     * @return SHA-256 hash of (salt || CBOR(value))
     * @throws CertifyException if hashing or encoding fails
     */
    public static byte[] calculateValueDigest(byte[] salt, Object value) throws CertifyException {
        byte[] cborValue = CBORUtils.encode(value);
        return calculateValueDigest(salt, cborValue);
    }

    /**
     * Calculate digest with salt and return as Base64 string
     *
     * @param salt The random salt bytes
     * @param value The value to hash
     * @return Base64 encoded SHA-256 hash of (salt || value)
     * @throws CertifyException if hashing fails
     */
    public static String calculateValueDigestBase64(byte[] salt, byte[] value) throws CertifyException {
        byte[] digest = calculateValueDigest(salt, value);
        return Base64.getEncoder().encodeToString(digest);
    }

    /**
     * Generate a cryptographically secure random byte array
     *
     * @param length The desired length in bytes
     * @return Random byte array
     */
    public static byte[] generateSecureRandom(int length) {
        byte[] randomBytes = new byte[length];
        SECURE_RANDOM.nextBytes(randomBytes);
        return randomBytes;
    }

    /**
     * Convert byte array to hex string for debugging
     *
     * @param bytes The bytes to convert
     * @return Hex string representation
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}