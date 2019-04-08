package com.stars.modules.achievement.event;

import com.stars.core.event.Event;

/**
 * Created by zhouyaohui on 2016/10/18.
 */
public class JoinActivityEvent extends Event {
    public final static int GAMECAVE = 8;   // 洞府活动
    public final static int SEARCHTREASURE = 9;       // 虚空深渊,六国寻宝
    public final static int CALLBOSS = 10;     // 召唤boss
    public final static int SKYTOWER = 11;     // 镇妖塔
    public final static int TEAMDUNGEON_DEFEND = 12;     // 守护水晶
    public final static int TEAMDUNGEON_CHALLENGE = 13;     // 挑战boss
    public final static int BONFIRE = 14;   // 家族篝火
    public final static int FAMILY_EXPE = 15;     // 家族远征
    public final static int var16 = 16;     // 个人竞技场
    public final static int LOOTTREASURE = 17;     // 野外夺宝
    public final static int PRODUCEDUNGEON_EXP = 18;     // 经验副本(资源产出副本type=1)
    public final static int PRODUCEDUNGEON_STRENGTHEN_STONE = 19;     // 资源产出副本type=2

    private int activity;

    public JoinActivityEvent(int activity) {
        this.activity = activity;
    }

    public int getActivity() {
        return activity;
    }
}
