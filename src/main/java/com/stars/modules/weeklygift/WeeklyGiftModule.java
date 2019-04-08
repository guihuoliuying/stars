package com.stars.modules.weeklygift;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.role.RoleModule;
import com.stars.modules.vip.VipModule;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-06-28.
 */
public class WeeklyGiftModule extends AbstractModule implements OpActivityModule {
    public WeeklyGiftModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public int getCurShowActivityId() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_WeeklyGift);
        if (curActivityId != -1) {
            OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
            OperateActivityModule operateActivityModule = module(MConst.OperateActivity);
            boolean show = operateActivityModule.isShow(operateActVo.getRoleLimitMap());
            LogUtil.info("show:{},curId:{}", show, curActivityId);
            if (show) {
                return curActivityId;
            } else {
                return -1;
            }
        }
        return curActivityId;
    }

    @Override
    public byte getIsShowLabel() {
        return 0;
    }

    public void initRoleData() {
        VipModule vip = module(MConst.Vip);
        RoleModule role = module(MConst.Role);
        ServiceHelper.weeklyGiftService().initRoleData(id(), vip.getVipLevel(), role.getLevel(), vip.getDailyChargeSum());
    }

    public void doCharge(int money) {
        VipModule vip = module(MConst.Vip);
        RoleModule role = module(MConst.Role);
        ServiceHelper.weeklyGiftService().doCharge(id(), vip.getVipLevel(), role.getLevel(), money);
    }
}
