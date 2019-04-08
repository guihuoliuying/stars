package com.stars.modules.dragonboat.prodata;

/**
 * Created by huwenjun on 2017/5/9.
 */
public class DragonBoatVo implements Comparable<DragonBoatVo> {
    private Integer dragonBoatId;
    private String name;
    private String stayEffect;
    private String speedEffect;
    private String finishEffect;
    private String img;
    private Integer order;

    public Integer getDragonBoatId() {
        return dragonBoatId;
    }

    public void setDragonBoatId(Integer dragonBoatId) {
        this.dragonBoatId = dragonBoatId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStayEffect() {
        return stayEffect;
    }

    public void setStayEffect(String stayEffect) {
        this.stayEffect = stayEffect;
    }

    public String getSpeedEffect() {
        return speedEffect;
    }

    public void setSpeedEffect(String speedEffect) {
        this.speedEffect = speedEffect;
    }

    public String getFinishEffect() {
        return finishEffect;
    }

    public void setFinishEffect(String finishEffect) {
        this.finishEffect = finishEffect;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    @Override
    public int compareTo(DragonBoatVo o) {
        if (!this.order.equals(o.getOrder())) {
            return this.order - o.getOrder();
        }
        return this.getDragonBoatId() - o.getDragonBoatId();
    }
}
