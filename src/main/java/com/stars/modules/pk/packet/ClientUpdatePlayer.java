package com.stars.modules.pk.packet;

import com.stars.modules.pk.PKPacketSet;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.prodata.BuffVo;
import com.stars.modules.scene.prodata.MonsterVo;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.skill.prodata.SkillVo;
import com.stars.modules.skill.prodata.SkillvupVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.*;

/**
 * Created by daiyaorong on 2016/10/14.
 * 向lua端同步玩家的增加与移除
 */
public class ClientUpdatePlayer extends Packet {

    private List<FighterEntity> newFighter;
    private List<String> removeFighter;
    private List<SkillvupVo> skillvupVoList = new LinkedList<SkillvupVo>();
    private List<BuffVo> buffVoList = new LinkedList<BuffVo>();
    private List<ServerOrder> orderList = new LinkedList<>();
    private Map<Integer, MonsterVo> monsterVoMap;// 怪物模型使用数据

    @Override
    public short getType() {
        return PKPacketSet.Client_Player_Update;
    }

    @Override
    public void execPacket() {

    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        //怪物产品数据
        byte size = (byte) (monsterVoMap == null ? 0 : monsterVoMap.size());
        buff.writeByte(size);
        if (size > 0) {
            for (MonsterVo monsterVo : monsterVoMap.values()) {
                monsterVo.writeToBuff(buff);
            }
        }
        //新增玩家
        size = (byte) (newFighter == null ? 0 : newFighter.size());
        buff.writeByte(size);
        if (size > 0) {
            for (FighterEntity entity : newFighter) {
                entity.writeToBuff(buff);
            }
        }
        //移除玩家
        size = (byte) (removeFighter == null ? 0 : removeFighter.size());
        buff.writeByte(size);
        if (size > 0) {
            for (String playerId : removeFighter) {
                buff.writeString(playerId);
            }
        }
        //服务端指令
        size = (byte) (orderList == null ? 0 : orderList.size());
        buff.writeByte(size);
        if (size > 0) {
            for (ServerOrder order : orderList) {
                order.writeToBuffer(buff);
            }
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    public void setNewFighter(List<FighterEntity> newFighter) {
        this.newFighter = newFighter;
        FighterEntity fighterEntity = null;
        SkillVo skillVo = null;
        int skillLevel = 0;
        for (int i = 0, len = newFighter.size(); i < len; i++) {
            fighterEntity = newFighter.get(i);
            //填充skillUpVo数据;
            Map<Integer, Integer> skillIdLevelMap = fighterEntity.getSkills();
            for (Map.Entry<Integer, Integer> kvp : skillIdLevelMap.entrySet()) {
                skillLevel = kvp.getValue();
                if (addSkillupVo(kvp.getKey(), skillLevel)) {
                    //填充BuffVo数据;
                    skillVo = SkillManager.getSkillVo(kvp.getKey());
                    if (skillVo != null) {
                        Set<Integer> buffIdSet = skillVo.getBuffIdSet();
                        for (Integer buffId : buffIdSet) {
                            addBuffVo(buffId, skillLevel);
                        }
                    }
                }
            }
        }

    }

    private boolean addSkillupVo(int SkillId, int level) {
        SkillvupVo skillvupVo = SkillManager.getSkillvupVo(SkillId, level);
        if (skillvupVo != null && skillvupVoList != null && skillvupVoList.contains(skillvupVo) == false) {
            skillvupVoList.add(skillvupVo);
            BuffVo buffVo = SceneManager.getBuffVo(skillvupVo.getBuffId(), skillvupVo.getLevel());
            if (buffVo != null) {
                buffVoList.add(buffVo);
            }
            return true;
        }
        return false;
    }

    private boolean addBuffVo(int buffId, int level) {
        BuffVo buffVo = SceneManager.getBuffVo(buffId, level);
        if (buffVo != null && buffVoList != null && buffVoList.contains(buffVo) == false) {
            buffVoList.add(buffVo);
            return true;
        }
        return false;
    }

    public void setRemoveFighter(List<String> removeFighter) {
        this.removeFighter = removeFighter;
    }

    public List<ServerOrder> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<ServerOrder> orderList) {
        this.orderList = orderList;
    }

    public void addOrder(ServerOrder order) {
        this.orderList.add(order);
    }

    public Map<Integer, MonsterVo> getMonsterVoMap() {
        return monsterVoMap;
    }

    public void setMonsterVoMap(Map<Integer, MonsterVo> monsterVoMap) {
        this.monsterVoMap = monsterVoMap;
    }

    public void addMonsterVo(MonsterVo monsterVo) {
        if (monsterVoMap == null) {
            monsterVoMap = new HashMap<>();
        }
        monsterVoMap.put(monsterVo.getId(), monsterVo);
    }
}
