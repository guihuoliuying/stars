-- region TrackCalculator.lua
-- Date    : 2015-11-25
-- Author : daiyaorong
-- Description : 轨迹计算器
-- endregion

TrackCalculator = {}

local sqrt = math.sqrt
local abs = math.abs
local min = math.min
local Ceil = math.ceil
local Floor = math.floor
local G = 0
-- 常用变量
local oldPos = nil
local result = nil
local entity = nil
local resultList = { [1]=nil,[2]=nil,[3]=nil } --初始化
local wrapTable = {}
local tempX = 0
local tempZ = 0
local tempY = 0
local temp = nil
local FRAME_DELTA_TIME = ConstantData.FRAME_DELTA_TIME
local _Target = nil
local _Attacker = nil
local _diff = nil
local _targetPos = nil
local tempVector = Vector3.zero

local L_Quat90 = Quaternion.Euler(0,90,0)
local R_Quat90 = Quaternion.Euler(0,-90,0)

------------------------------------------------轨迹计算--------------------------------------------


--因为lua的math库运算以后的数字会产生精度问题,不能直接判断
--使用这个方法进行判断
--true表示这两个数可以认为是相等的
local function numberEquals(num1,num2)
	if not num1 or not num2 then return false end 

	return abs(num1-num2) <= FightDefine.PRECISION
end 

-- 因时间转换帧数时可能存在小数，统一向上取整
function TrackCalculator.timeAjust( time )
	-- body
	if time == 0 then
		return time
	end
	local totalFrame = time * ConstantData.FRAME_RATE
	if Floor( totalFrame )  == totalFrame then
		return time
	end
	totalFrame = Ceil( totalFrame )
	local fixedTime = totalFrame * FRAME_DELTA_TIME
	return fixedTime
end
local timeAjust = TrackCalculator.timeAjust

-- 打包
local defaultAcce = nil
local function dataWrap( time, speedVec, speedDirec, acceVec )
	-- body
	defaultAcce = defaultAcce or Vector3.zero
	wrapTable.totalFrame = time * ConstantData.FRAME_RATE
	wrapTable.speed = speedVec
	wrapTable.speedDirec = nil
	if speedDirec ~= nil then
		wrapTable.speedDirec = Quaternion.Euler(speedDirec.x, speedDirec.y, speedDirec.z)
	end
	wrapTable.acceleration = acceVec or defaultAcce
	-- print("计算："..tableplus.formatstring(wrapTable,true))
	return wrapTable
end

-- 定点定速直线型
local function FixSpeedStraigt( startPoint, endPoint, time, speedX, speedY )
	-- body
	local x = endPoint.x - startPoint.x
	local y = endPoint.y - startPoint.y
	local z = endPoint.z - startPoint.z
	local speedDirec = nil
	local speedVec = Vector3.New(0,0,0)
	if speedX ~= 0 then
		local distance = sqrt(x * x + y * y + z * z)
		time = distance / speedX
		time = timeAjust( time )
		speedX = distance / time
		tempVector:Set(x, y, z)
		tempVector:SetNormalize()
		speedVec.x = speedX * tempVector.x
		speedVec.y = speedX * tempVector.y
		speedVec.z = speedX * tempVector.z
	else
		tempVector:Set(x, y, z)
	end
	speedDirec = Quaternion.LookRotation( tempVector )
	if speedDirec ~= nil then
		speedDirec = speedDirec:ToEulerAngles()
	end
	return dataWrap( time, speedVec, speedDirec )
end

-- 定点定时直线型
local function FixTimeStraight( startPoint, endPoint, time, speedX, speedY )
	-- body
	tempVector.x = endPoint.x - startPoint.x
	tempVector.y = endPoint.y - startPoint.y
	tempVector.z = endPoint.z - startPoint.z
	local speedDirec = nil
	local speedVec = Vector3.New(0,0,0)
	if time ~= 0 and speedX == 0 then
		tempZ = tempVector.z
		if numberEquals( tempZ, 0 ) == true then
			tempZ = 0
		end
		speedVec.x = tempVector.x / time
		speedVec.y = tempVector.y / time
		speedVec.z = tempZ / time
	end

	speedDirec = Quaternion.LookRotation( tempVector )
	if speedDirec ~= nil then
		speedDirec = speedDirec:ToEulerAngles()
	end
	return dataWrap( time, speedVec, speedDirec )
