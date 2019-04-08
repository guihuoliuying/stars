package com.stars.modules.skill;

import com.stars.core.attr.Attr;
import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.book.BookModule;
import com.stars.modules.data.DataManager;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.fashioncard.FashionCardModule;
import com.stars.modules.newequipment.NewEquipmentManager;
import com.stars.modules.newequipment.NewEquipmentModule;
import com.stars.modules.newequipment.prodata.TokenSkillVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.packet.ClientRole;
import com.stars.modules.role.prodata.Job;
import com.stars.modules.role.prodata.Resource;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.skill.event.SkillBatchLvUpEvent;
import com.stars.modules.skill.event.SkillLevelAchieveEvent;
import com.stars.modules.skill.event.SkillLevelUpEvent;
import com.stars.modules.skill.event.SkillPositionChangeEvent;
import com.stars.modules.skill.packet.ClientRoleSkillFlush;
import com.stars.modules.skill.packet.ClientRoleSkills;
import com.stars.modules.skill.packet.ClientSkillPosition;
import com.stars.modules.skill.prodata.SkillPosition;
import com.stars.modules.skill.prodata.SkillVo;
import com.stars.modules.skill.prodata.SkillvupVo;
import com.stars.modules.skill.summary.SkillSummaryComponentImpl;
import com.stars.modules.skill.userdata.RoleSkill;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.trump.TrumpModule;
import com.stars.services.summary.SummaryComponent;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.util._HashMap;

import java.util.*;

public class SkillModule extends AbstractModule {

    private RoleSkill roleSkill;
    private Attribute skillAttr;// 属性

    public SkillModule(long id, Player self, EventDispatcher eventDispatcher,
                       Map<String, Module> moduleMap) {
        super("技能", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        roleSkill = new RoleSkill(this.id());
        _HashMap map = DBUtil.querySingleMap(DBUtil.DB_USER, "select skilllevel,useskills,pendingskills from roleskill where roleid=" + id());
        String str = "";
        String useStr = "";
        String pendingstr = "";
        if (map != null && map.size() > 0) {
            str = (String) map.get("roleskill.skilllevel");
            useStr = (String) map.get("roleskill.useskills");
            pendingstr = (String) map.get("roleskill.pendingskills");
            roleSkill.setSkillLevelStr(str);
            roleSkill.setUseSkill(useStr);
            roleSkill.setPendingSkills(pendingstr);
        } else {
            //没有数据
            roleSkill.setInsertStatus();
        }

    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        roleSkill = new RoleSkill(this.id());
        roleSkill.setInsertStatus();
        //初始技能
        RoleModule rm = (RoleModule) module(MConst.Role);
        int jobId = rm.getRoleRow().getJobId();
        Job job = RoleManager.getJobById(jobId);
        List<Integer> skills = RoleManager.getResourceById(job.getModelres()).getBornSkill();
        boolean saveDb = false;
        if (skills != null) {
            byte index = 0;
            for (int skillId : skills) {
                if (skillId != 0) {
                    roleSkill.putSkillLevel(skillId, 1);
                    roleSkill.putUseSkill(index, skillId);
                }
                index++;
            }
            saveDb = true;
        }
        skills = job.getBornPassSkill();
        if (skills != null) {
            for (int skillId : skills) {
                roleSkill.putSkillLevel(skillId, 1);
            }
            saveDb = true;
        }
        if (saveDb) {
            this.context().insert(roleSkill);
        }
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        dealUseSkillAndPendingSkill();
        upAllRoleSkill();
        // 标记需要计算的红点
        signCalRedPoint(MConst.Skill, RedPointConst.SKILL_LVUP);
    }

    @Override
    public void onSyncData() throws Throwable {
        updatePassEffect(false);
        updateSkillFightScore(false);
        updateRoleSkill();
        if (roleSkill != null) { //成就达成登陆检测
            for (Map.Entry<Integer, Integer> entry : roleSkill.getSkillLevelMap().entrySet()) {
                eventDispatcher().fire(new SkillLevelAchieveEvent(entry.getKey(), entry.getValue()));
            }
        }

    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(Integer.valueOf(RedPointConst.SKILL_LVUP))) {
            checkRedPointLvUp(redPointMap);
        }
    }

    @Override
    public void onUpateSummary(Map<String, SummaryComponent> componentMap) {
        if (!SpecialAccountManager.isSpecialAccount(id())) {
            componentMap.put("skill", new SkillSummaryComponentImpl(getUseSkill(), getSkillDamageMap(), roleSkill.getUseSkillMap()));
        }
    }

    /**
     * 获取指定技能的总伤害值;(区别于skillvupVo, 这个包含了其他系统对该技能的加成值)
     */
    public int getSkillTotalDamage(int skillId) {
        int skillLevel = roleSkill.getSkillLv(skillId);
        SkillvupVo skillvupVo = SkillManager.getSkillvupVo(skillId, skillLevel);
        if (skillvupVo != null) {
            int rtnValue = skillvupVo.getDamage();
            return rtnValue;
        }
        return -1;
    }

    /**
     * 获取指定技能的总伤害值;(区别于skillvupVo, 这个包含了其他系统对该技能的加成值)
     */
    public int getSkillTotalDamage(int skillId, int skillLevel) {
        SkillvupVo skillvupVo = SkillManager.getSkillvupVo(skillId, skillLevel);
        if (skillvupVo != null) {
            int rtnValue = skillvupVo.getDamage();
            return rtnValue;
        }
        return -1;
    }

    private void dealUseSkillAndPendingSkill() {
        SkillvupVo vo;
        for (Map.Entry<Integer, Integer> entry : roleSkill.getSkillLevelMap().entrySet()) {
            vo = SkillManager.getSkillvupVo(entry.getKey(), entry.getValue());
            if (vo.getSkillType() == SkillConstant.LVUP_SKILLTYPE_PASS) {
                if (!roleSkill.getUseSkillMap().containsValue(entry.getKey())) {
                    roleSkill.addPendingSkill(entry.getKey());
                } else {
                    if (roleSkill.getPendingSkillSet().contains(entry.getKey())) {
                        roleSkill.delPendingSkill(entry.getKey());
                    }
                }
            }
        }
        context().update(roleSkill);
    }

