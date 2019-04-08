package com.stars.modules.daily5v5;

import com.stars.bootstrap.ServerManager;
import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.daily5v5.event.Daily5v5AchieveEvent;
import com.stars.modules.daily5v5.packet.ClientDaily5v5;
import com.stars.modules.daily5v5.userdata.RoleDaily5v5Po;
import com.stars.modules.demologin.packet.ClientAnnouncement;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.drop.DropModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.imp.city.FamilyScene;
import com.stars.modules.scene.imp.city.SafeCityScene;
import com.stars.modules.scene.imp.fight.Daily5v5Scene;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.skill.SkillConstant;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.skill.SkillModule;
import com.stars.modules.skill.prodata.SkillvupVo;
import com.stars.modules.skill.userdata.RoleSkill;
import com.stars.modules.skyrank.event.SkyRankScoreHandleEvent;
import com.stars.modules.skyrank.prodata.SkyRankScoreVo;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.modules.vip.VipManager;
import com.stars.modules.vip.VipModule;
import com.stars.modules.vip.prodata.VipinfoVo;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.daily5v5.Daily5v5Flow;
import com.stars.multiserver.daily5v5.Daily5v5Manager;
import com.stars.multiserver.daily5v5.Daily5v5ServiceActor;
import com.stars.multiserver.daily5v5.data.Daily5v5BuffInfo;
import com.stars.multiserver.daily5v5.data.FivePvpMerge;
import com.stars.multiserver.daily5v5.data.MatchingInfo;
import com.stars.multiserver.daily5v5.data.PvpExtraEffect;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;
import com.stars.util.ServerLogConst;
import com.stars.util.StringUtil;

import java.util.*;

public class Daily5v5Module extends AbstractModule {

	public Daily5v5Module(long id, Player self, EventDispatcher eventDispatcher,
                          Map<String, Module> moduleMap) {
		super("Daily5v5", id, self, eventDispatcher, moduleMap);
	}
	
	private RoleDaily5v5Po roleDaily5v5Po;
	
	private LinkedHashMap<Integer, Integer> fightSkillMap = new LinkedHashMap<Integer, Integer>();//装载技能
	
	private Map<Byte, Integer> fightUseSkillMap = new HashMap<>();//装载技能位置
	
	private int continueFihgtServerId;
	
	private boolean isMatching;
	
	@Override
	public void onCreation(String name, String account) throws Throwable {
		roleDaily5v5Po = new RoleDaily5v5Po(id());
		context().insert(roleDaily5v5Po);
	}
	
	@Override
	public void onDataReq() throws Throwable {
		roleDaily5v5Po = DBUtil.queryBean(DBUtil.DB_USER, RoleDaily5v5Po.class, 
				"select * from roledaily5v5 where roleid = "+id());
		if(roleDaily5v5Po==null){
			roleDaily5v5Po = new RoleDaily5v5Po(id());
			context().insert(roleDaily5v5Po);
		}
	}

	@Override
	public void onInit(boolean isCreation) throws Throwable {
		if (Daily5v5ServiceActor.isOpen) {
			send(new ClientAnnouncement("fivepvp_tvtips_desc"));
		}
	}

