package com.justin.eagle.bank.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.justin.eagle.bank.dao.UserRepository;
import com.justin.eagle.bank.dao.model.UserStatusDbInfo;
import com.justin.eagle.bank.generated.openapi.rest.model.UserAuthResponse;
import com.justin.eagle.bank.utl.NowTimeSupplier;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthenticateUserServiceImpl implements AuthenticateUserService {

    private final NowTimeSupplier nowTimeSupplier;
    private final BuildJwtService buildJwtService;
    private final UserRepository userRepository;

    public AuthenticateUserServiceImpl(NowTimeSupplier nowTimeSupplier, BuildJwtService buildJwtService, UserRepository userRepository) {
        this.nowTimeSupplier = nowTimeSupplier;
        this.buildJwtService = buildJwtService;
        this.userRepository = userRepository;
    }

    @Override
    public UserAuthResponse buildNewAuthToken(String userId) {
        final Optional<UserStatusDbInfo> userStatusInfo = userRepository.getUserStatusInfo(userId);

        return userStatusInfo
                .filter(info -> {
                    final boolean equals = "ACTIVE".equals(info.status());
                    if (!equals) {
                        log.error("the user '{}' is not in ACTIVE state", userId);
                    }
                    return equals;
                })
                .map(activeUser -> {
                    log.info("found active user for id '{}", activeUser.userId());
                    return buildJwtService.buildJwt(userId);
                })
                .map(token -> {
                    log.info("successfully build token for userId '{}'",userId);
                    return UserAuthResponse.builder()
                            .token(token)
                            .userId(userId)
                            .type("Bearer")
                            .createdTimestamp(nowTimeSupplier.currentInstant())
                            .build();
                })
                .orElseThrow(() -> {
                    log.error("unable to generate token for user id '{}", userId);
                    return new TokenNotCreatedException();
                });

    }

    @Override
    public String authorizeRequest(@NotNull String userId, @NotNull String bearerToken) {
        try {
            final DecodedJWT decodedToken = getDecodedJWT(bearerToken);
            validateTokenIsNotExpired(decodedToken);

            return Optional.ofNullable(decodedToken.getClaims())
                    .map(map -> map.get("sub"))
                    .map(Claim::asString)
                    .filter(userId::equals)
                    .orElseThrow(getUserNotAuthorizedExceptionSupplier());

        } catch (JWTDecodeException decodeException) {
            log.error("invalid JWT provided", decodeException);
            throw new UserNotAuthorizedException("invalid JWT provided");
        }
    }



    @Override
    public String findUserIdFromAuthToken(String bearerToken) {
        try {
            final DecodedJWT decodedToken = getDecodedJWT(bearerToken);
            validateTokenIsNotExpired(decodedToken);

            return Optional.ofNullable(decodedToken.getClaims())
                    .map(map -> map.get("sub"))
                    .map(Claim::asString)
                    .orElseThrow(getUserNotAuthorizedExceptionSupplier());

        } catch (JWTDecodeException decodeException) {
            log.error("invalid JWT provided", decodeException);
            throw new UserNotAuthorizedException("invalid JWT provided");
        }
    }

    private static DecodedJWT getDecodedJWT(String bearerToken) {
        final String jwtToken = bearerToken.replace("Bearer", "").strip();
        return JWT.decode(jwtToken);
    }

    private void validateTokenIsNotExpired(DecodedJWT decodedToken) {
        final Instant tokeExpiresAt = decodedToken.getExpiresAt().toInstant();
        final Instant now = nowTimeSupplier.currentInstant();
        if (now.isAfter(tokeExpiresAt)) {
            log.error("the provided token is expired at {}. current instant is{}", tokeExpiresAt.getEpochSecond(), now.getEpochSecond());
            throw new UserNotAuthorizedException("token is expired");
        }
    }

    private Supplier<UserNotAuthorizedException> getUserNotAuthorizedExceptionSupplier() {
        return () -> {
            log.error("the sub claim on the token does not match the provided user");
            return new UserNotAuthorizedException("sub claim and user does not match");
        };
    }


}
