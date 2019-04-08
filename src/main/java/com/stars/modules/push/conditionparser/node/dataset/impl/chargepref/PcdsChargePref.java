package com.stars.modules.push.conditionparser.node.dataset.impl.chargepref;

import com.stars.modules.chargepreference.ChargePrefManager;
import com.stars.modules.chargepreference.prodata.ChargePrefVo;
import com.stars.modules.chargepreference.userdata.RoleChargePrefPo;
import com.stars.modules.push.PushManager;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.prodata.PushVo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/6/22.
 */
public class PcdsChargePref implements PushCondData {

    public static Set<String> fieldSet() {
        return new HashSet<String>(Arrays.asList("id", "gid"));
    }

    private RoleChargePrefPo po;

    public PcdsChargePref(RoleChargePrefPo po) {
        this.po = po;
    }

    @Override
    public long getField(String name) {
        switch (name) {
            case "id": return po.getPrefId();
            case "gid":
                ChargePrefVo vo = ChargePrefManager.getPrefVo(po.getPrefId());
                if (vo == null) break;
                PushVo pushVo = PushManager.getPushVo(vo.getPushId());
                if (pushVo == null) break;
                return pushVo.getGroup();
        }
        return 0;
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
