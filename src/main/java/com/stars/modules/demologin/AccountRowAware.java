package com.stars.modules.demologin;

import com.stars.AccountRow;

/**
 * 实现此接口的模块会将账户信息注入此模块
 * 适用于onDataReq方法中需要获取accountrow信息的场景
 * Created by huwenjun on 2017/3/25.
 */
public interface AccountRowAware {
    void setAccountRow(AccountRow accountRow);
}
