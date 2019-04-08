package com.stars.server.login;

import com.stars.server.login.bean.AccountInfo;
import com.stars.server.login.bean.BlackAccount;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by liuyuheng on 2016/1/5.
 */
public enum LoginDataPool {
    INSTANCE;

    private ConcurrentHashMap<String, Long> macMap = new ConcurrentHashMap();// mac-timestamp
    private ConcurrentHashMap<String, AccountInfo> accountMap = new ConcurrentHashMap<>();// account-AccountInfo
    private HashMap<String, BlackAccount> blackList = new HashMap<>();// account-BlackAccount

    public ConcurrentHashMap<String, Long> getMacMap() {
        return macMap;
    }

    public void putMacMap(String mac, long timestamp){
        macMap.putIfAbsent(mac, timestamp);
    }

    public AccountInfo getAccountInfo(String account) {
        return accountMap.get(account);
    }

    public void putAccountInfo(AccountInfo accountInfo) {
        accountMap.putIfAbsent(accountInfo.getAccount(), accountInfo);
    }

    public void putBlackList(BlackAccount blackAccount) {
        blackList.put(blackAccount.getAccount(), blackAccount);
    }

    public boolean isInBlack(String account) {
        return blackList.get(account) != null ? true : false;
    }

    public boolean isRegister(String account) {
        return accountMap.get(account) != null ? true : false;
    }
}
