package com.stars.modules.shop.prodata;

import com.stars.core.module.Module;
import com.stars.modules.shop.ExtendConditionHandle;
import com.stars.modules.shop.ShopManager;
import com.stars.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * Created by zhouyaohui on 2016/9/5.
 */
public class Shop {
    private int goodsId;
    private int itemId;
    private int shopType;
    private int subtype;
    private String salePic;
    private String primePrice;
    private String price;
    private int count;
    private int putOrder;
    private int isOnline;
    private String serverDays;
    private String saleDate;
    private int serviceLimitNum;
    private int personalLimitNum;
    private int dailyLimitNum;
    private String vipNum;
    private String platform;
    private String levelRange;
    private int weight;
    private String extendCondition;

    private int serverDaysMin;
    private int serverDaysMax;
    private long saleDateMin;
    private long saleDateMax;
    private int levelRangeMin;
    private int levelRangeMax;
    private String viprange;//商品vip等级范围

    private ExtendConditionHandle handle;

    public int getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(int goodsId) {
        this.goodsId = goodsId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getShopType() {
        return shopType;
    }

    public void setShopType(int shopType) {
        this.shopType = shopType;
    }

    public int getSubType() {
        return subtype;
    }

    public void setSubtype(int subtype) {
        this.subtype = subtype;
    }

    public String getSalePic() {
        return salePic;
    }

    public void setSalePic(String salePic) {
        this.salePic = salePic;
    }

    public String getPrimePrice() {
        return primePrice;
    }

    public void setPrimePrice(String primePrice) {
        this.primePrice = primePrice;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPutOrder() {
        return putOrder;
    }

    public void setPutOrder(int putOrder) {
        this.putOrder = putOrder;
    }

    public int getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(int isOnline) {
        this.isOnline = isOnline;
    }

    public String getServerDays() {
        return serverDays;
    }

    public void setServerDays(String serverDays) {
        this.serverDays = serverDays;
        if (StringUtil.isEmpty(this.serverDays)) return;
        if (this.serverDays.trim().split("\\+").length != 2) return;
        serverDaysMin = Integer.valueOf(this.serverDays.trim().split("\\+")[0]);
        serverDaysMax = Integer.valueOf(this.serverDays.trim().split("\\+")[1]);
    }

    public String getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
        if (StringUtil.isEmpty(this.saleDate)) return;
        if (this.saleDate.trim().split("\\+").length != 2) return;
        saleDateMin = Long.valueOf(this.saleDate.trim().split("\\+")[0]);
        saleDateMax = Long.valueOf(this.saleDate.trim().split("\\+")[1]);
    }

    public int getServiceLimitNum() {
        return serviceLimitNum;
    }

    public void setServiceLimitNum(int serviceLimitNum) {
        this.serviceLimitNum = serviceLimitNum;
    }

    public int getPersonalLimitNum() {
        return personalLimitNum;
    }

    public void setPersonalLimitNum(int personalLimitNum) {
        this.personalLimitNum = personalLimitNum;
    }

    public int getDailyLimitNum() {
        return dailyLimitNum;
    }

    public void setDailyLimitNum(int dailyLimitNum) {
        this.dailyLimitNum = dailyLimitNum;
    }

    public String getVipNum() {
        return vipNum;
    }

    public void setVipNum(String vipNum) {
        this.vipNum = vipNum;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getLevelRange() {
        return levelRange;
    }

    public void setLevelRange(String levelRange) {
        this.levelRange = levelRange;
        if (StringUtil.isEmpty(this.levelRange)) return;
        if (this.levelRange.trim().split("\\+").length != 2) return;
        levelRangeMin = Integer.valueOf(this.levelRange.trim().split("\\+")[0]);
        levelRangeMax = Integer.valueOf(this.levelRange.trim().split("\\+")[1]);
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getExtendCondition() {
        return extendCondition;
    }

    public void setExtendCondition(String extendCondition) {
        this.extendCondition = extendCondition;
    }

    public ExtendConditionHandle getHandle() {
        return handle;
    }

    public void setHandle(ExtendConditionHandle handle) {
        this.handle = handle;
    }

    public String getViprange() {
        return viprange;
    }

    public void setViprange(String viprange) {
        this.viprange = viprange;
    }
    /********************** 便利接口 ***********************/

    /**
     * 检查产品上线，开服时效以及出售时效，无特殊需求用这个检查时效性
     *
     * @return
     */
    public boolean checkOnlineAndTime(long curMillis, int serverDays) {
        return isOnline() && isInServerDays(serverDays) && isOnSaleDays(curMillis);
    }

    /**
     * 检查 角色等级
     *
     * @param level
     * @return
     */
    public boolean checkLevel(int level) {
        if (levelRangeMin == 0 && levelRangeMax == 0) return true;
        if (level < levelRangeMin || level > levelRangeMax) return false;
        return true;
    }

    /**
     * 检查 出售时效性
     *
     * @param curMillis
     * @return
     */
    private boolean isOnSaleDays(long curMillis) {
        if (saleDateMin == 0 && saleDateMax == 0) return true;
        long curDay = Integer.valueOf(new SimpleDateFormat(ShopManager.YYMMDDHH).format(curMillis));
        if (curDay < saleDateMin || curDay >= saleDateMax) return false;
        return true;
    }

    /**
     * 检查产品开服时效性
     *
     * @param serverDays
     * @return
     */
    public boolean isInServerDays(int serverDays) {
        if (serverDaysMin == 0 && serverDaysMax == 0) return true;
        if (serverDays < serverDaysMin || serverDays > serverDaysMax) return false;
        return true;
    }

    /**
     * 检查产品上线
     *
     * @return
     */
    public boolean isOnline() {
        return getIsOnline() == 1;
    }

    /**
     * 检查 个人终身限购条件
     *
     * @param personalLimitTimes
     * @return
     */
    public boolean checkPersonalLimit(int personalLimitTimes) {
        if (personalLimitNum == 0) return true;
        if (personalLimitTimes > personalLimitNum) return false;
        return true;
    }

    /**
     * 检查全服限购条件
     *
     * @param serviceLimitTimes
     * @return
     */
    public boolean checkServiceLimit(int serviceLimitTimes) {
        if (serviceLimitNum == 0) return true;
        if (serviceLimitTimes > serviceLimitNum) return false;
        return true;
    }

    /**
     * 检查 每日购买条件
     *
     * @param dailyTimes
     * @return
     */
    public boolean checkDailyLimit(int dailyTimes) {
        if (dailyLimitNum == 0) return true;
        if (dailyTimes > dailyLimitNum) return false;
        return true;
    }

    /**
     * 是否是每日限购
     *
     * @return
     */
    public boolean isDailyLimit() {
        return dailyLimitNum != 0;
    }

    /**
     * 是否是个人终身限购
     *
     * @return
     */
    public boolean isPersonalLimit() {
        return personalLimitNum != 0;
    }

    /**
     * 是否是全服限购
     *
     * @return
     */
    public boolean isServiceLimit() {
        return serviceLimitNum != 0;
    }

    /**
     * 检查额外的条件
     *
     * @param moduleMap
     * @return
     */
    public boolean checkExtendCondition(Map<String, Module> moduleMap) {
        if (handle == null) return true;
        return handle.check(moduleMap);
    }

    /**
     * 检查角色vip等级是否满足
     *
     * @param vipLevel
     * @return
     */
    public boolean checkVipLevel(int vipLevel) {
        int highLevel = 0;
        int lowLevel = 0;
        if (viprange != null) {
            String[] vipLevels = viprange.split("\\+");
            lowLevel = Integer.parseInt(vipLevels[0]);
            highLevel = Integer.parseInt(vipLevels[1]);
        }
        return vipLevel >= lowLevel && vipLevel <= highLevel;
    }
}
