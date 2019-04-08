--------------------------------------------------------------
-- region FindPathAI.lua
-- Date : 2016-7-7
-- Author : jjm
-- Description : 怪物寻路AI
-- endregion
---------------------------------------------------------------

FindPathAI = {
    aiType = nil, --AI类型
    dis = nil, --行为距离
    monster = nil,--monster对象
    target = nil,--目标对象
    surroundData = nil, --环绕寻路数据
	intervalFrame = 0,
	currentFrame = nil,
	isRunning = false,
}
FindPathAI = createClass(FindPathAI)
local Atan = math.atan
local Rag2Deg = math.rad2Deg
local standardAngle = math.standardAngle
local l_cosTable = cosTable
local l_sinTable = sinTable
local Abs = math.abs
local Random = math.random
local Acos = math.acos
local Ceil = math.ceil
local Pow = math.pow
local tempPos = Vector3.zero
local resultPos = Vector3.zero

local function getFrameInterval()
	-- 服务端环境下的运行频率
	if EnvironmentHandler.isInServer then
		return 15
	-- 组队模式下的运行频率
	elseif StageManager.getCurStageType() == ConstantData.STAGE_TYPE_TEAM then
		return 15
	-- 默认的运行频率
	else
		return 3
	end
end


local function getPosAngle(a,b)
    local angle = Atan((b.z-a.z)/(b.x-a.x))*Rag2Deg
    if b.x < a.x then
        angle = angle + 180
    elseif b.z < a.z then
        angle = angle + 360
    end
    return angle
end

local function getRoundPos(angle,radius,centrePos)
    angle = standardAngle(angle)
    if l_cosTable[angle] == nil then
        return centrePos.x,centrePos.z
    end
    local x = centrePos.x + l_cosTable[angle]*radius
    local z = centrePos.z + l_sinTable[angle]*radius
    return x,z
end

--跟随
local function follow( self )
    local monsterPos = self.monster:getPosition()
    local targetPos = self.target:getPosition()
    local distance = CharacterUtil.distanceWithoutY(monsterPos,targetPos)
    if distance - self.dis > CharacterConstant.RUN_PRECISION then
        resultPos = ColliderCheck.checkStopPos(self.target,self.monster,self.dis,self.monster.lastPathAngle, resultPos)
        return resultPos
    end
end

--远离
local function away( self )
    local monsterPos = self.monster:getPosition()
    local targetPos = self.target:getPosition()
    if resultPos == nil then
        resultPos = Vector3.zero
    end
	resultPos.x = monsterPos.x - targetPos.x
	resultPos.y = monsterPos.y - targetPos.y
	resultPos.z = monsterPos.z - targetPos.z
	resultPos:SetNormalize()
	resultPos.x = resultPos.x + monsterPos.x
	resultPos.y = resultPos.y + monsterPos.y
	resultPos.z = resultPos.z + monsterPos.z
    return resultPos
end

--保持距离
local function distance( self )
    local monsterPos = self.monster:getPosition()
    local targetPos = self.target:getPosition()
    local distance = CharacterUtil.distanceWithoutY(monsterPos,targetPos)
    if Abs(distance - self.dis) > CharacterConstant.RUN_PRECISION then
        resultPos = ColliderCheck.checkStopPos(self.target,self.monster,self.dis,self.monster.lastPathAngle, resultPos)
        if self.monster.findPathState == CharacterConstant.FINDPATH_STATE_SUCCESS then
			return resultPos, self.target.uniqueID
        end
    end
end

--环绕
local function surround( self )
    local monsterPos = self.monster:getPosition()
    local targetPos = self.target:getPosition()
    if not self.surroundData.angle then
        self.surroundData.angle = getPosAngle(targetPos,monsterPos)
    end
    if self.surroundData.isClockwise then
        self.surroundData.angle = self.surroundData.angle + self.surroundData.minAngle
    else
        self.surroundData.angle = self.surroundData.angle - self.surroundData.minAngle
    end
    local x,z = getRoundPos(self.surroundData.angle,self.dis,targetPos)
    tempPos:Set(x,monsterPos.y,z)
    return tempPos
end

local funcTable = {
    [0] = follow,
    [1] = away,
    [2] = distance,
    [3] = surround,
}

function FindPathAI:start(type,dis,monster,target)
    self.aiType = type
    self.dis = dis
	self.sqrDis = dis * dis
    self.monster = monster
    self.target = target
	local frameTick = getFrameInterval()
    if not self.surroundData then self.surroundData = {} end
    -- 类型3：环绕型
    if type == 3 then
        self.surroundData.isClockwise = Random(1, 2) == 1 and true or false
        self.surroundData.minAngle = Acos(1 - Pow(self.monster.framespeed * frameTick,2)/Pow(self.dis,2)/2)*Rag2Deg
        self.surroundData.minAngle = Ceil(self.surroundData.minAngle)
    end
	self.intervalFrame = frameTick
	self.currentFrame = 0
	self.isRunning = true
end

function FindPathAI:update()
	if self.currentFrame >= self.intervalFrame then
		self.currentFrame = 0
		if self.target then
			return funcTable[self.aiType](self)
		end
	end
	self.currentFrame = self.currentFrame + FightDefine.AI_RUN_INTERVAL
end

function FindPathAI:stop()
	self.isRunning = false
    self.aiType = nil
    self.surroundData = {}
end