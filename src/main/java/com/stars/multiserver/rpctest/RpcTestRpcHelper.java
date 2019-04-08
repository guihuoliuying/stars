package com.stars.multiserver.rpctest;

import com.stars.services.role.RoleService;

/**
 * Created by zhaowenshuo on 2016/11/4.
 */
public class RpcTestRpcHelper {

    static RoleService roleService;

    public static RoleService roleService() {
        return roleService;
    }
}
