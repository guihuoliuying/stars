package com.stars.modules.escort;

/**
 * Created by wuyuxing on 2016/12/7.
 */
public class EscortConstant {
    public static final byte ESCORT_TYPE_SINGLE = 0;    //单人运镖
    public static final byte ESCORT_TYPE_TEAM = 1;      //组队运镖

    public static final String CARGO_CAR_FIGHT_ID = "1";//镖车固定fightId

    //运镖阵营
    public static final byte CAMP_ESCORT = 11;
    public static final byte CAMP_ROB = 12;
    public static final byte CAMP_MONSTER = 13;

    //运镖结果
    public static final byte RESULT_DEAD = 1;           //战死
    public static final byte RESULT_ESCORT_FAIL = 2;    //护镖失败
    public static final byte RESULT_ROB_FAIL = 3;       //劫镖失败
    public static final byte RESULT_ESCORT_SUCCESS = 4; //护镖成功
    public static final byte RESULT_ROB_SUCCESS = 5;    //劫镖成功
    public static final byte RESULT_ESCORT_FINISH = 6;  //运镖结束
    public static final byte RESULT_PVE_ESCORT_FAIL = 7;  //运镖失败

    //活动次数扣除类型
    public static final byte SUB_TYPE_NONE = 0;             //无需扣除
    public static final byte SUB_TYPE_ESCORT_SUCCESS = 1;   //运镖成功,扣除运镖次数
    public static final byte SUB_TYPE_ROB_SUCCESS = 2;      //劫镖成功,扣除劫镖次数
    public static final byte SUB_TYPE_GUIDE_SUCCESS = 3;    //保镖运镖成功,不扣除劫镖次数

    //镖车状态
    public static final byte CARGO_STATUE_NORMAL = 0;   //正常
    public static final byte CARGO_STATUE_FIGHTING = 1; //战斗中
    public static final byte CARGO_STATUE_PROTECT = 2;  //保护中

    //镖车场景状态
    public static final byte SCENE_STATUS_NOT_BEGIN = 0;    //未开始
    public static final byte SCENE_STATUS_NORMAL = 1;       //进行中
    public static final byte SCENE_STATUS_PAUSE = 2;        //暂停
    public static final byte SCENE_STATUS_IN_PVP = 3;       //pvp中
    public static final byte SCENE_STATUS_FINISH = 4;       //结束
}
