package com.stars.modules.skyrank;

import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.skyrank.prodata.SkyRankScoreVo;
import com.stars.modules.skyrank.userdata.SkyRankDataPo;
import com.stars.modules.skyrank.userdata.SkyRankScoreRecord;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.skyrank.SkyRankRoleOp;
import com.stars.util.LogUtil;

/**
 * 
 * 天梯积分处理
 * 
 * @author xieyuejun
 *
 */
public class SkyRankScoreHandle {

	public static final byte RESULT_LOSE = 0;
	public static final byte RESULT_WIN = 2;

	private short type;
	private byte isWin;
	private long roleId;
	private String name;
	private int fightScore;

	public SkyRankScoreHandle(long roleId, short type, byte isWin, String name, int fightScore) {
		this.type = type;
		this.isWin = isWin;
		this.roleId = roleId;
		this.name = name;
		this.fightScore = fightScore;
	}

	// 积分锁
	public boolean getSwitch(short type) {
		if (!SkyRankScoreVo.SCORE_SWITCH_ALL)
			return false;
		if (type == SkyRankScoreVo.TYPE_5V5PVP) {
			return SkyRankScoreVo.SCORE_SWITCH_5V5PVP;
		}
		if (type == SkyRankScoreVo.TYPE_KFPVP) {
			return SkyRankScoreVo.SCORE_SWITCH_KFPVP;
		}
		if (type == SkyRankScoreVo.TYPE_OFFLINEPVP) {
			return SkyRankScoreVo.SCORE_SWITCH_OFFLINEPVP;
		}
		return false;
	}

	// 积分加成获取
	public int getReduceScore(SkyRankScoreVo rsv, SkyRankScoreRecord scoreRecord) {
		return rsv.getFailScore();
	}

	// 积分扣除获取
	public int getAddScore(SkyRankScoreVo rsv, SkyRankScoreRecord scoreRecord) {
		return rsv.getSucScore();
	}

	/**
	 * 处理积分业务
	 * 
	 * @param moduleMap
	 */
	public boolean handleScore(SkyRankRoleOp skyRankRoleOp) {
		SkyRankDataPo skyRankData = skyRankRoleOp.getSkyRankData();
		// 对应业务的加分记录
		SkyRankScoreRecord scoreRecord = skyRankRoleOp.getSkyRankScoreRecord(type);
		if (scoreRecord == null) {
			scoreRecord = new SkyRankScoreRecord(type);
			skyRankRoleOp.addLadderScoreRecord(scoreRecord);
		}
		SkyRankScoreVo rsv = SkyRankManager.getManager().getSkyRankScoreVo(type);
		if (rsv == null) {
			return false;
		}
		// 开关是否开启
		if (!getSwitch(type)) {
			
			com.stars.network.server.packet.PacketManager.send(roleId, new ClientText(rsv.getLockNotice()));
			
//			ServiceHelper.chatService().chat("系统", ChatManager.CHANNEL_PERSONAL, 0L, skyRankData.getRoleId(),
//					rsv.getLockNotice(), Boolean.TRUE);
			return false;
		}
		// 是否达到上限
		if (scoreRecord.getAddScoreTimes() >= rsv.getMaxTimes()) {
			PacketManager.send(roleId, new ClientText(rsv.getMaxtimeNotice()));
//			ServiceHelper.chatService().chat("系统", ChatManager.CHANNEL_PERSONAL, 0L, skyRankData.getRoleId(),
//					rsv.getMaxtimeNotice(), Boolean.TRUE);
			return false;
		}
		LogUtil.info("skyrank handleScore |" + roleId + "|" + skyRankRoleOp.getSkyRankData().getScore() + "|"
				+ skyRankRoleOp.getSkyRankData().getHideScore() + "|type" + type + "|isWin=" + isWin);
		scoreRecord.setAddScoreTimes(scoreRecord.getAddScoreTimes() + 1);
		skyRankData.setUpdateStatus();
		int oldScore = skyRankData.getScore();
		if (isWin == RESULT_LOSE) {
			skyRankRoleOp.reduceScore(getReduceScore(rsv, scoreRecord));
		} else {
			skyRankRoleOp.addScore(getAddScore(rsv, scoreRecord));
		}
		skyRankRoleOp.checkGradUp(oldScore, skyRankData.getScore());
		return true;
	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getFightScore() {
		return fightScore;
	}

	public void setFightScore(int fightScore) {
		this.fightScore = fightScore;
	}

}
