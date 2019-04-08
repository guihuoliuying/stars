package com.stars.services.pay;

public class PayOrderInfo {
    private String roleId = ""; // 角色编号，默认为空字符串，不允许出现null
    private String itemId = ""; // 购买的物品编号，默认为空字符串，不允许出现null
    private String privateField = ""; // cp（应用）自定义字段，默认为空字符串，不允许出现null
    private String cpTradeNo; // cp订单号
    private int gameId; // 游戏编号
    private String userId; // 用户编号
    private int serverId; // 游戏区号
    private int channelId; // 市场部定义的渠道码
    private int itemAmount; // 购买数量
    private int money; // 金额（分）；
    private String status; // 交易状态，0成功，>=1 失败
    private String sign; // 验证签名
    private String currencyType = "CNY";//货币类型：CNY(人民币);USD(美元)；
    private Float fee;// 实际支付金额;和currencyType结合；人民币以元为单位，USD以美元为单位；
    private String giftId;
    private String payType;//支付类型
    private int actionType;//行为类型,0：人民币充值，1：虚拟币充值

    public PayOrderInfo() {
    }

    public String getGiftId() {
        return giftId;
    }

    public void setGiftId(String giftId) {
        this.giftId = giftId;
    }

    public Float getFee() {
        return fee;
    }

    public void setFee(Float fee) {
        this.fee = fee;
    }

    public void setRoleId(String roleId) {
        this.roleId = (roleId == null ? "" : roleId);// 不允许出现null
    }

    public void setPrivateField(String privateField) {
        this.privateField = (privateField == null ? "" : privateField);// 不允许出现null
    }

    public void setItemId(String itemId) {
        this.itemId = (itemId == null ? "" : itemId);// 不允许出现null
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getPrivateField() {
        return privateField;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public String getCpTradeNo() {
        return cpTradeNo;
    }

    public void setCpTradeNo(String cpTradeNo) {
        this.cpTradeNo = cpTradeNo;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getItemAmount() {
        return itemAmount;
    }

    public void setItemAmount(int itemAmount) {
        this.itemAmount = itemAmount;
    }

    public String getItemId() {
        return itemId;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public String getRoleId() {
        return roleId;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }
}
