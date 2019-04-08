package com.stars.modules.escort;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.escort.packet.ClientEscort;
import com.stars.modules.escort.prodata.CargoCarVo;
import com.stars.modules.escort.userdata.RoleEscort;
import com.stars.modules.escort.userdata.RoleEscortEnemy;
import com.stars.modules.escort.userdata.vo.CargoRecord;
import com.stars.modules.operateCheck.OperateCheckModule;
import com.stars.modules.operateCheck.OperateConst;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.prodata.Job;
import com.stars.modules.role.prodata.Resource;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterPK;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.skill.prodata.SkillVo;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.vip.VipModule;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.ServiceHelper;
import com.stars.util.I18n;
import com.stars.util.ServerLogConst;
import com.stars.util.StringUtil;
import io.netty.buffer.UnpooledByteBufAllocator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wuyuxing on 2016/12/2.
 */
public class EscortModule extends AbstractModule {

    private RoleEscort roleEscort;
    private Map<Long, RoleEscortEnemy> enemyRecordMap;   //仇人列表

    public EscortModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("Escort", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from `roleescort` where `roleid`=" + id();
        roleEscort = DBUtil.queryBean(DBUtil.DB_USER,RoleEscort.class,sql);
        if(roleEscort == null){
            roleEscort = new RoleEscort(id());
            context().insert(roleEscort);
        }

        sql = "select * from `roleescortenemy` where `roleid`=" + id();
        enemyRecordMap = DBUtil.queryMap(DBUtil.DB_USER,"enemyid",RoleEscortEnemy.class,sql);
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        roleEscort = new RoleEscort(id());
        context().insert(roleEscort);

        enemyRecordMap = new HashMap<>();
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        checkEnemyRecord();//检测&更新 仇人列表
    }

    @Override
    public void onOffline() throws Throwable {
        ServiceHelper.escortService().handleOffline(id());
    }

    /**
     * 检测&更新 仇人列表
     */
    private void checkEnemyRecord(){
        long now = System.currentTimeMillis();
        List<RoleEscortEnemy> timeOutList = new ArrayList<>();
        for(RoleEscortEnemy enemyVo:enemyRecordMap.values()){
            if(enemyVo.isTimeOut(now)){
                timeOutList.add(enemyVo);
            }
        }

        //删除过时仇人记录
        for(RoleEscortEnemy enemyVo:timeOutList){
            enemyRecordMap.remove(enemyVo.getEnemyId());
            context().delete(enemyVo);
        }
    }

    @Override
    public void onSyncData() throws Throwable {

    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        roleEscort.dailyReset();
        context().update(roleEscort);
    }

    /**
     * 新增仇人关系记录
     */
    public void addEnemyRecord(List<Long> enemyList){
        if(StringUtil.isEmpty(enemyList)) return;
        for(long enemyId:enemyList){
            addEnemyRecord(enemyId);
        }
    }

    /**
     * 新增仇人记录/延长时间
     */
    public void addEnemyRecord(long enemyId){
        if(enemyId == 0) return;
        if(enemyRecordMap == null) enemyRecordMap = new HashMap<>();
        RoleEscortEnemy record = enemyRecordMap.get(enemyId);
        if(record == null){
            long lastTime = System.currentTimeMillis() + EscortManager.getCargocarEnemyTime();
            record = new RoleEscortEnemy(id(),enemyId,lastTime);
            enemyRecordMap.put(enemyId,record);
            context().insert(record);
        }else{
            record.addLastTime(EscortManager.getCargocarEnemyTime());
            context().update(record);
        }
    }

    /**
     * 运镖入口界面
     */
    public void viewMainEntryUI(){
        ClientEscort client = new ClientEscort(ClientEscort.RESP_VIEW_MAIN_ENTRY);
        client.setRemainTimes(roleEscort.getRemainTime());
        client.setRemainRobTimes(roleEscort.getRemainRobTime());
        send(client);
    }

    /**
     * 镖车选择界面
     */
    public void viewCargoSelectUI(){
        checkAndResetCargoSelectList();//检测镖车刷新时间并执行刷新

        ClientEscort client = new ClientEscort(ClientEscort.RESP_VIEW_CARGO_SELECT);
        client.setCargoRecordMap(roleEscort.getCargoRecordMap());
        client.setRemainTimes(roleEscort.getRemainTime());
        client.setDailyFreshTimes(roleEscort.getDailyFreshTimes());
        client.setRecordResetCoolDown(getRecordResetCoolDown());
        send(client);
    }

    /**
     * 获得距离下个整点的时间
     */
    private int getRecordResetCoolDown(){
        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int leftTime = (60 - minute - 1) * 60 + (60 - second);
        return leftTime;
    }

