package com.stars.modules.ride;

import com.stars.AccountRow;
import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.AccountRowAware;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.ride.event.NewRideEvent;
import com.stars.modules.ride.event.RideAchieveEvent;
import com.stars.modules.ride.event.RideChangeEvent;
import com.stars.modules.ride.event.RideLevelUpEvent;
import com.stars.modules.ride.packet.ClientRide;
import com.stars.modules.ride.packet.ClientRideAwakeLevelVo;
import com.stars.modules.ride.packet.ClientRideLevelVo;
import com.stars.modules.ride.prodata.RideAwakeLvlVo;
import com.stars.modules.ride.prodata.RideInfoVo;
import com.stars.modules.ride.prodata.RideLevelVo;
import com.stars.modules.ride.userdata.RoleRidePo;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.util.*;

import java.util.*;

import static com.stars.modules.ride.RideConst.*;

/**
 * Created by zhaowenshuo on 2016/9/18.
 */
public class RideModule extends AbstractModule implements AccountRowAware {

    private Map<Integer, RoleRidePo> ridePoMap;
    private Map<Integer, RoleRidePo> showRidePoMap = new HashMap<>();
    private Set<Integer> rideLevelUpOneUpList;
    //    private Set<Integer> rideLevelUpTenUpList;
    RoleModule roleModule = module(MConst.Role);
    private Set<Integer> rideList;
    private int activeRideId = NO_RIDE_ID;
    private AccountRow accountRow;

    public RideModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(MConst.Ride, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        ridePoMap = DBUtil.queryMap(DBUtil.DB_USER, "rideid", RoleRidePo.class,
                "select * from `roleride` where `roleid`=" + id());
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        ridePoMap = new HashMap<>();
        rideLevelUpOneUpList = new HashSet<>();
//        rideLevelUpTenUpList = new HashSet<>();
        rideList = new HashSet<>();
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        /* 生成坐骑初始数据 */
        for (RideInfoVo infoVo : RideManager.rideInfoVoMap.values()) {
            if (!ridePoMap.containsKey(infoVo.getRideId())) {
                RoleRidePo ridePo = new RoleRidePo(id(), infoVo.getRideId(), DEFAULT_AWAKE_LEVEL, NOT_OWNED, NOT_ACTIVE, NOT_CLICK);
                ridePoMap.put(ridePo.getRideId(), ridePo);
                context().insert(ridePo);
            }
        }
        /**
         * 过滤坐骑和激活坐骑
         */
        boolean changeActiveRide = false;
        boolean tempActiveRide = false;
        for (RoleRidePo ridePo : ridePoMap.values()) {
            RideInfoVo rideInfoVo = RideManager.rideInfoVoMap.get(ridePo.getRideId());
            if (rideInfoVo == null) {
                context().delete(ridePo);
                continue;
            }
            if (rideInfoVo.checkCondition(roleModule.getLevel(), accountRow.getVipLevel()) || ridePo.getOwned() == 1) {
                showRidePoMap.put(ridePo.getRideId(), ridePo);
            }
            
            if(ridePo.getSendOver()==1){
    			ClientRide packet = new ClientRide(ClientRide.RESP_OVER_TIME);
    			packet.setRideName(rideInfoVo.getName());
    			packet.setRideId(ridePo.getRideId());
    			send(packet);
    			ridePo.setSendOver((byte)0);
            }
            
            tempActiveRide = checkTimeLimitRide(ridePo);
            if(tempActiveRide){
            	changeActiveRide = true;
            }
            if (ridePo.isActive()) {
                activeRideId = ridePo.getRideId();
            }

            if (ridePo.getAwakeLevel() == 0) { // 对旧数据进行处理
                ridePo.setAwakeLevel(DEFAULT_AWAKE_LEVEL);
                context().update(ridePo);
            }

        }
        addList();
        removeList();
        view();
        if(changeActiveRide){//切换最高战力坐骑
        	RoleRidePo topRidePo = null;
        	RideInfoVo rideInfoVo = null;
        	int fightScore = 0;
        	int topFightScore = 0;
        	for (RoleRidePo ridePo : ridePoMap.values()) {
        		rideInfoVo = RideManager.rideInfoVoMap.get(ridePo.getRideId());
        		if(rideInfoVo==null) continue;
        		if(!ridePo.isOwned()) continue;
        		Attribute attr = new Attribute(rideInfoVo.getAttribute());
        		fightScore = FormularUtils.calFightScore(attr);
        		if(fightScore>topFightScore){
        			topFightScore = fightScore;
        			topRidePo = ridePo;
        		}
        	}
        	if(topRidePo!=null){
        		topRidePo.setActive(ACTIVE);
        		getOn(topRidePo.getRideId());
        	}
        }

        /* 生成战力 */
        recalcFightScore();
    }

