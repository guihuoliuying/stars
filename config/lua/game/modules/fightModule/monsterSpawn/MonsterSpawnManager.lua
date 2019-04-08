--region MonsterSpawnManager.lua
--Date 2016/07/06
--Author zhouxiaogang
-- 关卡刷怪管理
--endregion
MonsterSpawnManager = {}

local spawnAreas = nil			-- 刷怪触发区域，由客户端通知服务器主角是否进入刷怪区域
local monsterDeadDic = nil		-- 已经死亡但还未通知服务器的怪物sn列表
local deadFrame = 0				-- 检查死亡上报的帧计数,计数到MAX_DEAD_FRAME时上报一次
local reconfirmDeadFrame = 0	-- 当该值达到RECONFIRM_FRAME_INTERVAL设置的值时，将会把未确认的死亡列表重新上传给服务端确认
local delaySpawnList = nil		-- 需要延时创建的怪物列表

local MAX_DEAD_FRAME = 30		-- 死亡帧计数到达该值时上报服务器
local RECONFIRM_FRAME_INTERVAL = ConstantData.FRAME_RATE * 5	-- 重新跟服务端确认数据包的时间
local isReady = false
local bossComingEff = nil
local fixDelayTime = 0			-- loading到开始时经过的毫秒数
local waitForConfirmSpawnAreas = nil
local reconfirmSpawnAreaFrame = 0

local spawnedGroupDic = nil		-- 已经刷过的spawnID
local spawnedMonsterUIDs = nil

local areaFunc = {}

local lastReconnectState = nil

local showDropDic = nil


-- 圆形刷怪区域
areaFunc[FightDefine.SPAWNAREA_CIRCLE] = function(area, pos)
	local tempPos = pos - area.position
	local sqrMag = tempPos:SqrMagnitudeWithoutY()
	if sqrMag < area.sqrRadius then
		return true
	end
	return false
end

-- 矩形刷怪区域
areaFunc[FightDefine.SPAWNAREA_RECT] = function(area, pos)
	return math.isPointInRect(area.bounds, pos)
end

local function isNeedReconfirm()
	if EnvironmentHandler.environmentType == EnvironmentDef.DEFAULT or EnvironmentHandler.environmentType == EnvironmentDef.PVE_CLIENT then
		return true
	end
	return false
end

local function onShowDrop(param)
	if showDropDic and next(showDropDic) then
		for k,v in pairs(showDropDic) do
			if param[1]==k and param[2]==ConstantData.ROLE_CONST_HP then
				local gapTime = GameNet.GetServerTime() - v.lastTime
				if gapTime >= v.gapTime then
					ModuleEvent.dispatchWithFixedArgs(ModuleConstant.PLAY_DROP, k, v.dropList)
					v.lastTime = GameNet.GetServerTime()
				end 
			end
		end
	end
end 

--怪物受到伤害时隔固定时间播放一下掉落，只作掉落表现，不作数据处理
--groupid:掉落组id　，　gaptime: 间隔时间 单位　毫秒
local function createShowDrop(uId , groupid , gaptime)
	local dropList = DropCtrl:getDropListByGroupId(groupid)
	if dropList and next(dropList) then
		ModuleEvent.addListener(ModuleConstant.CHARAC_UPDATE_ATTR,onShowDrop)
		showDropDic = showDropDic or {}
		showDropDic[uId] = showDropDic[uId] or {}
		showDropDic[uId].lastTime = GameNet.GetServerTime()
		showDropDic[uId].gapTime = gaptime
		showDropDic[uId].dropList = dropList
	end
end

local function releaseShowDrop(uId)
	if showDropDic and showDropDic[uId] then
		showDropDic[uId] = nil
		if next(showDropDic) == nil then
			ModuleEvent.removeListener(ModuleConstant.CHARAC_UPDATE_ATTR,onShowDrop)
		end
	end
end 

