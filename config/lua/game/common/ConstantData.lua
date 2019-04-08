

ConstantData = {}

--层级
ConstantData.UI_BOTTOM = 0
ConstantData.UI_MIDDLE = 10
ConstantData.UI_TOP = 110

--定时类型
ConstantData.FRAME_EVENT_LUA = "lua" --lua
ConstantData.FRAME_EVENT_LUACHARAC = "lua_charac" --角色
ConstantData.FRAME_EVENT_LUASCENE = "lua_scene" --场景(地图 寻路 传送圈)
ConstantData.FRAME_EVENT_AI = "AI" --ai
ConstantData.FRAME_EVENT_UI = "UI" --ui
ConstantData.FRAME_EVENT_CAMERA = "camera" --相机
ConstantData.FRAME_EVENT_DEFAULT = "default" --默认

-- 场景类型
ConstantData.STAGE_TYPE_MAIN        = 0		--主城
ConstantData.STAGE_TYPE_DUNGEON     = 1		--普通关卡
ConstantData.STAGE_TYPE_SKYTOWER    = 2		--镇妖塔;
ConstantData.STAGE_TYPE_SEARCHTREASURE = 3  --仙山夺宝;
ConstantData.STAGE_TYPE_PRODUCEDUNGEON = 4  --产出副本;
ConstantData.STAGE_TYPE_GAMECAVE = 6  --洞府(小游戏场景);
ConstantData.STAGE_TYPE_CALLBOSS    = 5     --召唤boss
ConstantData.STAGE_TYPE_TEAM = 7--组队副本
ConstantData.STAGE_TYPE_FAMILYEXPED = 8  --家族远征
ConstantData.STAGE_TYPE_FAMILY = 9  -- 家族场景
ConstantData.STAGE_TYPE_LOOTTREASURE_WAIT = 10; --野外夺宝-等待场景;
ConstantData.STAGE_TYPE_LOOTTREASURE_PVE = 11; --野外夺宝-PVE场景;
ConstantData.STAGE_TYPE_LOOTTREASURE_PVP = 13; --野外夺宝-PVP场景;
ConstantData.STAGE_TYPE_OFFLINEPVP = 12--离线pvp
ConstantData.STAGE_TYPE_FAMILY_INVADE = 14--外族入侵
ConstantData.STAGE_TYPE_BRAVE_TRIAL = 15	-- 勇者试炼
ConstantData.STAGE_TYPE_FIGHTINGMASTER = 16	-- 斗神殿
ConstantData.STAGE_TYPE_ROYALREWARD = 17 --皇榜悬赏
ConstantData.STAGE_TYPE_TEAM_PVP = 18	 -- 组队PVP
ConstantData.STAGE_TYPE_CARGO_TROOP = 19	-- 运镖队列场景
ConstantData.STAGE_TYPE_FAMILY_WAR_ELITE_FIGHT = 20--家族战精英战场
ConstantData.STAGE_TYPE_CARGO_PVP = 21		-- 运镖PVP
ConstantData.STAGE_TYPE_MARRY = 22  -- 婚宴场景
ConstantData.STAGE_TYPE_CARGO_ROBOT = 23 -- 运镖：劫镖机器人用的场景(弱同步组队)
ConstantData.STAGE_TYPE_FAMILY_WAR_NORMAL_FIGHT = 24--家族匹配战
ConstantData.STAGE_TYPE_POEM = 25 --诗歌关卡_普通副本;
ConstantData.STAGE_TYPE_FAMILY_WAR_STAGE_FIGHT = 26--家族战小关卡
ConstantData.STAGE_TYPE_PK = 100--在线pvp
ConstantData.STAGE_TYPE_NEWGUIDE = 101 --新手场景
ConstantData.STAGE_TYPE_ADVENTURE_DAILY = 27  --家族探宝日常关卡
ConstantData.STAGE_TYPE_ADVENTURE_FINAL = 28  --家族探宝周日关卡
ConstantData.STAGE_TYPE_ELITE = 29 --精英副本
ConstantData.STAGE_TYPE_OFFLINEMATCH = 30 --荣誉竞技场
ConstantData.STAGE_TYPE_FAMILYTASK = 31 --家族任务专用关卡
ConstantData.STAGE_TYPE_FAMILY_WAR_PREPARE = 32 --家族战备战场景
ConstantData.STAGE_TYPE_FAMILY_TRANSPORT = 33 	--家族押镖运镖场景
ConstantData.STAGE_TYPE_FAMILY_TRANSPORT_PVP = 34 	--家族押镖战斗场景
ConstantData.STAGE_TYPE_DAILY5V5_PVP = 35	-- 日常5v5战斗场景
ConstantData.STAGE_TYPE_TEAM_POEM = 36 --诗歌关卡_组队关卡;
ConstantData.STAGE_TYPE_COUPLE_DUNGEON = 38 -- 夫妇副本
ConstantData.STAGE_TYPE_BUDDY_DUNGEON = 39 -- 伙伴副本
ConstantData.STAGE_TYPE_BENEFIT_TOKEN = 40 -- 符文预热活动
ConstantData.STAGE_TYPE_TOKEN_DUNGEON = 41 -- 符文副本
ConstantData.STAGE_TYPE_CAMP_QICHU = 42	-- 阵营战：齐楚之争
ConstantData.STAGE_TYPE_CAMP_TASK = 43 --阵营任务
ConstantData.STAGE_TYPE_CAMP_DAILY = 44 --阵营日常
ConstantData.STAGE_TYPE_CAMP_KILLGOD = 45 --弑神挑战

