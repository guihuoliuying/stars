package com.stars.server.connector.stat;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zws on 2015/9/11.
 */
public class ConnectorStat {

    private Map<Short, Long> packetNumber = new HashMap<>(1024);
    private Map<Short, Long> packetLength = new HashMap<>(1024);

    private long totalPacketNumber = 0L; //
    private long totalPacketLength = 0L;

    private long connectionNumber = 0L; // 连接数量(线程使用)

    public void packetNumber(short type, long delta) {
        packetNumber.put(type, get(packetNumber, type, 0L) + delta);
        totalPacketNumber += delta;
    }

    public long packetNumber(short type) {
        return get(packetNumber, type, 0L);
    }

    public void packetLength(short type, long delta) {
        packetLength.put(type, get(packetLength, type, 0L) + delta);
        totalPacketLength += delta;
    }

    public long packetLength(short type) {
        return get(packetLength, type, 0L);
    }

    private long get(Map<Short, Long> map, short key, long defaultValue) {
        Long val = map.get(key);
        if (val != null) {
            return val;
        }
        return defaultValue;
    }


    public long totalPacketNumber() {
        return totalPacketNumber;
    }

    public long totalPacketLength() {
        return totalPacketLength;
    }

    public void incrConnection() {
        this.connectionNumber++;
    }

    public void descConnection() {
        this.connectionNumber--;
    }

    public long connectionNumber() {
        return this.connectionNumber;
    }

    public Map<Short, Long> packetNumberMapCopy() {
        return new HashMap<>(packetNumber);
    }

    public Map<Short, Long> packetLengthMapCopy() {
        return new HashMap<>(packetLength);
    }

    public void add(ConnectorStat other) {
        for (int i = 0; i <= Short.MAX_VALUE; i++) {
            short idx = (short) i;
            long delta = 0L;
            if ((delta = get(other.packetNumber, idx, 0L)) > 0) {
                this.packetNumber.put(idx, get(this.packetNumber, idx, 0L) + delta);
            }
            if ((delta = get(other.packetLength, idx, 0L)) > 0) {
                this.packetLength.put(idx, get(this.packetLength, idx, 0L) + delta);
            }
        }
        this.totalPacketNumber += other.totalPacketNumber;
        this.totalPacketLength += other.totalPacketLength;
        this.connectionNumber += other.connectionNumber;
    }

    public String getConnectionString() {
        return "connectionNumber=" + connectionNumber;
    }

    public String getPacketDistributeString() {
        StringBuilder sb = new StringBuilder();
        sb.append("totalPacketNumber=").append(totalPacketNumber).append(",");
        sb.append("totalPacketLength=").append(totalPacketLength).append(",");
        // number
        sb.append("packetNumber=[");
        for (int i = 0; i <= Short.MAX_VALUE; i++) {
            if (get(packetNumber, (short) i, 0L) > 0) {
                sb.append(String.format("0x%04X", i))
                        .append("=").append(get(packetNumber, (short) i, 0L)).append(",");
            }
        }
        sb.append("],");
        // length
        sb.append("packetLength=[");
        for (int i = 0; i <= Short.MAX_VALUE; i++) {
            if (get(packetLength, (short) i, 0L) > 0) {
                sb.append(String.format("0x%04X", i))
                        .append("=").append(get(packetLength, (short) i, 0L)).append(",");
            }
        }
        sb.append("],");
        return sb.toString();
    }

    public String getJsonString() {
        Map<String, Object> map = new HashMap<>();
        map.put("packetNumber", packetNumber);
        map.put("packetLength", packetLength);
        map.put("totalPacketNumber", totalPacketNumber);
        map.put("totalPacketLength", totalPacketLength);
        map.put("connectionNumber", connectionNumber);

        return new Gson().toJson(map, HashMap.class);
    }

}
