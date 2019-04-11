package com.stars.multiserver.fight;

import com.stars.core.actor.AbstractActor;
import com.stars.core.actor.Actor;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.scene.packet.ServerExitFight;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.fight.handler.FightHandler;
import com.stars.multiserver.fight.message.AddNewfighterToFightActor;
import com.stars.multiserver.fight.message.NoticeFightServerAddServerOrder;
import com.stars.multiserver.fight.message.NoticeFightServerReady;
import com.stars.multiserver.fight.message.RemoveFromFightActor;
import com.stars.multiserver.packet.StopFightActor;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.server.connector.packet.FrontendClosedN2mPacket;
import com.stars.server.main.message.Disconnected;
import com.stars.util.LogUtil;
import com.stars.util.LuaUtil;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.keplerproject.luajava.LuaObject;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

import java.util.LinkedList;

/**
 * 单纯的PVP-ACTOR
 *
 * @author dengzhou
 */
public class FightActor extends AbstractActor {

    // ---- 业务结合修改(start
    private String fightId;
    private FightHandler fightHandler;
    private FightHandler protoHandler;
    private long frameCount = 0L;
    private long timeLimit = 0L;
    // ---- 业务结合修改(end

    /**
     * 客户端上传的指令
     */
    private NewByteBuffer clientOrders;
    /**
     * 初试化战斗需要的数据
     */
    private byte[] initData;

    private LinkedList<byte[]> orderFromServer;

    /**
     * actor的强制回收时间，避免孤儿没人管理，造成内存泄露
     */
    private long forceStopTime;

    /* lua */
    private LuaState luaState;
    private LuaObject updateObj;
    public LuaObject updateData;

    public FightActor(String fightId, byte[] data) {
        this.fightId = fightId;
        this.initData = data;
        this.clientOrders = new NewByteBuffer(UnpooledByteBufAllocator.DEFAULT.buffer());
        this.orderFromServer = new LinkedList<byte[]>();
        this.forceStopTime = System.currentTimeMillis() + 3600000l;//设置一小时后强制回收

//        this.forceStopTime = System.currentTimeMillis() + timeLimit + 120_000L; // 超过时间限制后两分钟关闭FightActor
    }


    @Override
    public void onReceived(Object message, Actor sender) {
        if (message instanceof FrontendClosedN2mPacket
                || message instanceof Disconnected
                || message instanceof ServerExitFight) {
            Packet fp = (Packet) message;
            if (fightHandler != null) {
                if (message instanceof FrontendClosedN2mPacket) {
                    fightHandler.handleFighterOffline(fp.getRoleId());
                }
                if (message instanceof ServerExitFight) {
                    fightHandler.handleFighterExit(fp.getRoleId());
                }
            }
            return;
        }

        if (message instanceof AddNewfighterToFightActor) {
            /** rpc版本的添加玩家到战斗中 */
            fightHandler.handNewFighter((AddNewfighterToFightActor) message);
            return;
        }

        if (message instanceof NoticeFightServerReady) {
            NoticeFightServerReady ready = (NoticeFightServerReady) message;
            try {
                setClientOrders(ready.getData());
                fightHandler.handleFightReady(MultiServerHelper.getServerId(), ready.getServerId(), ready.getFightId());
            } catch (Exception e) {
                protoHandler.onFightReadyFailed(MultiServerHelper.getServerId(), ready.getServerId(), ready.getFightId());
            }
            return;
        }
        if (message instanceof NoticeFightServerAddServerOrder) {
            NoticeFightServerAddServerOrder order = (NoticeFightServerAddServerOrder) message;
            addServerOrder(order.getData());
            return;
        }

        if (message instanceof StopFightActor) {
            handleStopMessage((StopFightActor) message);
            return;
        }
        if (message instanceof RemoveFromFightActor) {
            RemoveFromFightActor m = (RemoveFromFightActor) message;
            fightHandler.removeFighterId(m.getRoleIdList());
            return;
        }

        if (message instanceof FightFrameTick) {
            handleFightFrameTick();
            return;
        }

        if (fightHandler != null && fightHandler.getPassThroughPacketTypeSet().contains(message.getClass())) {
            fightHandler.handleMessage(message);
            return;
        }

        if (message instanceof Packet) {
            Packet packet = (Packet) message;
            try {
                packet.execPacket();
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                PacketManager.send(packet.getSession(), new ClientText("请求异常"));
            }
        }

    }

    private void handleStopMessage(StopFightActor message) {
//        this.stop();
//        if (fightHandler != null) {
//            fightHandler.handleFightStop0(message.getFightServerId(), message.getFromServerId(), fightId);
//        }
//        this.over();

        stopFight(message.getFightServerId(), message.getFromServerId());
    }

    private void handleFightFrameTick() {
        if (System.currentTimeMillis() >= forceStopTime) {
            handleStopMessage(new StopFightActor(MultiServerHelper.getServerId(), 0));
        } else {
            try {
                updateLuaState();
            } catch (Throwable cause) {
                LogUtil.error(cause.getMessage(), cause);
            }
        }
    }

