--region AiManager.lua
--Date 2016/09/20
--Author zhouxiaogang
--Desc AI管理器
--endregion

AIManager = {}

local enableAI		= true		-- 是否启用AI

local aiDic			= nil		-- AI列表（uid -> AI）
local typeDic		= nil		-- AI按characterType分类(charaType -> {uid -> AI})


local AIClasses = 
{
	[CharacterConstant.TYPE_SELF] = PlayerAI,
	[CharacterConstant.TYPE_MONSTER] = MonsterAI,
	[CharacterConstant.TYPE_PARTNER] = PartnerAI,
	[CharacterConstant.TYPE_PLAYER] = PlayerAI,
}
local tempVector = Vector3.zero
local updateCD = 0

-- 创建AI
local function onCreateAI(uid)
	local chara = CharacterManager:getCharacByUId(uid)
	if chara == nil then
		return
	end
	local charaType = chara.characterType
	if AIClasses[charaType] == nil then
		return
	end
	local ai = AIClasses[charaType]:create()
	if typeDic[charaType] == nil then
		typeDic[charaType] = {}
	end
	typeDic[charaType][uid] = ai
	aiDic[uid] = ai
	ai:init(chara)
	if enableAI then
		ai:start(true)
	end
end

-- 销毁指定AI
local function onDestroyAI(uid, charaType)
	local ai = aiDic[uid]
	if ai == nil then return end
	aiDic[uid] = nil
	if typeDic[charaType] then
		typeDic[charaType][uid] = nil
	end
	ai:dispose()
end

-- 激活指定AI
local function onAwakeAI(uid)
	if aiDic[uid] == nil then
		return
	end
	aiDic[uid]:awake()
end

-- 开始指定AI
local function onStartAI(uid)
	if enableAI==false then
		return
	end
	if aiDic[uid] == nil then
		return
	end
	aiDic[uid]:start()
end

-- 停止指定AI
local function onStopAI(uid)
	if aiDic[uid] == nil then
		return
	end
	aiDic[uid]:stop()
end

-- 清除指定AI的回合计时
local function onClearRoundTime(uid)
	if aiDic[uid] == nil then
		return
	end
	aiDic[uid]:clearRoundTick()
end

local function onChangeAIProperty(uid, key, val)
	if aiDic[uid] == nil then
		return
	end
	aiDic[uid]:changeProperty(key, val)
end

local function onBreakState(uid, opType, skillState)
	if aiDic == nil or aiDic[uid] == nil then
		return
	end
	if opType == 1 then
		aiDic[uid]:breakState()
	elseif opType == 2 then
		aiDic[uid]:endAISkill(skillState)
	elseif opType == 3 then
		aiDic[uid]:stopTime()
	end
end

local function doMoveAction(args)
	local uid = args[1]
	local chara = CharacterManager:getCharacByUId(uid)
	if chara == nil then return end
	local pos = Vector3.New(args[2], args[3], args[4])
	local faceToCharaId = args[5]
	if faceToCharaId and faceToCharaId ~= "0" then
		chara:moveTo(pos, function() 
			local faceChara = CharacterManager:getCharacByUId(faceToCharaId)
			if faceChara == nil then return end
			local targetPos = faceChara:getPosition()
			local charaPos = chara:getPosition()
			tempVector.x = targetPos.x - charaPos.x
			tempVector.y = targetPos.y - charaPos.y
			tempVector.z = targetPos.z - charaPos.z
			chara:setRotation(Quaternion.LookRotation(tempVector))
		end)
	else
		chara:moveTo(pos)
	end
end

local function parseMoveAction(args)
	doMoveAction(args)
end

local function doSwitchAI(args)
	local uid = args[1]
	local isOn = args[2] == 1
	local chara = CharacterManager:getCharacByUId(uid)
	if chara then
		if isOn == false then
			-- 嘲讽状态时不能取消AI
			if chara.charaBuff and chara.charaBuff:hasBuffEffect(FightDefine.BUFF_EFFECT_TYPE.TAUNT) then
				return
			end
		end
		if EnvironmentHandler.isPvpClient then
			-- chara.isAutoFight = isOn
			if isOn ~= 1 then
				local ai = aiDic[uid]
				if ai and ai.currState == nil and chara.state == CharacterConstant.STATE_RUN then
					chara:stopSearch()
				end
			end
			chara:switchAutoFight(isOn)
			
			ModuleEvent.dispatchWithFixedArgs(ModuleConstant.CHARAC_AUTOFIGHT_STATE, chara.uniqueID, chara.isAutoFight)
		else
			chara:switchAutoFight(isOn)
			if EnvironmentHandler.isInServer then
				local isAutoFight = 0
				if chara.isAutoFight then isAutoFight = 1 end
				EnvironmentHandler.sendSpecifiedOrderToAllClient(ModuleConstant.PLAYER_SWITCH_AI, {uid, isAutoFight})
			end
		end
	end 
end

local function parseSwitchAI(args)
	doSwitchAI(args)
end

local function teamDungeonCondition( ai )
    if TeamModel:PlayerIsLeader() == false and ai and ai.target and
    	(ai.target.characterType == CharacterConstant.TYPE_MONSTER or ai.target.characterType == CharacterConstant.TYPE_PLAYER) then
        return false
    end
    return true
end

local function cargoRobotCondition( ai )
	if CargoControl.isCargoTeamActive() == false then
		return true
	end
    return teamDungeonCondition(ai)
end

local function PKCondition( ai )
    return false
end

