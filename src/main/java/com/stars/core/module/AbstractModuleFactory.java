package com.stars.core.module;

import com.stars.core.event.EventDispatcher;
import com.stars.core.player.Player;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/1/13.
 */
public abstract class AbstractModuleFactory<T extends Module> implements ModuleFactory<T> {

    private PacketSet packetSet;
    
    private String muduleKey;

    public AbstractModuleFactory(PacketSet packetSet) {
        this.packetSet = packetSet;
    }

    @Override
    public void loadProductData() throws Exception {

    }

    @Override
    public final void initPacket() throws Exception {
        if (packetSet != null) {
        	List<Class<? extends Packet>> al = packetSet.getPacketList();
            for (Class<? extends Packet> clazz : al) {
                PacketManager.register(clazz);
            }
        }
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public T newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return null;
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {

    }

    public PacketSet getPacketSet() {
        return packetSet;
    }

	public String getMuduleKey() {
		return muduleKey;
	}

	 public void initModuleKey(String key){
		 this.setMuduleKey(key);
	 }

	public void setMuduleKey(String muduleKey) {
		this.muduleKey = muduleKey;
	}
}
