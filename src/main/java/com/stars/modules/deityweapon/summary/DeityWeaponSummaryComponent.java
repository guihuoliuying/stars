package com.stars.modules.deityweapon.summary;

import com.stars.modules.deityweapon.userdata.RoleDeityWeapon;
import com.stars.services.summary.SummaryComponent;

import java.util.List;

/**
 * Created by panzhenfeng on 2016/12/12.
 */
public interface DeityWeaponSummaryComponent extends SummaryComponent {
    public List<RoleDeityWeapon> getRoleDeityWeaponList();
    public RoleDeityWeapon getCurRoleDeityWeapon();
    public int getCurRoleDeityWeapoonId();
}
