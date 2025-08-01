package com.justin.eagle.bank.auth;

import com.justin.eagle.bank.generated.openapi.rest.model.UserAuthResponse;

public interface AuthenticateUserService {

    UserAuthResponse buildNewAuthToken(String userId);
}
