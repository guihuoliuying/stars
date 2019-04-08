package com.stars.modules.discountgift.prodata;

/**
 * Created by chenxie on 2017/5/26.
 */
public class DiscountGiftVo implements Comparable<DiscountGiftVo> {

    /**
     * 商品组id
     */
    private int giftGroupId;

    /**
     * 商品id
     */
    private int giftId;

    /**
     * 商品组排序字段，按照数值由小到大
     */
    private int rank;

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

    private int originalPrice;

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

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
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

    public int getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(int originalPrice) {
        this.originalPrice = originalPrice;
    }

    @Override
    public int compareTo(DiscountGiftVo o) {
        if (this.giftGroupId == o.getGiftGroupId()) {
            return rank - o.getRank();
        } else {
            return giftGroupId - o.getGiftGroupId();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DiscountGiftVo that = (DiscountGiftVo) o;

        if (giftGroupId != that.giftGroupId) return false;
        return giftId == that.giftId;
    }

    @Override
    public int hashCode() {
        int result = giftGroupId;
        result = 31 * result + giftId;
        return result;
    }
}
