package com.stars.modules.getway;

import com.stars.core.event.EventDispatcher;
import com.stars.core.expr.ExprUtil;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.getway.packet.ClientGetWay;
import com.stars.modules.getway.prodata.GetWayVo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/6/22.
 */
public class GetWayModule extends AbstractModule {

    public GetWayModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("获得途径", id, self, eventDispatcher, moduleMap);
    }

    public void view(List<Integer> getWayIdList) {
        List<Integer> resultList = new ArrayList<>();
        for (int getWayId : getWayIdList) {
            GetWayVo vo = GetWayManager.getGetWayVo(getWayId);
            if (vo != null && ExprUtil.isTrue(vo.getCondExpr(), moduleMap())) {
                resultList.add(getWayId);
            }
        }
        send(new ClientGetWay(resultList));
    }

}
