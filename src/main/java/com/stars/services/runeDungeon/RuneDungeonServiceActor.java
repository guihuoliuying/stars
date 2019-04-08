package com.stars.services.runeDungeon;

import com.stars.core.persist.DbRowDao;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.core.db.DBUtil;
import com.stars.modules.runeDungeon.RuneDungeonManager;
import com.stars.modules.runeDungeon.event.RuneDungeonHelpAwardEvent;
import com.stars.modules.runeDungeon.userData.RuneDungeonPo;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.*;
import java.util.Map.Entry;

public class RuneDungeonServiceActor extends ServiceActor implements RuneDungeonService{
	
	private DbRowDao dao;
	
	private Map<Long, Map<Integer, Long>> offlieAwardMap = new HashMap<Long, Map<Integer,Long>>();//助战奖励
	
	private Map<Long, Integer> offlieAwardTimesMap = new HashMap<>();//助战收益次数
	
	private Map<Long, Long> awardUpdateTimeMap = new HashMap<>();//更新时间
	
	private Map<Long, Set<Long>> offlieHelpPlayerMap = new HashMap<>();//被帮助好友

	@Override
	public void init() throws Throwable {
		ServiceSystem.getOrAdd(SConst.RuneDungeonService, this);
		dao = new DbRowDao(SConst.RuneDungeonService);
	}

	@Override
	public void printState() {
		LogUtil.info("容器大小输出:{},offlieAwardMap:{},offlieAwardTimesMap:{},awardUpdateTimeMap:{},offlieHelpPlayerMap:{}",
    			this.getClass().getSimpleName(),offlieAwardMap.size(),offlieAwardTimesMap.size(),
    			awardUpdateTimeMap.size(),offlieHelpPlayerMap.size());
	}
	
	/**
	 * 获取离线助战奖励信息
	 * @param roleId
	 * @return
	 */
	public List<Object> getOfflineAward(long roleId){
		try {
			if(offlieAwardMap.containsKey(roleId)){
				List<Object> list = new ArrayList<>();
				list.add(awardUpdateTimeMap.remove(roleId));
				list.add(offlieAwardMap.remove(roleId));
				list.add(offlieAwardTimesMap.remove(roleId));
				list.add(offlieHelpPlayerMap.remove(roleId));
				return list;
			}
		} catch (Exception e) {
			LogUtil.error("help award getOfflineAward fail", e);
		}
		return null;
	}
	
	/**
	 * 发放好友助战奖励
	 * @param friendId 助战好友角色id
	 */
	@Override
	public void sendHelpFightAward(long friendId, long beHelpId, Map<Integer, Integer> toolMap){
		Player player = PlayerSystem.get(friendId);
		if(player!=null){
			RuneDungeonHelpAwardEvent event = new RuneDungeonHelpAwardEvent();
			event.setOpType(RuneDungeonHelpAwardEvent.ONLINE_AFTER_FIGHT);
			event.setToolMap(toolMap);
			event.setBeHelpId(beHelpId);
			ServiceHelper.roleService().notice(friendId, event);
		}else{
			Map<Integer, Long> map = offlieAwardMap.get(friendId);
			if(map==null){
				RuneDungeonPo runeDungeonPo = getRuneDungeonPo(friendId);
				if(runeDungeonPo==null) return;
				map = runeDungeonPo.getHelpReward();
				offlieAwardMap.put(friendId, map);
				offlieAwardTimesMap.put(friendId, runeDungeonPo.getHelpAwardTimes());
				offlieHelpPlayerMap.put(friendId, runeDungeonPo.getHaveHelpPlayerSet());
			}
			int helpAwardTimes = offlieAwardTimesMap.get(friendId);
			if(helpAwardTimes>RuneDungeonManager.HelpAwardLimit){
				return;
			}
			helpAwardTimes += 1;
			Set<Long> helpPlayerSet = offlieHelpPlayerMap.get(friendId);
			long currentTime = DateUtil.getCurrentTimeLong();
			awardUpdateTimeMap.put(friendId, currentTime);
			helpPlayerSet.add(beHelpId);
			if(StringUtil.isNotEmpty(toolMap)){				
				Iterator<Entry<Integer, Integer>> iterator = toolMap.entrySet().iterator();
				Entry<Integer, Integer> entry = null;
				int itemId = 0;
				for(;iterator.hasNext();){
					entry = iterator.next();
					itemId = entry.getKey();
					if(map.containsKey(itemId)){
						map.put(itemId, map.get(itemId)+entry.getValue());
					}else{
						map.put(itemId, (long)entry.getValue());
					}
				}
			}
			String helpRewardStr = StringUtil.makeString(map, '+', '|');
			StringBuffer sql = new StringBuffer();
			sql.append("update rolerunedungeon set helpawardtimes = ").append(helpAwardTimes)
			.append(", havehelpplayer = '").append(getHaveHelpPlayer(helpPlayerSet))
			.append("', helprewardstr = '").append(helpRewardStr).append("', helprewardupdatetime=").append(currentTime)
			.append(" where roleid = ").append(friendId);
			try {
				DBUtil.execUserSql(sql.toString());				
			} catch (Exception e) {
				LogUtil.error("help award update fail, sql:"+sql.toString(), e);
			}
		}
	}
	
	private String getHaveHelpPlayer(Set<Long> haveHelpPlayerSet){
		StringBuffer sb = new StringBuffer();
		for(Long playerId : haveHelpPlayerSet){
			if(sb.length()==0){
				sb.append(playerId);
			}else{
				sb.append(",").append(playerId);
			}
		}
		return sb.toString();
	}
	
	private RuneDungeonPo getRuneDungeonPo(long friendId){
		try {			
			String sql = "select * from rolerunedungeon where roleid = "+friendId;
			RuneDungeonPo runeDungeonPo = DBUtil.queryBean(DBUtil.DB_USER, RuneDungeonPo.class, sql);
			if(runeDungeonPo==null){
				int tokendungeonId = RuneDungeonManager.runeDungeonList.get(0).getTokendungeonId();
				runeDungeonPo = new RuneDungeonPo(friendId, tokendungeonId);
				dao.insert(runeDungeonPo);
				dao.flush();
			}
			return runeDungeonPo;
		} catch (Exception e) {
			LogUtil.error("help award getRuneDungeonPo", e);
			return null;
		}
	}
	
}
