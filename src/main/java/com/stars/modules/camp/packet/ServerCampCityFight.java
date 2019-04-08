package com.stars.modules.camp.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.camp.CampModule;
import com.stars.modules.camp.CampPackset;
import com.stars.modules.camp.activity.CampActivity;
import com.stars.modules.camp.activity.imp.QiChuZhiZhengActivity;
import com.stars.network.server.buffer.NewByteBuffer;

public class ServerCampCityFight extends PlayerPacket {
	
	public static final byte REQ_MAIN_UI_INFO = 1;//请求主界面信息
	public static final byte REQ_CITY_INFO = 2;//请求选择城镇界面信息
	public static final byte REQ_MATCH_ENEMY = 3;//匹配对手
	public static final byte REQ_START_FIGHT = 4;//开始战斗
	public static final byte REQ_CONTINUE_FIGHT = 5;//继续战斗
	public static final byte REQ_CANCEL_MATCHING = 6;//取消匹配
	public static final byte REQ_BACK_TO_CITY = 7;//主动回城
	public static final byte REQ_MATCHING_TIME = 8;//获取匹配时间
	
	private byte opType;
	
	private int chaCityId;

	@Override
	public void execPacket(Player player) {
		CampModule module = module(MConst.Camp);
		QiChuZhiZhengActivity activity= (QiChuZhiZhengActivity) module.getCampActivityById(CampActivity.ACTIVITY_ID_QI_CHU_ZHI_ZHENG);
		if(opType==REQ_MAIN_UI_INFO){
			activity.openCityFightUI();
		}else if(opType==REQ_CITY_INFO){
			activity.getCityInfo();
		}else if(opType==REQ_MATCH_ENEMY){
			activity.matchEnemy(chaCityId);
		}else if(opType==REQ_START_FIGHT){
			activity.startFight();
		}else if(opType==REQ_CONTINUE_FIGHT){
			activity.nextFight();
		}else if(opType==REQ_CANCEL_MATCHING){
			activity.cancelMatching();
		}else if(opType==REQ_BACK_TO_CITY){
			activity.backToCity(true);
		}else if(opType==REQ_MATCHING_TIME){
			activity.getMatchTime((byte)0);
		}
	}
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		this.opType = buff.readByte();
		if(opType==REQ_MATCH_ENEMY){
			this.chaCityId = buff.readInt();
		}
	}

	@Override
	public short getType() {
		return CampPackset.Server_CampCityFight;
	}

}
