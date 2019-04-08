package com.stars.modules.guest.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.guest.GuestModule;
import com.stars.modules.guest.GuestPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.util.I18n;

/**
 * Created by zhouyaohui on 2017/1/3.
 */
public class ServerGuest extends PlayerPacket {

    private final static byte REQ_ACTIVE = 1;   // 激活门客
    private final static byte REQ_UPSTAR = 2;   // 升星
    private final static byte REQ_DISPATCH = 3; // 派遣
    private final static byte REQ_MISSION_INFO = 4; // 任务信息
    private final static byte REQ_FLUSH = 5;    // 刷新
    private final static byte REQ_GUEST_INFO = 6;   // 门客信息
    private final static byte REQ_MISSION_AWARD = 7;    // 领取奖励
    private final static byte REQ_EXCHANGE_ASK = 8; // 求助
    private final static byte REQ_EXCHANGE_GIVE = 9;    // 给予
    private final static byte REQ_EXCHANGE_INFO = 10;   // 交换信息

    private byte reqType;

    private int guestId;        // 门客id
    private int level;          // 等级
    private int missionId;      // 任务id
    private String guestGroup;  // 门客组合
    private String askClaim;    // 求助声明
    private int askCount;    // 求助碎片
    private long askId;     // 请求者的id
    private int askStamp;   // 请求的时间戳
    private int itemId;     // 请求碎片id
    private int index;      // 交换信息index

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        reqType = buff.readByte();
        switch (reqType) {
            case REQ_ACTIVE:
                guestId = buff.readInt();
                break;
            case REQ_UPSTAR:
                guestId = buff.readInt();
                break;
            case REQ_DISPATCH:
                missionId = buff.readInt();
                guestGroup = buff.readString();
                break;
            case REQ_MISSION_AWARD:
                missionId = buff.readInt();
                break;
            case REQ_EXCHANGE_ASK:
                guestId = buff.readInt();
                level = buff.readInt();
                askCount = buff.readInt();
                askClaim = buff.readString();
                break;
            case REQ_EXCHANGE_GIVE:
                askId = Long.valueOf(buff.readString());
                itemId = buff.readInt();
                askStamp = buff.readInt();
                break;
            case REQ_EXCHANGE_INFO:
                index = buff.readInt();
                break;
            default:
                PlayerUtil.send(getRoleId(), new ClientText(I18n.get("guest.req.error")));
        }
    }

    @Override
    public void execPacket(Player player) {
        GuestModule guestModule = module(MConst.Guest);
        switch (reqType) {
            case REQ_ACTIVE:
                guestModule.active(guestId);
                break;
            case REQ_UPSTAR:
                guestModule.upstar(guestId);
                break;
            case REQ_DISPATCH:
                guestModule.dispatch(missionId, guestGroup);
                break;
            case REQ_MISSION_INFO:
                guestModule.missionInfo();
                break;
            case REQ_FLUSH:
                guestModule.flush();
                break;
            case REQ_GUEST_INFO:
                guestModule.guestInfo();
                break;
            case REQ_MISSION_AWARD:
                guestModule.award(missionId);
                break;
            case REQ_EXCHANGE_ASK:
                if (SpecialAccountManager.isSpecialAccount(player.id())){
                    com.stars.network.server.packet.PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
                    return;
                }
                guestModule.exchangeAsk(guestId, level, askCount, askClaim);
                break;
            case REQ_EXCHANGE_GIVE:
                if (SpecialAccountManager.isSpecialAccount(player.id())){
                    com.stars.network.server.packet.PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
                    return;
                }
                guestModule.exchangeGive(askId, itemId, askStamp);
                break;
            case REQ_EXCHANGE_INFO:
                if (SpecialAccountManager.isSpecialAccount(player.id())){
                    PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
                    return;
                }
                guestModule.exchangeInfo(index);
                break;
        }
    }

    @Override
    public short getType() {
        return GuestPacketSet.S_GUEST;
    }
}
