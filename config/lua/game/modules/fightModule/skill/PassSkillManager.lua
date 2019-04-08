-- region
-- Date    : 2016-08-02
-- Author  : daiyaorong
-- Description :  被动技能管理
-- endregion

PassSkillManager = {}

function PassSkillManager.addHandler( character ,lock)
	-- body
	if EnvironmentHandler.isPvpClient == false then
		character.pSkillHandler = PassSkillHandler:new()
		character.pSkillHandler:init( character ,lock)
	end
end

function PassSkillManager.removeHandler( character )
	-- body
	character.pSkillHandler:dispose()
	character.pSkillHandler = nil
end