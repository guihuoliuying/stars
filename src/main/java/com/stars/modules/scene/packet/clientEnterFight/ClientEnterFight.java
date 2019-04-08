package com.stars.modules.scene.packet.clientEnterFight;

import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.prodata.BuffVo;
import com.stars.modules.scene.prodata.DynamicBlock;
import com.stars.modules.scene.prodata.MonsterVo;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.skill.prodata.SkillVo;
import com.stars.modules.skill.prodata.SkillvupVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.util.LogUtil;

import java.util.*;

/**
 * Created by liuyuheng on 2016/8/29.
 */
public class ClientEnterFight extends Packet {
    private byte isAgain;// 是否重复进入

    private byte fightType;// 战斗类型
    private int stageId;
    private Integer randomSeed;//随机种子
    private Collection<FighterEntity> fighterEntityList;// 战斗实体
    private Map<Integer, SkillVo> skillVoMap;// 预加载skill数据
    private Map<String, SkillvupVo> skillLevelVoMap;// 预加载技能等级数据
    private Set<BuffVo> buffSet;// 预加载BUFF数据
    private Map<String, DynamicBlock> blockMap;// 预加载动态阻挡数据
    private Map<String, Byte> blockStatusMap;// 动态阻挡状态
    private Map<Integer, MonsterVo> monsterVoMap;// 预加载怪物模型使用数据
    private int dungeonId;

    @Override
    public short getType() {
        return ScenePacketSet.C_ENTERFIGHT;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        writeBase(buff);
    }

