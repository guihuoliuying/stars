package com.stars.modules.newequipment.prodata;

/**
 * Created by zhanghaizhen on 2017/6/8.
 */
public class TokenVo {
    private int tokenId; //符文id
    private String icon; //符文icon
    private String tokenName; //符文名
    private String washCost; //洗练材料

    public int getTokenId() {
        return tokenId;
    }

    public void setTokenId(int tokenId) {
        this.tokenId = tokenId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public String getWashCost() {
        return washCost;
    }

    public void setWashCost(String washCost) {
        this.washCost = washCost;
    }
}
