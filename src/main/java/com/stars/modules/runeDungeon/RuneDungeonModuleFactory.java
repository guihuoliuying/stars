package com.stars.modules.runeDungeon;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.runeDungeon.event.RuneDungeonHelpAwardEvent;
import com.stars.modules.runeDungeon.listener.RuneDungeonListener;
import com.stars.modules.runeDungeon.proData.RuneDungeonStageInfo;
import com.stars.modules.runeDungeon.proData.RuneDungeonVo;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuneDungeonModuleFactory extends AbstractModuleFactory<RuneDungeonModule> {

	public RuneDungeonModuleFactory() {
		super(new RuneDungeonPacketSet());
	}
	
	@Override
	public RuneDungeonModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
		// TODO Auto-generated method stub
		return new RuneDungeonModule(id, self, eventDispatcher, map);
	}
	
	@Override
	public void loadProductData() throws Exception {
		RuneDungeonManager.Boss_Buff = DataManager.getCommConfig("tokendungeonrankbuff", 0);
		String restTimeStr = DataManager.getCommConfig("tokendungeonresttime");
		String[] timeArr = restTimeStr.split("[+]");
		RuneDungeonManager.RelaxTime = Integer.parseInt(timeArr[0])*3600+Integer.parseInt(timeArr[1])*60+Integer.parseInt(timeArr[2]);
		RuneDungeonManager.HelpAwardLimit = DataManager.getCommConfig("tokendungeonhelplimit", 0);
		
		loadRuneDungeon();
	}
	
	private void loadRuneDungeon() throws SQLException{
		String sql = "select * from tokendungeon order by tokendungeonid";
		List<RuneDungeonVo> list = DBUtil.queryList(DBUtil.DB_PRODUCT, RuneDungeonVo.class, sql);
		Map<Integer, RuneDungeonVo> dungeonMap = new HashMap<>();
		RuneDungeonVo vo = null;
		List<Integer> stageIdList = null;
		List<Integer> recommendList = null;
		List<Integer> singleKillDropList = null;
		List<Integer> showModelList = null;
		List<String> showNameList = null;
		List<String> showIconList = null;
		int totalSize = list.size();
		int size = 0;
		int stageId = 0;
		for(int j=0;j<totalSize;j++){
			vo = list.get(j);
			dungeonMap.put(vo.getTokendungeonId(), vo);
			stageIdList = vo.getStageIdList();
			recommendList = vo.getRecommendList();
			singleKillDropList = vo.getSingleKillDropList();
			showModelList = vo.getShowModelList();
			showNameList = vo.getShowNameList();
			showIconList = vo.getShowIconList();
			Map<Integer, RuneDungeonStageInfo> stageInfoMap = new HashMap<Integer, RuneDungeonStageInfo>();
			size = stageIdList.size();
			for(int i=0;i<size;i++){
				stageId = stageIdList.get(i);
				stageInfoMap.put(stageId, new RuneDungeonStageInfo(stageId, recommendList.get(i), 
						singleKillDropList.get(i), showModelList.get(i), showNameList.get(i), showIconList.get(i)));
			}
			vo.setStageInfoMap(stageInfoMap);
		}
		RuneDungeonManager.runeDungeonList = list;
		RuneDungeonManager.runeDungeonMap = dungeonMap;
	}
	
	@Override
	public void registerListener(EventDispatcher eventDispatcher, Module module) {
		RuneDungeonListener listener = new RuneDungeonListener((RuneDungeonModule)module);
		eventDispatcher.reg(RuneDungeonHelpAwardEvent.class, listener);
	}

}
