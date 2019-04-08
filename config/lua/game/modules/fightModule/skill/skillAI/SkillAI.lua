-- region
-- Date    : 2016-07-02
-- Author  : daiyaorong
-- Description :  技能AI
-- endregion

SkillAI = {
	behaviorList = nil,
}
SkillAI = createClass(SkillAI)

local aiResult = nil

function SkillAI:init( uid, aiData )
	-- body
	self.behaviorList = {}
	local behaviorNode = nil
	for key, value in ipairs( aiData ) do
		if key >= 2 then --1号位是类型
			behaviorNode = SkillBehavior:create()
			behaviorNode:init( uid, value )
			self.behaviorList[key-1] = behaviorNode
		end
	end
end

function SkillAI:execute( targetID )
	-- body
	aiResult = nil
	for k, node in ipairs( self.behaviorList ) do
		aiResult = node:execute( targetID )
		if aiResult ~= nil then
			break
		end
	end
	return aiResult
end

-- 释放成功后才开始算CD
function SkillAI:beginCDByIndex( skillIndex )
	-- body
	if self.behaviorList then
		for i = 1, #self.behaviorList do
			if self.behaviorList[i].skillIndex == skillIndex then
				self.behaviorList[i]:beginCD()
				break
			end
		end
	end
end

function SkillAI:dispose()
	-- body
	for k,v in ipairs( self.behaviorList ) do
		v:dispose()
	end
	self.behaviorList = nil
end