    /**
     * 获取计算后的damagedesc值
     */
    public void setCalDamagedesc() {
        Map<Integer, Integer> map = new HashMap<>();
        RoleModule rm = (RoleModule) module(MConst.Role);
        int jobId = rm.getRoleRow().getJobId();
        Set<Integer> skillSet = SkillManager.getCanLvUpSkill().get(jobId);
        for (Integer skillId : skillSet) {
            map.put(skillId, roleSkill.getSkillLv(skillId));
        }
    }


    /**
     * 发送所有的技能数据到客户端
     */
    public void sendSkillList2Client() {
        setCalDamagedesc();
        Map<SkillVo, Integer> map = new HashMap<SkillVo, Integer>();
        RoleModule rm = (RoleModule) module(MConst.Role);
        int jobId = rm.getRoleRow().getJobId();
        Job job = RoleManager.getJobById(jobId);
        List<Integer> skills = RoleManager.getResourceById(job.getModelres()).getSkillList();
        for (Integer skillId : skills) {
            SkillVo sVo = SkillManager.getSkillVo(skillId);
            map.put(sVo, roleSkill.getSkillLv(skillId));
        }
        skills = job.getPSkillList();
        if (skills != null) {
            for (Integer skillId : skills) {
                map.put(SkillManager.getSkillVo(skillId), roleSkill.getSkillLv(skillId));
            }
        }

        int roleAttack = rm.getRoleRow().getTotalAttr().getAttack();

        ClientRoleSkills clientRoleSkill = new ClientRoleSkills();
        clientRoleSkill.setSkillMap(map);
        clientRoleSkill.setRoleAttack(roleAttack);
        FashionCardModule cardModule = module(MConst.FashionCard);
        clientRoleSkill.setSkillCDMap(cardModule.getSkill_cd_Map());
        send(clientRoleSkill);
    }

    /**
     * 自动激活/升级
     */
    public void autoLevelUp(Integer skillId, RoleModule rm) {
        int curLevel = roleSkill.getSkillLv(skillId);
        int maxLevel = SkillManager.getMaxSkillLevel(skillId);
        if (curLevel >= maxLevel) {
            return;
        }
        SkillvupVo svv = SkillManager.getSkillvupVo(skillId, curLevel + 1);
        if (svv.getReqlv() != 0) {
            if (rm.getLevel() < svv.getReqlv()) {
                return;
            }
        }
        if (svv.getReqitem() != null) {
            return;
        }

        if (svv.getReqskilllevelMap().size() != 0) {
            return;
        }

        if (svv.getReqdungeon() != 0) {
            DungeonModule dungeon = (DungeonModule) moduleMap().get(MConst.Dungeon);
            if (!dungeon.isPassDungeon(svv.getReqdungeon())) {
                return;
            }
        }

        if (svv.getReqbook() != 0) {
            BookModule bookModule = (BookModule) moduleMap().get(MConst.Book);
            if (!bookModule.isBookActive(svv.getReqbook())) {
                return;
            }
        }

        if (isTokenSkill(svv.getSkillId()))
            return;


        //等级足够且无需道具则自动激活/升级
        upRoleSkill(skillId);
    }

    /**
     * 检测是否可以自动激活/升级,人物升级/通关时触发
     */
    public void upAllRoleSkill() {
        RoleModule rm = module(MConst.Role);
        //被动技能
        int jobId = rm.getRoleRow().getJobId();
        Job job = RoleManager.getJobById(jobId);
        List<Integer> skillList = job.getPSkillList();
        for (Integer skillId : skillList) {
            autoLevelUp(skillId, rm);
        }
        Map<Integer, TokenSkillVo> tokenSkillVoMap = NewEquipmentManager.getTokenSkillVoMap();
        for (Integer skillId : tokenSkillVoMap.keySet()) {
            updateTokenPassSkillLv(skillId, rm, true);
        }
        //主动技能
        List<Integer> skills = RoleManager.getResourceById(job.getModelres()).getSkillList();
        for (Integer skillId : skills) {
            SkillVo sv = SkillManager.getSkillVo(skillId);
            if (sv.getType() == SkillConstant.TYPE_SKILL || sv.getType() == SkillConstant.TYPE_ULTIMATE) {
                autoLevelUp(skillId, rm);
            }
        }
    }

    public int getUltimateSkillId() {
        RoleModule rm = module(MConst.Role);
        int jobId = rm.getRoleRow().getJobId();
        Job job = RoleManager.getJobById(jobId);
        List<Integer> skills = RoleManager.getResourceById(job.getModelres()).getSkillList();
        for (Integer skillId : skills) {
            SkillVo sv = SkillManager.getSkillVo(skillId);
            if (sv.getType() == SkillConstant.TYPE_ULTIMATE) {
                return sv.getSkillid();
            }
        }
        return -1;
    }

    /**
     * @param skillId 技能升级
     */
    public void upRoleSkill(int skillId) {
        int curLevel = roleSkill.getSkillLv(skillId);
        SkillvupVo svv = upSkillLv(skillId);
        if (svv == null) {
            ClientRoleSkillFlush skillFlush = new ClientRoleSkillFlush();
            skillFlush.setVoMap(new HashMap<Integer, SkillvupVo>());
            send(skillFlush);
            warn("不满足升级条件"); // 简单提示
            return;
        }
        if (svv.getSkillType() == SkillConstant.LVUP_SKILLTYPE_PASS) {
            updatePassEffect(false);
        }
        updateSkillFightScore(true);
        updateRoleSkill();
        this.context().update(roleSkill);
        flushSkill2Client(skillId, svv);
        eventDispatcher().fire(new SkillLevelUpEvent(skillId, curLevel + 1));
        eventDispatcher().fire(new SkillLevelAchieveEvent(skillId, curLevel + 1));
        signCalRedPoint(MConst.Skill, RedPointConst.SKILL_LVUP);
    }

