--region RuneSpawnManager.lua
--Date 2017/01/04
--Author zhouxiaogang
--Desc 神符刷新管理
--endregion

RuneSpawnManager = {}

local waitingSpawnList = nil		-- 待刷出的神符刷新点列表
local runningSpawnList = nil		-- 已经刷出的神符刷新点列表(array)
local runningSpawnDic = nil			-- 已经刷出的神符刷新点表（hash）
local spawnTimeInfos = nil			-- 神符刷新点的时间信息{spawnId=xx, startTime=xxx, endTime=xxx}

local initRuneInfoFromServer = nil	-- 缓存在进入关卡之前收到服务端的神符信息

local pickupInstanceRecord = {}		-- 记录已经拾取的神符实例ID，用于同步创建时判断神符是否已经创建过
local removeSpawnSeqRecord = {}		-- 记录已经移除的神符刷新点序号，用于同步创建时判断刷新点是否已经被移除了

local RUNE_INSTANCE_SEED = 0
local RUNE_SPAWN_SEED = 0
function RuneSpawnManager.createRuneInstanceId()
	RUNE_INSTANCE_SEED = RUNE_INSTANCE_SEED + 1
	return RUNE_INSTANCE_SEED
end

function RuneSpawnManager.createRuneSpawnSeq()
	RUNE_SPAWN_SEED = RUNE_SPAWN_SEED + 1
	return RUNE_SPAWN_SEED
end

-- 创建神符
local function createRune(args)
	local spawnId = args[1]
	local runeId = args[2]
	local instanceId = args[3]
	-- 已经创建过了
	if pickupInstanceRecord[instanceId] then
		return
	end
	if runningSpawnDic[spawnId] then
		runningSpawnDic[spawnId]:createOneRune(runeId, instanceId)
	end
end

-- 收到服务端创建神符的通知
local function parseCreateRune(args)
	if EnvironmentHandler.isPvpClient and StageManager.isStageReady() == false then
		if initRuneInfoFromServer[args[1]] then
			initRuneInfoFromServer[args[1]].runeId = args[2]
			initRuneInfoFromServer[args[1]].instanceId = args[3]
		else
			initRuneInfoFromServer[args[1]] = {spawnId = args[1], runeId = args[2], instanceId = args[3]}
		end
	else
		createRune(args)
	end
end

local function pickupRune(args)
	local spawnId = args[1]
	local runeId = args[2]
	local uniqueID = args[3]
	local instanceId = args[4]
	pickupInstanceRecord[instanceId] = true
	if runningSpawnDic[spawnId] then
		runningSpawnDic[spawnId]:pickUp()
	end
	local runeCfg = CFG.rune:get(runeId)
	if runeCfg then
		local buffInstanceId = BuffManager.getInstanceId()
		ModuleEvent.dispatch(ModuleConstant.ADD_BUFF, {uniqueID, uniqueID, runeCfg.buffid, 1, buffInstanceId})
	else
		LogManager.LogError("RuneSpawnManager.pickupRune():找不到id为" .. tostring(runeId) .. "的rune配置")
	end
end

-- 收到服务端拾取神符的通知
local function parsePickupRune(args)
	if EnvironmentHandler.isPvpClient and StageManager.isStageReady() == false then
		pickupInstanceRecord[args[4]] = true
		if initRuneInfoFromServer[args[1]] then
			initRuneInfoFromServer[args[1]].runeId = nil
			initRuneInfoFromServer[args[1]].instanceId = nil
		else
			initRuneInfoFromServer[args[1]] = {spawnId = args[1], runeId = nil, instanceId = nil}
		end
	else
		pickupRune(args)
	end
end

-- 移除神符刷新点
local function removeSpawnPoint(args)
	local spawnId = args[1]
	local spawnSeq = args[2]
    if spawnId == nil or spawnSeq == nil then
        return
    end
	removeSpawnSeqRecord[spawnSeq] = true
	local spawnPoint = runningSpawnDic[spawnId]
	if spawnPoint then
		runningSpawnDic[spawnId] = nil
		for i = #runningSpawnList, 1, -1 do
			if runningSpawnList[i].spawnId == spawnId then
				table.remove(runningSpawnList, i);break;
			end
		end
		if spawnPoint.runeObj then
			pickupInstanceRecord[spawnPoint.runeObj.instanceId] = true
		end
		spawnPoint:dispose()
	end
end

-- 创建神符刷新点
local function createSpawnPoint(args)
	if runningSpawnDic == nil then return end
	local spawnId = args[1]
	local spawnSeq = args[2]
	-- 已经被移除
	if removeSpawnSeqRecord[spawnSeq] then
		return
	end
	local spawnCfg = CFG.runespawn:get(spawnId)
	if spawnCfg == nil then
		return
	end
	if runningSpawnDic[spawnId] then
		removeSpawnPoint({spawnId})
	end
	local spawnPoint = RuneSpawnPoint:create()
	table.insert(runningSpawnList, spawnPoint)
	runningSpawnDic[spawnId] = spawnPoint
	spawnPoint:init(spawnCfg, spawnSeq)
