package com.stars.modules.familyactivities.war.event;

import com.stars.core.event.Event;

/**
 * Created by chenkeyu on 2017-04-11 18:32
 */
public class FamilyWarEnterSafeSceneEvent extends Event {
    private byte type;//成员初始类型
    private byte memberType;//成员类型1:精英成员，0:匹配成员
    private long camp1FamilyPoints;//己方家族积分
    private long camp2FamilyPoints;//对方家族积分
    private long remainTime;//本轮剩余时间

    public FamilyWarEnterSafeSceneEvent(byte type, byte memberType, long camp1FamilyPoints, long camp2FamilyPoints, long remainTime) {
        this.type = type;
        this.memberType = memberType;
        this.camp1FamilyPoints = camp1FamilyPoints;
        this.camp2FamilyPoints = camp2FamilyPoints;
        this.remainTime = remainTime;
    }

    public byte getType() {
        return type;
    }

    public byte getMemberType() {
        return memberType;
    }

    public long getCamp1FamilyPoints() {
        return camp1FamilyPoints;
    }

    public long getCamp2FamilyPoints() {
        return camp2FamilyPoints;
    }

    public long getRemainTime() {
        return remainTime;
    }
}
