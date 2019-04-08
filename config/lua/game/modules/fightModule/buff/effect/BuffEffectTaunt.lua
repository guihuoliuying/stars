--region BuffEffectTaunt.lua
--Date 2016/07/18
--Author zhouxiaogang
-- BUFF效果：嘲讽类
--endregion

--[[
	effectInfo = 
	{
		effectType = nil,
	}
]]

BuffEffectTaunt = {
	isForceRecalcAllEffect = true,
}

createClassWithExtends(BuffEffectTaunt, BuffEffectBase)

function BuffEffectTaunt:doEffect()
	-- 限制角色技能，生成控制类BUFF效果来实现
	local controlEffect = 
	{
		effectType = FightDefine.BUFF_EFFECT_TYPE.CTRL,
		controls = {CharacterConstant.ABILITY.SKILL},
	}
	local targetUID = self.refBuff.targetUID
	ModuleEvent.dispatchWithFixedArgs(ModuleConstant.ADD_BUFF_EFFECT, targetUID, self.refBuff.instanceId, controlEffect)
	if AIManager.hasAI(targetUID) == false then
		ModuleEvent.dispatch(ModuleConstant.AI_CREATE, targetUID)
	else
		ModuleEvent.dispatch(ModuleConstant.AI_START, targetUID)
	end
	ModuleEvent.dispatchWithFixedArgs(ModuleConstant.AI_CHANGE_PROPERTY, targetUID, "targetEnemy", self.refBuff.attacker)
end

function BuffEffectTaunt:onBuffFinish()
    BuffEffectBase.onBuffFinish(self)
    if AIManager.hasAI(targetUID) then
       ModuleEvent.dispatchWithFixedArgs(ModuleConstant.AI_CHANGE_PROPERTY, targetUID, "targetEnemy", nil)
	   if self.refBuff then
		   local target = self.refBuff.target
		   if target and (target.characterType == CharacterConstant.TYPE_PLAYER or target.characterType == CharacterConstant.TYPE_SELF) and (not self.refBuff.isAutoFight) then
				ModuleEvent.dispatch(ModuleConstant.AI_STOP, self.self.refBuff.target.uniqueID)
		   end
	   end
    end
end

local isJoystickCtrlByBuff = false
function BuffEffectTaunt.recalcAllEffect(charaBuff, effects)
	
	if effects and #effects > 0 then
		if charaBuff.target then
			charaBuff.target.isTaunt = true
		end
	else
		if charaBuff.target then
			charaBuff.target.isTaunt = false
		end
	end
	if EnvironmentHandler.isInServer then
		return
	end
	if charaBuff.target and charaBuff.target.uniqueID == RoleData.roleId then
		if effects and #effects > 0 then
			if isJoystickCtrlByBuff == false then
				Joystick.disableFrames = 99999999
				Joystick:resetJoystic()
				isJoystickCtrlByBuff = true
			end
		else
			if isJoystickCtrlByBuff then
				Joystick.disableFrames = 0
				isJoystickCtrlByBuff = false
			end
		end
	end
end