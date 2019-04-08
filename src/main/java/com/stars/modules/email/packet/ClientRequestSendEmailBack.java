package com.stars.modules.email.packet;

import com.stars.modules.email.EmailPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.HashMap;
import java.util.Map;

/**
 * 由其他服务器下发请求发送邮件的协议;
 * Created by panzhenfeng on 2016/10/28.
 */
public class ClientRequestSendEmailBack  extends Packet {
    //注意,该参数用于标识来源,来源供做特殊处理;
    private int customType;

    private long receiveRoleId;
    private int templateId;
    private long sendId;
    private String sendName;
    private Map<Integer, Integer> affixMap ;

    public ClientRequestSendEmailBack(){

    }
    @Override
    public short getType() {
        return EmailPacketSet.C_REQUEST_SEND_EMAIL;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(customType);
        buff.writeLong(receiveRoleId);
        buff.writeInt(templateId);
        buff.writeLong(sendId);
        buff.writeString(sendName);
        int affixSize = affixMap == null?0:affixMap.size();
        buff.writeInt(affixSize);
        for(Map.Entry<Integer, Integer> kvp : affixMap.entrySet()){
            buff.writeInt(kvp.getKey());
            buff.writeInt(kvp.getValue());
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        customType = buff.readInt();
        receiveRoleId = buff.readLong();
        templateId = buff.readInt();
        sendId = buff.readLong();
        sendName = buff.readString();
        int affixSize = buff.readInt();
        affixMap = new HashMap<>();
        for (int i = 0; i<affixSize; i++){
            affixMap.put(buff.readInt(), buff.readInt());
        }
    }

    @Override
    public void execPacket() {

    }

    public Map<Integer, Integer> getAffixMap() {
        return affixMap;
    }

    public void setAffixMap(Map<Integer, Integer> affixMap) {
        this.affixMap = affixMap;
    }

    public String getSendName() {
        return sendName;
    }

    public void setSendName(String sendName) {
        this.sendName = sendName;
    }

    public long getSendId() {
        return sendId;
    }

    public void setSendId(long sendId) {
        this.sendId = sendId;
    }

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public long getReceiveRoleId() {
        return receiveRoleId;
    }

    public void setReceiveRoleId(long receiveRoleId) {
        this.receiveRoleId = receiveRoleId;
    }

    public int getCustomType() {
        return customType;
    }

    public void setCustomType(int customType) {
        this.customType = customType;
    }
}
