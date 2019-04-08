package com.stars.modules.scene.packet;

import com.stars.core.attr.Attribute;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by liuyuheng on 2016/7/7.
 */
public class ServerMonsterDead extends PlayerPacket {
    private List<String> monsterUIdList = new LinkedList<>();// 死亡怪物唯一Id
    private Attribute attr = null;

    @Override
    public void execPacket(Player player) {
        if (attr != null) {
            ((RoleModule) module(MConst.Role)).roleAttrCheckReq(attr);
        }
        // 先返回确认包
        ClientMonsterDeadConfirm confirm = new ClientMonsterDeadConfirm(monsterUIdList);
        PlayerUtil.send(getRoleId(), confirm);
        SceneModule sceneModule = module(MConst.Scene);
        sceneModule.monsterDead(monsterUIdList);
    }

    @Override
    public short getType() {
        return ScenePacketSet.S_MONSTERDEAD;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        short size = buff.readShort();
        String monsterUId;
        for (short i = 0; i < size; i++) {
            monsterUId = buff.readString();
            monsterUIdList.add(monsterUId);
        }
        //是否检测玩家属性
        short flag = buff.readShort();
        if (flag == 1) {
            attr = new Attribute();
            attr.readFightAtrFromBuffer(buff);
        }
    }
}
