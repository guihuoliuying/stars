package com.stars.server.connector.monitor;

import com.stars.server.connector.Connector;
import com.stars.server.connector.stat.ConnectorStat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by zhaowenshuo on 2016/2/1.
 */
public class MonitorStatCallable implements Callable<Map<String, Object>> {

    @Override
    public Map<String, Object> call() throws Exception {
        Map<String, Object> newMap = new HashMap<>();
        ConnectorStat stat = Connector.accumulatedStat.get();
        Map<Short, Long> packetNumberMap = stat.packetNumberMapCopy();
        newMap.put("connector.packetNumberMap", stat.packetNumberMapCopy());
        newMap.put("connector.packetLengthMap", stat.packetLengthMapCopy());
        newMap.put("connector.connectionNumber", stat.connectionNumber());
        newMap.put("connector.totalPacketNumber", stat.totalPacketNumber());
        newMap.put("connector.totalPacketLength", stat.totalPacketLength());
        return newMap;
    }

}
