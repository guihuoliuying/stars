package com.stars.server.login2.sdk.impl;

import com.google.gson.Gson;
import com.stars.server.login2.model.pojo.LChannel;
import com.stars.server.login2.sdk.core.LSdk;
import com.stars.server.login2.sdk.core.LSdkVerifyResult;
import com.stars.server.login2.sdk.core.LVerifyContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/2/1.
 */
public class TestSdk implements LSdk {

    private static Gson gson = new Gson();

    @Override
    public void verify(LChannel ch, String extent, LVerifyContext callback) {
        Map map = gson.fromJson(extent, HashMap.class);
        String userId = (String) map.get("account");
        String password = (String) map.get("password");
        if (userId != null && !userId.trim().equals("") && password != null && password.equals("12345")) {
            callback.onResponse(new LSdkVerifyResult(true, userId, "", ""));
        } else {
            callback.onResponse(new LSdkVerifyResult(false, "login_err_password"));
        }
    }

}
