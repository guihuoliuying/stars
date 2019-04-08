package com.stars.modules.luckycard.pojo;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyCardAnnounce {
    private long roleId;
    private String roleName;
    private int cardId;

    public LuckyCardAnnounce(long roleId, String roleName, int cardId) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.cardId = cardId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public void writeBuff(NewByteBuffer buff) {
        buff.writeString(roleName);
        buff.writeInt(cardId);
    }
}
