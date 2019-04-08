package com.stars.multiserver.familywar;

import com.stars.multiserver.familywar.flow.FamilyWarFlowInfo;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/11/24.
 */
public class FamilyWarConst {

    public static int STEP_OF_GENERAL_FLOW = 0;//总赛程步数
    public static int STEP_OF_SUB_FLOW = 0;//本服子赛程步数
    public static int STEP_OF_SUB_QUALIFYING_FLOW = 0;//跨服海选子赛程步数
    public static int STEP_OF_SUB_REMOTE_FLOW = 0;//跨服决赛子赛程步数

    public static int battleType = 0;
    public static byte qualificationState = FamilyWarConst.waitQulification;//取资格的状态

    public static int remoteType = 0;

    public static int familyCount = 0;

    public static final int W_TYPE_LOCAL = 1; // 本服赛
    public static final int W_TYPE_QUALIFYING = 2; // 海选赛
    public static final int W_TYPE_REMOTE = 3; // 跨服赛

    //    public static final byte WAR_NOT_START = 0;//未开始
//    public static final byte WAR_START_WITHOUT_QUALIFY = 1;//开始后取资格前
//    public static final byte WAR_START_WITH_QUALIFY = 2;//开始后取资格后
//    public static final byte WAR_END = 3;//已完成
    public static final byte SHOW_APPLY_BUTTON = 1;
    public static final byte DISAPPEAR_APPLY_BUTTON = 0;
    public static Map<Integer, FamilyWarFlowInfo> FAMILY_WAR_FLOW_INFO_MAP;//各流程状态

//    public static final int W_STATE_START = 0; // 周期开始
//    public static final int W_STATE_LOCAL = 1; // 本服赛
//    public static final int W_STATE_QUALIFYING = 2; // 海选赛
//    public static final int W_STATE_REMOTE = 3; // 跨服赛
//    public static final int W_STATE_END = 4; // 周期结束

    public static final String[] K_SEQ_MARK = new String[]{
            "M", "N",
            "I", "J", "K", "L",
            "A", "B", "C", "D", "E", "F", "G", "H",};

    public static final int K_SEQ_QUARTER_A = 6; // 13
    public static final int K_SEQ_QUARTER_B = 7; // 12
    public static final int K_SEQ_QUARTER_C = 8; // 11
    public static final int K_SEQ_QUARTER_D = 9; // 10
    public static final int K_SEQ_QUARTER_E = 10; // 9
    public static final int K_SEQ_QUARTER_F = 11; // 8
    public static final int K_SEQ_QUARTER_G = 12; // 7
    public static final int K_SEQ_QUARTER_H = 13; // 6

    public static final int K_SEQ_SEMI_I = 2; // 5
    public static final int K_SEQ_SEMI_J = 3; // 4
    public static final int K_SEQ_SEMI_K = 4; // 3
    public static final int K_SEQ_SEMI_L = 5; // 2

    public static final int K_SEQ_FINAL_M = 0; // 1
    public static final int K_SEQ_FINAL_N = 1; // 0

    public static final int K_SEQ_FINAL_34_O = 14;
    public static final int K_SEQ_FINAL_34_P = 15;

    public static final int K_BATTLE_FINAL_34_OP = 7;
    public static final int K_BATTLE_QUARTER_AB = 3;
    public static final int K_BATTLE_QUARTER_CD = 4;
    public static final int K_BATTLE_QUARTER_EF = 5;
    public static final int K_BATTLE_QUARTER_GH = 6;
    public static final int K_BATTLE_SEMI_IJ = 1;
    public static final int K_BATTLE_SEMI_KL = 2;
    public static final int K_BATTLE_FINAL_MN = 0;

    public static final int K_BATTLE_TYPE_QUARTER = 1;//四分之一决赛
    public static final int K_BATTLE_TYPE_SEMI = 2;//二分之一决赛
    public static final int K_BATTLE_TYPE_FINAL = 3;//一二名决赛
    public static final int K_BATTLE_TYPE_FINAL_3RD4TH = 4; // 3、4名决赛

    public static final int Q_BATTLE_TYPE_1ST = 1;//海选赛第一天
    public static final int Q_BATTLE_TYPE_2ND = 2;//海选赛第一天
    public static final int Q_BATTLE_TYPE_3RD = 3;//海选赛第一天
    public static final int Q_BATTLE_TYPE_4TH = 4;//海选赛第一天
    public static final int Q_BATTLE_TYPE_5Th = 5;//海选赛第一天

    public static final int R_BATTLE_TYPE_INIT = 0;//
    public static final int R_BATTLE_TYPE_32TO16 = 32;//32进16
    public static final int R_BATTLE_TYPE_16TO8 = 16;//16进8
    public static final int R_BATTLE_TYPE_8TO4 = 8;//8进4
    public static final int R_BATTLE_TYPE_4TO2 = 4;//4进2
    public static final int R_BATTLE_TYPE_3RD4TH = 2;
    public static final int R_BATTLE_TYPE_FINAL = 1;//决赛
    public static final int R_BATTLE_TYPE_OVER = -1;//

    public static final int R_BATTLE_GROUP_A = 1;
    public static final int R_BATTLE_GROUP_B = 2;
    public static final int R_BATTLE_GROUP_C = 3;
    public static final int R_BATTLE_GROUP_D = 4;

