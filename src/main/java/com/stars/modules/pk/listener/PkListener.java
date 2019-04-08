package com.stars.modules.pk.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.pk.PKModule;
import com.stars.modules.pk.event.*;

/**
 * Created by daiyaorong on 2016/9/2.
 */
public class PkListener implements EventListener {

    private PKModule pm;

    public PkListener(PKModule pm) {
        this.pm = pm;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof InvitePkEvent) {
            pm.receiveInvite((InvitePkEvent) event);
        }
        if (event instanceof EnterPkEvent) {
            EnterPkEvent enterPkEvent = (EnterPkEvent) event;
            pm.enterPk(enterPkEvent.getData());
        }
        if (event instanceof PermitPkEvent) {
            PermitPkEvent permitPkEvent = (PermitPkEvent) event;
            pm.receivePermit(permitPkEvent.getInvitee(), permitPkEvent.getEntityList());
        }
        if (event instanceof FinishPkEvent) {
            FinishPkEvent finishPkEvent = (FinishPkEvent) event;
            pm.finishPk(finishPkEvent.getResult(), finishPkEvent.getEnemyRoleId());
        }
        if (event instanceof UpdateReceiveInviteEvent) {
            pm.sendReceiveInvite();
        }
    }
}
