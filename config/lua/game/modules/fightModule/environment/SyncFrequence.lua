--region SyncFrequence.lua
--Date 2016/10/11
--Author zhouxiaogang
--Desc 同步频率控制(客户端向服务端或队长机发送指令的频率限制)
--endregion

SyncEventInfo = 
{
	evtId				= nil,	-- 消息ID
	lastSyncTimeMs		= nil,	-- 上一次发出同步消息的时间
	timeoutMs			= nil,	-- 超时时间，超过该时间仍未发出过同步包，则触发timeout回调
	limitMs				= nil,	-- 发包的频率限制，两次发送时间小于该值时，后发出的将被截断
	unsyncRecords		= nil,	-- 被截取的消息记录
	timeoutFunc			= nil,	-- 超时回调
	limitFunc			= nil,	-- 限时回调
	nextSyncMs			= nil,	-- 下一次可发出消息的时间点
	nextTimeoutMs		= nil,	-- 下一次的超时时间点
	needCallLimit		= nil,	-- 是否需要调用limitFunc
	isRecordSyncPacket	= nil,	-- 是否需要记录消息历史
}

function SyncEventInfo:create(evtId, limitMs, timeoutMs, isRecordSyncPacket)
	local o = {}
	setmetatable(o, self)
	self.__index = self
	o.evtId = evtId
	o.limitMs = limitMs or 0
	o.timeoutMs = timeoutMs or 0
	o.lastSyncTimeMs = 0
	o.nextSyncMs = 0
	o.nextTimeoutMs = 0
	o.isRecordSyncPacket = isRecordSyncPacket
	if isRecordSyncPacket then
		o.unsyncRecords = {}
	end
	return o
end

local function getMsTimeNow()
	return GameNet.GetServerTime()
end

local evtList = nil
if EnvironmentHandler.isInServer then
	evtList = {
		[ModuleConstant.SERVER_SYNC_PLAYERINFO] = SyncEventInfo:create(ModuleConstant.SERVER_SYNC_PLAYERINFO, 1500, 1500, false),
	}
else
	evtList = {
		[ModuleConstant.JOYSTICK_MOVE] = SyncEventInfo:create(ModuleConstant.JOYSTICK_MOVE, 100, 750, false),
		[ModuleConstant.PLAYER_TIMING_SYNC] = SyncEventInfo:create(ModuleConstant.PLAYER_TIMING_SYNC, 1500, 1500, false),
	}
end

SyncFrequence = {
	isFightFinish		= nil,	-- 是否战斗结束
}

function SyncFrequence.onFightFinish()
	SyncFrequence.isFightFinish = true
end

function SyncFrequence.init()
	ModuleEvent.addListener(ModuleConstant.FIGHT_FINISH, SyncFrequence.onFightFinish)
end

-- 添加限制频率内发出同步包被截获后，到下一次允许发送的时间点时触发的函数
function SyncFrequence.addLimitCallBack(evtId, limitFunc)
	if evtList[evtId] == nil then return end
	evtList[evtId].limitFunc = limitFunc
end

-- 添加超时回调（指定时间内未发送evtId的同步包）
function SyncFrequence.addTimeoutCallBack(evtId, timeoutFunc)
	if evtList[evtId] == nil then return end
	evtList[evtId].timeoutFunc = timeoutFunc
end

function SyncFrequence.isCanSync(evtId, param)
	if evtList[evtId] == nil then return true end
	local evtInfo = evtList[evtId]
	local timeNow = getMsTimeNow()
	if evtInfo.lastSyncTimeMs == 0 or timeNow >= evtInfo.nextSyncMs then
		evtInfo.lastSyncTimeMs = timeNow
		evtInfo.nextSyncMs = timeNow + evtInfo.limitMs
		evtInfo.nextTimeoutMs = timeNow + evtInfo.timeoutMs
		evtInfo.needCallLimit = false
		return true
	end
	----GameLog("zxg", "can't sync too soon.................." .. tostring(evtId))
	evtInfo.needCallLimit = true
	if evtInfo.isRecordSyncPacket then
		table.insert(evtInfo.unsyncRecords, param)
	end
	return false
end

-- 客户端进入PVP战斗后逐帧更新
function SyncFrequence.update()
	if SyncFrequence.isFightFinish then return end
	local timeNow = getMsTimeNow()
	for _,evtInfo in pairs(evtList) do
		-- 上一次同步后evtInfo.timeoutMs毫秒内未进行同步，调用timeoutFunc让对应的监听者做处理
		if evtInfo.timeoutMs ~= 0 and timeNow >= evtInfo.nextTimeoutMs then
			evtInfo.nextTimeoutMs = timeNow + evtInfo.timeoutMs
			if evtInfo.timeoutFunc then
				evtInfo.timeoutFunc(evtInfo)
			end
		end
		-- 在限制时间内发出同步包被拦下后，当到达可同步时间时，调用limitFunc让对应监听者做处理
		if evtInfo.needCallLimit and timeNow >= evtInfo.nextSyncMs then
			evtInfo.needCallLimit = false
			if evtInfo.limitFunc then
				evtInfo.limitFunc(evtInfo)
			end
		end
	end
end

function SyncFrequence.clear()
	ModuleEvent.removeListener(ModuleConstant.FIGHT_FINISH, SyncFrequence.onFightFinish)
end