-- region
-- Date    : 2016-06-27
-- Author  : daiyaorong
-- Description :  击飞状态
-- endregion

HitflyState = createClassWithExtends(HitflyState, StateBase)

function HitflyState:onStateActive()
	self.target:stopSearch()
	self.target:setCanMove( false )
	self.stateAni = CharacterConstant.ANIMATOR_FLY
	self:crossFade( self.stateAni, 0 )
end

function HitflyState:onNextState()
	-- body
	if self.target.hp > 0 then
		self.target:switchState( CharacterConstant.STATE_STANDUP )
	else
		self.target:switchState( CharacterConstant.STATE_DEAD )
	end
end