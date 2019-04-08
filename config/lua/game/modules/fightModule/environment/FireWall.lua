-- region
-- Date    : 2017-01-04
-- Author  : daiyaorong
-- Description :  
-- endregion

FireWall = {}

-- 临时变量
local tempVector = Vector3.zero
local characterPos = Vector3.zero
local tempCharac = nil
local currentFrame = nil

local function JoystickMoveHandler( param )
	-- body
	tempCharac = CharacterManager:getCharacByUId( param[1] )
	if tempCharac == nil then
		return nil
	end
	--超过一定距离算瞬移
	tempVector.x = param[4]
	tempVector.y = param[5]
	tempVector.z = param[6]
	characterPos = tempCharac:getPosition()
	if CharacterUtil.distanceWithoutY( tempVector, characterPos ) > tempCharac.movespeed then
		-- EnvironmentHandler.sendLogToServer("超过范围啦~A")
		param[4] = characterPos.x
		param[5] = characterPos.y
		param[6] = characterPos.z
		FightServerControl.requestSyncInfo( param[1], 1 )  
	end

	return param
end

local function JoystickEndHandler( param )
	-- body
	tempCharac = CharacterManager:getCharacByUId( param[1] )
	if tempCharac == nil then
		return nil
	end
	tempVector.x = param[3]
	tempVector.y = param[4]
	tempVector.z = param[5]
	characterPos = tempCharac:getPosition()
	if CharacterUtil.distanceWithoutY( tempVector, characterPos ) > tempCharac.movespeed then
		-- EnvironmentHandler.sendLogToServer("超过范围啦~B")
		param[3] = characterPos.x
		param[4] = characterPos.y
		param[5] = characterPos.z
		FightServerControl.requestSyncInfo( param[1], 1 )  
	end

	return param
end

local function ClientSyncHandler( param )
	tempCharac = CharacterManager:getCharacByUId( param[1] )
	if tempCharac == nil then
		return nil
	end
	tempVector.x = param[2]
	tempVector.y = param[3]
	tempVector.z = param[4]
	characterPos = tempCharac:getPosition()
	if CharacterUtil.distanceWithoutY( tempVector, characterPos ) > tempCharac.movespeed then
		-- EnvironmentHandler.sendLogToServer("超过范围啦~C")
		param[2] = characterPos.x
		param[3] = characterPos.y
		param[4] = characterPos.z
		FightServerControl.requestSyncInfo( param[1], 1 )  
	end

	return param
end

local handlerList = {
	[ModuleConstant.JOYSTICK_MOVE] 	= JoystickMoveHandler,
	[ModuleConstant.JOYSTICK_END]	= JoystickEndHandler,
	[ModuleConstant.PLAYER_TIMING_SYNC] = ClientSyncHandler,
}

local function clearData(uniqueId)
	-- body
end

function FireWall.init()
	-- body
	ModuleEvent.addListener(ModuleConstant.CHARACTER_DEAD, clearData)
end

function FireWall.checkEvent( eventName, param )
	-- body
	if handlerList[eventName] == nil then
		return param
	end

	return handlerList[eventName]( param )
end