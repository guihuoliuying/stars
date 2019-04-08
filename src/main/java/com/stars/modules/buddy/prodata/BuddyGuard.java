package com.stars.modules.buddy.prodata;

import com.stars.core.attr.Attribute;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.vip.VipModule;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by huwenjun on 2017/8/30.
 */
public class BuddyGuard {
    private int position;// '唯一标识',
    private String guardname;// '护卫名称',
    private int groupid;//  '所属组',
    private String groupname;//  '组名',
    private int buddyid;// '对应的配置伙伴ID',
    private String attribute;//  '所加属性',千分比加成属性
    private String fixedAttr;//固定属性
    private String condition;//  '位置开启条件',


    private int[] conditions = new int[2];
    private Attribute fixedAttribute;
    private Map<String, Integer> attrPointMap;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getGuardname() {
        return guardname;
    }

    public void setGuardname(String guardname) {
        this.guardname = guardname;
    }

    public int getGroupid() {
        return groupid;
    }

    public void setGroupid(int groupid) {
        this.groupid = groupid;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public int getBuddyid() {
        return buddyid;
    }

    public void setBuddyid(int buddyid) {
        this.buddyid = buddyid;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
        this.attrPointMap = StringUtil.toMap(attribute, String.class, Integer.class, '=', '|');
    }

    /**
     * 根据属性名获得附加属性加成千分比
     *
     * @param attrName
     * @return
     */
    public int getAttrAddPoint(String attrName) {
        return attrPointMap.get(attrName);
    }

    public Map<String, Integer> getAttrPointMap() {
        return attrPointMap;
    }

    public String getFixedAttr() {
        return fixedAttr;
    }

    public void setFixedAttr(String fixedAttr) {
        this.fixedAttr = fixedAttr;
        fixedAttribute = new Attribute(fixedAttr);
    }

    public Attribute getFixedAttribute() {
        return fixedAttribute;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
        try {
            conditions = StringUtil.toArray(condition, int[].class, '+');
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 0表示伙伴未开放（敬请期待）
     * 1表示可激活
     * 2表示不可激活
     * 3表示角色条件不满足(不显示页签)
     *
     * @param moduleMap
     * @return
     */
    public int checkRoleOpen(Map<String, Module> moduleMap) {
        if (buddyid == 0) {
            return 0;
        }
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        VipModule vipModule = (VipModule) moduleMap.get(MConst.Vip);
        BuddyModule buddyModule = (BuddyModule) moduleMap.get(MConst.Buddy);
        int roleLevel = roleModule.getLevel();
        int vipLevel = vipModule.getVipLevel();
        if (conditions[0] > roleLevel || conditions[1] > vipLevel) {
            return 3;
        }
        if (buddyModule.getRoleBuddy(buddyid) != null) {
            return 1;
        }
        return 2;
    }


    public void writeBuff(NewByteBuffer buff) {
        buff.writeInt(position);// '唯一标识',
        buff.writeString(guardname);// '护卫名称',
        buff.writeInt(groupid);//  '所属组',
        buff.writeInt(buddyid);// '对应的配置伙伴ID',
        buff.writeString(attribute);//  '所加属性',
        buff.writeString(fixedAttr);//固定属性
        buff.writeString(condition);//  '位置开启条件',
    }
}