    @Override
    public void onSyncData() throws Throwable {
        ClientRide packet = new ClientRide(ClientRide.RESP_ACTIVE);
        packet.setActiveRideId(activeRideId);
        send(packet);

        if(rideList == null){
            rideList = new HashSet<>();
        }
        for(RoleRidePo po:ridePoMap.values()){
            if(po.isOwned()){
                rideList.add(po.getRideId());
            }
        }
        fireAchieveEvent();//登陆检测坐骑成就事件
        rideList.clear();
    }
    
    public void view() {
        RoleModule roleModule = module(MConst.Role);
        // 发产品数据
        ClientRideLevelVo levelVoPacket = new ClientRideLevelVo();
        levelVoPacket.add(roleModule.getRideLevelId());
        RideLevelVo maxLvVo = getCurrStageMaxLv(roleModule.getRideLevelId());
        if (maxLvVo != null) {
            levelVoPacket.add(maxLvVo.getId());
        }
        send(levelVoPacket);
        // 觉醒产品数据
        ClientRideAwakeLevelVo awakeLevelVoPacket = new ClientRideAwakeLevelVo();
        for (RoleRidePo po : showRidePoMap.values()) {
            awakeLevelVoPacket.add(po.getRideId(), po.getAwakeLevel());
        }
        send(awakeLevelVoPacket);
        // 发用户数据
        ClientRide ridePacket = new ClientRide(ClientRide.RESP_VIEW);
        ridePacket.setRidePoMap(showRidePoMap);
        send(ridePacket);
    }
    
    @Override
    public void onTimingExecute() {
    	boolean changeActiveRide = false;
    	boolean tempActiveRide = false;
    	RoleRidePo topRidePo = null;
    	RideInfoVo rideInfoVo = null;
    	for (RoleRidePo ridePo : ridePoMap.values()) {
    		rideInfoVo = RideManager.rideInfoVoMap.get(ridePo.getRideId());
    		if(rideInfoVo==null) continue;
    		tempActiveRide = checkTimeLimitRide(ridePo);
    		if(tempActiveRide){
    			changeActiveRide = true;
    		}
    	}
    	if(changeActiveRide){//切换最高战力坐骑
        	int fightScore = 0;
        	int topFightScore = 0;
        	for (RoleRidePo ridePo : ridePoMap.values()) {
        		rideInfoVo = RideManager.rideInfoVoMap.get(ridePo.getRideId());
        		if(rideInfoVo==null) continue;
        		if(!ridePo.isOwned()) continue;
        		Attribute attr = new Attribute(rideInfoVo.getAttribute());
        		fightScore = FormularUtils.calFightScore(attr);
        		if(fightScore>topFightScore){
        			topFightScore = fightScore;
        			topRidePo = ridePo;
        		}
        	}
        	if(topRidePo!=null){
        		topRidePo.setActive(ACTIVE);
        		getOn(topRidePo.getRideId());
        	}
        }
    }


    /**
     * 获得坐骑当前阶级的最大等级数据
     *
     * @return
     */
    public RideLevelVo getCurrStageMaxLv(int levelId) {
        RideLevelVo currLvVo = RideManager.getRideLvById(levelId);
        if (currLvVo == null) {
            warn(I18n.get("ride.error"));
            return null;
        }
        Map<Integer, Map<Integer, RideLevelVo>> rideLevelVoMap = RideManager.rideLevelVoMap;
        Map<Integer, RideLevelVo> stageMap = rideLevelVoMap.get(currLvVo.getStagelevel());
        if (StringUtil.isEmpty(stageMap)) return null;
        int size = currLvVo.getStagelevel() == 1 ? stageMap.size() - 1 : stageMap.size();
        return stageMap.get(size);
    }

    private int getOwnRideCount() {
        int count = 0;
        if (ridePoMap != null) {
            for (RoleRidePo po : ridePoMap.values()) {
                if (po == null) continue;
                if (po.isOwned()) count++;
            }
        }
        return count;
    }

    /**
     * 激活坐骑
     *
     * @param rideId
     */
    public void activeRide(int rideId) {
        if (isOwned(rideId)&&!isTimeLimit(rideId)) {
            warn(I18n.get("ride.Owned"));
            return;
        }
        RoleRidePo ridePo = ridePoMap.get(rideId);
        if (ridePo == null) {
            warn(I18n.get("ride.error"));
            return;
        }
        RideInfoVo rideInfoVo = RideManager.getRideInfoVo(rideId);
        if (rideInfoVo == null) {
            warn(I18n.get("ride.error"));
            return;
        }
        ToolModule toolModule = module(MConst.Tool);
        if (rideInfoVo.getReqtype() == RideConst.REQ_TYPE_TOOL_ACTIVE || rideInfoVo.getReqtype() == RideConst.REQ_TYPE_BUY_ACTIVE) {
            if (rideInfoVo.getReqitem().equals("0+0")) {
                getRide(rideId);
            } else {
                if (toolModule.deleteAndSend(rideInfoVo.getReqItemMap(), EventType.ACTIVE_RIDE.getCode())) {
                    getRide(rideId);
                }
            }
        }
    }

