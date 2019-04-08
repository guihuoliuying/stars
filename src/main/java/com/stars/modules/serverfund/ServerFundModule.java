package com.stars.modules.serverfund;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.drop.DropModule;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverfund.packet.ClientServerFund;
import com.stars.modules.serverfund.prodata.ServerFundVo;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.vip.VipModule;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerFundModule extends AbstractModule implements OpActivityModule {

	private int currActivityId = -1;//当前活动id
	
	/* rolerecord的key */
	public static final String SERVERFUND_BUY = "serverfund.buy";	//是否已经购买，0=没购买，1=已购买
	public static final String SERVERFUND_GETITEMS = "serverfund.getItems"; //领取记录
	
	private Set<Integer> getItemsRecord = new HashSet<>();	//领取过的基金记录
	
	public ServerFundModule(long id, Player self, EventDispatcher eventDispatcher,
                            Map<String, Module> moduleMap) {
		super(MConst.ServerFund, id, self, eventDispatcher, moduleMap);
	}
	
	@Override
	public void onDataReq() throws Throwable {
		
	}
	
	@Override
	public void onInit(boolean isCreation) throws Throwable {
		initRecordSet();
		signCalRedPoint(MConst.ServerFund, RedPointConst.SERVER_FUND);
	}
	
	/**
	 * 打开基金界面
	 */
	public void openFundUI() {
		Map<Integer, ServerFundVo> fundVoMap = ServerFundManager.ServerFundVoMap.get(currActivityId);
		if (StringUtil.isEmpty(fundVoMap)) {
			warn("活动数据不存在");
			return;
		}
		List<ServerFundVo> fundVoList = new ArrayList<>(fundVoMap.values());
		Collections.sort(fundVoList);
		ClientServerFund packet = new ClientServerFund(ClientServerFund.RESP_OPENUI);
		packet.setFundVoList(fundVoList);
		packet.setRecordStateMap(getRecordStateMap());
		packet.setHasBuy((byte) (haveBuy() ? 1 : 0));
		send(packet);
	}
	
	/**
	 * 获取基金的领取状态
	 * @return
	 */
	public Map<Integer, Byte> getRecordStateMap() {
		RoleModule roleModule = module(MConst.Role);
		int roleLv = roleModule.getLevel();
		Map<Integer, Byte> stateMap = new ConcurrentHashMap<>();
		Map<Integer, ServerFundVo> fundVoMap = ServerFundManager.ServerFundVoMap.get(currActivityId);
		for (ServerFundVo fundVo : fundVoMap.values()) {
			byte state = ServerFundManager.FUND_STATE_OF_NOTBUY;
			if (!haveBuy()) {
				state = ServerFundManager.FUND_STATE_OF_NOTBUY;
			} else if (getItemsRecord.contains(fundVo.getFundid())) {
				state = ServerFundManager.FUND_STATE_OF_HAVEGET;
			} else if (fundVo.getLevel() > roleLv) {
				state = ServerFundManager.FUND_STATE_OF_CANTGET;
			} else {
				state = ServerFundManager.FUND_STATE_OF_CANGET;
			}
			stateMap.put(fundVo.getFundid(), state);
		}
		return stateMap;
	}
	
	/**
	 * 是否已购买基金
	 * @return
	 */
	public boolean haveBuy() {
		return context().recordMap().getByte(SERVERFUND_BUY) == 1;
	}
	
	/**
	 * 购买基金
	 */
	public void buyFund() {
		VipModule vipModule = module(MConst.Vip);
		if (vipModule.getVipLevel() < ServerFundManager.minVipLevel) {
			warn(DataManager.getGametext("serverfund_viplevelimit"));
			return;
		}
		ToolModule toolModule = module(MConst.Tool);
		if (!toolModule.contains(ServerFundManager.moneyId, ServerFundManager.moneyCount)) {
			// TODO 弹出快速获得界面
			return;
		}
		if (haveBuy()) {
			warn(DataManager.getGametext("serverfund_cantrepeat"));
			return;
		}
		if (toolModule.deleteAndSend(ServerFundManager.moneyId, ServerFundManager.moneyCount, EventType.ACTIVITY_SERVER_FUND.getCode())) {
			context().recordMap().setByte(SERVERFUND_BUY, (byte) 1);
			signCalRedPoint(MConst.ServerFund, RedPointConst.SERVER_FUND);
			send(new ClientServerFund(ClientServerFund.RESP_BUY));
		}
	}
	
	/**
	 * 初始化领取记录
	 */
	private void initRecordSet() {
		String getRecord = context().recordMap().getString(SERVERFUND_GETITEMS);
		if (StringUtil.isEmpty(getRecord)) return;
		getItemsRecord.clear();
		String[] records = getRecord.split("&");
		for (String str : records) {
			getItemsRecord.add(Integer.parseInt(str));
		}
	}
	
	/**
	 * 更新记录
	 */
	private void insertRecordStr() {
		if (StringUtil.isEmpty(getItemsRecord)) return;
		StringBuffer buffer = new StringBuffer();
		for (int fundId : getItemsRecord) {
			buffer.append(fundId).append("&");
		}
		if (buffer.length() != 0) {
			buffer.deleteCharAt(buffer.length() - 1);
        }
		context().recordMap().setString(SERVERFUND_GETITEMS, buffer.toString());
	}
	
	/**
	 * 领取基金
	 */
	public void getFund(int fundId) {
		if (!haveBuy()) {
			warn("请先购买基金");
			return;
		}
		if (getItemsRecord.contains(fundId)) {
			warn("不能重复领取");
			return;
		}
		Map<Integer, ServerFundVo> fundVoMap = ServerFundManager.ServerFundVoMap.get(currActivityId);
		if (StringUtil.isEmpty(fundVoMap)) {
			warn("活动数据不存在");
			return;
		}
		ServerFundVo fundVo = fundVoMap.get(fundId);
		if (fundVo == null) {
			warn("产品数据出错");
			LogUtil.error("开服基金缺少产品数据，fundid=" + fundId);
			return;
		}
		RoleModule roleModule = module(MConst.Role);
		if (roleModule.getLevel() < fundVo.getLevel()) {
			warn("等级不足");
			return;
		}
		DropModule dropModule = module(MConst.Drop);
		Map<Integer, Integer> rewardMap = dropModule.executeDrop(fundVo.getReward(), 1,true);
		ToolModule toolModule = module(MConst.Tool);
		toolModule.addAndSend(rewardMap, EventType.ACTIVITY_SERVER_FUND.getCode());
		getItemsRecord.add(fundId);
		insertRecordStr();
		ClientServerFund packet = new ClientServerFund(ClientServerFund.RESP_GET);
		packet.setFundId(fundId);
		send(packet);
		signCalRedPoint(MConst.ServerFund, RedPointConst.SERVER_FUND);
		if (isOver()) {
			warn(DataManager.getGametext("serverfund_finish_desc"));
		}
	}
	
	/**
	 * 是否已领完所有基金
	 * @return
	 */
	public boolean isOver() {
		Map<Integer, ServerFundVo> fundVoMap = ServerFundManager.ServerFundVoMap.get(currActivityId);
		if (StringUtil.isEmpty(fundVoMap)) return true;
		return getItemsRecord.size() >= fundVoMap.size();
	}

	@Override
	public int getCurShowActivityId() {
		currActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_ServerFund);
    	if (currActivityId != -1) {
    		OperateActVo vo = OperateActivityManager.getOperateActVo(currActivityId);
			OperateActivityModule operateActivityModule = (OperateActivityModule)module(MConst.OperateActivity);
			if (vo != null && operateActivityModule.isShow(vo.getRoleLimitMap()) && !isOver()) {
				return currActivityId;
			}
		}
    	
    	return -1;
	}

	@Override
	public byte getIsShowLabel() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * 检查是否有可领取但还没领取的奖励
	 * @return
	 */
	private boolean hasReward() {
		Map<Integer, ServerFundVo> fundVoMap = ServerFundManager.ServerFundVoMap.get(currActivityId);
		if (StringUtil.isEmpty(fundVoMap)) return false;
		if (!haveBuy()) return false;
		RoleModule roleModule = module(MConst.Role);
		for (ServerFundVo fundVo : fundVoMap.values()) {
			if (roleModule.getLevel() >= fundVo.getLevel() && !getItemsRecord.contains(fundVo.getFundid())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
		OperateActivityModule operateActivityModule = (OperateActivityModule)module(MConst.OperateActivity);
        if (redPointIds.contains(Integer.valueOf(RedPointConst.SERVER_FUND))) {
        	if (currActivityId != -1 && operateActivityModule.isShow(currActivityId) && hasReward()) {
            	redPointMap.put(RedPointConst.SERVER_FUND, "");
			}else{
				redPointMap.put(RedPointConst.SERVER_FUND, null);
			}   
        }
	}

	public void handleEvent(Event event) {
		if (event instanceof RoleLevelUpEvent) {
			signCalRedPoint(MConst.ServerFund, RedPointConst.SERVER_FUND);
		}
	}
}
