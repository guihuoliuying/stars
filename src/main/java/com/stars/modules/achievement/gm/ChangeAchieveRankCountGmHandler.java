package com.stars.modules.achievement.gm;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.achievement.AchievementManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/8/12.
 */
public class ChangeAchieveRankCountGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        try {
            /**
             * achieverankcount count 修改成就排行榜显示入榜人数
             */
            int count = Integer.parseInt(args[0]);
            AchievementManager.setRankCount(count);
        } catch (Exception e) {
            PlayerUtil.send(roleId, new ClientText("执行失败,achieverankcount " + args[0]));
        }
    }
}
