package com.stars.modules.raffle.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.modules.raffle.define.RaffleDefineManager;
import com.stars.modules.raffle.define.RaffleRewardEntry;
import com.stars.modules.raffle.define.Range;
import com.stars.modules.raffle.helper.RaffleHelper;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.List;

public class RoleRaffleInfo extends DbRow {

	private static final long serialVersionUID = -5232306136247587049L;

	private static final int DAILY_TIMES = RaffleDefineManager.instance.getCommonConfig().dailyTimes;
	private static final int TIMES = RaffleDefineManager.instance.getCommonConfig().times;

	// 实例区
	private long roleId;
	private int rewardIndex;// 本轮奖励组
	private int userTimes;// 本轮使用抽奖次数
	private int position;// 当前位置
	private int totalMoney;// 总钻石(总钻石=累计+初始)
	private int dailyUserTimes;// 今天的总使用次数
	private List<Integer> speed;
	private int lastSuperPosition; //上次十连抽未领取奖励位置
	private int lastSuperRewardIndex; //上次十连抽奖励索引

	// 内存数据,重登会重置慎用
	private int preposition;

	// =================== creator =========================

	public RoleRaffleInfo() {

	}

	public RoleRaffleInfo(long roleId) {
		this.roleId = roleId;
	}
	// ==================== Getter And Setter ==============================

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public int getUserTimes() {
		return userTimes;
	}
	public void setUserTimes(int userTimes) {
		this.userTimes = userTimes;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public int getTotalMoney() {
		return totalMoney;
	}
	public void setTotalMoney(int totalMoney) {
		this.totalMoney = totalMoney;
	}
	public int getDailyUserTimes() {
		return dailyUserTimes;
	}
	public void setDailyUserTimes(int dailyUserTimes) {
		this.dailyUserTimes = dailyUserTimes;
	}

	public String getSpeed() {
		StringBuffer sb = new StringBuffer();
		for (int i : speed) {
			sb.append(i).append(",");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}
	public List<Integer> getSpeedList() {
		return speed;
	}

	public void setSpeed(String speedContent) {
		List<Integer> list = null;
		try {
			list = StringUtil.toArrayList(speedContent, Integer.class, ',');
		} catch (Exception e) {
			// 吞掉报错
			LogUtil.error("RoleRaffleInfo setSpeed is error!");
		}
		this.speed = list;
	}

	public int getRewardIndex() {
		return rewardIndex;
	}

	public void setRewardIndex(int rewardIndex) {
		this.rewardIndex = rewardIndex;
	}

	public int getPreposition() {
		return preposition;
	}

	public int getDailyLeftTimes() {
		int left = DAILY_TIMES - dailyUserTimes;
		return Math.max(0, left);
	}

	public int getLastSuperPosition() {
		return lastSuperPosition;
	}

	public void setLastSuperPosition(int lastSuperPosition) {
		this.lastSuperPosition = lastSuperPosition;
	}

	public int getLastSuperRewardIndex() {
		return lastSuperRewardIndex;
	}

	public void setLastSuperRewardIndex(int lastSuperRewardIndex) {
		this.lastSuperRewardIndex = lastSuperRewardIndex;
	}

	// ============================= Operate ===============================

	// 初始化

	public void init(int vipLevel) {
		this.dailyUserTimes = 0;
		this.userTimes = 0;
		this.position = 0;
		this.preposition = 0;
		this.rewardIndex = RaffleHelper.mapReffleRewardGroup(vipLevel);
		this.speed = RaffleHelper.createSpeed(rewardIndex);
		this.totalMoney = RaffleDefineManager.instance.getCommonConfig().originMoney;
	}

	// 轮重置

	public void reset(int vipLevel, int getMoney) {
		this.userTimes = 0;
		this.position = 0;
		this.preposition = 0;
		this.rewardIndex = RaffleHelper.mapReffleRewardGroup(vipLevel);
		this.speed = RaffleHelper.createSpeed(rewardIndex);
		this.totalMoney -= getMoney;
	}

	// 日重置

	public void dailyReset() {
		this.dailyUserTimes = 0;
	}

	// 周重置

	public void weekReset() {
		this.totalMoney = RaffleDefineManager.instance.getCommonConfig().originMoney;
	}

	public void incUserTimes() {
		this.userTimes++;
	}

	public void incDailyUserTimes() {
		this.dailyUserTimes++;
	}

	public void incTotalMomey(int increment) {
		this.totalMoney += increment;
	}

	public void incPosition(int increment) {
		this.position += increment;
	}

	// 在抽奖操作时内存数据的更新
	public void onRaffle() {
		// 更新前一个位置
		this.preposition = position;
		// 累计钻石
		RaffleRewardEntry rewardEntry = RaffleDefineManager.instance.getRewardEntry(rewardIndex, preposition);
		if (rewardEntry == null) {
			return;
		}
		Range range = rewardEntry.getMoneyRange();
		this.totalMoney += RaffleHelper.getRandomMoney(range);
		// 更新位置
		int incPosition = speed.get(userTimes);
		this.position += incPosition;
		// 更新使用次数
		incUserTimes();
		incDailyUserTimes();

	}

	public boolean validTimes() {
		if (userTimes >= TIMES) {
			return false;
		}
		if (dailyUserTimes >= DAILY_TIMES) {
			return false;
		}
		return true;
	}

	public boolean validateTenTimes(){
		if(dailyUserTimes+10 > DAILY_TIMES){ //只需要检测每日次数是否不足
			return false;
		}
		return true;
	}

	public boolean isEnd() {
		return userTimes >= TIMES;
	}

	@Override
	public String getChangeSql() {
		return SqlUtil.getSql(this, DBUtil.DB_USER, "roleraffleinfo", "`roleid`=" + roleId);
	}
	@Override
	public String getDeleteSql() {
		return SqlUtil.getSql(this, DBUtil.DB_USER, "roleraffleinfo", "`roleid`=" + roleId);
	}

}
