package com.stars.server.connector.runnable.stat;

import com.stars.server.connector.Connector;
import com.stars.server.connector.stat.ConnectorStat;
import com.stars.util.LogUtil;

/**
 * Created by zws on 2015/9/14.
 */
public class StatPrintTask implements Runnable {

    @Override
    public void run() {
        try {
            ConnectorStat currentStat = com.stars.server.connector.Connector.currentStat.get();
            ConnectorStat accumulatedStat = com.stars.server.connector.Connector.accumulatedStat.get();

            accumulatedStat.add(currentStat);
            Connector.currentStat.set(new ConnectorStat());

            com.stars.util.LogUtil.info("stat[process,60s]: {}, {}",
                    currentStat.getConnectionString(), currentStat.getPacketDistributeString());
            com.stars.util.LogUtil.info("stat[process,total]: {}, {}",
                    accumulatedStat.getConnectionString(), accumulatedStat.getPacketDistributeString());
        } catch (Exception e) {
        	LogUtil.error("", e);
        }
    }
}
