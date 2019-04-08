-- region
-- Date    : 2016-06-15
-- Author  : daiyaorong
-- Description :  角色管理器
-- endregion

CharacterManager = {
    myCharac    = nil,
    characDic   = nil,		--角色字典
	typeDic     = nil,		--类型字典(characterType)	
    campDic     = nil,		--阵营字典
    masterUIdDic= nil,      --伙伴的主人字典
    campCount   = nil,      --阵营计数
    trapCount   = nil,      --陷阱怪计数
    characUID   = nil,		--唯一ID
    selectNpcID = nil,      --被选中的Npc的ID
}

local tempCharac = nil     --本地引用
local Creater_Map = {
    [CharacterConstant.TYPE_SELF]       = MainCharacter.create,
    [CharacterConstant.TYPE_PLAYER]     = Player.create,
    [CharacterConstant.TYPE_MONSTER]    = Monster.create,
    [CharacterConstant.TYPE_NPC]        = Npc.create,
    [CharacterConstant.TYPE_PARTNER]    = Partner.create,
    [CharacterConstant.TYPE_BABY]    = BabyCharacter.create,
}

-- 服务端定时向客户端发送怪物状态 暂时是家族战匹配关卡副本专用
local function parseServerSyncMonster(conn)
    -- body
    local size = conn:ReadSbyte()
    if size > 0 then
        for i = 1, size do
            local uniqueID = conn:ReadString()
            if uniqueID ~= "0" then
                local posX = conn:ReadFloat()
                local posY = conn:ReadFloat()
                local posZ = conn:ReadFloat()
                local skillid = conn:ReadInt()
                local lv = nil
                local serialNum = nil
                local targetID = nil
                if skillid ~= 0 then
                    lv = conn:ReadInt()
                    serialNum = conn:ReadInt()
                    targetID = conn:ReadString()
                end
                
                local monster = CharacterManager:getCharacByUId(uniqueID)
                if monster ~= nil and monster.hp > 0 and monster.state ~= CharacterConstant.STATE_DEAD then
                    local pos = Vector3.New(posX, posY, posZ)
                    local dis = CharacterUtil.sqrDistance( pos, monster:getPosition() )
                    if dis >= 1 then
                        monster:setPosition(pos)
                    end
                    if skillid ~= 0 and monster.state == CharacterConstant.STATE_IDLE then
                        monster:fire(skillid, lv, targetID, pos, serialNum, 1)
                    end
                end
            end
        end
    end
end

function CharacterManager:init()
    -- body
    self.characUID = 0
    self.characDic = {}
    self.campDic = {}
    self.campCount = {}
    self.trapCount = 0
	self.typeDic = {}
    self.masterUIdDic = {}  --伙伴的主人字典
    CharacterPool:init()
    CharacterModel.init()
    self:initEvent()
end

function CharacterManager.register()
	CharacterManager:init()
end

function CharacterManager:addToDic( character, camp, characterType )
    -- body
    self.characDic[character.uniqueID] = character
    if self.campDic[camp] == nil then
        self.campDic[camp] = {}
        self.campCount[camp] = 0
    end
    self.campDic[camp][character.uniqueID] = character
    self.campCount[camp] = self.campCount[camp] + 1
	if self.typeDic[characterType] == nil then
		self.typeDic[characterType] = {}
	end
	self.typeDic[characterType][character.uniqueID] = character
end

function CharacterManager:removeFromDic( character )
    -- body
    self.characDic[character.uniqueID] = nil
    self.campDic[character.camp][character.uniqueID] = nil
    self.campCount[character.camp] = self.campCount[character.camp] - 1
	self.typeDic[character.characterType][character.uniqueID] = nil
    -- if self.masterUIdDic[character.uniqueID] then   --如果是带有伙伴的角色,被移除则清空对应的伙伴记录
    --     self.masterUIdDic[character.uniqueID] = nil
    -- end
    if character.istrap and character.istrap == 1 then
        self.trapCount = self.trapCount - 1
    end
end

