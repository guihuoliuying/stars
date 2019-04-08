package com.stars.core.actor;

import com.stars.util.log.CoreLogger;

/**
 *
 *
 * Created by zhaowenshuo on 2014/12/29.
 */
public interface Actor {

    /**
     * 获取Actor的命名
     *
     * @return Actor的命名
     */
    String getName();

    /**
     * 向Actor发送一条消息
     *
     * @param message 消息
     * @param sender 发送消息的Actor
     */
    void tell(Object message, Actor sender);

    /**
     * 处理接收到的消息
     *
     * @param message 消息
     * @param sender 发送消息的Actor
     */
    void onReceived(Object message, Actor sender);

    /**
     * 停止Actor
     */
    void stop();

    /**
     * 设置剩余消息的处理钩子
     * @param handler 钩子
     */
    void setDeadMessageHandler(DeadMessageHandler handler);

    /**
     * 当不存在发送者时，可使用noSender替代
     */
    Actor noSender = new Actor() {
        @Override
        public String getName() {
            return "Actor.noSender";
        }

        @Override
        public void tell(Object message, Actor sender) {
            CoreLogger.debug("actor: message to noSender");
        }

        @Override
        public void onReceived(Object message, Actor sender) {}

        @Override
        public void stop() {}

        @Override
        public void setDeadMessageHandler(DeadMessageHandler handler) {}
    };

}
