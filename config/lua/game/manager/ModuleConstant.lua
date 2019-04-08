--ModuleConstant.lua
--各模块的常亮
--事件Id列表使用ID_的形式
ModuleConstant = {}


ModuleConstant.UI_OPEN   = 1    --打开界面的标志
ModuleConstant.UI_HIDE   = 2    --关闭界面的标志
ModuleConstant.UI_REMOVE = 3    --销毁界面的标志

ModuleConstant.BUSYINDICATE_SHOW = 100  --繁忙提示
ModuleConstant.BUSYINDICATE_STOP = 101  --繁忙提示
ModuleConstant.GAMENET_DISCONNECT = 102		   -- 掉线通知
ModuleConstant.GAME_QUIT = 103			-- 退出游戏
ModuleConstant.WINDOW_BECOME_VISIBLE = 104		-- 窗口由不可见转可见，参数:窗口名称
ModuleConstant.WINDOW_BECOME_INVISIBLE = 105	-- 窗口由可见转为不可见，参数:窗口名称
ModuleConstant.WINDOW_HIDE = 106  --窗口隐藏
ModuleConstant.WINDOW_REFRESHLAYER = 107  --窗口层级调整

-- 角色 10000-10050
ModuleConstant.CHARAC_ADDMYCHARAC = 10000
ModuleConstant.CHARAC_CREATE = 10001
ModuleConstant.CHARAC_REMOVE = 10002
--lyt 20160729 角色创建完成
ModuleConstant.CHARAC_CREATE_COMPLETE = 10003
ModuleConstant.CHARAC_UPDATE_ATTR = 10004
ModuleConstant.CHARAC_FIREREQUEST = 10005
ModuleConstant.CHARAC_UPDATE_DAMAGE = 10006   -- 玩家造成的伤害
ModuleConstant.CHARAC_AUTOFIGHT_STATE = 10007 -- 玩家自动战斗状态改变
ModuleConstant.CHARAC_REVIVE = 10008
ModuleConstant.CHARAC_UPDATE_ATTR_PVP = 10009  -- 角色血量改变,主要针对由服务端更新血量的情况（如PVP）
ModuleConstant.TRANSFERMAP_START_GOTONPC = 10010;
ModuleConstant.TRANSFERMAP_FINISH_GOTONPC = 10011;
ModuleConstant.CHARAC_REVIVE_FAIL = 10012  --复活失败
ModuleConstant.CHARAC_DAMAGE_SUM  = 10013  --角色伤害统计
ModuleConstant.NPCAVATAR_CREATE   =10014   --NPC被创建
ModuleConstant.MYCHANGE_OUTSHOW   =10015   --外显变更
-- 角色end

--摇杆 10051-10070
ModuleConstant.JOYSTICK_START = 10051
ModuleConstant.JOYSTICK_MOVE  = 10052
ModuleConstant.JOYSTICK_END   = 10053
ModuleConstant.JOYSTICK_SHOW_HIDE  = 10054
ModuleConstant.JOYSTICK_ENABLE = 10055

--login 10071 - 10100
ModuleConstant.LOGIN_WINDOW = 10071
ModuleConstant.CREATE_ROLE_WINDOW = 10072
ModuleConstant.LOGIN_RESPONSE = 10073
ModuleConstant.LOGIN_VERIFY_SUCC = 10074
ModuleConstant.SELECT_SERVER_WINDOW = 10075
ModuleConstant.LOGIN_SUCC = 10076
ModuleConstant.GET_RANDOM_NAME = 10077;
ModuleConstant.LOGIN_SYN_CREATEROLECD = 10078;
ModuleConstant.LOGIN_WINDOW_SHOW_PRELOAD = 10079;
ModuleConstant.SERVERSTATE_UPDATE = 10080;

--scene 10101-10200
ModuleConstant.ENTER_STAGE = 10101
ModuleConstant.LEAVING_STAGE = 10102;
ModuleConstant.FIGHT_SCENE_UPDATEGUIDE = 10103;
ModuleConstant.STAGE_LOADCOMPLETE = 10104;
ModuleConstant.STAGE_ADD_MAINSTAGEREADY_CALLBACK = 10105;
ModuleConstant.FIGHT_READY_GO = 10106;
ModuleConstant.FIRST_ENTER_STAGE = 10107;	-- 首次进入场景
ModuleConstant.NEWGUIDE_STAGE_LOADCOMPLETE = 10108; --新手关卡加载完成

--摄像机 10201-10230
ModuleConstant.CAMERA_POSITION_CHANGED = 10202		-- 主摄像机位置发生变化
ModuleConstant.CAMERA_RADIALBLUREFFECT = 10203;     -- 调用摄像机的径向模糊
ModuleConstant.CAMERA_DISABLERADIALBLUREFFECT = 10204; --停止径向模糊;
ModuleConstant.CAMERA_DRAG = 10205; --相机拖动

--主界面 10301 - 10330
ModuleConstant.MAIN_WINDOW = 10301
ModuleConstant.MAIN_WINDOW_BUTTON_ACTIVE = 10302	-- 开启主界面指定的按钮，事件参数：按钮id(MainWindowDef.BUTTONS)
ModuleConstant.MAIN_WINDOW_ACTIVE_CHANGED = 10303;
ModuleConstant.ACTIVE_AND_FLY_BUTTONS	= 10304;

--关卡界面 10331 - 10400
ModuleConstant.DUNGEON_MAIN_WINDOW = 10331
ModuleConstant.DUNGEON_CHAPTER_UPDATE = 10332
ModuleConstant.DUNGEON_DUNGEON_UPDATE = 10333
ModuleConstant.DUNGEON_DUNGEON_SWEEP = 10334		-- 通知关卡扫荡结果
ModuleConstant.DUNGEON_INFO_WINDOW = 10335
ModuleConstant.CHAPTER_ACTIVE_WINDOW = 10336
ModuleConstant.NEW_DUNGEON_OPEN = 10337
ModuleConstant.POWER_GETWAY_WINDOW = 10338


--角色界面(注意这里使用Hero) 10401-10410
ModuleConstant.ROLE_WINDOW = 10401
ModuleConstant.FASHION_WINDOW= 10406            -- 时装界面数据
ModuleConstant.ROLE_FASHION_MODELVIEW    = 10407    -- 时装界面更新模型 
ModuleConstant.ROLE_FASHION_CAMERA       = 10408    -- 更新后重置相机焦点
ModuleConstant.ROLE_FASHION_SYNCSTATE    = 10409    -- 接受消息后更新场景模型
--ModuleConstant.ROLE_FASHION_DELAYRIDE    = 10410    -- 载入坐骑模型后的延迟事件
--ModuleConstant.ROLE_FASHION_DELAYFASHION = 10411    -- 载入时装时的延迟事件

--称号系统 10421 - 10430
ModuleConstant.ROLE_TITLE_DATA = 10421
ModuleConstant.TITLE_WINDOW = 10422
ModuleConstant.ROLE_TITLE_REFRESH = 10423
ModuleConstant.ROLE_TITLE_HIDE_OR_SHOW = 10424 --称号显示或者隐藏
ModuleConstant.TITLE_VO_RECV = 10425

--背包界面 10371-10385
ModuleConstant.BAG_VIEW_OPEN = 10371
ModuleConstant.USE_TOOL = 10372			-- 使用道具
ModuleConstant.BAG_ITEM_REFRESH = 10373	-- 道具数据刷新
ModuleConstant.SHOW_USE_BOX_UI = 10374	-- 显示宝箱使用提示框,参数为道具toolId
ModuleConstant.SHOW_ITEM_OPERATION_WIN = 10375	-- 显示物品操作界面（出售、使用、购买）
ModuleConstant.BAG_ADD_NEW_ITEM = 10376	-- 新加物品到背包
ModuleConstant.BAG_ITEM_USED = 10377	-- 物品被使用掉
ModuleConstant.BAG_ITEM_COUNT_CHANGED = 10378; --物品数量改变;
ModuleConstant.BAG_MONEY_COUNT_CHANGED = 10379; --金币等的数量改变;

--剧情对话界面 10386 - 10396
ModuleConstant.DIALOG_WINDOW = 10386
--对话开始;
ModuleConstant.DIALOG_START = 10387
--对话结束;
ModuleConstant.DIALOG_END = 10388

-- 战斗界面 10431 - 10470
ModuleConstant.STAGE_FIGHT_WINDOW = 10431
ModuleConstant.STAGE_FIGHT_HPUPDATE = 10432
ModuleConstant.STAGE_FIGHT_SCREEN = 10433
ModuleConstant.STAGE_FIGHT_PARTNER_HP = 10434
ModuleConstant.STAGE_FIGHT_PARTNER_REVIVE_TIME = 10435
ModuleConstant.STAGE_FIGHT_AUTOFIGHT = 10436 --查看自动战斗记录
ModuleConstant.DISABLE_STAGE_FIGHT_WINDOW = 10437 -- 禁掉战斗界面（置灰，不可点击）

-- 角色数据改变 10471 - 10484
ModuleConstant.ROLE_DATA_CHANGED = 10471
ModuleConstant.ROLE_ATTR_CHANGED = 10472
ModuleConstant.ROLE_EXP_CHANGED  = 10473
ModuleConstant.ROLE_LEVEL_UP	 = 10474
ModuleConstant.ROLE_FIGHTSCORE_UP = 10475	-- 角色战力提升
ModuleConstant.ROLE_TITLE_CHANGED = 10476
ModuleConstant.ROLE_VIP_LV_CHANGED = 10478 --vip等级变化了
ModuleConstant.ROLE_NAME_CHANGED = 10479

-- 奖励展示界面 10485 - 10490
ModuleConstant.AWARD_SHOW_WINDOW = 10485

-- 获取途径界面 10491 - 10500
ModuleConstant.GETWAY_WINDOW = 10491

-- 装备界面 10500-10530
ModuleConstant.EQUIPMENT_WINDOW = 10500
ModuleConstant.EQUIPMENT_WINDOWNAME = "EquipmentWindow";
ModuleConstant.EQUIPMENT_PREVIEW_FUMO_ATTR_UPDATE = 10501;
ModuleConstant.EQUIPMENT_XILIAN_UPDATE = 10502;
ModuleConstant.EQUIPMENT_BATCHRESULT_WINDOW = 10503
ModuleConstant.EQUIPMENT_REPLACEXILIAN_WINDOW = 10504
--获取装备的宝石列表窗口显示的通知;
ModuleConstant.EQUIPMENT_GEN_LISTWINDOW_ACTIVED = 10505;
ModuleConstant.EQUIPMENT_ADD_NEWVO = 10506;
ModuleConstant.EQUIPMENT_TISHENOPR_SUCCESS = 10507;
--装备界面隐藏的事件通知;
ModuleConstant.EQUIPMENT_VISIBLE_STATE = 10508;
ModuleConstant.EQUIPMENT_ALLGEM_COMPOSEINFO = 10509;
--新装备 
ModuleConstant.NEWEQUIP_REFRESH = 10510
ModuleConstant.NEWEQUIP_XILIAN_SHOW = 10511
ModuleConstant.NEWEQUIP_ZHUANYI_SHOW = 10512
ModuleConstant.STRENGTHEN_INFO = 10513  --强化信息
ModuleConstant.STARLEVEL_INFO = 10514   --星级信息
ModuleConstant.NEWEQUIP_OPEN_CHANGE_INFO = 10515 --打开界面时指定显示内容
ModuleConstant.NEWEQUIP_MARK_CHANGE_INFO = 10516 --装备角标内容改变
ModuleConstant.NEWEQUIP_PUTON_TIPS_SHOW = 10517  --显示装备穿戴提示是抛出一个事件
ModuleConstant.NEWEQUIP_ADD_NEWVO_END = 10518  --所有数据更新完后