    @Override
    public void readFromBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }

    public void writeBase(com.stars.network.server.buffer.NewByteBuffer buff) {
        if (fightType == SceneManager.SCENETYPE_NEWOFFLINEPVP) {
            isAgain = 0;
        }
        buff.writeByte(isAgain);
        buff.writeByte(fightType);
        if (fightType == SceneManager.SCENETYPE_DUNGEON) {
            buff.writeInt(dungeonId);
        }
        buff.writeInt(stageId);
        buff.writeInt(SceneManager.getStageVo(stageId).getRebornTime());
        writeFightEntity(buff);
        writeDynamicBlock(buff);
        if (isAgain == 0) {
            writeMonsterVo(buff);
        }
        writeRandomSeed(buff);
    }

    private void writeFightEntity(com.stars.network.server.buffer.NewByteBuffer buff) {
        byte size = (byte) (fighterEntityList == null ? 0 : fighterEntityList.size());
        buff.writeByte(size);
        if (size > 0) {
            for (FighterEntity entity : fighterEntityList) {
                entity.writeToBuff(buff);
            }
        }
    }

    private void writeSkill(com.stars.network.server.buffer.NewByteBuffer buff) {
        short size = (short) (skillVoMap == null ? 0 : skillVoMap.size());
        buff.writeShort(size);
        if (size > 0) {
            for (SkillVo skillVo : skillVoMap.values()) {
                skillVo.writeToBuffer(buff);
            }
        }
        size = (short) (skillLevelVoMap == null ? 0 : skillLevelVoMap.size());
        buff.writeShort(size);
        if (size > 0) {
            for (SkillvupVo lvupVo : skillLevelVoMap.values()) {
                writeToPSkillBuffer(buff, lvupVo);
            }
        }
    }

    /**
     * 下发技能等级数据 战斗中使用
     */
    private void writeToPSkillBuffer(com.stars.network.server.buffer.NewByteBuffer buff, SkillvupVo pskillVo) {
        pskillVo.writeToBuffer(buff);
    }

    private void writeBuff(com.stars.network.server.buffer.NewByteBuffer buff) {
        short size = (short) (buffSet == null ? 0 : buffSet.size());
        buff.writeShort(size);
        if (size > 0) {
            for (BuffVo buffVo : buffSet) {
                buffVo.writeToBuff(buff);
            }
        }
    }

    private void writeDynamicBlock(com.stars.network.server.buffer.NewByteBuffer buff) {
        short size = (short) (blockMap == null ? 0 : blockMap.size());
        buff.writeShort(size);
        if (blockMap != null) {
            for (DynamicBlock dynamicBlock : blockMap.values()) {
                dynamicBlock.writeToBuff(buff);
                buff.writeByte(blockStatusMap.get(dynamicBlock.getUnniqueId()));
            }
        }
    }

    private void writeMonsterVo(com.stars.network.server.buffer.NewByteBuffer buff) {
        short size = (short) (monsterVoMap == null ? 0 : monsterVoMap.size());
        buff.writeShort(size);
        if (monsterVoMap != null) {
            for (MonsterVo monsterVo : monsterVoMap.values()) {
                monsterVo.writeToBuff(buff);
            }
        }
    }

    private void writeRandomSeed(NewByteBuffer buff) {
        Long timeStamp = System.currentTimeMillis();
        this.randomSeed = timeStamp.intValue();
        buff.writeInt(this.randomSeed);
    }

    public void setIsAgain(byte isAgain) {
        this.isAgain = isAgain;
    }

    public void setFightType(byte fightType) {
        this.fightType = fightType;
    }

    public byte getFightType() {
        return fightType;
    }

    public void setStageId(int stageId) {
        this.stageId = stageId;
    }

    public int getStageId() {
        return stageId;
    }

    public Integer getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(Integer randomSeed) {
        this.randomSeed = randomSeed;
    }

    public void setFighterEntityList(Collection<FighterEntity> fighterEntityList) {
        this.fighterEntityList = fighterEntityList;
        if (fighterEntityList.isEmpty())
            return;
        parseModelAndSkillAndBuff();
    }

    /**
     * 注入Fighter时,解析Fighter,注入Model/Skill/Buff相关
     */
    private void parseModelAndSkillAndBuff() {
        if (monsterVoMap == null)
            monsterVoMap = new HashMap<>();
        for (FighterEntity entity : fighterEntityList) {
            // monsterVo模型相关数据预加载,目前怪物/伙伴需要
            if (entity.getFighterType() == FighterEntity.TYPE_MONSTER || entity.getFighterType() == FighterEntity.TYPE_BUDDY) {
                addMonsterVoMap(SceneManager.getMonsterVo(entity.getModelId()));
            }
            addSkillData(entity.getSkills());
        }
    }

    public void setBlockMap(Map<String, DynamicBlock> blockMap) {
        this.blockMap = blockMap;
    }

    public void addBlockStatusMap(Map<String, Byte> blockStatusMap) {
        if (this.blockStatusMap == null)
            this.blockStatusMap = new HashMap<>();
        this.blockStatusMap.putAll(blockStatusMap);
    }

    public Set<BuffVo> getBuff() {
        return buffSet;
    }

    public Map<Integer, MonsterVo> getMonsterVoMap() {
        return monsterVoMap;
    }

    public Map<Integer, SkillVo> getSkillMap() {
        return skillVoMap;
    }

    public Collection<FighterEntity> getFighterEntityList() {
        return fighterEntityList;
    }

    public void addMonsterVoMap(Map<Integer, MonsterVo> monsterVoMap) {
        if (this.monsterVoMap == null)
            this.monsterVoMap = new HashMap<>();
        this.monsterVoMap.putAll(monsterVoMap);
        for (MonsterVo monsterVo : monsterVoMap.values()) {
            addSkillData(monsterVo.getSkillMap());
        }
    }

    public void addMonsterVoMap(MonsterVo monsterVo) {
        Map<Integer, MonsterVo> map = new HashMap<>();
        map.put(monsterVo.getId(), monsterVo);
        addMonsterVoMap(map);
    }

    public void addSkillData(Map<Integer, Integer> skillMap) {
        if (skillVoMap == null)
            skillVoMap = new HashMap<>();
        if (skillLevelVoMap == null)
            skillLevelVoMap = new HashMap<>();
        if (buffSet == null)
            buffSet = new HashSet<>();
        for (Map.Entry<Integer, Integer> entry : skillMap.entrySet()) {
            SkillVo skillVo = SkillManager.getSkillVo(entry.getKey());
            if (skillVo != null) {
                skillVoMap.put(entry.getKey(), skillVo);
                for (int buffId : skillVo.getBuffIdSet()) {
                    BuffVo buffVo = SceneManager.getBuffVo(buffId, entry.getValue());
                    if (buffVo == null) {
                        // buffVo
                        com.stars.util.LogUtil.error("找不到buffId={},level={}的buff数据", buffId, entry.getValue(),
                                new IllegalArgumentException());
                        continue;
                    }
                    buffSet.add(buffVo);
                }
            }
            // skillLevelVo
            SkillvupVo skillvupVo = SkillManager.getSkillvupVo(entry.getKey(), entry.getValue());
            if (skillvupVo != null) {
                skillLevelVoMap.put("" + skillvupVo.getSkillId() + skillvupVo.getLevel(), skillvupVo);
                BuffVo buffVo = SceneManager.getBuffVo(skillvupVo.getBuffId(), skillvupVo.getLevel());
                if (buffVo != null) {
                    buffSet.add(buffVo);
                }
            }
        }
    }

    public void addBuffData(Map<Integer, Integer> buffIdLevelMap) {
        if (buffSet == null)
            buffSet = new HashSet<>();
        int buffId;
        int buffLevel;
        for (Map.Entry<Integer, Integer> kvp : buffIdLevelMap.entrySet()) {
            buffId = kvp.getKey();
            buffLevel = kvp.getValue();
            BuffVo buffVo = SceneManager.getBuffVo(buffId, buffLevel);
            if (buffVo == null) {
                LogUtil.error("找不到buffId={},level={}的buff数据", buffId, buffLevel,
                        new IllegalArgumentException());
                continue;
            }
            buffSet.add(buffVo);
        }
    }

    public void setSkillVoMap(Map<Integer, SkillVo> skillVoMap) {
        this.skillVoMap = skillVoMap;
    }

    public int getDungeonId() {
        return dungeonId;
    }

    public void setDungeonId(int dungeonId) {
        this.dungeonId = dungeonId;
    }
}