    /**
     * 获得坐骑
     */
    public boolean getRide(int rideId) {
    	boolean useResult = false;
        if (isOwned(rideId)&&!isTimeLimit(rideId)) {
            warn(I18n.get("ride.Owned"));
            return useResult;
        }
        RoleRidePo ridePo = ridePoMap.get(rideId);
        if (ridePo == null) {
            warn(I18n.get("ride.error"));
            return useResult;
        }
        RideInfoVo rideInfoVo = RideManager.getRideInfoVo(rideId);
        if (rideInfoVo == null) {
            warn(I18n.get("ride.error"));
            return useResult;
        }
        boolean needOn = true;//是否需要骑乘
        for (RoleRidePo tempPo : ridePoMap.values()) {
            if (tempPo.isOwned()) {
                needOn = false;
                break;
            }
        }
        if(isTimeLimit(rideId)){//限时坐骑处理
        	byte timeLimitType = rideInfoVo.getTimeLimitType();
        	int oldEndTime = ridePo.getEndTime();
        	int newEndTime = oldEndTime;
        	switch (timeLimitType) {
			case TIME_LIMIT_TYPE1://增加持续时间
				int addTime = rideInfoVo.getContinueTime();
				if(oldEndTime==0){					
					newEndTime = DateUtil.getCurrentTimeInt()+addTime;
				}else{
					newEndTime = oldEndTime+addTime;					
				}
//				send(new ClientText(I18n.get("ride.AddTime", rideInfoVo.getName(), addTime)));
				useResult = true;
				break;
			case TIME_LIMIT_TYPE2:
				int endTime = rideInfoVo.getEndTime();
				if(oldEndTime<endTime){
					newEndTime = endTime;
					useResult = true;
				}else{
					useResult = false;
				}
				break;
			}
        	ridePo.setEndTime(newEndTime);
        	context().update(ridePo);
        	if(useResult&&isOwned(rideId)){
        		ClientRide packet = new ClientRide(ClientRide.RESP_GET);
            	packet.setRidePo(ridePo);
            	packet.setGetType((byte)3);
            	send(packet);
        	}
        	LogUtil.info("Time Limit Ride timeChange, rideId:"+rideId+" , oldEndTime:"+oldEndTime+" , newEndTime"+newEndTime);
        	if(!useResult){
        		return useResult;
        	}
        }
        if(!isOwned(rideId)){        	
        	ridePo.setOwned(OWNED);
        	byte getType = 1;
        	if(ridePo.getFirstGet()!=FIRST_GET){        		
        		ridePo.setFirstGet(FIRST_GET);
        	}else{
        		getType = 2;
        	}
        	context().update(ridePo);
        	/**
        	 * 刷新坐骑显示
        	 */
        	refreshShowRide();
        	//增加皮肤属性
//        Attribute attr = new Attribute(rideInfoVo.getAttribute());
        	RoleModule roleModule = (RoleModule) module(MConst.Role);
        	/*roleModule.updatePartAttr(MConst.Ride, attr);
        roleModule.updatePartFightScore(MConst.Ride, FormularUtils.calFightScore(attr));*/
        	recalcFightScore();
        	roleModule.sendRoleAttr();
        	roleModule.sendUpdateFightScore();
        	
        	// 通知客户端
        	ClientRide packet = new ClientRide(ClientRide.RESP_GET);
        	packet.setRidePo(ridePo);
        	packet.setGetType(getType);
        	send(packet);
        	if (rideList == null) {
        		rideList = new HashSet<>();
        	}
        	rideList.add(rideId);
        	signCalRedPoint(MConst.Ride, RedPointConst.RIDE_NEW);
        	// fire event
        	RideLevelUpEvent event = new RideLevelUpEvent(0, DEFAULT_LEVEL);
        	eventDispatcher().fire(event);
        	eventDispatcher().fire(new NewRideEvent(rideId));
        	if(getType==1){        		
        		fireAchieveEvent();//坐骑成就事件
        	}
        	if (needOn) {
        		getOn(rideId);
        	}
        	return true;
        }
        return useResult;
    }

