--region BuffEffectHpPercent.lua
--Date 2017/04/05
--Author zhouxiaogang
-- BUFF效果：百分比治疗类（可增可减）
--endregion

--[[
	effectInfo = 
	{
		effectType = nil,
		rate = nil,		-- 千分比
		targetType = nil,	-- 目标类型 1 为Buff施法者  2为Buff拥有者
		interval = nil,	-- 间隔时间
	}
]]

BuffEffectHpPercent = {}
createClassWithExtends(BuffEffectHpPercent, BuffEffectBase)

function BuffEffectHpPercent:onBuffStart()
	self.sourceAttack = self.refBuff.attacker[ATTR_KEY.ATTACK]
	self.targetType = self.effectInfo.targetType
end

-- 执行一次治疗效果
function BuffEffectHpPercent:doEffect()
	if self.refBuff == nil then return end
	local target = nil
	if self.targetType == 1 then
		target = CharacterManager:getCharacByUId(self.refBuff.attackerUID)
	elseif self.targetType == 2 then
		target = self.refBuff.target
	else
		LogManager.LogError("百分比治疗类buff目标类型配置错误![buffId=" .. self.refBuff.buffData.buffId .. "]")
		return
	end
	local cureTarget = self.refBuff.target
	if target == nil or cureTarget == nil or cureTarget.hp <= 0 then
		return	-- 已死
	end
	local cureHp = target.maxhp * (self.effectInfo.rate * 0.001)
	cureHp = cureHp * self.refBuff.curLayer
	cureHp = math.ceil(cureHp)
	if cureHp == 0 then
		return
	end
	local newHp = cureHp + cureTarget.hp
	if newHp > cureTarget.maxhp then
		newHp = cureTarget.maxhp
	end
	if newHp < 0 then
		newHp = 0
	end
	local numberType = self.refBuff.buffData.damageNumberType
	if self.numberInfo == nil then
		self.numberInfo = {}
		self.numberInfo.numFlag = FightDefine.FIGHT_NUM_FLAG.CURE
		self.numberInfo.numSign = 1
	end
	self.numberInfo.numType = numberType
	self:changeHp(self.refBuff.targetUID, cureHp, newHp, 1, self.numberInfo)
	if BuffManager.isDebug then
		CmdLog("执行百分比治疗效果[target=" .. tostring(self.refBuff.targetUID) .. ",buffid=" .. tostring(self.refBuff.buffData.buffId) .. ",bufflv=" .. tostring(self.refBuff.buffData.bufflv) .. ",cureHp=" .. tostring(cureHp) .. "]")
	end
end

function BuffEffectHpPercent:dispose()
	BuffEffectBase.dispose(self)
	self.numberInfo = nil
end