package com.stars.modules.gm.gmhandler;

import com.stars.AccountRow;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.modules.demologin.message.KickOffMsg;
import com.stars.modules.gm.GmHandler;
import com.stars.startup.MainStartup;
import com.stars.util.ExecuteManager;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaowenshuo on 2016/1/14.
 */
public class KickOffGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) {
        // fixme: 对全服玩家作用的GM应该独立于Player执行
        ExecuteManager.execute(new KickOffTask());
    }

    class KickOffTask implements Runnable {
        @Override
        public void run() {
            for (AccountRow accountRow : MainStartup.accountMap.values()) {
                try {
                    long roleId = accountRow.getCurrentRoleId();
                    if (roleId > 0) {
                        Player player = PlayerSystem.get(roleId);
                        if(player!=null) {
                            KickOffMsg message = new KickOffMsg();
                            try {
                                player.tell(message, com.stars.core.actor.Actor.noSender);
                                message.await(5, TimeUnit.SECONDS);
                                if (!message.isSucceeded()) {
                                    com.stars.util.LogUtil.error("剔除玩家失败, roleId=" + roleId);
                                }
                            } catch (Throwable t) {
                                com.stars.util.LogUtil.error("剔除玩家失败, roleId=" + roleId, t);
                            }
                        }
                    }
                    MainStartup.accountMap.remove(accountRow.getName());
                } catch (Throwable t) {
                    com.stars.util.LogUtil.error("", t);
                }
            }

            // 剔除漏网之鱼
            for (com.stars.core.actor.Actor actor : PlayerSystem.system().getActors().values()) {
                KickOffMsg message = new KickOffMsg();
                try {
                    actor.tell(message, Actor.noSender);
                    message.await(5, TimeUnit.SECONDS);
                    if (!message.isSucceeded()) {
                        com.stars.util.LogUtil.error("剔除玩家失败, roleId=" + actor.getName());
                    }
                } catch (Throwable t) {
                    LogUtil.error("剔除玩家失败, roleId=" + actor.getName(), t);
                }
            }
        }
    }
}
