-- region
-- Date    : 2016-06-21
-- Author  : daiyaorong
-- Description : 技能数据 
-- endregion

SkillVo = {
	skillid = nil,
	skilltype = nil,    -- 技能类型
	skilltarget = nil,
	skilldistance = nil,    -- 释放距离
	buffInfo = nil,
	effectinfo = nil,    -- 技能特效
	bulleteffectinfo = nil,    -- 弹道技能特效
	effecttype = nil,    -- 技能效果
	movement = nil,    -- 技能移动信息
	hittime = nil,    -- 技能打击帧
	collision = nil,    -- 碰撞信息
	hiteffect = nil,    -- 打击效果
	specialeffect = nil,    -- 特殊效果
	sound = nil,    -- 音效
	resttime = nil,		--收招时间
	damagenumbertype = nil,	-- 伤害数字类型
	level = nil,			-- 技能等级
	areawarning = nil,		-- 区域预警
	icon = nil,				-- 技能图标
	action = nil,			-- 技能动作
	direction = nil,		-- 技能朝向
	joystickarea = nil,		-- 摇杆检测区域
	damageadd = nil,		-- 附加伤害
	hitlevel = nil,			-- 打断等级
	name = nil;
	targetdistance = nil,	-- 检测距离 一般是手动战斗或者自动战斗目标无效时使用
}

createClass(SkillVo)

function SkillVo:read( conn )
	-- body
	self.skillid = conn:ReadInt()
	self.skilltype = conn:ReadString()
	self.skilltarget = conn:ReadInt()
	self.skilldistance = conn:ReadString()
	self.buffInfo = conn:ReadString()
	self.effectinfo = conn:ReadString()
	self.bulleteffectinfo = conn:ReadString()
	self.effecttype = conn:ReadInt()
	self.movement = conn:ReadString()
	self.hittime = conn:ReadString()
	self.collision = conn:ReadString()
	self.hiteffect = conn:ReadString()
	self.specialeffect = conn:ReadString()
	self.sound = conn:ReadString()
	self.resttime = conn:ReadInt()
	self.damagenumbertype = conn:ReadString()
	self.areawarning = conn:ReadString()
	self.icon = conn:ReadString()
	self.action = conn:ReadString()
	self.direction = conn:ReadString()
	self.joystickarea = conn:ReadString()
	self.damageadd = conn:ReadString()
	self.hitlevel = conn:ReadString()
	self.name = conn:ReadString();

	if EnvironmentHandler.isInServer then
		self.rawSkillObj = tableplus.shallowcopy(self)
	end

	self:init()
--	--print("打印"..tableplus.formatstring(self,true))
end

function SkillVo:write(conn)
	local rawObj = self.rawSkillObj
	conn:WriteInt(rawObj.skillid)
	conn:WriteString(rawObj.skilltype)
	conn:WriteInt(rawObj.skilltarget)
	conn:WriteString(rawObj.skilldistance)
	conn:WriteString(rawObj.buffInfo)
	conn:WriteString(rawObj.effectinfo)
	conn:WriteString(rawObj.bulleteffectinfo)
	conn:WriteInt(rawObj.effecttype)
	conn:WriteString(rawObj.movement)
	conn:WriteString(rawObj.hittime)
	conn:WriteString(rawObj.collision)
	conn:WriteString(rawObj.hiteffect)
	conn:WriteString(rawObj.specialeffect)
	conn:WriteString(rawObj.sound)
	conn:WriteInt(rawObj.resttime)
	conn:WriteString(rawObj.damagenumbertype)
	conn:WriteString(rawObj.areawarning)
	conn:WriteString(rawObj.icon)
	conn:WriteString(rawObj.action)
	conn:WriteString(rawObj.direction)
	conn:WriteString(rawObj.joystickarea)
	conn:WriteString(rawObj.damageadd)
	conn:WriteString(rawObj.hitlevel)
	conn:WriteString(rawObj.name)
end

local tempTableA = nil
local tempTableB = nil
local G = nil

local function getHitFlySpeed(hitEff)
	local param = hitEff.param
	if G == nil then
		G = math.abs(FightModel:getFightCommon(FightDefine.FIGHT_COMMON_G)) * 0.1 --米/二次方秒
	end
	local time = math.sqrt((2*param[2])/G) * 2 --秒
	local speedy = (time*0.5)*G --米/秒
	local speedx = param[1]/time --米/秒
	if param[2] < 0 then
		speedy = -speedy --符号与Y距离配置保持一致 
	end
	return time,speedx,speedy
end

