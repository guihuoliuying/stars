package com.stars.multiserver.teamPVPGame;

import com.stars.multiserver.teamPVPGame.stepIns.*;
import com.stars.util.DateUtil;

import java.util.*;

public class TPGUtil {
	
	public static String TPGSTEP_WAIT = "wait";
	
	public static String TPGSTEP_SIGNUP = "signup";//报名
	
	public static String TPGSTEP_SCORE = "score";//积分赛
	
	public static String TPGSTEP_GROUP = "group";//小组赛
	
	public static String TPGSTEP_QUARTER = "quarter";//四强赛
	
	public static String TPGSTEP_CHAMPION = "champion";//冠军
	
	public static Map<String, Class <? extends AbstractTPGStep>>tpgStepInsClassMap = new HashMap<>();

	// 聊天窗提示发送者名字
	public static String chatNoticeSenderName = "比武判官";
	// 奖励邮件发送者名字
	public static String rewardEmailSenderName = "比武判官";
	// 活动类别
	public static byte TPG_LOACAL = 1;// 本服
	public static byte TPG_REMOTE = 2;// 跨服
	// 奖励类型
	public static int AWARD_LOCAL_SCORE = 1;// 本服积分赛
	public static int AWARD_LOCAL_GROUP = 2;// 本服小组赛
	public static int AWARD_LOCAL_QUARTER = 3;// 本服四强赛
	public static int AWARD_REMOTE_GROUP = 4;// 跨服小组赛
	public static int AWARD_REMOTE_QUARTER = 5;// 跨服四强赛
	// 本服组队pvp流程
	public static List<String> localTPGFlow = new LinkedList<>();
	// 跨服组队pvp流程
	public static List<String> remoteTPGFlow = new LinkedList<>();

	static{
		tpgStepInsClassMap.put(TPGSTEP_SIGNUP, SignupTPGStep.class);
		tpgStepInsClassMap.put(TPGSTEP_SCORE, ScoreTPGStep.class);
		tpgStepInsClassMap.put(TPGSTEP_GROUP, GroupTPGStep.class);
		tpgStepInsClassMap.put(TPGSTEP_QUARTER, QuarterTPGStep.class);
		tpgStepInsClassMap.put(TPGSTEP_CHAMPION, ChampionTPGStep.class);
		// 流程拼装需要保证顺序
		// 本服
		localTPGFlow.add(TPGUtil.TPGSTEP_SIGNUP);
		localTPGFlow.add(TPGUtil.TPGSTEP_SCORE);
		localTPGFlow.add(TPGUtil.TPGSTEP_GROUP);
		localTPGFlow.add(TPGUtil.TPGSTEP_QUARTER);
		localTPGFlow.add(TPGUtil.TPGSTEP_CHAMPION);
		// 跨服
		remoteTPGFlow.add(TPGUtil.TPGSTEP_GROUP);
		remoteTPGFlow.add(TPGUtil.TPGSTEP_QUARTER);
		remoteTPGFlow.add(TPGUtil.TPGSTEP_CHAMPION);
	}
	
	
	public static long weekTime2AbsolutTime(String config){
		//周时间转化成绝对时间
		//格式：周几+hh:mm:ss
		String[] ss = config.split("[+]");
		int start_dayOfWeek = Integer.parseInt(ss[0]);
		
		Date date = DateUtil.hourStrTimeToDateTime(ss[1]);
		int dayOfWeek = DateUtil.getChinaWeekDay();
		
		int disDay = start_dayOfWeek - dayOfWeek;
		disDay = disDay >= 0?disDay:7+disDay;

		return date.getTime() + disDay * DateUtil.DAY;
	}
}
