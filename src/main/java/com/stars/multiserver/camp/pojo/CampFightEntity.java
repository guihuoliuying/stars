package com.stars.multiserver.camp.pojo;

import com.stars.modules.scene.fightdata.FighterEntity;

/**
 * Created by huwenjun on 2017/7/20.
 */
public class CampFightEntity {
    private int campType;
    private FighterEntity fighterEntity;

    public int getCampType() {
        return campType;
    }

    public void setCampType(int campType) {
        this.campType = campType;
    }

    public FighterEntity getFighterEntity() {
        return fighterEntity;
    }

    public void setFighterEntity(FighterEntity fighterEntity) {
        this.fighterEntity = fighterEntity;
    }
}
