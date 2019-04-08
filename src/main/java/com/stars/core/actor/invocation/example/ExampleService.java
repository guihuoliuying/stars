package com.stars.core.actor.invocation.example;


import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;
import com.stars.core.actor.invocation.annotation.DispatchAll;

import java.util.List;

/**
 * Created by zhaowenshuo on 2016/6/14.
 */
public interface ExampleService extends ActorService, NonActorService {

//    @AsyncInvocation
    @com.stars.core.actor.invocation.annotation.DispatchAll
    void print();

    @AsyncInvocation
    @com.stars.core.actor.invocation.annotation.DispatchAll
    void print(String str);

//    @AsyncInvocation
    void print(String str1, String str2);

    @DispatchAll
    List<String> print(List<String> list);

}
