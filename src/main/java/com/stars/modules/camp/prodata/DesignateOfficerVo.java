package com.stars.modules.camp.prodata;

/**
 * Created by huwenjun on 2017/6/26.
 */
public class DesignateOfficerVo {
    private Integer id;//流水号
    private Integer quality;//填数值，表示官职品质，分一到九品
    private String name;//gametext的key，表示当前等级的官职名称
    private String attr;//格式为: attrname=数值, attrname=数值
    private String dayaward;//格式：itemid+数量| itemid+数量
    private Integer appellationid;//填appellation表的appellationid对应称谓，获得该官职时，激活该称谓，当官职变更时，称谓消失
    private String privilege;//配gametext的key，表示官职特权描述

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getQuality() {
        return quality;
    }

    public void setQuality(Integer quality) {
        this.quality = quality;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
    }

    public String getDayaward() {
        return dayaward;
    }

    public void setDayaward(String dayaward) {
        this.dayaward = dayaward;
    }

    public Integer getAppellationid() {
        return appellationid;
    }

    public void setAppellationid(Integer appellationid) {
        this.appellationid = appellationid;
    }

    public String getPrivilege() {
        return privilege;
    }

    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }
}
