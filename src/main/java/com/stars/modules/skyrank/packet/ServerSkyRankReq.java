package com.stars.modules.skyrank.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.skyrank.SkyRankManager;
import com.stars.modules.skyrank.SkyRankModule;
import com.stars.modules.skyrank.SkyRankPacketSet;
import com.stars.modules.skyrank.prodata.SkyRankSeasonVo;
import com.stars.multiserver.MultiServerHelper;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.services.skyrank.SkyRankShowData;

/**
 * 天梯相关请求
 * 
 * @author xieyuejun
 *
 */
public class ServerSkyRankReq extends PlayerPacket {

	public static final byte REQ_MAINUI = 1;// 天梯玩家数据
	public static final byte REQ_GRAD_DATA = 2;// 天梯段位数据
	public static final byte REQ_RANK_DATA = 3;// 天梯排行榜
	public static final byte REQ_RANK_WARDDATA = 4; // 天梯奖励产品数据
	public static final byte REQ_TIME_DESC = 5; // 天梯当前时间说明
	public static final byte REQ_GET_AWARD = 6; // 领取每日奖励

	private byte reqType;

	@Override
	public void execPacket(Player player) {
		if (reqType == REQ_MAINUI) {
			RoleModule rm = module(MConst.Role);
			SkyRankShowData myDefalutRank = new SkyRankShowData();
			myDefalutRank.setRoleId(player.id());
			myDefalutRank.setName(rm.getRoleRow().getName());
			myDefalutRank.setFightscore(rm.getFightScore());
			myDefalutRank.setServerName(MultiServerHelper.getServerName());
			ServiceHelper.skyRankLocalService().reqRoleScoreMsg(player.id(),myDefalutRank);
		} else if (reqType == REQ_GRAD_DATA) {
			
			ClientSkyRankGradData clientSkyRankGradData = new ClientSkyRankGradData();
			clientSkyRankGradData.setSkyRankGradMap(SkyRankManager.getManager().getSkyRankGradMap());
			com.stars.network.server.packet.PacketManager.send(player.id(), clientSkyRankGradData);

			RoleModule roleModule = module(MConst.Role);
			Role roleRow = roleModule.getRoleRow();
			ServiceHelper.skyRankLocalService().checkRankGradeWhileLogin(
					player.id(), roleRow.getName(), roleRow.getFightScore());
		} else if (reqType == REQ_RANK_DATA) {
			RoleModule rm = module(MConst.Role);
			SkyRankShowData myDefalutRank = new SkyRankShowData();
			myDefalutRank.setRoleId(player.id());
			myDefalutRank.setName(rm.getRoleRow().getName());
			myDefalutRank.setFightscore(rm.getFightScore());
			myDefalutRank.setServerId(MultiServerHelper.getServerId());
			myDefalutRank.setServerName(MultiServerHelper.getServerName());
			ServiceHelper.skyRankLocalService().reqRankMsg(player.id(),myDefalutRank);
		} else if (reqType == REQ_RANK_WARDDATA) {
			ClientSkyRankAwardData clientSkyRankAwardData = new ClientSkyRankAwardData();
			clientSkyRankAwardData.setAwardList(SkyRankManager.getManager().getAwardList());
			com.stars.network.server.packet.PacketManager.send(player.id(), clientSkyRankAwardData);
		} else if (reqType == REQ_TIME_DESC) {
			SkyRankSeasonVo nowSeason = SkyRankManager.getManager().getSkyRankSeasonVo(SkyRankManager.getManager().getNowSeasonId());
			if(nowSeason ==  null)return;
			ClientSkyRankTimeDesc clientSkyRankTimeDesc = new ClientSkyRankTimeDesc();
			long now = System.currentTimeMillis();
			if(now < nowSeason.getLockedTime()){
				clientSkyRankTimeDesc.setTimeType(ClientSkyRankTimeDesc.TIME_TYPE_OPENSCORE);
				clientSkyRankTimeDesc.setTimeStamp(nowSeason.getSendAwardTime());
			}else{
				clientSkyRankTimeDesc.setTimeType(ClientSkyRankTimeDesc.TIME_TYPE_CLOSESCORE);
				clientSkyRankTimeDesc.setTimeStamp(nowSeason.getFinishedTime());
			}
			RoleModule rm = module(MConst.Role);
			Role roleRow = rm.getRoleRow();
			Object[] info = ServiceHelper.skyRankLocalService().getDailyAwardState(roleRow.getRoleId(), roleRow.getName(), roleRow.getFightScore());
			clientSkyRankTimeDesc.setAwardId((Integer)info[0]);
			clientSkyRankTimeDesc.setDailyAwardState((Byte)info[1]);
			PacketManager.send(player.id(), clientSkyRankTimeDesc);
		} else if(reqType == REQ_GET_AWARD){
			SkyRankModule skyRankModule = module(MConst.SkyRank);
			skyRankModule.getDailyAward();
		}
	}

	@Override
	public short getType() {
		return SkyRankPacketSet.ServerSkyRankReq;
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		reqType = buff.readByte();
	}

	public byte getReqType() {
		return reqType;
	}

	public void setReqType(byte reqType) {
		this.reqType = reqType;
	}

}
