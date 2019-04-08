package com.stars.modules.camp.pojo;

import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.prodata.RareOfficerVo;

/**
 * Created by huwenjun on 2017/7/25.
 */
public class CampFightOfficerAttr {
    private int min;
    private int max;
    private int scale;
    private String img;

    public CampFightOfficerAttr(int min, int max, int scale, String img) {
        this.min = min;
        this.max = max;
        this.scale = scale;
        this.img = img;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public boolean belongTo(int rareOfficerId) {
        RareOfficerVo rareOfficerVo = CampManager.rareOfficerMap.get(rareOfficerId);
        return rareOfficerVo.getQuality() >= min && rareOfficerVo.getQuality() <= max;
    }
}