    public static final int K_FAMILY_STATE_RANKING_1 = 0; // 第一名
    public static final int K_FAMILY_STATE_RANKING_2 = 1; // 第二名
    public static final int K_FAMILY_STATE_RANKING_3 = 2; // 第三名
    public static final int K_FAMILY_STATE_RANKING_4 = 3; // 第四名
    public static final int K_FAMILY_STATE_QUARTER_FAILED = 4; // 四分之一决赛败者
    public static final int K_FAMILY_STATE_QUARTER_FINAL = 4; // 四分之一决赛资格
    public static final int K_FAMILY_STATE_SEMI_FINAL = 4; // 二分之一决赛资格
    public static final int K_FAMILY_STATE_3RD4TH_FINAL = 4; // 三四名决赛资格
    public static final int K_FAMILY_STATE_FINAL = 4; // 一二名决赛资格

    public static final byte K_MEMBER_NOT_JOINED = 0; // 没参加（怎么才会有参加）
    public static final byte K_MEMBER_JOINED = 0; // 有参加

    public static final byte K_CAMP1 = 1; // 阵营1
    public static final byte K_CAMP2 = 2; // 阵营2

    public static final byte K_TOWER_TYPE_CRYSTAL = 0; // 水晶
    public static final byte K_TOWER_TYPE_TOP = 1; // 上塔
    public static final byte K_TOWER_TYPE_MID = 2; // 中塔
    public static final byte K_TOWER_TYPE_BOT = 3; // 下塔
    public static final byte K_TOWER_TYPE_BASETOP = 4;//基地上塔
    public static final byte K_TOWER_TYPE_BASEBOT = 5;//基地下塔

    public static final byte K_TOWER_CATEGORY_OUTER = 1;//外塔
    public static final byte K_TOWER_CATEGORY_INNER = 2;//基地塔

    public static final byte K_APP_QUAL_NORMAL = 0; // 匹配战资格
    public static final byte K_APP_QUAL_ELITE = 1; // 精英战资格

    public static final byte K_SELF_QUAL_MASTER = 0; // 族长
    public static final byte K_SELF_QUAL_NOT_APPLIED = 1; // 未报名
    public static final byte K_SELF_QUAL_APPLIED = 2; // 已报名

//    public static final byte K_CAMP1_CRYSTAL = 0;
//    public static final byte K_CAMP1_TOP = 1;
//    public static final byte K_CAMP1_MID = 2;
//    public static final byte K_CAMP1_BOT = 3;
//    public static final byte K_CAMP2_CRYSTAL = 4;
//    public static final byte K_CAMP2_TOP = 5;
//    public static final byte K_CAMP2_MID = 6;
//    public static final byte K_CAMP2_BOT = 7;

    //个人积分奖励类型
    public static final byte MIN_AWARD_ELITE = 0;//本服精英
    public static final byte MIN_AWARD_NORMAL = 1;//本服匹配
    public static final byte MIN_AWARD_QUALIFYING_ELITE = 2;//跨服海选精英
    public static final byte MIN_AWARD_QUALIFYING_NORMAL = 3;//跨服海选匹配
    public static final byte MIN_AWARD_REMOTE_ELITE = 4;//跨服决赛精英
    public static final byte MIN_AWARd_REMOTE_NORMAL = 5;//跨服决赛匹配

    public static final int RANK_AWARD_OBJ_TYPE_MASTER = 1; // 族长
    public static final int RANK_AWARD_OBJ_TYPE_FIGHTER = 2; // 精英赛参战成员
    public static final int RANK_AWARD_OBJ_TYPE_MEMBER = 3; // 匹配赛参战成员

    public static final int KNOCKOUT_FAMILY_COUNT = 8;//参加家族战的家族数量
    public static final int QUALIFYING_FAMILY_COUNT = 24;//参加跨服海选的家族数量
    public static final int GROUP_FAMILY_COUNT = 6;//每小组家族数量

    /* 主界面icon状态 */
    public static final int STATE_START = 1;//开始，显示图标，倒计时8进4开始时间			2000-2
    public static final int STATE_UNDERWAY = 2;//进行中							2000-3
    public static final int STATE_END = 3;//赛事结束，						2000-9
    public static final int STATE_ICON_DISAPPEAR = 4;//图标消失
    public static final int STATE_NOTICE_MASTER = 5;//通知族长设置精英名单

    /* 场景类型 */
    public static final byte SCENE_TYPE_ALL = 1;        //所有场景
    public static final byte SCENE_TYPE_SAFE = 2;        //安全区
    public static final byte SCENE_TYPE_FAMILY_WAR = 3;    //家族战区域
    public static final byte SCENE_TYPE_NOT_FIGHT_SCENE = 4;//非战斗场景

    /* 家族参赛资格 */
    public static final byte WITHOUT_QUALIFICATION = 0;    //没资格
    public static final byte WITH_QUALIFICATION = 1;    //有资格

    public static final int WarTypeElite = 1;
    public static final int WarTypeNormal = 2;
    public static final int WarTypeStage = 3;
    public static final byte win = 1;
    public static final byte none = 0;
    public static final byte lose = -1;
    public static final long winSorce = 3;//跨服海选胜利后增加积分
    public static final int play = 1;
    public static final int AI = 0;
    public static final int eliteWarLog = 2;
    public static final int normalWarLog = 1;
    public static final int successLog = 1;
    public static final int failLog = 1;

    public static final int WIN_BY_CRYSTAL_DEAD = 0;//爆水晶
    public static final int WIN_BY_TOWER_HP = 1;//塔血量
    public static final int WIN_BY_CAMP_MORALE = 2;//双方士气对比
    public static final int WIN_BY_CAMP_FIGHTSCORE = 3;//双方战力对比
    public static final int WIN_BY_RANDOM = 4;//随机(camp1 胜利)

    public static final byte havQulification = 1;
    public static final byte noneQulification = 0;
    public static final byte waitQulification = -1;
    public static boolean openAI = true;
}
