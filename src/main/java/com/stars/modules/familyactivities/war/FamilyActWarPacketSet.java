package com.stars.modules.familyactivities.war;

import com.stars.modules.familyactivities.war.packet.*;
import com.stars.modules.familyactivities.war.packet.fight.ClientFamilyWarBattleFightEliteResult;
import com.stars.modules.familyactivities.war.packet.fight.ClientFamilyWarBattleResult;
import com.stars.modules.familyactivities.war.packet.fight.ClientFamilyWarFightNormalResult;
import com.stars.modules.familyactivities.war.packet.fight.ClientFamilyWarFightStageResult;
import com.stars.modules.familyactivities.war.packet.ui.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * (short) 0x61B0, (short) 0x61DF
 * Created by zhaowenshuo on 2016/11/22.
 */
public class FamilyActWarPacketSet extends PacketSet {

    //0x61B0-0x61DF

    // UI相关
    public static final short S_FAMILY_WAR_UI_MAIN = 0x61B0; // 主界面(流程状态)
    public static final short C_FAMILY_WAR_UI_MAIN = 0x61B1; // 主界面(流程状态)
    public static final short S_FAMILY_WAR_UI_RULES = 0x61B2; // 规则
    public static final short C_FAMILY_WAR_UI_RULES = 0x61B3; // 规则
    public static final short S_FAMILY_WAR_UI_APPLY = 0x61B4; // 报名相关
    public static final short C_FAMILY_WAR_UI_APPLY = 0x61B5; // 报名相关
    public static final short S_FAMILY_WAR_UI_FIXTURES = 0x61B6; // 赛程（赛事状态，家族信息，对阵表，冠亚季殿，个人资格）
    public static final short C_FAMILY_WAR_UI_FIXTURES = 0x61B7; // 赛程（赛事状态，家族信息，对阵表，冠亚季殿，个人资格）
    public static final short S_FAMILY_WAR_UI_POINTS_RANK = 0x61B8; // 积分排位（精英战个人，匹配战个人，海选家族（包括资格展示），海选个人，以及对应的个人/家族积分和排位）
    public static final short C_FAMILY_WAR_UI_POINTS_RANK = 0x61B9; // 积分排位（精英战个人，匹配战个人，海选家族（包括资格展示），海选个人，以及对应的个人/家族积分和排位）
    public static final short S_FAMILY_WAR_UI_MIN_POINTS_AWARD = 0x61BA; // 个人积分达标奖励（最低积分奖励）
    public static final short C_FAMILY_WAR_UI_MIN_POINTS_AWARD = 0x61BB; // 个人积分达标奖励（最低积分奖励）
    public static final short S_FAMILY_WAR_UI_SUPPORT = 0x61BC;    //点赞（请求）
    public static final short C_FAMILY_WAR_UI_SUPPORT = 0x61BD; //点赞（回应）

    public static final short S_FAMILY_WAR_UI_ENTER = 0x61BE; // 进入
    public static final short C_FAMILY_WAR_UI_ENTER = 0x61BF; // 进入

    // 战斗相关
    public static final short C_FAMILY_WAR_BATTLE_FIGHT_INIT_INFO = 0x61C0; // 初始信息（塔位置，塔血量，塔最大血量，塔uid）
    public static final short C_FAMILY_WAR_BATTLE_FIGHT_UPDATE_INFO = 0x61C1; // 塔的更新信息（塔血量，士气）
    public static final short C_FAMILY_WAR_BATTLE_FIGHT_KILL_COUNT = 0x61C2; // 个人连杀计数
    public static final short C_FAMILY_WAR_BATTLE_FIGHT_PERSONAL_POINTS = 0x61C3; // 个人积分
    public static final short S_FAMILY_WAR_BATTLE_FIGHT_REVIVE = 0x61C4; // 复活（请求）
    public static final short C_FAMILY_WAR_BATTLE_FIGHT_REVIVE = 0x61C5; // 复活（次数）
    public static final short S_FAMILY_WAR_BATTLE_FIGHT_DIRECT = 0x61C6; // 指挥（阵营，塔类型）
    public static final short C_FAMILY_WAR_BATTLE_FIGHT_DIRECT = 0x61C7; // 指挥（roleId，roleName，阵营，塔类型）
    public static final short C_FAMILY_WAR_BATTLE_FIGHT_ELITE_RESULT = 0x61C8; // 精英战结算界面
    public static final short C_FAMILY_WAR_BATTLE_FIGHT_NORMAL_RESULT = 0x61C9; // 匹配战结算界面

