package com.stars.modules.fashioncard.prodata;

import com.stars.core.attr.Attribute;
import com.stars.modules.fashioncard.FashionCardManager;
import com.stars.modules.fashioncard.effect.FashionCardEffect;
import com.stars.util.StringUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-10-13.
 */
public class FashionCard {
    private int fashionCardId;
    private String icon;
    private int itemId;
    private String name;
    private String desc;
    private int order;
    private int resource;
    private String action;
    private String bodyEffect;
    private String actionEffect;
    private String specialEffect;
    private String specialEffectDesc;
    private int showExpressId;
    private String attr;

    private Attribute attribute = new Attribute();

    private Map<Integer, FashionCardEffect> effectMap = new HashMap<>();

    public int getFashionCardId() {
        return fashionCardId;
    }

    public void setFashionCardId(int fashionCardId) {
        this.fashionCardId = fashionCardId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getBodyEffect() {
        return bodyEffect;
    }

    public void setBodyEffect(String bodyEffect) {
        this.bodyEffect = bodyEffect;
    }

    public String getActionEffect() {
        return actionEffect;
    }

    public void setActionEffect(String actionEffect) {
        this.actionEffect = actionEffect;
    }

    public String getSpecialEffect() {
        return specialEffect;
    }

    public void setSpecialEffect(String specialEffect) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        this.specialEffect = specialEffect;
        if (specialEffect == null || specialEffect.equals("0") || specialEffect.equals("")) return;
        Map<Integer, String> tmpMap = StringUtil.toMap(specialEffect, Integer.class, String.class, '|', ';');
        for (Map.Entry<Integer, String> entry : tmpMap.entrySet()) {
            Class<? extends FashionCardEffect> clazz = FashionCardManager.getEffect(entry.getKey());
            if (clazz != null) {
                FashionCardEffect effect = clazz.getConstructor(FashionCard.class).newInstance(this);
                effect.parseData(entry.getValue());
                this.effectMap.put(entry.getKey(), effect);
            }
        }
    }

    public String getSpecialEffectDesc() {
        return specialEffectDesc;
    }

    public void setSpecialEffectDesc(String specialEffectDesc) {
        this.specialEffectDesc = specialEffectDesc;
    }

    public int getShowExpressId() {
        return showExpressId;
    }

    public void setShowExpressId(int showExpressId) {
        this.showExpressId = showExpressId;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
        if (attr == null || attr.equals("") || attr.equals("0")) return;
        Map<String, Integer> attrMap = StringUtil.toMap(attr, String.class, Integer.class, '=', ',');
        for (Map.Entry<String, Integer> entry : attrMap.entrySet()) {
            attribute.setSingleAttr(entry.getKey(), entry.getValue());
        }
    }

    public Map<Integer, FashionCardEffect> getEffectMap() {
        return effectMap;
    }

    public Attribute getAttribute() {
        return attribute;
    }
}
