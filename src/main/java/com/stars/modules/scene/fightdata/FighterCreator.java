package com.stars.modules.scene.fightdata;

import com.stars.core.attr.FormularUtils;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.prodata.Job;
import com.stars.modules.role.prodata.Resource;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.scene.prodata.MonsterAttributeVo;
import com.stars.modules.skill.summary.SkillSummaryComponent;
import com.stars.services.summary.Summary;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/8/29.
 */
public class FighterCreator {

    public static FighterEntity createSelf(Map<String, Module> modules) {
        return null;
    }

    public static FighterEntity createPlayer(Map<String, Module> modules) {
        return null;
    }

    public static FighterEntity createSelf(Map<String, Module> modules, byte camp) {
        return null;
    }

    public static FighterEntity createSelf(Map<String, Module> modules, String pos, int rot) {
        return null;
    }

    /**
     * 组装角色FighterEntity
     *
     * @param type
     * @param camp
     * @param role
     * @param position
     * @param rotation
     * @param skillMap
     * @param skillDamageMap
     * @return
     */
    public static FighterEntity create(byte type, byte camp, Role role, String position, int rotation,
                                       Map<Integer, Integer> skillMap, Map<Integer, Integer> skillDamageMap,
                                       Map<Integer, String> trumpSkillAttrMap, int curDeityWeapon,List<String> dragonBallIdsList) {
        FighterEntity entity = new FighterEntity(type, "" + role.getRoleId());
        entity.setName(role.getName());
        entity.setLevel(role.getLevel());
        entity.setCamp(camp);
        entity.setModelId(RoleManager.getJobById(role.getJobId()).getModelres());
//        entity.setScale(0);
//        entity.setBlood((short) 0);
        entity.setPosition(position);
        entity.setRotation(rotation);
//        entity.setAwake("");
//        entity.setTalk("");
        entity.setSkills(skillMap);
        entity.setSkillDamageMap(skillDamageMap);
        entity.setTrumpSkillAttr(trumpSkillAttrMap);
        entity.setAttribute(role.getTotalAttr().clone());
//        entity.setResurgence(0);
        entity.setFightArea(RoleManager.getJobById(role.getJobId()).getFightarea());
        Resource resourceVo = RoleManager.getResourceById(RoleManager.getJobById(role.getJobId()).getModelres());
        entity.setHitSize(resourceVo.getHitsize());
        entity.setMoveSpeed(resourceVo.getMovespeed());
        // 显示经验条,战力等信息
        entity.setExp(role.getExp());
        entity.setReqExp(RoleManager.getRequestExpByJobLevel(role.getJobId(), role.getLevel() + 1));
        entity.setFightScore(role.getFightScore());
        Map<Integer, Integer> nextLevelExpMap = new HashMap<>();
        Map<Integer, Integer> nextLevelFsMap = new HashMap<>();
        for (byte i = 0; i < FighterEntity.NEXT_LEVEL_DATA_NUM; i++) {
            int nextLevel = role.getLevel() + i + 1;
            if (RoleManager.getGradeByJobLevel(role.getJobId(), nextLevel) == null)
                break;
            // 经验要取下一级
            if (RoleManager.getGradeByJobLevel(role.getJobId(), nextLevel + 1) == null)
                break;
            nextLevelExpMap.put(nextLevel, RoleManager.getRequestExpByJobLevel(role.getJobId(), nextLevel + 1));
            int addFightScore =
                    FormularUtils.calFightScore(RoleManager.getGradeByJobLevel(role.getJobId(), nextLevel + 1).getAttribute())
                            - FormularUtils.calFightScore(RoleManager.getGradeByJobLevel(role.getJobId(), nextLevel).getAttribute());
            nextLevelFsMap.put(nextLevel, addFightScore);
        }
        entity.setNextLevelExp(nextLevelExpMap);
        entity.setNextLevelFightScore(nextLevelFsMap);
        entity.setCurDeityWeapon(curDeityWeapon);
        entity.setDragonBallIdList(dragonBallIdsList);
        return entity;
    }

