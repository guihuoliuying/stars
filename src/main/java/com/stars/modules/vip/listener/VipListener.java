package com.stars.modules.vip.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.vip.VipModule;
import com.stars.modules.vip.event.VipChargeEvent;
import com.stars.modules.vip.event.VipLevelupEvent;

/**
 * Created by liuyuheng on 2016/12/6.
 */
public class VipListener extends AbstractEventListener<VipModule> {
    public VipListener(VipModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof VipLevelupEvent) {
            VipLevelupEvent vEvent = (VipLevelupEvent) event;
            module().vipLevelUpHandler(vEvent.getNewVipLevel(), vEvent.getTotalCharge());
        } else if (event instanceof VipChargeEvent) {
            VipChargeEvent vIChargeEvent = (VipChargeEvent) event;
            /**
             * actionType为1表示虚拟充值，为0表示真实充值
             */
            if (vIChargeEvent.getActionType() == 1) {
                module().consignment4Virtual(vIChargeEvent.getChargeId(), vIChargeEvent.getOrderNo(), vIChargeEvent.getMoney(), vIChargeEvent.isFirst(), vIChargeEvent.getPayPoint());
            } else {
                module().consignment(vIChargeEvent.getChargeId(), vIChargeEvent.getOrderNo(), vIChargeEvent.getMoney(), vIChargeEvent.isFirst(), vIChargeEvent.getPayPoint(),vIChargeEvent.getLastVipLevel());
            }
        }
    }
}
