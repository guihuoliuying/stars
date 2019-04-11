package com.stars.modules.friendInvite;

import com.stars.AccountRow;
import com.stars.core.db.DBUtil;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.demologin.userdata.AccountRole;
import com.stars.modules.drop.DropModule;
import com.stars.modules.friendInvite.event.BindInviteCodeEvent;
import com.stars.modules.friendInvite.packet.ClientBeInvite;
import com.stars.modules.friendInvite.packet.ClientInvite;
import com.stars.modules.friendInvite.packet.ClientServerInfo;
import com.stars.modules.friendInvite.prodata.InviteVo;
import com.stars.modules.friendInvite.userdata.RoleBeInvitePo;
import com.stars.modules.friendInvite.userdata.RoleInvitePo;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.multiserver.MultiServerHelper;
import com.stars.services.ServiceHelper;
import com.stars.services.friend.userdata.FriendApplicationPo;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;
import com.stars.util.RandomUtil;
import com.stars.util.StringUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by chenxie on 2017/6/7.
 */
public class InviteModule extends AbstractModule implements OpActivityModule {

    private AccountRow accountRow;

    private String mainChannel;

    private InviteVo inviteVo;

    private RoleInvitePo roleInvitePo;

    private RoleBeInvitePo roleBeInvitePo;

    public InviteModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("好友邀请", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from roleinvite where roleid = " + id();
        roleInvitePo = DBUtil.queryBean(DBUtil.DB_COMMON, RoleInvitePo.class, sql);
        String sql_ = "select * from rolebeinvite where roleid = " + id();
        roleBeInvitePo = DBUtil.queryBean(DBUtil.DB_COMMON, RoleBeInvitePo.class, sql_);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        LoginModule loginModule = module(MConst.Login);
        accountRow = loginModule.getAccountRow();
        mainChannel = String.valueOf(loginModule.getChannnel());
        com.stars.util.LogUtil.info("好友邀请模块，客户端主渠道号：{}", mainChannel);
        inviteVo = InviteManager.INVITE_VO_MAP.get(mainChannel);
        com.stars.util.LogUtil.info("好友邀请模块，渠道配置数据：{}", inviteVo);
        if (roleInvitePo == null && inviteVo != null) {
            initRoleInvitePo();
        }
        if (roleBeInvitePo == null) {
            initRoleBeInvitePo();
        }
        // 红点
//        signCalRedPoint(MConst.FriendInvite, RedPointConst.FRIEND_INVITE);
//        signCalRedPoint(MConst.FriendInvite, RedPointConst.FRIEND_BE_INVITE);
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (roleInvitePo != null && roleInvitePo.getInviteCount() > roleInvitePo.getFetchCount()) {
            redPointMap.put(RedPointConst.FRIEND_INVITE, "");
        } else {
            redPointMap.put(RedPointConst.FRIEND_INVITE, null);
        }
        if (roleBeInvitePo != null && roleBeInvitePo.getStatus() == 1) {
            redPointMap.put(RedPointConst.FRIEND_BE_INVITE, "");
        } else {
            redPointMap.put(RedPointConst.FRIEND_BE_INVITE, null);
        }
    }

    /**
     * 数据初始化
     *
     * @throws SQLException
     */
    private void initRoleInvitePo() throws SQLException {
        String code = inviteVo.getCode();
        int serverId = MultiServerHelper.getServerId();
        long sequence = InviteManager.SEQUENCE;
        String inviteCode = createUniqueInviteCode(code, serverId, sequence);
        roleInvitePo = new RoleInvitePo();
        roleInvitePo.setRoleId(id());
        roleInvitePo.setInviteCode(inviteCode);
        roleInvitePo.setInviteCount(0);
        roleInvitePo.setFetchCount(0);
        roleInvitePo.setServerId(serverId);
        ServiceHelper.inviteService().insert(roleInvitePo);
    }

    /**
     * 数据初始化
     *
     * @throws SQLException
     */
    private void initRoleBeInvitePo() throws SQLException {
        roleBeInvitePo = new RoleBeInvitePo();
        roleBeInvitePo.setRoleId(id());
        roleBeInvitePo.setBindInviteCode(null);
        roleBeInvitePo.setStatus((byte) 0);
        ServiceHelper.inviteService().insert(roleBeInvitePo);
    }

    /**
     * 生成唯一邀请码
     *
     * @param code
     * @param serverId
     * @return
     * @throws SQLException
     */
    private String createUniqueInviteCode(String code, int serverId, long sequence) throws SQLException {
        String inviteCode = code + serverId % 1000 + sequence + RandomUtil.getRandomString(3);
        InviteManager.SEQUENCE++;
        String sql = "UPDATE sequence SET sequence = " + InviteManager.SEQUENCE;
        DBUtil.execSql(DBUtil.DB_USER, sql);
        return inviteCode;
    }

