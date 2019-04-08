package com.stars.core.actor.invocation.example;

import com.stars.core.actor.ActorSystem;
import com.stars.core.actor.invocation.ServiceActor;
import com.stars.core.actor.invocation.ServiceActorHelper;
import com.stars.core.actor.invocation.annotation.DispatchAll;
import com.stars.util.log.CoreLogger;

import java.util.List;

/**
 * Created by zhaowenshuo on 2016/6/14.
 */
public class ExampleServiceActor extends ServiceActor implements com.stars.core.actor.invocation.example.ExampleService, DummyService {

    private int actorId;

    public ExampleServiceActor(int id) {
        super();
        this.actorId = id;
    }

    @Override
    public void init() {
        System.out.println("fuck");
    }

    @Override
    public void printState() {
        System.out.println("printState");
    }

    @Override
    public void print() {
//        if (true) {
//            throw new IllegalArgumentException("fuck you");
//        }
        try {
            System.out.println(actorId + ": hello");
//            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {

        }
    }

    @DispatchAll
    @Override
    public void print(String str) {
        System.out.println(actorId + ": str = " + str);
    }

    @Override
    public void print(String str1, String str2) {
        System.out.println(actorId + ": str1=" + str1 + ", str2=" + str2);
    }

    @Override
    public List<String> print(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            list.set(i, list.get(i) + "-" + actorId);
        }
        return list;
    }

    @Override
    public void helloWorld() {
        System.out.println(actorId + ": fuck");
    }

    public static void main(String[] args) throws InterruptedException {
        CoreLogger.init("routeclienttest");
        com.stars.core.actor.ActorSystem system = new ActorSystem();
        ExampleServiceActor actor1 = new ExampleServiceActor(1);
        ExampleServiceActor actor2 = new ExampleServiceActor(2);
        com.stars.core.actor.invocation.example.ExampleService proxy = (ExampleService) ServiceActorHelper.newService(actor1);
//        ExampleService proxy = (ExampleService) ServiceActorHelper.newDispatchService(new ServiceActor[] { actor1, actor2 }, ExampleService.class);
        system.addActor(actor1);
//        system.addActor(actor2);

//        proxy.print();
//        System.out.println("end 1");

//        proxy.init();
//        proxy.print("fuck");
//        System.out.println("end 2");
//        for (int i = 0; i < 1000; i++) {
//            proxy.init();
//            proxy.print("xxx");
//            System.out.println("end 3");
//            Thread.sleep(new Random().nextInt(1000));
//        }

//        List<String> list = new ArrayList<>();
//        list.add("a");
//        list.add("b");
//        list.add("c");
//        list.add("d");
        proxy.printState();
    }
}
