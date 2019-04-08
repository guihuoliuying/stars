package com.stars.modules.activeweapon.prodata;

/**
 * Created by huwenjun on 2017/6/15.
 */
public class ActiveWeaponVo {
    private Integer id;//条件id
    private Integer type;//条件类型，1登录，2主线关卡，3条件id并合
    private String condition;//条件值，对应1类型为天数，对应2类型为通关关卡，对应3类型是条件组合，达成对应条件才满足
    private Integer reward;//对应drop的物品
    private String showid;//对应展示id表里表现，填0不调用。根据玩家角色职业对应，1剑尊，2墨客，3魅影，4女萝，配置：1+201,2+202,….
    private String desc;//条件描述说明
    private Integer itemsign;//对应星孔位置

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Integer getReward() {
        return reward;
    }

    public void setReward(Integer reward) {
        this.reward = reward;
    }

    public String getShowid() {
        return showid;
    }

    public void setShowid(String showid) {
        this.showid = showid;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getItemsign() {
        return itemsign;
    }

    public void setItemsign(Integer itemsign) {
        this.itemsign = itemsign;
    }
}
