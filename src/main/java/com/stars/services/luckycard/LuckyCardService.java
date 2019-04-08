package com.stars.services.luckycard;

import com.stars.modules.luckycard.pojo.LuckyCardAnnounce;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.LinkedList;

/**
 * Created by huwenjun on 2017/8/10.
 */
public interface LuckyCardService extends Service, ActorService {
    @AsyncInvocation
    void luckyAnnounce(LuckyCardAnnounce luckyCardAnnounce);

    LinkedList<LuckyCardAnnounce> getLuckyAnnounceTop10();
}
