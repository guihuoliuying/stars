package com.stars.util.actlock;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/5/10.
 */
public class ActLock {

    private long versionGenerator = 0L;
    private long maxTimeoutTimestamp = 0L;
    private Map<Long, Long> reqMap = new HashMap<>();

    public long lock(long timeout) {
        long version = ++versionGenerator;
        long timeoutTimestamp = System.currentTimeMillis() + timeout;
        reqMap.put(version, timeoutTimestamp);
        if (timeoutTimestamp > maxTimeoutTimestamp) {
            maxTimeoutTimestamp = timeoutTimestamp;
        }
        return version;
    }

    public void unlock(long version) {
        reqMap.remove(version);
        maxTimeoutTimestamp = 0L;
        for (Long timestamp : reqMap.values()) {
            if (timestamp != null && timestamp > maxTimeoutTimestamp) {
                maxTimeoutTimestamp = timestamp;
            }
        }
    }

    public boolean isLock() {
        return reqMap.size() > 0 && System.currentTimeMillis() <= maxTimeoutTimestamp;
    }

}
