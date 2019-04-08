package com.stars.multiserver.camp.pojo;

import com.stars.modules.camp.pojo.CampEquilibrium;

/**
 * Created by huwenjun on 2017/6/27.
 */
public class CampTypeScale {
    private int lowCampType;
    private float scale;
    private CampEquilibrium campEquilibrium;
    public int getLowCampType() {
        return lowCampType;
    }

    public void setLowCampType(int lowCampType) {
        this.lowCampType = lowCampType;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public CampEquilibrium getCampEquilibrium() {
        return campEquilibrium;
    }

    public void setCampEquilibrium(CampEquilibrium campEquilibrium) {
        this.campEquilibrium = campEquilibrium;
    }
}