-- 刷出一个怪物
local function spawnMonster(monsterInfo)
	if monsterInfo == nil then return end
	local monsterVo = FightModel:getMonsterVo(monsterInfo.configId)
	for key,val in pairs(monsterVo) do
		monsterInfo[key] = val
	end
	-- if EnvironmentHandler.isPvpClient then
	-- 	if UnHandleInfoBeforeLoaded.isDeadBeforeLoadDone(monsterInfo.uniqueID) then
	-- 		return false
	-- 	end
	-- end
	if EnvironmentHandler.isInServer == false then
		if UnHandleInfoBeforeLoaded.isDeadBeforeLoadDone(monsterInfo.uniqueID) then
			return false
		end
	end
	ModuleEvent.dispatch(ModuleConstant.CHARAC_CREATE, monsterInfo)
	if EnvironmentHandler.isInServer and monsterInfo.uniqueID then
		ModuleEvent.dispatch(ModuleConstant.SPAWN_MONSTER, {monsterInfo.uniqueID})
	end

	if monsterInfo.monsterType == CharacterConstant.MONSTER_TYPE.BOSS and EnvironmentHandler.isInServer == false then
		local bossMusic = StageManager.getCurStage().stageData.bossmusic
		if bossMusic and bossMusic ~= "" and bossMusic ~= "0" then
			StageManager.getCurStage():stopMusic()
			AudioMgr.PlaySoundInfoScene(bossMusic)
		end
		--增加boss掉落表现
		if monsterInfo.stageMonsterId then
			local monsterAttrConfig = CFG.monsterattribute:get(monsterInfo.stageMonsterId)
	    	if monsterAttrConfig and monsterAttrConfig.showdrop and monsterAttrConfig.showdrop ~= "0" then
	    		local config = StringUtils.split(monsterAttrConfig.showdrop , "+" , nil , tonumber)
	    		if config and next(config) then
	    			createShowDrop(monsterInfo.uniqueID , config[1] , config[2])
	    		end
	    	end
		end
	end
end

-- 刷怪帧函数，负责检查是否进入刷怪区域，是否需要上报死亡列表，是否需要创建新的怪物
function MonsterSpawnManager:update()
	if isReady == false then
		return
	end
	--当在播剧情时锁定战斗，不刷怪
	if spawnAreas and #spawnAreas > 0 and (StoryController == nil or StoryController.GetIsLockFight()~=true) then
		local myCharac = CharacterManager:getMyCharac()
		local pos = myCharac:getPosition()
		local tempSpawn = nil
		for i = #spawnAreas, 1, -1 do
			tempSpawn = spawnAreas[i]
			if areaFunc[tempSpawn.area.areaType](tempSpawn.area, pos) then
				if waitForConfirmSpawnAreas == nil then
					waitForConfirmSpawnAreas = {}
				end
				waitForConfirmSpawnAreas[tempSpawn.spawnid] = 0
				if GameState and GameState.GetReConnectingState()~=true then
					MonsterSpawnProtocal.triggerSpawnArea(tempSpawn.spawnid)
				end
				table.remove(spawnAreas, i)
			end
		end
	end

	if GameState then
		if lastReconnectState and GameState.GetReConnectingState()~=true then
			deadFrame = 0
			reconfirmDeadFrame = 0
		end
		lastReconnectState = GameState.GetReConnectingState()
	end

	if GameState and GameState.GetReConnectingState()~=true and isNeedReconfirm() then
		if deadFrame > MAX_DEAD_FRAME then
			MonsterSpawnProtocal.tellServerMonsterDead(monsterDeadDic, 0)
			deadFrame = 0
		end
		if reconfirmDeadFrame > RECONFIRM_FRAME_INTERVAL then
			MonsterSpawnProtocal.tellServerMonsterDead(monsterDeadDic, 1)
			if waitForConfirmSpawnAreas then
				for spawnId,v in pairs(waitForConfirmSpawnAreas) do
					MonsterSpawnProtocal.triggerSpawnArea(spawnId)
				end
			end
			reconfirmDeadFrame = 0
		end
		
		deadFrame = deadFrame + 1
		reconfirmDeadFrame = reconfirmDeadFrame + 1
	end

	if #delaySpawnList > 0 then
		local timeNow = GameNet.GetServerTime() * 0.001
		for i = #delaySpawnList, 1, -1 do
			if delaySpawnList[i].spawnTime <= timeNow then
				--lyt 20161203 在播剧情,锁定战斗时，不刷怪，播放完在根据延迟时间刷怪
				if StoryController == nil or StoryController.GetIsLockFight()~=true then
					spawnMonster(delaySpawnList[i])
					table.remove(delaySpawnList, i)
				else
					delaySpawnList[i].spawnTime = timeNow + (delaySpawnList[i].delay * 0.001)
				end
			end
		end
	end
end

-- 服务端确认死亡包
function MonsterSpawnManager.confirmMonsterDead(confirmDeadList)
	if confirmDeadList == nil or monsterDeadDic == nil then return end
	for k,v in pairs(confirmDeadList) do
		if monsterDeadDic[v] then
			monsterDeadDic[v] = nil
		end
	end
end

-- 服务端确认刷怪区域生效
function MonsterSpawnManager.confirmSpawnArea(spawnId)
	if spawnId == nil or waitForConfirmSpawnAreas == nil then
		return
	end
	waitForConfirmSpawnAreas[spawnId] = nil
