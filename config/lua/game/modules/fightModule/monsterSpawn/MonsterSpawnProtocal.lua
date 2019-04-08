--region MonsterSpawnProtocal.lua
--Date 2016/07/06
--Author zhouxiaogang
-- 刷怪协议
--endregion

MonsterSpawnProtocal = {}

local lastSendTime = nil

function MonsterSpawnProtocal.tellServerMonsterDead(deadDic, state)
	if deadDic == nil then
		return 
	end
	local count = 0
	for sn,v in pairs(deadDic) do 
		if v == state then count = count + 1 end
	end
	if count == 0 then return end
	local conn = GameNet.GetSocket()
	conn:WriteProtocol(PacketType.ServerMonsterDead)
	conn:WriteShort(count)
	local snList = {}
	for sn,v in pairs(deadDic) do
		if v == state then
			conn:WriteString(sn)
			table.insert(snList, sn)
		end
	end
	-- 发过的标记为状态1
	for k,sn in ipairs(snList) do
		deadDic[sn] = 1
	end
	-- 是否检测属性
	local currentTime = GameNet.GetServerTime()
	if lastSendTime == nil or (currentTime - lastSendTime > 5000) then
		lastSendTime = currentTime
		local mycharac = CharacterManager:getMyCharac()
	    if mycharac then
	    	conn:WriteShort(1)
	        conn:WriteSbyte(ATTR_COUNT)
	        for index,attribKey in ipairs(AttrEnum) do
	            conn:WriteShort(index-1)
	            conn:WriteInt(mycharac[attribKey])
	        end
       	else
       		conn:WriteShort(0)
	    end
	else
		conn:WriteShort(0)
	end
	conn:SendData()
end

function MonsterSpawnProtocal.recvConfirmMonsterDead()
	local conn = GameNet.GetSocket()
	local count = conn:ReadShort()
	if count == 0 then return end
	local confirmDeadList = {}
	for i = 1, count do
		table.insert(confirmDeadList, conn:ReadString())
	end
	MonsterSpawnManager.confirmMonsterDead(confirmDeadList)
end

function MonsterSpawnProtocal.tellServerRoleDead()
	local conn = GameNet.GetSocket()
	conn:WriteProtocol(PacketType.ServerRoleDead)
	conn:SendData()
end

function MonsterSpawnProtocal.confirmRecvSpawnInfo(spawnId)
	local conn = GameNet.GetSocket()
	conn:WriteProtocol(PacketType.ServerSpawnMonsterConfirm)
	local stageType = StageManager.getCurStageType() or 0
	conn:WriteSbyte(stageType)
	conn:WriteInt(spawnId)
	conn:SendData()
end


function MonsterSpawnProtocal.recvSpawnMonster()
	local conn = GameNet.GetSocket()
	local spawnWave = conn:ReadInt()
	local blockSize = conn:ReadShort()
	if blockSize > 0 then
		local blockStates = {}
		for i = 1, blockSize do
			local blockId = conn:ReadString()
			blockStates[blockId] = conn:ReadSbyte()
		end
		DynamicBlockManager:doStateChange(blockStates)
	end
	MonsterSpawnProtocal.confirmRecvSpawnInfo(spawnWave)
	local spawnSize = conn:ReadShort()
	if spawnSize > 0 then
		local spawnList = {}
		local spawnId = nil
		for i = 1, spawnSize do
			local spawnInfo = FighterVo:create()
			spawnInfo:read(conn)
			spawnInfo:parseData()
			if MonsterSpawnManager.isMonsterSpawned(spawnInfo.uniqueID) == false then
				table.insert(spawnList, spawnInfo)
			end
			if spawnInfo.characterType == CharacterConstant.TYPE_MONSTER then
				spawnId = spawnInfo.monsterSpawnId
			end
		end
		ModuleEvent.dispatch(ModuleConstant.UPDATE_TEAM_ENEMY_WAVE)
		MonsterSpawnManager:doSpawnMonsters(spawnList)
		if spawnId then
			MonsterSpawnManager.confirmSpawnArea(spawnId)
		end
	end
	local removeMonsterUidSize = conn:ReadShort();
	local tmpRemoveMonsterUid = nil;
	for i=1, removeMonsterUidSize do
		tmpRemoveMonsterUid = conn:ReadString();
		ModuleEvent.dispatch(ModuleConstant.CHARAC_REMOVE, tmpRemoveMonsterUid);
	end
end

function MonsterSpawnProtocal.triggerSpawnArea(spawnId)
	local conn = GameNet.GetSocket()
	conn:WriteProtocol(PacketType.ServerAreaSpawn)
    conn:WriteSbyte(StageManager.getCurStageType())
	conn:WriteInt(spawnId)
	conn:SendData()
--	CmdLog("客户端通知服务器玩家进入刷怪区域:" .. tostring(spawnId) .. debug.traceback())
end

function MonsterSpawnProtocal.register()
	GameNet.registerSend(PacketType.ServerMonsterDead, MonsterSpawnProtocal.tellServerMonsterDead)
	GameNet.registerRecv(PacketType.ClientMonsterSpawn, MonsterSpawnProtocal.recvSpawnMonster)
	GameNet.registerSend(PacketType.ServerAreaSpawn, MonsterSpawnProtocal.triggerSpawnArea)
	GameNet.registerSend(PacketType.ServerRoleDead, MonsterSpawnProtocal.tellServerRoleDead)
	GameNet.registerRecv(PacketType.ClientMonsterDeadConfirm, MonsterSpawnProtocal.recvConfirmMonsterDead)
end

function MonsterSpawnProtocal.debugLastSendTime()
	lastSendTime = math.huge
end