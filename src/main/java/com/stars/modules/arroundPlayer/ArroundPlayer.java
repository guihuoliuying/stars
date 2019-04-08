package com.stars.modules.arroundPlayer;

import java.util.ArrayList;
import java.util.List;

public class ArroundPlayer {

    //组装场景id
    private String sceneId;
    //原始场景id
    private String originSceneId;

    private String arroundId;

    private byte sceneType;

    private long roleId;

    private String name;

    private int job;

    private short level;

    private int activeRideId;

    private int x;

    private int y;

    private int z;

    private int fightScore;// 当前战力

    private int curFashionId;//角色穿着的时装id，若没穿，则为-1

    private byte deityweaponType;//神兵类型;

    private int curTitleId;// 角色装上的称号Id,没有则发0
    private int cutVipLevel;// 角色当前vip等级

    private String familyId;

    private List<String> dragonBallList = new ArrayList<>();
    private byte babyFollow;//1跟随,0:不跟随

    private int babyCurFashionId;

    private int curFashionCardId;

    public ArroundPlayer() {

    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getJob() {
        return job;
    }

    public void setJob(int job) {
        this.job = job;
    }

    public short getLevel() {
        return level;
    }

    public void setLevel(short level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getSceneId() {
        return sceneId;
    }

    public void setSceneId(String sceneId) {
        this.sceneId = sceneId;
    }

    public int getActiveRideId() {
        return activeRideId;
    }

    public void setActiveRideId(int activeRideId) {
        this.activeRideId = activeRideId;
    }

    public int getCurFashionId() {
        return curFashionId;
    }

    public void setCurFashionId(int curFashionId) {
        this.curFashionId = curFashionId;
    }

    public byte getDeityweaponType() {
        return deityweaponType;
    }

    public void setDeityweaponType(byte deityweaponType) {
        this.deityweaponType = deityweaponType;
    }

    public int getCurTitleId() {
        return curTitleId;
    }

    public void setCurTitleId(int curTitleId) {
        this.curTitleId = curTitleId;
    }

    public int getCutVipLevel() {
        return cutVipLevel;
    }

    public void setCutVipLevel(int cutVipLevel) {
        this.cutVipLevel = cutVipLevel;
    }

    public String getArroundId() {
        return this.arroundId;
    }

    public void setArroundId(String arroundId) {
        this.arroundId = arroundId;
    }

    public byte getSceneType() {
        return this.sceneType;
    }

    public void setSceneType(byte sceneType) {
        this.sceneType = sceneType;
    }

    public String getFamilyId() {
        return familyId;
    }

    public void setFamilyId(String familyId) {
        this.familyId = familyId;
    }

    public String getOriginSceneId() {
        return originSceneId;
    }

    public void setOriginSceneId(String originSceneId) {
        this.originSceneId = originSceneId;
    }

    public int getFightScore() {
        return fightScore;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public List<String> getDragonBallList() {
        return dragonBallList;
    }

    public void setDragonBallList(List<String> dragonBallList) {
        this.dragonBallList = dragonBallList;
    }

    public byte getBabyFollow() {
        return babyFollow;
    }

    public void setBabyFollow(byte babyFollow) {
        this.babyFollow = babyFollow;
    }

    public int getBabyCurFashionId() {
        return babyCurFashionId;
    }

    public void setBabyCurFashionId(int babyCurFashionId) {
        this.babyCurFashionId = babyCurFashionId;
    }

    public int getCurFashionCardId() {
        return curFashionCardId;
    }

    public void setCurFashionCardId(int curFashionCardId) {
        this.curFashionCardId = curFashionCardId;
    }
}
