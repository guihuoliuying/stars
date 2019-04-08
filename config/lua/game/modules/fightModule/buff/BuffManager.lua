--region BuffManager.lua
--Date 2016/07/14
--Author zhouxiaogang
-- BUFF管理
--endregion

BuffManager = 
{
	isDebug = false,
}

local charaBuffDic = nil

-- 缓存服务端同步下来的尚未添加的buff请求
local waitToDisplayBuffs = nil

-- 当前不能显示buff特效
local isForbidShowEffect = false

local BUFF_INSTANCE_ID_SEED = 0

-- buff对象ID直接自增
function BuffManager.getInstanceId()
    BUFF_INSTANCE_ID_SEED = BUFF_INSTANCE_ID_SEED + 1
    return tostring(BUFF_INSTANCE_ID_SEED);
end

-- 添加buff
-- args = {攻击者uniqueID, 目标uniqueID, buffId, buffLv,buffInstanceId}
local function addBuff(args)
	local attackerUID = args[1]
	local targetUID = args[2]
    local target = CharacterManager:getCharacByUId(targetUID)
    -- 陷阱不能被施加添加BUFF
    if target == nil or target.hp <= 0 or target.istrap == 1 then return end
	local buffId = args[3]
	local buffLv = args[4]
	local buffInstanceId = args[5]
	local charaBuff = charaBuffDic[targetUID]
	----print("==================addbuff:" .. tostring(buffInstanceId) .. ",buffid:" .. tostring(buffId) .. "," .. tostring(buffLv))
	if charaBuff == nil then
		charaBuff = CharacterBuff:create()
		charaBuff:init(targetUID)
		charaBuffDic[targetUID] = charaBuff
		if target then
			target.charaBuff = charaBuff
		end
	end
	local buffData = FightModel:getBuffVo(buffId, buffLv)
	if buffData then
		charaBuff:addBuff(buffData, attackerUID,buffInstanceId)
	end
end

local function parseAddBuff(args)
	if EnvironmentHandler.isInServer == false then
		if StageManager.isStageReady() == false or CharacterManager:getCharacByUId(args[2]) == nil then
			args.recvTime = Time.realtimeSinceStartup
			
			table.insert(waitToDisplayBuffs, args)
			return
		end
	end
	addBuff(args)
end

local function checkAddDisplayBuff()
	if #waitToDisplayBuffs == 0 then
		return
	end
	local curTime = Time.realtimeSinceStartup
	local target = nil
	local buffData = nil
	local buffArgs = nil
	for i = #waitToDisplayBuffs, 1, -1 do
		buffArgs = waitToDisplayBuffs[i]
		target = CharacterManager:getCharacByUId(buffArgs[2])
		if target and target.hp > 0 then
			buffData = FightModel:getBuffVo(buffArgs[3], buffArgs[4])
			if buffData and (buffData.duringTime == -1 or (buffArgs.recvTime + buffData.duringTime * 0.001) > (curTime - 0.5)) then
				table.remove(waitToDisplayBuffs, i)
				addBuff(buffArgs)
			else
				table.remove(waitToDisplayBuffs, i)		-- 该buff已超时
			end
		end
	end
end

local function checkRemoveDisplayBuff(buffInstanceId)
	if waitToDisplayBuffs == nil or #waitToDisplayBuffs == 0 then
		return
	end
	for i = #waitToDisplayBuffs, 1, -1 do
		if waitToDisplayBuffs[i][5] == buffInstanceId then
			table.remove(waitToDisplayBuffs, i)
		end
	end
end

local function removeDisplayBuffWhenTargetDie(targetUID)
	if #waitToDisplayBuffs == 0 then
		return
	end
	for i = #waitToDisplayBuffs, 1, -1 do
		if waitToDisplayBuffs[i][2] == targetUID then
			table.remove(waitToDisplayBuffs, i)
		end
	end
end

-- 移除buff
-- args = {目标uniqueID, buff唯一ID}
local function removeBuff(args)
	local targetUID = args[1]
	local buffInstanceId = args[2]
	if charaBuffDic == nil then return end
	local charaBuff = charaBuffDic[targetUID]
	if charaBuff then
		charaBuff:removeBuff(buffInstanceId)
	end
	checkRemoveDisplayBuff(buffInstanceId)
end

local function parseRemoveBuff(args)
	removeBuff(args)
end

