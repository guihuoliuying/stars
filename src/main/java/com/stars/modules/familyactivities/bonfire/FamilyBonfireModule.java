package com.stars.modules.familyactivities.bonfire;

import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.achievement.event.JoinActivityEvent;
import com.stars.modules.data.DataManager;
import com.stars.modules.drop.DropModule;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.familyactivities.FamilyActUtil;
import com.stars.modules.familyactivities.bonfire.packet.ClientBonfire;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.prodata.SafeinfoVo;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.services.ServiceHelper;
import com.stars.services.family.FamilyAuth;
import com.stars.util.I18n;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;
import com.stars.util.TimeUtil;

import java.util.HashSet;
import java.util.Map;

/**
 * 家族篝火
 * Created by zhouyaohui on 2016/10/8.
 */
public class FamilyBonfireModule extends AbstractModule {
    private HashSet<Integer> bonfireSet = new HashSet<>();
    private long lastGenerateTime = 0;
    private final static String actVersion = "familyactivity.bonfire.actVersion";
    private long lastThrowGoldTimes;    //上次投元宝时间
    private long lastThrowWoodTimes;    //上次投干柴时间

    public FamilyBonfireModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("家族篝火", id, self, eventDispatcher, moduleMap);
    }

    public void enterBonefireScene() {
        SceneModule sm = module(MConst.Scene);
        int safeId = MapUtil.getInt(DataManager.commonConfigMap, "family_gohome", 0);
        SafeinfoVo curSafeinfoVo = SceneManager.getSafeVo(safeId);
        sm.enterScene(SceneManager.SCENETYPE_FAMIL, curSafeinfoVo.getSafeId(), 1000);
        fireSpecialAccountLogEvent("进入家族篝火场景");
        //进入篝火日志
        if (BonfireActivityFlow.isStarted()){        	
        	ServerLogModule logger = module(MConst.ServerLog);
        	logger.log_personal_family_bonfire((byte)1, "");
        }
    }

    /**
     * 生成灯笼
     *
     * @param bonfireId 用时间戳作为篝火id
     */
    public void generateBonfire(int bonfireId) {
        if (hasFamily() == false) {
            return;
        }
        if (BonfireActivityFlow.getState() == BonfireActivityFlow.CLOSE) {
            warn(I18n.get("family.bonfire.activityEnd"));
            return;
        }
        long millis = System.currentTimeMillis();
        String randomTimes = MapUtil.getString(DataManager.commonConfigMap, "family_bonfire_interval", null);
        long minDelay = Long.valueOf(randomTimes.split("[+]")[0]);
        if (lastGenerateTime + minDelay > millis) {
            return;
        }
        lastGenerateTime = millis;
        bonfireSet.add(bonfireId);
        int curActivityVersion = BonfireActivityFlow.getActivityVersion();
        if (context().recordMap().getInt(actVersion, 0) != curActivityVersion) {
            context().recordMap().setInt(actVersion, curActivityVersion);
            eventDispatcher().fire(new JoinActivityEvent(JoinActivityEvent.BONFIRE));
        }
    }

    /**
     * 打破灯笼
     *
     * @param bonfireId
     */
    public void breakBonfire(int bonfireId) {
        if (bonfireSet.contains(bonfireId) == false) {
            return;
        }
        DropModule dropModule = module(MConst.Drop);
        ToolModule toolModule = module(MConst.Tool);
        int dropId = MapUtil.getInt(DataManager.commonConfigMap, "family_bonfire_award", 0);
        Map<Integer, Integer> dropMap = dropModule.executeDrop(dropId, 1, true);
        toolModule.addAndSend(dropMap, EventType.FAMILYBONFIRE.getCode());
        ClientBonfire drop = new ClientBonfire();
        drop.setResType(ClientBonfire.DROP);
        drop.setDropStr(StringUtil.makeString(dropMap, '+', '|'));
        send(drop);
        bonfireSet.remove(bonfireId);
    }

    /**
     * 清除生成的灯笼
     */
    public void clear() {
        bonfireSet.clear();
    }

    /**
     * 通知客户端活动开始
     */
    public void noticeClientBegin() {
        FamilyModule fm = module(MConst.Family);
        if (hasFamily() == false) {
            return;
        }
        if (BonfireActivityFlow.getState() == BonfireActivityFlow.CLOSE) {
            return;
        }
        clear();
        ClientBonfire begin = new ClientBonfire();
        begin.setResType(ClientBonfire.BEGIN);
        send(begin);
    }

    /**
     * 通知客户端活动结束
     */
    public void noticeClientEnd() {
        if (hasFamily() == false) {
            return;
        }
        ClientBonfire end = new ClientBonfire();
        end.setResType(ClientBonfire.END);
        send(end);
    }

    /**
     * 通知客户端活动结束，不判断家族条件
     */
    public void noticeClientEnd2() {
        ClientBonfire end = new ClientBonfire();
        end.setResType(ClientBonfire.END);
        send(end);
    }

    /**
     * 登陆成功处理
     */
    public void loginSuccessHandle() {
        if (BonfireActivityFlow.getState() == BonfireActivityFlow.OPEN) {
            noticeClientBegin();
        }
    }

    public boolean hasFamily() {
        FamilyModule fm = module(MConst.Family);
        if (fm.getAuth() == null || fm.getAuth().getFamilyId() == 0) {
            return false;
        }
        return true;
    }

    /**
     * 移除参加活动成员数据
     */
    public void removeMemeber() {
        FamilyAuth familyAuth = FamilyActUtil.getAuth(moduleMap());
        if (familyAuth == null)
            return;
        // 活动未开启
        if (!BonfireActivityFlow.isStarted())
            return;
        ServiceHelper.familyBonFireService().removeMember(familyAuth.getFamilyId(), id());
        //离开篝火
        ServerLogModule logger = module(MConst.ServerLog);
        logger.log_personal_family_bonfire((byte)2, "");
    }

    /**
     * 更新参加活动成员数据
     */
    public void updateMember() {
        FamilyAuth familyAuth = FamilyActUtil.getAuth(moduleMap());
        if (familyAuth == null)
            return;
        // 活动未开启
        if (!BonfireActivityFlow.isStarted())
            return;
        SceneModule sceneModule = module(MConst.Scene);
        if (sceneModule == null) return;
        if (sceneModule.getScene().getSceneType() != SceneManager.SCENETYPE_FAMIL) return;
        RoleModule roleModule = module(MConst.Role);
        if (roleModule == null) return;
        ServiceHelper.familyBonFireService().addUpdateMember(familyAuth.getFamilyId(), id(), roleModule.getLevel(), roleModule.getRoleRow().getJobId());
    }

    public void updateLevel(int level) {
        FamilyAuth familyAuth = FamilyActUtil.getAuth(moduleMap());
        if (familyAuth == null)
            return;
        // 活动未开启
        if (!BonfireActivityFlow.isStarted())
            return;

        SceneModule sceneModule = module(MConst.Scene);
        if (sceneModule == null || sceneModule.getScene() == null) return;
        if (sceneModule.getScene().getSceneType() == SceneManager.SCENETYPE_FAMIL) {//在家族领地
            ServiceHelper.familyBonFireService().updateRoleLevel(familyAuth.getFamilyId(), id(), level);
        }
    }

    /**
     * 家族篝火经验下发
     */
    public void sendFireDropAward(Map<Integer, Integer> map) {
        if (StringUtil.isEmpty(map)) return;
        ToolModule toolModule = module(MConst.Tool);
        toolModule.addAndSend(map, EventType.FAMILY_ACT_BONFIRE.getCode());
    }

    /**
     * 刷新篝火活动信息
     */
    public void updateRoleFire() {
        FamilyAuth familyAuth = FamilyActUtil.getAuth(moduleMap());
        if (familyAuth == null || familyAuth.getFamilyId() == 0) return;//没有家族
        // 活动未开启
        //if (!BonfireActivityFlow.isStarted()) return;
        ServiceHelper.familyBonFireService().updateRoleFire(familyAuth.getFamilyId(), id());
        fireSpecialAccountLogEvent("刷新篝火活动信息");
    }

    /**
     * 初始化篝火信息
     */
    public void initRoleFireInfo() {
        FamilyAuth familyAuth = FamilyActUtil.getAuth(moduleMap());
        if (familyAuth == null || familyAuth.getFamilyId() == 0) {
//            warn("篝火信息初始化，没有家族！！");
            return;//没有家族
        }
        // 活动未开启
//        if (!BonfireActivityFlow.isStarted()) {
////            warn("篝火信息初始化，篝火活动尚未开启！！");
//            return;
//        }
        RoleModule roleModule = module(MConst.Role);
        if (roleModule == null) return;
        int hasUsedCount = roleModule.getRoleRow().getDailyThrowGoldCount();
        int dailyLimit = FamilyBonfrieManager.getDailyThrowGoldCount();
        int remainTimes = dailyLimit - hasUsedCount;
        if (remainTimes < 0) remainTimes = 0;
        ServiceHelper.familyBonFireService().initRoleFireInfo(familyAuth.getFamilyId(), id(), roleModule.getLevel(), roleModule.getRoleRow().getJobId(), remainTimes);
        fireSpecialAccountLogEvent("初始化家族篝火");
    }

    /**
     * 投元宝
     */
    public void throwGold() {
        FamilyAuth familyAuth = FamilyActUtil.getAuth(moduleMap());
        if (familyAuth == null || familyAuth.getFamilyId() == 0) return;//没有家族
        // 活动未开启
        if (!BonfireActivityFlow.isStarted()) {
            warn(I18n.get("family.bonfire.activityEnd"));
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastThrowGoldTimes < 1 * TimeUtil.SECOND) return;
        lastThrowGoldTimes = now;

        RoleModule roleModule = module(MConst.Role);
        if (roleModule == null || roleModule.getRoleRow() == null) return;
        int hasUsedCount = roleModule.getRoleRow().getDailyThrowGoldCount();
        int dailyLimit = FamilyBonfrieManager.getDailyThrowGoldCount();
        if (hasUsedCount >= dailyLimit) {
            warn(I18n.get("common.dailyTimes.notEnough"));
            return;
        }

        ToolModule toolModule = module(MConst.Tool);
        if (!toolModule.deleteAndSend(ToolManager.GOLD, FamilyBonfrieManager.GOLD_COUNT, EventType.FAMILY_BONFIRE_THROW_GOLD.getCode())) {
            warn(I18n.get("family.bonfire.hasNoGold"));
            return;//元宝数量不足
        }

        ClientBonfire client = new ClientBonfire(ClientBonfire.UPDATE_THROW_GOLD_TIMES);
        client.setDailyThrowGoldTimes(dailyLimit - hasUsedCount - 1);
        send(client);

        roleModule.getRoleRow().addDailyThrowGoldCount();
        DropModule dropModule = module(MConst.Drop);
        Map<Integer, Integer> map = dropModule.executeDrop(FamilyBonfrieManager.GOLD_DROP_GROUP, 1, true);
        if (StringUtil.isNotEmpty(map)) {
            toolModule.addAndSend(map, EventType.FAMILY_BONFIRE_THROW_GOLD.getCode());
        }
        fireSpecialAccountLogEvent("家族篝火投元宝");
        //篝火增加经验
        ServiceHelper.familyBonFireService().addFamilyFireExp(familyAuth.getFamilyId(), id(), FamilyBonfrieManager.GOLD_EXP);
    }

    /**
     * 投干柴
     */
    public void throwWood() {
        FamilyAuth familyAuth = FamilyActUtil.getAuth(moduleMap());
        if (familyAuth == null || familyAuth.getFamilyId() == 0) return;//没有家族
        // 活动未开启
        if (!BonfireActivityFlow.isStarted()) {
            warn(I18n.get("family.bonfire.activityEnd"));
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastThrowWoodTimes < 1 * TimeUtil.SECOND) return;
        lastThrowWoodTimes = now;

        ToolModule toolModule = module(MConst.Tool);
        if (!toolModule.deleteAndSend(FamilyBonfrieManager.WOOD_ID, 1, EventType.FAMILY_BONFIRE_THROW_WOOD.getCode())) {
            warn(I18n.get("family.bonfire.hasNoWood"));
            return;//干柴数量不足
        }
        DropModule dropModule = module(MConst.Drop);
        Map<Integer, Integer> map = dropModule.executeDrop(FamilyBonfrieManager.WOOD_DROP_GROUP, 1, true);
        if (StringUtil.isNotEmpty(map)) {
            toolModule.addAndSend(map, EventType.FAMILY_BONFIRE_THROW_WOOD.getCode());
        }
        fireSpecialAccountLogEvent("家族篝火投干柴");
        //篝火增加经验
        ServiceHelper.familyBonFireService().addFamilyFireExp(familyAuth.getFamilyId(), id(), FamilyBonfrieManager.WOOD_EXP);
    }

    /**
     * 捡干柴
     */
    public void pickWood() {
        FamilyAuth familyAuth = FamilyActUtil.getAuth(moduleMap());
        if (familyAuth == null || familyAuth.getFamilyId() == 0) return;//没有家族
        // 活动未开启
        if (!BonfireActivityFlow.isStarted()) {
            warn(I18n.get("family.bonfire.activityEnd"));
//            LogUtil.info("pickWood|活动尚未开启,拣柴失败");
            return;
        }
        fireSpecialAccountLogEvent("家族篝火捡干柴");
        ServiceHelper.familyBonFireService().pickWood(familyAuth.getFamilyId(), id());
    }

    public void answerQuestion(int questionId, int questionIndex) {
        FamilyAuth familyAuth = FamilyActUtil.getAuth(moduleMap());
        if (familyAuth == null || familyAuth.getFamilyId() == 0) {
//            warn("篝火答题|没有家族!!!!!!!");
//            LogUtil.info("篝火答题|没有家族!!!!!!!");
            return;//没有家族
        }
        // 活动未开启
        if (!BonfireActivityFlow.isStarted()) {
//            warn("篝火答题|活动未开启!!!!!!!");
//            LogUtil.info("篝火答题|活动未开启!!!!!!!");
            return;
        }
        fireSpecialAccountLogEvent("家族篝火回答问题");
        RoleModule roleModule = module(MConst.Role);
        ServiceHelper.familyBonFireService().answerQuestion(familyAuth.getFamilyId(), id(), roleModule.getRoleRow().getName(), questionId, questionIndex);
    }

    private void fireSpecialAccountLogEvent(String content) {
        if (SpecialAccountManager.isSpecialAccount(id())) {
            eventDispatcher().fire(new SpecialAccountEvent(id(), content, true));
        }
    }

}
