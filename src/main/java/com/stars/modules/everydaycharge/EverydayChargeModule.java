package com.stars.modules.everydaycharge;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.drop.DropUtil;
import com.stars.modules.everydaycharge.packet.ClientEverydayCharge;
import com.stars.modules.everydaycharge.prodata.EverydayChargeRewardVo;
import com.stars.modules.everydaycharge.prodata.EverydayChargeVo;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.event.OperateActivityFlowEvent;
import com.stars.modules.operateactivity.opentime.ActOpenTime4;
import com.stars.modules.operateactivity.opentime.ActOpenTimeBase;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.vip.VipModule;
import com.stars.services.ServiceHelper;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;
import com.stars.util.RandomUtil;
import com.stars.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.*;

public class EverydayChargeModule extends AbstractModule implements OpActivityModule {

	private int curActivityId = -1;
	
	/* rolerecord的key */
	public static final String EVERYDAY_CHARGE_PRODATA_ID = "everydaycharge.prodataId";//玩家奖励的产品数据id
	public static final String EVERYDAY_CHARGE_QUALIFY_STATE = "everydaycharge.qualifyState";//抽奖资格状态
	public static final String EVERYDAY_CHARGE_LOTTERY_TIMES = "everydaycharge.lotteryTimes";//可抽奖次数
	public static final String EVERYDAY_CHARGE_RECORDS = "everydaycharge.records"; //抽奖记录
	
	private Set<Integer> recordSet = new HashSet<>();//抽奖记录，记录dropid
	
	public EverydayChargeModule(long id, Player self, EventDispatcher eventDispatcher,
                                Map<String, Module> moduleMap) {
		super(MConst.EverydayCharge, id, self, eventDispatcher, moduleMap);
	}
	
	@Override
	public void onInit(boolean isCreation) throws Throwable {
		initRecordSet();
		signCalRedPoint(MConst.EverydayCharge, RedPointConst.EVERYDAY_CHARGE);
	}
	
	public Map<Integer, EverydayChargeVo> getcurActivityVoMap() {
		return EverydayChargeManager.everydayChargeVoMap.get(curActivityId);
	}
	
	public void openUI() {
		EverydayChargeVo chargeVo = getRoleChargeVo();
		if (chargeVo == null) {
			warn("数据不存在");
			return;
		}
		ClientEverydayCharge packet = new ClientEverydayCharge();
		packet.setRespType(ClientEverydayCharge.RESP_OPEN_UI);
		packet.setRewardVoMap(chargeVo.getRewardMap());
		packet.setRewardRecord(recordSet);
		packet.setChargeState(context().recordMap().getByte(EVERYDAY_CHARGE_QUALIFY_STATE));
		packet.setTimeDesc(getActivityTimeDesc());
		packet.setLotteryTimes(context().recordMap().getByte(EVERYDAY_CHARGE_LOTTERY_TIMES));
		send(packet);
	}
	
