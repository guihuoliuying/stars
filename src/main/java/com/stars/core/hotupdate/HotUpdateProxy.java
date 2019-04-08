package com.stars.core.hotupdate;

import com.stars.util.LogUtil;

/**
 * Created by wuyuxing on 2017/1/5.
 */
public abstract class HotUpdateProxy{

    public abstract void setHotUpdataInstance(HotUpdateInterface instance);

    public abstract String getClassName();

    public boolean reload(){
        try {
            Class cl = YinHanHotUpdateManager.loadClass(getClassName());
            if (cl == null) {
                return false;
            }
            HotUpdateInterface o = (HotUpdateInterface)cl.newInstance();
            setHotUpdataInstance(o);
            return true;
        } catch (Exception e) {
            LogUtil.error("HotUpdateProxy.reload", e.getMessage(), e);
        }
        return false;
    }
}