    /**
     * 显示界面
     */
    public void view() {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        int level = roleModule.getLevel();
        if (level < InviteManager.INVITEATR_ROLELIMIT) {
            if (roleBeInvitePo != null) {
                // X级以前，开放邀请码输入功能，显示邀请码输入界面（受邀方）
                ClientBeInvite clientBeInvite = new ClientBeInvite();
                clientBeInvite.setBindInviteCode(roleBeInvitePo.getBindInviteCode());
                clientBeInvite.setStatus(roleBeInvitePo.getStatus());
                send(clientBeInvite);
            }
        } else if (level >= InviteManager.INVITEATR_ROLELIMIT) {
            if (roleInvitePo != null) {
                // X级及以后，不可再输入邀请码，开放好友邀请功能，显示好友邀请界面（邀请方）
                ClientInvite clientInvite = new ClientInvite();
                clientInvite.setInviteCode(roleInvitePo.getInviteCode());
                clientInvite.setLink(inviteVo.getLink());
                clientInvite.setInviteCount(roleInvitePo.getInviteCount());
                clientInvite.setFetchCount(roleInvitePo.getFetchCount());
                send(clientInvite);
            }
        }
    }

    /**
     * 领取奖励（邀请方）
     */
    public void award() {
        ServerLogModule logModule = module(MConst.ServerLog);
        if (roleInvitePo != null) {
            DropModule dropModule = module(MConst.Drop);
            ToolModule toolModule = module(MConst.Tool);
            if (roleInvitePo.getInviteCount() > 0 && roleInvitePo.getFetchCount() == 0) {
                // 玩家累计邀请好友个数大于0且累计领取奖励次数为0，首次奖励
                Map<Integer, Integer> toolMap = dropModule.executeDrop(InviteManager.INVITEATR_REWARD_FIRST, 1, true);
                toolModule.addAndSend(toolMap, EventType.FRIEND_INVITE.getCode());
                roleInvitePo.setFetchCount(roleInvitePo.getFetchCount() + 1);
                ServiceHelper.inviteService().update(roleInvitePo);
                // 通知客户端
                ClientInvite clientInvite = new ClientInvite();
                clientInvite.setInviteCode(roleInvitePo.getInviteCode());
                clientInvite.setLink(inviteVo.getLink());
                clientInvite.setInviteCount(roleInvitePo.getInviteCount());
                clientInvite.setFetchCount(roleInvitePo.getFetchCount());
                send(clientInvite);
                ClientAward clientAward = new ClientAward(toolMap);
                clientAward.setType((byte) 0);
                toolModule.sendPacket(clientAward);
                if (roleInvitePo.getInviteCount() == roleInvitePo.getFetchCount()) {
                    // 红点
//                    signCalRedPoint(MConst.FriendInvite, RedPointConst.FRIEND_INVITE);
                }
                warn("剩余可领取奖励份数：" + (roleInvitePo.getInviteCount() - roleInvitePo.getFetchCount()));
                logModule.logInvite(id(), roleInvitePo.getFetchCount(), toolMap);
            } else if (roleInvitePo.getInviteCount() > 0 && roleInvitePo.getFetchCount() < roleInvitePo.getInviteCount()) {
                // 玩家累计邀请好友个数大于0且累计领取奖励次数小于玩家累计邀请好友个数，每次奖励
                Map<Integer, Integer> toolMap = dropModule.executeDrop(InviteManager.INVITEATR_REWARD_EVERYTIME, 1, true);
                toolModule.addAndSend(toolMap, EventType.FRIEND_INVITE.getCode());
                roleInvitePo.setFetchCount(roleInvitePo.getFetchCount() + 1);
                ServiceHelper.inviteService().update(roleInvitePo);
                // 通知客户端
                ClientInvite clientInvite = new ClientInvite();
                clientInvite.setInviteCode(roleInvitePo.getInviteCode());
                clientInvite.setLink(inviteVo.getLink());
                clientInvite.setInviteCount(roleInvitePo.getInviteCount());
                clientInvite.setFetchCount(roleInvitePo.getFetchCount());
                send(clientInvite);
                ClientAward clientAward = new ClientAward(toolMap);
                clientAward.setType((byte) 0);
                toolModule.sendPacket(clientAward);
                if (roleInvitePo.getInviteCount() == roleInvitePo.getFetchCount()) {
                    // 红点
//                    signCalRedPoint(MConst.FriendInvite, RedPointConst.FRIEND_INVITE);
                }
                warn("剩余可领取奖励份数：" + (roleInvitePo.getInviteCount() - roleInvitePo.getFetchCount()));
                logModule.logInvite(id(), roleInvitePo.getFetchCount(), toolMap);
            } else {
                warn("奖励已领取完");
            }
        } else {
//            warn("成功邀请1名好友即可领取");
            warn(DataManager.getGametext("inviteatr_reward_invitefailed"));
        }
    }

