package com.stars.services.chat.cache;

import java.util.HashSet;

/**
 * Created by chenkeyu on 2016/11/9.
 */
public class RpcChatObject {
    private long roleId;
    public MyLinkedListNode linkedListNode;
    private HashSet<Byte> refuseChannel;
    public RpcChatObject(long roleId){
        this.roleId= roleId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public void setRefuseChannel(HashSet<Byte>refuseChannel){
        if (this.refuseChannel == null) {
            this.refuseChannel = new HashSet<Byte>();
        }else {
            this.refuseChannel.clear();
        }
        for (Byte byte1 : refuseChannel) {
            this.refuseChannel.add(byte1);
        }
    }
}
