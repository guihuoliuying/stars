//package com.stars.modules.scene.packet;
//
//import com.stars.modules.buddy.userdata.RoleBuddy;
//import com.stars.modules.scene.ScenePacketSet;
//import com.stars.modules.scene.prodata.*;
//import com.stars.modules.searchtreasure.prodata.SearchStageVo;
//import com.stars.modules.skill.SkillModule;
//import com.stars.modules.skill.prodata.SkillVo;
//import com.stars.modules.skill.prodata.SkillvupVo;
//import com.stars.network.server.buffer.NewByteBuffer;
//import com.stars.network.server.packet.Packet;
//import com.stars.util.EmptyUtil;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
///**
// * Created by daiyaorong on 2016/6/20.
// */
//public class ClientEnterFight extends Packet {
//    private byte isAgain;// 是否重复进入
//    private byte playDrama;// 是否播放剧情 1=是;0=否
//
//    /**PVE副本的stage数据;*/
//    private StageinfoVo stageVo;
//
//    private Map<Integer, SkillVo> skillMap;// 技能数据(角色+怪物)
//    private Map<String, SkillvupVo> skillLevelMap;// 技能等级数据(角色+怪物)
//    private static Map<Integer, Fcd> defineMap;
//    private Map<Integer, MonsterVo> monsterVoMap;// 预加载怪物数据
//    private Map<String, DynamicBlock> blockMap;// 预加载动态阻挡数据
//    private List<MonsterSpawnVo> areaSpawnList;// 预加载坐标刷怪配置
//    private Map<String, BuffVo> buffMap;// 预加载BUFF数据
//    private Set<Integer> itemIdSet;// 怪物掉落物品IdSet,预加载掉落模型
//
//    private Map<String, Byte> blockStatusMap;// 动态阻挡状态
//    private Map<String, MonsterAttributeVo> spawnMonsterMap;// 进关卡初始刷怪数据
//    private List<String> waveMonsterTypeList; //波数怪物类型的信息列表;
//    private RoleBuddy roleFightBuddy;// 玩家出战伙伴
//    private SkillModule skillModule;
//
//    public ClientEnterFight() {
//    }
//
//    private void writeStageInfo(NewByteBuffer buff){
//        stageVo.writeToBuff(buff);
//    }
//
//    @Override
//    public void writeToBuffer(NewByteBuffer buff) {
//        buff.writeByte(isAgain);
//        buff.writeByte(playDrama);
//        if (isAgain == 0) {
//            writeStageInfo(buff);
//            writeSkill(buff);
//            writePSkill(buff);
//            writeFcd(buff);
//            writeMonsterProdData(buff);
//            writeDynamicBlock(buff);
//            writeAreaSpawn(buff);
//            writeSpawnMonster(buff);
//            writeBuffData(buff);
//            writeDropItemId(buff);
//            writeMonsterTypeCountInfo(buff);
//        } else {
//            writeSkill(buff);
//            writePSkill(buff);
//            writeDynamicBlock(buff);
//            writeBuffData(buff);
//            writeSpawnMonster(buff);
//            writeMonsterTypeCountInfo(buff);
//        }
//        writeFightBuddy(buff);
//    }
//
//    public void writeSkill(NewByteBuffer buff) {
//        if (EmptyUtil.isEmpty(skillMap)) {
//            buff.writeShort((short) 0);
//        } else {
//            buff.writeShort((short) skillMap.size());
//            for (SkillVo skillvo : skillMap.values()) {
//                skillvo.writeToBuffer(buff);
//            }
//        }
//    }
//
//    /**
//     * 下发技能等级数据 战斗中使用
//     */
//    private void writeToPSkillBuffer(NewByteBuffer buff, SkillvupVo pskillVo) {
//        buff.writeInt(pskillVo.getSkillId());
//        buff.writeInt(pskillVo.getLevel());
//        buff.writeString(pskillVo.getConditions());
//        buff.writeString(pskillVo.getEffectinfo());
//        buff.writeInt(pskillVo.getCoefficient());
//        //获取最终伤害值; Fixme 这个接口目前是怪物技能和玩家技能公用的,之后需要优化为玩家和怪物分开;
//        int damageValue = skillModule.getSkillTotalDamage(pskillVo.getSkillId());
//        if(damageValue>0){
//            buff.writeInt(damageValue);
//        }else{
//            buff.writeInt(pskillVo.getDamage());
//        }
//        buff.writeInt(pskillVo.getCooldown());
//    }
//
//    public void writePSkill(NewByteBuffer buff) {
//        if (EmptyUtil.isEmpty(skillLevelMap)) {
//            buff.writeShort((short) 0);
//        } else {
//            buff.writeShort((short) skillLevelMap.size());
//            for (SkillvupVo pskillvo : skillLevelMap.values()) {
//                writeToPSkillBuffer(buff, pskillvo);
//            }
//        }
//    }
//
//    public void writeFcd(NewByteBuffer buff) {
//        if (EmptyUtil.isEmpty(defineMap)) {
//            buff.writeShort((short) 0);
//        } else {
//            buff.writeShort((short) defineMap.size());
//            for (Fcd fcd : defineMap.values()) {
//                fcd.writeToBuffer(buff);
//            }
//        }
//    }
//
//    private void writeMonsterProdData(NewByteBuffer buff) {
//        short size = (short) (monsterVoMap == null ? 0 : monsterVoMap.size());
//        buff.writeShort(size);
//        if (monsterVoMap != null) {
//            for (MonsterVo monsterVo : monsterVoMap.values()) {
//                monsterVo.writeToBuff(buff);
//            }
//        }
//    }
//
//    private void writeDynamicBlock(NewByteBuffer buff) {
//        short size = (short) (blockMap == null ? 0 : blockMap.size());
//        buff.writeShort(size);
//        if (blockMap != null) {
//            for (DynamicBlock dynamicBlock : blockMap.values()) {
//                dynamicBlock.writeToBuff(buff);
//                buff.writeByte(blockStatusMap.get(dynamicBlock.getUnniqueId()));
//            }
//        }
//    }
//
//    private void writeAreaSpawn(NewByteBuffer buff) {
//        short size = (short) (areaSpawnList == null ? 0 : areaSpawnList.size());
//        buff.writeShort(size);
//        if (areaSpawnList != null) {
//            for (MonsterSpawnVo monsterSpawnVo : areaSpawnList) {
//                monsterSpawnVo.writeToBuff(buff);
//            }
//        }
//    }
//
//    private void writeSpawnMonster(NewByteBuffer buff) {
//        short size = (short) (spawnMonsterMap == null ? 0 : spawnMonsterMap.size());
//        buff.writeShort(size);
//        if (spawnMonsterMap != null) {
//            for (Map.Entry<String, MonsterAttributeVo> entry : spawnMonsterMap.entrySet()) {
//                buff.writeString(entry.getKey());
//                entry.getValue().writeSpawnDataToBuff(buff);
//            }
//        }
//    }
//
//    private void writeMonsterTypeCountInfo(NewByteBuffer buff) {
//        short size = (short) (waveMonsterTypeList == null ? 0 : waveMonsterTypeList.size());
//        buff.writeShort(size);
//        if (waveMonsterTypeList != null) {
//            for (int i = 0, len = waveMonsterTypeList.size(); i < len; i++) {
//                buff.writeString(waveMonsterTypeList.get(i));
//            }
//        }
//    }
//
//    private void writeBuffData(NewByteBuffer buff) {
//        short size = (short) (buffMap == null ? 0 : buffMap.size());
//        buff.writeShort(size);
//        if (buffMap != null) {
//            for (BuffVo buffVo : buffMap.values()) {
//                buffVo.writeToBuff(buff);
//            }
//        }
//    }
//
//    private void writeDropItemId(NewByteBuffer buff) {
//        byte size = (byte) (itemIdSet == null ? 0 : itemIdSet.size());
//        buff.writeByte(size);
//        for (int itemId : itemIdSet) {
//            buff.writeInt(itemId);
//        }
//    }
//
//    private void writeFightBuddy(NewByteBuffer buff) {
//        buff.writeByte((byte) (roleFightBuddy == null ? 0 : 1));// 0=没有出战伙伴;1=有出战伙伴
//        if (roleFightBuddy != null)
//            roleFightBuddy.writeFightBuddy(buff);
//    }
//
//    @Override
//    public void readFromBuffer(NewByteBuffer buff) {
//    }
//
//    @Override
//    public short getType() {
//        return ScenePacketSet.C_ENTERFIGHT;
//    }
//
//    @Override
//    public void execPacket() {
//
//    }
//
//    public Map<Integer, SkillVo> getSkillMap() {
//        return skillMap;
//    }
//
//    public void setSkillMap(Map<Integer, SkillVo> skillMap) {
//        this.skillMap = skillMap;
//    }
//
//    public Map<String, SkillvupVo> getPSkillMap() {
//        return skillLevelMap;
//    }
//
//    public void setPSkillMap(Map<String, SkillvupVo> skillLevelMap) {
//        this.skillLevelMap = skillLevelMap;
//    }
//
//    public void setDefineMap(Map<Integer, Fcd> defineMap) {
//        this.defineMap = defineMap;
//    }
//
//    public void setBlockMap(Map<String, DynamicBlock> blockMap) {
//        this.blockMap = blockMap;
//    }
//
//    public void setSpawnMonsterMap(Map<String, MonsterAttributeVo> spawnMonsterMap) {
//        this.spawnMonsterMap = spawnMonsterMap;
//    }
//
//    public void setAreaSpawnList(List<MonsterSpawnVo> areaSpawnList) {
//        this.areaSpawnList = areaSpawnList;
//    }
//
//    public void addBlockStatusMap(Map<String, Byte> blockStatusMap) {
//        if (this.blockStatusMap == null) {
//            this.blockStatusMap = new HashMap<>();
//        }
//        this.blockStatusMap.putAll(blockStatusMap);
//    }
//
//    public Map<Integer, MonsterVo> getMonsterVoMap() {
//        return monsterVoMap;
//    }
//
//    public void addMonsterVoMap(Map<Integer, MonsterVo> monsterVoMap) {
//        if (this.monsterVoMap == null) {
//            this.monsterVoMap = new HashMap<>();
//        }
//        this.monsterVoMap.putAll(monsterVoMap);
//    }
//
//    public void addMonsterVoMap(MonsterVo monsterVo) {
//        if (this.monsterVoMap == null) {
//            this.monsterVoMap = new HashMap<>();
//        }
//        this.monsterVoMap.put(monsterVo.getId(), monsterVo);
//    }
//
//    public void setBuffMap(Map<String, BuffVo> buffMap) {
//        this.buffMap = buffMap;
//    }
//
//    public void setItemIdSet(Set<Integer> itemIdSet) {
//        this.itemIdSet = itemIdSet;
//    }
//
//    public void setStageVo(StageinfoVo stageVo) {
//        this.stageVo = stageVo;
//    }
//
//    public void setIsAgain(byte isAgain) {
//        this.isAgain = isAgain;
//    }
//
//    public void setPlayDrama(byte playDrama) {
//        this.playDrama = playDrama;
//    }
//
//    public void setWaveMonsterTypeList(List<String> waveMonsterTypeList) {
//        this.waveMonsterTypeList = waveMonsterTypeList;
//    }
//
//    public void setRoleBuddy(RoleBuddy roleBuddy) {
//        this.roleFightBuddy = roleBuddy;
//    }
//
//    public void setSkillModule(SkillModule skillModule) {
//        this.skillModule = skillModule;
//    }
//
//
//    public Map<String, BuffVo> getBuffMap() {
//        return buffMap;
//    }
//}
