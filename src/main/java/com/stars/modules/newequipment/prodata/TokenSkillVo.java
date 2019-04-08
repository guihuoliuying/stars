package com.stars.modules.newequipment.prodata;

import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/6/8.
 */
public class TokenSkillVo {
    private int tokenSkillId;
    private String active;
    private int effect;
    private String resolve;
    private Map<Integer,Integer> resolveMap;

    private int activeTokenId;
    private int activeTokenLevel;

    public int getTokenSkillId() {
        return tokenSkillId;
    }

    public void setTokenSkillId(int tokenSkillId) {
        this.tokenSkillId = tokenSkillId;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
        String[] array = active.split("\\+");
        this.activeTokenId = Integer.parseInt(array[0]);
        this.activeTokenLevel = Integer.parseInt(array[1]);
    }

    public int getActiveTokenId() {
        return activeTokenId;
    }

    public void setActiveTokenId(int activeTokenId) {
        this.activeTokenId = activeTokenId;
    }

    public int getActiveTokenLevel() {
        return activeTokenLevel;
    }

    public void setActiveTokenLevel(int activeTokenLevel) {
        this.activeTokenLevel = activeTokenLevel;
    }

    public int getEffect() {
        return effect;
    }

    public void setEffect(int effect) {
        this.effect = effect;
    }

    public String getResolve() {
        return resolve;
    }

    public void setResolve(String resolve) {
        resolveMap = new HashMap<>();
        this.resolve = resolve;
        if(StringUtil.isEmpty(resolve)||resolve.equals("0"))
            return;
        this.resolveMap = StringUtil.toMap(resolve,Integer.class,Integer.class,'+',',');
    }

    public Map<Integer, Integer> getResolveMap() {
        return resolveMap;
    }

    public void setResolveMap(Map<Integer, Integer> resolveMap) {
        this.resolveMap = resolveMap;
    }

    public boolean isActive(int currentTotalTokenLevel){
        return  currentTotalTokenLevel >= activeTokenLevel;
    }
}