--角色货币类型
ConstantData.ROLE_MONEY_TYPE =
{
	GOLD				= 1,   -- RMB
	BINDGOLD			= 2,   -- 绑金
	MONEY				= 3,   -- 金币
    VIRGOR				= 4,   -- 体力
	EXP					= 5,   -- 经验
	DAILY_POINT			= 6,   -- 日常活跃度
    GLORY               = 8,   --荣誉点
    SKILLPOINT          = 9,   -- 技能点
    VIPEXP              = 16,  --贵族经验
    FAMILYCONTRIBUTION  = 51,  -- 家族贡献
    FAMILYMONEY         = 54,  -- 家族资金
    FEATS 				= 59,  -- 功勋
    ENERGY 				= 19,  --精力
}

ConstantData.RESOURCE_TYPE = 1   --资源类型

-- tag
ConstantData.TAG_CHARACTER = "Character"

ConstantData.CANNOTGRAYNAME = "cannotgray";

--登陆的类型;
ConstantData.LoginType = {
	--不显示1,2的视图效果,只有背景;
	TYPE_NONE = 0;
	--输入用户名密码;
	TYPEIN_USER = 1,
	--选择服务器;
	SELECT_SERVER = 2,

	--输入用户名密码背景图;
	TYPEIN_USER_BG = "loginBG1",
	--选择服务器背景图;
	SELECT_SERVER_BG = "loginBG1",
}

--栅栏相关;
ConstantData.Fence = {
	TYPE_RECTANGLE = 1, --矩形;
}

--协议相关
ConstantData.ByteValueLen =  1 --  1 个字节
ConstantData.ShortValueLen = 2 -- 2个字节
ConstantData.IntValueLen =   4 --   4个字节
ConstantData.LongValueLen =  8 -- 8个字节


--点击的时间间隔(秒);
ConstantData.CLICK_CD_TIME = 0.2;
ConstantData.LAYER_PLAYER = "player" -- player层
ConstantData.LAYER_NPC = "npc"
ConstantData.LAYER_MONSTER = "monster"
ConstantData.LAYER_DEFAULT = "Default" -- player层
ConstantData.LAYER_UI = "UI"		 -- ui层
ConstantData.LAYER_UI_EFFECT = "UIEffect"		 -- ui effect层
ConstantData.LAYER_SKILL_TOP = "skillTop"
ConstantData.LAYER_SKILL_EFFECT = "skillEffect"

ConstantData.ROLE_CONST_HP = "hp"
ConstantData.ROLE_CONST_INVINCIBLE = "invincible"
ConstantData.ROLE_CONST_POSITION = "position"
ConstantData.ROLE_CONST_ROTATION = "rotation"
ConstantData.ROLE_CONST_MAXHP = "maxhp"
ConstantData.ROLE_CONST_MOVESPEED = "movespeed"
ConstantData.ROLE_CONST_OUTSHOW = "outShowInfo"

ConstantData.ID_CHARACTER_IDLE_STATE = 1 	--待机状态
ConstantData.ID_CHARACTER_RUN_STATE = 2	--跑动状态
ConstantData.ID_CHARACTER_ATTACK_STATE = 3	--攻击状态
ConstantData.ID_CHARACTER_DIE_STATE = 4	--死亡状态
ConstantData.ID_CHARACTER_ACTION_STATE = 5	--播放特殊动作状态
ConstantData.ID_CHARACTER_JUMP = 6		--跳跃动作状态

ConstantData.CHARACTER_ANIM_TYPE_ANIMATOR = 1  --新动画
ConstantData.CHARACTER_ANIM_TYPE_MESH = 2 		--蒙皮动画
ConstantData.CHARACTER_ROTATE_SPEED = 360  	--角色旋转速度

