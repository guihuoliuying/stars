-- region
-- Date    : 2016-06-14
-- Author  : daiyaorong
-- Description :  战斗控制器
-- endregion

local framTimeIns = nil
local frameKey = nil
local battleTime = nil		--当前战斗时间
local currentFrame = nil	--当前帧

local fightState = nil --FightDefine.FIGHT_STATE_NONE
local attrCheckKey = nil
local syncAttrKey = nil
local canRecordAuto = nil --是否可记录自动战斗
local isMeAutoFight = nil --是否处于自动战斗

FightControl = {}

local function tickTime()
	-- body
	battleTime = battleTime + ConstantData.FRAME_DELTA_TIME
	currentFrame = currentFrame + 1
end


local function onFrame()
	-- body
	tickTime()
	AIManager.update()
	SkillManager:update()
	PressureDispatcher.update()
	FixedTimeDispatcher.update()
	SideEffectManager:update()
	MonsterSpawnManager:update()
	BuffManager:update()
	RuneSpawnManager:update()
	if EnvironmentHandler.isPvpClient then
		SyncFrequence.update()
		SyncSkillEffectHandler.update()
		--[[ DEBUGSTART
		if currentFrame % 60 == 0 then
			local ttl,max,avg = ModuleEvent.debugBufferInfo()
			--print("client recv bytes info{total=" .. ttl .. ",max=" .. max .. ",avr=" .. avg .. "}")
		end
		-- DEBUGEND]]
	end
end

local syncInfoTable = nil
local function syncMyselfByTiming(evtInfo)
	local myCharac = CharacterManager:getMyCharac()
	if myCharac == nil then return end
	if myCharac.state == CharacterConstant.STATE_IDLE and myCharac:isJoystickMoving() == false then
		return --处于待机 无摇杆移动时无需同步
	end
	if myCharac.state == CharacterConstant.STATE_DEAD then
		return --死亡时无需同步
	end
	if syncInfoTable == nil then
		syncInfoTable = {}
	end
	syncInfoTable[1] = myCharac.uniqueID
	local myPos = myCharac:getPosition()
	local myRot = myCharac:getRotation()
	syncInfoTable[2] = math.round(myPos.x, 2)
	syncInfoTable[3] = math.round(myPos.z, 2)
	syncInfoTable[4] = math.round(myRot:ToEulerAngles().y, 0)
	syncInfoTable[5] = math.round(Time.realtimeSinceStartup * 1000, 0)
	ModuleEvent.dispatch(ModuleConstant.PLAYER_TIMING_SYNC, syncInfoTable)
end

local function initManager()
	-- body
	AIManager.init()
	MonsterAwakeControl.init()
	SkillManager:init()
	PressureDispatcher.init()
	FixedTimeDispatcher.init()
	SideEffectManager:init()
	FightNumberManager2.init()
	DropCtrl:init()
	DynamicBlockManager:init()
	if EnvironmentHandler.isPvpClient then
		SkillDelayList.init()
		SyncFrequence.init()
		SyncSkillEffectHandler.init()
		SyncFrequence.addTimeoutCallBack(ModuleConstant.PLAYER_TIMING_SYNC, syncMyselfByTiming)
	end
end

-- 查看自动战斗记录
local function initAutoFightFlag()
	-- body
	canRecordAuto = StageManager.canRecordAuto()
	isMeAutoFight = false
	if canRecordAuto then
		local flag = LuaHelper.GetString(FightDefine.AUTOFIGHT_NAME..RoleData.roleId)
		if flag and flag == "1" then
			isMeAutoFight = true
		end 
	end
end

-- 保存自动战斗记录
local function saveAutoFightFlag()
	-- body
	if canRecordAuto then
		local flag = (isMeAutoFight == true) and 1 or 0
		LuaHelper.SetString(FightDefine.AUTOFIGHT_NAME..RoleData.roleId, flag)
	end
	canRecordAuto = nil
	isMeAutoFight = nil
end

local function onFightReadyGo()
	MonsterSpawnManager:start();
	if EnvironmentHandler.isPvpClient then
		UnHandleInfoBeforeLoaded.init()
	end
	ModuleEvent.dispatch(ModuleConstant.JOYSTICK_SHOW_HIDE,true)
	-- 非强同步检测属性
	if EnvironmentHandler.isPvpClient == false then
		local function checkFunc()
			FightModel:roleAttrCheck()
		end
		attrCheckKey = FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(150,0,checkFunc)
	end
	-- 主线关卡实时同步hp
	if FightModel:getFightStageType() == ConstantData.STAGE_TYPE_DUNGEON then
		local function syncFunc()
			FightModel:syncRoleAttr()
		end
		syncAttrKey = FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(30,0,syncFunc)
	end
	EnvironmentHandler.updateNetworkDelay(0)
end

local function addListener()
	ModuleEvent.addListener(ModuleConstant.FIGHT_READY_GO, onFightReadyGo)
end

local function removeListener()
	ModuleEvent.removeListener(ModuleConstant.FIGHT_READY_GO, onFightReadyGo)
end

-- 向服务端发送准备标记
local function sendReadyFlag()
	local conn = GameNet.GetSocket()
	conn:WriteProtocol(PacketType.ServerPK_Ready)
	local stageType = FightModel:getFightStageType()
	conn:WriteInt(stageType)
	conn:SendData()
	-- print("-----------------------------------------ready flag")
end

local function stopAttrCheck()
	-- body
	if attrCheckKey ~= nil then
		FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):removeLuaFunc( attrCheckKey )
		attrCheckKey = nil
	end
end

