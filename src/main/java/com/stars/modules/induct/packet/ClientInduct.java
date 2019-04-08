package com.stars.modules.induct.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.induct.InductPacketSet;
import com.stars.modules.induct.userdata.RoleInduct;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/8/18.
 */
public class ClientInduct extends PlayerPacket {
    private byte sendType;// 下发类型
    private int inductId;

    public static final byte LOGIN_SYNC = 0; //登录时同步;
    public static final byte UPDATE_INDUCT = 1;// 更新玩家引导记录
    public static final byte RESPONSE_NONE_INDUCT = 2;//响应无引导;
    public static final byte RESPONSE_NONE = 3;//响应None;

    private Map<Integer, RoleInduct> map;

    public ClientInduct() {
    }

    public ClientInduct(byte sendType) {
        this.sendType = sendType;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return InductPacketSet.C_INDUCT;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(sendType);
        switch (sendType) {
            case LOGIN_SYNC://登录时同步;
            case UPDATE_INDUCT:// 更新玩家引导记录
                short size = (short) (map == null ? 0 : map.size());
                buff.writeShort(size);
                if (size == 0) return;
                for (RoleInduct roleInduct : map.values()) {
                    roleInduct.writeToBuff(buff);
                }
                break;
            case RESPONSE_NONE_INDUCT: //给客户端的服务端无记录反馈;
                buff.writeInt(inductId);
                break;
            default:
                break;
        }
    }

    public void setMap(Map<Integer, RoleInduct> map) {
        this.map = map;
    }

    public void setRoleInduct(RoleInduct roleInduct) {
        map = new HashMap<>();
        map.put(roleInduct.getInductId(), roleInduct);
    }

    public void setInductId(int inductId) {
        this.inductId = inductId;
    }
}
