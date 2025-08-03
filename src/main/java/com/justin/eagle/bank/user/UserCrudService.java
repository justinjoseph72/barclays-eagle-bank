package com.justin.eagle.bank.user;

import java.util.Optional;

import com.justin.eagle.bank.domain.UserInfo;
import com.justin.eagle.bank.domain.ProvisionedUser;

public interface UserCrudService {

    ProvisionedUser createUser(UserInfo request);

    Optional<ProvisionedUser> fetchUser(String userId);
}
