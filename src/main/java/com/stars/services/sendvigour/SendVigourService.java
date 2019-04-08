package com.stars.services.sendvigour;

import com.stars.services.Service;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by gaopeidian on 2017/3/29.
 */
public interface SendVigourService extends Service{

    @AsyncInvocation
	void setCurStepId(int curStepId);
    
    int getCurStepId();
}
