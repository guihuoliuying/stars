package com.stars.modules.teampvpgame;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.baseteam.BaseTeamManager;
import com.stars.modules.baseteam.handler.CouplePvpTeamHandler;
import com.stars.modules.changejob.event.ChangeJobEvent;
import com.stars.modules.data.DataManager;
import com.stars.modules.teampvpgame.event.SignUpEvent;
import com.stars.modules.teampvpgame.listener.ChangeJobListenner;
import com.stars.modules.teampvpgame.listener.TeamPvpGameListener;
import com.stars.modules.teampvpgame.prodata.DoublePVPConfigVo;
import com.stars.modules.teampvpgame.prodata.DoublePVPRewardVo;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/12/15.
 */
public class TeamPVPGameModuleFactory extends AbstractModuleFactory<TeamPVPGameModule> {
    public TeamPVPGameModuleFactory() {
        super(new TeamPVPGamePacketSet());
    }

    @Override
    public void init() throws Exception {
        BaseTeamManager.registerTeamHandler(BaseTeamManager.TEAM_TYPE_COUPLE_PVP, CouplePvpTeamHandler.class);
    }

    @Override
    public TeamPVPGameModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new TeamPVPGameModule(id, self, eventDispatcher, map);
    }

    @Override
    public void loadProductData() throws Exception {
        loadCommondefine();
        loadTPGRewardVo();
        String sql = "select * from `doublepvpconfig`; ";
        TeamPVPGameManager.pvpConfigVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "type", DoublePVPConfigVo.class, sql);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(SignUpEvent.class, new TeamPvpGameListener((TeamPVPGameModule) module));
        eventDispatcher.reg(ChangeJobEvent.class, new ChangeJobListenner((TeamPVPGameModule) module));
    }

    private void loadCommondefine() throws Exception {
        TeamPVPGameManager.levelExcursion = Integer.parseInt(DataManager.getCommConfig("doublepvp_recruit_levelrange"));
        TeamPVPGameManager.signUpConfirmTime = 1000L *
                Integer.parseInt(DataManager.getCommConfig("doublepvp_signup_holdtime"));
        TeamPVPGameManager.scoreRankLimit = Integer.parseInt(DataManager.getCommConfig("doublepvp_scorebattle_ranknum"));
    }

    private void loadTPGRewardVo() throws SQLException {
        String sql = "select * from `doublepvpreward`; ";
        Map<Integer, List<DoublePVPRewardVo>> map = new HashMap<>();
        List<DoublePVPRewardVo> list = DBUtil.queryList(DBUtil.DB_PRODUCT, DoublePVPRewardVo.class, sql);
        for (DoublePVPRewardVo vo : list) {
            List<DoublePVPRewardVo> voList = map.get(vo.getRewardType());
            if (voList == null) {
                voList = new LinkedList<>();
                map.put(vo.getRewardType(), voList);
            }
            voList.add(vo);
        }
        TeamPVPGameManager.pvpRewardVoMap = map;
    }
}
