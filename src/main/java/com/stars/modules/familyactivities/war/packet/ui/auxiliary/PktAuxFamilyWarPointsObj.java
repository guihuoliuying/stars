package com.stars.modules.familyactivities.war.packet.ui.auxiliary;

import com.stars.multiserver.MultiServerHelper;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2016/12/19.
 */
public class PktAuxFamilyWarPointsObj {

    private long roleId;
    private String roleName;
    private String familyName;
    private int serverId;
    private long points;

    public PktAuxFamilyWarPointsObj(long roleId, String roleName, String familyName, int serverId, long points) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.familyName = familyName;
        this.serverId = serverId;
        this.points = points;
    }

    public long getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public int getServerId() {
        return serverId;
    }

    public long getPoints() {
        return points;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeString(Long.toString(roleId)); // role id
        buff.writeString(roleName); // role name
        buff.writeString(Integer.toString(serverId)); // server id
        buff.writeString(MultiServerHelper.getServerName(serverId));//serverName
        buff.writeString(Long.toString(points)); // points
        buff.writeString(familyName);// 家族名字
    }
}
