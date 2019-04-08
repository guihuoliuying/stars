-- region
-- Date    : 2016-08-19
-- Author  : daiyaorong
-- Description :  战斗控制器 服务端专用
-- endregion

local framTimeIns = nil
local frameKey = nil
local battleTime = nil		--当前战斗时间
local currentFrame = nil	--当前帧
local killerList = nil          --凶手记录

--每帧战斗记录
local deadList = nil    --亡者以及杀手
local damageList = nil  --每个角色的伤害值
local deadPosList = nil	-- 死亡的位置
local expList = nil     -- 获取经验值
local fightReadyCD = 0	--战斗倒计时
local fightEndTime = 0	--战斗结束时间,为0表示不限时

local fightState = FightDefine.FIGHT_STATE_NONE
local stageType = 0	-- 当前的场景类型

FightServerControl = {}

local CAL_TIME_IN_READY_STATE = 
{
	[ConstantData.STAGE_TYPE_DAILY5V5_PVP] = true,
	[ConstantData.STAGE_TYPE_CAMP_DAILY] = true,
}

local function tickTime()
	-- body
	battleTime = battleTime + ConstantData.FRAME_DELTA_TIME
	currentFrame = currentFrame + 1
end

local function checkFightReadyCD()
	if os.time() >= fightReadyCD then
		fightReadyCD = 0
		ModuleEvent.dispatch(ModuleConstant.PVP_FIGHT_START, {})
		FightServerControl.setFightState(FightDefine.FIGHT_STATE_FIGHTING)
	end
end

-- 是否已到达战斗结束时间
local function isFightTimeout()
	if fightEndTime > 0 and os.time() >= fightEndTime then
		return true
	end
	return false
end

local function onFrame()
	-- body
	tickTime()
    AIManager.update()
	SkillManager:update()
	PressureDispatcher.update()
	MonsterSpawnManager:update()
	BuffManager:update()
	SyncFrequence.update()
	RuneSpawnManager:update()
	if fightReadyCD > 0 then
		checkFightReadyCD()
	end

	--[[ DEBUGSTART
	if currentFrame % 60 == 0 then
		local rttl,rmax,ravg = ModuleEvent.debugBufferInfo()
		--print("serverlua recv byte info{total=" .. rttl .. ",max=" .. rmax .. ",avg=" .. ravg .. "}")
		local sttl, smax, savg = FightServerControl.debugBufferInfo()
		--print("serverlua send byte info{total=" .. sttl .. ",max=" .. smax .. ",avg=" .. savg .. "}")
	end
	-- DEBUGEND]]
end

-- 定时向每个客户端发送服务端对应玩家的信息
local function syncPlayerInfoToClient(evtInfo, isForce)
	local characs = CharacterManager:getCharacByType(CharacterConstant.TYPE_PLAYER)
	isForce = isForce or 0
	if characs then
		for _,chara in pairs(characs) do
			if (not chara.isRobot) then
				local pos =  chara:getPosition()
				local posX = math.round(pos.x, 2)
				local posZ = math.round(pos.z, 2)
				local isAutoFight = 0
				if chara.isAutoFight then isAutoFight = 1 end
				local curHp = chara.hp or 0
				EnvironmentHandler.sendSpecifiedOrderToClient(chara.uniqueID, ModuleConstant.SERVER_SYNC_PLAYERINFO, {isForce, posX, posZ, isAutoFight, curHp})
            else               
                if FightModel:getFightStageType() == ConstantData.STAGE_TYPE_CAMP_DAILY then -- 阵营日常需要同步机器人
                    local pos =  chara:getPosition()
				    local posX = math.round(pos.x, 2)
				    local posZ = math.round(pos.z, 2) 
                    EnvironmentHandler.sendSpecifiedOrderToAllClient(ModuleConstant.SERVER_SYNC_ROBOTINFO, {chara.uniqueID, posX,posZ})
                end
            end
		end
	end

	-- 临时解决家族匹配关卡中 马车移动同步问题
	if FightModel:getFightStageType() == ConstantData.STAGE_TYPE_FAMILY_WAR_STAGE_FIGHT then	
		local monsters = CharacterManager:getCharacByType(CharacterConstant.TYPE_MONSTER)
		if monsters then
			local count = 0
			for k, v in pairs(monsters) do
				count = count + 1
			end
			if count > 0 then
				local pos = nil
				local conn = EnvironmentHandler.getNetworkDecoder()
				conn:initBuffer()
				conn:WriteProtocol(ModuleConstant.SERVER_SYNC_MONSTER)
				conn:WriteSbyte(count)
				for k, monster in pairs(monsters) do
					if monster and monster.hp > 0 and (monster.state ~= CharacterConstant.STATE_DEAD and monster.state ~= CharacterConstant.STATE_IDLE) then
						conn:WriteString(monster.uniqueID)
						pos = monster:getPosition()
						conn:WriteFloat(math.round(pos.x, 2))
						conn:WriteFloat(math.round(pos.y, 2))
						conn:WriteFloat(math.round(pos.z, 2))
						local skill = SkillManager:getRunningSkill(monster.uniqueID)
						if skill and skill.isActive == true then
							conn:WriteInt(skill.skillID or 0)
							conn:WriteInt(skill.skillLv or 1)
							conn:WriteInt(skill.fireSerialNum or 1)
							conn:WriteString(skill.targetID or "0")
						else
							conn:WriteInt(0)
						end
					else
						conn:WriteString("0")
					end
				end
				EnvironmentHandler.sendPackToAllClient(conn:GetPack())
			end
		end
	end
