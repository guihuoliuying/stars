package com.stars.modules.familyactivities.bonfire.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.familyactivities.bonfire.FamilyBonfireModule;
import com.stars.modules.familyactivities.bonfire.FamilyBonfirePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.util.LogUtil;

/**
 * Created by zhouyaohui on 2016/10/9.
 */
public class ServerBonfire extends PlayerPacket {
    /** 常量 */
    public final static byte IDLE = 0;
    public final static byte GENERATE = 1;  // 生成灯笼
    public final static byte BREAK = 2;     // 打破灯笼
    public final static byte INIT = 3;      // 初始化篝火信息
    public final static byte UPDATE = 4;    // 刷新篝火信息
    public final static byte THROW_GOLD = 5;     // 投元宝
    public final static byte THROW_WOOD = 6;     // 投干柴
    public final static byte WOOD_PICK = 7;      // 捡干柴
    public final static byte ANSWER_QUESTION = 8;// 答题

    private byte reqType;
    private int bonfireId;
    private int questionId;
    private int questionIndex;

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", FamilyBonfirePacketSet.S_BONFIRE));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        FamilyBonfireModule fm = (FamilyBonfireModule) module(MConst.FamilyActBonfire);
        if (reqType == GENERATE) {
//            fm.generateBonfire(bonfireId);
        }else if (reqType == BREAK) {
//            fm.breakBonfire(bonfireId);
        }else if (reqType == INIT) {
            fm.initRoleFireInfo();
        }else if (reqType == UPDATE) {
            fm.updateRoleFire();
        }else if (reqType == THROW_GOLD) {
            fm.throwGold();
        }else if (reqType == THROW_WOOD) {
            fm.throwWood();
        }else if (reqType == WOOD_PICK) {
            fm.pickWood();
        }else if(reqType == ANSWER_QUESTION){
            fm.answerQuestion(questionId,questionIndex);
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        reqType = buff.readByte();
        if (reqType == GENERATE) {
            bonfireId = buff.readInt();
        }else if (reqType == BREAK) {
            bonfireId = buff.readInt();
        }else if(reqType == ANSWER_QUESTION){
            questionId = buff.readInt();
            questionIndex = buff.readInt();
        }
    }

    @Override
    public short getType() {
        return FamilyBonfirePacketSet.S_BONFIRE;
    }
}
