package com.stars.modules.daily;

import com.stars.core.attr.FormularUtils;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.daily.event.DailyCountUseUpEvent;
import com.stars.modules.daily.packet.ClientDailyData;
import com.stars.modules.daily.prodata.DailyAwardVo;
import com.stars.modules.daily.prodata.DailyBallStageVo;
import com.stars.modules.daily.prodata.DailyVo;
import com.stars.modules.daily.userdata.DailyRecord;
import com.stars.modules.daily.userdata.RoleTagFightDelta;
import com.stars.modules.daily.userdata.RoleTmpDayInfo;
import com.stars.modules.data.DataManager;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.gem.GemModule;
import com.stars.modules.masternotice.MasterNoticeManager;
import com.stars.modules.newequipment.NewEquipmentModule;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.retrievereward.event.PreDailyRecordResetEvent;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.vip.VipModule;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.*;

public class DailyModule extends AbstractModule {


    private DailyRecord dailyRecord;

    public DailyModule(long id, Player self, EventDispatcher eventDispatcher,
                       Map<String, Module> moduleMap) {
        super("日常", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        dailyRecord = DBUtil.queryBean(DBUtil.DB_USER, DailyRecord.class,
                "select * from dailyrecord where roleid = " + id());
        if (dailyRecord == null) {
            dailyRecord = new DailyRecord(id());
            this.context().insert(dailyRecord);
        }
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        dailyRecord = new DailyRecord(id());
        this.context().insert(dailyRecord);
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        this.dailyRecord.clearGotAward();//清除领取奖励记录
        this.dailyRecord.clearCanGetAward(); //清除前一天领取的奖励
        this.dailyRecord.setSuperAwardDailyId(0); //清除前一天超级奖励
        this.dailyRecord.setSuperAwardId(0);
        this.dailyRecord.setDayFightScore(0); //清除前一天的战力差,标识有否超级奖励用
        dailyRecord.setHadDrawToday((byte)0); //标识未今日未抽过签
        RoleModule rm = (RoleModule) module(MConst.Role);
        this.dailyRecord.setDayRoleLevel(rm.getLevel()); //记录重置时候玩家的等级
        this.dailyRecord.setDayFightScore(getRoleTotalFightScore()); //计算奖励用
        checkAndResetSuperAward(); //检测是否触发超级大奖
        //short eventType = EventType.AWARD.getCode();
        //rm.addResource((byte) ToolManager.DAILY, -(rm.getResource((byte) ToolManager.DAILY)),eventType);//活跃度置0
        tellDailyReset();
        resetDailyRecord((byte) 1);
		getDailyData();
//        sendDailyAwardData();
        this.context().update(dailyRecord);
        signCalRedPoint();
    }

    @Override
    public void onWeeklyReset(boolean isLogin) throws Throwable {
        resetDailyRecord((byte) 2);
    }

    @Override
    public void onMonthlyReset() throws Throwable {

    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        // 标记需要计算的红点
        signCalRedPoint(MConst.Daily, RedPointConst.DAILY_AWARD);
        updateDailyBallAttrAndFightScore(false);
        if(dailyRecord.getDayRoleLevel() == 0){
            RoleModule rm = (RoleModule) module(MConst.Role);
            this.dailyRecord.setDayRoleLevel(rm.getLevel()); //记录重置时候玩家的等级
            this.dailyRecord.setDayFightScore(getRoleTotalFightScore()); //计算奖励用
            checkAndResetSuperAward(); //检测是否触发超级大奖
            getDailyData();
            this.context().update(dailyRecord);
            signCalRedPoint();
        }
    }

    @Override
    public void onSyncData() throws Throwable {
        checkAndSendDailyAward(DailyManager.AWARD_PROMPT_IMMEDIATE,false);
        getDailyData();
        signCalRedPoint();
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(RedPointConst.DAILY_BALL_LEVELUP)) {
            checkDailyBallLevelupRedPoint(redPointMap);
        }
        if (redPointIds.contains(RedPointConst.DAILY_HAD_AWARD)) {
            checkDailyAwardRedPoint(redPointMap);
        }
    }

    @Override
    public void signCalRedPoint(String moduelName, int redPointId) {
        super.signCalRedPoint(moduelName, redPointId);
    }

    public void signCalRedPoint() {
        signCalRedPoint(MConst.Daily, RedPointConst.DAILY_BALL_LEVELUP);
        signCalRedPoint(MConst.Daily, RedPointConst.DAILY_HAD_AWARD);
    }

