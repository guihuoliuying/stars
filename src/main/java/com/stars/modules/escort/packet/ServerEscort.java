package com.stars.modules.escort.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.escort.EscortModule;
import com.stars.modules.escort.EscortPacketSet;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.ServiceHelper;
import com.stars.util.ServerLogConst;

/**
 * Created by wuyuxing on 2016/12/2.
 */
public class ServerEscort extends PlayerPacket {

    public static final byte REQ_VIEW_MAIN_ENTRY = 0x00;    // 请求运镖入口界面
    public static final byte REQ_VIEW_CARGO_SELECT = 0x01;  // 镖车选择界面
    public static final byte REQ_RESET_SELECT_LIST = 0x02;  // 请求金币刷新镖车选择列表
    public static final byte REQ_BEGIN_ESCORT = 0x03;       // 请求开始运镖
    public static final byte REQ_ENTER_CARGO_LIST_SCENE = 0x04;     // 请求进入镖车队列场景
    public static final byte REQ_UPDATE_CARGO_LIST_SCENE = 0x05;    // 请求刷新镖车队列场景
    public static final byte REQ_ROB_CARGO = 0x06;          // 请求进入运镖场景进行劫镖
    public static final byte REQ_ESCORT_CONTINUE = 0x07;    // 请求继续运镖
    public static final byte REQ_ROB_ROBOT = 0x08;          // 请求劫机器人镖车
    public static final byte REQ_ROB_FINISH_BACK_CITY = 0x09;   //劫镖结束请求返回场景(队列/安全区)
    public static final byte REQ_BACK_CITY_IN_SAFE_SCENE = 0x10;//镖车队列场景的掉线/回城处理
    public static final byte REQ_DISBAND_TEAM = 0x11;       // 请求解散队伍
    public static final byte REQ_ESCORT_FINISH_BACK_CITY = 0x12;       // 运镖结束请求回城

    private byte subtype;
    private byte escortType;   //押镖类型 0个人  1组队
    private int carId;
    private byte index;
    private int sectionId;
    private byte useMask;   //劫镖时是否使用面具
    private byte success;   //劫镖结果，1胜利 0失败

    @Override
    public void execPacket(Player player) {
        EscortModule escortModule = module(MConst.Escort);
        switch (subtype) {
            case REQ_VIEW_MAIN_ENTRY:
                escortModule.viewMainEntryUI();
                break;
            case REQ_VIEW_CARGO_SELECT:
                escortModule.viewCargoSelectUI();
                break;
            case REQ_RESET_SELECT_LIST:
                escortModule.reqResetCargoSelectList();
                break;
            case REQ_BEGIN_ESCORT:
                escortModule.beginEscort(escortType,index,carId);
                break;
            case REQ_ENTER_CARGO_LIST_SCENE:
//                escortModule.enterCargoListScene(escortType);
                break;
            case REQ_UPDATE_CARGO_LIST_SCENE:
//                ServiceHelper.escortService().updateCargoListScene(getRoleId(),index);
                break;
            case REQ_ROB_CARGO:
//                escortModule.robCargo(escortType,index,useMask);
                break;
            case REQ_ESCORT_CONTINUE:
//                ServiceHelper.escortService().escortContinue(getRoleId());
                break;
            case REQ_ROB_ROBOT:
//                escortModule.robRobotCargo(escortType, sectionId);
                break;
            case REQ_ROB_FINISH_BACK_CITY:
                if(success == 0) {
                    ServerLogModule serverLogModule = module(MConst.ServerLog);
                    serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_FAIL, ThemeType.ACTIVITY_24.getThemeId(),0);
                }
//                ServiceHelper.escortService().leaveSceneAfterRob(getRoleId(),success);
                break;
            case REQ_BACK_CITY_IN_SAFE_SCENE://镖车队列场景的掉线/回城处理
                ServiceHelper.escortService().handleOfflineInSafeScene(getRoleId());
                break;
            case REQ_DISBAND_TEAM:
                ServiceHelper.baseTeamService().disbandTeam(getRoleId());
                break;
            case REQ_ESCORT_FINISH_BACK_CITY:
                SceneModule sceneModule = module(MConst.Scene);
                sceneModule.backToCity(false);
                break;
        }
    }

    @Override
    public short getType() {
        return EscortPacketSet.S_ESCORT;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        switch (subtype) {
            case REQ_VIEW_MAIN_ENTRY:
                break;
            case REQ_VIEW_CARGO_SELECT:
                break;
            case REQ_RESET_SELECT_LIST:
                break;
            case REQ_BEGIN_ESCORT:
                escortType = buff.readByte();
                index = buff.readByte();
                carId = buff.readInt();
                break;
            case REQ_ENTER_CARGO_LIST_SCENE:
                escortType = buff.readByte();
                break;
            case REQ_UPDATE_CARGO_LIST_SCENE:
                index = buff.readByte();
                break;
            case REQ_ROB_CARGO:
                escortType = buff.readByte();
                index = buff.readByte();
                useMask = buff.readByte();
                break;
            case REQ_ROB_ROBOT:
                escortType = buff.readByte();
                sectionId = buff.readInt();
                break;
            case REQ_ROB_FINISH_BACK_CITY:
                success = buff.readByte();
                break;
            case REQ_BACK_CITY_IN_SAFE_SCENE:
                break;
            case REQ_ESCORT_FINISH_BACK_CITY:
                break;
        }
    }
}
