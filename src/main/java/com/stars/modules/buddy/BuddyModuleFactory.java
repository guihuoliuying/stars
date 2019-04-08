package com.stars.modules.buddy;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.buddy.event.BuddyActiveEvent;
import com.stars.modules.buddy.event.BuddyUpgradeEvent;
import com.stars.modules.buddy.listener.*;
import com.stars.modules.buddy.prodata.*;
import com.stars.modules.buddy.summary.BuddySummaryComponentImpl;
import com.stars.modules.data.DataManager;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.prodata.MonsterVo;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.skill.prodata.SkillvupVo;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.modules.tool.event.UseToolEvent;
import com.stars.modules.vip.event.VipLevelupEvent;
import com.stars.services.summary.Summary;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/8/5.
 */
public class BuddyModuleFactory extends AbstractModuleFactory<BuddyModule> {
    public BuddyModuleFactory() {
        super(new BuddyPacketSet());
    }

    @Override
    public BuddyModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new BuddyModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        BuddyListenner buddyListenner = new BuddyListenner((BuddyModule) module);
        eventDispatcher.reg(RoleLevelUpEvent.class, new ActiveBuddyLineupListener((BuddyModule) module));
        BuddyUpgradeStageListener buddyUpgradeStageListener = new BuddyUpgradeStageListener((BuddyModule) module);
        eventDispatcher.reg(BuddyUpgradeEvent.class, buddyUpgradeStageListener);
        eventDispatcher.reg(AddToolEvent.class, buddyUpgradeStageListener);
        ToolChangeListener toolChangeListener = new ToolChangeListener((BuddyModule) module);
        eventDispatcher.reg(AddToolEvent.class, toolChangeListener);
        eventDispatcher.reg(UseToolEvent.class, toolChangeListener);
        eventDispatcher.reg(ForeShowChangeEvent.class, new ForeShowChangeListener((BuddyModule) module));
        eventDispatcher.reg(BuddyActiveEvent.class, buddyListenner);
        eventDispatcher.reg(RoleLevelUpEvent.class, buddyListenner);
        eventDispatcher.reg(VipLevelupEvent.class, buddyListenner);
    }

    @Override
    public void init() throws Exception {
        Summary.regComponentClass("buddy", BuddySummaryComponentImpl.class);

    }

    @Override
    public void loadProductData() throws Exception {
        Map<Integer, BuddyinfoVo> buddyinfoVoMap = loadBuddyinfoVo();
        Map<Integer, Map<Integer, BuddyLevelVo>> buddyLevelVoMap = loadBuddyLevelVo();
        Map<Integer, Map<Integer, BuddyStageVo>> buddyStageVoMap = loadBuddyStageVo();
        Map<Byte, Map<Integer, BuddyLineupVo>> lineupLevelMap = loadBuddyLineupVo();
        Map<Integer, Map<Integer, BuddyArmsVo>> buddyArmsVoMap = loadBuddyArmsVo();
        Attribute attribute;
        // 计算 初始等级+阶级+武装等级+被动技能加成 属性、战力
        for (BuddyinfoVo buddyinfoVo : buddyinfoVoMap.values()) {
            attribute = new Attribute();
            attribute.addAttribute(buddyLevelVoMap.get(buddyinfoVo.getBuddyId()).
                    get(BuddyManager.BUDDY_INIT_LEVEL).getAttribute());
            BuddyStageVo buddyStageVo = buddyStageVoMap.get(buddyinfoVo.getBuddyId()).
                    get(BuddyManager.BUDDY_INIT_STAGELV);
            if (buddyStageVo != null) {
                attribute.addAttribute(buddyStageVo.getAttribute());
            }
            attribute.addAttribute(buddyArmsVoMap.get(buddyinfoVo.getBuddyId()).get(BuddyManager.BUDDY_INIT_ARMLV)
                    .getArmLevelAttr());
            // 根据伙伴调用的怪物的被动技能，计算属性加成
            int monsterId = buddyStageVo.getMonsterId();
            MonsterVo monsterVo = SceneManager.getMonsterVo(monsterId);
            if (monsterVo != null) {
                List<Integer> passSkill = monsterVo.getPassSkillList();
                for (Integer skillId : passSkill) {
                    //怪物默认skillLevel是1级的，这里skillId填的必须正好是1级的skillId，否则会拿不到skillvup
                    SkillvupVo skillvupVo = SkillManager.getSkillvupVo(skillId, 1);
                    if (skillvupVo != null) {
                        skillvupVo.addAttrByEffectInfo(attribute);
                    }
                }
            }
            buddyinfoVo.setInitAttr(attribute);
            buddyinfoVo.setFightScore(FormularUtils.calFightScore(attribute));
        }
        /**
         * 初始化伙伴经验物品itemid和单个添加经验单位值
         */
        String buddy_expitem = DataManager.getCommConfig("buddy_expitem");
        int expItemId = BuddyManager.expItemId;
        int expUnit = BuddyManager.expUnit;
        if (buddy_expitem != null) {
            String[] buddy_expItems = buddy_expitem.split("\\+");
            if (buddy_expItems.length == 2) {
                expItemId = Integer.parseInt(buddy_expItems[0]);
                expUnit = Integer.parseInt(buddy_expItems[1]);
            }
        }
        BuddyManager.buddyinfoVoMap = buddyinfoVoMap;
        BuddyManager.buddyLevelVoMap = buddyLevelVoMap;
        BuddyManager.buddyStageVoMap = buddyStageVoMap;
        BuddyManager.lineupLevelMap = lineupLevelMap;
        BuddyManager.buddyArmsVoMap = buddyArmsVoMap;

        BuddyManager.expItemId = expItemId;
        BuddyManager.expUnit = expUnit;
        List<BuddyGuard> buddyGuards = DBUtil.queryList(DBUtil.DB_PRODUCT, BuddyGuard.class, "select * from buddyguard;");
        Map<Integer, List<BuddyGuard>> buddyGrardGroupMap = new HashMap<>();
        Map<Integer, BuddyGuard> buddyIdGuardMap = new HashMap<>();
        Map<Integer, String> buddyGuardGroupIdNameMap = new HashMap<>();
        for (BuddyGuard buddyGuard : buddyGuards) {
            List<BuddyGuard> buddyGuardGroupList = buddyGrardGroupMap.get(buddyGuard.getGroupid());
            if (buddyGuardGroupList == null) {
                buddyGuardGroupList = new ArrayList<>();
                buddyGrardGroupMap.put(buddyGuard.getGroupid(), buddyGuardGroupList);
            }
            buddyGuardGroupList.add(buddyGuard);
            buddyIdGuardMap.put(buddyGuard.getBuddyid(), buddyGuard);
            buddyGuardGroupIdNameMap.put(buddyGuard.getGroupid(), buddyGuard.getGroupname());
        }
        BuddyManager.buddyGuardGroupIdNameMap = buddyGuardGroupIdNameMap;
        BuddyManager.buddyGuardGroupMap = buddyGrardGroupMap;
        BuddyManager.buddyIdGuardMap = buddyIdGuardMap;
    }

    private Map<Integer, BuddyinfoVo> loadBuddyinfoVo() throws SQLException {
        String sql = "select * from `buddyinfo`; ";
        Map<Integer, BuddyinfoVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "buddyid", BuddyinfoVo.class, sql);
