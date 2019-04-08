package com.stars.modules.poemdungeon;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.baseteam.BaseTeamModule;
import com.stars.modules.poemdungeon.packet.ClientPoemDungeon;
import com.stars.modules.poemdungeon.teammember.RobotTeamMember;
import com.stars.services.baseteam.BaseTeamMember;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by gaopeidian on 2017/5/12.
 */
public class PoemDungeonModule extends AbstractModule {
    private List<RobotTeamMember> robotMembers;

    public PoemDungeonModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("精英副本", id, self, eventDispatcher, moduleMap);
    }

    public void sendTeamInfo(int dungeonId) {
        BaseTeamModule baseTeamModule = (BaseTeamModule) module(MConst.Team);
        BaseTeamMember selfMember = baseTeamModule.selfToTeamMember((byte)0);

        List<RobotTeamMember> teamMembers = new ArrayList<RobotTeamMember>();
        robotMembers = PoemDungeonManager.getRobotMembers(dungeonId);
        teamMembers.addAll(robotMembers);

        ClientPoemDungeon clientPoemDungeon = new ClientPoemDungeon();
        clientPoemDungeon.setSelfMember(selfMember);
        clientPoemDungeon.setTeamMembers(teamMembers);
        send(clientPoemDungeon);
    }

    public List<RobotTeamMember> getRobotMembers() {
        return robotMembers;
    }
}
