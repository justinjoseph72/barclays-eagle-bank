package com.justin.eagle.bank.user;

import com.justin.eagle.bank.generated.openapi.rest.model.CreateUserRequest;
import com.justin.eagle.bank.user.model.NewUser;
import com.justin.eagle.bank.user.model.ProvisionedUser;

public interface CreateUserService {

    ProvisionedUser createUser(NewUser request);
}
