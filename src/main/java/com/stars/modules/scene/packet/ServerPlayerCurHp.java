package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.scene.Scene;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.modules.scene.imp.fight.DungeonScene;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2017/4/25.
 */
public class ServerPlayerCurHp extends PlayerPacket {
	private int curHp;
	
    @Override
    public void execPacket(Player player) {
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        Scene scene = sceneModule.getScene();
        if (scene != null && scene instanceof DungeonScene) {
			DungeonScene dungeonScene = (DungeonScene)scene;
			dungeonScene.updateCurHp(player.id() , curHp);
		}
    }

    @Override
    public short getType() {
        return ScenePacketSet.S_PLAYER_CURHP;
    }
    
    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.curHp = buff.readInt();
    }
}