    /**
     * 升级技能
     *
     * @param skillId
     * @return
     */
    private SkillvupVo upSkillLv(int skillId) {
        int curLevel = roleSkill.getSkillLv(skillId);
        int maxLevel = SkillManager.getMaxSkillLevel(skillId);
        if (curLevel >= maxLevel) {
            return null;
        }
        SkillvupVo svv = SkillManager.getSkillvupVo(skillId, curLevel + 1);
        if (svv.getReqlv() != 0) {
            RoleModule rm = (RoleModule) module(MConst.Role);
            if (rm.getLevel() < svv.getReqlv()) {
                return null;
            }
        }
        if (svv.getReqdungeon() != 0) {
            DungeonModule dungeon = (DungeonModule) moduleMap().get(MConst.Dungeon);
            if (!dungeon.isPassDungeon(svv.getReqdungeon())) {
                return null;
            }
        }
        if (svv.getReqbook() != 0) {
            BookModule bookModule = (BookModule) moduleMap().get(MConst.Book);
            if (!bookModule.isBookActive(svv.getReqbook())) {
                return null;
            }
        }
        if (isTokenSkill(svv.getSkillId())) {
            return null;
        }
        ToolModule tm = (ToolModule) module(MConst.Tool);
        if (svv.getReqitem() != null) {
            Iterator<Integer> it = svv.getReqitem().keySet().iterator();
            while (it.hasNext()) {
                int itemId = it.next();
                int count = svv.getReqitem().get(itemId);
                if (!tm.contains(itemId, count)) {
                    return null;
                }
            }
        }
        if (svv.getReqskilllevelMap().size() != 0) {
            for (Map.Entry<Integer, Integer> entry : svv.getReqskilllevelMap().entrySet()) {
                if (roleSkill.getSkillLv(entry.getKey()) < entry.getValue()) {
                    return null;
                }
            }
        }
        roleSkill.upSkillLevel(skillId);
        if (svv.getReqitem() != null) {
            tm.deleteAndSend(svv.getReqitem(), EventType.UPSKILL.getCode());
        }
        if (svv.getSkillType() == SkillConstant.LVUP_SKILLTYPE_PASS && roleSkill.getSkillLevelMap().containsKey(skillId)) {
            if (!roleSkill.getUsePassSkillList().containsKey(skillId)) {
                roleSkill.addPendingSkill(svv.getSkillId());
            }
        }
        return svv;
    }


    /**
     * 计算伤害值
     *
     * @param svv
     * @return
     */
    private int damagedesc(SkillvupVo svv) {
        RoleModule roleModule = (RoleModule) moduleMap().get("role");
        int calcdamagedesc = 0;
        String damagedesc = svv.getDamagedesc();
        if (damagedesc != null) {
            String[] strings = damagedesc.split("\\+");
            if (strings.length == 2) {
                int a = Integer.parseInt(strings[0]);
                int b = Integer.parseInt(strings[1]);
                calcdamagedesc = (roleModule.getRoleRow().getTotalAttr().getAttack() * svv.getCoefficient() / 1000 + svv.getDamage()) * a + b;
            }
        }
        return calcdamagedesc;
    }

    /**
     * 一键升级所有技能
     */
    public void upAllRoleSkillLv() {
        Set<Integer> cantLvUps = new HashSet<>();//所有升级失败的技能
        Map<Integer, SkillvupVo> lvUps = new HashMap<>();//所有升级的技能
        Map<Integer, Map<Integer, Integer>> reqSkillLevel = new HashMap<>();
        RoleModule role = (RoleModule) moduleMap().get(MConst.Role);
        //当所有技能无法升级时退出循环
        while (cantLvUps.size() < roleSkill.getSkillLevelMap().size()) {
            int skillId = getNextLevelUpSkillId(cantLvUps);
            if (!SkillManager.getCanLvUpSkill().get(role.getRoleRow().getJobId()).contains(skillId)) {
                cantLvUps.add(skillId);
                continue;
            }
            if (cantLvUps.contains(skillId)) continue;
            SkillvupVo vo = upSkillLv(skillId);
            if (vo == null) {
                cantLvUps.add(skillId);
                reqSkillLevel.put(skillId, getReqskilllevelMap(skillId));
            } else {
                lvUps.put(skillId, vo);
                int cantLvSkillId = reqSkillLevel(skillId, reqSkillLevel);
                //当无法升级的技能发现其前置技能达到要求时，则重新加入升级技能的队伍中
                if (cantLvSkillId != 0) {
                    cantLvUps.remove(cantLvSkillId);
                }
            }
        }
        boolean havPassSkill = false;
        Map<Integer, Integer> lvMap = new HashMap<>();
        for (SkillvupVo vo : lvUps.values()) {
            if (vo.getSkillType() == SkillConstant.LVUP_SKILLTYPE_PASS) {
                havPassSkill = true;
            }
            lvMap.put(vo.getSkillId(), vo.getLevel());
        }
        eventDispatcher().fire(new SkillBatchLvUpEvent(lvMap));
        if (havPassSkill) {
            updatePassEffect(false);
        }
        updateSkillFightScore(true);
        updateRoleSkill();
        this.context().update(roleSkill);
        flushSkill2Client(lvUps);
        signCalRedPoint(MConst.Skill, RedPointConst.SKILL_LVUP);
    }

    /**
     * 前置技能容器
     * 如果该技能的升级被前置技能所限制,那么就会缓存到map中，方便判断
     *
     * @param skillId
     * @return
     */
    private Map<Integer, Integer> getReqskilllevelMap(int skillId) {
        SkillvupVo skillvupVo = SkillManager.getSkillvupVo(skillId, roleSkill.getSkillLv(skillId));
        return skillvupVo.getReqskilllevelMap();
    }

