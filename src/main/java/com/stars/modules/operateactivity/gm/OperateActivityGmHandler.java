package com.stars.modules.operateactivity.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.skill.SkillModule;
import com.stars.util.DateUtil;

import java.util.Date;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/11/21.
 */
public class OperateActivityGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        OperateActivityModule operateActivityModule = (OperateActivityModule) moduleMap.get(MConst.OperateActivity);
        switch (args[0]) {
            case "test": {

                break;
            }
            case "test2": {
                String createTime = args[1];
                Date date = DateUtil.toDate(createTime);
                RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
                roleModule.getRoleRow().setCreateTime(date.getTime());

                break;
            }
            case "test3": {
                int activityId = Integer.parseInt(args[1]);
                break;
            }
            case "test4": {
                int activityId = Integer.parseInt(args[1]);
                int signRewardId = Integer.parseInt(args[2]);

                break;
            }
            case "test5": {

                SkillModule skillModule = (SkillModule) moduleMap.get(MConst.Skill);
                int lv = skillModule.getUseSkillLvTotal();
                System.out.println("=======================================================================");
                System.out.println("=======================================================================");
                System.out.println("=================================" + lv + "================================");
                System.out.println("=======================================================================");
                System.out.println("=======================================================================");


                break;
            }
            case "test6": {
                int param1 = Integer.parseInt(args[1]);


                break;
            }
            case "loopReset": {
            }
            break;
        }
    }

}