end

local function syncInfoToClient(uid, isForce)
	local chara = CharacterManager:getCharacByUId( uid )
	if chara and (not chara.isRobot) then
		isForce = isForce or 0
		local pos =  chara:getPosition()
		local posX = math.round(pos.x, 2)
		local posZ = math.round(pos.z, 2)
		local isAutoFight = 0
		if chara.isAutoFight then isAutoFight = 1 end
		local curHp = chara.hp or 0
		EnvironmentHandler.sendSpecifiedOrderToClient(chara.uniqueID, ModuleConstant.SERVER_SYNC_PLAYERINFO, {isForce, posX, posZ, isAutoFight, curHp})
	end
end

local function initManager()
	-- body
	AIManager.init()
	MonsterAwakeControl.init()
	BuffManager:init()
	SkillManager:init()
	PressureDispatcher.init()
	MonsterSpawnManager:init()
	MonsterSpawnManager:start();
	SyncFrequence.init()
	RuneSpawnManager:init()
	DynamicBlockManager:init()
	SyncFrequence.addTimeoutCallBack(ModuleConstant.SERVER_SYNC_PLAYERINFO, syncPlayerInfoToClient)
    if FightModel:getFightStageType() == ConstantData.STAGE_TYPE_CAMP_DAILY then
        CampDailyModel.register()
    end
end

local function initRecord()
    damageList = {}
    deadList = {}
    expList = {}
    killerList = {}
	deadPosList = {}
end

local function onFightReadyGo()
	MonsterSpawnManager:start();
end

-- 创建玩家时，如果未处于战斗状态，让角色无法行动
local function onCreateCharacter(character)
	-- 如果战斗未开始，禁止角色的一切动作
	if fightState ~= FightDefine.FIGHT_STATE_FIGHTING then
		character:setAbilityEnable(CharacterConstant.ABILITY.NORMAL_SKILL, 0)
		character:setAbilityEnable(CharacterConstant.ABILITY.SKILL, 0)
		character.ctrlState = FightDefine.CONTROL_NONE
	else
		-- 如果是在战斗过程中创建，而且是机器人，就让它自动战斗
		if character.isRobot then
			character:setRandomAI()
			character:switchAutoFight(true)
		end
		if character.pSkillHandler then
			character.pSkillHandler:onFightStart()
		end
	end
end

local function addListener()
	ModuleEvent.addListener(ModuleConstant.FIGHT_READY_GO, onFightReadyGo)
	ModuleEvent.addListener(ModuleConstant.CHARAC_CREATE_COMPLETE, onCreateCharacter)
end

local function removeListener()
	ModuleEvent.removeListener(ModuleConstant.FIGHT_READY_GO, onFightReadyGo)
	ModuleEvent.removeListener(ModuleConstant.CHARAC_CREATE_COMPLETE, onCreateCharacter)
