package com.stars.modules.luckydraw.pojo;

import com.stars.modules.luckydraw.LuckyDrawManagerFacade;
import com.stars.modules.luckydraw.prodata.LuckyPumpAwardVo;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyDrawAnnounce {
    private long roleId;
    private String roleName;
    private int awardId;
    private int actType;

    public LuckyDrawAnnounce(long roleId, String roleName, int awardId, int actType) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.awardId = awardId;
        this.actType=actType;
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

    public int getAwardId() {
        return awardId;
    }

    public void setAwardId(int awardId) {
        this.awardId = awardId;
    }

    public int getActType() {
        return actType;
    }

    public void setActType(int actType) {
        this.actType = actType;
    }

    public void writeBuff(NewByteBuffer buff) {
        buff.writeString(roleName);
        LuckyPumpAwardVo luckyPumpAwardVo = LuckyDrawManagerFacade.getLuckyPumpAwardMap(getActType()).get(awardId);
        buff.writeInt(awardId);
        buff.writeString(luckyPumpAwardVo.getDesc());
    }
}
