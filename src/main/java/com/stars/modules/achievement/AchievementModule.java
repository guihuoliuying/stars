package com.stars.modules.achievement;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.achievement.event.AchievementEvent;
import com.stars.modules.achievement.packet.ClientAchievement;
import com.stars.modules.achievement.prodata.AchievementStageVo;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.modules.achievement.userdata.AchievementStagePo;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.event.FightScoreChangeEvent;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.modules.vip.VipModule;
import com.stars.services.ServiceHelper;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.AchievementRankPo;
import com.stars.util.DateUtil;
import com.stars.util.I18n;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by zhouyaohui on 2016/10/17.
 */
public class AchievementModule extends AbstractModule {

    private Map<Integer, AchievementRow> roleAchievement = new HashMap<>();
    private AchievementStagePo roleAchievementStagePo = null;
    private Set<Integer> achievementId = new HashSet<>();
    private AchievementRankPo achievementRankPo = null; //玩家刷新成就排行
    private long lastUpdateRankTime = 0L; //上次刷新排行榜时间（升战力，改名）

    public AchievementModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("成就", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        //加载玩家各项成就的状态
        roleAchievement = DBUtil.queryMap(DBUtil.DB_USER, "achievementid", AchievementRow.class,
                "select * from roleachievement where roleid = " + id());
        String sql = "select * from roleachievementstage where roleid = " + id();
        roleAchievementStagePo = DBUtil.queryBean(DBUtil.DB_USER, AchievementStagePo.class, sql);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        if(StringUtil.isEmpty(roleAchievementStagePo)){ //如果没有过记录，初始化
            roleAchievementStagePo = new AchievementStagePo();
            roleAchievementStagePo.setRoleId(id());
            context().insert(roleAchievementStagePo);
        }
        canGetAward();
    }

    @Override
    public void onTimingExecute() {
        if(achievementRankPo != null && (now() - lastUpdateRankTime) > 5* DateUtil.MINUTE){ //成就排行榜战力或改名5分钟刷新一次
            ServiceHelper.rankService().updateRank(RankConstant.RANK_TYPE_ACHIEVEMENT, achievementRankPo);
            lastUpdateRankTime = now();
            achievementRankPo = null;
        }
    }

    /**
     * 触发检查
     * @param type
     * @param condition
     */
    public void triggerCheck(int type, Object condition) {
        Map<Integer, AchievementVo> map = AchievementManager.typeMap.get(type);
        if (map == null) {
            return;
        }
        for (AchievementVo vo : map.values()) {
            AchievementRow row = roleAchievement.get(vo.getAchievementId());
            if (row == null) {
                row = new AchievementRow();
                row.setRoleId(id());
                row.setAchievementId(vo.getAchievementId());
                roleAchievement.put(row.getAchievementId(), row);
                context().insert(row);
            }
            if (row.getState() != AchievementRow.UNFINISH) {
                continue;
            }
            AchievementHandler handler = AchievementModuleFactory.newHandle(type);
            boolean update = false;
            try {
                update = handler.handler(row, vo, condition);
                canGetAward();
            } catch (Exception e) {
            }

            if (update) {
                context().update(row);
            }
            if (row.getState() != AchievementRow.UNFINISH) {
                // 达成成就，抛事件
                eventDispatcher().fire(new AchievementEvent(row.getAchievementId()));
            }
        }
    }

    /**
     * 成就是否达成
     * @param id
     * @return
     */
    public boolean isAchievementFinish(int id) {
        AchievementRow row = roleAchievement.get(id);
        if (row == null) {
            return false;
        }
        return row.getState() > AchievementRow.UNFINISH ? true : false;
    }

    /**
     * 打开成就界面
     */
    public void view() {
        ClientAchievement view = new ClientAchievement();
        view.setResType(ClientAchievement.RES_DATA);
        view.setRoleAchievement(roleAchievement);
        view.setRoleAchievementStagePo(roleAchievementStagePo);
        view.setRankRate(getRankRate());
        send(view);
        sendStageAwardStatus2Client();
    }