--处理hittime
local function hitTimeAndHitEffect(skillVo)
	if skillVo.hittime == "0" then
		skillVo.hittime = nil
	else
		tempTableA = StringUtils.split(skillVo.hittime,",")
		skillVo.hittime = {}
		for k,obj in ipairs( tempTableA ) do
			skillVo.hittime[k] = tonumber(obj)
		end 
	end

	if skillVo.hiteffect == "0" then
		skillVo.hiteffect = nil
	else
		tempTableA = StringUtils.split(skillVo.hiteffect,",")
		skillVo.hiteffect = {}
		local tempHit = nil
		for k,v in ipairs( tempTableA ) do
			tempHit = StringUtils.split(tempTableA[k],"|")
			local tempType = tonumber(tempHit[1])
			if tempType ~= 0 then
				local hitTb = { type=tempType, param=nil, hitspeedX=0, hitflyTime=0, hitflyspeedX=0, hitflyspeedY = 0}
                skillVo.hiteffect[k] = hitTb
				if tempType == 1 or tempType == 2 then
					hitTb.param = StringUtils.split(tempHit[2],"+",nil,tonumber)
				end
                if tempType == 1 then
					if hitTb.param and hitTb.param[2] ~= 0 then --分母不可为0
						hitTb.hitspeedX = (hitTb.param[1]*0.1) / (hitTb.param[2]*0.001)
					end
                elseif tempType == 2 then
                    hitTb.param[1] = hitTb.param[1] * 0.1
                    hitTb.param[2] = math.abs(hitTb.param[2]) * 0.1
					hitTb.hitflyTime,hitTb.hitflyspeedX, hitTb.hitflyspeedY = getHitFlySpeed(hitTb)
                end
			end
		end
	end
end 

-- 1+1|2+2,1+1|2+2
local function specialEffect(skillVo)
	if skillVo.specialeffect == "0" then
		skillVo.specialeffect = nil
	else
		tempTableA = StringUtils.split(skillVo.specialeffect,",")
		if #tempTableA ~= #skillVo.hittime then 
			-- --CmdLog("specialEffect字段错误:id:"..skillVo.skillid)
			return
		end 
		skillVo.specialeffect = {}
		for k,specialObj in pairs(tempTableA) do 
			if specialObj ~= "0" then 
				local tempData = {}
				local tempStr = StringUtils.split(specialObj,"|")
				if tempStr ~= "0" then 
					for index,param in pairs(tempStr) do 
						local tempLast = StringUtils.split(param,"+")
						local effType = tonumber(tempLast[1])
						if effType then
							tempData.type = effType
							if effType == 1 then --命中震屏
								tempData.shakeId = tonumber(tempLast[2])
							elseif effType == 3 then --技能震屏
								tempData.actionShakeId = tonumber(tempLast[2])
							elseif effType == 4 then --打击特效
								tempData.hitEffectName = tempLast[2]
							elseif effType == 5 then --屏幕特效
								tempData.screenEffect = tempLast[2]
							elseif effType == 6 then --模糊
								tempData.blurinfo = {
									inTime = tonumber(tempLast[2]) * 0.001,
									outTime = tonumber(tempLast[3]) * 0.001,
									inPower = tonumber(tempLast[4]),
									outPower = tonumber(tempLast[5]),
								}
							end 
						end 
					end 
				end
				skillVo.specialeffect[k] = tempData
			else
				skillVo.specialeffect[k] = specialObj
			end 
		end 
	end
end 

local function handleStartPoint(moveData,startStr,endStr)
	local tempStart = StringUtils.split(startStr,"+") 
	local tempEnd = StringUtils.split(endStr,"+") 
	moveData.startpositiontype = tonumber(tempStart[1])
	moveData.endpositiontype = tonumber(tempEnd[1])
	local tempStartY = 0
	if tempStart[4] then
		tempStartY = tonumber(tempStart[4])
	end
	moveData.startposition = Vector3.New(tonumber(tempStart[2])
		,tempStartY,tonumber(tempStart[3])) * 0.1
	
	local tempEndY = 0
	if tempEnd[4] then
		tempEndY = tonumber(tempEnd[4])
	end
	moveData.endposition = Vector3.New(tonumber(tempEnd[2])
		,tempEndY,tonumber(tempEnd[3])) * 0.1
end 

local function SpeedStraightMoveHandler( moveData, paramStr )
    moveData.speedx = math.abs(tonumber(paramStr) * 0.1)
	moveData.speedy = 0
	moveData.time = 0
end

local function TimeStraightMoveHandler( moveData, paramStr )
    moveData.time = TrackCalculator.timeAjust(math.abs(tonumber(paramStr) * 0.001))
	moveData.speedx = 0
	moveData.speedy = 0
end

local function SpeedParabolaMoveHandler( moveData, paramStr )
    local temp = StringUtils.split(paramStr,"+") 
	moveData.speedx = math.abs(tonumber(temp[1]) * 0.1)
	moveData.speedy = math.abs(tonumber(temp[2]) * 0.1)
	moveData.time = 0
end