ModuleConstant.FUWENPROMOTESLECTCHANGE = 10519  --符文升级选择修改
ModuleConstant.FUWENREFRESHESLECTCHANGE = 10520  --符文洗炼选择修改
ModuleConstant.FUWENREFRESHERESLECTCHANGE = 10521  --符文洗炼结果选择修改
ModuleConstant.FUWEN_REFRESH_CHANGE = 10522  --符文洗炼结果修改
ModuleConstant.FUWEN_RESOLVE_BACK = 10523  --符文洗炼结果修改
ModuleConstant.FUWEN_REFRESH_CHANGEFINISH = 10524  --符文洗炼替换完成
ModuleConstant.FUWEN_LEVEUP_CHANGEFINISH = 10525  --符文等级提升

ModuleConstant.FUWEN_JICHENG_WINDOW = 10526 		-- 符文继承窗口
ModuleConstant.FUWEN_JICHENG_TIPS_WINDOW = 10526 	-- 转移时符文继承提示窗口

--关卡结算界面 10531-10540
ModuleConstant.STAGE_RESULT_WINDOW = 10531


-- 加载界面 10541 - 10545
ModuleConstant.LOADING_WINDOW = 10541
ModuleConstant.LOADING_UPDATE = 10542
ModuleConstant.LOADING_VISIBLE_CHANGED = 10543

--扫荡界面 10546-10550
ModuleConstant.SWEEP_WINDOW = 10546

-- 任务系统 10551 - 10600
ModuleConstant.SHOW_TASK_TRACE = 10551		-- 显示任务追踪界面
ModuleConstant.TASK_MAIN_WINDOW = 10552		-- 显示/隐藏任务主界面
ModuleConstant.TASK_DATA_CHANGED = 10553	-- 任务数据改变
ModuleConstant.TASK_REMOVE = 10554			-- 任务被移除
ModuleConstant.SHOW_TASK_COLLECT = 10555	-- 显示任务采集界面
ModuleConstant.TASK_STATE_CHANGED = 10556	-- 任务状态改变
ModuleConstant.HIDE_TASK_TRACE = 10557		-- 隐藏任务追踪界面
ModuleConstant.CHECK_POEM_SYSTEM_OPEN = 10558;

--剧情系统 10601 - 10620
ModuleConstant.STORY_BEGIN = 10601 --开始剧情
ModuleConstant.STORY_UIBLACK_OPEN = 10602 --剧情系统黑屏界面
ModuleConstant.STORY_INTRODUCE_OPEN = 10603 --剧情介绍界面
ModuleConstant.STORY_CANNELBTN_OPEN = 10604 --跳过剧情按钮
ModuleConstant.STORY_TIPS_WINDOW = 10605--显示剧情tip界面
ModuleConstant.STORY_MAP_TIPS_WINDOW = 10606--显示剧情地图tips界面
ModuleConstant.STORY_BOSS_INFO_WINDOW = 10607--显示boss信息
ModuleConstant.STORY_FINISH = 10608--播完剧情
ModuleConstant.STORY_INDUCT = 10609--剧情调用引导

-- 战力系统 10621 - 10640
ModuleConstant.FIGHT_ABILITY_REFRESH = 10621
ModuleConstant.FIGHT_ABILITY_GIFT = 10622
ModuleConstant.FIGHT_SCORE_UPDATE = 10623
ModuleConstant.FIGHT_ABILITY_GETGIFT_RESULT = 10624
ModuleConstant.FIGHT_SCORE_MAIN_WINDOW = 10625


--技能升级系统 10641-10650
ModuleConstant.SKILLUP_SELECTED = 10641   --技能选中
ModuleConstant.SKILL_LV_UPDATE  = 10642   --技能升级
ModuleConstant.SKILL_LV_UP_VO   = 10643   --技能产品数据
ModuleConstant.SKILL_WINDOW     = 10644   --技能升级界面显示 
ModuleConstant.SKILL_REPLACE    = 10645   --技能替换 
ModuleConstant.SKILLUP_SELECTED_2 = 10646 --技能选中2

--好友系统 10651-10665
ModuleConstant.FRIEND_NOTICE_WIN = 10651--有新好友通知
ModuleConstant.FRIEND_AFFIRM_WIN = 10652--好友相关操作确认界面
ModuleConstant.FRIEND_WIN = 10653--打开或关闭好友界面
ModuleConstant.FRIEND_UPDATE_RECOMMEND = 10654--推荐好友数据更新
ModuleConstant.FRIEND_UPDATE_MY_FRIEND = 10655--我的好友数据更新
ModuleConstant.FRIEND_UPDATE_APPLY_FRIEND = 10656--申请好友数据更新
ModuleConstant.FRIEND_UPDATE_BLACKLIST = 10657--黑名单数据更新
ModuleConstant.FRIEND_UPDATE_CONTACT_FRIEND = 10658--最近联系人数据更新
ModuleConstant.FRIEND_SEND_CHAT = 10659--发送好友聊天
ModuleConstant.SEND_FLOWER_WINDOW = 10660--打开送花界面
ModuleConstant.SEND_FLOWER_RECORD_VIEW = 10661--打开送花记录界面
ModuleConstant.UPDATE_FLOWER_RECORD_LIST = 10662--更新送花记录
ModuleConstant.SEND_FLOWER_SUCESS = 10663--送花成功
ModuleConstant.FIRST_SEND_FLOWER_AWARD = 10664--是否领取第一次送花奖励
ModuleConstant.PRIVATE_CHAT_WINDOW = 10665--打开私聊界面

--玩家详情 10666-10680
ModuleConstant.PLAYER_INFO_WIN = 10666--打开或关闭玩家详情界面
ModuleConstant.SHOW_FIGHT_COMPARE_VIEW = 10667--打开战力对比界面
ModuleConstant.SHOW_ROLE_COMPARE_VIEW = 10668--打开角色对比界面


--伙伴界面 10681 - 10690
ModuleConstant.PARTNER_WINDOW = 10681  	--打开或关闭伙伴界面
ModuleConstant.PARTNER_REFRESH = 10682  --刷新伙伴界面 
ModuleConstant.PARTNER_GUARD_UPDATE = 10683 --刷新护卫界面

-- 聊天系统 10691 - 10710
ModuleConstant.CHAT_MAIN_WINDOW = 10691			-- 打开或关闭聊天主界面
ModuleConstant.CHAT_SET_WINDOW = 10692			-- 打开或关闭聊天设置界面
ModuleConstant.CHAT_INSERT_OBJECT = 10693		-- 向聊天输入框中插入对象,如表情，道具等
ModuleConstant.CHAT_NEW_MSG_COME = 10694		-- 有新的聊天消息到达
ModuleConstant.CHAT_PERSONAL_MSG_COME = 10695	-- 有新的私密消息到达
ModuleConstant.CHAT_OBJECT_WINDOW = 10696		-- 聊天插入对象界面
ModuleConstant.CHAT_SIMPLE_VIEW = 10697			-- 主界面简单的聊天框
ModuleConstant.CHAT_TIME_UPDATE = 10698			-- 更新最近一次的聊天时间
ModuleConstant.CHAT_SETTING_CHANGED = 10699		-- 聊天设置改变
ModuleConstant.CHAT_PERSONAL_REQUEST = 10700	-- 请求私聊，将打开好友聊天界面
ModuleConstant.CHAT_PERSONAL_MSG_STATE_CHANGED = 10701; --私人信息状态变化通知, 标记是否查看了;
ModuleConstant.CHAT_CLEAR_MSG_HISTORY = 10702;  --队伍信息离队清除数据


-- 活跃度系统 10711 - 10720
ModuleConstant.DAILY_PO  = 10711     --下发活跃度玩家数据
ModuleConstant.DAILY_AWARD_SUCCESS = 10712 --获取奖励成功
ModuleConstant.DAILY_WINDOW = 10713			-- 打开日常界面
ModuleConstant.DAILY_LIMITTIMECHNAGE = 10714			-- 限时任务修改
ModuleConstant.RIDESHOW = 10715			-- 坐骑
ModuleConstant.FINDTREASHOW = 10716			-- 6国寻宝

-- 家族系统 10721 - 10770
ModuleConstant.FAMILY_MAIN_WINDOW = 10721			-- 家族主界面
ModuleConstant.FAMILY_LIST_WINDOW = 10722			-- 家族列表界面
ModuleConstant.FAMILY_CREATE_WINDOW = 10723			-- 家族创建界面
ModuleConstant.FAMILY_APPLY_WINDOW = 10724			-- 家族申请列表界面
ModuleConstant.FAMILY_APPLY_LIST_CHANGED = 10725	-- 申请列表改变
ModuleConstant.FAMILY_SNATCH_REDPACKET_WINDOW = 10726	-- 抢红包界面
ModuleConstant.FAMILY_INFO_CHANGED = 10727			-- 自己家族数据改变, 事件会带一个协议号和子协议类型
ModuleConstant.FAMILY_INFO_WINDOW = 10728			-- 玩家家族信息界面
ModuleConstant.FAMILY_LIST_CHANGED = 10729			-- 家族列表改变
ModuleConstant.FAMILY_EXPEDITION_WINDOW = 10730		-- 家族远征界面
ModuleConstant.FAMILY_EXPD_AWARD_WINDOW = 10731		-- 家族远征奖励选择界面
ModuleConstant.FAMILY_UPGRADE_INFO_CHANGED = 10732	-- 家族升级信息改变
ModuleConstant.FAMILY_BONFIRE_WINDOW = 10733		-- 家族篝火活动界面
ModuleConstant.FAMILY_EXPED_RESULT_WINDOW   = 10734 --家族远征结算界面
ModuleConstant.FAMILY_ACTIVITY_LIST_CHANGED = 10735 --家族活动列表
ModuleConstant.FAMILY_EXPEDITION_UPDATE     = 10736 --家族远征用户数据更新
ModuleConstant.FAMILY_EXPEDITION_BUFF       = 10737 --家族远征buff
ModuleConstant.FAMILY_LEVEL_UP              = 10738 --家族等级变化
ModuleConstant.FAMILY_REDPACKET_SEND_WINDOW = 10739 --家族红包发送界面
ModuleConstant.FAMILY_BONFIRE_ASK_WINDOW    = 10740 --家族篝火问答界面
ModuleConstant.FAMILY_BONFIRE_START         = 10741 --家族篝火开始
ModuleConstant.FAMILY_BONFIRE_STOP         = 10742 --家族篝火结束


