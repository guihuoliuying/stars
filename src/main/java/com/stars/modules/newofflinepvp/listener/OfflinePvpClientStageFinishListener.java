package com.stars.modules.newofflinepvp.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.newofflinepvp.NewOfflinePvpModule;
import com.stars.modules.newofflinepvp.event.OfflinePvpClientStageFinishEvent;

/**
 * Created by chenkeyu on 2017-03-13 10:49
 */
public class OfflinePvpClientStageFinishListener extends AbstractEventListener<NewOfflinePvpModule> {


    public OfflinePvpClientStageFinishListener(NewOfflinePvpModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        OfflinePvpClientStageFinishEvent finishEvent = (OfflinePvpClientStageFinishEvent) event;
        module().exitFightPacketToClient(finishEvent.getFinish(), finishEvent.getMyRank(), finishEvent.getUpdateRank());
        module().doRankChangeEvent(finishEvent.getMyRank());
    }
}
