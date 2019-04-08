-- region
-- Date    : 2016-06-21
-- Author  : daiyaorong
-- Description :  碰撞检测
-- endregion

CollisionTool = {}

local ceil = math.ceil
local standardAngle = math.standardAngle
local mPos2DmPos = math.mPos2DmPos
local judgeCircleInterCircle = math.judgeCircleInterCircle
local judgeCircleIntersectRectangle = math.judgeCircleIntersectRectangle
local judgeCircleIntersectFan = math.judgeCircleIntersectFan
local sectorTemp = Vector2.zero
local skewingTemp = Vector3.zero
local _sinTable = sinTable
local _cosTable = cosTable
local _tempPos1 = Vector2.zero
local _tempPos2 = Vector2.zero

local function getLocalPosition( attacker, paramOffset )
	-- body
	if EnvironmentHandler.isInServer == false and attacker.view then --直接用C#接口换算
		local result = attacker.view:getSelfOffPosition(paramOffset)
		if result ~= nil then
			return result
		end
	end
	-- 用lua接口换算
	return Vector3.LocalOffToWorld( attacker:getPosition(), attacker:getRotation(), paramOffset )
end

local function getCollisionInfo( attacker, targetEntity, paramOffset )
	skewingTemp:Set(paramOffset[1], 0, paramOffset[2])
	local entityPos = getLocalPosition( attacker, skewingTemp )
	local targetPos = targetEntity:getPosition()
	-- 算法以x y为基础进行运算
	_tempPos1.x = entityPos.x
	_tempPos1.y = entityPos.z
	_tempPos2.x = targetPos.x
	_tempPos2.y = targetPos.z
	return _tempPos1, _tempPos2
end
--不同形状的碰撞检测函数
--圆形
local function circleCollision( position, rotation, targetEntity, param, attacker )
	local targetShapeCenterPos = targetEntity:getPosition()
	skewingTemp:Set(param.offset[1], 0, param.offset[2])
	local attackerPos = getLocalPosition( attacker, skewingTemp )
	local minX = targetShapeCenterPos.x - attackerPos.x
	local minY = targetShapeCenterPos.z - attackerPos.z
	local dis = targetEntity.hitsize + param.radius
	return (minX * minX + minY * minY) <= (dis * dis)
end

--矩形
local function rectCollision( position, rotation, targetEntity, param, attacker )
	local UIPos, targetShapeCenterPos = getCollisionInfo(attacker, targetEntity, param.offset)

	local eulerAngle = rotation.eulerAngles.y
	if eulerAngle > 180 then
		eulerAngle = eulerAngle - 360
	end
	local offAngle = eulerAngle + param.rotate --需要加上自身的朝向
	return judgeCircleIntersectRectangle( targetShapeCenterPos, targetEntity.hitsize, 
												UIPos, param.xLength, param.zLength, offAngle )
end

--扇形
local function sectorCollision( position, rotation, targetEntity, param, attacker )
	if param.angle <= 0 or param.angle >= 360 then
		return false
	end
	local UIPos, targetShapeCenterPos = getCollisionInfo(attacker, targetEntity, param.offset)
	param.rotate = param.rotate or 0

	local halfAngle = ceil(param.angle * 0.5)
	local offsetAngle = standardAngle(-param.rotate + halfAngle + 90) --+90以z轴为正向 rotate取负值顺时针旋转 与unity保持一致
	local R = param.radius
	sectorTemp:Set( R * _cosTable[offsetAngle], R * _sinTable[offsetAngle] )
	skewingTemp:Set(param.offset[1]+sectorTemp.x, 0, param.offset[2]+sectorTemp.y)
	local leftPos = getLocalPosition( attacker, skewingTemp )
	leftPos.y = leftPos.z

	offsetAngle = standardAngle( -param.rotate - halfAngle + 90 )
	sectorTemp:Set( R * _cosTable[offsetAngle], R * _sinTable[offsetAngle] )
	skewingTemp:Set(param.offset[1]+sectorTemp.x, 0, param.offset[2]+sectorTemp.y)
	local rightPos = getLocalPosition( attacker, skewingTemp )
	rightPos.y = rightPos.z

	return judgeCircleIntersectFan( targetShapeCenterPos, targetEntity.hitsize, 
											UIPos, param.radius, param.angle, leftPos, rightPos )
end

local collisionFuncList = {
	[FightDefine.BULLET_SHAPE_CIRCLE] 	= circleCollision,
	[FightDefine.BULLET_SHAPE_RECT] 	= rectCollision,
	[FightDefine.BULLET_SHAPE_SECTOR] 	= sectorCollision,
}

-- ignoreInvicible  是否忽略invincible判断
local function checkCondition( attacker, target, ignoreInvincible )
	-- body
	if target == nil or target.state == CharacterConstant.STATE_DEAD or target.hitsize == 0 or target.camp == CharacterConstant.CAMP_TYPE_NEUTRAL then
		return false
	end
	if (not ignoreInvincible) and target.invincible == 1 then
		return false
	end
	if attacker.camp == CharacterConstant.CAMP_TYPE_NEUTRAL then
		return false
	end
end

function CollisionTool.isCollide( attacker, targetID, param, ignoreInvincible )
	-- body
	local target = CharacterManager:getCharacByUId( targetID )
	if checkCondition( attacker, target, ignoreInvincible ) == false then
		return false
	end
	if collisionFuncList[param.shape] then
		return collisionFuncList[param.shape]( attacker:getPosition(), attacker:getRotation(), target, param, attacker )
	else
		return false
	end
end

function CollisionTool.isCollideByJoystick( attacker, joystickDirec, targetID, param )
	-- body
	local target = CharacterManager:getCharacByUId( targetID )
	if checkCondition( attacker, target ) == false then
		return false
	end
	if collisionFuncList[param.shape] then
		return collisionFuncList[param.shape]( attacker:getPosition(), joystickDirec, target, param, attacker )
	else
		return false
	end
end

function CollisionTool.isCollideByCircle( attacker, areaSize, target )
	if checkCondition(attacker, target) == false then
		return false
	end
	if target.hitsize == 0 or areaSize == 0 then
		return false
	end
	local attackerPos = attacker:getPosition()
	local targetPos = target:getPosition()
	local minX = targetPos.x - attackerPos.x
	local minY = targetPos.z - attackerPos.z
	local dis = target.hitsize + areaSize
	return (minX * minX + minY * minY) <= (dis * dis)
end