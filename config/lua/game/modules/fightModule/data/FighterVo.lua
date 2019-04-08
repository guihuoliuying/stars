-- region
-- Date    : 2016-08-30
-- Author  : daiyaorong
-- Description :  战斗实体数据集
-- endregion

FighterVo = 
{
	characterType = nil,		-- 角色类型
	uniqueID = nil,				-- 角色唯一ID
	name = nil,					-- 角色名字
	level = nil,				-- 角色等级
    exp = nil,                  -- 当前经验值
    reqExp = nil,               -- 下一级经验值
    fightScore = nil,           -- 当前战力
	camp = nil,					-- 阵营
	configId = nil,				-- 配置ID
	scale = nil,				-- 缩放
	position = nil,				-- 位置
	rotation = nil,				-- 朝向
	awake = nil,				-- 激活条件
	talk = nil,					-- 说话
	reviveTime = nil,			-- 复活时间
	fightArea = nil,			-- 攻击半径
	hitsize = nil,				-- 身体半径
	movespeed = nil,			-- 移动速度
	deityWeaponId = nil,		-- 神兵
	outShowInfo=nil,            --外显信息
}

createClass(FighterVo)

local tempTable = nil

local formatExtValueFuncs = 
{
	['stageMonsterId'] = tonumber,
	['monsterSpawnId'] = tonumber,
	['delay'] = tonumber,
	['monsterType'] = tonumber,
	['isRobot'] = tonumber,
	['isAuto'] = tonumber,
	['masterUId'] = nil,		-- nil 为不作处理，即为字符串
	['defaultbuff'] = nil,
}

function FighterVo:read(conn)
	self.characterType = conn:ReadSbyte()
	self.uniqueID = conn:ReadString()
	self.name = conn:ReadString()
	self.level = conn:ReadShort()
    self.exp = conn:ReadInt()
    self.reqExp = conn:ReadInt()
    self.fightScore = conn:ReadInt()
	self.camp = conn:ReadSbyte()
	
	self.configId = conn:ReadInt()
	self.scale = conn:ReadShort()
	local posStr = conn:ReadString()
	if posStr ~= "" then
		self.position = Vector3.NewByStr(posStr)
		self.position:Mul(0.1)
	
	end
	self.rotation = conn:ReadShort()
	self.awake = conn:ReadString()
	self.talk = conn:ReadString()
	self.reviveTime = conn:ReadShort()
	self.fightarea = conn:ReadShort() * 0.1
	self.hitsize = conn:ReadShort() * 0.1
	self.movespeed = conn:ReadShort() * 0.1
	self.deityWeaponId = conn:ReadInt()
	-- 属性
	local attr = Attribute:create()
    attr:read(conn)
    tableplus.copyParamValue(attr, self)
    -- 技能
    local size = conn:ReadSbyte()
    self.allskill = {}
    local skillid = nil
	self.tempSkillIndex = {}
	self.recvSkillOrder = {}
    if size > 0 then
    	for i = 1, size do
    		skillid = conn:ReadInt()
			table.insert(self.recvSkillOrder, skillid)
			self.tempSkillIndex[skillid] = i - 1
    		self.allskill[skillid] = { level=1, damage=0 }
    		self.allskill[skillid]["level"] = conn:ReadShort()
    		self.allskill[skillid]["damage"] = conn:ReadInt()
            self.allskill[skillid]["skillAttr"] = conn:ReadString()
            if self.allskill[skillid]["skillAttr"] ~= "0" then
                tempTable = StringUtils.split( self.allskill[skillid]["skillAttr"], "+", nil, tonumber ) 
                self.allskill[skillid]["skillAttr"] = {["trumpRate"]=tempTable[1], ["attrIndex"]=tempTable[2]+1-ATTR_COUNT}
            else
                self.allskill[skillid]["skillAttr"] = nil
            end
    	end
    end

    local stageType = FightModel:getFightStageType()
    if stageType == ConstantData.STAGE_TYPE_CAMP_DAILY then
        -- 阵营日常技能替换
        self.allskill = CampDailyModel.getCampDailyAllSkill()
