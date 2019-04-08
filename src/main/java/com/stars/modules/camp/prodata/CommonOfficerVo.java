package com.stars.modules.camp.prodata;

import com.stars.modules.camp.CampManager;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/6/26.
 * 普通官职
 */
public class CommonOfficerVo implements Comparable<CommonOfficerVo> {
    private Integer id;//流水号
    private Integer level;//填数值，表示官职等级
    private String name;//gametext的key，表示当前等级的官职名称
    private Integer reqlevel;//升级所需功勋数量
    private String attr;//格式为: attrname=数值, attrname=数值
    private Integer camplevel;//填数值，表示所在阵营等级达到该值才能继续升级
    private String dayaward;//格式：itemid+数量| itemid+数量

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getReqlevel() {
        return reqlevel;
    }

    public void setReqlevel(Integer reqlevel) {
        this.reqlevel = reqlevel;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
    }

    public Integer getCamplevel() {
        return camplevel;
    }

    public void setCamplevel(Integer camplevel) {
        this.camplevel = camplevel;
    }

    public String getDayaward() {
        return dayaward;
    }

    public void setDayaward(String dayaward) {
        this.dayaward = dayaward;
    }

    @Override
    public int compareTo(CommonOfficerVo o) {
        return this.getLevel() - o.getLevel();
    }

    public CommonOfficerVo getNextLevelCommonOfficerVo() {
        return CampManager.commonOfficerMap.get(id + 1);
    }

    public void writeBuff(NewByteBuffer buff) {
        buff.writeInt(id);
        buff.writeInt(level);
        buff.writeString(name);
        buff.writeInt(reqlevel);
        buff.writeString(attr);
        buff.writeInt(camplevel);
        buff.writeString(dayaward);
    }
}
