--
-- Created by linzewei
-- User: linzewei
-- Date: 2015/9/29
-- Time: 11:00
-- To change this template use File | Settings | File Templates.
--

StateBase = {
	stateAni = nil,
	stateTime = nil,
	curTime = nil,
}
StateBase = createClass(StateBase)

function StateBase:setMachine( stateMachine ) 		--设置状态机目标，用于监听参数变动以及状态跳转
	-- body
	self.machine = stateMachine
	self.target = self.machine.target
	self.targetView = self.target.view
	self:initState()
	return self
end

function StateBase:initState()
	-- 
end

function StateBase:crossFade( animaName, duration ) 
	-- body
	if CFG.animatorClipArrTimeCheck.configs[self.target.model][animaName] == nil then
        local msg = "缺少模型动作时间配置=model:"..tostring(self.target.model).." 动作:"..tostring(animaName)
        if GameUtil.isAndroidPlatform or GameUtil.isIosPlatform then
			local errorMsg = string.format("[LuaException]:%s->%s%s%s%s%s",msg,"\n",debug.traceback(),"\n",TimeUtils.GetDateTime(),GameUtil.versionContent)
            SDKManager.Instance:updateErrorMsg(errorMsg)
        else
        	LogManager.LogError(msg)
        end
	end
	self.stateTime = CFG.animatorClipArrTimeCheck.configs[self.target.model][animaName] * ConstantData.FRAME_RATE
	if self.target then
		self.target:playAni(animaName,duration)
	end
	-- if self.targetView ~= nil then
	-- 	self.targetView:playAni( animaName, duration )
	-- end
end

function StateBase:pause( )
	-- body
end

function StateBase:resume( )
	-- body
end

function StateBase:onStateActive(  )
	-- body
end

function StateBase:onStateDeative(  )
	-- body
end

function StateBase:onStateUpdate(  )

end

function StateBase:getStateTime()
	-- body
	return self.stateTime
end

function StateBase:onNextState()
	-- body
	if self.target.hp > 0 then
		self.target:switchState( CharacterConstant.STATE_IDLE )
	else
		self.target:switchState( CharacterConstant.STATE_DEAD )
	end
end