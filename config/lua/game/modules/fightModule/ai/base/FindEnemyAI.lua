--------------------------------------------------------------
-- region FindEnemyAI.lua
-- Date : 2016-7-6
-- Author : jjm
-- Description : 怪物索敌类
-- endregion
---------------------------------------------------------------

FindEnemyAI = {}

--[[--
是否能判断该目标（如果目标已经死亡或者无敌则不需要判断该目标）
]]
local function canCheckThis(character)
    if character.camp == CharacterConstant.CAMP_TYPE_NEUTRAL then
        return false
    end
    if character.state == CharacterConstant.STATE_DEAD or 
		character.hp == 0 or character.invincible == nil or
		character.invincible == 1 or
		character.characterType == CharacterConstant.TYPE_PARTNER or 
		(character.istrap and character.istrap == 1) then
        return false
    end
    return true
end

--[[--
获取距离最近的目标
@monster 怪物自身
@characterList 目标列表
@return target
{
    uid 对象id
    character  实体对象
}
]]
local function getNearestTarget(monster,characterList, listLen)
    local pos = monster:getPosition()
    local targetList
    if characterList and listLen > 0 then
        targetList = characterList
    else
        targetList = CharacterManager:getCharacByRelation( monster.uniqueID, monster.camp, CharacterConstant.RELATION_ENEMY )
    end
    local distance = nil
    local target = {}
	listLen = 0
    if targetList then
        for uid,character in pairs (targetList) do
            if canCheckThis(character) then
                local dis = CharacterUtil.sqrDistanceWithoutY(pos,character:getPosition())
                if not distance then
                    distance = dis
                    target[uid] = character
					listLen = listLen + 1
                elseif distance > dis then 
                    target = {}
                    distance = dis
                    target[uid] = character
					listLen = 1
                elseif distance == dis then
                    target[uid] = character
					listLen = listLen + 1
                end
            end
        end
    end
    return target, listLen
end

--[[--
获取生命值最低的目标
@monster 怪物自身
@characterList 目标列表
@return target
{
    uid 对象id
    character  实体对象
}
]]
local function getLeastHp(monster,characterList, listLen)
    local targetList
    if characterList and listLen > 0 then
        targetList = characterList
    else
        targetList = CharacterManager:getCharacByRelation( monster.uniqueID, monster.camp, CharacterConstant.RELATION_ENEMY )
    end
    local hp = nil
    local target = {}
	listLen = 0
    if targetList then
        for uid,character in pairs (targetList) do
            if canCheckThis(character) then
                local temp = character.hp
                if not hp then
                    hp = temp
                    target[uid] = character
					listLen = listLen + 1
                elseif hp > temp then 
                    target = {}
                    hp = temp
                    target[uid] = character
					listLen = 1
                elseif hp == temp then
                    target[uid] = character
					listLen = listLen + 1
                end
            end
        end
    end
    return target, listLen
end

--[[--
获取仇恨最高的目标
@monster 怪物自身
@characterList 目标列表
@return target
{
    uid 对象id
    character  实体对象
}
]]
local function getTopHatred(monster,characterList, listLen)
    local target = {}
    if characterList and listLen > 0 then
        local maxHatred = nil
		listLen = 0
        for uid,character in pairs (characterList) do
            if canCheckThis(character) then
                local temp = monster.hatredmap:getHatredbyUid(uid)
                if temp ~= nil then
                    if not maxHatred then
                        maxHatred = temp
                        target[uid] = character
						listLen = listLen + 1
                    elseif maxHatred < temp then
                        target = {}
                        maxHatred = temp
                        target[uid] = character
						listLen = 1
                    elseif maxHatred == temp then
                        target[uid] = character
						listLen = listLen + 1
                    end
                end
            end
        end
    else
        local uidList = monster.hatredmap:getTopHatred()
		listLen = 0
        if uidList ~= nil then
            for k,uid in pairs (uidList) do
                local character = CharacterManager:getCharacByUId(uid)
                target[uid] = character
				listLen = listLen + 1
            end
        end
    end
    --获取之后清空仇恨列表
    monster.hatredmap:clearAllHatred()
    return target, listLen
