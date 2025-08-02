package com.justin.eagle.bank.user;

import java.time.Instant;
import java.util.Optional;

import com.justin.eagle.bank.dao.UserRepository;
import com.justin.eagle.bank.user.model.NewUser;
import com.justin.eagle.bank.user.model.ProvisionedUser;
import com.justin.eagle.bank.utl.IdSupplier;
import com.justin.eagle.bank.utl.NowTimeSupplier;
import org.springframework.stereotype.Service;

@Service
class UserCrudServiceImpl implements UserCrudService {

private final IdSupplier idSupplier;
private final NowTimeSupplier nowTimeSupplier;
private final UserRepository userRepository;

    UserCrudServiceImpl(IdSupplier idSupplier, NowTimeSupplier nowTimeSupplier, UserRepository userRepository) {
        this.idSupplier = idSupplier;
        this.nowTimeSupplier = nowTimeSupplier;
        this.userRepository = userRepository;
    }

    @Override
    public ProvisionedUser createUser(NewUser request) {
        final Instant createdTimestamp = nowTimeSupplier.currentInstant();
        final ProvisionedUser user = ProvisionedUser.builder()
                .userId(idSupplier.getNewId())
                .externalUserId(idSupplier.getNewUserExternalId())
                .user(request)
                .createdTimestamp(createdTimestamp)
                .updatedTimestamp(createdTimestamp)
                .build();
        userRepository.saveUser(user);
        return user;
    }

    //TODO check if the auth is correct
    @Override
    public Optional<ProvisionedUser> fetchUser(String userId) {
        return userRepository.fetchLatestUserDetails(userId);

    }
}
