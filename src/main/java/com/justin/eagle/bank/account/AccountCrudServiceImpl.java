package com.justin.eagle.bank.account;

import java.util.UUID;

import com.justin.eagle.bank.domain.ActiveAccount;
import com.justin.eagle.bank.domain.PendingAccount;
import com.justin.eagle.bank.dao.UserRepository;
import com.justin.eagle.bank.dao.model.UserStatusDbInfo;
import com.justin.eagle.bank.rest.controller.UserNotFoundException;
import com.justin.eagle.bank.utl.IdSupplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
class AccountCrudServiceImpl implements AccountCrudService{

    private final UserRepository userRepository;
    private final IdSupplier idSupplier;

    AccountCrudServiceImpl(UserRepository userRepository, IdSupplier idSupplier) {
        this.userRepository = userRepository;
        this.idSupplier = idSupplier;
    }

    @Override
    public ActiveAccount createNewAccount(PendingAccount accountDetails, String userId) {
        final UUID partyId = userRepository.getUserStatusInfo(userId)
                .filter(user -> {
                    final boolean isActive = "ACTIVE".equals(user.status());
                    if (!isActive) {
                        log.warn("the user with id '{}' status is {}", user.userId(), user.status());
                    }
                    return isActive;
                })
                .map(UserStatusDbInfo::partyId)
                .orElseThrow(() -> {
                    log.warn("user with id '{}' not found", userId);
                    return new UserNotFoundException();
                });
//        ActiveAccount.builder().id(idSupplier.getNewId())
//                .
        return null;
    }
}