end

--1v1pk
local function pvpHandler( result )
    for k,v in pairs(deadList) do
        result['dead'] = deadList   --非空表即可写入result
        deadList = {}
        break
    end

    for k,v in pairs(damageList) do
        result['damage'] = damageList --非空表即可写入result
        damageList = {}
        break
    end

	return result
end

-- 斗神殿
local function fightingMasterHandler( result )
	for k,v in pairs(deadList) do
        result['dead'] = deadList   --非空表即可写入result
        deadList = {}
        break
    end
	-- 战斗时间结束时发整场战斗的伤害列表
	if isFightTimeout() then
		local allCharac = CharacterManager:getAllCharac()
		local hpInfo = {}
		if allCharac then
			for k,charac in pairs(allCharac) do
				if charac.characterType == CharacterConstant.TYPE_PLAYER then
					hpInfo[charac.uniqueID] = tostring(charac.maxhp) .. "+" .. tostring(charac.hp)
				end
			end
		end
		result['hpInfo'] = hpInfo
	end

	return result
end

--野外夺宝
local function lootTreasureHandler( result )
    for k,v in pairs(deadList) do
        result['dead'] = deadList   --非空表即可写入result
        deadList = {}
        break
    end

    for k,v in pairs(damageList) do
        result['damage'] = damageList --非空表即可写入result
        damageList = {}
        break
    end

    return result
end

-- 家族精英战
local function familyEliteWarHandler( result )
	-- body
	for k,v in pairs(deadList) do
        result['dead'] = deadList   --非空表即可写入result
        deadList = {}
        break
    end

    for k,v in pairs(damageList) do
        result['damage'] = damageList --非空表即可写入result
        damageList = {}
        break
    end

    for k,v in pairs(deadPosList) do
    	result['deadPos'] = deadPosList --非空表即可写入result
    	deadPosList = {}
    	break
    end

    return result
end

-- 家族战
local function familyWarHandler( result )
	-- body
	for k,v in pairs(deadList) do
        result['dead'] = deadList   --非空表即可写入result
        deadList = {}
        break
    end

    for k,v in pairs(damageList) do
        result['damage'] = damageList --非空表即可写入result
        damageList = {}
        break
    end

    return result
end

local function cargoPvpHandler( result )
	for k,v in pairs(deadList) do
        result['dead'] = deadList   --非空表即可写入result
        deadList = {}
        break
    end

    for k,v in pairs(damageList) do
        result['damage'] = damageList --非空表即可写入result
        damageList = {}
        break
    end

	for k,v in pairs(deadPosList) do
        result['deadPos'] = deadPosList --非空表即可写入result
        deadPosList = {}
        break
    end
	local cargo = CharacterManager:getCharacByUId("1")
	if cargo then
		local cargoPos = cargo:getPosition()
		local posX = math.round(cargoPos.x * 10, 2)
		local posY = math.round(cargoPos.y * 10, 2)
		local posZ = math.round(cargoPos.z * 10, 2)
		result["cargoPosition"] = posX .. "+" .. posY .. "+" .. posZ
	end
end

local function teamPvPHandler(result)
	-- body
	for k,v in pairs(deadList) do
        result['dead'] = deadList   --非空表即可写入result
        deadList = {}
        break
    end

    for k,v in pairs(damageList) do
        result['damage'] = damageList --非空表即可写入result
        damageList = {}
        break
    end

    return result
end

-- 日常5v5
local function daily5v5PvpHandler( result )
	for k,v in pairs(deadList) do
        result['dead'] = deadList   --非空表即可写入result
        deadList = {}
        break
    end

    for k,v in pairs(damageList) do
        result['damage'] = damageList --非空表即可写入result
        damageList = {}
        break
    end

    for k,v in pairs(deadPosList) do
    	result['deadPos'] = deadPosList --非空表即可写入result
    	deadPosList = {}
    	break
    end

    return result
end

