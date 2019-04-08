--region ServerOrderHandler.lua
--Date 2016/12/13
--Author zhouxiaogang
--Desc 服务端lua执行服务端java指令
--endregion

ServerOrderHandler = {}

-- 执行指令：给指定阵营添加buff
local function addCampBuffHandler(sOrder)
	local campBuffMap = FightModel:getFightData():getData(FightDefine.DATA_CAMP_BUFF)
	campBuffMap[sOrder.instanceid] = sOrder
	--print("add camp buff:" .. tostring(sOrder.buffid) .. "," .. tostring(sOrder.level))
	if sOrder.campid ~= 0 then
		local characList = CharacterManager:getCharacByCamp(sOrder.campid)
		if characList then
			for uid, charac in pairs( characList ) do
				if sOrder.charactertype == 0 then --全部适用
					ModuleEvent.dispatch(ModuleConstant.ADD_BUFF, {uid, uid, sOrder.buffid, sOrder.level, uid .. "_" .. sOrder.instanceid})
					--print("#####[ADD BUFF] uid=" .. tostring(uid) .. ",instanceId=" .. tostring(uid .. "_" .. sOrder.instanceid))
				else --指定角色类型
					if sOrder.charactertype == charac.characterType then
						ModuleEvent.dispatch(ModuleConstant.ADD_BUFF, {uid, uid, sOrder.buffid, sOrder.level, uid .. "_" .. sOrder.instanceid})
						--print("#####[ADD BUFF] uid=" .. tostring(uid) .. ",instanceId=" .. tostring(uid .. "_" .. sOrder.instanceid))
					end
				end
			end
		end
	end
	if #sOrder.characIdList > 0 then
		for k,uid in pairs(sOrder.characIdList) do
			ModuleEvent.dispatch(ModuleConstant.ADD_BUFF, {uid, uid, sOrder.buffid, sOrder.level, uid .. "_" .. sOrder.instanceid})
			--print("#####[ADD BUFF] uid=" .. tostring(uid) .. ",instanceId=" .. tostring(uid .. "_" .. sOrder.instanceid))
		end
		----print("----------------------------------------------------------------------------")
	end
	-- EnvironmentHandler.sendLogToServer("####do server order[add_camp_buff]:" .. tostring(sOrder.campid) .. "," .. tostring(sOrder.buffid) .. "," .. tostring(sOrder.level) .. "," .. tostring(sOrder.instanceid))
end

-- 执行指令：移除指定阵营的buff
local function removeCampBuffHandler(sOrder)
	local campBuffMap = FightModel:getFightData():getData(FightDefine.DATA_CAMP_BUFF)
	campBuffMap[sOrder.instanceid] = nil
	if sOrder.campid ~= 0 then
		local characList = CharacterManager:getCharacByCamp(sOrder.campid)
		if characList then
			for uid, charac in pairs( characList ) do
				ModuleEvent.dispatch(ModuleConstant.REMOVE_BUFF, {uid, uid .. "_" .. sOrder.instanceid})
			end
		end
	end
	if #sOrder.characIdList > 0 then
		for k,uid in pairs(sOrder.characIdList) do
			ModuleEvent.dispatch(ModuleConstant.REMOVE_BUFF, {uid, uid .. "_" .. sOrder.instanceid})
		end
	end
	-- EnvironmentHandler.sendLogToServer("####do server order[remove_camp_buff]:" .. tostring(sOrder.campid) .. "," .. tostring(sOrder.instanceid))
end

local moveFrameKeys = nil
-- 执行指令：移动指定角色到指定位置
local function moveCharacHandler(sOrder)
	local charac = CharacterManager:getCharacByUId(sOrder.uniqueID)
	if charac and sOrder.pos then
		local posX = math.round(sOrder.pos.x, 2)
		local posY = math.round(sOrder.pos.y, 2)
		local posZ = math.round(sOrder.pos.z, 2)
		if moveFrameKeys and moveFrameKeys[sOrder.uniqueID] then
			FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):removeLuaFunc(moveFrameKeys[sOrder.uniqueID])
			moveFrameKeys[sOrder.uniqueID] = nil
		end
		if moveFrameKeys == nil then
			moveFrameKeys = {}
		end
		ModuleEvent.dispatch(ModuleConstant.AI_ACTION_MOVE, {sOrder.uniqueID, posX, posY, posZ, "0"})
		moveFrameKeys[sOrder.uniqueID] = FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(60, 5, function() 
			-- EnvironmentHandler.sendLogToServer("####do server order[move]:" .. tostring(sOrder.uniqueID) .. "," .. tostring(sOrder.pos))
			ModuleEvent.dispatch(ModuleConstant.AI_ACTION_MOVE, {sOrder.uniqueID, posX, posY, posZ, "0"})
		end)
	end
end