    private void checkDailyBallLevelupRedPoint(Map<Integer, String> redPointMap){
        StringBuilder builder = new StringBuilder("");
        int level = dailyRecord.getDailyBallLevel();
        DailyBallStageVo dailyBallStageVo = DailyManager.getDailyBallStageVoByLevel(level);
        if(dailyBallStageVo != null && level != DailyManager.getMaxDailyBallLevel()){ //未满级
            RoleModule roleModule = module(MConst.Role);
            ToolModule toolModule = (ToolModule) module(MConst.Tool);
            if (roleModule.getResource((byte) ToolManager.DAILY_BALL_SCORE) >= dailyBallStageVo.getReqScore()
                    &&toolModule.contains(dailyBallStageVo.getCostMap())) { //材料足够
                builder.append("+");
            }
        }

        redPointMap.put(RedPointConst.DAILY_BALL_LEVELUP,
                builder.toString().isEmpty() ? null : builder.toString());

    }

    private void checkDailyAwardRedPoint(Map<Integer,String> redPointMap){
        StringBuilder builder = new StringBuilder("");
        //获得玩家标签信息
        List<RoleTagFightDelta> roleTagFightDeltaList = getRoleFightDeltaData();
        ForeShowModule foreShowModule = (ForeShowModule) module(MConst.ForeShow);
        if(StringUtil.isNotEmpty(roleTagFightDeltaList)){
            for(RoleTagFightDelta roleTagFightDelta:roleTagFightDeltaList){ //遍历标签
                Map<Short, DailyVo> dailyVoMap = DailyManager.getDailyVoMapByTag(roleTagFightDelta.getTagId());
                if(StringUtil.isEmpty(dailyVoMap))
                    continue;
                for(DailyVo dailyVo:dailyVoMap.values()){ //遍历活动
                    if(!foreShowModule.isOpen(dailyVo.getOpenName())) //功能未开启
                        continue;
                    if(dailyRecord.getDailyRecord(dailyVo.getDailyid()) != 0) //已经完成
                        continue;
                    DailyAwardVo dailyAwardVo = null;
                    if(dailyVo.getDailyid() == dailyRecord.getSuperAwardDailyId() && dailyVo.getSuperawardind() == (byte)1){ //如果是超级奖励
                        dailyAwardVo = DailyManager.getDailyAwardVoById(dailyRecord.getSuperAwardId()); //getTodayDailyAwardVo(dailyid,DailyManager.SUPER_AWARD);
                    }else if(dailyVo.getMutipleind() == (byte)1 && dailyRecord.getDailyRecord(dailyVo.getDailyid()) < 1){ //如果是首次奖励
                        dailyAwardVo = getTodayDailyAwardVo(dailyVo.getDailyid(),DailyManager.MUTIPLE_AWARD);
                    }
                    if(dailyAwardVo != null && !dailyRecord.getGotAwardSet().contains(dailyAwardVo.getDailyAwardId())){
                        builder.append(roleTagFightDelta.getTagId()).append("+");//.append(dailyVo.getDailyid()).append("+");
                        break;
                    }
                }
            }
        }
        redPointMap.put(RedPointConst.DAILY_HAD_AWARD,
                builder.toString().isEmpty() ? null : builder.toString());

    }

    public void resetDailyRecord(byte resetType) {
        Map<Short, Integer> map = dailyRecord.getRecordMap();
        if (map != null && map.size() > 0) {
            Iterator<Short> it = map.keySet().iterator();
            while (it.hasNext()) {
                short dailyId = it.next();
                DailyVo dv = DailyManager.getDailyVo(dailyId);
                dailyRecord.putDailRecord(dailyId, 0);
//                ClientDailyUpdate cdu = new ClientDailyUpdate();
//                cdu.putUpdateData(dailyId, 0);
//                cdu.setDailyCounter(0);
//                send(cdu);
                if (dv.getReset() == resetType) {
                    //map.remove(dailyId);
                    it.remove();
                }
            }
            addDailyCount((short) 1,1);
        }
    }

    /**
     * 获得今日最多可玩次数
    */
    public byte getMaxCount(short dailyId){
    	if (dailyId == DailyManager.DAILYID_MASTER_NOTICE) {//皇榜悬赏的总次数，需要做特殊处理
    		VipModule vipModule = (VipModule)module(MConst.Vip);
    		int myNobleLevel = vipModule.getVipLevel();
			return (byte) MasterNoticeManager.getTotalCountByNobelLevel(myNobleLevel);
		}else{
			DailyVo dv = DailyManager.getDailyVo(dailyId);
			if(dv == null) return 0;
			return dv.getCount();
		}
    }
    
    //获取对应的活跃次数;
    public int getDailyCount(short dailyId) {
        return dailyRecord.getDailyRecord(dailyId);
    }

    /**
     * 获取剩余次数
     *
     * @param dailyId
     * @return
     */
    public int getDailyRemain(short dailyId) {
        if (dailyId <= 0)
            return 0;
        return getMaxCount(dailyId) - getDailyCount(dailyId);
    }

