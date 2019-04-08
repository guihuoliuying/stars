--region BuffEffectBuffClean.lua
--Date 2017/04/05
--Author zhouxiaogang
-- BUFF效果：驱散净化类BUFF
--endregion

--[[
	effectInfo = 
	{
		effectType = nil,
		cleanTypes = nil,
	}
]]

BuffEffectBuffClean = 
{
	isForceRecalcAllEffect = true,
}

createClassWithExtends(BuffEffectBuffClean, BuffEffectBase)

function BuffEffectBuffClean:onBuffStart()
	self.cleanTypes = self.effectInfo.cleanTypes
	-- 清除指定类型的buff
	if self.cleanTypes and self.refBuff and self.refBuff.target then
		if BuffManager.isDebug then
			CmdLog("执行驱散净化类BUFF效果[target=" .. tostring(self.refBuff.targetUID) .. ",buffid=" .. tostring(self.refBuff.buffData.buffId) .. ",bufflv=" .. tostring(self.refBuff.buffData.bufflv) .. "]")
		end
		local charaBuff = self.refBuff.target.charaBuff
		if charaBuff == nil then return end
		local buffList = nil
		local instanceId = self.refBuff.instanceId
		for _,buffType in pairs(self.cleanTypes) do
			buffList = charaBuff:getBuffByType(buffType)
			if buffList and #buffList > 0 then
				for k,buff in pairs(buffList) do
					if buff.instanceId ~= instanceId then
						ModuleEvent.dispatch(ModuleConstant.REMOVE_BUFF, {buff.targetUID, buff.instanceId})
					end
				end
			end
		end
	end
end

function BuffEffectBuffClean:onBuffFinish()
	self.cleanTypes = nil
end

function BuffEffectBuffClean:dispose()
	self.cleanTypes = nil
end


function BuffEffectBuffClean.recalcAllEffect(charaBuff, effects)
	if effects == nil then
		charaBuff.immuneBuffTypes = nil
	else
		if charaBuff.immuneBuffTypes == nil then
			charaBuff.immuneBuffTypes = {keys={},}
		else
			for k,v in ipairs(charaBuff.immuneBuffTypes.keys) do
				charaBuff.immuneBuffTypes[v] = false
			end
		end
		local keys = charaBuff.immuneBuffTypes.keys
		for k,v in pairs(effects) do
			if v.effectInfo.cleanTypes then
				for _,tp in pairs(v.effectInfo.cleanTypes) do
					if charaBuff.immuneBuffTypes[tp] == nil then
						table.insert(keys, tp)
					end
					charaBuff.immuneBuffTypes[tp] = true
				end
			end
		end
	end
end