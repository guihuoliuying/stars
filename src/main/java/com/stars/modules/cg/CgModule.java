package com.stars.modules.cg;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.cg.packet.ClientCg;
import com.stars.modules.cg.userdata.RoleCg;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by panzhenfeng on 2017/3/7.
 */
public class CgModule  extends AbstractModule {

    private Map<String, RoleCg> roleCgMap = new HashMap<>();// 角色CG

    public CgModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("CG", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from `rolecg` where `roleid`=" + id();
        roleCgMap = DBUtil.queryMap(DBUtil.DB_USER, "cgid", RoleCg.class, sql);
    }

    @Override
    public void onSyncData() throws Throwable {
        ClientCg packet = new ClientCg();
        packet.setMap(roleCgMap);
        lazySend(packet);
    }

    public RoleCg getRoleCg(int inductId) {
        return roleCgMap.get(inductId);
    }

    public Map<String, RoleCg> getRoleCgMap() {
        return roleCgMap;
    }

    public void setFinished(String cgId){
        RoleCg roleCg;
        if (!roleCgMap.containsKey(cgId)) {
            roleCg = new RoleCg(id(), cgId);
            roleCgMap.put(cgId, roleCg);
            context().insert(roleCg);
        }
        roleCg = roleCgMap.get(cgId);
        if (roleCg.getFinished() == CgManager.CG_STATE_NOT_FINISH){
            roleCg.setFinished(CgManager.CG_STATE_FINISH);
            context().update(roleCg);
        }
        Map<String, RoleCg> map = new HashMap<>();
        map.put(cgId, roleCg);
        ClientCg packet = new ClientCg();
        packet.setMap(map);
    }
}
