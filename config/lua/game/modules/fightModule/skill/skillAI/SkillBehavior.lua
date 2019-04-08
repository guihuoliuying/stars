-- region
-- Date    : 2016-07-02
-- Author  : daiyaorong
-- Description :  技能行为
-- endregion

SkillBehavior = {
	skillIndex = nil,
	conditionNodes = nil,	--条件列表
	logicNodes = nil,		--逻辑符列表
	CD = nil,
	timeRecord = nil,	--下次生效时间点
	logicIndex = nil,
	behaviorResult = nil,
}
SkillBehavior = createClass(SkillBehavior)

local behaviorResult = nil
local logicIndex = nil

function SkillBehavior:init( uid, data )
	-- body
	local temp = data
	self.skillIndex = tonumber(temp[1])
	self.CD = tonumber(temp[3]) * 0.001

	self.getFightTimeFunc = FightControlFactory.getControl().getFightTime

	local tempList = StringUtils.split( temp[2], FightDefine.AI_MATCH.logic )
	self.conditionNodes = {}
	local condition = nil
	for key,value in ipairs( tempList ) do
		condition = SkillCondition:create()
		condition:init( uid, value )
		self.conditionNodes[key] = condition
	end

	self.logicNodes = StringUtils.getPattern( temp[2], FightDefine.AI_MATCH.logic )
end

function SkillBehavior:execute( targetID )
	-- body
	if self:checkInCD() == true then
		return
	end

	self.behaviorResult = nil
	self.logicIndex = 1
	for k,node in ipairs( self.conditionNodes ) do
		if self.behaviorResult == nil then
			self.behaviorResult = node:execute( targetID )
		else
			self.behaviorResult = JudgeLogic[self.logicNodes[self.logicIndex]]( self.behaviorResult, node:execute( targetID ) )
			self.logicIndex = self.logicIndex + 1
		end
	end

	if self.behaviorResult == false then
		return
	end
	-- self.timeRecord = self.getFightTimeFunc() + self.CD
	return self.skillIndex
end

function SkillBehavior:beginCD()
	-- body
	self.timeRecord = self.getFightTimeFunc() + self.CD
end

function SkillBehavior:checkInCD()
	-- body
	if self.timeRecord == nil then
		return false
	end
	if self.timeRecord <= self.getFightTimeFunc() then
		return false
	end
	return true
end

function SkillBehavior:dispose()
	-- body
	for k,node in ipairs( self.conditionNodes ) do
		node:dispose()
	end
	self.conditionNodes = nil
	self.logicNodes = nil
	self.getFightTimeFunc = nil
end