    private void fireAchieveEvent() {
        RoleModule roleModule = module(MConst.Role);
        int roleRideLvId = roleModule.getRideLevelId();
        int ownCount = getOwnRideCount();

        Set<Integer> list = new HashSet<>();
        if (rideList != null) {
            list.addAll(rideList);
        }
        RideAchieveEvent event = new RideAchieveEvent(list, roleRideLvId, ownCount);
        eventDispatcher().fire(event);
    }

    public void getOn(int rideId) {
        RoleRidePo newActiveRidePo = ridePoMap.get(rideId);
        if (newActiveRidePo == null || !newActiveRidePo.isOwned()) {
            warn(I18n.get("ride.NotOwned"));
            return;
        }
        RoleRidePo oldActiveRidePo = ridePoMap.get(activeRideId);
        if (oldActiveRidePo != null) {
            oldActiveRidePo.setActive(NOT_ACTIVE);
            context().update(oldActiveRidePo);
        }
        newActiveRidePo.setActive(ACTIVE);
        activeRideId = newActiveRidePo.getRideId();
        context().update(newActiveRidePo);
        // 通知客户端
        ClientRide packet = new ClientRide(ClientRide.RESP_ACTIVE);
        packet.setActiveRideId(activeRideId);
        send(packet);
        // fire event
        eventDispatcher().fire(new RideChangeEvent(oldActiveRidePo == null ? NO_RIDE_ID : oldActiveRidePo.getRideId(), activeRideId));
    }

    public void getDown() {
        RoleRidePo activeRidePo = ridePoMap.get(activeRideId);
        if (activeRidePo == null) {
            warn(I18n.get("ride.Not"));
            return;
        }
        activeRidePo.setActive(NOT_ACTIVE);
        activeRideId = NO_RIDE_ID;
        context().update(activeRidePo);
        // 须通知客户端
        ClientRide packet = new ClientRide(ClientRide.RESP_ACTIVE);
        packet.setActiveRideId(activeRideId);
        send(packet);
        // fire event
        eventDispatcher().fire(new RideChangeEvent(activeRidePo == null ? NO_RIDE_ID : activeRidePo.getRideId(), NO_RIDE_ID));
    }

    @Deprecated
    public void upgradeOneTimes(int stage, int level) {
//        if (getRideInfoVo(rideId) == null) {
//            warn(I18n.get("ride.Not"));
//            return;
//        }
//        RoleRidePo ridePo = ridePoMap.get(rideId);
//        if (ridePo == null) {
//            warn(I18n.get("ride.error"));
//            return;
//        }
//        if (!ridePo.isOwned()) {
//            warn(I18n.get("ride.NotOwned"));
//            return;
//        }
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        int roleRideLvId = roleModule.getRideLevelId();
        RideLevelVo currLevelVo = RideManager.getRideLvById(roleRideLvId);
        if (currLevelVo == null) {
            warn(I18n.get("ride.error"));
            return;
        }
        if (stage != currLevelVo.getStagelevel() || level != currLevelVo.getLevel()) {
            warn(I18n.get("ride.click"));
            return;
        }
        int nextLevelId = roleRideLvId + 1;
        RideLevelVo nextLevelVo = RideManager.getRideLvById(nextLevelId);
        if (nextLevelVo == null) {
            warn(I18n.get("ride.notNextLevel"));
            return;
        }
        if (roleModule.getLevel() < nextLevelVo.getReqRoleLevel()) {
            warn("ride_tips_reqlevel", Integer.toString(nextLevelVo.getReqRoleLevel()));
            return;
        }
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        if (toolModule.deleteAndSend(nextLevelVo.getReqItemMap(), EventType.UPRIDE.getCode())) {
//            ridePo.setRideLevel(nextLevel);
            roleModule.updateRideLevelId(nextLevelId);
            recalcFightScore();
            ((RoleModule) module(MConst.Role)).sendRoleAttr();
            ((RoleModule) module(MConst.Role)).sendUpdateFightScore();
            // 通知客户端
            ClientRideLevelVo levelVoPacket = new ClientRideLevelVo();
            levelVoPacket.add(nextLevelId);
            RideLevelVo maxLvVo = getCurrStageMaxLv(nextLevelId);
            if (maxLvVo != null) {
                levelVoPacket.add(maxLvVo.getId());
            }
            send(levelVoPacket);
            ClientRide packet = new ClientRide(ClientRide.RESP_UPGRADE_ONE);
            packet.setPrevStage(currLevelVo.getStagelevel());
            packet.setPrevLevel(currLevelVo.getLevel());
            packet.setCurrStage(nextLevelVo.getStagelevel());
            packet.setCurrLevel(nextLevelVo.getLevel());
            send(packet);
            // 发送事件
            eventDispatcher().fire(new RideLevelUpEvent(roleRideLvId, nextLevelId));
            fireAchieveEvent();//坐骑成就事件
        } else {
            warn("ride_tips_reqitem", ToolManager.getFirstItemName(nextLevelVo.getReqItemMap()));
        }
    }

