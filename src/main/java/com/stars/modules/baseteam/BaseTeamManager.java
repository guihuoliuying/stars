package com.stars.modules.baseteam;

import com.stars.modules.baseteam.handler.TeamHandler;
import com.stars.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/11/8.
 */
public class BaseTeamManager {
    // 队伍类型,需要约定
    public static final byte TEAM_TYPE_DAILYDUNGEON = 1;// 日常活动
    public static final byte TEAM_TYPE_FAMILYINVADE = 2;// 家族入侵活动
    public static final byte TEAM_TYPE_MARRY = 3;       // 情谊副本
    public static final byte TEAM_TYPE_ESCORT = 4;      // 运镖
    public static final byte TEAM_TYPE_CARGO_ROB = 5;  // 劫镖
    public static final byte TEAM_TYPE_COUPLE_PVP = 6;// 双人组队pvp
    public static final byte TEAM_TYPE_ELITEDUNGEON = 7;// 精英副本
    public static final byte TEAM_TYPE_POEM = 8;// 诗歌
    public static final byte TEAM_TYPE_CAMPCITYFIGHT = 9;// 阵营   齐楚之战

    public static Map<Byte, Class<? extends TeamHandler>> teamHandlerMap = new HashMap<>();

    public static void registerTeamHandler(byte type, Class<? extends TeamHandler> clazz) {
        teamHandlerMap.put(type, clazz);
    }

    public static TeamHandler getHandler(byte teamType) {
        try {
        	Class<? extends TeamHandler> handlerClass = teamHandlerMap.get(teamType);
        	if(handlerClass==null) return null;
            return handlerClass.newInstance();
        } catch (Exception e) {
            LogUtil.error("创建类型={}的队伍处理器失败", teamType, e);
        }
        return null;
    }
}
