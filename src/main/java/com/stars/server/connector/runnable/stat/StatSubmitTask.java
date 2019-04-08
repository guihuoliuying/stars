package com.stars.server.connector.runnable.stat;

import com.stars.server.connector.Connector;
import com.stars.server.connector.stat.ConnectorStat;
import com.stars.util.LogUtil;

/**
 * Created by zws on 2015/9/14.
 */
public class StatSubmitTask implements Runnable {

    @Override
    public void run() {
        try {
            ConnectorStat currentStat = com.stars.server.connector.Connector.currentStat.get(); // 当前数据统计
            ConnectorStat accumulatedStat = com.stars.server.connector.Connector.accumulatedStat.get(); // 累计数据统计

            accumulatedStat.add(currentStat); // 累加
            com.stars.server.connector.Connector.currentStat.set(new ConnectorStat()); // 清空

            Runnable task = new StatSyncTask(currentStat);
            Connector.scheduler.submit(task);

            if (false) {
            	com.stars.util.LogUtil.info("stat[thread@{},60s]: {}, {}", Thread.currentThread().getId(),
                        currentStat.getConnectionString(), currentStat.getPacketDistributeString());
            	com.stars.util.LogUtil.info("stat[thread@{},total]: {}, {}", Thread.currentThread().getId(),
                        accumulatedStat.getConnectionString(), accumulatedStat.getPacketDistributeString());
            }
        } catch (Exception e) {
        	LogUtil.error("", e);
        }
    }
}
