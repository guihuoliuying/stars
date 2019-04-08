package com.stars.multiserver.fight.data;

import java.util.HashMap;

/**
 * Created by daiyaorong on 2016/10/17.
 */
public class LuaFrameData {

    private HashMap<String, String> dead;//每帧死亡角色ID列表 key:死亡角色ID value:杀手角色ID
    private HashMap<String, HashMap<String, Integer>> damage;//每帧伤害列表 key:目标角色ID value:记录表(key:攻击者ID value:伤害值)
    private boolean fighttimeout;   // 是否战斗时间到(有时间限制的战斗，在限制时间内未分出胜负时，会将该变量标记为true)
    private HashMap<String, String> hpInfo;
    private HashMap<String, String> deadPos;    // 死亡的位置列表 key:死亡角色ID value:位置字符串"x+y+z"

    private String cargoPosition;   // 运镖专用：镖车实时位置，字符串"x+y+z"
    private HashMap<String, Integer> exp;//阵营日常经验需要

    public HashMap<String, String> getDead() {
        return dead;
    }

    public void setDead(HashMap<String, String> dead) {
        this.dead = dead;
    }

    public HashMap<String, HashMap<String, Integer>> getDamage() {
        return damage;
    }

    public void setDamage(HashMap<String, HashMap<String, Integer>> damage) {
        this.damage = damage;
    }

    public HashMap<String, String> getHpInfo() {
        return hpInfo;
    }

    public void setHpInfo(HashMap<String, String> hpInfo) {
        this.hpInfo = hpInfo;
    }

    public void setFighttimeout(boolean fighttimeout) {
        this.fighttimeout = fighttimeout;
    }

    public boolean getFighttimeout() {
        return fighttimeout;
    }

    public void setDeadPos(HashMap<String, String> deadPos) {
        this.deadPos = deadPos;
    }

    public HashMap<String, String> getDeadPos() {
        return this.deadPos;
    }

    public void setCargoPosition(String posStr) {
        this.cargoPosition = posStr;
    }

    public String getCargoPosition() {
        return this.cargoPosition;
    }

    public HashMap<String, Integer> getExp() {
        return exp;
    }

    public void setExp(HashMap<String, Integer> exp) {
        this.exp = exp;
    }
}