    /**
     * Map<skillId,Map<reqSkillId,reqSkillLevel>>
     * 前置技能
     * 如果该技能的升级被前置技能所限制，那么当该技能的前置技能达到要求时，会返回该技能
     *
     * @return
     */
    private int reqSkillLevel(int skillId, Map<Integer, Map<Integer, Integer>> reqSkillLevel) {
        int level = roleSkill.getSkillLv(skillId);
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : reqSkillLevel.entrySet()) {
            for (Map.Entry<Integer, Integer> skillLevel : entry.getValue().entrySet()) {
                if (skillId == skillLevel.getKey()) {
                    if (level >= skillLevel.getValue()) {
                        return entry.getKey();
                    }
                }
            }
        }
        return 0;
    }

    /**
     * 获得下一个可升级的技能Id
     * 取等级最小的技能，判断不能存在于cantLvups
     *
     * @return skillId
     */
    public int getNextLevelUpSkillId(Set<Integer> cantLvUps) {
        int nextSkillLevel = 0;
        int nextSkillId = 0;
        for (Map.Entry<Integer, Integer> entry : roleSkill.getSkillLevelMap().entrySet()) {
            if (cantLvUps.contains(entry.getKey())) continue;
            if (nextSkillId == 0) nextSkillId = entry.getKey();
            if (nextSkillLevel == 0) nextSkillLevel = entry.getValue();
            if (entry.getValue() < nextSkillLevel) {
                nextSkillLevel = entry.getValue();
                nextSkillId = entry.getKey();
            } else if (entry.getValue() == nextSkillLevel) {
                if (entry.getKey() < nextSkillId) {
                    nextSkillLevel = entry.getValue();
                    nextSkillId = entry.getKey();
                }
            }
        }
        return nextSkillId;
    }

    private Map<Integer, Set<Integer>> getLvSkillMap() {
        Map<Integer, Set<Integer>> lvSkillMap = new HashMap<>();//<level,Set<skillId>>
        for (Map.Entry<Integer, Integer> entry : roleSkill.getSkillLevelMap().entrySet()) {
            Set<Integer> skillSet = lvSkillMap.get(entry.getValue());
            if (skillSet == null) {
                skillSet = new HashSet<>();
                lvSkillMap.put(entry.getValue(), skillSet);
            }
            skillSet.add(entry.getKey());
        }
        return lvSkillMap;
    }


    /**
     * @param skillId 更新单个技能到客户端，常用于升级后
     */
    public void flushSkill2Client(int skillId, SkillvupVo svv) {
        Map<Integer, SkillvupVo> skillLvUpMap = new HashMap<>();
        skillLvUpMap.put(skillId, svv);
        flushSkill2Client(skillLvUpMap);
    }

    public void flushSkill2Client(Map<Integer, SkillvupVo> skillLvUpMap) {
        Map<Integer, String> reqItemStr = new HashMap<>();
//        Map<Integer, Integer> calcDamege = new HashMap<>();
//        Map<Integer, Integer> nextLvCalcDamege = new HashMap<>();
        Map<Integer, String> nextLvReqSkillLv = new HashMap<>();
        Map<Integer, Integer> nextReqLv = new HashMap<>();
        Map<Integer, SkillvupVo> nextVoMap = new HashMap<Integer, SkillvupVo>();
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        int roleAttack = roleModule.getRoleRow().getTotalAttr().getAttack();

        //todo:这里加上下一等级的calcdamege
        for (int skillId : skillLvUpMap.keySet()) {
            int level = roleSkill.getSkillLv(skillId);
            SkillvupVo vvo = SkillManager.getSkillvupVo(skillId, level);
//            calcDamege.put(skillId, damagedesc(vvo));
            SkillvupVo nextVvo = SkillManager.getSkillvupVo(skillId, level + 1);
            reqItemStr.put(skillId, nextVvo == null ? "" : nextVvo.getReqItemStr());
//            nextLvCalcDamege.put(skillId, nextVvo == null ? -1 : damagedesc(nextVvo));
            nextLvReqSkillLv.put(skillId, nextVvo == null ? "" : nextVvo.getReqskilllevel());
            nextReqLv.put(skillId, nextVvo == null ? -1 : nextVvo.getReqlv());
            nextVoMap.put(skillId, nextVvo == null ? vvo : nextVvo);
        }
        ClientRoleSkillFlush skillFlush = new ClientRoleSkillFlush();
        skillFlush.setReqItemStr(reqItemStr);
        skillFlush.setVoMap(skillLvUpMap);
//        skillFlush.setCalcDamege(calcDamege);
        skillFlush.setNextLvReqSkillLv(nextLvReqSkillLv);
//        skillFlush.setNextCalcDamege(nextLvCalcDamege);
        skillFlush.setNextReqLv(nextReqLv);
        skillFlush.setNextVoMap(nextVoMap);
        skillFlush.setRoleAttack(roleAttack);
        FashionCardModule cardModule = module(MConst.FashionCard);
        skillFlush.setSkillCDMap(cardModule.getSkill_cd_Map());
        send(skillFlush);
    }

    private void updateRoleSkill() {
        RoleModule rolemodule = (RoleModule) this.module(MConst.Role);
        ClientRole clientrole = new ClientRole(ClientRole.UPDATE_SKILL, rolemodule.getRoleRow());
        clientrole.setNormalSkill(getNormalSkill(rolemodule.getRoleRow().getJobId()));
        clientrole.setSkill(getSkillStr());
        clientrole.setPSkill(getPassSkill());
        send(clientrole);
    }

    private void updateSkillFightScore(boolean needSend) {
        int fightScore = 0;
        Map<Integer, Integer> skillList = roleSkill.getSkillLevelMap();
        if (skillList != null) {
            Set<Map.Entry<Integer, Integer>> set = skillList.entrySet();
            SkillvupVo vvo = null;
            Attribute passAttr = new Attribute();
            RoleModule rm = (RoleModule) module(MConst.Role);
            Attribute baseAttr = rm.getRoleRow().getAttrMap().get(RoleManager.ROLEATTR_GRADEBASE);
            for (Map.Entry<Integer, Integer> entry : set) {
                if (entry.getValue() > 0) {
                    vvo = SkillManager.getSkillvupVo(entry.getKey(), entry.getValue());
                    fightScore = fightScore + vvo.getBattlepower();
                }
            }
            if (skillAttr != null)
                fightScore = fightScore + FormularUtils.calFightScore(skillAttr);//加上被动技能效果所产生的战力
        }
        RoleModule rm = (RoleModule) module(MConst.Role);
        rm.updatePartFightScore(RoleManager.FIGHTSCORE_SKILL, fightScore);
        if (needSend == true) {
            rm.sendRoleAttr();
            rm.sendUpdateFightScore();
        }
    }

    /**
     * 更新被动技能的效果
     */
    public void updatePassEffect(boolean forceSycn) {
        Map<Integer, Integer> skillList = roleSkill.getSkillLevelMap();
        if (skillList != null) {
            Set<Map.Entry<Integer, Integer>> set = skillList.entrySet();
            SkillvupVo vvo = null;
            RoleModule rm = (RoleModule) module(MConst.Role);
            Attribute baseAttr = rm.getRoleRow().getAttrMap().get(RoleManager.ROLEATTR_GRADEBASE);
            if (baseAttr == null) {
                return;
            }
            skillAttr = new Attribute();
            boolean isNeedUpdate = false;
            for (Map.Entry<Integer, Integer> entry : set) {
                vvo = SkillManager.getSkillvupVo(entry.getKey(), entry.getValue());
                if (vvo == null) continue;
                if (vvo.getSkillType() == SkillConstant.TRUMP_SKILLTYPE_PASS) {
                    isNeedUpdate = updatePaskSkillEffect(vvo, baseAttr, isNeedUpdate);
                }
                if (vvo.getSkillType() == SkillConstant.LVUP_SKILLTYPE_PASS && roleSkill.getUsePassSkillList().containsKey(vvo.getSkillId())) {
                    isNeedUpdate = updatePaskSkillEffect(vvo, baseAttr, isNeedUpdate);
                }
            }
            if (isNeedUpdate == true || forceSycn == true) {
                rm.updatePartAttr(RoleManager.ROLEATTR_SKILL, skillAttr);
                rm.sendRoleAttr();
            }
        }
    }

    private boolean updatePaskSkillEffect(SkillvupVo vvo, Attribute baseAttr, boolean isNeedUpdate) {
        String[] tempStr = vvo.getEffectinfo().split("\\|");
        if (Integer.parseInt(tempStr[0]) == SkillConstant.PASS_EFFECT_ATTRIBUTE) {
            isNeedUpdate = true;
            String[] sts = tempStr[1].split("\\+");
            int value = baseAttr.getAttributes()[Attr.getIndexByteEn(sts[0])];
            value = (int) (value * Integer.parseInt(sts[1]) * 0.001 + Integer.parseInt(sts[2]));
            skillAttr.addSingleAttr(Attr.getIndexByteEn(sts[0]), value);
        }
        return isNeedUpdate;
    }

    /**
     * 获得所有使用技能+普攻
     *
     * @return
     */
    public Map<Integer, Integer> getSkill() {
        Map<Integer, Integer> skillMap = new HashMap<>();
        Map<Byte, Integer> useList = roleSkill.getUseSkillMap();
        if (useList != null && useList.size() > 0) {
            Set<Map.Entry<Byte, Integer>> set = useList.entrySet();
            for (Map.Entry<Byte, Integer> entry : set) {
                int level = roleSkill.getSkillLv(entry.getValue());
                if (level > 0) {
                    skillMap.put(entry.getValue(), level);
                }
            }
        }
        // 加入普攻
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        Resource resource = RoleManager.getResourceById(RoleManager.getJobById(roleModule.getRoleRow().getJobId()).getModelres());
        for (int normalSkillId : resource.getNormalSkill()) {
            skillMap.put(normalSkillId, SkillConstant.ROLE_NORMAL_LEVEL);
        }
        return skillMap;
    }

    public String getSkillStr() {
        String str = "";
        Map<Byte, Integer> useList = this.roleSkill.getUseSkillMap();
        if (useList != null && useList.size() > 0) {
            Set<Map.Entry<Byte, Integer>> set = useList.entrySet();
            int level = 1;
            byte index = 0;
            for (Map.Entry<Byte, Integer> entry : set) {
                if (index > 0) {
                    str = str + "|";
                }
                level = this.roleSkill.getSkillLv(entry.getValue());
                str = str + entry.getKey() + "=" + entry.getValue() + "=" + level;
                index++;
            }
        }
        return str;
    }

    public String getPassSkill() {
        return roleSkill.getUsePassSkillStr();
    }

    public Map<Integer, Integer> getPassSkillList() {
        return roleSkill.getUsePassSkillList();
    }

    public String getNormalSkill(int JobId) {
        Job curJob = RoleManager.getJobById(JobId);
        Resource curResource = RoleManager.getResourceById(curJob.getModelres());
        List<Integer> skillList = curResource.getNormalSkill();
        String skillStr = "";
        if (skillList != null) {
            byte index = 0;
            for (int skillid : skillList) {
                if (index > 0) {
                    skillStr = skillStr + "+";
                }
                skillStr = skillStr + skillid;
                index++;
            }
        }
        return skillStr;
    }

    public List<Integer> getNormalList(int JobId) {
        Job curJob = RoleManager.getJobById(JobId);
        Resource curResource = RoleManager.getResourceById(curJob.getModelres());
        return curResource.getNormalSkill();
    }

    /**
     * 设置技能位置
     *
     * @param skillId  技能Id
     * @param position 技能位置
     */
    public void setSkillPosition(int skillId, byte position) {
        ClientSkillPosition cp = new ClientSkillPosition();
        HashMap<Integer, Byte> map = new HashMap<>();
        int skillType = SkillManager.getSkillvupVo(skillId, 1).getSkillType();
        if (position == 4) { //闪避不可替换
            cp.setMap(new HashMap<Integer, Byte>());
            send(cp);
            return;
        }
        //如果是法宝技能，不作处理
        if (skillType == SkillConstant.TRUMP_SKILLTYPE_PASS) {
            cp.setMap(new HashMap<Integer, Byte>());
            send(cp);
            return;
        }
        //如果是被动技能
        if (skillType == SkillConstant.LVUP_SKILLTYPE_PASS) {
            if (position < 5 || position > 6) {
                cp.setMap(new HashMap<Integer, Byte>());
                send(cp);
                return;
            }
            if (!isOpenPassSkillPosition(position)) {
                cp.setMap(new HashMap<Integer, Byte>());
                send(cp);
                return;
            }
            if (roleSkill.getUseSkillMap().containsValue(skillId)) {
                byte oldPosition = this.roleSkill.getSkillPostion(skillId);
                changePosition(skillId, position, map, oldPosition);
            }
            if (!roleSkill.getUseSkillMap().containsValue(skillId)) {
                int oldSkillid = this.roleSkill.getSkillId(position);
                if (oldSkillid != 0) {
                    roleSkill.addPendingSkill(oldSkillid);
                }
                roleSkill.putUseSkill(position, skillId);
                map.put(skillId, position);
            }
            if (roleSkill.getPendingSkillSet().contains(skillId)) {
                roleSkill.delPendingSkill(skillId);
                if (isOpenPassSkillPosition(position)) {
                    roleSkill.putUseSkill(position, skillId);
                    map.put(skillId, position);
                }
            }
        }
        if (skillType == SkillConstant.UNIQUE_SKILL) {
            if (position != 3) {
                cp.setMap(new HashMap<Integer, Byte>());
                send(cp);
                return;
            }
            int oldSkillid = this.roleSkill.getSkillId(position);
            if (oldSkillid == skillId) {
                cp.setMap(new HashMap<Integer, Byte>());
                send(cp);
                return;
            }
            roleSkill.putUseSkill(position, skillId);
            map.put(skillId, position);

        }
        if (skillType == SkillConstant.ACTIVE_SKILL) {
            if (position < 0 || position > 2) {
                cp.setMap(new HashMap<Integer, Byte>());
                send(cp);
                return;
            }
            byte oldPosition = this.roleSkill.getSkillPostion(skillId);
            changePosition(skillId, position, map, oldPosition);
        }
        cp.setMap(map);
        send(cp);
        this.context().update(this.roleSkill);
        updatePassEffect(true);
        updateSkillFightScore(true);
        updateRoleSkill();
        updateSkillSummary();
        eventDispatcher().fire(new SkillPositionChangeEvent());
    }

    private void changePosition(int skillId, byte position, HashMap<Integer, Byte> map, byte oldPosition) {
        if (oldPosition != -1) {
            this.roleSkill.clearPosition(oldPosition);
            int oldSkillid = this.roleSkill.getSkillId(position);
            if (oldSkillid != 0) {
                this.roleSkill.putUseSkill(oldPosition, oldSkillid);
                map.put(oldSkillid, oldPosition);
            } else {
                map.put(-1, oldPosition);
            }
            this.roleSkill.putUseSkill(position, skillId);
            map.put(skillId, position);
        } else {
            this.roleSkill.putUseSkill(position, skillId);
            map.put(skillId, position);
        }
    }

    /**
     * 判断相应的被动技能栏是否开启
     *
     * @param position
     * @return
     */
    public boolean isOpenPassSkillPosition(byte position) {
        String[] positionStr = DataManager.getCommConfig("skill_passskillopen").split("\\+");
        RoleModule role = (RoleModule) moduleMap().get(MConst.Role);
        if (role.getLevel() >= Integer.parseInt(positionStr[position - 5])) {
            return true;
        }
        return false;
    }

    private int openPassSkillPositionNum() {
        int num = 0;
        if (isOpenPassSkillPosition((byte) 5)) {
            num++;
        }
        if (isOpenPassSkillPosition((byte) 6)) {
            num++;
        }
        return num;
    }


    /**
     * 一键设置技能（位置）
     */
    public void autoSetSkillPosition() {
        Map<Integer, Integer> map = roleSkill.getSkillLevelMap();
        if (map == null || map.size() <= 0) {
            return;
        }
        //先把主动技能筛选出来（被动技能不需要设置位置）
        // 2017/1/18，被动技能也需要设置位置
        LinkedList<SkillvupVo> listPassSKill = new LinkedList<>();
        LinkedList<SkillvupVo> listActiveSKill = new LinkedList<>();
        LinkedList<SkillvupVo> listUniqueSKill = new LinkedList<>();
        Iterator<Integer> it = map.keySet().iterator();
        RoleModule role = module(MConst.Role);

        while (it.hasNext()) {
            int skillId = it.next();
            int level = map.get(skillId);
            if (SkillManager.getCanLvUpSkill().get(role.getRoleRow().getJobId()).contains(skillId)) {
                SkillvupVo vvo = SkillManager.getSkillvupVo(skillId, level);
                if (vvo.getSkillType() == SkillConstant.LVUP_SKILLTYPE_PASS) {
                    listPassSKill.add(vvo);
                }
                if (vvo.getSkillType() == SkillConstant.ACTIVE_SKILL) {
                    listActiveSKill.add(vvo);
                }
                if (vvo.getSkillType() == SkillConstant.UNIQUE_SKILL) {
                    listUniqueSKill.add(vvo);
                }
            }
        }
        //按战力排个序
        Collections.sort(listPassSKill);
        Collections.sort(listActiveSKill);
        Collections.sort(listUniqueSKill);

        //按战力从高到低排位置
        Map<Integer, Byte> tmp = new HashMap<>();
        for (int i = 0; i < openPassSkillPositionNum() && i < listPassSKill.size(); i++) {
            SkillvupVo vo = listPassSKill.get(i);
            if (roleSkill.getPendingSkillSet().contains(vo.getSkillId())) {
                roleSkill.delPendingSkill(vo.getSkillId());
            }
            byte position = roleSkill.getSkillPostion(vo.getSkillId());
            if (position != -1 && position != i + 5) {
                roleSkill.clearPosition(position);
                tmp.put(-1, position);
            }
            roleSkill.putUseSkill((byte) (i + 5), vo.getSkillId());
            tmp.put(vo.getSkillId(), (byte) (i + 5));
            if (tmp.containsKey(-1) && tmp.get(-1) == (byte) (i + 5)) {
                tmp.remove(-1);
            }
        }

        LogUtil.info("roleSkill:{}", roleSkill.getUseSkillMap());
        int loopTime = 0;
        for (int i = 2; i >= 0; i--) {
            SkillvupVo vo = listActiveSKill.get(loopTime);
            roleSkill.putUseSkill((byte) i, vo.getSkillId());
            tmp.put(vo.getSkillId(), (byte) i);
            loopTime++;
        }
        if (!listUniqueSKill.isEmpty()) {
            roleSkill.putUseSkill((byte) 3, listUniqueSKill.getFirst().getSkillId());
            tmp.put(listUniqueSKill.getFirst().getSkillId(), (byte) 3);
        }
        context().update(roleSkill);
        ClientSkillPosition cp = new ClientSkillPosition();
        cp.setMap(tmp);
        send(cp);
        updatePassEffect(false);
        updateSkillFightScore(false);
        updateRoleSkill();
        updateSkillSummary();
        eventDispatcher().fire(new SkillPositionChangeEvent());
    }

    /**
     * 获得当前使用技能(含普攻)+被动技能
     *
     * @return id, level
     */
    public Map<Integer, Integer> getUseSkill() {
        Map<Integer, Integer> map = new HashMap<>();
        map.putAll(getSkill());
        map.putAll(getPassSkillList());
        return map;
    }

    /**
     * 获得计算后的技能伤害值
     *
     * @return <skillid,damage>
     */
    public Map<Integer, Integer> getSkillDamageMap() {
        Map<Integer, Integer> map = new HashMap<>();
        for (int skillId : getUseSkill().keySet()) {
            map.put(skillId, getSkillTotalDamage(skillId));
        }
        return map;
    }

    /**
     * 获得计算后的技能伤害值
     *
     * @return <skillid,damage>
     */
    public Map<Integer, Integer> getSkillDamageMap(Map<Integer, Integer> skillMap) {
        Map<Integer, Integer> map = new HashMap<>();
        if (StringUtil.isNotEmpty(skillMap)) {
            for (int skillId : skillMap.keySet()) {
                map.put(skillId, getSkillTotalDamage(skillId, skillMap.get(skillId)));
            }
        }
        return map;
    }


    /**
     * 更新法宝技能
     *
     * @param skillLevelMap
     */
    public void updateTrumpSkill(Map<Integer, Integer> skillLevelMap) {
        Map<Integer, Integer> roleLevelMap = roleSkill.getSkillLevelMap();
        boolean change = false;
        for (Map.Entry<Integer, Integer> entry : skillLevelMap.entrySet()) {
            change = true;
            if (entry.getValue() <= 0) {
                roleLevelMap.remove(entry.getKey());
            } else {
                roleSkill.putSkillLevel(entry.getKey(), entry.getValue());
            }
        }
        if (change == false) {
            return;
        }
        updatePassEffect(false);
        updateSkillFightScore(true);
        updateRoleSkill();
        context().update(roleSkill);
        updateSkillSummary();
    }

    /**
     * 获取法宝被动技能数值
     */
    public Map<Integer, String> getTrumpPassSkillAttr() {
        TrumpModule tm = (TrumpModule) module(MConst.Trump);
        return tm.getTrumpSkillDamage();
    }

    /**
     * 技能等级，没有学习该技能返回0
     *
     * @param skillId
     * @return
     */
    public int getSkillLevel(int skillId) {
        Integer level = roleSkill.getSkillLevelMap().get(skillId);
        return level == null ? 0 : level;
    }

    /**
     * 更新技能常用数据
     */
    private void updateSkillSummary() {
//        try {
//            ServiceHelper.summaryService().updateSummaryComponent(id(),
//                    new SkillSummaryComponentImpl(getUseSkill(), getSkillDamageMap(), roleSkill.getUseSkillMap()));
//        } catch (Exception e) {
//        }
        context().markUpdatedSummaryComponent(MConst.Skill);
    }

    private boolean isTrumpSkill(int skillId) {
        TrumpModule module = (TrumpModule) moduleMap().get(MConst.Trump);
        Set<Integer> set = module.getSkillIdList();
        if (set != null) {
            for (int s : set) {
                if (skillId == s) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTokenSkill(int skillId) {
        TokenSkillVo tokenSkillVo = NewEquipmentManager.getTokenSkillVoBySkillId(skillId);
        if (tokenSkillVo == null) //不是符文被动技能
            return false;
        else
            return true;
    }

    /**
     * 检查技能是否可升级
     */
    private void checkRedPointLvUp(Map<Integer, String> redPointMap) {
        StringBuilder builder = new StringBuilder("");
        Map<Integer, Integer> skillLvMap = roleSkill.getSkillLevelMap();
        RoleModule rm = module(MConst.Role);
        ToolModule tm = module(MConst.Tool);
        boolean itemMark;
        long st = System.currentTimeMillis();
        for (Map.Entry<Integer, Integer> entry : skillLvMap.entrySet()) {
            itemMark = true;
            int maxLevel = SkillManager.getMaxSkillLevel(entry.getKey());
            if (entry.getValue() >= maxLevel) {
                continue;
            }
            if (isTrumpSkill(entry.getKey())) {//去除法宝技能
                continue;
            }
            SkillvupVo svv = SkillManager.getSkillvupVo(entry.getKey(), entry.getValue() + 1);
            if (svv.getReqlv() != 0) {
                if (rm.getLevel() < svv.getReqlv()) {
                    continue;
                }
            }
            if (svv.getReqdungeon() != 0) {
                DungeonModule dungeon = (DungeonModule) moduleMap().get(MConst.Dungeon);
                if (!dungeon.isPassDungeon(svv.getReqdungeon())) {
                    continue;
                }
            }
            if (svv.getReqitem() != null) {
                Iterator<Integer> it = svv.getReqitem().keySet().iterator();
                while (it.hasNext()) {
                    int itemId = it.next();
                    int count = svv.getReqitem().get(itemId);
                    if (!tm.contains(itemId, count)) {
                        itemMark = false;
                        break;
                    }
                }
            }
            if (svv.getReqskilllevelMap() != null) {
                for (Map.Entry<Integer, Integer> reqSkillLvEntry : svv.getReqskilllevelMap().entrySet()) {
                    if (roleSkill.getSkillLv(reqSkillLvEntry.getKey()) < reqSkillLvEntry.getValue()) {
                        itemMark = false;
                        break;
                    }
                }
            }
            if (svv.getReqbook() != 0) {
                BookModule bookModule = (BookModule) moduleMap().get(MConst.Book);
                if (!bookModule.isBookActive(svv.getReqbook())) {
                    itemMark = false;
                    break;
                }
            }
            if (isTokenSkill(svv.getSkillId())) {
                itemMark = false;
                continue;
            }

            if (itemMark) {
                builder.append(entry.getKey()).append("+");
            }
        }
        redPointMap.put(RedPointConst.SKILL_LVUP,
                builder.toString().isEmpty() ? null : builder.toString());
    }

    /**
     * 当前所携带技能等级之和
     */
    public Integer getUseSkillLvTotal() {
//        Integer totalLv = 0;
//        for (Map.Entry<Byte, Integer> entry : this.roleSkill.getUseSkillMap().entrySet()) {
//            totalLv = totalLv + this.roleSkill.getSkillLv(entry.getValue());
//        }
//        return totalLv;

        Integer totalLv = 0;
        for (Map.Entry<Integer, Integer> entry : this.roleSkill.getSkillLevelMap().entrySet()) {
            int skillId = entry.getKey();
            SkillVo skillVo = SkillManager.getSkillVo(skillId);
            if (skillVo == null) continue;
            String skillTypeStr = skillVo.getSkilltype();
            byte skillType = Byte.parseByte(skillTypeStr);
            if (skillType != SkillConstant.TYPE_AVOID) {
                int skillLevel = entry.getValue();
                totalLv += skillLevel;
            }
        }
        return totalLv;
    }

    /**
     * 检查符文被动技能的激活和失效
     *
     * @param skillId
     * @param rm
     */
    public void updateTokenPassSkillLv(int skillId, Module rm, boolean needSend) {

        if (!isTokenSkill(skillId)) //不是符文技能
            return;
        NewEquipmentModule newEquipmentModule = this.module(MConst.NewEquipment);
        Map<Byte, TokenSkillVo> activeTokenSkillVoMap = newEquipmentModule.getActiveTokenSkillMap();
        boolean isActiveFlag = false; //是否激活状态
        Iterator iter = activeTokenSkillVoMap.entrySet().iterator();
        while (iter.hasNext()) { //遍历激活的符文技能
            Map.Entry<Byte, TokenSkillVo> entry = (Map.Entry<Byte, TokenSkillVo>) iter.next();
            TokenSkillVo activeTokenSkillVo = (TokenSkillVo) entry.getValue();
            if (activeTokenSkillVo.getTokenSkillId() != skillId)
                continue;
            int newLevel = newEquipmentModule.getRoleEquipMap().get(entry.getKey()).getTokenSKillLevel(); //获得最新的符文技能等级
            if (roleSkill.getSkillLv(entry.getValue().getTokenSkillId()) < newLevel) {
                roleSkill.getSkillLevelMap().put(skillId, newLevel);
                this.context().update(roleSkill);
            }
            isActiveFlag = true;  //可以激活的才会true
        }

        if ((!isActiveFlag) && roleSkill.getSkillLv(skillId) != 0) {  //删除失效技能
            roleSkill.getSkillLevelMap().remove(skillId);
            this.context().update(roleSkill);
            updatePassEffect(true);
            updateSkillFightScore(needSend);
            updateRoleSkill();
        }
        if (isActiveFlag) {
            SkillvupVo svv = SkillManager.getSkillvupVo(skillId, roleSkill.getSkillLv(skillId));
            if (svv.getSkillType() == SkillConstant.LVUP_SKILLTYPE_PASS) {
                updatePassEffect(true);
            }
        }
        updateSkillFightScore(needSend);
        updateSkillSummary();
        updateRoleSkill();

    }

    public RoleSkill getRoleSkill() {
        return roleSkill;
    }

    public void onChangeJob(int newJobId) {
        RoleSkill roleSkill = getRoleSkill();
        Map<Integer, Integer> skillLevelMap = roleSkill.getSkillLevelMap();
        Map<Integer, Integer> newSkillLevelMap = new LinkedHashMap<>();
        Map<Byte, Integer> newUseSkillMap = new LinkedHashMap<>();
        Set<Integer> newPendingSkillSet = new LinkedHashSet<>();
        /**
         * 技能等级
         */
        for (Map.Entry<Integer, Integer> entry : skillLevelMap.entrySet()) {
            Integer skillId = entry.getKey();
            int level = entry.getValue();
            Integer position = SkillManager.skillPostionMap.get(skillId);
            if (NewEquipmentManager.getTokenSkillVoBySkillId(skillId) != null) {//符文技能没有位置
                newSkillLevelMap.put(skillId, level);
                continue;
            }
            if (SkillManager.getSkillvupVo(skillId, level).getSkillType() == SkillConstant.TRUMP_SKILLTYPE_PASS) {//法宝技能直接放
                newSkillLevelMap.put(skillId, level);
                continue;
            }
            if (SkillManager.getSkillvupVo(skillId, level).getSkillType() == SkillConstant.FASHIONCARD_SKILLTYPE_PASS) {
                newSkillLevelMap.put(skillId, level);
                continue;
            }
            SkillPosition newSkillPosition = SkillManager.jobSkillPostionMap.get(newJobId).get(position);
            newSkillLevelMap.put(newSkillPosition.getSkillid(), level);
        }

//        Map<Integer,TokenSkillVo> tokenSkillVoMap = NewEquipmentManager.getTokenSkillVoMap();
//        for(Integer skillId:tokenSkillVoMap.keySet()) { //
//            updateTokenPassSkillLv(skillId, null,true);
//        }

        /**
         * 使用技能
         */
        Map<Byte, Integer> useSkillMap = roleSkill.getUseSkillMap();
        for (Map.Entry<Byte, Integer> entry : useSkillMap.entrySet()) {
            Byte usePosition = entry.getKey();
            int skillId = entry.getValue();
            Integer position = SkillManager.skillPostionMap.get(skillId);
            SkillPosition newSkillPosition = SkillManager.jobSkillPostionMap.get(newJobId).get(position);
            newUseSkillMap.put(usePosition, newSkillPosition.getSkillid());
        }
        /**
         * 被动技能
         */
        Set<Integer> pendingSkillSet = roleSkill.getPendingSkillSet();
        for (Integer pendingSkillId : pendingSkillSet) {
            if (NewEquipmentManager.getTokenSkillVoBySkillId(pendingSkillId) != null) //符文技能没有位置
                continue;
            if (SkillManager.getSkillvupVo(pendingSkillId, 1).getSkillType() == SkillConstant.TRUMP_SKILLTYPE_PASS) {//法宝技能直接放
                newPendingSkillSet.add(pendingSkillId);
                continue;
            }
            if (SkillManager.getSkillvupVo(pendingSkillId, 1).getSkillType() == SkillConstant.FASHIONCARD_SKILLTYPE_PASS) {
                newPendingSkillSet.add(pendingSkillId);
                continue;
            }
            Integer position = SkillManager.skillPostionMap.get(pendingSkillId);
            SkillPosition newSkillPosition = SkillManager.jobSkillPostionMap.get(newJobId).get(position);
            newPendingSkillSet.add(newSkillPosition.getSkillid());
        }
        roleSkill.setSkillLevel(newSkillLevelMap);
        roleSkill.setUseSkills(newUseSkillMap);
        roleSkill.setPendingSkills(newPendingSkillSet);
        context().update(roleSkill);
        updateSkillSummary();
    }
}
