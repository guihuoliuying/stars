--region MonsterVo.lua
--Date 2016/07/04
--Author zhouxiaogang
-- 怪物数据
--endregion

MonsterVo = 
{
	monsterid = nil,		-- 怪物id
	skillorder = nil,		-- 技能顺序
	superarmor = nil,		-- 霸体等级
	searchradii = nil,		-- 警戒范围
	findenemyai = nil,		-- 寻敌ai
	pathai = nil,			-- 寻路ai
	attackinterval = nil,	-- 攻击间隔
	stopTime = nil,			-- 怪物ai暂停时间
    istrap = nil,           -- 是否陷阱
    isinvincible = nil,       -- 是否无敌
    monsdynablock = nil,    -- 动态阻挡
    passSkill = nil,		-- 被动技能
}

createClass(MonsterVo)

function MonsterVo:read(conn)
	self.monsterid = conn:ReadInt()
	self.skillorder = conn:ReadString()
	self.superarmor = conn:ReadSbyte()
	self.searchradii = conn:ReadInt()
	self.findenemyai = conn:ReadString()
	self.pathai = conn:ReadString()
	self.attackinterval = conn:ReadInt()
	self.stopTime = conn:ReadShort()
	self.istrap = conn:ReadSbyte()
	self.isinvincible = conn:ReadSbyte()
	self.monsdynablock = conn:ReadString()
	self.passSkill = conn:ReadString()
	if EnvironmentHandler.isInServer then
		self.rawMonsterObj = tableplus.shallowcopy(self)
	end
	self:parseData()
end

function MonsterVo:write(conn)
	local rawObj = self.rawMonsterObj
	conn:WriteInt(rawObj.monsterid)
	conn:WriteString(rawObj.skillorder)
	conn:WriteSbyte(rawObj.superarmor)
	conn:WriteInt(rawObj.searchradii)
	conn:WriteString(rawObj.findenemyai)
	conn:WriteString(rawObj.pathai)
	conn:WriteInt(rawObj.attackinterval)
	conn:WriteShort(rawObj.stopTime)
	conn:WriteSbyte(rawObj.istrap)
	conn:WriteSbyte(rawObj.isinvincible)
	conn:WriteString(rawObj.monsdynablock)
	conn:WriteString(rawObj.passSkill)
end

function MonsterVo:parseData()
	-- body
	local info = CFG.monsterinfo:get(self.monsterid)
	if info == nil then
		EnvironmentHandler.sendLogToServer("monstervo data is null ===  "..tostring(self.monsterid))
	end
	tableplus.copyParamValue(info, self)
	self.downspeed = self.downspeed * 0.1
	self.uiposition = self.uiposition * 0.1
	if self.searchradii then
		self.searchradii = self.searchradii * 0.1
	end
	self.monsterType = self.type

	if info.hitsound ~= "0" then
        self.hitsound = StringUtils.split(info.hitsound, "+")
    else
        self.hitsound = nil
    end
    if info.deathsound ~= "0" then
        self.deathsound = StringUtils.split(info.deathsound, "+")
    else
        self.deathsound = nil
    end
	if self.deadeffect == "0" then
		self.deadeffect = nil
	else
		self.deadeffect = StringUtils.split(self.deadeffect, ",")
		for k,v in ipairs( self.deadeffect ) do
			v = StringUtils.split(v, "+")
			v[3] = tonumber(v[3]) * 0.001 * ConstantData.FRAME_RATE
			self.deadeffect[k] = v
		end
	end
	if self.findenemyai then
		self.findenemyai = StringUtils.split(self.findenemyai, "+", nil, tonumber)
	end
	if self.pathai then
		self.pathai = StringUtils.split(self.pathai, "|", nil, tonumber)
	end

	if self.passSkill == "" or self.passSkill == "0" then
		self.passSkill = nil
	end

	self.skillIdList = {}
	if self.awakeskill ~= 0 then
		table.insert(self.skillIdList, self.awakeskill)
	end
	if self.bornskill ~= 0 then
		table.insert(self.skillIdList, self.bornskill)
	end
	if self.skill and self.skill ~= "" and self.skill ~= "0" then --攻击技能
		local skillList = StringUtils.split(self.skill,"|")
		local skillids = nil
		for _, skills in ipairs(skillList) do
			skillids = StringUtils.split(skills,"+",nil,tonumber)
			for _, skillid in ipairs(skillids) do
				if skillid ~= 0 then
					table.insert(self.skillIdList, skillid)
				end
			end
		end
	end
	if self.passSkill then
		local skillList = StringUtils.split(self.passSkill,"+",nil,tonumber)
		for _, skillid in ipairs(skillList) do
			if skillid ~= 0 then
				table.insert(self.skillIdList, skillid)
			end
		end
		self.passSkill = ""
		for k,v in pairs(skillList) do
			if v ~= 0 then
				self.passSkill = self.passSkill .. v .. "+1|" --默认1级
			end
		end
	end
	if self.borneffect == nil or self.borneffect=="" or self.borneffect=="0" then
		self.bornEffList=nil
	else
		self.bornEffList={}
		local effectList=StringUtils.split(self.borneffect,",")
		for i=1,#effectList do
			local bornEffNode={}
			local nodeList=StringUtils.split(effectList[i],"+")
			bornEffNode.effName=nodeList[1]
			bornEffNode.hangPoint=nodeList[2]
			bornEffNode.delayTime=tonumber(nodeList[3])
			self.bornEffList[i]=bornEffNode
		end
	end

	if self.skillorder ~= nil then
		self.skillorderTable = StringUtils.split( self.skillorder, "|" )
		self.skillorderTable[1] = tonumber(self.skillorderTable[1]) --orderType
		if self.skillorderTable[1] ~= FightDefine.MONSTERORDER_AI then
			self.skillorderTable[2] = StringUtils.split( self.skillorderTable[2], "+", nil, tonumber )
		else
			for i = 2, #self.skillorderTable do
				self.skillorderTable[i] = StringUtils.split( self.skillorderTable[i], "+" )
			end
		end
	end

	if self.skill ~= nil and self.skill ~= "" then
		self.skillTable = StringUtils.split( self.skill, "|" )
		self.skillTable[1] = StringUtils.split( self.skillTable[1], "+", nil, tonumber )
		self.skillTable[2] = StringUtils.split( self.skillTable[2], "+", nil, tonumber )
		self.skillTable[3] = tonumber(self.skillTable[3])
	    self.skillTable[4] = tonumber(self.skillTable[4])
	end
end

MonsterVo.__tostring = function(self)
	local str = "MonsterVo:["
	for key,val in pairs(self) do
		str = str .. tostring(key) .. "=" .. tostring(val) .. ", "
	end
	str = str .. "]"
	return str
end