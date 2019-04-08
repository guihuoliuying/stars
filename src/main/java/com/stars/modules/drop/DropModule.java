package com.stars.modules.drop;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.userdata.Role;

import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/6/30.
 */
public class DropModule extends AbstractModule {

    public DropModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("掉落", id, self, eventDispatcher, moduleMap);
    }

    /**
     * 掉落外部入口
     *
     * @param dropGroup 掉落组Id
     * @param times     执行次数
     * @return 合并后的物品map itemId-number
     */
    public Map<Integer, Integer> executeDrop(int dropGroup, int times, boolean showReport) {
        RoleModule roleModule = module(MConst.Role);
        Role role = roleModule.getRoleRow();
        return DropUtil.executeDrop(dropGroup, id(), role.getLevel(), role.getJobId(), role.getFightScore(), role.getName(), showReport, times);
    }

    public List<Map<Integer, Integer>> executeDropNotCombine(int dropGroup, int times, boolean showReport) {
        RoleModule roleModule = module(MConst.Role);
        Role role = roleModule.getRoleRow();
        return DropUtil.executeDropNotCombine(dropGroup, id(), role.getLevel(), role.getJobId(), role.getFightScore(), role.getName(), showReport, times);
    }

    /**
     * 根据掉落组、等级、职业获取showItem
     */
    public Map<Integer, Integer> getShowItemByDropGroup(int dropGroup) {
        RoleModule roleModule = module(MConst.Role);
        return DropUtil.getShowItemByDropGroup(dropGroup, roleModule.getLevel(), roleModule.getRoleRow().getJobId(), roleModule.getFightScore());
    }

}
