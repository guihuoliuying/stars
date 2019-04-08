-- region
-- Date    : 2016-08-18
-- Author  : daiyaorong
-- Description :  事件转发器
-- endregion

EventHandler = {}

-- 直接执行
local EXE = 1
-- 指令同步到等待队列 一般服务端环境使用
local TOQ = 2
-- 实时同步至服务端 一般客户端环境使用
local SYN = 3
-- 弱同步指令 弱同步环境下使用
local WSYN = 4

-- 根据环境选择不同的响应的策略
-- 默认直接执行
EventHandler.TABLE = {
	-- 点击技能按钮
	[ModuleConstant.SKILL_CLICKFIRE] = 
	{ 
		[EnvironmentDef.PVP_CLIENT]={SYN},
	},
	-- 释放技能
	[ModuleConstant.CHARAC_FIREREQUEST] = 
	{ 
		[EnvironmentDef.PVE_WEAKSYNC]={EXE,WSYN},
        [EnvironmentDef.PVP_CLIENT]={}, 
		[EnvironmentDef.PVP_SERVER]={EXE,TOQ},
	},
	-- 更新角色属性
	[ModuleConstant.CHARAC_UPDATE_ATTR] = 
	{ 
		[EnvironmentDef.PVP_CLIENT]={}, 
		[EnvironmentDef.PVP_SERVER]={EXE,TOQ},
	},
	-- 技能命中表现效果
	[ModuleConstant.SKILL_SHOWEFFECT] = 
	{ 
		[EnvironmentDef.PVP_CLIENT]={}, 
		[EnvironmentDef.PVP_SERVER]={TOQ},
	},
	-- 摇杆开始移动
	[ModuleConstant.JOYSTICK_MOVE] = 
	{ 
		[EnvironmentDef.PVE_WEAKSYNC]={EXE,WSYN},
		[EnvironmentDef.PVP_CLIENT]={EXE,SYN}, 
		[EnvironmentDef.PVP_SERVER]={TOQ},
	},
	-- 摇杆停止移动
	[ModuleConstant.JOYSTICK_END] = 
	{ 
		[EnvironmentDef.PVE_WEAKSYNC]={EXE,WSYN},
		[EnvironmentDef.PVP_CLIENT]={EXE,SYN}, 
		[EnvironmentDef.PVP_SERVER]={TOQ},
	},
	-- AI控制移动
	[ModuleConstant.AI_ACTION_MOVE] = 
	{
		[EnvironmentDef.PVE_WEAKSYNC] = {EXE,WSYN},
		[EnvironmentDef.PVP_CLIENT] = {},
		[EnvironmentDef.PVP_SERVER] = {EXE,TOQ},
	},
	-- 飘字
	[ModuleConstant.SHOW_FIGHT_NUMBER] = 
	{
		[EnvironmentDef.PVP_CLIENT] = {EXE},
		[EnvironmentDef.PVP_SERVER] = {TOQ},
	},
	-- 添加BUFF
	[ModuleConstant.ADD_BUFF] = 
	{
		[EnvironmentDef.PVP_CLIENT] = {},
		[EnvironmentDef.PVP_SERVER] = {EXE, TOQ},
	},
	[ModuleConstant.REMOVE_BUFF] = 
	{
		[EnvironmentDef.PVP_CLIENT] = {EXE},
		[EnvironmentDef.PVP_SERVER] = {EXE, TOQ},
	},
	-- 调试用，服务端LUA向客户端输出LOG
	[ModuleConstant.SERVER_TO_CLIENT_LOG] = 
	{
		[EnvironmentDef.PVE_WEAKSYNC] = {},
		[EnvironmentDef.PVP_CLIENT] = {EXE},
		[EnvironmentDef.PVP_SERVER] = {TOQ},
	},
	-- 定时同步包
	[ModuleConstant.PLAYER_TIMING_SYNC] = 
	{
		[EnvironmentDef.PVP_CLIENT] = {SYN},
		[EnvironmentDef.PVP_SERVER] = {TOQ},
	},
	-- 开启/关闭主角AI
	[ModuleConstant.PLAYER_SWITCH_AI] = 
	{
		[EnvironmentDef.PVP_CLIENT] = {SYN},
		[EnvironmentDef.PVP_SERVER] = {EXE, TOQ},
	},
	-- 服务端通知客户端战斗开始
	[ModuleConstant.PVP_FIGHT_START] = 
	{
		[EnvironmentDef.PVP_CLIENT] = {EXE},
		[EnvironmentDef.PVP_SERVER] = {TOQ},
	},
	-- 战斗准备完毕，服务端由Java通知，所以这里留空
	[ModuleConstant.PVP_ALL_READY] = 
	{
		[EnvironmentDef.PVP_CLIENT] = {EXE},
		[EnvironmentDef.PVP_SERVER] = {TOQ},
	},
	-- 客户端loading完毕，通知Serverlua，主要用于ServerOrder同步
	[ModuleConstant.CLIENT_LOADINGEND] = 
	{
		[EnvironmentDef.PVP_CLIENT] = {SYN},
	},
	-- 服务端lua通知战斗状态改变
	[ModuleConstant.CLIENT_SYNC_FIGHTSTATE] = 
	{
		[EnvironmentDef.PVP_CLIENT]={EXE}, 
		[EnvironmentDef.PVP_SERVER] = {TOQ},
	},
	-- 添加神符刷新点
	[ModuleConstant.RUNESPAWNPOINT_CREATE] = 
	{
		[EnvironmentDef.PVP_CLIENT]={}, 
		[EnvironmentDef.PVP_SERVER] = {EXE, TOQ},
	},
	-- 移除神符刷新点
	[ModuleConstant.RUNESPAWNPOINT_REMOVE] = 
	{
		[EnvironmentDef.PVP_CLIENT]={}, 
		[EnvironmentDef.PVP_SERVER] = {EXE, TOQ},
	},
	-- 创建神符
	[ModuleConstant.RUNE_CREATE] = 
	{
		[EnvironmentDef.PVP_CLIENT]={}, 
		[EnvironmentDef.PVP_SERVER] = {EXE, TOQ},
	},
	-- 移除神符
	[ModuleConstant.RUNE_PICKUP] = 
	{
		[EnvironmentDef.PVP_CLIENT]={}, 
		[EnvironmentDef.PVP_SERVER] = {EXE, TOQ},
	},
	-- 刷怪
	[ModuleConstant.SPAWN_MONSTER] = 
	{
		[EnvironmentDef.PVP_CLIENT]={}, 
		[EnvironmentDef.PVP_SERVER] = {TOQ},
	},
	-- 龙珠状态
	[ModuleConstant.DRAGONBALL_SETVISIBLE] = 
	{
		[EnvironmentDef.PVP_CLIENT]={}, 
		[EnvironmentDef.PVP_SERVER] = {TOQ},
	},
	--修改加锁
    [ModuleConstant.CAMPDAILY_UNLOCK_PASSIVESKILL] =
    {
        [EnvironmentDef.PVP_CLIENT]={SYN},
    },
    --修改角色技能
    [ModuleConstant.CAMPDAILY_CHANGE_SKILL] =
    {
        [EnvironmentDef.PVP_CLIENT]={SYN},
    },
}

