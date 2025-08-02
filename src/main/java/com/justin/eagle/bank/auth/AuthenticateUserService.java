package com.justin.eagle.bank.auth;

import com.justin.eagle.bank.generated.openapi.rest.model.UserAuthResponse;

public interface AuthenticateUserService {

    UserAuthResponse buildNewAuthToken(String userId);

   String authorizeRequest(String userId, String bearerToken);

   String findUserIdFromAuthToken(String bearerToken);
}
