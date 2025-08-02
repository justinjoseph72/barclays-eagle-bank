package com.justin.eagle.bank.account;

import com.justin.eagle.bank.domain.ActiveAccount;
import com.justin.eagle.bank.domain.PendingAccount;

public interface AccountCrudService {

    ActiveAccount createNewAccount(PendingAccount accountDetails);
}
