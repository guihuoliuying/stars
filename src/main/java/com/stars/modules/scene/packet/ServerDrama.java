package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2017/1/4.
 */
public class ServerDrama extends PlayerPacket {
    private byte reqType;// 请求类型(子协议)

    /* 参数 */
    private String type;// 类型(安全区场景/战斗场景/任务)
    private String paramId;// 条目Id
    private String dramaId;// 剧情Id

    @Override
    public void execPacket(Player player) {
        SceneModule sceneModule = module(MConst.Scene);
        switch (reqType) {
            case 1:// 更新播放剧情记录
                sceneModule.updatePlayedDrama(type, paramId, dramaId);
                break;
        }
    }

    @Override
    public short getType() {
        return ScenePacketSet.S_DRAMA;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
        switch (reqType) {
            case 1:// 更新播放剧情记录
                this.type = buff.readString();// 类型(安全区场景/战斗场景/任务)
                this.paramId = buff.readString();// 条目Id
                this.dramaId = buff.readString();// 剧情Id
                break;
        }
    }
}