end

-- 定点定速抛物线
local function FixSpeedParabola( startPoint, endPoint, time, speedX, speedY )
	-- body
	if speedY == 0 then
		--CmdLog("============运动计算:竖直速度为0=======")
		return
	end 
	local diffY = endPoint.y - startPoint.y
	local temp = speedY*speedY - 2 * G * diffY
	if temp < 0 then 
		local speedYMin = sqrt(2*G*diffY)
		--CmdLog("============定点定速抛物线初速度不足======="..",最低速度为speedY = "..speedYMin)
		return 
	end 
	local diffX = endPoint.x - startPoint.x
	local diffZ = endPoint.z - startPoint.z
	local speedDirec = Vector3.forward
	local speedVec = Vector3.New(0,0,0)

	local horDis = sqrt((diffX*diffX) + (diffZ*diffZ))
	if horDis == 0 then
		speedX = 0
	end
	local tempHor = horDis
	if horDis == 0 then
		tempHor = 1
		diffX = 1
	end

	local oldTime = (speedY + sqrt(temp)) / G
	time = timeAjust( oldTime )
	speedY = (diffY + 0.5*G*time*time) / time

	if speedY ~= 0 and speedX == 0 then
		speedX = horDis / time
		speedDirec.sinA = diffZ / tempHor
		speedDirec.cosA = diffX / tempHor
		temp = sqrt(speedY*speedY+speedX*speedX)
		speedDirec.sinB = speedY / temp
		speedDirec.cosB = speedX / temp
		speedVec.x = speedX * speedDirec.cosA
		speedVec.y = speedY
		speedVec.z = speedX * speedDirec.sinA
	elseif speedY ~= 0 and speedX ~= 0 then
		speedDirec.sinA = diffZ / tempHor
		speedDirec.cosA = diffX / tempHor
		temp = sqrt(speedY*speedY+speedX*speedX)
		speedDirec.sinB = speedY / temp
		speedDirec.cosB = speedX / temp

		speedVec.x = speedX * speedDirec.cosA
		speedVec.y = speedY
		speedVec.z = speedX * speedDirec.sinA
	end

	return dataWrap( time, speedVec, speedDirec )
end

-- 定点定时抛物线
local function FixTimeParabola( startPoint, endPoint, time, speedX, speedY )
	-- body
	if time == 0 then
		--CmdLog("============运动计算:时间配置错误=======")
		return
	end
	local speedDirec = Vector3.forward
	local speedVec = Vector3.New(0,0,0)
	local acceVec = Vector3.New(0,-G,0)

	local diffY = endPoint.y - startPoint.y
	local diffX = endPoint.x - startPoint.x
	local diffZ = endPoint.z - startPoint.z
	local horDis = sqrt((diffX*diffX) + (diffZ*diffZ))
	local tempHor = horDis
	if horDis == 0 then
		-- --CmdLog("============运动计算:抛物线水平距离为0=======")
		tempHor = 1
		diffX = 1
	end
	if speedX == 0 then
		speedY = diffY / time + G * time * 0.5
		speedX = horDis / time
		speedDirec.sinA = diffZ / tempHor
		speedDirec.cosA = diffX / tempHor
		local temp = sqrt(speedY*speedY+speedX*speedX)
		speedDirec.sinB = speedY / temp
		speedDirec.cosB = speedX / temp

		speedVec.x = speedX * speedDirec.cosA
		speedVec.y = speedY
		speedVec.z = speedX * speedDirec.sinA
	end

	return dataWrap( time, speedVec, speedDirec, acceVec )
end