function CharacterManager:changeOutShow(param)
    if (param.uniqueID == nil and param.roleId==nil)or (self.myCharac and param.uniqueID==self.myCharac.uniqueID) then
        if self.myCharac then
            self.myCharac:updateProperties( param )
        end
    end
    if param.uniqueID or param.roleId then
       local char=CharacterManager:getCharacByUId(param.uniqueID or param.roleId)
        if char then
            char:changeProperty(ConstantData.ROLE_CONST_OUTSHOW,param[ConstantData.ROLE_CONST_OUTSHOW])
        end
    end
    
end

function CharacterManager:initEvent( )
    self.event = Event:create( self )
    if EnvironmentHandler.isInServer == false then
		self.event:attachEvent(RoleProtocol.R_0x0010, self.updateMyCharac )
        self.event:attachEvent(ModuleConstant.MYCHANGE_OUTSHOW, self.changeOutShow )
	end
    self.event:attachEvent(ModuleConstant.SKILL_REPLACE, self.updateMyCharacSkill )
    self.event:attachEvent(ModuleConstant.CHARAC_CREATE, self.requestAddCharac )
    self.event:attachEvent(ModuleConstant.CHARAC_REMOVE, self.removeCharac )
    self.event:attachEvent(ModuleConstant.JOYSTICK_MOVE,self.characterMove, self.parseMove )
    self.event:attachEvent(ModuleConstant.JOYSTICK_END,self.characterMoveEnd, self.parseMoveEnd )
    self.event:attachEvent(ModuleConstant.SKILL_CLICKFIRE, self.clickFire, self.parseClickFire )
    self.event:attachEvent(ModuleConstant.CHARAC_UPDATE_ATTR, self.updateAttr, self.parseAttrUpdate )
    self.event:attachEvent(ModuleConstant.CHARAC_FIREREQUEST, self.fireRequest, self.parseSkillRequest )
    self.event:attachEvent(ModuleConstant.CHARAC_REVIVE, self.revive )
	self.event:attachEvent(ModuleConstant.PLAYER_TIMING_SYNC, self.syncPlayerByTiming, self.parseSyncPlayerByTiming)
    self.event:attachEvent(ModuleConstant.DRAGONBALL_SETVISIBLE, self.dragonballHandle, self.dragonballParse)
    self.event:attachEvent(ModuleConstant.CAMPDAILY_UNLOCK_PASSIVESKILL, self.unlockPassiveSkill,self.parseUnlockPassiveSkill)
    self.event:attachEvent(ModuleConstant.CAMPDAILY_CHANGE_SKILL,self.changeSkill,self.parseChangeSkill)
	ModuleEvent.registerRecv(ModuleConstant.SERVER_SYNC_PLAYERINFO, self.parseServerSyncInfo)
    ModuleEvent.registerRecv(ModuleConstant.SERVER_SYNC_MONSTER, parseServerSyncMonster)
    ModuleEvent.registerRecv(ModuleConstant.SERVER_SYNC_ROBOTINFO, self.parseServerSuncRobotInfo)

end

--获取实体动态id
function CharacterManager:getCharacId( )
    -- body
    self.characUID = self.characUID + 1
    return self.characUID..""
end

--设置主角的出生位置
function CharacterManager:setMyCharacBornPos(bornPos,bornRot)
    if self.myCharac then
        self.myCharac.position = nil

        if PathFinder.isBlock(bornPos) then --Jim 加入这个为了服务端发过来的出生点高度有问题，客户端强制校正一下，避免卡死
            if StageManager.isInSafeStage() == false then
                LogManager.LogError("出生位置非法 stageid="..tostring(StageManager.getCurStageId()))
			else
--				if GameUtil.isMobilePlatform == false then
--					LogManager.LogError("出生位置或上次记录的位置非法，可能是地图已经修改！")
--				end
            end
            if StageManager.getCurStage() then
			    bornPos = StageManager.getCurStage():getDefaultPosAndRot()
            end
        end

        bornPos = PathFinder.samplePosition(bornPos)
        self.myCharac:setPosition(bornPos)
        self.myCharac:changeProperty( "bornPosition", bornPos )
        self.myCharac:changeProperty( "defaultPosition", bornPos )
        if bornRot then
            self.myCharac:setRotation(Quaternion.Euler(0, bornRot, 0))
        end
        self.myCharac:switchState( CharacterConstant.STATE_IDLE )
        TransferManager.checkIntoTransfer()
        MapManager.checkMapAndRoleInitOver()
    end
end

