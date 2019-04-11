package com.stars.modules.skill;

import com.stars.core.db.DBUtil;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.gm.GmManager;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.scene.event.PassStageEvent;
import com.stars.modules.skill.gm.upRoleSkilllvGmHandler;
import com.stars.modules.skill.listener.LevelUpSkillListener;
import com.stars.modules.skill.listener.PassDungeonListener;
import com.stars.modules.skill.listener.UseToolListener;
import com.stars.modules.skill.prodata.SkillPosition;
import com.stars.modules.skill.prodata.SkillVo;
import com.stars.modules.skill.prodata.SkillvupVo;
import com.stars.modules.skill.summary.SkillSummaryComponentImpl;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.modules.tool.event.UseToolEvent;
import com.stars.services.summary.Summary;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SkillModuleFactory extends AbstractModuleFactory<SkillModule> {
    public SkillModuleFactory() {
        super(new SkillPacketSet());
    }

    @Override
    public SkillModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new SkillModule(id, self, eventDispatcher, map);
    }

    @Override
    public void init() throws Exception {
        Summary.regComponentClass("skill", SkillSummaryComponentImpl.class);
        GmManager.reg("up", new upRoleSkilllvGmHandler());
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        LevelUpSkillListener listener = new LevelUpSkillListener(module);
        eventDispatcher.reg(AddToolEvent.class, listener);
        eventDispatcher.reg(RoleLevelUpEvent.class, listener);
        eventDispatcher.reg(UseToolEvent.class, new UseToolListener((SkillModule) module));
        eventDispatcher.reg(PassStageEvent.class, new PassDungeonListener((SkillModule) module));
    }

    @Override
    public void loadProductData() throws Exception {
        Map<String, SkillvupVo> map = new HashMap<String, SkillvupVo>();
        Map<Integer, Integer> map2 = new HashMap<Integer, Integer>();
        List<SkillvupVo> ls = DBUtil.queryList(DBUtil.DB_PRODUCT, SkillvupVo.class, "select * from skilllvup");
        for (SkillvupVo skillvupVo : ls) {
            map.put(skillvupVo.getSkillId() + "_" + skillvupVo.getLevel(), skillvupVo);
            if (map2.containsKey(skillvupVo.getSkillId())) {
                int level = map2.get(skillvupVo.getSkillId());
                if (level < skillvupVo.getLevel()) {
                    map2.put(skillvupVo.getSkillId(), skillvupVo.getLevel());
                }
            } else {
                map2.put(skillvupVo.getSkillId(), skillvupVo.getLevel());
            }
        }
        SkillManager.setSkillVUPMap(map);
        SkillManager.setMaxSkillLevel(map2);
        loadSkill();
        loadSkillPosition();
    }

    public void loadSkill() throws SQLException {
        String sql = "select * from skill;";
        Map<Integer, SkillVo> skillVoList = DBUtil.queryMap(DBUtil.DB_PRODUCT, "skillid", SkillVo.class, sql);
        Map<Integer, SkillVo> t1 = new ConcurrentHashMap<>();
        for (SkillVo skillVo : skillVoList.values()) {
            t1.put(skillVo.getSkillid(), skillVo);
            SkillvupVo slvVo = SkillManager.getSkillvupVo(skillVo.getSkillid(), 1);
            if (slvVo == null) {
                throw new IllegalArgumentException("skilllvup表配置技能升级数据不存在skillid=" + skillVo.getSkillid());
            }
        }
        SkillManager.setSkillVoMap(t1);
    }

    private void loadSkillPosition() throws SQLException {
        String sql = "select * from skillposition";
        List<SkillPosition> skills = DBUtil.queryList(DBUtil.DB_PRODUCT, SkillPosition.class, sql);
        Map<Integer, Set<Integer>> canLvUpSkill = new HashMap<>();
        Set<Integer> integerSet;
        Map<Integer, Integer> skillPostionMap = new HashMap<>();
        Map<Integer, Map<Integer, SkillPosition>> jobSkillPostionMap = new HashMap<>();
        for (SkillPosition skill : skills) {
            if (canLvUpSkill.containsKey(skill.getJobid())) {
                canLvUpSkill.get(skill.getJobid()).add(skill.getSkillid());
            } else {
                integerSet = new HashSet<>();
                integerSet.add(skill.getSkillid());
                canLvUpSkill.put(skill.getJobid(), integerSet);
            }
            /**
             * 装载技能位置映射
             */
            skillPostionMap.put(skill.getSkillid(), skill.getPosition());
            if (!jobSkillPostionMap.containsKey(skill.getJobid())) {
                jobSkillPostionMap.put(skill.getJobid(), new HashMap<Integer, SkillPosition>());
            }
            Map<Integer, SkillPosition> innerSkillPositionMap = jobSkillPostionMap.get(skill.getJobid());
            innerSkillPositionMap.put(skill.getPosition(), skill);

        }
        SkillManager.setCanLvUpSkill(canLvUpSkill);
        SkillManager.skillPostionMap = skillPostionMap;
        SkillManager.jobSkillPostionMap = jobSkillPostionMap;

    }
}
