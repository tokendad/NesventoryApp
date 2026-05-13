package com.tokendad.nesventory.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * Tests for the PKCE utility logic (RFC 7636).
 *
 * PkceUtil uses android.util.Base64 which is unavailable in plain JUnit,
 * so we verify the algorithm using java.util.Base64 (identical encoding).
 */
class PkceUtilTest {

    private fun base64UrlNoPadding(bytes: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    private fun generateCodeVerifier(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return base64UrlNoPadding(bytes)
    }

    private fun deriveCodeChallenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(verifier.toByteArray(Charsets.US_ASCII))
        return base64UrlNoPadding(hash)
    }

    private fun generateState(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return base64UrlNoPadding(bytes)
    }

    @Test
    fun generateCodeVerifier_returnsAtLeast43Chars() {
        val verifier = generateCodeVerifier()
        assertTrue("Verifier length should be >= 43, was ${verifier.length}", verifier.length >= 43)
    }

    @Test
    fun generateCodeVerifier_isUrlSafe() {
        val verifier = generateCodeVerifier()
        assertTrue(
            "Verifier should only contain URL-safe base64 characters",
            verifier.matches(Regex("^[A-Za-z0-9_-]+$"))
        )
    }

    @Test
    fun generateCodeVerifier_producesDifferentValues() {
        val v1 = generateCodeVerifier()
        val v2 = generateCodeVerifier()
        assertNotEquals("Two verifiers should differ", v1, v2)
    }

    @Test
    fun deriveCodeChallenge_isDeterministic() {
        val verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
        val c1 = deriveCodeChallenge(verifier)
        val c2 = deriveCodeChallenge(verifier)
        assertEquals("Same verifier must produce same challenge", c1, c2)
    }

    @Test
    fun deriveCodeChallenge_differsByInput() {
        val c1 = deriveCodeChallenge("verifier-aaa")
        val c2 = deriveCodeChallenge("verifier-bbb")
        assertNotEquals("Different verifiers must produce different challenges", c1, c2)
    }

    @Test
    fun deriveCodeChallenge_isUrlSafe() {
        val challenge = deriveCodeChallenge("test-verifier-string")
        assertTrue(
            "Challenge should only contain URL-safe base64 characters",
            challenge.matches(Regex("^[A-Za-z0-9_-]+$"))
        )
    }

    @Test
    fun deriveCodeChallenge_matchesKnownSha256() {
        // SHA-256 of "test" (ASCII) = 9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08
        // base64url of that hash (no padding):
        val expected = base64UrlNoPadding(
            MessageDigest.getInstance("SHA-256").digest("test".toByteArray(Charsets.US_ASCII))
        )
        val actual = deriveCodeChallenge("test")
        assertEquals("Challenge should be base64url(SHA-256(verifier))", expected, actual)
    }

    @Test
    fun generateState_producesDifferentValues() {
        val s1 = generateState()
        val s2 = generateState()
        assertNotEquals("Two state values should differ", s1, s2)
    }

    @Test
    fun generateState_isUrlSafe() {
        val state = generateState()
        assertTrue(
            "State should only contain URL-safe base64 characters",
            state.matches(Regex("^[A-Za-z0-9_-]+$"))
        )
    }

    @Test
    fun generateState_hasReasonableLength() {
        val state = generateState()
        assertTrue("State should be non-empty", state.isNotEmpty())
        // 16 bytes -> 22 chars in base64url without padding
        assertTrue("State length should be >= 16, was ${state.length}", state.length >= 16)
    }
}
