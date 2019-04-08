package com.stars.modules.camp.prodata;

import com.stars.modules.camp.CampManager;

/**
 * Created by huwenjun on 2017/6/26.
 * 阵营等级表
 */
public class CampLevelVo {
    private Integer camptype;//填整数，表示阵营，1表示齐国，2表示楚国
    private Integer level;//数值，表示阵营等级，1级开始
    private Integer reqlevel;//数值，表示该等级阵营升级需要繁荣度达到某个字，升级后，繁荣度不消耗
    private String image;//填图片路径名称，表示该等级阵营的标志
    private String addactivity;//格式：活动类型+加成千分比
    private Integer email;//emailtemplate表id
    private Integer addprosperous;//每日自动成长繁荣度

    public Integer getCamptype() {
        return camptype;
    }

    public void setCamptype(Integer camptype) {
        this.camptype = camptype;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getReqlevel() {
        return reqlevel;
    }

    public void setReqlevel(Integer reqlevel) {
        this.reqlevel = reqlevel;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAddactivity() {
        return addactivity;
    }

    public void setAddactivity(String addactivity) {
        this.addactivity = addactivity;
    }

    public Integer getEmail() {
        return email;
    }

    public void setEmail(Integer email) {
        this.email = email;
    }

    public Integer getAddprosperous() {
        return addprosperous;
    }

    public void setAddprosperous(Integer addprosperous) {
        this.addprosperous = addprosperous;
    }

    public CampLevelVo getNextLevelCampLevelVo() {
        return CampManager.getCampLevelVo(this.getCamptype(), this.getLevel() + 1);
    }
}
