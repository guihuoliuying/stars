package com.stars.core.redpoint;

import com.stars.core.player.Player;
import com.stars.modules.redpoint.packet.ClientRedPoint;
import com.stars.network.server.packet.Packet;
import com.stars.util.LogUtil;

import java.util.*;

/**
 * Created by zhaowenshuo on 2016/7/2.
 */
public class RedPoints {

    private Player player;
    private Map<Integer, String> redPointMap;// 当前红点状态
    private Map<String, Set<Integer>> signCalRedPointMap;// 标记需要计算的红点,<moduleName, Set<redpointId>>
    private Map<Integer, String> changeMap;// 改变红点

    public RedPoints(Player player) {
        this.player = player;
        signCalRedPointMap = new HashMap<>();
        redPointMap = new HashMap<>();
    }

    /**
     * 标记计算红点
     *
     * @param moduleName
     * @param redPointIds
     */
    public void addSign(String moduleName, int... redPointIds) {
        if (!signCalRedPointMap.containsKey(moduleName)) {
            signCalRedPointMap.put(moduleName, new HashSet<Integer>());
        }
        for (int redPointId : redPointIds) {
            signCalRedPointMap.get(moduleName).add(redPointId);
        }
    }

    private void send(Packet packet) {
        player.send(packet);
    }

    /**
     * 下发红点
     */
    public void flush() {
        try {
            if (changeMap == null || changeMap.isEmpty()) {
                return;
            }
            ClientRedPoint crp = new ClientRedPoint();
            Map<Integer, String> addMap = new HashMap<>();
            Map<Integer, String> removeMap = new HashMap<>();
            for (Map.Entry<Integer, String> entry : changeMap.entrySet()) {
                if (entry.getValue() != null) {
                    redPointMap.put(entry.getKey(), entry.getValue());
                    addMap.put(entry.getKey(), entry.getValue());
                } else {
                    if (redPointMap.containsKey(entry.getKey())) {
                        redPointMap.remove(entry.getKey());
                        removeMap.put(entry.getKey(), "");
                    }
                }
            }
            crp.setAddMap(addMap);
            crp.setRemoveMap(removeMap);
            List<Map<Integer, String>> addList = new ArrayList<>();
            addList.add(addMap);
            List<Map<Integer, String>> removeList = new ArrayList<>();
            removeList.add(removeMap);
            if (!addMap.isEmpty() || !removeMap.isEmpty()) {
                LogUtil.info("roleId:{},增加红点:{},消除红点:{}", player.id(), addList, removeList);
                send(crp);
            }
        } finally {
            if (signCalRedPointMap != null) {
                signCalRedPointMap.clear();
            }
            if (changeMap != null) {
                changeMap.clear();
            }
        }
    }

    /**
     * 获得已标记需要计算的红点
     *
     * @return
     */
    public Map<String, Set<Integer>> getSignCalRedPointMap() {
        return signCalRedPointMap;
    }

    public Map<Integer, String> getRedPointMap() {
        return redPointMap;
    }

    public Map<Integer, String> getChangeMap() {
        return changeMap;
    }

    /**
     * 改变的红点
     *
     * @param changeMap
     */
    public void setChangeMap(Map<Integer, String> changeMap) {
        this.changeMap = changeMap;
        if (redPointMap.isEmpty()) {
            redPointMap.putAll(changeMap);
        }
    }
}