-- 排行榜系统 10771 - 10780
ModuleConstant.RANK_WINDOW    =  10772     --打开或关闭排行榜界面
ModuleConstant.RANK_ITEM_SHOW = 10773      --显示排行榜item
ModuleConstant.RANK_COMMON_WINDOWNAME = "CommonRankWindow"
ModuleConstant.RANK_COMMON_WITH_TYPE_WINDOW = "CommonRankWithTypeWindow"
ModuleConstant.RANK_COMMON_WINDOW = 10774      --通用的单个排行榜系统;
ModuleConstant.RANK_COMMON_WITH_TYPE_WINDOW_UPDATE  = 10775
ModuleConstant.RANK_COMMON_WITH_TYPE_WINDOW_REFRESH = 10776

--探宝系统 10781-10800
ModuleConstant.SEARCH_TREASURE_WINDOW = 10781 --打开探宝系统界面
ModuleConstant.SEARCH_MAP_VIEW = 10782--打开探宝地图界面
ModuleConstant.SEARCH_TREASURE_FIGHT_VIEW = 10783--打开探宝战斗界面
ModuleConstant.GET_TREASURE_VIEW = 10784--打开探宝的领取奖励界面
ModuleConstant.REVIVE_TIPS_VIEW = 10785--打开复活提示界面
ModuleConstant.UPDATE_SEARCH_TREASURE_DATA = 10786--玩家探宝数据更新
ModuleConstant.UPDATE_SEARCH_MAP_DATA = 10787 --searchMap数据更新
ModuleConstant.UPDATE_SEARCH_STAGE_DATA = 10788 --searchStage数据更新
ModuleConstant.CREATE_TREASURE_CHEST = 10789--创建宝箱
ModuleConstant.DEL_TREASURE_CHEST = 10790--移除单个宝箱
ModuleConstant.UPDATE_TREASURE_GOOD_ITEM = 10791--探宝的道具更新
ModuleConstant.UPDATE_SEARCH_POINT_PROGRESS = 10792--更新探索点的进度
ModuleConstant.SEARCH_MAP_OR_STAGE_COMPLETE = 10793--通知探索的地图或层已经完成
ModuleConstant.CHAT_VIEW_MOVE = 10794--聊天界面移动
ModuleConstant.SHOW_GOODS_TIPS = 10795--显示道具tip

--end

--组队系统 10801-10820
ModuleConstant.TEAM_WINDOW = 10801 --打开或关闭主队界面
ModuleConstant.UPDATE_TEAM_DUNGEON_LIST = 10802--组队副本列表更新
ModuleConstant.UPDATE_TEAM_LIST = 10803--更新队伍列表数据
ModuleConstant.UPDATE_TEAM_MEMBER_LIST = 10804--更新队伍成员信息
-- ModuleConstant.UPDATE_TEAM_DATA = 10805--更新队伍的基本信息
ModuleConstant.UPDATE_TEAM_APPLY_LIST = 10806--更新申请列表
ModuleConstant.UPDATE_TEAM_CAN_INVITE_LIST = 10807--更新可邀请玩家列表
ModuleConstant.UPDATE_TEAM_INVITE_LIST = 10808--更新邀请列表
ModuleConstant.UPDATE_TEAM_CANCEL_MATCH = 10809--取消匹配
ModuleConstant.TEAM_CAN_INVITE_VIEW = 10810--打开可邀请界面
ModuleConstant.TEAM_INVITE_VIEW = 10811--打开邀请界面
ModuleConstant.TEAM_APPLY_VIEW = 10812--打开申请界面
ModuleConstant.UPDATE_TEAM_ENEMY_WAVE = 10813--更新组队怪物波数
ModuleConstant.UPDATE_TEAM_FIGHT_ITEM = 10814--更新战斗item
ModuleConstant.UPDATE_TEAM_INVITE_FLAG = 10815--更新邀请标示
ModuleConstant.CHANGE_TEAM_TARGET = 10816 --更改队伍目标
ModuleConstant.TEAM_LEADER_CHANGED = 10817 --队长变更
--end

-- 坐骑系统 10821 - 10830
ModuleConstant.RIDE_WINDOW			= 10821	-- 坐骑系统主界面
ModuleConstant.RIDE_SYNC_STATE  	= 10822 -- 角色坐骑状态
ModuleConstant.RIDE_CAMERA_TARGET   = 10823 -- 当场景中坐骑载入完毕后需要通知更改相机焦点
ModuleConstant.RIDE_INIT_STATE  	= 10824 -- 角色坐骑初始化状态
ModuleConstant.RIDE_LEVEL_UP_WINDOW = 10825 -- 升级提示窗口
ModuleConstant.RIDE_UPDAE 			= 10826	-- 坐骑数据更新
ModuleConstant.RIDE_AWAKEUPDATE		= 10827 -- 坐骑觉醒更新
--end

-- 滚动公告 10831 - 10835
ModuleConstant.SHOW_ROLL_NOTICES = 10831

-- 系统开放预告 10841 - 10850
ModuleConstant.SYS_OPEN_WINDOW = 10841		-- 系统开放预告界面
ModuleConstant.SYS_OPEN_INFO_CHANGED = 10842	-- 系统开放预告信息变化
ModuleConstant.SYS_OPEN_FINISH = 10843
ModuleConstant.SYS_ACTIVE_NOW = 10844		-- 某个系统开放了
ModuleConstant.SYS_ACTIVE_FLY_END = 10845		-- 某个系统飞完毕了
-- 运镖系统 10851 - 10880
ModuleConstant.CARGO_WINDOW = 10851
ModuleConstant.CARGO_INFO_WINDOW = 10852
ModuleConstant.CARGO_SELECT_WINDOW = 10853
ModuleConstant.CARGO_TROOP_UPDATE = 10854		-- 更新镖车队列
ModuleConstant.CARGO_INFO_CHANGED = 10855		-- 运镖系统信息变更（界面）
ModuleConstant.CARGO_TROOP_WINDOW = 10856		-- 运镖队列场景的界面
ModuleConstant.CARGO_TRANSPORT_STATE_CHANGED = 10857	-- 运镖战斗状态改变   0 为运送中， 1为被人劫镖中

-- 战斗相关 20000-21000
ModuleConstant.SKILL_FIRE = 20001 --释放技能
ModuleConstant.SKILL_FINISH = 20002 --技能结束
ModuleConstant.SKILL_DESTROY = 20003 --销毁
ModuleConstant.SKILL_MAKEEFFECT = 20004 --命中效果
ModuleConstant.SKILL_CREATEBULLET = 20005 --创建飞行
ModuleConstant.SKILL_FIRE_SUCCESS = 20006 -- 释放技能成功
ModuleConstant.SKILL_COLLISION = 20007 --技能碰撞
ModuleConstant.SKILL_SHOWEFFECT = 20008 --技能效果的表现
ModuleConstant.SKILL_CLICKFIRE = 20009 --点击释放技能
ModuleConstant.SKILL_REAL_START = 20010 -- 技能真正开始

ModuleConstant.PLAY_SIDE_EFFECT = 20049 -- SideEffectPlay专用
ModuleConstant.STOP_SIDE_EFFECT = 20051 --停止（实际是销毁）特效
ModuleConstant.PLAY_SKILL_EFFECT = 20052 --播放技能特效
ModuleConstant.PLAY_AUTO_EFFECT = 20053
ModuleConstant.ACTIVE_SIDE_EFFECT = 20054--设置特效显示和隐藏
ModuleConstant.PLAY_AUTOEFFECT_IN_WORLDSPACE = 20176	-- 在世界坐标播放特效
ModuleConstant.SHOW_FIGHT_NUMBER = 20177	-- 战斗飘字{uniqueID,FightDefine.FIGHT_NUM_FLAG,value,数字类型}
ModuleConstant.CHARACTER_DEAD = 20179		-- 角色死亡（血量为0时就派发，这时候死亡动作还没播完）
ModuleConstant.FIGHT_FINISH = 20180			-- 通知战斗结束
ModuleConstant.BOSS_BLOOD_SHOW = 20181      -- 显示boss血条
ModuleConstant.BOSS_HP_CHANGE  = 20182      -- boss血量变化
ModuleConstant.COMBO_HIT_CHANGE = 20183		-- 连击数变化
ModuleConstant.ADD_BUFF = 20184				-- 添加BUFF
ModuleConstant.REMOVE_BUFF = 20185			-- 移除BUFF
ModuleConstant.ADD_BUFF_EFFECT = 20186		-- 添加一个BUFF效果到某个buff上
ModuleConstant.PLAY_DROP = 20187			-- 播放掉落
ModuleConstant.FIGHT_FINISH_TO_STORY = 20188 --通知剧情系统，战斗结束
ModuleConstant.BOSS_COMING = 20189			-- BOSS准备要出来啦
ModuleConstant.REMOVE_CHARC_BUFF = 20190	-- 移除某个角色的所有buff
ModuleConstant.FIGHTER_ADDEXP = 20300       -- 角色增加经验值
ModuleConstant.FIGHTER_SCOREUP = 20301      -- 角色增加战力
ModuleConstant.PARTNER_ADDEXP = 20302       -- 伙伴增加经验值
ModuleConstant.COMBO_KILL_CHANGE = 20303		-- 连斩数变化
ModuleConstant.FIGHT_INTEGRAL = 20304       -- 积分变化

ModuleConstant.AI_CREATE			= 20191			-- 创建一个AI
ModuleConstant.AI_DESTROY			= 20192			-- 移除某个AI
ModuleConstant.AI_AWAKE				= 20193			-- 激活某个AI
ModuleConstant.AI_START				= 20194			-- 开始某个AI
ModuleConstant.AI_STOP				= 20195			-- 停止某个AI
ModuleConstant.AI_CHANGE_PROPERTY   = 20196			-- 更改AI属性
ModuleConstant.AI_BREAK_STATE		= 20197
ModuleConstant.AI_CLEARROUNDTIME	= 20198			-- 清除回合计时
ModuleConstant.AI_ACTION_MOVE		= 20199			-- AI指令：移动
ModuleConstant.PLAYER_SWITCH_AI		= 20200			-- 启用/禁用玩家AI

ModuleConstant.SERVER_TO_CLIENT_LOG = 20201			-- 将服务端输出同步到客户端
ModuleConstant.PLAYER_STATE_END = 20202				-- 玩家某个状态结束时派发
ModuleConstant.CAMERA_SLOW_ACTION = 20203           -- 镜头慢动作
ModuleConstant.PLAYER_TIMING_SYNC = 20204			-- PVP时定时同步玩家信息（位置，朝向等）
ModuleConstant.SERVER_SYNC_PLAYERINFO = 20205		-- 服务端定向发送玩家状态给指定客户端


