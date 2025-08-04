package com.justin.eagle.bank.account;

import java.util.List;

import com.justin.eagle.bank.domain.ActiveAccount;
import com.justin.eagle.bank.domain.PendingAccount;

public interface AccountService {

    ActiveAccount createNewAccount(PendingAccount accountDetails);

    List<ActiveAccount> fetchAllAccountsForUser(String userId);

    ActiveAccount fetchAccountDetails(String accountNumber, String userId);
}
