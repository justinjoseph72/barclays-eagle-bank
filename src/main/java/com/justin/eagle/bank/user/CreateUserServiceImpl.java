package com.justin.eagle.bank.user;

import com.justin.eagle.bank.dao.UserRepository;
import com.justin.eagle.bank.user.model.NewUser;
import com.justin.eagle.bank.user.model.ProvisionedUser;
import com.justin.eagle.bank.utl.IdSupplier;
import com.justin.eagle.bank.utl.NowTimeSupplier;
import org.springframework.stereotype.Service;

@Service
class CreateUserServiceImpl implements CreateUserService {

private final IdSupplier idSupplier;
private final NowTimeSupplier nowTimeSupplier;
private final UserRepository userRepository;

    CreateUserServiceImpl(IdSupplier idSupplier, NowTimeSupplier nowTimeSupplier, UserRepository userRepository) {
        this.idSupplier = idSupplier;
        this.nowTimeSupplier = nowTimeSupplier;
        this.userRepository = userRepository;
    }

    @Override
    public ProvisionedUser createUser(NewUser request) {
        final ProvisionedUser user = ProvisionedUser.builder()
                .userId(idSupplier.getNewId())
                .externalUserId(idSupplier.getNewUserExternalId())
                .user(request)
                .createdTimestamp(nowTimeSupplier.currentInstant())
                .build();
        userRepository.saveUser(user);
        return user;
    }
}
