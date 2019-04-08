package com.stars.modules.newequipment.prodata;

import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/6/8.
 */
public class TokenLevelVo {
    private int tokenId;
    private int level;
    private int minLevel;
    private int tokenFight;
    private String material; //格式为：itemid+数量，itemid+数量
    private String resolve; //格式为：itemid+数量，itemid+数量
    private String transferBack; //格式为：itemid+数量，itemid+数量
    private Map<Integer, Integer> materialMap;
    private Map<Integer, Integer> resolveMap;
    private Map<Integer, Integer> transferBackMap;


    public int getTokenId() {
        return tokenId;
    }

    public void setTokenId(int tokenId) {
        this.tokenId = tokenId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public int getTokenFight() {
        return tokenFight;
    }

    public void setTokenFight(int tokenFight) {
        this.tokenFight = tokenFight;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
        this.materialMap = new HashMap<>();
        if (StringUtil.isNotEmpty(material) && !material.equals("0")) {
            this.materialMap = StringUtil.toMap(material, Integer.class, Integer.class, '+', ',');
        }

    }

    public String getResolve() {
        return resolve;
    }

    public void setResolve(String resolve) {
        this.resolve = resolve;
        this.resolveMap = new HashMap<>();
        if (StringUtil.isNotEmpty(resolve) && !resolve.equals("0")) {
            resolveMap = StringUtil.toMap(resolve, Integer.class, Integer.class, '+', ',');
        }
    }

    public String getTransferBack() {
        return transferBack;
    }

    public void setTransferBack(String transferBack) {
        this.transferBack = transferBack;
        this.transferBackMap = new HashMap<>();
        if (StringUtil.isNotEmpty(transferBack) && !transferBack.equals("0")) {
            this.transferBackMap = StringUtil.toMap(transferBack, Integer.class, Integer.class, '+', ',');
        }
    }

    public Map<Integer, Integer> getMaterialMap() {
        return materialMap;
    }

    public Map<Integer, Integer> getResolveMap() {
        return resolveMap;
    }

    public Map<Integer, Integer> getTransferBackMap() {
        return transferBackMap;
    }
}