-- 击飞
local flyAcceVec = nil
local function HitFly( startPoint, endPoint, time, speedX, speedY, config )
	-- body
	local speedDirec = nil
	local speedVec = Vector3.New(0,0,0)
	flyAcceVec = flyAcceVec or Vector3.New(0,-G,0)

	local entityID = config.endposition[1]
	local entityData = CharacterManager:getCharacByUId(entityID)
	local curPos = entityData.position
	local defaultPos = entityData.defaultPosition
	if defaultPos.y > curPos.y then
		defaultPos = curPos
	end

	speedDirec = config.endposition[2]
	local heightPlus = config.endposition[3] or 0 
	--击飞时需要加上初始的高度
	startPoint.y = startPoint.y + heightPlus
	local h = speedY * speedY / (2 * G) + curPos.y - defaultPos.y
	time = speedY / G + sqrt(2 * h / G)
	

	--击飞如果有初始高度,其实是有两段运动,两段时间
	local tempParam = speedY * speedY + 2 * G * heightPlus
	local timePlus =  (-speedY + sqrt(tempParam)) / (G)
	time = timeAjust( time + timePlus )
	speedVec.x = speedX * speedDirec.x
	speedVec.z = speedX * speedDirec.z
	speedVec.y = speedY
	return dataWrap( time, speedVec, speedDirec, flyAcceVec )
end

-- 击退
local function HitBack(startPoint, endPoint, time, speedX, speedY, config )
	-- body
	local speedDirec = nil
	local speedVec = Vector3.New(0,0,0)
	-- if time <= 0 then
	-- 	CmdLog("============运动计算:击退配置不运动=======")
	-- elseif time > 0 and (speedX <= 0) then
	-- 	CmdLog("============运动计算:击退配置无速度与加速度=======")
	-- end 
	speedDirec = config.endposition[2]
	if time > 0 and speedX > 0 then
		speedVec.x = speedX * speedDirec.x
		speedVec.z = speedX * speedDirec.z
		speedVec.y = speedY
	end
	return dataWrap( time, speedVec, speedDirec )
end

-- 定向定速直线型
local function DirecStraight( startPoint, endPoint, time, speedX, speedY )
	-- body
	local speedDirec = nil
	local speedVec = Vector3.New(0,0,0)
	local x = endPoint.x - startPoint.x
	local y = endPoint.y - startPoint.y
	local z = endPoint.z - startPoint.z

	speedDirec = Vector3.New(x, y, z)
	speedDirec:SetNormalize()

	-- if GameUtil.isMobilePlatform == false then
	-- 	if startPoint.x == endPoint.x and startPoint.z == endPoint.z then
	-- 		CmdLog("============运动计算:水平投影重合=======")
	-- 	end
	-- end

	speedVec.x = speedX * speedDirec.x
	speedVec.y = speedX * speedDirec.y
	speedVec.z = speedX * speedDirec.z

	tempVector:Set(x, y, z)
	speedDirec = Quaternion.LookRotation( tempVector )
	if speedDirec ~= nil then
		speedDirec = speedDirec:ToEulerAngles()
	end
	return dataWrap( time, speedVec, speedDirec )
end

-- 圆周运动
local function Circle( startPoint, endPoint, time, speedX, speedY )
	-- body
	local speedVec = Vector3.New(0,0,0)

	if speedX ~= 0 then
		local x = startPoint.x - endPoint.x
		local y = startPoint.y - endPoint.y
		local z = startPoint.z - endPoint.z
		tempVector:Set(x, y, z)
		local speedDirec = Quaternion.LookRotation(tempVector)
		-- speed.x保存角速度
		-- speed.y保存半径
		-- speed.z保存朝向
		speedVec.x = Quaternion.Euler(0, speedX * 10 * FRAME_DELTA_TIME, 0) --入口处统一做了*0.1的处理
		speedVec.y = sqrt(x * x + y * y + z * z)
		speedVec.z = speedDirec

		if speedX > 0 then
			speedDirec = speedDirec * L_Quat90
		else
			speedDirec = speedDirec * R_Quat90
		end
	end
	
	return dataWrap( time, speedVec, speedDirec )
end

