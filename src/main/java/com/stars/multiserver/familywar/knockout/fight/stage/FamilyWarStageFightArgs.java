package com.stars.multiserver.familywar.knockout.fight.stage;

import java.util.List;
import java.util.Map;

public class FamilyWarStageFightArgs {
	private String battleId;
	private int mainServerId;
	private List<Long> roleIds;
    private Map<Long, Integer> roleWarType;

    public Map<Long, Integer> getRoleWarType() {
        return roleWarType;
    }

    public void setRoleWarType(Map<Long, Integer> roleWarType) {
        this.roleWarType = roleWarType;
    }

	public String getBattleId() {
		return battleId;
	}

	public void setBattleId(String battleId) {
		this.battleId = battleId;
	}

	public List<Long> getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(List<Long> roleIds) {
		this.roleIds = roleIds;
	}

	public int getMainServerId() {
		return mainServerId;
	}

	public void setMainServerId(int mainServerId) {
		this.mainServerId = mainServerId;
	}
}
