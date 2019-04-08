package com.stars.modules.push.conditionparser.node.dataset.impl.guest;

import com.stars.modules.guest.GuestManager;
import com.stars.modules.guest.userdata.RoleGuest;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcdsGuest implements PushCondData {

    public static Set<String> fieldSet() {
        return new HashSet<String>(Arrays.asList("id", "star", "quality"));
    }

    private RoleGuest guest;

    public PcdsGuest(RoleGuest guest) {
        this.guest = guest;
    }

    @Override
    public long getField(String name) {
        switch (name) {
            case "id": return guest.getGuestId();
            case "star": return guest.getLevel();
            case "quality": return GuestManager.getInfoVo(guest.getGuestId()).getQuality();
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
