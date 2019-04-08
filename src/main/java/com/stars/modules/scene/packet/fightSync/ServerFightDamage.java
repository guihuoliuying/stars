package com.stars.modules.scene.packet.fightSync;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.modules.scene.fightdata.Damage;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by liuyuheng on 2016/9/22.
 */
public class ServerFightDamage extends PlayerPacket {
    private byte sceneType;// 战斗场景类型
    private List<Damage> damageList;

    @Override
    public void execPacket(Player player) {
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        sceneModule.receiveFightPacket(sceneType, this);
    }

    @Override
    public short getType() {
        return ScenePacketSet.S_FIGHTDAMANGE;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        damageList = new LinkedList<>();
        sceneType = buff.readByte();
        byte size = buff.readByte();
        for (int i = 0; i < size; i++) {
            String giverId = buff.readString();
            String receiverId = buff.readString();
            byte damageType = buff.readByte();// 0=伤害;1=治疗
            int value = buff.readInt();
            if (damageType==0) {
                value = value * -1;
            }
            Damage damage = new Damage(giverId, receiverId, value);
            damageList.add(damage);
        }
    }

    public List<Damage> getDamageList() {
        return damageList;
    }
}
