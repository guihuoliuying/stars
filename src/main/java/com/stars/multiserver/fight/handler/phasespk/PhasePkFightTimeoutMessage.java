package com.stars.multiserver.fight.handler.phasespk;

import com.stars.multiserver.fight.FightActor;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.server.main.actor.ActorServer;

/**
 * Created by zhaowenshuo on 2016/12/3.
 */
public class PhasePkFightTimeoutMessage extends Packet {

    private String fightId;
    private byte phase;

    public PhasePkFightTimeoutMessage() {
    }

    public PhasePkFightTimeoutMessage(String fightId, byte phase) {
        this.fightId = fightId;
        this.phase = phase;
    }

    @Override
    public short getType() {
        return 0;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {
        FightActor fightActor = (FightActor) ActorServer.getActorSystem().getActor(fightId);
        if (fightActor == null) {
            return;
        }
        PhasesPkFightHandler fightHandler = (PhasesPkFightHandler) fightActor.getFightHandler();
        if (phase == PhasesPkFightManager.PHASE_INITIAL) {
            fightHandler.finishInitialPhase();

        } else if (phase == PhasesPkFightManager.PHASE_CLIENT_PREPARATION) {
            fightHandler.finishClientPreparationPhase();

        }
    }

    public byte getPhase() {
        return phase;
    }

    public void setPhase(byte phase) {
        this.phase = phase;
    }
}