ConstantData.FRAME_DELTA_TIME = 0.033 		--帧间隔
ConstantData.FRAME_RATE = 30 		--帧频

ConstantData.AI_NORMAL = 1
ConstantData.AI_SIMPLE = 2
ConstantData.AI_DEFEND = 3
ConstantData.AI_NATURE_ADJUST_DISTANCE = 5 	--基础AI调整站位间隔
ConstantData.AI_NATURE_CAREFUL_DISTANCE = 3 	--谨慎
ConstantData.AI_NATURE_EAGER_WAR_DISTANCE = 1 	--好战AI调整站位间隔
ConstantData.AI_CHARACTER_DISTANCE = 1.2	--角色与角色的站位间隔，检测间隔距离
ConstantData.AI_CHARACTER_STAND_DISTANCE = 1.4 --站位调整间隔
ConstantData.AI_WALK_DISTANT_MIN = 0.5 		--伪碰撞走位最小距离
ConstantData.AI_CHARACTER_TIMEGAP = 1 --伪碰撞检测时间间隔

ConstantData.ROLE_PROFESSION_WARRIOR = 0
ConstantData.ROLE_PROFESSION_RABBI = 1
ConstantData.ROLE_PROFESSION_MINISTER = 2

ConstantData.ANKIND_ROLEPLAYER = 1
ConstantData.ANKIND_NPC = 2
ConstantData.ANKIND_MONSTER = 3
ConstantData.ANKIND_SKILL = 4
ConstantData.ANKIND_EFFECT = 5
ConstantData.ANKIND_UI_EFFECT = 6
ConstantData.ANKIND_SOLDIER_LEADER = 7
ConstantData.ANKIND_SOLDIER = 8
ConstantData.ANKIND_RIDE = 9

ConstantData.NAV_DEFAULT_LAYER = 1

ConstantData.FIX_FRAME_SECOND = 30					--一秒多少帧

--相机参数
-- ConstantData.MIN_CAMERA_ROTATION = 25
-- ConstantData.MAX_CAMERA_ROTATION = 40

--宝石TIPS;
ConstantData.GEM_TIPS_SHOW_TYPE_FULL = 0; --包含属性, 包含按钮
ConstantData.GEM_TIPS_SHOW_TYPE_SIMPLE = 1; --不包含属性,不包含按钮;
ConstantData.GEM_TIPS_SHOT_TYPE_NORMALBAG = 3; --调用背包的tips;

ConstantData.CAMERA_BLACK_WHITE_EFFECT = "ueff_poemsdungeon_round";
-------------------------图集相关---------------------------
ConstantData.AtlasName = {
	LoginWindow = "loginWindowAtlas",
	Loading = "loading",
}

--游戏里的星级最大数;
ConstantData.STAR_MAX_NUM = 5;
--------------------------UIPlayer模型相关------------------
ConstantData.UIPlayerModel = {
	dirDegree = 180,
	dialog = "roles",
	LAYERNAME = ConstantData.LAYER_UI,
	BG_WIDTH = 330,
	BG_HEIGHT = 360,
	MODEL_SCALE = Vector3.New(230,230,230),
	MODEL_SCALE_MAINWINDOW = Vector3.New(270,270,270),
	MODEL_CURRENT_POSITION = Vector3.New(15, 137, -171),
	MODEL_LOCAL_ROTATION = Quaternion.Euler(0, 180, 0),
	NAME_LOCALPOSITION = Vector3.New(43,61.7,0),
	NAME_FONTSIZE = 24,
	NAME_SIZEDETA = Vector2.New(200,100),
	SHADOW_LOCAL_POSITION = Vector3.New(24,106,0),
	STARBOARD_LOCALPOSITION = Vector3.New(23,40,0),
	STAR_SIZEDETA = Vector2.New(35, 35),
	STAR_WIDTH = 32,
	--技能数据的播放类型;
	CREATEROLEWINDOW_SKILLSHOWDATA = 0,
	HEROWINDOW_SKILLSHOWDATA = 1,
	PROTOCOL_FRAGMENT_TAG = ";",
	PROTOCOL_FIELD_TAG = ",",
}

------------------------美术字相关--------------------------
--美术字类型
ConstantData.NUMBER_ART_TYPE = {
	FIGHT = 0, --战力
	FIGHT_FORMAT = "U_fight_",
	--战力数字的宽度;
	FIGHT_WIDTH = 19,

	LVL = 1,
	LVL_FORMAT = "U_lv_",
	LVL_WIDTH = 19,
    LVL_DOT = "U_lv_dot",
    LV_PAG = "U_lv_pag",
    LV_X = "U_lv_X",
}

