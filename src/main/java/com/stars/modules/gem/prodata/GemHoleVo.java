package com.stars.modules.gem.prodata;

/**
 * 宝石槽配置数据项;
 * Created by panzhenfeng on 2016/7/25.
 */
public class GemHoleVo {
    private byte equipType;
    private byte holeId;
    private int unlockLvl;


    public byte getHoleId() {
        return holeId;
    }

    public void setHoleId(byte holeId) {
        this.holeId = holeId;
    }

    public int getUnlockLvl() {
        return unlockLvl;
    }

    public void setUnlockLvl(int unlockLvl) {
        this.unlockLvl = unlockLvl;
    }

    public byte getEquipType() {
        return equipType;
    }

    public void setEquipType(byte equipType) {
        this.equipType = equipType;
    }
}
