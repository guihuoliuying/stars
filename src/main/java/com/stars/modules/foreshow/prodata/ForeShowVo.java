package com.stars.modules.foreshow.prodata;

import com.stars.util.StringUtil;

/**
 * Created by chenkeyu on 2016/10/28.
 */
public class ForeShowVo {
    private int openid;
    private String name;
    private String nametext;
    private String icon;
    private int flyposition;
    private String entericon;
    private String openlimit;
    private String opentips;
    private int windowtips;
    private String openinduct;
    private int foreshowserial;
    private String foreshowtext;
    private int isall;
    private String forecastcondition;
    private boolean noForecastCondition;
    private int conditionLevel;
    private int conditionDungeonId;
    private String forecasteffects;
    private boolean noEffect;
    private int effectLevel;
    private int effectDungeonId;
    private String effect;
    private String conditiondesc;

    public String getConditiondesc() {
        return conditiondesc;
    }

    public void setConditiondesc(String conditiondesc) {
        this.conditiondesc = conditiondesc;
    }

    public int getFlyposition() {
        return flyposition;
    }

    public void setFlyposition(int flyposition) {
        this.flyposition = flyposition;
    }

    public int getForeshowserial() {
        return foreshowserial;
    }

    public void setForeshowserial(int foreshowserial) {
        this.foreshowserial = foreshowserial;
    }

    public String getForeshowtext() {
        return foreshowtext;
    }

    public void setForeshowtext(String foreshowtext) {
        this.foreshowtext = foreshowtext;
    }


    public int getOpenid() {
        return openid;
    }

    public void setOpenid(int openid) {
        this.openid = openid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNametext() {
        return nametext;
    }

    public void setNametext(String nametext) {
        this.nametext = nametext;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getEntericon() {
        return entericon;
    }

    public void setEntericon(String entericon) {
        this.entericon = entericon;
    }

    public String getOpenlimit() {
        return openlimit;
    }

    public void setOpenlimit(String openlimit) {
        this.openlimit = openlimit;
    }

    public String getOpentips() {
        return opentips;
    }

    public void setOpentips(String opentips) {
        this.opentips = opentips;
    }

    public int getWindowtips() {
        return windowtips;
    }

    public void setWindowtips(int windowtips) {
        this.windowtips = windowtips;
    }

    public String getOpeninduct() {
        return openinduct;
    }

    public void setOpeninduct(String openinduct) {
        this.openinduct = openinduct;
    }

    public int getIsall() {
        return isall;
    }

    public void setIsall(int isall) {
        this.isall = isall;
    }

    public String getForecastcondition() {
        return forecastcondition;
    }

    public void setForecastcondition(String forecastcondition) {
        this.forecastcondition = forecastcondition;
        if(StringUtil.isEmpty(forecastcondition) || forecastcondition.equals("0")) {
            this.noForecastCondition = true;
            return;
        }
        String[] arr = forecastcondition.split("\\+");
        this.conditionLevel = Integer.parseInt(arr[0]);
        this.conditionDungeonId = Integer.parseInt(arr[1]);
    }

    public String getForecasteffects() {
        return forecasteffects;
    }

    public void setForecasteffects(String forecasteffects) {
        this.forecasteffects = forecasteffects;
        if(StringUtil.isEmpty(forecasteffects) || forecasteffects.equals("0")) {
            noEffect = true;
            return;
        }
        String[] arr = forecasteffects.split("\\+");
        this.effectLevel = Integer.parseInt(arr[0]);
        this.effectDungeonId = Integer.parseInt(arr[1]);
        this.effect = arr[2];
    }

    public boolean isNoForecastCondition() {
        return noForecastCondition;
    }

    public int getConditionLevel() {
        return conditionLevel;
    }

    public int getConditionDungeonId() {
        return conditionDungeonId;
    }

    public int getEffectLevel() {
        return effectLevel;
    }

    public int getEffectDungeonId() {
        return effectDungeonId;
    }

    public String getEffect() {
        return effect;
    }

    public boolean isNoEffect() {
        return noEffect;
    }
}
