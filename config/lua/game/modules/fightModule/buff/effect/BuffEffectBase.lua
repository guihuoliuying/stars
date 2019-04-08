--region BuffEffectBase.lua
--Date 2016/07/13
--Author zhouxiaogang
-- BUFF效果基类
--endregion

BuffEffectBase = 
{
	effectType = nil,
	intervalTime = nil,
	nextEffectiveTime = nil,
	refBuff = nil,
	isForceRecalcAllEffect = false,	-- 标记为true时，在任何环境（PVP，PVE等）都会执行recalcAllEffect方法
}

createClass(BuffEffectBase)
local numberEvtTb = nil

function BuffEffectBase:init(buff, effectInfo)
	self.refBuff = buff
	self.effectType = effectInfo.effectType
	self.effectInfo = effectInfo
	self.nextEffectiveTime = 0
	self.intervalTime = effectInfo.interval
end

function BuffEffectBase:updateNextEffectiveTime()
	if self.intervalTime then
		self.nextEffectiveTime = self.intervalTime * 0.001
	end
end

function BuffEffectBase:checkEffectiveTime(deltaTime)
	self.nextEffectiveTime = self.nextEffectiveTime - deltaTime
	if self.nextEffectiveTime <= 0 then
		return true
	end
	return false
end

-- 取该buff效果剩余的执行次数
function BuffEffectBase:getRemainExecuteTimes()
	if self.intervalTime == nil then
		return 0
	end
	local restSec = self.refBuff.remainTime
	if self.intervalTime == 0 then
		return math.floor(restSec * ConstantData.FRAME_RATE)
	end
	local times = math.floor(restSec / (self.intervalTime * 0.001))
	if times < 0 then times = 0 end
	return times
end

function BuffEffectBase:onBuffStart()

end

function BuffEffectBase:doEffect()
	
end

function BuffEffectBase:overlay()

end

function BuffEffectBase:onBuffFinish()

end

function BuffEffectBase:changeHp(uniqueID, changeHp, newHp, sign, numberInfo)
	if self.refBuff == nil then
		return
	end
	if self.refBuff.target.hp <= 0 then return end
	if newHp < 0 then newHp = 0 end
	local targetUID = self.refBuff.targetUID
	local attackerUID = self.refBuff.attackerUID
	if EnvironmentHandler.isInServer == false then
    	FixedTimeDispatcher.triggerDamageUpload(targetUID,uniqueID,sign,changeHp,newHp)
    	FixedTimeDispatcher.triggerDamageSave(attackerUID,uniqueID,sign,changeHp)
	end
	ModuleEvent.dispatch( ModuleConstant.CHARAC_UPDATE_ATTR, { uniqueID, ConstantData.ROLE_CONST_HP, newHp } )
	if EnvironmentHandler.isPvpClient == false and numberInfo then
		if numberEvtTb == nil then numberEvtTb = {} end
		numberEvtTb[1] = uniqueID
		numberEvtTb[2] = numberInfo.numFlag
		numberEvtTb[3] = changeHp
		numberEvtTb[4] = numberInfo.numType or 1
		numberEvtTb[5] = numberInfo.numSign or 0
		numberEvtTb[6] = ""
		numberEvtTb[7] = 0
		numberEvtTb[8] = 0
		ModuleEvent.dispatch(ModuleConstant.SHOW_FIGHT_NUMBER, numberEvtTb)
	end
    if EnvironmentHandler.isInServer and sign == 0 and attackerUID then --伤害才需要记录
        FightServerControl.setFrameRecord( attackerUID, uniqueID, changeHp, newHp ) 
    end
end

function BuffEffectBase:dispose()
	self.refBuff = nil
end


-- 外部方法，重新计算角色身上所有该类型buff叠加后的总效果, 不用考虑叠加的，子类可不重写该方法
function BuffEffectBase.recalcAllEffect(charaBuff, effects)

end