function CharacterManager:updateMyCharac(param)
    if self.myCharac ~= nil and param.isAttribChanged then
        self.myCharac:updateProperties( param )
    end
end

function CharacterManager:updateMyCharacSkill( param )
    -- body
    if self.myCharac ~= nil then
        self.myCharac:updateSkill( param )
    end
end

-- 战斗场景需进行分帧处理
function CharacterManager:requestAddCharac( param )
    -- body
    if EnvironmentHandler.isInServer then
        CharacterManager:addCharac( param )
    else
        local stageinfo = StageManager.getCurStage()
        if stageinfo ~= nil and  StageManager.isInFightStage() and param.uniqueID ~= RoleData.roleId then
            local function onAddFunc( param )
                -- body
                CharacterManager:addCharac( param )
            end
            PressureDispatcher.orderAddCharacter( onAddFunc, param )
        else
            CharacterManager:addCharac( param )
        end
    end
end

-- 添加角色
function CharacterManager:addCharac( param )
	if param.uniqueID and self.characDic[param.uniqueID] then
		return
	end
    local character = CharacterPool:getObject( param.characterType )
    if character == nil then
        character = Creater_Map[param.characterType](param)
    end
	if param.characterType == CharacterConstant.TYPE_SELF then
        self.myCharac = character
    elseif param.characterType == CharacterConstant.TYPE_PARTNER then  --如果创建的是伙伴,则记录改伙伴的主人
                self.masterUIdDic[param.masterUId] = param.uniqueID
    elseif param.characterType == CharacterConstant.TYPE_BABY then --宝宝，记录改宝宝的主人
        -- self.masterUIdDic[param.masterUId] = param.uniqueID
    end
    character.uniqueID = param.uniqueID or self:getCharacId() --玩家的用户ID 怪物唯一ID
    character.camp = param.camp or CharacterConstant.CAMP_DEFAULTMAP[param.characterType]
    character.storyIndex = param.storyIndex
    character.cgData = param.cgData; --cg动画的标识;
    self:addToDic( character, character.camp, param.characterType )
    character:init( param )
    ModuleEvent.dispatch(ModuleConstant.CHARAC_CREATE_COMPLETE,character)
    if character.istrap and character.istrap == 1 then
        self.trapCount = self.trapCount + 1
    end
	if character.defaultbuff then
		for _,buff in pairs(character.defaultbuff) do
			ModuleEvent.dispatch(ModuleConstant.ADD_BUFF, {character.uniqueID, character.uniqueID, buff.buffid, buff.bufflv, buff.instanceid or BuffManager.getInstanceId()})
		end
	end
end

-- 移除角色
function CharacterManager:removeCharac( uid )
    local character = self.characDic[uid]
    if character ~= nil then

        self:removeFromDic( character )
        character:dispose()
        CharacterPool:poolObject( character )
	else
		-- 检查待创建列表里是否存在，存在的话标记为无效值
		PressureDispatcher.markCharacInvalidIfExist(uid)
    end
end

function CharacterManager:parseMove( params )
    -- body
    if EnvironmentHandler.isInServer then
        CharacterManager:characterMove( params )
		ModuleEvent.dispatch(ModuleConstant.JOYSTICK_MOVE,params)
    else
        if params[1] ~= RoleData.roleId then
            CharacterManager:characterMove( params, true )
        end
    end
end

function CharacterManager:parseMoveEnd( params )
    -- body
    if EnvironmentHandler.isInServer then
        CharacterManager:characterMoveEnd( params )
        ModuleEvent.dispatch(ModuleConstant.JOYSTICK_END,params)
    else
        if params[1] ~= RoleData.roleId then
            CharacterManager:characterMoveEnd( params, true )
        end
    end
end

-- 角色移动
-- moveBySync 是否是别人的移动同步包
function CharacterManager:characterMove( param, moveBySync )
    -- body
    tempCharac = self.characDic[param[1]]
    if tempCharac ~= nil then
        tempCharac:joystickMove( param, moveBySync )
    end
end

-- 角色移动停止
function CharacterManager:characterMoveEnd( param, endBySync )
    -- body
    tempCharac = self.characDic[param[1]]
    if tempCharac ~= nil then
        tempCharac:joystickMoveEnd( param, endBySync )
    end
end

