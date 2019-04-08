--
-- Created by linzewei
-- User: linzewei
-- Date: 2015/9/29
-- Time: 11:00
-- To change this template use File | Settings | File Templates.
--
IdleState = createClassWithExtends(IdleState, StateBase)

function IdleState:initState(  )    --初始化，避免创建时监听不到参数变化
	-- body
end

function IdleState:onStateActive( preState ) --状态激活
	-- body
	local action = nil
	if AIManager.hasAI(self.target.uniqueID) then
        action = AIManager.getIdleActionFromAI(self.target.uniqueID)
    end
    if action == nil or action == "0" then
        action = CharacterConstant.ANIMATOR_IDLE
    end
	self:crossFade( action )
	self.hurtTime = nil
    self.target:changeProperty("defaultPosition",self.target.position)
	if preState ~= nil and (preState == CharacterConstant.STATE_HITBACK or preState == CharacterConstant.STATE_STANDUP) then
	 	if AIManager.hasAI(self.target.uniqueID) then
       		ModuleEvent.dispatchWithFixedArgs( ModuleConstant.AI_BREAK_STATE, self.target.uniqueID, 1)
       	end
    end
end

function IdleState:onStateDeative(  )  --状态休眠
	-- body
	if self.target.stopMove ~= nil then
		self.target:stopMove()
	end
end

function IdleState:onStateUpdate()
	if self.hurtTime ~= nil then
		self.hurtTime = self.hurtTime + 1
		if self.hurtTime >= self.stateTime then
			self.hurtTime = nil
			if self.target.movingKey == nil then
				self:crossFade( CharacterConstant.ANIMATOR_IDLE )
			end
		end
	end
end

function IdleState:onIdleHurt( direction )
	-- body
	if self.target.stopMove ~= nil then
		self.target:stopMove()
	end
	self.hurtTime = 0
	self:crossFade( CharacterConstant.ANIMATOR_HURT )
	if direction ~= nil and direction ~= Vector3.zero then
		local hitDirection = Quaternion.LookRotation(Vector3.zero - direction)
		if hitDirection ~= nil then
			self.target:setRotation( hitDirection )
		end
	end
end