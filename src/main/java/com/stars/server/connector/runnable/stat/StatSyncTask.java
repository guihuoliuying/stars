package com.stars.server.connector.runnable.stat;

import com.stars.server.connector.Connector;
import com.stars.server.connector.stat.ConnectorStat;
import com.stars.util.LogUtil;

/**
 * Created by zws on 2015/9/14.
 */
public class StatSyncTask implements Runnable {

    private ConnectorStat stat;

    public StatSyncTask(ConnectorStat stat) {
        this.stat = stat;
    }

    @Override
    public void run() {
        try {
            Connector.currentStat.get().add(stat);
        } catch (Exception e) {
        	LogUtil.error("", e);
        }
    }
}