--品质对应的颜色表
-- ConstantData.ColorQuality = {
-- 	Color.New(1,1,1,1),                       --白
-- 	Color.New(0,1,0,1),						  --绿
-- 	Color.New(0.1529,0.6901,0.9490,1),        --蓝
-- 	Color.New(0.945, 0.33725, 1, 1),		  --紫
-- 	Color.New(1,0.4745,0,1),                  --橙
-- 	Color.New(0.9843, 0.7804, 0.2941, 1),     --金
-- }

-- ConstantData.ColorQualityHex = 
-- {	'#FFFFFF',        --白
-- 	'#00FF00',		  --绿
-- 	'#1DB0F2',        --蓝
-- 	'#F156FF',        --紫   
-- 	'#FF7800',        --橙
-- 	'#FBC74B',        --金
-- }

ConstantData.UEFF_TEAM_CHOOSE = "ueff_team_choose";
ConstantData.UEFF_CITYDECISION_GAINSOURCE = "ueff_citydecision_gainsource";
ConstantData.UEFF_CITYMAKE_EUIP_MAKEBLE = "ueff_citymake_euip_makeble";
ConstantData.UEFF_CITYMAKE_EUIP_SOURCESHORT = "ueff_citymake_euip_sourceshort";
ConstantData.UEFF_ITEM_CHOOSE = "ueff_item_choose";
ConstantData.UEFF_POINT_EFFECT = "ueff_gameinduct_arrowinduct"
ConstantData.UEFF_ARROR_EFFECT = "ueff_gameinduct_arrowdirection"
ConstantData.U_BATTLE_QTETIMEWARING = "u_battle_qtetimewaring";
ConstantData.U_BATTLE_NUMLIGHT = "u_battle_numlight";
ConstantData.GUIDE_CURSOR = "guide_cursor";
ConstantData.EFFECT_WORLDMAP_ENTERAREA_GATHER = "effect_worldmap_enterarea_gather";
ConstantData.EFFECT_WORLDMAP_ENTERAREA_EXPAND = "effect_worldmap_enterarea_expand";
ConstantData.EFFECT_STAGE_FIGHT_START = "ueff_stagebattle_start"
ConstantData.EFFECT_LOADING_RUNNINGHORSE = "ueff_stageinside_loadarrow"
ConstantData.EFFECT_STAGE_FOOD_REDUCE = "ueff_stagewindow_resources_food"

ConstantData.UEFF_MODELUP_LIGHT = "ueff_modleup_light"		-- 模型激活特效
ConstantData.UEFF_ATT_UP = "ueff_att_up"					-- 属性增加特效
ConstantData.UEFF_ITEM_BREAK = "ueff_item_break"
ConstantData.UEFF_ITEM_FOCUS = "ueff_item_focus"

-------------------------音频相关---------------------------
--音效的最大播放数量
ConstantData.AUDIO_COUNT_MAX = 20;
--这里与C#定义一样, 播放scene;
ConstantData.AUDIO_TYPE_SCENE = 1;
--这里与C#定义一样, 播放fight;
ConstantData.AUDIO_TYPE_FIGHT = 2;
--这里与C#定义一样, 播放ui;
ConstantData.AUDIO_TYPE_UI = 3;
-- 切入式战斗
ConstantData.AUDIO_TYPE_BATTLE = 4
-- 切入式战斗场景
ConstantData.AUDIO_TYPE_BATTLE_SCENE = 5
--主界面音乐
ConstantData.SOUND_MAIN_CITY = "UI_City"
ConstantData.SOUND_UI_CLICK_IN = "UI_Clickin"
ConstantData.SOUND_UI_CLICK_OUT = "UI_Clickout"
--登陆界面
ConstantData.SOUND_LOGIN = "landing";
--胜利界面
ConstantData.SOUND_WIN = "win";
--胜利循环音乐
ConstantData.SOUND_WIN_LOOP = "win_loop";
--失败界面
ConstantData.SOUND_LOSE = "lose";
--失败循环音乐
ConstantData.SOUND_LOSE_LOOP = "lose_loop";
ConstantData.TASKTRACEITEM_EFFECT = "ueff_poems_open";

--画质;
ConstantData.GRAPHICQUALITY_LOW = 2;
ConstantData.GRAPHICQUALITY_MIDDLE = 1
ConstantData.GRAPHICQUALITY_HIGH = 0
ConstantData.PLAYERWEAPONEFFECT_MIDDLE_MAXINDEX = 3;