--[[
        local campSkillConfig = CampDailyModel.getCampJobSkill()
        if campSkillConfig ~= nil then
            for i=1,#campSkillConfig do
                for j=1,#campSkillConfig[i].skillGroup do
                    local skillid = campSkillConfig[i].skillGroup[j]
                    self.allskill[skillid] = { level=j, damage=0, type = campSkillConfig[i].type,skillAttr = nil }
                end
            end            
        end
        ]]
    end

    -- 掉落
    size = conn:ReadSbyte()
    self.drop = {}
    local itemid = nil
    if size > 0 then
    	for i = 1, size do
    		self.drop[i]           = { itemId=0,count=0 }
    		self.drop[i]["itemId"] = conn:ReadInt()
    		self.drop[i]["count"]  = conn:ReadShort()
    	end
    end

    -- 等级数据
    size = conn:ReadSbyte()
    self.levelData = {}
    if size > 0 then
        for i = 1, size do
            self.levelData[i] = { level=0, reqExp=0, fightScoreUp=0 }
            self.levelData[i]["level"] = conn:ReadShort()
            self.levelData[i]["reqExp"] = conn:ReadInt()
            self.levelData[i]["fightScoreUp"] = conn:ReadInt()
        end
        table.sort( self.levelData, 
                    function(a,b) 
                        return a.level < b.level      
                    end )
--        --print("升级数据"..tableplus.formatstring(self.levelData,true))
    end

    -- 拓展内容
    self.extraValue = conn:ReadString()

    size = conn:ReadSbyte()
    if size>0 then
    	self.outShowInfo={}
    	for i=1,size do
    		local stroutShow = conn:ReadString()
            local items=StringUtils.split(stroutShow , "+" , nil, tonumber)
            self.outShowInfo[i]={id=items[2],param=items[1],visable=true}
    	end
    end
end

local function parseDefaultBuff(defaultbuff)
	if defaultbuff and defaultbuff ~= "" then
		local buffArr = StringUtils.split(defaultbuff, "|")
		local result = {}
		local tempArr = nil
		for _,buffStr in pairs(buffArr) do
			tempArr = StringUtils.split(buffStr, "+", nil, tonumber)
			table.insert(result, {buffid = tempArr[1], bufflv = tempArr[2], instanceid = tempArr[3]})
		end
		return result
	end
	return nil
end

function FighterVo:parseData()
	-- body
	self.isRobot = nil
	self.isAuto = nil
	if self.characterType == CharacterConstant.TYPE_ROBOT then
		self.isRobot = true
		self.characterType = CharacterConstant.TYPE_PLAYER
	end
	if self.characterType == CharacterConstant.TYPE_MONSTER then
		self:parseMonster()
	elseif self.characterType == CharacterConstant.TYPE_PLAYER then
		self:parsePlayer()
	elseif self.characterType == CharacterConstant.TYPE_PARTNER then
		self:parseParnet()
		
	elseif self.characterType == CharacterConstant.TYPE_SELF then
		self:parseMain()
	end
	self.defaultbuff = parseDefaultBuff(self.defaultbuff)
end

-- 怪物
function FighterVo:parseMonster()
	-- body
	local monsterVo = FightModel:getMonsterVo(self.configId)
	for key,val in pairs(monsterVo) do
		self[key] = val
	end
	-- 解析拓展内容
	--{ stageMonsterId monsterSpawnId delay monsterType defaultbuff }
	if self.extraValue ~= "" then
		local temp = StringUtils.split(self.extraValue,";")
		local attr = nil
		for index = 1, #temp do
			attr = StringUtils.split(temp[index],"=")
			if formatExtValueFuncs[attr[1]] then
				self[attr[1]] = formatExtValueFuncs[attr[1]](attr[2])
			else
				self[attr[1]] = attr[2]
			end
		end
	end
end