-- 不同技能同步类型要覆盖掉默认的配置
do
	if EnvironmentHandler.getSkillSyncType() ~= EnvironmentDef.SKILL_SYNC_STRICT then
		EventHandler.TABLE[ModuleConstant.SKILL_CLICKFIRE] = 
		{
			[EnvironmentDef.PVP_CLIENT]={EXE, SYN},
		}
		EventHandler.TABLE[ModuleConstant.CHARAC_FIREREQUEST] = 
		{ 
			[EnvironmentDef.PVE_WEAKSYNC]={EXE,WSYN},
			[EnvironmentDef.PVP_CLIENT]={EXE}, 
			[EnvironmentDef.PVP_SERVER]={EXE,TOQ} 
		}
	end
	if EnvironmentHandler.getSkillSyncType() == EnvironmentDef.SKILL_SYNC_LOOSE then
		EventHandler.TABLE[ModuleConstant.SKILL_SHOWEFFECT] = 
		{ 
			[EnvironmentDef.PVP_CLIENT]={EXE}, 
			[EnvironmentDef.PVP_SERVER]={} 
		}
		EventHandler.TABLE[ModuleConstant.SHOW_FIGHT_NUMBER] = {
			[EnvironmentDef.PVP_CLIENT] = {EXE},
			[EnvironmentDef.PVP_SERVER] = {} 
		}
	end
