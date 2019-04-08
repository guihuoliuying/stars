package com.stars.multiserver.teamPVPGame.stepIns;

import com.stars.modules.data.DataManager;
import com.stars.modules.teampvpgame.TeamPVPGameManager;
import com.stars.modules.teampvpgame.packet.ClientTPGData;
import com.stars.modules.teampvpgame.prodata.DoublePVPConfigVo;
import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.multiserver.teamPVPGame.*;
import com.stars.multiserver.teamPVPGame.helper.TPGTask;
import com.stars.services.ServiceHelper;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.services.chat.ChatManager;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author dengzhou
 *报名
 */
public class SignupTPGStep extends AbstractTPGStep {
	
	private int teamIdCounter;	
	
	public SignupTPGStep(){

	}


	@Override
	public void init0(TPGHost tpgFlow, Collection<TPGTeam> teams) {
		teamIdCounter = 0;
	}
	
	@Override
	public void initFromDB0(TPGHost tpgFlow){
		teamIdCounter = getMaxTeamId()+1;
	}
	
	
	private int getMaxTeamId(){
		Set<Integer>set = teamMap.keySet();
		int id = 0;
		for (Integer integer : set) {
			if (integer > id) {
				id = integer;
			}
		}
		return id;
	}
	
	/**
	 * @param team
	 * @return
	 * 报名
	 */
	public boolean addTeam(BaseTeam team){
		if (System.currentTimeMillis() < beginTime) {
			LogUtil.info("报名还未开始");
			return false;
		}
		Map<Long, BaseTeamMember> map = team.getMembers();
		if (map.size() <= 0) {
			return false;
		}
		Set<Entry<Long, BaseTeamMember>> set = map.entrySet();
		for (Entry<Long, BaseTeamMember> entry : set) {
			if (memberMap.containsKey(entry.getKey())) {
				LogUtil.error("存在重复的角色："+entry.getKey());
				return false;
			}
		}
		TPGTeam tpgTeam = new TPGTeam(teamIdCounter++);
		tpgTeam.setTpgId(tHost.getTpgId());
		tpgTeam.setCaptainId(team.getCaptainId());
		for (Entry<Long, BaseTeamMember> entry : set) {
			TPGTeamMember tpgMember = new TPGTeamMember();
			tpgMember.setRoleId(entry.getKey());
			tpgMember.setName(entry.getValue().getName());
			tpgMember.setLevel(entry.getValue().getLevel());
			tpgMember.setTeamId(tpgTeam.getTeamId());
			tpgMember.setJob(entry.getValue().getJob());
			tpgMember.setFight(entry.getValue().getFightSocre());
			tpgMember.setStep(tHost.getStep());
			tpgMember.setfEntity(entry.getValue().getRoleEntity());
			tpgMember.setFamilyName(entry.getValue().getFamilyName());
			tpgMember.setInsertStatus();
			insertDBList.add(tpgMember);
			tpgTeam.addUpdateMember(tpgMember);
			memberMap.put(entry.getKey(), tpgMember);
		}
		tpgTeam.setStep(tHost.getStep());
		teamMap.put(tpgTeam.getTeamId(),tpgTeam);
		tpgTeam.setInsertStatus();
		synchronized (insertDBList) {
			insertDBList.add(tpgTeam);
		}
		ClientTPGData clientTPGData = new ClientTPGData(ClientTPGData.TPG_TEAM_DATA);
		clientTPGData.setMyTeam(tpgTeam);
		tpgTeam.sendPacketToMember(clientTPGData);
		return true;
	}
	
	@Override
	public void onReceived(Object message) {
		
	}
	
	@Override
	public void maintenance() {
		if (System.currentTimeMillis() >= this.endTime) {
			//报名结束
			this.tHost.promotion(teamMap.values());
			return;
		}
		// 滚屏通知
		doTask(announceTasks);
		// 聊天窗口通知
		doTask(chatNoticeTasks);
	}
	
	@Override
	public void initConfig() throws Exception {
//		doublepvp_signtime
//		报名时间段, 格式为周几+hh:mm:ss|周几+hh:mm:ss, 表示在这个一个星期的这一天的开始时间点+结束时间点内可以报名
		DoublePVPConfigVo configVo = TeamPVPGameManager.getConfigVo(tHost.getTpgType());
		if (configVo == null)
			return;
		String configStr = configVo.getSignTime();
		String[] ss = configStr.split("[|]");
		this.beginTime = TPGUtil.weekTime2AbsolutTime(ss[0]);
		this.endTime = TPGUtil.weekTime2AbsolutTime(ss[1]);
		// doublepvp_signuptips_time
		// 报名提示,格式:全服滚动提示次数+间隔时间+聊天界面提示次数+间隔时间
		int[] temp = StringUtil.toArray(DataManager.getCommConfig("doublepvp_signuptips_time"), int[].class, '+');
		this.noticeCount = temp[0];
		this.noticeDisTime = temp[1];
		this.chatNoticeCount = temp[2];
		this.chatNoticeInterval = temp[3];
		this.announceNotice = DataManager.getGametext("doublepvp_signup_servertips");
		this.chatNotice = DataManager.getGametext("doublepvp_signup_servermessage");
		for (int i = 0; i < noticeCount; i++) {
			announceTasks.addLast(new AnnounceSignUpTask(beginTime + i * noticeDisTime * 1000L));
		}
		for (int i = 0; i < chatNoticeCount; i++) {
			chatNoticeTasks.addLast(new ChatSignUpTask(beginTime + i * chatNoticeInterval * 1000L));
		}
	}
	
	@Override
	public void doFightScenceEnd(TPGFightScence scence, TPGTeam winner, TPGTeam loser, Object... params) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void doLuaFram(String fightScence, LuaFrameData luaFrameData) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void doOffLine(long roleId) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean offline(long memberId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void enterFight(long initiator) {

	}

	/**
	 * 本服滚屏通知
	 */
	class AnnounceSignUpTask extends TPGTask {
		public AnnounceSignUpTask(long time) {
			super(time);
		}

		@Override
		public void doTask() {
			ServiceHelper.chatService().announce(announceNotice);
		}
	}

	/**
	 * 本服聊天窗口通知
	 */
	class ChatSignUpTask extends TPGTask {

		public ChatSignUpTask(long time) {
			super(time);
		}

		@Override
		public void doTask() {
			ServiceHelper.chatService().chat(TPGUtil.chatNoticeSenderName, ChatManager.CHANNEL_WORLD, 0L, 0L,
					chatNotice, Boolean.TRUE);
		}
	}

}
