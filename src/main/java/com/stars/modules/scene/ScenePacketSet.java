package com.stars.modules.scene;

import com.stars.modules.scene.packet.*;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterFight;
import com.stars.modules.scene.packet.fightSync.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyuheng on 2016/7/19.
 */
public class ScenePacketSet extends PacketSet {
    // 进入战斗
    public static short S_ENTERFIGHT = 0x7B00;
    public static short C_ENTERFIGHT = 0x7B01;
    // 怪物死亡 请求
    public static short S_MONSTERDEAD = 0x7B02;
    // 关卡进行中刷怪数据 下发
    public static short C_SPAWNMONSTER = 0x7B03;
    // 区域触发刷怪 请求
    public static short S_AREASPAWN = 0x7B04;
    // 关卡完成状态(当胜利/失败条件达成才下发)
    public static short C_STAGEFINISH = 0x7B05;
    // 角色死亡
    public static short S_ROLEDEAD = 0x7B06;
    // 进入城镇场景/安全区传送
    public static short S_CITY_TRANSFER = 0x7B07;
    public static short C_CITY_TRANSFER = 0x7B08;
    // NPC对话
    public static short S_TALKWITH_NPC = 0x7B09;
    // 计时
    public static short S_PAUSE_TIME = 0x7B0A;// 暂停计时
    public static short S_START_TIME = 0x7B0B;// 开始/继续计时
    public static short C_FIGHT_TIME = 0x7B0C;// 计时响应
    public static short C_INFIGHT_CHANGE_ATTR = 0x7B0D; //战斗过程中更改玩家的属性;
    public static short C_SERVER_SKILL_MONSTERS = 0x7B0E; //服务器通知主动击杀怪物;
    public static short C_SERVER_CLEAR_CD = 0x7B0F; //服务器通知主动要求清除技能CD;
    public static short C_SERVER_ADD_BUFF = 0x7B10; //服务器通知主动要求添加BUFF;
    // 下发阵营数据
    public static short C_CAMPVO = 0x7B11;
    // 产出副本领取奖励
    public static short S_PRODUCEDUNGEON_REWARD = 0x7B12;
    // 客户端上传伤害包
    public static short S_FIGHTDAMANGE = 0x7B13;
    // 同步客户端属性包(血量)
    public static short C_SYNC_ATTR = 0x7B14;
    // 退出战斗
    public static short S_EXITFIGHT = 0x7B15;
    // 同步指令
    public static short S_SYNCORDER = 0x7B16;// 上传指令
    public static short C_SYNCORDER = 0x7B17;// 下发指令
    // 下发怪物掉落结果(组队副本使用)
    public static short C_MONSTER_DROP = 0x7B18;
    // 复活
    public static short S_REVIVE = 0x7B19;// 请求
    public static short C_REVIVE = 0x7B1A;// 结果响应
    // 修改指定role的属性;
    public static short C_CHANGEATTR = 0x7B1B;
    // 退出战斗(非本服发送过来的)
    public static short C_EXITFIGHT_BACK = 0x7B1C;
    public static short C_READYTO_EXIT_FIGHT = 0x7B1D;// 通知客户端准备离开战斗;
    // 自动战斗标记
    public static short S_AUTOFLAG = 0x7B1E;
    // 剧情相关
    public static short S_DRAMA = 0x7B1F;
    public static short C_DRAMA = 0x7B20;
    // 客户端收到刷怪包确认
    public static short S_SPAWN_MONSTER_CONFIRM = 0x7B21;
    // 服务端收到怪物死亡确认(单人pve)
    public static short C_MONSTER_DEAD_CONFIRM = 0x7B22;
    // 同步玩家当前血量(单人pve)
    public static short S_PLAYER_CURHP = 0x7B23;
    // 同步玩家情义副本积分
    public static short C_MARRY_BATTLE_SCORE = 0x7B24;


    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ServerEnterFight.class);
        al.add(ClientEnterFight.class);
        al.add(ServerMonsterDead.class);
        al.add(ClientSpawnMonster.class);
        al.add(ServerAreaSpawnMonster.class);
        al.add(ServerRoleDead.class);
        al.add(ServerEnterCity.class);
        al.add(ClientEnterCity.class);
        al.add(ServerTalkWithNpc.class);
        al.add(ServerPauseTime.class);
        al.add(ServerStartTime.class);
        al.add(ClientFightTime.class);
        al.add(ClientCampVo.class);
        al.add(ServerProduceDungeonReward.class);
        al.add(ServerFightDamage.class);
        al.add(ClientSyncAttr.class);
        al.add(ServerExitFight.class);
        al.add(ServerSyncOrder.class);
        al.add(ClientSyncOrder.class);
        al.add(ClientMonsterDrop.class);
        al.add(ServerRoleRevive.class);
        al.add(ClientRoleRevive.class);
        al.add(ClientChangeFightVoAttr.class);
        al.add(ServerExitFightBack.class);
        al.add(ServerAutoFlag.class);
        al.add(ServerDrama.class);
        al.add(ClientDrama.class);
        al.add(ServerSpawnMonsterConfirm.class);
        al.add(ClientMonsterDeadConfirm.class);
        al.add(ServerPlayerCurHp.class);
        al.add(ClientMarryBattleScore.class);
        return al;
    }
}