    /**
     * 领取奖励（受邀方）
     */
    public void awardBe() {
        if (roleBeInvitePo != null) {
            if (roleBeInvitePo.getStatus() == 0) {
//                warn("请先绑定好友邀请码");
                warn(DataManager.getGametext("inviteatr_reward_bindfailed"));
                return;
            }
            if (roleBeInvitePo.getStatus() == 1) {
                DropModule dropModule = module(MConst.Drop);
                ToolModule toolModule = module(MConst.Tool);
                Map<Integer, Integer> toolMap = dropModule.executeDrop(InviteManager.INVITEATR_REWARD_BIND, 1, true);
                toolModule.addAndSend(toolMap, EventType.FRIEND_INVITE.getCode());
                roleBeInvitePo.setStatus((byte) 2);
                ServiceHelper.inviteService().update(roleBeInvitePo);
                // 通知客户端
                ClientBeInvite clientBeInvite = new ClientBeInvite();
                clientBeInvite.setBindInviteCode(roleBeInvitePo.getBindInviteCode());
                clientBeInvite.setStatus(roleBeInvitePo.getStatus());
                send(clientBeInvite);
                ClientAward clientAward = new ClientAward(toolMap);
                clientAward.setType((byte) 0);
                toolModule.sendPacket(clientAward);
                // 红点
//                signCalRedPoint(MConst.FriendInvite, RedPointConst.FRIEND_BE_INVITE);
            }
        }
    }

