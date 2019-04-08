--region BuffEffectGetHp.lua
--Date 2016/07/15
--Author qisen
-- BUFF效果：吸血
--endregion

--[[
	effectInfo = 
	{
		rate = nil,		-- 千分比
		value = nil,	-- 固定值
	}
]]

BuffEffectGetHp = {}
createClassWithExtends(BuffEffectGetHp, BuffEffectBase)

function BuffEffectGetHp:onBuffStart()
	self.Rate = self.effectInfo.rate
	self.Value = self.effectInfo.value
end

function BuffEffectGetHp:overlay()
	if self.refBuff then
		self.Rate = self.Rate * self.refBuff.curLayer
		self.Value = self.Value * self.refBuff.curLayer
	end
end

function BuffEffectGetHp.recalcAllEffect(charaBuff, effects)
	if effects then
		charaBuff.buffGetHpRate = 0
		charaBuff.buffGetHpValue = 0
		for k,eff in pairs(effects) do
			charaBuff.damageNumberType = eff.refBuff.buffData.damageNumberType
			if eff.Rate then
				charaBuff.buffGetHpRate = charaBuff.buffGetHpRate + eff.Rate
			end
			if eff.Value then
				charaBuff.buffGetHpValue = charaBuff.buffGetHpValue + eff.Value
			end
		end
	else
		charaBuff.buffGetHpRate = nil
		charaBuff.buffGetHpValue = nil
	end
end