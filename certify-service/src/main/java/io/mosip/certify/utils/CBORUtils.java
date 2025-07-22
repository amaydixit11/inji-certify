/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.certify.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;
import io.mosip.certify.core.exception.CertifyException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Utility class for CBOR (Concise Binary Object Representation) encoding and decoding operations.
 * Used for mDoc (Mobile Document) credential format processing.
 */
@Slf4j
public class CBORUtils {

    private static final ObjectMapper CBOR_MAPPER = new ObjectMapper(new CBORFactory());

    static {
        // Configure CBOR mapper for mDoc requirements
        CBORFactory cborFactory = (CBORFactory) CBOR_MAPPER.getFactory();
        cborFactory.configure(CBORGenerator.Feature.WRITE_MINIMAL_INTS, true);
        // Removed ALLOW_MISSING_VALUES as it doesn't exist in CBORParser.Feature
    }

    /**
     * Encode an object to CBOR bytes
     *
     * @param object The object to encode
     * @return CBOR encoded bytes
     * @throws CertifyException if encoding fails
     */
    public static byte[] encode(Object object) throws CertifyException {
        try {
            return CBOR_MAPPER.writeValueAsBytes(object);
        } catch (IOException e) {
            log.error("Failed to encode object to CBOR: {}", e.getMessage());
            throw new CertifyException("CBOR encoding failed: " + e.getMessage());
        }
    }

    /**
     * Decode CBOR bytes to a Map
     *
     * @param cborBytes The CBOR bytes to decode
     * @return Decoded Map object
     * @throws CertifyException if decoding fails
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> decodeToMap(byte[] cborBytes) throws CertifyException {
        try {
            return CBOR_MAPPER.readValue(cborBytes, Map.class);
        } catch (IOException e) {
            log.error("Failed to decode CBOR bytes to Map: {}", e.getMessage());
            throw new CertifyException("CBOR decoding failed: " + e.getMessage());
        }
    }

    /**
     * Decode CBOR bytes to a specific class type
     *
     * @param cborBytes The CBOR bytes to decode
     * @param clazz The target class type
     * @param <T> The type parameter
     * @return Decoded object of type T
     * @throws CertifyException if decoding fails
     */
    public static <T> T decode(byte[] cborBytes, Class<T> clazz) throws CertifyException {
        try {
            return CBOR_MAPPER.readValue(cborBytes, clazz);
        } catch (IOException e) {
            log.error("Failed to decode CBOR bytes to {}: {}", clazz.getSimpleName(), e.getMessage());
            throw new CertifyException("CBOR decoding failed: " + e.getMessage());
        }
    }

    /**
     * Encode a Map to CBOR bytes with proper ordering for mDoc
     *
     * @param map The map to encode
     * @return CBOR encoded bytes
     * @throws CertifyException if encoding fails
     */
    public static byte[] encodeMap(Map<String, Object> map) throws CertifyException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CBORGenerator generator = (CBORGenerator) CBOR_MAPPER.getFactory().createGenerator(outputStream);

            // Write map with deterministic ordering for mDoc compliance
            generator.writeStartObject();

            // Sort keys for deterministic output
            map.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        try {
                            generator.writeFieldName(entry.getKey());
                            generator.writeObject(entry.getValue());
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to write CBOR field", e);
                        }
                    });

            generator.writeEndObject();
            generator.close();

            return outputStream.toByteArray();
        } catch (IOException | RuntimeException e) {
            log.error("Failed to encode Map to CBOR: {}", e.getMessage());
            throw new CertifyException("CBOR Map encoding failed: " + e.getMessage());
        }
    }

    /**
     * Create a CBOR-encoded tagged value
     *
     * @param tag The CBOR tag number
     * @param value The value to tag
     * @return CBOR encoded tagged value
     * @throws CertifyException if encoding fails
     */
    public static byte[] encodeTaggedValue(int tag, Object value) throws CertifyException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CBORGenerator generator = (CBORGenerator) CBOR_MAPPER.getFactory().createGenerator(outputStream);

            generator.writeTag(tag);
            generator.writeObject(value);
            generator.close();

            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Failed to encode tagged value to CBOR: {}", e.getMessage());
            throw new CertifyException("CBOR tagged value encoding failed: " + e.getMessage());
        }
    }

    /**
     * Validate if bytes are valid CBOR
     *
     * @param cborBytes The bytes to validate
     * @return true if valid CBOR, false otherwise
     */
    public static boolean isValidCBOR(byte[] cborBytes) {
        try {
            CBORParser parser = (CBORParser) CBOR_MAPPER.getFactory().createParser(new ByteArrayInputStream(cborBytes));
            parser.nextToken();
            parser.close();
            return true;
        } catch (IOException e) {
            log.debug("Invalid CBOR bytes: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get the CBOR ObjectMapper instance
     *
     * @return The configured CBOR ObjectMapper
     */
    public static ObjectMapper getCBORMapper() {
        return CBOR_MAPPER;
    }
}