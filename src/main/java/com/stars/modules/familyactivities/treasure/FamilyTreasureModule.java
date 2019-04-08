package com.stars.modules.familyactivities.treasure;

import com.stars.core.SystemRecordMap;
import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.data.DataManager;
import com.stars.modules.drop.DropModule;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.familyactivities.treasure.packet.ClientFamilyTreasure;
import com.stars.modules.familyactivities.treasure.prodata.FTBuffVo;
import com.stars.modules.familyactivities.treasure.prodata.FamilyAdvendVo;
import com.stars.modules.familyactivities.treasure.prodata.FamilyAdventureVo;
import com.stars.modules.familyactivities.treasure.userdata.RoleFamilyTreasure;
import com.stars.modules.rank.RankModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.imp.fight.FamilyTreasureScene;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.scene.packet.fightSync.ClientSyncAttr;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.modules.tool.ToolModule;
import com.stars.services.ServiceHelper;
import com.stars.services.chat.ChatManager;
import com.stars.services.family.FamilyAuth;
import com.stars.services.family.event.FamilyEvent;
import com.stars.services.rank.userdata.RoleRankPo;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017/2/10 11:29
 */
public class FamilyTreasureModule extends AbstractModule {
    private RoleFamilyTreasure roleFamilyTreasure;
    private int thisLevel;          //当前家族探宝阶级
    private int thisStep;           //当前家族探宝步数
    private long damageValue;       //当前boss被伤害值
    private long totalDamageValue;  //一周内所有boss被伤害值
    private int startType;          //当前家族探宝开始的类型
    private int rank;               //当前家族排名