local function stopSyncAttr()
	-- body
	if syncAttrKey then
		FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):removeLuaFunc( syncAttrKey )
		syncAttrKey = nil
	end
end

function FightControl.init()
	addListener();
	initManager()
	initAutoFightFlag()
	--不是在剧情锁定战斗的情况下才显示ui
	if StoryController.GetIsLockFight()~=true then
		if MainPanel.uiMonsterBlood then
            MainPanel.uiMonsterBlood.gameObject:SetActive(true)
        end
		ModuleEvent.dispatch(ModuleConstant.STAGE_FIGHT_WINDOW, ModuleConstant.UI_OPEN)
	end
	frameKey = framTimeIns:RegisterLuaCallBack(1,0,onFrame)
	-- PVP中一如果是要准备的，要向服务端发一个准备好的消息
	if fightState == FightDefine.FIGHT_STATE_READY and EnvironmentHandler.isPvpClient then
		sendReadyFlag()
	end
	if EnvironmentHandler.isPvpClient then
		ModuleEvent.dispatch(ModuleConstant.CLIENT_LOADINGEND, {RoleData.roleId}) --通知ServerLua完成loading
	end
end

function FightControl.onDataReady()
	-- body
	framTimeIns = FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA)
	battleTime = 0
	currentFrame = 0
	local stageVo = FightModel:getFightSceneVo()
	ModuleEvent.dispatch(ModuleConstant.ENTER_STAGE, {stageId = stageVo.stageid, stageType = stageVo.stagetype})
	BuffManager:init()
	MonsterSpawnManager:init()
	RuneSpawnManager:init()
end

-- 设置战斗状态
function FightControl.setFightState(newState)
	if fightState == newState then
		return
	end
	fightState = newState
	local myCharac = CharacterManager:getMyCharac()
	if myCharac then
		local enableAbility = (fightState == FightDefine.FIGHT_STATE_FIGHTING) and 1 or 0
		-- 是否可触发技能
		myCharac:setAbilityEnable(CharacterConstant.ABILITY.NORMAL_SKILL, enableAbility)
		myCharac:setAbilityEnable(CharacterConstant.ABILITY.SKILL, enableAbility)
		-- 是否可移动
		if FightModel:getFightStageType() == ConstantData.STAGE_TYPE_FIGHTINGMASTER or FightModel:getFightStageType() == ConstantData.STAGE_TYPE_FAMILY_TRANSPORT_PVP then
			Joystick:enableJoystick( (enableAbility ~= 0) )
		end
	end
end

-- 设置自动战斗标记
function FightControl.setAutoFightFlag( value )
	-- body
	isMeAutoFight = value
end

function FightControl.getAutoFightFlag()
	-- body
	return isMeAutoFight
end

function FightControl.getFightTime()
	-- body
	return battleTime
end

function FightControl.stopFight(isWin)
	stopAttrCheck()
	stopSyncAttr()
	AIManager.disableAllAI()
	BuffManager.clear()
	SkillManager:enableSkillEffect(false)
	MonsterSpawnManager:clear()
	PressureDispatcher.clear()
	StageManager.stopStageMusic()  --背景音乐干掉
	if isWin and EnvironmentHandler.environmentType == EnvironmentDef.DEFAULT 
	and (StageManager.getCurStageType() == ConstantData.STAGE_TYPE_DUNGEON
	or StageManager.getCurStageType() == ConstantData.STAGE_TYPE_NEWGUIDE
	or StageManager.getCurStageType() == ConstantData.STAGE_TYPE_POEM) then
		local monsters = CharacterManager:getCharacByCamp(CharacterConstant.CAMP_TYPE_MONSTER)
		if monsters then
			for _,m in pairs(monsters) do
				if m.istrap == nil or m.istrap ~= 1 then
					m:changeProperty(ConstantData.ROLE_CONST_HP, 0)
				end
			end
		end
	end
end

-- 副本倒计时结束
function FightControl.timeIsUp()
	-- body
	stopAttrCheck()
	stopSyncAttr()
	AIManager.disableAllAI()
	BuffManager.clear()
	SkillManager:enableSkillEffect(false)
	MonsterSpawnManager:clear()
	PressureDispatcher.clear()
end

function FightControl.dispose()
	--FightControl.setFightState(FightDefine.FIGHT_STATE_END)
	--[[ DEBUGSTART
	ModuleEvent.debugClearBufferInfo()
	-- DEBUGEND]]
	stopAttrCheck()
	stopSyncAttr()
	saveAutoFightFlag()
	Joystick.disableFrames = 0
	removeListener();
	-- body
	AIManager.dispose()
    MonsterAwakeControl.clear()
	if framTimeIns then
		if frameKey then
			framTimeIns:removeLuaFunc(frameKey)
		end
		framTimeIns:dispose()
		frameKey = nil
	end
	SideEffectManager:clear()
	PressureDispatcher.clear()
	FixedTimeDispatcher.clear()
	if StageManager.isStageReady() then
		SkillManager:dispose()
	end
	MonsterSpawnManager:clear()
	DynamicBlockManager:clear()
	FightNumberManager2.clear()
	DropCtrl:dispose()
	BuffManager:clear()
	RuneSpawnManager:clear()
	MonsterBlood.ClearPool()
	UnHandleInfoBeforeLoaded.clear()
	if EnvironmentHandler.isPvpClient then
		SkillDelayList.dispose()
		SyncFrequence.clear()
		SyncSkillEffectHandler.clear()
	end
end

function FightControl.getCurrentFrame()
	return currentFrame;
end

function FightControl.getFightState()
	return fightState
end