    public void addDailyCount(short dailyId, int count) {
        int curr = dailyRecord.getDailyRecord(dailyId);
        DailyVo dv = DailyManager.getDailyVo(dailyId);
        if (dv == null) {
            return;
        }
        int maxCount = getMaxCount(dailyId);
        if (maxCount > 0 && curr >= maxCount) {
            return;
        }
        ToolModule tm = module(MConst.Tool);
        //第一次才加活跃度,改版成获得斗魂值
        if (curr < 1) {
//            tm.addAndSend(dv.getAward(),EventType.DAILYCOUNT.getCode());
            tm.addAndSend(dv.getAward(), EventType.GET_DAILY_BALL_SCORE.getCode());
            checkAndRecordDailyAward(dailyId);
        }
        int total = curr + count;
        int newCount = maxCount == 0 ? total : (total >= maxCount ? maxCount : total);
        dailyRecord.putDailRecord(dailyId, newCount);
        signCalRedPoint(MConst.Daily, RedPointConst.DAILY_AWARD);
//        ClientDailyUpdate cdu = new ClientDailyUpdate();
//        cdu.putUpdateData(dailyId, dailyRecord.getDailyRecord(dailyId));
//        cdu.setDailyCounter((int) tm.getCountByItemId(ToolManager.DAILY));
//        send(cdu);
        getDailyData();
        this.context().update(dailyRecord);

        //若活动剩余次数为0，则触发活动次数耗尽事件
        if (getDailyRemain(dailyId) <= 0) {
            eventDispatcher().fire(new DailyCountUseUpEvent(dailyId));
        }
        signCalRedPoint();
    }

//    public void getDailyAward(int award) {
//        if (!DailyManager.getDailyAwardMap().containsKey(award)) {
//            warn(I18n.get("daily.awardnotexist"));
//            return;
//        }
//        if (dailyRecord.isGotAward(award)) {
//            warn(I18n.get("daily.hasgetaward"));
//            return;
//        }
//        RoleModule roleModule = module(MConst.Role);
//        if (roleModule.getResource((byte) ToolManager.DAILY) < award) {
//            warn(I18n.get("daily.canNotReward"));
//            return;
//        }
//        String aString = DailyManager.getDailyAwardMap().get(award);
//        ToolModule tm = (ToolModule) module(MConst.Tool);
//        tm.addAndSend(aString,EventType.DAILYAWARD.getCode());
//        dailyRecord.gotAward(award);
//        signCalRedPoint(MConst.Daily, RedPointConst.DAILY_AWARD);
//        ClientDailyAward ca = new ClientDailyAward((byte) 0, award);
//        send(ca);
//        this.context().update(dailyRecord);
//    }

    public void getDailyData() {
        ClientDailyData cdu = new ClientDailyData();
        cdu.setRespType(ClientDailyData.RESP_DAILY_DATA_UPDATE);
        Map<Short, Integer> map = dailyRecord.getRecordMap();
        if(StringUtil.isEmpty(map))
            map = new HashMap<>();
        cdu.setDailyInfoMap(map);
        send(cdu);
    }

//    public void sendDailyAwardData(){
//    	HashSet<Integer> set = new HashSet<Integer>(dailyRecord.getGotAwardSet());
//        ClientDailyAward ca = new ClientDailyAward((byte) 1, set);
//        send(ca);
//    }
    
    public void checkRedPoint(Map<Integer, String> redPointMap) {
        //是否有奖励可领取
//        ToolModule tm = module(MConst.Tool);  可领取奖励已经去除，被斗魂珠替换掉了
//        int totalCount = (int) tm.getCountByItemId(ToolManager.DAILY);
//        Map<Integer, String> awardMap = DailyManager.getDailyAwardMap();
//        for (Map.Entry<Integer, String> entry : awardMap.entrySet()) {
//            if (totalCount >= entry.getKey() && !dailyRecord.isGotAward(entry.getKey())) {
//                redPointMap.put(RedPointConst.DAILY_AWARD, "");
//                return;
//            }
//        }
//        redPointMap.put(RedPointConst.DAILY_AWARD, null);
    }
    
    public void tellDailyReset(){
    	Map<Short, Integer> tempMap = new HashMap<Short, Integer>(this.dailyRecord.getRecordMap());
        eventDispatcher().fire(new PreDailyRecordResetEvent(tempMap));//通知日常活动将要重置
    }

