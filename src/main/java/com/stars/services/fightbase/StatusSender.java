package com.stars.services.fightbase;

import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.ServerManager;
import com.stars.multiserver.fight.FightActor;
import com.stars.multiserver.fight.FightRPC;
import com.stars.network.server.session.GameSession;
import com.stars.server.main.actor.ActorServer;
import com.stars.util.LogUtil;
import com.stars.core.actor.AbstractActor;
import com.stars.core.rpc2.RpcManager;

import java.util.Collection;
import java.util.Properties;

/**
 * Created by zhaowenshuo on 2017/3/8.
 */
public class StatusSender extends Thread {
    private boolean runState = false;

    private int serverId;
    private String ip;
    private int port;
    private int fsManaerId;
    private int fsManaerId1 = -1;

    public StatusSender() {
        serverId = ServerManager.getServer().getConfig().getServerId();
        ip = ServerManager.getServer().getConfig().getServerIp();
        port = ServerManager.getServer().getConfig().getServerPort();
        Properties properties = ServerManager.getServer().getConfig().getProps().get(BootstrapConfig.FIGHTMANAGER);
        fsManaerId = Integer.parseInt(properties.getProperty("serverId"));
        properties = ServerManager.getServer().getConfig().getProps().get(BootstrapConfig.FIGHTMANAGER1);
        if (properties != null) {
            fsManaerId1 = Integer.parseInt(properties.getProperty("serverId"));
        }
    }

    @Override
    public void run() {
        while (runState) {
            try {
                GameSession gs = RpcManager.sessionMap.get(fsManaerId);
                if (gs == null || !gs.isActive()) {
                    gs = RpcManager.sessionMap.get(fsManaerId1);
                }
                if (gs != null && gs.isActive()) {
                    Collection<AbstractActor> col = ActorServer.getActorSystem().getActors().values();
                    int load = 0;
                    for (AbstractActor abstractActor : col) {
                        if (abstractActor instanceof FightActor) {
                            FightActor fightActor = (FightActor) abstractActor;
                            load = load + fightActor.getFightHandler().getFighterCount();
                        }
                    }
                    try {
                        FightRPC.rmfsManagerService().registerFightServer(fsManaerId, serverId, ip, port, load);
                    } catch (Exception e) {
                        LogUtil.debug(e.getMessage(), e);
                    }
                    try {
                        FightRPC.rmfsManagerService().registerFightServer(fsManaerId1, serverId, ip, port, load);
                    } catch (Exception e) {
                        LogUtil.debug(e.getMessage(), e);
                    }

                }

                sleep(1000l);
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        }
    }

    public boolean isRunState() {
        return runState;
    }

    public void setRunState(boolean runState) {
        this.runState = runState;
    }
}