ModuleConstant.CLIENT_PVP_INITDATA = 20206			-- PVP中由服务端lua下发的初始化数据
ModuleConstant.CLIENT_PVP_UPDATEPLAYER = 20207		-- PVP中由服务端lua下发的玩家产品数据
ModuleConstant.CLIENT_PVP_CHARACINFO = 20208		-- PVP中由服务端lua下发的玩家动态数据
ModuleConstant.PVP_FIGHT_START = 20209				-- PVP中服务端通知客户端战斗开始
ModuleConstant.PVP_ALL_READY = 20210				-- PVP中全部人员准备完毕
ModuleConstant.CLIENT_LOOTTREASURE_PVE_UPDATEPLAYER = 20211;
ModuleConstant.CLIENT_LOADINGEND = 20212			-- PVP中客户端loading完毕
ModuleConstant.CLIENT_SYNC_FIGHTSTATE = 20213		-- PVP中由服务端lua同步服务端当前的战斗状态给客户端(状态，时间等)
ModuleConstant.RUNESPAWNPOINT_CREATE = 20214		-- 创建神符刷新点
ModuleConstant.RUNESPAWNPOINT_REMOVE = 20215		-- 移除神符刷新点
ModuleConstant.RUNE_CREATE = 20216					-- 创建神符
ModuleConstant.RUNE_PICKUP = 20217					-- 拾取神符
ModuleConstant.SYNC_SERVER_RUNE_INFO = 20218		-- 当有新玩家加入服务端时，将神符信息发给他
ModuleConstant.SPAWN_MONSTER = 20219				-- 刷怪通知
ModuleConstant.SERVER_SYNC_MONSTER = 20220 			-- 定时同步怪物
ModuleConstant.SYNC_ALLCHARAC_RUNTIME_INFO = 20221	-- 同步所有角色的运行时数据给新加入的玩家
ModuleConstant.SYNC_SKILL_CD = 20222				-- 同步玩家当前剩余的技能CD给客户端(从战斗中进入再离开)
ModuleConstant.SERVER_LUA_SHOW_MSG = 20223			-- 战斗服lua通知显示提示消息
ModuleConstant.DRAGONBALL_SETVISIBLE = 20224		-- 修改龙珠显示状态
ModuleConstant.CAMPDAILY_UNLOCK_PASSIVESKILL = 20225 -- 阵营日常,开启被动技能阵营日常
ModuleConstant.CAMPDAILY_CHANGE_SKILL = 20226 --阵营日常,更改人物角色技能

ModuleConstant.SERVER_SYNC_ROBOTINFO = 20227		-- 服务端定向发送机器人状态给指定客户端
-- 战斗相关end


-- 邮箱系统 21001 - 21010
ModuleConstant.EMAIL_WINDOW = 21001			--邮箱窗口
ModuleConstant.EMAIL_REFRESH = 21002		--刷新邮件列表
ModuleConstant.EMAIL_AFFIXGOT = 21003		--领取附件完成
--end

-- 活动相关 30000
--镇妖塔 30000~30010
ModuleConstant.ACTIVITY_SKYTOWER_WINDOW = 30000
ModuleConstant.ACTIVITY_SKYTOWER_WINDOWNAME = "SkyTowerWindow"
ModuleConstant.ACTIVITY_SKYTOWER_RECEIVEINFO = 30001
ModuleConstant.ACTIVITY_SKYTOWER_RECEIVEVOS = 30002

--世界boss 30011 - 30020
ModuleConstant.ACTIVE_CALL_BOSS_WINDOW  = 30011  --打开boss界面
ModuleConstant.CALL_BOSS_SELECT         = 30012  --点击boss头像 
ModuleConstant.CALL_BOSS_STATE          = 30013  --倒计时
ModuleConstant.CALL_BOSS_SHOW_BOSS_LIST = 30014  --显示boss列表
ModuleConstant.CALL_BOSS_CALL_RECORD    = 30015  --显示召唤记录

ModuleConstant.CALL_BOSS_SELECT_RECORD  = 30016  --选择某个记录
ModuleConstant.CALL_BOSS_RANK           = 30017  --排行榜数据
ModuleConstant.CALL_BOSS_STATE_UPDATE   = 30018  --召唤boss的状态修改
ModuleConstant.CALL_BOSS_RESULT         = 30019  --召唤boss结算

--仙山探宝 30050-30070
ModuleConstant.ACTIVITY_GAMECAVE_WINDOW = 30050;--洞府界面;
ModuleConstant.ACTIVITY_TINYGAME_REMAINCOUNT = 30051;  --小游戏的剩余次数;
ModuleConstant.ACTIVITY_TINYGAME_READYTOPLAY = 30052;  --准备开始玩小游戏,已进入小游戏界面;
ModuleConstant.ACTIVITY_TINYGAME_EXIT = 30053;  --离开小游戏;
ModuleConstant.ACTIVITY_TINYGAME_STEP = 30054;  --小游戏的步骤信息;
ModuleConstant.ACTIVITY_TINYGAME_RESULT = 30055;--小游戏结果;
ModuleConstant.ACTIVITY_TINYGAME_RECEIVEINFO = 30056;--小游戏的基本信息;
ModuleConstant.ACTIVITY_TINYGAME_PLAY = 30057;  --开始玩小游戏;
ModuleConstant.ACTIVITY_TINYGAME_SENDTOSERVER_STARTGAMEDATA = 30058;  --发送到服务器,告诉服务器可以开始游戏逻辑了;
ModuleConstant.ACTIVITY_TINYGAME_SCORE = 30059; --更新积分;
ModuleConstant.ACTIVITY_GAMECAVE_RANK_WINDOW = 30060;  --洞府排行榜界面
ModuleConstant.ACTIVITY_GAMECAVE_RANK_REFRESH = 30061;  --洞府排行榜数据刷新
ModuleConstant.ACTIVITY_TINYGAME_OPENTRACEWINDOW = 30062; --小游戏打开追踪界面;
ModuleConstant.ACTIVITY_TINYGAME_REALLY_EXIT = 30063;  --请求真正离开小游戏;
ModuleConstant.ACTIVITY_TINYGAME_NPC_INIT_COMPLETED = 30064;  --游园的NPC初始化完毕;
ModuleConstant.ACTIVITY_TINYGAME_BET_COMPLETED = 30065;  --赌大小游戏完成了;


-- 引导系统相关 30070 - 30080
ModuleConstant.REQUEST_INDUCT_EXECUTE = 30070	--请求执行引导
ModuleConstant.INDUCT_FINISH_STEP_INDEX = 30071;
ModuleConstant.INDUCT_START_STEP_INDEX = 30072;
ModuleConstant.INDUCT_VIEW_SETACTIVE = 30073;
ModuleConstant.INDUCT_VIEW_BESTRONG = 30074;
ModuleConstant.INDUCT_LEVEL_CHANGED = 30075;
ModuleConstant.INDUCT_VIEW_FORCE_SETINACTIVE = 30076;
--end

--经验副本相关 30081 - 30090
ModuleConstant.PRODUNGEON_WINDOW = 30081 			--窗口
ModuleConstant.PRODUNGEON_CLICKED = 30082 			--点击进入经验副本
ModuleConstant.PRODUNGEON_FIGHT_START = 30083 		--已初始化战斗界面窗口
ModuleConstant.PRODUNGEON_FIGHT_RESULT = 30084 		--战斗结果
--end

--商店系统 30091 - 30100
ModuleConstant.SHOP_WINDOW = 30091 			--窗口
ModuleConstant.SHOP_REFRESH = 30092 		--刷新
--end

--法宝系统 30101 - 30110
ModuleConstant.TRUMP_WINDOW = 30101 		--窗口
ModuleConstant.TRUMP_REFRESH = 30102 		--刷新 
ModuleConstant.TRUMP_SHOWLVLUP = 30103 		--显示炼化界面 
--end

--心法系统 30111 - 30120
ModuleConstant.MIND_WINDOW       = 30111  --心法系统界面
ModuleConstant.MIND_ITEM_SELECT  = 30112  --心法item被选中
ModuleConstant.MIND_STATE_UPDATE = 30113  --心法状态更新 
ModuleConstant.MIND_GRADE_UPDATE = 30114  --心法等级更新
ModuleConstant.MIND_VIEW_DISPOSE = 30115  --销毁

--离线pvp 30131 - 30150
ModuleConstant.OFFLINE_PVP_WINDOW = 30131 --打开离线pvp窗口
ModuleConstant.OFFLINE_PVP_UPDATE_VIEW = 30132--更新离线pvp界面
ModuleConstant.OFFLINE_PVP_UPDATE_SELECTED = 30133--更新选择对手
ModuleConstant.OFFLINE_PVP_UPDATE_AWARD = 30134--更新领取奖励
ModuleConstant.OFFLINE_PVP_UPDATE_TIMES = 30135--更新刷新次数和挑战次数
--end

--结算界面 30161 - 30165
ModuleConstant.VICTORY_WINDOW = 30161 --胜利界面
ModuleConstant.FAIL_WINDOW = 30162 --失败界面
--end


-- 野外夺宝30171 - 30180
ModuleConstant.ACTIVITY_LOOTTREASURE_WINDOW = 30171
ModuleConstant.ACTIVITY_LOOTTREASURE_WINDOWNAME = "LootsectionWindow"
ModuleConstant.ACTIVITY_LOOTTREASURE_RESULTVIEWNAME = "ResultFailWindow";
ModuleConstant.ACTIVITY_LOOTTREASURE_INFO = 30173
ModuleConstant.ACTIVITY_LOOTTREASURE_MONSTER_HPINFO = 30174;
ModuleConstant.ACTIVITY_LOOTTREASURE_RANKINFO = 30175
ModuleConstant.ACTIVITY_LOOTTREASURE_SINGLEDAMAGE = 30176;
ModuleConstant.ACTIVITY_LOOTTREASURE_BOXCOUNTINFO = 30177;
ModuleConstant.ACTIVITY_LOOTTREASURE_RESULTVIEW = 30178;
ModuleConstant.ACTIVITY_LOOTTREASURE_RESULTVIEW_OPEN = 30179;
ModuleConstant.ACTIVITY_LOOTTREASURE_RESULTVIEW_HIDE = 30180;

--成就系统30181 - 30190
ModuleConstant.ACHIEVE_WINDOW = 30181
ModuleConstant.ACHIEVE_REFRESH = 30182
ModuleConstant.ACHIEVE_RECEIVE_NEW = 30183
ModuleConstant.ACHIEVE_STAGEUP_WINDOW = 30184
ModuleConstant.ACHIEVE_STAGEUP_TIPS_WINDOW = 30185
ModuleConstant.ACHIEVE_PERFECT_WINDOW = 30186
ModuleConstant.ACHIEVE_RANK_WINDOW = 30187
ModuleConstant.ACHIEVE_RANK_UPDATE = 30188
ModuleConstant.ACHIEVE_CHANGE_UPDATE = 30189
ModuleConstant.ACHIEVE_REDPOINT_UPDATE = 30190
--end

--外族入侵30191-30210
ModuleConstant.FAMILY_INVADE_WINDOW = 30191
ModuleConstant.FAMILY_INVADE_CREATE_NPC = 30192--创建npc
ModuleConstant.FAMILY_INVADE_UPDATE_NPC = 30193--更新npc状态
ModuleConstant.FAMILY_INVADE_CREATE_AWARD_BOX = 30194--创建宝箱
ModuleConstant.FAMILY_INVADE_UPDATE_AWARD_BOX = 30195--更新宝箱状态
ModuleConstant.FAMILY_INVADE_UPDATE_RANK = 30196--伤害排行版更新
ModuleConstant.FAMILY_INVADE_ACTIVITY_START = 30197--外族入侵活动开始
ModuleConstant.FAMILY_INVADE_ACTIVITY_END = 30198--外族入侵活动结束
ModuleConstant.FAMILY_SHOW_CLICK_BG = 30199
--end

