--region BuffVo.lua
--Date 2016/07/14
--Author zhouxiaogang
-- BUFF产品数据
--endregion

BuffVo = 
{
	buffKey = nil,				-- 唯一ID,用buffID_bufLv组成，客户端用
	
	buffId = nil,				-- 这个id会有重复
	buffLv = nil,				-- 等级
	duringTime = nil,			-- 持续时间 -1为无限
	buffType = nil,				-- buff类型 增益/减益
	buffInfo = nil,				-- buff效果列表
	maxLayer = nil,				-- 最大叠加层数
	buffEffect = nil,			-- 显示特效
	damageNumberType = nil,		-- 伤害数字类型
}

createClass(BuffVo)

local function AttribHandler(effect, paramArr)
	effect.attribName = paramArr[1]
	effect.rate = tonumber(paramArr[2]) or 0
	effect.value = tonumber(paramArr[3]) or 0
end

local function PoisonCureHandler(effect, paramArr)
	effect.rate = tonumber(paramArr[1]) or 0
	effect.value = tonumber(paramArr[2]) or 0
	effect.interval = tonumber(paramArr[3]) or 0
end

local function CtrlHandler(effect, paramArr)
	effect.controls = {}
	for k,v in ipairs(paramArr) do
		table.insert(effect.controls, tonumber(v))
	end
end

