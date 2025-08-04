package com.justin.eagle.bank.user;

import java.time.Instant;
import java.util.Optional;

import com.justin.eagle.bank.dao.UserRepository;
import com.justin.eagle.bank.domain.AuditData;
import com.justin.eagle.bank.domain.UserIdentifier;
import com.justin.eagle.bank.domain.UserInfo;
import com.justin.eagle.bank.domain.ProvisionedUser;
import com.justin.eagle.bank.utl.IdSupplier;
import com.justin.eagle.bank.utl.NowTimeSupplier;
import org.springframework.stereotype.Service;

@Service
class UserServiceImpl implements UserService {

    private final IdSupplier idSupplier;
    private final NowTimeSupplier nowTimeSupplier;
    private final UserRepository userRepository;

    UserServiceImpl(IdSupplier idSupplier, NowTimeSupplier nowTimeSupplier, UserRepository userRepository) {
        this.idSupplier = idSupplier;
        this.nowTimeSupplier = nowTimeSupplier;
        this.userRepository = userRepository;
    }

    @Override
    public ProvisionedUser createUser(UserInfo request) {
        final Instant createdTimestamp = nowTimeSupplier.currentInstant();
        final ProvisionedUser user = ProvisionedUser.builder()
                .identifier(UserIdentifier.builder()
                        .partyId(idSupplier.getNewId())
                        .externalUserId(idSupplier.getNewUserExternalId())
                        .build())
                .info(request)
                .auditData(AuditData.builder()
                        .createdTimestamp(createdTimestamp)
                        .lastUpdatedTimestamp(createdTimestamp)
                        .build())
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
