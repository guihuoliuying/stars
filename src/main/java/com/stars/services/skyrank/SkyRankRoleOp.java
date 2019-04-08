package com.stars.services.skyrank;

import com.stars.modules.skyrank.SkyRankManager;
import com.stars.modules.skyrank.event.SkyRankGradAwardEvent;
import com.stars.modules.skyrank.prodata.SkyRankGradVo;
import com.stars.modules.skyrank.userdata.SkyRankDataPo;
import com.stars.modules.skyrank.userdata.SkyRankScoreRecord;
import com.stars.services.ServiceHelper;

/**
 * 玩家积分操作对象
 * 
 * @author xieyuejun
 *
 */
public class SkyRankRoleOp {
	//上次的活跃时间，用于清除一下不活跃数据
	private long lastActivtyTime;

	private SkyRankDataPo skyRankData;

	public void active(){
		lastActivtyTime = System.currentTimeMillis();
	}
	
	public SkyRankRoleOp(SkyRankDataPo skyRankData) {
		active();
		this.skyRankData = skyRankData;
	}

	public SkyRankDataPo getSkyRankData() {
		return skyRankData;
	}

	public void setSkyRankData(SkyRankDataPo skyRankData) {
		this.skyRankData = skyRankData;
	}

	public int getScore() {
		return skyRankData.getScore();
	}

	/**
	 * 积分增加记录
	 * @param record
	 */
	public void addLadderScoreRecord(SkyRankScoreRecord record) {
		skyRankData.addLadderScoreRecord(record);
	}

	public SkyRankScoreRecord getSkyRankScoreRecord(short type) {
		return skyRankData.getScoreRecord().get(type);
	}

	/**
	 * 积分加
	 * @param addScore
	 * @return
	 */
	public boolean addScore(int addScore) {
		int oldScore = skyRankData.getScore();
		skyRankData.addScore(addScore);
		sendScoreToClient(oldScore, skyRankData.getScore());
		return true;
//		return checkGradUp(oldScore, skyRankData.getScore());
	}

	/**
	 * 扣积分
	 * @param reduceScore
	 * @return
	 */
	public boolean reduceScore(int reduceScore) {
		int oldScore = skyRankData.getScore();
		skyRankData.reduceScore(reduceScore);
		sendScoreToClient(oldScore, skyRankData.getScore());
		return true;
	}

	/**
	 * 检测段位是否下降
	 * 
	 * @param oldScore
	 * @param newScore
	 * @return
	 */
	public boolean checkGradDown(int oldScore, int newScore) {
		SkyRankGradVo oldRGV = SkyRankManager.getManager().getSkyRankGradVoByScore(oldScore);
		SkyRankGradVo newRGV = SkyRankManager.getManager().getSkyRankGradVoByScore(newScore);
		if (newRGV != oldRGV) {
			return true;
		}
		return false;
	}

	/**
	 * 检测段位是否升级
	 * 
	 * @param oldScore
	 * @param newScore
	 * @return
	 */
	public boolean checkGradUp(int oldScore, int newScore) {
		SkyRankGradVo oldRGV = SkyRankManager.getManager().getSkyRankGradVoByScore(oldScore);
		SkyRankGradVo newRGV = SkyRankManager.getManager().getSkyRankGradVoByScore(newScore);
		if (newRGV.getSkyRankGradId() != oldRGV.getSkyRankGradId()) {
			//隐藏分更新
			skyRankData.setHideScore(newRGV.getBufferScore());
			// 发送奖励
			if (skyRankData.containsUpAwardRecord(newRGV.getSkyRankGradId()))
				return false;
			skyRankData.addUpAwardRecord(newRGV.getSkyRankGradId());
			ServiceHelper.roleService().notice(this.getSkyRankData().getRoleId(), new SkyRankGradAwardEvent(newRGV));
			return true;
		}
		return false;
	}

    public void checkAwardWhileLogin(int score) {
        SkyRankGradVo vo = SkyRankManager.getManager().getSkyRankGradVoByScore(score);
        while (vo != null) {
            if (!skyRankData.containsUpAwardRecord(vo.getSkyRankGradId())) {
                skyRankData.addUpAwardRecord(vo.getSkyRankGradId());
                ServiceHelper.roleService().notice(this.getSkyRankData().getRoleId(), new SkyRankGradAwardEvent(vo));
            }
            vo = SkyRankManager.getManager().getSkyRankGradById(vo.getSkyRankGradId()-1);
        }
    }

	/**
	 * 同步积分到客户端
	 * @param oldScore
	 * @param newScore
	 */
	public void sendScoreToClient(int oldScore, int newScore) {
		if (oldScore == newScore)
			return;
		 ServiceHelper.skyRankLocalService().reqRoleScoreMsg(this.getSkyRankData().getRoleId(),this.skyRankData.getNewShowData());
	}

	public long getLastActivtyTime() {
		return lastActivtyTime;
	}

	public void setLastActivtyTime(long lastActivtyTime) {
		this.lastActivtyTime = lastActivtyTime;
	}
}
