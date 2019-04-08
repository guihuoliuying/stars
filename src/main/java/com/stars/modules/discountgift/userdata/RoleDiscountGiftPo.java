package com.stars.modules.discountgift.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.modules.discountgift.prodata.DiscountGiftVo;

/**
 * Created by chenxie on 2017/5/26.
 */
public class RoleDiscountGiftPo extends DbRow {

    /**
     * 角色ID
     */
    private long roleId;

    /**
     * 累计充值额度（对当前选择的商品有效）
     */
    private int totalCharge;

    /**
     * 商品组id
     */
    private int giftGroupId;

    /**
     * 商品id
     */
    private int giftId;

    /**
     * 商品id对应的dropid
     */
    private int dropId;

    /**
     * 商品对应的充值额度
     */
    private int charge;

    /**
     * 界面背景图资源名
     */
    private String img;

    /**
     * 活动时间
     */
    private String date;


    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getGiftGroupId() {
        return giftGroupId;
    }

    public void setGiftGroupId(int giftGroupId) {
        this.giftGroupId = giftGroupId;
    }

    public int getGiftId() {
        return giftId;
    }

    public void setGiftId(int giftId) {
        this.giftId = giftId;
    }

    public int getDropId() {
        return dropId;
    }

    public void setDropId(int dropId) {
        this.dropId = dropId;
    }

    public int getCharge() {
        return charge;
    }

    public void setCharge(int charge) {
        this.charge = charge;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getTotalCharge() {
        return totalCharge;
    }

    public void setTotalCharge(int totalCharge) {
        this.totalCharge = totalCharge;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolediscountgift", "`roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolediscountgift", "`roleid`=" + roleId);
    }

    public void copyToUserSpace(DiscountGiftVo discountGiftVo) {
        this.giftGroupId = discountGiftVo.getGiftGroupId();
        this.giftId = discountGiftVo.getGiftId();
        this.dropId = discountGiftVo.getDropId();
        this.charge = discountGiftVo.getCharge();
        this.img = discountGiftVo.getImg();
    }
}