    /**
     * 领取奖励
     * @param achievementId
     */
    public void award(int achievementId) {
        AchievementRow row = roleAchievement.get(achievementId);
        if (row == null) {
            warn(I18n.get("achievement.notExist"));
            return;
        }
        if (row.getState() == AchievementRow.UNFINISH) {
            warn(I18n.get("achievement.notAchieve"));
            return;
        }
        if (row.getState() == AchievementRow.ONFINISH) {
            warn(I18n.get("achievement.rewarded"));
            return;
        }
        AchievementVo vo = AchievementManager.getAchievementVo(achievementId);
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        VipModule vipModule = (VipModule) module(MConst.Vip);
        if(!vo.matchLimit(roleModule.getLevel(),vipModule.getVipLevel())){
            warn("主角等级或贵族等级不足");
            return;
        }
        if(StringUtil.isEmpty(roleAchievementStagePo)){ //如果没有过记录，初始化
            roleAchievementStagePo = new AchievementStagePo();
            roleAchievementStagePo.setRoleId(id());
            context().insert(roleAchievementStagePo);
        }
        if (vo.getStage() > roleAchievementStagePo.getMaxActiveStage()) { //该段位还未开启
            warn("achievement_tips_notopen");
            return;
        }
        row.setState(AchievementRow.ONFINISH);
        context().update(row);

        ToolModule toolModule = module(MConst.Tool);
        toolModule.addAndSend(vo.getAward() ,EventType.AWARD.getCode());
        addStageScore(vo,row); //获得成就值
        canGetAward();
        ClientAchievement res = new ClientAchievement();
        res.setResType(ClientAchievement.RES_AWARD);
        res.setAward(vo.getAward());
        res.setAchievementId(achievementId);
        send(res);
        sendStageAwardStatus2Client();
    }

    public void onEvent(Event event){
        if (event instanceof RoleRenameEvent) {
            AbstractRankPo po = ServiceHelper.rankService().getRank(RankConstant.RANKID_ACHIEVEMENT, id());
            if(po == null)
                return;
            if(achievementRankPo == null) {
                achievementRankPo = (AchievementRankPo) po;
                achievementRankPo = AchievementRankPo.build(achievementRankPo);
            }
            String newName = ((RoleRenameEvent) event).getNewName();
            achievementRankPo.setName(newName);
            //ServiceHelper.rankService().updateRank(RankConstant.RANK_TYPE_ACHIEVEMENT, achievementRankPo);
        }
        if(event instanceof FightScoreChangeEvent){
            AbstractRankPo po = ServiceHelper.rankService().getRank(RankConstant.RANKID_ACHIEVEMENT, id());
            if(po == null)
                return;
            if(achievementRankPo == null) {
                achievementRankPo = (AchievementRankPo) po;
                achievementRankPo = AchievementRankPo.build(achievementRankPo);
            }
            int newFightScore = ((FightScoreChangeEvent) event).getNewFightScore();
            achievementRankPo.setFightScore(newFightScore);
            //ServiceHelper.rankService().updateRank(RankConstant.RANK_TYPE_ACHIEVEMENT, achievementRankPo);
        }

    }

    public void canGetAward(){
        AchievementRow row;
        RoleModule roleModule = (RoleModule) moduleMap().get(MConst.Role);
        int roleLevel = roleModule.getLevel();
        for(Map.Entry<Integer, AchievementRow> entry:roleAchievement.entrySet()){
            row = entry.getValue();
            AchievementVo vo = AchievementManager.getAchievementVo(row.getAchievementId());
            if(vo == null)
                continue;
            if(row!=null ){
                if(row.getState()!=AchievementRow.ONFINISH && row.getState()!=AchievementRow.UNFINISH ){
                    VipModule vipModule = (VipModule) module(MConst.Vip);
                    if(!vo.matchLimit(roleModule.getLevel(),vipModule.getVipLevel())){
                       continue;
                    }
                    int maxStage = 1;
                    if(roleAchievementStagePo != null){
                        maxStage = roleAchievementStagePo.getMaxActiveStage();
                    }
                    if (maxStage >= vo.getStage()) {
                        achievementId.add(row.getAchievementId());
                    }
                }
                else {
                    if(achievementId.contains(row.getAchievementId())){
                        achievementId.remove(row.getAchievementId());
                    }
                }
            }
            else {
                if(achievementId.contains(row.getAchievementId())){
                    achievementId.remove(row.getAchievementId());
                }
            }
        }
        signCalRedPoint(MConst.Achievement,RedPointConst.ACHIEVEMENT_AWARD);
        signCalRedPoint(MConst.Achievement,RedPointConst.ACHIEVEMENT_STAGE_AWARD);
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if(redPointIds.contains(Integer.valueOf(RedPointConst.ACHIEVEMENT_AWARD))){
            checkAwardRedPoint(redPointMap, achievementId, RedPointConst.ACHIEVEMENT_AWARD);
        }
        if(redPointIds.contains(Integer.valueOf(RedPointConst.ACHIEVEMENT_STAGE_AWARD))){
            checkStageAwardRedPoint(redPointMap,RedPointConst.ACHIEVEMENT_AWARD);
        }
    }

