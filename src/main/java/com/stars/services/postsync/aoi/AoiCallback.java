package com.stars.services.postsync.aoi;

import java.util.List;

/**
 * Created by zhaowenshuo on 2017/6/24.
 */
public interface AoiCallback {

    void exec(AoiScene scene, long roleId, List<AoiObject> aoiObjectList);

}