    /**
     * 获得玩家所有标签信息，并排序
     * @return
     */
    private List<RoleTagFightDelta> getRoleFightDeltaData(){
        List<Byte> tagList = new ArrayList<>();
        tagList.addAll(DailyManager.getDailyVoByTagMap().keySet()); //获得所有的标签数
        List<RoleTagFightDelta> roleTagFightDeltaList = new ArrayList<>();

        List<Byte> superAwardTagList = null;
        if(dailyRecord.getSuperAwardDailyId() != 0){
            DailyVo dailyVo = DailyManager.getDailyVo((short)dailyRecord.getSuperAwardDailyId());
            if(dailyVo != null){
                superAwardTagList = dailyVo.getTagList();
            }
        }
        RoleTagFightDelta roleTagFightDelta;
        for(byte tagId:tagList){
            if(!checkTagOpen(tagId)) //如果所有系统未开启，则忽略
                continue;
            roleTagFightDelta = new RoleTagFightDelta();
            roleTagFightDelta.setRoleId(id());
            roleTagFightDelta.setTagId(tagId);
            roleTagFightDelta.setTotalRemainCount(getTotalRemainCountByTag(tagId));
            roleTagFightDelta.setFightDelta(getFightScoreDeltaByTag(tagId));
            if(superAwardTagList!=null && superAwardTagList.contains(tagId)){
                roleTagFightDelta.setContainSuperAward((byte)1); //标签里有超级奖励
            }
            roleTagFightDeltaList.add(roleTagFightDelta);
        }
        Collections.sort(roleTagFightDeltaList);
        return roleTagFightDeltaList;
    }

    /**
     * 获得排好序的左边标签
     */
    public void reqGetTagList(){
        //获得玩家标签信息
        List<RoleTagFightDelta> roleTagFightDeltaList = getRoleFightDeltaData();
        //发送左标签协议
        ClientDailyData clientDailyData = new ClientDailyData();
        clientDailyData.setRespType(ClientDailyData.RESP_TAG_LIST);
        clientDailyData.setRoleTagFightDeltaList(roleTagFightDeltaList);
        send(clientDailyData);

        //发送抽签
        if(dailyRecord.getHadDrawToday() != (byte)1){ //今日还没抽过签
           byte drawResult = 0; //默认没有超级大奖
           if(dailyRecord.isTriggerSuperAwardToday()){ //如果今日有超级大奖
               drawResult = (byte)1;
           }
            ClientDailyData client1 = new ClientDailyData();
            client1.setRespType(ClientDailyData.RESP_LUCK_DRAW);
            client1.setIsSuperAwardToday(drawResult); //今日抽奖结果
            //send(client1);  不需要了，先屏蔽协议
        }
        //发送斗魂珠数据
        sendDailyBall2Client();
    }

    //改变今天已经抽过签状态
    public void changeHadDrawTodayStatus(){
        //接受协议，改变已经抽签的状态
        dailyRecord.setHadDrawToday((byte) 1);
        this.context().update(dailyRecord);
    }

    /**
     * 获得该标签下的活动集合
     * @param tag
     */
    public void getActivityListByTag(byte tag){
        Map<Short,DailyVo> dailyVoMap = DailyManager.getDailyVoMapByTag(tag);
        if(StringUtil.isEmpty(dailyVoMap))
            return;
        ForeShowModule foreShowModule = (ForeShowModule) module(MConst.ForeShow);
        List<RoleTmpDayInfo> roleTmpDayInfos = new ArrayList<>();
        for(DailyVo dailyVo:dailyVoMap.values()){  //遍历该tag下的所有活动
            if(!foreShowModule.isOpen(dailyVo.getOpenName())) //系统未开放忽略
                continue;
            RoleTmpDayInfo roleTmpDayInfo = new RoleTmpDayInfo();
            short dailyid = dailyVo.getDailyid();
            roleTmpDayInfo.setDailyid(dailyid);
            Map<Integer, Integer> awardMap = new LinkedHashMap<>();
            if(dailyVo.getDailyid() == dailyRecord.getSuperAwardDailyId() && dailyVo.getSuperawardind() == (byte)1){ //如果是超级奖励
                DailyAwardVo dailyAwardVo = DailyManager.getDailyAwardVoById(dailyRecord.getSuperAwardId()); //getTodayDailyAwardVo(dailyid,DailyManager.SUPER_AWARD);
                if(dailyAwardVo != null && !dailyRecord.getGotAwardSet().contains(dailyAwardVo.getDailyAwardId())){ //有奖励且未领取
                    awardMap.putAll(dailyAwardVo.getShowAwardMap());
                    roleTmpDayInfo.setIsSuperAward((byte) 1);
                }
            }else if(dailyVo.getMutipleind() == (byte)1 && dailyRecord.getDailyRecord(dailyVo.getDailyid()) < 1){ //如果是首次奖励
                DailyAwardVo dailyAwardVo = getTodayDailyAwardVo(dailyid,DailyManager.MUTIPLE_AWARD);
                if(dailyAwardVo != null && !dailyRecord.getGotAwardSet().contains(dailyAwardVo.getDailyAwardId())){
                    awardMap.putAll(dailyAwardVo.getShowAwardMap());
                    roleTmpDayInfo.setIsMutipleAward((byte)1);
                    roleTmpDayInfo.setMutipleCount(dailyAwardVo.getMutiple());
                }
            }
            Map<Integer,Integer> showAwardMap = StringUtil.toMap(dailyVo.getShowitem(), Integer.class, Integer.class, '=', '|');
            awardMap.putAll(showAwardMap);
            roleTmpDayInfo.setFirstScore(dailyVo.getCanGetScore());
            roleTmpDayInfo.setRank(dailyVo.getRank());
            roleTmpDayInfo.setDoneCount(getDailyCount(dailyid));
            roleTmpDayInfo.setAwardMap(awardMap);
            roleTmpDayInfo.setFightDelta(getFightScoreDelta(dailyVo.getSysName()));
            roleTmpDayInfos.add(roleTmpDayInfo); //加入列表
        }
        Collections.sort(roleTmpDayInfos); //排序

        //发送到客户端
        ClientDailyData clientDailyData = new ClientDailyData();
        clientDailyData.setRespType(ClientDailyData.RESP_DAILYINFO_IN_TAG);
        clientDailyData.setChooseTagId(tag);
        clientDailyData.setRoleTmpDayInfoList(roleTmpDayInfos);
        send(clientDailyData);

    }