    public void upgradeAwakeLevelOneTimes(int rideId) {
        ForeShowModule foreShowModule = module(MConst.ForeShow);
        if (!foreShowModule.isOpen(ForeShowConst.RIDE_AWAKE)) {
            warn("没有开启坐骑觉醒系统");
            return;
        }
        RoleRidePo po = ridePoMap.get(rideId);
        if (po == null) {
            warn("不存在坐骑用户数据");
            return;
        }
        if (!po.isOwned()) {
            warn("不能升级未激活坐骑");
            return;
        }
        RideInfoVo infoVo = RideManager.getRideInfoVo(rideId);
        if (infoVo == null) {
            warn("不存在坐骑产品数据");
            return;
        }
        if (infoVo.getTimeLimitType() != 0) {
            warn("不能升级限时坐骑");
            return;
        }
        RideAwakeLvlVo currVo = RideManager.getRideAwakeLvlVo(po.getRideId(), po.getAwakeLevel());
        RideAwakeLvlVo nextVo = RideManager.getRideAwakeLvlVo(po.getRideId(), po.getAwakeLevel()+1);
        if (currVo == null) {
            warn("不存在坐骑当前等级产品数据");
            return;
        }
        if (nextVo == null) {
            warn("不存在坐骑下一等级产品数据");
            return;
        }
        ToolModule toolModule = module(MConst.Tool);
        if (!toolModule.deleteAndSend(nextVo.getToolMap(), EventType.RIDE_AWAKE_LEVELUP.getCode())) {
            warn("资源不足够");
            return;
        }
        po.setAwakeLevel(nextVo.getAwakeLevel());
        context().update(po);
        recalcFightScore();
        ((RoleModule) module(MConst.Role)).sendRoleAttr();
        ((RoleModule) module(MConst.Role)).sendUpdateFightScore();
        // 下发产品数据
        ClientRideAwakeLevelVo voPacket = new ClientRideAwakeLevelVo();
        voPacket.add(po.getRideId(), po.getAwakeLevel());
        send(voPacket);
        // 下发用户数据
        ClientRide poPacket = new ClientRide(ClientRide.RESP_UPGRADE_AWAKE_LEVEL_ONE);
        poPacket.setRideId(rideId);
        poPacket.setAwakeLevel(po.getAwakeLevel());
        send(poPacket);
    }

    /**
     * 一键升级
     */
    public void oneKeyUpgrade() {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        int roleRideLvId = roleModule.getRideLevelId();
        RideLevelVo currLevelVo = RideManager.getRideLvById(roleRideLvId);
        if (currLevelVo == null) {
            warn(I18n.get("ride.error"));
            return;
        }
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        int nextLevelId = roleRideLvId;
        Map<Integer, Integer> accumulatedReqItemMap = new HashMap<>();
        int levelSize = RideManager.rideLevelIdMap.size() - 1;//最高等级
        for (int i = 0; i < levelSize; i++) {
            int tmpLevelId = nextLevelId + 1;
            RideLevelVo nextLevelVo = RideManager.getRideLvById(tmpLevelId);
            if (nextLevelVo == null) {
                if (i == 0) {
                    warn(I18n.get("ride.notNextLevel"));
                }
                break;
            }
            if (roleModule.getLevel() < nextLevelVo.getReqRoleLevel()) {
                if (i == 0) {
                    warn("ride_tips_reqlevel", Integer.toString(nextLevelVo.getReqRoleLevel()));
                }
                break;
            }
            Map<Integer, Integer> tmpMap = new HashMap<>(accumulatedReqItemMap);
            MapUtil.add(tmpMap, nextLevelVo.getReqItemMap());
            if (!toolModule.contains(tmpMap)) {
                if (i == 0) {
                    warn("ride_tips_reqitem", ToolManager.getFirstItemName(nextLevelVo.getReqItemMap()));
                }
                break;
            }
            nextLevelId = tmpLevelId;
            accumulatedReqItemMap = tmpMap;
        }
        if (nextLevelId > roleRideLvId && toolModule.deleteAndSend(accumulatedReqItemMap, EventType.UPRIDE.getCode())) {
            roleModule.updateRideLevelId(nextLevelId);
            recalcFightScore();
            ((RoleModule) module(MConst.Role)).sendRoleAttr();
            ((RoleModule) module(MConst.Role)).sendUpdateFightScore();
            // 同步数据给客户端
            ClientRideLevelVo levelVoPacket = new ClientRideLevelVo();
            levelVoPacket.add(nextLevelId);
            RideLevelVo maxLvVo = getCurrStageMaxLv(nextLevelId);
            if (maxLvVo != null) {
                levelVoPacket.add(maxLvVo.getId());
            }
            send(levelVoPacket);
            // 发送事件
            eventDispatcher().fire(new RideLevelUpEvent(roleRideLvId, nextLevelId));
            fireAchieveEvent();//坐骑成就事件
        }
    }

