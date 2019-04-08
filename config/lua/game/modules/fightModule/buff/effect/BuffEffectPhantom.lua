--region BuffEffectPhantom.lua
--Date 2016/07/18
--Author zhouxiaogang
-- BUFF效果：幻影类
--endregion

--[[
	effectInfo = 
	{
		effectType = nil,
		rate = nil,		-- 伤害比率
	}
]]

BuffEffectPhantom = {}

createClassWithExtends(BuffEffectPhantom, BuffEffectBase)

function BuffEffectPhantom:doSecondDamage(targetId, params)
	local target = CharacterManager:getCharacByUId(targetId)
	if target == nil then return end
	local damageRate = (self.effectInfo.rate * 0.001) * self.refBuff.curLayer
	local delay = FightModel:getFightCommon(FightDefine.FIGHT_COMMON_SHADOWDAMAGE)
	if delay then
		delay = delay * 0.001 * ConstantData.FRAME_RATE
		delay = math.ceil(delay) 
	end
	local numType = self.refBuff.buffData.damageNumberType or 1
	local delayFunc = function()
		params[9] = damageRate
		params[10] = numType
		params[11] = 1
		ModuleEvent.dispatch( ModuleConstant.SKILL_MAKEEFFECT, params)
	end
	if delay then
		FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(delay, 1, delayFunc)
	else
		delayFunc()
	end
end

function BuffEffectPhantom:onBuffStart()
	BuffEffectBase.onBuffStart(self)
	if self.refBuff == nil or self.refBuff.target == nil then
		return
	end
	local ghostCode = FightModel:getFightCommon(FightDefine.FIGHT_COMMON_GHOSTSHADOW)
	if ghostCode == nil or ghostCode == "0" then
		return
	end
	local ghostArr = StringUtils.split(ghostCode, "+", nil, tonumber)
	local interval = ghostArr[1] or 0
	local alpha = ghostArr[2] or 0
	local perTime = ghostArr[3] or 0
	local duration = self.refBuff.buffData.duringTime
	if duration == -1 then
		duration = 9999999
	else
		duration = duration * 0.001
	end
	self.refBuff.target:showGhostShadow(interval * 0.001, duration, perTime * 0.001, alpha / 255)
end

function BuffEffectPhantom:dispose()
	if self.refBuff and self.refBuff.target then
		self.refBuff.target:removeGhostShadow()
	end
	BuffEffectBase.dispose(self)
end