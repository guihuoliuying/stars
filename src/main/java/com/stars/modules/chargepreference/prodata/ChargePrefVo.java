package com.stars.modules.chargepreference.prodata;

import com.stars.modules.push.conditionparser.PushCondLexer;
import com.stars.modules.push.conditionparser.PushCondParser;
import com.stars.modules.push.conditionparser.node.PushCondNode;
import com.stars.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/30.
 */
public class ChargePrefVo {

    // 数据库
    private int prefId; // 特惠id
    private int pushId; // 推送id（对应push表的id）
    private int rank; // 排序优先级
    private String goods; // 奖励物品
    private String showItem; // 展示物品
    private byte isNew; // 是否新品
    private int originPrice; // 原价
    private int currentPrice; // 现价
    private int rebatePrice; // 折扣价
    private String rebateCondition; // 折扣条件（非时间）
    private String rebateDate; // 折扣条件（时间）

    // 内存
    private Map<Integer, Integer> items;
    private PushCondNode condChecker;
    private boolean hasExpirationDate;
    private long beginTimeMillis;
    private long endTimeMillis;

    public void init() throws Exception {
        // 物品
        this.items = StringUtil.toMap(goods, Integer.class, Integer.class, '+', ',');
        // 条件
        this.condChecker = new PushCondParser(new PushCondLexer(rebateCondition)).parse();
        // date -> yyyy-MM-dd HH:mm:ss | yyyy-MM-dd HH:mm:ss
        if (rebateDate != null && rebateDate.trim().length() > 0 && !rebateDate.trim().equals("0")) {
            this.hasExpirationDate = true;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.beginTimeMillis = sdf.parse(rebateDate.split("\\|")[0].trim()).getTime();
            this.endTimeMillis = sdf.parse(rebateDate.split("\\|")[1].trim()).getTime();
        }
    }

    public int getPrefId() {
        return prefId;
    }

    public void setPrefId(int prefId) {
        this.prefId = prefId;
    }

    public int getPushId() {
        return pushId;
    }

    public void setPushId(int pushId) {
        this.pushId = pushId;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getGoods() {
        return goods;
    }

    public void setGoods(String goods) {
        this.goods = goods;
    }

    public String getShowItem() {
        return showItem;
    }

    public void setShowItem(String showItem) {
        this.showItem = showItem;
    }

    public byte getIsNew() {
        return isNew;
    }

    public void setIsNew(byte isNew) {
        this.isNew = isNew;
    }

    public int getOriginPrice() {
        return originPrice;
    }

    public void setOriginPrice(int originPrice) {
        this.originPrice = originPrice;
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(int currentPrice) {
        this.currentPrice = currentPrice;
    }

    public int getRebatePrice() {
        return rebatePrice;
    }

    public void setRebatePrice(int rebatePrice) {
        this.rebatePrice = rebatePrice;
    }

    public String getRebateCondition() {
        return rebateCondition;
    }

    public void setRebateCondition(String rebateCondition) {
        this.rebateCondition = rebateCondition;
    }

    public String getRebateDate() {
        return rebateDate;
    }

    public void setRebateDate(String rebateDate) {
        this.rebateDate = rebateDate;
    }


    /*
     * 内存数据GETTER
     */
    public boolean isNew() {
        return isNew == 1;
    }

    public Map<Integer, Integer> getItems() {
        return items;
    }

    public PushCondNode getCondChecker() {
        return condChecker;
    }

    public boolean hasExpirationDate() {
        return hasExpirationDate;
    }

    public long getBeginTimeMillis() {
        return beginTimeMillis;
    }

    public long getEndTimeMillis() {
        return endTimeMillis;
    }
}
