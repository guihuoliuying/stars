--region BuffEffectInvincible.lua
--Date 2016/12/01
--Author zhouxiaogang
-- BUFF效果：无敌类
--endregion

--[[
	effectInfo = 
	{
		effectType = nil,
		rate = nil,
		value = nil,
	}
]]

BuffEffectDurationAdd = 
{
	isForceRecalcAllEffect = true,
}

createClassWithExtends(BuffEffectDurationAdd, BuffEffectBase)

function BuffEffectDurationAdd:doEffect()
	
end

-- 计算某角色属性类buff效果的总属性加成
function BuffEffectDurationAdd.recalcAllEffect(charaBuff, effects)
	local fixedDuration = 0
	local percentDuration = 0
	if effects then
		for k,eff in pairs(effects) do 
			fixedDuration = fixedDuration + eff.effectInfo.value
			percentDuration = percentDuration + eff.effectInfo.rate
		end
	end
	if charaBuff  then
		charaBuff.extraFixedBuffDuration = fixedDuration
		charaBuff.extraPercentBuffDuration = percentDuration
	end
end