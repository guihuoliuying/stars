package com.stars.modules.demologin.packet;

import com.stars.AccountRow;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.demologin.LoginPacketSet;
import com.stars.modules.demologin.userdata.AccountRole;
import com.stars.modules.role.userdata.Role;
import com.stars.network.server.buffer.NewByteBuffer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 推送帐号角色列表;
 * Created by panzhenfeng on 2016/8/2.
 */
public class ClientAccountRoleList extends PlayerPacket {
    private List<Role> roleList = new ArrayList<>();
    private List<String> accountRoleInfoList = new ArrayList<>();
    private long remainMilliSeconds = 0;
    public static final byte TYPE_ROLELIST = 1;
    public static final byte TYPE_CREATEROLECD = 2;
    private byte rtnType = TYPE_ROLELIST;
    private AccountRow accountRow;

    private List<AccountRole> accountRoleList;
    private List<Integer> jobTypes;

    public ClientAccountRoleList() {

    }

    public ClientAccountRoleList(AccountRow accountRow) throws SQLException {
        accountRoleList = new ArrayList<>();
        jobTypes = new ArrayList<>();
        for (AccountRole accountRole : accountRow.getRelativeRoleList()) {
            accountRoleList.add(accountRole.copy());
        }
        for (Integer jobId : accountRow.getActivedJobs()) {
            jobTypes.add(jobId);
        }
        sort();
    }

    public AccountRole getAccountRoleByRoleId(String roleId) {
        List<AccountRole> accountRoleList = this.accountRow.getRelativeRoleList();
        AccountRole tmpAccountRole = null;
        for (int i = 0, len = accountRoleList.size(); i < len; i++) {
            tmpAccountRole = accountRoleList.get(i);
            if (tmpAccountRole != null) {
                if (tmpAccountRole.getRoleId().equals(roleId)) {
                    return tmpAccountRole;
                }
            }
        }
        return null;
    }

    public void setType(byte state_) {
        rtnType = state_;
    }

    public void setReaminCreateRoleCd(long milliSeconds_) {
        remainMilliSeconds = milliSeconds_;
    }

    private void addRole(Role tmpRole) {
        roleList.add(tmpRole);
    }

    private void sort() throws SQLException {
        Collections.sort(accountRoleList, new Comparator<AccountRole>() {
            @Override
            public int compare(AccountRole r1, AccountRole r2) {
                if (r1.lastLoginTimestamp > r2.lastLoginTimestamp) {
                    return -1;
                }
                if (r1.lastLoginTimestamp < r2.lastLoginTimestamp) {
                    return 1;
                }
                return r1.roleLevel > r2.roleLevel ? -1 : 1;
            }
        });
        for (AccountRole r : accountRoleList) {
            addData(r.getRoleId(), r.roleLevel, r.roleName, r.roleJobId, r.getTime().getTime());
        }
    }

    /**
     * 添加帐号角色数据用于选角;
     */
    private void addData(String roleId_, int roleLevel_, String roleName_, int jobId_, long createTime) {
        StringBuilder sb = new StringBuilder();
        sb.append(roleId_);
        sb.append("+");
        sb.append(roleLevel_);
        sb.append("+");
        sb.append(roleName_);
        sb.append("+");
        sb.append(jobId_);
        sb.append("+");
        sb.append(createTime);
        accountRoleInfoList.add(sb.toString());
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return LoginPacketSet.C_ACCOUNTROLELIST;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(rtnType);
        if (rtnType == TYPE_ROLELIST) {
            buff.writeInt(accountRoleInfoList.size());
            for (int i = 0, len = accountRoleInfoList.size(); i < len; i++) {
                buff.writeString(accountRoleInfoList.get(i));
            }
            if (jobTypes.size() == 0) {
                jobTypes = new ArrayList<>();
            }
            /**
             * 被激活的职业id
             */
            buff.writeInt(jobTypes.size());
            for (Integer jobId : jobTypes) {
                buff.writeInt(jobId);
            }
        } else if (rtnType == TYPE_CREATEROLECD) {
            buff.writeString(String.valueOf(remainMilliSeconds / 1000));
        }
    }

}
