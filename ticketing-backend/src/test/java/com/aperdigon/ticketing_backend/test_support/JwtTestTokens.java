package com.aperdigon.ticketing_backend.test_support;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public final class JwtTestTokens {

    private JwtTestTokens() {}

    public static String token(String subject, List<String> roles, long ttlSeconds) {
        try {
            RSAPrivateKey privateKey = readPkcs8PrivateKeyFromClasspath("/keys/jwt-test-private.pem");

            Instant now = Instant.now();
            Instant exp = now.plusSeconds(ttlSeconds);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .issueTime(java.util.Date.from(now))
                    .expirationTime(java.util.Date.from(exp))
                    .claim("roles", roles) // <- nuestro claim
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT)
                    .build();

            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(new RSASSASigner(privateKey));
            return jwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate test JWT", e);
        }
    }

    private static RSAPrivateKey readPkcs8PrivateKeyFromClasspath(String classpathLocation) throws Exception {
        // classpathLocation debe empezar por "/"
        String pem = new String(
                JwtTestTokens.class.getResourceAsStream(classpathLocation).readAllBytes()
        );

        String base64 = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] der = Base64.getDecoder().decode(base64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
    }
}
