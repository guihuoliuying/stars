--region BuffBase.lua
--Date 2016/07/13
--Author zhouxiaogang
-- BUFF基类
--endregion


BuffBase = 
{
	instanceId = nil,		-- buff唯一ID
	buffData = nil,			-- buff数据
	remainTime = nil,		-- buff结束时间
	effects = nil,			-- buff效果列表
	curLayer = nil,			-- 当前叠加数
	attacker = nil,			-- buff的施加者
	target = nil,			-- buff的所有者
	targetUID = nil,		-- buff所有者的uniqueID
	attackerUID = nil,		-- buff施加者的uniqueID
	effectIdTable = nil,	-- 特效序列号table
	buffState = nil,		-- buff状态 1:初始化 2:运行
}

local STATE_READY 	= 1
local STATE_RUN		= 2

function BuffBase:create()
	local o = {}
	setmetatable(o, self)
	self.__index = self
	return o
end

function BuffBase:init(buffData, attacker, target)
	self.buffData = buffData
	self.curLayer = 1
	self.attacker = attacker
	self.target = target
	if self.attacker then
		self.attackerUID = self.attacker.uniqueID
	end
	if self.target then
		self.targetUID = self.target.uniqueID
	end
	self.effectIdTable = {}
	self:initEffects()
	self:resetDuration()
	self.buffState = STATE_READY
end

-- 初始化buff效果列表
function BuffBase:initEffects()
	self.effects = {}
	if self.buffData.buffInfo then
		for _,info in pairs(self.buffData.buffInfo) do
			self:addEffect(info, false)
		end
	end
end

-- 添加一个buff效果
-- effectInfo 效果参数
-- doItNow 是否马上执行一次效果
function BuffBase:addEffect(effectInfo, doItNow)
	local temp = BuffEffectFactory.createBuffEffect(effectInfo.effectType)
	if temp then
		temp:init(self, effectInfo)
		table.insert(self.effects, temp)
		if doItNow then
			temp:onBuffStart()
			temp:doEffect()
		end
	end
	return temp
end

-- 重置buff时间
function BuffBase:resetDuration()
	-- 不限时buff就给个有生之年系列的时间
	if self.buffData.duringTime == -1 then
		self.remainTime = 9999999999
	else
		self.remainTime = self.buffData.duringTime * 0.001
		local charaBuff = self.target.charaBuff
		if charaBuff then
			if charaBuff.extraPercentBuffDuration then
				self.remainTime = self.remainTime * (1 + charaBuff.extraPercentBuffDuration)
			end
			if charaBuff.extraFixedBuffDuration then
				self.remainTime = self.remainTime + self.target.charaBuff.extraFixedBuffDuration
			end
		end
	end
end

-- 开始执行buff
function BuffBase:startBuff(isDisplayEffect)
	self.buffState = STATE_RUN
	if isDisplayEffect ~= false then
		self:playBuffEffect()
	end
	-- BUFF开始时马上执行一下效果
	if EnvironmentHandler.isPvpClient == false then
		for _,eff in ipairs(self.effects) do
			eff:onBuffStart()
			eff:doEffect()
			if eff.intervalTime then
				eff:updateNextEffectiveTime()
			end
		end
	end
end

-- buff叠加或覆盖
function BuffBase:overlayBuff(otherBuffData)
	if otherBuffData == nil then return end
	-- 覆盖
	if self.buffData.maxLayer == 1 then
		self:resetDuration()
		return
	end
	-- 叠加
	if self.buffData.maxLayer > 1 and self.curLayer < self.buffData.maxLayer then
		self.curLayer = self.curLayer + 1
		if EnvironmentHandler.isPvpClient == false then
			for _,eff in ipairs(self.effects) do
				eff:overlay()
			end
		end
		self:resetDuration()
	end
end

-- 是否为同样的buff，只有判断为同样的buff，才可以进行叠加或覆盖
function BuffBase:isSameBuff(otherBuff, attackerUID)
	if otherBuff == nil or attackerUID ~= self.attackerUID then return false end
	if otherBuff.buffId == self.buffData.buffId and otherBuff.buffLv == self.buffData.buffLv then
		return true
	end
	return false
end

-- 是否相同id
function BuffBase:isSameId(otherBuff)
    if otherBuff == nil then return false end
    if otherBuff.buffId == self.buffData.buffId then
        return true
    end
    return false
end

-- 帧定时器
function BuffBase:update(deltaTime)
	self.remainTime = self.remainTime - deltaTime
	if self.remainTime <= 0 then
		self:buffFinish()
		return
	end
	if self.buffState == STATE_RUN and EnvironmentHandler.isPvpClient == false then
		for _,eff in ipairs(self.effects) do
			if eff.intervalTime and eff:checkEffectiveTime(deltaTime) then
				eff:doEffect()
				eff:updateNextEffectiveTime()
			end
		end
	end
end

-- 播放特效
function BuffBase:playBuffEffect()
	if EnvironmentHandler.isInServer then return end
	if self.buffData.buffEffect == nil then return end
	if self.effectIdTable == nil then
		self.effectIdTable = {}
	end
	for k,v in pairs(self.buffData.buffEffect) do
        local effectSID = SideEffectManager:getSerialNo()
        table.insert(self.effectIdTable,effectSID) 
        local params = SideEffectManager.getFreeTable()
        params.entityID = self.targetUID
        params.serialNo = effectSID
        params.effectResName = v.name
        params.boneName = v.bone
        params.effectType = FightDefine.EFFTYPE_FOLLOWROLE
        params.posOffset = v.offset
        ModuleEvent.dispatch(ModuleConstant.PLAY_SIDE_EFFECT, params)
	end
end

-- 停止播放特效
function BuffBase:stopBuffEffect()
	if self.effectIdTable then
	   for k,v in pairs(self.effectIdTable) do
	       ModuleEvent.dispatch(ModuleConstant.STOP_SIDE_EFFECT, v)
	   end
	end
	self.effectIdTable = nil
end

-- buff生命周期结束
function BuffBase:buffFinish()
	if self.effects == nil then return end
	for _,eff in ipairs(self.effects) do
		eff:onBuffFinish()
	end
	ModuleEvent.dispatch(ModuleConstant.REMOVE_BUFF, {self.targetUID, self.instanceId})
end

-- 移除buff效果
function BuffBase:removeEffect( index )
    if self.effects == nil then return end
    if self.effects[index] == nil then return end

    self.effects[index]:dispose()
    table.remove( self.effects, index )
end

-- 销毁buff
function BuffBase:dispose()
	for _,eff in ipairs(self.effects) do
		eff:dispose()
	end
	self:stopBuffEffect()
	self.effects = nil
	self.buffData = nil
end
