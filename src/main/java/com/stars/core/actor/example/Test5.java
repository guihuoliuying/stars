package com.stars.core.actor.example;

import com.stars.core.actor.AbstractActor;
import com.stars.core.actor.Actor;
import com.stars.core.actor.ActorSystem;

import java.util.Random;

/**
 * Created by Administrator on 2015/2/13.
 */
public class Test5 {

    public static void main(String[] args) {
        Test5 t = new Test5();
        t.start();
    }

    public void start(){
        Player [] players = new Player[2];
        players[0] = new Player("jack", 30);
        players[1] = new Player("andy", 30);
        Monster[] monster = new Monster[1];
        monster[0] = new Monster ("妖猴", 10);

        com.stars.core.actor.ActorSystem system = new ActorSystem();
        BattleField bf = new BattleField(players, monster);
        system.addActor(bf);

        new Thread(new PlayerTask(bf)).start();
        new Thread(new MonsterTask(bf)).start();
    }

    class BattleField extends AbstractActor {
        private Player[] players;
        private Monster[] monsters;

        public BattleField(Player[] players, Monster[] monsters){
            this.players = players;
            this.monsters = monsters;
        }

        @Override
        public void onReceived(Object message, Actor sender) {
            if(players == null || monsters == null){return;}
            if (message instanceof PlayerAttack) {
                execPlayerAttack();
            } else if (message instanceof MonsterSingleAttack){
                execMonsterSingleAttack();
            } else if (message instanceof MonsterAreaAttack){
                execMonsterAreaAttack();
            }

        }

        private void execMonsterAreaAttack() {
            if(monsters[0].hp <= 0){return;}
            for (int i = 0;i < players.length;i++){
                if(players[i].hp <= 0){
                    continue;
                }
                players[i].hp--;
                if(players[i].hp <= 0){
                    System.out.println(players[i].name + " 挂了");
                    continue;
                }
                System.out.println(monsters[0].name + " attack | " + players[i].name+" hp -1 剩余HP " + players[i].hp);
            }
            if(isWin()){
                monsters = null;
                players = null;
                System.out.println("怪物胜利");
            }
        }

        private void execMonsterSingleAttack() {
            if(monsters[0].hp <= 0){return;}
            Random r = new Random();
            Player player = players[r.nextInt(players.length)];
            if(player.hp <= 0){
                return;
            }
            player.hp--;
            System.out.println(monsters[0].name + " attack | " + player.name+" hp -1 剩余HP " + player.hp);
            if(player.hp <= 0){
                System.out.println(player.name + " 挂了");
            }
            if(isWin()){
                monsters = null;
                players = null;
                System.out.println("怪物 胜利");
            }
        }

        private void execPlayerAttack() {
            boolean isWin = false;
            for (int i = 0;i < players.length;i++){
                if(players[i].hp <= 0){
                    continue;
                }
                monsters[0].hp--;
                System.out.println(players[i].name + " attack | " + monsters[0].name + " hp -1 剩余HP " + monsters[0].hp);
                if(monsters[0].hp <= 0){
                    System.out.println(monsters[0].name + " 挂了");
                    isWin = true;
                    break;
                }
            }
            if(isWin){
                monsters = null;
                players = null;
                System.out.println("玩家 胜利");
            }
        }

        private boolean isWin(){
            for (int i = 0;i < this.players.length;i++){
                if(this.players[i].hp > 0){
                    return false;
                }
            }
            return true;
        }
    }

   class Player{
        private String name;
        private int hp = 0;

        public Player(String name,int hp) {
            this.name = name;
            this.hp = hp;
        }
    }

    private class PlayerTask implements Runnable {
        private  BattleField bf;

        public PlayerTask(BattleField bf) {
            this.bf = bf;
        }

        @Override
        public void run() {
            while (bf.players != null && bf.monsters != null) {
                try {
                    bf.tell(new PlayerAttack(), null);
                    Thread.sleep(2000);
                }  catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    class Monster{
        private String name;
        private int hp = 0;

        public Monster(String name, int hp) {
            this.name = name;
            this.hp = hp;
        }

        private int randomAttackType(){
            Random r = new Random();
            return r.nextInt(2);
        }
    }

    private class MonsterTask implements Runnable {
        private  BattleField bf;

        public MonsterTask(BattleField bf) {
            this.bf = bf;
        }

        @Override
        public void run() {
            while (bf.players != null && bf.monsters != null) {
                try {
                    if(bf.monsters[0].randomAttackType() == 0){
                        bf.tell(new MonsterSingleAttack(), null);
                    }else {
                        bf.tell(new MonsterAreaAttack(), null);
                    }
                    Thread.sleep(2000);
                }  catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    class PlayerAttack {

    }

    class MonsterSingleAttack {

    }

    class MonsterAreaAttack {

    }

}