local function TimeParabolaMoveHandler( moveData, paramStr )
    local temp = StringUtils.split(paramStr,"+") 
	moveData.time = TrackCalculator.timeAjust(math.abs(tonumber(temp[1]) * 0.001))
	moveData.speedy = math.abs(tonumber(temp[2]) * 0.1)
	moveData.speedx = 0
end

local function DirecStraightMoveHandler( moveData, paramStr )
    local temp = StringUtils.split(paramStr,"+") 
	moveData.speedx = tonumber(temp[1]) * 0.1
	moveData.time = 0
	moveData.speedy = 0	
end

local function CircleMoveHandler( moveData, paramStr )
    moveData.speedx = tonumber(paramStr) * 0.1
	moveData.speedy = 0
	moveData.time = 0
end

local function TraceMoveHandler( moveData, paramStr )
    moveData.speedx = tonumber(paramStr) * 0.1
	moveData.speedy = 0
	moveData.time = 0
end

local function AutoFaceHandler( moveData, paramStr )
	moveData.speedx = math.abs(tonumber(paramStr))
	moveData.speedy = 0
	moveData.time = 0
end

local MoveParamHandlerList = {
    [FightDefine.TRACK_SPEEDSTRAIGHT]   = SpeedStraightMoveHandler,
    [FightDefine.TRACK_TIMESTRAIGHT]    = TimeStraightMoveHandler,
    [FightDefine.TRACK_SPEEDPARABOLA]   = SpeedParabolaMoveHandler,
    [FightDefine.TRACK_TIMEPARABOLA]    = TimeParabolaMoveHandler,
    [FightDefine.TRACK_DIRECSTRAIGHT]   = DirecStraightMoveHandler,
    [FightDefine.TRACK_CIRCLE]          = CircleMoveHandler,
    [FightDefine.TRACK_TRACE]           = TraceMoveHandler,
    [FightDefine.TRACK_AUTOFOLLOW]      = SpeedStraightMoveHandler,
    [FightDefine.TRACK_AUTOFACE]        = AutoFaceHandler,
    [FightDefine.TRACK_TRACE_CIRCLE]        = CircleMoveHandler,
    [FightDefine.TRACK_AUTOFOLLOWTURN]      = SpeedStraightMoveHandler,
}

local function handlerMoveParam(moveData,paramStr)
    MoveParamHandlerList[moveData.tracktype](moveData,paramStr)
end 

local function handleTimeParam(moveData,timeParam)
	local temp = StringUtils.split(timeParam,"+") 
	moveData.starttime = tonumber(temp[1]) * ConstantData.FRAME_RATE*0.001
	moveData.endtime = tonumber(temp[2]) * ConstantData.FRAME_RATE*0.001
    moveData.moveframe = moveData.endtime - moveData.starttime
end 

local function getMoveData(moveStr)
	if moveStr == nil or moveStr == "0" then return end 
	-- 1|2+0+0+0|2+0+0+0|167|0+167
	local moveData = {}
	local tempMove = StringUtils.split(moveStr,"|") 
	if #tempMove ~= 5 then 
		return 
	end
	moveData.tracktype = tonumber(tempMove[1])
	handleStartPoint(moveData,tempMove[2],tempMove[3])
	handlerMoveParam(moveData,tempMove[4])
	handleTimeParam(moveData,tempMove[5])
	return moveData
end 

local function initMovement(skillVo)
	-- 1|2+0+0+0|2+0+0+0|167|0+167,2|2+0+0+0|2+20+0+0|400|167+567,1|2+0+0+0|2+0+0+0|100|567+667
	if skillVo.movement == "0" then
		skillVo.movement = nil
	else
		tempTableA = StringUtils.split(skillVo.movement,",")
		skillVo.movement = {}
		for k,moveStr in pairs(tempTableA) do 
			local moveData = getMoveData(moveStr)
			skillVo.movement[k] = moveData
			if moveData ~= nil then 
				moveData.skillid = skillVo.skillid
			end  
		end 
	end
end 

local function initSound(skillVo)
	if skillVo.sound == "0" then
		skillVo.sound = nil
	else
		tempTableA = StringUtils.split(skillVo.sound,",")
		local tempInfo = nil
		skillVo.sound = {}
		for k,soundStr in ipairs(tempTableA) do
			local soundInfo = {}
			tempInfo = StringUtils.split(soundStr,"|")
			soundInfo.soundList = StringUtils.split(tempInfo[1],"+")
			soundInfo.soundNum = #soundInfo.soundList
			soundInfo.delay = tonumber(tempInfo[2]) * 0.001 * ConstantData.FRAME_RATE
			skillVo.sound[k] = soundInfo
		end
	end
end 

