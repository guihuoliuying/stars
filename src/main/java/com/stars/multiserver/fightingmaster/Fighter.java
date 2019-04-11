package com.stars.multiserver.fightingmaster;


import com.stars.modules.scene.fightdata.FighterEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouyaohui on 2016/11/7.
 */
public class Fighter implements Matchable {

    private FighterEntity charactor;
    private String familyName = "";
    private List<FighterEntity> otherEntities = new ArrayList<>();
    private int serverId;
    private String fightId;

    // 真人匹配采用客户端协助，如果一开始没有匹配到，服务端不做轮询，客户端自己主动请求匹配
    private int personMatchBegin;   // 匹配真人的开始时间，之后客户端每三秒请求一次匹配
    private int lastpersonMatch;    // 客户端上一次请求匹配真人的时间

    public int getPersonMatchBegin() {
        return personMatchBegin;
    }

    public void setPersonMatchBegin(int personMatchBegin) {
        this.personMatchBegin = personMatchBegin;
    }

    public int getLastpersonMatch() {
        return lastpersonMatch;
    }

    public void setLastpersonMatch(int lastpersonMatch) {
        this.lastpersonMatch = lastpersonMatch;
    }

    /**
     * 获取匹配积分
     * @param fightFactor
     * @param seqFactor
     * @return
     */
    public int getMatchScore(int fightFactor, int seqFactor) {
        return 0;
    }

    @Override
    public int compare(Matchable other) {
        if (other instanceof Robot) {
            Robot robot = (Robot) other;
            return charactor.getFightScore() - robot.getCharactor().getFightScore();
        }
        if (other instanceof Fighter) {
            return 0;
        }
        return 0;
    }

    public List<FighterEntity> getOtherEntities() {
        return otherEntities;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getFightId() {
        return fightId;
    }

    public void setFightId(String fightId) {
        this.fightId = fightId;
    }

    public void addOtherEntity(FighterEntity entity) {
        otherEntities.add(entity);
    }

    public FighterEntity getCharactor() {
        return charactor;
    }

    public void setCharactor(FighterEntity entry) {
        this.charactor = entry;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getRoleId() {
        return charactor.getUniqueId();
    }

    public long getRoleIdLong() {
        return Long.valueOf(getRoleId());
    }

}
