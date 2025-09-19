package com.gym.member_service.Util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
/**
 * Utility component for generating unique and deterministic session identifiers.
 *
 * <p>This utility provides secure session ID generation for training sessions
 * using cryptographic hashing algorithms. The generated IDs are:
 * <ul>
 *   <li>Deterministic - same inputs always produce the same session ID</li>
 *   <li>Unique - collision probability is extremely low due to SHA-256 hashing</li>
 *   <li>URL-safe - uses Base64 URL encoding for web compatibility</li>
 *   <li>Compact - 16-character length for efficient storage and transmission</li>
 * </ul>
 *
 * <p>The utility incorporates multiple session parameters (member ID, trainer ID,
 * and timing information) to ensure uniqueness across different training sessions
 * while maintaining deterministic behavior for identical session parameters.
 *
 * <p>The implementation includes robust error handling with UUID-based fallback
 * generation to ensure system reliability even in edge cases where SHA-256
 * hashing is unavailable.
 *
 * <p>Session IDs are generated using the following algorithm:
 * <ol>
 *   <li>Concatenate input parameters with colon separators</li>
 *   <li>Apply SHA-256 cryptographic hash function</li>
 *   <li>Encode result using Base64 URL encoding (without padding)</li>
 *   <li>Truncate to first 16 characters for optimal length</li>
 * </ol>
 *
 * @author Arpan Das
 * @version 1.0
 * @since 1.0
 */
@Component
public class SessionIdGenUtil {

    /**
     * Generates a unique and deterministic session identifier for training sessions.
     *
     * <p>This method creates a secure session ID by combining member information,
     * trainer details, and session timing data through SHA-256 cryptographic hashing.
     * The resulting ID is deterministic (same inputs always produce identical outputs)
     * while maintaining extremely low collision probability.
     *
     * <p>The generation process involves:
     * <ul>
     *   <li>Creating a composite input string from all session parameters</li>
     *   <li>Applying SHA-256 hashing for cryptographic security</li>
     *   <li>Base64 URL encoding for web-safe character representation</li>
     *   <li>Truncating to 16 characters for optimal storage efficiency</li>
     * </ul>
     *
     * <p>The method includes robust error handling with a UUID-based fallback
     * mechanism to ensure reliable ID generation even if SHA-256 hashing becomes
     * unavailable (which is extremely unlikely in modern Java environments).
     *
     * <p><strong>Input Format:</strong> {@code memberId:trainerId:startDateTime:endDateTime}
     * <p><strong>Output Format:</strong> 16-character URL-safe Base64 string
     *
     * <p><strong>Example:</strong>
     * <pre>
     * String sessionId = generateSessionId(
     *     "member123",
     *     "trainer456",
     *     LocalDateTime.of(2023, 10, 15, 14, 30),
     *     LocalDateTime.of(2023, 10, 15, 15, 30)
     * );
     * // Returns: "A1B2C3D4E5F6G7H8"
     * </pre>
     *
     * @param memberId the unique identifier of the member participating in the session.
     *                 Must not be null or empty. Used to ensure session uniqueness
     *                 across different members.
     * @param trainerId the unique identifier of the trainer conducting the session.
     *                  Must not be null or empty. Used to differentiate sessions
     *                  with different trainers.
     * @param startDate the scheduled start date and time of the training session.
     *                  Must not be null. Used to ensure temporal uniqueness of
     *                  session identifiers.
     * @param endDate the scheduled end date and time of the training session.
     *                Must not be null and should be after startDate. Used to
     *                incorporate session duration into ID generation.
     * @return String containing a 16-character unique session identifier that is
     *         URL-safe and deterministic for the given input parameters
     * @throws RuntimeException if UUID fallback generation fails (extremely rare)
     *
     * @see MessageDigest
     * @see Base64
     * @see UUID
     */
    public String generateSessionId(String memberId, String trainerId, LocalDateTime startDate, LocalDateTime endDate) {
        String input = memberId + ":" + trainerId + ":" + startDate.toString() + ":" + endDate.toString();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            return encoded.substring(0, 16);
        } catch (
                NoSuchAlgorithmException e) {
            // Fallback in the very unlikely case SHA-256 is unavailable
            return UUID.randomUUID().toString().replace("-", "").substring(0, 16); }
        }
}