    private void checkAwardRedPoint(Map<Integer, String> redPointMap,Set<Integer> list,int redPointConst){
        StringBuilder builder = new StringBuilder("");
        Set<Integer> stageSet = getHasAwardStage();
        if(StringUtil.isNotEmpty(stageSet)){
            for(int stage:stageSet){
                builder.append(stage).append("+");
            }
        }
//        if(!list.isEmpty()){
//            Iterator<Integer> iterator = list.iterator();
//            while (iterator.hasNext()){
//                builder.append(iterator.next()).append("+");
//            }
//            redPointMap.put(redPointConst,builder.toString().isEmpty() ? null : builder.toString());
//        }
//        else {
//            redPointMap.put(redPointConst, null );
//        }

            redPointMap.put(RedPointConst.ACHIEVEMENT_AWARD,
                    builder.toString().isEmpty() ? null : builder.toString());
    }

    private void checkStageAwardRedPoint(Map<Integer, String> redPointMap,int redPointConst){
        StringBuilder builder = new StringBuilder("");
        Set<Integer> stageSet = getHasStageAwardStage();
        if(StringUtil.isNotEmpty(stageSet)){
            for(int stage:stageSet){
                builder.append(stage).append("+");
            }
        }
        redPointMap.put(RedPointConst.ACHIEVEMENT_STAGE_AWARD,
                builder.toString().isEmpty() ? null : builder.toString());
    }

    public List<AchievementRow> getAchievementRowList() {
        return new ArrayList<>(roleAchievement.values());
    }

    /**
     * 增加玩家成就积分
     * @param achievementVo
     */
    private void addStageScore(AchievementVo achievementVo,AchievementRow achievementRow){
        if(achievementRow.getState() != AchievementRow.ONFINISH)
            return;
        //获得当前玩家该段位成就值
        Map<Integer, Integer> scoreMap = roleAchievementStagePo.getStageScoreMap();
        Integer currentScore = scoreMap.get(achievementVo.getStage());
        if(currentScore == null)
            currentScore = 0;
        int newScore = currentScore + achievementVo.getAchievementCount(); //加上新的成就值
        roleAchievementStagePo.getStageScoreMap().put(achievementVo.getStage(), newScore);
        com.stars.util.LogUtil.info("玩家获得成就值|roleid:{}|stage:{}|achievementid:{}|score:{}->{}",id(),achievementVo.getStage(),achievementVo.getAchievementId(),currentScore,newScore);
        context().update(roleAchievementStagePo);
        //更新排行榜信息
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        AchievementRankPo rank =  new AchievementRankPo();
        rank.setRoleId(id());
        rank.setName(roleModule.getRoleRow().getName());
        rank.setFightScore(roleModule.getFightScore());
        rank.setStage(roleAchievementStagePo.getMaxActiveStage());
        rank.setAchieveScore(roleAchievementStagePo.getTotalStageScore());
        ServiceHelper.rankService().updateRank(RankConstant.RANK_TYPE_ACHIEVEMENT, rank);
    }

