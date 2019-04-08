--ModuleEvent
--事件与事件对应的监听方法
--可以支持单个事件多个监听事件


ModuleEvent = {}

local eventDic = {}
local parseDic = {}

local lua2luaRecvFuncs = {}
local functionMap = {}

function ModuleEvent.addListener(eventName, callback, parseCallback)
    local callbackList = eventDic[eventName]
    if (callbackList == nil) then
        callbackList = {}
        eventDic[eventName] = callbackList
    end
    if (callbackList[callback] == nil) then
        callbackList[#callbackList + 1] = callback
        callbackList[callback] = true
    end
    if parseCallback ~= nil then
        parseDic[eventName] = parseCallback
    end
end

--是否已经有对应的监听器了;
function ModuleEvent.isHasListener(eventName, callback)
	local callbackList = eventDic[eventName];
	if(callbackList)then
		for k,v in pairs(callbackList) do
			if(k == callback)then
				return true;
			end
		end
	end
	return false;
end

function ModuleEvent.removeListener(eventName, callback)
    local callbackList = eventDic[eventName]
    if (callbackList ~= nil and callbackList[callback] ~= nil) then
        callbackList[callback] = nil
        tableplus.remove(callbackList, callback)
        if #callbackList == 0 then
            parseDic[eventName] = nil
        end
    end
end

function ModuleEvent.removeListenerAll(eventName)
    local callbackList = eventDic[eventName]
    if (callbackList ~= nil ) then
        for k,v in pairs(callbackList) do
        	callbackList[k]=nil
        end
        if #callbackList == 0 then
            parseDic[eventName] = nil
        end
    end
end

--派发消息入口
function ModuleEvent.dispatch(eventName, param)
    -- body
    local envFuncList = EventHandler.getMethod( eventName )
    if envFuncList ~= nil then
        for key,func in pairs( envFuncList ) do
            functionMap[func](eventName, param)
        end
    else
        ModuleEvent.exe( eventName, param)
    end
end

-- 固定参数个数的事件派发接口，只支持9个以内的参数，多于9个的请采用dispatch用table方式派发
-- 主要是为了减少战斗过程中调用dispatch产生大量的临时table,导致内存上涨过快
-- 不使用 ... 变长参数的原因是，使用变长参数会导致调用效率严重降低
-- !!!只有非前后端同步的事件才可以调用该接口
-- !!!只有非前后端同步的事件才可以调用该接口
-- !!!只有非前后端同步的事件才可以调用该接口
function ModuleEvent.dispatchWithFixedArgs(eventName, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9)
	-- body
    local callbackList = eventDic[eventName]
	if (callbackList ~= nil) then
		for i = #callbackList, 1, -1 do
			if callbackList[i] ~= nil then
				callbackList[i](arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9)
			end
		end
	end
end

--UI事件注册
function ModuleEvent.addViewListener(eventName,openWin,hideWin,removeWin)
    local function viewH(vType)
        if vType == nil then return end
		local data = nil
		if type(vType) == "table" then
			data = vType[2]
			vType = vType[1]
		end
        if vType == ModuleConstant.UI_OPEN then
            if openWin and type(openWin) == 'function' then
                openWin(data)
            end
        elseif vType == ModuleConstant.UI_HIDE then
            if hideWin and type(hideWin) == 'function' then
                hideWin(data)
            end
        elseif vType == ModuleConstant.UI_REMOVE then
            if removeWin and type(removeWin) == 'function' then
                removeWin(data)
            end
        end
        WindowRule.dispatch(eventName, vType, data);
    end
    ModuleEvent.addListener(eventName,viewH)
end

function ModuleEvent.registerRecv(evtId, callback)
	if evtId == nil or callback == nil then
		return
	end
	lua2luaRecvFuncs[evtId] = callback
end

function ModuleEvent.exe( eventName, param )
    -- body
    local callbackList = eventDic[eventName]
    if (callbackList ~= nil) then
        for i = #callbackList, 1, -1 do
        	if callbackList[i] ~= nil then
            	callbackList[i](param)
        	end
        end
    end
end

function ModuleEvent.parse( eventName, param )
    -- body
    if parseDic[eventName] ~= nil then
    	parseDic[eventName]( param )
    end
end

---------------------------------------------------------------------
-- 双端通信
local orderList = nil

-- 添加指令到队列
function ModuleEvent.addOrder( eventName, eventParam )
    -- body
    local paramTypes = EventHandler.getEventParamTypes(eventName)
	if paramTypes == nil then
		LogManager.LogError("EventId=" .. eventName .. " cannot find the params definition in EventHandler.lua (ModuleEvent.addOrder)");
		return
	end
	local netwDecoder = EnvironmentHandler.getNetworkDecoder()
	netwDecoder:initBuffer()
	netwDecoder:WriteProtocol(eventName)
	for k,t in pairs(paramTypes) do
		netwDecoder:WriteByType(t, eventParam[k])
	end
    local pack = netwDecoder:GetPack()
    ModuleEvent.addPackToOrder(pack)
end

function ModuleEvent.addPackToOrder(pack)
	orderList = orderList or {}
	local orderLen = #orderList
	if pack then
		for i = 1, #pack do
			orderList[orderLen + i] = pack[i]
		end
	end
end

-- 获取所有指令
function ModuleEvent.getOrdersStr()
    -- body
    if orderList == nil or #orderList == 0 then return nil end
    local tempBuffer = orderList
	orderList = nil
	return tempBuffer
end


-- DEBUGSTART
local maxBuffLen = 0	-- 峰值
local avgBuffLen = 0	-- 平均值
local ttlBuffLen = 0	-- 总值

function ModuleEvent.debugBufferInfo()
	avgBuffLen = ttlBuffLen / (FightControlFactory.getControl().getCurrentFrame())
	return ttlBuffLen,maxBuffLen,avgBuffLen;
end

function ModuleEvent.debugClearBufferInfo()
	maxBuffLen = 0
	avgBuffLen = 0
	ttlBuffLen = 0
end
-- DEBUGEND

-- 接收并解析指令
function ModuleEvent.receiveOrders( allStr )
    -- body
    if allStr == nil or allStr == "" then return end
	-- DEBUGSTART
	local tempBuffLen = string.len(allStr)
	if tempBuffLen > maxBuffLen then maxBuffLen = tempBuffLen end
	ttlBuffLen = ttlBuffLen + tempBuffLen
	-- DEBUGEND
	local netwDecoder = EnvironmentHandler.getNetworkDecoder()
	netwDecoder:reflesh(allStr)
	local tempEvtId = nil
	local tempParam = nil
	local paramTypes = nil
	local parsedOrders = {}
	local isReadyRecv = (EnvironmentHandler.isInServer or StageManager.isStageReady())
	while netwDecoder:hasMoreData() do
		tempEvtId = netwDecoder:ReadProtocal()
		if lua2luaRecvFuncs[tempEvtId] then
			lua2luaRecvFuncs[tempEvtId](netwDecoder)
		elseif isReadyRecv or EventHandler.isIgnoreLoading(tempEvtId) then
			paramTypes = EventHandler.getEventParamTypes(tempEvtId)
			if paramTypes then
				tempParam = {}
				for _,t in pairs(paramTypes) do
					local val = netwDecoder:ReadByType(t)
					table.insert(tempParam, val)
				end
				table.insert(parsedOrders, {tempEvtId, tempParam})
			else
				LogManager.LogError("EventId=" .. tempEvtId .. " cannot find the params definition in EventHandler.lua (ModuleEvent.receiveOrders)");
			end
		else
			-- --print("not ready to execute pvp order...")
		end
	end
	for k,v in pairs(parsedOrders) do
		-- 拦截技能效果表现
		if EnvironmentHandler.isPvpClient == false or SyncSkillEffectHandler.interceptSkillEffectOrder(v[1], v[2]) == false then
			ModuleEvent.parse( v[1], v[2] )
		end
	end
end

-- 实时发送指令
function ModuleEvent.syncOrder( eventName, eventParam )
    -- body
	if SyncFrequence.isCanSync(eventName, eventParam) == false then
		return
	end
	local paramTypes = EventHandler.getEventParamTypes(eventName)
	if paramTypes == nil then
		LogManager.LogError("EventId=" .. eventName .. " cannot find the params definition in EventHandler.lua (ModuleEvent.syncOrder)");
		return
	end
	local netwDecoder = EnvironmentHandler.getNetworkDecoder()
	netwDecoder:initBuffer()
	netwDecoder:WriteProtocol(eventName)
	for k,t in pairs(paramTypes) do
		netwDecoder:WriteByType(t, eventParam[k])
	end
    local pack = netwDecoder:GetPack()
    EnvironmentHandler.sendOrders( pack )
end

-- 发送弱同步指令
function ModuleEvent.weakSync( eventName, eventParam )
    -- body
    local paramTypes = EventHandler.getEventParamTypes(eventName)
	if paramTypes == nil then
		LogManager.LogError("EventId=" .. eventName .. " cannot find the params definition in EventHandler.lua (ModuleEvent.weakSync)");
		return
	end
	local netwDecoder = EnvironmentHandler.getNetworkDecoder()
	netwDecoder:initBuffer()
	netwDecoder:WriteProtocol(eventName)
	for k,t in pairs(paramTypes) do
		netwDecoder:WriteByType(t, eventParam[k])
	end
    local pack = netwDecoder:GetPack()
    EnvironmentHandler.sendWeakSync( pack )
end

functionMap = {
	[1] = ModuleEvent.exe,
	[2] = ModuleEvent.addOrder,
	[3] = ModuleEvent.syncOrder,
	[4] = ModuleEvent.weakSync,
}