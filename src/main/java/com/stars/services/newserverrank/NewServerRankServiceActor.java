package com.stars.services.newserverrank;

import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.drop.DropUtil;
import com.stars.modules.newserverrank.NewServerRankManager;
import com.stars.modules.newserverrank.packet.ClientNewServerRank;
import com.stars.modules.newserverrank.prodata.NewServerRankVo;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.opentime.ActOpenTime3;
import com.stars.modules.operateactivity.opentime.ActOpenTimeBase;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by gaopeidian on 2016/12/19.
 */
public class NewServerRankServiceActor extends ServiceActor implements NewServerRankService {
    int curActivityId = -1;
    
	@Override
	public void init() throws Throwable {
		ServiceSystem.getOrAdd(SConst.NewServerRankService, this);
			
		curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_NewServerRank);			
	}

	@Override
	public void printState() {

	}

	@Override
	public void openActivity(int activityId) {
		
	}

	@Override
	public void closeActivity(int activityId) {
		if (1002 != activityId) {
			return;
		}
		
        sendReward(activityId);
	}
	
	@Override
	public void getRewardInfo(int activityId , long roleId){
		Date startDate = new Date();
		Date endDate = new Date();
		OperateActVo vo = OperateActivityManager.getOperateActVo(activityId);
		if (vo != null) {
			ActOpenTimeBase openTimeBase = vo.getActOpenTimeBase();
			if ((openTimeBase != null) && (openTimeBase instanceof ActOpenTime3)) {
				ActOpenTime3 time = (ActOpenTime3)openTimeBase;
				Date openServerDate = DataManager.getOpenServerDate();
				startDate = ActOpenTimeBase.getStartDateByOpenTime3(time, openServerDate);
				endDate = ActOpenTimeBase.getEndDateByOpenTime3(time, openServerDate);
			}
		}
		
		int rankType = NewServerRankManager.getRankType(activityId);
		List<NewServerRankVo> voList = NewServerRankManager.getActivityRankVoList(activityId);
		
		ClientNewServerRank clientNewServerRank = new ClientNewServerRank();
		clientNewServerRank.setFlag(clientNewServerRank.Flag_Get_Reward_Info);
		clientNewServerRank.setRankType(rankType);
		clientNewServerRank.setRankRewardVoList(voList);
		clientNewServerRank.setStartTimeStamp(startDate.getTime());
		clientNewServerRank.setEndTimeStamp(endDate.getTime());
		PlayerUtil.send(roleId, clientNewServerRank);
	}
	 
	@Override
	public void getRankInfo(int activityId , long roleId){
		int rankType = NewServerRankManager.getRankType(activityId);
		
		Map<Long, String> rankMap = new LinkedHashMap<Long, String>();
		int myRank = -1;
		
		if (rankType == NewServerRankVo.RankTypeRoleLevel) {// 等级排名
			int rankId = RankConstant.RANKID_ROLELEVEL;
			List<AbstractRankPo> rankPoList = ServiceHelper.rankService().getFrontRank(rankId, 3);
			if (rankPoList != null) {
				for (AbstractRankPo po : rankPoList) {
					long rId = po.getUniqueId();
					String name = "";
					RoleSummaryComponent roleSummary = (RoleSummaryComponent)
		                    ServiceHelper.summaryService().getSummaryComponent(rId, MConst.Role);
		            if (roleSummary != null){
		            	name = roleSummary.getRoleName();
		            }
		            
		            rankMap.put(rId, name);
				}
			}
		
			AbstractRankPo myRankPo = ServiceHelper.rankService().getRank(rankId, roleId);
			if (myRankPo != null) {
				myRank = myRankPo.getRank();
			}
		}
		
		ClientNewServerRank clientNewServerRank = new ClientNewServerRank();
		clientNewServerRank.setFlag(clientNewServerRank.Flag_Get_Rank_Info);
	    clientNewServerRank.setrankMap(rankMap);
		clientNewServerRank.setMyRank(myRank);
		PlayerUtil.send(roleId, clientNewServerRank);
	}
	
	public void sendReward(int activityId){
		int rankType = NewServerRankManager.getRankType(activityId);
		if (rankType < 0) {
			LogUtil.info("NewServerRankServiceActor.sendReward get rankType fail,rankType=" + rankType);
			return;
		}
		
		if (rankType == NewServerRankVo.RankTypeRoleLevel) {// 等级排名奖励
			int maxRank = NewServerRankManager.getMaxRewardRank(activityId);
			List<AbstractRankPo> rankPoList = ServiceHelper.rankService().getFrontRank(RankConstant.RANKID_ROLELEVEL, maxRank);
			List<NewServerRankVo> activityRewadList = NewServerRankManager.getActivityRankVoList(activityId);
			
			if (activityRewadList != null) {
				int size = rankPoList.size();
				for (NewServerRankVo vo : activityRewadList) {
					int rankStart = vo.getRankStart();
					int rankEnd = vo.getRankEnd();
					for (int rank = rankStart; rank <= rankEnd; rank++) {
						int index = rank - 1;
						if (index < 0 || index > size - 1) {
							continue;
						}
						AbstractRankPo rankPo = rankPoList.get(index);
						if (rankPo != null) {
							Map<Integer, Integer> rewardMap = DropUtil.executeDrop(Integer.parseInt(vo.getReward()), 1);
							ServiceHelper.emailService().sendToSingle(rankPo.getUniqueId(), NewServerRankVo.RoleLevelEmailId,
									0L, "活动管理员", rewardMap, Integer.toString(rank));
						}
					}
				}
			}			
		}else{
			LogUtil.info("NewServerRankServiceActor.sendReward rankType is not available,rankType=" + rankType);
		}	
	}
}