end

-- 收到服务端创建神符刷新点的通知
local function parseCreateSpawnPoint(args)
	if EnvironmentHandler.isPvpClient and StageManager.isStageReady() == false then
		if initRuneInfoFromServer[args[1]] == nil then
			initRuneInfoFromServer[args[1]] = {spawnId = args[1], spawnSeq = args[2]}
		end
	else
		createSpawnPoint(args)
	end
end

-- 收到服务端移除神符刷新点的通知
local function parseRemoveSpawnPoint(args)
	if EnvironmentHandler.isPvpClient and StageManager.isStageReady() == false then
		removeSpawnSeqRecord[args[2]] = true
		if initRuneInfoFromServer[args[1]] then
			initRuneInfoFromServer[args[1]] = nil
		end
	else
		removeSpawnPoint(args)
	end
end

local function checkActiveRuneSpawn()
	if waitingSpawnList then
		local timeNow = GameNet.GetServerTime() * 0.001
		local spawnInfo = nil
		for i = #waitingSpawnList, 1, -1 do
			spawnInfo = waitingSpawnList[i]
			if spawnInfo.startTime == 0 or (spawnInfo.startTime <= timeNow and timeNow < spawnInfo.endTime) then
				table.remove(waitingSpawnList, i)
				ModuleEvent.dispatch(ModuleConstant.RUNESPAWNPOINT_CREATE, {spawnInfo.spawnId, RuneSpawnManager.createRuneSpawnSeq()})
			end
		end
	end
end

-- 初始化该场景的待创建神符列表
local function initRuneSpawnInStage()
	local stageCfg = FightModel:getFightSceneVo()
	if stageCfg.runespawnid == nil or stageCfg.runespawnid == "0" or stageCfg.runespawnid == "" then
		return
	end
	local idList = StringUtils.split(stageCfg.runespawnid, "+", nil, tonumber)
	local dateNow = os.date("*t", GameNet.GetServerTime() * 0.001)
	local gameResetTime = ConstantData.GAME_RESET_TIME
	local gameResetSecsInDay = gameResetTime.hour * 3600 + gameResetTime.min * 60 + gameResetTime.sec
	local spawnCfg = nil
	for k,id in ipairs(idList) do
		spawnCfg = CFG.runespawn:get(id)
		if spawnCfg ~= nil then
			local spawnInfo = nil
			if spawnCfg.spawntime == nil or spawnCfg.spawntime == "0" then
				spawnInfo = {spawnId = id, startTime = 0, endTime = 0}
			else
				local timeTable = StringUtils.split(spawnCfg.spawntime, "+", nil, tonumber)
				local startTime = os.time({year = dateNow.year, month = dateNow.month, day = dateNow.day, hour = timeTable[1], min = timeTable[2], sec = timeTable[3]})
				local startSecsInDay = timeTable[1] * 3600 + timeTable[2] * 60 + timeTable[3]
				local endTime = 0
				if startSecsInDay < gameResetSecsInDay then
					endTime = startTime + gameResetSecsInDay - startSecsInDay
				elseif startSecsInDay > gameResetSecsInDay then
					endTime = startTime + (24 * 3600) - (startSecsInDay - gameResetSecsInDay)
				else
					endTime = startTime
				end
				spawnInfo = {spawnId = id, startTime = startTime, endTime = endTime}
			end
			table.insert(waitingSpawnList, spawnInfo)
			spawnTimeInfos[id] = spawnInfo
		else
			LogManager.LogError("RuneSpawnManager.initRuneSpawnInStage():关卡[" .. tostring(stageCfg.stageid) .. "]的runespawnid中配置的神符[" .. tostring(id) .. "]未能在runespawn表中找到")
		end
	end
end

local function onEnterStage()
	if EnvironmentHandler.isPvpClient then
		if initRuneInfoFromServer == nil then return end
		for spawnId, spawnInfo in pairs(initRuneInfoFromServer) do
			createSpawnPoint({spawnId, spawnInfo.spawnSeq})
			if spawnInfo.runeId then
				createRune({spawnId, spawnInfo.runeId, spawnInfo.instanceId})
			end
		end
		initRuneInfoFromServer = {}
	else
		initRuneSpawnInStage()
	end
end

local function recvServerRuneInfo(conn)
	if EnvironmentHandler.isPvpClient == false then
		return
	end
	RuneSpawnManager:clearRunningRunes()
	local size = conn:ReadSbyte()
	if size == 0 then return end
	local runeInfo = {}
	for i = 1, size do
		local spawnId = conn:ReadInt()
		local spawnSeq = conn:ReadInt()
		local runeId = conn:ReadInt()
		local runeInstanceId = conn:ReadInt()
		table.insert(runeInfo, {spawnId, spawnSeq, runeId, runeInstanceId})
	end
	for k,info in ipairs(runeInfo) do
		parseCreateSpawnPoint(info)
		if info[3] ~= 0 then
			parseCreateRune({info[1], info[3], info[4]})
		end
	end
end