//        BuddyManager.buddyinfoVoMap = map;
        return map;
    }

    private Map<Integer, Map<Integer, BuddyLevelVo>> loadBuddyLevelVo() throws SQLException {
        String sql = "select * from `buddylevel`; ";
        List<BuddyLevelVo> list = DBUtil.queryList(DBUtil.DB_PRODUCT, BuddyLevelVo.class, sql);
        Map<Integer, Map<Integer, BuddyLevelVo>> map = new HashMap<>();
        for (BuddyLevelVo vo : list) {
            Map<Integer, BuddyLevelVo> levelVoMap = map.get(vo.getBuddyId());
            if (levelVoMap == null) {
                levelVoMap = new HashMap<>();
                map.put(vo.getBuddyId(), levelVoMap);
            }
            levelVoMap.put(vo.getLevel(), vo);
        }
//        BuddyManager.buddyLevelVoMap = map;
        return map;
    }

    private Map<Integer, Map<Integer, BuddyStageVo>> loadBuddyStageVo() throws SQLException {
        String sql = "select * from `buddystage`; ";
        List<BuddyStageVo> list = DBUtil.queryList(DBUtil.DB_PRODUCT, BuddyStageVo.class, sql);
        Map<Integer, Map<Integer, BuddyStageVo>> map = new HashMap<>();
        for (BuddyStageVo vo : list) {
            Map<Integer, BuddyStageVo> levelVoMap = map.get(vo.getBuddyId());
            if (levelVoMap == null) {
                levelVoMap = new HashMap<>();
                map.put(vo.getBuddyId(), levelVoMap);
            }
            levelVoMap.put(vo.getStageLevel(), vo);
        }
//        BuddyManager.buddyStageVoMap = map;
        return map;
    }

    private Map<Byte, Map<Integer, BuddyLineupVo>> loadBuddyLineupVo() throws SQLException {
        String sql = "select * from `buddylineup`; ";
        List<BuddyLineupVo> list = DBUtil.queryList(DBUtil.DB_PRODUCT, BuddyLineupVo.class, sql);
        Map<Byte, Map<Integer, BuddyLineupVo>> map = new HashMap<>();
        for (BuddyLineupVo lineupVo : list) {
            Map<Integer, BuddyLineupVo> levelVoMap = map.get(lineupVo.getLineupId());
            if (levelVoMap == null) {
                levelVoMap = new HashMap<>();
                map.put(lineupVo.getLineupId(), levelVoMap);
            }
            levelVoMap.put(lineupVo.getArmsLv(), lineupVo);
        }
//        BuddyManager.lineupLevelMap = map;
        return map;
    }

    private Map<Integer, Map<Integer, BuddyArmsVo>> loadBuddyArmsVo() throws SQLException {
        String sql = "select * from `buddyarms`; ";
        Map<Integer, Map<Integer, BuddyArmsVo>> map = new HashMap<>();
        List<BuddyArmsVo> list = DBUtil.queryList(DBUtil.DB_PRODUCT, BuddyArmsVo.class, sql);
        for (BuddyArmsVo vo : list) {
            Map<Integer, BuddyArmsVo> armsVoMap = map.get(vo.getBuddyId());
            if (armsVoMap == null) {
                armsVoMap = new HashMap<>();
                map.put(vo.getBuddyId(), armsVoMap);
            }
            armsVoMap.put(vo.getArmLevel(), vo);
        }
//        BuddyManager.buddyArmsVoMap = map;
        return map;
    }
}
