package com.stars.services.fightingmaster.data;

import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.multiserver.fightingmaster.Fighter;
import com.stars.multiserver.fightingmaster.FightingMasterRPC;
import com.stars.multiserver.fightingmaster.Robot;
import com.stars.util.LogUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhouyaohui on 2016/11/9.
 */
public class Fighting {

    private String fightId;
    private int needReady = 0;  // 需要准备的次数
    private Set<Fighter> fighterSet = new HashSet<>();
    private boolean ready = false;
    private int fightServer;

    public Fighting(String id) {
        fightId = id;
    }

    public String getFightId() {
        return fightId;
    }

    public void addFighter(Fighter fighter) {
        if (!(fighter instanceof Robot)) {
            needReady++;
        }
        fighterSet.add(fighter);
    }

    public Set<Fighter> getFighterSet() {
        return fighterSet;
    }

    public Fighter getFighter(String key) {
        for (Fighter fighter : fighterSet) {
            if (fighter.getRoleId().equals(key)) {
                return fighter;
            }
        }
        return null;
    }

    public Fighter getOther(String key) {
        Fighter other = null;
        for (Fighter fighter : fighterSet) {
            if (fighter.getRoleId().equals(key)) {
                continue;
            } else {
                other = fighter;
                break;
            }
        }
        return other;
    }

    public boolean isReady() {
        return ready;
    }

    public void ready() {
        ready = true;
        needReady--;
        if (needReady <= 0) {
            LogUtil.info("通知战斗服战斗开始");
//            NoticeFightServerReady ready = new NoticeFightServerReady();
//            ready.setFightId(fightId);
//            ready.setServerId(MultiServerHelper.getServerId());
//            ready.setData(Ints.toByteArray(20210));
//            FightingMasterRPC.fightBaseService().noticeFightServerReady(MultiServerHelper.getFightServer(),
//                    MultiServerHelper.getServerId(), ready);

            FightingMasterRPC.fightBaseService().readyFight(
                    fightServer, FightConst.T_FIGHTING_MASTER, MultiServerHelper.getServerId(), fightId);
        }
    }

	public int getFightServer() {
		return fightServer;
	}

	public void setFightServer(int fightServer) {
		this.fightServer = fightServer;
	}
}
