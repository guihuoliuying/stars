package com.stars.services.family.activities.bonfire;

import com.stars.core.activityflow.ActivityFlowUtil;
import com.stars.coreManager.ExcutorKey;
import com.stars.coreManager.SchedulerManager;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.drop.DropUtil;
import com.stars.modules.familyactivities.bonfire.BonfireActivityFlow;
import com.stars.modules.familyactivities.bonfire.FamilyBonfrieManager;
import com.stars.modules.familyactivities.bonfire.event.BonFireDropEvent;
import com.stars.modules.familyactivities.bonfire.packet.ClientBonfire;
import com.stars.modules.familyactivities.bonfire.prodata.FamilyFireVo;
import com.stars.modules.familyactivities.bonfire.prodata.FamilyQuestion;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.chat.ChatManager;
import com.stars.services.family.activities.bonfire.cache.BonFireQuestionCache;
import com.stars.services.family.activities.bonfire.cache.FamilyBonFireData;
import com.stars.services.family.activities.bonfire.cache.RoleBonFireCache;
import com.stars.services.family.activities.bonfire.cache.RoleQuestionCache;
import com.stars.services.family.main.FamilyData;
import com.stars.services.role.RoleNotification;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.util.TimeUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by wuyuxing on 2017/3/9.
 */
public class FamilyBonFireServiceActor extends ServiceActor implements FamilyBonFireService {

    private Map<Long, FamilyBonFireData> FAMILY_FIRE_DATAS = new HashMap<>();
    private long bonFireBeginTimes;
    private boolean isBegin = false;

    public static List<BonFireQuestionCache> DailyFireQuestionCaches = new ArrayList<>();
    public static int questionIndex = -1;          //当前题目序号
    public static long curQuestionBeginTimes; //当前题目开始时间
    public static long curQuestionEndTimes;   //当前题目结束时间

