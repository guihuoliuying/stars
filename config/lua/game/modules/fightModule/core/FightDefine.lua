-- region
-- Date    : 2016-06-15
-- Author  : daiyaorong
-- Description :  
-- endregion
FightDefine = {}

-- 技能类型
FightDefine.SKILLTYPE_NORMAL 	= 1 			--普攻
FightDefine.SKILLTYPE_SKILL 	= 2 			--小技能
FightDefine.SKILLTYPE_AVOID		= 3				--闪避
FightDefine.SKILLTYPE_ULTIMATE  = 4 			--大招
FightDefine.SKILLTYPE_BORN		= 5 			--出场
FightDefine.SKILLTYPE_ACTIVE	= 6 			--激活

FightDefine.POOLTYPE_CHARAC		= 1
FightDefine.POOLTYPE_BULLET		= 2

-- 目标类型
FightDefine.TARGETTYPE_NONE		= 0 			--无目标
FightDefine.TARGETTYPE_NEAREST	= 1 			--最近目标
FightDefine.TARGETTYPE_RANDOM	= 2 			--随机目标
FightDefine.TARGETTYPE_MASTER	= 3 			--伙伴主人

-- 怪物释放类型
FightDefine.MONSTERORDER_SEQUENCE	= 1
FightDefine.MONSTERORDER_RAMDOM		= 2
FightDefine.MONSTERORDER_AI			= 3

-- 技能操控类型
FightDefine.CONTROL_DEFAULT		= 0 			--可操控
FightDefine.CONTROL_NONE		= 1 			--不可操控
FightDefine.CONTROL_TURN		= 2 			--可转向
FightDefine.CONTROL_MOVE		= 3 			--可移动
FightDefine.CONTROL_TURN_MOVE	= 4 			--可转向可移动但不会播放走的动作
 

-- 技能朝向
FightDefine.DIRECTION_SPEED		= 1 			--运动速度方向

-- 距离类型
FightDefine.DISTYPE_AREA		= 1 			--范围内即可
FightDefine.DISTYPE_FIX			= 2 			--特定距离

-- 受击方向
FightDefine.HITDIREC_FORWARD	= 1 			--攻击者朝向
FightDefine.HITDIREC_LINE		= 2 			--攻击者与目标连线
FightDefine.HITDIREC_TOATTACKER = 3				--朝攻击者的方向

-- 特效类型
FightDefine.EFFTYPE_FOLLOWROLE 	= 1
FightDefine.EFFTYPE_FIXED 		= 2
FightDefine.EFFTYPE_SKILL		= 3

-- 队列限制
FightDefine.QUEUELIMIT_EFFECT	= 4
FightDefine.HITEFFECT_LIMIT 	= 10

-- 特效数量限制
FightDefine.NUMLIMIT_AUTO 		= 10
FightDefine.NUMLIMIT_BONE		= 10
FightDefine.NUMLIMIT_NUMBER_PVP		= 10
FightDefine.NUMLIMIT_NUMBER_PVE		= 10

-- 动作相关
FightDefine.ACTION_UNKNOWPOINT		= 0 		--未知型
FightDefine.ACTION_WORLDPOINT		= 1 		--世界坐标
FightDefine.ACTION_ATTACKERSKEWING 	= 2 		--释放者坐标系偏移
FightDefine.ACTION_ATTACKPOINT		= 3 		--相对释放者偏移
FightDefine.ACTION_TARGETHIT		= 4 		--相对目标偏移
FightDefine.ACTION_BORNPOS			= 5 		--相对释放者出生点偏移
FightDefine.ACTION_ATTACKERINIT 	= 8 		--释放者初始位置
FightDefine.ACTION_TARGETINIT		= 9 		--技能释放初始时目标所在位置
FightDefine.ACTION_TARGETSKEWING	= 10 		--目标坐标系偏移
FightDefine.ACTION_ATTACKERDIRECOFF = 11 		--攻击者向目标朝向的偏移
FightDefine.ACTION_TARGETDIRECOFF  	= 12 		--目标向攻击者朝向的偏移

