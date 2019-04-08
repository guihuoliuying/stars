package com.stars.modules.bestcp520.prodata;

/**
 * Created by huwenjun on 2017/5/20.
 */
public class BestCP {
    private int cpId;//组合id
    private String cpName;//组合名称
    private int order;//排序
    private String icon;
    private String desc;//组合描述
    private String cpRole;

    public int getCpId() {
        return cpId;
    }

    public void setCpId(int cpId) {
        this.cpId = cpId;
    }

    public String getCpName() {
        return cpName;
    }

    public void setCpName(String cpName) {
        this.cpName = cpName;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }


    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCpRole() {
        return cpRole;
    }

    public void setCpRole(String cpRole) {
        this.cpRole = cpRole;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
