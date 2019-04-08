-- region
-- Date    : 2016-06-20
-- Author  : daiyaorong
-- Description :  角色常量
-- endregion

CharacterConstant = {}

-- 阵营类型
CharacterConstant.CAMP_TYPE_NEUTRAL 	= 0 --中立默认
CharacterConstant.CAMP_TYPE_MYSELF 		= 1 --我方默认
CharacterConstant.CAMP_TYPE_MONSTER 	= 2 --怪物默认
CharacterConstant.CAMP_TYPE_TEAMGUIDE   = 3 --组队副本被守护怪物默认

-- 阵营关系类型
CharacterConstant.RELATION_FRIEND		= 0 	-- 友好
CharacterConstant.RELATION_PEACE		= 1 	-- 中立
CharacterConstant.RELATION_ENEMY		= 2 	-- 敌对

-- 角色类型
CharacterConstant.TYPE_SELF 	= 0  --主角
CharacterConstant.TYPE_PLAYER 	= 1  --玩家
CharacterConstant.TYPE_MONSTER 	= 2  --怪物
CharacterConstant.TYPE_NPC 	= 3  --Npc
CharacterConstant.TYPE_PARTNER 	= 4  --伙伴
CharacterConstant.TYPE_ROBOT	= 5	 -- 机器人（相当于PLAYER）
CharacterConstant.TYPE_BABY     = 6  --宝宝

CharacterConstant.CAMP_DEFAULTMAP = {
    [CharacterConstant.TYPE_SELF]       = CharacterConstant.CAMP_TYPE_MYSELF,
    [CharacterConstant.TYPE_PLAYER]     = CharacterConstant.CAMP_TYPE_MYSELF,
    [CharacterConstant.TYPE_MONSTER]    = CharacterConstant.CAMP_TYPE_MONSTER ,
    [CharacterConstant.TYPE_NPC]        = CharacterConstant.CAMP_TYPE_NEUTRAL,
    [CharacterConstant.TYPE_PARTNER]    = CharacterConstant.CAMP_TYPE_MYSELF,
	[CharacterConstant.TYPE_ROBOT]		= CharacterConstant.CAMP_TYPE_MYSELF,
    [CharacterConstant.TYPE_BABY]    = CharacterConstant.CAMP_TYPE_MYSELF,
}

-- 动作名
CharacterConstant.ANIMATOR_IDLE  = "bidle"
CharacterConstant.ANIMATOR_MOVE  = "walk"
CharacterConstant.ANIMATOR_HURT	 = "hurt"
CharacterConstant.ANIMATOR_FLY   = "fly"
CharacterConstant.ANIMATOR_STAND = "standup"
CharacterConstant.ANIMATOR_DEAD  = "dead"
CharacterConstant.ANIMATOR_DIE 	 = "die" --倒地死亡
CharacterConstant.ANIMATOR_AVOID = "avoidskill"
CharacterConstant.ANIMATOR_ULTI  = "ultskill"
CharacterConstant.ANIMATOR_BORN  = "appear"
CharacterConstant.ANIMATOR_ACTIVE= "awake"

-- 状态
CharacterConstant.STATE_IDLE     = 1
CharacterConstant.STATE_HITBACK  = 3
CharacterConstant.STATE_HITFLY	 = 4
CharacterConstant.STATE_STANDUP  = 5
CharacterConstant.STATE_DEAD     = 8
CharacterConstant.STATE_ATTACK   = 9	--普攻
CharacterConstant.STATE_RUN      = 11   --跑动

CharacterConstant.STATE_ANIMATOR = {
	[CharacterConstant.STATE_HITBACK] = CharacterConstant.ANIMATOR_HURT,
	[CharacterConstant.STATE_HITFLY] = CharacterConstant.ANIMATOR_FLY,
}

-- 技能状态
CharacterConstant.SKILLSTATE_NONE 	= 0
CharacterConstant.SKILLSTATE_NORMAL = 1
CharacterConstant.SKILLSTATE_SKILL 	= 2
CharacterConstant.SKILLSTATE_AVOID 	= 3
CharacterConstant.SKILLSTATE_ULTI	= 4
CharacterConstant.SKILLSTATE_BORN   = 5
CharacterConstant.SKILLSTATE_ACTIVE = 6

--寻路状态
CharacterConstant.FINDPATH_STATE_SUCCESS = 1   --寻路成功
CharacterConstant.FINDPATH_STATE_FAIL    = 2   --寻路失败

-- 技能动作
CharacterConstant.ATTACK_ANI = "attack"
CharacterConstant.SKILL_ANI = "skill"

CharacterConstant.RUN_PRECISION = 0.2
CharacterConstant.RUN_RRECISIONSQUARE = 0.04
CharacterConstant.DEGREE_PRECISION = 5
CharacterConstant.SQR_RUN_PRECISION = CharacterConstant.RUN_PRECISION * CharacterConstant.RUN_PRECISION

CharacterConstant.DEAD_REMOVEFRAME = 45     --延迟销毁帧数

-- 怪物类型
CharacterConstant.MONSTER_TYPE = 
{
	NORMAL = 0,			-- 普通小怪
	BOSS = 1,			-- BOSS
	ELITE = 2,			-- 精英
    PARTNER=10,         -- 伙伴
}

-- 角色能力列表
CharacterConstant.ABILITY = 
{
	MOVE = 1,				-- 移动能力
	NORMAL_SKILL = 2,		-- 使用普通攻击的能力
	SKILL = 3,				-- 使用技能的能力
}

-- NPC显示条件
CharacterConstant.NPC_DISPLAY = 
{
	NONE = "0",
	TASK = "mission",
	LEVEL = "lv",
}

CharacterConstant.BEGIN_SKILL_CD=0 --开始技能cd
CharacterConstant.CLEAR_SKILL_CD=1 --清除技能cd

--通用特效
CharacterConstant.EXPUP_LIGHT = "ueff_expup_light"


CharacterConstant.BONE_LEFT_WEAPON = "Weapon_Point00_L" --左手武器
CharacterConstant.BONE_RIGHT_WEAPON = "Weapon_Point00_R"  --右手武器  R_Weapon_Point00   Weapon_Point00_R

CharacterConstant.delayType = 
{
    NONE    = 0 , -- 没有在加载模型
    RIDE    = 1 , -- 坐骑
    FASHION = 2 , -- 时装
    WEAPON  = 3 , -- 武器
    MODEL = 4, -- 初始模型
}

CharacterConstant.modelFacadeType = 
{
    RIDE_BASE = 0,--坐骑基本模型
    BASE = 1, -- 基本模型
}