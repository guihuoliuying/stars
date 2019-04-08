package com.stars.services.baseteam;

import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/11/10.
 */
public class BaseTeamMember implements Comparable<BaseTeamMember> {
    private long roleId;//
    private byte type;// 0=真实玩家;1=构造玩家数据,默认是0
    private byte job;
    private Map<String, FighterEntity> entityMap = new HashMap<>();
    private String familyName;// 家族名称

    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeString(String.valueOf(getRoleId()));// roleId
        buff.writeByte(type);// 0=真实玩家;1=构造玩家数据,默认是0
        buff.writeString(getRoleEntity().getName());// 名字
        buff.writeShort((short) getRoleEntity().getLevel());// 等级
        buff.writeByte(getJob());// 职业
        buff.writeInt(getFightSocre());// 战力
        buff.writeInt(getCurDeityWeapon());// 当前使用神兵
    }

    public BaseTeamMember() {
        this.type = 0;
    }

    public BaseTeamMember(byte type) {
        this.type = type;
    }

    @Override
    public int compareTo(BaseTeamMember o) {
        if (roleId == o.getRoleId()) {
            return 0;
        }
        int tag = getRoleEntity().getLevel() - o.getRoleEntity().getLevel();
        if (tag != 0) {
            return tag;
        }
        return this.getRoleEntity().getFightScore() - o.getRoleEntity().getFightScore();
    }

    /**
     * 过滤去掉伙伴
     */
    public void removeBuddyEntity() {
        String buddyEntityUId = null;
        for (FighterEntity entity : entityMap.values()) {
            if (entity.getFighterType() == FighterEntity.TYPE_BUDDY) {
                buddyEntityUId = entity.getUniqueId();
            }
        }
        if (!StringUtil.isEmpty(buddyEntityUId)) {
            entityMap.remove(buddyEntityUId);
        }
    }

    public boolean isPlayer() {
        return type == 0;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return getRoleEntity().getName();
    }

    public short getLevel() {
        return (short) getRoleEntity().getLevel();
    }

    public byte getJob() {
        return job;
    }

    public void setJob(byte job) {
        this.job = job;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public Map<String, FighterEntity> getEntityMap() {
        return entityMap;
    }

    public FighterEntity getRoleEntity() {
        return entityMap.get(String.valueOf(roleId));
    }

    public void addEntity(FighterEntity entity) {
        entityMap.put(entity.getUniqueId(), entity);
    }

    public int getFightSocre() {
        return getRoleEntity().getFightScore();
    }

    public void setEntityMap(Map<String, FighterEntity> entityMap) {
        this.entityMap = entityMap;
    }

    public int getCurDeityWeapon() {
        return getRoleEntity().getCurDeityWeapon();
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }
    
    public static void main(String u[]){
    	String roleids = "r1001";
    	long test = Long.parseLong(roleids);
    	int t = 0;
    	int a = t;
    }

    @Override
    public String toString() {
        return "BaseTeamMember{" +
                "roleId=" + roleId +
                ", type=" + type +
                ", job=" + job +
                ", entityMap=" + entityMap +
                ", familyName='" + familyName + '\'' +
                '}';
    }
}