local conditionTable = {
    [ConstantData.STAGE_TYPE_TEAM] = teamDungeonCondition,
    [ConstantData.STAGE_TYPE_ELITE] = teamDungeonCondition,
    -- [ConstantData.STAGE_TYPE_TEAM_POEM] = teamDungeonCondition,
    [ConstantData.STAGE_TYPE_PK] = PKCondition,
    [ConstantData.STAGE_TYPE_FIGHTINGMASTER] = PKCondition,
	[ConstantData.STAGE_TYPE_CARGO_ROBOT] = cargoRobotCondition,
	[ConstantData.STAGE_TYPE_COUPLE_DUNGEON] = teamDungeonCondition
}

local function isEnableAI( ai )
    if EnvironmentHandler.isInServer == false then
    	if EnvironmentHandler.isPvpClient then
    		return false
		end
        if conditionTable[StageManager.getCurStageType()] ~= nil then
            return conditionTable[StageManager.getCurStageType()](ai)
        end
    end
    return true 
end

local function initEvent()
	ModuleEvent.addListener(ModuleConstant.AI_CREATE,onCreateAI)
	ModuleEvent.addListener(ModuleConstant.AI_DESTROY, onDestroyAI)
	ModuleEvent.addListener(ModuleConstant.AI_AWAKE, onAwakeAI)
	ModuleEvent.addListener(ModuleConstant.AI_START, onStartAI)
	ModuleEvent.addListener(ModuleConstant.AI_STOP, onStopAI)
	ModuleEvent.addListener(ModuleConstant.AI_CHANGE_PROPERTY, onChangeAIProperty)
	ModuleEvent.addListener(ModuleConstant.AI_CLEARROUNDTIME, onClearRoundTime)
	ModuleEvent.addListener(ModuleConstant.AI_BREAK_STATE, onBreakState)
	ModuleEvent.addListener(ModuleConstant.AI_ACTION_MOVE, doMoveAction, parseMoveAction)
	ModuleEvent.addListener(ModuleConstant.PLAYER_SWITCH_AI, doSwitchAI, parseSwitchAI)
end

local function removeEvent()
	ModuleEvent.removeListener(ModuleConstant.AI_CREATE,onCreateAI)
	ModuleEvent.removeListener(ModuleConstant.AI_DESTROY, onDestroyAI)
	ModuleEvent.removeListener(ModuleConstant.AI_AWAKE, onAwakeAI)
	ModuleEvent.removeListener(ModuleConstant.AI_START, onStartAI)
	ModuleEvent.removeListener(ModuleConstant.AI_STOP, onStopAI)
	ModuleEvent.removeListener(ModuleConstant.AI_CHANGE_PROPERTY, onChangeAIProperty)
	ModuleEvent.removeListener(ModuleConstant.AI_CLEARROUNDTIME, onClearRoundTime)
	ModuleEvent.removeListener(ModuleConstant.AI_BREAK_STATE, onBreakState)
	ModuleEvent.removeListener(ModuleConstant.AI_ACTION_MOVE, doMoveAction)
	ModuleEvent.removeListener(ModuleConstant.PLAYER_SWITCH_AI, doSwitchAI)
end

-- 初始化AI管理器
function AIManager.init()
	enableAI=true
	aiDic = {}
	typeDic = {}
	initEvent()
end

-- 帧调用
function AIManager.update()
	if enableAI == false or aiDic == nil then return end
	if updateCD > 0 then
		updateCD = updateCD - 1
		return
	end
	updateCD = FightDefine.AI_RUN_INTERVAL -1 
	for k,ai in pairs(aiDic) do
		if ai.target==nil then
			ai:dispose()
			ai=nil
			aiDic[k]=nil
		else
			if isEnableAI(ai) then
		        ai:update()
            end
		end
        
	end
end

-- 获取指定uid的AI
function AIManager.getAIByUniqueID(uid)
	if aiDic == nil then return nil end
	return aiDic[uid]
end

-- 开启指定角色类型的AI
function AIManager.enableAIByType(charaType)
	if typeDic[charaType] == nil then
		return
	end
	local tempDic = typeDic[charaType]
	for k, ai in pairs(tempDic) do
		ai:doRoundAI()
	end
end

function AIManager.activeAllAI()
	enableAI=true
    if aiDic == nil then return end
	for k, ai in pairs(aiDic) do
		ai:doRoundAI()
	end
end

-- 开启全部AI
function AIManager.enableAllAI()
	enableAI=true
	if aiDic == nil then return end
	for k, ai in pairs(aiDic) do
		ai:start()
	end
end

-- 禁用指定角色类型的AI
function AIManager.disableAIByType(charaType)
	if typeDic[charaType] == nil then
		return
	end
	local tempDic = typeDic[charaType]
	for k, ai in pairs(tempDic) do
		ai:stop()
	end
end

-- 禁用全部AI
function AIManager.disableAllAI()
	enableAI=false
	-- if aiDic == nil then return end
	-- for k, ai in pairs(aiDic) do
	-- 	ai:stop()
	-- end
end

-- 判断某个角色是否有AI
function AIManager.hasAI(uid)
	if aiDic == nil then return false end
	return (aiDic[uid] ~= nil)
end

-- 判断某个角色是否在运行AI
function AIManager.hasAIRun(uid)
    if aiDic == nil then return false end
    if aiDic[uid] == nil then return false end
    return aiDic[uid]:isRunning()
end

function AIManager.getIdleActionFromAI(uid)
	if aiDic == nil or aiDic[uid] == nil then
		return nil
	end
	return aiDic[uid]:getIdleAction()
end

-- 销毁AI管理器
function AIManager.dispose()
	removeEvent()
	if aiDic then
		for _,ai in pairs(aiDic) do
			ai:dispose()
		end
		aiDic = nil
	end
	typeDic = nil
end