package com.stars.modules.baseteam.event;

import com.stars.core.event.Event;
import com.stars.modules.baseteam.userdata.BaseTeamInvitor;

/**
 * Created by liuyuheng on 2016/11/10.
 */
public class BaseTeamEvent extends Event {
    public static byte RECEIVE_INVITE = 0;// 收到组队邀请
    public static byte JOIN_TEAM = 1;// 加入队伍通知
    public static byte CANCEL_MATCH_MEMBER = 2;// 取消匹配队员
    public static byte CANCEL_MATCH_TEAM = 3;// 取消匹配队伍
    public static byte TEAM_TARGET_CHANGED = 4;// 队伍目标已更换
    public static byte APPLY_JOIN_TEAM = 5;// 申请入队
    public static byte DELETE_INVITE = 6;//删除组队邀请

    private byte tag;
    private long invitorId;
    private long applierId;
    private int teamId;
    private BaseTeamInvitor teamInvitor;
    private int teamTarget;
    private byte teamType;
    private String notice;

    public BaseTeamEvent(byte tag){
        this.tag = tag;
    }

    public byte getTag() {
        return tag;
    }

    public void setTag(byte tag) {
        this.tag = tag;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public long getInvitorId() {
        return invitorId;
    }

    public void setInvitorId(long invitorId) {
        this.invitorId = invitorId;
    }
    
    public long getApplierId() {
        return applierId;
    }

    public void setApplierId(long applierId) {
        this.applierId = applierId;
    }

    public BaseTeamInvitor getTeamInvitor() {
        return teamInvitor;
    }

    public void setTeamInvitor(BaseTeamInvitor teamInvitor) {
        this.teamInvitor = teamInvitor;
    }

    public int getTeamTarget() {
        return teamTarget;
    }

    public void setTeamTarget(int teamTarget) {
        this.teamTarget = teamTarget;
    }

    public byte getTeamType() {
        return teamType;
    }

    public void setTeamType(byte teamType) {
        this.teamType = teamType;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }
}
