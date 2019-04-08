package com.stars.server.proxy.weaknetwork;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

/**
 * Created by Garwah on 2015/10/27.
 */
public class NetProxySession {
    private int sessionId;
    private Channel channel;
    private int delay;


    private boolean autoRead;

    //以下参数从channel拿出来，为了方便取所以写进对象里
    private String remoteIp;
    private int removePort;
    private String localIp;
    private int localPort;

    /* 统计相关 */
    private long readCount;

    public boolean isFix(String remoteIp, String localIp, int removePort, int localPort) {
        if (this.remoteIp.equals(remoteIp)
                && this.localIp.equals(localIp)
                && this.removePort == removePort
                && this.localPort == localPort) {
            return true;
        }
        return false;
    }

    public boolean isFix(int removePort,int localPort){
        if(this.removePort == removePort && this.localPort == localPort){
            return true;
        }
        return false;
    }

    public NetProxySession(int sessionId, Channel channel) {
        this.sessionId = sessionId;
        this.channel = channel;
        initParam(channel);
        this.autoRead = true;
    }

    private void initParam(Channel channel){
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        remoteIp = socketAddress.getAddress().getHostAddress();
        removePort = socketAddress.getPort();
        InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
        localIp = localAddress.getAddress().getHostAddress();
        localPort = localAddress.getPort();
    }


    public String getStr(){
        StringBuilder builder = new StringBuilder();
        builder.append("sessionId:").append(sessionId).append("|")
                .append("delay:").append(delay).append("|")
                .append("autoRead:").append(autoRead).append("|")
                .append("ipParam:").append(remoteIp).append("&").append(removePort).append("&").append(localIp).append("&").append(localPort);
        return builder.toString();
    }

    public Channel getChannel() {
        return channel;
    }

    public int getSessionId() {
        return sessionId;
    }


    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public boolean isAutoRead() {
        return autoRead;
    }

    public void setAutoRead(boolean autoRead) {
        this.autoRead = autoRead;
    }

    public long getReadCount() {
        return readCount;
    }

    public void setReadCount(long readCount) {
        this.readCount = readCount;
    }

    public void increaseReadCount() {
        this.readCount++;
    }

    public void increaseReadCount(long delta) {
        this.readCount = this.readCount + delta;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public int getRemovePort() {
        return removePort;
    }

    public String getLocalIp() {
        return localIp;
    }

    public int getLocalPort() {
        return localPort;
    }
}
