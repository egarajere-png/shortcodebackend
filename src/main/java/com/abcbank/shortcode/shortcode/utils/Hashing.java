package com.abcbank.shortcode.shortcode.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Service;

/**
 * Cryptographic hashing utility service for the Short Code application.
 * 
 * This service provides SHA-256 hashing functionality used for:
 * - Data integrity validation: Computing cryptographic hashes of short code records
 * - Tamper detection: Comparing stored hashes with newly computed hashes to ensure
 *   that short code data has not been altered
 * 
 * The hashing algorithm is deterministic and produces consistent results for the same input,
 * enabling integrity verification at any point in the short code lifecycle.
 * 
 * Important: This utility does NOT encrypt sensitive data but rather creates checksums
 * for integrity verification purposes. Encryption is handled separately for data at rest.
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@Service
public class Hashing {

    /**
     * Generates a SHA-256 hash of the provided input string.
     * 
     * This method is used to create cryptographic hashes of short code records for integrity
     * validation. It uses UTF-8 character encoding and converts the resulting hash bytes to
     * a hexadecimal string representation.
     * 
     * Security Considerations:
     * - SHA-256 is a cryptographically secure hashing algorithm approved by NIST
     * - The hash is deterministic: the same input always produces the same output
     * - The operation is one-way: it is computationally infeasible to reverse the hash
     * - Used in conjunction with the Maker-Checker workflow to detect unauthorized modifications
     * 
     * @param originalString the input string to be hashed
     * @return the SHA-256 hash as a hexadecimal string, or null if the algorithm is unavailable
     *         (this should not occur in normal operation)
     * @throws No exceptions are thrown; errors are logged and null is returned
     */
    public String hash256(String originalString) {
        MessageDigest digest;
        try {
            // Initialize SHA-256 message digest algorithm
            digest = MessageDigest.getInstance("SHA-256");
            // Compute the hash of the input string using UTF-8 encoding
            byte[] encodedhash = digest.digest(
                    originalString.getBytes(StandardCharsets.UTF_8));
            // Convert the byte array to hexadecimal representation
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 should always be available in Java runtime
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts a byte array to its hexadecimal string representation.
     * 
     * This is a utility method used internally to format the output of the SHA-256
     * hash digest. Each byte is converted to a two-character hexadecimal string,
     * with leading zeros preserved.
     * 
     * @param hash the byte array containing the hash digest
     * @return the hexadecimal string representation of the hash (64 characters for SHA-256)
     */
    private String bytesToHex(byte[] hash) {
        // Pre-allocate StringBuilder with appropriate capacity for efficiency
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        // Iterate through each byte in the hash array
        for (int i = 0; i < hash.length; i++) {
            // Convert byte to unsigned hexadecimal value
            String hex = Integer.toHexString(0xff & hash[i]);
            // Ensure two-character representation with leading zero if necessary
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}