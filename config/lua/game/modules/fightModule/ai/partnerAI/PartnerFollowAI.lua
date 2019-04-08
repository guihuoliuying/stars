--region PartnerFollowAI.lua
--Date 2016/09/22
--Author zhouxiaogang
--Desc 宠物跟随AI
--endregion

PartnerFollowAI = {
	idleCD = 0,
}
createClass(PartnerFollowAI)

function PartnerFollowAI:init(chara, driveBySelf)
	self.chara = chara
	local commondef = CFG.commondefine:get('buddy_follow')
    local followTb = StringUtils.split(commondef.value,'+')
    self.followInterval  = tonumber(followTb[1] or 500) * 0.001 * ConstantData.FRAME_RATE
    self.percentInterval = tonumber(followTb[2] or 2000) * 0.001 * ConstantData.FRAME_RATE
    self.doActPercent    = tonumber(followTb[3] or 50)
    self.walkPosX        = tonumber(followTb[4] or 10) * 0.1
    self.walkPosZ        = tonumber(followTb[5] or 10) * 0.1
    self.followRadius    = tonumber(followTb[7] or 20) * 0.1
	self.sqrFollowRadius = self.followRadius * self.followRadius
	self.currState = AIState.NOTHING
	self:initParterPosition()
	self.idleCD = 0
	self.followCD = self.followInterval
	self.isDrivingBySelf = driveBySelf
	self.moveEvtTable = {}
	if driveBySelf then
		self.frameKey = FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_AI):RegisterLuaCallBack(1, 0, function() 
			self:update()
		end)
	end
end

function PartnerFollowAI:initParterPosition()
	if self.chara.master == nil then
		return
	end
	local masterPos = self.chara.master:getPosition()
	local pos = self.chara:getPosition()
	local direction = Vector3(masterPos.x - pos.x, 0, masterPos.z - pos.z)
    local toPos = masterPos - Vector3.Normalize(direction) * self.followRadius
	self.chara:setPosition(toPos)
	local rot = Quaternion.LookRotation(masterPos)
	if rot ~= nil then
		local euler = rot:ToEulerAngles()
    	self.chara:setRotation(Quaternion.Euler(0, euler.y, 0))
	end
end

function PartnerFollowAI:update()
	if self.followCD ~= nil and self.followCD > 0 then
		self.followCD = self.followCD - FightDefine.AI_RUN_INTERVAL
	else
		self:doFollowAI()
	end
	if self.currState == AIState.IDLE then
		if self.idleCD > 0 then
			self.idleCD = self.idleCD - FightDefine.AI_RUN_INTERVAL
		else
			self:doSomething()
		end
	end
end

function PartnerFollowAI:doFollowAI()
	if self.chara.master == nil then
		return
	end
	local pos = self.chara:getPosition()
	local masterPos = self.chara.master:getPosition()
	if CharacterUtil.sqrDistanceWithoutY(pos, masterPos) > self.sqrFollowRadius then
        local direction = Vector3(masterPos.x - pos.x, 0, masterPos.z - pos.z)
        local toPos = masterPos - Vector3.Normalize(direction) * self.followRadius
		if CharacterUtil.sqrDistanceWithoutY(pos, toPos) <= CharacterConstant.SQR_RUN_PRECISION then
			self.currState = AIState.IDLE
		else
			self.currState = AIState.FIND_PATH
			self.followCD = self.followInterval
			if self.isDrivingBySelf then
				self.chara:moveTo(toPos)
			else
				self.moveEvtTable    = self.moveEvtTable or {}
				self.moveEvtTable[1] = self.chara.uniqueID
				self.moveEvtTable[2] = math.round(toPos.x, 2)
				self.moveEvtTable[3] = math.round(toPos.y, 2)
				self.moveEvtTable[4] = math.round(toPos.z, 2)
				self.moveEvtTable[5] = "0"
				ModuleEvent.dispatch(ModuleConstant.AI_ACTION_MOVE, self.moveEvtTable)
			end
		end
	else
		if self.currState == AIState.FIND_PATH then
			self.idleCD = 0
		end
		self.currState = AIState.IDLE
    end
end

--伙伴跟随完后做的一些动作
function PartnerFollowAI:doSomething()
	self.idleCD = self.percentInterval
    local rand = math.random(1,100)
    if rand >= self.doActPercent then
        --播放动作和对白
        self:doAction()
    else
        self:doWalk()
    end
end

function PartnerFollowAI:doTalk()
    if self.chara.view then
        self.chara.view:doAction(self.chara)
    end
end

function PartnerFollowAI:doWalk()
    if self.chara.master and self.chara.master.position then
        local randX = math.random(-self.walkPosX,self.walkPosX)
        local randZ = math.random(-self.walkPosZ,self.walkPosZ) 
        local toPos = self.chara.master.position + Vector3(randX,0,randZ)
		if self.isDrivingBySelf then
			self.chara:moveTo(toPos)
		else
			self.moveEvtTable    = self.moveEvtTable or {}
			self.moveEvtTable[1] = self.chara.uniqueID
			self.moveEvtTable[2] = math.round(toPos.x, 2)
			self.moveEvtTable[3] = math.round(toPos.y, 2)
			self.moveEvtTable[4] = math.round(toPos.z, 2)
			self.moveEvtTable[5] = "0"
			ModuleEvent.dispatch(ModuleConstant.AI_ACTION_MOVE, self.moveEvtTable)
		end
    end
end

--播放动作和对白
function PartnerFollowAI:doAction()
    if self.chara.view then
        self.chara.view:doAction(self.chara)
    end
end

function PartnerFollowAI:dispose()
	if self.frameKey then
		FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_AI):removeLuaFunc(self.frameKey)
		self.frameKey = nil
	end
	self.chara = nil
end
