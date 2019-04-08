package com.stars.services.luckydraw;

import com.stars.modules.luckydraw.pojo.LuckyDrawAnnounce;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.List;

/**
 * Created by huwenjun on 2017/8/10.
 */
public interface LuckyDrawService extends Service, ActorService {
    @AsyncInvocation
    void luckyAnnounce(LuckyDrawAnnounce luckyDrawAnnounce);

    List<LuckyDrawAnnounce> getLuckyAnnounceTop10(int actType);
}