-- 玩家
function FighterVo:parsePlayer()
	-- body
	if self.extraValue ~= "" then
		--{ isRobot defaultbuff isAuto }
		local temp = StringUtils.split(self.extraValue,";")
		local attr = nil
		for index = 1, #temp do
			attr = StringUtils.split(temp[index],"=")
			if formatExtValueFuncs[attr[1]] then
				self[attr[1]] = formatExtValueFuncs[attr[1]](attr[2])
			else
				self[attr[1]] = attr[2]
			end
		end
		self.isRobot = (self.isRobot == 1) and true or false
		self.isAuto = (self.isAuto == 1) and true or false --暂时只有家族战精英战场会使用
	end
	local resourceinfo = CFG.resource:get(self.configId)
	if resourceinfo == nil then
		EnvironmentHandler.sendLogToServer("resource data is null ===  "..tostring(self.configId))
	end
	tableplus.copyParamValue(resourceinfo, self)
	self.uiposition = self.uiposition * 0.1
	self.movespeed = self.movespeed * 0.1
	if resourceinfo.hitsound ~= "0" then
		self.hitsound = StringUtils.split(resourceinfo.hitsound, "+")
	end
	-- 提取技能
	local temp = StringUtils.split(self.skill,"|")
	self.passSkill = ""
	self.skill = ""
	local skilllvdata = nil
	local skilldata = nil
	local skillIndex = -1
	local stageType = FightModel:getFightStageType()

    if stageType == ConstantData.STAGE_TYPE_CAMP_DAILY then -- 阵营日常被动技能
        if CampDailyModel.getCampDailySkill() and  CampDailyModel.getCampDailyPassiveSkill() then
            --[[
            for key, value in pairs( self.allskill ) do
                if value.type == 3 then
                    self.passSkill = self.passSkill .. key.. "+" .. 1  .."|"   --记录所有被动技能         
                elseif value.type == 1 then
                    self.skill = self.skill.. 1 .."="..key.."=".. value.level .."|"
                elseif value.type == 2 then
                    self.skill = self.skill.. 4 .."="..key.."=".. value.level .."|"
                elseif value.type == 4 then
                    self.skill = self.skill .. 5 .. "=" .. key .."=".. value.level .."|" --阵营日常时,闪避技能类型为
                end  
            end
            ]]
            self.skill = CampDailyModel.getCampDailySkill()
            self.passSkill = CampDailyModel.getCampDailyPassiveSkill()
        end
    else
        for key, value in pairs( self.allskill ) do
        	skilllvdata = FightModel:getSkillLevelData( key, value.level )
	        if self.passSkill~=nil and value.level~=nil and skilllvdata ~= nil and (skilllvdata.skillType == 1 or skilllvdata.skillType == 3) then --被动技能
		        self.passSkill = self.passSkill .. key .. "+" .. value.level .. "|"
	        end
	        skilldata = FightModel:getSkillData( key )
	        if skilldata ~= nil then
		        if skilldata.skilltype == FightDefine.SKILLTYPE_SKILL then
			        if stageType == ConstantData.STAGE_TYPE_DAILY5V5_PVP and self.tempSkillIndex[key] then
				        skillIndex = self.tempSkillIndex[key]
			        else
				        skillIndex = skillIndex + 1
			        end
			        self.skill = self.skill .. skillIndex .. "=" .. key .. "=" .. value.level .. "|"
		        elseif skilldata.skilltype == FightDefine.SKILLTYPE_ULTIMATE then
			        self.skill = self.skill .. 3 .. "=" .. key .. "=" .. value.level .. "|"
		        end
	        end
        end
        self.skill = self.skill .. 4 .. "=" .. temp[4] .. "=1" --闪避技能类型为4
	    temp[2] = StringUtils.split(temp[2], "+", nil, tonumber) --配置里可能拥有的技能
	    self.configSkill = {}
	    for index = 1, #temp[2] do
		    self.configSkill[temp[2][index]] = 1
	    end
	    self.superarmor = 0
	    if FightModel:isOnPVP() then
		    self.superarmor = 1
	    elseif FightModel:getFightStageType() == ConstantData.STAGE_TYPE_CAMP_QICHU then
		    self.superarmor = 50
	    end
    end

	-- 主角数据可能夹杂在此类型中
	if (EnvironmentHandler.isInServer == false) then
		if self.uniqueID == RoleData.roleId then
			self:parseMain()
		end
	end
end

-- 伙伴
function FighterVo:parseParnet()
	-- body
	local monsterinfo = FightModel:getMonsterVo(self.configId)
	if monsterinfo == nil then
		EnvironmentHandler.sendLogToServer("monster data is null ===  "..tostring(self.configId))
	end
	tableplus.copyParamValue(monsterinfo, self)
	-- 解析拓展内容
	--{ masterUId defaultbuff }
	local temp = StringUtils.split(self.extraValue,";")
	local attr = nil
	for index = 1, #temp do
		attr = StringUtils.split(temp[index],"=")
		if formatExtValueFuncs[attr[1]] then
			self[attr[1]] = formatExtValueFuncs[attr[1]](attr[2])
		else
			self[attr[1]] = attr[2]
		end
	end
	self.passSkill = ""
	local skilllvdata = nil
	for key, value in pairs( self.allskill ) do
		skilllvdata = FightModel:getSkillLevelData( key, value.level )
		if skilllvdata ~= nil and (skilllvdata.skillType == 1 or skilllvdata.skillType == 3) then --被动技能
			self.passSkill = self.passSkill .. key .. "+" .. value.level .. "|"
		end
	end
	if FightModel:isOnPVP() then
		self.superarmor = 1
	elseif FightModel:getFightStageType() == ConstantData.STAGE_TYPE_CAMP_QICHU then
		self.superarmor = 50
	end