end

local STR = ConstantData.LUA_TYPE_STRING
local INT = ConstantData.LUA_TYPE_INT
local BYTE = ConstantData.LUA_TYPE_BYTE
local SHORT = ConstantData.LUA_TYPE_SHORT
local FLOAT = ConstantData.LUA_TYPE_FLOAT
local UNSIGNBYTE = ConstantData.LUA_TYPE_BYTE_UNSIGNED
-- 事件参数类型定义
EventHandler.EVENT_PARAMTYPES = 
{
	-- 点击技能按钮，参数：{角色ID, 技能类型, 技能ID, 技能等级, 位置x, 位置z}
	[ModuleConstant.SKILL_CLICKFIRE] = {STR, BYTE, INT, UNSIGNBYTE, FLOAT, FLOAT},
	-- 释放技能, 参数：{角色ID, 技能ID, 技能等级, 目标ID, 位置x, 位置z, 技能序号, 是否由AI触发}
	[ModuleConstant.CHARAC_FIREREQUEST] = {STR, INT, UNSIGNBYTE, STR, FLOAT, FLOAT, SHORT, BYTE},
	-- 更新角色属性，参数：{角色ID, 属性名, 属性值}
	[ModuleConstant.CHARAC_UPDATE_ATTR] = {STR, STR, INT},
	-- 技能命中表现效果，参数：{攻击者ID, 受击者ID, 技能ID, 打击帧, 伤害值, 是否暴击, 打击位置x, 打击位置y, 打击位置z, 旋转y, 技能序号, 技能开始至今经过的帧数, 飘字类型, 打击等级, 打击序号,时候会心一击}
	[ModuleConstant.SKILL_SHOWEFFECT] = {STR, STR, INT, BYTE, INT, BYTE, FLOAT, FLOAT, FLOAT, SHORT, SHORT, SHORT, BYTE, BYTE, BYTE,BYTE},
	-- 摇杆开始移动， 参数：{角色ID, 朝向, 位置x, 位置z}
	[ModuleConstant.JOYSTICK_MOVE] = {STR, SHORT, FLOAT, FLOAT},
	-- 摇杆停止移动，参数：{角色ID, 朝向, 位置x, 位置z}
	[ModuleConstant.JOYSTICK_END] = {STR, SHORT, FLOAT, FLOAT},
	-- AI控制移动，参数：{角色ID, 位置x, 位置y, 位置z, 朝向角色ID}
	[ModuleConstant.AI_ACTION_MOVE] = {STR, FLOAT, FLOAT, FLOAT, STR},
	-- 飘字，参数：{受击者ID, 伤害类型, 伤害值, 字体类型, 是否显示正负号, 攻击者ID, 技能序号, 延迟帧数}
	[ModuleConstant.SHOW_FIGHT_NUMBER] = {STR, BYTE, INT, BYTE, BYTE, STR, SHORT, SHORT},
	-- 切换AI，参数：{角色ID, 是否开启AI}
	[ModuleConstant.PLAYER_SWITCH_AI] = {STR, BYTE},
	-- 添加BUFF，参数：{攻击者ID, 受击者ID, buffID, buff等级, buff实例ID}
	[ModuleConstant.ADD_BUFF] = {STR, STR, INT, UNSIGNBYTE, STR},
	-- 移除BUFF, 参数：{BUFF所有者ID，BUFF唯一ID}
	[ModuleConstant.REMOVE_BUFF] = {STR, STR},
	-- 调试用，服务端LUA向客户端输出LOG，参数：{log内容}
	[ModuleConstant.SERVER_TO_CLIENT_LOG] = {STR},
	-- 客户端发出的定时同步包，参数：{角色ID, 位置x, 位置z, 朝向y, 时间}
	[ModuleConstant.PLAYER_TIMING_SYNC] = {STR, FLOAT, FLOAT, SHORT, INT},
	-- 服务端发出的定时同步包，参数：{是否强制更新, 位置x, 位置z, 是否自动战斗，剩余血量}
	[ModuleConstant.SERVER_SYNC_PLAYERINFO] = {BYTE, FLOAT, FLOAT, BYTE, INT},
	-- 服务端通知客户端战斗开始, 参数：{}
	[ModuleConstant.PVP_FIGHT_START] = {},
	-- 战斗准备就绪，参数：{}
	[ModuleConstant.PVP_ALL_READY] = {},
	-- ClientLua完成loading，参数：{角色ID}
	[ModuleConstant.CLIENT_LOADINGEND] = {STR},
	-- 服务端Lua通知战斗状态变化， 参数：{战斗状态, 剩余时间}
	[ModuleConstant.CLIENT_SYNC_FIGHTSTATE] = {BYTE, INT},
	-- 创建神符刷新点, 参数：{刷新点ID, 刷新点序号}
	[ModuleConstant.RUNESPAWNPOINT_CREATE] = {INT, SHORT},
	-- 移除神符刷新点，参数：{刷新点ID, 刷新点序号}
	[ModuleConstant.RUNESPAWNPOINT_REMOVE] = {INT, SHORT},
	-- 创建神符，参数：{刷新点ID, 神符ID, 实例ID}
	[ModuleConstant.RUNE_CREATE] = {INT, INT, SHORT},
	-- 拾取神符，参数：{刷新点ID, 神符ID, 角色ID, 刷新点序号}
	[ModuleConstant.RUNE_PICKUP] = {INT, INT, STR, SHORT},
	-- 通知刷怪，参数：{怪物唯一ID}
	[ModuleConstant.SPAWN_MONSTER] = {STR},
	-- 修改龙珠显示状态 参数: {角色ID，技能ID，状态值}
	[ModuleConstant.DRAGONBALL_SETVISIBLE] = {STR, INT, BYTE},
    -- 开启被锁着的被动技能{角色ID, 技能ID}
    [ModuleConstant.CAMPDAILY_UNLOCK_PASSIVESKILL] = {STR,INT},
    -- 更改當前技能(角色ID，按鍵位置（1普通，2技能），技能ID，技能level)
    [ModuleConstant.CAMPDAILY_CHANGE_SKILL] = {STR,INT,INT,INT},
}

