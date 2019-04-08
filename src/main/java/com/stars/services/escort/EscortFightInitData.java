package com.stars.services.escort;

import java.util.List;

/**
 * Created by wuyuxing on 2016/12/7.
 */
public class EscortFightInitData {
    private List<Long> escortRoleIds;

    public EscortFightInitData(List<Long> escortRoleIds) {
        this.escortRoleIds = escortRoleIds;
    }

    public EscortFightInitData() {
    }

    public List<Long> getEscortRoleIds() {
        return escortRoleIds;
    }
}
