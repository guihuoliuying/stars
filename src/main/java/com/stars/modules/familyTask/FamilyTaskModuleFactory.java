package com.stars.modules.familyTask;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.family.event.FamilyLeaveEvent;
import com.stars.modules.familyTask.event.FamilyTaskEvent;
import com.stars.modules.familyTask.listener.FamilyTaskListener;
import com.stars.modules.familyTask.prodata.FamilyMissionGroup;
import com.stars.modules.familyTask.prodata.FamilyMissionInfo;
import com.stars.modules.tool.event.AddToolEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuyuxing on 2017/3/28.
 */
public class FamilyTaskModuleFactory extends AbstractModuleFactory<FamilyTaskModule> {
    public FamilyTaskModuleFactory() {
        super(new FamilyTaskPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        loadMissionInfo();
        loadMissionGroup();
        loadCommondefine();
    }

    private void loadMissionInfo() throws Exception {
        String sql = "select * from familymission";
        Map<Integer, FamilyMissionInfo> missionInfoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", FamilyMissionInfo.class, sql);

        int totalOdds = 0;
        for(FamilyMissionInfo missionInfo : missionInfoMap.values()){
            totalOdds += missionInfo.getOdds();
        }

        FamilyTaskManager.MissionInfoMap = missionInfoMap;
        FamilyTaskManager.MISSION_TOTAL_ODDS = totalOdds;//总权重
    }

    private void loadMissionGroup() throws Exception {
        String sql = "select * from familymissiongroup";
        Map<Integer, FamilyMissionGroup> missionGroupMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", FamilyMissionGroup.class, sql);
        Map<Integer, List<FamilyMissionGroup>> groupListMap = new HashMap<>();
        Map<Integer,Integer> groupOdds = new HashMap<>();
        List<FamilyMissionGroup> list;
        int groupId;
        for(FamilyMissionGroup groupInfo : missionGroupMap.values()){
            groupId = groupInfo.getGroupId();
            list = groupListMap.get(groupId);
            if(list == null){
                list = new ArrayList<>();
                groupListMap.put(groupId,list);
            }
            list.add(groupInfo);
            groupOdds.put(groupId, groupInfo.getOdds() + (groupOdds.get(groupId)==null?0:groupOdds.get(groupId)));
        }
        FamilyTaskManager.MissionGroupMap = missionGroupMap;
        FamilyTaskManager.GroupListMap = groupListMap;
        FamilyTaskManager.GroupOdds = groupOdds;
    }
    
    private void loadCommondefine(){
    	FamilyTaskManager.familymission_count = Byte.parseByte(DataManager.getCommConfig("familymission_count"));
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public FamilyTaskModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new FamilyTaskModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
    	FamilyTaskListener listener = new FamilyTaskListener(module);
    	
    	eventDispatcher.reg(FamilyLeaveEvent.class, listener);
    	eventDispatcher.reg(FamilyTaskEvent.class, listener);
//    	eventDispatcher.reg(SubmitTaskEvent.class, listener);
    	eventDispatcher.reg(AddToolEvent.class, listener);
    }
}