-- 执行指令：让指定角色在当前位置停下来
local function stopMoveCharacHandler(sOrder)
	local charac = CharacterManager:getCharacByUId(sOrder.uniqueID)
	if moveFrameKeys and moveFrameKeys[sOrder.uniqueID] then
		FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):removeLuaFunc(moveFrameKeys[sOrder.uniqueID])
		moveFrameKeys[sOrder.uniqueID] = nil
	end
	if charac then
		-- 用移动来实现停止，移动到当前点，因为已经在当前点，所以会结束当前移动
		local pos = charac:getPosition()
		local posX = math.round(pos.x, 2)
		local posY = math.round(pos.y, 2)
		local posZ = math.round(pos.z, 2)
		ModuleEvent.dispatch(ModuleConstant.AI_ACTION_MOVE, {sOrder.uniqueID, posX, posY, posZ, "0"})
		-- EnvironmentHandler.sendLogToServer("####do server order[stop_move]:" .. tostring(sOrder.uniqueID))
	end
end

-- 执行指令：更新战斗状态
-- duration 在FIGHTING状态时，表示剩余的战斗时间; 在PAUSE状态时，表示暂停多久
local function changeFightStateHandler(sOrder)
	local fightState = sOrder.fightState
	local duration = sOrder.duration or -1
	local fightCtrl = FightControlFactory.getControl()
	if fightCtrl then
		fightCtrl.setFightState(fightState)
		if fightState == FightDefine.FIGHT_STATE_FIGHTING and duration ~= -1 then
			fightCtrl.setFightTimeInfo(duration * 0.001, 0)
		end
		if fightState == FightDefine.FIGHT_STATE_PAUSE and duration ~= -1 then
			local pauseFrame = duration * 0.001 * ConstantData.FRAME_RATE
			FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(pauseFrame, 1, function()  
				if fightCtrl.getFightState() == FightDefine.FIGHT_STATE_PAUSE then
					fightCtrl.setFightState(FightDefine.FIGHT_STATE_FIGHTING)
				end
			end)
		end
		-- EnvironmentHandler.sendLogToServer("####do server order[change_fightstate]:" .. tostring(fightState) .. "," .. tostring(duration))
		ModuleEvent.dispatch(ModuleConstant.CLIENT_SYNC_FIGHTSTATE, {fightState, duration})
	end
end

-- 执行指令：重置指定角色列表的状态
local function resetCharacsHandler(sOrder)
	if sOrder.characIdList == nil then
		return
	end
	local tempCharac= nil
	for k,uid in pairs(sOrder.characIdList) do
		tempCharac = CharacterManager:getCharacByUId(uid)
		if tempCharac then
			ModuleEvent.dispatch(ModuleConstant.CHARAC_UPDATE_ATTR,{uid, ConstantData.ROLE_CONST_HP, tempCharac.maxhp})
			-- EnvironmentHandler.sendLogToServer("#####reset charac:" .. tostring(uid))
		end
	end
end

local function addBuffToCharacs(sOrder)
	if sOrder.characIdList == nil then
		return
	end
	for k,uid in pairs(sOrder.characIdList) do
		ModuleEvent.dispatch(ModuleConstant.ADD_BUFF, {uid, uid, sOrder.buffid, sOrder.level, uid .. "_" .. sOrder.instanceid})
	end
end

local function setCharacterAI( sOrder )
	-- body
	if sOrder.characIdList == nil then
		return
	end
	for k,uid in pairs(sOrder.characIdList) do
		ModuleEvent.dispatch(ModuleConstant.PLAYER_SWITCH_AI, {uid, sOrder.aiState})
	end
end

local handlerMap = 
{
	[FightDefine.SORDER_ADDCAMPBUFF] = addCampBuffHandler,
	[FightDefine.SORDER_CHANGE_FIGHTSTATE] = changeFightStateHandler,
	[FightDefine.SORDER_MOVECHARAC] = moveCharacHandler,
	[FightDefine.SORDER_REMOVECAMPBUFF] = removeCampBuffHandler,
	[FightDefine.SORDER_RESET_CHARACS] = resetCharacsHandler,
	[FightDefine.SORDER_STOPCHARAC] = stopMoveCharacHandler,
	[FightDefine.SORDER_ADD_BUFF] = addBuffToCharacs,
	[FightDefine.SORDER_SETAI] = setCharacterAI,
}

-- 执行指令
function ServerOrderHandler.handler(sOrder)
	if sOrder and handlerMap[sOrder.ordertype] then
		handlerMap[sOrder.ordertype](sOrder)
	end
end

function ServerOrderHandler.handlerOrderList(sOrders)
	if sOrders and #sOrders > 0 then
		for _,sOrder in ipairs(sOrders) do
			-- EnvironmentHandler.sendLogToServer("execute java order:" .. tostring(sOrder.ordertype))
			ServerOrderHandler.handler(sOrder)
		end
	end
end