-- 跟踪
local function Trace( startPoint, endPoint, time, speedX, speedY )
	-- body
	local speedVec = Vector3.zero
	local x = endPoint.x - startPoint.x
	local y = endPoint.y - startPoint.y
	local z = endPoint.z - startPoint.z
	local speedDirec = nil

	if speedX ~= 0 then
		tempVector:Set(x, y, z)
		speedDirec = tempVector:Normalize()
		speedVec.x = speedX * speedDirec.x
		speedVec.y = speedX * speedDirec.y
		speedVec.z = speedX * speedDirec.z
	else
		tempVector:Set(x, y, z)
	end
	speedDirec = Quaternion.LookRotation( tempVector )
	if speedDirec ~= nil then
		speedDirec = speedDirec:ToEulerAngles()
	end
	return dataWrap( time, speedVec, speedDirec )
end

-- 自动或摇杆跟随
local function AutoFollow( startPoint, endPoint, time, speedX, speedY )
    local speedVec = Vector3.zero
    tempVector.x = endPoint.x - startPoint.x
	tempVector.y = endPoint.y - startPoint.y
	tempVector.z = endPoint.z - startPoint.z
    if speedX ~= 0 then
		speedVec.x = speedX
		speedVec.y = speedX
		speedVec.z = speedX
	end
	local speedDirec = Quaternion.LookRotation( tempVector )
	if speedDirec ~= nil then
		speedDirec = speedDirec:ToEulerAngles()
	end
	return dataWrap( time, speedVec, speedDirec )
end

-- 自动或摇杆朝向
local function AutoFace( startPoint, endPoint, time, speedX, speedY )
    local speedVec = Vector3.zero
	tempVector.x = endPoint.x - startPoint.x
	tempVector.y = endPoint.y - startPoint.y
	tempVector.z = endPoint.z - startPoint.z

    speedVec.y = speedX --此处保存转向Y轴角速度
	local speedDirec = Quaternion.LookRotation( tempVector )
	if speedDirec ~= nil then
		speedDirec = speedDirec:ToEulerAngles()
	end
	return dataWrap( time, speedVec, speedDirec )
end

--跟踪圆周
local function TraceCircle( startPoint, endPoint, time, speedX, speedY )
	local speedVec = Vector3.New(0,0,0)
	if speedX ~= 0 then
		local x = startPoint.x - endPoint.x
		local y = startPoint.y - endPoint.y
		local z = startPoint.z - endPoint.z
		tempVector:Set(x, y, z)
		local speedDirec = Quaternion.LookRotation(tempVector)
		-- speed.x保存角速度
		-- speed.y保存半径
		-- speed.z保存朝向
		speedVec.x = Quaternion.Euler(0, speedX * 10 * FRAME_DELTA_TIME, 0) --入口处统一做了*0.1的处理
		speedVec.y = sqrt(x * x + y * y + z * z)
		speedVec.z = speedDirec

		if speedX > 0 then
			speedDirec = speedDirec * L_Quat90
		else
			speedDirec = speedDirec * R_Quat90
		end
	end	
	return dataWrap( time, speedVec, speedDirec )	
end 

local initFunc = {
	[FightDefine.TRACK_SPEEDSTRAIGHT] 		= FixSpeedStraigt,
	[FightDefine.TRACK_TIMESTRAIGHT] 		= FixTimeStraight,
	[FightDefine.TRACK_SPEEDPARABOLA] 		= FixSpeedParabola,
	[FightDefine.TRACK_TIMEPARABOLA] 		= FixTimeParabola,
	[FightDefine.TRACK_HITFLY] 				= HitFly,
	[FightDefine.TRACK_HITBACK] 			= HitBack,
	[FightDefine.TRACK_TRACE]				= Trace,
	[FightDefine.TRACK_DIRECSTRAIGHT] 		= DirecStraight,
	[FightDefine.TRACK_CIRCLE]				= Circle,
    [FightDefine.TRACK_AUTOFOLLOW]          = AutoFollow,
    [FightDefine.TRACK_AUTOFACE]            = AutoFace,
    [FightDefine.TRACK_TRACE_CIRCLE]        = TraceCircle,
    [FightDefine.TRACK_AUTOFOLLOWTURN]          = AutoFollow,
}