    /**
     * 检查tag是否已经开启
     * @param tag
     * @return
     */
    private boolean checkTagOpen(byte tag){
        Map<Short,DailyVo> dailyVoMap = DailyManager.getDailyVoMapByTag(tag);
        if(dailyVoMap == null)
            return false;
        ForeShowModule foreShowModule = (ForeShowModule) module(MConst.ForeShow);
        for(DailyVo dailyVo:dailyVoMap.values()){
            if(foreShowModule.isOpen(dailyVo.getOpenName())) //存在已开启的系统
                return true;
        }
        return false;
    }

    //获得某个标签下的总剩余次数
    private int getTotalRemainCountByTag(byte tag){
        Map<Short,DailyVo> dailyVoMap = DailyManager.getDailyVoMapByTag(tag);
        if(dailyVoMap == null)
            return 0;
        int totalRemainCount = 0;
        ForeShowModule foreShowModule = (ForeShowModule)module(MConst.ForeShow);
        for(DailyVo dailyVo:dailyVoMap.values()){
            if(!foreShowModule.isOpen(dailyVo.getOpenName())) //系统未开放忽略
                continue;
            totalRemainCount += getRemainCount(dailyVo);
        }
        return totalRemainCount;
    }

    //获得某个活动剩余活动次数
    private int getRemainCount(DailyVo dailyVo){
        if(dailyVo == null)
            return 0;
        if(dailyVo.getCount() == 0) //无限多次的为1，为了比剩余0次数的优先级高
            return 0;
        int remainCount = 0;
        int totolCount = dailyVo.getCount();
        int usedCount = dailyRecord.getDailyRecord(dailyVo.getDailyid());
        remainCount = totolCount - usedCount;
        remainCount = remainCount > 0 ? remainCount : 0;
        return remainCount;
    }

    /**
     * 获得某个标签下的战力差
     * @param tag
     * @return
     */
    private int getFightScoreDeltaByTag(byte tag){
        Map<Short,DailyVo> dailyVoMap = DailyManager.getDailyVoMapByTag(tag);
        if(dailyVoMap == null)
            return 0;
        ForeShowModule foreShowModule = (ForeShowModule) module(MConst.ForeShow);
        int totalFightDelta = 0; //统计总战力差
        List<String> mouduleNameList = new ArrayList<>();
        String sysName;
        for(DailyVo dailyVo:dailyVoMap.values()){
            sysName = dailyVo.getSysName();
            if(!foreShowModule.isOpen(dailyVo.getOpenName())) //系统未开放忽略
                continue;
            if(StringUtil.isEmpty(sysName)||sysName.equals("0")){
                totalFightDelta = totalFightDelta - 1; //没有战力系统
                continue;
            }
            if(!mouduleNameList.contains(sysName)){ //模块已经统计过了，不再统计
                totalFightDelta += getFightScoreDelta(sysName);
                mouduleNameList.add(sysName);
            }
        }
        return totalFightDelta;
    }

    /**
     * 计算某模块与推荐战力的查（推荐战力-当前战力）
     * @param sysName
     * @return
     */
    private int getFightScoreDelta(String sysName){
        RoleModule roleModule = (RoleModule)module(MConst.Role);
        int roleLevel = roleModule.getLevel();
        int fightDelta = 0; //中间变量
        int recommFightScore = 0; //推荐战力
        int currentFightScore = 0; //当前拥有的战力
        recommFightScore = DailyManager.getRecommFightScore(sysName,DataManager.getServerDays());
        currentFightScore = getFightScore(sysName);
        com.stars.util.LogUtil.info("玩家模块战力|roleid:{}|模块名:{}|推荐战力:{}|玩家模块战力:{}",id(),sysName,recommFightScore,currentFightScore);
        fightDelta = recommFightScore - currentFightScore;
        fightDelta = fightDelta > 0 ? fightDelta : 0; //算出战力差
        return fightDelta;
    }


