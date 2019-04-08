package com.stars.modules.archery;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.archery.packet.ClientArchery;
import com.stars.modules.archery.packet.ServerArchery;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.drop.DropModule;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author huzhipeng
 * 射箭小游戏
 * 2017-06-08
 */
public class ArcheryModule extends AbstractModule implements OpActivityModule {

	public ArcheryModule(long id, Player self, EventDispatcher eventDispatcher,
                         Map<String, Module> moduleMap) {
		super("Archery", id, self, eventDispatcher, moduleMap);
	}
	
	private int myIntegeral;
	
	private byte startPaly;
	
	private ClientArchery offPacket = null;
	
	@Override
	public void onInit(boolean isCreation) throws Throwable {
		super.onInit(isCreation);
		signCalRedPoint(MConst.Archery, RedPointConst.ARCHERY);
		if(offPacket!=null){
			send(offPacket);
			offPacket = null;
		}
	}
	
	@Override
	public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
		setByte("archery.times", (byte)0);
	}
	
	@Override
	public int getCurShowActivityId() {
		int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_Archery);
        if (curActivityId != -1) {
            OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
            OperateActivityModule operateActivityModule = (OperateActivityModule) module(MConst.OperateActivity);
            boolean show = operateActivityModule.isShow(operateActVo.getRoleLimitMap());
            if (show) {
                return curActivityId;
            }
        }
        return -1;
	}
	
	public void handleRequest(ServerArchery packet){
		byte opType = packet.getOpType();
		if(opType==ArcheryManager.GET_PLAY_TIMES){
			getLeftPlayNum();
		}else if(opType==ArcheryManager.SYN_INTEGRAL){
			synIntegral(packet.getIntegral());
		}else if(opType==ArcheryManager.GET_AWARD){
			getReward(packet.getIntegral(), false);
		}else if(opType==ArcheryManager.START_PLAY){
			startPlay();
		}
	}
	
	/**
	 * 开始小游戏
	 */
	public void startPlay(){
		if(getCurShowActivityId()==-1){
			send(new ClientText("不在活动时间内"));
			return;
		}
		byte archeryTimes = getByte("archery.times");//已玩次数
		if(archeryTimes>=ArcheryManager.TotalPlayNum){
			send(new ClientText("今日次数已用完"));
			return;
		}
		startPaly = 1;
		setByte("archery.times", (byte)(archeryTimes+1));
		signCalRedPoint(MConst.Archery, RedPointConst.ARCHERY);
	}
	
	/**
	 * 同步积分（用于强退、掉线时结算处理）
	 */
	public void synIntegral(int integral){
		if(integral<0){
			return;
		}
		this.myIntegeral = integral;
	}
	
	/**
	 * 根据射箭积分发放奖励
	 */
	public void getReward(int integral, boolean isOff){
		if(integral<0){
			return;
		}
		if(getCurShowActivityId()==-1){
			send(new ClientText("不在活动时间内"));
			return;
		}
		if(integral==0){
			integral = myIntegeral;
		}
		if(integral>ArcheryManager.MaxIntegral){//修正积分（防止玩家作弊导致问题）
			integral = ArcheryManager.MaxIntegral;
		}
		if(startPaly==0){
			return;
//			setByte("archery.times", (byte)(archeryTimes+1));
		}
		byte archeryTimes = getByte("archery.times");
		if(archeryTimes>ArcheryManager.TotalPlayNum){
			return;
		}
		myIntegeral = 0;
		startPaly = 0;
		Iterator<Entry<int[], Integer>> iterator = ArcheryManager.integralAwardMap.entrySet().iterator();
		Entry<int[], Integer> entry = null;
		int[] integralLimit = null;
		int dropId = 0;
		for(;iterator.hasNext();){
			entry = iterator.next();
			integralLimit = entry.getKey();
			if(integral>=integralLimit[0]&&integral<=integralLimit[1]){
				dropId = entry.getValue();
				break;
			}
		}
		DropModule drop = module(MConst.Drop);
		ToolModule toolModule = (ToolModule) module(MConst.Tool);
    	Map<Integer, Integer> dropMap = drop.executeDrop(dropId, 1, true);
    	Map<Integer, Integer> getReward = toolModule.addAndSend(dropMap, EventType.ARCHERY_AWARD.getCode());
    	//发获奖提示到客户端
    	signCalRedPoint(MConst.Archery, RedPointConst.ARCHERY);
    	ClientAward clientAward = new ClientAward(getReward);
    	ClientArchery packet = new ClientArchery(ArcheryManager.SHOW_AWARD);
    	packet.setAward(dropMap);
    	if(!isOff){    		
    		send(clientAward);
    		send(packet);
    	}else{
    		offPacket = packet;
    	}
	}
		
	@Override
	public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
		if(redPointIds.contains(RedPointConst.ARCHERY)){
			if(getCurShowActivityId()==-1){
				return;
			}
			byte archeryTimes = getByte("archery.times");//已玩次数
			if(archeryTimes<ArcheryManager.TotalPlayNum){
				redPointMap.put(RedPointConst.ARCHERY, "");
			}else{
				redPointMap.put(RedPointConst.ARCHERY, null);
			}
		}
	}
	
	@Override
	public void onOffline() throws Throwable {
		if(getCurShowActivityId()==-1){
			return;
		}
		getReward(0, true);
	}
	
	/**
	 * 获取剩余可玩次数
	 */
	public void getLeftPlayNum(){
//		setByte("archery.times", (byte)0);
		byte archeryTimes = getByte("archery.times");//已玩次数
		byte leftTimes = (byte)(ArcheryManager.TotalPlayNum - archeryTimes);
		ClientArchery packet = new ClientArchery(ArcheryManager.PLAY_TIMES);
		packet.setLeftTimes(leftTimes);
		send(packet);
	}

	@Override
	public byte getIsShowLabel() {
		return 0;
	}

}
