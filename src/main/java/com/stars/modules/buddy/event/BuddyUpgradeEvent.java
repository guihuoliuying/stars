package com.stars.modules.buddy.event;

import com.stars.core.event.Event;
import com.stars.modules.buddy.BuddyManager;
import com.stars.modules.buddy.prodata.BuddyinfoVo;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/10/19.
 */
public class BuddyUpgradeEvent extends Event {
    public static byte LEVELUP = 1;// 升级
    public static byte STAGEUP = 2;// 升阶
    public static byte ARMLEVELUP = 3;// 武装等级(悟性等级)

    private int buddyId;
    private byte type;// 事件类型
    private int preLevel;// 改变前
    private int curLevel;// 改变后
    private Map<Integer, Integer> roleBuddyLevelMap;

    public BuddyUpgradeEvent(int buddyId, byte type, int preLevel, int curLevel , Map<Integer, Integer> roleBuddyLevelMap) {
        this.buddyId = buddyId;
        this.type = type;
        this.preLevel = preLevel;
        this.curLevel = curLevel;
        this.roleBuddyLevelMap = roleBuddyLevelMap;
    }

    public byte getType() {
        return type;
    }

    public int getPreLevel() {
        return preLevel;
    }

    public int getCurLevel() {
        return curLevel;
    }

    public int getBuddyId() {
        return buddyId;
    }

    public Map<Integer, Integer> getRoleBuddyLevelMap() {
        return roleBuddyLevelMap;
    }

    public byte getBuddyQuality() {
        BuddyinfoVo buddyinfoVo = BuddyManager.getBuddyinfoVo(buddyId);
        if (buddyinfoVo == null)
            return 0;
        return buddyinfoVo.getQuality();
    }
}
