package com.justin.eagle.bank.user;

import java.util.Optional;

import com.justin.eagle.bank.user.model.NewUser;
import com.justin.eagle.bank.user.model.ProvisionedUser;

public interface UserCrudService {

    ProvisionedUser createUser(NewUser request);

    Optional<ProvisionedUser> fetchUser(String userId);
}
