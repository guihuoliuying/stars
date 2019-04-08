-- region
-- Date    : 2016-07-02
-- Author  : daiyaorong
-- Description :  技能行为条件节点
-- endregion

SkillCondition = {
	compareFunc = nil,
	paramFunc1 = nil,
	paramFunc2 = nil,
	superMark = false,
}
SkillCondition = createClass(SkillCondition)

local string_match = string.match
local math_ceil = math.ceil
local math_tonumber = tonumber
local ARGUMENT_MYSELF = "myself"
local ARGUMENT_TARGET = "target"
local ARGUMENT_MASTER = "master"

-- 血量函数
local function convertHppercent( uid, param )
	-- body
	local temp, argument = string_match( param, FightDefine.AI_MATCH.hppercent )
	local character = nil
    local aiCharacter = CharacterManager:getCharacByUId( uid )

	local function execute( targetID )
		-- body
        character = nil
        if argument == ARGUMENT_MYSELF then
            character = aiCharacter
        elseif argument == ARGUMENT_TARGET and targetID then
            character = CharacterManager:getCharacByUId( targetID )
        elseif argument == ARGUMENT_MASTER then
        	if aiCharacter and aiCharacter.characterType == CharacterConstant.TYPE_PARTNER then
        		character = CharacterManager:getCharacByUId( aiCharacter.masterUId )
    		end
        end
		if character ~= nil then
			return math_ceil( (character.hp/character.maxhp) * 100 )
		else
			return 0
		end
	end
	return execute
end

-- 随机函数
local function converRandom( uid, param )
	-- body
	local temp1, argument1, temp2, argument2 = string_match( param, FightDefine.AI_MATCH.random )
	argument1 = math_tonumber(argument1) - 1 --因为要取到下边界 因此作减一处理
	argument2 = math_tonumber(argument2)
	local function execute()
		-- body
		return math_ceil( math.LogicRandom(argument1, argument2) )
	end
	return execute
end

-- 友军数量
local function converAllynumber( uid, param )
    local character = CharacterManager:getCharacByUId( uid )
    local allyList = nil
    local allyCount = 0
    local function execute()
        allyList = CharacterManager:getCharacByRelation( uid, character.camp, CharacterConstant.RELATION_FRIEND )
        allyCount = 0
        if allyList ~= nil then
            for k,v in pairs(allyList) do
                allyCount = allyCount + 1
            end
        end
        return allyCount
    end
    return execute
end

-- 状态判断
local function converState( uid, param )
    local temp, argument = string_match( param, FightDefine.AI_MATCH.state )
	local character = nil
    local aiCharacter = CharacterManager:getCharacByUId( uid )

	local function execute( targetID )
		-- body
        character = nil
        if argument == ARGUMENT_MYSELF then 
			character = aiCharacter
		elseif argument == ARGUMENT_TARGET and targetID then
            character = CharacterManager:getCharacByUId( targetID )
        elseif argument == ARGUMENT_MASTER then
        	if aiCharacter and aiCharacter.characterType == CharacterConstant.TYPE_PARTNER then
        		character = CharacterManager:getCharacByUId( aiCharacter.masterUId )
    		end
		end
        if character ~= nil then
		    return character.state
        else
            return 0
        end
	end
	return execute
end

-- 是否拥有buff
local function converHavebuff( uid, param )
    local temp1, argument1, temp2, argument2 = string_match( param, FightDefine.AI_MATCH.havebuff )
    argument2 = math_tonumber(argument2)
    local character = nil
    local aiCharacter = CharacterManager:getCharacByUId( uid )

    local function execute( targetID )
        character = nil
        if argument1 == ARGUMENT_MYSELF then 
			character = aiCharacter
		elseif argument1 == ARGUMENT_TARGET and targetID then
            character = CharacterManager:getCharacByUId( targetID )
        elseif argument1 == ARGUMENT_MASTER then
        	if aiCharacter and aiCharacter.characterType == CharacterConstant.TYPE_PARTNER then
        		character = CharacterManager:getCharacByUId( aiCharacter.masterUId )
    		end
		end
        if character ~= nil and character.charaBuff and character.charaBuff:hasBuff(argument2) then
		    return 1
        else
            return 0
        end
    end
    return execute
end

-- 距离判断
local function converDistance( uid, param )
    local temp1, argument1, temp2, argument2 = string_match( param, FightDefine.AI_MATCH.distance )
    local config1 = StringUtils.split(argument1,"*",nil,math_tonumber)
    local configPos1 = Vector3.New( config1[2],0,config1[3] ) * 0.1
    local config2 = StringUtils.split(argument2,"*",nil,math_tonumber)
    local configPos2 = Vector3.New( config2[2],0,config2[3] ) * 0.1
    local pos1 = nil
    local pos2 = nil

    local function execute( targetID )
        if targetID == nil then
            return 0
        else
            pos1 = PathFinder.samplePosition(PointCalculator.getInitPoint( uid, targetID, config1[1], configPos1 ))
            pos2 = PathFinder.samplePosition(PointCalculator.getInitPoint( uid, targetID, config2[1], configPos2 ))
            return CharacterUtil.distance( pos1, pos2 ) * 10
        end
    end
    return execute
end

-- 数值
local function converNumber( uid, param )
	-- body
	param = math_tonumber(param)
	local function execute()
		-- body
		return param
	end
	return execute
end

local conditionFunc = {
	["hppercent"] = convertHppercent,
	["random"] = converRandom,
    ["allynumber"] = converAllynumber,
    ["state"] = converState,
    ["havebuff"] = converHavebuff,
    ["distance"] = converDistance,
	["number"] = converNumber,
}

function SkillCondition:init( uid, data  )
	-- body
	if data == "0" then		--条件为0则必定成功
		self.superMark = true
	else
		local param1, compareParam, param2 = string_match( data, FightDefine.AI_MATCH.compare )
		self.compareFunc = JudgeCompare[compareParam]

		local funcName = string_match( param1, FightDefine.AI_MATCH.func )
		if funcName == nil then
			funcName = "number" --默认为数值类型
		end
		self.paramFunc1 = conditionFunc[funcName]( uid, param1 )

		funcName = string_match( param2, FightDefine.AI_MATCH.func )
		if funcName == nil then
			funcName = "number"
		end
		self.paramFunc2 = conditionFunc[funcName]( uid, param2 )
	end
end

function SkillCondition:execute( targetID )
	-- body
	if self.superMark == true then
		return true
	end
	return( self.compareFunc(self.paramFunc1(targetID), self.paramFunc2(targetID)) )
end

function SkillCondition:dispose()
	-- body
	self.paramFunc1 = nil
	self.paramFunc2 = nil
end