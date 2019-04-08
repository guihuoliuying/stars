-- region
-- Date    : 2016-06-27
-- Author  : daiyaorong
-- Description :  
-- endregion

StandupState = createClassWithExtends(StandupState, StateBase)

function StandupState:onStateActive()
	self.stateAni = CharacterConstant.ANIMATOR_STAND
	self:crossFade( self.stateAni )
	self.curTime = 0
end

function StandupState:onStateDeative(  )
	-- body
	self.target:setCanMove( true )
end

function StandupState:onStateUpdate()
	-- body
	self.curTime = self.curTime + 1
	if self.curTime >= self.stateTime then
		self:onNextState()
	end
end