--buffer使用技能的buffinfo
local function initBuffInfo(skillVo,effect)
	if skillVo.buffinfo == "0" or skillVo.buffinfo == "" then
		effect.childbuffInfo = nil
		effect.buffIdList = nil
	else
		tempTableA = StringUtils.split(skillVo.buffinfo, ",")
		if #tempTableA ~= 1 then 
			LogManager.Log("buffInfo字段错误:id:"..skillVo.skillid)
			return
		end
		effect.childbuffInfo = {}
		effect.buffIdList = {}
		local arr = nil
		local buffArr = nil
		local tempTb = nil
		for k,v in ipairs(tempTableA) do
			arr = StringUtils.split(v, "|")
			tempTb = {}
			for _,buffStr in ipairs(arr) do
				-- LogManager.Log("buffStr:"..buffStr)
				buffArr = StringUtils.split(buffStr, "+", nil, tonumber)
				tempTb[#tempTb+1] = {buffId = buffArr[1], target = buffArr[2]}
				table.insert( effect.buffIdList, buffArr[1] )
			end
			effect.childbuffInfo[k] = tempTb
		end
	end
end

local function DamageHandler(effect, paramArr)
	effect.radius = tonumber(paramArr[1]) or 0
	effect.rate = tonumber(paramArr[2]) or 0
	effect.value = tonumber(paramArr[3]) or 0
	effect.interval = tonumber(paramArr[4]) or 0
	effect.showSkillID = 0
	if paramArr[5] then
		effect.showSkillID = tonumber(paramArr[5]) or 0
	end
	-- LogManager.Log("effectSkillid"..tostring(effect.showSkillID))
	local effectSkill=CFG.skill.configs[effect.showSkillID]
	if effectSkill then
        -- HitEffect(effectSkill,effect)
        -- specialEffect(effectSkill,effect)
        initBuffInfo(effectSkill,effect)
	end

end

local function BeatbackHandler(effect, paramArr)
	effect.rate = tonumber(paramArr[1]) or 0
	effect.value = tonumber(paramArr[2]) or 0
end

local function TauntHandler(effect, paramArr)
	-- body
end

local function PhantomHandler(effect, paramArr)
	effect.rate = tonumber(paramArr[1]) or 0
end

local function ShieldHandler(effect, paramArr)
	effect.rate = tonumber(paramArr[1]) or 0
	effect.value = tonumber(paramArr[2]) or 0
end

local function PoisonBombHandler(effect, paramArr)
	effect.rate = tonumber(paramArr[1]) or 0
end

local function SuperarmorHandler(effect, paramArr)
	effect.rate = tonumber(paramArr[1]) or 0
	effect.value = tonumber(paramArr[2]) or 0
end

local function HpDownHandler(effect, paramArr)
	effect.attribName = tostring(paramArr[1])
	effect.percent = tonumber(paramArr[2]) or 1000
	effect.rate = tonumber(paramArr[3]) or 0
	effect.value = tonumber(paramArr[4]) or 0
	effect.interval = tonumber(paramArr[5]) or 0
	effect.limit = tonumber(paramArr[6]) or 0
end

local function InvincibleHandler(effect, paramArr)
	-- body
end

local function DamageIntensifyHandler(effect, paramArr)
	effect.rate = tonumber(paramArr[1]) or 0
	effect.value = tonumber(paramArr[2]) or 0
end

local function DamageAddHandler(effect, paramArr)
	effect.rate = tonumber(paramArr[1]) or 0
	effect.value = tonumber(paramArr[2]) or 0
end

local function BuffCleanHandler(effect, paramArr)
	effect.cleanTypes = {}
	for k,v in pairs(paramArr) do
		table.insert(effect.cleanTypes, tonumber(v))
	end
end

local function HpPercentHandler(effect, paramArr)
	effect.targetType = tonumber(paramArr[1]) or 0
	effect.rate = tonumber(paramArr[2]) or 0
	effect.interval = tonumber(paramArr[3]) or 0
end

local function BuffDurationAddHandler(effect, paramArr)
	effect.rate = (tonumber(paramArr[1]) or 0) * 0.001
	effect.value = (tonumber(paramArr[2]) or 0) * 0.001
end

local function BuffGetHpHandler(effect, paramArr)
	-- body
	effect.rate = (tonumber(paramArr[1]) or 0) * 0.001
	effect.value = (tonumber(paramArr[2]) or 0)
end

local function BuffGetDemageHandler(effect, paramArr)
	-- body
	effect.rate = (tonumber(paramArr[1]) or 0) * 0.001
end

local handlerList = {
	[FightDefine.BUFF_EFFECT_TYPE.ATTRIB] = AttribHandler,
	[FightDefine.BUFF_EFFECT_TYPE.POISONING] = PoisonCureHandler,
	[FightDefine.BUFF_EFFECT_TYPE.CURE] = PoisonCureHandler,
	[FightDefine.BUFF_EFFECT_TYPE.CTRL] = CtrlHandler,
	[FightDefine.BUFF_EFFECT_TYPE.DAMAGE] = DamageHandler,
	[FightDefine.BUFF_EFFECT_TYPE.BEATBACK] = BeatbackHandler,
	[FightDefine.BUFF_EFFECT_TYPE.TAUNT] = TauntHandler,
	[FightDefine.BUFF_EFFECT_TYPE.PHANTOM] = PhantomHandler,
	[FightDefine.BUFF_EFFECT_TYPE.SHIELD] = ShieldHandler,
	[FightDefine.BUFF_EFFECT_TYPE.POISONBOMB] = PoisonBombHandler,
	[FightDefine.BUFF_EFFECT_TYPE.SUPERARMOR] = SuperarmorHandler,
	[FightDefine.BUFF_EFFECT_TYPE.HPDOWN] = HpDownHandler,
	[FightDefine.BUFF_EFFECT_TYPE.INVINCIBLE] = InvincibleHandler,
	[FightDefine.BUFF_EFFECT_TYPE.DAMAGE_INTENSIFY] = DamageIntensifyHandler,
	[FightDefine.BUFF_EFFECT_TYPE.DAMAGE_ADD] = DamageAddHandler,
	[FightDefine.BUFF_EFFECT_TYPE.BUFF_CLEAN] = BuffCleanHandler,
	[FightDefine.BUFF_EFFECT_TYPE.HP_PERCENT] = HpPercentHandler,
	[FightDefine.BUFF_EFFECT_TYPE.BUFF_DURATION_ADD] = BuffDurationAddHandler,
	[FightDefine.BUFF_EFFECT_TYPE.BUFF_GETHP] = BuffGetHpHandler,
	[FightDefine.BUFF_EFFECT_TYPE.BUFF_GETDEMAGEHP] = BuffGetDemageHandler,
	[FightDefine.BUFF_EFFECT_TYPE.BUFF_NEWPOISONING] = PoisonCureHandler,
}

-- 解析BUFF效果列表
-- infoStr "类型|参数1+参数2+...+参数n,类型|参数..."
local function parseBuffInfo(infoStr)
	if infoStr == nil then return nil end
	local effArr = StringUtils.split(infoStr, ",")
	local result = {}
	for _,effStr in ipairs(effArr) do
		local tempArr = StringUtils.split(effStr, "|")
		local effect = {}
		effect.effectType = tonumber(tempArr[1])
		local paramArr = StringUtils.split(tempArr[2], "+")
		if effect.effectType then
			handlerList[effect.effectType](effect, paramArr)
		end
		table.insert(result, effect)
	end
	return result
end

-- 解析BUFF显示特效
local function parseBuffEffect(effectStr)
	if effectStr == nil or effectStr == "0" then
		return nil
	end
	local effectTable = {}
	local effects = StringUtils.split(effectStr, ",")
	for k,v in pairs (effects) do
        local result = {}
        local arr = StringUtils.split(v, "+")
        result.name = arr[1]
        result.bone = arr[2]
        local x = tonumber(arr[3]) or 0
        local y = tonumber(arr[4]) or 0
        local z = tonumber(arr[5]) or 0
        result.offset = Vector3.New(x * 0.1, y * 0.1, z * 0.1)
        table.insert(effectTable,result)
	end
	return effectTable
end

function BuffVo:read(conn)
	self.buffId = conn:ReadInt()
	self.buffLv = conn:ReadInt()
	self.duringTime = conn:ReadInt()
	self.buffType = conn:ReadSbyte()
	self.buffInfo = conn:ReadString()
	self.maxLayer = conn:ReadSbyte()
	self.buffEffect = conn:ReadString()
	self.damageNumberType = conn:ReadString()
	if EnvironmentHandler.isInServer then
		self.rawBuffObj = tableplus.shallowcopy(self)
	end
	self.damageNumberType = tonumber(self.damageNumberType) or 1
	self.buffInfo = parseBuffInfo(self.buffInfo)
	self.buffEffect = parseBuffEffect(self.buffEffect)
	self.buffKey = self.buffId .. "_" .. self.buffLv
end

function BuffVo:write(conn)
	local rawObj = self.rawBuffObj
	conn:WriteInt(rawObj.buffId)
	conn:WriteInt(rawObj.buffLv)
	conn:WriteInt(rawObj.duringTime)
	conn:WriteSbyte(rawObj.buffType)
	conn:WriteString(rawObj.buffInfo)
	conn:WriteSbyte(rawObj.maxLayer)
	conn:WriteString(rawObj.buffEffect)
	conn:WriteString(rawObj.damageNumberType)
end

BuffVo.__tostring = function(self)
	local str = "BuffVo[" .. self.buffKey .. "]={"
	for k,v in pairs(self) do
		str = str .. k .. "=" .. tostring(v) .. ","
	end
	str = str .. "}"
	return str
end

-- BuffVo = {}

function BuffVo.parse( config )
	-- body
	config.buffId = config.buffid
	config.buffLv = config.bufflv
	config.duringTime = config.duringtime
	config.buffType = config.bufftype
	config.maxLayer = config.maxlayer
	config.damageNumberType = tonumber(config.damagenumbertype) or 1
	config.buffInfo = parseBuffInfo(config.buffinfo)
	config.buffEffect = parseBuffEffect(config.buffeffect)
	config.buffKey = config.buffId .. "_" .. config.buffLv
end

function BuffVo.doParseBuffInfo(buffinfo)
	return parseBuffInfo(buffinfo)
end