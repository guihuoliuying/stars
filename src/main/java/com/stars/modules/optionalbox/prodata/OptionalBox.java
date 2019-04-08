package com.stars.modules.optionalbox.prodata;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.vip.VipModule;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by huwenjun on 2017/11/15.
 */
public class OptionalBox {
    private int id;//'唯一id',
    private int group;//'自选宝箱组',
    private String roleLevel;// '角色等级范围',
    private String roleVip;// '角色vip范围',
    private String roleFight;// '角色战力范围',
    private String item;// '可选择的道具',

    private int roleLevelMin;//等级范围下限
    private int roleLevelMax;//等级范围上限
    private int roleVipMin;//vip等级下限
    private int roleVipMax;//vip等级上限
    private int roleFightMin;//战力下限
    private int roleFightMax;//战力上限
    private Map<Integer, Integer> itemMap;//奖励

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public String getRoleLevel() {
        return roleLevel;
    }

    public void setRoleLevel(String roleLevel) {
        this.roleLevel = roleLevel;
        try {
            int[] roleLevelRange = StringUtil.toArray(roleLevel, int[].class, '+');
            this.roleLevelMin = roleLevelRange[0];
            this.roleLevelMax = roleLevelRange[1];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getRoleVip() {
        return roleVip;
    }

    public void setRoleVip(String roleVip) {
        this.roleVip = roleVip;
        try {
            int[] roleVipRange = StringUtil.toArray(roleVip, int[].class, '+');
            this.roleVipMin = roleVipRange[0];
            this.roleVipMax = roleVipRange[1];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getRoleFight() {
        return roleFight;
    }

    public void setRoleFight(String roleFight) {
        this.roleFight = roleFight;
        try {
            int[] roleFightRange = StringUtil.toArray(roleFight, int[].class, '+');
            this.roleFightMin = roleFightRange[0];
            this.roleFightMax = roleFightRange[1];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
        itemMap = StringUtil.toMap(item, Integer.class, Integer.class, '+', '|');
    }

    public Map<Integer, Integer> getItemMap() {
        return itemMap;
    }

    public boolean isValid(Map<String, Module> moduleMap) {
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        VipModule vipModule = (VipModule) moduleMap.get(MConst.Vip);
        int level = roleModule.getLevel();
        int vipLevel = vipModule.getVipLevel();
        int fight = roleModule.getFightScore();
        if (level > roleLevelMax || level < roleLevelMin) {
            return false;
        }
        if (vipLevel > roleVipMax || vipLevel < roleVipMin) {
            return false;
        }
        if (fight > roleFightMax || fight < roleFightMin) {
            return false;
        }
        return true;
    }

    public void writeBuff(NewByteBuffer buff) {
        buff.writeInt(id);//'唯一id',
        buff.writeString(item);// '可选择的道具',
    }
}
