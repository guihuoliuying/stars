/**
 * 个人业务代码开发指南
 *
 *
 * 术语
 * 1. 协议/数据包 --> Packet（协议/数据包）
 * 2. Actor间的消息 --> Message（消息）
 * 3. 模块间的事件 --> Event（事件）
 *
 *
 * 概述
 * 1.  业务层功能分模块实现
 *   1.1  模块内只管自己的业务
 *   1.2  尽量做到模块间不存在相互依赖
 *   1.3  属于某个某块模块的协议，随模块注册而注册
 *
 * 2.  模块间交互
 *   2.0  假设模块B依赖模块A，那么A排在B前面
 *   2.1  提供两种方式：函数调用，事件通知
 *   2.2  B调用A的函数
 *   2.3  A以事件通知B
 *
 * 3.  具体实现（com.stars.core）
 *   3.1  event包实现了事件机制
 *     3.1.1  介绍
 *            事件机制主要用于模块间解耦，以及解决模块间相互依赖的关系。模块的状态变化是通过事件通知其他模块，模块通过实现并
 *            注册监听器获取其他模块的事件。
 *     3.1.2  类/接口说明
 *            Event -- 事件的基类；提供了2种特殊事件：ALL和LAST；ALL事件表示所有事件，监听该事件，则表示监听所有事件（除了
 *              LAST事件）；LAST事件表示事件循环结束事件，监听该事件，则表示在事件循环结束时监听者会收到该事件。
 *            EventListener -- 监听者接口；
 *            EventDispatcher -- 事件分派器；提供3个功能：注册/注销监听者，和派发事件；
 *              A)  注册监听者：把监听者加到某个事件类型的监听者队列中；如果在事件循环中注册监听者，那么这个操作将推迟到下一个
 *              事件前完成。
 *              B)  注销监听者：把监听者从某个事件类型的监听者队列中移除；如果在事件循环中注销监听者，那么这个操作将推迟到
 *              这次事件后下一个事件前完成。
 *              C)  派发事件：在派发事件时，找事件类型对应的监听者队列，依次调用监听者（通知感兴趣的监听者：某个事件发生了）。
 *     3.1.3  注意事项
 *            A)  不推荐用事件机制取代函数调用。假设模块B依赖模块A，那么B调用A的函数，A用事件通知B。
 *            B)  如果在事件循环中，注册/注销监听者的操作是在这个事件后下个事件前完成；不影响这次事件。
 *            C)  不要依赖监听者的注册顺序
 *   3.2  module包实现了模块框架
 *     3.2.1  介绍
 *            模块框架为了降低业务代码的耦合度，提高可读性和维护性而制定的。开发者根据业务的类型和范围而划分模块；模块自己管辖
 *            自己的数据（存储/修改/...）；对其他模块数据修改，只能通过其他模块的接口完成；对于可能被其他模块修改的数据，需要
 *            提供相关的接口；另外，框架规定了模块通用流程（整体/玩家），和定义了通用接口。
 *     3.2.2  类/接口说明
 *            Module -- 定义了模块通用流程和对应接口（单个玩家），包括：登录流程（创建角色/正常登陆），保存流程，下线流程，
 *            重置流程。
 *              A)  登录流程（正常流程）
 *                a)  onDataReq()                   // 请求加载用户数据
 *                b)  onDailyReset()                // 每天重置（每周/每月重置暂时不使用）
 *                c)  onInit()                      // 模块初始化（单个玩家）
 *                d)  onSyncData()                  // 下发数据
 *              B)  登录流程（创建角色）
 *                a)  onCreation()                  // 请求创建用户数据
 *                c)  onDailyReset()
 *                d)  onInit()
 *                e)  onSyncData()
 *              C)  下线流程
 *                a)  onOffline()                   // 玩家下线
 *                b)  onExit()                      // 清除内存数据时触发
 *              D)  每天重置流程
 *                a)  onDailyReset()
 *            ModuleFactory -- 定义了模块通用流程和对应接口，包括：注册模块流程，初始化玩家模块列表流程。
 *              A)  init()                          // 模块的初始化工作
 *              B)  loadProductData()               // 加载产品数据
 *     3.2.3  注意事项
 *            A)  需要认真考虑模块的划分，减少模块间相互依赖的可能性
 *            B)  模块应当自己管理自己的数据
 *            C)  需要认真考虑模块的接口设计（通用/易懂/方便/完整），提供模块的完整功能，必须对参数做检查
 *            D)  模块不能直接修改其他模块数据，如有需要，请调用其他模块的接口进行修改
 *   3.3  player包，player继承了actor，提供了多线程编程框架，并整合了事件和模块（略）
 *
 * 4.  开发指南
 *   4.1  模块目录结构
 *        moduleName/
 *            |-- event/              // 这个模块产生的事件
 *            |-- listener/           // 这个模块的监听器
 *            |-- gmhandler/          // GM命令
 *            |-- prodata/            // 产品数据
 *            |-- usrdata/            // 用户数据
 *            |-- packet/             // 协议/数据包（C/S，S/S间）
 *            |-- message/            // 消息（Actor）
 *            |-- XXXModuleFactory.java           // 模块的工厂
 *            |-- XXXManager.java                 // 产品数据相关
 *            |-- XXXModule.java                  // 用户数据相关（单个玩家），接口
 *            |-- XXXConst.java                   // 常量相关
 *            |-- XXXPacketSet.java               // 用于注册协议/数据包
 *            |-- package-info.java               // 该模块的说明文档（按需）
 *   4.2  注意事项
 *     4.2.1  产品数据放在XXXManager，用户数据放在XXXModule（对单个玩家），常量放在XXXConst，
 *     4.2.2  产品数据和用户数据必须分开下发，就是说必须有两个数据包
 *     4.2.3  建表语句和存储过程必须写在package-info.java，并且与数据库同步
 *     4.2.4  模块相关的协议放在XXXPacketSet（随模块注册，解决入口问题），并且在PacketChecker划定协议号范围（保证协议
 *            号唯一）
 *     4.2.5  模块函数为public时必定是接口；模块函数为内部使用时，最好保证可见性至少是protected，否则请注释。
 *     4.2.6  模块接口必须对参数进行检查，参数异常可抛出异常告诉调用方。
 *     4.2.7  模块接口必须对玩家状态进行检查，状态异常可抛出异常告诉调用方。
 *   4.3  命名规范
 *     4.3.1  使用驼峰命名法
 *     4.3.2  方法名：以动词、介词开头
 *     4.3.3  变量：以名词、形容词开头
 *     4.3.4  上行数据包：ServerXXX
 *     4.3.5  下行数据包：ClientXXX
 *     4.3.6  产品数据：XXXVo
 *     4.3.7  用户数据：XXXPo
 *   4.4  红点
 *     4.4.1  原理：Player是以消息驱动的，所以可以在消息处理过程中先标记哪些地方要计算红点，在消息处理完成后再进行计算和下发
 *     4.4.2 标记方法：AbstractModule.signCalRedPoint()
 *     4.4.3 计算方法: AbstractModule.calRedPoint()
 *   4.5  摘要数据
 *     4.5.1  原理：Player是以消息驱动的，所以可以在消息处理过程中先标记哪些地方要更新摘要数据，在消息处理完成后再进行更新
 *     4.5.2  标记方法：ModuleContext.markUpdatedSummaryComponent()
 *     4.5.3  更新方法：Module.onUpateSummary()
 *   4.6  零星变量的获取/保存方法（一两变量，又不想单独开一张新表）
 *     4.6.1  支持类型：基本类型（byte, short, int, long, float, double, String）和简单的Map
 *     4.6.2  获取：AbstractModule.getXXX(name)，AbstractModule.getXXX(name, defaultValue)
 *     4.6.3  从Map中获取：AbstractModule.getXXXFromMap(name, key)，AbstractModule.getXXXFromMap(name, key, defaultValue)
 *     4.6.4  保存：AbstractModule.setXXX(name, value)
 *     4.6.5  保存到Map中：AbstractModule.setXXXToMap(name, key, value)
 *   4.7  多条件判断（等级/vip），可以使用精准推送模块中的条件表达式解析器
 *     4.7.1  解析表达式：PushCondNode node = new PushCondParser(new PushCondLexer(condition)).parse();
 *     4.7.2  表达式求值：node.eval(moduleMap)
 *     4.7.3  判断真假：PushUtil.isTrue(node, moduleMap)
 *     4.7.3  例子：PopUpModule和ChargePrefModule
 *
 *   A. 注意事项
 *     A.001  登录时不要获取摘要数据
 *     A.002  登录时少发送跑马灯（如果每个玩家都发送会有问题的）
 *     A.003  登录时少调用公共业务的同步方法
 *     A.004  批量请求公共业务时，请尽可能调用公共业务的批量方法（如果有）
 * Created by zhaowenshuo on 2016/1/15.
 */
package com.stars.modules;