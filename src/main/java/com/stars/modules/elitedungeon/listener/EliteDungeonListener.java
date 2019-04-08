package com.stars.modules.elitedungeon.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.elitedungeon.EliteDungeonModule;
import com.stars.modules.elitedungeon.event.*;
import com.stars.modules.scene.event.PassStageEvent;

/**
 * Created by gaopeidian on 2017/3/10.
 */
public class EliteDungeonListener extends AbstractEventListener<EliteDungeonModule> {
    public EliteDungeonListener(EliteDungeonModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EliteDungeonEnterFightEvent) {
        	EliteDungeonEnterFightEvent eliteDungeonEnterFightEvent = (EliteDungeonEnterFightEvent)event;
			module().handleEnterFightEvent(eliteDungeonEnterFightEvent);
		}else if (event instanceof BackToCityFromEliteDungeonEvent) {
			BackToCityFromEliteDungeonEvent backToCityFromEliteDungeonEvent = (BackToCityFromEliteDungeonEvent)event;
			module().handleBackToCityEvent(backToCityFromEliteDungeonEvent);
		}else if (event instanceof EliteDungeonFinishEvent) {
			module().finishReward(event);
		}else if (event instanceof EliteDungeonDropEvent) {
			EliteDungeonDropEvent tddEvent = (EliteDungeonDropEvent) event;
	        module().addMonsterDrop(tddEvent.getDropIds());
		}else if (event instanceof PassStageEvent) {
			PassStageEvent psEvent = (PassStageEvent) event;
	        module().handlePassStageEvent(psEvent);
		}else if(event instanceof EliteDungeonAddImageDataEvent){
			EliteDungeonAddImageDataEvent addEvent = (EliteDungeonAddImageDataEvent)event;
			module().addPlayerImageDate(addEvent.getStageId());
		}
    }
}