    /**
     * 骑术升级后自动激活坐骑
     *
     * @param levelId
     */
    public void autoActiveByLvUp(int levelId) {
        for (RideInfoVo rideInfoVo : RideManager.rideInfoVoMap.values()) {
            if (rideInfoVo.getReqtype() != RideConst.REQ_TYPE_AUTO_ACTIVE) continue;
            RoleRidePo roleRidePo = ridePoMap.get(rideInfoVo.getRideId());
            if (roleRidePo != null && !roleRidePo.isOwned() && levelId >= rideInfoVo.getReqridelevel() && rideInfoVo.getReqitem().equals("0+0")) {
                activeRide(rideInfoVo.getRideId());
            }
        }
    }

    /**
     * 获得骑术阶级
     *
     * @return
     */
    public int getRideStage() {
        ForeShowModule foreShowModule = (ForeShowModule) module(MConst.ForeShow);
        //若坐骑系统未开启，则为0阶
        if (!foreShowModule.isOpen(ForeShowConst.RIDE)) {
            return 0;
        }

        RoleModule roleModule = (RoleModule) module(MConst.Role);
        int roleRideLvId = roleModule.getRideLevelId();
        RideLevelVo currLevelVo = RideManager.getRideLvById(roleRideLvId);
        return currLevelVo == null ? 0 : currLevelVo.getStagelevel();
    }

    public boolean isOwned(int rideId) {
        RoleRidePo ridePo = ridePoMap.get(rideId);
        return ridePo != null && ridePo.isOwned();
    }
    
    /**
     * 是否限时坐骑
     * @return
     */
    public boolean isTimeLimit(int rideId){
    	RideInfoVo rideInfoVo = RideManager.rideInfoVoMap.get(rideId);
    	if(rideInfoVo!=null&&rideInfoVo.getTimeLimitType()>0){
    		return true;
    	}
    	return false;
    }

    public int getActiveRideId() {
        return activeRideId;
    }

    private void recalcFightScore() {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        Attribute attr = new Attribute();
        RideLevelVo levelVo = RideManager.getRideLvById(roleModule.getRideLevelId());
        if (levelVo != null) {
            attr.setHp(attr.getHp() + levelVo.getHp());
            attr.setAttack(attr.getAttack() + levelVo.getAttack());
            attr.setDefense(attr.getDefense() + levelVo.getDefense());
            attr.setHit(attr.getHit() + levelVo.getHit());
            attr.setAvoid(attr.getAvoid() + levelVo.getAvoid());
            attr.setCrit(attr.getCrit() + levelVo.getCrit());
            attr.setAnticrit(attr.getAnticrit() + levelVo.getAnticrit());
        }

        for (RoleRidePo ridePo : ridePoMap.values()) {
            if (ridePo == null || !ridePo.isOwned()) continue;
            RideInfoVo infoVo = RideManager.getRideInfoVo(ridePo.getRideId());
            RideAwakeLvlVo awakeLvlVo = RideManager.getRideAwakeLvlVo(ridePo.getRideId(), ridePo.getAwakeLevel());
            if (infoVo == null || awakeLvlVo == null) continue;
            attr.addAttribute(new Attribute(infoVo.getAttribute()));
            attr.addAttribute(new Attribute(awakeLvlVo.getAwakeAttr()));
        }
        roleModule.updatePartAttr(MConst.Ride, attr);
        roleModule.updatePartFightScore(MConst.Ride, FormularUtils.calFightScore(attr));
    }

    public void removeList() {
        RoleModule roleModule = module(MConst.Role);
        int nextLevelId = roleModule.getRideLevelId() + 1;
        if (!isOneConditions(nextLevelId)) {
            if (rideLevelUpOneUpList == null) {
                rideLevelUpOneUpList = new HashSet<>();
            }
            rideLevelUpOneUpList.remove(1);
            signCalRedPoint(MConst.Ride, RedPointConst.RIDE_LVUP1);

        }

        // 标记觉醒等级红点
        signCalRedPoint(MConst.Ride, RedPointConst.RIDE_AWAKE_LEVELUP);
    }

