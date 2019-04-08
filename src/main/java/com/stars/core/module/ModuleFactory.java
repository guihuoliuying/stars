package com.stars.core.module;

import com.stars.core.event.EventDispatcher;
import com.stars.core.player.Player;

import java.util.Map;

/**
 * Created by zws on 2015/11/30.
 */
public interface ModuleFactory<T extends Module> {

    /* 模块 - 公共接口 */
    /**
     * 加载产品数据
     * fixme: 利用注解解决重载时依赖管理问题
     * @throws Exception
     */
    void loadProductData() throws Exception;

    void initPacket() throws Exception;

    /**
     * 初始化操作
     * 1. 注册数据包
     * 2. 注册数据表
     * 3. 初始化数据
     */
    void init() throws Exception;

    /* 模块 - 用户接口 */
    /**
     * 创建模块实例
     * @param eventDispatcher 消息/事件分发器
     * @param id 模块所属的用户ID
     * @param moduleMap 模块映射表
     * @return 模块实例
     */
    T newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap);

    /**
     * 注册监听者
     * @param eventDispatcher 消息/事件分发器
     * @param module 模块实例
     */
    void registerListener(EventDispatcher eventDispatcher, Module module);
    
    void initModuleKey(String key);

}