    public static final short C_FAMILY_WAR_FIGHT_NORMAL_OPPONENT_INFO = 0x61CA; // 对手信息界面
    //
    public static final short C_FAMILY_WAR_MAIN_ICON = 0x61CB;    //主界面图标
    public static final short C_FAMILY_WAR_BATTLE_FIGHT_BLOCK = 0x61CC;//动态阻挡
    public static final short C_FAMILY_WAR_BATTLE_FIGHT_STAGE_RESULT = 0x61CD;//匹配关卡结算界面
    public static final short C_FAMILY_WAR_BATTLE_FIGHT_START_TIPS = 0x61CE;//精英战场开启提示
    public static final short C_FAMILY_WAR_BATTLE_FIGHT_PRELOAD_FINISHED = 0x61CF;//匹配战场预加载完成

    public static final short S_FAMILY_WAR_ENTER_SAFE_SCENE = 0x61D0;//进入备战场景-上行
    public static final short C_FAMILY_WAR_ENTER_SAFE_SCENE = 0x61D1;//进入备战场景-下行
    public static final short C_FAMILY_WAR_BATTLE_RESULT = 0x61D2;//每一轮的结算界面
    public static final short C_FAMILY_WAR_BATTLE_STAT = 0x61D3;//战斗内个人战斗数据
    public static final short C_FAMILY_WAR_BATTLE_FAMILY_POINTS = 0x61D4;//战斗内家族积分

    public static final short C_FAMILY_WAR_POINTS_RANK = 0x61D5;//海选赛家族积分相关
    public static final short S_FAMILY_WAR_POINTS_RANK = 0x61D6;//海选赛家族积分相关

    public static final short C_FAMILY_RANK = 0x61D7;//
    public static final short S_FAMILY_RANK = 0x61D8;//

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        // UI相关
        list.add(ServerFamilyWarUiApply.class);
        list.add(ClientFamilyWarUiApply.class);
        list.add(ServerFamilyWarUiFixtures.class);
        list.add(ClientFamilyWarUiFixtures.class);
        list.add(ServerFamilyWarUiPointsRank.class);
        list.add(ClientFamilyWarUiPointsRank.class);
        list.add(ServerFamilyWarUiEnter.class);
        list.add(ClientFamilyWarUiEnter.class);
        list.add(ServerFamilyWarUiMinPointsAward.class);
        list.add(ClientFamilyWarUiMinPointsAward.class);
        list.add(ClientFamilyWarMainIcon.class);
        list.add(ServerFamilyWarUiSupport.class);
        list.add(ClientFamilyWarUiSupport.class);
        list.add(ClientFamilyWarBattleStartTips.class);
        list.add(ServerFamilyWarUiFlowInfo.class);
        list.add(ClientFamilyWarUiFlowInfo.class);
        list.add(ServerFamilyWarSafeSceneEnter.class);
        list.add(ClientFamilyWarSafeScene.class);
        list.add(ClientFamilyWarPointsRank.class);
        list.add(ServerFamilyWarPointsRank.class);

        // 战斗相关
        list.add(ClientFamilyWarBattleFightInitInfo.class);
        list.add(ClientFamilyWarBattleFightUpdateInfo.class);
        list.add(ClientFamilyWarBattleFightKillCount.class);
        list.add(ClientFamilyWarBattleFightPersonalPoint.class);
        list.add(ClientFamilyWarBattleStat.class);
        list.add(ClientFamilyWarBattleFamilyPoints.class);
        list.add(ServerFamilyWarBattleFightDirect.class);
        list.add(ClientFamilyWarBattleFightDirect.class);
        list.add(ClientFamilyWarBattleFightEliteResult.class);
        list.add(ClientFamilyWarFightNormalResult.class);
        list.add(ClientFamilyWarFightStageResult.class);
        list.add(ClientFamilyWarBattlePreloadFinished.class);
        list.add(ClientFamilyWarBattleResult.class);
        list.add(ServerFamilyRank.class);
        list.add(ClientFamilyRank.class);
        return list;
    }

}
