package com.stars.multiserver.LootTreasure;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by panzhenfeng on 2016/10/12.
 */
public class LTDamageRankVo implements Comparable<LTDamageRankVo> {
    private long roleId;
    private String roleName;
    private int damage;
    private short rank;
    private short roleLevel;
    private byte roleJob;
    private String serverName;

    public LTDamageRankVo(){

    }

    public LTDamageRankVo(long roleId, String roleName, int roleLevel, int job, String serverName) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.damage = 0;
        this.rank = -1;
        this.roleLevel = (short)roleLevel;
        this.roleJob = (byte)job;
        this.serverName = serverName;
    }

    public void writeBuff(NewByteBuffer buff){
        buff.writeString(String.valueOf(this.roleId));
        buff.writeString(this.roleName);
        buff.writeInt(this.damage);
        buff.writeShort(this.rank);
        buff.writeShort(this.roleLevel);
        buff.writeByte(this.roleJob);
        buff.writeString(this.serverName);
    }

    public void readBuff(NewByteBuffer buff){
        this.roleId = Long.parseLong(buff.readString());
        this.roleName = buff.readString();
        this.damage = buff.readInt();
        this.rank = buff.readShort();
        this.roleLevel = buff.readShort();
        this.roleJob = buff.readByte();
        this.serverName = buff.readString();
    }

    public void addAddedDamage(int value) {
        this.damage += value;
    }

    @Override
    public int compareTo(LTDamageRankVo o) {
        if (roleId==o.getRoleId()) {
            return 0;
        }
        if (damage != o.getDamage()) {
            return damage < o.getDamage() ? 1 : -1;
        } else {
            return roleId == o.getRoleId() ? 0 : roleId < o.getRoleId() ? -1 : 1;
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof LTDamageRankVo){
            LTDamageRankVo rankVo = (LTDamageRankVo)obj;
            if(rankVo.getRoleId() == this.getRoleId()){
                return true;
            }
        }
        return false;
    }

    public int getDamage() {
        return damage;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(short rank) {
        this.rank = rank;
    }

    public long getRoleId() {
        return roleId;
    }


}