	@Override
	public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
		roleDaily5v5Po.setFrequency((byte)0);
		context().update(roleDaily5v5Po);
	}
	
	@Override
	public void onOffline() throws Throwable {
		cancleMatching(true);//下线取消匹配     （匹配  第一阶段：直接取消，第二阶段：解散队伍，其他四个人回到第一阶段）
	}
	
	public void startMatching(){
		try {
			if(!Daily5v5Flow.isStarted()){
				return;
			}
			//次数判断
//			int frequency = roleDaily5v5Po.getFrequency();
//			if(frequency>=Daily5v5Manager.Daily5v5TotalCount){
//				send(new ClientText("dailyfivepvp_timedesc_finishall"));
//				return;
//			}
			RoleModule role = module(MConst.Role);
			SkillModule skillModule = module(MConst.Skill);
			int serverId = com.stars.bootstrap.ServerManager.getServer().getConfig().getServerId();
			String serverName = ServerManager.getServer().getConfig().getServerName();
			//创建战斗实体
			FivePvpMerge fivePvpMerge = Daily5v5Manager.pvpMergeMap.get(role.getRoleRow().getJobId());
			FighterEntity selfEntity = FighterCreator.createSelf(moduleMap());
			Attribute attribute = fivePvpMerge.getAttribute();
			Attribute loadingAttribute = new Attribute(attribute);
			loadingAttribute.setMaxhp(loadingAttribute.getHp());
			selfEntity.setAttribute(loadingAttribute);//装载属性
			
			selfEntity.setSkills(fightSkillMap);//装载技能
			Map<Integer, Integer> skillDamageMap = skillModule.getSkillDamageMap(fightSkillMap);
			selfEntity.setSkillDamageMap(skillDamageMap);//技能重新装载了，所以对于的伤害也重新重新计算
			//战力设置
			int fightScore = FormularUtils.calFightScore(loadingAttribute);//属性
			Set<Map.Entry<Integer, Integer>> set = fightSkillMap.entrySet();//技能
	        SkillvupVo vvo = null;
	        for (Map.Entry<Integer, Integer> entry : set) {
	            if (entry.getValue() > 0) {
	                vvo = SkillManager.getSkillvupVo(entry.getKey(), entry.getValue());
	                fightScore = fightScore + vvo.getBattlepower();
	            }
	        }
			selfEntity.setFightScore(fightScore);
			VipModule vipModule =  module(MConst.Vip);
			int vipLevel = vipModule.getVipLevel();
			VipinfoVo vipinfoVo = VipManager.getVipinfoVo(vipLevel);
			List<int[]> pvpextraeffectList = vipinfoVo.getPvpextraeffectList();
			Map<Integer, Daily5v5BuffInfo> initiativeBuff = new HashMap<>();
			Map<Integer, Daily5v5BuffInfo> passivityBuff = new HashMap<>();
			if(pvpextraeffectList.size()>0){
				PvpExtraEffect pvpExtraEffect = null;
				int[] iArr = pvpextraeffectList.get(0);
				for(int id : iArr){
					pvpExtraEffect = Daily5v5Manager.pvpExtraEffectMap.get(id);
					if(pvpExtraEffect!=null){
						int[] paramArr = pvpExtraEffect.getParamArr();
						int buffId = pvpExtraEffect.getBuffId();
						Daily5v5BuffInfo daily5v5BuffInfo = new Daily5v5BuffInfo(pvpExtraEffect.getFivepvpvipeffid(), buffId, 
								paramArr, pvpExtraEffect.getLevel());
						initiativeBuff.put(pvpExtraEffect.getEffecttype(), daily5v5BuffInfo);
					}
				}
				if(pvpextraeffectList.size()>1){
					int[] pArr = pvpextraeffectList.get(1);
					for(int id : pArr){
						pvpExtraEffect = Daily5v5Manager.pvpExtraEffectMap.get(id);
						if(pvpExtraEffect!=null){
							int[] paramArr = pvpExtraEffect.getParamArr();
							int buffId = pvpExtraEffect.getBuffId();
							Daily5v5BuffInfo daily5v5BuffInfo = new Daily5v5BuffInfo(pvpExtraEffect.getFivepvpvipeffid(), 
									buffId, paramArr, pvpExtraEffect.getLevel());
							passivityBuff.put(pvpExtraEffect.getEffecttype(), daily5v5BuffInfo);
						}
					}
				}
			}
			//获取天梯段位积分
			String roleName = role.getRoleRow().getName();
			int myFightScore = role.getFightScore();
//			int skyScore = ServiceHelper.skyRankLocalService().getSkyScore(id(), roleName, myFightScore);
//			if(skyScore<0){
//				skyScore = 0;
//			}
			
			MatchingInfo info = new MatchingInfo(id(), roleName, role.getLevel(), role.getRoleRow().getJobId(), role.getLevel(),
					roleDaily5v5Po.getWin(), roleDaily5v5Po.getLose(), serverId, serverName, selfEntity, myFightScore,
					initiativeBuff, passivityBuff);
			ServiceHelper.daily5v5Service().startMatching(info);
			this.isMatching = true;
		} catch (Exception e) {
			com.stars.util.LogUtil.error("daily5v5 startMatching fail, roleId:"+id(), e);
		}
	}
	
	public void checkContinue(){
		if(!Daily5v5Flow.isStarted()){
			return;
		}
		ServiceHelper.daily5v5Service().checkContinue(id());
	}
	
	public void cancleMatching(boolean isOffline){
		if(!Daily5v5Flow.isStarted()){
//			return;
		}
		ServiceHelper.daily5v5Service().cancelMatching(id(), isOffline);
	}
	
	public void continueFighting(){
		if(!Daily5v5Flow.isStarted()){
			return;
		}
		MultiServerHelper.modifyConnectorRoute(id(), continueFihgtServerId);
		ServiceHelper.daily5v5Service().continueFighting(id());
	}
	
	public void openUI(){
		try {
			if(!Daily5v5Flow.isStarted()){
				send(new ClientText("dailyfivepvp_timedesc_notopen"));
				return;
			}
//			int frequency = roleDaily5v5Po.getFrequency();
//			if(frequency>=Daily5v5Manager.Daily5v5TotalCount){
//				send(new ClientText("dailyfivepvp_timedesc_finishall"));
//				return;
//			}
			RoleModule role = module(MConst.Role);
			FivePvpMerge fivePvpMerge = Daily5v5Manager.pvpMergeMap.get(role.getRoleRow().getJobId());
			Attribute attribute = fivePvpMerge.getAttribute();
			Attribute loadingAttribute = new Attribute(attribute);
//			Map<Integer, Integer> skillMap = fivePvpMerge.getSkillMap();
			fightSkillMap.clear();
			if(StringUtil.isEmpty(fightSkillMap)){
				Map<Integer, Integer> tempSkillMap = new HashMap<>();
				SkillModule skillModule = module(MConst.Skill);
				LinkedHashMap<Integer, Integer> skillMap = fivePvpMerge.getSkillMap();
				LinkedHashMap<Integer, Integer> selectSkillMap = new LinkedHashMap<Integer, Integer>(skillMap);
				RoleSkill roleSkill = skillModule.getRoleSkill();
//				Map<Integer, Integer> skillLevelMap = roleSkill.getSkillLevelMap();//角色身上技能
				Map<Byte, Integer> useSkillMap = roleSkill.getUseSkillMap();//技能位置
				Map<Byte, Integer> useCopySkillMap = new HashMap<>(useSkillMap);//技能位置
				Map<Byte, Integer> emptyUseSkillMap = new HashMap<Byte, Integer>();//空技能位置
				Iterator<Byte> iterator = useCopySkillMap.keySet().iterator();
				byte position = 0;
				Integer skillId = null;
				for(;iterator.hasNext();){
					position = iterator.next();
					skillId = useCopySkillMap.get(position);
					if(position==5||position==6){						
						if(!skillModule.isOpenPassSkillPosition(position)){
							continue;
						}
					}
					if(position==4){//闪避
						continue;
					}else if(skillId!=null){		
						if(selectSkillMap.containsKey(skillId)){
							tempSkillMap.put(skillId, selectSkillMap.get(skillId));
							fightUseSkillMap.put(position, skillId);
							selectSkillMap.remove(skillId);
						}else{
							emptyUseSkillMap.put(position, 0);
						}
					}else{
						emptyUseSkillMap.put(position, 0);
					}
				}
				Iterator<Byte> emptyUseIterator = emptyUseSkillMap.keySet().iterator();
				for(;emptyUseIterator.hasNext();){
					position = emptyUseIterator.next();
					Iterator<Integer> selectIterator = selectSkillMap.keySet().iterator();
					for(;selectIterator.hasNext();){
						skillId = selectIterator.next();
						SkillvupVo skillvupVo = SkillManager.getSkillvupVo(skillId, 1);
						if(skillvupVo.getSkillType()!=SkillConstant.LVUP_SKILLTYPE_PASS){
							if(position!=5&&position!=6){
								continue;
							}
						}
						tempSkillMap.put(skillId, selectSkillMap.get(skillId));
						fightUseSkillMap.put(position, skillId);
						selectIterator.remove();
						break;
					}
				}
				for(byte i=0;i<7;i++){
					skillId = fightUseSkillMap.get(i);
					if(skillId==null) continue;
					fightSkillMap.put(skillId, tempSkillMap.get(skillId));
				}
			}
			VipModule module = module(MConst.Vip);
			VipinfoVo curVipinfoVo = module.getCurVipinfoVo();
			List<int[]> pvpextraeffectList = curVipinfoVo.getPvpextraeffectList();
			List<int[]> extraeffectshowList = curVipinfoVo.getExtraeffectshowList();
			SkillModule skillModule = module(MConst.Skill);
			Map<Integer, Integer> skillDamageMap = skillModule.getSkillDamageMap(fightSkillMap);
			
			ClientDaily5v5 packet = new ClientDaily5v5(Daily5v5Manager.OPEN_UI_INFO);
			packet.setAttribute(loadingAttribute);
			packet.setSkillMap(fightSkillMap);
			packet.setSkillDamageMap(skillDamageMap);
			packet.setPvpextraeffectList(pvpextraeffectList);
			packet.setExtraeffectshowList(extraeffectshowList);
			send(packet);
			checkContinue();
		} catch (Exception e) {
			com.stars.util.LogUtil.error("daily5v5 openUI fail, roleId:"+id(), e);
		}
	}
	
	public void getJoinTimes(){
		int frequency = roleDaily5v5Po.getFrequency();
		byte leftCount = 0;
		ClientDaily5v5 packet = new ClientDaily5v5(Daily5v5Manager.RESP_JOIN_TIMES);
		packet.setFrequency(frequency);
		send(packet);
	}
	
	/**
	 * 完成匹配   设置场景类型     (重新进入战场时也要调用)
	 */
	public void finishMatching(int continueFihgtServerId){
		if(this.continueFihgtServerId==0){			
			try {
				ServerLogModule serverLogModule = module(MConst.ServerLog);
				serverLogModule.log_daily5v5((byte)1, (byte)2, (byte)0);
				serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_START, ThemeType.ACTIVITY_104.getThemeId(), 0);
			} catch (Exception e) {
				com.stars.util.LogUtil.error("finishMatching log", e);
			}
		}
		this.continueFihgtServerId = continueFihgtServerId;
		SceneModule module = module(MConst.Scene);
		Daily5v5Scene scene = new Daily5v5Scene();
		module.enterScene(scene, SceneManager.SCENETYPE_FIGHTPK, -99, null);
		module.setLastSceneType(SceneManager.SCENETYPE_DAILY_5V5);
		this.isMatching = false;
	}
	
	/**
	 * 战斗结束处理
	 */
	public void finishFight(ClientDaily5v5 packet, byte result, int passTime){
		try{
			this.isMatching = false;
			this.continueFihgtServerId = 0;
			int frequency = roleDaily5v5Po.getFrequency();
//			if(frequency>=Daily5v5Manager.Daily5v5TotalCount){
//				return;
//			}
			frequency =	frequency+1;//已参与次数
			roleDaily5v5Po.setFrequency((byte)frequency);
			int[] finalReward = null;
			if(frequency<=Daily5v5Manager.gainsCounts[0]){//高收益
				finalReward = Daily5v5Manager.highReward;
			}else if(frequency>Daily5v5Manager.gainsCounts[0]&&frequency<=Daily5v5Manager.gainsCounts[1]){//低收益
				finalReward = Daily5v5Manager.lowReward;
			}else{//无收益
				finalReward = Daily5v5Manager.blankReward;
			}
			SceneModule sceneModule = module(MConst.Scene);
	        // 不在安全区
	        if (!(sceneModule.getScene() instanceof SafeCityScene) && !(sceneModule.getScene() instanceof FamilyScene)){
	        	if(sceneModule.getLastSceneType()==SceneManager.SCENETYPE_DAILY_5V5){	        		
	        		MultiServerHelper.modifyConnectorRoute(id(), MultiServerHelper.getServerId());
	        	}
	        }
			//发放奖励
			int awardId = 0;
			Map<Integer, Integer> awardMap = null;
			ToolModule toolModule = (ToolModule) module(MConst.Tool);
			DropModule dropModule = module(MConst.Drop);
			if(result==Daily5v5Manager.WIN_RESULT){
				awardId = finalReward[0];
				roleDaily5v5Po.setWin(roleDaily5v5Po.getWin()+1);
				roleDaily5v5Po.setLose(0);
			}else{
				awardId = finalReward[1];
				roleDaily5v5Po.setWin(0);
				roleDaily5v5Po.setLose(roleDaily5v5Po.getLose()+1);
			}
			context().update(roleDaily5v5Po);
			awardMap = dropModule.executeDrop(awardId, 1, true);
			Map<Integer, Integer> getReward = toolModule.addAndSend(awardMap, EventType.DAILY_5V5_AWARD.getCode());
			packet.setAward(awardMap);
			send(packet);
			//发获奖提示到客户端
			ClientAward clientAward = new ClientAward(getReward);
			send(clientAward);
			ServerLogModule serverLogModule = module(MConst.ServerLog);
			serverLogModule.log_daily5v5((byte)2, (byte)2, result);
			com.stars.util.LogUtil.info("daily5v5 award, roleId:"+id()+" , award:"+getReward.toString());
			if(result==Daily5v5Manager.WIN_RESULT){
				serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_WIN, ThemeType.ACTIVITY_104.getThemeId(), 
						serverLogModule.makeJuci(), ThemeType.ACTIVITY_104.getThemeId(), 0, passTime);
			}else{
				serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_FAIL, ThemeType.ACTIVITY_104.getThemeId(), 
						serverLogModule.makeJuci(), ThemeType.ACTIVITY_104.getThemeId(), 0, passTime);
			}
		} catch (Exception e) {
			LogUtil.error("daily5v5 finishFight fail, roleId:"+id(), e);
		}finally{
			if(result==Daily5v5Manager.WIN_RESULT){
				ServiceHelper.roleService().notice(id(), new SkyRankScoreHandleEvent(SkyRankScoreVo.TYPE_5V5PVP,SceneManager.STAGE_VICTORY));
			}else{
				ServiceHelper.roleService().notice(id(), new SkyRankScoreHandleEvent(SkyRankScoreVo.TYPE_5V5PVP,SceneManager.STAGE_FAIL));
			}
			ServiceHelper.roleService().notice(id(), new DailyFuntionEvent(DailyManager.DAILYID_DAILY_5V5, 1));
		}
		fireDaily5v5AchieveEvent(result);
		
//		ServiceHelper.emailService().sendToSingle(id(), templateId, 0L, "系统", award);
	}
	
	public void continueButFightEnd(){
		MultiServerHelper.modifyConnectorRoute(id(), MultiServerHelper.getServerId());
		send(new ClientText("战斗结束"));
	}

	public void fireDaily5v5AchieveEvent(int result){
		Daily5v5AchieveEvent event =  new Daily5v5AchieveEvent(result);
		eventDispatcher().fire(event);
	}
	
	/**
	 * 取消匹配成功
	 */
	public void cancelSuccess(){
		this.isMatching = false;
	}
	
	public boolean chckIsMaching(){
		return isMatching;
	}
	
	public void gmHandler(String[] args){
		byte opType = Byte.parseByte(args[0]);
		if(opType==1){			
			ServiceHelper.daily5v5Service().gmHandler(id(), args);
		}else if(opType==2){
			roleDaily5v5Po.setFrequency((byte)0);
		}
	}

	public int getContinueFihgtServerId() {
		return continueFihgtServerId;
	}
}
