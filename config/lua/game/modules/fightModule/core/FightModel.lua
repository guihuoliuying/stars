-- region
-- Date    : 2016-06-14
-- Author  : daiyaorong
-- Description :  战斗数据模块
-- endregion
FightModel = {}

--数据模块名
local fightData = nil
local dataModelName = ConstantData.MODULE_NAME_FIGHT
local base = FightDefine.ASSEMBLEID_BASE
local limit = base + FightDefine.ASSEMBLEID_MAX
local assembleSkillIndex = base
local dungeonStageId = nil
local fightStageId = nil
local fightStageType = nil
local rebornTime = nil
local limitTime = nil   --副本限制时间
local readyTime = nil   --pvp准备时间
local autoFlag = nil
--lyt 20160729 是否播放剧情 1=是;0=否
local isPlayDrama=nil
--end
local totalDamageValue=0--造成的总伤害
local totalMonsterNum=0--关卡中的怪物总数（只有组队才发此数据）
local damageValueDic={}--记录每个玩家的伤害
local pvpFightTime=0 -- PVP时间
local recvDataTime = 0 -- 记录收到进战斗数据的时间
-- local familyWarEliteStartTime=0--家族精英战开战时间
local fightStartTime=0--pvp战斗开始时间
local isRecvFightData = false  --是否收到战斗数据
local isAttacker = nil 			--是否为发起战斗者 0否 1是
local characDamageDic = {}  --角色伤害记录
local pvpRestSkillCD = nil	-- pvp主角在服务端的技能释放时间记录，用于计算剩余CD，只在进入时下发
local dungeonIntegral = 0   --副本积分 


-- 接收怪物数据
local function receiveMonster(conn)
	local monsterSet = {}
	local size = conn:ReadShort()
	--CmdLog("-----------怪物数据size:"..size)

	local monster = nil
	for i = 1, size do
		monster = MonsterVo:create()
		monster:read(conn)
		monsterSet[monster.monsterid] = monster
	end
	FightModel:getFightData():setData(FightDefine.DATA_MONSTER, monsterSet)
end

-- 接收动态阻挡数据
local function receiveDynamicBlock(conn)
	local blockSet = {}
	local size = conn:ReadShort()
	--CmdLog("-----------dynamic block size:"..size)
	-- GameLog("lyt","----------接收动态阻挡数据-------------:",size)
	for i = 1, size do
		local block = DynamicBlockVo:create()
		block:read(conn)
		blockSet[block.blockid] = block
	end
	FightModel:getFightData():setData(FightDefine.DATA_DYNAMIC_BLOCK, blockSet)
end

-- 接收区域刷怪数据
local function receiveMonsterSpawnArea(conn)
	local spawnAreaSet = {}
	local size = conn:ReadShort()
	-- CmdLog("-----------区域刷怪数据size:"..size)
	for i = 1, size do
		local area = SpawnAreaVo:create()
		area:read(conn)
		spawnAreaSet[area.spawnid] = area
	end
	FightModel:getFightData():setData(FightDefine.DATA_MONSTER_SPAWN_AREA, spawnAreaSet)
end

local function receiveDrop(conn)
	local size = conn:ReadSbyte()
	local dropSet = {}
	for i = 1, size do		
		table.insert(dropSet, conn:ReadInt())
		--print("dropId:" , dropSet[i])
	end
	FightModel:getFightData():setData(FightDefine.DATA_DROP, dropSet)
end

--解析当前副本的刷怪数据(对应每一波的类型和数量)
--注意：目前只有镇妖塔会下发这个数据, 其他的副本size==0;
local function receiveMonsterTypeCountInfo(conn)
	local size = conn:ReadShort()
	local tmpStr = nil;
	local tmpTypeCount = nil;
	local tmpItemArr = nil;
	local waveDic = {};
	local itemWave = nil;
	for i=1,size do
		itemWave = {};
		tmpStr = conn:ReadString();
		tmpItemArr = StringUtils.split(tmpStr, ",");
		for k=1,#tmpItemArr do
			tmpTypeCount = StringUtils.split(tmpItemArr[k], "+");
			itemWave[2*k-1] = tonumber(tmpTypeCount[1]);
			itemWave[2*k] = tonumber(tmpTypeCount[2]);
		end
		table.insert(waveDic, itemWave);
	end
	FightModel:getFightData():setData(FightDefine.DATA_MONSTERWAVETYPECOUNT, waveDic)
end

--副本限制时间
local function receiveLimitTime( conn )
	limitTime = conn:ReadInt() * 0.001
end 

----经验副本里面是否显示打怪进度 
local function receiveisProDungeonShowTower( conn )
	local x = conn:ReadInt()
	if x==1 then
		FightModel:getFightData():setData(FightDefine.DATA_ISPRODUNGEONSHOWTOWER, true)
	else
        FightModel:getFightData():setData(FightDefine.DATA_ISPRODUNGEONSHOWTOWER, false)
	end
end

