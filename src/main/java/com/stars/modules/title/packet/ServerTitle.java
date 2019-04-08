package com.stars.modules.title.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.title.TitleModule;
import com.stars.modules.title.TitlePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/7/25.
 */
public class ServerTitle extends PlayerPacket {
    private byte reqType;// 请求类型(子协议)

    private int titleId;// 称号Id

    @Override
    public void execPacket(Player player) {
        TitleModule titleModule = module(MConst.Title);
        switch (reqType) {
            case 1:// 打开称号界面
                titleModule.sendAllTitleData();
                break;
            case 2:// 替换称号
                titleModule.changeTitle(titleId);
                break;
            default:
                break;
        }
    }

    @Override
    public short getType() {
        return TitlePacketSet.S_TITLE;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
        switch (reqType) {
            case 1:// 打开称号界面
                break;
            case 2:// 替换称号
                this.titleId = buff.readInt();
                break;
            default:
                break;
        }
    }
}
