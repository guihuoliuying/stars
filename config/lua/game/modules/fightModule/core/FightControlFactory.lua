-- region
-- Date    : 2016-08-19
-- Author  : daiyaorong
-- Description :  战斗控制器工厂
-- endregion

FightControlFactory = {}

local envType = nil
local CONTROL_MAP = {
	[EnvironmentDef.DEFAULT] = FightControl,
	[EnvironmentDef.PVE_CLIENT] = FightControl,
	[EnvironmentDef.PVE_SERVER] = FightServerControl,
	[EnvironmentDef.PVP_CLIENT] = FightControl,
	[EnvironmentDef.PVP_SERVER] = FightServerControl,
	[EnvironmentDef.PVE_WEAKSYNC] = FightControl,
}

function FightControlFactory.getControl()
	-- body
	envType = EnvironmentHandler.environmentType
	return CONTROL_MAP[envType]
end