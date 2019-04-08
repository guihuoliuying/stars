package com.stars.server.connector.backdoor;

import com.stars.util.backdoor.command.CommandFactory;

/**
 * Created by zws on 2015/11/9.
 */
public class ConnectorBackdoor {
    public static void init() {
        CommandFactory.register(new ConnCommand());
    }
}
