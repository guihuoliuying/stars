--游戏网络类

GameNet = {}

-- 网络状态
-- GameNet.NetState = {
-- 	UNKNOW = 0,				-- 未知
--     ACCEPT = 1,				-- 连接成功
-- 	CLOSE = 2,				-- 断开
-- 	EXCEPTION = 3,			-- 异常
-- 	CONNECT_EXCEPTION = 4,	-- 连接异常
-- }

-- -- 服务器类型
-- GameNet.SERVER_TYPE = 
-- {
-- 	LOGIN_SERVER = 1,		-- 登录服
-- 	GAME_SERVER = 2,		-- 游戏服
-- }

-- GameNet.State = GameNet.NetState.UNKNOWN
-- require("Config_Net")
-- local AccepetCallBack = nil --链接成功回调函数
-- local heartBeatGap = 2 --心跳包间隔秒数
-- local oldTime = Time.realtimeSinceStartup
-- local managerInstance -- ConnectionManager的实例
-- local currConnIP,currConnPort = nil,nil
-- local mainVersion,subVersion = 5,0    --游戏版本号
-- local versionStr = mainVersion .. "." .. subVersion
-- local reConnectTimes = 0		-- 已经进行过的重连次数
-- local MAX_RECONNECT_TIMES = 5	-- 最大重连次数，超过之后将强制回到登录界面
-- local isNeedSendHeartBeat = false	-- 是否需要发送心跳包,登录成功后标记为true


-- local serverConntection = nil	-- 当前连接
-- local packetSendArrayFunc = {}	-- 发送函数列表
-- local multiRecvList = {}		-- 多协议组合回调列表，收到所有目标协议后，才会执行回调

-- local myselfCharacter = nil

-- ----------------------------局部方法-----------------------------

-- --心跳包发送协议
-- local function sendHeartPacket(x,y,z)
--     local connMng = GameNet.GetSocket()
--     connMng:WriteProtocol(PacketType.ServerHeartBeat) --包协议类型
--     connMng:WriteInt(x)
--     connMng:WriteInt(y)
--     connMng:WriteInt(z)
--     connMng:SendData()
-- end

-- --定时发送心跳包
-- local function heartBeat() 
--     local newTime = Time.realtimeSinceStartup
--     if(newTime - oldTime >= heartBeatGap) then
--         oldTime = newTime
--         if myselfCharacter == nil then
--         	myselfCharacter = CharacterManager:getMyCharac()
--         end
--     	if myselfCharacter then
--     		local pos = myselfCharacter.position * 10
--         	sendHeartPacket(pos.x,pos.y,pos.z)
--     	end
--     end
-- end

-- --网络定时器 检测连接状态 与发包 收包
-- local function connUpdateFunc()
-- 	if isNeedSendHeartBeat then
-- 		heartBeat() --心跳检测
-- 	end
-- 	TimeUtils.TimerUpdate()
-- end

-- -- 网络断开时尝试重新连接服务器
-- local function tryReconnectToServer()
-- 	if reConnectTimes > MAX_RECONNECT_TIMES then
-- 		reConnectTimes = 0
-- 		if LoginWindow.IsVisible == false then
-- 			GameState.DisConnect()
-- 		end
-- 		return
-- 	end
-- 	reConnectTimes = reConnectTimes + 1
-- 	ModuleEvent.dispatch(ModuleConstant.BUSYINDICATORWINDOW_SHOW, {ConstantData.BUSY_TYPE_NETWORK, "reconnect", 5, "重新连接中..."})
-- 	-- TODO connect
-- end

-- --连接断开
-- local function connClose()
--     CmdLog("conn state close")
--     GameNet.State = GameNet.NetState.CLOSE
--     GameState.DisConnect()
-- end

-- -- 连接异常
-- local function connException()
--     CmdLog("conn state exception")
--     GameNet.State = GameNet.NetState.EXCEPTION
--     GameState.DisConnect()
-- end
-- local function connAccpet()
--     CmdLog("accept connec")
--     GameNet.State = GameNet.NetState.ACCPET
--     if AccepetCallBack ~= nil then
--         AccepetCallBack()
--         AccepetCallBack = nil
--     end
-- end
-- --连接状态入口方法
-- local function connState(state)
-- 	LuaHelper.consoleLog("=========================conn state:" .. tostring(state))
-- 	-- 因为该方法是由网络线程调用的，这里不能调主线程显示相关的东西，所以延时一帧执行函数
--     if state == GameNet.NetState.EXCEPTION or state == GameNet.NetState.CONNECT_EXCEPTION then
--         FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(1, 1, connException)
--     elseif state == GameNet.NetState.CLOSE then
--         FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(1, 1, connClose)
--     elseif state == GameNet.NetState.ACCEPT then
-- 		FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(1, 1, connAccpet)
--     end
-- end

-- local function multiRecvFunc(cmd)
-- 	local isAllRecv = false
-- 	local temp = nil
-- 	for i = #multiRecvList, 1, -1 do
-- 		temp = multiRecvList[i]
-- 		if temp.protocals[cmd] ~= nil then
-- 			temp.protocals[cmd] = true
-- 			isAllRecv = true
-- 			for _,v in pairs(temp.protocals) do
-- 				if v == false then
-- 					isAllRecv = false;break;
-- 				end
-- 			end
-- 			if isAllRecv then
-- 				temp.func()
-- 				table.remove(multiRecvList, i)
-- 			end
-- 		end
-- 	end
-- 	if #multiRecvList == 0 then
-- 		NetWorkManager.Instance.onRecvCommandLua = nil
-- 	end
-- end