function CharacterManager:parseAttrUpdate( param )
    -- body
	CharacterManager:updateAttr( param )
	-- 临时处理Loading过程中死亡
	if EnvironmentHandler.isPvpClient and StageManager.isStageReady() == false then
		if param[2] == ConstantData.ROLE_CONST_HP and param[3] <= 0 then
			UnHandleInfoBeforeLoaded.addDead(param[1])
		end
	end
end

-- 更新角色属性
function CharacterManager:updateAttr( param )
    -- body
    tempCharac = self.characDic[param[1]]
    if tempCharac ~= nil then
		if param[2] == ConstantData.ROLE_CONST_MOVESPEED then
			tempCharac:setFrameSpeed(param[3] * 0.01)
		else
			tempCharac:changeProperty( param[2], param[3] )
		end
    end
end

function CharacterManager:parseSkillRequest( param )
    -- body
	-- 在客户端先行表现技能的方式下，忽略由服务端广播下来自己的技能请求（由服务端AI触发的除外param[9] == 1）
	if EnvironmentHandler.getSkillSyncType() ~= EnvironmentDef.SKILL_SYNC_STRICT then
		if EnvironmentHandler.isInServer == false and param[1] == RoleData.roleId and param[9] == 0 then
			return
		end
	else
		if param[1] == RoleData.roleId then
			Joystick.disableFrames = 0
		end
	end
	-- 检查是否需要添加到延迟队列
	if SkillDelayList.checkDelay(param[1], param) then
		SkillDelayList.addRecord(param[1], param)
	else
		CharacterManager:fireRequest( param )
	end
end

function CharacterManager:fireRequest( param )
    -- body
    tempCharac = self.characDic[param[1]]
    if tempCharac ~= nil then
        local pos = nil
    	if EnvironmentHandler.isInServer or param[1] ~= RoleData.roleId then
    		pos = Vector3.New(param[5], tempCharac:getPosition().y, param[6])  
    	end
        -- if EnvironmentHandler.isPvpClient then
        --     if param[1] ~= RoleData.roleId and param[4] ~= "0" and self.characDic[param[4]] then
        --         local targetPos = self.characDic[param[4]]:getPosition()
        --         local attackerPos = tempCharac:getPosition()
        --         if StageObjFilter.checkOnScreen( targetPos ) == false and StageObjFilter.checkOnScreen( attackerPos ) == false then
        --             return --攻击者和目标都在屏幕外则不表现
        --         end
        --     end
        -- end
        tempCharac:fire( param[2], param[3], param[4], pos, param[7], param[8] )
    end
end

function CharacterManager:revive( uid )
    tempCharac = self.characDic[uid]
    if tempCharac ~= nil then
        tempCharac:revive()
    end
end

function CharacterManager:parseClickFire( param )
    -- body
    self:clickFire( param )
end

function CharacterManager:clickFire( param )
    -- body
    tempCharac = self.characDic[param[1]]
    if tempCharac ~= nil then
        tempCharac:clickFire( param[2], param[3], param[4], nil )
    end
end

function CharacterManager:parseUnlockPassiveSkill(param)
    -- body
    self:unlockPassiveSkill(param)
end

function CharacterManager:unlockPassiveSkill(param)
    -- body
    tempCharac = self.characDic[param[1]]
    if tempCharac ~= nil then
        tempCharac:unlockPassiveSkill( param[2] )
    end 
end

function CharacterManager:changeSkill(param)
    tempCharac = self.characDic[param[1]]
    if tempCharac ~= nil then
        tempCharac:changeSkill(param[2],param[3],param[4])
    end
end

function CharacterManager:parseChangeSkill(param)
    self:changeSkill(param)
end

-- pvp时要同步效果 暂时放这里
function CharacterManager:dragonballHandle( param )
    -- body
    if param == nil then return end
    local state = param[3] == 1
    OutShowManager.SetVisableItem( param[1], param[2], state )
end

function CharacterManager:dragonballParse( param )
    -- body
    self:dragonballHandle(param)
end

function CharacterManager:syncPlayerByTiming(param)
	local charac = self.characDic[param[1]]
	if charac then
		local pos = Vector3.New(param[2], charac:getPosition().y, param[3])
		local rot = Quaternion.Euler(0, param[4], 0)
		charac:timingSyncPosAndRot(pos, rot)
	end
