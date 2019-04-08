package com.stars.multiserver.fightingmaster;

import com.stars.modules.fightingmaster.FightingMasterManager;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterPK;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.fightingmaster.data.RoleFightingMaster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouyaohui on 2016/11/7.
 */
public class Fighter implements Matchable {

    private FighterEntity charactor;
    private String familyName = "";
    private List<FighterEntity> otherEntities = new ArrayList<>();
    private int serverId;
    private String fightId;
    private RoleFightingMaster roleFightingMaster;

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
        if (roleFightingMaster.isSequenceWin()) {
            return getRoleFightingMaster().getDisScore() + getCharactor().getFightScore() / fightFactor + seqFactor * roleFightingMaster.sequenceCount();
        } else {
            return getRoleFightingMaster().getDisScore() + getCharactor().getFightScore() / fightFactor - seqFactor * roleFightingMaster.sequenceCount();
        }
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

    public RoleFightingMaster getRoleFightingMaster() {
        return roleFightingMaster;
    }

    public void setRoleFightingMaster(RoleFightingMaster roleFightingMaster) {
        this.roleFightingMaster = roleFightingMaster;
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

    public void noticClientEntry(NoticeClientEnter notice) {
        this.fightId = notice.getFightId();
        ClientEnterPK enterPack = new ClientEnterPK();
        enterPack.setFightType(SceneManager.SCENETYPE_FIGHTINGMASTER);
        enterPack.setStageId(FightingMasterManager.stageId);
        StageinfoVo info = SceneManager.getStageVo(FightingMasterManager.stageId);
        enterPack.setBlockMap(info.getDynamicBlockMap());
        // 限制时间
        enterPack.setLimitTime(FightingMasterManager.fighttime);
        List<FighterEntity> list = new ArrayList<>();
        FighterEntity copy = charactor.copy();
        copy.setFighterType(FighterEntity.TYPE_SELF);
        list.add(copy);
        for (FighterEntity buddy : getOtherEntities()) {
            copy = buddy.copy();
            copy.setCamp(charactor.getCamp());
            list.add(copy);
        }
        enterPack.setFighterEntityList(list);

        Map<Integer, Integer> skillMap = new HashMap<>();
        for (String skillStr : notice.getSkillList()) {
            String[] skill = skillStr.split("[_]");
            int skillId = Integer.valueOf(skill[0]);
            int skillLevel = Integer.valueOf(skill[1]);
            skillMap.put(skillId, skillLevel);
            enterPack.addSkillData(skillMap);   // 这里一个一个加，因为两个角色技能可能重复但是等级不一样，用map存不下
            skillMap.clear();
        }
        PacketManager.send(Long.valueOf(getRoleId()), enterPack);
    }
}
