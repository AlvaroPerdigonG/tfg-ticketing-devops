package com.aperdigon.ticketing_backend.infrastructure.security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public class DevJwtGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevJwtGenerator.class);

    public static void main(String[] args) throws Exception {

        if (args.length < 3) {
            LOGGER.info("Usage: DevJwtGenerator <privateKeyPath> <subUuid> <role>");
            return;
        }

        String privateKeyPath = args[0];
        String sub = args[1];
        String role = args[2];

        String token = generate(privateKeyPath, sub, List.of(role));

        LOGGER.info("sub = {}", sub);
        LOGGER.info("role = {}", role);
        LOGGER.info("TOKEN:");
        LOGGER.info("{}", token);
    }

    public static String generate(String privateKeyPath, String subject, List<String> roles) throws Exception {

        RSAPrivateKey privateKey = loadPrivateKey(privateKeyPath);

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(3600);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .claim("roles", roles)
                .issueTime(java.util.Date.from(now))
                .expirationTime(java.util.Date.from(exp))
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .type(JOSEObjectType.JWT)
                        .build(),
                claims
        );

        signedJWT.sign(new RSASSASigner(privateKey));

        return signedJWT.serialize();
    }

    private static RSAPrivateKey loadPrivateKey(String path) throws Exception {
        String pem = Files.readString(Path.of(path));
        pem = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] der = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);

        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
    }
}
