package com.stars.services.marry;

import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

/**
 * Created by zhouyaohui on 2016/12/7.
 */
public class MarryServiceCheck implements Runnable {

    @Override
    public void run() {
        try {
            ServiceHelper.marryService().check();
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }
}
