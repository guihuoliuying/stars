package com.stars.modules.tool.event;


import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by zhangjiahua on 2016/5/4.
 *
 * 增加道具时抛出的事件
 *
 * 注意这里的数量是已经增加的数量(包裹内+邮件都算)
 */
public class AddToolEvent extends Event {

    //key:道具id  value:数量
    private Map<Integer,Integer> toolMap;

    public AddToolEvent(Map<Integer,Integer> map){
        this.toolMap = map;
    }

    public Map<Integer, Integer> getToolMap() {
        return toolMap;
    }

    public void setToolMap(Map<Integer, Integer> toolMap) {
        this.toolMap = toolMap;
    }


}
