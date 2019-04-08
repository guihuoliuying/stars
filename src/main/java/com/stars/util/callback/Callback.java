package com.stars.util.callback;

/**
 * Created by zhaowenshuo on 2015/12/21.
 */
public interface Callback {

    /**
     * todo: 应该带可变参数
     * @param ctx
     */
    void onCalled(CallbackContext ctx);
    
}
