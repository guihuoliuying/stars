package com.stars.services.shop;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by zhouyaohui on 2016/9/7.
 */
public class ShopServerLimitRow extends DbRow {
    private int goodsId;
    private int times;

    public ShopServerLimitRow(){}

    public ShopServerLimitRow(int goodsId) {
        this.goodsId = goodsId;
        times = 0;
    }

    public int getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(int goodsId) {
        this.goodsId = goodsId;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "shopserverlimit", "goodsid = " + goodsId);
    }

    @Override
    public String getDeleteSql() {
        return "";
    }
}
