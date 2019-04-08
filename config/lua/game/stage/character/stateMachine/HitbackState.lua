-- region
-- Date    : 2016-06-27
-- Author  : daiyaorong
-- Description :  击退状态
-- endregion

HitbackState = createClassWithExtends(HitbackState, StateBase)

function HitbackState:onStateActive()
	self.target:stopSearch()
	self.target:setCanMove( false )
	self.stateAni = CharacterConstant.ANIMATOR_HURT
	self:crossFade( self.stateAni, 0 )
end

function HitbackState:onStateDeative(  )
	-- body
	self.target:setCanMove( true )
end

function HitbackState:onNextState()
	-- body
	if self.target.hp > 0 then
		StateBase.onNextState( self )
	else
		self.target:switchState( CharacterConstant.STATE_DEAD )
	end
end