-- -----------------------------全局方法-----------------------------

-- -- 建立连接, 目前同时只连一个服，有需要可以改成保存多个
-- function GameNet.ConnectServer(serverType, ip, port, callback)
--   --建立连接
--   LuaHelper.consoleLog("connect to server:" .. tostring(ip) .. ":" .. tostring(port))
--   AccepetCallBack = callback
--   if serverConntection then
-- 	GameNet.DisconnectServer()
--   end
--   serverConntection = NetWorkManager.Instance:CreateServerConnect(serverType, ip, port)
--   --注册连接状态改变回调函数
--   serverConntection:AddConnectChangeCB(connState)
-- end

-- -- 关闭连接
-- function GameNet.DisconnectServer()
-- 	if serverConntection then
-- 		NetWorkManager.Instance:DisposeServerConnect(serverConntection)
-- 		serverConntection = nil
-- 		--关闭发送心跳包;
-- 		GameNet:enableHeartBeat(false);
-- 		myselfCharacter = nil
-- 	end
-- end

-- function GameNet.isValid()
-- 	return serverConntection ~= nil;
-- end

-- -- 获取取当前连接
-- function GameNet.GetSocket()
-- 	return serverConntection;
-- end

-- -- 是否已连上服务器
-- function GameNet.IsConnect()
-- 	return GameNet.State == GameNet.NetState.ACCEPT;
-- end

-- -- 注册发送函数
-- -- protocalId 协议号
-- -- callback	回调函数
-- function GameNet.registerSend(protocalId, callback)
-- 	packetSendArrayFunc[protocalId] = callback
-- end

-- 注册接收函数
-- protocalId 协议号
-- callback 回调函数，参数为空
function GameNet.registerRecv(protocalId, callback)
	-- if protocalId and callback then
	-- 	NetWorkManager.Instance:RegisterLuaNetEvent(protocalId, callback)
	-- end
end

-- -- 发送协议包
-- -- protocalId 协议号
-- -- ... 协议内容,如果在func中有相应处理，这里可以不传
-- function GameNet.sendPacket(protocalId, ...)
-- 	local func = packetSendArrayFunc[protocalId]
-- 	if func then
-- 		CmdLog("发出协议:" .. tostring(protocalId) .. "|" .. tableplus.formatstring({...}))
-- 		func(...)
-- 	end
-- end

-- -- 注册一个回调，这个回调会在指定的多个协议都收到后才执行
-- -- func 回调函数
-- -- ... 协议号列表,可多个
-- -- * 这个函数不是用来解析协议的,只是个辅助功能，解析协议请使用GameNet.registerRecv方法
-- function GameNet.registerMultiRecvFunc(func, ...)
-- 	if func == nil then return end
-- 	local protocals = {...}
-- 	if #protocals == nil then return end
-- 	local temp = {}
-- 	temp.func = func
-- 	temp.protocals = {}
-- 	for _,cmd in ipairs(protocals) do
-- 		temp.protocals[cmd] = false
-- 	end
-- 	table.insert(multiRecvList, temp)
-- 	if NetWorkManager.Instance.onRecvCommandLua == nil then
-- 		NetWorkManager.Instance.onRecvCommandLua = multiRecvFunc
-- 	end
-- end

-- -- 移除多协议监听回调，与上面方法配对(在回调未执行时有效，执行后会自动删除，不用再调用该方法)
-- function GameNet.removeMultiRecvFunc(func)
-- 	if func == nil then return end
-- 	local hitIndex = nil
-- 	for index, temp in ipairs(multiRecvList) do
-- 		if temp.func == func then
-- 			hitIndex = index;break;
-- 		end
-- 	end
-- 	if hitIndex then
-- 		table.remove(multiRecvList, hitIndex)
-- 	end
-- end

-- --网络初始化入口g
-- function GameNet:initNet()    
--     --网络定时器启动 忽略timescale
--     TimeManager.addRepeat(connUpdateFunc, 1, true )
-- end

-- -- 获取主版本号
-- function GameNet:getMainVersion()
--     return mainVersion
-- end

-- -- 获取版本号
-- function GameNet:getVersionStr()
--     return versionStr
-- end

-- -- 是否发送心跳包(登录成功后设置)
-- function GameNet:enableHeartBeat(enable)
-- 	isNeedSendHeartBeat = enable
-- end

--[[ 
发协议：
	local function send(id, name)
		local conn:GameNet.GetSocket()
		conn:WriteProtocol(0x00001)
		conn:WriteString(name)
		conn:WriteInt(id)
		conn:WriteLong(100)
		conn:WriteSbyte(1)
		conn:WriteShort(15)
		...
		conn:SendData()
	end

	-- 注册
	GameNet.registerSend(0x00001, send)
	
	-- 发送
	GameNet.sendPacket(0x00001, 10001, "hehe")

收协议:
	local function recv()
		local conn = GameNet.GetSocket()
		conn:ReadInt()
		conn:ReadString()
		conn:ReadSbyte()
		...
	end

	--注册
	GameNet.registerRecv(0x00002, recv)

--]]