------------------------------------------------位置计算--------------------------------------------
-- 直线运动
local function StraightUpdate( motionInfo, pos )
	-- body
	local speed = motionInfo.speed;
	if pos.x and pos.z and speed.x and speed.z and (speed.x ~= 0 or speed.z ~= 0) then
		pos.x = pos.x + speed.x * FRAME_DELTA_TIME;
		pos.z = pos.z + speed.z * FRAME_DELTA_TIME;
		
		if motionInfo.curFrame >= motionInfo.totalFrame and motionInfo.collideState == 0 then
			pos = motionInfo.endWorldPoint
		end
	end
	resultList[1] = pos
	-- resultList[2] = motionInfo
	return resultList
end

-- 抛物线运动
local function ParabolaUpdate( motionInfo, pos )
	-- body
	local speed = motionInfo.speed
	local acceleration = motionInfo.acceleration
	if speed ~= Vector3.zero or acceleration ~= Vector3.zero then
		speed.x = speed.x + acceleration.x * FRAME_DELTA_TIME
		speed.y = speed.y + acceleration.y * FRAME_DELTA_TIME
		speed.z = speed.z + acceleration.z * FRAME_DELTA_TIME

		pos.x = pos.x + speed.x * FRAME_DELTA_TIME
		pos.y = pos.y + speed.y * FRAME_DELTA_TIME
		pos.z = pos.z + speed.z * FRAME_DELTA_TIME

		if motionInfo.curFrame >= motionInfo.totalFrame and motionInfo.collideState == 0 then
			pos = motionInfo.endWorldPoint
		end
	end
	resultList[1] = pos
	-- resultList[2] = motionInfo
	return resultList
end

-- 击飞
local function HitFlyUpdate( motionInfo, pos )
	-- body
	local speed = motionInfo.speed
	local acceleration = motionInfo.acceleration
	if speed and (speed.x ~= 0 or speed.z ~= 0 or acceleration.y ~= 0) then
		if speed.y == nil then
			speed.y = 0
		end
		speed.y = speed.y + acceleration.y * FRAME_DELTA_TIME
		pos.x = pos.x + speed.x * FRAME_DELTA_TIME
		pos.y = pos.y + speed.y * FRAME_DELTA_TIME
		pos.z = pos.z + speed.z * FRAME_DELTA_TIME
	end
	if motionInfo.totalFrame - motionInfo.curFrame <= 1 and PathFinder.isBlock(pos) then
		pos = PathFinder.samplePosition(pos)
	end
	resultList[1] = pos
	-- resultList[2] = motionInfo
	return resultList
end

-- 击退
local function HitBackUpdate( motionInfo, pos, config, targetID, attackerID )
	-- body
	local speed = motionInfo.speed
	if speed and (speed.x ~= 0 or speed.z ~= 0) then
		if motionInfo.curFrame <= motionInfo.totalFrame then
			speed.y = 0
			pos.x = pos.x + speed.x * FRAME_DELTA_TIME
			pos.z = pos.z + speed.z * FRAME_DELTA_TIME
		end
	end
	resultList[1] = pos
	-- resultList[2] = motionInfo
	return resultList
end

-- 定向定速直线型
local function DirecStraightUpdate( motionInfo, pos, config, targetID, attackerID )
	-- body
    if motionInfo.curFrame <= config.moveframe then
		local speed = motionInfo.speed
	    pos.x = pos.x + speed.x * FRAME_DELTA_TIME
	    pos.z = pos.z + speed.z * FRAME_DELTA_TIME
    end

	resultList[1] = pos
	-- resultList[2] = motionInfo
	return resultList
end

-- 圆周运动
local function CircleUpdate( motionInfo, pos, config, targetID, attackerID )
	-- body
	-- speed.x保存角速度
	-- speed.y保存半径
	-- speed.z保存朝向
    if motionInfo.curFrame <= config.moveframe then
		local speed = motionInfo.speed
	    if speed.x ~= 0 then
		    speed.z = speed.z * speed.x
		  	tempVector:Set(0,0,speed.y)
		    pos = motionInfo.endWorldPoint + speed.z * tempVector
		    if speed.x.y > 0 then
			    resultList[3] = speed.z * L_Quat90
		    else
			    resultList[3] = speed.z * R_Quat90
		    end
	    end
    end

	resultList[1] = pos
	-- resultList[2] = motionInfo
	return resultList
