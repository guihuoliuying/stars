package com.stars.multiserver.familywar.knockout.fight.elite;

/**
 * Created by zhaowenshuo on 2016/12/8.
 */
public class EliteFightTower {

    private String uid;
    private byte camp;
    private byte type;
    private String pos;
    private int maxHp;
    private int hp;

    public EliteFightTower(String uid, byte camp, byte type, String pos, int maxHp) {
        this.uid = uid;
        this.camp = camp;
        this.type = type;
        this.pos = pos;
        this.maxHp = maxHp;
        this.hp = maxHp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public byte getCamp() {
        return camp;
    }

    public void setCamp(byte camp) {
        this.camp = camp;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }
    
    public void reduceHp(int delta) {
    	this.hp -= delta;
    }
}
