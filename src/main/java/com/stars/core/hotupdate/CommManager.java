package com.stars.core.hotupdate;

import com.stars.core.gmpacket.CommandGm;
import com.stars.util.LogUtil;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by wuyuxing on 2017/1/5.
 */
public class CommManager implements CommManagerInterface, HotUpdateInterface {

    /**
     * 执行comm命令
     */
    public String comm(List<String> paramerList) {
        if (paramerList == null) return "gm命令参数为null,执行失败";
        CommandGm.gmLock.lock();
        String result = "command命令执行成功";
        try {
            String operateName = paramerList.get(0);
            Method m = CommManager.class.getDeclaredMethod(operateName.trim(), List.class);
            result = (String) m.invoke(null, paramerList);
            return result;
        } catch (NoSuchMethodException e) {
            result = "没有该命令,请检查";
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            result = "command命令执行失败," + e.getMessage();
        } finally {
            CommandGm.gmLock.unlock();
        }
        return result;
    }

}
