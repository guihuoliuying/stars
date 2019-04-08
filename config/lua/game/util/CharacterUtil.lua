CharacterUtil = {}

local sqrt = math.sqrt
local floor = math.floor
local ceil = math.ceil
local LogicRandom = math.LogicRandom
local isNumberEqual = math.isNumberEqual

CharacterUtil.distanceWithoutY = function ( posA, posB )
	-- body
	local x = posB.x - posA.x
	local z = posB.z - posA.z
	return sqrt(x * x + z * z)
end

-- 忽略Y轴的未开方距离
CharacterUtil.sqrDistanceWithoutY = function( posA, posB )
	local x = posB.x - posA.x
	local z = posB.z - posA.z
	return (x * x + z * z);
end

CharacterUtil.distance = function ( posA, posB )
	-- body
	local x = posB.x - posA.x
	local y = posB.y - posA.y
	local z = posB.z - posA.z
	return sqrt(x * x + y * y + z * z)
end

CharacterUtil.sqrDistance = function (posA, posB)
	local x = posB.x - posA.x
	local y = posB.y - posA.y
	local z = posB.z - posA.z
	return (x * x + y * y + z * z)
end

CharacterUtil.movementWithoutY = function ( startP, endP, speed )
	-- body
	local direction = endP - startP
	local x = endP.x - startP.x
	local z = endP.z - startP.z
	local num = sqrt(x * x + z * z)
	local vec3 = Vector3.New(0, 0, 0)
	if num > 1e-5 then
		vec3.x = x / num
		vec3.z = z / num				
    end		
	return vec3 * speed;
end

CharacterUtil.truelyAttackDistance = function ( position, characterB )
	-- body
	if( characterB.avatar == nil or characterB.data == nil or characterB:isLockable() == false )then
		return 0
	end
	return CharacterUtil.distanceWithoutY( position, characterB:getPosition()) - characterB.data.acceptAttackDistance
end

local tempDirection = Vector3.zero
CharacterUtil.faceToInTween = function ( character,position )
	-- body
	if( character == nil or position == nil)then
		return
	end
	local characPos = character:getPosition()
	tempDirection.x = position.x - characPos.x
	tempDirection.z = position.z - characPos.z
	if( tempDirection.x ~= 0 or tempDirection.z ~= 0 )then
		return Quaternion.LookRotation(tempDirection)
	end
end

-- 获取范围内最近的目标
function CharacterUtil.getNearestEnemy( character, areaParam )
	-- body
	if character == nil then return end
	local enemys = CharacterManager:getCharacByRelation( character.uniqueID, character.camp, CharacterConstant.RELATION_ENEMY )
	if enemys == nil then return end
	local minDistance = 0
	local nearestID = nil
	local sqrDis = 0
	areaParam = areaParam * areaParam
	local charaPos = character:getPosition()
	for k,v in pairs( enemys ) do
		if v.state ~= CharacterConstant.STATE_DEAD and (v.invincible == nil or v.invincible == 0) then
			sqrDis = CharacterUtil.sqrDistance( charaPos, v:getPosition() )
			if sqrDis <= areaParam then
				if nearestID == nil then
					nearestID = v.uniqueID
					minDistance = sqrDis
				else
					if sqrDis < minDistance then
						minDistance = sqrDis
						nearestID = v.uniqueID
					end
				end
			end
		end
	end
	return nearestID
end

-- 获取两点间特定距离的位置
function CharacterUtil.getDistancePoint1( characterA, characterB, configParam, distance )
	-- body
	local percent = 1 - (configParam / distance)
	return Vector3.Lerp( characterA:getPosition(), characterB:getPosition(), percent )
end

