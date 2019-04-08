package com.stars.services.accounttransfer;

import com.stars.modules.demologin.userdata.AccountTransfer;
import com.stars.services.Service;
import com.stars.services.accounttransfer.po.AccountTransferCount;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.sql.SQLException;


/**
 * Created by huwenjun on 2017/8/10.
 */
public interface AccountTransferService extends Service, ActorService {

    @AsyncInvocation
    void transfer(String fromAccount, String toAccount, String reason, AccountTransferCount accountTransferCount) throws SQLException;


    @AsyncInvocation
    void cleanUp();

    @AsyncInvocation
    void transferBack(AccountTransfer accountTransfer) throws SQLException;
}
