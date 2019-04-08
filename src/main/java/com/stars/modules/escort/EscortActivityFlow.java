package com.stars.modules.escort;

import com.stars.core.activityflow.ActivityFlow;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;

/**
 * Created by wuyuxing on 2016/12/14.
 */
public class EscortActivityFlow extends ActivityFlow {

    private static boolean isStarted = false;

    @Override
    public String getActivityFlowName() {
        return "escort";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
        switch (step) {
            case STEP_START_CHECK:
                if (between(1, 2)) {
                    start();
                }
                break;
            case STEP_START:
                if (isStarted) return;
                start();
                break;
            case STEP_END:
                if (!isStarted) return;
                end();
                break;
        }
    }

    public static boolean isStarted() {
        return isStarted;
    }

    public static void start() {
        isStarted = true;
    }

    public static void end() {
        isStarted = false;
    }

    private static void borcastPacket(byte type) {
        for (Actor actor : PlayerSystem.system().getActors().values()) {
            Player player = (Player) actor;
            try {
//                PacketManager.send(player.id(), new ClientEscort());
            } catch (Throwable t) {
                LogUtil.error("", t);
            }
        }
    }
}
