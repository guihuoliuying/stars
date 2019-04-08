package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.ride.RideManager;
import com.stars.modules.ride.RideModule;
import com.stars.modules.ride.packet.ClientRide;
import com.stars.modules.ride.prodata.RideInfoVo;
import com.stars.modules.ride.userdata.RoleRidePo;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/19.
 */
public class RideToolFunc extends ToolFunc {

    private int rideId;
    private Map<Integer, Integer> toolMap;

    public RideToolFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        try {
            String[] params = function.split("\\|");
            rideId = Integer.parseInt(params[1]);
            toolMap = StringUtil.toMap(params[2], Integer.class, Integer.class, '+', '|');
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public ToolFuncResult check(Map<String, Module> moduleMap, int count, Object... args) {
        if (count <= 0) {
            return new ToolFuncResult(false, new ClientText("道具数量为零"));
        }
        return new ToolFuncResult(true, null);
    }

    @Override
    public Map<Integer, Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        RideModule rideModule = (RideModule) moduleMap.get(MConst.Ride);
        ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
        int size = count;
        for (int i = 0; i < size; i++) {
            if (!rideModule.isOwned(rideId) || rideModule.isTimeLimit(rideId)) {
                boolean useResult = rideModule.getRide(rideId);
                if (useResult) {
                    count--;
                }
            } else {
                RideInfoVo rideInfoVo = RideManager.getRideInfoVo(rideId);
                toolModule.addAndSend(rideInfoVo.getResolveItemId(), rideInfoVo.getResolveItemCount(), EventType.USETOOL.getCode());
                String tips = String.format(DataManager.getGametext("ride_resolve_tips"),
                        DataManager.getGametext(rideInfoVo.getName()),
                        DataManager.getGametext(ToolManager.getItemName(rideInfoVo.getResolveItemId())), rideInfoVo.getResolveItemCount());
                PlayerUtil.send(rideModule.id(), new ClientText(tips));
            }
        }
        if (count > 0) {
            LogUtil.info("use ridetool count:" + count);
            Map<Integer, Integer> map = new HashMap<>(toolMap);
            for (Integer itemId : toolMap.keySet()) {
                map.put(itemId, map.get(itemId) * count);
            }
            RoleRidePo roleRidePo = rideModule.getRidePoMap().get(rideId);
            Map<Integer, Integer> resultMap = toolModule.addAndSend(map, EventType.USETOOL.getCode());
            ClientRide packet = new ClientRide(ClientRide.RESP_GET);
            if (roleRidePo != null && StringUtil.isNotEmpty(resultMap)) {
                packet.setRidePo(roleRidePo);
                packet.setGetType((byte) 3);
                packet.setToolMap(map);
                PlayerUtil.send(rideModule.id(), packet);
            }
            return resultMap;
        }
        return null;
    }
}
