package com.stars.modules.poem.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.dungeon.DungeonManager;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.dungeon.userdata.RoleDungeon;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by gaopeidian on 2017/2/10.
 */
public class PoemGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        switch (args[0]) {
            case "open":
            {
            	int dungeonId = Integer.parseInt(args[1]);
            	
            	DungeonModule dungeonModule = (DungeonModule)moduleMap.get(MConst.Dungeon);
            	Map<Integer, RoleDungeon> passMap = dungeonModule.getRolePassDungeonMap();
            	RoleDungeon targetDungeon = passMap.get(dungeonId);
            	if (targetDungeon != null) {
					targetDungeon.setStatus(DungeonManager.STAGE_OPEN);
					dungeonModule.context().update(targetDungeon);
					dungeonModule.sendAllChapterData();
				}
            	
                break;
            }
        }
    
    }
}
