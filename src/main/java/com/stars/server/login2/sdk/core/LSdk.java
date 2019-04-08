package com.stars.server.login2.sdk.core;

import com.stars.server.login2.model.pojo.LChannel;

/**
 * Created by zhaowenshuo on 2016/1/29.
 */
public interface LSdk {

    void verify(LChannel ch, String extent, LVerifyContext callback);

}