end

-- 跟踪
local function TraceUpdate( motionInfo, pos, config, targetID, attackerID, runner )
	-- body
	resultList[3] = nil
    if motionInfo.curFrame <= config.moveframe then
	    if config.speedx ~= 0 then
		    _targetPos = PointCalculator.getInitPoint( attackerID, targetID, config.endpositiontype, config.endposition, motionInfo, _targetPos )
		    if CharacterUtil.sqrDistance(pos, _targetPos) > CharacterConstant.RUN_RRECISIONSQUARE then
				local sqrSpeedx = (config.speedx * FRAME_DELTA_TIME)
				sqrSpeedx = sqrSpeedx * sqrSpeedx
				local runnerPos = runner:getPosition()
				local x = _targetPos.x - runnerPos.x
				local y = _targetPos.y - runnerPos.y
				local z = _targetPos.z - runnerPos.z
				local speedDirec = Vector3.New(x, y, z)
		    	if (x * x + y * y + z * z) > sqrSpeedx then
				    local nSpeedDirec = speedDirec:Normalize()
					local speed = motionInfo.speed
				    speed.x = config.speedx * nSpeedDirec.x
				    speed.z = config.speedx * nSpeedDirec.z

				    pos.x = pos.x + speed.x * FRAME_DELTA_TIME
				    pos.z = pos.z + speed.z * FRAME_DELTA_TIME
			    else
			    	pos.x = _targetPos.x
			    	pos.y = _targetPos.y
			    	pos.z = _targetPos.z
		    	end

		    	if config.faceDirec and config.faceDirec == FightDefine.DIRECTION_SPEED then
				    speedDirec = Quaternion.LookRotation( speedDirec )
				    if speedDirec ~= nil then
					    speedDirec = speedDirec:ToEulerAngles()
					    resultList[3] = Quaternion.Euler( 0,speedDirec.y,0 )
				    end
			    end
		    -- else
			   --  motionInfo.speedDirec = runner:getRotation()
		    end
	    end
    end

	resultList[1] = pos
	-- resultList[2] = motionInfo
	return resultList
end

local tempPos = nil
local tempDirec = nil
local function AutoFollowUpdate( motionInfo, pos, config, targetID, attackerID )
    if motionInfo.curFrame <= config.moveframe then
        local attacker = CharacterManager:getCharacByUId( attackerID )
        if attacker ~= nil and AIManager.hasAIRun(attackerID) and attacker.movingKey == nil then --处于AI运行中并且没有摇杆操作
            local targetPos = PointCalculator.getInitPoint( attackerID, targetID, config.endpositiontype, config.endposition, motionInfo )
            if CharacterUtil.sqrDistance(pos, targetPos) > CharacterConstant.RUN_RRECISIONSQUARE then
				local sqrSpeedx = (config.speedx * FRAME_DELTA_TIME)
				sqrSpeedx = sqrSpeedx * sqrSpeedx
				temp = attacker:getPosition() 
				tempPos = tempPos or Vector3.zero
				tempPos.x = targetPos.x - temp.x
				tempPos.y = 0
				tempPos.z = targetPos.z - temp.z
            	if tempPos:SqrMagnitude() > sqrSpeedx then
	                tempPos:SetNormalize()
				    pos.x = pos.x + motionInfo.speed.x * tempPos.x * FRAME_DELTA_TIME
				    pos.z = pos.z + motionInfo.speed.z * tempPos.z * FRAME_DELTA_TIME
				else
			    	pos.x = targetPos.x
			    	pos.y = targetPos.y
			    	pos.z = targetPos.z
		    	end

			    -- tempDirec = Quaternion.LookRotation( tempPos )
			    -- if tempDirec ~= nil then
				   --  motionInfo.speedDirec = tempDirec:ToEulerAngles()
			    -- end
            -- else
            --     motionInfo.speedDirec = attacker:getRotation()
            end
        end
    end
    resultList[1] = pos
	-- resultList[2] = motionInfo
	return resultList
