/**
 *
 * 开发指南
 * Z1 个人业务（参见com.stars.modules.package-info.java）
 * Z2 公共业务（参见com.stars.services.package-info.java）
 * 关键点 A
 * A1 模块机制（参见com.stars.modules.package-info.java的3.2，这里不再详述）
 * A2 事件机制（参见com.stars.modules.package-info.java的3.1，这里不再详述）
 * A3 访问控制机制
 * A4 业务处理的线程模型
 * 关键流程 B
 * B2 请求流程（收包）
 * B3 响应流程（发包）
 * B4 启动流程
 * B5 todo: 登录流程（与数据处理部分一同完成）
 * B6 todo: 离线流程（与数据处理部分一同完成）
 * B7 todo: 数据保存流程（与数据处理部分一同完成）
 * B8 todo: 常用数据读/写流程（与数据处理部分一同完成）
 * 热更指南 C
 *
 * A3 访问控制机制（数据包屏蔽，实现类AccessControl）
 *   A3.1 提供了根据协议号屏蔽数据包的功能（从而实现功能上的屏蔽）
 *   A3.2 使用位图实现，第n位表示第n号协议，一位能表示两个数值0和1，0表示该协议号禁用，1表示该协议号启用
 *   A3.3 现在最多可以用32768个协议号，需要512个长整形去标识这些协议号（32468 / 64）
 *   A3.4 使用了一个布尔变量notUseBitmap，利用短路原理，加快在不使用访问控制时的处理速度
 *
 * A4 业务处理的线程模型
 *   A4.1 分成两层：网络层和业务层
 *   A4.2 网络层
 *     a) 因使用Netty，所以网络层是使用Netty的IO线程池
 *     b) 收包/拆包/发包是在网络层完成
 *   A4.3 业务层
 *     a) 分成两个线程池：Actor和ExecuteManager
 *     b) 登录请求是在ExecuteManager中处理
 *     c) 除了登录请求，玩家的其他请求包都会在Actor里处理
 *
 * B2 请求流程（收包）
 *   B2.1 Netty IO线程收到数据包，经过GamePacketDecoder处理（按长度截包），把有效载荷交给MainServerHandler2
 *   ---- MainServerHandler2.channelRead0()
 *   B2.2 读取连接ID，判断连接ID是否存在；如果不存在就生成一个新的会话（GameSession）
 *   B2.3 根据协议号进行拆包
 *   B2.4 判断是否底层包
 *     a) 是：判断是不是客户端连接关闭数据包（FrontendClosedN2mPacket）
 *       1) 是：移除会话（GameSession），创建客户端连接关闭消息（Disconnected），把Disconnected传给MainServer.getBusiness().dispatch()
 *       2) 否：交给ExecuteManager
 *     b) 否：把数据包交给Disconnected传给MainServer.getBusiness().dispatch()
 *   ---- MainStartup.dispatch()
 *   B2.5 检查数据包是否可以访问（AccessControl）
 *     a) 可以：继续B2.6
 *     b) 不可：发送提示数据包
 *   B2.6 判断是否连接关闭消息（Disconnected），因为GameSession已经移除，所以要先做一次判断
 *     a) 是：交给对应的Actor处理
 *     b) 否：继续B2.7
 *   B2.7 判断会话（GameSession）是否为NULL
 *     a) 是：丢弃数据包
 *     b) 否：判断roleId是否为NULL
 *       1) 是：数据包交给ExecuteManager处理
 *       2) 否：数据包交给Actor处理（实际是Player类，下述过程是Player.onReceived()方法中）
 *   ---- Player.onReceived()
 *   B2.8 Actor（Player）的处理过程（只说Packet包）
 *     a) 由登录模块（为了解耦，这里定义了一个Guard接口）检查这个数据包是否可以访问
 *       1) 这个主要是基于玩家的登录状态来判断的；当玩家还没完成登录时，不允许除登录外的其他数据包访问
 *       2) 可以访问：执行数据包的方法(Packet.execPacket()）
 *       3) 不可访问：丢弃请求
 *
 * B3 响应流程（发包） fixme: 增加缓存数据包
 *   ---- PacketManager.send()
 *   B3.1 调用PacketManager.send(GameSession, Packet)
 *   B3.2 判断会话（GameSession）是否为空
 *     a) 会话为空，则不作处理
 *     b) 会话不为空，继续B3.3
 *   B3.3 粘包（调用Packet.writeToBuffer()）
 *     a) 是调用PacketManager.send()方法的线程进行粘包，因为Packet可能持有用户数据的引用，导致并发访问的问题
 *   B3.4 交给Netty IO线程进行发送
 *
 * B4 启动流程
 *   ---- MainServer.start()
 *   B4.1 初始化ExecuteManager
 *   B4.2 加载底层数据包定义
 *   B4.6 初始化数据库工具（DbUtil）
 *   ---- MainStartup.init()
 *   B4.7 调用MainStartup.init()方法，初始化业务层
 *     c) 检查协议号，判断是否全局唯一
 *     d) 初始化数据表定义
 *     e) 初始化模块
 *       1) 注册各业务模块
 *       2) 注册数据包定义
 *       3) 模块初始化（注册GM处理器）
 *     f) 加载产品数据
 *     g) 初始化Actor系统
 *     h) 初始化定时任务
 *     i) 初始化id生成器（roleId和itemId）
 *   ---- MainServer.start()
 *   B4.8 启动网络层
 *
 * 热更指南
 * C1 只能热更代码逻辑（不能新增/移除方法，不能修改方法签名，不能新增/移除/修改成员变量/类变量）
 * C2 不要使用匿名内部类，静态内部类，内部类。
 * C3 有些热更代码只执行一次，可以这样：
 *     if (YinHanHotUpdateManager.needHotUpdate("0511.ride.01")) {
 *         ...
 *     }
 * C4 如果需要缓存变量，可以这样：
 *     YinHanHotUpdateManager.globalCache("key", "val"); // 赋值
 *     String val = YinHanHotUpdateManager.global("key"); // 取值
 *
 * Created by zhaowenshuo on 2016/3/24.
 */
package com.stars;