    public void stageAward(int stage,byte awardType){
        AchievementStageVo achievementStageVo = AchievementManager.getAchievementStageVoByStage(stage);
        if(achievementStageVo == null){
            com.stars.util.LogUtil.info("没有该成就阶段产品数据|roleid:{}|stage:{}",id(),stage);
            return;
        }
        Integer stageScore = roleAchievementStagePo.getStageScoreMap().get(stage);
        if (stageScore == null){
            warn("积分不足");
            return;
        }

        if ( (awardType == AchievementManager.COMMON_STAGE_AWARD && stageScore < achievementStageVo.getStageUp())
                || (awardType == AchievementManager.PERFECT_STAGE_AWARD && stageScore < achievementStageVo.getReqperfect())){
            com.stars.util.LogUtil.info("玩家当前阶段成就值不足领取奖励|roleid:{}|stage:{}|awardType:{}|currentScore:{}",id(),stage,awardType,stageScore);
            warn("积分不足");
            return;
        }
        if (awardType == AchievementManager.COMMON_STAGE_AWARD){
            RoleModule roleModule = (RoleModule) module(MConst.Role);
            AchievementStageVo nextVo = AchievementManager.getAchievementStageVoByStage(stage+1);
            if(nextVo == null){
                warn("已经达到了最高阶");
                return;
            }
            if(roleModule.getLevel() < nextVo.getRoleLevel()){
                warn("等级未达到新段位要求");
                com.stars.util.LogUtil.info("等级未达到新段位要求|roleid:{}|leve：{}|stage:{}",id(),roleModule.getLevel(),stage);
                return;
            }
            Integer status = roleAchievementStagePo.getCommonAwardMap().get(stage);
            if(status != null){
                warn("奖励已领取，不可重复领取");
                com.stars.util.LogUtil.info("玩家已经领取过成就升阶奖励|roleid:{}|stage:{}",id(),stage);
                return;
            }
            ToolModule toolModule = module(MConst.Tool);
            toolModule.addAndSend(achievementStageVo.getStageUpAwardMap() ,EventType.ACHIEVEMENT_STAGE_AWARD.getCode());
            roleAchievementStagePo.getCommonAwardMap().put(stage,1); //奖励标识为领取
            canGetAward();
            view(); //刷新界面
            //发送升阶成功标志
            ClientAchievement client = new ClientAchievement();
            client.setResType(ClientAchievement.RES_STAGEUP_RESULT);
            client.setResult((byte)1);
            send(client);

        }else if(awardType == AchievementManager.PERFECT_STAGE_AWARD){
            Integer status = roleAchievementStagePo.getPerfectAwardMap().get(stage);
            if(status != null){
                warn("奖励已领取，不可重复领取");
                LogUtil.info("玩家已经领取过成就完美完成奖励|roleid:{}|stage:{}",id(),stage);
                return;
            }
            ToolModule toolModule = module(MConst.Tool);
            Map<Integer,Integer> awardMap = toolModule.addAndSend(achievementStageVo.getPerfectAwardMap() ,EventType.ACHIEVEMENT_STAGE_AWARD.getCode());
            ClientAward clientAward = new ClientAward(awardMap); //奖励飘字
            send(clientAward);
            ClientAchievement clientAchievement = new ClientAchievement();
            clientAchievement.setResType(ClientAchievement.RES_GET_PERFECT_AWARD_RESULT);
            clientAchievement.setStage(stage);
            send(clientAchievement);
            roleAchievementStagePo.getPerfectAwardMap().put(stage,1); //奖励标识为领取
        }
        sendStageAwardStatus2Client();
        context().update(roleAchievementStagePo);
    }



    public void viewRank(){
        List<AbstractRankPo> list = ServiceHelper.rankService().getFrontRank(RankConstant.RANKID_ACHIEVEMENT,AchievementManager.RANK_COUNT);
        AbstractRankPo po = ServiceHelper.rankService().getRank(RankConstant.RANKID_ACHIEVEMENT, id());

        List<AchievementRankPo> roleRankList = new ArrayList<>();
        if(po == null){
            RoleModule roleModule = (RoleModule) module(MConst.Role);
            AchievementRankPo rank =  new AchievementRankPo();
            rank.setRoleId(id());
            rank.setName(roleModule.getRoleRow().getName());
            rank.setFightScore(roleModule.getFightScore());
            rank.setStage(roleAchievementStagePo.getMaxActiveStage());
            rank.setAchieveScore(roleAchievementStagePo.getTotalStageScore());
            list.add((AbstractRankPo)rank);
        }else{
            if(po.getRank() > AchievementManager.RANK_COUNT)
                po.setRank(999);
            list.add(po.copy());
        }

        //发送到客户端
        ClientAchievement client = new ClientAchievement();
        client.setResType(ClientAchievement.RES_VIEW_RANK);
        client.setRankList(list);
        send(client);
        sendStageAwardStatus2Client();
    }

