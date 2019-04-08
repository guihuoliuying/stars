-- region
-- Date    : 2016-06-27
-- Author  : daiyaorong
-- Description :  进攻状态
-- endregion

AttackState = createClassWithExtends(AttackState, StateBase)

function AttackState:onStateActive(  )
	self.duration = 0
	self.target:changeProperty( "superarmor", (self.target.defaultArmor+1) )
	if self.target.skillstate == CharacterConstant.SKILLSTATE_NORMAL then
		self.target:changeProperty( "superarmor", self.target.defaultArmor )	--普攻无法获得霸体
		self.duration = nil --普攻需要动作过渡
	elseif self.target.skillstate == CharacterConstant.SKILLSTATE_AVOID then
		self.target:changeProperty( "invincible", 1 )	--闪避时无敌
		self.target:changeProperty( "continueNormal", nil )
	else
		self.target:changeProperty( "continueNormal", nil )
	end
	self.target:fireSuccess()
end

function AttackState:onStateDeative(  )
	-- body
	local preSkillState = self.target.skillstate
	ModuleEvent.dispatch( ModuleConstant.SKILL_DESTROY, self.target.uniqueID)
	self.target:changeProperty( "skillstate", CharacterConstant.SKILLSTATE_NONE )
	self.target:changeProperty( "targetID", nil )	--置空目标标记
	self.target:changeProperty( "superarmor", self.target.defaultArmor )	--重置霸体
	self.target:changeProperty( "continueNormal", nil )	--重置普攻连招标记
	self.target:changeProperty( "usingSkill", nil )		--重置技能标记
	if self.target.characterType == CharacterConstant.TYPE_SELF or self.target.characterType == CharacterConstant.TYPE_PLAYER then
		if self.target.ctrlState then
			if self.target.ctrlState == FightDefine.CONTROL_MOVE then
	        	self.target:setFrameSpeed( self.target.movespeed ) --还原摇杆移动速度
        	elseif self.target.ctrlState == FightDefine.CONTROL_TURN then
        		self.target:changeProperty( "turnspeed", self.target.defaultTurnspeed )
        	elseif self.target.ctrlState == FightDefine.CONTROL_TURN_MOVE then
        		self.target:setFrameSpeed( self.target.movespeed ) --还原摇杆移动速度
        		self.target:changeProperty( "turnspeed", self.target.defaultTurnspeed )
    		end
	    end
		self.target:changeProperty( "ctrlState", FightDefine.CONTROL_DEFAULT )	--重置控制标记
		self.target:changeProperty( "attackIndex", 1 )		--重置普攻下标
		self.target:changeProperty( "invincible", 0 )		--重置无敌状态
	end
	if AIManager.hasAI(self.target.uniqueID) then
		ModuleEvent.dispatchWithFixedArgs(ModuleConstant.AI_BREAK_STATE, self.target.uniqueID, 2, preSkillState)
    end
end