-- 移除某个玩家身上的全部buff
local function removeCharaBuff(uniqueId)
	local charaBuff = charaBuffDic[uniqueId]
	if charaBuff then
		charaBuff:dispose()
		charaBuffDic[uniqueId] = nil
		local target = CharacterManager:getCharacByUId(uniqueId)
		if target then
			target.charaBuff = nil
		end
	end
	removeDisplayBuffWhenTargetDie(uniqueId)
end

local function onCharacRemove(uid)
	if uid == nil then return end
	removeCharaBuff(uid)
end

-- 现在buff给某个buff(一般是buff自己给自己添加)添加一个效果
local function addBuffEffect(targetUID, buffInstanceId, effectInfo)
	local charaBuff = charaBuffDic[targetUID]
	if charaBuff then
		charaBuff:addEffectToBuff(buffInstanceId, effectInfo)
	end
end

-- 添加事件
local function initEvent()
	ModuleEvent.addListener(ModuleConstant.ADD_BUFF, addBuff, parseAddBuff)
	ModuleEvent.addListener(ModuleConstant.REMOVE_BUFF, removeBuff, parseRemoveBuff)
	ModuleEvent.addListener(ModuleConstant.CHARACTER_DEAD, removeCharaBuff)
	ModuleEvent.addListener(ModuleConstant.ADD_BUFF_EFFECT, addBuffEffect)
	ModuleEvent.addListener(ModuleConstant.REMOVE_CHARC_BUFF, removeCharaBuff)
	ModuleEvent.addListener(ModuleConstant.STAGE_LOADCOMPLETE, checkAddDisplayBuff)
	ModuleEvent.addListener(ModuleConstant.CHARAC_CREATE_COMPLETE, checkAddDisplayBuff)
	ModuleEvent.addListener(ModuleConstant.CHARAC_REMOVE, onCharacRemove)
end

-- 移除事件
local function removeEvent()
	ModuleEvent.removeListener(ModuleConstant.ADD_BUFF, addBuff)
	ModuleEvent.removeListener(ModuleConstant.REMOVE_BUFF, removeBuff)
	ModuleEvent.removeListener(ModuleConstant.CHARACTER_DEAD, removeCharaBuff)
	ModuleEvent.removeListener(ModuleConstant.ADD_BUFF_EFFECT, addBuffEffect)
	ModuleEvent.removeListener(ModuleConstant.REMOVE_CHARC_BUFF, removeCharaBuff)
	ModuleEvent.removeListener(ModuleConstant.STAGE_LOADCOMPLETE, checkAddDisplayBuff)
	ModuleEvent.removeListener(ModuleConstant.CHARAC_CREATE_COMPLETE, checkAddDisplayBuff)
	ModuleEvent.removeListener(ModuleConstant.CHARAC_REMOVE, onCharacRemove)
end

-- 获取一个CharacterBuff对象
-- charaUniqueId 角色的uniqueId
function BuffManager:getCharacterBuff(charaUniqueId)
	if charaBuffDic == nil then return end
	return charaBuffDic[charaUniqueId]
end

-- 某个角色身上是否有指定的buff
function BuffManager:characHasBuff(uniqueID, buffId)
	if charaBuffDic == nil then return false end
	if charaBuffDic[uniqueID] == nil then 
		return false
	end
	return charaBuffDic[uniqueID]:hasBuff(buffId)
end

-- buff更新
function BuffManager:update()
	if charaBuffDic == nil then return end
	local deltaTime = Time.deltaTime
	for _,charaBuff in pairs(charaBuffDic) do
		charaBuff:update(deltaTime)
	end
end

-- 启用所有BUFF特效
function BuffManager:enableAllBuffEffect()
	if isForbidShowEffect and charaBuffDic ~= nil then
		isForbidShowEffect = false
		for _,charaBuff in pairs(charaBuffDic) do
			charaBuff:enableBuffDisplayEffect()
		end
	end
end

-- 停止所有BUFF特效
function BuffManager:disableAllBuffEffect()
	if isForbidShowEffect == false and charaBuffDic ~= nil then
		isForbidShowEffect = true
		for _,charaBuff in pairs(charaBuffDic) do
			charaBuff:disableBuffDisplayEffect()
		end
	end
end

function BuffManager:handlerRuntimeBuffs(buffs)
	for k,buffArgs in pairs(buffs) do
		parseAddBuff(buffArgs)
	end
end

function BuffManager:init()
	charaBuffDic = {}
	waitToDisplayBuffs = {}
	initEvent()
end

function BuffManager:clear()
	removeEvent()
	if charaBuffDic then
		for _,charBuff in pairs(charaBuffDic) do
			charBuff:dispose()
		end
	end
	charaBuffDic = nil
	waitToDisplayBuffs = nil
end