    public FamilyTreasureModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        roleFamilyTreasure = DBUtil.queryBean(DBUtil.DB_USER, RoleFamilyTreasure.class, "select * from rolefamilytreasure where roleid = " + id());
        if (roleFamilyTreasure == null) {
            roleFamilyTreasure = new RoleFamilyTreasure();
            insertRoleFamilyTreasure(roleFamilyTreasure);
        }
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        roleFamilyTreasure = new RoleFamilyTreasure();
        insertRoleFamilyTreasure(roleFamilyTreasure);
    }

    private void insertRoleFamilyTreasure(RoleFamilyTreasure roleFamilyTreasure) {
        roleFamilyTreasure.setRoleId(id());
        roleFamilyTreasure.setAdventureCount(0);
        roleFamilyTreasure.setSundayAdventureCount(0);
        roleFamilyTreasure.setDamage(0);
        roleFamilyTreasure.setLastResetDamage(SystemRecordMap.familyTreasureResetTimestamp);
        context().insert(roleFamilyTreasure);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        if (roleFamilyTreasure != null) {
            if (roleFamilyTreasure.getLastResetDamage() < SystemRecordMap.familyTreasureResetTimestamp) {
                roleFamilyTreasure.setDamage(0);
                roleFamilyTreasure.setLastResetDamage(SystemRecordMap.familyTreasureResetTimestamp);
                context().update(roleFamilyTreasure);
            }
        }
    }

    @Override
    public void onOffline() throws Throwable {

    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        if (getFamilyId() != -1) {
            roleFamilyTreasure.setAdventureCount(0);
            roleFamilyTreasure.setSundayAdventureCount(0);
            context().update(roleFamilyTreasure);
        }
    }

    /**
     * 打开页面
     */
    public void view() {
        flushDamageAndProcess(damageValue, thisLevel, thisStep);
        fireSpecialAccountLogEvent("打开家族探宝普通boss关卡界面");
    }

    /**
     * 打开周日页面
     */
    public void viewSunday() {
        flushSundayDamageAndProcess(totalDamageValue, rank);
        fireSpecialAccountLogEvent("打开家族探宝普通boss关卡界面");
    }

    /**
     * 家族探宝状态改变
     *
     * @param level       当前探宝阶层
     * @param step        当前探宝步数
     * @param damage      当前boss伤害值
     * @param totalDamage 本周内boss的伤害总和
     * @param rank        当前家族排名
     * @param startType   当前活动开始的类型
     * @param flush       是否同步到客户端
     */
    public void doEvent(int level, int step, long damage, long totalDamage, int rank, int startType, boolean flush) {
        this.thisLevel = level;
        this.thisStep = step;
        this.damageValue = damage;
        this.totalDamageValue = totalDamage;
        this.rank = rank;
        this.startType = startType;
        if (flush) {
            flushDamageAndProcess(damage, level, step);
        }
    }

    /**
     * 下发探宝普通关卡数据
     *
     * @param damage 当前boss的伤害
     * @param level  当前探宝阶层
     * @param step   当前探宝步数
     */
    private void flushDamageAndProcess(long damage, int level, int step) {
        ClientFamilyTreasure cft = new ClientFamilyTreasure(ClientFamilyTreasure.normal);
        cft.setHp(damgeToHp(damage));
        cft.setLevel(level);
        cft.setStep(step);
        cft.setCount(FamilyTreasureManager.familyadventure_count - roleFamilyTreasure.getAdventureCount());
        send(cft);
    }


    /**
     * 下发周日宝箱关卡数据
     *
     * @param totalDamage 本周内boss的伤害总和
     * @param rank        当前家族排名
     */
    public void flushSundayDamageAndProcess(long totalDamage, int rank) {
        ClientFamilyTreasure cft = new ClientFamilyTreasure(ClientFamilyTreasure.sunday);
        cft.setCount(FamilyTreasureManager.familyadventure_sundaycount - roleFamilyTreasure.getSundayAdventureCount());
        cft.setTotalDamage(totalDamage);
        cft.setRank(rank);
        send(cft);
    }

    /**
     * 进入普通boss关卡
     *
     * @param level 当前探宝阶层
     * @param step  当前探宝步数
     */
    public void enterFamilyTreauserScene(int level, int step) {
        SceneModule scene = module(MConst.Scene);
        if (verifyProcess(level, step)) {
            if (canEnter(FamilyTreasureConst.NORMAL_TREASURE)) {
                scene.enterScene(SceneManager.SCENETYPE_FAMILY_TREASURE, getFamilyAdventureStageId(), getFamilyAdventureStageId());
            }
        }
        fireSpecialAccountLogEvent("进入普通boss关卡");
    }

    /**
     * 进入周日宝箱关卡
     */
    public void enterFTSundayScene() {
        SceneModule scene = module(MConst.Scene);
        if (canEnter(FamilyTreasureConst.SUNDAY_TREASURE)) {
            scene.enterScene(SceneManager.SCENETYPE_FAMILY_TREASURE_SUNDAY, getFamilyAdvendStageId(), getFamilyAdvendStageId());
        }
        fireSpecialAccountLogEvent("进入周日宝箱关卡");
    }

    /**
     * 验证当前阶层和步数是否匹配
     *
     * @param level 当前探宝阶层
     * @param step  当前探宝步数
     * @return
     */
    private boolean verifyProcess(int level, int step) {
        if (level < this.thisLevel) {
            flushDamageAndProcess(damageValue, thisLevel, thisStep);
            warn("familyadventure_tips_fightover");
            return false;
        } else if (level > this.thisLevel) {
            flushDamageAndProcess(damageValue, thisLevel, thisStep);
            warn("familyadventure_tips_noopen");
            return false;
        } else {
            if (step < this.thisStep) {
                flushDamageAndProcess(damageValue, thisLevel, thisStep);
                warn("familyadventure_tips_fightover");
                return false;
            } else if (step > this.thisStep) {
                flushDamageAndProcess(damageValue, thisLevel, thisStep);
                warn("familyadventure_tips_noopen");
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * 判断战斗类型和今日是否还有次数
     *
     * @param type 进入关卡的类型
     * @return
     */
    private boolean canEnter(int type) {
        long familyId = getFamilyId();
        if (familyId <= 0) {
            return false;
        }
        if (type != startType) {
            if (startType == FamilyTreasureConst.SUNDAY_TREASURE) {
                warn("familyadventure_tips_sunday");
            } else {
                warn("familyadventure_tips_noopenbox");
            }
            return false;
        }
        if (startType == FamilyTreasureConst.SUNDAY_TREASURE
                && roleFamilyTreasure.getSundayAdventureCount() >=
                FamilyTreasureManager.familyadventure_sundaycount) {
            warn("stage_entercountover");
            return false;
        }
        if (startType == FamilyTreasureConst.NORMAL_TREASURE
                && roleFamilyTreasure.getAdventureCount() >=
                FamilyTreasureManager.familyadventure_count) {
            warn("stage_entercountover");
            return false;
        }
        return true;
    }

    /**
     * 产品数据，获取当前的stageId
     *
     * @return
     */
    private int getFamilyAdventureStageId() {
        return FamilyTreasureManager.getFamilyAdventureVoMap()
                .get(thisLevel)
                .get(thisStep)
                .getStageId();
    }

    /**
     * 产品数据，获取周日关卡的stageId
     *
     * @return
     */
    private int getFamilyAdvendStageId() {
        return FamilyTreasureManager.familyAdvendVoMap.get(thisLevel + "+" + thisStep).getStageId();
    }

    /**
     * @return thisLevel
     */
    public int getFamilyTreasureLevel() {
        return thisLevel;
    }

    /**
     * @return thisStep
     */
    public int getFamilyTreasureStep() {
        return thisStep;
    }


    /**
     * 退出家族的逻辑，只需要清空伤害值就可以了
     */
    public void doChangeFamily() {
        roleFamilyTreasure.setDamage(0);
        context().update(roleFamilyTreasure);
        updateRank();
    }

    /**
     * 处理进度更新时不能使用全局变量中的thisLevel和thisStep
     * 因为可能会存在并发的问题，全局变量中的thisLevel和thisStep很容易被其他玩家更改
     *
     * @param level
     * @param step
     */
    public void dealUpdateProcess(int level, int step) {
        long familyId = getFamilyId();
        if (familyId <= 0) {
            return;
        }
        int maxLevel = FamilyTreasureManager.getMaxLevel();
        int maxStep = FamilyTreasureManager.getMaxStepByLevel(level);
        if (step < maxStep) {
            ServiceHelper.familyTreasureService().updateProcess(familyId, level, step + 1, false);
        }
        if (step == maxStep) {
            //需要判断进阶时，家族等级是否满足condition条件
            if (level == maxLevel) {
                ServiceHelper.familyTreasureService().updateProcess(familyId, level, step, true);
                return;
            }
            int nextLevel = level + 1;
            if (nextLevel <= maxLevel) {
                int nextStep = FamilyTreasureManager.getMinStepByLevel(nextLevel);
                int condition = FamilyTreasureManager.getFamilyAdventureVoMap().get(nextLevel).get(nextStep).getCondition();
                FamilyModule family = module(MConst.Family);
                int familyLv = family.getAuth().getFamilyLevel();
                if (familyLv >= condition) {
                    ServiceHelper.familyTreasureService().updateProcess(familyId, nextLevel, FamilyTreasureManager.getMinStepByLevel(nextLevel), false);
                }
            }
        }
    }

    /**
     * 加buff
     *
     * @return
     */
    public Map<Integer, Integer> addBuff() {
        Map<Integer, Integer> buffMap = new HashMap<>();
        FTBuffVo ftBuffVo = FamilyTreasureManager.buffVoMap.get(getToday());
        if (ftBuffVo != null) {
            buffMap.put(ftBuffVo.getBuffid(), ftBuffVo.getLevel());
        }
        return buffMap;
    }

    /**
     * 退出boss战斗场景
     * 带上level和step的原因：
     * 玩家进去探宝关卡时，有可能在战斗中家族探宝的进度被修改，
     * 但是玩家只能拿到他所进去关卡的奖励，所以用以区分--另，计算伤害时考虑并发的问题
     *
     * @param csf           用来区分战斗时是否是回城结束的
     * @param level         当前level
     * @param step          当前step
     * @param damage        造成的伤害
     * @param monsterAttrId 怪物Id
     */
    public void dealFinishOrExitScene(ClientStageFinish csf, int level, int step, long damage, int monsterAttrId, String uId) {
        long familyId = getFamilyId();
        if (familyId <= 0) {
            return;
        }
        LogUtil.info("本次boss关卡造成的伤害值:{}", damage);
        roleFamilyTreasure.addDamage(damage);
        context().update(roleFamilyTreasure);
        LogUtil.info("玩家累积的伤害值:{}", roleFamilyTreasure.getDamage());
        //结算奖励
        ServiceHelper.familyTreasureService().updateDamage(familyId, damage);
        ToolModule tool = module(MConst.Tool);
        Map<Integer, Integer> itemMap = getAdventureItemMap(damage, level, step);
        Map<Integer, Integer> killItemMap = dealMonsterHp(level, step, damage, monsterAttrId, familyId, uId);
        if (csf != null) {
            csf.setFtdamage(damage);
            csf.setWeekdamage(getDamage());
            csf.setItemMap(itemMap);
            if (killItemMap != null) {
                csf.setKillItemMap(killItemMap);
                tool.addAndSend(killItemMap, EventType.FAMILY_ACT_TREASURE.getCode());
            }
            send(csf);
            tool.addAndSend(itemMap, EventType.FAMILY_ACT_TREASURE.getCode());
        } else {
            if (!itemMap.isEmpty()) {
                ServiceHelper.emailService().sendToSingle(id(), 10402, 0L, "系统", itemMap);
            }
        }
        updateRank();
        //家族探险
        familyTreasureLog(itemMap, killItemMap, damage);
    }
    
    private void familyTreasureLog(Map<Integer, Integer> itemMap, Map<Integer, Integer> killItemMap, long damage){
    	ServerLogModule logger = module(MConst.ServerLog);
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        FamilyTreasureScene scene = (FamilyTreasureScene)sceneModule.getScene();
        long endTime = scene.endTimestamp==0?System.currentTimeMillis():scene.endTimestamp;
        int useTime = (int) Math.floor((endTime - scene.startTimestamp) / 1000.0);
        StringBuffer timeStr = new StringBuffer();
        timeStr.append(scene.stageId).append("@").append(useTime);
        
        StringBuffer awardStr = new StringBuffer();
        if(!StringUtil.isEmpty(itemMap)){        	
        	for(Integer itemId : itemMap.keySet()){
        		if(awardStr.length()==0){
        			awardStr.append(itemId).append("@").append(itemMap.get(itemId));
        		}else{
        			awardStr.append("&").append(itemId).append("@").append(itemMap.get(itemId));        			
        		}
        	}
        }
        if(!StringUtil.isEmpty(killItemMap)){
        	for(Integer itemId : killItemMap.keySet()){
        		if(awardStr.length()==0){
        			awardStr.append(itemId).append("@").append(killItemMap.get(itemId));
        		}else{
        			awardStr.append("&").append(itemId).append("@").append(killItemMap.get(itemId));        			
        		}
        	}
        }
        StringBuffer damageStr = new StringBuffer();
        damageStr.append(scene.stageId).append("@").append(damage);
        logger.log_personal_family_find(timeStr.toString(), awardStr.toString(), damageStr.toString());
    }

    public void doResetEvent() {
        if (roleFamilyTreasure != null) {
            roleFamilyTreasure.setDamage(0L);
            roleFamilyTreasure.setLastResetDamage(SystemRecordMap.familyTreasureResetTimestamp);
            context().update(roleFamilyTreasure);
        }
    }

    /**
     * 更新个人伤害排行榜
     */
    private void updateRank() {
        RankModule rank = (RankModule) moduleMap().get(MConst.Rank);
        RoleRankPo rankPo = rank.getCurRoleRankPo();
        if (rankPo != null) {
            rankPo.setAccDamage(roleFamilyTreasure.getDamage());
            rank.updateToRank(rankPo);
        }
    }

    /**
     * 退出周日宝箱关卡
     *
     * @param finish 胜利与否
     */
    public void dealFinishSundayScene(byte finish) {
        dealSundayAdventureCountInc();
        Map<Integer, Integer> itemMap = getAdvendItemMap(thisLevel, thisStep);
        ToolModule tool = module(MConst.Tool);
        tool.addAndSend(itemMap, EventType.OFFLINEPVP.getCode());
        ClientStageFinish csf = new ClientStageFinish(SceneManager.SCENETYPE_FAMILY_TREASURE_SUNDAY, finish);
        csf.setItemMap(itemMap);
        send(csf);
    }

    /**
     * 产品数据
     *
     * @param level 当前探宝阶层
     * @param step  当前探宝步数
     * @return
     */
    private FamilyAdventureVo getThisFamilyAdventure(int level, int step) {
        return FamilyTreasureManager.getFamilyAdventureVoMap().get(level).get(step);
    }

    /**
     * 产品数据
     *
     * @param level 当前探宝阶层
     * @param step  当前探宝步数
     * @return
     */
    private FamilyAdvendVo getThisfamilyAdvend(int level, int step) {
        return FamilyTreasureManager.familyAdvendVoMap.get(level + "+" + step);
    }


    /**
     * 判断本关boss是否死掉
     * 致命一击
     *
     * @param level         当前level
     * @param step          当前step
     * @param damegeValue   造成的伤害
     * @param monsterAttrId 怪物Id
     * @param familyId      家族Id
     * @return
     */
    private Map<Integer, Integer> dealMonsterHp(int level, int step, long damegeValue, int monsterAttrId, long familyId, String uId) {
        int hp = SceneManager.getMonsterAttrVo(monsterAttrId).getHp();
        long allDamage = ServiceHelper.familyTreasureService().getFamilyTreasureDamage(familyId);
        if (hp <= damgeToHp(allDamage + damegeValue)) {
            syncAttr(uId);
            dealUpdateProcess(level, step);
            addFamilyContribution(level, step, monsterAttrId, familyId);
            return deadlyStrike(level, step);
        }
        return null;
    }

    /**
     * 客户端需求，需要知道怪物死亡
     *
     * @param uId
     */
    private void syncAttr(String uId) {
        ClientSyncAttr clientSyncAttr = new ClientSyncAttr();
        clientSyncAttr.addSyncCurHp(uId, 0);
        send(clientSyncAttr);
    }

    /**
     * 致命一击奖励
     *
     * @param level 当前探宝阶层
     * @param step  当前探宝步数
     * @return
     */
    private Map<Integer, Integer> deadlyStrike(int level, int step) {
        int killAwardDropId = getThisFamilyAdventure(level, step).getKillAward();
        DropModule drop = module(MConst.Drop);
        return drop.executeDrop(killAwardDropId, 1, true);
    }

    /**
     * 增加家族资金
     *
     * @param level
     * @param step
     * @param monsterAttrId
     * @param familyId
     */
    private void addFamilyContribution(int level, int step, int monsterAttrId, long familyId) {
        int killMoneyAward = getThisFamilyAdventure(level, step).getMoneyAward();
        FamilyModule family = module(MConst.Family);
        ServiceHelper.familyMainService().addMoneyAndUpdateContribution(family.getAuth(), id(), killMoneyAward, 0, SystemRecordMap.dateVersion, 0);
        chatFamilyChannel(level, step, killMoneyAward, familyId);
    }

    /**
     * 聊天频道发信息，增加家族事迹
     *
     * @param level
     * @param killMoneyAward
     * @param familyId
     */
    private void chatFamilyChannel(int level, int step, int killMoneyAward, long familyId) {
        String chatMessageGameText = DataManager.getGametext("familyadventure_desc_complete");
        String chatMessages = String.format(chatMessageGameText, level, step, killMoneyAward);
        RoleModule rm = module(MConst.Role);
        ServiceHelper.chatService().chat(rm.getRoleRow().getName(), ChatManager.CHANNEL_FAMILY, id(), familyId, chatMessages, false);
        ServiceHelper.familyEventService().logEvent(familyId, FamilyEvent.A_TREASURE, String.valueOf(level), String.valueOf(step), String.valueOf(killMoneyAward));
    }


    /**
     * 伤害转成生命值
     *
     * @param damage 伤害值
     * @return
     */
    private long damgeToHp(long damage) {
        if (thisLevel == 0 || thisStep == 0) {
            return 0;
        }
        int coefficient = FamilyTreasureManager.familyAdventureVoMap.get(thisLevel)
                .get(thisStep).getCoefficient();
        long damageHp = coefficient == 0 ?  damage :  (damage / coefficient);
        return damageHp;
    }

    /**
     * 每日次数+1
     */
    public void dealAdventureCountInc() {
        roleFamilyTreasure.setAdventureCount(roleFamilyTreasure.getAdventureCount() + 1);
        context().update(roleFamilyTreasure);
        ServiceHelper.roleService().notice(id(), new DailyFuntionEvent(DailyManager.DAILYID_FAMILY_TREASURE, 1));
    }

    /**
     * 周日次数+1
     */
    private void dealSundayAdventureCountInc() {
        roleFamilyTreasure.setSundayAdventureCount(roleFamilyTreasure.getSundayAdventureCount() + 1);
        context().update(roleFamilyTreasure);
        ServiceHelper.roleService().notice(id(), new DailyFuntionEvent(DailyManager.DAILYID_FAMILY_TREASURE, 1));
    }

    /**
     * 返回家族id
     *
     * @return
     */
    private long getFamilyId() {
        FamilyModule family = module(MConst.Family);
        FamilyAuth auth = family.getAuth();
        return auth == null ? -1 : auth.getFamilyId();
    }

    /**
     * 玩家累积的伤害值
     *
     * @return
     */
    public long getDamage() {
        return roleFamilyTreasure.getDamage();
    }

    /**
     * boss关卡结算后的奖励
     *
     * @param damage 伤害值
     * @param level  当前探宝阶层
     * @param step   当前探宝步数
     * @return
     */
    private Map<Integer, Integer> getAdventureItemMap(long damage, int level, int step) {
        FamilyAdventureVo adventureVo = getThisFamilyAdventure(level, step);
        int dropId = FamilyTreasureManager.getDropByGroupAndDamage(adventureVo.getAwardGroup(), damage);
        if (dropId == -1) {
            return new HashMap<>();
        }
        DropModule drop = module(MConst.Drop);
        return drop.executeDrop(dropId, 1, true);
    }

    /**
     * 宝箱关卡结算后的奖励
     *
     * @param level 当前探宝阶层
     * @param step  当前探宝步数
     * @return
     */
    private Map<Integer, Integer> getAdvendItemMap(int level, int step) {
        FamilyAdvendVo advendVo = getThisfamilyAdvend(level, step);
        DropModule drop = module(MConst.Drop);
        return drop.executeDrop(advendVo.getAward(), 1, true);
    }

    /**
     * @return 返回今天
     */
    private int getToday() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        int day = cal.get(Calendar.DAY_OF_WEEK) - 1;
        return day == 0 ? 7 : day;
    }

    private void fireSpecialAccountLogEvent(String content) {
        if (SpecialAccountManager.isSpecialAccount(id())) {
            eventDispatcher().fire(new SpecialAccountEvent(id(), content, true));
        }
    }

}
