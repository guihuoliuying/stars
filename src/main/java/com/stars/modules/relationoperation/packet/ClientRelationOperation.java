package com.stars.modules.relationoperation.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.relationoperation.RelationOperationPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2016/9/14.
 */
public class ClientRelationOperation extends PlayerPacket {

    /* 自身相关 */
    private long roleId; // 玩家id
    private int roleJobId; // 玩家职业id
    private String roleName; // 玩家名字
    private int roleLevel; // 玩家等级
    private boolean isFriend; // 是否好友
    private boolean isBlacker; // 是否黑名单
    private boolean isFriendOpen;//好友系统是否开放

    /* 家族相关 */
    private long familyId; // 家族id
    private String familyName; // 家族名字
    private byte postId; // 家族职位id

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return RelationOperationPacketSet.C_RELATION_OPERATION;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeString(Long.toString(roleId)); // roleId
        buff.writeInt(roleJobId); // 职业id
        buff.writeString(roleName); // 名字
        buff.writeInt(roleLevel); // 等级
        buff.writeByte((byte) (isFriend ? 1 : 0)); // 是否好友
        buff.writeByte((byte) (isBlacker ? 1 : 0)); // 是否黑名单
        buff.writeString(Long.toString(familyId)); // 家族id
        buff.writeString(familyName); // 家族名字
        buff.writeByte(postId); // 家族职位id
        buff.writeByte((byte) (isFriendOpen ? 1 : 0));
    }

    @Override
    public long getRoleId() {
        return roleId;
    }

    @Override
    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getRoleJobId() {
        return roleJobId;
    }

    public void setRoleJobId(int roleJobId) {
        this.roleJobId = roleJobId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getRoleLevel() {
        return roleLevel;
    }

    public void setRoleLevel(int roleLevel) {
        this.roleLevel = roleLevel;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public boolean isBlacker() {
        return isBlacker;
    }

    public void setBlacker(boolean blacker) {
        isBlacker = blacker;
    }

    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public byte getPostId() {
        return postId;
    }

    public void setPostId(byte postId) {
        this.postId = postId;
    }

    public boolean isFriendOpen() {
        return isFriendOpen;
    }

    public void setFriendOpen(boolean isFriendOpen) {
        this.isFriendOpen = isFriendOpen;
    }
}
