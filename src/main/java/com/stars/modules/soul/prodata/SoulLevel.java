package com.stars.modules.soul.prodata;

import com.stars.core.attr.Attribute;
import com.stars.core.module.Module;
import com.stars.modules.soul.SoulManager;
import com.stars.modules.soul.limit.AbstractLimit;
import com.stars.modules.soul.limit.LimitFactory;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/11/15.
 */
public class SoulLevel {
    private int soulGodStage;// '元神阶级',
    private int soulGodType;// '元神类型',
    private String typeName;//'元神部位名称',
    private int soulGodLevel;// '元神等级',
    private String attr;// '属性',
    private String reqItem;// '消耗',
    private String reqLimit;//'升级条件',
    private String resource;// '元神展示资源',

    private Attribute attribute = new Attribute();
    private List<AbstractLimit> abstractLimits = new ArrayList<>();
    private Map<Integer, Integer> reqItemMap = new HashMap<>();

    public int getSoulGodStage() {
        return soulGodStage;
    }

    public void setSoulGodStage(int soulGodStage) {
        this.soulGodStage = soulGodStage;
    }

    public int getSoulGodType() {
        return soulGodType;
    }

    public void setSoulGodType(int soulGodType) {
        this.soulGodType = soulGodType;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getSoulGodLevel() {
        return soulGodLevel;
    }

    public void setSoulGodLevel(int soulGodLevel) {
        this.soulGodLevel = soulGodLevel;
    }


    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
        attribute = new Attribute(attr);
    }

    public String getReqItem() {
        return reqItem;
    }

    public void setReqItem(String reqItem) {
        this.reqItem = reqItem;
        reqItemMap = StringUtil.toMap(reqItem, Integer.class, Integer.class, '+', '&');
    }

    public String getReqLimit() {
        return reqLimit;
    }

    public void setReqLimit(String reqLimit) {
        this.reqLimit = reqLimit;
        abstractLimits = LimitFactory.parseLimit(reqLimit);

    }

    public boolean isLimit(Map<String, Module> moduleMap, boolean sendTips) {
        for (AbstractLimit abstractLimit : abstractLimits) {
            if (abstractLimit.limit(moduleMap,sendTips,AbstractLimit.TYPE_UPGRADE)) {
                return true;
            }
        }
        return false;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public Map<Integer, Integer> getReqItemMap() {
        return reqItemMap;
    }

    /**
     * 获取下一级数据
     *
     * @return
     */
    public SoulLevel getNextSoulLevel(int roleStage) {
        SoulLevel nextSoulLevel = SoulManager.soulTypeMap.get(soulGodType).get(soulGodLevel + 1);
        if (nextSoulLevel == null || (nextSoulLevel != null && nextSoulLevel.getSoulGodStage() != roleStage)) {
            Map<Integer, SoulLevel> soulLvMap = SoulManager.soulTypeMap.get(soulGodType + 1);
            if (soulLvMap == null) {
                /**
                 * 需要突破
                 */
                return null;
            } else {
                SoulStage soulStage = SoulManager.soulStageMap.get(soulGodStage);
                Integer minLv = soulStage.getMinLevel();
                if(minLv==0){
                    minLv=1;
                }
                SoulLevel soulLevel = soulLvMap.get(minLv);
                return soulLevel;
            }
        } else {
            return nextSoulLevel;
        }
    }
}
