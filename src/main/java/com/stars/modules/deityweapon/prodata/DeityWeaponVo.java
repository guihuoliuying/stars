package com.stars.modules.deityweapon.prodata;

/**
 * 神兵数据;
 * Created by panzhenfeng on 2016/12/14.
 */
public class DeityWeaponVo {
    private int deityweaponId; //神兵ID;
    private int jobid;    //整值, 职业id
    private String name;    //字符串, 神兵名称
    private String icon;    //字符串, 神兵图标
    private byte type;    //整值, 神兵类型. 一个职业不存在两个type相同的神兵
    private byte order;    //整值, 界面显示顺序
    private String model;    //字符串, 神兵模型调用的武器资源名
    private int modelscale;    //整值, 神兵界面显示的模型缩放千分比
    private String effect;    //字符串, 神兵模型挂载的模型特效. 格式为: name1+name2, 可能只存在一个name
    private int timelimit;    //整值, 时装时间限制, 以天为单位
    private String attrdesc;    //字符串, 属性描述文本索引
    private String getwaydesc;    //字符串, 获取途径描述文本索引
    private int reqrolelevel;//神兵显示的最小角色等级
    private int reqviplevel;//神兵显示的最小vip等级
    private int itemId;//对应物品的id
    /**额外接口 start**/

    /**
     * 是否是永久的;
     *
     * @return
     */
    public boolean isForever() {
        return timelimit == 0;
    }

    /**
     * 额外接口 end
     **/

    public int getJobid() {
        return jobid;
    }

    public void setJobid(int jobid) {
        this.jobid = jobid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getOrder() {
        return order;
    }

    public void setOrder(byte order) {
        this.order = order;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getModelscale() {
        return modelscale;
    }

    public void setModelscale(int modelscale) {
        this.modelscale = modelscale;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public int getTimelimit() {
        return timelimit;
    }

    public void setTimelimit(int timelimit) {
        this.timelimit = timelimit;
    }

    public String getAttrdesc() {
        return attrdesc;
    }

    public void setAttrdesc(String attrdesc) {
        this.attrdesc = attrdesc;
    }

    public String getGetwaydesc() {
        return getwaydesc;
    }

    public void setGetwaydesc(String getwaydesc) {
        this.getwaydesc = getwaydesc;
    }

    public int getDeityweaponId() {
        return deityweaponId;
    }

    public void setDeityweaponId(int deityweaponId) {
        this.deityweaponId = deityweaponId;
    }

    public int getReqrolelevel() {
        return reqrolelevel;
    }

    public void setReqrolelevel(int reqrolelevel) {
        this.reqrolelevel = reqrolelevel;
    }

    public int getReqviplevel() {
        return reqviplevel;
    }

    public void setReqviplevel(int reqviplevel) {
        this.reqviplevel = reqviplevel;
    }

    public boolean checkCondition(int rolelevel, int vipLevel) {
        if (rolelevel >= reqrolelevel || vipLevel >= reqviplevel) {
            return true;
        }
        return false;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
}
