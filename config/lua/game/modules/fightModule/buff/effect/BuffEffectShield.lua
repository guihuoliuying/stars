--region BuffEffectShield.lua
--Date 2016/07/18
--Author zhouxiaogang
-- BUFF效果：护盾类
--endregion

--[[
	effectInfo = 
	{
		effectType = nil,
		rate = nil,
		value = nil,
	}
]]

BuffEffectShield = {}

createClassWithExtends(BuffEffectShield, BuffEffectBase)

function BuffEffectShield:onBuffStart()
	local maxHp = self.refBuff.target.maxhp
	self.shieldValue = maxHp * (self.effectInfo.rate * 0.001) + self.effectInfo.value
	self.shieldValue = math.ceil(self.shieldValue)
	self.curShieldValue = self.shieldValue
end

function BuffEffectShield:overlay()
	self.curShieldValue = self.curShieldValue + self.shieldValue
end

function BuffEffectShield:doShield(damage)
	self.curShieldValue = self.curShieldValue - damage
	local newDamage = 0
	if self.curShieldValue < 0 then
		newDamage = math.abs(self.curShieldValue)
	else
		newDamage = 0
	end
	
	local chgValue = damage - newDamage
	local numberType = self.refBuff.buffData.damageNumberType
	if self.numberEvtTb == nil then
		self.numberEvtTb = {}
	end
	self.numberEvtTb[1] = self.refBuff.targetUID
	self.numberEvtTb[2] = FightDefine.FIGHT_NUM_FLAG.ABSORB
	self.numberEvtTb[3] = chgValue
	self.numberEvtTb[4] = numberType or 1
	self.numberEvtTb[5] = 0
	self.numberEvtTb[6] = ""
	self.numberEvtTb[7] = 0
	self.numberEvtTb[8] = 0
	ModuleEvent.dispatch(ModuleConstant.SHOW_FIGHT_NUMBER, self.numberEvtTb)
	if self.curShieldValue < 0 then
		-- 策划要求:带护盾的BUFF，只要护盾效果消失，就干掉整个BUFF
		-- 销毁自己一定要放在最后面去做，不然可能会影响后续逻辑
		ModuleEvent.dispatch(ModuleConstant.REMOVE_BUFF, {self.refBuff.targetUID, self.refBuff.instanceId})
	end
	return newDamage
end

function BuffEffectShield:dispose()
	BuffEffectBase.dispose(self)
	self.numberEvtTb = nil
end