--音乐淡入淡出参数
--startVolumn:淡入所需要的音量(0~1)
ConstantData.SOUND_FADEIN_VOLUMN = 1;
--endVolumn:淡出所需要的音量(0~1)
ConstantData.SOUND_FADEOUT_VOLUMN = 0;
--changeRate:淡入的变化速率
ConstantData.SOUND_CHANGE_FADEIN_RATE = 0.05;
--changeRate:淡出的变化速率
ConstantData.SOUND_CHANGE_FADEOUT_RATE = 0.05;
--changeRate:淡入的变化速率时间间隔
ConstantData.SOUND_CHANGE_FADEIN_DELAY = 0.1;
--changeRate:淡出的变化速率时间间隔
ConstantData.SOUND_CHANGE_FADEOUT_DELAY = 0.1;
--continueTimeToEnd:淡出的持续时间(秒)
ConstantData.SOUND_CONTINUE_TIME_TO_END = 0.2;

ConstantData.EFFECT_NUMBER_COUNT_FACTOR = 5 --每帧可飘伤害数量=len/factor
ConstantData.EFFECT_NUMBER_LAYER_NORMAL = 1 	--飘字层次
ConstantData.EFFECT_NUMBER_LAYER_TOP = 2

ConstantData.RADIAL_BLUR_BEGIN_VALUE = 0 --径向模糊开始参数
ConstantData.RADIAL_BLUR_END_VALUE = 0.07 	--径向模糊最终参数
ConstantData.RADIAL_BLUR_DURATION = 1.1 		--径向模糊持续时间
ConstantData.RADIAL_BLUR_INC_SPEED = 0.01 	--模糊递增速度
ConstantData.RADIAL_BLUR_DEC_SPEED = 0.01 	--模糊递减速度

ConstantData.ENABLE_WARART = false

ConstantData.FRAMES_ONE_SECOND = 30   --一秒30帧

ConstantData.MinTime = 1 / ConstantData.FRAMES_ONE_SECOND / 2

ConstantData.FIGHT_NUMBER_POS_DEFAULT = Vector3(-2000,0,0)

