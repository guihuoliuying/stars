--autor zrc
--Date  2015-03-09
PacketType = {
    --数据包定义
    --Server* -- 客户端请求服务端
    --Client* -- 服务端下发到客户端
    ServerHeartBeat = 0x002f, -- 心跳包
    ClientHeratBeat = 0x0030,

    ServerLogin = 0x000E, --登陆请求包
    ClientLogin = 0x000F, --登陆返回包
    -- ServerCreateRole = 0x0005,     --创建角色包

    ClientRoleData = 0x0010, --角色数据返回

    ServerReconnect = 0x0004,
    ClientReconnect = 0x0005,

    ClientHeartBeatReply = 0x0017, --心跳包检测
    ServerHeartBeatReply = 0x0018, --心跳包检测返回
    ServerRoleAttrCheck = 0x0019, --用户属性检测返回

    -- 登录
    ServerLoginRequest = 0x7c00, --请求登录
    ServerListRequest = 0x7c02, --请求服务器列表
    ClinetLoginRecv = 0x7c50, --登录反馈
    ClientServerList = 0x7c54, --服务器列表返回
    ServerUpdateServerList = 0x7c04, -- 更新服务器列表  上行
    ClientUpdateServerList = 0x7c53, -- 更新服务器列表  下发
    -- 登录end

    --ServerCreatName = 0x010A,     --请求创建名字
    --ServerRandomName = 0x010B,    --请求随机名字
    --ClientRandomName = 0x0109,    --接受随机名字

    ClientCommonTips = 0x0008, --通用提示
    ClientRollTips = 0x0009, --滚动提示

    ServerTitle = 0x0040, --称号上传
    ClientTitle = 0x0041, --称号下发

    --角色信息菜单
    ServerPlayerPopupMenuInfo = 0x0110, --角色菜单数据上传
    ClientPlayerPopupMenuInfo = 0x0111, --角色菜单数据下发

    --道具系统
    ClientTool = 0x0031, --道具系统下发
    ServerTool = 0x0032, --道具系统上发
    ClientAward = 0x0033, --使用宝箱、任务奖励等获得得奖励的通知

    --技能升级系统
    ServerSkillVo = 0x0047, --技能产品数据
    ClientSkillVo = 0x0044, --技能产品数据
    ServerSkillUp = 0x0045, --技能升级
    ClientSkillUpdate = 0x0046, --技能更新
    ServerSkillReplace = 0x0048, --技能替换请求
    ClientSkillReplace = 0x0049, --响应技能替换

    --战力系统
    C2G_InitFightAbility = 0x0011,
    G2C_InitFightAbility = 0x0012,
    C2G_GetGiftFightAbility = 0x0013,
    G2C_GetGiftFightAbility = 0x0014,

    --伙伴系统
    ServerPartnerData = 0x0070, --请求伙伴数据 上行
    ClientPartnerData = 0x0071, --接收到伙伴数据 下行
    ServerPartnerChange = 0x0072, --请求伙伴操作
    ClientPartnerChange = 0x0073, --更新伙伴数据
    ServerLineupDataChange = 0x0074, --七星阵操作
    ClientLineupDataChange = 0x0075, --更新七星阵数据
    ServerPartnerFollowData = 0x0076, --登录下发伙伴跟随数据
    ServerPartnerGuardData = 0x0077, --伙伴护卫上行
    ClientPartnerGuardData = 0x0078, --伙伴护卫下行


    --坐骑系统
    ServerRideInfo = 0x0120, --坐骑界面主协议上行
    ClientRideInfo = 0x0121, --坐骑界面主协议下行
    ClientRideProductInfo = 0x0122, --坐骑界面产品（坐骑）数据
    ClientRideAwakeLvProInfo = 0x0123, --坐骑觉醒等级产品数据

    ServerFashionInfo = 0x0150, --时装系统主协议上行
    ClientFashionInfo = 0x0151, --时装系统主协议下行

    --活跃度系统
    ClientDailyUpdate = 0x002a, --日常更新
    ServerGetDailyAward = 0x002b, --请求获取日常奖励
    ServerGetDailyData = 0x002c, --请求活动数据
    ClientDailyAward = 0x002d, --返回领取奖励的信息

    --显示周围玩家的数据下发
    ClientArroundPlayer = 0x002e, --下发周围玩家数据

    -- PVP
    ServerPkPo = 0x0101, --PK主协议,上行
    ClientPkPo = 0x010A, --PK主协议,下行
    ServerPKData = 0x0104, --pk数据 上行
    ClientPKData = 0x0103, --pk数据 下行
    Client_PK_Over = 0x0107, --通知PK结束
    ClientUpdatePlayer = 0x0109, --同步玩家状态
    ClientClearOther = 0x6187, --清除其他玩家
    ServerPK_Ready = 0x61a2, -- 通知服务端准备就绪

    ServerRank = 0x6050, --排行榜上行请求
    ClientRank = 0x6051, --排行榜下行响应

    GMCommand = 0x7D00, -- 作弊命令

    ServerWorldView = 0x0050, -- 关卡章节数据上行
    ClientWorldView = 0x0051, -- 关卡章节数据下行
    ServerDungeon = 0x0052, -- 关卡列表上行
    ClientDungeon = 0x0053, -- 关卡列表下行

    -- 战斗
    ServerEnterFight = 0x7B00, -- 请求
    ClientEnterFight = 0x7B01, -- 进战斗下发数据

    ServerMonsterDead = 0x7B02, -- 通知服务器怪物死亡
    ClientMonsterSpawn = 0x7B03, -- 通知客户端刷怪
    ServerAreaSpawn = 0x7B04, -- 通知服务器区域刷怪
    ClientStageFinish = 0x7B05, -- 通知客户端关卡战斗结果
    ServerRoleDead = 0x7B06, -- 通知服务器主角死亡
    ServerSpawnMonsterConfirm = 0x7B21, -- 通知服务端客户端确认收到刷怪数据
    ClientMonsterDeadConfirm = 0x7B22, -- 通知客户端服务端已经收到怪物死亡信息
    ServerRoleAttr = 0x7B23, -- 通知服务端主角的实时属性进行星级判定
    ClientIntegral = 0x7B24, -- 通知客户端夫妻副本时的积分

    ServerSynOrder = 0x7B16, -- 战斗消息上传
    ClientSynOrder = 0x7B17, -- 战斗消息下发

    ServerTransfer = 0x7B07, -- 安全区传送，上行
    ClientTransfer = 0x7B08, -- 安全区传送，下行

    ClinetCampRelation = 0x7B11, --阵营关系表

    ServerPauseTime = 0x7B0A, --暂停计时
    ServerContinueTime = 0x7B0B, --继续计时
    ClientFightTime = 0x7B0C, --战斗时间
    ServerAutoRecord = 0x7B1E, --自动战斗标记
    ServerExitFight = 0x7B15, --请求退出战斗
    ServerGiveDamage = 0x7B13, --角色输出属性
    ClientRecvValue = 0x7B14, --角色同步属性
    ClientRecvDrop = 0x7B18, --同步掉落
    ServerRevive = 0x7B19, --请求复活
    ClientRevive = 0x7B1A, --复活反馈
    ClientChangeFightVoAttr = 0x7B1B, --更改战斗属性;

    ClientTaskList = 0x0036, -- 任务列表
    ClientTaskProcess = 0x0037, -- 任务进度
    ClientTaskRemove = 0x0038, -- 删除任务
    ServerTaskSubmit = 0x0039, -- 提交任务
    ServerTaskAccept = 0x003A, -- 接受任务

    ServerTalkWithNpc = 0x7B09, -- 与NPC交互

    ServerEmail = 0x6000, -- 邮件信息
    ClientEmail = 0x6001, --

    -- ServerVigor = 0x0015,			-- 体力请求
    -- ClientVigor = 0x0016,			-- 体力响应

    ServerCommonResBuy = 0x0015, -- 购买通用资源
    ClientCommonResBuy = 0x0016, -- 购买通用资源

    ClientChat = 0x0026, -- 聊天下行
    ServerChat = 0x0027, -- 聊天上行
    ClientRefuseChannel = 0x0029, -- 屏蔽聊天频道下行
    ServerRefuseChannel = 0x0028, -- 屏蔽聊天频道上行

    ClientPlayer = 0x6ff1, -- 同步其他玩家，下行

    ServerRecommendation = 0x6008, --推荐好友请求
    ClientRecommendation = 0x6009, --返回推荐好友

    SeverFriend = 0x600A, --好友请求（好友列表，申请列表，申请，同意，拒绝）
    ClientFriend = 0x600B, --好友响应
    SeverBlacker = 0x600C, --黑名单请求
    ClientBlacker = 0x600D, --黑名单响应

    ServerPlayerDetails = 0x6010, --玩家详细信息请求
    ClientPlayerDetails = 0x6011, --玩家详细信息响应

    ServerContacts = 0x600E, --最近联系人请求
    ClientContacts = 0x600F, --最近联系人响应

    ServerInduct = 0x0056, -- 引导上行
    ClientInduct = 0x0057, -- 引导下行

    ServerFamilyList = 0x6020, -- 请求家族列表
    ClientFamilyList = 0x6021, -- 响应家族列表
    ClientMyFamilyInfo = 0x6023, -- 我的家族基本信息下发(登录时下发)
    ServerFamily = 0x6024, -- 家族管理上行
    ClientFamily = 0x6025, -- 家族管理下行
    ClientFamilyContribute = 0x6027, -- 响应家族贡献
    ServerFamilyDonate = 0x6028, -- 请求家族捐献
    ClientFamilyDonate = 0x6029, -- 响应家族捐献
    -- ServerFamilyRedPacket		= 0x6030,	-- 家族红包上行
    -- ClientFamilyRedPacket		= 0x6031,	-- 家族红包下行
    ServerFamilyRedPacket = 0x0210, -- 家族红包上行
    ClientFamilyRedPacket = 0x0211, -- 家族红包下行
    ServerFamilySkill = 0x6032, -- 家族心法上行
    ClientFamilySkill = 0x6033, -- 家族心法下行
    ServerFamilyEvent = 0x602A, -- 家族事迹上行
    ClientFamilyEvent = 0x602B, -- 家族事迹下行
    ServerFamilyActivity = 0x6040, -- 家族活动列表上行
    ClientFamilyActivity = 0x6041, -- 家族活动列表下行
    ServerFamilyStage = 0x6070, -- 家族场景上行
    ClientFamilyStage = 0x6071, -- 家族场景下行
    ServerFamilyBonfire = 0x6072, -- 家族篝火活动上行
    ClientFamilyBonfire = 0x6073, -- 家族篝火活动下行
    ServerFamilyExpedition = 0x6068, -- 家族远征上行
    ClientFamilyExpedition = 0x6069, -- 家族远征下行
    ClientFamilyExpedFinished = 0x606A, -- 家族远征战斗结算


    ServerProduceDungeon = 0x0054, -- 产出副本上行
    ClientProduceDungeon = 0x0055, -- 产出副本下行
    ServerProDungeonReward = 0x7b12, --产出副本领取奖励

    ServerCallBossData = 0x6058, --请求召唤boss数据
    ClientCallBossVo = 0x6059, --下发召唤boss的产品数据
    ClientCallBossPo = 0x605A, --下发召唤boss的用户数据
    ServerExcuteCallBoss = 0x605B, --执行召唤
    ServerCallRecord = 0x605C, --请求召唤记录
    ClientCallRecord = 0x605D, --下发召唤记录
    ServerCallBossRank = 0x605E, --请求伤害排行榜
    ClientCallBossRank = 0x605F, --下发伤害排行数据

    ClientShop = 0x0090, --商店下行
    ServerShop = 0x0091, --商店上行

    ClientTeamDungeon = 0x0140, --组队副本信息
    ServerTeamDungeon = 0x0141, --请求组队副本信息
    ClientTeamInfo = 0x0144,
    ServerTeamInfo = 0x0145, --组队相关的一些操作
    ClientTeamApply = 0x0142,
    ServerTeamApply = 0x0143,
    ClientTeamInvite = 0x0146,
    ServerTeamInvite = 0x0147,
    ClientTeamMatch = 0x0148,
    ServerTeamMatch = 0x0149,

    --心法系统
    ServerMindVo = 0x0138, --心法产品请求数据
    ClientMindVo = 0x0139, --心法产品数据下发
    ServerMindPo = 0x013A, --心法用户数据请求
    ClientMindPo = 0x013B, --心法用户数据下发
    ServerMindUpGrade = 0x013C, --心法升级
    ClientMindUpGrade = 0x013D, --升级下发

    ServerTrump = 0x0130, --法宝上行
    ClientTrump = 0x0131, --法宝下行

    ServerOfflinePVPData = 0x6170, --请求离线pvp数据
    ServerOfflinePVPOption = 0x6171, --离线pvp操作请求
    ClientOfflinePVPData = 0x6172, --下发离线pvp数据

    ServerAchieve = 0x0156, --成就上行
    ClientAchieve = 0x0157, --成就下行

    --外族入侵
    SeverInvadeTeam = 0x6078, --外族入侵组队请求
    ClinetInvadeTeam = 0x6079, --外族入侵组队响应
    ClientInvadeNotice = 0x607A, --活动开始结束通知
    ServerInvadeOperate = 0x607B, --外族入侵操作请求
    ClientInvadeOperate = 0x607C, --外族入侵操作响应

    -- 系统开放
    ServerSysOpen = 0x0161,
    ClientSysOpen = 0x0160,

    --勇者试炼
    ServerBraveInfo = 0x0168,
    ClientBraveInfo = 0x0169,
    ServerJoinBrave = 0x016A,
    ClientBravePassAward = 0x016B,


    -- 巅峰对决
    ServerFightingMasterWindow = 0x61a0, -- 打开巅峰对决界面（主服）
    ServerFightingMaster = 0x61a1, -- 巅峰对决（全服）
    ClentFightingMaster = 0x61a3, -- 巅峰对决界面下行（全服）

    --皇榜悬赏
    ServerRoyalRewardInfo = 0x0171,
    ClientRoyalRewardInfo = 0x0172,
    ServerRoyalRewardUpdate = 0x0173,
    ClientRoyalRewardUpdate = 0x0174,
    ServerRoyalRefresh = 0x0175,
    ClientRoyalRefresh = 0x0176,
    ClientRoyalCountDown = 0x0177,

    -- 红点
    ClientRedPoint = 0x017B, --红点内容下发

    --装备
    ServerNewEquip = 0x0180, --新装备上行
    ClientNewEquip = 0x0181, --新装备下行


    ServerRegisterAccount = 0x0000, -- 注册账号（上行）
    ClientRegisterAccount = 0x0001, -- 注册账号（下行）

    ServerLogout = 0x0002, -- 注销登录（上行）
    ClientLogout = 0x0003, -- 注销登录（下行）

    --签到奖励
    ClientSigninInfo = 0x0164, --签到数据下发
    ClientSigninAward = 0x0165, --下发签到获得的奖励
    ClientSigninVo = 0x0166, --月签到产品数据下发
    ServerSignin = 0x0167, --签到请求


    ServerMarry = 0x185, -- 结婚（上行）
    ClientMarry = 0x184, -- 结婚（下行）
    ClientMarrDungeon = 0x186, -- 结婚副本相关
    ClientMarryBattleInfo = 0x0187, --结婚对方的组队数据

    ServerCargo = 0x019C, -- 运镖（上行）
    ClientCargo = 0x019D, -- 运镖（下行）
    ClientCargoSafe = 0x019E, -- 运镖安全区（队列场景）下行

    --福利系统
    ServerBenefit = 0x0190, --福利上行
    ClientBenefit = 0x0191, --福利下行

    --在线奖励
    ServerBenefitOnline = 0x0198, --上行
    ClientBenefitOnline = 0x0199, --下行
    ServerBenefitOnlineTime = 0x019A, --上行
    ClientBenefitOnlineTime = 0x019B, --下行

    --vip系统
    ServerVipInfo = 0x0188, --vip数据请求
    ClientVipInfo = 0x0189,
    ServerVipOption = 0x018A, --VIP操作
    ClientChargeInfo = 0x018B, --下发充值数据
    ClientChargeSwitch = 0x018C, ---充值开关

    --家族战
    ClientFamilyBattleInfo = 0x61C0, --下行，初始信息（塔位置，塔血量，塔最大血量，塔uid）
    ClientFamilyBattleUpdateInfo = 0x61C1, --下行，塔的更新信息（塔血量，士气）
    ClientFamilyBattleKillCount = 0x61C2, --个人连斩计数
    ClientFamilyBattlePersonalIntegral = 0x61C3, --战斗内个人积分
    SeverBattleDirect = 0x61C6, --指挥
    ClientBattleDirect = 0x61C7,
    ClientFamilyEliteBattleResult = 0x61C8, --精英战结算界面
    ClientFamilyNormalBattleResult = 0x61C9, --匹配战结算界面
    ClientFamilyWarMatchingMember = 0x61CA, --匹配成员信息

    ServerFamilyWarMenu = 0x61B0, --家族战选择界面（上行）
    ClientFamilyWarMenu = 0x61B1, --家族战选择界面（下行）
    ServerFamilyWarRule = 0x61B2, --规则（上行）
    ClientFamilyWarRule = 0x61B3, --规则（下行）
    ServerFamilyWarApply = 0x61B4, --报名相关
    ClientFamilyWarApply = 0x61B5,
    ServerFamilyWarSchedule = 0x61B6, --赛程（赛事状态，家族信息，对阵表，冠亚季殿，个人资格）
    ClientFamilyWarSchedule = 0x61B7,
    ServerFamilyWarScoreRank = 0x61B8, --积分排位（精英战个人积分，匹配战个人积分，海选家族积分（包括资格展示），海选个人积分）
    ClientFamilyWarScoreRank = 0x61B9,
    ServerFamilyWarPersonalScore = 0x61BA, --个人积分达标奖励
    ClientFamilyWarPersonalScore = 0x61BB,
    ServerFamilyWarEnterBattle = 0x61BE, --请求进入家族战
    ClientFamilyWarEnterBattle = 0x61BF, --
    ClientFamilyWarMainIcon = 0x61CB, --主界面图标更新
    ClientFamilyWarRevive = 0x61C5, --复活弹窗
    ServerFamilyWarRevive = 0x61C4, --向服务端请求复活
    ClientFamilyWarDynamic = 0x61CC, --动态阻挡相关
    ClientFamilyWarStageBattleResult = 0x61CD, --匹配关卡结算界面
    ClientFamilyWarEliteStartTips = 0x61CE, --精英战场开启提示
    ClientFamilyWarMatchRemainderTime = 0x61CF, --匹配战场剩下准备时间
    ServerFamilyWarEnterPrepareStage = 0x61D0, --请求进入备战场景
    ClientFamilyWarEnterPrepareStage = 0x61D1, --进入备战场景后的数据
    ClientFamilyWarBattleResult = 0x61D2, --家族战每一轮的结算
    ClientFamilyWarKillNum = 0x61D3, --精英战的杀人数，被杀数，助攻数等更新
    ClientFamilyWarFamilyIntegral = 0x61D4, --战斗内家族积分更新
    ClientFamilyWarPointsRank = 0x61D5, --海选赛家族积分相关
    ServerFamilyWarPointsRank = 0x61D6, --海选赛家族积分相关
    ClientFamilyRank = 0x61D7, --家族战排行榜
    ServerFamilyWarRank = 0x61D8, --请求是否能显示家族战排行榜


    --比武大赛双人PVP
    ServerDoublePVP = 0x61E0, --打开界面数据上行
    ClientDoublePVP = 0x61E1, --打开界面数据下行
    ServerDoublePVPSignUp = 0x61E2, --请求报名 上行
    ClientDoublePVPSignUp = 0x61E3, --下发报名 下行
    ClinetDoublePVPDamage = 0x61E7, --同步伤害下发
    ServerFightEnter = 0x61E6, --进入战斗

    --鉴宝
    ServerAuthentic = 0x01B9, --上行
    ClientAuthentic = 0x01BA, --下发奖励
    ClientAuthenticData = 0x01BB, --打开界面数据下行

    ServerDrama = 0x7B1F, --剧情上行
    ClientDrama = 0x7B20, --剧情下行

    --游戏公告
    ServerGameBoard = 0x01DD, --游戏公告上行
    ClientGameBoard = 0x01DC, --游戏公告下行
    ServerLoginBoard = 0x7C09, --游戏公告登录服上行

    --门客
    ServerGuest = 0x01c0, -- 门客上行
    ClientGuest = 0x01c1, -- 门客下行

    --诗集系统
    ServerPoems = 0x01DE, --诗集系统,请求所有诗集数据
    ClientPoems = 0x01DF,
    ServerPoemsBoss = 0x01E1, --boss关卡数据
    ClientPoemsBoss = 0x01E2,
    ServerPoemsMember = 0x025E, --请求诗歌队伍数据
    ClientPoemsMember = 0x025F, --接受诗歌队伍数据


    ClientBlockAccount = 0x0006, -- 封号提示

    ClientOpenServerDays = 0x0007, --开服天数

    ServerActivateCode = 0x0034, --激活码

    --探宝
    ServerFamilyAdventure = 0x6200, --上行
    ClientFamilyAdventure = 0x6201, --下行

    --集字
    ServerBenefitCollect = 0x6210, --上行
    ClientBeneFitCollect = 0x6211, --下行
    --荣誉竞技场
    ServerOfflineMatch = 0x6205, --上行
    ClientOfflineMatch = 0x6206, --下行

    --家族冲级
    ServerFamilyLevelUp = 0x6214, --上行
    ClientFamilyLevelUp = 0x6215, --下行
    --角色战力冲级
    ServerFightUp = 0x621A, --上行
    ClientFightUp = 0x6219, --下行
    ---公测充值返利
    ServerChargeBack = 0x0214,
    ClientChargeBack = 0x0215,

    --开服基金
    ServerServerFund = 0x021B,
    ClientServerFund = 0x021E,

    --每日首充
    ServerDayRecharge = 0x021F,
    ClientDayRecharge = 0x0220,

    --弹出提示协议相关
    ServerPopup = 0x6220, --弹出提示上行
    ClientPopup = 0x6221, --弹出提示下行

    --充值特惠
    ServerChargePreference = 0x022A, --上行
    ClientChargePreference = 0x022B, --下行

    --日累计充值
    ServerBenefitDailyTotalCharge = 0x6229, --上行
    ClientBenefitDailyTotalCharge = 0x622A, --下行

    --限时领取体力
    ServerVigorGiftState = 0x0222, --上行
    ClientVigorGiftState = 0x0223, --下行
    ServerVigorGiftGet = 0x0224,

    --家族任务
    ServerFamilyTask = 0x6224, --上行
    ClientFamilyTask = 0x6225, --下行

    --组队精英副本
    ServerEliteDungeon = 0x0230,
    ClientEliteDungeon = 0x0231,
    ServerEliteData = 0x0232,
    ClientEliteData = 0x0233,

    --家族押镖
    ServerFamilyTransport = 0x0242, --上行 押镖相关的请求
    ClientFamilyTransportCar = 0x0243, --新增加镖车
    ClientCarFlush = 0x0244, --镖车状态刷新
    ClientBarrierInfo = 0x0245, --障碍物信息刷新
    ClientFamilyInterceptList = 0x0246, --家族镖车拦截列表
    ClientFamilyTransportWinData = 0x0247, --家族镖车打开窗口数据
    ClientFamilyTransportScene = 0x0248, --家族镖车进入场景信息
    ClientFamilyTransportSuccess = 0x0249, --家族镖车运镖成功
    ClientFamilyTransportPlayerState = 0x024a, --家族镖车周围玩家的状态
    ClientFamilyTransportClearUp = 0x024b, --清场景提示
    ClientFamilyTransportTimeTips = 0x024c, --运镖时间提示

    --福利号信息
    ServerWealfareRole = 0x023A, --上行
    ClientWealfareRole = 0x023B, --下行

    --礼尚往来
    ServerBenefitGiftExchange = 0x023E, --上行
    ClientBenefitGiftExchange = 0x023F, --下行


    --充值排行
    ServerChargeRank = 0x622D, --上行
    ClientChargeRank = 0x622E, --下行
    --消费返还
    ServerBenefitDayExpense = 0x622F, --上行
    ClientBenefitDayExpense = 0x6230, --下行
    --周累计充值
    ServerBenefitWeekExpense = 0x0276, --上行
    ClientBenefitWeekExpense = 0x0277, --下行

    --元宝抽奖
    ServerRaffleRewardInfo = 0x0254, --上行
    ClientRaffleRewardInfo = 0x0255, --下行
    ServerRaffleRewardGetAward = 0x0256, --上行
    ClientRaffleRewardGetAward = 0x0257, --下行
    ServerRaffleRewardSelect = 0x0258, --上行
    ClientRaffleRewardSelect = 0x0259, --下行

    ServerBestCPInfo = 0x0250, --上行
    ClientBestCPInfo = 0x0251, --下行

    --龙舟
    ServerDragonInfo = 0x025A, --上行
    ClientDragonInfo = 0x025B, --下行
    --五毒
    ServerWuDUInfo = 0x0218, --上行
    ClientWuDUInfo = 0x0219, --下行


    ServerChargeGift = 0x026B, ---充值送礼包 上行
    ClientChargeGift = 0x026C, ---充值送礼包 下行

    --书籍功能
    ServerBook = 0x0267, --上行
    ClientBook = 0x0266, --下行

    ClientConfigPatcher = 0x000C, -- 配置补丁热更新

    ServerDaily5v5 = 0x6240, -- 日常5v5上行请求
    ClientDaily5v5 = 0x6232, -- 日常5v5下行数据
    ClientDaily5v5PersonalPoint = 0x6233, -- 日常5v5个人积分同步
    ClientDaily5v5BattleStat = 0x6234, -- 日常5v5战斗状态同步
    ClientDaily5v5Revive = 0x6235, -- 日常5v5通知复活
    ClientDaily5v5KillCount = 0x6236, -- 日常5v5通知击杀数
    ClientDaily5v5TeamPoints = 0x6237, -- 日常5v5阵营积分同步
    ClientDaily5v5Morale = 0x6238, -- 日常5v5士气同步
    ServerDaily5v5Revive = 0x6239, -- 日常5v5请求复活
    ClientDaily5v5FightInitInfo = 0x623a, -- 日常5v5战斗信息
    ClientDaily5v5FightUpdateInfo = 0x623b, -- 日常5v5战斗状态更新
    ServerUseVipExtraEff = 0x623c, -- 请求使用vip特殊效果(主动)

    --pvp天梯
    ServerSports = 0x026F, --上行
    ClientSkyrankGradData = 0x0270, --下行 天梯表数据
    ClientSkyrankMyData = 0x0271, --下行 用户数据
    ClientSkyrankRankData = 0x0272, --下行 排名数据
    ClientSkyrankReward = 0x0273, --下行 获得的奖励
    ClientSkyrankAwardData = 0x0274, --下行 奖励产品数据
    ClientSkyrankRewardInfo = 0x0275, --下行 天梯当前奖励时间与每日奖励信息
    --优惠豪礼
    ServerDiscountGift = 0x027C, --请求豪礼
    ClientDiscountGift = 0x027D, --接收豪礼数据



    --转职
    ServerChangeJob = 0x0280, -- 转职（上行）
    ClientChangeJob = 0x0281, -- 转职（下行）

    --射箭活动游戏
    ClientArcherGame = 0x0288, -- （下行）
    ServerArcherGame = 0x0289, -- （上行）

    ServerShare = 0x0284, --分享朋友圈上行
    ClientShare = 0x0285, --分享朋友圈下行

    ServerInvite = 0x028A, --邀请好友上行
    ClientInvite = 0x028B, --邀请方下行
    ClientBeInvite = 0x028C, --受邀方下行
    ClientServerId = 0x028D, --邀请方的服务器id


    --实名认证
    ServerReallyName = 0x028F, --实名认证 上行
    ClientReallyName = 0x028E, --实名认证 下行
    ServerReallyNameLogin = 0x0290, --实名认证 上行 二次登录时用到
    ServerRename = 0x0023, --改名上行
    ClinetRename = 0x0024, --改名下行

    -- 符文预热活动
    ServerBenefitTokenAct = 0x0292,
    ClientBenefitTokenAct = 0x0293,
    --活跃神兵
    ServerActiWeapon = 0x02A0, --活跃神兵上行
    ClientActiWeapon = 0x02A1, --活跃神兵下行

    -- 符文副本
    ServerTokenDungeon = 0x02AB,
    ClientTokenDungeon = 0x02AA,

    --获取途径
    ServerGetway = 0x02A4, --上行
    ClientGetway = 0x02A5, --下行

    --vip弹脸
    ServerVipCustomer = 0x02A8, --上行
    ClientVipCustomer = 0x02A9, --下行

    --周优惠礼包
    ServerWeeklyGift = 0x06244, --上行
    ClientWeeklyGift = 0x06245, --下行

    --阵营
    ServerCamp = 0x02B0, -- 上行(阵营)
    ClientCamp = 0x02B1, -- 下行（阵营）
    ServerCampCity = 0x02B2, --上行（城池）
    ClientCampCity = 0x02B3, --下行（城池）
    ServerCampPosition = 0x02B4, -- 上行（阵营官职）
    ClientCampPosition = 0x02B5, -- 下行（阵营官职）
    ServerCampActivity = 0x02B6, -- 上行（活动）
    ClientCampActivity = 0x02B7, -- 下行（活动）
    ServerCampTask = 0x02B8, -- 上行（任务）
    ClientCampTask = 0x02B9, -- 下行（任务）
    ServerCampTaskScene = 0x02D4, --上行 (任务副本)
    ServerCampEnter = 0x02BC, --上行（阵营日常）
    ClientCampEnter = 0x02BD, --下行（阵营日常）

    ServerCampFight = 0x02BB,
    ClientCampFight = 0x02BA,

    ServerNewDailyCharge = 0x02D1, --上行
    ClientNewDailyCharge = 0x02D0, --下行

    --战力提升
    ServerFightGrowUp = 0x002B, --上行
    ClientFightGrowUp = 0x002A, --下行

    ServerLuckTurntable = 0x02D8, --转盘（上行）
    ClientLuckTurntable = 0x02D9, --转盘（下行）

    -- 老玩家回归
    ServerPlayerBack = 0x02DC, -- 老玩家回归
    ClientPlayerBack = 0x02DD, -- 老玩家回归

    --限时秒杀
    ServerPanicBuy = 0x6248, --上行
    ClientPanicBuy = 0x6249, --下行

    --宝宝
    ServerBaby = 0x02E0, --
    ClientBaby = 0x02E1, --

    --炼化
    ServerRefine = 0x02F0, --上行
    ClientRefine = 0x02F1, --下行

    --每日一抽
    ServerLuckDay = 0x02F2, --上行
    ClientLuckDay = 0x02F3, --下行

    --跨服弑神版
    ServerKillGod = 0x624A, --上行
    ClientKillGod = 0x624B, --下行

    ---新每日充值/登录奖励
    ServerDailyLivenss = 0x02F6, --上行
    ClientDailyLiveness = 0x02F7, --下行

    ServerMoonCake = 0x02FE,
    ClientMoonCake = 0x02FF,

    ServerCollectPhone = 0x02FA,
    ClientCollectPhone = 0x02FB,

}