	/**
	 * 获得活动时间描述
	 * @return
	 */
	public String getActivityTimeDesc() {
		if (curActivityId == -1) return "";
		Date startDate = new Date();
		Date endDate = new Date();
		OperateActVo vo = OperateActivityManager.getOperateActVo(curActivityId);
		if (vo == null) return "";
		ActOpenTimeBase openTimeBase = vo.getActOpenTimeBase();
		if ((openTimeBase != null) && (openTimeBase instanceof ActOpenTime4)) {
			ActOpenTime4 time = (ActOpenTime4)openTimeBase;
			RoleModule roleModule = module(MConst.Role);
			Date createDate = DateUtil.toDate(roleModule.getRoleCreatedTime());
			startDate = ActOpenTime4.getStartDate(time, createDate);
			endDate = ActOpenTime4.getEndDate(time, createDate);
			String timeDesc = vo.getTimedesc();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtil.MDHM);
			String startDays = simpleDateFormat.format(startDate);
			String endDays = simpleDateFormat.format(endDate);
			String timeStr = DataManager.getGametext(timeDesc);
			return String.format(timeStr, startDays, endDays);
		}
		return vo.getTimedesc();
	}
	
	/**
	 * 随即抽取奖励
	 * @param rewardVoMap
	 * @return dropid
	 */
	public int randomReward(Map<Integer, EverydayChargeRewardVo> rewardVoMap) {
		int totalRate = 0;
		Set<EverydayChargeRewardVo> rewardVoSet = new HashSet<>();
		for (EverydayChargeRewardVo rewardVo : rewardVoMap.values()) {
			if (recordSet.contains(rewardVo.getDropid())) continue;
			rewardVoSet.add(rewardVo);
			totalRate += rewardVo.getRate();
		}
		if (totalRate == 0) {
			return -1;
		}
		int random = RandomUtil.SRANDOMINT.nextInt(totalRate) + 1;
		int tempRate = 0;
		for (EverydayChargeRewardVo rewardVo : rewardVoSet) {
			tempRate += rewardVo.getRate();
			if (tempRate >= random) {
				return rewardVo.getDropid();
			}
		}
		return -1;
	}
	
	public void getReward() {
		byte lotteryTimes = getByte(EVERYDAY_CHARGE_LOTTERY_TIMES);
		if (lotteryTimes <= 0) {
			warn("抽奖次数已用完");
			return;
		}
		EverydayChargeVo chargeVo = getRoleChargeVo();
		if (chargeVo == null) {
			warn("数据不存在");
		}
		int dropId = randomReward(chargeVo.getRewardMap());
        if (dropId == -1) {
            warn("抽奖次数已用完");
            return;
        }
		ClientEverydayCharge packet = new ClientEverydayCharge();
		packet.setRespType(ClientEverydayCharge.RESP_LOTTERY);
		packet.setDropId(dropId);
		send(packet);
		Map<Integer, Integer> dropMap = DropUtil.executeDrop(dropId, 1);
		ServiceHelper.emailService().sendToSingle(id(), EverydayChargeManager.REWARD_MAIL_TEMPLATE_ID, 0L, "系统", dropMap);
		context().recordMap().setByte(EVERYDAY_CHARGE_LOTTERY_TIMES, --lotteryTimes);
		recordSet.add(dropId);
		insertRecordStr();

		signCalRedPoint(MConst.EverydayCharge,RedPointConst.EVERYDAY_CHARGE);
	}
	
	@Override
	public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
		OperateActivityModule operateActivityModule = (OperateActivityModule)module(MConst.OperateActivity);
        if (redPointIds.contains(Integer.valueOf(RedPointConst.EVERYDAY_CHARGE))) {
        	byte lotteryTimes = context().recordMap().getByte(EVERYDAY_CHARGE_LOTTERY_TIMES);
        	if (curActivityId != -1 && operateActivityModule.isShow(curActivityId) && lotteryTimes > 0 && isOpenActivity()) {
				EverydayChargeVo chargeVo = getRoleChargeVo();
				if (chargeVo == null) {
					redPointMap.put(RedPointConst.EVERYDAY_CHARGE, null);
					return;
				}
				int dropId = randomReward(chargeVo.getRewardMap());
				if(dropId == -1){
					redPointMap.put(RedPointConst.EVERYDAY_CHARGE, null);
				}else {
					redPointMap.put(RedPointConst.EVERYDAY_CHARGE, "");
				}
			}else{
				redPointMap.put(RedPointConst.EVERYDAY_CHARGE, null);
			}   
        }
	}
	
	/**
	 * 更新记录
	 */
	private void insertRecordStr() {
		if (StringUtil.isEmpty(recordSet)) return;
		StringBuffer buffer = new StringBuffer();
		for (int dropId : recordSet) {
			buffer.append(dropId).append("&");
		}
		if (buffer.length() != 0) {
			buffer.deleteCharAt(buffer.length() - 1);
        }
		context().recordMap().setString(EVERYDAY_CHARGE_RECORDS, buffer.toString());
	}
	
	/**
	 * 初始化领取记录
	 */
	private void initRecordSet() {
		String recordStr = context().recordMap().getString(EVERYDAY_CHARGE_RECORDS);
		if (StringUtil.isEmpty(recordStr)) return;
		recordSet.clear();
		String[] records = recordStr.split("&");
		for (String str : records) {
			recordSet.add(Integer.parseInt(str));
		}
	}
	
	@Override
	public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
		try {
			context().recordMap().setByte(EVERYDAY_CHARGE_QUALIFY_STATE, EverydayChargeManager.QUALIFY_STATE_NOT_ACTIVE);
			context().recordMap().setByte(EVERYDAY_CHARGE_LOTTERY_TIMES, (byte) 0);
			onEndReset();
		} catch (Exception e) {
			com.stars.util.LogUtil.error(e.getMessage());
		}
	}
	
	public void handleOperateActivityFlowEvent(OperateActivityFlowEvent event){
		if (event.getStepType() == OperateActivityConstant.FLOW_STEP_NEW_DAY) {
			signCalRedPoint(MConst.EverydayCharge, RedPointConst.EVERYDAY_CHARGE);
		}
	}

	/**
	 * 活动结束重置
	 */
	public void onEndReset() {
		if (!isOpenActivity()) {
			context().recordMap().setInt(EVERYDAY_CHARGE_PRODATA_ID, -1);
			context().recordMap().setString(EVERYDAY_CHARGE_RECORDS, "");
		}
	}
	
	/**
	 * 充值增加抽奖次数
	 * @return
	 */
	public void addLotteryTimes() {
		try {
			byte charged = context().recordMap().getByte(EVERYDAY_CHARGE_QUALIFY_STATE);
			if (charged == EverydayChargeManager.QUALIFY_STATE_ACTIVE) return;
			context().recordMap().setByte(EVERYDAY_CHARGE_QUALIFY_STATE, EverydayChargeManager.QUALIFY_STATE_ACTIVE);
			context().recordMap().setByte(EVERYDAY_CHARGE_LOTTERY_TIMES, (byte) 1);
			signCalRedPoint(MConst.EverydayCharge, RedPointConst.EVERYDAY_CHARGE);
		} catch (Exception e) {
			LogUtil.error(e.getMessage());
		}
	}
	
	/**
	 * 获取玩家的产品数据
	 * @return
	 */
	private EverydayChargeVo getRoleChargeVo() {
		Map<Integer, EverydayChargeVo> chargeVoMap = getcurActivityVoMap();
		if (StringUtil.isEmpty(chargeVoMap)) return null;
		int prodataId = context().recordMap().getInt(EVERYDAY_CHARGE_PRODATA_ID);
		if (prodataId > 0) {
			return chargeVoMap.get(prodataId);
		}
		//不应该会执行到这里，用来防错
		matchRoleChargeVo();
		return chargeVoMap.get(context().recordMap().getInt(EVERYDAY_CHARGE_PRODATA_ID));
	}
	
	/**
	 * 获取跟玩家匹配的数据
	 * @return
	 */
	public void matchRoleChargeVo() {
		int prodataId = context().recordMap().getInt(EVERYDAY_CHARGE_PRODATA_ID);
		if (prodataId > 0) return;
		Map<Integer, EverydayChargeVo> chargeVoMap = getcurActivityVoMap();
		if (StringUtil.isEmpty(chargeVoMap)) return;
		RoleModule roleModule = module(MConst.Role);
		VipModule vipModule = module(MConst.Vip);
		int roleLv = roleModule.getLevel();
		int vipLv = vipModule.getVipLevel();
		for (EverydayChargeVo chargeVo : chargeVoMap.values()) {
			if (chargeVo.isSuitLv(roleLv) && chargeVo.isSuitVipLv(vipLv)) {
				context().recordMap().setInt(EVERYDAY_CHARGE_PRODATA_ID, chargeVo.getEverydayid());
				return;
			}
		}
		return;
	}
	
	/**
     * 是否在活动有效时间内
     * @param openTime
     * @param openDays
     * @return
     */
	public boolean isEffectiveTime() {
		int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_EverydayCharge);
		OperateActVo actVo = OperateActivityManager.getOperateActVo(curActivityId);
		if (actVo == null) return false;
		ActOpenTimeBase openTimeBase = actVo.getActOpenTimeBase();
		if (!(openTimeBase instanceof ActOpenTime4)) return true;
		ActOpenTime4 openTime4 = (ActOpenTime4)openTimeBase;
		RoleModule roleModule = module(MConst.Role);
		int createRoleDays = roleModule.getRoleCreatedDays();
		return openTime4.isEffectiveTime(openTime4, createRoleDays);
	}
	
	/**
	 * 是否开启活动
	 * @return
	 */
	public boolean isOpenActivity() {
		curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_EverydayCharge);
		if (curActivityId == -1) return false;
		OperateActVo vo = OperateActivityManager.getOperateActVo(curActivityId);
		OperateActivityModule operateActivityModule = (OperateActivityModule)module(MConst.OperateActivity);
		if (vo != null && operateActivityModule.isShow(vo.getRoleLimitMap()) && isEffectiveTime()) {
			return true;
		}
		return false;
	}

	@Override
	public int getCurShowActivityId() {
    	if (isOpenActivity()) {
			return curActivityId;
		}
    	
    	return -1;
	}

	@Override
	public byte getIsShowLabel() {
		// TODO Auto-generated method stub
		return 0;
	}

}
