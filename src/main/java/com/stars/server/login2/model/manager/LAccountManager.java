package com.stars.server.login2.model.manager;

import com.stars.server.login2.model.pojo.LAccount;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhaowenshuo on 2016/2/1.
 */
public class LAccountManager {

    private static ConcurrentMap<String, LAccount> accountMap = new ConcurrentHashMap<>();

    public static LAccount get(String accoutName) {
        return accountMap.get(accoutName);
    }

    public static LAccount putIfAbsent(String accountName, LAccount account) {
        return accountMap.putIfAbsent(accountName, account);
    }

    public static void putAll(Map<String, LAccount> accounts) {
        accountMap.putAll(accounts);
    }

    public static LAccount remove(String accountName) {
        return accountMap.remove(accountName);
    }

    public static Collection<LAccount> valueSet() {
        return accountMap.values();
    }

}
