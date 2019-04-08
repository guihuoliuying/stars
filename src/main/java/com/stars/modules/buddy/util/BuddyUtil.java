package com.stars.modules.buddy.util;

import com.stars.core.attr.Attribute;
import com.stars.modules.buddy.prodata.BuddyLineupVo;
import com.stars.modules.buddy.userdata.RoleBuddy;

import java.util.Random;

/**
 * Created by liuyuheng on 2016/8/8.
 */
public class BuddyUtil {
    /**
     * 计算阵型附加给角色属性
     *
     * @return
     */
    public static int calAddRoleAttr(int attr, int addpoint) {
        return (int) Math.ceil(attr * (addpoint / 1000.0));
    }

    public static int calAddRoleAttr(BuddyLineupVo vo, RoleBuddy buddy, String attrName) {
        return calAddRoleAttr(buddy.getAttribute().get(attrName), vo.getAttrAddPoint(attrName));
    }

    public static int calAddRoleAttr(BuddyLineupVo vo, Attribute attr, String attrName) {
        return calAddRoleAttr(attr.get(attrName), vo.getAttrAddPoint(attrName));
    }

    public static void main(String[] args) {
        int rand = 7 - new Random().nextInt(7 - (-7) + 1);
        int result = Math.min(Math.max((0 + rand), 0 + 1), 1 + 10);
        System.err.println("oldTalent=" + 0 + ",newTalent=" + result);
    }
}
