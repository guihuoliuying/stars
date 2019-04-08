package com.stars.services.newredbag;

import com.stars.services.ServiceHelper;

/**
 * Created by zhouyaohui on 2017/2/18.
 */
public class NewRedbagSchedule implements Runnable {
    @Override
    public void run() {
        ServiceHelper.newRedbagService().schedule();
    }
}
