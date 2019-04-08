--------------------------------------------------------------
-- region ColliderCheck.lua
-- Date : 2016-7-18
-- Author : jjm
-- Description : 碰撞检测类
-- endregion
---------------------------------------------------------------

ColliderCheck = {}
local angleStep = 12
local l_cosTable = cosTable
local l_sinTable = sinTable
local Atan = math.atan
local Ceil = math.ceil
local Rad2Deg = math.rad2Deg
local Mod = math.mod
local tempPos = Vector3.zero

local function getPosAngle(a,b)
    if b.x == a.x then return 0 end
    local angle = Atan((b.z-a.z)/(b.x-a.x))*Rad2Deg
    if b.x < a.x then
        angle = angle + 180
    elseif b.z < a.z then
        angle = angle + 360
    end
    return angle
end

local function getRoundPos(angle,radius,centrePos)
    angle = Ceil(angle)
    local x = centrePos.x + l_cosTable[angle]*radius
    local z = centrePos.z + l_sinTable[angle]*radius
    return x,z
end

--[[--
修正停靠点(把目标target按角度angleStep分成N分，计算出monster与target的角度，把monster放到最近的一个角度范围内)
@target 目标
@monster 自身
@dis 距离
@LastAngle 上一次返回的角度 
@refPos 如果该变量不为空，则将结果设置到该vec中，否则创建新的vec
]]
function ColliderCheck.checkStopPos(target,monster,dis,LastAngle, refPos)
    if target.stopAngle == nil then
        target.stopAngle = {}
    end
    monster.lastPathAngle = nil
	local stopAngle = target.stopAngle
    local selfUid = monster.uniqueID
	local targetPosition = target:getPosition()
	local monsterPosition = monster:getPosition()
    if LastAngle ~= nil then
        local x,z = getRoundPos(LastAngle,dis,targetPosition)
		if refPos == nil then
			refPos = Vector3.New(x, targetPosition.y, z)
		else
			refPos.x = x;
			refPos.y = targetPosition.y
			refPos.z = z
		end
        monster.lastPathAngle = LastAngle
        return refPos
    elseif stopAngle[selfUid] then
        local currAnagle = stopAngle[selfUid] 
        stopAngle[currAnagle] = nil
    end
    local angle = getPosAngle(targetPosition,monsterPosition)
    local mod = Mod(angle,angleStep)
    local flag = nil
    if mod > angleStep/2 then
        flag = true
        angle = angle - mod + angleStep
    else
        flag = false
        angle = angle - mod
    end
    if stopAngle[angle] == nil then
        stopAngle[angle] = selfUid
        stopAngle[selfUid] = angle
        local x,z = getRoundPos(angle,dis,targetPosition)
		if refPos == nil then
			refPos = Vector3.New(x, targetPosition.y, z)
		else
			refPos.x = x;
			refPos.y = targetPosition.y
			refPos.z = z
		end
        monster.lastPathAngle = angle
        return refPos
    else
        local i = angleStep
        local originCharacter = nil
        if flag then angle = angle - angleStep end
        while i < 180 do
            originCharacter = nil
            local myAngle = angle + i
            if myAngle > 360 then 
                myAngle = myAngle - 360 
            elseif myAngle < 0 then
                myAngle = myAngle + 360
            end
            if stopAngle[myAngle] ~= nil then
                originCharacter = CharacterManager:getCharacByUId(stopAngle[myAngle])
                if originCharacter == nil or originCharacter.hp <= 0 then
                    stopAngle[myAngle] = nil
                end
            end
            if stopAngle[myAngle] == nil then
                stopAngle[myAngle] = selfUid
                stopAngle[selfUid] = myAngle
                local x,z = getRoundPos(myAngle,dis,targetPosition)
                if refPos == nil then
					refPos = Vector3.New(x, targetPosition.y, z)
				else
					refPos.x = x;
					refPos.y = targetPosition.y
					refPos.z = z
				end
                monster.lastPathAngle = myAngle
                return refPos
            end
            if i > 0 then
                i = -i
            else
                i = -i + angleStep
            end
        end
    end
end