    public void addList() {
        RoleModule roleModule = module(MConst.Role);
        int nextLevelId = roleModule.getRideLevelId() + 1;
        if (isOneConditions(nextLevelId)) {
            if (rideLevelUpOneUpList == null) {
                rideLevelUpOneUpList = new HashSet<>();
            }
            rideLevelUpOneUpList.add(1);
            signCalRedPoint(MConst.Ride, RedPointConst.RIDE_LVUP1);
        }

        // 标记觉醒等级红点
        signCalRedPoint(MConst.Ride, RedPointConst.RIDE_AWAKE_LEVELUP);
    }

    /**
     * 刷新坐骑展示
     */
    public void refreshShowRide() {
        RoleModule roleModule = module(MConst.Role);
        showRidePoMap.clear();
        for (RoleRidePo ridePo : ridePoMap.values()) {
            RideInfoVo rideInfoVo = RideManager.rideInfoVoMap.get(ridePo.getRideId());
            if (rideInfoVo != null) {
                if (rideInfoVo.checkCondition(roleModule.getLevel(), accountRow.getVipLevel()) || ridePo.getOwned() == 1) {
                    showRidePoMap.put(ridePo.getRideId(), ridePo);
                }
            }
        }
        ClientRide ridePacket = new ClientRide(ClientRide.RESP_UPDATE_SHOW);
        ridePacket.setRidePoMap(showRidePoMap);
        send(ridePacket);
    }


//    private int  isTenConditions(int nextLevelId,int reqItemCount){
//        RideLevelVo nextLevelVo = getRideLvById(nextLevelId);
//        RoleModule roleModule = (RoleModule) module(MConst.Role);
//        ToolModule toolModule = (ToolModule) module(MConst.Tool);
//        if (nextLevelVo != null) {
//            if(roleModule.getLevel()>=nextLevelVo.getReqRoleLevel()){
//                for(Map.Entry<Integer,Integer> entry:nextLevelVo.getReqItemMap().entrySet()){
//                    if(toolModule.contains(entry.getKey(),entry.getValue()+reqItemCount)==false){
//                        return 0;
//                    }
//                    else{
//                        return entry.getValue();
//                    }
//                }
//            }
//        }
//        return 0;
//    }

    private boolean isOneConditions(int nextLevelId) {
        RideLevelVo nextLevelVo = RideManager.getRideLvById(nextLevelId);
        if (nextLevelVo == null) {
            return false;
        }
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        if (roleModule.getLevel() < nextLevelVo.getReqRoleLevel()) {
            return false;
        }
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        for (Map.Entry<Integer, Integer> entry : nextLevelVo.getReqItemMap().entrySet()) {
            if (toolModule.contains(entry.getKey(), entry.getValue()) == false) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(Integer.valueOf(RedPointConst.RIDE_NEW))) {
            getNewRide(redPointMap);
        }
        if (redPointIds.contains(Integer.valueOf(RedPointConst.RIDE_LVUP1))) {
            rideLevelUpOne(redPointMap);
        }
        // 觉醒的红点计算
        ToolModule toolModule = module(MConst.Tool);
        ForeShowModule foreShowModule = module(MConst.ForeShow);
        if (redPointIds.contains(RedPointConst.RIDE_AWAKE_LEVELUP) && foreShowModule.isOpen(ForeShowConst.RIDE_AWAKE)) { // 这里才进行计算
            List<Integer> list = new ArrayList<>();
            for (RoleRidePo po : ridePoMap.values()) {
                if (!po.isOwned()) continue;
                RideInfoVo infoVo = RideManager.getRideInfoVo(po.getRideId());
                RideAwakeLvlVo vo = RideManager.getRideAwakeLvlVo(po.getRideId(), po.getAwakeLevel() + 1);
                if (infoVo != null && infoVo.getTimeLimitType() == 0
                        && vo != null && toolModule.contains(vo.getToolMap())) {
                    list.add(po.getRideId());
                }
            }
            if (list.size() > 0) {
                redPointMap.put(RedPointConst.RIDE_AWAKE_LEVELUP, StringUtil.makeString(list, '+'));
            } else {
                redPointMap.put(RedPointConst.RIDE_AWAKE_LEVELUP, null);
            }
        }
    }

    /**
     * 预留点击坐骑的方法，点击后改为旧坐骑
     *
     * @param
     */
    /*public void setClick(int rideId){
        RoleRidePo roleRidePo = ridePoMap.get(rideId);
        if(roleRidePo.getOwned()==OWNED && roleRidePo.getClick()==RideConst.NOT_CLICK){
            roleRidePo.setClick(RideConst.CLICK);
            context().update(roleRidePo);
            signCalRedPoint(MConst.Ride,RedPointConst.RIDE_NEW);
        }
    }*/
    private void rideLevelUpOne(Map<Integer, String> redPointMap) {
        checkRedPoint(redPointMap, rideLevelUpOneUpList, RedPointConst.RIDE_LVUP1);
    }