    /**
     * 发送有奖励可领取（成就奖励，成就提升奖励和成就完美完成奖励）的段位集
     */
    public void sendStageAwardStatus2Client(){
        Set<Integer> stageSet = new HashSet<>();
        stageSet.addAll(getHasAwardStage());
        stageSet.addAll(getHasStageAwardStage());
        ClientAchievement client = new ClientAchievement();
        client.setResType(ClientAchievement.RES_STAGE_AWARD_STATUS);
        client.setStageSet(stageSet);
        send(client);
    }

    /**
     * 获得玩家排行榜的超越百分比
     * @return
     */
    private int getRankRate(){
        AbstractRankPo roleRank = ServiceHelper.rankService().getRank(RankConstant.RANKID_ACHIEVEMENT,id()); //加上获得玩家排行榜名次
        if(roleRank != null && roleRank.getRank() == 1) //第一名
            return 100;
        int rankRate = 0;
        if(roleRank != null && roleRank.getRank() <= AchievementManager.RANK_COUNT && roleRank.getRank() !=0){ //排行榜50名内
            rankRate = (int)Math.floor((250-roleRank.getRank()) * 0.4);  //（250-排名）/250*100（除第一名外向下取整）
            return rankRate;
        }
        List<AbstractRankPo> frontRank50List = ServiceHelper.rankService().getFrontRank(RankConstant.RANKID_ACHIEVEMENT,AchievementManager.RANK_COUNT);
        if(StringUtil.isNotEmpty(frontRank50List)){ //50名外
            AchievementRankPo achievementRankPo = (AchievementRankPo) frontRank50List.get(frontRank50List.size()-1);
            int rankScore50 = achievementRankPo.getAchieveScore(); //获得第50名玩家的成就值
            rankRate = (int) Math.floor(roleAchievementStagePo.getTotalStageScore()* 80 / rankScore50 );
            return rankRate;
        }
        return 0; //没有排行榜都是0
    }


    /**
     * 返回有成就项可领取奖励的段位集
     * @return
     */
    private Set<Integer> getHasAwardStage(){
        Set<Integer> set = new HashSet<>();
        if(StringUtil.isNotEmpty(achievementId)){
            Set<Integer> stageSet = new HashSet<>();
            for(int achieveId : achievementId){  //获得所有可领取的奖励段位
                AchievementVo vo = AchievementManager.getAchievementVo(achieveId);
                if(vo == null)
                    continue;
                stageSet.add(vo.getStage());
            }

            for(int stage : stageSet){
                set.add(stage);
            }
        }
        return set;
    }

    /**
     * 返回有段位奖励（提升奖励和完成完成奖励）的段位集
     * @return
     */
    private Set<Integer> getHasStageAwardStage(){
        Set<Integer> set = new HashSet<>();
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        if(roleAchievementStagePo.getTotalStageScore() > 0){
            for(int stage = 1; stage <= roleAchievementStagePo.getMaxActiveStage(); stage++){
                Integer score = roleAchievementStagePo.getStageScoreMap().get(stage);
                if(score == null)
                    continue;
                Integer commonAwardStatus = roleAchievementStagePo.getCommonAwardMap().get(stage);
                Integer perfectAwardStatus = roleAchievementStagePo.getPerfectAwardMap().get(stage);
                AchievementStageVo stageVo = AchievementManager.getAchievementStageVoByStage(stage);
                if(stageVo == null)
                    continue;
                AchievementStageVo nextStageVo = AchievementManager.getAchievementStageVoByStage(stage+1);
                if((commonAwardStatus == null && score >= stageVo.getStageUp())){
                    if(nextStageVo != null && roleModule.getLevel() >= nextStageVo.getRoleLevel()){
                        set.add(stage);
                    }
                }

                if((perfectAwardStatus == null && score >= stageVo.getReqperfect())) {
                    set.add(stage);
                }
            }
        }
        return set;
    }


}