local function initCollision( skillVo )
	-- body
	if skillVo.collision == "0" then
		skillVo.collision = nil
	else
		tempTableA = StringUtils.split(skillVo.collision,",")
		if #tempTableA > 0 then
			skillVo.collision = {}
			local temp = nil
			for i = 1, #tempTableA do
				skillVo.collision[i] = {}
				temp = StringUtils.split(tempTableA[i],"|")
				skillVo.collision[i].shape = tonumber(temp[1])
				if skillVo.collision[i].shape == FightDefine.BULLET_SHAPE_CIRCLE then
					skillVo.collision[i].radius = tonumber(temp[2]) * 0.1
				elseif skillVo.collision[i].shape == FightDefine.BULLET_SHAPE_RECT then
					local tempTable = StringUtils.split(temp[2],"+",nil,tonumber)
					skillVo.collision[i].xLength = tempTable[1] * 0.1
					skillVo.collision[i].zLength = tempTable[2] * 0.1
				else
					local tempTable = StringUtils.split(temp[2],"+",nil,tonumber)
					skillVo.collision[i].radius = tempTable[1] * 0.1
					skillVo.collision[i].angle = tempTable[2]
				end
				skillVo.collision[i].rotate = tonumber(temp[3])
				skillVo.collision[i].offset = StringUtils.split(temp[4],"+",nil,tonumber)
				temp = skillVo.collision[i].offset
				for j=1, #temp do
					temp[j] = temp[j] * 0.1
				end
			end
		end
	end
end

local function getSkillActionEff(str)
	local effData = {}
	local tempStr = StringUtils.split(str,"+")
	effData.id = tempStr[1]
	effData.originId = tempStr[1]
	effData.bone = tempStr[2]
	if effData.bone == "0" then
		effData.bone = "FootCenter_Point00"
	end
	effData.offset = {}
	effData.offset[1] = tonumber(tempStr[3])
	effData.offset[2] = tonumber(tempStr[4])
	effData.offset[3] = tonumber(tempStr[5])
	effData.beginTime = tonumber(tempStr[6]) * 0.001 * ConstantData.FRAME_RATE
	return effData
end 

local function getBulletActionEff(str)
	local effData = {}
	local tempStr = StringUtils.split(str,"+")
	effData.model = tempStr[1]
	effData.originModel = tempStr[1]
	effData.beginTime = tonumber(tempStr[2]) * 0.001 * ConstantData.FRAME_RATE
	effData.lastTime = tonumber(tempStr[3]) * 0.001 * ConstantData.FRAME_RATE
	effData.hitFrameGap = tonumber(tempStr[4]) * 0.001 * ConstantData.FRAME_RATE
	effData.bulletType = tonumber(tempStr[5])
	if effData.bulletType == FightDefine.BULLET_NORMAL then
		effData.maxHit = tonumber(tempStr[6])
	elseif effData.bulletType == FightDefine.BULLET_PIERCE then
		effData.maxHit = 999	--无限打击次数
		effData.hurtLimit = tonumber(tempStr[6])
	elseif effData.bulletType == FightDefine.BULLET_BOMB then
		effData.maxHit = 1 		--只打击一次
		effData.hurtArea = tonumber(tempStr[6]) * 0.1
		effData.bombeff = tempStr[7]
	end
	return effData
end 

local function initEffectInfo(skillVo)
	-- 1|eff_chr_hero_liubei01_bullet02+FootCenter_Point00+0+0+0+0,XXX
	if skillVo.effectinfo == "0" then
		skillVo.effectinfo = nil
	else
		tempTableA = StringUtils.split(skillVo.effectinfo,",")
		skillVo.effectinfo = {}
		local effData = nil
		local tempStr = nil
		for k,effectStr in pairs(tempTableA) do
			effData = nil
			tempStr = StringUtils.split(effectStr,"|")
			effData = getSkillActionEff(tempStr[1])
			if effData then 
				skillVo.effectinfo[k] = effData
			end 
		end 
	end
end 

local function initBulletEffectInfo(skillVo)
	-- body
	if skillVo.bulleteffectinfo == "0" then
		skillVo.bulleteffectinfo = nil
	else
		tempTableA = StringUtils.split(skillVo.bulleteffectinfo,",")
		skillVo.bulleteffectinfo = {} 
		local effData = nil
		local tempStr = nil
		for k,effectStr in pairs(tempTableA) do
			effData = nil
			tempStr = StringUtils.split(effectStr,"|")
			effData = getBulletActionEff(tempStr[1])
			if effData then 
				skillVo.bulleteffectinfo[k] = effData
			end 
		end 
	end
end

local function initSkillType(skillVo)
	-- body
	if skillVo.skilltype == "0" then
		skillVo.skilltype = nil
	else
		local temp = StringUtils.split(skillVo.skilltype,"|")
		skillVo.skilltype = tonumber(temp[1])
	end
end

