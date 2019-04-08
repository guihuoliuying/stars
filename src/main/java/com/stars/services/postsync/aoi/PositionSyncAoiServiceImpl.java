package com.stars.services.postsync.aoi;

import com.stars.modules.positionsync.PositionSyncManager;
import com.stars.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/6/23.
 */
public class PositionSyncAoiServiceImpl implements PositionSyncAoiService {

    private Map<String, AoiScene> aoiSceneMap = new HashMap<>();

    @Override
    public void enter(long roleId, String sceneId, byte sceneType, double x, double z) {
        AoiScene scene = aoiSceneMap.get(sceneId);
        if (scene == null) {
            aoiSceneMap.put(sceneId, scene = new AoiScene(sceneId, sceneType, PositionSyncManager.ViewRadius));
        }
        scene.enter(roleId, x, z);
    }

    @Override
    public void leave(long roleId, String sceneId) {
        AoiScene scene = aoiSceneMap.get(sceneId);
        if (scene != null) {
            scene.leave(roleId);
        }
    }

    @Override
    public void update(long roleId, String sceneId, double x, double z) {
        AoiScene scene = aoiSceneMap.get(sceneId);
        if (scene != null) {
            scene.update(roleId, x, z);
        }
    }

    @Override
    public void check(String sceneId, AoiCallback callback) {
        AoiScene scene = aoiSceneMap.get(sceneId);
        if (scene != null) {
            try {
                scene.check(callback);
            } catch (Throwable cause) {
                LogUtil.error("", cause);
            }
        }
    }

    @Override
    public void check(AoiCallback callback) {
        for (AoiScene scene : aoiSceneMap.values()) {
            try {
                scene.check(callback);
            } catch (Throwable cause) {
                LogUtil.error("", cause);
            }
        }
    }

    @Override
    public String toString() {
        return aoiSceneMap.toString();
    }
}
