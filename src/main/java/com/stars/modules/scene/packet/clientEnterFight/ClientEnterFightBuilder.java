package com.stars.modules.scene.packet.clientEnterFight;

import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.prodata.DynamicBlock;
import com.stars.modules.scene.prodata.MonsterVo;

import java.util.Collection;
import java.util.Map;

/**
 * Created by chenkeyu on 2016/11/23.
 */
public class ClientEnterFightBuilder {
    private ClientEnterFight clientEnterFight;
    public ClientEnterFightBuilder(ClientEnterFight clientEnterFight){
        this.clientEnterFight = clientEnterFight;
    }

    public ClientEnterFightBuilder setIsAgain(byte isAgain) {
        clientEnterFight.setIsAgain(isAgain);
        return this;
    }

    public ClientEnterFightBuilder setFightType(byte fightType) {
        clientEnterFight.setFightType(fightType);
        return this;
    }

    public ClientEnterFightBuilder setStageId(int stageId) {
        clientEnterFight.setStageId(stageId);
        return this;
    }

    public ClientEnterFightBuilder setRandomSeed(Integer randomSeed) {
        clientEnterFight.setRandomSeed(randomSeed);
        return this;
    }

    public ClientEnterFightBuilder setFighterEntityList(Collection<FighterEntity> fighterEntityList) {
        clientEnterFight.setFighterEntityList(fighterEntityList);
        return this;
    }
    public ClientEnterFightBuilder setBlockMap(Map<String, DynamicBlock> blockMap) {
        clientEnterFight.setBlockMap(blockMap);
        return this;
    }
    public ClientEnterFightBuilder addMonsterVoMap(Map<Integer, MonsterVo> monsterVoMap){
        clientEnterFight.addMonsterVoMap(monsterVoMap);
        return this;
    }
    public ClientEnterFightBuilder addMonsterVoMap(MonsterVo monsterVo){
        clientEnterFight.addMonsterVoMap(monsterVo);
        return this;
    }
    public ClientEnterFightBuilder addSkillData(Map<Integer, Integer> skillMap){
        clientEnterFight.addSkillData(skillMap);
        return this;
    }
    public ClientEnterFightBuilder addBuffData(Map<Integer, Integer> buffIdLevelMap){
        clientEnterFight.addBuffData(buffIdLevelMap);
        return this;
    }
    public ClientEnterFightBuilder addBlockStatusMap(Map<String, Byte> blockStatusMap){
        clientEnterFight.addBlockStatusMap(blockStatusMap);
        return this;
    }

}
