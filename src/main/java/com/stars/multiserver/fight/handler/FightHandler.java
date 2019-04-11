package com.stars.multiserver.fight.handler;

import com.google.gson.Gson;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.fight.FightActor;
import com.stars.multiserver.fight.RoleId2ActorIdManager;
import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.multiserver.fight.message.AddNewfighterToFightActor;
import com.stars.multiserver.packet.NewFighterToFightActor;
import com.stars.network.server.session.SessionManager;
import com.stars.server.main.actor.ActorServer;
import com.stars.util.LogUtil;
import io.netty.util.internal.ConcurrentSet;

import java.util.*;

/**
 * Created by zhaowenshuo on 2016/11/18.
 */
public abstract class FightHandler {

    protected static Gson gson = new Gson();

    private Set<Class> passThroughPacketTypeSet = new HashSet<>();

    protected String fightId; // 战斗Id（业务生成，尽量保证唯一）
    protected int fightServerId; // 战斗服Id（有点怪，应该直接通过方法拿，先这样）
    protected int fromServerId; // 请求创建战斗的服务Id
    protected short handlerType;

    protected Set<Long> fighterIdSet = new ConcurrentSet<>();
    protected FightActor actor;

//    private long frameCount = 0L; // 帧计数

    /*
     * 类方法（）
     */
    public abstract void onFightCreationSucceeded(int fightServerId, int fromServerId, String fightId, Object args);

    public abstract void onFightCreationFailed(int fightServerId, int fromServerId, String fightId, Object args, Throwable cause);

    public abstract void onFighterAddingSucceeded(int fightServerId, int fromServerId, String fightId, Set<Long> entitySet);

    public abstract void onFighterAddingFailed(int fightServerId, int fromServerId, String fightId, Set<Long> entitySet, Throwable cause);

    public void onFightReadyFailed(int fightServerId, int fromServerId, String fightId) {
    }

    public abstract void onFightStopFailed(int fightServerId, int fromServerId, String fightId, Throwable cause);

    /*
     * 实例方法
     */

    /**
     * 用于初始化FightHandler
     *
     * @param args FightHandler.createFight的args参数，继承类根据业务需要进行初始化
     */
    public abstract void init(Object args);

    public void handleFightReady(int fightServerId, int fromServerId, String fightId) {
    }

    ;

    /**
     * 调用FightHandler.stopFight会触发该方法
     *
     * @param fightServerId
     * @param fromServerId
     * @param fightId
     */
    public abstract void handleFightStop(int fightServerId, int fromServerId, String fightId);

    /**
     * 处理死亡
     *
     * @param deadMap (受害者, 攻击者)
     */
    public void handleDead(long frameCount, Map<String, String> deadMap) {
    }

    /**
     * 处理伤害
     *
     * @param damageMap (受害者, (攻击者, 伤害值))
     */
    public void handleDamage(long frameCount, Map<String, HashMap<String, Integer>> damageMap) {
    }

    /**
     * 处理服务端LUA每帧返回的结果
     *
     * @param frameCount
     * @param data
     * @param rawData
     */
    public void handleLuaFrameData(long frameCount, LuaFrameData data, Object[] rawData) {
    }

    /**
     * 处理客户端主动退出
     *
     * @param roleId
     */
    public void handleFighterExit(long roleId) {
    } // 主动退出

    /**
     * 处理家族战客户端主动退出到备战场景
     *
     * @param roleId
     */
    public void handleFighterExitToFamilySafeScene(long roleId) {
    }

    /**
     * 超时处理
     *
     * @param frameCount
     * @param hpInfo
     */
    public void handleTimeOut(long frameCount, HashMap<String, String> hpInfo) {
    }

    /**
     * 处理客户端掉线情况
     *
     * @param roleId
     */
    public abstract void handleFighterOffline(long roleId); // 客户端掉线


    /**
     * 处理切连接
     */
    public void handChangeConn(long roleId) {
    }

    public void handleMessage(Object message) {
    }

    public  void handleFightStop0(int fightServerId, int fromServerId, String fightId) {
        try {
            handleFightStop(fightServerId, fromServerId, fightId);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        ActorServer.getActorSystem().removeActor(fightId);
        for (Long fighterId : fighterIdSet) {
            SessionManager.remove(fighterId);
            RoleId2ActorIdManager.removeRoleId(fighterId);
            RoleId2ActorIdManager.remove(fighterId);
        }
    }

    public final void handleLuaFrameData0(long frameCount, Object[] rawData) {

    }



    public void handNewFighter(AddNewfighterToFightActor aNewfighterToFightActor) {

        NewFighterToFightActor newers = aNewfighterToFightActor.getNewer();
        //中途加入的玩家需要处理
        Map<Long, Byte> fighterOlMap = newers.getFightersMap();
        HashSet<Long> set = new HashSet<>();
        for (Map.Entry<Long, Byte> kvp : fighterOlMap.entrySet()) {
            addFighterId(kvp.getKey());
            set.add(kvp.getKey());
        }
        actor.addServerOrder(newers.getData());
        onFighterAddingSucceeded(MultiServerHelper.getServerId(), aNewfighterToFightActor.getServerId(), fightId, set);
    }

    public final void registerPassThroughPacketType(Class messageClass) {
        this.passThroughPacketTypeSet.add(messageClass);
    }

    /*
     * 便捷方法
     */
    protected long toLong(String s) {
        return Long.parseLong(s);
    }

    protected String toString(long l) {
        return Long.toString(l);
    }

    /*
     * Getter / Setter
     */
    public String getFightId() {
        return fightId;
    }

    public void setFightId(String fightId) {
        this.fightId = fightId;
    }

    public int getFightServerId() {
        return fightServerId;
    }

    public void setFightServerId(int fightServerId) {
        this.fightServerId = fightServerId;
    }

    public int getFromServerId() {
        return fromServerId;
    }

    public void setFromServerId(int fromServerId) {
        this.fromServerId = fromServerId;
    }

    public final void setFightActor(FightActor actor) {
        this.actor = actor;
    }

    public Set<Long> getFighterIdSet() {
        return fighterIdSet;
    }

    public void setFighterIdSet(Set<Long> fighterIdSet) {
        this.fighterIdSet = fighterIdSet;
    }

    public void addFighterId(long fighterId) {
        fighterIdSet.add(fighterId);
    }

    public void removeFighterId(long fighterId) {
        fighterIdSet.remove(fighterId);
    }

//    public long getFrameCount() {
//        return frameCount;
//    }

    public short getHandlerType() {
        return handlerType;
    }

    public void setHandlerType(short handlerType) {
        this.handlerType = handlerType;
    }

    public Set<Class> getPassThroughPacketTypeSet() {
        return passThroughPacketTypeSet;
    }

    public void setPassThroughPacketTypeSet(Set<Class> passThroughPacketTypeSet) {
        this.passThroughPacketTypeSet = passThroughPacketTypeSet;
    }

    public int getFighterCount() {
        return fighterIdSet.size();
    }

    public void removeFighterId(List<Long> list) {
        for (Long fighterId : list) {
            if (fighterIdSet.contains(fighterId)) {
                fighterIdSet.remove(fighterId);
                SessionManager.remove(fighterId);
                RoleId2ActorIdManager.removeRoleId(fighterId);
                RoleId2ActorIdManager.remove(fighterId);
            }
        }
    }

    /**
     * 齐楚大作战可以中途加入开战时所没有的战斗实体，其他战斗请确认是否必要
     *
     * @param entityList
     */
    public void handleAddFighter(List<FighterEntity> entityList) {
    }

}