    /**
     * 绑定邀请码
     *
     * @param inviteCode
     */
    public void bindInviteCode(String inviteCode) {
        ServerLogModule serverLogModule = module(MConst.ServerLog);
        String sql = "select * from roleinvite where invitecode = '" + inviteCode + "'";
        try {
            RoleInvitePo roleInvitePoFrom = DBUtil.queryBean(DBUtil.DB_COMMON, RoleInvitePo.class, sql);

            com.stars.util.LogUtil.info("好友邀请模块，绑定邀请码：{}", roleInvitePoFrom);
            com.stars.util.LogUtil.info("好友邀请模块，绑定邀请码：{}", roleBeInvitePo);

            if (StringUtil.isNotEmpty(roleBeInvitePo.getBindInviteCode())) {
                warn("当前角色已经绑定了邀请码");
                serverLogModule.logBeInvite(false, roleBeInvitePo.getRoleId(), "");
                return;
            }
            if (roleInvitePoFrom == null) {
//                warn("请输入正确的好友邀请码");
                warn(DataManager.getGametext("inviteatr_bind_failed2"));
                serverLogModule.logBeInvite(false, roleBeInvitePo.getRoleId(), "");
                return;
            }
            if (accountRow.getAccountRole(roleInvitePoFrom.getRoleId()) != null) {
//                warn("不能输入自己的邀请码");
                warn(DataManager.getGametext("inviteatr_bind_failed3"));
                serverLogModule.logBeInvite(false, roleBeInvitePo.getRoleId(), "");
                return;
            }
            List<AccountRole> relativeRoleList = accountRow.getRelativeRoleList();
            StringBuilder sb = new StringBuilder();
            for (AccountRole accountRole : relativeRoleList) {
                sb.append(accountRole.getRoleId()).append(",");
            }
            String roleStr = sb.toString();
            String sql_ = "select * from rolebeinvite where roleid in (" + roleStr.substring(0, roleStr.length() - 1) + ") and bindinvitecode is not null";
            long count = DBUtil.queryCount(DBUtil.DB_COMMON, sql_);
            if (count > 0) {
//                warn("你的账号内其他角色已绑定过邀请码");
                warn(DataManager.getGametext("inviteatr_bind_failed4"));
                serverLogModule.logBeInvite(false, roleBeInvitePo.getRoleId(), "");
                return;
            }
            // 有可领取次数上限，以防刷号
            if (roleInvitePoFrom.getInviteCount() >= InviteManager.INVITEATR_REWARD_LIMIT) {
//                warn("此邀请码邀请次数已达到上限");
                warn(DataManager.getGametext("inviteatr_bind_failed5"));
                serverLogModule.logBeInvite(false, roleBeInvitePo.getRoleId(), "");
                return;
            }

            // 绑定成功，双方均达成1次邀请/受邀领奖条件
            roleInvitePoFrom.setInviteCount(roleInvitePoFrom.getInviteCount() + 1);
            roleBeInvitePo.setBindInviteCode(inviteCode);
            roleBeInvitePo.setStatus((byte) 1);
            // 受邀方自动向邀请方申请加好友，并记录双方邀请关系（方便以后拓展功能）
            RoleModule roleModule = (RoleModule) module(MConst.Role);
            Role role = roleModule.getRoleRow();
            ServiceHelper.inviteService().bindInviteCode(roleInvitePoFrom, roleBeInvitePo);

            // 日志打印#受邀方
            serverLogModule.logBeInvite(true, roleInvitePoFrom.getRoleId(), roleBeInvitePo.getBindInviteCode());

            if (roleInvitePoFrom.getServerId() != MultiServerHelper.getServerId()) {
                // 发送邀请方服务器信息
                ClientServerInfo clientServerInfo = new ClientServerInfo();
                clientServerInfo.setServerId(roleInvitePoFrom.getServerId());
                clientServerInfo.setServerName(MultiServerHelper.getServerName(roleInvitePoFrom.getServerId()));
                send(clientServerInfo);
            } else {
                FriendApplicationPo applicationPo = new FriendApplicationPo();
                applicationPo.setApplicantId(roleBeInvitePo.getRoleId());
                applicationPo.setObjectId(roleInvitePoFrom.getRoleId());
                applicationPo.setApplicantName(role.getName());
                applicationPo.setApplicantJobId(role.getJobId());
                applicationPo.setApplicantLevel(role.getLevel());
                applicationPo.setAppliedTimestamp(DateUtil.getSecondTime());
                // 向邀请方申请加好友
                ServiceHelper.friendService().applyFriend(roleBeInvitePo.getRoleId(), roleInvitePoFrom.getRoleId(), applicationPo);
            }

            // 通知客户端
            ClientBeInvite clientBeInvite = new ClientBeInvite();
            clientBeInvite.setBindInviteCode(roleBeInvitePo.getBindInviteCode());
            clientBeInvite.setStatus(roleBeInvitePo.getStatus());
            send(clientBeInvite);
            warn("绑定邀请码成功");

            // 红点
//            signCalRedPoint(MConst.FriendInvite, RedPointConst.FRIEND_INVITE);
//            signCalRedPoint(MConst.FriendInvite, RedPointConst.FRIEND_BE_INVITE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理绑定邀请码事件
     */
    public void handleInviteEvent(BindInviteCodeEvent event) {
        LogUtil.info("处理绑定邀请码事件");
        if (event.getInviterId() == id()) {
            roleInvitePo.setInviteCount(roleInvitePo.getInviteCount() + 1);
            // 通知客户端
            ClientInvite clientInvite = new ClientInvite();
            clientInvite.setInviteCode(roleInvitePo.getInviteCode());
            clientInvite.setLink(inviteVo.getLink());
            clientInvite.setInviteCount(roleInvitePo.getInviteCount());
            clientInvite.setFetchCount(roleInvitePo.getFetchCount());
            send(clientInvite);

            // 红点
//            signCalRedPoint(MConst.FriendInvite, RedPointConst.FRIEND_INVITE);
//            signCalRedPoint(MConst.FriendInvite, RedPointConst.FRIEND_BE_INVITE);
        }
    }

    public boolean isOpenActivity() {
        return getCurShowActivityId() != -1;
    }

    @Override
    public int getCurShowActivityId() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_Invite);
        if (curActivityId != -1 && isAccordWithLimit(curActivityId)) {
            return curActivityId;
        }
        return -1;
    }

    private boolean isAccordWithLimit(int curActivityId) {
        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
        OperateActivityModule operateActivityModule = module(MConst.OperateActivity);
        if (operateActVo != null) {
            if (operateActivityModule.isShow(operateActVo.getRoleLimitMap())) {
                if ("0".equals(operateActVo.getChannel())) {
                    return true;
                } else {
                    String[] passChannelArray = operateActVo.getChannel().split(",");
                    for (String pass : passChannelArray) {
                        if (mainChannel.equals(pass)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public byte getIsShowLabel() {
        return 0;
    }

    public String getInviteCount() {
        return roleInvitePo == null ? null : roleInvitePo.getInviteCode();
    }

    public String getLink() {
        return inviteVo == null ? null : inviteVo.getLink();
    }

}
