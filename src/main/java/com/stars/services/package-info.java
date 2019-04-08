/**
 * 公共业务指南
 * 1. 数据应该独立于玩家数据（内存/数据库），避免数据争用（data race），也方便公共业务分离。
 * 2. 玩家模块（module）和公共业务（service）之间的交互，应该使用方法调用和消息的形式，避免使用共享数据
 *   A) module --调用--> service
 *   B) service --消息--> module，可以将事件包装成消息（已有现成的方法）
 * 3. 注解说明
 *   A) @AsyncInvocation -> 表示该service方法是异步调用（不注明时，是同步调用）。
 *   B) @Timeout -> 表示该同步service方法的超时时间。
 *   C) @DispatchAll -> 表示实现了该接口的所有的ServiceActor都收到。
 * 4. 注意事项
 *   A) 尽量不用同步方法，在公共业务中调用其他公共业务的同步要特别小心
 *   B) 不要在登录时使用同步方法
 *   C) RPC调用方法必须要以rpc开头，如FamilyEscortServiceActor.rpcOnFightCreated()
 *
 * Created by zhaowenshuo on 2016/7/28.
 */
package com.stars.services;