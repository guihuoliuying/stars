--region BuffEffectDamageAdd.lua
--Date 2017/04/05
--Author zhouxiaogang
-- BUFF效果：伤害加成类(攻击者拥有该BUFF时对敌伤害增加)
--endregion

--[[
	effectInfo = 
	{
		effectType = nil,
		rate = nil,			-- 伤害千分比
		value = nil,		-- 固定伤害值
	}
]]

BuffEffectDamageAdd = {}

createClassWithExtends(BuffEffectDamageAdd, BuffEffectBase)

function BuffEffectDamageAdd:onBuffStart()
	self.addRate = self.effectInfo.rate * 0.001
	self.addValue = self.effectInfo.value
--	if self.refBuff then
--		CmdLog(string.format("charac=%s, buffid=%s, damagerate=%s, damagevalue=%s", self.refBuff.target.uniqueID, self.refBuff.buffData.buffId, self.addRate, self.addValue))
--	end
end

function BuffEffectDamageAdd:overlay()
	if self.refBuff then
		self.addRate = self.effectInfo.rate * 0.001 * self.refBuff.curLayer
		self.addValue = self.effectInfo.value * self.refBuff.curLayer
	end
--	if self.refBuff then
--		CmdLog(string.format("charac=%s, buffid=%s, damagerate=%s, damagevalue=%s", self.refBuff.target.uniqueID, self.refBuff.buffData.buffId, self.addRate, self.addValue))
--	end
end

function BuffEffectDamageAdd.recalcAllEffect(charaBuff, effects)
	if effects then
		charaBuff.addDamageRate = 0
		charaBuff.addDamageValue = 0
		for k,eff in pairs(effects) do
			if eff.addRate then
				charaBuff.addDamageRate = charaBuff.addDamageRate + eff.addRate
			end
			if eff.addValue then
				charaBuff.addDamageValue = charaBuff.addDamageValue + eff.addValue
			end
		end
	else
		charaBuff.addDamageRate = nil
		charaBuff.addDamageValue = nil
	end
end