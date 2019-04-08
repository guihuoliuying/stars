package com.stars.core.module;


import com.stars.services.summary.SummaryComponent;

import java.util.Calendar;
import java.util.Map;

/**
 * todo: moduleMap -> 封装，提供模块越界检查（先强制提供，后发现不合用再去掉）
 *
 *
 * 模块接口汇总
 * 1. 通用接口
 * 2. 登录接口
 * 3. 保存接口
 * 4. 登出接口
 * 5. 获取/增加/删除模块内对象的接口
 *
 * Created by zws on 2015/11/30.
 */
public interface Module {


    // -------------------- 模块接口

    /**
     * 获取模块的名字
     * @return 模块名
     */
    String name();

    long id();

    // -------------------- 用户登录接口

    /**
     * 加载用户数据 new api
     */
    void onDataReq() throws Throwable;

    /**
     * 用户创建(同步接口) new api
     */
    void onCreation(String name, String account) throws Throwable;


    /**
     * 如果用户每天登陆需要进行初始化操作，则需要实现该方法
     * @param now
     * @param isLogin
     */
    void onDailyReset(Calendar now, boolean isLogin) throws Throwable;

    /**
     * 每天凌晨五点重置
     */
    void onFiveOClockReset(Calendar now) throws Throwable;

    /**
     * 每周刷新
     * @param isLogin
     */
    void onWeeklyReset(boolean isLogin) throws Throwable;

    /**
     * 每月刷新
     */
    void onMonthlyReset() throws Throwable;

    /**
     * 如果用户每次登陆需要进行初始化操作，则需要实现该方法
     * @param isCreation
     */
    void onInit(boolean isCreation) throws Throwable;

    /**
     * 断线重连时操作
     * 默认为调用onInit方法，如有特殊处理，请重写此接口
     */
    void onReconnect() throws Throwable;

    /**
     * 登录时下发数据包操作（发包操作都集中到这里，对某些需要经过多个模块的数据可以在最后才发包）
     */
    void onSyncData() throws Throwable;

    /**
     * 玩家离线时调用
     */
    void onOffline() throws Throwable;

    /**
     * 移除数据时调用，用于下线时释放资源（在用户数据移除出内存时生效）
     */
    void onExit() throws Throwable;

    /**
     * 用于定时执行或者检测业务(在线情况下)
     */
    void onTimingExecute();

    /**
     * 用于更新常用数据
     * @param componentMap
     */
    void onUpateSummary(Map<String, SummaryComponent> componentMap);
    
    /**
     * 用户各个模块向log对象发送数据
     */
    void onLog();
}
