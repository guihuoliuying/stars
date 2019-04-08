package com.stars.modules.runeDungeon;

import com.stars.modules.runeDungeon.proData.RuneDungeonVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuneDungeonManager {
	
	public static List<RuneDungeonVo> runeDungeonList = new ArrayList<>();
	
	public static Map<Integer, RuneDungeonVo> runeDungeonMap = new HashMap<Integer, RuneDungeonVo>();
	
	public static int Boss_Buff = 0;//强化buff  叠加层数和怒气等级相等
	
	public static int RelaxTime = 0;//助战冷却时间
	
	public static int HelpAwardLimit = 0;//好友助战奖励收益限制
	
	public static final byte SINGLE_PLAY = 0;
	
	public static final byte TEAM_PLAY = 1;
	
	public static final byte STATE_COOLING = 1;

	public static final byte STATE_FREE = 0;
	
	/*客户端上传*/
	public static final byte REQ_UI_INFO = 1;//请求玩法界面信息

	public static final byte SELECT_UPDATE_MAIN_UI = 2;//选取某副本更新界面信息

	public static final byte REQ_SELECT_UI_INFO = 3;//请求切换副本界面信息
	
	public static final byte REQ_START_FIGHT = 4;//请求进入战斗
	
	public static final byte RESET_DUNGEON = 5;//重置难度（怒气、轮数）
	
	public static final byte GET_HELP_REWARD = 6;//领取助战奖励
	
	public static final byte REQ_HELP_AWARD_INFO = 7;//助战奖励界面信息
	
	/*服务下发*/
	public static final byte SEND_UI_INFO = 1;//下发界面数据
	
	public static final byte SEND_DUNGEON_INFO = 2;//副本信息
	
	public static final byte FIGHT_END = 3;//战斗结算
	
	public static final byte HELP_AWARD_UI = 4;//助战奖励界面数据

	public static final byte NOTICE_NEXT = 5;//通知开启下一级副本

}