local function daily5v5CampDailyHandler( result )
	for k,v in pairs(deadList) do
        result['dead'] = deadList   --非空表即可写入result
        deadList = {}
        break
    end

    for k,v in pairs(expList) do
        result['exp'] = expList --非空表即可写入result
        expList = {}
        break
    end     
    return result
end

local handlerMap = {
    [ConstantData.STAGE_TYPE_PK] = pvpHandler,
    [ConstantData.STAGE_TYPE_LOOTTREASURE_PVP] = lootTreasureHandler,
	[ConstantData.STAGE_TYPE_FIGHTINGMASTER] = fightingMasterHandler,
	[ConstantData.STAGE_TYPE_FAMILY_WAR_ELITE_FIGHT] = familyEliteWarHandler,
	[ConstantData.STAGE_TYPE_CARGO_PVP] = cargoPvpHandler,
	[ConstantData.STAGE_TYPE_FAMILY_WAR_NORMAL_FIGHT] = familyWarHandler,
	[ConstantData.STAGE_TYPE_TEAM_PVP] = teamPvPHandler,
	[ConstantData.STAGE_TYPE_FAMILY_WAR_STAGE_FIGHT] = familyWarHandler,
	[ConstantData.STAGE_TYPE_FAMILY_TRANSPORT_PVP] = fightingMasterHandler,
	[ConstantData.STAGE_TYPE_DAILY5V5_PVP] = daily5v5PvpHandler,
    [ConstantData.STAGE_TYPE_CAMP_DAILY]   = daily5v5CampDailyHandler,
}

function FightServerControl.init()
	addListener();
	initManager()
    initRecord()
	frameKey = framTimeIns:RegisterLuaCallBack(1,0,onFrame)
end

function FightServerControl.onDataReady()
	-- body
	framTimeIns = FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA)
	battleTime = 0
	currentFrame = 0
	stageType = FightModel:getFightStageType()
	FightServerControl.initStage()
	FightServerControl.init()
	FightServerControl.createPlayer()
	FightServerControl.createPartner()
	syncPlayerInfoToClient(nil, 1)
end

-- 设置战斗状态
function FightServerControl.setFightState(newState)
	if fightState == newState then
		return
	end
	fightState = newState
	local allCharacs = CharacterManager:getAllCharac()
	local enableAbility = fightState == FightDefine.FIGHT_STATE_FIGHTING and 1 or 0
	if allCharacs then
		for _,charac in pairs(allCharacs) do
			charac:setAbilityEnable(CharacterConstant.ABILITY.NORMAL_SKILL, enableAbility)
			charac:setAbilityEnable(CharacterConstant.ABILITY.SKILL, enableAbility)
			if charac.isRobot then
				charac:setRandomAI()
				charac:switchAutoFight((enableAbility == 1))
			end
			if enableAbility == 0 then
				charac.ctrlState = FightDefine.CONTROL_NONE
			else
				charac.ctrlState = FightDefine.CONTROL_DEFAULT
				if charac.pSkillHandler then
					charac.pSkillHandler:onFightStart()
				end
			end
		end
	end
end

function FightServerControl.setFightTimeInfo(duration, readyCD)
	fightReadyCD = os.time() + (readyCD or 0)
	if duration and duration > 0 then
		fightEndTime = os.time() + duration
	else
		fightEndTime = 0
	end
	checkFightReadyCD()
end

-- 场景初始化
function FightServerControl.initStage()
	local stageInfo = FightModel:getFightSceneVo()
	local navmesh = Project_Path.."/config/luajava/map/"..stageInfo.stagemap..".navmesh"
	PathFinder.addData(navmesh)
end

-- 生成玩家
function FightServerControl.createPlayer()
	local playerList = FightModel:getFightData():getData( FightDefine.DATA_PLAYER )
	if playerList ~= nil then
		for key, data in pairs( playerList ) do
			ModuleEvent.dispatch(ModuleConstant.CHARAC_CREATE, data)
		end
	end
end

-- 生成伙伴
function FightServerControl.createPartner()
	local partnerList = FightModel:getFightData():getData( FightDefine.DATA_PARTNER )
	if partnerList ~= nil then
		for key, data in pairs( partnerList ) do
			ModuleEvent.dispatch(ModuleConstant.CHARAC_CREATE, data)
		end
	end
