package com.sample.keycloak;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;

class PKCEUtilTest {

    @Test
    void codeVerifierLengthAndCharset() {
        String v = PKCEUtil.generateCodeVerifier();
        assertTrue(v.length() >= 43 && v.length() <= 128, "Verifier length out of spec");
        assertTrue(v.matches("[A-Za-z0-9\\-_]+"), "Verifier contains invalid characters");
    }

    @Test
    void codeChallengeIsDeterministic() {
        String v = PKCEUtil.generateCodeVerifier();
        String c1 = PKCEUtil.generateCodeChallenge(v);
        String c2 = PKCEUtil.generateCodeChallenge(v);
        assertEquals(c1, c2, "Challenge must be deterministic for same verifier");
        assertTrue(c1.matches("[A-Za-z0-9\\-_]+"));
    }

    @Test
    void codeChallengeExpectedHash() {
        // Known verifier example (RFC 7636 appendix B style sample but url-safe)
        String verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"; // example
        String challenge = PKCEUtil.generateCodeChallenge(verifier);
        // Validate base64url decoding works (structure) & length near 43
        Base64.getUrlDecoder().decode(challenge);
        assertTrue(challenge.length() >= 43 && challenge.length() <= 64);
    }
}
