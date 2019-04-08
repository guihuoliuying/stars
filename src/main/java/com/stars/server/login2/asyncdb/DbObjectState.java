package com.stars.server.login2.asyncdb;

/**
 * Created by zhaowenshuo on 2016/2/22.
 */
public enum DbObjectState {

    /*
     * 状态转移关系（试作）
     *
     * UNCHANGED --更新--> CHANGED
     *
     * CHANGED --保存--> SAVING
     * CHANGED --更新--> CHANGED
     *
     * SAVING --保存成功--> UNCHANGED
     * SAVING --保存失败/超时--> CHANGED
     * SAVING --更新--> UPDATED
     *
     * UPDATED --保存成功--> CHANGED
     * UPDATED --保存失败/超时--> CHANGED
     * UPDATED --更新--> UPDATED
     *
     * NEW --保存--> NEW_SAVING
     * NEW --更新--> NEW
     *
     * NEW_SAVING --保存成功--> UNCHANGED
     * NEW_SAVING --保存失败(非主键重复)/超时--> NEW
     * NEW_SAVING --保存失败(主键重复)--> CHANGED
     * NEW_SAVING --更新--> NEW_UPDATED
     *
     * NEW_UPDATED --保存成功--> CHANGED
     * NEW_UPDATED --保存失败(非主键重复)/超时--> NEW
     * NEW_UPDATED --保存失败(主键重复)--> CHANGED
     * NEW_UPDATED --更新--> NEW_UPDATED
     *
     */

    UNCHANGED,
    CHANGED,
    SAVING,
    UPDATED,
    NEW,
    NEW_SAVING ,
    NEW_UPDATED;

}
