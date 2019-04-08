package com.stars.core.tca;

import com.stars.network.server.session.GameSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zws on 2015/12/17.
 */
public class TCAHelper {

    public static void newRecord(GameSession session, String name, long timestamp, long elapse) {
        if (session.getAttribute("login") == null) {
            session.putAttribute("login", new ArrayList<TCRecord>());
        }
        List<TCRecord> list = (List<TCRecord>) session.getAttribute("login");
        list.add(new TCRecord(name, timestamp, elapse));
    }

}
