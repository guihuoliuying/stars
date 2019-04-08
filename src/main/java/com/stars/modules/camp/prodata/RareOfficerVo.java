package com.stars.modules.camp.prodata;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/6/26.
 */
public class RareOfficerVo {
    private Integer id;//流水号
    private Integer quality;//填数值，表示官职品质，分一到九品
    private String name;//九品最低，一品最高
    private String attr;//gametext的key，表示当前等级的官职名称
    private String dayaward;//填数值，表示所在阵营等级达到该值才能继续升级
    private String designateofficer;//格式：itemid+数量| itemid+数量
    private String designateaward;//格式：任命官职id+数量
    private Integer appellationid;//配置活动id+次数+奖励dropid，多个用|隔开
    private String privilege;//表示任命玩家每完成一次对应活动时，可获得1份奖励，多次完成累计，及每日最多可获得的次数

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

    public String getDesignateofficer() {
        return designateofficer;
    }

    public void setDesignateofficer(String designateofficer) {
        this.designateofficer = designateofficer;
    }

    public String getDesignateaward() {
        return designateaward;
    }

    public void setDesignateaward(String designateaward) {
        this.designateaward = designateaward;
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

    public void writeBuff(NewByteBuffer buff) {
        buff.writeInt(id);
        buff.writeInt(quality);
        buff.writeString(name);
        buff.writeString(attr);
        buff.writeString(dayaward);
        buff.writeString(designateofficer);
        buff.writeString(designateaward);
        buff.writeInt(appellationid);
        buff.writeString(privilege);
    }
}
