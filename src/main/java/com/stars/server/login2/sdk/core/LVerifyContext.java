package com.stars.server.login2.sdk.core;

import com.stars.server.login2.model.pojo.LChannel;
import io.netty.channel.Channel;

/**
 * Created by zhaowenshuo on 2016/1/29.
 */
public interface LVerifyContext {

    void onResponse(LSdkVerifyResult result);

    Channel nettyChannel();

    LChannel channel();

}
