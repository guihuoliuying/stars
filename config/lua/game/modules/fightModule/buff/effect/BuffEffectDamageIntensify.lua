--region BuffEffectDamageIntensify.lua
--Date 2017/04/05
--Author zhouxiaogang
-- BUFF效果：伤害加深类(受击者拥有该BUFF时受击时伤害增加)
--endregion

--[[
	effectInfo = 
	{
		effectType = nil,
		rate = nil,			-- 伤害千分比
		value = nil,		-- 固定伤害值
	}
]]

BuffEffectDamageIntensify = {}

createClassWithExtends(BuffEffectDamageIntensify, BuffEffectBase)

function BuffEffectDamageIntensify:onBuffStart()
	self.intensifyRate = self.effectInfo.rate * 0.001
	self.intensifyValue = self.effectInfo.value
end

function BuffEffectDamageIntensify:overlay()
	if self.refBuff then
		self.intensifyRate = self.intensifyRate * self.refBuff.curLayer
		self.intensifyValue = self.intensifyValue * self.refBuff.curLayer
	end
end

function BuffEffectDamageIntensify.recalcAllEffect(charaBuff, effects)
	if effects then
		charaBuff.intensifyDamageRate = 0
		charaBuff.intensifyDamageValue = 0
		for k,eff in pairs(effects) do
			if eff.intensifyRate then
				charaBuff.intensifyDamageRate = charaBuff.intensifyDamageRate + eff.intensifyRate
			end
			if eff.intensifyValue then
				charaBuff.intensifyDamageValue = charaBuff.intensifyDamageValue + eff.intensifyValue
			end
		end
	else
		charaBuff.intensifyDamageRate = nil
		charaBuff.intensifyDamageValue = nil
	end
end