--region BuffEffectCTRL.lua
--Date 2016/07/15
--Author zhouxiaogang
-- BUFF效果:控制类
--endregion

--[[
	effectInfo = 
	{
		effectType = nil,
		controls = nil, -- 控制列表
	}
]]

BuffEffectCTRL = {
	isForceRecalcAllEffect = true,
}

createClassWithExtends(BuffEffectCTRL, BuffEffectBase)

function BuffEffectCTRL:doEffect()
	local target = self.refBuff.target
	for _,cType in pairs(self.effectInfo.controls) do
		if cType == CharacterConstant.ABILITY.NORMAL_SKILL then
			if target.skillstate == SKILLSTATE_NORMAL then
				ModuleEvent.dispatch( ModuleConstant.SKILL_DESTROY, target.uniqueID)
				target:switchState( CharacterConstant.STATE_IDLE )
			end
		elseif cType == CharacterConstant.ABILITY.SKILL then
    		if target.state == CharacterConstant.STATE_HITBACK or target.state == CharacterConstant.STATE_HITFLY then
                return
            end
			ModuleEvent.dispatch( ModuleConstant.SKILL_DESTROY, target.uniqueID)
			target:switchState( CharacterConstant.STATE_IDLE )
		end
	end
end

-- 计算某角色身上全部控制类buff的叠加效果
function BuffEffectCTRL.recalcAllEffect(charaBuff, effects)
	if charaBuff.controls == nil then
		charaBuff.controls = {}
	end
	for k,v in pairs(CharacterConstant.ABILITY) do
		charaBuff.controls[v] = 1
	end
	if effects then
		for _,eff in pairs(effects) do
			for k,v in pairs(eff.effectInfo.controls) do
				charaBuff.controls[v] = 0
			end
		end
	end
end