    public void addServerOrder(byte[] data) {
        this.orderFromServer.addFirst(data);
    }

    public void start() {
        initLuaState();
    }

    public void over() {
        closeLuaState();
        clientOrders.getBuff().release();
    }

    public byte[] getInitData() {
        return initData;
    }

    public void setInitData(byte[] initData) {
        this.initData = initData;
    }

    public void setClientOrders(byte[] clientOrder) {
        this.clientOrders.writeBytes(clientOrder);
    }

    public byte[] getClientOrders() {
        byte[] s = this.clientOrders.readBytes();
        clientOrders.getBuff().release();
        this.clientOrders = new NewByteBuffer(UnpooledByteBufAllocator.DEFAULT.buffer());
        return s;
    }

    public String getFightId() {
        return fightId;
    }

    public void setFightId(String fightId) {
        this.fightId = fightId;
    }

    public FightHandler getFightHandler() {
        return fightHandler;
    }

    public void setFightHandler(FightHandler fightHandler) {
        this.fightHandler = fightHandler;
    }

    public FightHandler getProtoHandler() {
        return protoHandler;
    }

    public void setProtoHandler(FightHandler protoHandler) {
        this.protoHandler = protoHandler;
    }

    public void stopFight(int fightServerId, int fromServerId) {
        this.stop();
        if (fightHandler != null) {
            fightHandler.handleFightStop0(fightServerId, fromServerId, fightId);
        }
        this.over();
    }

    /* luaState相关 */
    private void initLuaState() {
        luaState = LuaStateFactory.newLuaState();
        luaState.openLibs();
        try {
//            new LuaRequireFunc(luaState).register("require");
            String path = System.getProperty("user.dir");
            LogUtil.info("path=" + path);
            int isErr = luaState.LdoFile(path + "/config/lua/ServerEnter.lua");
            if (isErr != 0) {
                throw new Exception("load lua file foreshow error, isErr=" + isErr);
            }
            LuaObject pathObj = LuaUtil.getLuaFunc(luaState, "ServerEnter", "setPath");
            pathObj.call(new Object[]{path}, 0);
            LuaObject obj = LuaUtil.getLuaFunc(luaState, "EnvironmentHandler", "setServerData");
            obj.call(new Object[]{initData}, 0);
            updateObj = LuaUtil.getLuaFunc(luaState, "EnvironmentHandler", "update");
            updateData = LuaUtil.getLuaFunc(luaState, "EnvironmentHandler", "updateData");
            LogUtil.info("luaState|new|fightId:{}", fightId);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            closeLuaState();
            throw new RuntimeException(e);
        }
    }

    private void updateLuaState() throws Throwable {
//        long st = System.currentTimeMillis();
        byte[] orderFS = null;
        orderFS = orderFromServer.pollLast();
        if (orderFS != null) {
//            long stOfOrderFS = System.currentTimeMillis();
            updateData.call(new Object[]{orderFS}, 0);
//            LogUtil.info("stOfOrderFS:{}", System.currentTimeMillis() - stOfOrderFS);
        }
        byte[] order = getClientOrders();
//        if (order.length > 0) {
//            LogUtil.info("exec client order:"+order);
//        }
        long stOfRawResult = System.currentTimeMillis();
        Object[] rawResult = updateObj.call(new Object[]{order}, 2);


        frameCount++;
//        if (frameCount % 300 == 0) {
//            LogUtil.info("stOfRawResult:{}", System.currentTimeMillis() - stOfRawResult);
//        }
//        if (frameCount % 600 == 0) { // 每5秒记录一次luaState的内存大小
//            logLuaState(frameCount, luaState);
//        }
        if (rawResult[0] != null) {
            long stOfFightHandler = System.currentTimeMillis();
            fightHandler.handleLuaFrameData0(frameCount, rawResult);
//            LogUtil.info("stOfFightHandler:{}", System.currentTimeMillis() - stOfFightHandler);
        }
//        LogUtil.info("updateLuaState:{}", System.currentTimeMillis() - st);
    }

    private void closeLuaState() {
        if (luaState != null && !luaState.isClosed()) {
            try {
                Double mem = (Double) LuaUtil.call(luaState, "collectgarbage", "count"); // 打印luaState的内存占用量
                LuaUtil.callNoResult(luaState, "EnvironmentHandler.clear"); // 清理luaState资源
                luaState.close();
                LogUtil.info("luaState|close|fightId:{}|mem:{}K", fightId, mem);
            } catch (Throwable t) {
                LogUtil.error("", t);
            }
        }
    }

    private void logLuaState(long frameCount, LuaState luaState) {
        try {
//            Double mem = (Double) LuaUtil.call(luaState, "collectgarbage", "count"); // 打印luaState的内存占用量
//            LogUtil.info("luaState|stat|fightId:{}|frameCount:{}|mem:{}K", fightId, frameCount, mem);
        } catch (Throwable t) {
            LogUtil.error("", t);
        }
    }

}
