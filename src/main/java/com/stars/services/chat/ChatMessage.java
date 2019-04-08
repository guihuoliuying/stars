package com.stars.services.chat;

import com.stars.multiserver.MultiServerHelper;
import com.stars.network.server.buffer.NewByteBuffer;
import io.netty.util.CharsetUtil;

import java.nio.ByteBuffer;


/**
 * @author dengzhou
 */
public class ChatMessage {
    /**
     * 频道
     */
    private byte channel;
    /**
     * 信息发送者
     */
    private long senderId;

    /**
     * 信息发送者姓名
     */
    private String senderName;

    private int senderVipLv;

    private String account;
    /**
     * 接收者
     */
    private long receiver;
    /**
     * 信息内容
     */
    private String content;

    private short senderLevel;

    private byte senderJob;

    private int serverId;
    private int campType;
    private int commonOfficerId;//普通官职
    private int rareOfficerId;//稀有官职
    private int designateOfficerId;//任命官职
    private boolean containsObject = false;

    /**
     * 服务接到消息的时间戳
     */
    private long timestamp;

    public ChatMessage() {

    }

    public void writeToBuffer(ByteBuffer buffer) {
        buffer.put(channel);
        byte[] data = String.valueOf(senderId).getBytes(CharsetUtil.UTF_8);
        buffer.putShort((short) data.length);
        buffer.put(data);
        buffer.putShort(senderLevel);
        buffer.put(senderJob);
        data = senderName.getBytes(CharsetUtil.UTF_8);
        buffer.putShort((short) data.length);
        buffer.put(data);
        buffer.putInt(senderVipLv);
        data = account == null ? "".getBytes(CharsetUtil.UTF_8) : account.getBytes(CharsetUtil.UTF_8);
        buffer.putShort((short) data.length);
        buffer.put(data);
        data = String.valueOf(receiver).getBytes(CharsetUtil.UTF_8);
        buffer.putShort((short) data.length);
        buffer.put(data);
        data = content.getBytes(CharsetUtil.UTF_8);
        buffer.putShort((short) data.length);
        buffer.put(data);
        if (channel == ChatManager.CHANNEL_RM) {
            buffer.putInt(MultiServerHelper.getDisplayServerId(serverId));
        }
        if (channel == ChatManager.CHANNEL_RM_CAMP) {
            buffer.putInt(MultiServerHelper.getDisplayServerId(serverId));
            buffer.putInt(campType);//阵营类型
            buffer.putInt(commonOfficerId);//普通官职
            buffer.putInt(rareOfficerId);//稀有官职
            buffer.putInt(designateOfficerId);//任命官职
        }
        data = Long.toString(timestamp).getBytes(CharsetUtil.UTF_8);
        buffer.putShort((short) data.length);
        buffer.put(data);

    }

    public void writeToBuffer(NewByteBuffer buffer) {
        buffer.writeByte(channel);
        buffer.writeString(String.valueOf(senderId));
        buffer.writeShort(senderLevel);
        buffer.writeByte(senderJob);
        buffer.writeString(senderName);
        buffer.writeInt(senderVipLv);
        buffer.writeString(account);
        buffer.writeString(String.valueOf(receiver));
        buffer.writeString(content);
        if (channel == ChatManager.CHANNEL_RM) {
            buffer.writeInt(MultiServerHelper.getDisplayServerId(serverId));
        }
        if (channel == ChatManager.CHANNEL_RM_CAMP) {
            buffer.writeInt(MultiServerHelper.getDisplayServerId(serverId));
            buffer.writeInt(campType);
            buffer.writeInt(commonOfficerId);//普通官职
            buffer.writeInt(rareOfficerId);//稀有官职
            buffer.writeInt(designateOfficerId);//任命官职
        }
        buffer.writeString(Long.toString(timestamp));
    }

    public byte getChannel() {
        return channel;
    }

    public void setChannel(byte channel) {
        this.channel = channel;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long sender) {
        this.senderId = sender;
    }

    public long getReceiver() {
        return receiver;
    }

    public void setReceiver(long receiver) {
        this.receiver = receiver;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public short getSenderLevel() {
        return senderLevel;
    }

    public void setSenderLevel(short senderLevel) {
        this.senderLevel = senderLevel;
    }

    public short getSenderJob() {
        return senderJob;
    }

    public void setSenderJob(byte senderJob) {
        this.senderJob = senderJob;
    }

    public boolean containsObject() {
        return containsObject;
    }

    public void setContainsObject(boolean containsObject) {
        this.containsObject = containsObject;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getSenderVipLv() {
        return senderVipLv;
    }

    public void setSenderVipLv(int senderVipLv) {
        this.senderVipLv = senderVipLv;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getCampType() {
        return campType;
    }

    public void setCampType(int campType) {
        this.campType = campType;
    }

    public int getRareOfficerId() {
        return rareOfficerId;
    }

    public void setRareOfficerId(int rareOfficerId) {
        this.rareOfficerId = rareOfficerId;
    }

    public int getCommonOfficerId() {
        return commonOfficerId;
    }

    public void setCommonOfficerId(int commonOfficerId) {
        this.commonOfficerId = commonOfficerId;
    }

    public int getDesignateOfficerId() {
        return designateOfficerId;
    }

    public void setDesignateOfficerId(int designateOfficerId) {
        this.designateOfficerId = designateOfficerId;
    }
}