end

local function AutoFollowTurnUpdate( motionInfo, pos, config, targetID, attackerID )
    if motionInfo.curFrame <= config.moveframe then
        local attacker = CharacterManager:getCharacByUId( attackerID )
        if attacker ~= nil and AIManager.hasAIRun(attackerID) and attacker.movingKey == nil then --处于AI运行中并且没有摇杆操作
            local targetPos = PointCalculator.getInitPoint( attackerID, targetID, config.endpositiontype, config.endposition, motionInfo )
            if CharacterUtil.sqrDistance(pos, targetPos) > CharacterConstant.RUN_RRECISIONSQUARE then
				local sqrSpeedx = (config.speedx * FRAME_DELTA_TIME)
				sqrSpeedx = sqrSpeedx * sqrSpeedx
				temp = attacker:getPosition() 
				tempPos = tempPos or Vector3.zero
				tempPos.x = targetPos.x - temp.x
				tempPos.y = 0
				tempPos.z = targetPos.z - temp.z
				tempDirec = Quaternion.LookRotation( tempPos )
            	if tempPos:SqrMagnitude() > sqrSpeedx then
	                tempPos:SetNormalize()
				    pos.x = pos.x + motionInfo.speed.x * tempPos.x * FRAME_DELTA_TIME
				    pos.z = pos.z + motionInfo.speed.z * tempPos.z * FRAME_DELTA_TIME
				else
			    	pos.x = targetPos.x
			    	pos.y = targetPos.y
			    	pos.z = targetPos.z
		    	end
		    	resultList[3] = tempDirec
            end
        end
    end
    resultList[1] = pos
	-- resultList[2] = motionInfo
	return resultList
end

local tempEuler = nil
local function AutoFaceUpdate( motionInfo, pos, config, targetID, attackerID )
    resultList[3] = nil --默认置空
    if motionInfo.curFrame <= config.moveframe then
        local attacker = CharacterManager:getCharacByUId( attackerID )
        if attacker then
        	tempDirec = nil
        	if attacker.targetRot then
        		tempDirec = attacker.targetRot
    		elseif AIManager.hasAIRun(attackerID) then
    			local targetPos = PointCalculator.getInitPoint( attackerID, targetID, config.endpositiontype, config.endposition, motionInfo )
	            temp = attacker:getPosition() 
	            tempPos = tempPos or Vector3.zero
				tempPos.x = targetPos.x - temp.x
				tempPos.y = 0
				tempPos.z = targetPos.z - temp.z
	            tempDirec = Quaternion.LookRotation( tempPos )
    		end
    		if tempDirec ~= nil then
                tempEuler = tempDirec:ToEulerAngles()
                local curEuler = attacker:getRotation():ToEulerAngles()
                local diff = abs(curEuler.y - tempEuler.y)
                if diff > (config.speedx * FRAME_DELTA_TIME) then
                    if (diff<180 and curEuler.y>tempEuler.y) or (diff>180 and curEuler.y<tempEuler.y) then
                        curEuler.y = curEuler.y - config.speedx * FRAME_DELTA_TIME
                    else
                        curEuler.y = curEuler.y + config.speedx * FRAME_DELTA_TIME
                    end
                else
                    curEuler.y = tempEuler.y --角度差距小于一帧转向幅度时直接设置
                end
                resultList[3] = Quaternion.Euler( 0,curEuler.y,0 )
		    end
    	end
    end
    resultList[1] = pos
	-- resultList[2] = motionInfo
	return resultList
end

