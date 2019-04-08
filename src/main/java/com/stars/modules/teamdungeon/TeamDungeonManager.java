package com.stars.modules.teamdungeon;

import com.stars.modules.teamdungeon.prodata.TeamDungeonVo;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/11/11.
 */
public class TeamDungeonManager {

    // 邀请类型常量
    public final static byte INVOTEE_TYPE_ARROUND = 0;
    public final static byte INVOTEE_TYPE_FRIEND = 1;
    public final static byte INVOTEE_TYPE_FAMILY = 2;
    public final static byte INVOTEE_TYPE_MARRY = 3;

    // 组队副本入口常量
    public final static byte ENTRANCE_DAILY = 1;
    public final static byte ENTRANCE_MARRY = 2;

    // 组队加成关系类型
    public static byte ADDREWARD_TYPE_FRIEND = 1;// 好友
    public static byte ADDREWARD_TYPE_COUPLE = 2;// 夫妻
    public static byte ADDREWARD_TYPE_FAMILY = 3;// 家族

    public static Map<Integer, TeamDungeonVo> teamDungeonVoMap;
    public static byte minTeamCount;
    public static byte maxTeamCount;
    public static long matchTeamTime;

    public static TeamDungeonVo getTeamDungeonVo(int id) {
        return teamDungeonVoMap.get(id);
    }

    public static Map<Integer, TeamDungeonVo> getTeamDungeonVoMap() {
        return teamDungeonVoMap;
    }
}
