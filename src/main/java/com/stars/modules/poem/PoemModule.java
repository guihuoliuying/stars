package com.stars.modules.poem;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.dungeon.DungeonManager;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.dungeon.prodata.DungeoninfoVo;
import com.stars.modules.dungeon.prodata.WorldinfoVo;
import com.stars.modules.dungeon.userdata.RoleDungeon;
import com.stars.modules.poem.packet.ClientPoem;
import com.stars.modules.poem.packet.ClientPoemBoss;
import com.stars.modules.poem.prodata.PoemVo;
import com.stars.modules.poem.userdata.PoemData;
import com.stars.modules.scene.event.PassStageEvent;
import com.stars.util.LogUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by gaopeidian on 2017/1/9.
 */
public class PoemModule extends AbstractModule {
	public PoemModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
		super(MConst.Poem, id, self, eventDispatcher, moduleMap);
	}

	@Override
	public void onCreation(String name_, String account_) throws Throwable {

	}

	@Override
	public void onInit(boolean isCreation) {

	}

	@Override
	public void onDataReq() throws Exception {

	}

	@Override
	public void onSyncData() {
		sendCurPoemData();
	}

	public void sendAllPoemData(){
		DungeonModule dungeonModule = (DungeonModule)module(MConst.Dungeon);
		Map<Integer, RoleDungeon> rolePassDungeonsMap = dungeonModule.getRolePassDungeonMap();

		Map<Integer, PoemData> poems = new LinkedHashMap<>();
		List<PoemVo> poemVoList = PoemManager.getPoemVoList();

		for (PoemVo vo : poemVoList) {
			PoemData data = getPoemData(vo.getPoemsId(), rolePassDungeonsMap);
			if (data != null) {
				poems.put(data.poemId, data);
			}
		}

		ClientPoem clientPoem = new ClientPoem();
		clientPoem.setFlag(ClientPoem.Flag_Send_All_Poem_Data);
		clientPoem.setPoemDatas(poems);
		send(clientPoem);
	}

	public void sendCurPoemData(){
		PoemData data = getCurPoemData();
		if (data == null) {
			data = new PoemData(-1, 0, 0, 0, "", "", "", 0, "", "", 0);
		}

		ClientPoem clientPoem = new ClientPoem();
		clientPoem.setFlag(ClientPoem.Flag_Send_Cur_Poem);
		clientPoem.setPoemData(data);
		send(clientPoem);
	}

	public void handlePassStageEvent(PassStageEvent event){
		if (event.getIsFirstPass()) {
			int dungeonId = event.getStageId();
			DungeoninfoVo infoVo = DungeonManager.getDungeonVo(dungeonId);
			if (infoVo == null) {
				com.stars.util.LogUtil.info("PoemModule.handlePassStageEvent get no infoVo,dungeonId=" + dungeonId);
				return;
			}

			int worldId = infoVo.getWorldId();
			PoemVo poemVo = PoemManager.getPoemVoByWorldId(worldId);
			if (poemVo == null) {
				com.stars.util.LogUtil.info("PoemModule.handlePassStageEvent get no poemVo,dungeonId=" + dungeonId);
				return;
			}

			DungeonModule dungeonModule = (DungeonModule)module(MConst.Dungeon);
			Map<Integer, RoleDungeon> rolePassDungeonsMap = dungeonModule.getRolePassDungeonMap();

			//通知客户端诗集进度更新
			PoemData updateData = getPoemData(poemVo.getPoemsId(), rolePassDungeonsMap);
			ClientPoem clientPoem = new ClientPoem();
			clientPoem.setFlag(ClientPoem.Flag_Update_Poem);
			clientPoem.setPoemData(updateData);
			send(clientPoem);

			//更新当前诗集并通知客户端
			sendCurPoemData();

		}
	}

	public boolean isPoemDungeonActive(int bossDungeonId){
		DungeonModule dungeonModule = (DungeonModule)module(MConst.Dungeon);
		Map<Integer, RoleDungeon> rolePassDungeonsMap = dungeonModule.getRolePassDungeonMap();
		
		DungeoninfoVo bossDungeoninfoVo = DungeonManager.getDungeonVo(bossDungeonId);
		if (bossDungeoninfoVo == null) return false;
		
		Map<Byte, DungeoninfoVo> dungeonInfoVos = DungeonManager.getDungeonVoByWorldId(bossDungeoninfoVo.getWorldId());
		if (dungeonInfoVos == null) return false; 
		
		int finishCount = 0;
		int totalCount = 0;
		for (DungeoninfoVo dungeonInfoVo : dungeonInfoVos.values()) {
			totalCount ++;
			if (rolePassDungeonsMap != null && rolePassDungeonsMap.get(dungeonInfoVo.getDungeonId()) != null) {
				finishCount ++;
			}
		}
		
		return finishCount >= totalCount - 1;
	}
	
	PoemData getPoemData(int poemId , Map<Integer, RoleDungeon> rolePassDungeonsMap){
		PoemVo poemVo = PoemManager.getPoemVo(poemId);
		if (poemVo == null) {
			com.stars.util.LogUtil.info("PoemModule.getPoemData get no poemVo poemId = " + poemId);
			return null;
		}
		int worldId = poemVo.getWorldId();
		WorldinfoVo worldinfoVo = DungeonManager.getChapterVo(worldId);
		if (worldinfoVo == null) {
			com.stars.util.LogUtil.info("PoemModule.sendAllPoemData get no worldinfoVo worldId = " + worldId);
			return null;
		}
		DungeoninfoVo bossDungeoninfoVo = DungeonManager.getBossDungeonInfoVo(worldId);
		if (bossDungeoninfoVo == null) {
			com.stars.util.LogUtil.info("PoemModule.sendAllPoemData get no bossDungeoninfoVo worldId = "  + worldId);
			return null;
		}

		Map<Byte, DungeoninfoVo> dungeonInfoVos = DungeonManager.getDungeonVoByWorldId(worldId);
		int finishCount = 0;
		int totalCount = 0;
		for (DungeoninfoVo dungeonInfoVo : dungeonInfoVos.values()) {
			totalCount ++;
			if (rolePassDungeonsMap != null && rolePassDungeonsMap.get(dungeonInfoVo.getDungeonId()) != null) {
				finishCount ++;
			}
		}

		PoemData data = new PoemData(poemId, finishCount, totalCount,
				bossDungeoninfoVo.getDungeonId(), worldinfoVo.getTitle(), worldinfoVo.getName(),
				bossDungeoninfoVo.getGeneralDrop(), bossDungeoninfoVo.getRecommend(),
				poemVo.getShowItem(), poemVo.getShowDescWin() , bossDungeoninfoVo.getTeamtype());

		return data;
	}

	PoemData getCurPoemData(){
		DungeonModule dungeonModule = (DungeonModule)module(MConst.Dungeon);
		Map<Integer, RoleDungeon> rolePassDungeonsMap = dungeonModule.getRolePassDungeonMap();

		List<PoemVo> poemVoList = PoemManager.getPoemVoList();

		for (PoemVo vo : poemVoList) {
			PoemData data = getPoemData(vo.getPoemsId(), rolePassDungeonsMap);
			if (data != null && data.finishDungeonCount < data.totalDungeonCount) {
				return data;
			}
		}

		return null;
	}

	public void sendDungeonInfoVo(int dungeonId){
		DungeoninfoVo infoVo = DungeonManager.getDungeonVo(dungeonId);
		if (infoVo == null) {
			LogUtil.info("PoemModule.sendDungeonInfoVo infoVo is null,dungeonId=" + dungeonId);
			return;
		}

		ClientPoemBoss clientPoemBoss = new ClientPoemBoss();
		clientPoemBoss.setBossDungeonInfoVo(infoVo);
		send(clientPoemBoss);
	}
}

