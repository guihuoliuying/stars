package com.stars.modules.family.gm;

import com.stars.core.SystemRecordMap;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.gm.GmHandler;
import com.stars.services.ServiceHelper;
import com.stars.services.family.FamilyAuth;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/27.
 */
public class FamilyGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        if (args == null && args.length == 0) {
            throw new IllegalArgumentException("");
        }
        FamilyModule familyModule = (FamilyModule) moduleMap.get(MConst.Family);
        FamilyAuth auth = familyModule.getAuth();
        switch (args[0]) {
            case "create": // 创建家族
                familyModule.createFamily(args[1], args[2]);
                break;
            case "apply": // 申请加入
                familyModule.apply(Long.parseLong(args[1]));
                break;
            case "verify": // 审核申请
                familyModule.verify(Long.parseLong(args[1]), Boolean.parseBoolean(args[2]));
                break;
            case "kickout": // 强制踢出
                familyModule.kickOut(Long.parseLong(args[1]));
                break;
            case "leave": // 主动退出
                familyModule.leave();
                break;
            case "abdicate": // 禅让（退位）
                familyModule.abdicate(Long.parseLong(args[1]));
                break;
            case "appoint": // 任命
                familyModule.appoint(Long.parseLong(args[1]), Byte.parseByte(args[2]));
                break;
            case "donatemoney": // 捐献（银币）
                familyModule.donate();
                break;
            case "donatermb": // 捐献（元宝）
                familyModule.donateRmb();
                break;
            case "lock": // 锁定家族
                if (auth.getFamilyId() == 0) {
                    familyModule.warn("没有家族");
                    break;
                }
                ServiceHelper.familyMainService().lockFamily(familyModule.getAuth().getFamilyId());
                break;
            case "unlock":
                if (auth.getFamilyId() == 0) {
                    familyModule.warn("没有家族");
                    break;
                }
                ServiceHelper.familyMainService().unlockFamily(familyModule.getAuth().getFamilyId());
                break;

            case "halflock":
                if (auth.getFamilyId() == 0) {
                    familyModule.warn("没有家族");
                    break;
                }
                ServiceHelper.familyMainService().halfLockFamily(familyModule.getAuth().getFamilyId());
                break;
            /* 作弊 */
            case "addcontribution":
                ServiceHelper.familyRoleService().addAndSendContribution(roleId, 100000000);
                break;
            case "addmoney":
                ServiceHelper.familyMainService().addMoneyAndUpdateContribution(
                        familyModule.getAuth(), roleId, 100000000, 100000000, SystemRecordMap.dateVersion, 0);
                break;
        }
    }

}