end

--[[--
获取队伍编号最小的目标
@TODO 待补充
]]
local function getLeastTeamNum(monster,characterList, listLen)
    return characterList, listLen
end

--[[--
获取随机目标
@monster 怪物自身
@characterList 目标列表
@return target
{
    uid 对象id
    character  实体对象
}
]]
local function getRandomTarget(monster)
    local targetList = CharacterManager:getCharacByRelation( monster.uniqueID, monster.camp, CharacterConstant.RELATION_ENEMY )
    for i = 1,10 do
        local target = {}
        local character,uid = math.randomUnit(targetList)
        if canCheckThis(character) then
            target.uid = uid
            target.character = character
            return target
        end
    end
end

--[[--
获取配置阵营最近的目标
@monster 怪物自身
@characterList 目标列表
@return target
{
    uid 对象id
    character  实体对象
}
]]
local function getCampTarget( monster,characterList, listLen )
    -- body
    local targetList = nil
    if characterList and listLen > 0 then
        targetList = characterList
    else
        targetList = CharacterManager:getCharacByRelation( monster.uniqueID, monster.camp, CharacterConstant.RELATION_ENEMY )
    end
    local relation = CharacterManager:getRelation( monster.camp, monster.findenemyai[3] )
    if relation ~= CharacterConstant.RELATION_ENEMY then
        return targetList,tableplus.mapLength(targetList) --配置的阵营非敌对关系则返回默认
    end
    local campTargetList = CharacterManager:getCharacByCamp( monster.findenemyai[3] )
    if campTargetList == nil or tableplus.mapLength(campTargetList) == 0 then
        return targetList,tableplus.mapLength(targetList) --配置的阵营无对象则返回默认
    end

    local pos = monster:getPosition()
    local distance = nil
    local target = {}
	listLen = 0
    for uid,character in pairs (campTargetList) do
        if canCheckThis(character) then
            local dis = CharacterUtil.sqrDistanceWithoutY(pos,character:getPosition())
            if not distance then
                distance = dis
                target[uid] = character
				listLen = listLen + 1
            elseif distance > dis then 
                target = {}
                distance = dis
                target[uid] = character
				listLen = 1
            end
        end
    end

    if listLen == 0 then
        return targetList, tableplus.mapLength(targetList)
    else
        return target, listLen
    end
end

local selectAIFunction = {
    [1] = getNearestTarget,
    [2] = getLeastHp,
    [3] = getTopHatred,
    [4] = getLeastTeamNum,
    [5] = getCampTarget,
}

local function DoAIType(typeTable,monster)
    local target = nil
    local lastTarget = nil
	local targetLen = 0
    for k, aitype in pairs (typeTable) do
        if target ~= nil then
            lastTarget = target
        end
        target,targetLen = selectAIFunction[aitype](monster,target, targetLen)
        if lastTarget == nil then
            lastTarget = target
        end
        if targetLen == 1 then
            return target
        elseif targetLen == 0 then
            return lastTarget
        end
    end
    return target
end


--[[--
获取索敌目标
@type 索敌类型
{
    类型0，逐个击破：判断顺序为1>2>3>4
    类型1，捏软柿子：判断顺序为2>1>3>4
    类型2，胜者为王：判断顺序为3>1>2>4
    类型3，唯我独尊：判断顺序为4>3>2>1
    类型4，东邪西毒：判断顺序为：随机选取目标。
}
@return target
{
    uid 对象id
    character  实体对象
}
影响索敌的因素有
1、距离最近
2、生命值最低
3、仇恨最高
4、队伍编号最小
]]

local typeFuncDic = 
{
	[0] = {1, 2, 3, 4},
	[1] = {2, 1, 3, 4},
	[2] = {3, 1, 2, 4},
	[3] = {4, 3, 2, 1},
	[4] = nil,
	[5] = {5, 1},
}
function FindEnemyAI.getTarget(itype,monster)
	if itype and typeFuncDic[itype] then
		return DoAIType(typeFuncDic[itype], monster)
	else
		return getRandomTarget(monster)
	end
end
