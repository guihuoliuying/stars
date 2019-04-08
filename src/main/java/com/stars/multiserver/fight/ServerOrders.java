package com.stars.multiserver.fight;

import com.stars.modules.pk.packet.ServerOrder;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhaowenshuo on 2016/12/29.
 */
public class ServerOrders {

    private static AtomicInteger instanceIdCreator = new AtomicInteger(0);

    public static ServerOrder newAddBuffOrder(byte camp, int buffId, int buffLevel) {
        ServerOrder order = new ServerOrder();
        order.setOrderType(ServerOrder.ORDER_TYPE_ADD_CAMP_BUFF);
        order.setCmapId(camp);
        order.setCharacterType((byte) 0);
        order.setBuffId(buffId);
        order.setLevel(buffLevel);
        order.setInstanceId(nextInstanceId());
        return order;
    }

    public static ServerOrder newAddBuffOrder(byte camp, byte characterType, int buffId, int buffLevel) {
        ServerOrder order = new ServerOrder();
        order.setOrderType(ServerOrder.ORDER_TYPE_ADD_CAMP_BUFF);
        order.setCmapId(camp);
        order.setCharacterType(characterType);
        order.setBuffId(buffId);
        order.setLevel(buffLevel);
        order.setInstanceId(nextInstanceId());
        return order;
    }

    public static ServerOrder newAddBuffOrderNoCamp(int buffId, int buffLevel) {
        ServerOrder order = new ServerOrder();
        order.setOrderType(ServerOrder.ORDER_TYPE_ADD_BUFF);
        order.setBuffId(buffId);
        order.setLevel(buffLevel);
        order.setInstanceId(nextInstanceId());
        return order;
    }

    public static ServerOrder newRemoveBuffOrder(byte camp, int instanceId) {
        ServerOrder order = new ServerOrder();
        order.setOrderType(ServerOrder.ORDER_TYPE_REMOVE_CAMP_BUFF);
        order.setCmapId(camp);
        order.setInstanceId(instanceId);
        return order;
    }

    public static ServerOrder newAiOrder(byte ai, ArrayList<String> uniqueIDs) {
        ServerOrder order = new ServerOrder();
        order.setOrderType(ServerOrder.ORDER_TYPE_SETAI);
        order.setAiState(ai);
        order.setUniqueIDs(uniqueIDs);
        return order;
    }

    private static int nextInstanceId() {
        int id = instanceIdCreator.decrementAndGet();
        if (id >= 0) {
            synchronized (instanceIdCreator) {
                if (!instanceIdCreator.compareAndSet(id, -1)) {
                    return instanceIdCreator.decrementAndGet(); // 实际上不会这么快耗尽，所有不再做检查
                } else {
                    return -1;
                }
            }
        }
        return id;
    }

}
