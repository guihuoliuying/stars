package com.stars.services.escort;

import com.stars.modules.escort.packet.vo.CargoPo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuyuxing on 2016/12/8.
 * 镖车队列场景缓存
 */
public class CargoListCache {

    private long roleId;
    private int fighting;
    private List<Long> enemyList;   //历史仇人列表
    private Map<Byte,CargoPo> cargoList;//镖车队列

    public CargoListCache(long roleId, List<Long> enemyList,int fighting) {
        this.roleId = roleId;
        this.fighting = fighting;
        this.enemyList = enemyList;
        this.cargoList = new HashMap<>();
    }

    public CargoListCache() {
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public List<Long> getEnemyList() {
        return enemyList;
    }

    public void setEnemyList(List<Long> enemyList) {
        this.enemyList = enemyList;
    }

    public int getFighting() {
        return fighting;
    }

    public void setFighting(int fighting) {
        this.fighting = fighting;
    }

    public Map<Byte, CargoPo> getCargoList() {
        return cargoList;
    }

    public void setCargoList(Map<Byte, CargoPo> cargoList) {
        this.cargoList = cargoList;
    }
}