end

function CharacterManager:parseSyncPlayerByTiming(param)
	if EnvironmentHandler.isInServer then
		ModuleEvent.dispatch(ModuleConstant.PLAYER_TIMING_SYNC, param)
		self:syncPlayerByTiming(param)
	else
		if param[1] == RoleData.roleId then
			local delayMs = (Time.realtimeSinceStartup * 1000) - param[5]
--			--GameLog("zxg", "net work delay is:" .. tostring(delayMs))
			EnvironmentHandler.updateNetworkDelay(delayMs)
		else
			self:syncPlayerByTiming(param)
		end
	end
end

--  服务端定时向客户端推送角色的状态
function CharacterManager.parseServerSyncInfo(conn)
	local isForceSync = conn:ReadSbyte() == 1
	local posX = conn:ReadFloat()
	local posZ = conn:ReadFloat()
    local isAutoFight = conn:ReadSbyte() == 1
    local curHp = conn:ReadInt()
    local myChara = CharacterManager.myCharac
    -- 如果客户端的血量已经小于0了，就走复活那边的，这里不管
    if curHp and myChara.hp ~= curHp and myChara.hp > 0 then
        myChara:changeProperty(ConstantData.ROLE_CONST_HP, curHp)
    end
    if myChara.isAutoFight ~= isAutoFight then
        myChara.isAutoFight = isAutoFight
        ModuleEvent.dispatchWithFixedArgs(ModuleConstant.CHARAC_AUTOFIGHT_STATE, myChara.uniqueID, myChara.isAutoFight)
    end
    local clientPos = myChara:getPosition()
	local pos = Vector3.New(posX, clientPos.y, posZ)
	if isForceSync or CharacterUtil.sqrDistance(clientPos, pos) >= 100 then
		myChara:setPosition(pos)
	end
end

function CharacterManager.parseServerSuncRobotInfo(conn)
    local uid = conn:ReadString()
    local posX = conn:ReadFloat()
	local posZ = conn:ReadFloat()
    local robotChara = CharacterManager.getCharacByUId(uid)
    local clientPos = robotChara:getPosition()
    local pos = Vector3.New(posX, clientPos.y, posZ)
	if CharacterUtil.sqrDistance(clientPos, pos) >= 100 then
		robotChara:setPosition(pos)
	end
end

-- 根据uid获取角色
function CharacterManager:getCharacByUId( uid )
    -- body
    return self.characDic[uid]
end

-- 根据配置ID获取角色列表
function CharacterManager:getCharacByConfigId( configID )
    local characterList = nil
    if configID ~= nil then
        for uid, character in pairs( self.characDic ) do
            if character.configId ~= nil and character.configId == configID then
                characterList = characterList or {}
                characterList[uid] = character
            end
        end 
    end
    return characterList
end

function CharacterManager:getCharacByCamp( camp )
    -- body
    return self.campDic[camp]
end

function CharacterManager:getPartnerUId(masterUId)
    if self.masterUIdDic then
        return self.masterUIdDic[masterUId]
    end
end

function CharacterManager:getCharacByRelation( requesterID, requesterCamp, relation )
    -- body
    local characterList = nil
    local campA = CharacterModel.getCampStr(requesterCamp)
    local campB = nil
    for uid, character in pairs( self.characDic ) do
        if character.uniqueID ~= requesterID then --排除请求者自身
            campB = CharacterModel.getCampStr(character.camp)
            if CharacterModel.getRelation( campA, campB) == relation then
                characterList = characterList or {}
                characterList[uid] = character
            end
        end
    end
    return characterList
end

function CharacterManager:getRelation( requesterCamp, camp )
    -- body
    local campA = CharacterModel.getCampStr(requesterCamp)
    local campB = CharacterModel.getCampStr(camp)
    return CharacterModel.getRelation( campA, campB)
end

function CharacterManager:getRelationByMyCharac(camp)
    local campA = CharacterModel.getCampStr(self.myCharac.camp)
    local campB = CharacterModel.getCampStr(camp)
    return CharacterModel.getRelation( campA ,campB)
end

function CharacterManager:getCharacByType( type )
    -- body
    return self.typeDic[type];
end

function CharacterManager:getCharacCountByCamp( camp )
    -- body
    return self.campCount[camp]
