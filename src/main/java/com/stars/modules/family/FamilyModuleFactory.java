package com.stars.modules.family;

import com.stars.core.db.DBUtil;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.family.event.*;
import com.stars.modules.family.gm.FamilyEmailGmHandler;
import com.stars.modules.family.gm.FamilyGmHandler;
import com.stars.modules.family.gm.FamilyRedPacketGmHandler;
import com.stars.modules.family.gm.FamilySkillGmHandler;
import com.stars.modules.family.listener.*;
import com.stars.modules.family.prodata.FamilySkillVo;
import com.stars.modules.family.summary.FamilySummaryComponentImpl;
import com.stars.modules.gm.GmManager;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.role.event.FightScoreChangeEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.services.family.main.prodata.FamilyLevelVo;
import com.stars.services.summary.Summary;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stars.modules.data.DataManager.commonConfigMap;

/**
 * Created by zhaowenshuo on 2016/8/27.
 */
public class FamilyModuleFactory extends AbstractModuleFactory<FamilyModule> {

    public FamilyModuleFactory() {
        super(new FamilyPacketSet());
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("family", new FamilyGmHandler());
        GmManager.reg("familyrp", new FamilyRedPacketGmHandler());
        GmManager.reg("familyskill", new FamilySkillGmHandler());
        GmManager.reg("familyemail", new FamilyEmailGmHandler());


        Summary.regComponentClass("family", FamilySummaryComponentImpl.class);

//        FamilyManager.actEntryFilters.put(ActConst.ID_FAMILY_ESORT, new FamilyEscortEntryFilter());
    }

