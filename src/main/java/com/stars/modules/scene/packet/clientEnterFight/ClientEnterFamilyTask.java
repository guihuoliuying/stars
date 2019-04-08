package com.stars.modules.scene.packet.clientEnterFight;

import com.stars.network.server.buffer.NewByteBuffer;

public class ClientEnterFamilyTask extends ClientEnterDungeon{
	@Override
    public void writeToBuffer(NewByteBuffer buff) {
        super.writeToBuffer(buff);
    }
}
