--
-- Created by linzewei
-- User: linzewei
-- Date: 2015/9/29
-- Time: 11:00
-- To change this template use File | Settings | File Templates.
--

-- 状态机映射
local STATE_MAP = {
    [CharacterConstant.STATE_IDLE] = IdleState,
    [CharacterConstant.STATE_ATTACK] = AttackState,
    [CharacterConstant.STATE_HITBACK] = HitbackState,
    [CharacterConstant.STATE_HITFLY] = HitflyState,
    [CharacterConstant.STATE_STANDUP] = StandupState,
    [CharacterConstant.STATE_RUN] = RunState,
    [CharacterConstant.STATE_DEAD] = DeadState,
}

CharacterStateMachine = createClass(CharacterStateMachine)

function CharacterStateMachine:init( character )
 	-- body
 	self.target = character
 	self.stateDic = {}
 	self.curState = nil
 	self.curStateID = nil
 	self.nextState = nil
 	self.nextStateID = nil
 	self.enabled = true
 	self.runTime = 0
 	self.isRunning = false
 	self.speed    = 0
 end

 function CharacterStateMachine:startRunning()
 	self.isRunning = true
 	local function onFrame()
 		self:onFrame()
 	end
 	self.funcKey =  FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUACHARAC):RegisterLuaCallBack(1,0,onFrame)
 end

 function CharacterStateMachine:onFrame(  ) 		--帧驱动
 	if( self.enabled == false)then
 		return
 	end
 	if( self.nextState ~= nil and self.curState ~= self.nextState ) then 				--状态替换
 		if( self.curState ~= nil) then
 			self.preStateID = self.target.state
 			self.curState.isActive = false
 			self.curState:onStateDeative()
 		end
 		-- 在onStateDeative中存在覆盖本次转状态的情况 需加个空值判断
 		if self.nextState ~= nil and self.nextStateID ~= nil then
	 		self.curState = self.nextState
	 		self.curStateID = self.nextStateID
	 		self.target:changeProperty( "state", self.curStateID )
	 		self.curState.isActive = true
	 		self.curState:onStateActive( self.preStateID )
	 		self.nextState = nil
	 		self.nextStateID = nil
	 		self.preStateID = nil
	 	end
 	elseif(self.curState ~= nil)then
 		self.curState:onStateUpdate()
 	end
 	self.runTime = self.runTime + ConstantData.FRAME_DELTA_TIME
 end

 function CharacterStateMachine:pause( )
 	-- body
 	self.enabled = false
 	if( self.curState ~= nil) then 
 		self.curState:pause()
 	end
 end

 function CharacterStateMachine:resume(  )
 	-- body
 	self.enabled = true
 	if( self.curState ~= nil) then 
 		self.curState:resume()
 	end
 end

function CharacterStateMachine:stateTransition( state ) 	--节点跳转
	-- body
	if( self.stateDic == nil)then
		return
	end

	if( self.isRunning == false)then
		self:startRunning()
	end
	if self.nextStateID ~= nil and self.nextStateID == state then
		return
	end
	if( self.stateDic[state])then 		--拿缓存节点
		self.nextState = self.stateDic[state]
	else 								--新生成节点
		local stateNode = STATE_MAP[state]
		self.nextState = stateNode:create()
		self.stateDic[state] = self.nextState
		self.nextState:setMachine( self )
	end
	self.nextStateID = state
	if self.curState ~= nil and self.curState == self.nextState then
		self.curState:onStateActive()
		self.nextState = nil
		self.nextStateID = nil
	else
		self:onFrame()
	end
end

-- 状态结束的操作
function CharacterStateMachine:stateEnd()
	-- body
	if self.curState == nil then
		return
	end
	self.curState:onNextState()
end

function CharacterStateMachine:invoke(funName,param)
	if self.curState and self.curState[funName] then
		self.curState[funName](self.curState,param)
	end
end

function CharacterStateMachine:dispose(  )
	-- body
	if( self.stateDic ~= nil) then
		self.stateDic = nil
	end
	self.curState = nil
	self.nextState = nil
	if(  self.funcKey ~= nil and self.funcKey > 0 ) then 		--干掉帧驱动
		FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUACHARAC):removeLuaFunc(self.funcKey)
	end
end