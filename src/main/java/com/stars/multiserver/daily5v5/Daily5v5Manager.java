package com.stars.multiserver.daily5v5;

import com.stars.multiserver.daily5v5.data.Daily5v5MoraleVo;
import com.stars.multiserver.daily5v5.data.FivePvpMerge;
import com.stars.multiserver.daily5v5.data.MatchFloat;
import com.stars.multiserver.daily5v5.data.PvpExtraEffect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Daily5v5Manager {
	
	public static Map<Integer, Integer> fixMap = new HashMap<>();//段位分修正值信息      key:连胜）   value：百分比值
	
	public static Map<Byte, List<MatchFloat>> matchFloatMap = new HashMap<>();//匹配浮动值
	
	public static Map<Integer, FivePvpMerge> pvpMergeMap = new HashMap<>();//属性装配
	
	public static Map<Integer, PvpExtraEffect> pvpExtraEffectMap = new HashMap<>();//pvp额外属性   （vip）
	
	public static Map<Integer, Integer> effectMaxLevel = new HashMap<>();
	
	public static Map<Integer, List<PvpExtraEffect>> effectTypeMap = new HashMap<>();
	
	public static Map<Integer, Integer> reliveTimeMap = new HashMap<>(); 
	
	public static List<Daily5v5MoraleVo> moraleVoList;
	
	public static Map<Integer, String> dayFlowMap = new HashMap<>();
	
	public static int[] ActFlow = null;
	
//	public static int[] FinalReward = null; 
	public static int[] highReward = null;//高收益 
	public static int[] lowReward = null;//低收益
	public static int[] blankReward = null;//无收益
	
	public static int[] gainsCounts = null;//收益次数    gainsCounts[0] 高收益次数 ，gainsCounts[1] 低收益次数
	
	public static int WIN_STANDARD = 5;//最大修正连胜值
	
	public static int LOSE_STANDARD = 5;//最大修正连败值
	
	public static int MAX_STEP = 3;//扩展次数上限
	
	public static int SingleMatchTime = 1000;//秒
	
	public static int TEAM_MATCHING_TIME = 2000;//秒
	
	public static byte ACT_OPEN = 1;

	public static byte ACT_CLOSE = 0;
	
	public static byte TEAM_MEMBER_NUM = 5;//队伍人数
	
	public static int STAGEID = 9421;
	
	public static int FIGHT_TIME_LIMIT = 5*60;
	
	public static int START_REMIND_TIME = 10;

	public static int FIX_TIME = 20;

	public static long TIPS_INTERVAL = 300_000; // 跑马灯间隔，5分钟
	
	/**
     * 动态阻挡时间(秒)
     */
    public static int DYNAMIC_BLOCK_TIME = 30;
	
	public static int timeLimitOfInitial = 0; // 初始阶段（展示玩家信息）
	
    public static int timeLimitOfPreparation = 3_000; // 客户端准备阶段（预加载）
    public static double paramValue = 10000.0;
    
    public static byte CAMP1 = 1;

    public static byte CAMP2 = 2;
    
    public static byte WIN_RESULT = 1;
    public static byte LOSE_RESULT = 2;
    
    public static final byte K_TOWER_TYPE_CRYSTAL = 0; // 水晶
    public static final byte K_TOWER_TYPE_TOP = 1; // 上塔
    public static final byte K_TOWER_TYPE_MID = 2; // 中塔
    public static final byte K_TOWER_TYPE_BOT = 3; // 下塔
    public static final byte K_TOWER_TYPE_BASETOP = 4;//基地上塔
    public static final byte K_TOWER_TYPE_BASEBOT = 5;//基地下塔
    
    public static final byte K_TOWER_CATEGORY_OUTER = 1;//外塔
    public static final byte K_TOWER_CATEGORY_INNER = 2;//基地塔
    
    /**
     * 无敌buffId
     */
    public static int invincibleBuffId = 999400;
    
    public static int ANGLE_PRAY = 201;//天使祝福
    public static int BUFF_ADD = 202;//buff增益
    public static int TOWER_SOUL = 203;//箭塔残魂
    public static int KILL_AND_ADD_HP = 204;//杀人饮血
    public static int HOME_GUARDIAN = 205;//家园守卫
    
    //战斗参数
    public static double DamagePercent = 1; //百分比的伤害临界值

    public static int[] Coefficient_A = null; //积分修正系数
    
    public static int pointsDeltaOfDestoryTower = 0;//对塔最后一击   获得积分
    
    public static int moraleDeltaOfKillFighterInEliteFight = 0;//敌方每死亡一个角色，本方队伍增加士气

    public static int moraleDeltaOfDestoryTower = 0;//敌方塔每损失一座，本方增加士气

    public static int moraleDeltaOfLosingTower = 0;//己方塔被破坏，损失士气

    public static int KillNotice = 0;//连杀监听标准
    
    public static Map<Integer, Byte> towerTypeMap = new HashMap<>();
    
//    public static byte Daily5v5TotalCount = 2;//总参与次数
    
    public static byte homeRevive = 0;
    
    public static int coefficient_hp = 0;
    public static int coefficient_attack = 0;
    public static int coefficient_defense = 0;
    public static int coefficient_hit = 0;
    public static int coefficient_avoid = 0;
    public static int coefficient_crit = 0;
    public static int coefficient_anticrit = 0;
    
    public static Daily5v5MoraleVo getMoraleVo(int morale){
    	for(Daily5v5MoraleVo vo : moraleVoList){
    		if (morale >= vo.getMinMorale() && morale <= vo.getMaxMorale()) {
                return vo;
            }
    	}
    	return null;
    }
    
	
	/* 服务下发协议 */
	public static final byte ACTIVITY_STATE = 1;//活动开启状态   控制icon
	
	public static final byte OPEN_UI_INFO = 2;//匹配界面信息
	
	public static final byte READY_FIGHT = 3;//准备战斗倒计时界面信息
	
	public static final byte CANCEL_SUCCESS = 4;//取消匹配成功
	
	public static final byte FIGHTING_END = 5;//战斗结算

	public static final byte CONTINUE_FIGHTING = 6;//战斗未结束是否重新进入战斗
	
	public static final byte USE_BUFF_RESULT = 7;//使用主动buff返回
	
	public static final byte RESP_JOIN_TIMES = 8;//活动参与次数返回
	
	public static final byte MATCHING_STATE = 9;//匹配状态
	
	public static final byte BUFF_CD_INFO = 10;//重进战场  下发主动buff CD
	
	public static final byte REVIVE_CD_INFO = 11;//重进战场  下发复活 CD
	
	/* 客户端上传协议 */
	public static final byte OPEN_UI = 1;//打开匹配界面
	
	public static final byte START_MATCHING = 2;//开始匹配
	
	public static final byte CANCEL_MATCHING = 3;//取消匹配

	public static final byte YES_FIGHTING = 4;//继续战斗

	public static final byte REQ_JOIN_TIMES = 5;//请求活动参与次数
	
}
