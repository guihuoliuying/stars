package com.stars.core.hotupdate;

import java.util.List;

/**
 * Created by wuyuxing on 2017/1/5.
 */
public interface CommManagerInterface {

    /**
     * 执行comm命令
     */
    public String comm(List<String> paramerList);
}