--宝石系统30211~30230
ModuleConstant.GEM_WINDOW = 30211;
ModuleConstant.GEM_WINDOW_WINDOWNAME = "GemWindow";
ModuleConstant.GEM_LEVELUP_PANEL = 30212;
ModuleConstant.GEM_CLOSE_WINDOW = 30213;
ModuleConstant.GEM_MAIN_WINDOW_BAGLISTUPDATE = 30214;
--end


--在线pk系统30231~30240
ModuleConstant.ONLINEPVP_MAINMENU_POINT = 30231 --  主界面PK提示点
ModuleConstant.ONLINEPVP_WINDOW         = 30232 --  打开
ModuleConstant.ONLINEPVP_RESULT         = 30233 --  结束事件
ModuleConstant.ONLINEPVP_RKRECORD       = 30234 --  PK记录数据
--end

--勇者试炼 30241~30250
ModuleConstant.BRAVETRIAL_WINDOW = 30241  --勇者试炼界面相关
ModuleConstant.BRAVETRIAL_INFO   = 30242
ModuleConstant.BRAVETRIAL_AWARD  = 30243 

--皇榜悬赏 30251~30260
ModuleConstant.ROYALREWARD_WINDOW = 30251 --皇榜悬赏界面相关
ModuleConstant.ROYALREWARD_INFO   = 30252 --皇榜悬赏信息
ModuleConstant.ROYALREWARD_UPDATE = 30253 --皇榜悬赏更新
ModuleConstant.ROYALREWARD_REFRESHCD = 30254 --皇榜悬赏刷新倒计时
ModuleConstant.ROYALREWARD_FINISH  = 30255 --皇榜悬赏任务完成
ModuleConstant.ROYAL_TASK_AUTOACCEPT = 30256 --皇榜悬赏任务完成
--end

--巅峰对决PVP系统 30261-30270
ModuleConstant.FIGHTINGMASTER_WINDOW = 30261 -- 打开



--红点 30271-30280
ModuleConstant.REDPOINT_UPDATE = 30271
ModuleConstant.REDPOINT_DATA_UPDATE = 30272
ModuleConstant.REDPOINT_MAINWINDOW_CHANGED = 30273

-- 游戏设置 30281 - 30290
ModuleConstant.GAME_SETTING_WINDOW = 30281
ModuleConstant.GAME_SETTING_QUALITYSETEFFECT = 30282;
ModuleConstant.GAME_SETTING_GRAPHICCHANGE = 30283;

-- 签到系统 30291 - 30300
ModuleConstant.SIGNIN_WINDOW = 30291   --签到奖励的界面相关
ModuleConstant.SIGNIN_INFO   = 30292   --签到奖励的数据

-- 结婚系统30301 - 30320
ModuleConstant.MARRY_WINDOW       = 30301 -- 结婚窗口
ModuleConstant.MARRYORDER_WINDOW  = 30302 -- 婚宴窗口
ModuleConstant.MARRY_DUNGEON_INFO   = 30303 -- 结婚副本数据事件 
ModuleConstant.MARRYCOUPLE_WINDOW = 30304 -- 结义收益窗口
ModuleConstant.MARRYSTAGE_WINDOW  = 30305 -- 结义活动窗口
ModuleConstant.MARRYMSG_WINDOW    = 30306 -- 结义信息窗口
ModuleConstant.MARRYPOINT_PROFRESS = 30307 -- 表白浮动提示
ModuleConstant.MARRYPOINT_WEDDING = 30308 -- 婚礼预约浮动提示
ModuleConstant.MARRYPOINT_PROFRESS_SUCCESS = 30309 -- 双方表白成功浮动提示
ModuleConstant.BREAK_NOTWEDDING        = 30310  -- 分手
ModuleConstant.BREAK      = 30311 -- 决裂
ModuleConstant.MARRYDIALOG_WINDOW = 30312 -- NPC对话框
ModuleConstant.WEDDING_ACTIVITY = 30313 -- 婚宴中
ModuleConstant.MARRYDUNGEON_WINDOW = 30314   -- 夫妇婚礼提示
ModuleConstant.WEDDINGLIST_WINDOW = 30315 -- 婚宴列表
ModuleConstant.WEDDINGFASHION_WINDOW = 30316 -- 购买结婚时装界面
ModuleConstant.UPDATE_FASHION_WINDOW = 30317 -- 刷新结婚时装界面
ModuleConstant.OPEN_MARRY_WINDOW = 30318 -- 跳转结婚时装界面

--end

--家族战 30321-30340
ModuleConstant.FAMILY_WAR_WINDOW = 30321--打开家族战界面
ModuleConstant.FAMILY_WAR_MENU_WINDOW = 30322--打开家族战选择界面
ModuleConstant.FAMILY_BATTLE_WINDOW = 30323--打开家族战战斗界面
ModuleConstant.FAMILY_BATTLE_DIRECT = 30324--家族战指挥
ModuleConstant.FAMILY_BATTLE_UPDATE_MORALE = 30325--更新士气值
ModuleConstant.FAMILY_ELITE_RESULT_WINDOW = 30326--显示精英战结算界面
ModuleConstant.FAMILY_WAR_SELECTED_RANK_ID = 30327--更新当前选择的排名id
ModuleConstant.UPDATE_FAMILY_WAR_SCHEDULE = 30328--更新赛程界面
ModuleConstant.FAMILY_WRA_MEMBER_VIEW = 30329--打开报名界面
ModuleConstant.UPDATE_FAMILY_WAR_MEMBER_NUM = 30330--更新参战人数
ModuleConstant.UPDATE_FAMILY_WAR_PERSONAL_RANK = 30331--更新个人积分排名
ModuleConstant.UPDATE_BATTLE_PERSONAL_INTEGRAL = 30332--更新战斗内个人积分的显示
ModuleConstant.UPDATE_BATTLE_KILL_COUNT = 30333--更新个人连杀数
ModuleConstant.FAMILY_WAR_MATCH = 30334--家族战匹配相关
ModuleConstant.FAMILY_WAR_MATCH_SUCCEED_WINDOW = 30335--显示匹配成功界面
ModuleConstant.UPDATE_PERSONAL_INTEGRAL_AWARD = 30336--更新个人积分奖励
ModuleConstant.UPDATE_FAMILY_WAR_MAIN_ICON_STATE = 30337--更新家族战按钮状态
ModuleConstant.FAMILY_WAR_REVIVE = 30338--复活
ModuleConstant.UPDATE_FAMILY_WAR_MENU_DATA = 30339--更新选择界面数据
ModuleConstant.FAMILY_BATTLE_RESULT_WINDOW = 30340--家族战结算界面
-- ModuleConstant.UPDATE_FAMILY_WAR_REMIAN_TIME = 30340--更新匹配战场剩余准备时间
--end

--福利系统 30341-30360
ModuleConstant.BENEFIT_WINDOW = 30341	--打开福利界面
ModuleConstant.BENEFIT_REFRESH = 30342 	--刷新福利活动
ModuleConstant.BENEFIT_ONLINE_WINDOW = 30345  --打开在线奖励窗口
ModuleConstant.BENEFIT_ONLINE_REFRESH = 30346 	--刷新在线奖励
ModuleConstant.BENEFIT_ONLINE_TIME_REFRESH = 30347 	--刷新在线奖励时间
ModuleConstant.BENEFIT_UPDATE_BUTTON_STATE = 30348	--刷新主界面活动按钮状态
ModuleConstant.BENEFIT_SPECIAL_WINDOW = 30349 --打开特殊活动界面

--end


--vip系统 30361-30370
ModuleConstant.VIP_WINDOW = 30361 --打开vip界面
ModuleConstant.VIP_INFO = 30362 --
ModuleConstant.VIP_CHARGE_CHANGED = 30363 --充值进度变化
ModuleConstant.CHARGE_PRODUCT_INFO =30364 ---充值产品数据

--小地图 30371-30390
ModuleConstant.LITTLE_MAP_WINDOW = 30371--打开小地图界面
ModuleConstant.PLAYER_MOVE = 30372--玩家移动
ModuleConstant.INIT_TOWER_SIGN = 30373--初始化塔标示
ModuleConstant.UPDATE_TOWER_SIGN = 30374--更新塔标示（主要更新血量）
ModuleConstant.REMOVE_TOWER_SIGN = 30375--移除塔标示
--end

--神兵系统30391 - 30410;
ModuleConstant.DEITYWEAPON_WINDOW = 30391;
ModuleConstant.DEITYWEAPON_WINDOWNAME = "DeityweaponWindow";
ModuleConstant.DEITYWEAPON_UPDATE = 30392;
ModuleConstant.DEITYWEAPON_SELECT_CHANGE = 30393;
ModuleConstant.DEITYWEAPON_RECEIVE_LEVELVO = 30394;
ModuleConstant.ROLE_DEITYWEAPON_SYNCSTATE    = 30395 ;

--奖励找回30411-30430
ModuleConstant.BENEFITRETRIEVE_WINDOW = 30411;
ModuleConstant.BENEFITRETRIEVE_WINDOWNAME = "BenefitRetrieveWindow";
ModuleConstant.BENEFITRETRIEVE_UPDATEDATA = 30412;
ModuleConstant.BENEFITRETRIEVE_UPDATESINGLEDATA = 30413;


--充值特惠系统 30431 - 30440
ModuleConstant.CHARGE_PRIVILEGE_WINDOW = 30431
ModuleConstant.CHARGE_MCARD_WINDOW = 'ChargeMCardWindow'


--比武大赛 30441 - 30455
ModuleConstant.DOUBLEPVP_MENU_WINDOW = 30441
ModuleConstant.DOUBLEPVP_MENU_REFRESH = 30442
ModuleConstant.DOUBLEPVP_WINDOW = 30443
ModuleConstant.DOUBLEPVP_REFRESH = 30444
ModuleConstant.DOUBLEPVP_FIGHT_WINDOW = 30445
ModuleConstant.DOUBLEPVP_DAMAGE_REFRESH = 30446
ModuleConstant.DOUBLEPVP_RESULT_WINDOW = 30447
ModuleConstant.DOUBLEPVP_TEAMINFO_REFRESH = 30448
ModuleConstant.DOUBLEPVP_SIGNUP_CONFIRM = 30449
ModuleConstant.DOUBLEPVP_SIGNUP_WAIT = 30450
--end

--鉴宝 30456-30460
ModuleConstant.AUTHENTIC_WINDOW = 30456
ModuleConstant.AUTHENTIC_REFRESH = 30457
--end

--购买金币、体力 30461 - 30465
ModuleConstant.BUY_MONEY_SUCCESS = 30461
ModuleConstant.BUY_VIGOR_SUCCESS = 30462

--新服活动-7日目标活动30470-30476
ModuleConstant.BENEFITSEVENDAY_WINDOW = 30470;
ModuleConstant.BENEFITSEVENDAY_WINDOWNAME = "BenefitSevenDayGiftWindow";

