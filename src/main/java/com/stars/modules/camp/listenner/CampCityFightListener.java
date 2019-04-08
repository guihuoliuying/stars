package com.stars.modules.camp.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.camp.CampModule;
import com.stars.modules.camp.activity.CampActivity;
import com.stars.modules.camp.activity.imp.QiChuZhiZhengActivity;
import com.stars.modules.camp.event.CampCityFightEvent;

public class CampCityFightListener extends AbstractEventListener<CampModule> {

	public CampCityFightListener(CampModule module) {
		super(module);
	}
	
	@Override
	public void onEvent(Event event) {
		if(event instanceof CampCityFightEvent){
			CampCityFightEvent nowEvent = (CampCityFightEvent)event;
			QiChuZhiZhengActivity activity= (QiChuZhiZhengActivity) module().getCampActivityById(CampActivity.ACTIVITY_ID_QI_CHU_ZHI_ZHENG);
			byte opType = nowEvent.getOpType();
			if(opType==CampCityFightEvent.FIGHT_END){
				activity.fightEnd(nowEvent.getResult(), nowEvent.getIntegral(), nowEvent.getIntegralList(),
						nowEvent.getChaCityId(), nowEvent.isTeamAddition());
			}else if(opType==CampCityFightEvent.GET_PLAYERIMAGE){
				activity.getPlayerImageData();
			}else if(opType==CampCityFightEvent.BACK_TO_CITY){
				activity.backToCity(false);
			}else if(opType==CampCityFightEvent.CHANGE_SCENE){
				activity.changeScene(nowEvent.getScene());
			}else if(opType==CampCityFightEvent.SYN_ENEMY_INFO){
				activity.setEnemy(nowEvent.getEnemyList());
			}
		}
	}

}