FightDefine.TRACK_SPEEDSTRAIGHT  	= 1 		--定点定速直线
FightDefine.TRACK_TIMESTRAIGHT 		= 2 		--定点定时直线
FightDefine.TRACK_SPEEDPARABOLA		= 3 		--定点定速抛物线
FightDefine.TRACK_TIMEPARABOLA		= 4 		--定点定时抛物线
FightDefine.TRACK_HITFLY			= 5 		--击飞
FightDefine.TRACK_HITBACK			= 6 		--击退
FightDefine.TRACK_TRACE				= 7 		--跟踪
FightDefine.TRACK_AUTOFOLLOW        = 8         --自动跟随
FightDefine.TRACK_AUTOFACE          = 9         --自动朝向
FightDefine.TRACK_DIRECSTRAIGHT 	= 10 		--定向定速直线
FightDefine.TRACK_CIRCLE			= 15 		--圆周运动
FightDefine.TRACK_TRACE_CIRCLE		= 16 		--跟踪圆周运动
FightDefine.TRACK_AUTOFOLLOWTURN    = 17    	--自动跟随可转向

FightDefine.RAYCAST_TRACK = 
{
	[FightDefine.TRACK_SPEEDPARABOLA] = true,
	[FightDefine.TRACK_TIMEPARABOLA] = true,
	[FightDefine.TRACK_CIRCLE] = true,
	[FightDefine.TRACK_TRACE] = true,
    [FightDefine.TRACK_AUTOFOLLOW] = true,
    [FightDefine.TRACK_AUTOFOLLOWTURN] = true,
}

FightDefine.PRECISION				= 0.001 	--默认精度

-- 战斗数据分类
FightDefine.DATA_SKILL				= "skill"
FightDefine.DATA_SKILLLEVEL			= "skilllevel"
FightDefine.DATA_MONSTER			= "monster"
FightDefine.DATA_DYNAMIC_BLOCK		= "dynamicblock"
FightDefine.DATA_MONSTER_SPAWN_AREA = "monsterspawnarea"
FightDefine.DATA_INIT_MONSTERS		= "initmonsterids"
FightDefine.DATA_BUFF				= "buff"
FightDefine.DATA_DROP				= "drop"
FightDefine.DATA_PLAYER				= "player"
FightDefine.DATA_FIGHTER			= "fighter"
FightDefine.DATA_PARTNER			= "partner"
FightDefine.DATA_MONSTERWAVETYPECOUNT = "monsterwavetypecount" --怪物波数里的怪物类型和数量, 注意, 并不是每个副本都会这个数据的;
FightDefine.DATA_ISPRODUNGEONSHOWTOWER = "isProDungeonShowTower"  --经验副本里面是否显示打怪进度 
FightDefine.DATA_BORN_BUFF			= "bornbuff"  --出生Buff
FightDefine.DATA_CAMP_BUFF			= "campbuff" --阵营buff

FightDefine.BULLET_SHAPE_CIRCLE 	= 1			--圆形
FightDefine.BULLET_SHAPE_RECT 		= 2			--矩形
FightDefine.BULLET_SHAPE_SECTOR 	= 3			--扇形

-- 技能表现类型
FightDefine.SKILLEFF_CHARAC			= 1 		--刀光类
FightDefine.SKILLEFF_BULLET			= 2 		--弹道类

-- 子弹类型
FightDefine.BULLET_NORMAL			= 1
FightDefine.BULLET_PIERCE 			= 2
FightDefine.BULLET_BOMB				= 3

FightDefine.ASSEMBLEID_BASE			= 10000000	--组装的ID起始数
FightDefine.ASSEMBLEID_MAX			= 30 		--组装的ID最大值
FightDefine.HITACTION_HITBACK		= 1 		--击退效果
FightDefine.HITACTION_HITFLY 		= 2 		--击飞效果