end

-- BOSS出现
local function onBossComing()
	if bossComingEff == nil or bossComingEff.thingPlayer.isAvatarValid then
		bossComingEff = UIEffect:create()
		bossComingEff:setLoadOverCallBack(function()
			if bossComingEff then
				bossComingEff:playEffect(bossComingEff.timeLen)
			end
		end)
		bossComingEff:init(FightDefine.BOSS_COMING_EFFECT)
		local scale = GameUtil.winScale
		bossComingEff:setScale(Vector3.New(scale, scale, scale))
		bossComingEff:setVisible(true, Vector3.New(0, 0, 0), MainPanel.uiMiddle.transform)
	else
		bossComingEff:playEffect(bossComingEff.timeLen)
	end
	AudioMgr.PlaySoundInfoUI("sound_ui_boss_coming")
	local soundId = FightModel:getFightCommon(FightDefine.FIGHT_COMMON_BOSSCOMING_MUSIC)
	if soundId and soundId ~= "0" then
		AudioMgr.PlaySoundInfoFight(soundId)
	end
	local shakeStr = FightModel:getFightCommon(FightDefine.FIGHT_COMMON_BOSSCOMING_SHAKE)
	if shakeStr and shakeStr ~= "0" then
		local shakeArr = StringUtils.split(shakeStr, "+", nil, tonumber)
		local delay = shakeArr[2] or 0
		delay = math.floor(delay * 0.001 * ConstantData.FRAME_RATE)
		if delay <= 0 then
			CameraManager:shakeCam(shakeArr[1])
		else
			local delayShake = function()
				if bossComingEff == nil or isReady == false then return end
				CameraManager:shakeCam(shakeArr[1])
			end
			FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(delay, 1, delayShake)
		end
	end
end

local function startSpawnMonsters(spawnList, diffTime)
	local bossSn = nil
	local bossSpawnId = nil	-- 这组怪的刷怪组ID
	local monsterVo = nil
	for _,m in pairs(spawnList) do
		monsterVo = FightModel:getMonsterVo(m.configId)
		if monsterVo.monsterType == CharacterConstant.MONSTER_TYPE.BOSS then
			bossSn = m.uniqueID
			bossSpawnId = m.monsterSpawnId
		end
	end
	diffTime = diffTime or 0
	for _,m in pairs(spawnList) do
		if m.monsterSpawnId then
			spawnedGroupDic[m.monsterSpawnId] = 1
		end
		if m.uniqueID then
			spawnedMonsterUIDs[m.uniqueID] = 1
		end
		if bossSpawnId and m.monsterSpawnId == bossSpawnId then
			m.delay = m.delay or 0
			m.delay = m.delay + FightModel:getFightCommon(FightDefine.FIGHT_COMMON_BOSSCOMING)
			if m.uniqueID == bossSn then
				ModuleEvent.dispatch(ModuleConstant.BOSS_COMING, m.monsterid)
			end
		end
		if m.delay == nil or m.delay <= 0 or m.delay <= diffTime then
			spawnMonster(m)
		else
			m.spawnTime = (GameNet.GetServerTime() * 0.001) + ((m.delay - diffTime) * 0.001)
			table.insert(delaySpawnList, m)
		end
	end
end

-- 初始化进关卡后需要马上创建的怪物
local function initMonsters()
	if EnvironmentHandler.isPvpClient then
		if waitToSpawnServerMonsters then
			for i,m in pairs(waitToSpawnServerMonsters) do
				spawnMonster(m)
			end
		end
		return
	end
	local monsters = FightModel:getFightData():getData(FightDefine.DATA_INIT_MONSTERS)
	if monsters then
		startSpawnMonsters(monsters, 0)
	end
end

-- 怪物死亡时，记录怪物sn号
local function onCharacterDead(uniqueId)
	if isReady == false then return end
	local character = CharacterManager:getCharacByUId(uniqueId)
	if character == nil then return end
	if character.characterType == CharacterConstant.TYPE_MONSTER then
		monsterDeadDic[character.uniqueID] = 0
		if character.drop and #character.drop > 0 then
			ModuleEvent.dispatchWithFixedArgs(ModuleConstant.PLAY_DROP, character.uniqueID, character.drop)
            for key,object in ipairs(character.drop) do
                if object.itemId == 5 then --人物经验
                    ModuleEvent.dispatch( ModuleConstant.FIGHTER_ADDEXP, object.count ) 
                elseif object.itemId == 999 then --伙伴经验
                    ModuleEvent.dispatch( ModuleConstant.PARTNER_ADDEXP, object.count ) 
                end
            end
		end
		releaseShowDrop(character.uniqueID)

	elseif character.characterType == CharacterConstant.TYPE_SELF then
		if EnvironmentHandler.isPvpClient == false and StageManager.getCurStageType() ~= ConstantData.STAGE_TYPE_TOKEN_DUNGEON then
			MonsterSpawnProtocal.tellServerRoleDead()
		end
	end