sinTable ={0 ,0.02 ,0.03 ,0.05 ,0.07 ,0.09 ,0.1 ,0.12 ,0.14 ,0.16 ,0.17 ,0.19 ,0.21 ,0.22 ,0.24 ,0.26 ,0.28 ,0.29 ,0.31 ,0.33 ,0.34 ,
0.36 ,0.37 ,0.39 ,0.41 ,0.42 ,0.44 ,0.45 ,0.47 ,0.48 ,0.5 ,0.52 ,0.53 ,0.54 ,0.56 ,0.57 ,0.59 ,0.6 ,0.62 ,0.63 ,0.64 ,0.66 ,0.67 ,0.68 ,
0.69 ,0.71 ,0.72 ,0.73 ,0.74 ,0.75 ,0.77 ,0.78 ,0.79 ,0.8 ,0.81 ,0.82 ,0.83 ,0.84 ,0.85 ,0.86 ,0.87 ,0.87 ,0.88 ,0.89 ,0.9 ,0.91 ,0.91 ,
0.92 ,0.93 ,0.93 ,0.94 ,0.95 ,0.95 ,0.96 ,0.96 ,0.97 ,0.97 ,0.97 ,0.98 ,0.98 ,0.98 ,0.99 ,0.99 ,0.99 ,0.99 ,1 ,1 ,1 ,1 ,1 ,1 ,1 ,1 ,1 ,1 ,
1 ,0.99 ,0.99 ,0.99 ,0.99 ,0.98 ,0.98 ,0.98 ,0.97 ,0.97 ,0.97 ,0.96 ,0.96 ,0.95 ,0.95 ,0.94 ,0.93 ,0.93 ,0.92 ,0.91 ,0.91 ,0.9 ,0.89 ,
0.88 ,0.87 ,0.87 ,0.86 ,0.85 ,0.84 ,0.83 ,0.82 ,0.81 ,0.8 ,0.79 ,0.78 ,0.77 ,0.75 ,0.74 ,0.73 ,0.72 ,0.71 ,0.69 ,0.68 ,0.67 ,0.66 ,0.64 ,
0.63 ,0.62 ,0.6 ,0.59 ,0.57 ,0.56 ,0.54 ,0.53 ,0.52 ,0.5 ,0.48 ,0.47 ,0.45 ,0.44 ,0.42 ,0.41 ,0.39 ,0.37 ,0.36 ,0.34 ,0.33 ,0.31 ,0.29 ,
0.28 ,0.26 ,0.24 ,0.22 ,0.21 ,0.19 ,0.17 ,0.16 ,0.14 ,0.12 ,0.1 ,0.09 ,0.07 ,0.05 ,0.03 ,0.02 ,0 ,-0.02 ,-0.03 ,-0.05 ,-0.07 ,-0.09 ,
-0.1 ,-0.12 ,-0.14 ,-0.16 ,-0.17 ,-0.19 ,-0.21 ,-0.22 ,-0.24 ,-0.26 ,-0.28 ,-0.29 ,-0.31 ,-0.33 ,-0.34 ,-0.36 ,-0.37 ,-0.39 ,-0.41 ,
-0.42 ,-0.44 ,-0.45 ,-0.47 ,-0.48 ,-0.5 ,-0.52 ,-0.53 ,-0.54 ,-0.56 ,-0.57 ,-0.59 ,-0.6 ,-0.62 ,-0.63 ,-0.64 ,-0.66 ,-0.67 ,-0.68 ,-0.69 ,
-0.71 ,-0.72 ,-0.73 ,-0.74 ,-0.75 ,-0.77 ,-0.78 ,-0.79 ,-0.8 ,-0.81 ,-0.82 ,-0.83 ,-0.84 ,-0.85 ,-0.86 ,-0.87 ,-0.87 ,-0.88 ,-0.89 ,-0.9 ,
-0.91 ,-0.91 ,-0.92 ,-0.93 ,-0.93 ,-0.94 ,-0.95 ,-0.95 ,-0.96 ,-0.96 ,-0.97 ,-0.97 ,-0.97 ,-0.98 ,-0.98 ,-0.98 ,-0.99 ,-0.99 ,-0.99 ,
-0.99 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-0.99 ,-0.99 ,-0.99 ,-0.99 ,-0.98 ,-0.98 ,-0.98 ,-0.97 ,-0.97 ,-0.97 ,-0.96 ,-0.96 ,
-0.95 ,-0.95 ,-0.94 ,-0.93 ,-0.93 ,-0.92 ,-0.91 ,-0.91 ,-0.9 ,-0.89 ,-0.88 ,-0.87 ,-0.87 ,-0.86 ,-0.85 ,-0.84 ,-0.83 ,-0.82 ,-0.81 ,
-0.8 ,-0.79 ,-0.78 ,-0.77 ,-0.75 ,-0.74 ,-0.73 ,-0.72 ,-0.71 ,-0.69 ,-0.68 ,-0.67 ,-0.66 ,-0.64 ,-0.63 ,-0.62 ,-0.6 ,-0.59 ,-0.57 ,
-0.56 ,-0.54 ,-0.53 ,-0.52 ,-0.5 ,-0.48 ,-0.47 ,-0.45 ,-0.44 ,-0.42 ,-0.41 ,-0.39 ,-0.37 ,-0.36 ,-0.34 ,-0.33 ,-0.31 ,-0.29 ,-0.28 ,
-0.26 ,-0.24 ,-0.22 ,-0.21 ,-0.19 ,-0.17 ,-0.16 ,-0.14 ,-0.12 ,-0.1 ,-0.09 ,-0.07 ,-0.05 ,-0.03 ,-0.02 ,0}
sinTable[0] = 0

