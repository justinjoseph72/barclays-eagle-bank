package com.justin.eagle.bank.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import com.justin.eagle.bank.utl.NowTimeSupplier;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class BuildJwtService {

    private final NowTimeSupplier nowTimeSupplier;
    private final long authExpirySec;

    BuildJwtService(NowTimeSupplier nowTimeSupplier,
            @Value("${app.config.auth-expiry-seconds}")
            long authExpirySec) {
        this.nowTimeSupplier = nowTimeSupplier;
        this.authExpirySec = authExpirySec;
    }

    String buildJwt(String userId) {
        Long createdAt = nowTimeSupplier.currentEpochSec();
        Long expireAt = createdAt + authExpirySec;
        log.info(" created at {} and expiring at {}", createdAt, expireAt);
        final SignatureAlgorithm securityAlo = SignatureAlgorithm.HS512;
        final byte[] keyBytes = Base64.getEncoder().encode("someKeyTobeInjected".repeat(100).getBytes(StandardCharsets.UTF_8));
        final SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, securityAlo.getJcaName());
        final JwtBuilder jwtBuilder = Jwts.builder();
        return jwtBuilder.setHeaderParam("typ", "JWT")
                .setIssuer("eagle-bank-api")
                .setClaims(Map.of(
                        "sub", userId,
                        "iat", createdAt,
                        "exp", expireAt
                ))
                .signWith(secretKeySpec, securityAlo)
                .compact();
    }
}
