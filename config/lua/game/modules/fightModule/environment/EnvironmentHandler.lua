-- region
-- Date    : 2016-08-18
-- Author  : daiyaorong
-- Description :  环境控制器
-- endregion

EnvironmentHandler = {}

-- 环境类型,很多地方用到，使用函数返回会比这种要慢很多
EnvironmentHandler.environmentType = EnvironmentDef.DEFAULT
EnvironmentHandler.isInServer = false
EnvironmentHandler.isPvpClient = false

-- 技能同步方式
local skillSyncType = EnvironmentDef.SKILL_SYNC_STRICT

-- 粗略的网络延迟情况(毫秒)
local networkDelayMs = 0
local networkDelayCount = 0
local networkDelayTotal = 0

local orderData = nil

local s2c_specifiedOrders = nil
local s2c_MultiClientOrders = nil

local clientNetworkDecoder = nil
local updateFlag = nil	--用于控制定时器调用频率

--测试
local syncGap = nil
local specifiOrderLen = 0
local multiCOrderLen = 0
local logList = nil
--测试end

-- 客户端接收服务端lua指令
local function clientReceivePkData()
	-- body
	local conn = GameNet.GetSocket()
	orderData = conn:ReadLuaBytes()

	EnvironmentHandler.exeOrders()
end

-- 客户端接受java转发的指令
local function clientReceiveWeakSyncData()
	-- body
	local conn = GameNet.GetSocket()
	orderData = conn:ReadLuaBytes()
	EnvironmentHandler.exeOrders()
end

local function showServerLuaMsg(conn)
	local msgType = conn:ReadSbyte()
	local msg = conn:ReadString()
	local argsCount = conn:ReadSbyte()
	local args = nil
	if argsCount > 0 then
		args = {}
		for i = 1, argsCount do
			table.insert(args, conn:ReadString())
		end
	end
	if msgType == 1 then
		msg = CFG.gametext:getFormatText(msg)
	end
	if args and #args > 0 then
		msg = string.format(msg, unpack(args))
	end
	CommonTips.showCommonFloatTips(msg)
end

function EnvironmentHandler.setEnv( param )
	-- body
	EnvironmentHandler.environmentType = param
	EnvironmentHandler.isInServer = (param == EnvironmentDef.PVE_SERVER or param == EnvironmentDef.PVP_SERVER)
	EnvironmentHandler.isPvpClient = (param == EnvironmentDef.PVP_CLIENT)
	networkDelayDic = {}
	if EnvironmentHandler.isPvpClient then
		GameNet.registerRecv(PacketType.ClientPKData,clientReceivePkData)
		ModuleEvent.registerRecv(ModuleConstant.SERVER_LUA_SHOW_MSG, showServerLuaMsg)
	end
	if EnvironmentHandler.environmentType == EnvironmentDef.PVE_WEAKSYNC then
		GameNet.registerRecv(PacketType.ClientSynOrder,clientReceiveWeakSyncData)
	end
	-- 服务端向客户端输出的LOG，只在PC端有效
	-- if EnvironmentHandler.isPvpClient and GameUtil.isMobilePlatform == false then
	-- 	ModuleEvent.addListener(ModuleConstant.SERVER_TO_CLIENT_LOG, function()end, function(message)
	-- 		--print("ServerLog:" .. message)
	-- 	end)
	-- end
	if EnvironmentHandler.isInServer then
		s2c_specifiedOrders = {}
		s2c_MultiClientOrders = {}
	end
end

-- 传入服务端lua所需的数据
function EnvironmentHandler.setServerData( data )
	-- body
    syncGap = 0
    updateFlag = false
	FightModel:setServerData( data )
end

function EnvironmentHandler.updateData( data )
    FightModel:setRuntimeData( data )
end 

-- 服务端lua每帧update
function EnvironmentHandler.update( data )
	-- body
	orderData = data
	EnvironmentHandler.exeOrders()
	if updateFlag then --java调用频率为60fps，lua定时器固定为30fps
		FrameTimerManager.frameHandle()
	end
	updateFlag = not updateFlag
    local frameData, frameOrder = FightServerControl.updateResult()
	local jsonFrameData = json.encode(frameData)
	local jsonFrameOrder = json.encode(frameOrder)
    return jsonFrameData, jsonFrameOrder
end

-- 执行指令集
function EnvironmentHandler.exeOrders()
	-- body
	if EnvironmentHandler.environmentType ~= EnvironmentDef.DEFAULT then
		-- 模拟网络延迟
		if OrderDelaySimulation and OrderDelaySimulation.isEnable() then
			OrderDelaySimulation.recvOrder(orderData)
		else
			ModuleEvent.receiveOrders( orderData )
		end
		orderData = nil
	end
end

-- 发送指令集
function EnvironmentHandler.sendOrders( orders )
	-- body
	if EnvironmentHandler.isPvpClient then
		if orders == nil then
			orders = ModuleEvent.getOrdersStr()
		end
		if orders == nil then return end
		local conn = GameNet.GetSocket()
		conn:WriteProtocol(PacketType.ServerPKData)
		conn:WriteBytes(orders)
		conn:SendData()
--		--print("发送指令集~~~"..orders)
	end
end

-- 发送弱同步指令
function EnvironmentHandler.sendWeakSync( orders )
	-- body
	if EnvironmentHandler.environmentType == EnvironmentDef.PVE_WEAKSYNC then
		if orders == nil then
			orders = ModuleEvent.getOrdersStr()
		end
		if orders == nil then return end
		if StageManager.getCurStageType() == nil then return end
		local conn = GameNet.GetSocket()
		conn:WriteProtocol(PacketType.ServerSynOrder)
		conn:WriteSbyte(StageManager.getCurStageType())
		conn:WriteBytes(orders)
		conn:SendData()