    /**
     * 获得某个模块的战力
     * @param sysName
     * @return
     */
    private int getFightScore(String sysName){
        if(StringUtil.isEmpty(sysName)){
            return -1;
        }

        //宝石、装备、伙伴系统的特殊处理
        if(sysName.equals(RoleManager.FIGHTSCORE_GEM)){
            return context().recordMap().getInt(GemModule.GEM_MAX_HISTORY_FIGHTSCORE,0);
        }else if(sysName.equals(RoleManager.FIGHTSCORE_EQUIPMENT)){
            return context().recordMap().getInt(NewEquipmentModule.NEWEQUIPMENT_MAX_HISTORY_FIGHTSCORE,0);
        }else if(sysName.equals(MConst.Buddy)){
            return context().recordMap().getInt(BuddyModule.BUDDY_MAX_HISTORY_FIGHTSCORE,0);
        }

        RoleModule roleModule = (RoleModule) module(MConst.Role);
        Map<String, Integer> fightScoreMap = roleModule.getRoleRow().getFightScoreMap();
        if(!fightScoreMap.containsKey(sysName))
            return 0;
        return fightScoreMap.get(sysName);
    }

    /**
     * 检查是否触发超级大奖
     */
    private void checkAndResetSuperAward(){

        Map<Short,DailyVo> dailyVoMap = DailyManager.getDailyVoMap();
        if(dailyVoMap == null)
            return ;
        ForeShowModule foreShowModule = (ForeShowModule) module(MConst.ForeShow);
        List<RoleTmpDayInfo> roleTmpDayInfoList = new ArrayList<>();
        String sysName;
        for(DailyVo dailyVo:dailyVoMap.values()){ //遍历所有的系统
            sysName = dailyVo.getSysName();
            if(!foreShowModule.isOpen(dailyVo.getOpenName())) //系统未开放忽略
                continue;
            if(StringUtil.isEmpty(sysName) ||sysName.equals("0")) //没有战力系统
                continue;
            if(dailyVo.getSuperawardind() != (byte)1)   //没有配置超级大奖
                continue;
            int fightDelta = getFightScoreDelta(dailyVo.getSysName());
            if(fightDelta <= 0)
                continue;
            short dailyid = dailyVo.getDailyid();
            DailyAwardVo dailyAwardVo = getTodayDailyAwardVo(dailyid,DailyManager.SUPER_AWARD);
            if(dailyAwardVo == null) //没有对应的奖励
                continue;
            RoleTmpDayInfo roleTmpDayInfo = new RoleTmpDayInfo();
            roleTmpDayInfo.setDailyid(dailyid);
            roleTmpDayInfo.setFightDelta(fightDelta);
            roleTmpDayInfoList.add(roleTmpDayInfo); //加入列表
        }

        if(StringUtil.isEmpty(roleTmpDayInfoList)) //没有超级大奖信息
            return;
        Collections.sort(roleTmpDayInfoList);
        RoleTmpDayInfo roleTmpDayInfo = roleTmpDayInfoList.get(0); //战力差最大的活动
        short dailyId = roleTmpDayInfo.getDailyid();
        DailyVo dailyVo = DailyManager.getDailyVo(dailyId);
        int recommFightScore = DailyManager.getRecommFightScore(dailyVo.getSysName(),DataManager.getServerDays());
        int roleFightScore = getFightScore(dailyVo.getSysName());
        if (recommFightScore < roleFightScore) //战力已经比推荐的高了
            return;
        DailyAwardVo dailyAwardVo = getTodayDailyAwardVo(roleTmpDayInfo.getDailyid(),DailyManager.SUPER_AWARD);
        dailyRecord.setSuperAwardDailyId(dailyId);
        dailyRecord.setSuperAwardId(dailyAwardVo.getDailyAwardId());
        context().update(dailyRecord);

    }

    /**
     * 获得奖励， 1 为超级奖励 2为多倍奖励
     * @param dailyid
     * @param awardType
     * @return
     */
    private DailyAwardVo getTodayDailyAwardVo(short dailyid,byte awardType){
        List<DailyAwardVo> dailyAwardVoList = new ArrayList<>();
        if(awardType == (byte)1){
            dailyAwardVoList = DailyManager.getSuperAwardList();
        }else if(awardType == (byte)2){
            dailyAwardVoList = DailyManager.getMultipleAwardList();
        }
        if(StringUtil.isEmpty(dailyAwardVoList))
            return null;

        byte supserAwardType = checkAndGetSuperAwardType(dailyid);

        int roleLevel = dailyRecord.getDayRoleLevel();  //日重置时玩家等级
        for(DailyAwardVo dailyAwardVo: dailyAwardVoList){
            if(!dailyAwardVo.matchDailyId(dailyid))
                continue;
            if (!dailyAwardVo.matchLevel(roleLevel))
                continue;
            if(!dailyAwardVo.matchFightScore(dailyRecord.getDayFightScore()))
                continue;
            if(!dailyAwardVo.matchSuperAwardType(supserAwardType))
                continue;
            return dailyAwardVo; //获得匹配的奖励
        }
        return null;
    }