--新服活动-新服升级30480-30486
ModuleConstant.BENEFITLEVELUP_WINDOW = 30480;
ModuleConstant.BENEFITLEVELUP_WINDOWNAME = "BenefitLevelUpWindow";

--新服活动-7日登录30490-30496
ModuleConstant.BENEFITSEVENLOGIN_WINDOW = 30490;
ModuleConstant.BENEFITSEVENLOGIN_ICON = 30491;
ModuleConstant.BENEFITSEVENLOGIN_WINDOWNAME = "BenefitSevenLoginWindow";

--门客 30500-30510
ModuleConstant.GUEST_WINDOW = 30500; -- 门客窗口
ModuleConstant.GUEST_LIST   = 30501 -- 门客列表
ModuleConstant.GUEST_INFO   = 30502 -- 门客交换信息
ModuleConstant.GUEST_ASK    = 30503 -- 门客求助信息
ModuleConstant.GUEST_GIVE   = 30504 -- 门客给予信息
--end

--新服活动-新服撒钱 30511-30516
ModuleConstant.BENEFITNEWSERVERMONEY_WINDOW = 30511;
ModuleConstant.BENEFITNEWSERVERMONEY_WINDOWNAME = "BenefitNewservermoneyWindow";
ModuleConstant.BENEFITNEWSERVERMONEY_UPDATEDATA = 30512;
ModuleConstant.BENEFITNEWSERVERMONEY_UPDATEMYRECORD = 30513;
ModuleConstant.BENEFITNEWSERVERMONEY_UPDATEREWARDRESULT = 30514;
--end

-- 用户协议 30520
ModuleConstant.USER_AGREEMENT_WINDOW = 30520
ModuleConstant.LOGIN_CHANGE = 30521
--end

--游戏公告 30531-30535
ModuleConstant.GAMEBOARD_WINDOW = 30531
ModuleConstant.GAMEBOARD_REFRESH = 30532
--end--end

--新服活动-战力冲冲冲 30536-30542
ModuleConstant.BENEFITNEWSERVERFIGHTSCORE_WINDOW = 30536;
ModuleConstant.BENEFITNEWSERVERFIGHTSCORE_WINDOWNAME = "BenefitNewserverfightscoreWindow";
ModuleConstant.BENEFITNEWSERVERFIGHTSCORE_UPDATEDATA = 30537;
ModuleConstant.BENEFITNEWSERVERFIGHTSCORE_UPDATERANKDATA = 30538;
ModuleConstant.BENEFITNEWSERVERFIGHTSCORE_UPDATEREWARDSTATE = 30539;

--诗集系统 30543-30555
ModuleConstant.POEMS_WINDOW = 30543--打开诗集界面
ModuleConstant.UPDATE_POEMS_WINDOW = 30544--更新诗集界面
ModuleConstant.UPDATE_CUR_POEM = 30545--更新当前诗集数据
ModuleConstant.POEMS_BOSS_WINDOW = 30546--boss关卡界面
ModuleConstant.WRITE_POEMS_WINDOW = 30547--写诗界面
ModuleConstant.POEM_STAGE_RESULT_WINDOW = 30548;
ModuleConstant.TEAM_POEM_WINDOW = 30549
ModuleConstant.TEAM_POEM_MEMBER = 30550
ModuleConstant.POEM_STAGE_RESULT_WINDOWNAME = "PoemStageResultWindow";
--end

ModuleConstant.CHARAC_VIEW_CREATE_COMPLETE = 30556--角色模型创建完成



--新洞府 30560-30600
ModuleConstant.GAMECAVE_MAIN_WINDOW = 30560;
ModuleConstant.GAMECAVE_MAIN_WINDOW_NAME = "GamecaveMainWindow";
ModuleConstant.GAMECAVE_RECORD_WINDOW = 30561;
ModuleConstant.GAMECAVE_RECORD_WINDOW_NAME = "GamecaveRecordWindow";
ModuleConstant.GAMECAVE_RECOMMEND_WINDOW = 30562;
ModuleConstant.GAMECAVE_RECOMMEND_WINDOW_NAME = "GamecaveRecommendWindow";
ModuleConstant.GAMECAVE_RESULT_WINDOW = 30563;
ModuleConstant.GAMECAVE_RESULT_WINDOW_NAME = "GamecaveResultWindow";
ModuleConstant.GAMECAVE_EXCHANGE_WINDOW = 30564;
ModuleConstant.GAMECAVE_EXCHANGE_WINDOW_NAME = "GamecaveExchangeWindow";
ModuleConstant.GAMECAVE_TRACEVIEW_OPENHIDE = 30565; --洞府的任务追踪;
ModuleConstant.GAMECAVE_DIALOG_WINDOW = 30566;
ModuleConstant.GAMECAVE_INFO_UPDATE = 30567;
ModuleConstant.GAMECAVE_SINGLEGAME_UPDATE = 30568;
ModuleConstant.GAMECAVE_PRODUCTVO_UPDATE = 30569;
ModuleConstant.GAMECAVE_CAN_SELECTED_GAMELIST = 30570;
ModuleConstant.GAMECAVE_CONTAINER_WINDOW = 30571;
ModuleConstant.GAMECAVE_CONTAINER_WINDOW_NAME = "GameCaveContainerWindow";
ModuleConstant.GAMECAVE_CONTAINER_WINDOW_SHOWORHIDE = 30572;
ModuleConstant.ACTIVITY_TINYGAME_UPDATEREMAINTIME = 30573;
ModuleConstant.GAMECAVE_UPDATEALLCARDID = 30574;
ModuleConstant.GAMECAVE_UPDATECANSELECTED_CARDS = 30575;
ModuleConstant.GAMECAVE_UPDATECANALLRECORDS = 30576;
ModuleConstant.GAMECAVE_SAY_TO_END = 30578;
ModuleConstant.GAMECAVE_GAEM_PAUSECONTINUE = 30579;
ModuleConstant.GAMECAVE_SAY_TO_EXIT = 30580;

--获得物品提示窗口 30601
ModuleConstant.SYS_GET_WINDOW = 30601
ModuleConstant.SYS_GET_FINISH = 30602
--end

--好友悬浮提示 30603-30605
ModuleConstant.TIPS_FRIEND = 30603
--end


--组队精英副本 30606 - 30610
ModuleConstant.DUNGEON_ELITE_WINDOW = 30606
ModuleConstant.DUNGEON_ELITE_PO = 30607
ModuleConstant.DUNGEON_ELITE_TIPS   = 30608

--家族探宝 30611 - 30670
ModuleConstant.FAMILY_ADVENTURE_WINDOW = 30611
ModuleConstant.FAMILY_ADVENTURE_UPDATE = 30612
--end


-- Cg相关 30671-30700
ModuleConstant.CG_INFO = 30671
ModuleConstant.CG_REQUEST_PLAY_CGGROUPID = 30672;
ModuleConstant.CG_REQUEST_PLAY_CGID = 30673;

--购买金币,购买体力相关 30701-30702
ModuleConstant.BUY_MONEY_WINDOW = 30701
ModuleConstant.BUY_VIGOR_WINDOW = 30702

--荣誉竞技场 30705 - 30710
ModuleConstant.OFFLINE_MATCH_WINDOW = 30705
ModuleConstant.OFFLINE_MATCH_UPDATE = 30706
ModuleConstant.OFFLINE_MATCH_RANKUP_WINDOW = 30707
ModuleConstant.OFFLINE_MATCH_RANK_AWARD_UPDATE = 30708
--end

--集字活动活动 30711 - 30720
ModuleConstant.BENEFITCOLLECT_WINDOW =  30711
ModuleConstant.BENEFITCOLLECT_WINDOWNAME = "BenefitCollectWindow";

-- 领主召唤
ModuleConstant.CALLBOSSPAGE_OPEN =  30800 --打开callbosspage
ModuleConstant.CALLBOSSPAGE_CLOSE =  30801 --关闭callbosspage

--充值特惠 30805 - 30810
ModuleConstant.CHARGE_PREFERENCE_WINDOW = 30805
ModuleConstant.CHARGE_PREFERENCE_UPDATE = 30806
--end

--日累计充值 30811 - 30815
ModuleConstant.BENEFIT_DAILYTOTALCHARGE_WINDOW = 30811
ModuleConstant.BENEFIT_DAILYTOTALCHARGE_UPDATE = 30812
--end

--开服基金
ModuleConstant.BENEFITSERVERFUND_WINDOW = 30820
ModuleConstant.BENEFITSERVERFUND_DATAUPDATE = 30821
ModuleConstant.BENEFITSERVERFUND_CHANGEBTNSTATE = 30822

--家族冲榜活动活动
ModuleConstant.BENEFITFAMILY_LVUP_WINDOW =  30831
ModuleConstant.BENEFITFAMILY_LVUP_WINDOWNAME = "BenefitFamilyLevelUpWindow";


--公测返利活动 30841 - 30845
ModuleConstant.BENEFITEXPENSEBACK_WINDOW =  30841
ModuleConstant.BENEFITEXPENSEBACK_WINDOWNAME = "BenefitExpenseBackWindow";
ModuleConstant.BENEFITEXPENSEBACK_REFRESH = 30842 	
ModuleConstant.BENEFITEXPENSEBACK_REFRESHEXPENSE = 30843	
--end

--角色冲级活动 30851 - 30855
ModuleConstant.BENEFIT_FIGHTUP_WINDOW =  30851
ModuleConstant.BENEFIT_FIGHTUP_WINDOWNAME = "BenefitFightUpWindow";

--月卡活动 30856 - 30860
ModuleConstant.BENEFIT_PUBLICITY_WINDOW =  30856
ModuleConstant.BENEFIT_PUBLICITY_WINDOWNAME = "BenefitPublicityWindow";

--每日首充
ModuleConstant.BENEFITDAYRECHARGE_WINDOW = 30860
ModuleConstant.BENEFITDAYRECHARGE_GETINFO = 30861
ModuleConstant.BENEFITDAYRECHARGE_GETGIFTBACK = 30862

--试炼入口 30870-30880
ModuleConstant.CHALLENGE_WINDOW = 30870;

ModuleConstant.CHALLENGE_WINDOWNAME = "ChallengeWindow";
ModuleConstant.CHALLENGE_NOSYSOPENTEXT_REFRESH = 30871;

--弹出提示30881 - 30885
ModuleConstant.POPUP_WINDOW    = 30881 --弹脸提示界面
ModuleConstant.POPUP_START     = 30882 --弹脸事件
ModuleConstant.POPUP_FINISH    = 30883 --弹脸结束
ModuleConstant.POPUP_CHECK     = 30884 --弹脸检测

--首冲活动 30886-30890
ModuleConstant.FIRST_CHARGE_WINDOW = 30886

--领取体力 30891-30895
ModuleConstant.VIGORGIFT_WINDOW =30891
ModuleConstant.VIGORGIFT_UPDATE =30892

--家族任务
ModuleConstant.FAMILYTASK_WINDOW =30900
ModuleConstant.FAMILYTASK_UPDATE =30901
ModuleConstant.FAMILYTASK_SHOWTIPS =30902
ModuleConstant.FAMILYTASKTIP_WINDOW =30903

