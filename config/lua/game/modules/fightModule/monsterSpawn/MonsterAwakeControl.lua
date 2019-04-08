--region MonsterAwakeControl.lua
--Date 2016/09/21
--Author zhouxiaogang
--Desc 怪物激活控制
--endregion

MonsterAwakeControl = {}

local groupList			= nil		-- 怪物组列表
local groupCount        = nil
local patrolList		= nil		-- 警戒列表
local patrolCount       = nil
local deadAwakeList		= nil		-- 死亡激活列表
local patrolFrameKey	= nil

function MonsterAwakeControl.init()
    groupList		= {}
    groupCount      = {}
    patrolList		= {}
    patrolCount     = 0
    deadAwakeList	= {}
	MonsterAwakeControl.initEvent()
end

function MonsterAwakeControl.initEvent()
	ModuleEvent.addListener(ModuleConstant.CHARAC_CREATE_COMPLETE, MonsterAwakeControl.addMonster)
	ModuleEvent.addListener(ModuleConstant.CHARACTER_DEAD, MonsterAwakeControl.removeMonster)
end

function MonsterAwakeControl.removeEvent()
	ModuleEvent.removeListener(ModuleConstant.CHARAC_CREATE_COMPLETE, MonsterAwakeControl.addMonster)
	ModuleEvent.removeListener(ModuleConstant.CHARACTER_DEAD, MonsterAwakeControl.removeMonster)
end

function MonsterAwakeControl.addMonster(character)
	if character == nil or character.characterType ~= CharacterConstant.TYPE_MONSTER then
		return
	end
    local monsterSpawnId = character.monsterSpawnId
	if monsterSpawnId == nil then return end
    if monsterSpawnId and groupList[monsterSpawnId] == nil then
        groupList[monsterSpawnId] = {}
        groupCount[monsterSpawnId] = 0
    end
    groupList[monsterSpawnId][character.uniqueID] = character
    groupCount[monsterSpawnId] = groupCount[monsterSpawnId] + 1
end

function MonsterAwakeControl.removeMonster(uid)
	local monster = CharacterManager:getCharacByUId(uid)
	if monster == nil then
		return
	end
    local monsterSpawnId = monster.monsterSpawnId
    if monsterSpawnId and groupList[monsterSpawnId] then
		groupList[monsterSpawnId][uid] = nil
        groupCount[monsterSpawnId] = groupCount[monsterSpawnId] - 1
    end
    MonsterAwakeControl.checkDeadAwake(monster)
    MonsterAwakeControl.removePatrol(monster)
end

--[[--
激活一组怪
]]
function MonsterAwakeControl.awakeMonsterSpawn(monsterSpawnId)
    if monsterSpawnId then
        for uid,monster in pairs (groupList[monsterSpawnId]) do
            ModuleEvent.dispatch(ModuleConstant.AI_AWAKE, uid)
        end
    end
end

--[[--
怪物警戒
]]
local function doPatrol()
    local awakeSpawnIds = {}
    for uid,monster in pairs (patrolList) do
        if awakeSpawnIds[monster.monsterSpawnId] then
            patrolList[uid] = nil
            -- patrolCount = patrolCount - 1
        else
			local enemyList = CharacterManager:getCharacByRelation( monster.uniqueID, monster.camp, CharacterConstant.RELATION_ENEMY )
			if enemyList then
				local monsterPos = monster:getPosition()
				local sqrSearchRadii = monster.searchradii * monster.searchradii
				for uid, enemy in pairs(enemyList) do
					local dis = CharacterUtil.sqrDistanceWithoutY(monsterPos, enemy:getPosition())
					if dis <= sqrSearchRadii then
						MonsterAwakeControl.awakeMonsterSpawn(monster.monsterSpawnId)
						awakeSpawnIds[monster.monsterSpawnId] = monster
					end
				end
			end
        end
    end
    patrolCount = 0
    for uid,monster in pairs (patrolList) do
        patrolCount = patrolCount + 1
        if awakeSpawnIds[monster.monsterSpawnId] then
            patrolList[uid] = nil
            patrolCount = patrolCount - 1
        end 
    end
    if patrolCount == 0 and patrolFrameKey then
        FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_AI):removeCallback(patrolFrameKey)
        patrolFrameKey = nil
    end
end

--[[--
添加警戒怪物
]]
function MonsterAwakeControl.addPatrol(monster)
    patrolList[monster.uniqueID] = monster
    -- patrolCount = patrolCount + 1
    if patrolFrameKey == nil then
        patrolFrameKey = FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_AI):RegisterLuaCallBack(3,0,doPatrol)
    end
end

--[[--
删除怪物警戒
]]
function MonsterAwakeControl.removePatrol(monster)
    if patrolList then
        patrolList[monster.uniqueID] = nil
        -- patrolCount = patrolCount - 1
    end
    -- if patrolCount == 0 and patrolFrameKey then
    --     FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_AI):removeCallback(patrolFrameKey)
    --     patrolFrameKey = nil
    -- end
end

--[[--
添加死亡激活监听
]]
function MonsterAwakeControl.addDeadListener(awakeParam, monster)
    if deadAwakeList[awakeParam] == nil then
        deadAwakeList[awakeParam] = monster
    end
end

--[[--
检测死亡激活
]]
function MonsterAwakeControl.checkDeadAwake(monster)
    if groupCount[monster.monsterSpawnId] == 0 then
        --判断是否需要激活某一组怪物
        if deadAwakeList[monster.monsterSpawnId] then
            MonsterAwakeControl.awakeMonsterSpawn(deadAwakeList[monster.monsterSpawnId].monsterSpawnId)
            deadAwakeList[monster.monsterSpawnId] = nil
        end
        --判断是否需要删除自己的监听
		local ai = AIManager.getAIByUniqueID(monster.uniqueID)
        if ai and ai.awakeParam and deadAwakeList[ai.awakeParam] then
            deadAwakeList[ai.awakeParam] = nil
        end
    end
end

--[[--
清空数据
]]
function MonsterAwakeControl.clear()
    groupList = nil
    groupCount = nil
    deadAwakeList = nil
	patrolList = nil
    patrolCount = nil
    if patrolFrameKey then
        FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_AI):removeCallback(patrolFrameKey)
        patrolFrameKey = nil
    end
	MonsterAwakeControl.removeEvent()
end