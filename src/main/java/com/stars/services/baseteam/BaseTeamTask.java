package com.stars.services.baseteam;

import com.stars.services.ServiceHelper;

/**
 * Created by zhouyaohui on 2017/2/23.
 */
public class BaseTeamTask implements Runnable {
    @Override
    public void run() {
        ServiceHelper.baseTeamService().schedule();
    }
}
