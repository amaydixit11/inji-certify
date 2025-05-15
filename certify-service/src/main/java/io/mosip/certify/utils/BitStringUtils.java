package io.mosip.certify.utils;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Utility class for handling bit string operations related to Status List Credentials.
 * Provides functionality for encoding/decoding bit strings and performing bit-level operations.
 */
public class BitStringUtils {

    private static final int BUFFER_SIZE = 4096;

    /**
     * Decode a base64url-encoded string to a bit string (byte array)
     *
     * @param encodedList Base64Url encoded and compressed bit string
     * @return Decoded byte array representing the bit string
     */
    public static byte[] decodeBase64UrlToBitString(String encodedList) {
        // Decode from base64url
        byte[] compressedData = Base64.getUrlDecoder().decode(encodedList);

        // Decompress the data
        try {
            Inflater inflater = new Inflater();
            inflater.setInput(compressedData);

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer.array(), buffer.position(), buffer.remaining());
                buffer.position(buffer.position() + count);

                if (buffer.remaining() == 0) {
                    // Need more space
                    ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                    buffer.flip();
                    newBuffer.put(buffer);
                    buffer = newBuffer;
                }
            }

            inflater.end();
            buffer.flip();

            byte[] result = new byte[buffer.remaining()];
            buffer.get(result);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to decompress bit string", e);
        }
    }

    /**
     * Encode a bit string (byte array) to a base64url-encoded string
     *
     * @param bitString Byte array representing the bit string
     * @return Base64Url encoded and compressed bit string
     */
    public static String encodeBitStringToBase64Url(byte[] bitString) {
        // Compress the data
        try {
            Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
            deflater.setInput(bitString);
            deflater.finish();

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer.array(), buffer.position(), buffer.remaining());
                buffer.position(buffer.position() + count);

                if (buffer.remaining() == 0) {
                    // Need more space
                    ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                    buffer.flip();
                    newBuffer.put(buffer);
                    buffer = newBuffer;
                }
            }

            deflater.end();
            buffer.flip();

            byte[] compressedData = new byte[buffer.remaining()];
            buffer.get(compressedData);

            // Encode to base64url
            return Base64.getUrlEncoder().withoutPadding().encodeToString(compressedData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compress bit string", e);
        }
    }

    /**
     * Set a specific bit in a bit string (byte array) to the specified value
     *
     * @param bitString Byte array representing the bit string
     * @param index The zero-based index of the bit to set
     * @param value The boolean value to set (true = 1, false = 0)
     */
    public static void setBit(byte[] bitString, int index, boolean value) {
        if (index < 0 || index >= (bitString.length * 8)) {
            throw new IndexOutOfBoundsException("Bit index out of bounds: " + index);
        }

        int byteIndex = index / 8;
        int bitPosition = index % 8;

        if (value) {
            // Set bit to 1
            bitString[byteIndex] |= (1 << (7 - bitPosition));
        } else {
            // Set bit to 0
            bitString[byteIndex] &= ~(1 << (7 - bitPosition));
        }
    }

    /**
     * Get the value of a specific bit in a bit string (byte array)
     *
     * @param bitString Byte array representing the bit string
     * @param index The zero-based index of the bit to get
     * @return The boolean value of the bit (true = 1, false = 0)
     */
    public static boolean getBit(byte[] bitString, int index) {
        if (index < 0 || index >= (bitString.length * 8)) {
            throw new IndexOutOfBoundsException("Bit index out of bounds: " + index);
        }

        int byteIndex = index / 8;
        int bitPosition = index % 8;

        return (bitString[byteIndex] & (1 << (7 - bitPosition))) != 0;
    }
}