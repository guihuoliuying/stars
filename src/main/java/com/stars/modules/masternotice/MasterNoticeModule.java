package com.stars.modules.masternotice;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyAwardCheckEvent;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.masternotice.packet.ClientMasterNotice;
import com.stars.modules.masternotice.packet.ClientMasterNoticePageInfo;
import com.stars.modules.masternotice.packet.ClientMasterNoticeRefresh;
import com.stars.modules.masternotice.prodata.MasterNoticeVo;
import com.stars.modules.masternotice.recordmap.MasterNoticeData;
import com.stars.modules.masternotice.recordmap.RecordMapMasterNotice;
import com.stars.modules.operateCheck.OperateCheckModule;
import com.stars.modules.operateCheck.OperateConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.event.PassMasterNoticeStageEvent;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.shop.event.BuyGoodsEvent;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.vip.VipManager;
import com.stars.modules.vip.VipModule;
import com.stars.modules.vip.prodata.VipinfoVo;
import com.stars.util.I18n;
import com.stars.util.LogUtil;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by gaopeidian on 2016/11/19.
 */
public class MasterNoticeModule extends AbstractModule {
	RecordMapMasterNotice record = null;
	
	public MasterNoticeModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
		super(MConst.BravePractise, id, self, eventDispatcher, moduleMap);
	}
	
    @Override
    public void onCreation(String name_, String account_) throws Throwable {
    	initRecordMap();
    }
    
    @Override
    public void onDataReq() throws Exception {
    	initRecordMap();    	
    }
    
    @Override
    public void onInit(boolean isCreation) throws Throwable {
    	checkAutoRefresh();
    }
    
    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
    	if (record != null) {
			record.reset(false);
		}
    	
    	sendMasterNoticePageInfo();
    }
    
    @Override
    public void onTimingExecute(){
        //checkRefresh();
    }
    
    private void initRecordMap() throws SQLException {
		record = new RecordMapMasterNotice(moduleMap() , context());
    } 
    
    public void onClock(){
    	autoRefresh();
    	record.setAutoRefreshTimestamp(new Date().getTime());
    	
    	//发送刷新消息到客户端
    	int leftRefreshCount = record.getFreeRefreshCount();
    	int leftCostRefreshCount = record.getCostRefreshCount();
        Map<Integer, MasterNoticeData> noticesMap = new HashMap<Integer, MasterNoticeData>(record.getNoticesMap());
    	ClientMasterNoticeRefresh clientMasterNoticeRefresh = new ClientMasterNoticeRefresh();
    	clientMasterNoticeRefresh.setLeftRefreshCount(leftRefreshCount);
    	clientMasterNoticeRefresh.setLeftCostRefreshCount(leftCostRefreshCount);
    	clientMasterNoticeRefresh.setNoticesMap(noticesMap);
    	send(clientMasterNoticeRefresh);
    }
    
    public void acceptNotice(int noticeId){
    	int totalCount = getTotalCount();
    	if (record.getDoneCount() >= totalCount) {
			com.stars.util.LogUtil.info("MasterNoticeModule.acceptNotice have no leftCount,roleId=" + id() + ",noticeId=" + noticeId);
			warn("master_time_out");
			return;
		}
    	
    	Map<Integer, MasterNoticeData> noticeDatas = record.getNoticesMap();
    	MasterNoticeData noticeData = noticeDatas.get(noticeId);
    	if (noticeData == null) {
			warn("master_no_notice");
			return;
		}
    	
    	if (noticeData.getStatus() == MasterNoticeData.STATUS_NOT_ACCEPT) {
    		noticeData.setStatus(MasterNoticeData.STATUS_ACCEPT);
    		record.setNoticesMap(record.getNoticesMap());
    		
    		//发送消息通知客户端
    		ClientMasterNotice clientMasterNotice = new ClientMasterNotice();
    		clientMasterNotice.setNoticeId(noticeData.getNoticeId());
    		clientMasterNotice.setNoticeStatus(noticeData.getStatus());
    		clientMasterNotice.setLeftCount(getTotalCount() - record.getDoneCount());
    		clientMasterNotice.setRewardMap(null);
    		send(clientMasterNotice);
		}else{
			warn("master_notice_has_accepted");
		}
    }

	/**
	 * 立刻完成
	 */
	public void finishRightNow(int noticeId){
		MasterNoticeVo noticeVo = MasterNoticeManager.getMasterNoticeVoById(noticeId);
		if(noticeVo == null) return;
		Map<Integer, MasterNoticeData> noticeDatas = record.getNoticesMap();
		MasterNoticeData noticeData = noticeDatas.get(noticeId);
		if(noticeData == null) return;

		int totalCount = getTotalCount();
		if (record.getDoneCount() >= totalCount) return;

		VipModule vipModule = module(MConst.Vip);
		VipinfoVo vipInfoVo = vipModule.getCurVipinfoVo();
		if(vipInfoVo == null) return;
		if(vipInfoVo.getNoticeAuto() != 1) return;//没有权限立刻完成

		//扣除金币判断
		ToolModule toolModule = module(MConst.Tool);
		if(!toolModule.deleteAndSend(VipManager.FINISH_MASTER_NOTICE_COST,EventType.FINISH_MASTER_NOTICE_BY_GOLD.getCode())){
			warn(I18n.get("family.bonfire.hasNoGold"));
			return;
		}

		submitNotice(noticeId, true);
		eventDispatcher().fire(new DailyAwardCheckEvent());
	}
    
    public void submitNotice(int noticeId,boolean finishRightNow){
		MasterNoticeVo noticeVo = MasterNoticeManager.getMasterNoticeVoById(noticeId);
		if(noticeVo == null) return;
    	int totalCount = getTotalCount();
    	if (record.getDoneCount() >= totalCount) {
			com.stars.util.LogUtil.info("MasterNoticeModule.submitNotice have no leftCount,roleId=" + id() + ",noticeId=" + noticeId);
			return;
		}
    	
    	Map<Integer, MasterNoticeData> noticeDatas = record.getNoticesMap();
    	MasterNoticeData noticeData = noticeDatas.get(noticeId);
    	if (noticeData != null && (noticeData.getStatus() == MasterNoticeData.STATUS_ACCEPT || finishRightNow)) {
    		noticeData.setStatus(MasterNoticeData.STATUS_FINISH);
    		record.setNoticesMap(record.getNoticesMap());
    		record.setDoneCount(record.getDoneCount() + 1);
    		eventDispatcher().fire(new DailyFuntionEvent(DailyManager.DAILYID_MASTER_NOTICE, 1));
    		
    		// 处理奖励   (关卡任务的奖励已在打胜关卡的时候发了，这里不重复发)
    		Map<Integer, Integer> reward = null;
            if (finishRightNow || noticeVo.getTaskType() != MasterNoticeVo.TASK_TYPE_PASS_STAGE) {
				reward = ((ToolModule)module(MConst.Tool)).addAndSend(noticeVo.getAwardMap(),EventType.MASTERNOTICE.getCode());
            }
            
            //添加到已完成的groups中
            List<Integer> finishGroups = record.getFinishGroups();
            finishGroups.add(noticeId);
            record.setFinishGroups(record.getFinishGroups());
            
            // 通知客户端该皇榜悬赏已完成
    		ClientMasterNotice clientMasterNotice = new ClientMasterNotice();
    		clientMasterNotice.setNoticeId(noticeData.getNoticeId());
    		clientMasterNotice.setNoticeStatus(noticeData.getStatus());
    		clientMasterNotice.setLeftCount(getTotalCount() - record.getDoneCount());
    		clientMasterNotice.setRewardMap(reward);
    		send(clientMasterNotice);
            
            //检查剩余次数是否为0
            checkLeftCount();

			if(finishRightNow){
//				sendMasterNoticePageInfo();//刷新界面
			}
		}
    }
    
    public void autoRefresh(){
    	Map<Integer, MasterNoticeData> curNoticesMap = record.getNoticesMap();
    	Map<Integer, MasterNoticeData> newNoticesMap = new HashMap<Integer, MasterNoticeData>();
    	List<Integer> acceptIds = new ArrayList<Integer>();
    	for (MasterNoticeData data : curNoticesMap.values()) {
			if (data.getStatus() == MasterNoticeData.STATUS_ACCEPT) {
				newNoticesMap.put(data.getNoticeId(), data);
				acceptIds.add(data.getNoticeId());
			}
		}
    	
    	int newCount = MasterNoticeConstant.noticeCount - newNoticesMap.size();
    	if (newCount > 0) {
    		//生成新的notice
			//不过滤已完成的数据 避免无法全刷橙色 下面那个刷新也一样 by dyr 2017.06.02
//            List<Integer> finishGroups = record.getFinishGroups();
            List<Integer> groups = new ArrayList<Integer>();//new ArrayList<Integer>(finishGroups);
            groups.addAll(acceptIds);
            Map<Integer, MasterNoticeData> tempNoticesMap = getNewMasterNotices(groups, newCount);
            
            for (MasterNoticeData data : tempNoticesMap.values()) {
				newNoticesMap.put(data.getNoticeId(), data);
			}
            
            record.setNoticesMap(newNoticesMap);
		}
    }
    
    public void checkAutoRefresh(){
    	long autoTimestamp = record.getAutoRefreshTimestamp();
    	if (autoTimestamp > 0) {
    		int second = getNextOnClockSecond(autoTimestamp);//上次刷新的时候，距离下次刷新还有多少时间
    		int passSecond = (int)((new Date().getTime() - autoTimestamp)/1000);//距离上次刷新经过了多少时间
    		if (passSecond > second) {//若经过的时间大于距离下次刷新的时间，则刷新
				autoRefresh();
				record.setAutoRefreshTimestamp(new Date().getTime());
			}
		}
    }
    
    //获得距离下次整点还有多少秒
    public int getNextOnClockSecond(long timestamp){
    	long hourSecond = 60 * 60;
    	long dateSecond = timestamp/1000;
    	int leftSecond = (int)(dateSecond % hourSecond);
    	int ret = (int)hourSecond - leftSecond;
        return ret;
    }
      
    public void refresh(){
    	if(!OperateCheckModule.checkOperate(id(), OperateConst.MASTER_NOTICE_REFRESH, OperateConst.FIVE_HUNDRED_MS)) return;
    	int freeRrfreshCount = record.getFreeRefreshCount();
    	if (freeRrfreshCount > 0) {
    		refreshNoticesByFree();
    		return;
		}
    	
    	int costRefreshCount = record.getCostRefreshCount();
    	if (costRefreshCount > 0) {
    		refreshNoticesByItem();
    		return;
		}
    	
    	warn("commonbtntext_norefreshcount");
    }
    
    public void checkRefresh(){
//    	if (record != null) {
//			long startTimestamp = record.getStartTimestamp();
//			int initRefreshCount = MasterNoticeManager.initRefreshCount;
//			int freeRrfreshCount = record.getFreeRefreshCount();
//			if (freeRrfreshCount >= initRefreshCount) {
//				if (startTimestamp > 0) {
//					record.setStartTimestamp(0);
//				}
//			}else{
//				if (startTimestamp > 0) {//startTimestamp > 0 ：说明有在倒计时
//					int coolDowmTime = MasterNoticeManager.refreshCoolDownTime;
//					int passTimeSecond = (int)((new Date().getTime() - startTimestamp)/1000);
//					if (passTimeSecond >= coolDowmTime) {
//						record.setStartTimestamp(0);
//						record.setFreeRefreshCount(freeRrfreshCount + 1);
//						//发送消息通知客户端增加了一次免费刷新次数
//						ClientMasterNoticeCountDown clientMasterNoticeCountDown = new ClientMasterNoticeCountDown();
//						clientMasterNoticeCountDown.setFlag(MasterNoticePacketSet.Flag_Add_Refresh_Count);
//						clientMasterNoticeCountDown.setRefreshTime(0);
//						clientMasterNoticeCountDown.setLeftRefreshCount(record.getFreeRefreshCount());
//						send(clientMasterNoticeCountDown);
//		
//					}
//				}else if (startTimestamp == 0) {//startTimestamp == 0：说明没在倒计时 
//					record.setStartTimestamp(new Date().getTime());
//					//发消息通知客户端开始一次新的倒计时
//					ClientMasterNoticeCountDown clientMasterNoticeCountDown = new ClientMasterNoticeCountDown();
//					clientMasterNoticeCountDown.setFlag(MasterNoticePacketSet.Flag_Begin_Count_Down);
//					clientMasterNoticeCountDown.setRefreshTime(MasterNoticeManager.refreshCoolDownTime);
//					send(clientMasterNoticeCountDown);
//				}
//			}
//		}
    }
    
    public void refreshNotices(){
        //生成新的notice
//        List<Integer> finishGroups = record.getFinishGroups();
        List<Integer> groups = new ArrayList<Integer>();//new ArrayList<Integer>(finishGroups);
        Map<Integer, MasterNoticeData> newNoticesMap = getNewMasterNotices(groups, MasterNoticeConstant.noticeCount);
        record.setNoticesMap(newNoticesMap);
    }
    
    public void refreshNoticesByFree(){
    	int freeRrfreshCount = record.getFreeRefreshCount();
    	if (freeRrfreshCount <= 0) {
    		warn("master_free_refresh_time_out");
			com.stars.util.LogUtil.info("MasterNoticeModule.refreshNoticesByFree freeRrfreshCount is 0,roleId=" + id());
			return;
		}
    	
    	refreshNotices();
    	
    	record.setFreeRefreshCount(record.getFreeRefreshCount() - 1);
    	
    	//发送刷新消息到客户端
    	int leftRefreshCount = record.getFreeRefreshCount();
    	int leftCostRefreshCount = record.getCostRefreshCount();
        Map<Integer, MasterNoticeData> noticesMap = new HashMap<Integer, MasterNoticeData>(record.getNoticesMap());
    	ClientMasterNoticeRefresh clientMasterNoticeRefresh = new ClientMasterNoticeRefresh();
    	clientMasterNoticeRefresh.setLeftRefreshCount(leftRefreshCount);
    	clientMasterNoticeRefresh.setLeftCostRefreshCount(leftCostRefreshCount);
    	clientMasterNoticeRefresh.setNoticesMap(noticesMap);
    	send(clientMasterNoticeRefresh);
    }
    
    public void refreshNoticesByItem(){
    	int costRrfreshCount = record.getCostRefreshCount();
    	if (costRrfreshCount <= 0) {
    		warn("commonbtntext_norefreshcount");
			com.stars.util.LogUtil.info("MasterNoticeModule.refreshNoticesByItem costRrfreshCount is 0,roleId=" + id());
			return;
		}
    	
    	Map<Integer, Integer> refreshCost = MasterNoticeManager.refreshCost;
    	ToolModule toolModule = (ToolModule)module(MConst.Tool);
    	
    	Iterator<Map.Entry<Integer , Integer>> iter = refreshCost.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, Integer> entry = iter.next();
            int itemId = entry.getKey();
            int count = entry.getValue();
            if (!toolModule.contains(itemId , count)) {
            	warn(I18n.get("bag_buyitem_nomoney") , ToolManager.getItemVo(itemId).getName());// 道具不足
            	return;
			}		            
        }
    	
    	if (!toolModule.deleteAndSend(refreshCost,EventType.MASTERNOTICE.getCode())) {
    		warn("bag_buyitem_nomoney");
    		LogUtil.info("MasterNoticeModule.refreshNoticesByItem item is not enough,roleId=" + id());
			return;
		}
    	
    	refreshNotices();
    	
    	record.setCostRefreshCount(record.getCostRefreshCount() - 1);
    	
    	//发送刷新消息到客户端
    	int leftRefreshCount = record.getFreeRefreshCount();
    	int leftCostRefreshCount = record.getCostRefreshCount();
        Map<Integer, MasterNoticeData> noticesMap = new HashMap<Integer, MasterNoticeData>(record.getNoticesMap());
    	ClientMasterNoticeRefresh clientMasterNoticeRefresh = new ClientMasterNoticeRefresh();
    	clientMasterNoticeRefresh.setLeftRefreshCount(leftRefreshCount);
    	clientMasterNoticeRefresh.setLeftCostRefreshCount(leftCostRefreshCount);
    	clientMasterNoticeRefresh.setNoticesMap(noticesMap);
    	send(clientMasterNoticeRefresh);
    }
    
    public int getTotalCount(){
    	VipModule vipModule = (VipModule)module(MConst.Vip);
    	//int myNobleLevel = MasterNoticeConstant.myNobleLevel;
    	int myNobleLevel = vipModule.getVipLevel();
    	return MasterNoticeManager.getTotalCountByNobelLevel(myNobleLevel);
    }
    
    public void checkLeftCount(){
    	int totalCount = getTotalCount();
    	int doneCount = record.getDoneCount();
    	VipModule vipModule = (VipModule)module(MConst.Vip);
    	int myNobleLevel = vipModule.getVipLevel();
    	if (doneCount >= totalCount && myNobleLevel < MasterNoticeConstant.maxNobleLevel) {
			//发送通知客户端提示用户通过提升贵族等级来增加次数
		}
    }
    
    public Map<Integer, MasterNoticeData> getMasterNoticeDatas(){
    	if (record != null) {
			return record.getNoticesMap();
		}
    	
    	return null;
    }
    
    public void handMasterNoticeEvent(Event event){
    	if (event instanceof PassMasterNoticeStageEvent) {
    		PassMasterNoticeStageEvent psEvent = (PassMasterNoticeStageEvent) event;
			int stageId = psEvent.getStageId();
        	Map<Integer, MasterNoticeData> masterNotices = record.getNoticesMap();
        	if (masterNotices != null) {
				for (MasterNoticeData data : masterNotices.values()) {
					if (data.getStatus() == MasterNoticeData.STATUS_ACCEPT) {
						MasterNoticeVo vo = MasterNoticeManager.getMasterNoticeVoById(data.getNoticeId());
						if (vo != null && vo.getTaskType() == MasterNoticeVo.TASK_TYPE_PASS_STAGE && vo.getTaskParam() == stageId) {
							submitNotice(data.getNoticeId(),false);
						}
					}
				}
			}
		}else if (event instanceof BuyGoodsEvent) {
			BuyGoodsEvent bgEvent = (BuyGoodsEvent) event;
			int goodsId = bgEvent.getGoodsId();
			Map<Integer, MasterNoticeData> masterNotices = record.getNoticesMap();
        	if (masterNotices != null) {
				for (MasterNoticeData data : masterNotices.values()) {
					if (data.getStatus() == MasterNoticeData.STATUS_ACCEPT) {
						MasterNoticeVo vo = MasterNoticeManager.getMasterNoticeVoById(data.getNoticeId());
						if (vo != null && vo.getTaskType() == MasterNoticeVo.TASK_TYPE_BUY_GOODS && vo.getTaskParam() == goodsId) {
							submitNotice(data.getNoticeId(),false);
							eventDispatcher().fire(new DailyAwardCheckEvent());
						}
					}
				}
			}
		}
    }
    
    public Map<Integer, MasterNoticeData> getNewMasterNotices(List<Integer> groups , int noticeCount){
    	Map<Integer, MasterNoticeData> ret = new HashMap<Integer, MasterNoticeData>();
    	RoleModule roleModule = (RoleModule)module(MConst.Role);
    	int level = roleModule.getLevel();
    	for (int i = 0; i < noticeCount; i++) {
    		MasterNoticeVo masterNoticeVo = getRandomMasterNotice(level, groups);
    		if (masterNoticeVo != null) {
    			int noticeId = masterNoticeVo.getNoticeId();
    			MasterNoticeData data = new MasterNoticeData(noticeId, MasterNoticeData.STATUS_NOT_ACCEPT, 0);
    			ret.put(data.getNoticeId(), data);
    			
    			groups.add(noticeId);
			}
		}
    	
    	return ret;
    }
    
    public Map<Integer, MasterNoticeData> getFirstMasterNotices(){
    	Map<Integer, MasterNoticeData> ret = new HashMap<Integer, MasterNoticeData>();
    	List<Integer> firstNoticeIds = MasterNoticeManager.firstNoticeIdList;
    	for (Integer noticeId : firstNoticeIds) {
    		MasterNoticeVo masterNoticeVo = MasterNoticeManager.getMasterNoticeVoById(noticeId);
    		if (masterNoticeVo != null) {
    			MasterNoticeData data = new MasterNoticeData(noticeId, MasterNoticeData.STATUS_NOT_ACCEPT, 0);
    			ret.put(data.getNoticeId(), data);
			}
		}
    	
    	return ret;
    }
    
    public MasterNoticeVo getRandomMasterNotice(int level , List<Integer> groups){
    	Map<Integer, MasterNoticeVo> masterNoticeVos = MasterNoticeManager.getMasterNoticesByLevel(level);
    	for (Integer noticeId : groups) {
    		masterNoticeVos.remove(noticeId);
		}
    	
    	int totalOdds = 0;
    	for (MasterNoticeVo masterNoticeVo : masterNoticeVos.values()) {
			totalOdds += masterNoticeVo.getOdds();
		}
    	
    	Random random = new Random();
    	int randomInt = random.nextInt(totalOdds);
    	
    	int index = 0;
    	for (MasterNoticeVo masterNoticeVo : masterNoticeVos.values()) {
			index += masterNoticeVo.getOdds();
			if (index >= randomInt) {
				return masterNoticeVo;
			}
		}
    	
    	return null;
    }
    
    public void requsetMasterNoticePageInfo(){
    	if (record.getIsEverIn() == 0) {
			record.reset(true);
			record.setIsEverIn((byte)1);
		}
    	
    	sendMasterNoticePageInfo();
    }
    
    public void sendMasterNoticePageInfo(){
        int totalCount = getTotalCount();
        
        int leftCount = totalCount - record.getDoneCount();
        
//        int refreshTime = 0;
//        int coolDowmTime = MasterNoticeManager.refreshCoolDownTime;
//        long startTimestamp = record.getStartTimestamp();
//        if (startTimestamp > 0) {
//        	int passTimeSecond = (int)((new Date().getTime() - startTimestamp)/1000);
//        	refreshTime = coolDowmTime - passTimeSecond;
//		}
		 
        int leftRefreshCount = record.getFreeRefreshCount();
        int leftCostRefreshCount = record.getCostRefreshCount();
        Map<Integer, MasterNoticeData> noticesMap = new HashMap<Integer, MasterNoticeData>(record.getNoticesMap());
    	
    	ClientMasterNoticePageInfo pageInfo = new ClientMasterNoticePageInfo();
    	pageInfo.setLeftCount(leftCount);
    	pageInfo.setTotalCount(totalCount);
    	//pageInfo.setRefreshTime(refreshTime);
    	pageInfo.setRefreshTime(0);
    	pageInfo.setLeftRefreshCount(leftRefreshCount);
    	pageInfo.setLeftCostRefreshCount(leftCostRefreshCount);
    	pageInfo.setNoticesMap(noticesMap);
    	send(pageInfo);
    }
    
    @SuppressWarnings("unused")
	public static void main(String[] args){
//    	Date data = new Date();
//    	GregorianCalendar cal = new GregorianCalendar();
//    	cal.setTime(data);
//    	cal.get(Calendar.MINUTE);
//    	cal.get(Calendar.SECOND);
    	
    	
    	Date date1 = new Date();
    	long hourS = 60 * 60;
    	long date1Second = date1.getTime()/1000;
    	int leftSecond = (int)(date1Second % hourS);
    	int ret = (int)hourS - leftSecond;
        int t = 0;
    }
}

