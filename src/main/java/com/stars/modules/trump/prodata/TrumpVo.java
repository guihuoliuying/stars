package com.stars.modules.trump.prodata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhouyaohui on 2016/9/23.
 */
public class TrumpVo {

    private int trumpId;        // 法宝id
    private int itemId;         // 法宝对应的物品id
    private String icon;        // 法宝图标
    private String name;        // 法宝名称
    private String desc;        // 法宝获取途径描述
    private byte order;          // 显示顺序
    private String resolve;     // 分解获得物品
    private String isShow;      // 显示条件
    private String skillUnlock; // 技能解锁条件

    private Map<Integer, Integer> resolveMap = new HashMap<>();
    private Set<Integer> skillSet = new HashSet<>();

    public int getTrumpId() {
        return trumpId;
    }

    public void setTrumpId(int trumpId) {
        this.trumpId = trumpId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
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

    public byte getOrder() {
        return order;
    }

    public void setOrder(byte order) {
        this.order = order;
    }

    public String getResolve() {
        return resolve;
    }

    public void setResolve(String resolve) {
        this.resolve = resolve;
        String[] arr = resolve.trim().split("\\|");
        for (String s : arr) {
            String[] item = s.split("\\+");
            resolveMap.put(Integer.parseInt(item[0]), Integer.parseInt(item[1]));
        }
    }

    public String getIsShow() {
        return isShow;
    }

    public void setIsShow(String isShow) {
        this.isShow = isShow;
    }

    public String getSkillUnlock() {
        return skillUnlock;
    }

    public void setSkillUnlock(String skillUnlock) {
        this.skillUnlock = skillUnlock;
        String[] arr = skillUnlock.trim().split("\\|");
        for (String s : arr) {
            String[] skill = s.split("\\+");
            if (skill.length != 3) {
                continue;
            }
            skillSet.add(Integer.valueOf(skill[0]));
        }
    }

    /** 便利接口 */

    /**
     * 返回分解物品,产品可能被多个线程访问，返回map的拷贝
     * @return
     */
    public Map<Integer, Integer> getResolveToolMap() {
        Map<Integer, Integer> map = new HashMap<>();
        map.putAll(resolveMap);
        return map;
    }

    /**
     * 法宝技能集合
     * @return
     */
    public Set<Integer> getSkillSet() {
        Set<Integer> set = new HashSet<>();
        set.addAll(skillSet);
        return set;
    }
}
