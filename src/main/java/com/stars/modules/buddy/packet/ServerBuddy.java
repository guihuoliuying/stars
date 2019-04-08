package com.stars.modules.buddy.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.buddy.BuddyPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/8/11.
 */
public class ServerBuddy extends PlayerPacket {
    private byte reqType;// 请求类型(子协议)

    private int buddyId;// 伙伴Id
    private byte partId;// 装备位置Id

    @Override
    public void execPacket(Player player) {
        BuddyModule buddyModule = module(MConst.Buddy);
        switch (reqType) {
            case 1:// 修改跟随
                buddyModule.changeFollowBuddy(buddyId);
                break;
            case 2:// 修改出战
                buddyModule.changeFightBuddy(buddyId);
                break;
            case 3:// 升阶
                buddyModule.upgradeStageLv(buddyId);
                break;
            case 4:// 穿装备
                buddyModule.putOnEquip(buddyId, partId);
                break;
            case 5:// 武装升级
                buddyModule.upgradeArmLevel(buddyId);
                break;
            case 6:// 提升一级
                buddyModule.upgradeLevel1(buddyId);
                break;
            case 7:// 升至最高
                buddyModule.upgradeLevelHighest(buddyId);
                break;
            default:
                break;
        }
    }

    @Override
    public short getType() {
        return BuddyPacketSet.S_BUDDY;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
        switch (reqType) {
            case 1:// 修改跟随
                this.buddyId = buff.readInt();
                break;
            case 2:// 修改出战
                this.buddyId = buff.readInt();
                break;
            case 3:// 升阶
                this.buddyId = buff.readInt();
                break;
            case 4:// 穿装备
                this.buddyId = buff.readInt();
                this.partId = buff.readByte();// 装备位置Id
                break;
            case 5:// 武装升级
                this.buddyId = buff.readInt();
                break;
            case 6:// 提升一级
                this.buddyId = buff.readInt();
                break;
            case 7:// 升至最高
                this.buddyId = buff.readInt();
                break;
            default:
                break;
        }
    }
}
