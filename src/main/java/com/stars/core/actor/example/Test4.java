package com.stars.core.actor.example;

import com.stars.core.actor.AbstractActor;
import com.stars.core.actor.Actor;
import com.stars.core.actor.ActorSystem;

/**
 * Created by zhaowenshuo on 2015/2/11.
 */
public class Test4 {

    public static void main(String[] args) {
        com.stars.core.actor.ActorSystem system = new ActorSystem();
        Player player = new Player(100);
        Monster monster = new Monster(90);
        system.addActor(player);
        system.addActor(monster);

        player.tell(new Attack(), monster);
    }

}

class Player extends com.stars.core.actor.AbstractActor {
    private int hp = 0;

    public Player(int hp) {
        this.hp = hp;
    }

    @Override
    public void onReceived(Object message, com.stars.core.actor.Actor sender) {
        if (message instanceof Attack) {
            hp--;
            System.out.println("Player -1 HP, HP: " + hp);
            if (hp == 0) {
                System.out.println("You...");
            } else {
                sender.tell(new Attack(), this);
            }
        }
    }
}

class Monster extends AbstractActor {
    private int hp = 0;

    public Monster(int hp) {
        this.hp = hp;
    }

    @Override
    public void onReceived(Object message, Actor sender) {
        if (message instanceof Attack) {
            hp--;
            System.out.println("Monster -1 HP, HP: " + hp);
            if (hp == 0) {
                System.out.println("Oh...");
            } else {
                sender.tell(new Attack(), this);
            }
        }
    }
}

class Attack {

}