    /**
     * 请求升级斗魂珠
     */
    public void reqDailyBallLevelup(){
        int level = dailyRecord.getDailyBallLevel();
        DailyBallStageVo dailyBallStageVo = DailyManager.getDailyBallStageVoByLevel(level);
        if(dailyBallStageVo == null){
            com.stars.util.LogUtil.info("该玩家等级缺乏产品数据dailyballstage|roleid:{}|level:{}",id(),level);
            return;
        }
        if(level == DailyManager.getMaxDailyBallLevel()){
            warn("已达到最高等级");
            return;
        }
        RoleModule roleModule = module(MConst.Role);
        if (roleModule.getResource((byte) ToolManager.DAILY_BALL_SCORE) < dailyBallStageVo.getReqScore()) {
            warn("活跃值不足");
            return;
        }
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        if(!toolModule.contains(dailyBallStageVo.getCostMap())){
            warn("材料不足");
            return;
        }

        //扣除斗魂值
        roleModule.addResource((byte) ToolManager.DAILY_BALL_SCORE, -dailyBallStageVo.getReqScore(),EventType.DAILY_BALL_LEVEL_UP.getCode());
        toolModule.deleteAndSend(dailyBallStageVo.getCostMap(),EventType.DAILY_BALL_LEVEL_UP.getCode());
        int preLevel = dailyRecord.getDailyBallLevel();
        dailyRecord.setDailyBallLevel(preLevel+1);
        ServerLogModule serverLogModule = module(MConst.ServerLog);
        serverLogModule.log_dailyBall(dailyRecord.getDailyBallLevel());
        com.stars.util.LogUtil.info("斗魂珠升级|roleid:{}|level:{}->{}",id(),preLevel,dailyRecord.getDailyBallLevel());
        //发送斗魂珠数据
        sendDailyBall2Client();
        context().update(dailyRecord);
        //更新属性和战力
        updateDailyBallAttrAndFightScore(true);
        signCalRedPoint();
    }

    public void updateDailyBallAttrAndFightScore(Boolean isSend2Client){
        //添加属性加成的战力
        int fightScore = 0;
        DailyBallStageVo dailyBallStageVo = DailyManager.getDailyBallStageVoByLevel(dailyRecord.getDailyBallLevel());
        fightScore += FormularUtils.calFightScore(dailyBallStageVo.getAttribute());

        //更新属性和战力
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        roleModule.updatePartAttr(RoleManager.FIGHTSCORE_DAILY,dailyBallStageVo.getAttribute());
        roleModule.updatePartFightScore(RoleManager.FIGHTSCORE_DAILY, fightScore);
        if(isSend2Client){
            roleModule.sendRoleAttr();
            roleModule.sendUpdateFightScore();
        }
    }

    //检测并记录是否有超级奖励或多倍奖励
    private void checkAndRecordDailyAward(short dailyId){
        DailyVo dailyVo = DailyManager.getDailyVo(dailyId);
        if(dailyVo == null)
            return;
        DailyAwardVo dailyAwardVo = null;
        if(dailyId == dailyRecord.getSuperAwardDailyId() && dailyVo.getSuperawardind() == (byte)1){ //如果是超级奖励
            dailyAwardVo = DailyManager.getDailyAwardVoById(dailyRecord.getSuperAwardId());
        }else if(dailyVo.getMutipleind() == (byte)1 && dailyRecord.getDailyRecord(dailyVo.getDailyid()) < 1){ //如果是首次奖励
            dailyAwardVo = getTodayDailyAwardVo(dailyId,DailyManager.MUTIPLE_AWARD);
        }
        if(dailyAwardVo == null)
            return;
        if(dailyRecord.getGotAwardSet().contains(dailyAwardVo.getDailyAwardId())) //已经领取过了
            return;
        dailyRecord.canGetAward(dailyAwardVo.getDailyAwardId());
        com.stars.util.LogUtil.info("触发可领取每日超级或者多倍奖励|roleid：{}|awardId：{}",id(),dailyAwardVo.getDailyAwardId());
    }

