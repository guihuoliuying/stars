package com.stars.services.postsync.aoi;

/**
 * Created by zhaowenshuo on 2017/6/23.
 */
public interface PositionSyncAoiService {

    void enter(long roleId, String sceneId, byte sceneType, double x, double z);

    void leave(long roleId, String sceneId);

    void update(long roleId, String sceneId, double x, double z);

    void check(String sceneId, AoiCallback callback);

    void check(AoiCallback callback);

    String toString();

}
