package com.stars.modules.skyrank.prodata;

import com.stars.modules.data.DataManager;
import com.stars.modules.skyrank.SkyRankManager;
import com.stars.util.MapUtil;

public class SkyRankConfig {
	public static SkyRankConfig config = new SkyRankConfig();
	
	public int MAX_RANK = 100;//排行榜显示的最大数量
	
	public int AWARD_MAX_RANK = 200;//排行榜奖励的最大数量
	
	public int GRAD_AWARD_MAIL = 30001;//赛季段位奖励的邮件模板
	
	public int RANK_AWARD_MAIL = 30002;//排行榜奖励的 邮件模板
	
	public int KING_REQ_SCORE = 100;//王者入门分数
	
	public int screennotice_lower = 0;
	public int screennotice_upper = 0;
	public int skyrank_rank_specialskyrankgradid = 0;
	
	public int rankreward_screennotice_interval = 1000;
	public int rankreward_screennotice_begin = 1000;
	public int rankreward_screennotice_end = 1000;
	
	public String skyrank_screennotice_rewardsendtime ="";
	public String skyrank_screennotice_ranknotice ="";
	
	public void init(){
//		skyrank_rankserial_screennotice_rankrange	字符串，需要提示的排名段，格式为：下限+上限
//		skyrank_rank_specialskyrankgradid	skyrankgradid，表示上榜段位
//		skyrank_rankreward_screennotice_time	整值，表示赛季奖励跑马灯发送的时间参数，格式为：间隔时间，开始时间点+技术时间点
//		时间点是指距离奖励发送时间点的前x秒
		
		String skyrank_rankserial_screennotice_rankrange[] = com.stars.util.MapUtil.getString(DataManager.commonConfigMap,
				"skyrank_rankserial_screennotice_rankrange", "1+100").split("\\+");
		 screennotice_upper = Integer.parseInt(skyrank_rankserial_screennotice_rankrange[0]);
		 screennotice_lower = Integer.parseInt(skyrank_rankserial_screennotice_rankrange[1]);

		skyrank_rank_specialskyrankgradid = com.stars.util.MapUtil.getInt(DataManager.commonConfigMap, "skyrank_rank_specialskyrankgradid", 2);
		
		KING_REQ_SCORE = SkyRankManager.getManager().getSkyRankGradById(skyrank_rank_specialskyrankgradid).getReqscore();
		 
		skyrank_screennotice_rewardsendtime = com.stars.util.MapUtil.getString(DataManager.commonConfigMap, "skyrank_screennotice_rewardsendtime", "还有%s分%s秒发送赛季奖励，请做好准备");
		
		skyrank_screennotice_ranknotice = com.stars.util.MapUtil.getString(DataManager.commonConfigMap, "skyrank_screennotice_ranknotice", "恭喜玩家%s通过不懈努力，冲上了天梯排行榜第%s名");
		
		String[] skyrank_rankreward_screennotice_time = MapUtil.getString(DataManager.commonConfigMap, "skyrank_rankreward_screennotice_time", "10,2000+1000").split(",");
		
		rankreward_screennotice_interval = Integer.parseInt(skyrank_rankreward_screennotice_time[0]);
		String[] tmpStr = skyrank_rankreward_screennotice_time[1].split("\\+");
		rankreward_screennotice_begin = Integer.parseInt(tmpStr[0]);
		rankreward_screennotice_end = Integer.parseInt(tmpStr[1]);
	}
	
}
