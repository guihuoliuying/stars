package com.stars.modules.name.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.name.NameModule;
import com.stars.modules.name.NamePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/6/14.
 */
public class ServerRenamePacket extends PlayerPacket {
    private short subType;
    public static final short REQ_ROLE_RENAME = 1;//请求角色改名
    public static final short REQ_RENAME_SURPLUS_TIMES = 2;//请求剩余次数
    public static final short REQ_FAMILY_RENAME = 3;//请求家族改名
    public static final short REQ_RANDOM_NAME = 4;//请求随机名称
    public String newName;
    public String familyName;

    @Override
    public short getType() {
        return NamePacketSet.S_RENAME;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readShort();
        switch (subType) {
            case REQ_ROLE_RENAME: {
                newName = buff.readString();
            }
            break;
            case REQ_FAMILY_RENAME: {
                familyName = buff.readString();
            }

        }
    }

    @Override
    public void execPacket(Player player) {
        NameModule nameModule = module(MConst.Name);
        switch (subType) {
            case REQ_ROLE_RENAME: {
                nameModule.roleRename(newName);
            }
            break;
            case REQ_RENAME_SURPLUS_TIMES: {
                nameModule.sendRenameTimes();
            }
            break;
            case REQ_FAMILY_RENAME: {
                nameModule.familyRename(familyName);
            }
            break;
            case REQ_RANDOM_NAME: {
                nameModule.sendRandomName();
            }
            break;
        }
    }

}
