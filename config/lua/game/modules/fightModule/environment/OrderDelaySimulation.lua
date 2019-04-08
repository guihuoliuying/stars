--region OrderDelaySimulation.lua
--Date 2016/10/14
--Author zhouxiaogang
--Desc 模拟PVP双端指令的网络延迟
--endregion

OrderDelaySimulation = {}

local isActiveInClient = false	-- 客户端是否启用模拟延迟
local isActiveInServer = false	-- 服务端是否启用模拟延迟

-- 在minDelay 和 maxDelay 之间随机延迟一个毫秒数
local minDelay = 0		-- 延迟下限
local maxDelay = 1000	-- 延迟上限


local delayList = Queue:new()
local nextOrderMs = nil
local frameKey = nil
local recvFunc = nil
local clientIgnoreList = nil
local popingList = nil
local enable = false
function OrderDelaySimulation.init(minMs, maxMs, func)
	minDelay = minMs
	maxDelay = maxMs
	recvFunc = func
	clientIgnoreList = {}
	delayList = Queue:new()
	frameKey = FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(0, 0, OrderDelaySimulation.update)
	enable = true
end

function OrderDelaySimulation.isEnable()
	return enable
end

function OrderDelaySimulation.addClientIgnoreRoleId(roleId)
	clientIgnoreList[roleId] = true
end

function OrderDelaySimulation.recvOrder(order)
	if EnvironmentHandler.getEnv() == EnvironmentDef.PVP_CLIENT then
		if clientIgnoreList[RoleData.roleId] then
			if recvFunc then recvFunc(order) end
			return
		end
	end
	delayList:add(order)
	if nextOrderMs == nil then
		nextOrderMs = math.random(minDelay, maxDelay) + (GameNet.GetServerTime())
	end
end

function OrderDelaySimulation.update()
	if popingList then
		local order = table.remove(popingList, 1)
		if #popingList == 0 then
			popingList = nil
		end
		if recvFunc then recvFunc(order) end
		return
	end
	if delayList:getSize() == 0 then
		return
	end
	local msNow = GameNet.GetServerTime()
	if msNow >= nextOrderMs then
		nextOrderMs = math.random(minDelay, maxDelay) + msNow
		popingList = {}
		while true do
			local order = delayList:pop()
			if order == nil then break; end
			table.insert(popingList, order)
		end
	end
end

function OrderDelaySimulation.dispose()
	if frameKey then
		FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):removeLuaFunc(frameKey)
		frameKey = nil
	end
	delayList = nil
end


local function onStageLoadDone()
	if EnvironmentHandler.getEnv() == EnvironmentDef.PVP_CLIENT then
		OrderDelaySimulation.init(minDelay, maxDelay, ModuleEvent.receiveOrders)
	else
		OrderDelaySimulation.dispose()
	end
end

do
	if isActiveInServer then
		if EnvironmentHandler.isInServer() then
			OrderDelaySimulation.init(minDelay, maxDelay, ModuleEvent.receiveOrders)
		end
	end
	if isActiveInClient then
		ModuleEvent.addListener(ModuleConstant.STAGE_LOADCOMPLETE, onStageLoadDone)
	end
end