end

function CharacterManager:getTrapCount()
    return self.trapCount
end

function CharacterManager:getNpcByNpcId( npcId )
	local npcList = self.typeDic[CharacterConstant.TYPE_NPC]
	if npcList == nil then
		return nil
	end
	for _,npc in pairs(npcList) do
		if npc.npcid == npcId then
			return npc
		end
	end
	return nil
end

-- 按场景布怪ID取怪物
function CharacterManager:getMonsterByStageMonsterId(stageMonsterId)
	local monsterList = self:getCharacByType(CharacterConstant.TYPE_MONSTER)
	if monsterList == nil then return nil end
	for _,monster in pairs(monsterList) do
		if monster.stageMonsterId == stageMonsterId then
			return monster
		end
	end
	return nil
end

--根据怪物id取怪物
function CharacterManager:getMonsterByMonsterId(monsterId)
    local monsterList = self:getCharacByType(CharacterConstant.TYPE_MONSTER)
    if monsterList == nil then return nil end
    for _,monster in pairs(monsterList) do
        if monster.monsterid == monsterId then
            return monster
        end
    end
    return nil
end

--修改角色阵营记录
function CharacterManager:changeCharacCamp( uid, oldCamp, newCamp )
    tempCharac = self.characDic[uid]
    if tempCharac ~= nil then
        if self.campDic[oldCamp] ~= nil then
            self.campDic[oldCamp][tempCharac.uniqueID] = nil
            self.campCount[oldCamp] = self.campCount[oldCamp] - 1
        end
        if self.campDic[newCamp] == nil then
            self.campDic[newCamp] = {}
            self.campCount[newCamp] = 0
        end
        self.campDic[newCamp][tempCharac.uniqueID] = tempCharac
        self.campCount[newCamp] = self.campCount[newCamp] + 1
    end
end

function CharacterManager:disposeByType( characType )
    if self.typeDic[characType] ~= nil then
        local uid = nil
        for k,v in pairs( self.typeDic[characType] ) do
            uid = v.uniqueID
            if v.istrap and v.istrap == 1 then
                self.trapCount = self.trapCount - 1
            end
            self.campDic[v.camp][uid] = nil
            -- if self.masterUIdDic[uid] then
            --     self.masterUIdDic[uid] = nil
            -- end
            self.campCount[v.camp] = self.campCount[v.camp] - 1
            v:switchState( CharacterConstant.STATE_DEAD )
            v:dispose()
            self.characDic[uid] = nil
            CharacterPool:poolObject(v)
        end
        self.typeDic[characType] = nil
    end
end

-- excludeMyself 是否把我自己也删了
function CharacterManager:clearAll(excludeMyself)
	if excludeMyself == nil then excludeMyself = true end
	if self.characDic then
		for uid,charac in pairs(self.characDic) do
			if excludeMyself == false or charac ~= self.myCharac then
				charac:dispose()
				CharacterPool:poolObject(charac)
			end
		end
		self.characDic = {}
		self.campDic = {}
		self.typeDic = {}
        self.masterUIdDic = {}
		self.campCount = {}
        self.trapCount = 0
		if excludeMyself and self.myCharac then
			self:addToDic(self.myCharac, self.myCharac.camp, self.myCharac.characterType)
		else
			self.myCharac = nil
		end
	end
end

function CharacterManager:removeMyCharac()
	if self.myCharac then
		self:removeCharac(self.myCharac.uniqueID)
	end
	self.myCharac = nil
end

function CharacterManager:getMyCharac()
    -- body
    return self.myCharac
end

function CharacterManager:getMyCharacPos()
    -- body
    if self.myCharac then
        return self.myCharac.position
    end
    return nil
end

--[[--
获取所有的角色
]]
function CharacterManager:getAllCharac()
    return self.characDic
end

function CharacterManager:dispose()
    -- body
    self.event:removeEvent()
    self.event = nil
    self.characUID = nil
    for k,v in pairs( self.characDic ) do
        v:dispose()
    end
    self.characDic = nil
    self.campDic = nil
    self.myCharac = nil
    self.masterUIdDic = nil
    self.trapCount = nil
    tempCharac = nil
    CharacterPool:dispose()
end

function CharacterManager:clearUserData()
	
end