/**
 * Created by zhaowenshuo on 2017/2/23.
 */
package com.stars.db;

// fixme: 启动服务时，感觉会读到旧的代码(RoleOfflinePvp.challegedNum是int，但是内存里的是byte，导致了转换错误)；热更不生效的问题