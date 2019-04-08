package com.stars.modules.rank;

import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.familyactivities.treasure.FamilyTreasureModule;
import com.stars.modules.gamecave.GameCaveModule;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.role.RoleModule;
import com.stars.modules.skytower.SkyTowerModule;
import com.stars.services.ServiceHelper;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.RoleRankPo;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/8/24.
 */
public class RankModule extends AbstractModule {
    private RoleRankPo roleRankPo;

    public RankModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("排行榜", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        if (!SpecialAccountManager.isSpecialAccount(id())) {
            roleRankPo = new RoleRankPo(id());
        } else {
            roleRankPo = new RoleRankPo();
        }
    }

    @Override
    public void onSyncData() throws Throwable {
        if (!SpecialAccountManager.isSpecialAccount(id())) {
            initRoleRankPo();
            ServiceHelper.rankService().updateRank(RankConstant.RANK_TYPE_PERSON, roleRankPo.copy());
        }
    }

    @Override
    public void onOffline() throws Throwable {
        if (!SpecialAccountManager.isSpecialAccount(id())) {
            ServiceHelper.rankService().offline(RankConstant.RANK_TYPE_PERSON, id());
        }
    }

    @Override
    public void onReconnect() throws Throwable {
        if (!SpecialAccountManager.isSpecialAccount(id())) {
            initRoleRankPo();
            ServiceHelper.rankService().updateRank(RankConstant.RANK_TYPE_PERSON, roleRankPo.copy());
        }
    }

    /**
     * 更新排行榜数据
     */
    public void updateToRank(RoleRankPo roleRankPo) {
        if (!SpecialAccountManager.isSpecialAccount(id())) {
            ServiceHelper.rankService().updateRank(RankConstant.RANK_TYPE_PERSON, roleRankPo.copy());
        }
    }

    public RoleRankPo getCurRoleRankPo() {
        return roleRankPo;
    }

    public void initRoleRankPo() {
        RoleRankPo roleRankPo = new RoleRankPo(id());
        RoleModule roleModule = module(MConst.Role);
        roleRankPo.setFightScore(roleModule.getFightScore());
        roleRankPo.setRoleLevel(roleModule.getLevel());
        GameCaveModule gameCaveModule = module(MConst.GameCave);
        roleRankPo.setGamecaveScore(gameCaveModule.getRoleGameCaveScore());
        SkyTowerModule skyTowerModule = module(MConst.SkyTower);
        roleRankPo.setSkyTowerLayerSerial(skyTowerModule.getSkyTowerLayerSerial());
        FamilyTreasureModule nftModule = module(MConst.FamilyActTreasure);
        roleRankPo.setAccDamage(nftModule.getDamage());
        this.roleRankPo = roleRankPo;
    }


    public void onRoleRename(RoleRenameEvent event) {
        ServiceHelper.rankService().updateRoleName(id(),event.getNewName());
    }
}
