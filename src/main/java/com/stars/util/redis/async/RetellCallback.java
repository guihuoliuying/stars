package com.stars.util.redis.async;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.core.actor.Actor;
import com.stars.util.callback.Callback;
import com.stars.util.callback.CallbackContext;

/**
 * Created by zhaowenshuo on 2015/12/21.
 */
public class RetellCallback extends Packet implements com.stars.util.callback.Callback {

    private com.stars.core.actor.Actor actor;
    private com.stars.util.callback.Callback callback;
    private com.stars.util.callback.CallbackContext context;

    
    public RetellCallback(com.stars.core.actor.Actor actor, Callback callback) {
		this.actor = actor;
		this.callback = callback;
	}
    
    @Override
    public void onCalled(CallbackContext ctx) {
        this.context = ctx;
        actor.tell(this, Actor.noSender);
    }

    @Override
    public short getType() {
        return 0;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {
        callback.onCalled(context);
    }
    
}
