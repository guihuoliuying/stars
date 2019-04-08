package com.stars.server.login2.sdk.core;

import com.stars.server.login2.model.pojo.LChannel;
import com.stars.server.login2.task.LVerifyCallbackTask;
import com.stars.util.ExecuteManager;
import io.netty.channel.Channel;

/**
 * Created by zhaowenshuo on 2016/2/1.
 */
public class LVerifyContextImpl implements LVerifyContext {

    private Channel nettyChannel;
    private com.stars.server.login2.model.pojo.LChannel channel;

    public LVerifyContextImpl(Channel nettyChannel, com.stars.server.login2.model.pojo.LChannel channel) {
        this.nettyChannel = nettyChannel;
        this.channel = channel;
    }

    @Override
    public void onResponse(LSdkVerifyResult result) {

        ExecuteManager.execute(new LVerifyCallbackTask(this, result));

    }

    @Override
    public LChannel channel() {
        return channel;
    }

    @Override
    public Channel nettyChannel() {
        return nettyChannel;
    }
}