end

-- 生成怪物
function FightServerControl.createMonster()
	local monsterList = FightModel:getFightData():getData( FightDefine.DATA_MONSTER )
	if monsterList ~= nil then
		for key, data in pairs( monsterList ) do
			if data.uniqueID then
				ModuleEvent.dispatch(ModuleConstant.CHARAC_CREATE, data)
			end
		end
	end
end

function FightServerControl.getFightTime()
	-- body
	return battleTime
end

function FightServerControl.setFrameRecord( attackerID, targetID, damage, hp )
    if damageList[targetID] == nil then
        damageList[targetID] = {}
    end
    if damageList[targetID][attackerID] == nil then
        damageList[targetID][attackerID] = damage
    else
        damageList[targetID][attackerID] = damageList[targetID][attackerID] + damage
    end

    if hp == 0 then
        killerList[targetID] = attackerID --先记录下凶手ID
    end
end

function FightServerControl.setFrameDead( uniqueID, deadPos )
    deadList[uniqueID] = killerList[uniqueID]
	deadPosList[uniqueID] = math.round(deadPos.x * 10, 2) .. "+" .. math.round(deadPos.y * 10, 2) .. "+" .. math.round(deadPos.z * 10)
end

function FightServerControl.setFrameExpAdd(uniqueID, exp)
    if expList[uniqueID] == nil then
        expList[uniqueID] = 0
    end
    expList[uniqueID] = expList[uniqueID] + exp
end

function FightServerControl.requestSyncInfo( uid, isForce )
	syncInfoToClient(uid, isForce)
end

-- DEBUGSTART
local ttlByteLen = 0
local maxByteLen = 0
function FightServerControl.debugBufferInfo()
	local avg = ttlByteLen / currentFrame
	return ttlByteLen, maxByteLen, avg
end
-- DEBUGEND

--获取服务端lua帧更新结果
function FightServerControl.updateResult()
    local frameData = { }
	local frameOrder = { }
	local order = ModuleEvent.getOrdersStr()
	local orderLen = 0
	if order then
		frameOrder['order'] = order
		orderLen = orderLen + #order
	end
    local fightType = FightModel:getFightStageType()
    handlerMap[fightType]( frameData )
	orderLen = orderLen + EnvironmentHandler.getSpecifiedOrderLen()
	frameOrder['specificorder'] = EnvironmentHandler.getSpecifiedOrder()
	orderLen = orderLen + EnvironmentHandler.getMultiClientOrderLen()
	frameOrder['multiorder'] = EnvironmentHandler.getMultiClientOrder()
	frameOrder['log'] = EnvironmentHandler.getServerLog()
	-- 战斗时间到, （本来未结束时可以不写，但这样可能整个frameData是空的，java那边解析会出错）
	frameData['fighttimeout'] = isFightTimeout()
	
	--[[ DEBUGSTART
	ttlByteLen = ttlByteLen + orderLen
	if orderLen > maxByteLen then maxByteLen = orderLen end
	-- DEBUGEND]]

    return frameData, frameOrder
end 

function FightServerControl.stopFight(isWin)
	AIManager.disableAllAI()
end

function FightServerControl.dispose()
	FightModel:dispose()
	FightServerControl.setFightState(FightDefine.FIGHT_STATE_END)
	removeListener()
	damageList = nil
    deadList = nil
    expList = nil
    killerList = nil
	deadPosList = nil
	-- body
    AIManager.dispose()
    MonsterAwakeControl.clear()
	if framTimeIns then
		framTimeIns:dispose()
		framTimeIns = nil
		frameKey = nil
	end
	PressureDispatcher.clear()
	SkillManager:dispose()
	MonsterSpawnManager:clear()
	BuffManager:clear()
	SyncFrequence.clear()
	RuneSpawnManager:clear()
	PathFinder.dispose()
	collectgarbage("stop")
	collectgarbage("collect")
end

function FightServerControl.getCurrentFrame()
	return currentFrame
end

function FightServerControl.getFightState()
	return fightState
end

function FightServerControl.onAddNewFighters(fighters)
	RuneSpawnManager:sendToNewPlayers(fighters)
end