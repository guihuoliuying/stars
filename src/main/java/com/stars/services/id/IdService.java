package com.stars.services.id;

import com.stars.services.Service;

/**
 * Created by zhaowenshuo on 2016/7/14.
 */
public interface IdService extends Service {

    long newRoleId();

    long newToolId();

    public long newFamilyId();

    public long newFamilyRedPacketId();

}
