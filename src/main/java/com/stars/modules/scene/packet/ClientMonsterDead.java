package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务器主动杀死怪物通知;
 * Created by panzhenfeng on 2016/8/25.
 */
public class ClientMonsterDead extends PlayerPacket {
    private List<String> monsterUidList;

    public ClientMonsterDead() {

    }

    public boolean isValid(){
        if(monsterUidList == null || monsterUidList.size()<=0){
            return false;
        }
        return true;
    }

    public void addMonsterUid(String monsterUid){
        if(monsterUidList == null){
            monsterUidList = new ArrayList<>();
        }
        if(!monsterUidList.contains(monsterUid)){
            monsterUidList.add(monsterUid);
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return ScenePacketSet.C_SERVER_SKILL_MONSTERS;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(monsterUidList.size());
        for(int i = 0, len = monsterUidList.size(); i<len; i++){
            buff.writeString(monsterUidList.get(i));
        }
    }
}
