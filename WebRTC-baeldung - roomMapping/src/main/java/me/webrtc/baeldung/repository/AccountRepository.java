package me.webrtc.baeldung.repository;

import me.webrtc.baeldung.model.Account;

public class AccountRepository {
    public Account getUser(String email){
        Account account = new Account();
        account.setEmail(email);
        return account;
    }
}