--家族押镖 30910-30925
ModuleConstant.FAMILY_TRANSPORT_WINDOW = 30910
ModuleConstant.FAMILY_TRANSPORT_UPDATE = 30911
ModuleConstant.FAMILY_TRANSPORT_STATE = 30912 	--家族押镖状态更新
ModuleConstant.FAMILY_INTERCEPT_LIST = 30913 	--拦截列表更新
ModuleConstant.FAMILY_TRANSPORT_SCENE_WINDOW = 30914 	--押镖场景窗口
ModuleConstant.FAMILY_TRANSPORT_TIPS_WINDOW = 30915 	--提示运镖窗口
ModuleConstant.FAMILY_TRANSPORT_CAR_UPDATE = 30916 	--更新镖车信息
ModuleConstant.FAMILY_TRANSPORT_BARRIER_STATE = 30917  --更新障碍物信息
ModuleConstant.FAMILY_TRANSPORT_CAR_STATE_FLUSH = 30918 	--更新镖车状态
ModuleConstant.FAMILY_TRANSPORT_DELETE_CAR = 30919		-- 移除镖车
ModuleConstant.FAMILY_TRANSPORT_AUTO_STATE = 30920		-- 自动模式切换
ModuleConstant.FAMILY_TRANSPORT_PLAYER_STATE = 30921		-- 周围玩家状态
ModuleConstant.FAMILY_TRANSPORT_CLEAR_UP = 30922		-- 清场提示
ModuleConstant.FAMILY_TRANSPORT_TIME_TIPS = 30923		-- 时间提示
--end

--家族战（新增） 30926-30939
ModuleConstant.FAMILY_WAR_UPDATE_KILLNUM = 30926--更新家族战杀人数
ModuleConstant.FAMILY_WAR_UPDATE_FAMILY_INTEGRAL = 30927--更新家族积分
ModuleConstant.FAMILY_WAR_QUALIFYING_INTEGRAL_RANK = 30928--更新家族战海选积分排名


--消费返利
ModuleConstant.BENEFIT_DAYEXPENSE_WINDOW = 30940
ModuleConstant.BENEFIT_DAYEXPENSE_UPDATE = 30941

--充值排行
ModuleConstant.BENEFIT_CHARGE_WINDOW = 30942
ModuleConstant.BENEFIT_CHARGE_UPDATE = 30943
ModuleConstant.BENEFIT_CHARGE_UPDATERANK = 30944

--元宝充值
ModuleConstant.RAFFLEREWARD_WINDOW = 30945
ModuleConstant.RAFFLEREWARD_UPDATE = 30946
ModuleConstant.RAFFLEREWARD_SHOWAWARD = 30947
ModuleConstant.RAFFLEREWARD_SHOWFINAL = 30948
ModuleConstant.RAFFLEREWARD_SHOWAWARD_TEN = 30949
--最佳cp系统 30950 - 30959
ModuleConstant.BENEFIT_BESTCP_WINDOW = 30950
ModuleConstant.BENEFIT_BESTCP_WINDOWNAME = 'BenefitBestCpWindow'
ModuleConstant.BENEFIT_BESTCP_DATA    = 30851
ModuleConstant.BENEFIT_BESTCP_AWARD_RECORD = 30852
ModuleConstant.BENEFIT_BESTCP_CONFIG  = 30853
ModuleConstant.BENEFIT_BESTCP_RANK    = 30854

--end

--礼尚往来活动30960-30969
ModuleConstant.BENEFIT_GIFT_EXCHANGE_WINDOW = 30960
ModuleConstant.BENEFIT_GIFT_EXCHANGE_UPDATE_WINDOW = 30961
ModuleConstant.BENEFIT_GIFT_EXCHANGE_WINDOWName = "BenefitGiftExchangeWindow"
ModuleConstant.DIALOG_SPECIAL_BOX_WINDOW = 30962	--特殊对话框界面

--腾讯视屏活动
ModuleConstant.BENEFIT_TENCENT_WINDOW = 30970	--特殊对话框界面

--赛龙舟活动30971-30979
ModuleConstant.BENEFIT_DRAGONBOAT_WINDOW = 30971
ModuleConstant.BENEFIT_DRAGONBOAT_DATA = 30972
ModuleConstant.BENEFIT_DRAGONBOAT_RANKDATA = 30973
ModuleConstant.BENEFIT_DRAGONBOAT_AWARDDATA = 30976
ModuleConstant.BENEFIT_WUDU_WINDOW = 30974
ModuleConstant.BENEFIT_WUDU_DATA = 30975

-- 日常5v5  30980 - 30995
ModuleConstant.DAILY5V5_MATCH_WINDOW = 30980		-- 日常5v5主界面
ModuleConstant.DAILY5V5_DATA_CHANGED = 30981		-- 日常5v5数据更新
ModuleConstant.DAILY5V5_MATCH_STATE_CHANGED = 30982	-- 日常5v5匹配状态修改
ModuleConstant.DAILY5V5_MATCH_SUCC_WINDOW = 30983
ModuleConstant.DAILY5V5_RESULT_WINDOW = 30984
ModuleConstant.DAILY5V5_TOWERINFO_CHANGED = 30985
ModuleConstant.DAILY5V5_UPDATE_MORALES = 30986
ModuleConstant.DAILY5V5_LITTLEMAP_WINDOW = 30987
ModuleConstant.DAILY5V5_BATTLE_WINDOW = 30988
ModuleConstant.DAILY5V5_PERSONAL_INTEGRAL = 30989
ModuleConstant.DAILY5V5_BATTLE_KILL_COUNT = 30990
ModuleConstant.DAILY5V5_REVIVE = 30991
ModuleConstant.DAILY5V5_CAMP_INTEGRAL = 30992	-- 更新双方阵营的积分
ModuleConstant.DAILY5V5_UPDATE_KILLNUM = 30993	-- 更新击杀信息
ModuleConstant.DAILY5V5_VIPEXTRAEFF_USE_SUCC = 30994 -- vip额外效果使用成功
ModuleConstant.DAILY5V5_MATCH_STATE_CHANGED = 30995	 -- 匹配状态改变


ModuleConstant.UPDTAE_EMAIL_TIP_ICON = 30996

--典籍功能31000-31009
ModuleConstant.BOOK_MAIN_WINDOW = 31000
ModuleConstant.BOOK_MAIN_WINDOWName = "BookWindow"
ModuleConstant.BOOK_UPDATE_DETAIL = 31001--更新书籍详细信息
ModuleConstant.BOOK_UPDATE_MAIN_WND = 31002--更新典籍主界面
ModuleConstant.BOOK_UPDATE_READING_PLAYER_LIST = 31003 --更新正读书的好友列表
ModuleConstant.BOOK_UPDATE_PLAYER_READBOOK_LIST = 31004 --更新玩家正在读书的列表
ModuleConstant.BOOK_UPDATE_SHOW_BOOK_LIST = 31005 --刷新可显示书籍列表
ModuleConstant.BOOK_UPDATE_QUYUAN_READ_STATE = 31006 --刷新屈原读书状态
ModuleConstant.BOOK_UPDATE_HELP_TIMES = 31007 --刷新次数
ModuleConstant.BOOK_HELP_SUCCESS_TALK = 31008 --帮助成功后调用说话
ModuleConstant.BOOK_UPDATE_SHELFVIEW_STATE = 31009 --刷新书架快速完成状态

--赛事 30151-30160
--ModuleConstant.COMPETITION_WINDOW = 30151--打开赛事窗口

--pvp天梯竞技排名 31011 - 31025
ModuleConstant.SPORTS_MAIN_WINDOW = 30151 --延用旧的竞技窗口事件
ModuleConstant.SPORTS_UPDATE = 31012
ModuleConstant.SPORTS_RANK_VIEW = 31013
ModuleConstant.SPORTS_RANK_UPDATE = 31014
ModuleConstant.SPORTS_REWARD_VIEW = 31015
ModuleConstant.SPORTS_REWARD_UPDATE = 31016
ModuleConstant.SPORTS_CHANGE = 31017
ModuleConstant.SPORTS_CHANGE_SCORE_WINDOW = 31018
ModuleConstant.SPORTS_CHANGE_STAGE_WINDOW = 31019
ModuleConstant.SPORTS_CHANGE_REWARD_WINDOW = 31020
ModuleConstant.SPORTS_CHANGE_RANK_WINDOW = 31021
ModuleConstant.SPORTS_DAILY_REWARD = 31022

--优惠豪礼 31026 - 31029
ModuleConstant.DISCOUNTGIFT_WINDOW = 31026
ModuleConstant.DISCOUNTGIFT_DATA_UPDATE = 31027

---充值送礼 31030-31035
ModuleConstant.BENEFIT_CHARGEGIFT_WINDOW = 31030 
ModuleConstant.BENEFIT_CHARGEGIFT_UPDATE_WINDOW= 31031
ModuleConstant.BENEFIT_CHARGEGIFT_WINDOWNAME = "BenefitChargeGiftWindow"
--end

--角色转职31040-31050
ModuleConstant.CHANGE_JOB_WINDOW = 31040

--射箭游戏 31051-31060
ModuleConstant.BENEFIT_ARCHER_WINDOW = 31051
ModuleConstant.ARCHER_GAME_WINDOW = 31052

-- 节日活动中的通用活动模板界面 31061 - 31070
ModuleConstant.BENEFIT_COMMONACT_WINDOW = 31061 		--用在节日活动里面
ModuleConstant.BENEFIT_COMMON_WINDOW = 31062 			--用在福利活动里面

--实名认证31071-31079
ModuleConstant.CERTIFICATION_WINDOW = 31071
ModuleConstant.CERTIFICATION_WINDOWName = "CertificationWindow"
ModuleConstant.CERTIFICATION_WINDOW_HIDE = 31072
ModuleConstant.UPDATE_MAIN_WIN_CERTICATION_BTN = 31703 --更新主界面实名认证按钮是否隐藏

--邀请与分享31080-31089
ModuleConstant.BENEFIT_INVITE_WINDOW = 31080
ModuleConstant.SHARE_ACHIEVE_WINDOW  = 31081
ModuleConstant.SHARE_UPDATE = 31082
ModuleConstant.INVITE_DATA = 31083
ModuleConstant.ACCEPT_DATA = 31084
ModuleConstant.INVITE_SERVERID = 31085
ModuleConstant.SHARED_CALLBACK = 31086
ModuleConstant.SHARE_AWARD_WINDOW = 31087

--改名 31090-31099
ModuleConstant.CHANGE_NAME_WINDOW = 31090
ModuleConstant.UPDATE_RENAME = 31091
ModuleConstant.UPDATE_RENAME_Main_Btn = 31092

-- 符文预热活动 31100 - 31110
ModuleConstant.BENEFIT_TOKENACTIVITY_WINDOW = 31100

