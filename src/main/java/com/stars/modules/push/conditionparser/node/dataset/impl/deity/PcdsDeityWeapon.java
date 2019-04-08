package com.stars.modules.push.conditionparser.node.dataset.impl.deity;

import com.stars.modules.deityweapon.userdata.RoleDeityWeapon;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PcdsDeityWeapon implements PushCondData {

    public static Set<String> fieldSet() {
        return new HashSet<String>(Arrays.asList("id", "type", "lv"));
    }

    private int jobId;
    private RoleDeityWeapon deityWeapon;

    public PcdsDeityWeapon(int jobId, RoleDeityWeapon deityWeapon) {
        this.jobId = jobId;
        this.deityWeapon = deityWeapon;
    }

    @Override
    public long getField(String name) {
        switch (name) {
            case "id": return deityWeapon.getDeityweaponId(jobId);
            case "type": return deityWeapon.getType();
            case "lv": return deityWeapon.getLevel();
        }
        throw new RuntimeException();
    }

    @Override
    public boolean isOverlay() {
        return false;
    }

    @Override
    public long getOverlayCount() {
        return 0;
    }

    @Override
    public boolean isInvalid() {
        return false;
    }
}
