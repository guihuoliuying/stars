-- region
-- Date    : 2016-08-02
-- Author  : daiyaorong
-- Description :  技能等级数据
-- endregion

SkillLevelVo = {
	skillLevelKey = nil,	-- 唯一ID，客户端用
	skillid = nil,		--技能ID
	level = nil,			--技能等级
	condition = nil,		--触发条件
	effectinfo = nil,		--技能效果
	coefficient = nil,		--伤害效果
	damage = nil,			--伤害值
	cooldown = nil,			--冷却时间
	skillType = nil,		--技能类型
	reqlv = nil,			--所需等级
}

createClass(SkillLevelVo)

function SkillLevelVo:read( conn )
	-- body
	self.skillid = conn:ReadInt()
	self.level = conn:ReadInt()
	self.condition = conn:ReadString()
	self.effectinfo = conn:ReadString()
	self.coefficient = conn:ReadInt()
	self.damage = conn:ReadInt()
	self.cooldown = conn:ReadInt()
	self.skillType = conn:ReadSbyte()
	self.reqlv = conn:ReadInt()
	
	if EnvironmentHandler.isInServer then
		self.rawSkillLevelObj = tableplus.shallowcopy(self)
	end
	self.skillLevelKey = self.skillid .. "_" .. self.level
	self.cooldown = self.cooldown * 0.001 * ConstantData.FRAME_RATE
	self.coefficient = self.coefficient * 0.001
	self:init()
	-- --print("打印"..tableplus.formatstring(self,true))
end

function SkillLevelVo:write(conn)
	local rawObj = self.rawSkillLevelObj
	conn:WriteInt(rawObj.skillid)
	conn:WriteInt(rawObj.level)
	conn:WriteString(rawObj.condition)
	conn:WriteString(rawObj.effectinfo)
	conn:WriteInt(rawObj.coefficient)
	conn:WriteInt(rawObj.damage)
	conn:WriteInt(rawObj.cooldown)
	conn:WriteSbyte(rawObj.skillType)
	conn:WriteInt(rawObj.reqlv)
end


local tempTable = nil

local function initEffectInfo( skilllevelvo )
	-- body
	if skilllevelvo.effectinfo == "0" then
		skilllevelvo.effectinfo = nil
	else
		tempTable = StringUtils.split(skilllevelvo.effectinfo,"|")
		skilllevelvo.effectinfo = {}
		skilllevelvo.effectinfo["type"] = tonumber(tempTable[1])
		if skilllevelvo.effectinfo["type"] == FightDefine.PASSEFF_ATTRIBUTE  then --属性型
			skilllevelvo.effectinfo["param"] = StringUtils.split(tempTable[2],"+")
		elseif skilllevelvo.effectinfo["type"] == FightDefine.PASSEFF_BUFF or skilllevelvo.effectinfo["type"] == FightDefine.PASSEFF_DemagePercent then --buff型
			skilllevelvo.effectinfo["param"] = StringUtils.split(tempTable[2],"+",nil,tonumber)
		end
		tempTable = nil
	end
end

local function initCondition( skilllevelvo )
	-- body
    if skilllevelvo.condition ~= "0" then --格式是A|B+C或者A|B+C|C+D+E+..
    	skilllevelvo.condition = StringUtils.split(skilllevelvo.condition,"|")
        skilllevelvo.condition[1] = tonumber(skilllevelvo.condition[1]) --触发类型
        local temp = StringUtils.split(skilllevelvo.condition[2],"+",nil,tonumber)
        skilllevelvo.condition[2] = temp[1]

	    if skilllevelvo.condition[1] == FightDefine.PASSACTIVE_HP then
		    skilllevelvo.condition[2] = skilllevelvo.condition[2] * 0.001
        elseif skilllevelvo.condition[1] == FightDefine.PASSACTIVE_BEHIT then
            skilllevelvo.condition[3] = temp[2] * 0.001
       	elseif skilllevelvo.condition[1] == FightDefine.PASSACTIVE_HIT then
       		skilllevelvo.condition[2] = temp[2] --概率
       		skilllevelvo.condition[3] = StringUtils.split(skilllevelvo.condition[3],"+",nil,tonumber) --触发的具体id或type，是个table
       		skilllevelvo.condition[4] = temp[1] --类型1：按照技能id；类型2：按照技能type
   		elseif skilllevelvo.condition[1] == FightDefine.PASSACTIVE_SKILL then
   			skilllevelvo.condition[2] = temp[2] --概率
       		skilllevelvo.condition[3] = StringUtils.split(skilllevelvo.condition[3],"+",nil,tonumber) --触发的具体id或type，是个table
       		skilllevelvo.condition[4] = temp[1] --类型1：按照技能id；类型2：按照技能type
        else
            for i=2,#temp do
            	skilllevelvo.condition[i+1]=temp[i]
            end
	    end
    else
        skilllevelvo.condition = nil
    end
end

function SkillLevelVo:init()
	-- body
	initEffectInfo(self)
	initCondition(self)
end

function SkillLevelVo.parse( config )
	-- body
	config.skillType = config.skilltype
	config.condition = config.conditions
	config.skillLevelKey = config.skillid .. "_" .. config.level
	config.cdSecond = config.cooldown * 0.001
	config.cooldown = config.cooldown * 0.001 * ConstantData.FRAME_RATE
	config.coefficient = config.coefficient * 0.001
	initEffectInfo(config)
	initCondition(config)
end