end

local function onFightFinish()
	isReady = false
end

local waitToSpawnServerMonsters = nil
local function parseSpawnMonster(param)
	local monsterInfo = nil
	local initMonsters = FightModel:getFightData():getData(FightDefine.DATA_INIT_MONSTERS)
	if initMonsters and initMonsters[param[1]] then
		monsterInfo = initMonsters[param[1]]
	else
		monsterInfo = FightModel:getFighterVo(param[1])
	end
	if monsterInfo == nil or UnHandleInfoBeforeLoaded.isDeadBeforeLoadDone(param[1]) then
		return
	end

	if StageManager.isStageReady() == false then
		if waitToSpawnServerMonsters == nil then
			waitToSpawnServerMonsters = {}
		end
		waitToSpawnServerMonsters[param[1]] = monsterInfo
	else
		spawnMonster(monsterInfo)
	end
end

-- 服务器通知刷怪
-- spawnList 怪物列表
function MonsterSpawnManager:doSpawnMonsters(spawnList)
	if spawnList == nil then return end
	if isReady == false then
		 local initMonsters = FightModel:getFightData():getData(FightDefine.DATA_INIT_MONSTERS)
		 for _,m in pairs(spawnList) do
			table.insert(initMonsters, m)
		 end
		 return
	end
	startSpawnMonsters(spawnList)
end

-- 某组怪是否已经刷过了
function MonsterSpawnManager.isSpawned(spawnId)
	if spawnId == nil or spawnedGroupDic == nil or spawnedGroupDic[spawnId] == nil then
		return false
	end
	return true
end

function MonsterSpawnManager.isMonsterSpawned(uid)
	if uid == nil or spawnedMonsterUIDs == nil or spawnedMonsterUIDs[uid] == nil then
		return false
	end
	return true
end

local function emptyFunc()end

-- 初始化：监听事件、启动帧定时器
function MonsterSpawnManager:init()
	if EnvironmentHandler.isInServer == false then
		MonsterSpawnProtocal.register()
		ModuleEvent.addListener(ModuleConstant.CHARACTER_DEAD, onCharacterDead)
		ModuleEvent.addListener(ModuleConstant.FIGHT_FINISH, onFightFinish)
		ModuleEvent.addListener(ModuleConstant.BOSS_COMING, onBossComing)
		ModuleEvent.addListener(ModuleConstant.SPAWN_MONSTER, emptyFunc, parseSpawnMonster)
	end
	fixDelayTime = GameNet.GetServerTime()
end

-- 开始执行刷怪逻辑（完全进入场景之后）
function MonsterSpawnManager:start()
	local spawnAreaDic = FightModel:getFightData():getData(FightDefine.DATA_MONSTER_SPAWN_AREA)
	if spawnAreaDic then
		spawnAreas = {}
		for _, spawn in pairs(spawnAreaDic) do
			table.insert(spawnAreas, spawn)
		end
	end
	monsterDeadDic = {}
	delaySpawnList = {}
	spawnedGroupDic = {}
	spawnedMonsterUIDs = {}
	fixDelayTime = GameNet.GetServerTime() - fixDelayTime
	initMonsters()
	isReady = true
end

-- 清除
function MonsterSpawnManager:clear()
	ModuleEvent.removeListener(ModuleConstant.CHARACTER_DEAD, onCharacterDead)
	ModuleEvent.removeListener(ModuleConstant.FIGHT_FINISH, onFightFinish)
	ModuleEvent.removeListener(ModuleConstant.BOSS_COMING, onBossComing)
	ModuleEvent.removeListener(ModuleConstant.SPAWN_MONSTER, emptyFunc)
	ModuleEvent.removeListener(ModuleConstant.CHARAC_UPDATE_ATTR,onShowDrop)
	lastReconnectState = nil
	spawnAreas = nil
	monsterDeadDic = nil
	delaySpawnList = nil
	waitForConfirmSpawnAreas = nil
	fixDelayTime = 0
	reconfirmDeadFrame = 0
	reconfirmSpawnAreaFrame = 0
	deadFrame = 0
	isReady = false;
	if bossComingEff then
		bossComingEff:dispose()
		bossComingEff = nil
	end
	spawnedGroupDic = nil
	spawnedMonsterUIDs = nil
	showDropDic = nil
end