-- 符文副本 31111 - 31130
ModuleConstant.TOKEN_DUNGEON_ENTRANCE_WINDOW = 31111
ModuleConstant.TOKEN_DUNGEON_MAIN_WINDOW = 31112
ModuleConstant.TOKEN_DUNGEON_OFFLINE_AWARD_WINDOW = 31113
ModuleConstant.TOKEN_DUNGEON_SELECT_DUNGEON = 31114
ModuleConstant.TOKEN_DUNGEON_DATA_CHANGED = 31115
ModuleConstant.TOKEN_DUNGEON_ANGER_CHANGED = 31116
ModuleConstant.TOKEN_DUNGEON_UPDATE_FRIENDS = 31117
ModuleConstant.TOKEN_DUNGEON_RESULT_WINDOW = 31118
ModuleConstant.TOKEN_DUNGEON_UNLOCK_WINDOW = 31119
ModuleConstant.TOKEN_DUNGEON_UPDATE_DAMAGE = 31120

--活跃神兵器活动31130-31135
ModuleConstant.BENEFIT_ACTI_WEAPON = 31131
ModuleConstant.BENEFIT_ACTI_WEAPONWNAME = "BenefitActiveWeaponWindow"
ModuleConstant.BENEFIT_ACIT_WEAPON_UPDATE = 31132

--VIP弹脸 31136-31140
ModuleConstant.VIP_CUSTOMER_WINDOW = 31136

--微信推广31141-31150
ModuleConstant.BENEFIT_WECHAT_WINDOW = 31141
ModuleConstant.POPUP_AWARD_WINDOW = 31142

--周优惠礼包31151-31155
ModuleConstant.BENEFIT_TYPE_WEKKY_GIFT = 31151
ModuleConstant.BENEFIT_TYPE_WEKKY_GIFTNAME = "BenefitWeeklyGiftWindow"
ModuleConstant.BENEFIT_TYPE_WEKKY_GIFT_UPDATE = 31152

--阵营31200 - 31250
ModuleConstant.CAMP_RESULT_WINDOW = 31200 -- 阵营副本结算界面
ModuleConstant.CAMP_JOIN_WINDOW   = 31201 -- 加入阵营界面
ModuleConstant.CAMP_INFO_WINDOW   = 31202 -- 阵营主界面
ModuleConstant.CAMP_BATTLE_WINDOW = 31203 -- 阵营战斗界面
ModuleConstant.CAMP_POSITION_LEVELUP_WINDOW = 31204 -- 官职升级窗口
ModuleConstant.CAMP_POSITION_GET_WINDOW = 31205 -- 获得官职窗口
ModuleConstant.CAMP_WORLD_MAP_WINDOW = 31206 -- 世界地图窗口
ModuleConstant.CAMP_POSITION_LIST_WINDOW = 31207 -- 职位列表窗口
ModuleConstant.CAMP_TEAM_DUNGEON_WINDOW = 31208 -- 阵营组队副本窗口
ModuleConstant.CAMP_FIGHT_INTEGRAL_UPDATE = 31209 -- 阵营齐楚战积分更新
ModuleConstant.CAMP_FIGHT_PERONAL_INTEGRAL_UPDATE = 31210 -- 阵营齐楚战个人积分更新

ModuleConstant.CAMP_FIGHT_SELECT_CITY = 31211	-- 选择挑战的城池
ModuleConstant.CAMP_CIYT_LIST_WINDOW = 31212 -- 城市列表
ModuleConstant.CAMP_POSITION_RANK_WINDOW = 31213 	-- 阵营官职一览表界面
ModuleConstant.CAMP_RENOWN_LIST_WINDOW = 31214 -- 声望列表
ModuleConstant.CAMP_CANCEL_MATCH = 31215

ModuleConstant.CAMP_DAILY_FIGHT_WINDOW = 31216 -- 阵营日常战斗界面
ModuleConstant.CAMP_DAILY_LEVEL_UP = 31217 -- 阵营玩家升级
ModuleConstant.CAMP_DAILY_INTEGRAL_RANK_UPDATE = 31218-- 阵营积分排行榜更新

ModuleConstant.CAMP_ENTER_WINDOW = 31219--阵营进入界面
ModuleConstant.CAMP_ENTER_UPDATE = 31220--数据更新

ModuleConstant.CAMP_FIGHT_FAIL_WINDOW = 31221 -- 阵营日常战斗失败
ModuleConstant.CAMP_FIGHT_RESET_PLAYER_DATA = 31222--进入阵营日常及重生时需要reset数据

ModuleConstant.CAMP_RECV_MATCH_BEGIN = 31223 --接受开始匹配通知

--周活动31251-31260
ModuleConstant.BENEFIT_COUNT_DOWN_WINDOW = 31251
ModuleConstant.BENEFIT_COUNT_DOWN_SPECIAL_WINDOW = 31252
--end

--结婚组队相关 31261-31270
ModuleConstant.MARRY_TEAM_WINDOW = 31261
ModuleConstant.MARRY_TEAM_UPDATE_PARNER_INFO = 31262--更新配偶的组队数据
ModuleConstant.MARRY_TEAM_DUNGEON_TIMES = 31263--更新结婚组队剩余进入次数
--end

--战力提升 31271-31278
ModuleConstant.FIGHT_GROWUP_WINDOW = 31271
ModuleConstant.FIGHT_GROWUP_WINDOWNAME = "FightGrowUpWindow"
ModuleConstant.FIGHT_GROWUP_UPDATE = 31272
ModuleConstant.FIGHT_GROWUP_UPDATE_RIGHT = 31273
ModuleConstant.FIGHT_GROWUP_UPDATe_BALL = 31274
ModuleConstant.FIGHT_GROWUP_SHOW_LUCKY = 31275

--新公共弹窗奖励
ModuleConstant.NEW_COMTIPS_AWARD_SHOW_WIN = 31279
ModuleConstant.NEW_COMTIPS_AWARD_SHOWNAME = "FightAwardWindow"

--周累计充值反利31280-31285
ModuleConstant.BENEFIT_DAYREWARD_WINDOW = 31280
ModuleConstant.BENEFIT_DAYREWARD_UPDATE = 31281

--幸运转盘31286-31295
ModuleConstant.LUCK_TURNTABLE_WINDOW = 31286
ModuleConstant.UPDATE_LUCK_TURNTABLE_WINDOW = 31287
ModuleConstant.UPDATE_LUCK_TURNTABLE_ICON = 31288

--老玩家回归31300-31310
ModuleConstant.PLAYER_BACK_WINDOW = 31300
ModuleConstant.PLAYER_BACK_ICON = 31301

--限时秒杀31311-31319
ModuleConstant.PANIC_BUYING_WINDOW = 31311
ModuleConstant.PANIC_BUYING_WINDOWNAME = "PanicBuyingWindow"
ModuleConstant.PANIC_BUYING_WINDOW_UPDATE = 31312
ModuleConstant.UPDATE_MAINWND_PANIC_BUYING_BTN_STATE = 31313
ModuleConstant.PANIC_BUYING_WINDOW_HIDE = 31314

--宝宝系统 31320 - 31339
ModuleConstant.BABY_MAIN_WINDOW = 31320
ModuleConstant.BABY_WINDOW_UPDATE = 31321
ModuleConstant.BABY_PRAY_WINDOW = 31322
ModuleConstant.BABY_BABY_WINDOW = 31323
ModuleConstant.BABY_INFANT_WINDOW = 31324
ModuleConstant.BABY_CHILD_WINDOW = 31325
ModuleConstant.BABY_TASK_WINDOW = 31326
ModuleConstant.BABY_TASK_UPDATE = 31327
ModuleConstant.BABY_SETNAME_WINDOW = 31328
ModuleConstant.BABY_GET_WINDOW = 31329
ModuleConstant.BABY_TIMES_BUY_WINDOW = 31330
ModuleConstant.BABY_SWEEP_RESULT_WINDOW = 31331
ModuleConstant.BABY_TALK = 31332
ModuleConstant.BABY_EXPLAIN_WINDOW = 31333
ModuleConstant.BABY_CLOTH_WINDOW = 31334
ModuleConstant.BABY_CLOTH_UPDATE = 31335
ModuleConstant.BABY_CLOTH_USING = 31336 	--穿上时装


--炼化功能 31340-31349
ModuleConstant.REFINE_WINDOW = 31340
ModuleConstant.REFINE_WINDOWNAME = "BagBackWindow"
ModuleConstant.REFINE_WINDOW_UPDATE = 31341

--每日一抽 31350-31359
ModuleConstant.LUCKDAY_WINDOW = 31350
ModuleConstant.LUCKDAY_STATIC_UPDATE = 31351
ModuleConstant.LUCKDAY_DYNAMIC_UPDATE = 31352
ModuleConstant.LUCKDAY_AWARD_BACK = 31353
ModuleConstant.LUCKDAY_IS_SHOW = 31354

--新服七日签到 31360-31369
ModuleConstant.SIGNIN_TOMORROW_WINDOW = 31360


--弑神活动副本 31370-31389
ModuleConstant.KILLGOD_WINDOW = 31370
ModuleConstant.KILLGOD_WINDOWNAME = "KillGodWnd"
ModuleConstant.KILLGOD_UPDATE_WINDOW = 31371
ModuleConstant.KILLGOD_RANK_WINDOW = 31372
ModuleConstant.KILLGOD_RANK_WINDOWNAME = "KillGodRankWnd"
ModuleConstant.KILLGOD_RANK_UPDATE_WINDOW = 31373
ModuleConstant.KILLGOD_UPDATE_DAMAGE = 31374
ModuleConstant.KILLGOD_UPDATE_REWARD_DAMAGE = 31375
ModuleConstant.KILLGOD_UPDATE_TIMES = 31376
ModuleConstant.KILLGOD_OPEN_HIDE_TASK_BTN_EFFECT = 31377   --当弑神界面打开时，隐藏主界面左边任务按钮特效
ModuleConstant.KILLGOD_HIDE_SHOW_TASK_BTN_EFFECT = 31378   --当弑神界面关闭时，显示主界面左边任务按钮特效

--日累计充值 31390 - 31399
ModuleConstant.BENEFIT_DAILYLIVENESS_WINDOW = 31390
ModuleConstant.BENEFIT_DAILYLIVENESS_UPDATE = 31391
ModuleConstant.BENEFIT_DAILYLIVENESS_CHOOSE_WINDOW= 31392
ModuleConstant.BENEFIT_DAILYLIVENESS_CHOOSE_RWWARD= 31393

--问卷调查、手机号收集活动 31400 - 31410
ModuleConstant.BENEFIT_COLLECT_ACTIVITY_WINDOW = 31400
ModuleConstant.BENEFIT_COLLECT_ACTIVITY_UPDATE = 31401
ModuleConstant.BENEFIT_COLLECT_ACTIVITY_RESPOND = 31402

--月饼小游戏32400 - 32419
ModuleConstant.BENEFIT_MOON_CAKE_WINDOW = 32400
ModuleConstant.MOON_CAKE_GAME_WINDOW = 32401
ModuleConstant.BENEFIT_MOON_CAKE_WINDOW_UPDATE = 32402
ModuleConstant.MOONCAKE_GAME_RESULT_WINDOW = 32403
ModuleConstant.MOONCAKE_GAME_RESULT_WINDOWNAME = "MoonCakeResultWindow"
ModuleConstant.MOONCAKE_GAME_RANK_WINDOW = 32404
ModuleConstant.MOONCAKE_GAME_RANK_WINDOWNAME = "MoonCakeRankWindow"