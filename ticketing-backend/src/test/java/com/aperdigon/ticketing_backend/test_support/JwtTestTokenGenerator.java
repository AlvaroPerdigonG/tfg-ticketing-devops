package com.aperdigon.ticketing_backend.test_support;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public final class JwtTestTokenGenerator {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: JwtTestTokenGenerator src/main/resources/keys/jwt-private.pem");
            System.exit(1);
        }

        Path privateKeyPath = Path.of("src/main/resources/keys/jwt-private.pem"); //args[0]

        // Ejemplo: sub = userId (UUID)
        UUID userId = UUID.randomUUID();
        List<String> roles = List.of("USER"); // cambia a AGENT/ADMIN según quieras

        String token = generate(privateKeyPath, userId.toString(), roles, 60 * 60);
        System.out.println("sub (userId): " + userId);
        System.out.println("roles: " + roles);
        System.out.println("Bearer token:\n" + token);
    }

    public static String generate(Path privateKeyPemPath, String subject, List<String> roles, long ttlSeconds) throws Exception {
        RSAPrivateKey privateKey = readPkcs8PrivateKey(privateKeyPemPath);

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlSeconds);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issueTime(java.util.Date.from(now))
                .expirationTime(java.util.Date.from(exp))
                .claim("roles", roles)
                .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .build();

        SignedJWT jwt = new SignedJWT(header, claims);
        jwt.sign(new RSASSASigner(privateKey));

        return jwt.serialize();
    }

    private static RSAPrivateKey readPkcs8PrivateKey(Path pemPath) throws Exception {
        String pem = Files.readString(pemPath);
        String base64 = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] der = Base64.getDecoder().decode(base64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
    }
}
