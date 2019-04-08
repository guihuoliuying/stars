package com.stars.modules.title.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.title.TitlePacketSet;
import com.stars.modules.title.prodata.TitleVo;
import com.stars.modules.title.userdata.RoleTitle;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/7/25.
 */
public class ClientTitle extends PlayerPacket {
    private byte sendType;// 下发类型

    public static final byte SEND_ALL_TITLE = 1;// 下发所有称号数据
    public static final byte UPDATE_TITLE = 2;// 更新称号状态

    private Map<Integer, TitleVo> titleVoMap;// 称号产品数据
    private Map<Integer, RoleTitle> roleTitleMap;// 称号玩家数据

    public ClientTitle() {
    }

    public ClientTitle(byte sendType) {
        this.sendType = sendType;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return TitlePacketSet.C_TITLE;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(sendType);
        switch (sendType) {
            case SEND_ALL_TITLE:
                sendTitleVo(buff);
                sendTitlePo(buff);
                break;
            case UPDATE_TITLE:
                sendTitlePo(buff);
                break;
            default:
                break;
        }
    }

    private void sendTitleVo(com.stars.network.server.buffer.NewByteBuffer buff) {
        short size = (short) (titleVoMap == null ? 0 : titleVoMap.size());
        buff.writeShort(size);
        if (titleVoMap != null) {
            for (TitleVo titleVo : titleVoMap.values()) {
                titleVo.writeToBuff(buff);
            }
        }
    }

    private void sendTitlePo(NewByteBuffer buff) {
        short size = (short) (roleTitleMap == null ? 0 : roleTitleMap.size());
        buff.writeShort(size);
        if (roleTitleMap != null) {
            for (RoleTitle roleTitle : roleTitleMap.values()) {
                roleTitle.writeToBuff(buff);
            }
        }
    }

    public void setTitleVoMap(Map<Integer, TitleVo> titleVoMap) {
        this.titleVoMap = titleVoMap;
    }

    public void setRoleTitleMap(Map<Integer, RoleTitle> roleTitleMap) {
        this.roleTitleMap = roleTitleMap;
    }
}
