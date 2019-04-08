package com.stars.modules.friendShare;

import com.stars.core.SystemRecordMap;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.drop.DropModule;
import com.stars.modules.friendInvite.InviteManager;
import com.stars.modules.friendInvite.InviteModule;
import com.stars.modules.friendShare.packet.ClientShare;
import com.stars.modules.friendShare.userdata.RoleShareRecordPo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.util.StringUtil;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by chenxie on 2017/6/7.
 */
public class ShareModule extends AbstractModule {

    private RoleShareRecordPo roleShareRecordPo;

    public ShareModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("朋友圈分享", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from rolesharerecord where roleid = " + id();
        roleShareRecordPo = DBUtil.queryBean(DBUtil.DB_USER, RoleShareRecordPo.class, sql);
        if (roleShareRecordPo == null) {
            initRoleShareRecordPo();
        }
    }

    /**
     * 数据初始化
     *
     * @throws SQLException
     */
    private void initRoleShareRecordPo() throws SQLException {
        roleShareRecordPo = new RoleShareRecordPo();
        roleShareRecordPo.setRoleId(id());
        roleShareRecordPo.setShareNum(0);
        roleShareRecordPo.setStatus((byte) 0);
        context().insert(roleShareRecordPo);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        // 红点
        signCalRedPoint(MConst.FriendShare, RedPointConst.FRIEND_SHARE);
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        initRoleShareRecordPo();
    }

    /**
     * 显示界面
     */
    public void view() {
        InviteModule inviteModule = module(MConst.FriendInvite);
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        int level = roleModule.getLevel();
        ClientShare clientShare = new ClientShare();
        clientShare.setStatus(roleShareRecordPo.getStatus());
        if (level > InviteManager.INVITEATR_ROLELIMIT) {
            // X级及以后,开放好友邀请功能
            clientShare.setInviteCode(inviteModule.getInviteCount());
            clientShare.setLink(inviteModule.getLink());
        }
        send(clientShare);
    }

    /**
     * 分享朋友圈
     */
    public void share() {
        int shareNum = roleShareRecordPo.getShareNum();
        if (shareNum == 0) {
            //每周首次成功分享朋友圈，可领取分享奖励
            roleShareRecordPo.setStatus((byte) 1);
        }
        roleShareRecordPo.setShareNum(shareNum + 1);
        context().update(roleShareRecordPo);
        // 通知客户端
        ClientShare clientShare = new ClientShare();
        clientShare.setStatus(roleShareRecordPo.getStatus());
        send(clientShare);
        // 红点
        signCalRedPoint(MConst.FriendShare, RedPointConst.FRIEND_SHARE);
        // 日志
        ServerLogModule module = module(MConst.ServerLog);
        module.dynamic_4_Log_str(ThemeType.DYNAMIC_WX_SHARED.getThemeId(), "share", "", "");
    }

    /**
     * 领取奖励
     */
    public void award() {
        if (roleShareRecordPo.getStatus() == 1) {
            //当周第2次及以后分享，无分享奖励但是还能分享
            roleShareRecordPo.setStatus((byte) 2);
            context().update(roleShareRecordPo);
            DropModule dropModule = module(MConst.Drop);
            ToolModule toolModule = module(MConst.Tool);
            Map<Integer, Integer> toolMap = dropModule.executeDrop(ShareManager.INVITEATR_REWARD_SHARE, 1, true);
            toolModule.addAndSend(toolMap, EventType.FRIEND_SHARE.getCode());
            ClientAward clientAward = new ClientAward(toolMap);
            clientAward.setType((byte) 0);
            toolModule.sendPacket(clientAward);
            // 日志
            ServerLogModule module = module(MConst.ServerLog);
            module.dynamic_4_Log_str(ThemeType.DYNAMIC_WX_SHARED.getThemeId(), "award", "reward@number:" + StringUtil.makeString(toolMap, '@', '&'), "");
        }
        // 通知客户端
        ClientShare clientShare = new ClientShare();
        clientShare.setStatus(roleShareRecordPo.getStatus());
        send(clientShare);
        // 红点
        signCalRedPoint(MConst.FriendShare, RedPointConst.FRIEND_SHARE);
    }

    @Override
    public void onFiveOClockReset(Calendar now) throws Throwable {
        long s = System.currentTimeMillis();
        if (getLong("invite.resetTimestamp", 0) == 0) {
            setLong("invite.resetTimestamp", s);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 5);
        if ((now.getTimeInMillis() >= calendar.getTimeInMillis() && getLong("invite.resetTimestamp", 0) < calendar.getTimeInMillis())
                || s - getLong("invite.resetTimestamp", 0) > 3600 * 24 * 7 * 1000) {

            //每周奖励重置时间为周三05:00
            roleShareRecordPo.setShareNum(0);
            roleShareRecordPo.setStatus((byte) 0);
            context().update(roleShareRecordPo);
            setLong("invite.resetTimestamp", SystemRecordMap.fiveOClockResetTimestamp);
        }
        // 红点
        signCalRedPoint(MConst.FriendShare, RedPointConst.FRIEND_SHARE);
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (roleShareRecordPo.getStatus() == 1) {
            redPointMap.put(RedPointConst.FRIEND_SHARE, "");
        } else {
            redPointMap.put(RedPointConst.FRIEND_SHARE, null);
        }
    }
}