FightDefine.FIGHT_COMMON_FLOATSPEEDY = "floatinghitspeedY"
FightDefine.FIGHT_COMMON_G = "Gravitation"
FightDefine.FIGHT_COMMON_HITFLYHEIGHT = "hitflyheight"
FightDefine.FIGHT_COMMON_HITEFFCT = "hiteffect"
FightDefine.FIGHT_COMMON_FIGHTAREA = "fightarea"
FightDefine.FIGHT_COMMON_HITFLASH = "hitflash"
FightDefine.FIGHT_COMMON_COMBOFADETIME = "combofadetime"				-- 连击数消逝时间
FightDefine.FIGHT_COMMON_COMBONUMBERPOPSIZE = "combonumberpopsize"		-- 连击数弹出尺寸
FightDefine.FIGHT_COMMON_COMBONUMBERPOPTIME = "combonumberpoptime"		-- 连击数弹出时间
FightDefine.FIGHT_COMMON_COMBONUMBERFADETIME = "combonumberfadetime"	-- 连击数消失时间
FightDefine.FIGHT_COMMON_SHADOWDAMAGE = "shadowdamage"	-- 幻影buff伤害数字延时
FightDefine.FIGHT_COMMON_RESETAUTO = "losecontrol"
FightDefine.FIGHT_COMMON_BOSSCOMING = "bosscoming"
FightDefine.FIGHT_COMMON_BOSSCOMING_MUSIC = "bosscoming_music"
FightDefine.FIGHT_COMMON_BOSSCOMING_SHAKE = "bosscomingshake"
FightDefine.FIGHT_COMMON_GHOSTSHADOW = "ghostshadow"	-- 残影效果配置
FightDefine.FIGHT_COMMON_NEWDUNGEON = "newguidedungeon" -- 新手副本

FightDefine.TONUMER_LIST = {
	FightDefine.FIGHT_COMMON_FLOATSPEEDY,
	FightDefine.FIGHT_COMMON_G,
	FightDefine.FIGHT_COMMON_HITFLYHEIGHT,
	FightDefine.FIGHT_COMMON_FIGHTAREA,
	FightDefine.FIGHT_COMMON_COMBOFADETIME,
	FightDefine.FIGHT_COMMON_COMBONUMBERPOPSIZE,
	FightDefine.FIGHT_COMMON_COMBONUMBERPOPTIME,
	FightDefine.FIGHT_COMMON_COMBONUMBERFADETIME,
	FightDefine.FIGHT_COMMON_HITFLASH,
	FightDefine.FIGHT_COMMON_SHADOWDAMAGE,
	FightDefine.FIGHT_COMMON_RESETAUTO,
	FightDefine.FIGHT_COMMON_BOSSCOMING,
	FightDefine.FIGHT_COMMON_NEWDUNGEON,
}

-- 刷怪条件
FightDefine.MONSTER_SPAWN_CONDITION = 
{
	NONE = 1,			-- 无，进关卡马上刷出
	WAVE = 2,			-- 上一次全部死亡时刷出
	ENTER_AREA = 3,		-- 进入区域时刷出
}

FightDefine.AI_MATCH = {
	logic = "[%&%$]",
	compare = "([%d%a%,%(%)%*]+)([<>=!]+)([%d%a%,%(%)%*]+)",
	func = "([%a]+)([%(%)%,%a%d%*]+)",
	hppercent = "(hppercent%()(%a+)(%))",
	random = "(random%()(%d+)(%,)(%d+)(%))",
    allynumber = "(allynumber%()(%))",
    state = "(state%()(%a+)(%))",
    havebuff = "(havebuff%()(%a+)(%,)(%d+)(%))",
    distance = "(distance%()([%*%d]+)(%,)([%*%d]+)(%))",
}