end

-- 主角
function FighterVo:parseMain()
	-- body
	local resourceinfo = CFG.resource:get(self.configId)
	local updateValue = {}
	updateValue.camp = self.camp
	updateValue.allskill = self.allskill
	if resourceinfo.hitsound ~= "0" then
		self.hitsound = StringUtils.split(resourceinfo.hitsound, "+")
		updateValue.hitsound = self.hitsound
	end
	for index=1,#AttrEnum do
		updateValue[AttrEnum[index]] = self[AttrEnum[index]]
	end
    updateValue.levelData = self.levelData
	updateValue.superarmor = 0
	if FightModel:isOnPVP() then
		updateValue.superarmor = 1
	elseif FightModel:getFightStageType() == ConstantData.STAGE_TYPE_CAMP_QICHU then
		updateValue.superarmor = 50
	end
    updateValue.isAttribChanged = true
	ModuleEvent.dispatch(RoleProtocol.R_0x0010, updateValue)
end

function FighterVo:write(conn)
	conn:WriteSbyte(self.characterType)
	conn:WriteString(self.uniqueID)
	conn:WriteString(self.name)
	conn:WriteShort(self.level)
    conn:WriteInt(self.exp)
    conn:WriteInt(self.reqExp)
    conn:WriteInt(self.fightScore)
	conn:WriteSbyte(self.camp)
	conn:WriteInt(self.configId)
	conn:WriteShort(self.scale)
	conn:WriteString(self.position.x*10 .. "+" .. self.position.y*10 .. "+" .. self.position.z*10)
	conn:WriteShort(self.rotation)
	conn:WriteString(self.awake or "")
	conn:WriteString(self.talk or "")
	conn:WriteShort(self.reviveTime)
	conn:WriteShort(self.fightarea * 10)
	conn:WriteShort(self.hitsize * 10)
	conn:WriteShort(self.movespeed * 10)
	conn:WriteInt(self.deityWeaponId)

	local count = 0
	for index = 1, ATTR_COUNT do
		if self[AttrEnum[index]] and self[AttrEnum[index]] ~= 0 then
			count = count + 1
		end
	end
	for index = 1, EXTATTR_COUNT do
		if self[EXT_ATTR[index]] and self[EXT_ATTR[index]] ~= 0 then
			count = count + 1
		end
	end
	conn:WriteSbyte(count)
	if count > 0 then
		for index = 1, ATTR_COUNT do
			if self[AttrEnum[index]] and self[AttrEnum[index]] ~= 0 then
				conn:WriteShort(index-1)
				conn:WriteInt(self[AttrEnum[index]])
			end
		end
		for index = 1, EXTATTR_COUNT do
			if self[EXT_ATTR[index]] and self[EXT_ATTR[index]] ~= 0 then
				conn:WriteShort(index-1+ATTR_COUNT)
				conn:WriteInt(self[EXT_ATTR[index]])
			end
		end
	end

	local skillSize = 0
    local stageType = FightModel:getFightStageType()
    if stageType == ConstantData.STAGE_TYPE_CAMP_DAILY then
	    conn:WriteSbyte(skillSize)
    else
	    for k,v in pairs(self.allskill) do
		    skillSize = skillSize + 1
	    end
	    conn:WriteSbyte(skillSize)
	    for k,skillid in pairs(self.recvSkillOrder) do
		    local skill = self.allskill[skillid]
		    conn:WriteInt(skillid)
		    conn:WriteShort(skill.level)
		    -- 客户端不计算伤害 无需使用
		    conn:WriteInt(0)
		    conn:WriteString("0")
	    end
    end

	conn:WriteSbyte(0) --不发送掉落
	conn:WriteSbyte(0) --不发送等级数据
	conn:WriteString(self.extraValue or "")
	if self.outShowInfo == nil or #self.outShowInfo == 0 then
		conn:WriteSbyte(0) --没有外显龙珠
	else
		local size = #self.outShowInfo
		local value = nil
		conn:WriteSbyte(size)
		for i = 1, size do
			if self.outShowInfo[i] then
				value = ""..self.outShowInfo[i].param.."+"..self.outShowInfo[i].id
				conn:WriteString(value)
			end
		end
	end
end