cosTable = {1 ,1 ,1 ,1 ,1 ,1 ,0.99 ,0.99 ,0.99 ,0.99 ,0.98 ,0.98 ,0.98 ,0.97 ,0.97 ,0.97 ,0.96 ,
0.96 ,0.95 ,0.95 ,0.94 ,0.93 ,0.93 ,0.92 ,0.91 ,0.91 ,0.9 ,0.89 ,0.88 ,0.87 ,0.87 ,0.86 ,0.85 ,0.84 ,
0.83 ,0.82 ,0.81 ,0.8 ,0.79 ,0.78 ,0.77 ,0.75 ,0.74 ,0.73 ,0.72 ,0.71 ,0.69 ,0.68 ,0.67 ,0.66 ,0.64 ,
0.63 ,0.62 ,0.6 ,0.59 ,0.57 ,0.56 ,0.54 ,0.53 ,0.52 ,0.5 ,0.48 ,0.47 ,0.45 ,0.44 ,0.42 ,0.41 ,0.39 ,
0.37 ,0.36 ,0.34 ,0.33 ,0.31 ,0.29 ,0.28 ,0.26 ,0.24 ,0.22 ,0.21 ,0.19 ,0.17 ,0.16 ,0.14 ,0.12 ,0.1 ,
0.09 ,0.07 ,0.05 ,0.03 ,0.02 ,0 ,-0.02 ,-0.03 ,-0.05 ,-0.07 ,-0.09 ,-0.1 ,-0.12 ,-0.14 ,-0.16 ,-0.17 ,
-0.19 ,-0.21 ,-0.22 ,-0.24 ,-0.26 ,-0.28 ,-0.29 ,-0.31 ,-0.33 ,-0.34 ,-0.36 ,-0.37 ,-0.39 ,-0.41 ,
-0.42 ,-0.44 ,-0.45 ,-0.47 ,-0.48 ,-0.5 ,-0.52 ,-0.53 ,-0.54 ,-0.56 ,-0.57 ,-0.59 ,-0.6 ,-0.62 ,
-0.63 ,-0.64 ,-0.66 ,-0.67 ,-0.68 ,-0.69 ,-0.71 ,-0.72 ,-0.73 ,-0.74 ,-0.75 ,-0.77 ,-0.78 ,-0.79 ,
-0.8 ,-0.81 ,-0.82 ,-0.83 ,-0.84 ,-0.85 ,-0.86 ,-0.87 ,-0.87 ,-0.88 ,-0.89 ,-0.9 ,-0.91 ,-0.91 ,
-0.92 ,-0.93 ,-0.93 ,-0.94 ,-0.95 ,-0.95 ,-0.96 ,-0.96 ,-0.97 ,-0.97 ,-0.97 ,-0.98 ,-0.98 ,-0.98 ,
-0.99 ,-0.99 ,-0.99 ,-0.99 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-0.99 ,-0.99 ,-0.99 ,-0.99 ,
-0.98 ,-0.98 ,-0.98 ,-0.97 ,-0.97 ,-0.97 ,-0.96 ,-0.96 ,-0.95 ,-0.95 ,-0.94 ,-0.93 ,-0.93 ,-0.92 ,
-0.91 ,-0.91 ,-0.9 ,-0.89 ,-0.88 ,-0.87 ,-0.87 ,-0.86 ,-0.85 ,-0.84 ,-0.83 ,-0.82 ,-0.81 ,-0.8 ,
-0.79 ,-0.78 ,-0.77 ,-0.75 ,-0.74 ,-0.73 ,-0.72 ,-0.71 ,-0.69 ,-0.68 ,-0.67 ,-0.66 ,-0.64 ,-0.63 ,
-0.62 ,-0.6 ,-0.59 ,-0.57 ,-0.56 ,-0.54 ,-0.53 ,-0.52 ,-0.5 ,-0.48 ,-0.47 ,-0.45 ,-0.44 ,-0.42 ,
-0.41 ,-0.39 ,-0.37 ,-0.36 ,-0.34 ,-0.33 ,-0.31 ,-0.29 ,-0.28 ,-0.26 ,-0.24 ,-0.22 ,-0.21 ,-0.19 ,
-0.17 ,-0.16 ,-0.14 ,-0.12 ,-0.1 ,-0.09 ,-0.07 ,-0.05 ,-0.03 ,-0.02 ,0 ,0.02 ,0.03 ,0.05 ,0.07 ,
0.09 ,0.1 ,0.12 ,0.14 ,0.16 ,0.17 ,0.19 ,0.21 ,0.22 ,0.24 ,0.26 ,0.28 ,0.29 ,0.31 ,0.33 ,0.34 ,0.36 ,
0.37 ,0.39 ,0.41 ,0.42 ,0.44 ,0.45 ,0.47 ,0.48 ,0.5 ,0.52 ,0.53 ,0.54 ,0.56 ,0.57 ,0.59 ,0.6 ,0.62 ,
0.63 ,0.64 ,0.66 ,0.67 ,0.68 ,0.69 ,0.71 ,0.72 ,0.73 ,0.74 ,0.75 ,0.77 ,0.78 ,0.79 ,0.8 ,0.81 ,0.82 ,
0.83 ,0.84 ,0.85 ,0.86 ,0.87 ,0.87 ,0.88 ,0.89 ,0.9 ,0.91 ,0.91 ,0.92 ,0.93 ,0.93 ,0.94 ,0.95 ,0.95 ,
0.96 ,0.96 ,0.97 ,0.97 ,0.97 ,0.98 ,0.98 ,0.98 ,0.99 ,0.99 ,0.99 ,0.99 ,1 ,1 ,1 ,1 ,1 ,1}
cosTable[0] = 1



ConstantData.ImageFillMethod = {
    -- 摘要:
    --     The Image will be filled Horizontally.
    Horizontal = 0,
    --
    -- 摘要:
    --     The Image will be filled Vertically.
    Vertical = 1,
    --
    -- 摘要:
    --     The Image will be filled Radially with the radial center in one of the corners.
    Radial90 = 2,
    --
    -- 摘要:
    --     The Image will be filled Radially with the radial center in one of the edges.
    Radial180 = 3,
    --
    -- 摘要:
    --     The Image will be filled Radially with the radial center at the center.
    Radial360 = 4,
}