-- 战斗数字标识
FightDefine.FIGHT_NUM_FLAG = 
{
	DAMAGE = 1,		-- 普通伤害
	CRIT = 2,		-- 暴击
	MISS = 3,		-- 闪避
	CURE = 4,		-- 治疗
	IMMUNE = 5,		-- 免疫
	POISONING = 6,	-- 中毒
	ABSORB = 7,		-- 吸收
	FOCUS  = 8,     --会心
}

-- 通用特效
FightDefine.DYNAMIC_BLOCK_RES = "dynamicblock_01"		-- 动态阻挡资源
FightDefine.GUIDE_JIANTOU = "eff_chr_guide_direction"		-- 地面指引箭头
FightDefine.WARN_CIRCLE = "areawarning_circle"			-- 预警圆形
FightDefine.WARN_SQUARE = "areawarning_square"			-- 预警矩形
FightDefine.CLICKEFF_NORMAL = "ueff_fight_btn_touch"	-- 点击普攻
FightDefine.CLICKEFF_SKILL = "ueff_fight_btn_skill"		-- 点击技能
FightDefine.CD_FINISH = "ueff_fight_btn_cd_finish"		-- CD结束
FightDefine.CD_EFFECT = "ueff_fight_cd"                 -- 技能CD特效
FightDefine.BOSS_COMING_EFFECT	= "ueff_boss_coming"	-- BOSS来袭特效
FightDefine.FOOT_SHADOW = "eff_chr_shadow"					-- 角色脚底阴影
FightDefine.PARTER_LVUP = "ueff_fight_partner_levleup"  -- 伙伴升级
FightDefine.DEAD_EFFECT = "eff_common_grounddust_1"  	-- 出生死亡特效
FightDefine.WARNING_EFF ={
	FightDefine.WARN_CIRCLE,
	FightDefine.WARN_SQUARE,
	FightDefine.WARN_CIRCLE
}

FightDefine.WARN_CIRCLE_R = 50	--单位为分米
FightDefine.WARN_SQUARE_L = 50
FightDefine.WARN_SQUARE_W = 50	
FightDefine.VIEW_COLOR_PROPERTY    	= "_Color"

-- BUFF类型
FightDefine.BUFF_TYPE = 
{
	BENEFIT = 1,	-- 增益型
	HARMFUL = 2,	-- 减益型
}

-- BUFF效果类型
FightDefine.BUFF_EFFECT_TYPE = 
{
	ATTRIB = 1,			-- 属性类
	POISONING = 2,		-- 中毒类
	CURE = 3,			-- 治疗类
	CTRL = 4,			-- 控制类
	DAMAGE = 5,			-- 伤害类
	BEATBACK = 6,		-- 反击类
	TAUNT = 7,			-- 嘲讽类
	SHIELD = 8,			-- 护盾类
    POISONBOMB = 9,     -- 毒爆类
	SUPERARMOR = 10,	-- 霸体类
	INVINCIBLE = 11,	-- 无敌类
	HPDOWN = 12,		-- 血量下降
	DAMAGE_INTENSIFY = 13,	-- 伤害加深(受击者)
	DAMAGE_ADD = 14,	-- 伤害加成（攻击者）
	BUFF_CLEAN = 15,	-- 驱散净化类
	HP_PERCENT = 16,	-- 百分比治疗类（操作hp值，可增可减）
	BUFF_DURATION_ADD = 17,	-- 增加所有buff的持续时间
	BUFF_GETHP = 18,  --吸血类buff
	BUFF_GETDEMAGEHP = 19, --吸收类buff
	BUFF_NEWPOISONING = 20,	-- 新版中毒
	PHANTOM = 99,		-- 幻影类
}

-- BUFF目标
FightDefine.BUFF_TARGET = 
{
	ENEMY = 0,			-- 敌人
	SELF = 1,			-- 自己
	SELFSIDE = 2,		-- 己方全体
	MASTER = 3,			-- 伙伴主人
	SELFPLAYER = 4,		-- 己方玩家
}

