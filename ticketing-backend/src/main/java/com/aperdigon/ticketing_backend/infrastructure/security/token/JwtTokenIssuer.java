package com.aperdigon.ticketing_backend.infrastructure.security.token;

import com.aperdigon.ticketing_backend.application.auth.login.TokenIssuer;
import com.aperdigon.ticketing_backend.domain.user.User;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenIssuer implements TokenIssuer {

    private final RSAPrivateKey privateKey;
    private final Clock clock;
    private final long expirationSeconds;

    public JwtTokenIssuer(
            @Value("${app.security.jwt.private-key-location:classpath:keys/jwt-private.pem}") Resource privateKeyResource,
            @Value("${app.security.jwt.expiration-seconds:3600}") long expirationSeconds,
            Clock clock
    ) {
        this.privateKey = loadPrivateKey(privateKeyResource);
        this.expirationSeconds = expirationSeconds;
        this.clock = clock;
    }

    @Override
    public String issue(User user) {
        try {
            Instant now = clock.instant();
            Instant exp = now.plusSeconds(expirationSeconds);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.id().value().toString())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(exp))
                    .claim("roles", List.of(user.role().name()))
                    .build();

            SignedJWT jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType.JWT).build(),
                    claims
            );
            jwt.sign(new RSASSASigner(privateKey));
            return jwt.serialize();
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot sign JWT", ex);
        }
    }

    private RSAPrivateKey loadPrivateKey(Resource resource) {
        try (var input = resource.getInputStream()) {
            String pem = new String(input.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] der = Base64.getDecoder().decode(pem);
            var spec = new PKCS8EncodedKeySpec(der);
            return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot load RSA private key", ex);
        }
    }
}