ConstantData.ImageType = {
    -- 摘要:
    --     Displayes the full Image.
    Simple = 0,
    --
    -- 摘要:
    --     Displays the Image as a 9-sliced Image.
    Sliced = 1,
    --
    -- 摘要:
    --     Displays the Image Tiling the central part of the Sprite.
    Tiled = 2,
    --
    -- 摘要:
    --     Display portion of the Image.
    Filled = 3,
}

ConstantData.AnimatorCullMode = {
	AlwaysAnimate = 0,
    CullUpdateTransforms = 1,
    CullCompletely = 2,
}

-- 摘要:
-- Lightmap (and lighting) configuration mode, controls how lightmaps interact
-- with lighting and what kind of information they store.
ConstantData.LightmapsMode =
{
    --
    -- 摘要:
    --     Light intensity (no directional information), encoded as 1 lightmap.
    NonDirectional = 0,
    --
    -- 摘要:
    --     Directional information for direct light is combined with directional information
    --     for indirect light, encoded as 2 lightmaps.
    CombinedDirectional = 1,
    --
    -- 摘要:
    --     Directional information for direct light is stored separately from directional
    --     information for indirect light, encoded as 4 lightmaps.
    SeparateDirectional = 2,
}

-- 摘要:
--     Values for Camera.clearFlags, determining what to clear when rendering a
--     [[Camera]].
ConstantData.CameraClearFlags = 
{
    -- 摘要:
    --     Clear with the skybox.
    Skybox = 1,
    --
    -- 摘要:
    --     Clear with a background color.
    SolidColor = 2,
    Color = 2,
    --
    -- 摘要:
    --     Clear only the depth buffer.
    Depth = 3,
    --
    -- 摘要:
    --     Don't clear anything.
    Nothing = 4,
}

--顶级遮罩层级
ConstantData.TOP_MASK_LAYER = 999999

-- 忙碌状态类型
ConstantData.BUSY_TYPE_LOADING = 1		-- 加载中
ConstantData.BUSY_TYPE_NETWORK = 2		-- 网络通信中
ConstantData.BUSY_TYPE_TWEENNING = 3	-- 动效播放中
ConstantData.BUSY_TYPE_RECONNECT = 4    --重新建立连接

ConstantData.STAGE_DATA_MODULE_NAME = "stage_data_module_name" --关卡模块数据名
ConstantData.MODULE_NAME_FIGHT = "module_name_fight"

ConstantData.LOGIN_MUSIC = "login_music";

ConstantData.EQUIPMENT_TYPE=100   --装备的type


-- 需要预加载的UI预设
ConstantData.PRELOAD_UIPREFAB = 
{
	"bagitem",
	"EquipItem",
	"MainWindow",
}

ConstantData.LUA_TYPE_BYTE		= 1		-- 单字节
ConstantData.LUA_TYPE_SHORT		= 2		-- 短整型
ConstantData.LUA_TYPE_INT		= 3		-- 整型
ConstantData.LUA_TYPE_FLOAT		= 4		-- 浮点型
ConstantData.LUA_TYPE_STRING	= 5		-- 字符串
-- 无符号
ConstantData.LUA_TYPE_BYTE_UNSIGNED = 6


ConstantData.GAME_RESET_TIME	= {hour = 6, min = 0, sec = 0}		-- 游戏每日重置时间

ConstantData.EventDefine = 
{
    RemoveResource = 0,
    RemoveResourceImmediately = 1,
    WORLD_CLICK = 2,
    WORLD_DOWN = 3,
    WORLD_UP = 4,
    WORLD_DRAG = 5,
    GLO_CLICK = 6,
    UI_LISTENER = 7,
    UI_CLICK = 8,
    CHANGE_SCENE = 9,
    TIME_SCALE_CHANGED = 10,
    CancelLoad = 11,
    UPDATE_TIPS = 12,
    UPDATE_PROGRESS = 13,
    UPDATE_VERSION = 14,
    UPDATE_NEXT = 15,
    SHOW_UPDATE_TIPS = 16,
    CONTINUE_TO_UPDATE = 17,
    OFF_LINE_TRY = 18,
    REFRESH_WIFI = 19,
    YANZHENG_CON_ERROR = 20,
    LOAD_SERVER_XML_CON_ERROR = 21,
    LOAD_ALL_SERVER_XML_CON_ERROR = 22,
}