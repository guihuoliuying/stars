--region RuneSpawnPoint.lua
--Date 2017/01/04
--Author zhouxiaogang
--Desc 神符刷新点
--endregion

RuneSpawnPoint = {}
createClass(RuneSpawnPoint)

function RuneSpawnPoint:init(spawnInfo, spawnSeq)
	self.spawnInfo = spawnInfo
	self.spawnSeq = spawnSeq
	self.spawnId = spawnInfo.runespawnid
	if spawnInfo.position and spawnInfo ~= "0" then
		self.position = Vector3.NewByStr(spawnInfo.position) * 0.1
	else
		self.position = Vector3.zero
	end

	self.totalWeight = 0
	self.randomRuneList = {}
	if spawnInfo.rune then
		local arr = StringUtils.split(spawnInfo.rune, ",")
		local tempRune = nil
		local startVal = 0
		for k,v in ipairs(arr) do
			tempRune = StringUtils.split(v, "+", nil, tonumber)
			table.insert(self.randomRuneList, {runeId = tempRune[1], weightMin = self.totalWeight, weightMax = self.totalWeight + tempRune[2] - 1})
			self.totalWeight = self.totalWeight + tempRune[2]
		end
	end
	self.nextSpawnTime = 0
	self.sqrRadius = self.spawnInfo
end

-- 显示刷新点特效
function RuneSpawnPoint:showSpawnEffect()
	if self.spawnEff == nil and self.spawnInfo.stagemap ~= "0" then
		self.spawnEff = StageManager.getThingPool():getThingCallback(self.spawnInfo.stagemap, UrlManager.SKILL, function(thing) 
			if thing and thing.isAvatarValid then
				thing:setLocation(self.position.x, self.position.y, self.position.z)
				thing:setActive(true)
				thing.avatarName = "runespawn_" .. tostring(self.spawnInfo.runespawnid)
			end
		end)
	end
end

-- 随机一个神符ID出来，用于创建
function RuneSpawnPoint:randomOneRune()
	local count = #self.randomRuneList
	if count == 0 then return nil end
	if count == 1 then return self.randomRuneList[1] end
	local randomVal = math.random(0, self.totalWeight)
	for k,rune in ipairs(self.randomRuneList) do
		if randomVal <= rune.weightMax and randomVal >= rune.weightMin then
			return rune
		end
	end
	return nil
end

-- 在这个刷点上创建一个神符
function RuneSpawnPoint:createOneRune(runeId, instanceId)
	local runeCfg = CFG.rune:get(runeId)
	if runeCfg == nil then
		LogManager.LogError("RuneSpawnPoint:createOneRune():找不到runeId为" .. tostring(runeId) .. "的rune配置")
		return
	end
	if self.runeObj then
		self.runeObj:dispose()
	end
	self.runeObj = RuneObject:create()
	self.runeObj:init(runeCfg, instanceId, self.position)
	if EnvironmentHandler.isInServer == false then
		self:showSpawnEffect()
	end
end

function RuneSpawnPoint:update(serverTimeNow)
	if self.nextSpawnTime then
		if self.nextSpawnTime == 0 or serverTimeNow >= self.nextSpawnTime then
			local runeInfo = self:randomOneRune()
			if runeInfo then
				ModuleEvent.dispatch(ModuleConstant.RUNE_CREATE, {self.spawnInfo.runespawnid, runeInfo.runeId, RuneSpawnManager.createRuneInstanceId()})
			end
			self.nextSpawnTime = nil
		end	
	end
	if self.runeObj then
		local hitUniqueId = self.runeObj:checkPlayerIsInArea()
		if hitUniqueId then
			ModuleEvent.dispatch(ModuleConstant.RUNE_PICKUP, {self.spawnInfo.runespawnid, self.runeObj.runeData.runeid, hitUniqueId, self.runeObj.instanceId})
		end
	end
end

-- 拾取神符
function RuneSpawnPoint:pickUp()
	if self.runeObj then
		-- 非服务端环境时做些效果表现
		if EnvironmentHandler.isInServer == false then
			local pickEffName = self.runeObj.runeData.pickupeffect
			if pickEffName and pickEffName ~= "0" then
				local loadDoneFunc = function()
					self.pickEff:setLocation(self.position.x, self.position.y, self.position.z)
					self.pickEff:setActive(true)
					local effTimeLen = self.pickEff.effectTimeLen
					if effTimeLen then
						self.pickEffFrameKey = FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(effTimeLen * ConstantData.FRAME_RATE, 1, function()
							if self.pickEff then
								StageManager.getThingPool():poolThing(self.pickEff)
							end
						end)
					end
				end
				if self.pickEffFrameKey then
					FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):removeLuaFunc(self.pickEffFrameKey)
					self.pickEffFrameKey = nil
				end
				self.pickEff = StageManager.getThingPool():getThingCallback(pickEffName, UrlManager.SKILL, function(thing) 
					if thing and thing.isAvatarValid then
						self.pickEff = thing
						loadDoneFunc()
					end
				end)
			end
		end
		self.runeObj:dispose()
		self.runeObj = nil
	end
	if self.spawnInfo.rechargetime <= 0 then
		ModuleEvent.dispatch(ModuleConstant.RUNESPAWNPOINT_REMOVE, {self.spawnInfo.runespawnid, self.spawnSeq})
	else
		self.nextSpawnTime = GameNet.GetServerTime() * 0.001 + self.spawnInfo.rechargetime
	end
end

function RuneSpawnPoint:dispose()
	if self.runeObj then
		self.runeObj:dispose()
		self.runeObj = nil
	end
	if self.spawnEff then
		StageManager.getThingPool():poolThing(self.spawnEff)
		self.spawnEff = nil
	end
	if self.pickEff then
		StageManager.getThingPool():poolThing(self.pickEff)
		self.pickEff = nil
	end
	if self.pickEffFrameKey then
		FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):removeLuaFunc(self.pickEffFrameKey)
		self.pickEffFrameKey = nil
	end
	self.spawnInfo = nil
end


