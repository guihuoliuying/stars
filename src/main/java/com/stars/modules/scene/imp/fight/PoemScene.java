package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.dungeon.DungeonManager;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.dungeon.prodata.DungeoninfoVo;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.event.PassStageEvent;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.tool.ToolModule;
import com.stars.util.MapUtil;

import java.util.Map;

/**
 * 诗歌关卡场景;
 * Created by gaopeidian on 2017/2/7.
 */
public class PoemScene extends DungeonScene{
    @Override
    public void finishDeal(Map<String, Module> moduleMap, byte finish) {
        this.endTimestamp = System.currentTimeMillis();
        // 关卡掉落
        Map<Integer, Integer> rewardMap = totalDropMap;
        DungeonModule dungeonModule = (DungeonModule) moduleMap.get(MConst.Dungeon);
        byte star = 0;
        int juci = dungeonModule.getDungeonCount(dungeonId);
        DungeoninfoVo dungeonVo = DungeonManager.getDungeonVo(dungeonId);
        // 胜利通关
        String goType="1";
        boolean isFirstPass = false;
        StringBuffer info = new StringBuffer();
        if (finish == SceneManager.STAGE_VICTORY) {
        	if(dungeonModule.isFirstPass(dungeonId)){
            	goType = "l";
            	isFirstPass = true;
            }else{
            	goType = "2";
            	isFirstPass = false;
            }
            // 评星
            star = calStar(dungeonModule.id());
            // 通关奖励+首通奖励
            MapUtil.add(rewardMap, dungeonModule.getPassReward(dungeonId));
            dungeonModule.passDungeon(dungeonId, star);
            
            info.append("fight_time:");
            info.append((this.endTimestamp - this.startTimestamp) / 1000).append("#sp_case:");
            info.append(dungeonModule.getRoleMaxDungeonId((byte)0)).append("#nm_case:").append(dungeonModule.getRoleMaxDungeonId((byte)1));
            //日志
            ServerLogModule log = (ServerLogModule) moduleMap.get(MConst.ServerLog);
            log.Log_core_case(ThemeType.DUNGEON_WIN.getOperateId(), ThemeType.DUNGEON_WIN.getOperateName(), "1", juci + "", dungeonId + "", info.toString(),goType,dungeonVo.getBossIcon()+1);
        } else if (finish == SceneManager.STAGE_FAIL) {//挑战失败
            info.append("fight_time:");
            info.append((this.endTimestamp - this.startTimestamp) / 1000).append("#sp_case:");
            info.append(dungeonModule.getRoleMaxDungeonId((byte)0)).append("#nm_case:").append(dungeonModule.getRoleMaxDungeonId((byte)1));
            ServerLogModule log = (ServerLogModule) moduleMap.get(MConst.ServerLog);
            log.Log_core_case(ThemeType.DUNGEON_FAIL.getOperateId(), ThemeType.DUNGEON_FAIL.getOperateName(), "1", juci + "", dungeonId + "", info.toString(),goType,dungeonVo.getBossIcon()+1);
        }
        ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
        Map<Integer,Integer> map = toolModule.addAndSend(rewardMap, EventType.DUNGEONSCENE.getCode());
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        int jobId = roleModule.getRoleRow().getJobId();

        // 自动使用宝箱类型物品合并显示
        switchBoxTool(map, jobId);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        ClientStageFinish clientStageFinish = new ClientStageFinish(SceneManager.SCENETYPE_POEM, finish);
        clientStageFinish.setStar(star);
        clientStageFinish.setItemMap(map);
        clientStageFinish.setUseTime((int) Math.floor((endTimestamp - startTimestamp) / 1000.0));
        sceneModule.send(clientStageFinish);
        if (finish == SceneManager.STAGE_VICTORY) { 
            sceneModule.dispatchEvent(new PassStageEvent(this.dungeonId, star, isFirstPass));
        }
    }
}