    /**
     * 组装怪物FighterEntity
     * 这里传入的uniqueId已经拼接好了(生成唯一Id)
     *
     * @param type
     * @param uniqueId
     * @param spawnUId
     * @param spawnConfigId
     * @param monsterAttr
     * @param awake
     * @param spawnDelay
     * @param dropMapList
     * @return
     */
    public static FighterEntity create(byte type, String uniqueId, String spawnUId, int spawnConfigId,
                                       MonsterAttributeVo monsterAttr, String awake, int spawnDelay,
                                       List<Map<Integer, Integer>> dropMapList) {
        FighterEntity entity = new FighterEntity(type, uniqueId);
        entity.setName(monsterAttr.getName());
        entity.setLevel(1);
        entity.setCamp(monsterAttr.getCamp());
        entity.setModelId(monsterAttr.getMonsterId());
//        entity.setScale(monsterAttr.getScale());
//        entity.setBlood(monsterAttr.getBlood());
        entity.setPosition(monsterAttr.getPosition());
        entity.setRotation(monsterAttr.getRotation());
        entity.setAwake(awake);
        entity.setTalk(monsterAttr.getTalk());
        entity.setSkills(monsterAttr.getMonsterVo().getSkillMap());
        entity.setAttribute(monsterAttr.getAttribute().clone());
        if (!StringUtil.isEmpty(dropMapList))
            entity.setDropMapList(dropMapList);
//        entity.setResurgence(0);
//        entity.setFightArea(0);
        entity.setHitSize(monsterAttr.getMonsterVo().getHitSize());
        entity.setMoveSpeed(monsterAttr.getMonsterVo().getMoveSpeed());
        entity.setMonsterAttrId(monsterAttr.getStageMonsterId());
        entity.setSpawnUId(spawnUId);
        entity.setSpawnConfigId(spawnConfigId);
        // 拼装扩展字段 todo:注意！这里必须和客户端字段名一致
        StringBuilder builder = new StringBuilder("");
        builder.append("stageMonsterId=").append(monsterAttr.getStageMonsterId()).append(";");
        builder.append("monsterSpawnId=").append(spawnConfigId).append(";");
        builder.append("delay=").append(spawnDelay).append(";");
        builder.append("monsterType=").append(monsterAttr.getMonsterVo().getType()).append(";");
        entity.setExtraValue(builder.toString());
        return entity;
    }

    /**
     * 通过常用数据组装
     *
     * @param camp
     * @param summary
     * @return
     */
    public static Map<String, FighterEntity> createBySummary(byte camp, Summary summary) {
        Map<String, FighterEntity> map = new HashMap<>();
        RoleSummaryComponent rsc = (RoleSummaryComponent) summary.getComponent(MConst.Role);
        if (rsc == null)
            return null;
        if (RoleManager.getJobById(rsc.getRoleJob()) == null) {
            return null;
        }
        FighterEntity entity = new FighterEntity(FighterEntity.TYPE_PLAYER, String.valueOf(summary.getRoleId()));
        entity.setName(rsc.getRoleName());
        entity.setLevel(rsc.getRoleLevel());
        entity.setCamp(camp);
        Job jobVo = RoleManager.getJobById(rsc.getRoleJob());
        if (jobVo == null) {
            LogUtil.error("createBySummary|Err|jobVo == null|" + rsc.getRoleJob() + "|" + rsc.getRoleName());
            entity.setModelId(1);
        } else {
            entity.setModelId(jobVo.getModelres());
        }
        // 技能常用数据
        SkillSummaryComponent ssc = (SkillSummaryComponent) summary.getComponent("skill");
        entity.setSkills(ssc == null ? new HashMap<Integer, Integer>() : ssc.getSkillLevel());
        entity.setSkillDamageMap(ssc == null ? new HashMap<Integer, Integer>() : ssc.getSkillDamage());
        entity.setAttribute(rsc.getTotalAttr().clone());
        entity.setFightArea(RoleManager.getJobById(rsc.getRoleJob()).getFightarea());
        Resource resourceVo = RoleManager.getResourceById(RoleManager.getJobById(rsc.getRoleJob()).getModelres());
        entity.setHitSize(resourceVo.getHitsize());
        entity.setMoveSpeed(resourceVo.getMovespeed());
        entity.setFightScore(rsc.getFightScore());
        map.put(entity.getUniqueId(), entity);
        return map;
    }
    
}