local function receiveBornBuff( conn )
	local count=conn:ReadSbyte()
	--GameLog("lyt","--------------receiveBornBuff:",count)
	local buffList={}
	for i=1,count do
		local buffData={}
		buffData.buffId=conn:ReadInt()
		buffData.buffLevel=conn:ReadInt()
		buffList[#buffList+1]=buffData
	end
	FightModel:getFightData():setData(FightDefine.DATA_BORN_BUFF , buffList)
end

local function receiveFighter( conn )
	-- body
	local fighterSet = {}
	local fighter = nil
	local size = conn:ReadSbyte()

	--CmdLog("-------------战斗实体数据size:" .. size)
	for index = 1, size do
		fighter = FighterVo:create()
		fighter:read( conn )
		fighterSet[fighter.uniqueID] = fighter
	end
	FightModel:getFightData():setData( FightDefine.DATA_FIGHTER, fighterSet )
end

local function receiveRandomSeed( conn )
	-- body
	local seed = conn:ReadInt()
	-- print("随机种子="..seed)
	math.SetRandomSeed( seed )
end

local function parseSkillBuff( set, data, level )
	if data.buffIdList then
		for k,v in ipairs(data.buffIdList) do
			if v ~= 0 then
				if set[v] == nil then
					set[v] = {}
				end
				set[v][level] = level
				local buffer = FightModel:getBuffVo( v, level )
				if buffer and buffer.buffInfo and buffer.buffInfo.buffIdList then
					parseSkillBuff(set,buffer.buffInfo,level)
				end
			end
		end
	end
end

local function parseSkillLvBuff( set, data, level )
	if data.skillType == 1 or data.skillType == 3 then
    	if data.effectinfo and data.effectinfo["type"] == FightDefine.PASSEFF_BUFF then
    		if set[data.effectinfo["param"][2]] == nil then
    			set[data.effectinfo["param"][2]] = {}
			end
    		set[data.effectinfo["param"][2]][level] = level
    		local buffer = FightModel:getBuffVo( data.effectinfo["param"][2], level )
			if buffer and buffer.buffInfo and buffer.buffInfo.buffIdList then
				parseSkillBuff(set,buffer.buffInfo,level)
			end
		end
	end
end

local function parseSkillAndBuff( skillSet, buffSet, id, level )
	skillSet[id] = 0 --boss标记 0为否 1为是 默认非
	local vo = FightModel:getSkillData( id )
	parseSkillBuff(buffSet, vo, level)
	vo = FightModel:getSkillLevelData( id, level )
    parseSkillLvBuff(buffSet, vo, level)
end

local function parseFighter()
	if EnvironmentHandler.isInServer then
		return
	end
	local skillSet = {}
	local buffSet = {}
	local fighterSet = FightModel:getFightData():getData(FightDefine.DATA_FIGHTER)
	if fighterSet then --筛选玩家技能
		for k, fighter in pairs(fighterSet) do
			for key, value in pairs( fighter.allskill ) do
				parseSkillAndBuff(skillSet, buffSet, key, value.level)
			end
		end
	end
	if (EnvironmentHandler.isPvpClient
		and fightStageType ~= ConstantData.STAGE_TYPE_PK) or fightStageType == ConstantData.STAGE_TYPE_LOOTTREASURE_PVE
		or fightStageType == ConstantData.STAGE_TYPE_FAMILY_TRANSPORT_PVP then --同步模式时加载所有职业的技能
		local allJobs = CFG.job:getAll();
		if(allJobs)then
			local idlist = nil;
			for i=1, #allJobs do
				if allJobs[i].passskill ~= "0" then
					idlist = StringUtils.split(allJobs[i].passskill, "+", nil, tonumber)
					for k,v in ipairs(idlist) do
						parseSkillAndBuff(skillSet, buffSet, v, 1)
					end
				end
			end
		end
		local allResource = CFG.resource:getAll()
		if allResource then
			local idlist = nil
			local tempList = nil
			for i=1, #allResource do
				idlist = StringUtils.split(allResource[i].normalskill, "+", nil, tonumber)
				for k,v in ipairs(idlist) do
					parseSkillAndBuff(skillSet, buffSet, v, 1)
				end
				idlist = StringUtils.split(allResource[i].skill, "|")
				for k,v in ipairs(idlist) do
					tempList = StringUtils.split(v, "+", nil, tonumber)
					for index,id in ipairs(tempList) do
						parseSkillAndBuff(skillSet, buffSet, id, 1)
					end
				end
			end
		end
	end
	fighterSet = FightModel:getFightData():getData(FightDefine.DATA_MONSTER)
	if fighterSet then	-- 筛选怪物与伙伴技能
		local skillids = nil
		local skillList = nil
		for k, fighter in pairs(fighterSet) do
			if fighter.skillIdList then
				for _, id in ipairs(fighter.skillIdList) do
					parseSkillAndBuff(skillSet, buffSet, id, 1)
					if fighter.monsterType == CharacterConstant.MONSTER_TYPE.BOSS then
						skillSet[id] = 1 --调整boss标记
					end
				end
			end
		end
	end
	--预加载剧情技能,buff数据
	fighterSet = StoryController.GetDramaSkillList(fightStageId)
	if fighterSet then
		for k,v in pairs(fighterSet) do
			parseSkillAndBuff(skillSet,buffSet,v,1)
		end
	end
	FightModel:getFightData():setData( FightDefine.DATA_SKILL, skillSet )
	FightModel:getFightData():setData( FightDefine.DATA_BUFF, buffSet )
end

local function initFighter()
	-- body
	local fighterSet = fightData:getData(FightDefine.DATA_FIGHTER)
	local playerSet = {}
	local initMonsterSet = {}
	local partnerSet = {}
	local campBuff = {}
	for key, fighter in pairs( fighterSet ) do
		fighter:parseData()
		if fighter.characterType == CharacterConstant.TYPE_SELF or fighter.characterType == CharacterConstant.TYPE_PLAYER then
			playerSet[fighter.uniqueID] = fighter
		elseif fighter.characterType == CharacterConstant.TYPE_MONSTER then
			initMonsterSet[fighter.uniqueID] = fighter
		elseif fighter.characterType == CharacterConstant.TYPE_PARTNER then
			fighter.id = fighter.uniqueID
			partnerSet[fighter.uniqueID] = fighter
		end
	end
	FightModel:getFightData():setData( FightDefine.DATA_PLAYER, playerSet )
	FightModel:getFightData():setData( FightDefine.DATA_INIT_MONSTERS, initMonsterSet )
	FightModel:getFightData():setData( FightDefine.DATA_PARTNER, partnerSet )
	FightModel:getFightData():setData( FightDefine.DATA_CAMP_BUFF, campBuff )
	parseFighter()
end

local function receiveDungeon( conn )
	-- body
	receiveMonsterSpawnArea( conn )
	receiveDrop( conn )
	autoFlag = conn:ReadSbyte()
	receiveLimitTime(conn)
end

local function receiveSkyTower( conn )
	-- body
	receiveDungeon( conn )
	receiveMonsterTypeCountInfo( conn )
end

local function receiveProDungeon( conn )
	-- body
	ModuleEvent.dispatch(ModuleConstant.BUSYINDICATE_STOP, ModuleConstant.PRODUNGEON_WINDOW)
	receiveDungeon(conn)
	receiveLimitTime( conn )
	receiveMonsterTypeCountInfo( conn )
    receiveisProDungeonShowTower(conn)
end 

local function receiveTinyGame(conn)
	receiveMonsterSpawnArea( conn )
	receiveDrop( conn )
end

local function receiveCallBoss(conn)
	-- receiveMonsterSpawnArea( conn )
end
local function receivePk( conn )
    limitTime = conn:ReadInt()
    readyTime = conn:ReadInt()
    -- GameLog("lyt","----------------readyTime-------------:",readyTime)
    fightStartTime = readyTime+os.time()
	if EnvironmentHandler.isInServer == false then
		EnvironmentHandler.setEnv( EnvironmentDef.PVP_CLIENT )
	end
end

local function receiveTeamDungeon( conn )
    receiveDungeon( conn )
    totalMonsterNum = conn:ReadShort()
    EnvironmentHandler.setEnv( EnvironmentDef.PVE_WEAKSYNC )
end

local function receiveInvadeDungeon( conn )
	-- body
	receiveDungeon(conn)
	EnvironmentHandler.setEnv( EnvironmentDef.PVE_WEAKSYNC )
end

local function receiveLootTreasureWait(conn)
	-- local remainMonsterHp = conn:ReadInt();
	-- local minusHpPerSecond = conn:ReadInt();
	local remainMonsterHp = conn:ReadInt();
	local minusHpPerSecond = conn:ReadInt();
	local strArr = StringUtils.split(conn:ReadString(), "+");
	local monsterPos = Vector3.New(tonumber(strArr[1]), tonumber(strArr[2]), tonumber(strArr[3]));
	local timestamp = tonumber(conn:ReadString());
	LootTreasureCtrl.setPveMonsterHpInfo(remainMonsterHp, minusHpPerSecond, monsterPos, timestamp);
end

local function receiveLootTreasurePve(conn)
	local remainMonsterHp = conn:ReadInt();
	local minusHpPerSecond = conn:ReadInt();
	local strArr = StringUtils.split(conn:ReadString(), "+");
	local monsterPos = Vector3.New(tonumber(strArr[1]), tonumber(strArr[2]), tonumber(strArr[3]));
	local timestamp = tonumber(conn:ReadString());
	LootTreasureCtrl.setPveMonsterHpInfo(remainMonsterHp, minusHpPerSecond, monsterPos, timestamp);
end

local function receiveLootTreasurePvp(conn)
	if EnvironmentHandler.isInServer == false then
		EnvironmentHandler.setEnv( EnvironmentDef.PVP_CLIENT )
	end
end

local function receiveExpedition(conn)
	-- receiveMonsterSpawnArea(conn)
	receiveDungeon(conn)
end

local function receiveOfflinePVP(conn)
	-- receiveMonsterSpawnArea( conn )
	-- receiveDrop( conn )
	receiveLimitTime( conn )
	autoFlag = conn:ReadSbyte()
	receiveBornBuff( conn )--接收出生buff
end



local function receiveFamilyWarElite(conn)
	limitTime = conn:ReadInt()
	if EnvironmentHandler.isInServer == false then
		EnvironmentHandler.setEnv( EnvironmentDef.PVP_CLIENT )
	end
	local remainderTime=conn:ReadInt()
	-- familyWarEliteStartTime=GameNet.GetServerTime() * 0.001+remainderTime
	-- familyWarEliteStartTime=os.time()+remainderTime
	fightStartTime=os.time()+remainderTime
end

local function receiveFamilyWarNormal(conn)
	receivePk( conn )
end

local function recvDaily5v5PVP(conn)
	limitTime = conn:ReadInt()
	if EnvironmentHandler.isInServer == false then
		EnvironmentHandler.setEnv( EnvironmentDef.PVP_CLIENT )
	end
	local remainderTime=conn:ReadInt()
	fightStartTime=os.time()+remainderTime
end

local function receiveCargoRobot(conn)
	receiveDungeon(conn)
	-- nothing to read
end

local function receiveDoublePVP(conn)
	receivePk( conn )
end

local function receivePoem(conn)
	receiveDungeon(conn)
end

local function receiveFamilyWarStage(conn)
	limitTime=conn:ReadInt()
	if EnvironmentHandler.isInServer == false then
		EnvironmentHandler.setEnv( EnvironmentDef.PVP_CLIENT )
	end
end
--家族探宝
local function receiveFamilyAdventureDaily(conn)
	receiveLimitTime( conn )
end
local function receiveFamilyAdventureFinal(conn)
	--receiveLimitTime( conn )
end
--精英副本
local function receiveEliteDungeon(conn)
	receiveDungeon( conn )
    totalMonsterNum = conn:ReadShort()
    EnvironmentHandler.setEnv( EnvironmentDef.PVE_WEAKSYNC )
end

--荣誉竞技场
local function receiveOfflineMatch(conn)
	receiveLimitTime( conn )
	autoFlag = conn:ReadSbyte()
end 

--家族任务副本
local function receiveFamilyTaskDungeon(conn)
end

--家族钾镖战斗副本
local function receiveFamilyTransportPVP(conn)
	receiveLimitTime( conn )
	-- if EnvironmentHandler.isInServer == false then
	-- 	EnvironmentHandler.setEnv( EnvironmentDef.PVP_CLIENT )
	-- end
	local countdownofbegin = conn:ReadInt() 	--没用到
	isAttacker = conn:ReadSbyte() --是否是战斗发起者
end 

local function receiveCoupleDeungeon(conn)

	receiveMonsterSpawnArea( conn )
	receiveDrop( conn )
	autoFlag = conn:ReadSbyte()
	receiveLimitTime(conn)
    dungeonIntegral = conn:ReadInt() -- 过关积分
    -- MarryDungeonControl.setDungeonBegin(true)--进入夫妻副本,设置返回主界面时再次打开标记
    EnvironmentHandler.setEnv( EnvironmentDef.PVE_WEAKSYNC )
end

--组队诗歌副本
local function receiveTeamPoemDungeon(conn)
	receiveDungeon( conn )
	-- EnvironmentHandler.setEnv( EnvironmentDef.PVE_WEAKSYNC )
end

local function receiveBuddyDungeon(conn)
	receiveMonsterSpawnArea( conn )
	receiveDrop( conn )
	autoFlag = conn:ReadSbyte()
	receiveLimitTime(conn)
end

local function receiveTokenDungeon(conn)
	receiveDungeon(conn)
	local buffId = conn:ReadInt()
	local addLayer = conn:ReadInt()
	TokenDungeonControl.setMultiFightBuff(buffId, addLayer)
end

local function receiveCampDaily(conn)
	if EnvironmentHandler.isInServer == false then
		EnvironmentHandler.setEnv( EnvironmentDef.PVP_CLIENT )
	end
--[[	
	local remainderTime=3
	fightStartTime=os.time()+remainderTime

    limitTime = conn:ReadInt()
    readyTime = conn:ReadInt()
    -- GameLog("lyt","----------------readyTime-------------:",readyTime)
    fightStartTime = readyTime+os.time()
	if EnvironmentHandler.isInServer == false then
		EnvironmentHandler.setEnv( EnvironmentDef.PVP_CLIENT )
	end
    ]]
end

--弑神副本 
local function recviveCampKillGod(conn)
	receiveLimitTime(conn)
end

-- 根据场景类型接收
local RECEIVE_MAP = {
	[ConstantData.STAGE_TYPE_DUNGEON] = receiveDungeon,
	[ConstantData.STAGE_TYPE_SKYTOWER] = receiveSkyTower,
	[ConstantData.STAGE_TYPE_PRODUCEDUNGEON] = receiveProDungeon,
	[ConstantData.STAGE_TYPE_GAMECAVE] = receiveTinyGame;
	[ConstantData.STAGE_TYPE_CALLBOSS] = receiveCallBoss,
	[ConstantData.STAGE_TYPE_PK] = receivePk,	
	[ConstantData.STAGE_TYPE_SEARCHTREASURE] = receiveDungeon,
	[ConstantData.STAGE_TYPE_TEAM] = receiveTeamDungeon,
	[ConstantData.STAGE_TYPE_LOOTTREASURE_WAIT] = receiveLootTreasureWait,
	[ConstantData.STAGE_TYPE_LOOTTREASURE_PVE] = receiveLootTreasurePve,
	[ConstantData.STAGE_TYPE_LOOTTREASURE_PVP] = receiveLootTreasurePvp,
	[ConstantData.STAGE_TYPE_FAMILYEXPED] = receiveExpedition,	
	[ConstantData.STAGE_TYPE_OFFLINEPVP] = receiveOfflinePVP,
	[ConstantData.STAGE_TYPE_FAMILY_INVADE] = receiveInvadeDungeon,
	[ConstantData.STAGE_TYPE_BRAVE_TRIAL] = receiveDungeon,
	[ConstantData.STAGE_TYPE_NEWGUIDE] = receiveDungeon,
	[ConstantData.STAGE_TYPE_FIGHTINGMASTER] = receivePk,
	[ConstantData.STAGE_TYPE_ROYALREWARD] = receiveDungeon,
	[ConstantData.STAGE_TYPE_FAMILY_WAR_ELITE_FIGHT] = receiveFamilyWarElite,
	[ConstantData.STAGE_TYPE_CARGO_PVP] = receivePk,
	[ConstantData.STAGE_TYPE_CARGO_ROBOT] = receiveCargoRobot,
	[ConstantData.STAGE_TYPE_FAMILY_WAR_NORMAL_FIGHT] = receiveFamilyWarNormal,
	[ConstantData.STAGE_TYPE_TEAM_PVP] = receiveDoublePVP,
	[ConstantData.STAGE_TYPE_POEM] = receivePoem,
	[ConstantData.STAGE_TYPE_TEAM_POEM] = receiveTeamPoemDungeon,
	[ConstantData.STAGE_TYPE_FAMILY_WAR_STAGE_FIGHT] = receiveFamilyWarStage,
	[ConstantData.STAGE_TYPE_ADVENTURE_DAILY] = receiveFamilyAdventureDaily,
	[ConstantData.STAGE_TYPE_ADVENTURE_FINAL] = receiveFamilyAdventureFinal,
	[ConstantData.STAGE_TYPE_OFFLINEMATCH] = receiveOfflineMatch,
	[ConstantData.STAGE_TYPE_FAMILYTASK] = receiveFamilyTaskDungeon,
	[ConstantData.STAGE_TYPE_ELITE] = receiveEliteDungeon,
	[ConstantData.STAGE_TYPE_FAMILY_TRANSPORT_PVP] = receiveFamilyTransportPVP,
	[ConstantData.STAGE_TYPE_DAILY5V5_PVP] = recvDaily5v5PVP,
    [ConstantData.STAGE_TYPE_COUPLE_DUNGEON] = receiveCoupleDeungeon,
    [ConstantData.STAGE_TYPE_BUDDY_DUNGEON] = receiveBuddyDungeon,
	[ConstantData.STAGE_TYPE_BENEFIT_TOKEN] = receiveBuddyDungeon,
	[ConstantData.STAGE_TYPE_TOKEN_DUNGEON] = receiveTokenDungeon,
	[ConstantData.STAGE_TYPE_CAMP_QICHU] = receiveEliteDungeon,
	[ConstantData.STAGE_TYPE_CAMP_TASK] = receiveBuddyDungeon,
    [ConstantData.STAGE_TYPE_CAMP_DAILY] = receiveCampDaily,
    [ConstantData.STAGE_TYPE_CAMP_KILLGOD] = recviveCampKillGod,
}

-- 战斗初始化时的默认状态，没在这里配置的默认为FightDefine.FIGHT_STATE_FIGHTING，一进去就开战，没有准备过程（不用等待所有玩家准备好）
local DEFAULT_FIGHT_STATE = 
{
	[ConstantData.STAGE_TYPE_FIGHTINGMASTER] = FightDefine.FIGHT_STATE_READY,
	[ConstantData.STAGE_TYPE_FAMILY_WAR_ELITE_FIGHT] = FightDefine.FIGHT_STATE_READY,
	[ConstantData.STAGE_TYPE_FAMILY_WAR_NORMAL_FIGHT] = FightDefine.FIGHT_STATE_READY,	
	[ConstantData.STAGE_TYPE_TEAM_PVP] = FightDefine.FIGHT_STATE_READY,
	[ConstantData.STAGE_TYPE_FAMILY_TRANSPORT_PVP] = FightDefine.FIGHT_STATE_READY,
	-- [ConstantData.STAGE_TYPE_ELITE] = FightDefine.FIGHT_STATE_READY,
	--[ConstantData.STAGE_TYPE_PK] = FightDefine.FIGHT_STATE_READY,	
	[ConstantData.STAGE_TYPE_DAILY5V5_PVP] = FightDefine.FIGHT_STATE_READY,
	[ConstantData.STAGE_TYPE_CAMP_DAILY] = FightDefine.FIGHT_STATE_READY,
}

local function receiveData()
	-- body
	FightModel.RemoveBackMainStageTimer()
	recvDataTime = GameNet.GetServerTime()
	readyTime = nil
	if EnvironmentHandler.isInServer == false then
		EnvironmentHandler.setEnv( EnvironmentDef.DEFAULT )
	end
	local conn     = GameNet.GetSocket()
	local isAgain  = conn:ReadSbyte()
	-- isPlayDrama    = conn:ReadSbyte()
	fightStageType = conn:ReadSbyte()
	if fightStageType==ConstantData.STAGE_TYPE_DUNGEON then
		dungeonStageId=conn:ReadInt()
	end
	fightStageId   = conn:ReadInt()
    rebornTime     = conn:ReadInt()
	-- 基础数据
	receiveFighter( conn )
	receiveDynamicBlock( conn )
	--首次进入才接收
	if isAgain == 0 then
		receiveMonster( conn )
	end
	receiveRandomSeed( conn )
	-- 根据场景继续接收
	RECEIVE_MAP[fightStageType]( conn )
	initFighter()

	isRecvFightData = true  --标记已经收到战斗数据了

	characDamageDic = nil   --战斗开始前清空角色伤害统计表

	assembleSkillIndex = base
    -- 设置是否使用玩家等待及倒计时锁屏
	local defaultFightState = DEFAULT_FIGHT_STATE[fightStageType] or FightDefine.FIGHT_STATE_FIGHTING

	FightControlFactory.getControl().setFightState(defaultFightState)
	FightControlFactory.getControl().onDataReady()
	if EnvironmentHandler.isInServer 
		and (defaultFightState == FightDefine.FIGHT_STATE_FIGHTING or (readyTime and readyTime > 0)) then
		FightControlFactory.getControl().setFightTimeInfo(limitTime, readyTime or 0)
	end

end

--接受奖励数据
local function recvAwardResult(conn)
	local size = conn:ReadShort()
	local awards = nil
	if size > 0 then
		awards = {}
		for i = 1,size do
			local award = {}
			award.itemId = conn:ReadInt()
			award.count = conn:ReadInt()
			table.insert(awards, award)
		end
	end
	return awards
end
--接收精英副本的奖励数据
local function recvAwardElite(conn)
	local size = conn:ReadShort()
	local awards = nil
	local firstAwards = nil
	if size > 0 then
		awards = {}
		firstAwards = {}
		for i = 1,size do
			local award = {}
			award.itemId = conn:ReadInt()
			award.count = conn:ReadInt()
			award.isFirstAward = (conn:ReadSbyte() == 1)
			if award.isFirstAward == true then
				table.insert(firstAwards, award)
			else
				table.insert(awards, award)
			end
		end
	end
	local awardList = {}
	if firstAwards and #firstAwards > 0 then
		BagModel.sortTools(firstAwards)
		for i = 1,#firstAwards do
			table.insert(awardList,firstAwards[i])
		end
	end
	if awards and #awards > 0 then
		BagModel.sortTools(awards)
		for i = 1,#awards do
			table.insert(awardList,awards[i])
		end
	end
	return awardList
end


--防止各种情况下，导致的后端发送了结算但是被卡死的情况
local function BackMainStage()
	if StageManager.isInFightStage() then
		if StoryController.GetIsPlayingStory() then
			FightModel.AddBackMainStageTimer(10)
			return
		end
	    CommonTips.showMsgBox( CFG.gametext:getFormatText("stagebattle_quittips"), CFG.gametext:getFormatText("common_desc_tips"),MessageBox.BUTTON.YES,nil,function (result)
    	    TransferProtocol.sendTransfer("")
        end)
    end
end

local removeBackMainStageTimer=nil
function FightModel.RemoveBackMainStageTimer()
	if removeBackMainStageTimer then
        CommonFunc.removeFrameTimer(ConstantData.FRAME_EVENT_UI,removeBackMainStageTimer)
        removeBackMainStageTimer = nil
    end
end

function FightModel.AddBackMainStageTimer(time)
	FightModel.RemoveBackMainStageTimer()
	removeBackMainStageTimer=CommonFunc.addUIFrameTimer(ConstantData.FRAME_RATE*time,1,BackMainStage)
end



local function receiveFightResult()
	local conn = GameNet.GetSocket()
	local resultSet = {}
  	FightModel.AddBackMainStageTimer(10) 
	resultSet.stageType = conn:ReadSbyte();


	resultSet.result = conn:ReadSbyte()
	resultSet.isWin = (resultSet.result == 2)
	isRecvFightData = false
    -- 当关卡结束时pvp时
    FightControlFactory.getControl().stopFight(resultSet.isWin)


    if resultSet.stageType == ConstantData.STAGE_TYPE_PK then
        ModuleEvent.dispatch(ModuleConstant.ONLINEPVP_RESULT,resultSet)
        return
    end
	resultSet.star = conn:ReadSbyte()
	resultSet.passTime = conn:ReadInt()		-- 单位: 秒
	if resultSet.stageType == ConstantData.STAGE_TYPE_ELITE then
		resultSet.awards = recvAwardElite(conn)
	else
		resultSet.awards = recvAwardResult(conn)
		if resultSet.awards and #resultSet.awards > 0 then
			BagModel.sortTools(resultSet.awards)
		end
	end
	if resultSet.stageType == ConstantData.STAGE_TYPE_CARGO_PVP then
		resultSet.isWin = (resultSet.result == CargoConstant.CARGO_RESULT_ESCORT_FINISH_WIN or resultSet.result == CargoConstant.CARGO_RESULT_ESCORT_WIN or resultSet.result == CargoConstant.CARGO_RESULT_ROB_WIN)
    end
	if resultSet.stageType == ConstantData.STAGE_TYPE_CALLBOSS then   --召唤boss的数据
		resultSet.damage = conn:ReadInt()
	elseif resultSet.stageType == ConstantData.STAGE_TYPE_FIGHTINGMASTER then
        resultSet.curDisScore = conn:ReadInt() --当前积分
        resultSet.changeScore = conn:ReadInt() --变化的积分
        resultSet.isAwards = true
        if FightingMasterModel.getPlayerData() then
            local fightTime = FightingMasterModel.getPlayerData().fightTimes + 1
            if fightTime > FightingMasterModel.getFightAwardsTimes() then
                resultSet.isAwards = false
            end
        end
        ModuleEvent.dispatch(ModuleConstant.ONLINEPVP_RESULT,resultSet)
        return  
	elseif resultSet.stageType == ConstantData.STAGE_TYPE_CARGO_PVP then
		resultSet.isDoubleAward =  conn:ReadSbyte()
		if resultSet.result == CargoConstant.CARGO_RESULT_ESCORT_FAILED then
			resultSet.cargoLost = conn:ReadSbyte()
		elseif resultSet.result == CargoConstant.CARGO_RESULT_ESCORT_FINISH_WIN then
			resultSet.cargoRobTimes = conn:ReadSbyte()
			resultSet.cargoLost = conn:ReadSbyte()
		end
	elseif resultSet.stageType == ConstantData.STAGE_TYPE_TEAM then  --组队副本的奖励加成百分比
		resultSet.addPercentType = conn:ReadString()
	elseif resultSet.stageType == ConstantData.STAGE_TYPE_ADVENTURE_DAILY then
		resultSet.damage = conn:ReadLong()
		resultSet.totalDamage = conn:ReadLong()
		resultSet.extAwards = nil
		local extAwardSize = conn:ReadSbyte()
		if extAwardSize > 0 then
			resultSet.extAwards = {}
			for i=1,extAwardSize do
				local award = {}
				award.itemId = conn:ReadInt()
				award.count = conn:ReadInt()
				table.insert(resultSet.extAwards, award)
			end
		end
	elseif resultSet.stageType == ConstantData.STAGE_TYPE_OFFLINEMATCH then
		resultSet.curRank = conn:ReadInt() 	--当前排名
		resultSet.upRank = conn:ReadInt() 	--提升排名 排名不变时为0	
	elseif resultSet.stageType == ConstantData.STAGE_TYPE_FAMILY_TRANSPORT_PVP then
		resultSet.isHasCar = conn:ReadSbyte()
	elseif resultSet.stageType == ConstantData.STAGE_TYPE_COUPLE_DUNGEON then
		resultSet.mySocre=conn:ReadInt()
		resultSet.marrySocre=conn:ReadInt()
		local extAwardSize = conn:ReadSbyte()
		if extAwardSize>0 then
			resultSet.extAwards={}
			for i=1,extAwardSize do
				local awardVo={
					itemId=conn:ReadInt(),
					count=conn:ReadInt(),
				}
				table.insert(resultSet.extAwards, awardVo)
			end
		end
    end

	--lyt 20160730 战斗胜利，且剧情系统正在等待战斗胜利
	if resultSet.isWin and StoryController.IsWaitFightFinish()==true then
		-- 通知剧情系统战斗结束，开启通关剧情 剧情加10秒的延时返回主城
		FightModel.AddBackMainStageTimer(20)
		ModuleEvent.dispatch(ModuleConstant.FIGHT_FINISH_TO_STORY,resultSet)
		return
	end
	ModuleEvent.dispatch(ModuleConstant.FIGHT_FINISH, resultSet)
	-- CmdLog("======================战斗结束:isWin=" .. tostring(resultSet.isWin) .. ", star=" .. tostring(resultSet.star) .. ", passtime=" .. tostring(resultSet.passTime))
	--战斗结果为产出副本时，显示产出副本结束界面，否则显示关卡结束界面
	if resultSet.stageType == ConstantData.STAGE_TYPE_PRODUCEDUNGEON then	
		local proDungeonInfo = ProDungeonCtrl.getProDungeonInfo()
		if proDungeonInfo.type == ProDungeonDef.PRODUCE_TYPE_EXP then
			ModuleEvent.dispatch(ModuleConstant.PRODUNGEON_WINDOW, {ModuleConstant.UI_OPEN, {ProDungeonDef.WIN_RESULT, resultSet}})
		elseif proDungeonInfo.type >= ProDungeonDef.PRODUCE_TYPE_ORE then		
			resultSet.delayTime = 1 --产出副本延时时间作特殊处理
			ModuleEvent.dispatch(ModuleConstant.PRODUNGEON_FIGHT_RESULT, resultSet)
			ModuleEvent.dispatch(ModuleConstant.STAGE_RESULT_WINDOW, {ModuleConstant.UI_OPEN, resultSet})
		end
	elseif resultSet.stageType == ConstantData.STAGE_TYPE_LOOTTREASURE_PVE then
		resultSet.boxCount = conn:ReadInt() --宝箱数量
		ModuleEvent.dispatch(ModuleConstant.VICTORY_WINDOW,{ModuleConstant.UI_OPEN,resultSet})
	elseif resultSet.stageType == ConstantData.STAGE_TYPE_LOOTTREASURE_PVP then
		ModuleEvent.dispatch(ModuleConstant.VICTORY_WINDOW,{ModuleConstant.UI_OPEN,resultSet})
	elseif resultSet.stageType == ConstantData.STAGE_TYPE_CALLBOSS then
		ModuleEvent.dispatch(ModuleConstant.CALL_BOSS_RESULT,resultSet)
	elseif resultSet.stageType == ConstantData.STAGE_TYPE_TEAM_PVP then
		--[[
		local isScoreStep = conn:ReadSbyte()
		if isScoreStep == 1 then
			local scoreResult = {}
			scoreResult[1] = {} 									--胜利队伍
			scoreResult[1].teamId = conn:ReadInt() 
			scoreResult[1].winScore = conn:ReadInt()             	--胜负积分
			scoreResult[1].killScore = conn:ReadInt() 				--击杀积分
			scoreResult[1].successiveScore = conn:ReadInt() 		--连胜积分
			scoreResult[1].totalScore = conn:ReadInt() 				--总积分
			scoreResult[2] = {} 									--失败队伍
			scoreResult[2].teamId = conn:ReadInt()
			scoreResult[2].winScore = conn:ReadInt()
			scoreResult[2].killScore = conn:ReadInt()
			scoreResult[2].successiveScore = conn:ReadInt()
			scoreResult[2].totalScore = conn:ReadInt()
			resultSet.scoreResult = scoreResult
			print("reslut:" , tableplus.tostring(scoreResult , true))
		end

		ModuleEvent.dispatch(ModuleConstant.DOUBLEPVP_RESULT_WINDOW, {ModuleConstant.UI_OPEN, resultSet})
	    --]]	
	elseif(StageManager.getCurStageType() == ConstantData.STAGE_TYPE_POEM and resultSet.isWin)then
		ModuleEvent.dispatch(ModuleConstant.STAGE_RESULT_WINDOW, {ModuleConstant.UI_OPEN, resultSet})
	elseif (StageManager.getCurStageType() == ConstantData.STAGE_TYPE_TEAM_POEM and resultSet.isWin) then
		ModuleEvent.dispatch(ModuleConstant.STAGE_RESULT_WINDOW, {ModuleConstant.UI_OPEN, resultSet})
	elseif resultSet.stageType == ConstantData.STAGE_TYPE_ADVENTURE_DAILY 
		or resultSet.stageType == ConstantData.STAGE_TYPE_ADVENTURE_FINAL then
		ModuleEvent.dispatch(ModuleConstant.VICTORY_WINDOW,{ModuleConstant.UI_OPEN,resultSet})
	elseif resultSet.stageType == ConstantData.STAGE_TYPE_CAMP_KILLGOD then
		resultSet.iTotalDamage = conn:ReadLong()
		ModuleEvent.dispatch(ModuleConstant.VICTORY_WINDOW,{ModuleConstant.UI_OPEN,resultSet})
	elseif resultSet.stageType == ConstantData.STAGE_TYPE_OFFLINEMATCH then
		ModuleEvent.dispatch(ModuleConstant.VICTORY_WINDOW,{ModuleConstant.UI_OPEN,resultSet})
	elseif resultSet.stageType == ConstantData.STAGE_TYPE_COUPLE_DUNGEON then
		-- ModuleEvent.dispatch(ModuleConstant.VICTORY_WINDOW,{ModuleConstant.UI_OPEN,resultSet})
		ModuleEvent.dispatch(ModuleConstant.STAGE_RESULT_WINDOW, {ModuleConstant.UI_OPEN, resultSet})
	elseif resultSet.stageType == ConstantData.STAGE_TYPE_FAMILY_TRANSPORT_PVP then
		ModuleEvent.dispatch(ModuleConstant.VICTORY_WINDOW,{ModuleConstant.UI_OPEN,resultSet})
	else
		ModuleEvent.dispatch(ModuleConstant.STAGE_RESULT_WINDOW, {ModuleConstant.UI_OPEN, resultSet})
	end
end

--与服务端同步战斗时间
local function receiveFightTime()
	local conn=GameNet.GetSocket()
	local fightTime=conn:ReadInt()
	StageFightWindowCtrl:setStageTime(fightTime)

end

--服务端同步角色属性
local function receiveCharacterValue()
	local conn = GameNet.GetSocket()
	local size = conn:ReadSbyte()
    local character = nil
    local uniqueID = nil
    local newValue = nil
    if size > 0 then
        --血量同步
        for i = 1, size do
            uniqueID = conn:ReadString()
            newValue = conn:ReadInt()
            character = CharacterManager:getCharacByUId( uniqueID )
            if character and character.state ~= CharacterConstant.STATE_DEAD then
                if character.hp > newValue then
                    ModuleEvent.dispatch( ModuleConstant.CHARAC_UPDATE_ATTR, { uniqueID, ConstantData.ROLE_CONST_HP, newValue } )
                end
                if newValue == 0 then
                	character:changeProperty( ConstantData.ROLE_CONST_HP, 0, true )
                	if character.state ~= CharacterConstant.STATE_HITFLY then --不能在空中死亡
                   		character:switchState( CharacterConstant.STATE_DEAD )
               		end
                end
			else
				if newValue == 0 and character == nil then
					UnHandleInfoBeforeLoaded.addDead(uniqueID)
				end
            end
        end
    end
    size = conn:ReadSbyte()
    if size > 0 then
        --全部玩家所造成伤害值同步
        for i = 1, size do
            uniqueID = conn:ReadString()
            newValue = conn:ReadInt()
            if damageValueDic==nil then
            	damageValueDic={}
            end
            damageValueDic[uniqueID]=newValue
            if uniqueID == RoleData.roleId then
            	totalDamageValue=newValue
        	end
            ModuleEvent.dispatchWithFixedArgs(ModuleConstant.CHARAC_UPDATE_DAMAGE,uniqueID,newValue)
        end
    end
end

--服务端同步掉落 实时
local function receiveServerDrop()
    local conn = GameNet.GetSocket()
    local size = conn:ReadSbyte()
    if size > 0 then
        local dropList = nil
        local dropSize = nil
        local uid = nil
        for j = 1, size do
            dropList = {}
            uid = conn:ReadString()
            dropSize = conn:ReadSbyte()
    	    for i = 1, dropSize do
    		    dropList[i] = { itemId=0,count=0 }
    		    dropList[i]["itemId"] = conn:ReadInt()
    		    dropList[i]["count"] = conn:ReadInt()
    	    end
            ModuleEvent.dispatchWithFixedArgs(ModuleConstant.PLAY_DROP, uid, dropList)
        end
   end
end

--服务端复活反馈
local function receiveRevive()
    local conn = GameNet.GetSocket()
    local subType = conn:ReadSbyte()
    local uid = conn:ReadString()
    local result = conn:ReadSbyte()
    if result == 1 then
    	if subType == 1 then --复活玩家
        	ModuleEvent.dispatch(ModuleConstant.CHARAC_REVIVE,uid)
        elseif subType == 2 then --复活伙伴
        	local character = CharacterManager:getCharacByUId(uid);
        	if character then
	        	local reviveTime = character.reviveTime or 0; 		--延时复活伙伴  
	        	ModuleEvent.dispatch(ModuleConstant.STAGE_FIGHT_PARTNER_REVIVE_TIME , reviveTime)
	        	api_Delay(reviveTime , function ()	        		
	        		if FightControlFactory.getControl().getFightState() == FightDefine.FIGHT_STATE_FIGHTING then
	        			ModuleEvent.dispatch(ModuleConstant.CHARAC_REVIVE,uid)
	        		end
	        	end)        		
        	end
        end
    else
    	if subType == 1 then --复活玩家
    		ModuleEvent.dispatch(ModuleConstant.CHARAC_REVIVE_FAIL,uid)
    	end
    end
end

local function receiveChangeFightVoAttr()
	local conn = GameNet.GetSocket();
	local uid = conn:ReadString();
	local attrName = conn:ReadString();
	local value = conn:ReadInt();
	local character = CharacterManager:getCharacByUId(uid);
	if(character)then
		--判断是不是角色;
		if(character.characterType == CharacterConstant.TYPE_PLAYER or character.characterType == CharacterConstant.TYPE_SELF)then
			local playerData = FightModel:getPlayerData( uid );
            local oldValue = playerData[attrName]
			playerData[attrName] = value;
			character:changeProperty( attrName, value );
			--根据不同的属性需要做些特定的逻辑;
			if(attrName == "camp")then
				character.view.nameUI:setPlayerInfo(playerData);
                CharacterManager:changeCharacCamp( uid, oldValue, value )
			end
		end
	end
end

--家族远征战斗结算
local function receiveExpedFinished()
	local conn = GameNet.GetSocket()
	local expeditionId   = conn:ReadInt()
	local expeditionStep = conn:ReadInt()
	local isWin          = (conn:ReadSbyte() == 2)
	local star           = conn:ReadSbyte()
	local passTime       = conn:ReadInt()
	FightControlFactory.getControl().stopFight(isWin)
	if isWin then   --胜利
		ModuleEvent.dispatch(ModuleConstant.VICTORY_WINDOW,{ModuleConstant.UI_OPEN,{stageType = -1,expeditionId=expeditionId,expeditionStep=expeditionStep,star=star}})
	else
		ModuleEvent.dispatch(ModuleConstant.STAGE_RESULT_WINDOW, {ModuleConstant.UI_OPEN, {isWin=isWin,expeditionId=expeditionId}})
	end
end

-- 需要同步技能剩余CD给客户端的场景类型
local NEED_SYNC_SKILLCD_STAGE = 
{
	[ConstantData.STAGE_TYPE_DAILY5V5_PVP] = true,
	[ConstantData.STAGE_TYPE_CAMP_DAILY] = true,
}

-- 需要在新玩家进入时同步其他角色运行时状态的场景类型
local NEED_SYNC_RUNTIMEINFO_STAGE = 
{
	[ConstantData.STAGE_TYPE_DAILY5V5_PVP] = true,
	[ConstantData.STAGE_TYPE_CAMP_DAILY] = true,
}

-- 同步技能剩余CD给重新进入的角色(用于同步技能按钮上的CD表现，防止出现未表现CD但技能不可点的情况)
local function syncSkillCDtoClient(uniqueId)
	local charac = CharacterManager:getCharacByUId(uniqueId)
	-- 角色不存在或角色还没有技能使用记录时就不用同步这个消息
	if charac == nil or charac.skillFireSuccTime == nil then return end
	local size = 0
	for _,v in pairs(charac.skillFireSuccTime) do 
		size = size + 1
	end
	if size == 0 then return end
	local conn = EnvironmentHandler.getNetworkDecoder()
	conn:initBuffer()
	conn:WriteProtocol(ModuleConstant.SYNC_SKILL_CD)
	conn:WriteShort(fightStageType or 0)
	conn:WriteSbyte(size)
	for skillId, timeStamp in pairs(charac.skillFireSuccTime) do
		conn:WriteInt(skillId)
		conn:WriteInt(timeStamp)
	end
	local pack = conn:GetPack()
	EnvironmentHandler.sendPackToClient(uniqueId, pack)
end

-- 打包全场角色的运行时数据，发给新加入的人
local function getAllCharacterRuntimeInfoPack()
	local conn = EnvironmentHandler.getNetworkDecoder()
	local allCharacs = CharacterManager:getAllCharac()
	conn:initBuffer()
	conn:WriteProtocol(ModuleConstant.SYNC_ALLCHARAC_RUNTIME_INFO)
	conn:WriteShort(fightStageType or 0)
	local characSize = 0
	for _,character in pairs(allCharacs) do
		characSize = characSize + 1
	end
	conn:WriteShort(characSize)
	for _,character in pairs(allCharacs) do
		conn:WriteString(character.uniqueID)
		local pos = character:getPosition()
		-- 写入位置朝向信息
		conn:WriteFloat(math.round(pos.x, 2))
		conn:WriteFloat(math.round(pos.y, 2))
		conn:WriteFloat(math.round(pos.z, 2))
		conn:WriteShort(math.ceil(character:getRotation():ToEulerAngles().y))
		conn:WriteShort(math.round((character.framespeed or 0) * 100, 0))
		-- 写入角色属性信息
		conn:WriteSbyte(#AttrEnum+#EXT_ATTR)
		local attrIndex = 0
		for k,v in ipairs(AttrEnum) do
			conn:WriteShort(attrIndex)
			conn:WriteInt(character[v] or 0)
			attrIndex = attrIndex + 1
		end
		for k,v in ipairs(EXT_ATTR) do
			conn:WriteShort(attrIndex)
			conn:WriteInt(character[v] or 0)
			attrIndex = attrIndex + 1
		end
		-- 写入角色身上的BUFF信息
		if character.charaBuff and #character.charaBuff.buffList > 0 then
			conn:WriteSbyte(#character.charaBuff.buffList)
			for _,buff in pairs(character.charaBuff.buffList) do
				conn:WriteString(buff.attackerUID)
				conn:WriteString(buff.targetUID)
				conn:WriteInt(buff.buffData.buffId)
				conn:WriteInt(buff.buffData.buffLv)
				conn:WriteString(buff.instanceId)
			end
		else
			conn:WriteSbyte(0)
		end
	end
	return conn:GetPack()
end

local function getAllFighterPack()
	-- 发给新加入玩家的数据包,发的产品数据是服务端LUA现有所有的技能等级、buff等数据
	local conn = EnvironmentHandler.getNetworkDecoder()
	local allCharacs = CharacterManager:getAllCharac()
	conn:initBuffer()
	conn:WriteProtocol(ModuleConstant.CLIENT_PVP_UPDATEPLAYER)

	local monsterSet = fightData:getData(FightDefine.DATA_MONSTER)
	local monsterSize = 0
	if monsterSet then
		for k,v in pairs(monsterSet) do
			if v.uniqueID == nil then  monsterSize = monsterSize + 1 end
		end
	end
	conn:WriteSbyte(monsterSize)
	if monsterSize > 0 then
		for _,monster in pairs(monsterSet) do
			if monster.uniqueID == nil then
				monster:write(conn)
			end
		end
	end

	-- 把现有的角色（包括自己）发给新加进来的人
	local allCharacSize = 0
	local fighterSet = fightData:getData(FightDefine.DATA_FIGHTER)
	for k,v in pairs(allCharacs) do allCharacSize = allCharacSize + 1 end
	conn:WriteSbyte(allCharacSize)
	for _,charac in pairs(allCharacs) do
		local fighter = fighterSet[charac.uniqueID]
		if fighter then
			fighter.position = charac:getPosition():Clone()
			fighter.hp = charac.hp
			fighter.rotation = math.round(charac:getRotation():ToEulerAngles().y, 0)
			fighter:write(conn)
		end
	end
	conn:WriteSbyte(0) -- 移除的数量，新加入的人不用知道这个
	local tempPack = conn:GetPack()
	return tempPack
end

--pvp同步玩家增加与移除
local function receiveUpdatePlayer(conn)
	if conn == nil then
		if EnvironmentHandler.isPvpClient then
			return
		end
		conn = GameNet.GetSocket()
	end
	-- CmdLog("pvp update player info(add/del)")
	local fighter = nil
    local character = nil
	-- monsterVo
	local size = conn:ReadSbyte()
	local monsterVoList = nil
	if size > 0 then
		monsterVoList = {}
		local monsterSet = fightData:getData(FightDefine.DATA_MONSTER)
		for index = 1, size do
			local monster = MonsterVo:create()
			monster:read(conn)
			table.insert(monsterVoList, monster)
			monsterSet[monster.monsterid] = monster
		end
	end
    --增加
	size = conn:ReadSbyte()
	local fighterList = {}
	local tempPartnerList = {}
	local fighterSet = fightData:getData(FightDefine.DATA_FIGHTER)
	for index = 1, size do
		fighter = FighterVo:create()
		fighter:read( conn )
        fighter:parseData()
		fighterSet[fighter.uniqueID] = fighter

		if fighter.characterType == CharacterConstant.TYPE_PLAYER then
			fightData:getData(FightDefine.DATA_PLAYER)[fighter.uniqueID] = fighter
			table.insert(fighterList, fighter)
			--print("敌人数据 位置:" , StringUtils.encodeSyncVector3(fighter.position))

		elseif fighter.characterType == CharacterConstant.TYPE_MONSTER then
			fightData:getData(FightDefine.DATA_MONSTER)[fighter.uniqueID] = fighter
			table.insert(fighterList, fighter)
		elseif fighter.characterType == CharacterConstant.TYPE_PARTNER then
			fightData:getData(FightDefine.DATA_PARTNER)[fighter.uniqueID] = fighter
			table.insert(tempPartnerList, fighter)
		end
	end
	if #tempPartnerList > 0 then
		for k,v in ipairs(tempPartnerList) do
			table.insert(fighterList, v)
		end
	end
	for i,fighter in ipairs(fighterList) do
		if EnvironmentHandler.isInServer == true or fighter.uniqueID ~= RoleData.roleId then
            character = CharacterManager:getCharacByUId( fighter.uniqueID )
            if character ~= nil then
                if character.state == CharacterConstant.STATE_DEAD then
					character:setPosition(fighter.position)
                    character:revive()
                end
            else
                if EnvironmentHandler.isInServer == true or StageManager.isStageReady() == true then
                    ModuleEvent.dispatch(ModuleConstant.CHARAC_CREATE, fighter)
                    --print("创建敌人 位置:" , StringUtils.encodeSyncVector3(fighter.position))
                else
                    fightData:getData(FightDefine.DATA_PLAYER)[fighter.uniqueID] = fighter
                end
            end
        end
        if EnvironmentHandler.isInServer == false and fighter.uniqueID == RoleData.roleId then --客户端主角复活
			local myCharac = CharacterManager:getMyCharac()
			if myCharac.hp <= 0 or myCharac.state == CharacterConstant.STATE_DEAD then
				myCharac:revive()
				ModuleEvent.dispatch(ModuleConstant.CLIENT_LOADINGEND, {RoleData.roleId}) --获取buff
			end
			myCharac:setPosition(fighter.position)
        end
	end
    --移除
    size = conn:ReadSbyte()
    local removeUId = nil
	local removeList = {}
    for index = 1, size do
        removeUId = conn:ReadString()
	
		table.insert(removeList, removeUId)
		if EnvironmentHandler.isInServer or RoleData.roleId ~= removeUId then
            if fightData:getData(FightDefine.DATA_PLAYER)[removeUId] then
                fightData:getData(FightDefine.DATA_PLAYER)[removeUId] = nil
            end
			ModuleEvent.dispatch(ModuleConstant.CHARAC_REMOVE, removeUId)
		end
    end

	-- 服务端LUA收到更新玩家的消息后，要同步给战斗中的全部玩家
	if EnvironmentHandler.isInServer then
		 -- 服务端执行JAVA指令
		 size = conn:ReadSbyte()
		 if size > 0 then
			 local orderVo = nil
			 local orderMap = {}
			 for index = 1, size do
     			orderVo = ServerOrderVo:create()
     			orderVo:read(conn) 
     			table.insert( orderMap, orderVo )
			 end
			 ServerOrderHandler.handlerOrderList(orderMap)
		 end

		-- 没有新加入的战斗实体
		if #fighterList == 0 then
			-- 无新加也无移除的，就不用通知客户端了
			if #removeList == 0 then
				return
			end
			conn:initBuffer()
			conn:WriteProtocol(ModuleConstant.CLIENT_PVP_UPDATEPLAYER)
			conn:WriteSbyte(0)	-- 怪物配置数据量
			conn:WriteSbyte(0)	-- 新增人员量
			conn:WriteSbyte(#removeList)
			for k,uid in pairs(removeList) do
				conn:WriteString(uid)
			end
			local pack = conn:GetPack()
			EnvironmentHandler.sendPackToAllClient(pack)
		else
			-------------------------------------------------------------
			-- 发给新加入玩家的数据包,发的产品数据是服务端LUA现有所有的技能等级、buff等数据
			local tempPack = getAllFighterPack()
			local newIDList = {}
			for _,fighter in pairs(fighterList) do
				if fighter.characterType == CharacterConstant.TYPE_PLAYER and (not fighter.isRobot) then
					-- print("sync all characters to the new comer ->" .. tostring(fighter.name) .. "[" .. tostring(fighter.uniqueID) .. "]")
					table.insert(newIDList, fighter.uniqueID)
					if FightServerControl.getFightState() == FightDefine.FIGHT_STATE_FIGHTING then
						EnvironmentHandler.sendSpecifiedOrderToClient(fighter.uniqueID, ModuleConstant.PVP_FIGHT_START, {})
					end
				end
			end
			if #newIDList > 0 then
				EnvironmentHandler.sendPackToMultiClient( newIDList, tempPack )
				if fightStageType then
					-- 把当前所有玩家的运行时状态发给新加入的端
					if NEED_SYNC_RUNTIMEINFO_STAGE[fightStageType] then
						EnvironmentHandler.sendPackToMultiClient( newIDList, getAllCharacterRuntimeInfoPack())
					end
					if NEED_SYNC_SKILLCD_STAGE[fightStageType] then
						for _,uid in ipairs(newIDList) do
							syncSkillCDtoClient(uid)
						end
					end
				end
			end

			--------------------------------------------------------------------
			-- 发给其他已经在战斗中的玩家的数据包（只包括了新加入玩家的产品数据）
			conn:initBuffer()
			conn:WriteProtocol(ModuleConstant.CLIENT_PVP_UPDATEPLAYER)
			-- 新增怪物数据
			if monsterVoList == nil then
				conn:WriteSbyte(0)
			else
				conn:WriteSbyte(#monsterVoList)
				for _,monster in pairs(monsterVoList) do
					monster:write(conn)
				end
			end
			-- 新增玩家
			conn:WriteSbyte(#fighterList)
			local addCharacUIDDic = {}
			for _,fighter in pairs(fighterList) do
				fighter:write(conn)
				addCharacUIDDic[fighter.uniqueID] = true
			end
			-- 要移除的玩家
			conn:WriteSbyte(#removeList)
			for _,uid in pairs(removeList) do
				conn:WriteString(uid)
			end
			-- 发给非本次加人的其他玩家
			local tempPack = conn:GetPack()
			local oldIdList = {}
			local allCharacs = CharacterManager:getAllCharac()
			for _,charac in pairs(allCharacs) do
				if charac.characterType == CharacterConstant.TYPE_PLAYER and (not addCharacUIDDic[charac.uniqueID]) and (not charac.isRobot) then
					-- print("sync new comers to other characters ->" .. tostring(charac.name) .. "[" .. tostring(charac.uniqueID) .. "]")
					table.insert(oldIdList, charac.uniqueID)
				end
			end
			if #oldIdList > 0 then
				EnvironmentHandler.sendPackToMultiClient( oldIdList, tempPack )
			end
		end
		FightControlFactory.getControl().onAddNewFighters(fighterList)
	end
end

--清除其他玩家
local function receiveClearOther()
    local conn = GameNet.GetSocket()
    CharacterManager:disposeByType( CharacterConstant.TYPE_PLAYER )
end

local function receiveIntegral(conn)
	if conn == nil then
		conn = GameNet.GetSocket()
	end
	local count=conn:ReadSbyte()
	local totalIntegral=0
	for i=1,count do
		local data={
			type=1,
			roleId=conn:ReadString(),
			integral=conn:ReadInt(),
		}
		totalIntegral=data.integral+totalIntegral
		ModuleEvent.dispatch(ModuleConstant.FIGHT_INTEGRAL,data)
	end
    -- local integral = conn:ReadInt()
    local integralData={
    	type=2,
    	integral=totalIntegral,
	}
    ModuleEvent.dispatch(ModuleConstant.FIGHT_INTEGRAL, integralData)
    
end

-- PVP战斗开始
local function onPVPFightStart(conn)
	FightControlFactory.getControl().setFightState(FightDefine.FIGHT_STATE_FIGHTING)
	if EnvironmentHandler.isInServer == false then
		ModuleEvent.dispatch(ModuleConstant.PVP_FIGHT_START, {})
	end
end

-- PVP准备就绪
local function onPVPReady()
	--GameLog("lyt","-----------onPVPReady------------")
	-- print("-----------onPVPReady------------")
	if fightStageType == ConstantData.STAGE_TYPE_FAMILY_WAR_NORMAL_FIGHT then
		return
	end
	if EnvironmentHandler.isInServer then
		local readyCD = FightDefine.FIGHT_PVP_READY_CD
		if fightStageType == ConstantData.STAGE_TYPE_FAMILY_WAR_ELITE_FIGHT then
			readyCD = FightDefine.FIGHT_FAMILYWAR_READY_CD
		elseif fightStageType == ConstantData.STAGE_TYPE_DAILY5V5_PVP then
			readyCD = FightDefine.DAILY5V5_READY_CD
		end
		FightControlFactory.getControl().setFightTimeInfo(limitTime, readyCD)
		local pack = getAllFighterPack()
		local idList = {}
		local allCharacs = CharacterManager:getAllCharac()
		for _,charac in pairs(allCharacs) do
			if charac.characterType == CharacterConstant.TYPE_PLAYER and (not charac.isRobot) then
				table.insert(idList, charac.uniqueID)
			end
		end
		if #idList > 0 then
			EnvironmentHandler.sendPackToMultiClient( idList, pack )
		end
		ModuleEvent.dispatch(ModuleConstant.PVP_ALL_READY, {})
	else
		ModuleEvent.dispatch(ModuleConstant.PVP_ALL_READY, {})
	end
end

-- 客户端loading完毕,服务端lua同步指令
local function onClientLoadingEnd( param )
	-- 5v5已经用新的机制实现运行时状态同步，为了不影响其他战斗，其他战斗的暂时保持原来的做法，等5v5验证通过后再改成统一的方式
	if fightStageType ~= ConstantData.STAGE_TYPE_DAILY5V5_PVP then
		local charac = CharacterManager:getCharacByUId(param[1])
		if charac then
			local campBuffMap = fightData:getData(FightDefine.DATA_CAMP_BUFF)
			if campBuffMap then
				for uid, vo in pairs(campBuffMap) do
					if vo.campid == charac.camp then --补发阵营buff
						ModuleEvent.dispatch(ModuleConstant.ADD_BUFF, {charac.uniqueID, charac.uniqueID, vo.buffid, vo.level, vo.instanceid})
					end
				end
			end
		end
	end
end

-- 客户端收到战斗状态更新
local function updatePvpFightInfo(conn)
	local newFightState = conn:ReadSbyte()
	local duration = conn:ReadInt()
	limitTime = duration * 0.001
	recvDataTime = GameNet.GetServerTime()
	ModuleEvent.dispatch(ModuleConstant.CLIENT_SYNC_FIGHTSTATE, {newFightState, limitTime})
end

-- PVP中同步所有角色的运行时数据
local function updateCharacterRuntimeInfo(conn)
	local stageType = conn:ReadShort()
	local characSize = conn:ReadShort()
	if characSize == 0 then return end
	local characRuntimeInfos = {}
	for i = 1, characSize do
		local temp = {}
		temp.uniqueID = conn:ReadString()
		temp.position = Vector3.New(conn:ReadFloat(), conn:ReadFloat(), conn:ReadFloat())
		temp.rotationY = conn:ReadShort()
		temp.framespeed = conn:ReadShort()
		temp.attrib = Attribute:create()
		temp.attrib:read(conn)
		local buffSize = conn:ReadSbyte()
		if buffSize > 0 then
			temp.buffs = {}
			for j = 1, buffSize do
				temp.buffs[j] = {}
				temp.buffs[j][1] = conn:ReadString()
				temp.buffs[j][2] = conn:ReadString()
				temp.buffs[j][3] = conn:ReadInt()
				temp.buffs[j][4] = conn:ReadInt()
				temp.buffs[j][5] = conn:ReadString()
			end
		end
		table.insert(characRuntimeInfos, temp)
	end
	if stageType ~= fightStageType then
		return
	end
	local charac = nil
	for _,runtimeInfo in pairs(characRuntimeInfos) do
		charac = CharacterManager:getCharacByUId(runtimeInfo.uniqueID)
		local movespeed = runtimeInfo.framespeed * 0.01 * ConstantData.FRAME_RATE
		if charac then
			-- 更新位置和方向
			charac:setPosition(runtimeInfo.position)
			charac:setRotation(Quaternion.Euler(0, runtimeInfo.rotationY, 0))
			charac:setFrameSpeed(movespeed)
			-- 更新属性
			for k,v in pairs(runtimeInfo.attrib) do
				charac:changeProperty(k,v)
			end
		else
			local fighterVo = FightModel:getFighterVo(runtimeInfo.uniqueID)
			if fighterVo then
				fighterVo.position = runtimeInfo.position
				fighterVo.rotation = runtimeInfo.rotationY
				fighterVo.movespeed = movespeed
				-- 更新属性
				for k,v in pairs(runtimeInfo.attrib) do
					fighterVo[k] = v
				end
			end
		end
		-- 更新角色身上的BUFF
		if runtimeInfo.buffs then
			BuffManager:handlerRuntimeBuffs(runtimeInfo.buffs)
		end
	end
end

local function recvRestSkillCD(conn)
	local tempStageType = conn:ReadShort()
	local size = conn:ReadSbyte()
	pvpRestSkillCD = {}
	for i = 1, size do
		pvpRestSkillCD[conn:ReadInt()] = conn:ReadInt()
	end
	-- 因为模拟网络的读写是必须读完的，所以在读完之后再判断是否可用
	if tempStageType ~= fightStageType then 
		pvpRestSkillCD = nil
	end
end

local function registListener()
	-- body
	GameNet.registerRecv(PacketType.ClientEnterFight,receiveData)
	GameNet.registerRecv(PacketType.ClientStageFinish, receiveFightResult)
	GameNet.registerRecv(PacketType.ClientFightTime, receiveFightTime)
    GameNet.registerRecv(PacketType.ClientRecvValue, receiveCharacterValue)
    GameNet.registerRecv(PacketType.ClientRecvDrop, receiveServerDrop)
    GameNet.registerRecv(PacketType.ClientRevive, receiveRevive)
    GameNet.registerRecv(PacketType.ClientFamilyExpedFinished, receiveExpedFinished)
    GameNet.registerRecv(PacketType.ClientUpdatePlayer, receiveUpdatePlayer)
    GameNet.registerRecv(PacketType.ClientChangeFightVoAttr, receiveChangeFightVoAttr)
    GameNet.registerRecv(PacketType.ClientClearOther, receiveClearOther)
    GameNet.registerRecv(PacketType.ClientIntegral, receiveIntegral)

	ModuleEvent.registerRecv(ModuleConstant.CLIENT_PVP_UPDATEPLAYER, receiveUpdatePlayer)
	ModuleEvent.registerRecv(ModuleConstant.PVP_FIGHT_START, onPVPFightStart)
	ModuleEvent.registerRecv(ModuleConstant.PVP_ALL_READY, onPVPReady)
	ModuleEvent.registerRecv(ModuleConstant.CLIENT_SYNC_FIGHTSTATE, updatePvpFightInfo)
	ModuleEvent.registerRecv(ModuleConstant.SYNC_ALLCHARAC_RUNTIME_INFO, updateCharacterRuntimeInfo)
	ModuleEvent.registerRecv(ModuleConstant.SYNC_SKILL_CD, recvRestSkillCD)
	ModuleEvent.addListener(ModuleConstant.CLIENT_LOOTTREASURE_PVE_UPDATEPLAYER, receiveUpdatePlayer)
	ModuleEvent.addListener(ModuleConstant.CLIENT_LOADINGEND, onClientLoadingEnd, onClientLoadingEnd)
end

function FightModel.initFcd()
	-- body
	local defineData = CFG.fcd:getAll()
	for i, v in ipairs(FightDefine.TONUMER_LIST) do
		if defineData[v] ~= nil then
			defineData[v].value = tonumber( defineData[v].value )
		end
	end

	defineData[FightDefine.FIGHT_COMMON_FIGHTAREA].value = defineData[FightDefine.FIGHT_COMMON_FIGHTAREA].value * 0.1
	defineData[FightDefine.FIGHT_COMMON_HITFLASH].value = defineData[FightDefine.FIGHT_COMMON_HITFLASH].value * 0.001 * ConstantData.FRAME_RATE
	defineData[FightDefine.FIGHT_COMMON_HITFLASH].value = math.ceil(defineData[FightDefine.FIGHT_COMMON_HITFLASH].value)
	defineData[FightDefine.FIGHT_COMMON_RESETAUTO].value = defineData[FightDefine.FIGHT_COMMON_RESETAUTO].value * 0.001 * ConstantData.FRAME_RATE
end

function FightModel.register()
	-- body
	fightData = Storage.getModule( dataModelName )
	if EnvironmentHandler.isInServer then
		FightModel.initFcd()
	end
	registListener()
end

-- 请求进入战斗
-- stageType : 场景类型,参见ConstantData.STAGE_TYPE_SKYTOWER;
function FightModel:requestBattle(stageType, dungeonId)
	dungeonStageId = dungeonId
	local conn = GameNet.GetSocket()
	conn:WriteProtocol(PacketType.ServerEnterFight)
	conn:WriteSbyte(stageType);
	conn:WriteInt(dungeonId)
	conn:WriteSbyte(0)   --不是再战
	conn:SendData()
end

function FightModel:reqBattleAgain(stageType, dungeonId)
	dungeonStageId = tonumber(dungeonId)
	local conn = GameNet.GetSocket()
	conn:WriteProtocol(PacketType.ServerEnterFight)
	conn:WriteSbyte(stageType);
	conn:WriteInt(dungeonId)
	conn:WriteSbyte(1)   --再战
	conn:SendData()
end

-- 请求PK
function FightModel:requestPk( roleId )
	-- body
    GameNet.sendPacket(PacketType.ServerPkPo,OnlinePvpConstant.REQUEST.INVITE_PK, {RoleId = roleId})
end

--暂停战斗时间的计时
function FightModel:reqPauseFightTime()
	local conn = GameNet.GetSocket()
	conn:WriteProtocol(PacketType.ServerPauseTime)
	conn:SendData()
end

function FightModel:reqContinueFightTime()
	local conn = GameNet.GetSocket()
	conn:WriteProtocol(PacketType.ServerContinueTime)
	conn:WriteSbyte(fightStageType)
	conn:SendData()
end

function FightModel:requestExitFight()
	local conn = GameNet.GetSocket()
	conn:WriteProtocol(PacketType.ServerExitFight)
	conn:SendData()
end

function FightModel:sendAutoFlag( flag, stageType )
	-- body
	local conn = GameNet.GetSocket()
	conn:WriteProtocol(PacketType.ServerAutoRecord)
	conn:WriteSbyte(flag)
	conn:WriteSbyte(stageType)
	conn:SendData()
end

--上传玩家对其他的伤害/治疗数据
function FightModel.sendGiveDamage(damageList, size)

	local conn = GameNet.GetSocket()
	conn:WriteProtocol(PacketType.ServerGiveDamage)
    conn:WriteSbyte(StageManager.getCurStageType())
	conn:WriteSbyte(size)
	size = size or #damageList
	for i = 1,size do
        conn:WriteString(damageList[i].attackerID)
		conn:WriteString(damageList[i].targetID)
        conn:WriteSbyte(damageList[i].damageType) --0=伤害，1=治疗
		conn:WriteInt(damageList[i].value)
	end
    if size > 0 then
	    conn:SendData()
    end
end

--请求复活
function FightModel:requestRevive()
    local conn = GameNet.GetSocket()
    local subType = 1
    conn:WriteProtocol(PacketType.ServerRevive)
    conn:WriteSbyte(subType)
    conn:WriteSbyte(fightStageType)
    conn:SendData()
end

--请求复活伙伴
function FightModel:requestPartnerRevive( uId )
 	local conn = GameNet.GetSocket()
    local subType = 2
    conn:WriteProtocol(PacketType.ServerRevive)
    conn:WriteSbyte(subType)
    conn:WriteSbyte(fightStageType)
    conn:WriteString(uId)
    conn:SendData()
    --print("subType:" , subType , " fightStageType:" , fightStageType , " uId:" , uId)
 end 

--判断是否伙伴是否要复活 --目前只是pve才复活
function FightModel:isPartnerCanRevive()
	if EnvironmentHandler.environmentType == EnvironmentDef.DEFAULT then 
		return true
	end
	return false
end

-- 战斗时发送客户端属性以检测作弊
function FightModel:roleAttrCheck()
    -- body
    local mycharac = CharacterManager:getMyCharac()
    if mycharac ~= nil then
        local conn = GameNet.GetSocket()
        conn:WriteProtocol(PacketType.ServerRoleAttrCheck)
        conn:WriteSbyte(ATTR_COUNT)
        for index,attribKey in ipairs(AttrEnum) do
            conn:WriteShort(index-1)
            conn:WriteInt(mycharac[attribKey]) 
        end
        conn:SendData()
    end
end

-- 主线关卡中实时发送主角hp以进行星级判定
function FightModel:syncRoleAttr()
	-- body
	local mycharac = CharacterManager:getMyCharac()
    if mycharac and mycharac.hp then
        local conn = GameNet.GetSocket()
        conn:WriteProtocol(PacketType.ServerRoleAttr)
        conn:WriteInt(math.floor(mycharac.hp))
        conn:SendData()
    end
end

--单人副本请加入
function FightModel:canControlAI()
    if fightStageType == ConstantData.STAGE_TYPE_DUNGEON or
        fightStageType == ConstantData.STAGE_TYPE_SKYTOWER or
        fightStageType == ConstantData.STAGE_TYPE_SEARCHTREASURE or
        fightStageType == ConstantData.STAGE_TYPE_PRODUCEDUNGEON or
        fightStageType == ConstantData.STAGE_TYPE_GAMECAVE or
        fightStageType == ConstantData.STAGE_TYPE_CALLBOSS or
        fightStageType == ConstantData.STAGE_TYPE_FAMILYEXPED or
        fightStageType == ConstantData.STAGE_TYPE_BRAVE_TRIAL or
        fightStageType == ConstantData.STAGE_TYPE_ADVENTURE_DAILY or
        fightStageType == ConstantData.STAGE_TYPE_ADVENTURE_FINAL or
        fightStageType == ConstantData.STAGE_TYPE_ROYALREWARD or
        fightStageType == ConstantData.STAGE_TYPE_FAMILYTASK or
        fightStageType == ConstantData.STAGE_TYPE_BUDDY_DUNGEON or 
        fightStageType == ConstantData.STAGE_TYPE_CAMP_TASK or 
        fightStageType == ConstantData.STAGE_TYPE_CAMP_KILLGOD then
        return true
    end
    return false
end

function FightModel:canControlDead()
	if EnvironmentHandler.isInServer == false then
		if fightStageType == ConstantData.STAGE_TYPE_TEAM
			or fightStageType == ConstantData.STAGE_TYPE_ELITE
			-- or fightStageType == ConstantData.STAGE_TYPE_TEAM_POEM
			or fightStageType == ConstantData.STAGE_TYPE_OFFLINEPVP
			or fightStageType == ConstantData.STAGE_TYPE_OFFLINEMATCH
			or fightStageType == ConstantData.STAGE_TYPE_CARGO_ROBOT
			or fightStageType == ConstantData.STAGE_TYPE_COUPLE_DUNGEON
			or fightStageType == ConstantData.STAGE_TYPE_CAMP_QICHU then
			return false
		end
	end
    return true
end

--客户端能否控制怪死亡
function FightModel:canControlMonsterDead(characterType)
	if EnvironmentHandler.isInServer == false and characterType == CharacterConstant.TYPE_MONSTER then
		if fightStageType == ConstantData.STAGE_TYPE_ADVENTURE_DAILY then
			return false
		end
	end
    return true
end

function FightModel:isOnPVP()
	if EnvironmentHandler.isInServer == false then
		if fightStageType == ConstantData.STAGE_TYPE_PK or fightStageType == ConstantData.STAGE_TYPE_LOOTTREASURE_PVP
			or fightStageType == ConstantData.STAGE_TYPE_FAMILY_INVADE or fightStageType == ConstantData.STAGE_TYPE_FIGHTINGMASTER
			or fightStageType == ConstantData.STAGE_TYPE_OFFLINEPVP or fightStageType == ConstantData.STAGE_TYPE_OFFLINEMATCH 
			or fightStageType == ConstantData.STAGE_TYPE_FAMILY_WAR_ELITE_FIGHT 
			or fightStageType == ConstantData.STAGE_TYPE_FAMILY_WAR_NORMAL_FIGHT
			or fightStageType == ConstantData.STAGE_TYPE_FAMILY_TRANSPORT_PVP 
			or fightStageType == ConstantData.STAGE_TYPE_FAMILY_WAR_STAGE_FIGHT
			or fightStageType == ConstantData.STAGE_TYPE_FAMILY_TRANSPORT_PVP
			or fightStageType == ConstantData.STAGE_TYPE_DAILY5V5_PVP then
			return true
		else
			return false
		end
	end
    return true
end

--获取总伤害
function FightModel:getTotalDamage()
	return totalDamageValue
end

function FightModel:getFightData()
	-- body
	return fightData
end
--是否接收到了战斗数据
function FightModel:isRecvFightData()
	return isRecvFightData
end

function FightModel:clearRecvFightData()
	isRecvFightData = false
end

--保存角色伤害
function FightModel.saveCharacDamage(damageDic)
	if damageDic == nil then return end
	characDamageDic = characDamageDic or {}
	local charac = nil
	for k,v in pairs(damageDic) do
		characDamageDic[k] = v
	end
	ModuleEvent.dispatch(ModuleConstant.CHARAC_DAMAGE_SUM)
end

function FightModel:getCharacDamage(uid)
	if characDamageDic then
		return characDamageDic[uid] or 0
	end
	return 0
end

--是否为战斗发起者
function FightModel:isFightAttacker()
	return (isAttacker == 1)
end

function FightModel:getSkillData( skillid )
	-- body
	local config = CFG.skill:get(skillid)
	if config == nil then
		LogManager.LogError ("技能数据为空|ID:"..tostring(skillid))
		return
	end
	if config.hasParse == nil and skillid < FightDefine.ASSEMBLEID_BASE then
		SkillVo.parse(config)
		config.hasParse = true
	end
	return config
end

-- 公共配置
function FightModel:getFightCommon( key )
	-- body
	local config = CFG.fcd:get(key)
	if config == nil or config.value == nil then
--		CmdLog("-----------公共配置为空哦:"..key.."------------")
		return
	end
	return config.value
end

-- 获取组装技能的配置ID
function FightModel:getSkillID()
	-- body
	if assembleSkillIndex >= limit then
		-- CmdLog("-----------注意 FightModel:getSkillID 组装id超过上限！！！------------")
		assembleSkillIndex = base
	end
	assembleSkillIndex = assembleSkillIndex + 1
	return assembleSkillIndex
end

function FightModel:getPlayerData( uniqueID )
	-- body
	local config = fightData:getData(FightDefine.DATA_PLAYER)
	if config == nil or config[uniqueID] == nil then
		return nil
	end
	return config[uniqueID]
end

function FightModel:getMonsterVo(monsterId)
	local config = fightData:getData(FightDefine.DATA_MONSTER)
	if config == nil or config[monsterId] == nil then
--		CmdLog("-----------MonsterVo数据为空:"..monsterId.."------------")
		return nil
	end
	return config[monsterId]
end

function FightModel:getFightStageId()
	return dungeonStageId
end

function FightModel:canBeDead()
	local config = CFG.stageinfo:get(dungeonStageId)
	if config then
		if config["newcomer"] == 1 then
			return false
		end
	end
	return true
end

function FightModel:getStageInfoId()
	return fightStageId
end

function FightModel:getFightStageType()
    return fightStageType
end

function FightModel:getRebornTime()
    return rebornTime
end

-- 获取副本限制时间 单位为秒 不是每个副本都有
function FightModel:getLimitTime()
	local tempTime = limitTime
	if EnvironmentHandler.isPvpClient then
		-- 取关卡时间时减去加载耗时
	    if tempTime and tempTime > 0 and recvDataTime and recvDataTime > 0 then
			tempTime = tempTime - (GameNet.GetServerTime() - recvDataTime) * 0.001
			tempTime = math.ceil(tempTime)
			tempTime = math.max(0, tempTime)
		end
	end
	return tempTime
end

--获取副本剩余时间
function FightModel:getRemainTime()
	local tempTime = limitTime
	-- 取关卡时间时减去加载耗时
	   if tempTime and tempTime > 0 and recvDataTime and recvDataTime > 0 then
		tempTime = tempTime - (GameNet.GetServerTime() - recvDataTime) * 0.001
		tempTime = math.ceil(tempTime)
		tempTime = math.max(0, tempTime)
	end
	return tempTime
end

function FightModel:getOriginLimitTime()
	-- body
	return limitTime
end

function FightModel:getDungeonIntegral()
    -- body
    return dungeonIntegral
end

function FightModel:getAutoFlag()
	-- body
	return autoFlag
end

function FightModel:getInitMonsterInfo(monsterId)
	local config = fightData:getData(FightDefine.DATA_INIT_MONSTERS)
	if config == nil or config[monsterId] == nil then
		return nil
	end
	return config[monsterId]
end

-- 按buffID和等级取Buff数据
function FightModel:getBuffVo(buffId, level)
	local config = CFG.buff:getAll() --加载时ExtendConfigMethods会进行重排 调整为二维数组结构
	if config == nil or config[buffId] == nil or config[buffId][level] == nil then
		LogManager.LogError ("buff数据为空|ID:"..tostring(buffId).."|levle:"..tostring(level))
		return nil
	end
	if config[buffId][level].hasParse == nil then
		BuffVo.parse( config[buffId][level] )
		config[buffId][level].hasParse = true
	end
	return config[buffId][level]
end

function FightModel:getFighterVo(uniqueID)
	local fighterSet = fightData:getData(FightDefine.DATA_FIGHTER)
	if fighterSet==nil or fighterSet[uniqueID]==nil then
		return nil
	end
	return fighterSet[uniqueID]
end

function FightModel:getSkillLevelData( id, level )
	-- body
	local config = CFG.skilllvup:getAll() --加载时ExtendConfigMethods会进行重排 调整为二维数组结构
	if config == nil or config[id] == nil or config[id][level] == nil then
		LogManager.LogError ("技能等级数据为空|ID:"..tostring(id).."|level:"..tostring(level))
		return nil
	end
	if config[id][level].hasParse == nil then
		SkillLevelVo.parse( config[id][level] )
		config[id][level].hasParse = true
	end
	return config[id][level]
end

function FightModel:getFightSceneVo()
	return CFG.stageinfo:get(fightStageId)
end

--lyt 20160729 是否需要播放剧情
function FightModel:getPlayDrama()
	return isPlayDrama
end

-- 设置服务端运行数据
function FightModel:setServerData( data )
	-- body
	local decoder = GameNet.GetSocket()
	decoder:reflesh(data)
	receiveData()
end

function FightModel:setRuntimeData( data )
    local decoder = GameNet.GetSocket()
	decoder:reflesh(data)
    receiveUpdatePlayer()
end

function FightModel:getTotalMonsterNum()
	return totalMonsterNum
end

function FightModel:getDamageValueById(id)
	return damageValueDic[id]
end

function FightModel:getBornBuff()
	local data=fightData:getData(FightDefine.DATA_BORN_BUFF)
	return data
end

--[[--
获取家族战精英赛的开战时间
]]
-- function FightModel:getFamilyWarEliteStartTime()
-- 	return familyWarEliteStartTime
-- end

function FightModel:getFightStartTime()
	return fightStartTime
end

function FightModel:getRecvDataTime()
	return recvDataTime
end

-- 取在收到进入战斗数据后到loading完成时经过的时间，用于修正倒计时显示
function FightModel:getPassTimeInLoading()
	return (GameNet.GetServerTime() - recvDataTime) * 0.001
end

-- 取PVP技能剩余时间(一般是重新进入战斗时，要调这个接口，先要再NEED_SYNC_SKILLCD_STAGE里配置对应的场景类型)
function FightModel:getPvpSkillRestCD(skillId, skillLv)
	if pvpRestSkillCD == nil or pvpRestSkillCD[skillId] == nil then
		return 0
	end
	local skill = FightModel:getSkillLevelData(skillId, skillLv)
	if skill == nil or skill.cooldown <= 0 then
		return 0
	end
	local passtime = os.time() - pvpRestSkillCD[skillId]
	local cd = skill.cooldown / ConstantData.FRAME_RATE
	if cd > passtime then
		return (cd - passtime)
	end
	return 0
end

function FightModel:dispose()
	FightModel.RemoveBackMainStageTimer()
	totalDamageValue=0
	damageValueDic=nil
	characDamageDic = nil
	recvDataTime = 0
	isRecvFightData = false
	isAttacker = nil
	pvpRestSkillCD = nil
	dungeonIntegral = 0
	Storage.clearModule(dataModelName)
	fightData = Storage.getModule( dataModelName )
end

--切换角色销毁专用
function FightModel.clearUserData()
	FightModel:dispose()
end