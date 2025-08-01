package com.justin.eagle.bank.auth;

import com.justin.eagle.bank.generated.openapi.rest.model.UserAuthResponse;
import com.justin.eagle.bank.utl.NowTimeSupplier;
import org.springframework.stereotype.Service;

@Service
public class AuthenticateUserServiceImpl implements AuthenticateUserService {

    private final NowTimeSupplier nowTimeSupplier;
    private final BuildJwtService buildJwtService;

    public AuthenticateUserServiceImpl(NowTimeSupplier nowTimeSupplier, BuildJwtService buildJwtService) {
        this.nowTimeSupplier = nowTimeSupplier;
        this.buildJwtService = buildJwtService;
    }

    @Override
    public UserAuthResponse buildNewAuthToken(String userId) {

        //TODO fetch the user from the db and verify the user is in valid state.

        final String token = buildJwtService.buildJwt(userId);
        return UserAuthResponse.builder()
                .token(token)
                .userId(userId)
                .type("Bearer")
                .createdTimestamp(nowTimeSupplier.currentInstant())
                .build();
    }
}
