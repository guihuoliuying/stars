package com.stars.modules.familyactivities.invade.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.familyactivities.invade.FamilyInvadeModule;
import com.stars.modules.familyactivities.invade.FamilyInvadePacket;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

/**
 * Created by liuyuheng on 2016/10/21.
 */
public class ServerFamilyInvade extends PlayerPacket {
    private byte reqType;// 请求类型(子协议)

    public static final byte TRIGGER_FIGHT = 1;// 触发战斗
    public static final byte OPEN_AWARD_BOX = 2;// 开宝箱
    public static final byte REQ_RANK_LIST = 3;// 请求伤害排行榜
    public static final byte REQ_MONSTER_NPC = 4;// 请求当前存在的怪物npc
    public static final byte REQ_AWARD_BOX = 5;// 请求当前存在宝箱
    public static final byte QUIT_FROM_FIGHT = 6;// 从战斗中退出

    private int monsterNpcUId;// npc唯一Id
    private String awardBoxUId;// 宝箱唯一Id
    private float curPosX;// 当前x坐标
    private float curPosZ;// 当前z坐标

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", FamilyInvadePacket.S_INVADE));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        FamilyInvadeModule familyInvadeModule = module(MConst.FamilyActInvade);
        long familyId = familyInvadeModule.getFamilyId();
        if (familyId == 0)
            return;
        switch (reqType) {
            case TRIGGER_FIGHT:
                ServiceHelper.familyActInvadeService().triggerFight(familyId, getRoleId(), monsterNpcUId, curPosX, curPosZ);
                break;
            case OPEN_AWARD_BOX:
                familyInvadeModule.openAwardBox(awardBoxUId, curPosX, curPosZ);
                break;
            case REQ_RANK_LIST:
                ServiceHelper.familyActInvadeService().reqRankList(familyId, getRoleId());
                break;
            case REQ_MONSTER_NPC:
                ServiceHelper.familyActInvadeService().reqMonsterNpc(familyId, getRoleId());
                break;
            case REQ_AWARD_BOX:
                familyInvadeModule.reqAwardBox();
                break;
            case QUIT_FROM_FIGHT:
                ServiceHelper.familyActInvadeService().quitFromFight(familyId, getRoleId());
                break;
        }
    }

    @Override
    public short getType() {
        return FamilyInvadePacket.S_INVADE;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
        switch (reqType) {
            case TRIGGER_FIGHT:
                this.monsterNpcUId = buff.readInt();// npcId
                this.curPosX = Float.parseFloat(buff.readString());
                this.curPosZ = Float.parseFloat(buff.readString());
                break;
            case OPEN_AWARD_BOX:
                this.awardBoxUId = buff.readString();// awardBoxId
                this.curPosX = Float.parseFloat(buff.readString());
                this.curPosZ = Float.parseFloat(buff.readString());
                break;
        }
    }
}
