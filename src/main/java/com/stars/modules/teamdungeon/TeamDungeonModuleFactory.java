package com.stars.modules.teamdungeon;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.baseteam.BaseTeamManager;
import com.stars.modules.baseteam.handler.*;
import com.stars.modules.data.DataManager;
import com.stars.modules.teamdungeon.event.*;
import com.stars.modules.teamdungeon.listener.*;
import com.stars.modules.teamdungeon.prodata.TeamDungeonVo;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/11/11.
 */
public class TeamDungeonModuleFactory extends AbstractModuleFactory<TeamDungeonModule> {
    public TeamDungeonModuleFactory() {
        super(new TeamDungeonPacketSet());
    }

    @Override
    public void init() throws Exception {
        BaseTeamManager.registerTeamHandler(BaseTeamManager.TEAM_TYPE_DAILYDUNGEON, DailyDungeonTeamHandler.class);
        BaseTeamManager.registerTeamHandler(BaseTeamManager.TEAM_TYPE_ELITEDUNGEON, EliteDungeonTeamHandler.class);
        BaseTeamManager.registerTeamHandler(BaseTeamManager.TEAM_TYPE_FAMILYINVADE, FamilyInvadeTeamHandler.class);
        BaseTeamManager.registerTeamHandler(BaseTeamManager.TEAM_TYPE_ESCORT, EscortTeamHandler.class);
        BaseTeamManager.registerTeamHandler(BaseTeamManager.TEAM_TYPE_CARGO_ROB, CargoRobTeamHandler.class);
    }

    @Override
    public TeamDungeonModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new TeamDungeonModule(id, self, eventDispatcher, map);
    }

    @Override
    public void loadProductData() throws Exception {
        Map<Integer, TeamDungeonVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "teamdungeonid", TeamDungeonVo.class,
                "select * from teamdungeon");
        TeamDungeonManager.teamDungeonVoMap = map;
        String str = DataManager.getCommConfig("playerteam_num");
        String ss[] = str.split("[+]");
        TeamDungeonManager.minTeamCount = Byte.parseByte(ss[0]);
        TeamDungeonManager.maxTeamCount = Byte.parseByte(ss[1]);
        str = DataManager.getCommConfig("playerteam_searchtime");
        TeamDungeonManager.matchTeamTime = Long.parseLong(str);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(TeamDungeonDropEvent.class, new TeamDungeonDropListener((TeamDungeonModule) module));
        eventDispatcher.reg(TeamDungeonFinishEvent.class, new TeamDungeonFinishListener((TeamDungeonModule) module));
        eventDispatcher.reg(DeadInTeamDungeonEvent.class, new DeadInTeamDungeonListener((TeamDungeonModule) module));
        eventDispatcher.reg(BackToCityFromTeamDungeonEvent.class,
                new BackToCityFromTeamDungeonListener((TeamDungeonModule) module));
        eventDispatcher.reg(TeamDungeonEnterEvent.class, new TeamDungeonEnterListener((TeamDungeonModule) module));
        eventDispatcher.reg(TeamDungeonExitEvent.class, new TeamDungeonExitListener((TeamDungeonModule) module));
    }
}
