package com.stars.modules.family.event;

import com.stars.core.event.Event;
import com.stars.services.family.FamilyPost;

/**
 * Created by zhaowenshuo on 2016/8/24.
 */
public class FamilyAuthUpdatedEvent extends Event {

    public static final byte TYPE_NEW = 0x00; // 新成员
    public static final byte TYPE_UPDATED = 0x01; // 更新
    public static final byte TYPE_CREATED = 0x02; // 创建家族（不会更新auth）
    public static final byte TYPE_LOGIN = 0x03; // 登录时设置家族
    public static final byte TYPE_DISSOLVE = 0x04; // 解散家族（不会更新auth）

    private byte type;
    private long roleId;
    private long familyId;
    private String familyName;
    private int familyLevel;
    private FamilyPost post;
    private long prevFamilyId;

    public FamilyAuthUpdatedEvent(long roleId, long familyId, String familyName, int familyLevel, FamilyPost post, long prevFamilyId) {
        this.type = TYPE_UPDATED;
        this.roleId = roleId;
        this.familyId = familyId;
        this.familyName = familyName;
        this.familyLevel = familyLevel;
        this.post = post;
        this.prevFamilyId = prevFamilyId;
    }

    public FamilyAuthUpdatedEvent(byte type, long roleId, long familyId, String familyName, int familyLevel, FamilyPost post, long prevFamilyId) {
        this.type = type;
        this.roleId = roleId;
        this.familyId = familyId;
        this.familyName = familyName;
        this.familyLevel = familyLevel;
        this.post = post;
        this.prevFamilyId = prevFamilyId;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public long getRoleId() {
        return roleId;
    }

    public long getFamilyId() {
        return familyId;
    }

    public String getFamilyName() {
        return familyName;
    }

    public int getFamilyLevel() {
        return familyLevel;
    }

    public FamilyPost getPost() {
        return post;
    }

    public long getPrevFamilyId() {
        return prevFamilyId;
    }

    @Override
    public String toString() {
        return "FamilyAuthUpdatedEvent{" +
                "type=" + type +
                ", roleId=" + roleId +
                ", familyId=" + familyId +
                ", familyName='" + familyName + '\'' +
                ", familyLevel=" + familyLevel +
                ", post=" + post +
                '}';
    }
}
