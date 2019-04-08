package com.stars.server.login2.task;

import com.stars.server.login2.LoginServer2;
import com.stars.server.login2.model.manager.LChannelManager;
import com.stars.server.login2.model.manager.LSdkManager;
import com.stars.server.login2.model.pojo.LChannel;
import com.stars.server.login2.sdk.core.LSdk;
import com.stars.server.login2.sdk.core.LVerifyContextImpl;
import com.stars.util.LogUtil;
import io.netty.channel.Channel;

/**
 * Created by zhaowenshuo on 2016/2/19.
 */
public class LVerifyTask extends LoginTask {

    private int channelId;
    private String extent;
    private Channel nettyChannel;

    public LVerifyTask(int channelId, String extent, Channel nettyChannel) {
        this.channelId = channelId;
        this.extent = extent;
        this.nettyChannel = nettyChannel;
    }

    @Override
    public void run0() {
        LSdk sdk = LSdkManager.get(channelId);
        LChannel ch = LChannelManager.get(channelId);
        if (sdk != null && ch != null) {
            LVerifyContextImpl ctx = new LVerifyContextImpl(nettyChannel, ch);
            LoginServer2.callbackMap.putIfAbsent(ctx, System.currentTimeMillis());
            sdk.verify(ch, extent, ctx);
        } else {
        	LogUtil.error("找不到对相应渠道: {}", channelId);
        }
    }

}
