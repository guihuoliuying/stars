package com.stars.multiserver.fight.message;

import java.util.List;

/**
 * Created by wuyuxing on 2016/12/28.
 */
public class RemoveFromFightActor {
    private List<Long> roleIdList;

    public RemoveFromFightActor(List<Long> roleIdList) {
        this.roleIdList = roleIdList;
    }

    public List<Long> getRoleIdList() {
        return roleIdList;
    }
}