    @Override
    public FamilyModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new FamilyModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        FamilyEventListener listener = new FamilyEventListener((FamilyModule) module);
        eventDispatcher.reg(FamilyAuthUpdatedEvent.class, listener);
        eventDispatcher.reg(RoleLevelUpEvent.class, listener);
        eventDispatcher.reg(FightScoreChangeEvent.class, listener);
        eventDispatcher.reg(FamilyLockUpdatedEvent.class, listener);
        eventDispatcher.reg(FamilyLogEvent.class, listener);
        eventDispatcher.reg(FamilyAddApplyEvent.class, new FamilyAddApplyListener((FamilyModule) module));
        eventDispatcher.reg(FamilyRemoveApplyEvent.class, new FamilyRemoveApplyListener((FamilyModule) module));
        eventDispatcher.reg(FamilyContributionEvent.class, new FamilyContributionListener((FamilyModule) module));
        eventDispatcher.reg(FamilyChangeRedPacketEvent.class, new FamilyChangeRedPacketListener((FamilyModule) module));
        eventDispatcher.reg(RoleRenameEvent.class, listener);
    }

    @Override
    public void loadProductData() throws Exception {

        Map<Integer, FamilyLevelVo> levelVoMap = DBUtil.queryMap(
                DBUtil.DB_PRODUCT, "level", FamilyLevelVo.class, "select * from `familylevel`");
        List<FamilySkillVo> tmpSkillVoList = DBUtil.queryList(
                DBUtil.DB_PRODUCT, FamilySkillVo.class, "select * from `familyskill`");
        Map<String, Map<Integer, FamilySkillVo>> skillVoMap = new HashMap<>();
        for (FamilySkillVo skillVo : tmpSkillVoList) {
            Map<Integer, FamilySkillVo> childMap = skillVoMap.get(skillVo.getAttribute());
            if (childMap == null) {
                childMap = new HashMap<>();
                skillVoMap.put(skillVo.getAttribute(), childMap);
            }
            childMap.put(skillVo.getLevel(), skillVo);
        }
        // 计算一键升级的最大循环次数
        int tmpUpgradeSkillLevelAmapMaxLoop = 0;
        for (Map<Integer, FamilySkillVo> levelMap : skillVoMap.values()) {
            int maxLevel = 0;
            for (Integer level : levelMap.keySet()) {
                if (level > maxLevel) {
                    maxLevel = level;
                }
            }
            tmpUpgradeSkillLevelAmapMaxLoop += maxLevel;
        }
        int upgradeSkillLevelAmapMaxLoop = tmpUpgradeSkillLevelAmapMaxLoop + 1; // 需要加+1，需要额外的一次来进行升级

        int familyRecomListLimit = com.stars.util.MapUtil.getInt(commonConfigMap, "family_applylist", 10);
        int creationCost = com.stars.util.MapUtil.getIntVal(commonConfigMap, "family_createreq", "\\|", "\\+", 0, 0); // 创建家族的价格（代价）
        int applyAllLimitPerTime = com.stars.util.MapUtil.getInt(commonConfigMap, "family_apply", 10); // 一键申请（一次申请个数）
        int protectionContributionThreshold = com.stars.util.MapUtil.getInt(commonConfigMap, "family_getoutprotect", 9999); // 防误踢：贡献值阈值（大于此值才能踢）
        long protectionTimeLimit = com.stars.util.MapUtil.getLong(commonConfigMap, "family_appoint_max", 99) * 3600 * 24 * 1000; // 防误踢：离线时间期限（大于此值才能踢）
        long autoAbdicationTimeLimit = com.stars.util.MapUtil.getLong(commonConfigMap, "family_appoint_max", 99) * 3600 * 24 * 1000; // 自动禅让的期限

        int joinEmailTemplateId = com.stars.util.MapUtil.getInt(commonConfigMap, "family_joinemail", 0); // 加入的邮件模板id
        int kickOutEmailTemplateId = com.stars.util.MapUtil.getInt(commonConfigMap, "family_leaveemail", 0); // 强踢的邮件模板id

        int assistantLimit = com.stars.util.MapUtil.getIntVal(commonConfigMap, "family_postcount", "\\|", "\\+", 1, 2); // 副组长人数限制
        int elderLimit = com.stars.util.MapUtil.getIntVal(commonConfigMap, "family_postcount", "\\|", "\\+", 2, 2); // 长老人数限制

        byte donateLimit = com.stars.util.MapUtil.getByte(commonConfigMap, "family_donate_count", (byte) 10); // 一天的捐献次数限制
        int donateReqItemId = com.stars.util.MapUtil.getIntKey(commonConfigMap, "family_donate_req", "\\|", "\\+", 0, 3); // 捐献的道具id
        int donateReqValue = com.stars.util.MapUtil.getIntVal(commonConfigMap, "family_donate_req", "\\|", "\\+", 0, 1); // 捐献的道具数量
        int donateGainedFamilyMoney = com.stars.util.MapUtil.getInt(commonConfigMap, "family_donate_money", 1); // 捐献获得的家族资金
        int donateGainedContribution = com.stars.util.MapUtil.getIntVal(commonConfigMap, "family_donate_award", "\\|", "\\+", 0, 10); // 捐献获得的贡献

        byte rmbDonateLimit = com.stars.util.MapUtil.getByte(commonConfigMap, "family_rmbdonate_count", (byte) 10); // 元宝捐献的次数限制
        int rmbDonateReqItemId = com.stars.util.MapUtil.getIntKey(commonConfigMap, "family_rmbdonate_req", "\\|", "\\+", 0, 1); // 元宝捐献的道具id
        int rmbDonateReqValue = com.stars.util.MapUtil.getIntVal(commonConfigMap, "family_rmbdonate_req", "\\|", "\\+", 0, 1); // 元宝捐献的道具数据
        int rmbDonateGainedFamilyMoney = com.stars.util.MapUtil.getInt(commonConfigMap, "family_rmbdonate_money", 1); // 元宝捐献获得的家族资金
        int rmbDonateGainedContribution = com.stars.util.MapUtil.getIntVal(commonConfigMap, "family_rmbdonate_award", "\\|", "\\+", 0, 10); // 元宝捐献获得贡献

        double contributionPenaltyRatio = com.stars.util.MapUtil.getDouble(commonConfigMap, "family_getoutdonate", 80) / 100.0D; // 离开家族的贡献惩罚

        /* 福利（红包） */
        int rpCountDivisor = com.stars.util.MapUtil.getInt(commonConfigMap, "family_redcount", 10); // 家族人数和红包个数的比值（红包个数=家族任务/rpCountDivisor）
        int rpRmbDonationPerRedPacket = com.stars.util.MapUtil.getInt(commonConfigMap, "family_redsendreq", 10); // 多少元宝捐献获得一个红包
        Map<Integer, Integer> rpGiverRewardMap = StringUtil.toMap(com.stars.util.MapUtil.getString(commonConfigMap, "family_redaward", ""), Integer.class, Integer.class, '+', '|'); // 发红包者获得的奖励
        int rpSeizerAwardDropId = com.stars.util.MapUtil.getInt(commonConfigMap, "family_redgetaward", 3); // 抢红包者红包奖励的掉落id
        int rpTimeout = com.stars.util.MapUtil.getInt(commonConfigMap, "family_redtime", 300); // 红包时效

        /* 事迹 */
        int eventListLimit = MapUtil.getInt(commonConfigMap, "family_eventcount", 100); // 事迹列表大小

        /* 赋值 */
        FamilyManager.levelVoMap = levelVoMap;
        FamilyManager.skillVoMap = skillVoMap;
        FamilyManager.upgradeSkillLevelAmapMaxLoop = upgradeSkillLevelAmapMaxLoop;
        FamilyManager.familyRecomListLimit = familyRecomListLimit;
        FamilyManager.creationCost = creationCost;
        FamilyManager.applyAllLimitPerTime = applyAllLimitPerTime;
        FamilyManager.protectionContributionThreshold = protectionContributionThreshold;
        FamilyManager.protectionTimeLimit = protectionTimeLimit;
        FamilyManager.autoAbdicationTimeLimit = autoAbdicationTimeLimit;

        FamilyManager.joinEmailTemplateId = joinEmailTemplateId;
        FamilyManager.kickOutEmailTemplateId = kickOutEmailTemplateId;

        FamilyManager.assistantLimit = assistantLimit;
        FamilyManager.elderLimit = elderLimit;

        FamilyManager.donateLimit = donateLimit;
        FamilyManager.donateReqItemId = donateReqItemId;
        FamilyManager.donateReqValue = donateReqValue;
        FamilyManager.donateGainedFamilyMoney = donateGainedFamilyMoney;
        FamilyManager.donateGainedContribution = donateGainedContribution;

        FamilyManager.rmbDonateLimit = rmbDonateLimit;
        FamilyManager.rmbDonateReqItemId = rmbDonateReqItemId;
        FamilyManager.rmbDonateReqValue = rmbDonateReqValue;
        FamilyManager.rmbDonateGainedFamilyMoney = rmbDonateGainedFamilyMoney;
        FamilyManager.rmbDonateGainedContribution = rmbDonateGainedContribution;

        FamilyManager.contributionPenaltyRatio = contributionPenaltyRatio;

        /* 福利（红包） */
        FamilyManager.rpCountDivisor = rpCountDivisor;
        FamilyManager.rpRmbDonationPerRedPacket = rpRmbDonationPerRedPacket;
        FamilyManager.rpGiverRewardMap = rpGiverRewardMap;
        FamilyManager.rpSeizerAwardDropId = rpSeizerAwardDropId;
        FamilyManager.rpTimeout = rpTimeout;

        /* 事迹 */
        FamilyManager.eventListLimit = eventListLimit;
    }
}