    /**
     * 检测镖车刷新时间并执行刷新
     */
    private void checkAndResetCargoSelectList(){
        long lastResetTime = roleEscort.getCargoRecordResetTime();
        if(lastResetTime == 0){//首次初始化,直接刷新
            resetCargoSelectList(false);//重置镖车选择列表
            return;
        }

        //判断是否需要刷新
        long now = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(lastResetTime);
        int resetYear = calendar.get(Calendar.YEAR);
        int resetDay = calendar.get(Calendar.DAY_OF_YEAR);
        int resetHour = calendar.get(Calendar.HOUR_OF_DAY);

        calendar.setTimeInMillis(now);
        int curYear = calendar.get(Calendar.YEAR);
        int curDay = calendar.get(Calendar.DAY_OF_YEAR);
        int curHour = calendar.get(Calendar.HOUR_OF_DAY);

        //整点时间不一致则重置
        if(curYear != resetYear || curDay != resetDay || curHour != resetHour){
            resetCargoSelectList(false);//重置镖车选择列表
        }
    }

    private int getRandomCarId(List<CargoCarVo> list, int randomWeight){
        int curValue = 0;
        for(CargoCarVo carVo:list){
            curValue += carVo.getOdds();
            if(curValue >= randomWeight){
                return carVo.getCarId();
            }
        }
        return 0;
    }

    /**
     * 重置镖车选择列表
     */
    private void resetCargoSelectList(boolean useGold){
        List<CargoCarVo> list = getRoleCargoCarVoList();
        if(StringUtil.isEmpty(list)) return;

        int totalWeight = 0;//记录总权重
        for(CargoCarVo carVo:list){
            totalWeight += carVo.getOdds();
        }

        if(useGold){
            roleEscort.addDailyFreshTimes();
        }

        roleEscort.getCargoRecordMap().clear();//清空原记录
        Random random = new Random();
        int carId;
        for(byte index = 1;index <= 4;index++){
            carId = getRandomCarId(list, random.nextInt(totalWeight));
            if(carId != 0) {
                roleEscort.getCargoRecordMap().put(index, new CargoRecord(carId));
            }
        }

        //记录重置时间
        long now = System.currentTimeMillis();
        roleEscort.setCargoRecordResetTime(now);
        context().update(roleEscort);
    }

    private List<CargoCarVo> getRoleCargoCarVoList(){
        List<CargoCarVo> list = new ArrayList<>();
        RoleModule roleModule = module(MConst.Role);
        int roleLevel = roleModule.getLevel();

        VipModule vipModule = module(MConst.Vip);
        int vipLevel = vipModule.getVipLevel();

        Map<Integer, CargoCarVo> map = EscortManager.getCargoCarVoMap();
        if(StringUtil.isEmpty(map)) return list;
        for(CargoCarVo carVo:map.values()){
            if(carVo != null && carVo.isMatch(roleLevel,vipLevel)){
                list.add(carVo);
            }
        }
        return list;
    }

    public RoleEscort getRoleEscort() {
        return roleEscort;
    }

    /**
     * 条件检测
     */
    private boolean checkCondition(byte type){
        RoleModule roleModule = module(MConst.Role);
        int roleLevel = roleModule.getLevel();

        if(type == EscortConstant.ESCORT_TYPE_SINGLE){    //个人验证
            if(roleLevel < EscortManager.getOpenLevel() ||
                    roleLevel >= EscortManager.getTeamModeLevel()) return false;
        }else{  //组队验证
            if(roleLevel < EscortManager.getTeamModeLevel()) return false;

            //不是队长, 队长才能操作
            if(!ServiceHelper.baseTeamService().isCaptain(id())) return false;
        }
        return true;
    }

    /**
     * 请求金币刷新镖车选择列表
     */
    public void reqResetCargoSelectList(){
        if(roleEscort == null) return;
        if(!OperateCheckModule.checkOperate(id(), OperateConst.ESCORT_REFRESH, OperateConst.FIVE_HUNDRED_MS)) return;
        ToolModule toolModule = module(MConst.Tool);
        if(roleEscort.getDailyFreshTimes() >= EscortManager.getCargocarDailyRefreshCount()){
            warn("commonbtntext_norefreshcount");
            return;
        }

        //扣除刷新所需道具
        if(!toolModule.deleteAndSend(EscortManager.getCargocarRefreshItemid(),
                EscortManager.getCargocarRefreshCount(), EventType.ESCORT.getCode())){
            warn(I18n.get("escort.refreshToolNotEnough"));
            return;
        }

        resetCargoSelectList(true);//重置镖车选择列表

        viewCargoSelectUI();//刷新界面
    }

