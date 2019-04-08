--region BuffEffectGetDemageHp.lua
--Date 2017/06/07
--Author qisen
-- BUFF效果： 吸收加血类buff
--endregion

--[[
	effectInfo = 
	{
		rate = nil,		-- 千分比
	}
]]

BuffEffectGetDemageHp = {}
createClassWithExtends(BuffEffectGetDemageHp, BuffEffectBase)

function BuffEffectGetDemageHp:onBuffStart()
	self.Rate = self.effectInfo.rate
end

function BuffEffectGetDemageHp:overlay()
	if self.refBuff then
		self.Rate = self.Rate * self.refBuff.curLayer
	end
end

function BuffEffectGetDemageHp.recalcAllEffect(charaBuff, effects)
	if effects and #effects>0 then
		charaBuff.buffGetDemageHpRate = 0
		for k,eff in pairs(effects) do
			if eff.Rate then
				charaBuff.damageNumberType2 = eff.refBuff.buffData.damageNumberType
				charaBuff.buffGetDemageHpRate = charaBuff.buffGetDemageHpRate + eff.Rate
			end
		end
	else
		charaBuff.buffGetDemageHpRate = nil
	end
end