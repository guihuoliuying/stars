package com.stars.modules.soul.prodata;

import com.stars.core.attr.Attribute;
import com.stars.core.module.Module;
import com.stars.modules.soul.SoulManager;
import com.stars.modules.soul.limit.AbstractLimit;
import com.stars.modules.soul.limit.LimitFactory;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by huwenjun on 2017/11/15.
 */
public class SoulStage {
    private int stage;// '元神阶级',
    private String name;// '元神阶级名称',
    private String attr;// '突破属性',
    private String reqItem;// '突破消耗',
    private String reqLimit;// '升级条件',
    private String resource;// '元神展示资源',

    private Attribute attribute = new Attribute();
    private Integer minLevel;
    private Integer maxLevel;
    private Map<Integer, Integer> reqItemMap = new HashMap<>();
    private List<AbstractLimit> abstractLimits = new ArrayList<>();

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public SoulStage getNextStage() {
        return SoulManager.soulStageMap.get(stage + 1);
    }

    public Map<Integer, Integer> getReqItemMap() {
        return reqItemMap;
    }

    public Attribute getAttribute() {
        return attribute;
    }
    public boolean isLimit(Map<String, Module> moduleMap, boolean sendTips) {
        for (AbstractLimit abstractLimit : abstractLimits) {
            if (abstractLimit.limit(moduleMap, sendTips, AbstractLimit.TYPE_BREAK)) {
                return true;
            }
        }
        return false;
    }
    public void handleMaxMinLevel() {
        Map<Integer, SoulLevel> soulTypeMap = SoulManager.soulStageLevelMap.get(stage).get(1);
        Set<Integer> levelSet = soulTypeMap.keySet();
        List<Integer> levels = new ArrayList<Integer>(levelSet);
        Collections.sort(levels);
        minLevel = levels.get(0);
        maxLevel = levels.get(levels.size() - 1);
    }

    public Integer getMinLevel() {
        if (minLevel == null) {
            handleMaxMinLevel();
        }
        return minLevel;
    }

    public Integer getMaxLevel() {
        if (maxLevel == null) {
            handleMaxMinLevel();
        }
        return maxLevel;
    }
}
