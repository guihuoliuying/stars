package com.stars.modules.scene.fightdata;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyManager;
import com.stars.modules.buddy.prodata.BuddyArmsVo;
import com.stars.modules.buddy.prodata.BuddyLevelVo;
import com.stars.modules.buddy.prodata.BuddyStageVo;
import com.stars.modules.buddy.prodata.BuddyinfoVo;
import com.stars.modules.buddy.summary.BuddySummaryComponent;
import com.stars.modules.buddy.summary.BuddySummaryVo;
import com.stars.modules.buddy.userdata.RoleBuddy;
import com.stars.modules.deityweapon.DeityWeaponModule;
import com.stars.modules.deityweapon.summary.DeityWeaponSummaryComponent;
import com.stars.modules.elitedungeon.prodata.EliteDungeonRobotVo;
import com.stars.modules.elitedungeon.userdata.ElitePlayerImagePo;
import com.stars.modules.newequipment.NewEquipmentModule;
import com.stars.modules.newequipment.summary.NewEquipmentSummaryComponent;
import com.stars.modules.newofflinepvp.prodata.OfflineInitializeVo;
import com.stars.modules.offlinepvp.prodata.OPRobotVo;
import com.stars.modules.poemdungeon.prodata.PoemRobotVo;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.prodata.Job;
import com.stars.modules.role.prodata.Resource;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.scene.prodata.MonsterAttributeVo;
import com.stars.modules.skill.SkillModule;
import com.stars.modules.skill.summary.SkillSummaryComponent;
import com.stars.multiserver.MultiServerHelper;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryConst;
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
        RoleModule roleModule = (RoleModule) modules.get(MConst.Role);
        SkillModule skillModule = (SkillModule) modules.get(MConst.Skill);
        DeityWeaponModule deityWeaponModule = (DeityWeaponModule) modules.get(MConst.Deity);
        NewEquipmentModule equipmentModule = (NewEquipmentModule)modules.get(MConst.NewEquipment);
        List <String> dragonBallIdsList = equipmentModule.getDragonBallIdList();
        String pos = "0+0+0";
        int rot = 0;
        FighterEntity self = create(FighterEntity.TYPE_SELF, FighterEntity.CAMP_SELF,
                roleModule.getRoleRow(), pos, rot, skillModule.getUseSkill(),
                skillModule.getSkillDamageMap(), skillModule.getTrumpPassSkillAttr(),
                deityWeaponModule.getCurRoleDeityWeapoonId(),dragonBallIdsList);
        self.setServerName(MultiServerHelper.getServerName());
        return self;
    }

    public static FighterEntity createPlayer(Map<String, Module> modules) {
        RoleModule roleModule = (RoleModule) modules.get(MConst.Role);
        SkillModule skillModule = (SkillModule) modules.get(MConst.Skill);
        DeityWeaponModule deityWeaponModule = (DeityWeaponModule) modules.get(MConst.Deity);
        NewEquipmentModule equipmentModule = (NewEquipmentModule)modules.get(MConst.NewEquipment);
        List <String> dragonBallIdsList = equipmentModule.getDragonBallIdList();
        String pos = "0+0+0";
        int rot = 0;
        FighterEntity self = create(FighterEntity.TYPE_PLAYER, FighterEntity.CAMP_SELF,
                roleModule.getRoleRow(), pos, rot, skillModule.getUseSkill(),
                skillModule.getSkillDamageMap(), skillModule.getTrumpPassSkillAttr(),
                deityWeaponModule.getCurRoleDeityWeapoonId(),dragonBallIdsList);
        self.setServerName(MultiServerHelper.getServerName());
        return self;
    }

    public static FighterEntity createSelf(Map<String, Module> modules, byte camp) {
        RoleModule roleModule = (RoleModule) modules.get(MConst.Role);
        SkillModule skillModule = (SkillModule) modules.get(MConst.Skill);
        DeityWeaponModule deityWeaponModule = (DeityWeaponModule) modules.get(MConst.Deity);
        NewEquipmentModule equipmentModule = (NewEquipmentModule)modules.get(MConst.NewEquipment);
        List <String> dragonBallIdsList = equipmentModule.getDragonBallIdList();
        String pos = "0+0+0";
        int rot = 0;
        return create(FighterEntity.TYPE_SELF, camp,
                roleModule.getRoleRow(), pos, rot, skillModule.getUseSkill(),
                skillModule.getSkillDamageMap(), skillModule.getTrumpPassSkillAttr(),
                deityWeaponModule.getCurRoleDeityWeapoonId(),dragonBallIdsList);
    }

    public static FighterEntity createSelf(Map<String, Module> modules, String pos, int rot) {
        RoleModule roleModule = (RoleModule) modules.get(MConst.Role);
        SkillModule skillModule = (SkillModule) modules.get(MConst.Skill);
        NewEquipmentModule equipmentModule = (NewEquipmentModule)modules.get(MConst.NewEquipment);
        List <String> dragonBallIdsList = equipmentModule.getDragonBallIdList();
        DeityWeaponModule deityWeaponModule = (DeityWeaponModule) modules.get(MConst.Deity);
        FighterEntity self = create(FighterEntity.TYPE_SELF, FighterEntity.CAMP_SELF,
                roleModule.getRoleRow(), pos, rot, skillModule.getUseSkill(),
                skillModule.getSkillDamageMap(), skillModule.getTrumpPassSkillAttr(),
                deityWeaponModule.getCurRoleDeityWeapoonId(),dragonBallIdsList);
        self.setServerName(MultiServerHelper.getServerName());
        return self;
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
     * 组装伙伴FighterEntity
     *
     * @param type
     * @param camp
     * @param roleBuddy
     * @return
     */
    public static FighterEntity create(byte type, byte camp, RoleBuddy roleBuddy) {
        FighterEntity entity = new FighterEntity(type, "b" + roleBuddy.getRoleId() + roleBuddy.getBuddyId());
        BuddyinfoVo buddyinfoVo = BuddyManager.getBuddyinfoVo(roleBuddy.getBuddyId());
        BuddyStageVo buddyStageVo = BuddyManager.getBuddyStageVo(roleBuddy.getBuddyId(), roleBuddy.getStageLevel());
        entity.setName(buddyinfoVo.getName());
        entity.setLevel(roleBuddy.getLevel());
        entity.setCamp(camp);
        entity.setModelId(buddyStageVo.getMonsterId());
        entity.setScale(buddyStageVo.getSceneScale());
//        entity.setBlood(0);
//        entity.setPosition("");
//        entity.setRotation(0);
        entity.setAwake(String.valueOf(0));
        entity.setTalk(buddyinfoVo.getFollow());
        entity.setSkills(roleBuddy.getUseSkill());
        entity.setAttribute(roleBuddy.getAttribute().clone());
        entity.setResurgence(buddyinfoVo.getResurgence());
        entity.setHitSize(buddyStageVo.getUseMonsterVo().getHitSize());
        entity.setMoveSpeed(buddyStageVo.getUseMonsterVo().getMoveSpeed());
//        entity.setFightArea(0);
        // 显示等级信息
        entity.setExp(roleBuddy.getExp());
        BuddyLevelVo buddyLevelVo = BuddyManager.getBuddyLevelVo(roleBuddy.getBuddyId(), roleBuddy.getLevel() + 1);
        entity.setReqExp(buddyLevelVo == null ? 0 : buddyLevelVo.getReqExp());
        Map<Integer, Integer> nextLevelExpMap = new HashMap<>();
        for (byte i = 0; i < FighterEntity.NEXT_LEVEL_DATA_NUM; i++) {
            int nextLevel = roleBuddy.getLevel() + i + 1;
            // 经验要取下一级
            if (BuddyManager.getBuddyLevelVo(roleBuddy.getBuddyId(), nextLevel + 1) == null)
                break;
            nextLevelExpMap.put(nextLevel, BuddyManager.getBuddyLevelVo(roleBuddy.getBuddyId(), nextLevel + 1).getReqExp());
        }
        entity.setNextLevelExp(nextLevelExpMap);
        entity.setMasterUId(String.valueOf(roleBuddy.getRoleId()));
        // 拼装扩展字段
        StringBuilder builder = new StringBuilder("");
        builder.append("masterUId=").append("" + roleBuddy.getRoleId()).append(";");
        entity.setExtraValue(builder.toString());
        return entity;
    }

    /**
     * 组装机器人
     *
     * @param camp
     * @param robotVo
     * @return
     */
    public static Map<String, FighterEntity> createRobot(byte camp, OPRobotVo robotVo) {
        Map<String, FighterEntity> map = new HashMap<>();
        FighterEntity entity = new FighterEntity(FighterEntity.TYPE_PLAYER, "r" + robotVo.getRobotId());
        entity.setName(robotVo.getRobotName());
        entity.setLevel(robotVo.getRobotLevel());
        entity.setCamp(camp);
        entity.setModelId(RoleManager.getJobById(robotVo.getJobId()).getModelres());
        entity.setSkills(robotVo.getRobotSkillMap());
        entity.setSkillDamageMap(robotVo.getRobotSkillDamage());
        entity.setAttribute(robotVo.getRobotAttribute().clone());
        entity.setFightArea(RoleManager.getJobById(robotVo.getJobId()).getFightarea());
        Resource resourceVo = RoleManager.getResourceById(RoleManager.getJobById(robotVo.getJobId()).getModelres());
        entity.setHitSize(resourceVo.getHitsize());
        entity.setMoveSpeed(resourceVo.getMovespeed());
        entity.setFightScore(robotVo.getRobotFightScore());
        entity.setServerName(robotVo.getServerName());
        // todo:机器人暂时没有配置神兵字段,先默认为0,有了之后在这里加上
        entity.setCurDeityWeapon(0);
        map.put(entity.getUniqueId(), entity);
        /* 出战宠物 */
        if (robotVo.getBuddyId() != 0) {
            FighterEntity buddyEntity = createRobotBuddy(camp, robotVo, entity.getUniqueId());
            map.put(buddyEntity.getUniqueId(), buddyEntity);
        }
        return map;
    }

    /* 组装机器人的伙伴 */
    private static FighterEntity createRobotBuddy(byte camp, OPRobotVo robotVo, String masterUId) {
        FighterEntity entity = new FighterEntity(FighterEntity.TYPE_BUDDY, "b" + masterUId + robotVo.getBuddyId());
        BuddyinfoVo buddyinfoVo = BuddyManager.getBuddyinfoVo(robotVo.getBuddyId());
        BuddyLevelVo buddyLevelVo = BuddyManager.getBuddyLevelVo(robotVo.getBuddyId(), robotVo.getBuddyLevel());
        BuddyStageVo buddyStageVo = BuddyManager.getBuddyStageVo(robotVo.getBuddyId(), robotVo.getBuddyStageLevel());
        entity.setName(buddyinfoVo.getName());
        entity.setLevel(robotVo.getBuddyLevel());
        entity.setCamp(camp);
        entity.setModelId(buddyStageVo.getMonsterId());
        entity.setScale(buddyStageVo.getSceneScale());
        entity.setAwake(String.valueOf(0));
        entity.setTalk(buddyinfoVo.getFollow());
        entity.setSkills(buddyStageVo.getUseMonsterVo().getSkillMap());
        // 这里宠物属性=等级属性+飞升等级属性
        Attribute attribute = new Attribute();
        attribute.addAttribute(buddyLevelVo.getAttribute());
        attribute.addAttribute(buddyStageVo.getAttribute());
        entity.setAttribute(attribute);
        entity.getAttribute().setMaxhp(attribute.getHp());  // 设置最大血量
        entity.setResurgence(buddyinfoVo.getResurgence());
        entity.setHitSize(buddyStageVo.getUseMonsterVo().getHitSize());
        entity.setMoveSpeed(buddyStageVo.getUseMonsterVo().getMoveSpeed());
        entity.setMasterUId(masterUId);
        // 拼装扩展字段
        StringBuilder builder = new StringBuilder("");
        builder.append("masterUId=").append(masterUId).append(";");
        entity.setExtraValue(builder.toString());
        return entity;
    }

    /**
     * 组装机器人(诗歌副本机器人 poemrobot表)
     *
     * @param camp
     * @param robotVo
     * @return
     */
    public static Map<String, FighterEntity> createRobot(byte camp, PoemRobotVo robotVo) {
        Map<String, FighterEntity> map = new HashMap<>();
        FighterEntity entity = new FighterEntity(FighterEntity.TYPE_PLAYER, "r" + robotVo.getPoemRobotId());
        //FighterEntity entity = new FighterEntity(FighterEntity.TYPE_PLAYER, Integer.toString(robotVo.getPoemRobotId()));
        entity.setName(robotVo.getRobotName());
        entity.setLevel(robotVo.getRobotLevel());
        entity.setCamp(camp);
        entity.setModelId(RoleManager.getJobById(robotVo.getJobId()).getModelres());
        entity.setSkills(robotVo.getRobotSkillMap());
        entity.setSkillDamageMap(robotVo.getRobotSkillDamage());
        entity.setAttribute(robotVo.getRobotAttribute().clone());
        entity.setFightArea(RoleManager.getJobById(robotVo.getJobId()).getFightarea());
        Resource resourceVo = RoleManager.getResourceById(RoleManager.getJobById(robotVo.getJobId()).getModelres());
        entity.setHitSize(resourceVo.getHitsize());
        entity.setMoveSpeed(resourceVo.getMovespeed());
        entity.setFightScore(robotVo.getRobotFightScore());
        // todo:机器人暂时没有配置神兵字段,先默认为0,有了之后在这里加上
        entity.setCurDeityWeapon(0);
        map.put(entity.getUniqueId(), entity);
        /* 出战宠物 */
        if (robotVo.getBuddyId() != 0) {
            FighterEntity buddyEntity = createRobotBuddy(camp, robotVo, entity.getUniqueId());
            map.put(buddyEntity.getUniqueId(), buddyEntity);
        }
        return map;
    }

    /* 组装机器人的伙伴 (诗歌副本机器人)*/
    private static FighterEntity createRobotBuddy(byte camp, PoemRobotVo robotVo, String masterUId) {
        FighterEntity entity = new FighterEntity(FighterEntity.TYPE_BUDDY, "b" + masterUId + robotVo.getBuddyId());
        BuddyinfoVo buddyinfoVo = BuddyManager.getBuddyinfoVo(robotVo.getBuddyId());
        BuddyLevelVo buddyLevelVo = BuddyManager.getBuddyLevelVo(robotVo.getBuddyId(), robotVo.getBuddyLevel());
        BuddyStageVo buddyStageVo = BuddyManager.getBuddyStageVo(robotVo.getBuddyId(), robotVo.getBuddyStageLevel());
        entity.setName(buddyinfoVo.getName());
        entity.setLevel(robotVo.getBuddyLevel());
        entity.setCamp(camp);
        entity.setModelId(buddyStageVo.getMonsterId());
        entity.setScale(buddyStageVo.getSceneScale());
        entity.setAwake(String.valueOf(0));
        entity.setTalk(buddyinfoVo.getFollow());
        entity.setSkills(buddyStageVo.getUseMonsterVo().getSkillMap());
        // 这里宠物属性=等级属性+飞升等级属性
        Attribute attribute = new Attribute();
        attribute.addAttribute(buddyLevelVo.getAttribute());
        attribute.addAttribute(buddyStageVo.getAttribute());
        entity.setAttribute(attribute);
        entity.getAttribute().setMaxhp(attribute.getHp());  // 设置最大血量
        entity.setResurgence(buddyinfoVo.getResurgence());
        entity.setHitSize(buddyStageVo.getUseMonsterVo().getHitSize());
        entity.setMoveSpeed(buddyStageVo.getUseMonsterVo().getMoveSpeed());
        entity.setMasterUId(masterUId);
        // 拼装扩展字段
        StringBuilder builder = new StringBuilder("");
        builder.append("masterUId=").append(masterUId).append(";");
        entity.setExtraValue(builder.toString());
        return entity;
    }

    /**
     * 新版竞技场组装机器人
     *
     * @param camp
     * @param offlineInitializeVo
     * @return
     */
    public static Map<String, FighterEntity> createOfflinePvpRobot(byte camp, OfflineInitializeVo offlineInitializeVo) {
        Map<String, FighterEntity> map = new HashMap<>();
        FighterEntity entity = new FighterEntity(FighterEntity.TYPE_PLAYER, "r" + offlineInitializeVo.getInitializeId());
        entity.setName(offlineInitializeVo.getRobotName());
        entity.setLevel(offlineInitializeVo.getRobotLevel());
        entity.setCamp(camp);
        entity.setModelId(RoleManager.getJobById(offlineInitializeVo.getJobId()).getModelres());
        entity.setSkills(offlineInitializeVo.getRobotSkillMap());
        entity.setSkillDamageMap(offlineInitializeVo.getRobotSkillDamage());
        entity.setAttribute(offlineInitializeVo.getRobotAttribute().clone());
        entity.setFightArea(RoleManager.getJobById(offlineInitializeVo.getJobId()).getFightarea());
        Resource resourceVo = RoleManager.getResourceById(RoleManager.getJobById(offlineInitializeVo.getJobId()).getModelres());
        entity.setHitSize(resourceVo.getHitsize());
        entity.setMoveSpeed(resourceVo.getMovespeed());
        entity.setFightScore(offlineInitializeVo.getRobotFightScore());
        map.put(entity.getUniqueId(), entity);
        if (offlineInitializeVo.getBuddyId() != 0) {
            FighterEntity buddyEntity = createOfflinePvpBuddy(camp, offlineInitializeVo, entity.getUniqueId());
            map.put(buddyEntity.getUniqueId(), buddyEntity);
        }
        return map;
    }

    /**
     * 新版竞技场组装机器人的伙伴
     *
     * @param camp
     * @param offlineInitializeVo
     * @param masterUId
     * @return
     */
    private static FighterEntity createOfflinePvpBuddy(byte camp, OfflineInitializeVo offlineInitializeVo, String masterUId) {
        FighterEntity entity = new FighterEntity(FighterEntity.TYPE_BUDDY, "b" + masterUId + offlineInitializeVo.getBuddyId());
        BuddyinfoVo buddyinfoVo = BuddyManager.getBuddyinfoVo(offlineInitializeVo.getBuddyId());
        BuddyLevelVo buddyLevelVo = BuddyManager.getBuddyLevelVo(offlineInitializeVo.getBuddyId(), offlineInitializeVo.getBuddyLevel());
        BuddyStageVo buddyStageVo = BuddyManager.getBuddyStageVo(offlineInitializeVo.getBuddyId(), offlineInitializeVo.getBuddyStageLevel());
        entity.setName(buddyinfoVo.getName());
        entity.setLevel(offlineInitializeVo.getBuddyLevel());
        entity.setCamp(camp);
        entity.setModelId(buddyStageVo.getMonsterId());
        entity.setScale(buddyStageVo.getSceneScale());
        entity.setAwake(String.valueOf(0));
        entity.setTalk(buddyinfoVo.getFollow());
        entity.setSkills(buddyStageVo.getUseMonsterVo().getSkillMap());
        Attribute attribute = new Attribute();
        attribute.addAttribute(buddyLevelVo.getAttribute());
        attribute.addAttribute(buddyStageVo.getAttribute());
        entity.setAttribute(attribute);
        entity.getAttribute().setMaxhp(attribute.getHp());  // 设置最大血量
        entity.setResurgence(buddyinfoVo.getResurgence());
        entity.setHitSize(buddyStageVo.getUseMonsterVo().getHitSize());
        entity.setMoveSpeed(buddyStageVo.getUseMonsterVo().getMoveSpeed());
        entity.setMasterUId(masterUId);
        StringBuilder builder = new StringBuilder("");
        builder.append("masterUId=").append(masterUId).append(";");
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
        // 神兵常用数据
        DeityWeaponSummaryComponent dwsc = (DeityWeaponSummaryComponent) summary.getComponent(MConst.Deity);
        if (dwsc != null) {
            entity.setCurDeityWeapon(dwsc.getCurRoleDeityWeapoonId());
        }
        // 龙珠外显数据
        NewEquipmentSummaryComponent nes = (NewEquipmentSummaryComponent) summary.getComponent(SummaryConst.C_NEW_EQUIPMENT);
        List<String> dragonBallList = nes.getDragonBallList();
        if(dragonBallList != null){
            entity.setDragonBallIdList(dragonBallList);
        }
        map.put(entity.getUniqueId(), entity);
        /* 出战宠物 */
        FighterEntity buddyEntity = createBuddyBySummary(camp, summary, entity.getUniqueId());
        if (buddyEntity != null) {
            map.put(buddyEntity.getUniqueId(), buddyEntity);
        }
        return map;
    }

    /* 常用数据组装伙伴 */
    private static FighterEntity createBuddyBySummary(byte camp, Summary summary, String masterUId) {
        BuddySummaryComponent bsc = (BuddySummaryComponent) summary.getComponent(SummaryConst.C_BUDDY);
        if (bsc == null || bsc.getFightBuddySummaryVo() == null)
            return null;
        BuddySummaryVo bsv = bsc.getFightBuddySummaryVo();
        BuddyinfoVo buddyinfoVo = BuddyManager.getBuddyinfoVo(bsv.getBuddyId());
        BuddyStageVo buddyStageVo = BuddyManager.getBuddyStageVo(bsv.getBuddyId(), bsv.getStageLevel());
        if (buddyinfoVo == null || buddyStageVo == null) {
            return null;
        }

        FighterEntity entity = new FighterEntity(FighterEntity.TYPE_BUDDY, "b" + masterUId + bsv.getBuddyId());
        entity.setName(buddyinfoVo.getName());
        entity.setLevel(bsv.getLevel());
        entity.setCamp(camp);
        entity.setModelId(buddyStageVo.getMonsterId());
        entity.setScale(buddyStageVo.getSceneScale());
        entity.setAwake(String.valueOf(0));
        entity.setTalk(buddyinfoVo.getFollow());
        Map<Integer, Integer> skillMap = new HashMap<>();
        BuddyStageVo stageVo = BuddyManager.getBuddyStageVo(bsv.getBuddyId(), bsv.getStageLevel());
        BuddyArmsVo armsVo = BuddyManager.getBuddyArmVo(bsv.getBuddyId(), bsv.getArmLevel());
        if (stageVo != null && armsVo != null) {
            for (Map.Entry<Integer, Integer> entry : stageVo.getUseMonsterVo().getSkillMap().entrySet()) {
                if (armsVo.getSkillLevelMap().containsKey(entry.getKey())) {
                    skillMap.put(entry.getKey(), armsVo.getSkillLevelMap().get(entry.getKey()));
                } else {
                    skillMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
        entity.setSkills(skillMap);
        entity.setAttribute(bsv.getBuddyAttr().clone());
        entity.setResurgence(buddyinfoVo.getResurgence());
        entity.setHitSize(buddyStageVo.getUseMonsterVo().getHitSize());
        entity.setMoveSpeed(buddyStageVo.getUseMonsterVo().getMoveSpeed());
        entity.setMasterUId(masterUId);
        // 拼装扩展字段
        StringBuilder builder = new StringBuilder("");
        builder.append("masterUId=").append(masterUId).append(";");
        entity.setExtraValue(builder.toString());
        return entity;
    }
    
    public static Map<String, FighterEntity> createPlayerImageRobot(byte camp, ElitePlayerImagePo player, long uid){
    	Map<String, FighterEntity> map = new HashMap<>();
        FighterEntity entity = new FighterEntity(FighterEntity.TYPE_PLAYER, "r" + uid);
        entity.setName(player.getName());
        entity.setLevel(player.getLevel());
        entity.setCamp(camp);
        entity.setModelId(RoleManager.getJobById(player.getJob()).getModelres());
        entity.setSkills(player.getSkillMap());
        entity.setSkillDamageMap(player.getRobotSkillDamage());
        entity.setAttribute(player.getAttribute().clone());
        entity.setFightArea(RoleManager.getJobById(player.getJob()).getFightarea());
        Resource resourceVo = RoleManager.getResourceById(RoleManager.getJobById(player.getJob()).getModelres());
        entity.setHitSize(resourceVo.getHitsize());
        entity.setMoveSpeed(resourceVo.getMovespeed());
        entity.setFightScore(player.getFightScore());
//        entity.setServerName(robotVo.getServerName());
        // todo:机器人暂时没有配置神兵字段,先默认为0,有了之后在这里加上
        entity.setCurDeityWeapon(0);
        map.put(entity.getUniqueId(), entity);
        /* 出战宠物 */
        if (player.getBuddyId() != 0) {
            FighterEntity buddyEntity = createRobotBuddy(camp, player, entity.getUniqueId());
            map.put(buddyEntity.getUniqueId(), buddyEntity);
        }
        return map;
    }
    
    /* 组装机器人的伙伴 */
    private static FighterEntity createRobotBuddy(byte camp, ElitePlayerImagePo player, String masterUId) {
    	int buddyid = player.getBuddyId();
        FighterEntity entity = new FighterEntity(FighterEntity.TYPE_BUDDY, "b" + masterUId + buddyid);
        BuddyinfoVo buddyinfoVo = BuddyManager.getBuddyinfoVo(buddyid);
        BuddyLevelVo buddyLevelVo = BuddyManager.getBuddyLevelVo(buddyid, player.getBuddyLevel());
        BuddyStageVo buddyStageVo = BuddyManager.getBuddyStageVo(buddyid, player.getBuddyStageLevel());
        entity.setName(buddyinfoVo.getName());
        entity.setLevel(player.getBuddyLevel());
        entity.setCamp(camp);
        entity.setModelId(buddyStageVo.getMonsterId());
        entity.setScale(buddyStageVo.getSceneScale());
        entity.setAwake(String.valueOf(0));
        entity.setTalk(buddyinfoVo.getFollow());
        entity.setSkills(buddyStageVo.getUseMonsterVo().getSkillMap());
        // 这里宠物属性=等级属性+飞升等级属性
        Attribute attribute = new Attribute();
        attribute.addAttribute(buddyLevelVo.getAttribute());
        attribute.addAttribute(buddyStageVo.getAttribute());
        entity.setAttribute(attribute);
        entity.getAttribute().setMaxhp(attribute.getHp());  // 设置最大血量
        entity.setResurgence(buddyinfoVo.getResurgence());
        entity.setHitSize(buddyStageVo.getUseMonsterVo().getHitSize());
        entity.setMoveSpeed(buddyStageVo.getUseMonsterVo().getMoveSpeed());
        entity.setMasterUId(masterUId);
        // 拼装扩展字段
        StringBuilder builder = new StringBuilder("");
        builder.append("masterUId=").append(masterUId).append(";");
        entity.setExtraValue(builder.toString());
        return entity;
    }
    
    /**
     * 组装机器人
     *
     * @param camp
     * @param robotVo
     * @return
     */
    public static Map<String, FighterEntity> createRobot(byte camp, EliteDungeonRobotVo robotVo) {
        Map<String, FighterEntity> map = new HashMap<>();
        FighterEntity entity = new FighterEntity(FighterEntity.TYPE_PLAYER, "r" + robotVo.getRobotId());
        entity.setName(robotVo.getRobotName());
        entity.setLevel(robotVo.getRobotLevel());
        entity.setCamp(camp);
        entity.setModelId(RoleManager.getJobById(robotVo.getJobId()).getModelres());
        entity.setSkills(robotVo.getRobotSkillMap());
        entity.setSkillDamageMap(robotVo.getRobotSkillDamage());
        entity.setAttribute(robotVo.getRobotAttribute().clone());
        entity.setFightArea(RoleManager.getJobById(robotVo.getJobId()).getFightarea());
        Resource resourceVo = RoleManager.getResourceById(RoleManager.getJobById(robotVo.getJobId()).getModelres());
        entity.setHitSize(resourceVo.getHitsize());
        entity.setMoveSpeed(resourceVo.getMovespeed());
        entity.setFightScore(robotVo.getRobotFightScore());
//        entity.setServerName(robotVo.getServerName());
        // todo:机器人暂时没有配置神兵字段,先默认为0,有了之后在这里加上
        entity.setCurDeityWeapon(0);
        map.put(entity.getUniqueId(), entity);
        /* 出战宠物 */
        if (robotVo.getBuddyId() != 0) {
            FighterEntity buddyEntity = createRobotBuddy(camp, robotVo, entity.getUniqueId());
            map.put(buddyEntity.getUniqueId(), buddyEntity);
        }
        return map;
    }

    /* 组装机器人的伙伴 */
    private static FighterEntity createRobotBuddy(byte camp, EliteDungeonRobotVo robotVo, String masterUId) {
        FighterEntity entity = new FighterEntity(FighterEntity.TYPE_BUDDY, "b" + masterUId + robotVo.getBuddyId());
        BuddyinfoVo buddyinfoVo = BuddyManager.getBuddyinfoVo(robotVo.getBuddyId());
        BuddyLevelVo buddyLevelVo = BuddyManager.getBuddyLevelVo(robotVo.getBuddyId(), robotVo.getBuddyLevel());
        BuddyStageVo buddyStageVo = BuddyManager.getBuddyStageVo(robotVo.getBuddyId(), robotVo.getBuddyStageLevel());
        entity.setName(buddyinfoVo.getName());
        entity.setLevel(robotVo.getBuddyLevel());
        entity.setCamp(camp);
        entity.setModelId(buddyStageVo.getMonsterId());
        entity.setScale(buddyStageVo.getSceneScale());
        entity.setAwake(String.valueOf(0));
        entity.setTalk(buddyinfoVo.getFollow());
        entity.setSkills(buddyStageVo.getUseMonsterVo().getSkillMap());
        // 这里宠物属性=等级属性+飞升等级属性
        Attribute attribute = new Attribute();
        attribute.addAttribute(buddyLevelVo.getAttribute());
        attribute.addAttribute(buddyStageVo.getAttribute());
        entity.setAttribute(attribute);
        entity.getAttribute().setMaxhp(attribute.getHp());  // 设置最大血量
        entity.setResurgence(buddyinfoVo.getResurgence());
        entity.setHitSize(buddyStageVo.getUseMonsterVo().getHitSize());
        entity.setMoveSpeed(buddyStageVo.getUseMonsterVo().getMoveSpeed());
        entity.setMasterUId(masterUId);
        // 拼装扩展字段
        StringBuilder builder = new StringBuilder("");
        builder.append("masterUId=").append(masterUId).append(";");
        entity.setExtraValue(builder.toString());
        return entity;
    }
    
}
