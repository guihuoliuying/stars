-- region PointCalculator.lua
-- Date    : 2015-11-25
-- Author : daiyaorong
-- Description : 起点终点计算器
-- endregion

PointCalculator = {}
local entity = nil
local target = nil

------------------------------------------------计算起点与终点--------------------------------------------

local function getLocalPosition( entity, paramOffset )
	-- body
	if EnvironmentHandler.isInServer == false and entity.view then --直接用C#接口换算
		local result = entity.view:getSelfOffPosition(paramOffset)
		if result ~= nil then
			return result
		end
	end
	-- 用lua接口换算
	return Vector3.LocalOffToWorld( entity:getPosition(), entity:getRotation(), paramOffset )
end

-- 技能释放者坐标系偏移
local function AttackerSkewing( entityID, targetID, value, motionInfo, resultInstance )
	-- body
	entity = CharacterManager:getCharacByUId( entityID )
	if entity ~= nil then
		resultInstance = getLocalPosition(entity, value)
	end
	return resultInstance
end

-- 技能释放者的位置偏移
local function AttackerOffPoint( entityID, targetID, value, motionInfo, resultInstance )
	-- body
	entity = CharacterManager:getCharacByUId( entityID )
	if entity ~= nil then
		local ePos = entity:getPosition()
		resultInstance.x = ePos.x + value.x
		resultInstance.y = ePos.y + value.y
		resultInstance.z = ePos.z + value.z
	end
	return resultInstance
end

-- 技能目标受到射击的位置偏移
local function TargetShootPoint( entityID, targetID, value, motionInfo, resultInstance )
	-- body
	entity = CharacterManager:getCharacByUId( targetID )
	if entity == nil then
		entity = CharacterManager:getCharacByUId( entityID )
	end
	if entity ~= nil then
		local ePos = entity:getPosition()
		resultInstance.x = ePos.x + value.x
		resultInstance.y = entity.defaultPosition.y
		resultInstance.z = ePos.z + value.z
	end
	return resultInstance
end

-- 技能释放者出生位置的偏移
local function AttackerBornOffPoint( entityID, targetID, value, motionInfo, resultInstance )
	-- body
	entity = CharacterManager:getCharacByUId( entityID )
	if entity ~= nil then
		local ePos = entity.bornPosition
		resultInstance.x = ePos.x + value.x
		resultInstance.y = ePos.y + value.y
		resultInstance.z = ePos.z + value.z
	end
	return resultInstance
end

-- 技能释放者的初始化位置
local function AttackerInitPos( entityID, targetID, value, motionInfo, resultInstance )
	-- body
	entity = CharacterManager:getCharacByUId( entityID )
	if entity ~= nil and entity.defaultPosition  then
		local ePos = entity.defaultPosition
		resultInstance.x = ePos.x + value.x
		resultInstance.y = ePos.y + value.y
		resultInstance.z = ePos.z + value.z
	end
	return resultInstance
end

-- 技能释放初始时目标所在位置
local function TargetInitPos( entityID, targetID, value, motionInfo )
	return Vector3.LocalOffToWorld( motionInfo.targetInitPos, motionInfo.targetInitRot, value )
end

-- 目标坐标系偏移
local function TargetSkewing( entityID, targetID, value, motionInfo, resultInstance )
	-- body
	entity = CharacterManager:getCharacByUId( targetID )
	if entity == nil then
		entity = CharacterManager:getCharacByUId( entityID )
	end
	if entity ~= nil then
		resultInstance = getLocalPosition( entity, value )
	else
		resultInstance:Set(0,0,0)
	end
	return resultInstance
end

-- 攻击者向目标朝向的偏移(unity)
local function AttackerToTargetOff( entityID, targetID, value, motionInfo, resultInstance )
	-- body
	entity = CharacterManager:getCharacByUId( entityID )
	target = CharacterManager:getCharacByUId( targetID )
	if target == nil then
		target = CharacterManager:getCharacByUId( entityID )
	end
	if entity ~= nil and target ~= nil then
        local direction = entity:getRotation():Clone()
        if entity.position:Equals(target.position) == false then
            direction = Quaternion.LookRotation( target:getPosition() - entity:getPosition() )
            direction = direction or entity:getRotation():Clone()
		end
        resultInstance = Vector3.LocalOffToWorld( entity.position, direction, value )
	end
	return resultInstance
end

-- 目标向攻击者朝向的偏移(unity)
local function TargetToAttackerOff( entityID, targetID, value, motionInfo, resultInstance )
	-- body
	entity = CharacterManager:getCharacByUId( entityID )
	target = CharacterManager:getCharacByUId( targetID )
	if target == nil then
		target = CharacterManager:getCharacByUId( entityID )
	end
	if entity ~= nil and target ~= nil then
        local direction = target:getRotation():Clone()
        if entity.position:Equals(target.position) == false then
            direction = Quaternion.LookRotation( entity:getPosition() - target:getPosition() )
            direction = direction or target:getRotation():Clone()
		end
        resultInstance = Vector3.LocalOffToWorld( target.position, direction, value )
	end
	return resultInstance
end

local InitPointMap = {
	[FightDefine.ACTION_ATTACKERSKEWING] 		= AttackerSkewing,
	[FightDefine.ACTION_ATTACKPOINT] 			= AttackerOffPoint,
	[FightDefine.ACTION_TARGETHIT] 				= TargetShootPoint,
	[FightDefine.ACTION_BORNPOS]				= AttackerBornOffPoint,
	[FightDefine.ACTION_ATTACKERINIT] 			= AttackerInitPos,
	[FightDefine.ACTION_ATTACKERDIRECOFF]		= AttackerToTargetOff,
	[FightDefine.ACTION_TARGETDIRECOFF]			= TargetToAttackerOff,
	[FightDefine.ACTION_TARGETINIT]				= TargetInitPos,
	[FightDefine.ACTION_TARGETSKEWING]			= TargetSkewing,
}

function PointCalculator.getInitPoint( attackerID, targetID, pointType, pointValue, motionInfo, resultInstance )
	-- body
	resultInstance = resultInstance or Vector3.zero
	if pointType == FightDefine.ACTION_UNKNOWPOINT then
		-- 未知型 直接返回空
		return nil
	elseif pointType == FightDefine.ACTION_WORLDPOINT then
		-- 世界
		entity = CharacterManager:getCharacByUId( attackerID )
		if entity and entity.defaultPosition then
			resultInstance:Set( pointValue.x, entity.defaultPosition.y, pointValue.z )
		end
		return resultInstance
	else
		return InitPointMap[pointType]( attackerID, targetID, pointValue, motionInfo, resultInstance )
	end
end