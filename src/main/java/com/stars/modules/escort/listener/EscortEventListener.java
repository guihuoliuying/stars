package com.stars.modules.escort.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.escort.EscortModule;
import com.stars.modules.escort.event.*;

/**
 * Created by wuyuxing on 2016/12/7.
 */
public class EscortEventListener extends AbstractEventListener<Module> {

    private EscortModule escortModule;

    public EscortEventListener(Module module) {
        super(module);
        escortModule = (EscortModule) module;
    }

    @Override
    public void onEvent(Event event) {
        if(event instanceof EnterEscortSceneEvent){
            EnterEscortSceneEvent sceneEvent = (EnterEscortSceneEvent) event;
            escortModule.enterEscortScene(sceneEvent.getData());
        }else if(event instanceof EnterEscortSafeSceneEvent){
            EnterEscortSafeSceneEvent sceneEvent = (EnterEscortSafeSceneEvent) event;
            escortModule.realEnterEscortSafeScene(sceneEvent.getData());
        }else if(event instanceof CheckRobTimesBackCityEvent){
            escortModule.handleCheckRobTimesEvent();
        }else if(event instanceof EnterCargoListSceneEvent){
            escortModule.handleEnterCargoListSceneEvent();
        }else if(event instanceof NoticeConsumeMaskEvent){
            escortModule.consumeMask();//扣除劫镖使用面具
        }else if(event instanceof NoticeServerAddEnemyRecordEvent){
            NoticeServerAddEnemyRecordEvent enemyRecordEvent = (NoticeServerAddEnemyRecordEvent) event;
            escortModule.addEnemyRecord(enemyRecordEvent.getEnemyList());
        }else if(event instanceof NoticeServerAddEscortAwardEvent){
            NoticeServerAddEscortAwardEvent escortAwardEvent = (NoticeServerAddEscortAwardEvent) event;
            escortModule.handleAddEscortAwardEvent(escortAwardEvent.getSubType(),escortAwardEvent.getAward(),escortAwardEvent.getCarId(),escortAwardEvent.getIndex());
        }
    }
}
