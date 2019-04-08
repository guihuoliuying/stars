package com.stars.modules.familyactivities.treasure.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.familyactivities.treasure.FamilyTreasureConst;
import com.stars.modules.familyactivities.treasure.FamilyTreasureModule;
import com.stars.modules.familyactivities.treasure.FamilyTreasurePacket;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.util.LogUtil;

/**
 * Created by chenkeyu on 2017/2/11 15:15
 */
public class ServerFamilyTreasure extends PlayerPacket {
    public static final byte view = 0x00;//打开页面
    public static final byte viewSunday = 0x01;//打开周日界面
    public static final byte challenge = 0x02;//挑战


    private byte subtype;
    private byte ftType;//NORMAL_TREASURE = 1; SUNDAY_TREASURE = 0;
    private int level;//level
    private int step;//step

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        switch (subtype) {
            case view:
                break;
            case viewSunday:
                break;
            case challenge:
                ftType = buff.readByte();
                if (ftType == (byte) FamilyTreasureConst.NORMAL_TREASURE) {
                    level = buff.readInt();
                    step = buff.readInt();
                }
            default:
                break;
        }
    }

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", FamilyTreasurePacket.S_TREASURE));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        ForeShowModule open = module(MConst.ForeShow);
        if (!open.isOpen(ForeShowConst.FAMILY)) {
            PacketManager.send(player.id(), new ClientText("您的等级过低，不能参加家族探宝活动"));
            return;
        }
        FamilyTreasureModule ftm = module(MConst.FamilyActTreasure);
        switch (subtype) {
            case view:
                ftm.view();
                break;
            case viewSunday:
                ftm.viewSunday();
                break;
            case challenge:
                if (ftType == FamilyTreasureConst.NORMAL_TREASURE) {
                    LogUtil.info("进入boss场景 等级:{} 步数:{}", level, step);
                    ftm.enterFamilyTreauserScene(level, step);
                } else {
                    ftm.enterFTSundayScene();
                }
            default:
                break;
        }
    }

    @Override
    public short getType() {
        return FamilyTreasurePacket.S_TREASURE;
    }


}