    public static BonFireQuestionCache getCurQuestionCache() {
        if (questionIndex < 0 || questionIndex >= DailyFireQuestionCaches.size()) {
            return null;
        }
        return DailyFireQuestionCaches.get(questionIndex);
    }


    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.FamilyBonFireService, this);
        resetDailyQuestion();//重置题目
    }

    @Override
    public void printState() {
    	LogUtil.info("容器大小输出:{},FAMILY_FIRE_DATAS:{},DailyFireQuestionCaches:{}",
    			this.getClass().getSimpleName(),FAMILY_FIRE_DATAS.size(),DailyFireQuestionCaches.size());
    }

    @Override
    public void start() {
        bonFireBeginTimes = System.currentTimeMillis();
        isBegin = true;
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.FIRE_QUESTION, new FireQuestionTask(), 3, 3, TimeUnit.SECONDS);
    }

    @Override
    public void end() {
        isBegin = false;
        for (FamilyBonFireData fireData : FAMILY_FIRE_DATAS.values()) {
            if (fireData == null) return;
            for (RoleBonFireCache roleCache : fireData.getRoleBonFireMap().values()) {
                removeMemberWhileEnd(fireData, roleCache);
            }
            fireData.getRoleBonFireMap().clear();
        }
        FAMILY_FIRE_DATAS.clear();
        SchedulerManager.shutDownNow(ExcutorKey.FIRE_QUESTION);
    }

    @Override
    public void addUpdateMember(long familyId, long roleId, int roleLevel, int roleJob) {
        FamilyBonFireData fireData = getFamilyBonFireData(familyId);
        if (fireData == null || fireData.getRoleBonFireMap() == null) return;//家族信息有误,不存在此家族
        if (fireData.getRoleBonFireMap().containsKey(roleId)) return;

        fireData.getRoleBonFireMap().put(roleId, new RoleBonFireCache(roleId, roleLevel, roleJob));
    }

    @Override
    public void updateRoleLevel(long familyId, long roleId, int roleLevel) {
        FamilyBonFireData fireData = getFamilyBonFireData(familyId);
        if (fireData == null || fireData.getRoleBonFireMap() == null) return;//家族信息有误,不存在此家族
        if (!fireData.getRoleBonFireMap().containsKey(roleId)) return;

        RoleBonFireCache roleBonFireCache = fireData.getRoleBonFireMap().get(roleId);
        if (roleBonFireCache == null) return;
        roleBonFireCache.setRoleLevel(roleLevel);
    }

    @Override
    public void removeMember(long familyId, long roleId) {
        FamilyBonFireData fireData = getFamilyBonFireData(familyId);
        if (fireData == null || fireData.getRoleBonFireMap() == null) return;//家族信息有误,不存在此家族
        if (!fireData.getRoleBonFireMap().containsKey(roleId)) return;

        RoleBonFireCache roleBonFireCache = fireData.getRoleBonFireMap().remove(roleId);
        roleBonFireCache.setCanGetWood(false);
        Map<Integer, Integer> map = doRoleFireDrop(fireData, roleBonFireCache);
        if (StringUtil.isEmpty(map)) return;
        //移除前增加经验
        ServiceHelper.roleService().notice(roleId, new RoleNotification(new BonFireDropEvent(BonFireDropEvent.TYPE_EXP, map)));
    }

    private void removeMemberWhileEnd(FamilyBonFireData fireData, RoleBonFireCache roleBonFireCache) {
        Map<Integer, Integer> map = doRoleFireDrop(fireData, roleBonFireCache);
        if (StringUtil.isEmpty(map)) return;
        //移除前增加经验
        ServiceHelper.roleService().notice(roleBonFireCache.getRoleId(), new RoleNotification(new BonFireDropEvent(BonFireDropEvent.TYPE_EXP, map)));
    }

    private FamilyBonFireData getFamilyBonFireData(long familyId) {
        if (!isBegin) return null;// 活动未开启
        FamilyBonFireData familyBonFireData = FAMILY_FIRE_DATAS.get(familyId);
        if (familyBonFireData == null) {
            familyBonFireData = createFamilyBonFireData(familyId);
            if (familyBonFireData == null) return null;
            FAMILY_FIRE_DATAS.put(familyId, familyBonFireData);
        }
        updateFamilyBonFireExp(familyBonFireData);//刷新每秒扣除篝火经验
        return familyBonFireData;
    }

    private FamilyBonFireData createFamilyBonFireData(long familyId) {
        FamilyData familyData = ServiceHelper.familyMainService().getFamilyDataClone(familyId);
        if (familyData == null) return null;
        FamilyBonFireData familyBonFireData = new FamilyBonFireData(familyId);
        familyBonFireData.setLastUpdateExpTimes(bonFireBeginTimes);
        return familyBonFireData;
    }

    /**
     * 刷新每秒扣除篝火经验
     */
    private void updateFamilyBonFireExp(FamilyBonFireData familyFire) {
        if (familyFire == null) return;
        long curTime = System.currentTimeMillis();
        long lastUpdateTimes = familyFire.getLastUpdateExpTimes();

        int diff = (int) ((curTime - lastUpdateTimes) / TimeUtil.SECOND);
        if (diff >= 1) {//篝火经验最多1秒刷新一次
//            StringBuffer sb = new StringBuffer();
//            sb.append("家族id："+familyFire.getFamilyId()+"刷新每秒扣除篝火经验|当前时间：").append(TimeUtil.toDateString(curTime)).append("|上次更新:").append(TimeUtil.toDateString(lastUpdateTimes))
//              .append("|间隔：").append(diff).append("秒|更新后:");
            familyFire.setLastUpdateExpTimes(lastUpdateTimes + diff * TimeUtil.SECOND);
//            sb.append(TimeUtil.toDateString(familyFire.getLastUpdateExpTimes())).append("|当前篝火等级：")
//              .append(familyFire.getLevel()).append("|当前篝火经验:").append(familyFire.getExp());
//            LogUtil.info(sb.toString());
            reduceFamilyFireExp(familyFire, diff);//减少经验
        }
    }

    /**
     * 调用此方法前默认必定会触发降级
     */
    private void handleFamilyFireDegrade(FamilyBonFireData familyFire, int diff) {
        if (familyFire.getLevel() == 1) {//最低为1级0经验
            familyFire.setExp(0);
            return;
        }
        FamilyFireVo fireVo = FamilyBonfrieManager.getFireVo(familyFire.getLevel());
        if (fireVo == null) return;
        int time = (int) Math.ceil(1.0 * familyFire.getExp() / fireVo.getSubExp());
        int reduceExp = time * fireVo.getSubExp();
        familyFire.reduceExp(reduceExp);
        familyFire.reduceLevel();
        fireVo = FamilyBonfrieManager.getFireVo(familyFire.getLevel());
        familyFire.addExp(fireVo.getReqExp());

        diff = diff - time;
        if (diff <= 0) {
//            LogUtil.info("家族id："+familyFire.getFamilyId()+"|篝火扣除经验后等级："+familyFire.getLevel()+"|经验："+familyFire.getExp());
            return;
        }
        reduceFamilyFireExp(familyFire, diff);//减少经验
    }

    /**
     * 根据秒数扣除经验
     */
    private void reduceFamilyFireExp(FamilyBonFireData familyFire, int diff) {
        FamilyFireVo fireVo = FamilyBonfrieManager.getFireVo(familyFire.getLevel());
        if (fireVo == null) return;
        int reduceExp = diff * fireVo.getSubExp();
        if (reduceExp == 0) return;
        if (reduceExp > familyFire.getExp()) {//触发降级
            handleFamilyFireDegrade(familyFire, diff);
        } else {
            familyFire.reduceExp(reduceExp);
//            LogUtil.info("家族id："+familyFire.getFamilyId()+"|篝火扣除经验后等级："+familyFire.getLevel()+"|经验："+familyFire.getExp());
        }
    }

    /**
     * 获得篝火活动剩余时间,单位秒
     * -1为不在活动时间内
     */
    private long getRemainTime() {
        Map<Integer, String> configMap = DataManager.getActivityFlowConfig(3);
        if (StringUtil.isEmpty(configMap)) return -1;
        String cronExpr = configMap.get(2);
        if (StringUtil.isEmpty(cronExpr)) return -1;
        try {
            long remainTime = ActivityFlowUtil.remainder(System.currentTimeMillis(), cronExpr);
            if (remainTime < 0) {
                return -1;
            } else {
                return remainTime;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public void updateRoleFire(long familyId, long roleId) {
        FamilyBonFireData fireData = getFamilyBonFireData(familyId);
        if (fireData == null || fireData.getRoleBonFireMap() == null) return;//家族信息有误,不存在此家族
        if (!fireData.getRoleBonFireMap().containsKey(roleId)) return;

        RoleBonFireCache roleBonFireCache = fireData.getRoleBonFireMap().get(roleId);
        if (roleBonFireCache == null) return;

        checkAndRefreshWood(roleBonFireCache);//检测并刷新干柴

        Map<Integer, Integer> map = doRoleFireDrop(fireData, roleBonFireCache);

        int exp = 0;
        if (StringUtil.isNotEmpty(map)) {
            Integer expCount = map.get(ToolManager.EXP);
            if (expCount != null && expCount > 0) {
                exp = expCount;
                ServiceHelper.roleService().notice(roleId, new RoleNotification(new BonFireDropEvent(BonFireDropEvent.TYPE_EXP, map)));
            }
        }
        ClientBonfire client = new ClientBonfire(ClientBonfire.UPDATE);
        client.setActivityStatus(BonfireActivityFlow.isStarted()==true?(byte)1:(byte)0);
        client.setFireLevel(fireData.getLevel());
        client.setFireExp(fireData.getExp());
        client.setExp(exp);
        PacketManager.send(roleId, client);
    }

    /**
     * 检测并刷新干柴
     */
    private void checkAndRefreshWood(RoleBonFireCache roleBonFireCache) {
        if (roleBonFireCache == null || roleBonFireCache.isCanGetWood()) return;
        //刷新CD
        long now = System.currentTimeMillis();
        if ((now - roleBonFireCache.getLastUpdateWoodTimes()) / TimeUtil.SECOND < FamilyBonfrieManager.WOOD_REFRESH_CD)
            return;

        roleBonFireCache.setCanGetWood(true);
        roleBonFireCache.setLastUpdateWoodTimes(now);

        ClientBonfire client = new ClientBonfire(ClientBonfire.REFRESH_WOOD);
        PacketManager.send(roleBonFireCache.getRoleId(), client);
    }

    private Map<Integer, Integer> doRoleFireDrop(FamilyBonFireData fireData, RoleBonFireCache roleBonFireCache) {
        FamilyFireVo fireVo = FamilyBonfrieManager.getFireVo(fireData.getLevel());
        if (fireVo == null) return null;
        long now = System.currentTimeMillis();
        long lastUpdateTimes = roleBonFireCache.getLastUpdateExpTimes();
        if (now - lastUpdateTimes < 5 * TimeUtil.SECOND) {
            return null;
        }
        int times = (int) ((now - lastUpdateTimes) / (5 * TimeUtil.SECOND));
        roleBonFireCache.addLastUpdateExpTimes(times * 5 * TimeUtil.SECOND);

        Map<Integer, Integer> map = DropUtil.executeDrop(fireVo.getDropGroupId(), roleBonFireCache.getRoleId(), roleBonFireCache.getRoleLevel(), roleBonFireCache.getRoleJob(), 0, "", false, times);
        Integer expBoxCount = map.get(ToolManager.EXP_BOX);
        if (expBoxCount == null || expBoxCount <= 0) return null;
        ItemVo expBoxItem = ToolManager.getItemVo(ToolManager.EXP_BOX);
        if (expBoxItem == null) return null;
        double coeff = DataManager.getGradeCoeff(roleBonFireCache.getRoleLevel() + "+" + expBoxItem.getGradecoefftype());
        int count = (int) Math.floor(coeff / 100 * (double) expBoxCount);
        if (count <= 0) return null;

        map.clear();
        map.put(ToolManager.EXP, count);
        return map;
    }

    /**
     * 请求初始化篝火活动信息
     */
    public void initRoleFireInfo(long familyId, long roleId, int roleLevel, int roleJob, int remainThrowGoldTimes) {
        FamilyBonFireData fireData = getFamilyBonFireData(familyId);
        if (fireData == null || fireData.getRoleBonFireMap() == null) return;//家族信息有误,不存在此家族

        RoleBonFireCache roleBonFireCache = fireData.getRoleBonFireMap().get(roleId);
        if (roleBonFireCache == null) {
            roleBonFireCache = new RoleBonFireCache(roleId, roleLevel, roleJob);
            fireData.getRoleBonFireMap().put(roleId, roleBonFireCache);
        }
        roleBonFireCache.setCanGetWood(false);

        ClientBonfire client = new ClientBonfire(ClientBonfire.INIT);
        client.setActivityStatus(BonfireActivityFlow.isStarted()==true?(byte)1:(byte)0);
        client.setFireLevel(fireData.getLevel());
        client.setFireExp(fireData.getExp());
        client.setRemainTime(getRemainTime());
        client.setDailyThrowGoldTimes(remainThrowGoldTimes);
        PacketManager.send(roleId, client);

        refreshCurQuestion(roleId, fireData);//刷新当前答题信息
    }

    /**
     * 增加篝火经验
     */
    public void addFamilyFireExp(long familyId, long roleId, int exp) {
        FamilyBonFireData fireData = getFamilyBonFireData(familyId);
        if (fireData == null || fireData.getRoleBonFireMap() == null) return;//家族信息有误,不存在此家族
        if (!fireData.getRoleBonFireMap().containsKey(roleId)) return;
        RoleBonFireCache roleBonFireCache = fireData.getRoleBonFireMap().get(roleId);
        if (roleBonFireCache == null) return;
        FamilyFireVo fireVo = FamilyBonfrieManager.getFireVo(fireData.getLevel());
        if (fireVo == null) return;

//        StringBuffer sb = new StringBuffer();
//        sb.append("家族id:").append(fireData.getFamilyId()).append("|增加篝火经验|当前等级:").append(fireData.getLevel())
//                .append("|当前经验：").append(fireData.getExp());
        fireData.addExp(exp);//增加经验
//        sb.append("|增加后经验：").append(fireData.getExp()).append("|增加的经验：").append(exp);
        checkFireExpAndUpgrade(fireData);//检测升级
//
//        sb.append("|检查升级后等级：").append(fireData.getLevel()).append("|检查后篝火经验：").append(fireData.getExp());
//        LogUtil.info(sb.toString());
        updateRoleFire(familyId, roleId);//刷新篝火信息
    }

    /**
     * 捡干柴并立刻投掷
     */
    public void pickWood(long familyId, long roleId) {
        FamilyBonFireData fireData = getFamilyBonFireData(familyId);
        if (fireData == null || fireData.getRoleBonFireMap() == null) return;//家族信息有误,不存在此家族
        if (!fireData.getRoleBonFireMap().containsKey(roleId)) return;
        RoleBonFireCache roleBonFireCache = fireData.getRoleBonFireMap().get(roleId);
        if (roleBonFireCache == null) return;

        if (!roleBonFireCache.isCanGetWood()) return;
        roleBonFireCache.setCanGetWood(false);

        Map<Integer, Integer> reward = DropUtil.executeDrop(FamilyBonfrieManager.WOOD_DROP_GROUP, roleId, roleBonFireCache.getRoleLevel(), roleBonFireCache.getRoleJob(), 0, "", false, 1);
        if (StringUtil.isNotEmpty(reward)) {
            ServiceHelper.roleService().notice(roleId, new RoleNotification(new BonFireDropEvent(BonFireDropEvent.TYPE_THROW_WOOD, reward)));
        }

        //增加篝火经验
        addFamilyFireExp(familyId, roleId, FamilyBonfrieManager.WOOD_EXP);
    }

    /**
     * 检测并升级篝火
     */
    private void checkFireExpAndUpgrade(FamilyBonFireData fireData) {
        FamilyFireVo fireVo = FamilyBonfrieManager.getFireVo(fireData.getLevel());
        FamilyFireVo nextFireVo;
        if (fireVo == null) return;
        while (fireData.getExp() > fireVo.getReqExp()) {
            nextFireVo = FamilyBonfrieManager.getFireVo(fireData.getLevel() + 1);
            if (nextFireVo == null) {//没有下一级
                fireData.setExp(fireVo.getReqExp());//设为最大上限
            } else {
                fireData.setLevel(nextFireVo.getLevel());
                fireData.reduceExp(fireVo.getReqExp());
            }
        }
    }

    /**
     * 刷新题目,并下发给所有在篝火场景的玩家
     */
    public void sendCurQuestionToOnline() {
        BonFireQuestionCache questionCache = getCurQuestionCache();
        if (questionCache == null) return;

        ClientBonfire clientBonfire = getCurQuestionClient();
        if (clientBonfire == null) return;

        FamilyQuestion questionVo = FamilyBonfrieManager.getQuestion(questionCache.getQuestionId());
        if (questionVo == null) return;

        for (FamilyBonFireData fireData : FAMILY_FIRE_DATAS.values()) {
            if (fireData == null) continue;
            String questionDesc = "第" + StringUtil.getChineseNumber(questionIndex + 1) + "题:" + DataManager.getGametext(questionVo.getQuestionDesc());
            //把题目发到各个公会的聊天频道
            ServiceHelper.chatService().chat("系统", ChatManager.CHANNEL_FAMILY, 0L, fireData.getFamilyId(), questionDesc, false);

            for (RoleBonFireCache roleBonFireCache : fireData.getRoleBonFireMap().values()) {
                if (roleBonFireCache == null) continue;
                PacketManager.send(roleBonFireCache.getRoleId(), clientBonfire);
            }
        }
    }

    public void answerQuestion(long familyId, long roleId, String name, int questionId, int questionIndex) {
        FamilyBonFireData fireData = getFamilyBonFireData(familyId);
        if (fireData == null || fireData.getRoleBonFireMap() == null) return;//家族信息有误,不存在此家族
        if (!fireData.getRoleBonFireMap().containsKey(roleId)) return;

        RoleBonFireCache roleBonFireCache = fireData.getRoleBonFireMap().get(roleId);
        if (roleBonFireCache == null) return;

        BonFireQuestionCache questionCache = getCurQuestionCache();
        if (questionCache == null) return;
        if (questionCache.getQuestionId() != questionId) return;

        FamilyQuestion question = FamilyBonfrieManager.getQuestion(questionId);
        if (question == null) return;

        long now = System.currentTimeMillis();
        if (now - curQuestionEndTimes > 1 * TimeUtil.SECOND) {
            //回答超时
            return;
        }

        int index = 0;
        if (questionCache.getAnswerList() != null && questionCache.getAnswerList().size() >= questionIndex) {
            index = questionCache.getAnswerList().get(questionIndex - 1);
        }
        RoleQuestionCache cache = fireData.getRoleQuestionMap().get(roleId);
        if (cache != null && cache.hasAlreadyAnswer(questionId)) {
            //已经回答过了
            return;
        }

        Map<Integer, Integer> reward;
        if (index == 1) {//回答正确
            fireData.recordAnswer(roleId, name, questionId, true);
            reward = new HashMap<>(question.getAwardMap());

            addFamilyFireExp(fireData.getFamilyId(), roleId, question.getFireExp());
//            fireData.addExp(question.getFireExp());//回答正确，增加篝火经验

            PacketManager.send(roleId, new ClientText("回答正确"));
            PacketManager.send(roleId, new ClientAward(question.getAwardMap()));
        } else {//回答错误
            fireData.recordAnswer(roleId, name, questionId, false);
            reward = new HashMap<>(question.getWrongAwardMap());

            PacketManager.send(roleId, new ClientText("回答错误"));
        }

        if (StringUtil.isNotEmpty(reward)) {
            ServiceHelper.roleService().notice(roleId, new RoleNotification(new BonFireDropEvent(BonFireDropEvent.TYPE_ANSWER, reward)));
        }
    }

    /**
     * 刷新当前答题信息
     */
    private void refreshCurQuestion(long roleId, FamilyBonFireData fireData) {
        BonFireQuestionCache questionCache = getCurQuestionCache();
        if (questionCache == null) return;
        RoleQuestionCache cache = fireData.getRoleQuestionMap().get(roleId);
        if (cache != null && cache.hasAlreadyAnswer(questionCache.getQuestionId())) {
            return;//已经回答过了
        }

        ClientBonfire clientBonfire = getCurQuestionClient();
        if (clientBonfire == null) return;

        long now = System.currentTimeMillis();
        if (curQuestionEndTimes < now + 3 * TimeUtil.SECOND) return;//答题结束前3秒不下发
        PacketManager.send(roleId, clientBonfire);//下发题目信息
    }

    /**
     * 获得当前问题client
     */
    private ClientBonfire getCurQuestionClient() {
        BonFireQuestionCache questionCache = getCurQuestionCache();
        if (questionCache == null) return null;

        ClientBonfire client = new ClientBonfire(ClientBonfire.QUESTION_INFO);
        client.setQuestionId(questionCache.getQuestionId());
        client.setAnswerList(new ArrayList<>(questionCache.getAnswerList()));
        client.setRemainSecond(getRemainSecond());
        client.setQuestionIndex(questionIndex + 1);
        return client;
    }

    /**
     * 获得当前题目剩余时间
     */
    private int getRemainSecond() {
        long now = System.currentTimeMillis();
        int remainSecond = (int) ((curQuestionEndTimes - now) / TimeUtil.SECOND);
        return remainSecond < 0 ? 0 : remainSecond;
    }

    /**
     * 重置题目
     */
    public void resetDailyQuestion() {
        DailyFireQuestionCaches.clear();
        questionIndex = -1;
        curQuestionBeginTimes = 0;
        curQuestionEndTimes = 0;
        if (StringUtil.isEmpty(FamilyBonfrieManager.getFamilyQuestionMap())) return;

        int totalWeight = 0;//总权重
        Map<Integer, FamilyQuestion> questionMap = new HashMap<>(FamilyBonfrieManager.getFamilyQuestionMap().size());
        for (FamilyQuestion question : FamilyBonfrieManager.getFamilyQuestionMap().values()) {
            questionMap.put(question.getQuestionId(), question);
            totalWeight += question.getOdds();
        }

        Random random = new Random();
        int curWeight, randomValue;
        List<BonFireQuestionCache> list = new ArrayList<>();
        FamilyQuestion questionSelect;
        BonFireQuestionCache questionCache;
        while (list.size() < FamilyBonfrieManager.QUESTIONS_COUNT && questionMap.size() > 0) {
            questionSelect = null;
            randomValue = random.nextInt(totalWeight);
            curWeight = 0;
            for (FamilyQuestion question : questionMap.values()) {
                curWeight += question.getOdds();
                if (curWeight >= randomValue) {
                    questionSelect = question;
                    break;
                }
            }

            if (questionSelect == null) break;
            questionMap.remove(questionSelect.getQuestionId());
            totalWeight -= questionSelect.getOdds();

            questionCache = new BonFireQuestionCache(questionSelect.getQuestionId());
            randomAnswerIndex(questionCache);//随机打乱答案
            list.add(questionCache);
        }
        DailyFireQuestionCaches = list;
    }

    /**
     * 题目答案乱序，每日重置(/启服初始化)时处理
     */
    private void randomAnswerIndex(BonFireQuestionCache cache) {
        if (cache == null) return;
        FamilyQuestion question = FamilyBonfrieManager.getQuestion(cache.getQuestionId());
        if (question == null) return;
        int answerCount = question.getAnswerCount();
        List<Integer> list = new LinkedList<>();
        for (int i = 1; i <= answerCount; i++) {
            list.add(i);
        }
        List<Integer> answerList = new ArrayList<>();
        Random random = new Random();
        int randomIndex;
        while (list.size() > 0) {
            randomIndex = random.nextInt(list.size());
            answerList.add(list.get(randomIndex));
            list.remove(randomIndex);
        }
        cache.setAnswerList(answerList);
    }

    /**
     * 每日重置
     */
    public void dailyReset() {
        FAMILY_FIRE_DATAS.clear();
        bonFireBeginTimes = 0;
        resetDailyQuestion();//重置题目
    }

    public void questionEnd() {
        BonFireQuestionCache cache = FamilyBonFireServiceActor.getCurQuestionCache();
        if (cache == null || cache.isEnd()) return;
        cache.setEnd(true);//标识为已结束

        BonFireQuestionCache questionCache = getCurQuestionCache();
        if (questionCache == null) return;

        FamilyQuestion questionVo = FamilyBonfrieManager.getQuestion(questionCache.getQuestionId());
        if (questionVo == null) return;

        //A+答案
        String answerDesc = getQuestionShowChar(questionCache.getRightIndex()) + DataManager.getGametext(questionVo.getRightDesc());
        //第X题 & A+答案
        String questionDesc = String.format(DataManager.getGametext("familyfire_desc_questionover"), StringUtil.getChineseNumber(questionIndex + 1), answerDesc);

        for (FamilyBonFireData fireData : FAMILY_FIRE_DATAS.values()) {
            if (fireData == null) continue;
            //把题目答案发到各个公会的聊天频道
            ServiceHelper.chatService().chat("系统", ChatManager.CHANNEL_FAMILY, 0L, fireData.getFamilyId(), questionDesc, false);
        }
    }

    private String getQuestionShowChar(int i) {
        if (i == 1) {
            return "A";
        } else if (i == 2) {
            return "B";
        } else if (i == 3) {
            return "C";
        } else if (i == 4) {
            return "D";
        }
        return "";
    }
}