    /**
     * 开始运镖请求
     */
    public void beginEscort(byte escortType,byte index,int carId){
        if(roleEscort == null || roleEscort.getCargoRecordMap() == null) return;
        checkAndResetCargoSelectList();//检测镖车刷新时间并执行刷新

        int remainTime = roleEscort.getRemainTime();
        //运镖次数
        if(remainTime <= 0) {
            warn("team_dailyTimeNotEnough");
            return;
        }

        CargoRecord record = roleEscort.getCargoRecordMap().get(index);
        if(record == null) return;
        if(record.getHasUsed() == 1){
            warn(I18n.get("escort.hasUsed"));
            return;
        }

        //信息不对应
        if(record.getCargoId() != carId){
            warn(I18n.get("escort.operateAgain"));
            viewCargoSelectUI();
            return;
        }

        //条件检测
        if(!checkCondition(escortType)) return;

        long familyId = ServiceHelper.familyRoleService().getFamilyId(id());

        //开始押镖
        if(escortType == EscortConstant.ESCORT_TYPE_SINGLE){
            ServiceHelper.escortService().singleBeginEscort(id(), moduleMap(),index,carId,familyId);
        }else{
            ServiceHelper.escortService().teamBeginEscort(id(),index,carId,familyId);
        }
    }

    /**
     * 开始劫镖请求
     */
    public void robCargo(byte escortType,byte index,byte useMask){
        //条件检测
        if(!checkCondition(escortType)) return;
        if(useMask == 1){//使用面具
            ToolModule toolModule = module(MConst.Tool);
            int maskId = EscortManager.getCargocarMaskItemid();
            int maskCount = EscortManager.getCargocarMask();
            long hasCount = toolModule.getCountByItemId(maskId);
            if(maskCount > hasCount){//面具数量不足
                warn(I18n.get("escort.maskNotEnough"));
                return;
            }
        }
        EscortModule escortModule = module(MConst.Escort);
        if(escortModule == null || escortModule.getRoleEscort() == null) return;
        if(escortModule.getRoleEscort().getRemainRobTime() <= 0) {
            warn("team_dailyTimeNotEnough");
            return;
        }

        ServiceHelper.escortService().robCargo(id(), index,escortType,moduleMap(),useMask == 1,roleEscort.getRemainRobTime());
    }