function RuneSpawnManager:update()
	if EnvironmentHandler.isPvpClient then
		return
	end
	checkActiveRuneSpawn()
	if runningSpawnList then
		local serverTimeNow = GameNet.GetServerTime() * 0.001
		local spawnPoint = nil
		local endTime = nil
		for i = #runningSpawnList, 1, -1 do
			spawnPoint = runningSpawnList[i]
			if spawnPoint then
				endTime = spawnTimeInfos[spawnPoint.spawnId].endTime
				if serverTimeNow >= endTime and endTime ~= 0 then
					ModuleEvent.dispatch(ModuleConstant.RUNESPAWNPOINT_REMOVE, {spawnPoint.spawnId, spawnPoint.spawnSeq})
				else
					spawnPoint:update(serverTimeNow)
				end
			end
		end
	end
end

-- 向特定的客户端发送神符信息
function RuneSpawnManager:sendToNewPlayers(fighters)
	if fighters == nil then return end
	local delaySend = function()
		checkActiveRuneSpawn()
		if runningSpawnList == nil or #runningSpawnList == 0 then
			return
		end
		local conn = EnvironmentHandler.getNetworkDecoder()
		conn:initBuffer()
		conn:WriteProtocol(ModuleConstant.SYNC_SERVER_RUNE_INFO)
		conn:WriteSbyte(#runningSpawnList)
		for _,spawnPoint in ipairs(runningSpawnList) do
			conn:WriteInt(spawnPoint.spawnId)
			conn:WriteInt(spawnPoint.spawnSeq)
			if spawnPoint.runeObj and spawnPoint.runeObj.runeData then
				conn:WriteInt(spawnPoint.runeObj.runeData.runeid)
				conn:WriteInt(spawnPoint.runeObj.instanceId)
			else
				conn:WriteInt(0)
				conn:WriteInt(0)
			end
		end
		local pack = conn:GetPack()
		for k,fighter in pairs(fighters) do
			if fighter.characterType == CharacterConstant.TYPE_PLAYER and (not fighter.isRobot) then
				EnvironmentHandler.sendPackToClient(fighter.uniqueID, pack)
			end
		end
	end
	FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(5, 1, delaySend)
end

function RuneSpawnManager:init()
	ModuleEvent.addListener(ModuleConstant.RUNE_CREATE, createRune, parseCreateRune)
	ModuleEvent.addListener(ModuleConstant.RUNE_PICKUP, pickupRune, parsePickupRune)
	ModuleEvent.addListener(ModuleConstant.RUNESPAWNPOINT_CREATE, createSpawnPoint, parseCreateSpawnPoint)
	ModuleEvent.addListener(ModuleConstant.RUNESPAWNPOINT_REMOVE, removeSpawnPoint, parseRemoveSpawnPoint)
	ModuleEvent.addListener(ModuleConstant.STAGE_LOADCOMPLETE, onEnterStage)
	if runningSpawnList and #runningSpawnList > 0 then
		for _,runeSpawn in pairs(runningSpawnList) do
			runeSpawn:dispose()
		end
	end
	waitingSpawnList = {}
	runningSpawnList = {}
	runningSpawnDic = {}
	spawnTimeInfos = {}
	pickupInstanceRecord = {}
	removeSpawnSeqRecord = {}
	if EnvironmentHandler.isPvpClient then
		initRuneInfoFromServer = {}
	end
	if EnvironmentHandler.isInServer then
		onEnterStage()
	end
end

function RuneSpawnManager:clearRunningRunes()
	initRuneInfoFromServer = {}
	if runningSpawnList then
		for _,runeSpawn in pairs(runningSpawnList) do
			runeSpawn:dispose()
		end
		runningSpawnList = {}
	end
	runningSpawnDic = {}
	removeSpawnSeqRecord = {}
	pickupInstanceRecord = {}
end

function RuneSpawnManager:clear()
	waitingSpawnList = nil
	runningSpawnDic = nil
	spawnTimeInfos = nil
	initRuneInfoFromServer = nil
	if runningSpawnList then
		for _,runeSpawn in pairs(runningSpawnList) do
			runeSpawn:dispose()
		end
		runningSpawnList = nil
	end
	removeSpawnSeqRecord = {}
	pickupInstanceRecord = {}
	RUNE_SPAWN_SEED = 0
	RUNE_INSTANCE_SEED = 0
	ModuleEvent.removeListener(ModuleConstant.RUNE_CREATE, createRune)
	ModuleEvent.removeListener(ModuleConstant.RUNE_PICKUP, pickupRune)
	ModuleEvent.removeListener(ModuleConstant.RUNESPAWNPOINT_CREATE, createSpawnPoint)
	ModuleEvent.removeListener(ModuleConstant.RUNESPAWNPOINT_REMOVE, removeSpawnPoint)
	ModuleEvent.removeListener(ModuleConstant.STAGE_LOADCOMPLETE, onEnterStage)
end

function RuneSpawnManager:register()
	ModuleEvent.registerRecv(ModuleConstant.SYNC_SERVER_RUNE_INFO, recvServerRuneInfo)
end

do
	RuneSpawnManager:register()
end