local PosB = Vector3.zero
local tempDirection2 = Vector3.zero
local tempDirection3 = Vector3.zero
local fixedQua = Quaternion.Euler( 0,60,0 )
-- 获取两点连线上特定距离的位置
function CharacterUtil.getDistancePoint2( characterA, characterB, configParam, distance )
	-- body
	local temp = characterB:getPosition()
	PosB:Set( temp.x, temp.y, temp.z )
	temp = characterA:getPosition()
	tempDirection2:Set( temp.x, temp.y, temp.z )
	tempDirection2:Sub( PosB )
	tempDirection2.y = 0
	tempDirection3:Set( tempDirection2.x, tempDirection2.y, tempDirection2.z )
	tempDirection3:SetNormalize()
	tempDirection3:Mul(configParam)
	local toPos = PosB:Add( tempDirection3 )
	local fixPos = PathFinder.samplePosition( toPos )
	local diff = CharacterUtil.distanceWithoutY( toPos, fixPos )
	if PathFinder.isBlock(toPos) == true or isNumberEqual( diff, 0 ) == false then
		-- 遇上阻挡或者与可走点差距过大时就重新找目标点
		for i = 1, 5 do
			tempDirection3 = fixedQua:MulVec3( tempDirection2 )
			tempDirection3:SetNormalize()
			tempDirection3:Mul(configParam)
			toPos = PosB:Add(tempDirection3)
			fixPos = PathFinder.samplePosition( toPos )
			diff = CharacterUtil.distanceWithoutY( toPos, fixPos )
			if PathFinder.isBlock(toPos) == false or isNumberEqual( diff, 0 ) == false then
				break
			else
				toPos = nil
			end
		end
	end
	return toPos
end

-- fixedDis:角色移动速度  确保目标位置必定在技能范围内
function CharacterUtil.getTargetPos( characterA, characterB, param, fixedDis )
	-- body
	local targetPos = nil
	local angle = nil
	local distance = CharacterUtil.distanceWithoutY( characterA:getPosition(), characterB:getPosition() )
	param.skilldistance = param.skilldistance or 0 --确保param.skilldistance不为nil
	local skilldistance = param.skilldistance 
	if skilldistance > fixedDis then
		skilldistance = skilldistance - fixedDis
	end
    if param.distanceType == FightDefine.DISTYPE_AREA then
    	characterA.framespeed = characterA.framespeed or 0 --确保characterA.framespeed不为nil
        if (distance-param.skilldistance) > characterA.framespeed then
            targetPos = ColliderCheck.checkStopPos( characterB,characterA , skilldistance, characterA.lastPathAngle, targetPos)
        end
    else
        if isNumberEqual( distance, param.skilldistance ) == false then
            targetPos = ColliderCheck.checkStopPos( characterB,characterA , skilldistance, characterA.lastPathAngle, targetPos)
        end
    end
    if targetPos then
    	-- 此副本因地形限制存在寻路点可能无法到达的情况，需要重新找路点
    	if FightModel:getFightStageType() == ConstantData.STAGE_TYPE_CAMP_TASK and PathFinder.isBlock(targetPos) then
            return CharacterUtil.getDistancePoint2(characterA, characterB, skilldistance)
        end
    end
    return targetPos
end

-- 获取范围内的随机目标
function CharacterUtil.getRandomEnemy( character, areaParam )
	-- body
	local enemys = CharacterManager:getCharacByRelation( character.uniqueID, character.camp, CharacterConstant.RELATION_ENEMY )
	if enemys == nil then return nil end
	local IDList = {}
	areaParam = areaParam * areaParam
	local charaPos = character:getPosition()
	for k,v in pairs( enemys ) do
		if v.state ~= CharacterConstant.STATE_DEAD and (v.invincible == nil or v.invincible == 0) then
			if CharacterUtil.sqrDistance( charaPos, v:getPosition() ) <= areaParam then
				table.insert( IDList, v.uniqueID )
			end
		end
	end
	if #IDList == 0 then
		return nil
	else
		enemys = ceil( LogicRandom( 0, #IDList ) )
		return IDList[enemys]
	end
end

-- 获取摇杆检测范围内最近的目标
function CharacterUtil.getCloseJoystickEnemy( character, joystickVec, areaData )
	-- body
	local allEnemy = CharacterManager:getCharacByRelation( character.uniqueID, character.camp, CharacterConstant.RELATION_ENEMY )
	if allEnemy == nil then return end

	local areaEnemy = {}
	local count = 0
	local direction = Quaternion.LookRotation( joystickVec )
	for k,v in pairs( allEnemy ) do
		if CollisionTool.isCollideByJoystick( character, direction, v.uniqueID, areaData ) == true then
			count = count + 1
			areaEnemy[count] = v
		end
	end

	if count == 0 then
		return nil
	else
		local recordUId = nil
		local recordDis = nil
		local tempDis = nil
		local pos = character:getPosition()
		for k,object in ipairs(areaEnemy) do
			tempDis = CharacterUtil.sqrDistanceWithoutY( pos, object:getPosition() )
			if recordDis == nil or tempDis < recordDis then
				recordDis = tempDis
				recordUId = object.uniqueID
			end
		end
		return recordUId
	end
end