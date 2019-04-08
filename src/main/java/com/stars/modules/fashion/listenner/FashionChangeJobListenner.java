package com.stars.modules.fashion.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.changejob.event.ChangeJobEvent;
import com.stars.modules.fashion.FashionModule;
import com.stars.modules.fashioncard.event.FashionCardEvent;

/**
 * Created by huwenjun on 2017/6/8.
 */
public class FashionChangeJobListenner extends AbstractEventListener<FashionModule> {
    public FashionChangeJobListenner(FashionModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof ChangeJobEvent) {
            ChangeJobEvent changeJobEvent = (ChangeJobEvent) event;
            module().onChangeJob(changeJobEvent.getNewJobId());
        }
        if (event instanceof FashionCardEvent) {
            FashionCardEvent fashionCardEvent = (FashionCardEvent) event;
            if (fashionCardEvent.getCurFashionCardId() != 0) {
                module().undressFashion();
            }
        }
    }
}
