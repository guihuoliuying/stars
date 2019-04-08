package com.stars.core.gmpacket.email.condition;

import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.email.pojodata.EmailConditionArgs;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.role.summary.RoleSummaryComponentImpl;
import com.stars.services.ServiceHelper;
import com.stars.util._HashMap;
import org.springframework.cglib.beans.BeanCopier;

/**
 * Created by huwenjun on 2017/5/20.
 */
public class RoleMatcherManager {

    private static EmailConditionArgs load(Long roleId) throws Exception {
        String sql = "SELECT role.roleid, role.sex, role.`level`, role.jobid, role.createtime, accountrole.account  FROM `role` JOIN accountrole ON role.roleid = accountrole.roleid where role.roleid =" + roleId;
        _HashMap map = DBUtil.querySingleMap(DBUtil.DB_USER, sql);
        EmailConditionArgs emailConditionArgs = new EmailConditionArgs();
        emailConditionArgs.setAccount(map.getString("accountrole.account"));
        emailConditionArgs.setCreateTime(map.getLong("role.createtime"));
        emailConditionArgs.setJobId(map.getInt("role.jobid"));
        emailConditionArgs.setLevel(map.getInt("role.level"));
        emailConditionArgs.setRoleId(map.getLong("role.roleid"));
        return emailConditionArgs;
    }

    public static EmailConditionArgs get(Long roleId) {
        EmailConditionArgs emailConditionArgs = new EmailConditionArgs();
        RoleSummaryComponent roleSummaryComponent = (RoleSummaryComponent) ServiceHelper.summaryService().getOnlineSummaryComponent(roleId, MConst.Role);
        if (roleSummaryComponent != null) {
            BeanCopier.create(RoleSummaryComponentImpl.class, EmailConditionArgs.class, false).copy(roleSummaryComponent, emailConditionArgs, null);
            emailConditionArgs.setChannel(roleSummaryComponent.getChannel());
        } else {
            try {
                emailConditionArgs = load(roleId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return emailConditionArgs;
    }

}