    private void getNewRide(Map<Integer, String> rePointMap) {
        checkRedPoint(rePointMap, rideList, RedPointConst.RIDE_NEW);
        rideList.clear();
    }

    private void checkRedPoint(Map<Integer, String> redPointMap, Set<Integer> list, int redPointConst) {
        StringBuilder builder = new StringBuilder("");
        if (!list.isEmpty()) {
            Iterator<Integer> iterator = list.iterator();
            while (iterator.hasNext()) {
                builder.append(iterator.next()).append("+");
            }
            redPointMap.put(redPointConst, builder.toString().isEmpty() ? null : builder.toString());
        } else {
            redPointMap.put(redPointConst, null);
        }
    }
    
    /**
     * 检测限时坐骑
     */
    public boolean checkTimeLimitRide(RoleRidePo ridePo){
    	RideInfoVo rideInfoVo = RideManager.rideInfoVoMap.get(ridePo.getRideId());
    	if(rideInfoVo.getTimeLimitType()==0){
    		return false;
    	}
    	if(!ridePo.isOwned()) return false;
    	int effectTime = ridePo.getEndTime();
    	int currentTime = DateUtil.getCurrentTimeInt();
    	if(currentTime>=effectTime){
    		boolean result = false;
    		if(ridePo.isActive()){
    			result = true;
    		}
    		ridePo.setOwned(NOT_OWNED);//过期  设置为没拥有
    		ridePo.setActive(NOT_ACTIVE);
    		ridePo.setEndTime(0);
    		context().update(ridePo);
    		recalcFightScore();
        	roleModule.sendRoleAttr();
        	roleModule.sendUpdateFightScore();
    		//提示
//    		send(new ClientText(I18n.get("ride.OverTime", rideInfoVo.getName())));
    		LoginModule loginModule = module(MConst.Login);
    		if(!loginModule.isOnline()){
    			ridePo.setSendOver((byte)1);
    		}
    		ClientRide packet = new ClientRide(ClientRide.RESP_OVER_TIME);
    		packet.setRideName(rideInfoVo.getName());
    		packet.setRideId(ridePo.getRideId());
    		send(packet);
    		return result;
    	}
    	return false;
    }

    public Map<Integer, RoleRidePo> getRidePoMap() {
        return ridePoMap;
    }

    @Override
    public void setAccountRow(AccountRow accountRow) {
        this.accountRow = accountRow;
    }

    //    public Map<Integer, Integer> getRoleRideLevelMap() {
//        Map<Integer,Integer> map = new HashMap<>();
//        for (Map.Entry<Integer,RoleRidePo> entry : ridePoMap.entrySet()){
//            map.put(entry.getValue().getRideId(),entry.getValue().getRideLevel());
//        }
//        return map;
//    }

    public String makeFsStr() {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        int initFs = 0;
        int feedFs = 0;
        int awakeFs = 0;
        Attribute initAttr = new Attribute();
        Attribute feedAttr = new Attribute();
        Attribute awakeAttr = new Attribute();
        // 坐骑战力
        RideLevelVo levelVo = RideManager.getRideLvById(roleModule.getRideLevelId());
        for (RoleRidePo ridePo : ridePoMap.values()) {
            if (ridePo == null || !ridePo.isOwned()) continue;
            RideInfoVo infoVo = RideManager.getRideInfoVo(ridePo.getRideId());
            if (infoVo == null) continue;
            Attribute tempAttr = new Attribute(infoVo.getAttribute());
            initAttr.addAttribute(tempAttr);
            RideAwakeLvlVo awakeLvlVo = RideManager.getRideAwakeLvlVo(ridePo.getRideId(), ridePo.getAwakeLevel());
            if (awakeLvlVo == null) continue;
            awakeAttr.addAttribute(new Attribute(awakeLvlVo.getAwakeAttr()));
        }
        initFs = FormularUtils.calFightScore(initAttr);
        awakeFs = FormularUtils.calFightScore(awakeAttr);
        // 培养战力
        if (levelVo != null) {
            feedAttr.setHp(levelVo.getHp());
            feedAttr.setAttack(levelVo.getAttack());
            feedAttr.setDefense(levelVo.getDefense());
            feedAttr.setHit(levelVo.getHit());
            feedAttr.setAvoid(levelVo.getAvoid());
            feedAttr.setCrit(levelVo.getCrit());
            feedAttr.setAnticrit(levelVo.getAnticrit());
        }
        feedFs = FormularUtils.calFightScore(feedAttr);

        StringBuilder sb = new StringBuilder();
        sb.append("ride_base:").append(initFs).append("#")
                .append("ride_feed:").append(feedFs).append("#")
                .append("ride_awake:").append(awakeFs).append("#");
        return sb.toString();
    }
}