-- 跟踪圆周运动
local function TraceCircleUpdate( motionInfo, pos, config, targetID, attackerID )
	-- speed.x保存角速度
	-- speed.y保存半径
	-- speed.z保存朝向
    if motionInfo.curFrame <= config.moveframe then
		local speed = motionInfo.speed
	    if speed.x ~= 0 then
	    	motionInfo.endWorldPoint = PointCalculator.getInitPoint( attackerID, targetID, config.endpositiontype, 
	    		config.endposition, motionInfo, motionInfo.endWorldPoint )
		 
		    speed.z = speed.z * speed.x
		  	tempVector:Set(0,0,speed.y)
		    pos = motionInfo.endWorldPoint + speed.z * tempVector
		    if speed.x.y > 0 then
			    resultList[3] = speed.z * L_Quat90
		    else
			    resultList[3] = speed.z * R_Quat90
		    end
	    end
    end

	resultList[1] = pos
	return resultList
end


local updateFunc = {
	[FightDefine.TRACK_SPEEDSTRAIGHT] 		= StraightUpdate,
	[FightDefine.TRACK_TIMESTRAIGHT] 		= StraightUpdate,
	[FightDefine.TRACK_SPEEDPARABOLA] 		= ParabolaUpdate,
	[FightDefine.TRACK_TIMEPARABOLA] 		= ParabolaUpdate,
	[FightDefine.TRACK_HITFLY] 				= HitFlyUpdate,
	[FightDefine.TRACK_HITBACK] 			= HitBackUpdate,
	[FightDefine.TRACK_TRACE]				= TraceUpdate,
	[FightDefine.TRACK_DIRECSTRAIGHT] 		= DirecStraightUpdate,
	[FightDefine.TRACK_CIRCLE]				= CircleUpdate,
    [FightDefine.TRACK_AUTOFOLLOW]          = AutoFollowUpdate,
    [FightDefine.TRACK_AUTOFACE]            = AutoFaceUpdate,
    [FightDefine.TRACK_TRACE_CIRCLE]        = TraceCircleUpdate,
    [FightDefine.TRACK_AUTOFOLLOWTURN]          = AutoFollowTurnUpdate,
}

------------------------------------------------全局--------------------------------------------

local tempTime = nil
local tempSpeedX = nil
local tempSpeedY = nil
function TrackCalculator.getTrackData( startPoint, endPoint, config, entityID )
	-- body
	if initFunc[config.tracktype] == nil then
		-- CmdLog("============运动计算:轨迹类型参数错误======="..config.tracktype)
		return
	else
		if G == 0 or G == nil then
			G = FightModel:getFightCommon(FightDefine.FIGHT_COMMON_G) * 0.1
		end
		return initFunc[config.tracktype]( startPoint, endPoint, config.time, config.speedx, config.speedy, config )
	end
end

function TrackCalculator.getUpdateData( config, motionInfo, curPos, targetID, attackerID, runner )
	-- body
	if config == nil then 
		return 
	end 
	if updateFunc[config.tracktype] == nil then
		-- CmdLog("============运动计算:轨迹类型参数错误======="..config.tracktype)
		return
	else
		if G == 0 or G == nil then
			G = FightModel:getFightCommon(FightDefine.FIGHT_COMMON_G) * 0.1
		end
		oldPos = curPos:Clone()
		oldPos = oldPos or Vector3.zero
		resultList[3] = nil
		result = updateFunc[config.tracktype]( motionInfo, oldPos, config, targetID, attackerID, runner )
		if motionInfo.needCollide == true then
			if TrackCalculator.checkCollideMode( targetID, attackerID, result[1] ) == true then
				result[1].x = curPos.x
				result[1].z = curPos.z
				motionInfo.collideState = 1
			else
				motionInfo.collideState = 0
			end

			return result
		else
			return result
		end
	end
end

-- 检测是否碰撞
local _sqrHitSize = nil
function TrackCalculator.checkCollideMode( targetID, attackerID, newPos )
	-- body
	_Target = CharacterManager:getCharacByUId( targetID )
	if _Target == nil then
		return false
	end
	_Attacker = CharacterManager:getCharacByUId( attackerID )
	_diff = CharacterUtil.sqrDistanceWithoutY( newPos, _Target:getPosition() )
	_sqrHitSize = _Attacker.hitsize + _Target.hitsize
	_sqrHitSize = _sqrHitSize * _sqrHitSize
	if _diff <= _sqrHitSize then
		return true
	end
	return false
end