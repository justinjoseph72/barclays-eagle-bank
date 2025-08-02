package com.justin.eagle.bank.auth;

import java.util.Optional;

import com.justin.eagle.bank.dao.UserRepository;
import com.justin.eagle.bank.dao.model.UserStatusDbInfo;
import com.justin.eagle.bank.generated.openapi.rest.model.UserAuthResponse;
import com.justin.eagle.bank.utl.NowTimeSupplier;
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
                    if(!equals) {
                        log.error("the user '{}' is not in ACTIVE state", userId);
                    }
                    return equals;
                })
                .map(activeUser -> {
                    log.info("found active user for id '{}", activeUser.userId());
                    return buildJwtService.buildJwt(userId);
                })
                .map(token -> UserAuthResponse.builder()
                        .token(token)
                        .userId(userId)
                        .type("Bearer")
                        .createdTimestamp(nowTimeSupplier.currentInstant())
                        .build())
                .orElseThrow(() -> {
                    log.error("unable to generate token for user id '{}", userId);
                    return new TokenNotCreatedException();
                });

    }
}
