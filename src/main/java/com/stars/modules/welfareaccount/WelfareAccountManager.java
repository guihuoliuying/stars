package com.stars.modules.welfareaccount;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by huwenjun on 2017/4/11.
 */
public class WelfareAccountManager {
    public static final int VIRTUAL_MONEY_ITEM_ID = 58;//虚拟币itemid
    /**
     * 福利账号集
     */
    public static Set<String> welfareAccountSet = new HashSet<>();

    /**
     * 判断账号是否是福利账号
     *
     * @param account
     * @return
     */
    public static boolean isWelfareAccount(String account) {
        return welfareAccountSet.contains(account);
    }
}
