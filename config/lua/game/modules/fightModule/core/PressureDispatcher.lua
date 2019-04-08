-- region
-- Date    : 2016-06-15
-- Author  : daiyaorong
-- Description :  指令队列 帧更新
-- endregion

local addCharacterOrder = nil
local addCharacterOrderPerFrame = 1

local monsterFireOrder = nil
local monsterFireOrderPerFrame = 10

local nSkillEffectExecOrder = nil
local SkillEffectExecOrderPerFrame = 10     --每帧执行的skillEffect次数

local T_INSERT = table.insert
local T_REMOVE = table.remove

PressureDispatcher = {}

-- table池，避免反复创建table
local freeTables = nil
local freeTableCount = 0
local function poolFreeTable(t)
	if freeTables == nil then return end
	t.func = nil
	t.args = nil
	t.isInvalid = nil
	T_INSERT(freeTables, t)
	freeTableCount = freeTableCount + 1
end

local function getFreeTable()
	if freeTableCount > 0 then
		freeTableCount = freeTableCount - 1
		return T_REMOVE(freeTables)
	end
	return {func = nil, args = nil, isInvalid = nil}
end

function PressureDispatcher.init()
	freeTables = {}
    addCharacterOrder = addCharacterOrder or Queue:new()
    monsterFireOrder = monsterFireOrder or Queue:new()
    nSkillEffectExecOrder = nSkillEffectExecOrder or Queue:new()
end

--- 清空
function PressureDispatcher.clear()
	freeTables = nil
	freeTableCount = 0
    if(addCharacterOrder)then
        addCharacterOrder:clear()
    end
    if(monsterFireOrder)then
        monsterFireOrder:clear()
    end
    if(nSkillEffectExecOrder)then
        nSkillEffectExecOrder:clear()
    end
end

function PressureDispatcher.orderAddCharacter( func, args )
    -- body
    local pack = getFreeTable()
    pack.func = func
    pack.args = args
    addCharacterOrder = addCharacterOrder or Queue:new()
    addCharacterOrder:add( pack )
end

function PressureDispatcher.markCharacInvalidIfExist(uniqueID)
	if addCharacterOrder == nil or addCharacterOrder:getSize() <= 0 then return end
	local first = addCharacterOrder.first
	local last = addCharacterOrder.last
	for i = first, last do
		if addCharacterOrder:getVal(i).args.uniqueID == uniqueID then
			addCharacterOrder:getVal(i).isInvalid = true;break;
		end
	end
end

function PressureDispatcher.orderMonsterFire( func, args )
    -- body
    local pack = getFreeTable()
    pack.func = func
    pack.args = args
    monsterFireOrder = monsterFireOrder or Queue:new()
    monsterFireOrder:add( pack )
end

--- 将skillEffect计算加入队列
-- @param func
-- @param args
--
function PressureDispatcher.orderSkillEffect(func,args)
    local pack = getFreeTable()
    pack.func = func
    pack.args = args
    nSkillEffectExecOrder = nSkillEffectExecOrder or Queue:new()
    nSkillEffectExecOrder:add(pack)
end

--- 帧调用，每帧只执行一定数量的nfire和寻路
function PressureDispatcher.update()
    local pack = nil
    for i = 1, addCharacterOrderPerFrame do
        pack = addCharacterOrder:pop()
        if pack == nil then break end
		-- 已失效的不创建
		if (not pack.isInvalid) then
			pack.func( pack.args )
		end
		poolFreeTable(pack)
    end

    for i = 1, monsterFireOrderPerFrame do
        pack = monsterFireOrder:pop()
        if pack == nil then break end
        pack.func( pack.args )
		poolFreeTable(pack)
    end

    for i=1, SkillEffectExecOrderPerFrame do
        pack = nSkillEffectExecOrder:pop()
        if not pack then return end
        pack.func( pack.args )
		poolFreeTable(pack)
    end
end