--		--print("发送弱同步指令集~~~"..orders)
	end
end

-- 服务端向指定的客户端发送指令
function EnvironmentHandler.sendSpecifiedOrderToClient(uniqueID, evtId, param)
	local paramTypes = EventHandler.getEventParamTypes(evtId)
	if paramTypes == nil then
		LogManager.LogError("EventId=" .. evtId .. " cannot find the params definition in EventHandler.lua");
		return
	end
	local netwDecoder = EnvironmentHandler.getNetworkDecoder()
	netwDecoder:initBuffer()
	netwDecoder:WriteProtocol(evtId)
	for k,t in pairs(paramTypes) do
		netwDecoder:WriteByType(t, param[k])
	end
    local order = netwDecoder:GetPack()
	EnvironmentHandler.sendPackToClient(uniqueID, order)
end

function EnvironmentHandler.sendSpecifiedOrderToAllClient(evtId, param)
	local paramTypes = EventHandler.getEventParamTypes(evtId)
	if paramTypes == nil then
		LogManager.LogError("EventId=" .. evtId .. " cannot find the params definition in EventHandler.lua");
		return
	end
	local netwDecoder = EnvironmentHandler.getNetworkDecoder()
	netwDecoder:initBuffer()
	netwDecoder:WriteProtocol(evtId)
	for k,t in pairs(paramTypes) do
		netwDecoder:WriteByType(t, param[k])
	end
    local order = netwDecoder:GetPack()
	EnvironmentHandler.sendPackToAllClient(order)
end

-- 服务端向指定客户端发送消息包
function EnvironmentHandler.sendPackToClient(uniqueID, pack)
	local lastOrder = s2c_specifiedOrders[uniqueID]
	if lastOrder then
		local orderLen = #lastOrder
		for i = 1, #pack do
			lastOrder[orderLen + i] = pack[i]
		end
	else
		lastOrder = pack
	end
	specifiOrderLen = specifiOrderLen + #pack
	s2c_specifiedOrders[uniqueID] = lastOrder
end

-- 服务端向指定一组客户端发消息包
function EnvironmentHandler.sendPackToMultiClient( idList, pack )
	local record = { ['idlist']=idList, ['pack']=pack }
	multiCOrderLen = multiCOrderLen + 1
	s2c_MultiClientOrders[multiCOrderLen] = record
end

-- 服务端向全部客户端发送消息包
function EnvironmentHandler.sendPackToAllClient(pack)
	ModuleEvent.addPackToOrder(pack)
end

function EnvironmentHandler.getSpecifiedOrder()
	local temp = s2c_specifiedOrders
	s2c_specifiedOrders = {}
	specifiOrderLen = 0
	return temp
end

function EnvironmentHandler.getMultiClientOrder()
	local temp = s2c_MultiClientOrders
	s2c_MultiClientOrders = {}
	multiCOrderLen = 0
	return temp
end

function EnvironmentHandler.getMultiClientOrderLen()
	return multiCOrderLen
end

function EnvironmentHandler.getSpecifiedOrderLen()
	return specifiOrderLen;
end

-- 是否可碰撞
function EnvironmentHandler.canCollide()
	-- body
	if EnvironmentHandler.isPvpClient then
		return skillSyncType == EnvironmentDef.SKILL_SYNC_LOOSE
	end
	return true
end



-- 将调试信息发送到客户端
function EnvironmentHandler.sendLogToClient(message)
	ModuleEvent.dispatch(ModuleConstant.SERVER_TO_CLIENT_LOG, {tostring(message)})
end

-- 让指定客户端显示提示消息
-- msgType 1,gametext key  2,原始文本
function EnvironmentHandler.sendMsgToClient(uid, msgType, message, args)
	local conn = EnvironmentHandler.getNetworkDecoder()
	conn:initBuffer()
	conn:WriteProtocol(ModuleConstant.SERVER_LUA_SHOW_MSG)
	conn:WriteSbyte(msgType or 1)
	conn:WriteString(message or "")
	if args == nil or #args == 0 then
		conn:WriteSbyte(0)
	else
		conn:WriteSbyte(#args)
		for i = 1, #args do
			conn:WriteString(tostring(args[i]))
		end
	end
	local pack = conn:GetPack()
	EnvironmentHandler.sendPackToClient(uid, pack)
end

function EnvironmentHandler.sendLogToServer(message)
	if logList == nil then
		logList = {}
	end
	table.insert( logList, tostring(message) )
end

function EnvironmentHandler.getServerLog()
	if logList and #logList > 0 then
		local temp = logList
		logList = {}
		return temp
	end
	return nil
end

-- 取当前的技能同步方式
function EnvironmentHandler.getSkillSyncType()
	return skillSyncType
end

-- 更新网络延迟
function EnvironmentHandler.updateNetworkDelay(newDelay)
	if newDelay > 0 then
		networkDelayCount = networkDelayCount + 1
		networkDelayTotal = networkDelayTotal + newDelay
		networkDelayMs = networkDelayTotal / networkDelayCount 
	else
		networkDelayMs = 0
		networkDelayCount = 0
		networkDelayTotal = 0
	end
end

-- 取网络延迟
function EnvironmentHandler.getNetworkDelay()
	return networkDelayMs
end

function EnvironmentHandler.getNetworkDecoder()
	if EnvironmentHandler.isInServer then
		return GameNet.GetSocket()
	else
		if clientNetworkDecoder == nil then
			clientNetworkDecoder = NetImitateDecode:create()
		end
		return clientNetworkDecoder
	end
end

function EnvironmentHandler.clear()
	-- body
	FightServerControl.dispose()
	s2c_specifiedOrders = nil
	s2c_MultiClientOrders = nil
	orderData = nil
	clientNetworkDecoder = nil
end