    /**
     * 检测并发送奖励
     * @param promptType 弹窗类型
     * @param isSendTips 是否发送弹窗
     */
    //检测是否有可发送
    public void checkAndSendDailyAward(byte promptType, boolean isSendTips){
        if(!DailyManager.isSendAwardSwitch()){ //开关检查
            LogUtil.info("日常超级奖励或多倍奖励开关状态：{}", DailyManager.isSendAwardSwitch());
            return;
        }
        if(StringUtil.isEmpty(dailyRecord.getCanGetAwardSet())) //没有可领取的每日奖励
            return;
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        for(int awardId:dailyRecord.getCanGetAwardSet()){
            DailyAwardVo dailyAwardVo = DailyManager.getDailyAwardVoById(awardId);
            if(dailyAwardVo == null)
                continue;
            if(dailyRecord.getGotAwardSet().contains(dailyAwardVo.getDailyAwardId())) //已经领取过了
                continue;
            toolModule.addAndSend(dailyAwardVo.getAwardMap(),EventType.DAILY_MUTIPLE_OR_SUPER_AWARD.getCode());
            dailyRecord.gotAward(dailyAwardVo.getDailyAwardId());
            context().update(dailyRecord);
            ServerLogModule serverLogModule = module(MConst.ServerLog);
            serverLogModule.log_dailyAward(dailyAwardVo.getDailyid(),dailyAwardVo.getAwardType(),dailyAwardVo.getAwardMap());
            if(isSendTips) {
                //发送多倍或者超级奖励信息
                ClientDailyData clientDailyData = new ClientDailyData();
                clientDailyData.setRespType(ClientDailyData.RESP_MUTIPLE_OR_SUPER_AWARD);
                clientDailyData.setShowAwardType(promptType);
                clientDailyData.setDailyId(dailyAwardVo.getDailyid());
                clientDailyData.setAwardType(dailyAwardVo.getAwardType());
                clientDailyData.setAwardMap(dailyAwardVo.getAwardMap());
                send(clientDailyData);
            }
        }
    }

    public void sendDailyBall2Client(){
        //发送斗魂珠的数据
        ClientDailyData client2 = new ClientDailyData();
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        int currentDailyBallScore = roleModule.getResource((byte) ToolManager.DAILY_BALL_SCORE);
        client2.setRespType(ClientDailyData.RESP_DAILY_BALL);
        DailyBallStageVo dailyBallStageVo = DailyManager.getDailyBallStageVoByLevel(dailyRecord.getDailyBallLevel());
        client2.setDailyBallStageVo(dailyBallStageVo);
        client2.setRoleOwnDailyBallScore(currentDailyBallScore);
        send(client2);
    }

    private int getRoleTotalFightScore(){
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        int totalFightScore = roleModule.getFightScore(); //玩家当前战力
        //宝石、装备、伙伴系统拿到的是历史最高战力，需要特殊处理
        int needReduceFightScore = 0;
        int needIncreFightScore = 0;
        //计算需要减掉的战力
        Map<String, Integer> fightScoreMap = roleModule.getRoleRow().getFightScoreMap();
        for(String sysName:fightScoreMap.keySet()){
            if(!(sysName.equals(RoleManager.FIGHTSCORE_GEM) || sysName.equals(RoleManager.FIGHTSCORE_EQUIPMENT) || sysName.equals(MConst.Buddy)))
                continue;
            needReduceFightScore += fightScoreMap.get(sysName);
        }
        //计算需要增加的战力
        int maxGemHistoryFightScore = context().recordMap().getInt(GemModule.GEM_MAX_HISTORY_FIGHTSCORE,0);
        int maxEquipmentHistoryFightScore = context().recordMap().getInt(NewEquipmentModule.NEWEQUIPMENT_MAX_HISTORY_FIGHTSCORE,0);
        int maxBuddyHistoryFightScore = context().recordMap().getInt(BuddyModule.BUDDY_MAX_HISTORY_FIGHTSCORE,0);
        needIncreFightScore = maxGemHistoryFightScore + maxEquipmentHistoryFightScore + maxBuddyHistoryFightScore;
        //获得玩家历史最高战力
        totalFightScore = totalFightScore - needReduceFightScore + needIncreFightScore;

        return totalFightScore;
    }


    private byte checkAndGetSuperAwardType(short dailyid){
        //计算应该获得
        String sysName = DailyManager.getDailyVo(dailyid).getSysName();
        if(StringUtil.isEmpty(sysName) || sysName.equals("0"))
            return 0;

        int recommFight = 0;
        int roleFight = getFightScore(sysName);
        int openDays = DataManager.getServerDays(); //系统开服时间
        //检查是否有超量超级奖励
        int checkBestSuperAwardDays = openDays - DailyManager.getPreOpenDayForBestSuperAward();
        if(checkBestSuperAwardDays >= 1) {
            recommFight = DailyManager.getRecommFightScore(sysName, checkBestSuperAwardDays);
            if (roleFight <= recommFight)
                return DailyManager.SUPER_AWARD_BEST;
        }
        //检查是否有高级超级奖励
        checkBestSuperAwardDays = openDays - DailyManager.getPreOpenDayForBetterSuperAward();
        if(checkBestSuperAwardDays >= 1) {
            recommFight = DailyManager.getRecommFightScore(sysName, checkBestSuperAwardDays);
            if (roleFight <= recommFight)
                return DailyManager.SUPER_AWARD_BETTER;
        }
        //检查是否有普通超级奖励
        recommFight = DailyManager.getRecommFightScore(sysName,DataManager.getServerDays());
        if(roleFight <= recommFight)
            return DailyManager.SUPER_AWARD_COMMON;

        //没有超级奖励默认返回0
        return 0;
    }
}