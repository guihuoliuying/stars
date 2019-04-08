--region BuffEffectInvincible.lua
--Date 2016/12/01
--Author zhouxiaogang
-- BUFF效果：无敌类
--endregion

--[[
	effectInfo = 
	{
		effectType = nil,
	}
]]

BuffEffectInvincible = {}

createClassWithExtends(BuffEffectInvincible, BuffEffectBase)

function BuffEffectInvincible:doEffect()
	if self.refBuff and self.refBuff.target then
		self.refBuff.target:changeProperty("invincible", 1)
	end
end

-- 计算某角色属性类buff效果的总属性加成
function BuffEffectInvincible.recalcAllEffect(charaBuff, effects)
	local invincible = 0
	if effects then
		for k,eff in pairs(effects) do 
			invincible = 1;break;
		end
	end
	if charaBuff  then
		charaBuff.invincible = invincible
		if charaBuff.target then
			charaBuff.target:changeProperty("invincible", invincible)
		end
	end
end