package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

/**
 * Created by liuyuheng on 2016/10/8.
 */
public class ClientRoleRevive extends PlayerPacket {
    private byte subType;
    public static final byte ROLE = 1;//角色复活
    public static final byte BUDDY = 2;//伙伴复活

    private String reviceRoleId;
    private String buddyId;//此处是伙伴的uid，非真实伙伴id
    private boolean isSuc;

    public ClientRoleRevive() {
    }

    public ClientRoleRevive(long roleId, boolean isSuc) {
        this.subType = ROLE;
        this.reviceRoleId = String.valueOf(roleId);
        this.isSuc = isSuc;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return ScenePacketSet.C_REVIVE;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subType);
        switch (subType) {
            case ROLE: {
                buff.writeString(reviceRoleId);
                LogUtil.info(" {} 复活啦,{}", reviceRoleId, isSuc);
            }
            break;
            case BUDDY: {
                buff.writeString(buddyId);
            }
            break;
        }
        buff.writeByte((byte) (isSuc ? 1 : 0));
    }

    public byte getSubType() {
        return subType;
    }

    public void setSubType(byte subType) {
        this.subType = subType;
    }

    public String getReviceRoleId() {
        return reviceRoleId;
    }

    public void setReviceRoleId(String reviceRoleId) {
        this.reviceRoleId = reviceRoleId;
    }

    public String getBuddyId() {
        return buddyId;
    }

    public void setBuddyId(String buddyId) {
        this.buddyId = buddyId;
    }

    public boolean isSuc() {
        return isSuc;
    }

    public void setSuc(boolean suc) {
        isSuc = suc;
    }
}