local function initSkillDistance(skillVo)
	-- body
	if skillVo.skilldistance ~= "0" then
		local temp = StringUtils.split(skillVo.skilldistance,"|",nil,tonumber)
		skillVo.distanceType = temp[1]
		skillVo.skilldistance = temp[2] or 0 
		skillVo.skilldistance = skillVo.skilldistance * 0.1
	else
		skillVo.distanceType = 1
		skillVo.skilldistance = 0
	end
end

-- 解析buff数据(按打击帧分, 一个打击帧一个列表)
local function initBuffInfo(skillVo)
	if skillVo.buffInfo == "0" or skillVo.buffInfo == "" then
		skillVo.buffInfo = nil
	else
		tempTableA = StringUtils.split(skillVo.buffInfo, ",")
		skillVo.buffInfo = {}
		skillVo.buffIdList = {}
		local arr = nil
		local buffArr = nil
		local tempTb = nil
		for k,v in ipairs(tempTableA) do
			arr = StringUtils.split(v, "|")
			tempTb = {}
			for _,buffStr in ipairs(arr) do
				buffArr = StringUtils.split(buffStr, "+", nil, tonumber)
				tempTb[#tempTb+1] = {buffId = buffArr[1], target = buffArr[2]}
				table.insert( skillVo.buffIdList, buffArr[1] )
			end
			skillVo.buffInfo[k] = tempTb
		end
	end
end

local function initAreawarning( skillVo )
	-- body
	if skillVo.areawarning == "0" then
		skillVo.areawarning = nil
	else
		local temp = StringUtils.split(skillVo.areawarning, ",")
		local temp1 = nil
		skillVo.areawarning = {}
		for k,v in ipairs( temp ) do
			skillVo.areawarning[k] = {}
			v = StringUtils.split(v, "|")
			skillVo.areawarning[k]["beginTime"] = tonumber(v[1]) * 0.001 * ConstantData.FRAME_RATE
			skillVo.areawarning[k]["areaInfo"] = StringUtils.split(v[2], "+", nil, tonumber)
			skillVo.areawarning[k]["pos"] = StringUtils.split(v[3], "+", nil, tonumber)
			temp1 = StringUtils.split(v[3], "+", nil, tonumber)
			skillVo.areawarning[k]["posType"] = temp1[1]
			skillVo.areawarning[k]["pos"] = Vector3.New( temp1[2],0,temp1[3] ) * 0.1
			skillVo.areawarning[k]["angle"] = tonumber(v[4])
			skillVo.areawarning[k]["lifeTime"] = tonumber(v[5]) * 0.001
			skillVo.areawarning[k]["endTime"] = skillVo.areawarning[k]["beginTime"] + skillVo.areawarning[k]["lifeTime"] * ConstantData.FRAME_RATE
		end
	end
end

local function initDirection( skillVo )
	-- body
	if skillVo.direction == "0" then
		skillVo.direction = nil
	else
		skillVo.direction = StringUtils.split(skillVo.direction, ",", nil, tonumber)
	end
end

local function initAction( skillVo )
	-- body
	if skillVo.action ~= "0" then
		skillVo.action = StringUtils.split(skillVo.action, "+")
	else
		skillVo.action = nil
	end
end

local function initJoystickArea( skillVo )
	-- body
	if skillVo.joystickarea == "0" then
		skillVo.joystickarea = nil
	else
		tempTableA = StringUtils.split(skillVo.joystickarea,"|")
		skillVo.joystickarea = {}
		skillVo.joystickarea.shape = tonumber(tempTableA[1])
		if skillVo.joystickarea.shape == FightDefine.BULLET_SHAPE_CIRCLE then
			skillVo.joystickarea.radius = tonumber(tempTableA[2]) * 0.1
		elseif skillVo.joystickarea.shape == FightDefine.BULLET_SHAPE_RECT then
			local tempTable = StringUtils.split(tempTableA[2],"+",nil,tonumber)
			skillVo.joystickarea.xLength = tempTable[1] * 0.1
			skillVo.joystickarea.zLength = tempTable[2] * 0.1
		else
			local tempTable = StringUtils.split(tempTableA[2],"+",nil,tonumber)
			skillVo.joystickarea.radius = tempTable[1] * 0.1
			skillVo.joystickarea.angle = tempTable[2]
		end
		skillVo.joystickarea.rotate = tonumber(tempTableA[3])
		skillVo.joystickarea.offset = StringUtils.split(tempTableA[4],"+",nil,tonumber)
		tempTableA = skillVo.joystickarea.offset
		for j=1, #tempTableA do
			tempTableA[j] = tempTableA[j] * 0.1
		end
	end
end

local function initDamageadd( skillVo )
    if skillVo.damageadd == "0" then
        skillVo.damageadd = nil
    else
        tempTableA = StringUtils.split(skillVo.damageadd,"+",nil,tonumber)
        skillVo.damageadd = { ["targetType"]=tempTableA[1],
                              ["conditionType"]=tempTableA[2],
                              ["percent"]=tempTableA[3] * 0.001,
                              ["param"]=tempTableA[4],
                            }
    end
end

local function initHitLevel( skillVo )
	if skillVo.hitlevel == "0" then
		skillVo.hitlevel = nil
	else
		tempTableA = StringUtils.split(skillVo.hitlevel,",")
		skillVo.hitlevel = {}
		local tempTableC = nil
		for k,v in ipairs(tempTableA) do
			skillVo.hitlevel[k] = {}
			tempTableB = StringUtils.split(v,"|")
			skillVo.hitlevel[k]["level"] = tonumber(tempTableB[1])
			tempTableC = StringUtils.split(tempTableB[2],"+",nil,tonumber)
			skillVo.hitlevel[k]["targetType"] = tempTableC[1]
			skillVo.hitlevel[k]["levelUp"] = tempTableC[2]
			skillVo.hitlevel[k]["conditionType"] = tempTableC[3]
			skillVo.hitlevel[k]["param"] = tempTableC[4]
		end
	end
end

function SkillVo:init()
	hitTimeAndHitEffect(self)
	specialEffect(self)
	initMovement(self)
	initSound(self)
	initCollision(self)
	initEffectInfo(self)
	initBulletEffectInfo(self)
	initSkillType(self)
	initSkillDistance(self)
	initBuffInfo(self)
	initAreawarning(self)
	initDirection(self)
	initAction(self)
	initJoystickArea(self)
	initDamageadd(self)
	initHitLevel(self)
	self.effecttype = tonumber(self.effecttype)
	self.resttime = self.resttime * 0.001 * ConstantData.FRAME_RATE
	self.damagenumbertype = StringUtils.split(self.damagenumbertype, ",", nil, tonumber)
end

function SkillVo.parse( self )
	-- body
	self.buffInfo = self.buffinfo
	hitTimeAndHitEffect(self)
	specialEffect(self)
	initMovement(self)
	initSound(self)
	initCollision(self)
	initEffectInfo(self)
	initBulletEffectInfo(self)
	initSkillType(self)
	initSkillDistance(self)
	initBuffInfo(self)
	initAreawarning(self)
	initDirection(self)
	initAction(self)
	initJoystickArea(self)
	initDamageadd(self)
	initHitLevel(self)
	self.resttime = self.resttime * 0.001 * ConstantData.FRAME_RATE
	self.damagenumbertype = StringUtils.split(self.damagenumbertype, ",", nil, tonumber)
	self.targetdistance = self.targetdistance * 0.1
end
-------------------------------------------------------------------------------------------------------------------------------
--r egion *.lua
--Date 20170721
--Author :zhangluesen
--Description :阵营数据层
--endregion

--module("game/modules/RideModule/model/RideModel", package.seeall)

CampDailyModel = {}
local jobSkillData = nil

local normalSkillData = nil
local accordSkillData = nil
local passiveSkillData = nil
local campDailySkill = nil
local campDailyAllSkill = nil
local campDailyPassiveSkill = nil
--[[]]
---------------------------以上为产品数据,以下为用户数据

local normalHitData = { -- 普通技能
        id = -1,
        level = 1, 
    }
local skillHitData = { -- 特殊技能
        id = -1,
        level = 1, 
    }
local skillPassiveList = nil -- 被动技能
local levelUpList = {}


function CampDailyModel.getCampDailySkill()
    return campDailySkill
end

function CampDailyModel.getCampDailyAllSkill()
    return campDailyAllSkill
end

function CampDailyModel.getCampDailyPassiveSkill()
    return campDailyPassiveSkill
end

function CampDailyModel.setNormalHitData(skillId, level)
    normalHitData.id = skillId
    normalHitData.level   = level
end

function CampDailyModel.getNormalHitData()
    return normalHitData
end

function CampDailyModel.setSkillHitData(skillId,level)
    skillHitData.id = skillId
    skillHitData.level   = level    
end

function CampDailyModel.getSkillHitData()
    return skillHitData
end

function CampDailyModel.insertPassiveSkillList(index,skillid,level)
    if skillPassiveList == nil then
        skillPassiveList = {}
    end
    if skillPassiveList[index] == nil then
        skillPassiveList[index] = {
            skillGroup = {},
            level = 1,
            idx = index,
        }
    end
    table.insert(skillPassiveList[index].skillGroup,skillid)
    skillPassiveList[index].level = #skillPassiveList[index].skillGroup
end

function CampDailyModel.getPassiveSkillList()
    return skillPassiveList
end

function CampDailyModel.insertLevelUpList(data)
    table.insert(levelUpList,data)
end

function CampDailyModel.getLevelUpListData()
    if #levelUpList > 0 then
        return table.remove(levelUpList,#levelUpList)
    end
    return nil
end

function CampDailyModel.resetStageData()
    if normalSkillData and #normalSkillData > 0 then 
        normalHitData.id = normalSkillData[1].skillGroup[1] 
        normalHitData.level = 1
    end
    skillHitData.id = -1
    skillHitData.level = 1
    skillPassiveList = {}
    levelUpList = {}
end

local function saveSkillData()
    if jobSkillData == nil then
        jobSkillData = {}
    else
        return
    end
    local config = CFG.campskill:getAll()
    local skillConfig = CFG.skill:getAll()
    if config then
        for _,v in pairs(config) do
            local temp = {}
            temp.skillGroup = StringUtils.split(v.skillid,"|",nil,tonumber)
            temp.type = tonumber(v.type)
            temp.destGroup  = StringUtils.split(v.desc,"|")
            temp.odds = v.odds
            temp.condition = v.condition
            local att = string.split(v.opencondition,"|")
            temp.opencondition = {}
            for j=1,#att do
                local att2 = string.split(att[j],"+")
                temp.opencondition[tonumber(att2[1])] = {--1,等级，2，贵族，3，官职
                    min = tonumber(att2[2]),
                    max = tonumber(att2[3]),
                }
            end
            -- 图标数据
            temp.iconGroup  = {}
            temp.nameGroup = {}
            temp.param  = {}
            for j=1,#temp.skillGroup do
                local skillData = skillConfig[temp.skillGroup[j]]
                if skillData then
                    temp.iconGroup[j] = skillData.icon
                    temp.nameGroup[j] = skillData.name
                    --local tips = CFG.gametext:getFormatText(temp.destGroup[j])
                    temp.param[j] = {}
                    local paramIndex = 0
                    local skillLvConfig = FightModel:getSkillLevelData( temp.skillGroup[j], j )
                    if skillLvConfig and skillLvConfig.damagedesc ~= "0" then
                        local att = string.split(skillLvConfig.damagedesc,"|")
                        for k=1,#att do
                            local att2 = string.split(att[k],"+")
                            if #att2 >= 2 then
                                paramIndex = paramIndex + 1
                                if att2[1] == "2" then--整值
                                    temp.param[j][paramIndex] = att2[2] 
                                elseif att2[1] == "3" then --百分比
                                    temp.param[j][paramIndex] = string.format("%s%s",tonumber(att2[2])*100,"%")
                                end 
                            end
                        end
                        --[[
                        if paramIndex > 0 then
                            paramIndex = 1
                            tips = string.gsub(tips, "%%s", function(s)
                                paramIndex = paramIndex + 1
                                return temp.param[j][paramIndex-1]
                             end)
                        end
                        ]]
                    end
                    --temp.destGroup[j] = tips
                end
            end
            --技能说明外部显示
            local tempSkillData = skillConfig[temp.skillGroup[1]];
            if tempSkillData then
            	temp.skillDescOutShow = tempSkillData.describ;
            end

            temp.index = #jobSkillData+1
            table.insert(jobSkillData, temp)
            if temp.type == 1 then  -- 普通技能 
                if normalSkillData == nil then
                    normalSkillData = {}
                end
                table.insert(normalSkillData, temp)
            elseif temp.type == 2 then -- 主动技能 
                if accordSkillData == nil then
                    accordSkillData = {}
                end
                table.insert(accordSkillData, temp)
            elseif temp.type == 3 then -- 被动技能 
                if passiveSkillData == nil then
                    passiveSkillData = {}
                end
                table.insert(passiveSkillData, temp)
            end
        end      
    end     
    
    
            -- 阵营日常技能替换
    campDailyAllSkill = {}
    local campSkillConfig = CampDailyModel.getCampJobSkill()
    if campSkillConfig ~= nil then
        for i=1,#campSkillConfig do
            for j=1,#campSkillConfig[i].skillGroup do
                local skillid = campSkillConfig[i].skillGroup[j]
                campDailyAllSkill[skillid] = { level=j, damage=0, type = campSkillConfig[i].type,skillAttr = nil }
            end
        end            
    end 
    campDailyPassiveSkill = ""
    campDailySkill = ""
    for key, value in pairs( campDailyAllSkill ) do
        if value.type == 3 then
            campDailyPassiveSkill = campDailyPassiveSkill .. key.. "+" .. 1  .."|"   --记录所有被动技能         
        elseif value.type == 1 then
            campDailySkill = campDailySkill.. 1 .."="..key.."=".. value.level .."|"
        elseif value.type == 2 then
            campDailySkill = campDailySkill.. 4 .."="..key.."=".. value.level .."|"
        elseif value.type == 4 then
            campDailySkill = campDailySkill .. 5 .. "=" .. key .."=".. value.level .."|" --阵营日常时,闪避技能类型为
        end  
    end   
end

local function cleckCondition(skillData, level, vip, normalPosition)
    if skillData then
        if skillData.opencondition[1] then -- 等级
            --GameLog("zls","curr:",level,"min:",skillData.opencondition[1].min,"max:",skillData.opencondition[1].max)
            if level < skillData.opencondition[1].min or level > skillData.opencondition[1].max then

            else
                return true
            end
        end
        if skillData.opencondition[2] then -- 贵族
            if vip < skillData.opencondition[2].min or vip > skillData.opencondition[2].max then

            else
                return true
            end          
        end
        if skillData.opencondition[3] then -- 官职
            if normalPosition < skillData.opencondition[3].min or normalPosition > skillData.opencondition[3].max then

            else
                return true
            end                
        end
        --当满足上面其中一个条件则通过,假如有条件但一个不满足不则不通过
        if skillData.opencondition[1] or skillData.opencondition[2] or skillData.opencondition[3] then
            return false
        end
        --当没有条件则默认通过
        return true
    end
    return false
end

function CampDailyModel.getRandomFun(list, count, bRepeat)
    local copyList = {}
    for i=1, #list do
        copyList[i] = list[i]        
        copyList[i].readOdds = list[i].odds
    end

    if #copyList <= count then -- 当列表小于需要的数量时
        return copyList
    end
    local resultList = {}
    for z=1,count do
        local totalOdds = 0
        for k,v in pairs(copyList) do
            totalOdds = totalOdds + v.readOdds
        end

        local randomCount = math.random(1,totalOdds)
        for i=1,#copyList do
            randomCount = randomCount - copyList[i].readOdds
            if randomCount <= 0 then
                --GameLog("zls","random result:",i)
                table.insert(resultList,copyList[i])
                if bRepeat == false then
                    copyList[i].readOdds = 0
                end
                break
            end
        end
    end
    return resultList
end

function CampDailyModel.getRandomSkill(level,vip,normalPosition)
    local randomSkillList = {} -- 整天可选择的列表
    for i=1,#jobSkillData do 
        local isIn = true
        if cleckCondition(jobSkillData[i],level,vips,normalPosition) == false then
            --GameLog("zls","cleckCondition:false")
            isIn = false
        end   
        if jobSkillData[i].type == 1 then -- 普通技能
            if #jobSkillData[i].skillGroup >= normalHitData.level then --使用数组长度作为技能等级大小,当该技能等级大于等于当前技能等级时
                if jobSkillData[i].skillGroup[normalHitData.level] == normalHitData.id then--当该技能为当前技能时
                    if #jobSkillData[i].skillGroup == normalHitData.level then -- 当该技能等级最大时不需进队列
                        --GameLog("zls","normal:false")
                        isIn = false
                    end
                end            
            end    
        elseif jobSkillData[i].type == 2 then -- 主动技能      
            if #jobSkillData[i].skillGroup >= skillHitData.level then --使用数组长度作为技能等级大小,当该技能等级大于等于当前技能等级时
                if jobSkillData[i].skillGroup[skillHitData.level] == skillHitData.id then--当该技能为当前技能时
                    if #jobSkillData[i].skillGroup == skillHitData.level then -- 当该技能等级最大时不需进队列
                        --GameLog("zls","主动:false")
                        isIn = false
                    end
                end            
            end                   
        elseif jobSkillData[i].type == 3 then -- 被动技能
            if skillPassiveList[i] then -- 加入当前的被动技能已经满级,则不加进队列
                if #skillPassiveList[i].skillGroup >= #jobSkillData[i].skillGroup then
                    isIn = false
                end
            end
--[[
            if #jobSkillData[i].skillGroup > 0 then
                local count = 0
                for j=1,#jobSkillData[i].skillGroup do 
                    for k=1, #skillPassiveList do
                        if skillPassiveList[k] == jobSkillData[i].skillGroup[j] then -- 因为被动每个等级也是不同的技能,所以计算拥有的数量
                            count = count + 1
                        end
                    end
                end
                if count == #jobSkillData[i].skillGroup then --根据计算得到的技能数量判断是否满级
                    --GameLog("zls","passive:false")
                    isIn = false
                end
            end
   ]]
        elseif jobSkillData[i].type == 4 then -- 闪避技能
            isIn = false
        end   
        if isIn == true then
            table.insert(randomSkillList,jobSkillData[i])
        end
    end
    return CampDailyModel.getRandomFun(randomSkillList,3,false)
end

function CampDailyModel.getCampJobSkill()
    return jobSkillData
end

function CampDailyModel.register()
    saveSkillData()
end

function CampDailyModel.getNormalSkillData()
    if jobSkillData==nil then
        saveSkillData()
    end
    return normalSkillData
end

function CampDailyModel.getAccordSkillData()
    if jobSkillData==nil then
        saveSkillData()
    end
    return accordSkillData
end

function CampDailyModel.getPassiveSkillData()
    if jobSkillData==nil then
        saveSkillData()
    end
    return passiveSkillData
end