    /**
     * 请求劫机器人镖车(pve关卡)
     */
    public void robRobotCargo(byte escortType,int sectionId){
        if(true) {
            return;
        }
        //条件检测
        if(!checkCondition(escortType)) return;
        EscortModule escortModule = module(MConst.Escort);
        if(escortModule == null || escortModule.getRoleEscort() == null) return;
        if(escortModule.getRoleEscort().getRemainRobTime() <= 0) {
            warn("team_dailyTimeNotEnough");
            return;
        }

        ServiceHelper.escortService().robRobotCargo(id(), sectionId, escortType, moduleMap());

        ServerLogModule serverLogModule = module(MConst.ServerLog);
        serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_START, ThemeType.ACTIVITY_24.getThemeId(),0);
    }

    /**
     * 进入押镖场景
     */
    public void enterEscortScene(byte[] data){
        com.stars.network.server.buffer.NewByteBuffer buffer = new NewByteBuffer(UnpooledByteBufAllocator.DEFAULT.buffer());
        buffer.getBuff().writeBytes(data);
        ClientEnterPK enterPK = new ClientEnterPK();
        enterPK.readFromBuffer(buffer);
        buffer.getBuff().release();
        addAllJobSkill(enterPK);

        send(enterPK);

        ServerLogModule serverLogModule = module(MConst.ServerLog);
        serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_START,ThemeType.ACTIVITY_24.getThemeId(),0);

        RoleModule roleModule = module(MConst.Role);
        ServiceHelper.arroundPlayerService().removeArroundPlayer(roleModule.getJoinSceneStr(), id());
    }

    private void addAllJobSkill(ClientEnterPK client){
        Map<Integer, Job> jobMap = RoleManager.jobMap;
        Job job;
        Resource resource;
        SkillVo skillVo;
        List<Integer> skillVoList;
        Map<Integer, Integer> skillMap = new ConcurrentHashMap<>();
        for (Map.Entry<Integer, Job> kvp : jobMap.entrySet()) {
            job = kvp.getValue();
            resource = RoleManager.getResourceById(job.getModelres());
            skillVoList = resource.getSkillList();
            for (int i = 0, len = skillVoList.size(); i < len; i++) {
                skillVo = SkillManager.getSkillVo(skillVoList.get(i));
                skillMap.put(skillVo.getSkillid(), 1);
            }
            for (Integer pSkillId : job.getPSkillList()) {
                skillMap.put(pSkillId, 1);
            }
        }
        client.addSkillData(skillMap);
    }

    /**
     * 进入押镖场景
     */
    public void realEnterEscortSafeScene(byte[] data){
        SceneModule sceneModule = module(MConst.Scene);
        if(roleEscort.getRemainRobTime() > 0) {//劫镖次数足够,进入劫镖队列场景
            sceneModule.setRepeat(false);
            sceneModule.enterScene(SceneManager.SCENETYPE_ESCORT_SAFE, EscortManager.getCargocarSafeid(), data);
        }else{//次数不足
            ServiceHelper.baseTeamService().leaveTeam(id());
            sceneModule.backToCity(false);
        }
    }

    /**
     * 请求进入镖车队列场景
     */
    public void enterCargoListScene(byte escortType){
        if(roleEscort == null) return;
        int remainRobTime = roleEscort.getRemainRobTime();
        //劫镖次数
        if(remainRobTime <= 0) {
            warn("team_dailyTimeNotEnough");
            return;
        }

        //条件检测
        if(!checkCondition(escortType)) return;

        checkEnemyRecord();//检测&更新 仇人列表

        RoleModule roleModule = module(MConst.Role);

        if(escortType == EscortConstant.ESCORT_TYPE_SINGLE){
            ServiceHelper.escortService().singleEnterCargoListScene(id(),getEnemylist(),roleModule.getRoleRow().getFightScore());
        }else{
            ServiceHelper.escortService().teamEnterCargoListScene(id(),getEnemylist());
        }
    }

    /**
     * 获得仇人列表
     */
    private List<Long> getEnemylist(){
        List<Long> list = new ArrayList<>();
        if(StringUtil.isNotEmpty(enemyRecordMap)){
            list.addAll(enemyRecordMap.keySet());
        }
        return list;
    }

    /**
     * 处理运镖相关奖励增加 & 活动参与次数增加 & 设置镖车冷却时间
     */
    public void handleAddEscortAwardEvent(byte subType,Map<Integer,Integer> awardMap,int carId,byte index){
        ServerLogModule serverLogModule = module(MConst.ServerLog);
        if(subType == EscortConstant.SUB_TYPE_ESCORT_SUCCESS){
            roleEscort.addEscortTimes();

            DailyFuntionEvent event = new DailyFuntionEvent(DailyManager.DAILYID_ESCORT, 1);
            eventDispatcher().fire(event);

            CargoRecord record = roleEscort.getCargoRecordMap().get(index);
            if(record!=null && record.getCargoId() == carId && record.getHasUsed() == 0){
                record.setHasUsed((byte)1);//标识为已使用
            }
            context().update(roleEscort);
            serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_WIN,ThemeType.ACTIVITY_24.getThemeId(),0);
        }else if(subType == EscortConstant.SUB_TYPE_ROB_SUCCESS){
            roleEscort.addRobTimes();
            context().update(roleEscort);

            serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_WIN,ThemeType.ACTIVITY_24.getThemeId(),0);
        }else if(subType == EscortConstant.SUB_TYPE_GUIDE_SUCCESS){
            serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_WIN,ThemeType.ACTIVITY_24.getThemeId(),0);
        }

        ToolModule toolModule = module(MConst.Tool);
        toolModule.addAndSend(awardMap, EventType.ESCORT.getCode());
    }

    //扣除使用面具劫镖的面具数量
    public void consumeMask(){
        ToolModule toolModule = module(MConst.Tool);
        int maskId = EscortManager.getCargocarMaskItemid();
        int maskCount = EscortManager.getCargocarMask();
        toolModule.deleteAndSend(maskId,maskCount,EventType.ESCORT.getCode());
    }

    public void handleCheckRobTimesEvent(){
        ClientEscort client = new ClientEscort(ClientEscort.RESP_BACK_CITY_AND_OPEN_TEAM_UI);
        client.setRemainRobTimes(roleEscort.getRemainRobTime());
        send(client);
    }

    public void handleEnterCargoListSceneEvent(){
        RoleModule roleModule = module(MConst.Role);
        int roleLevel = roleModule.getLevel();

        if(roleLevel < EscortManager.getOpenLevel()) return;
        if(roleLevel < EscortManager.getTeamModeLevel()){//单人模式
            enterCargoListScene(EscortConstant.ESCORT_TYPE_SINGLE);
            return;
        }

        if(ServiceHelper.baseTeamService().isCaptain(id())){
            enterCargoListScene(EscortConstant.ESCORT_TYPE_TEAM);
        }
    }
}