EventHandler.IGNORE_LOADING_ORDERS = 
{
	[ModuleConstant.ADD_BUFF] = true,
	[ModuleConstant.CHARAC_UPDATE_ATTR] = true,
	[ModuleConstant.RUNESPAWNPOINT_CREATE] = true,
	[ModuleConstant.RUNESPAWNPOINT_REMOVE] = true,
	[ModuleConstant.RUNE_CREATE] = true,
	[ModuleConstant.RUNE_PICKUP] = true,
	[ModuleConstant.SPAWN_MONSTER] = true,
}

local tempTable = nil
function EventHandler.getMethod( eventID )
	-- envType = EnvironmentHandler.environmentType
	-- if EventHandler.TABLE[eventID] ~= nil and EventHandler.TABLE[eventID][envType] then
	-- 	return EventHandler.TABLE[eventID][envType]
	-- end
	-- return nil

	--优化后方法--by Jim
	tempTable = EventHandler.TABLE[eventID]
	if tempTable ~= nil then
		return tempTable[EnvironmentHandler.environmentType]
	end
	return nil
end

function EventHandler.getEventParamTypes(eventID)
	return EventHandler.EVENT_PARAMTYPES[eventID]
end

function EventHandler.isIgnoreLoading(eventID)
	if EventHandler.IGNORE_LOADING_ORDERS[eventID] then
		return true
	end
	return false
end