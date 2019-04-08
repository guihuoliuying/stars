--region BuffEffectSuperArmor.lua
--Date 2016/11/24
--Author zhouxiaogang
-- BUFF效果：霸体
--endregion

--[[
	effectInfo = 
	{
		effectType = nil,
		rate = nil,			-- 千分比
		value = nil,		-- 增加值
	}
]]

BuffEffectSuperArmor = {}

createClassWithExtends(BuffEffectSuperArmor, BuffEffectBase)

function BuffEffectSuperArmor:onBuffStart()
	self.curArmor = 0
	self.sourceArmor = self.refBuff.target.superarmor or 0
end

function BuffEffectSuperArmor:doEffect()
	self.curArmor = self.sourceArmor * (self.effectInfo.rate * 0.001) + self.effectInfo.value
	self.curArmor = self.curArmor * self.refBuff.curLayer
end

function BuffEffectSuperArmor:overlay()
	self:doEffect()
end

-- 计算某角色属性类buff效果的总属性加成
function BuffEffectSuperArmor.recalcAllEffect(charaBuff, effects)
	local superArmor = 0
	if effects then
		local attribName = nil
		for _,eff in pairs(effects) do
			superArmor = superArmor + eff.curArmor
		end
	end
	-- 把角色身上所有的霸体buff加起来
	charaBuff.superArmor = superArmor
end