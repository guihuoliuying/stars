package com.stars.modules.induct;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.induct.event.InductEvent;
import com.stars.modules.induct.packet.ClientInduct;
import com.stars.modules.induct.packet.ServerInduct;
import com.stars.modules.induct.prodata.InductVo;
import com.stars.modules.induct.userdata.RoleInduct;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/8/18.
 */
public class InductModule extends AbstractModule {
    private Map<Integer, RoleInduct> roleInductMap = new HashMap<>();// 角色引导

    public InductModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("引导", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from `roleinduct` where `roleid`=" + id();
        roleInductMap = DBUtil.queryMap(DBUtil.DB_USER, "inductid", RoleInduct.class, sql);
    }

    @Override
    public void onSyncData() throws Throwable {
        // send to client
        ClientInduct packet = new ClientInduct(ClientInduct.LOGIN_SYNC);
        packet.setMap(roleInductMap);
        lazySend(packet);
    }

    public RoleInduct getRoleInduct(int inductId) {
        return roleInductMap.get(inductId);
    }

    public Map<Integer, RoleInduct> getRoleInductMap() {
        return roleInductMap;
    }

    /**
     * 判断能否触发
     * 1.没有触发过一定触发
     * 2.触发过需要根据循环配置处理
     *
     * @param inductId
     * @return
     */
    public boolean canTrigger(int inductId) {
        InductVo inductVo = InductManager.getInductVo(inductId);
        RoleInduct roleInduct = roleInductMap.get(inductId);
        if (roleInduct == null)// 没触发过
            return true;
        // 触发过需要根据循环配置处理
        switch (inductVo.getLoopType()) {
            case InductManager.LOOP_TYPE_ONCE:// 只触发完成一次
                return false;
            case InductManager.LOOP_TYPE_REPEAT:// 可重复触发,重复完成
                return true;
            default:
                return false;
        }
    }

    /**
     * 判断能否完成
     *
     * @param inductId
     * @return
     */
    public boolean canFinish(int inductId) {
        if (!roleInductMap.containsKey(inductId))
            return false;
        return roleInductMap.get(inductId).getFinished() == InductManager.INDUCT_STATE_NOT_FINISH;
    }

    /**
     * 服务端触发引导
     *
     * @param inductId
     */
    public void inductTrigger(int inductId) {
        if (!canTrigger(inductId)) return;
        // 通知客户端触发引导
    }

    /**
     * 触发引导更新
     *
     * @param inductId
     */
    public void triggerUpdate(int inductId) {
        InductVo inductVo = InductManager.getInductVo(inductId);
        boolean isValid = false;
        do{
            if (inductVo == null){
                break;
            }
            if (!canTrigger(inductId)) {
                break;
            }
            if (roleInductMap.containsKey(inductId)) {
                break;
            }
            isValid = true;
        }while(false);
        ClientInduct packet = null;
        if(isValid){
            RoleInduct roleInduct = new RoleInduct(id(), inductId);
            roleInductMap.put(inductId, roleInduct);
            context().insert(roleInduct);
            // send to client
            packet = new ClientInduct(ClientInduct.UPDATE_INDUCT);
            packet.setRoleInduct(roleInduct);
        }else{
            packet = new ClientInduct(ClientInduct.RESPONSE_NONE_INDUCT);
            packet.setInductId(inductId);
        }
        send(packet);
    }

    public void finish(int inductId){
        finish(inductId, true);
    }

    public void forceFinish(int inductId){
        RoleInduct roleInduct;
        if (!roleInductMap.containsKey(inductId)) {
            roleInduct = new RoleInduct(id(), inductId);
            roleInductMap.put(inductId, roleInduct);
            context().insert(roleInduct);
        }
        roleInduct = roleInductMap.get(inductId);
        roleInduct.setFinished(InductManager.INDUCT_STATE_FORCE_FINISH);
        context().update(roleInduct);
    }
    /**
     * 完成引导
     *
     * @param inductId
     */
    public void finish(int inductId, boolean needSendToClient) {
        InductVo inductVo = InductManager.getInductVo(inductId);
        if (inductVo == null) return;
        // 可重复完成的引导不置为完成状态
        if (inductVo.getLoopType() == InductManager.LOOP_TYPE_REPEAT || !canFinish(inductId)){
        	ClientInduct packet = new ClientInduct(ClientInduct.RESPONSE_NONE_INDUCT);
            packet.setInductId(inductId);
            send(packet);
            return;
        }
        RoleInduct roleInduct = roleInductMap.get(inductId);
        roleInduct.setFinished(InductManager.INDUCT_STATE_FINISH);
        context().update(roleInduct);
        // send to client
        if(needSendToClient){
            ClientInduct packet = new ClientInduct(ClientInduct.UPDATE_INDUCT);
            packet.setRoleInduct(roleInduct);
            send(packet);
        }
    }
    
    public void handleInductEvent(InductEvent event){
    	if (event.getEventType() == ServerInduct.REQ_TYPE_FINISHED) {
			finish(event.getInductId());
		}
    }
}