-- 被动技能效果类型
FightDefine.PASSEFF_ATTRIBUTE 	= 1 	--属性型
FightDefine.PASSEFF_BUFF		= 2 	--buff型
FightDefine.PASSEFF_DemagePercent		= 3 	--直接计算伤害千分比型

FightDefine.PASSTYPE_TRUMP      = 3     --法宝被动技能

-- 被动技能效果触发
FightDefine.PASSACTIVE_NODE		= 0 	--无条件触发
FightDefine.PASSACTIVE_HIT		= 1 	--特定技能命中触发
FightDefine.PASSACTIVE_HP		= 2 	--血量触发
FightDefine.PASSACTIVE_COMMONHIT= 3     --命中触发
FightDefine.PASSACTIVE_SKILL    = 4     --技能触发
FightDefine.PASSACTIVE_BEHIT    = 5     --受击触发
FightDefine.PASSACTIVE_HITS     = 6     --玩家的每一次攻击均会触发   目标类型+条件类型+参数，目标类型：1代表自己，2代表敌人  条件类型：1类型，血量判断，参数填目标类型百分比      2类型，buff判断，参数填buffid   3类型，buff类型判断，填1或者2,1代表增益类，2代表减益类    4类型，暴击判断，不用填参数。


-- 被动技能目标
FightDefine.PASSTARGET_SELF		= 1 	--自身
FightDefine.PASSTARGET_ENEMY	= 0 	--敌人

FightDefine.SPAWNAREA_CIRCLE	= 1		-- 圆形刷怪区域
FightDefine.SPAWNAREA_RECT		= 2		-- 矩形刷怪区域

FightDefine.FIGHT_STATE_NONE		= 0		-- 战斗状态：未开始
FightDefine.FIGHT_STATE_READY		= 1		-- 战斗状态：准备中
FightDefine.FIGHT_STATE_FIGHTING	= 2		-- 战斗状态：进行中
FightDefine.FIGHT_STATE_PAUSE		= 3		-- 战斗状态：暂停中
FightDefine.FIGHT_STATE_END			= 4		-- 战斗状态：已结束

--条件类型
FightDefine.CONDITION_HP        = 1
FightDefine.CONDITION_BUFFEFF   = 2
FightDefine.CONDITION_BUFFTYPE  = 3
FightDefine.CONDITION_CRIT      = 4
FightDefine.CONDITION_STATE     = 5

FightDefine.FIGHT_PVP_READY_CD	= 3			-- PVP默认的准备CD
FightDefine.FIGHT_FAMILYWAR_READY_CD = 30	-- 家族战默认准备CD
FightDefine.FAMILYWAR_R = 17 				-- 家族战服务端阻挡半径(米)
FightDefine.FAMILYWAR_CIRCLE_A = Vector3.New(-63.3, 3.7, 0.5)
FightDefine.FAMILYWAR_CIRCLE_B = Vector3.New(63.3, 3.7, 0.5)

-- 服务端java指令类型
FightDefine.SORDER_ADDCAMPBUFF				= 1		-- 给某个阵营的全部成员添加buff
FightDefine.SORDER_REMOVECAMPBUFF			= 2		-- 移除某个阵营的指定buff
FightDefine.SORDER_MOVECHARAC				= 3		-- 移动指定角色到指定位置
FightDefine.SORDER_STOPCHARAC				= 4		-- 停止移动
FightDefine.SORDER_CHANGE_FIGHTSTATE		= 5		-- 更改战斗状态
FightDefine.SORDER_RESET_CHARACS			= 6		-- 重置角色状态
FightDefine.SORDER_ADD_BUFF					= 7		-- 给指定角色添加BUFF
FightDefine.SORDER_SETAI					= 8		-- 设置角色AI

FightDefine.AI_RUN_INTERVAL					= 1	-- AI运行间隔
FightDefine.DAILY5V5_READY_CD				= 30	-- 日常5v5战斗准备时间

-- 自动战斗记录前缀 后接角色ID
FightDefine.AUTOFIGHT_NAME = "autofight_record_"