-----------------------------------------------------
-- region : Player.lua
-- Date   : 2016.6.14
-- Author ：dinglin
-- Desc   ：玩家类
-- endregion
-----------------------------------------------------
Player = {
    characterType = CharacterConstant.TYPE_PLAYER
}
Player = createClassWithExtends(Player, CharacterBase )

--角色类
function Player.create( param )
	local player = Player:new()
	CharacterBase.create(player,param)
	return player
end

--初始化角色基类
function Player:init(playerData)
	self.model = playerData.model
    self.hatredmap = HatredMap:create()
    CharacterBase.init(self,playerData)
    --如果有神兵，就显示神兵

    if self.deityWeaponId then
        self:resetDeityweaponByResourceId(self.configId, self.fashionId, self.deityWeaponId)
    end
    --[[    ]]
    if self.deityweaponType then
        self:resetDeityweapon(self.jobId, self.fashionId, self.deityweaponType)
    end

    self:setPosAndRot(playerData)
	self:switchState(CharacterConstant.STATE_IDLE)
    -- 是否出生就开启自动
    if self.isAuto then
        self:switchAutoFight(true)
    end
    if self.view then
        if StageManager.isInShowRideStage() then
            self:resetRide(self.uniqueID, self.rideId)
        else
            self:resetRide(self.uniqueID, -1)
        end
        --[[
        --骑马和时装显示的地区是一样的,所以可以使用坐骑的显示来判断时装
        if StageManager.isInShowFashionStage() then
            self:resetFashion(self.uniqueID, self.jobId,self.fashionId)
        else
            self:resetFashion(self.uniqueID, self.jobId, -1)
        end
        ]]
    end
end

function Player:setPlayerNameLevel(level)
    if self.view then
        self.view:setPlayerNameLevel(level)
    end
end

-- 初始化技能配置
function Player:initSkill()
    CharacterBase.initSkill( self )
    local tempList = nil
    local stageType = FightModel:getFightStageType()
    if stageType == ConstantData.STAGE_TYPE_CAMP_DAILY then --阵营日常需要重新解析节能结构
        if self.skill ~= nil and self.skill ~= "" and self.skill ~= "0" then
            tempList = StringUtils.split( self.skill, "|" )
            self.skillAttack = {}
            self.skillAttackSort ={}
            self.skillAttackSortNormal ={}
            for key,value in ipairs( tempList ) do
                if value ~= "" then
                    value = StringUtils.split( value, "=", nil, tonumber )
                    if value[1] == 1 then
                        if value[3] == 1 then
                            table.insert(self.skillAttackSortNormal,{id=value[2], level=value[3]})
                        end
                        self.skillAttack[value[2]] = { id=value[2], level=value[3]}
                        --table.insert(self.campNormalSkillList,{ id=value[2], level=value[3] })
                    elseif value[1] == 4 then
                        if value[3] == 1 then
                            table.insert(self.skillAttackSort,{id=value[2], level=value[3]})
                        end
                        self.skillAttack[value[2]] = { id=value[2], level=value[3]}
                        --table.insert(self.campSkillList,{ id=value[2], level=value[3] })
                    elseif value[1] == 5 then
                        self.avoidSkill = { id=value[2], level=value[3] }
                    end
                end
            end 
            local skillData = {id=400101,level=1}
            self.normalAttack = nil
            self.skillAttack[1] = skillData
            if self.view then
                self.view:enableBoxCollider(false)
            end
         end       
    else
        if self.normalskill ~= nil and self.normalskill ~= "" then
            self.normalAttack = {}
            tempList = StringUtils.split( self.normalskill, "+", nil, tonumber )
            for key, value in ipairs( tempList ) do
                self.normalAttack[key] = { id=value, level=1 }
            end
            self.maxNormalAttack = #self.normalAttack
        end
        if self.skill ~= nil and self.skill ~= "" and self.skill ~= "0" then
            tempList = StringUtils.split( self.skill, "|" )
            self.skillAttack = {}
            self.ultiAttack = nil
            self.avoidSkill = nil
            for key,value in ipairs( tempList ) do
                if value ~= "" then
                    value = StringUtils.split( value, "=", nil, tonumber )
                    if value[1] <= 2 then
                        self.skillAttack[value[1]+1] = { id=value[2], level=value[3] }
                    elseif value[1] == 3 then
                        self.ultiAttack = { id=value[2], level=value[3] }
                    elseif value[1] == 4 then
                        self.avoidSkill = { id=value[2], level=value[3] }
                    end
                end
            end
            if self.view then
                self.view:enableBoxCollider(false)
            end
        end
    end
    if self.characterType ~= CharacterConstant.TYPE_SELF then
        if self.passSkillList ~= nil then
            if stageType == ConstantData.STAGE_TYPE_CAMP_DAILY then
                PassSkillManager.addHandler( self, true )
            else
                PassSkillManager.addHandler( self )
            end
        end
    end

end

function Player:setPosAndRot(playerData)
    --暂时加上判断，以免手机上收错id导致卡死
    if PathFinder.isBlock(playerData.position)  == false then
        self.bornPosition = PathFinder.samplePosition(playerData.position)
        self.defaultPosition = PathFinder.samplePosition(playerData.position)
        self:setPosition( self.defaultPosition )
    end
    self:setRotation( Quaternion.Euler(0,playerData.rotation, 0) )
    if self.view then
        self.view:setScale(Vector3.one*playerData.scale*0.001)
    end
end

function Player:initFightData()
    -- body
    CharacterBase.initFightData(self)
    self.invincible = 0
    self.defaultPosition = Vector3.zero
    self.skillCDKeys = {}
	self.skillFireSuccTime = {}
    self.skillCDEndTime = {}
    self.ctrlState = FightDefine.CONTROL_DEFAULT
	self.isAutoFight = false
	-- 为自动战斗而设
    self.orderIndex = 1
    self.orderList = { 1,2,3,4,0 }
    local stageType = FightModel:getFightStageType() 
    if stageType == ConstantData.STAGE_TYPE_CAMP_DAILY then
        self.orderList = {1}  
    end
    self.maxOrder = #self.orderList
    self.findenemyai = { 0, 500 }
    self.pathai = {0,0}
    self.stopTime = 100
    self.attackinterval = 0
end

function Player:initEvent()
    CharacterBase.initEvent(self)
end

function Player:ShowLevelUpEff()
    if self.view then
        self.view:ShowLevelUpEff()
    end
end

-- AI随机变化
function Player:setRandomAI( isRobot )
    if self.isRobot or isRobot then --机器人需要随机释放闪避 所谓的模拟真人
        self.orderList = CommonFunc.randomArray({ 1,2,3,4,0,5 }, 6);
    else
        self.orderList = CommonFunc.randomArray({ 1,2,3,4,0 }, 5);
    end
    local stageType = FightModel:getFightStageType() 
    if stageType == ConstantData.STAGE_TYPE_CAMP_DAILY then 
        if self.skillAttackSort and #self.skillAttackSort > 0 and 
            self.skillAttackSortNormal and #self.skillAttackSortNormal > 0  then
            self.skillAttack = {}
	    	local temp = math.random(1, #self.skillAttackSort)		
            table.insert(self.skillAttack,self.skillAttackSort[temp])
            temp = math.random(1, #self.skillAttackSortNormal)	
            table.insert(self.skillAttack,self.skillAttackSortNormal[temp])
        end
    	self.orderList = CommonFunc.randomArray({ 1,2}, 2);
    end
    self.orderIndex = math.random(1, #self.orderList);
    self.maxOrder = #self.orderList
    self.stopTime = math.random(100, 500);
    self.attackinterval = math.random(100, 1000);
end

function Player:changeProperty( key, value, isForce )
    if key == ConstantData.ROLE_CONST_HP and value < 0 then
        value = 0
    end
    CharacterBase.changeProperty(self,key,value,isForce)
    if key == ConstantData.ROLE_CONST_HP and self.view ~= nil  then
        self.view:updateBlood(value)
        ModuleEvent.dispatchWithFixedArgs( ModuleConstant.CHARAC_UPDATE_ATTR_PVP, self.uniqueID, ConstantData.ROLE_CONST_HP, value )
	elseif key == ConstantData.ROLE_CONST_MAXHP and self.view then
		self.view:updateMaxHp(value)
		ModuleEvent.dispatchWithFixedArgs( ModuleConstant.CHARAC_UPDATE_ATTR_PVP, self.uniqueID, ConstantData.ROLE_CONST_MAXHP, value )
	elseif key == "ctrlState" and value == FightDefine.CONTROL_NONE then
		self:stopMove()
    end
end

function Player:setSelect()
 --    CommonTips.showMsgBox('朋友，你我有缘，不如结义金兰吧','加好友',MessageBox.BUTTON.CONFIRM_CANCEL,'加好友|私聊', function(result) 
	-- 	if result == MessageBox.RESULT.YES then
	-- 		if FriendController.IsFriend(self.uniqueID) == false then
	-- 			FriendController.ReqAddFriend(self.uniqueID)
	-- 		end
	-- 	else
	-- 		FriendController.ChatPersonal(self.uniqueID, self.jobId, self.level, self.name)
	-- 	end
	-- end)
	if StageManager.getCurStageType() == ConstantData.STAGE_TYPE_FAMILY_TRANSPORT then
		FamilyTransportControl.onSelectPlayer(self)
	else
		CommonTips.showPlayerPopupMenu(self.uniqueID, Vector3(0,0,0))
	end
end

--打断寻路
function Player:breakOutSearch()
    self.stateMachine:invoke("stopSearchPath")
end

function Player:isJoystickMoving()
    -- body
    return self.movingKey ~= nil
end

--摇杆控制移动
function Player:joystickMove(param, moveBySync)    
    if self.state ~= CharacterConstant.STATE_IDLE 
        and self.state ~= CharacterConstant.STATE_RUN
        and self.state ~= CharacterConstant.STATE_ATTACK then
        self:stopMove()
        return
    end
    if self:isAbilityEnable(CharacterConstant.ABILITY.MOVE) == false then
        self:stopMove()
        return
    end
    if self.isAutoFight and (self.characterType == CharacterConstant.TYPE_SELF or EnvironmentHandler.isInServer) then
        self:swichAIState( false )
    end
    local dir       = param[2]
    local toRotation = Quaternion.Euler( 0, dir, 0 )
    self.joystickDirec = toRotation:Forward()
    if self.ctrlState == FightDefine.CONTROL_NONE then
        self:stopMove()
        return
    else
		-- 如果是别人的移动指令，要进行距离修正，使表现看起来正常点
		if moveBySync then
			local pos = Vector3.New(param[3], self.position.y, param[4])
			local sqrDistance = CharacterUtil.sqrDistance( pos, self.position )
			if sqrDistance > CharacterConstant.SQR_RUN_PRECISION then
--				--print("开始点差距稍大，缓动同步:" .. tostring(Vector3.Distance( pos, self.position )))
				local prePos = pos + self.joystickDirec * self.framespeed * ConstantData.FRAME_RATE * 0.5
				local tempDirection = prePos - self.position
				tempDirection.y = 0
				toRotation = Quaternion.LookRotation(tempDirection)
				if toRotation then
					self.joystickDirec = toRotation:Forward()
				end
				if self.syncLimitMoveKey then
					FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):removeLuaFunc(self.syncLimitMoveKey)
					self.syncLimitMoveKey = nil
				end
				-- 如果自己的网络延迟过大，就限制一下移动时间，避免位置走的偏差太远 
				if EnvironmentHandler.getNetworkDelay() > 500 then
					self.syncLimitMoveKey = FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(15, 1, function() 
						if self.joystickDirec and self.syncPosKey == nil then
							self.joystickDirec = nil
							self:stopMove()
							if self.state ~= CharacterConstant.STATE_ATTACK then
								self:playAni(CharacterConstant.ANIMATOR_IDLE,0.2)
							end
						end
					end)
				end
			end
			
		-- 服务端不用表现，直接设置为最新的位置
		elseif EnvironmentHandler.isInServer then
			local pos = Vector3.New(param[3], self.position.y, param[4])
			self:setPosition(pos)
		end
		self:doMoveAction(toRotation)
    end
end

function Player:doMoveAction(toRotation)
	if self.ctrlState == FightDefine.CONTROL_DEFAULT then
        self.targetID = nil
        self:stopSearch()
        self:breakOutSearch()
        self:lookAtSmoothByRotation( toRotation, nil, self.turnspeed )
        self.rotation = toRotation
        self:beginMove( toRotation )
        self:playAni(CharacterConstant.ANIMATOR_MOVE)
    elseif self.ctrlState == FightDefine.CONTROL_TURN then   -- 只改朝向
        self:lookAtSmoothByRotation( toRotation, nil, self.turnspeed )
        self.targetRot = toRotation
    elseif self.ctrlState == FightDefine.CONTROL_MOVE then   -- 只改位置
        self:beginMove( toRotation )
    elseif self.ctrlState == FightDefine.CONTROL_TURN_MOVE then
        self:lookAtSmoothByRotation( toRotation, nil, self.turnspeed )
        self.rotation = toRotation
        self:beginMove( toRotation )
    end
end

--摇杆触发停止移动
function Player:joystickMoveEnd( param, endBySync )
    if self.state == CharacterConstant.STATE_IDLE or self.state == CharacterConstant.STATE_RUN
        or self.state == CharacterConstant.STATE_ATTACK then
        if param[2] ~= nil then
			local tempRot = Quaternion.Euler(0, param[2], 0)
            local tempPos = Vector3.New(param[3],0,param[4])
            if self.position then
                tempPos.y = self.position.y
            end
			if endBySync then
				local dis = CharacterUtil.sqrDistance( tempPos, self.position )
				if dis > CharacterConstant.SQR_RUN_PRECISION then
--					--print("结束点差距太大，需要同步")
					dis = math.sqrt(dis)
					local isAlreadyStop = (self.joystickDirec == nil)
					self.joystickDirec = (tempPos - self.position)
					self.joystickDirec:SetNormalize()
					self.joystickDirec.y = 0
					local toRotation = Quaternion.LookRotation(self.joystickDirec)
					if toRotation then
						self:setRotation(toRotation)
					end
					local totalframe = math.ceil( dis / self.framespeed )
					local function finishFuc()
--						--print("结束点同步完成")
						self:setPosition( tempPos )
						self:joystickMoveEnd({})
					end
					if isAlreadyStop and toRotation then
						self:doMoveAction(toRotation)
					end
					self.syncPosKey = FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(totalframe,1,finishFuc)
					return
				else
					self:setRotation( tempRot )
					self:setPosition( tempPos )
				end
			else
				self:setRotation( tempRot )
				self:setPosition( tempPos )
			end
        end

        self:stopMove()
        self.joystickDirec = nil
        self.targetRot = nil
        if self.state ~= CharacterConstant.STATE_ATTACK then
            self:playAni(CharacterConstant.ANIMATOR_IDLE,0.2)
        end
    end
	if self.isAutoFight == true and AIManager.hasAI(self.uniqueID) then
        if self.resetKey ~= nil then
            FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):removeLuaFunc( self.resetKey )
            self.resetKey = nil
        end
        local function resetAuto()
            -- body
            if self.isAutoFight == true then
                self:swichAIState(true)
            end
        end
        local resetTime = FightModel:getFightCommon(FightDefine.FIGHT_COMMON_RESETAUTO)
        self.resetKey = FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(resetTime,1,resetAuto)
    end
end

function Player:timingSyncPosAndRot(newPos, newRot)
	if EnvironmentHandler.isInServer then
		self:setPosition(newPos)
		self:setRotation(newRot)
	elseif (self.state == CharacterConstant.STATE_IDLE or
			self.state == CharacterConstant.STATE_RUN) and 
			self.joystickDirec == nil then

		local dis = CharacterUtil.sqrDistance( newPos, self.position )
		if dis > CharacterConstant.SQR_RUN_PRECISION then
			self:moveTo(newPos, function() 
				self:setRotation(newRot)
			end)
		else
			self:setRotation( newRot )
			self:setPosition( newPos )
		end
	end
end

function Player:beginMove( targetRot )
    -- body
    self:stopMove()
    self.toPos = self.tempPos or Vector3.zero
    self.toPos:Set(0,0,0)
    local function moveFunc()
        if self.joystickDirec == nil then return end
		local pos = self:getPosition()
        self.toPos.x = self.joystickDirec.x * self.framespeed + pos.x
        self.toPos.y = self.joystickDirec.y * self.framespeed + pos.y
        self.toPos.z = self.joystickDirec.z * self.framespeed + pos.z
        local result = self:setPosition(self.toPos)
        if result then
            --通知角色移动了
            -- ModuleEvent.dispatch(ModuleConstant.PLAYER_MOVE,{self.uniqueID,self:getPosition()})
        end
    end
    self.movingKey = FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(0,0,moveFunc)
    if self.view == nil  then return end
    self:playerBeginMove()
end

function Player:stopMove()
    -- body
    if self.movingKey ~= nil then
        FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):removeLuaFunc( self.movingKey )
        self.movingKey = nil
    end
    if self.syncPosKey  then
        FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):removeLuaFunc( self.syncPosKey )
        self.syncPosKey = nil
    end
    if self.state == CharacterConstant.STATE_RUN then
        self:switchState(CharacterConstant.STATE_IDLE)
    end
    if self.view == nil  then return end
    self:playerStopMove()
end


function  Player:playerBeginMove()
    if StageManager.isInFightStage() then return end
    if self.view  then
        self.view:playerBeginMove()
    end
end

function  Player:playerStopMove()
    if StageManager.isInFightStage() then return end
    if self.view  then
        self.view:playerStopMove()
    end
end

function Player:onFire( skillid, level )
    -- body
    CharacterBase.onFire( self, skillid, level )
    -- 调整摇杆操控状态
    if self.ctrlState == FightDefine.CONTROL_MOVE then
        self:setFrameSpeed( self.movespeed ) --还原摇杆移动速度
    elseif self.ctrlState == FightDefine.CONTROL_TURN then
        self:changeProperty( "turnspeed", self.defaultTurnspeed )
    elseif self.ctrlState == FightDefine.CONTROL_TURN_MOVE then
        self:setFrameSpeed( self.movespeed ) --还原摇杆移动速度
    end
    self.ctrlState = FightDefine.CONTROL_NONE
    self:stopMove()
end

-- 释放成功
function Player:fireSuccess()
    -- body
    local stageType = FightModel:getFightStageType()
    if self.skillstate == CharacterConstant.SKILLSTATE_NORMAL and stageType ~= ConstantData.STAGE_TYPE_CAMP_DAILY then
        self.attackIndex = self.attackIndex or 1
        self.attackIndex = self.attackIndex + 1
        if self.attackIndex > self.maxNormalAttack then
            self.attackIndex = 1
        end
	else
		self.attackIndex = 1
        if AIManager.hasAI(self.uniqueID) then
            self:increaseOrderIndex()
        end
        local levelData = FightModel:getSkillLevelData( self.usingSkill.id, self.usingSkill.level )
        if not levelData then
            return
        end
        
        local function cdFinishFunc()
            -- body
            if self.skillCDKeys ~= nil and self.skillCDKeys[levelData.skillid] then
                FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):removeLuaFunc( self.skillCDKeys[levelData.skillid] )
                self.skillCDKeys[levelData.skillid] = nil
				self.skillFireSuccTime[levelData.skillid] = nil
                self.skillCDEndTime[levelData.skillid] = nil
            end
        end
        self.skillCDKeys[levelData.skillid] = FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(levelData.cooldown,1,cdFinishFunc)
        self.skillFireSuccTime[levelData.skillid] = os.time()
        self.skillCDEndTime[levelData.skillid] = self.skillFireSuccTime[levelData.skillid] + levelData.cdSecond --cd完成的时间
		if self.characterType == CharacterConstant.TYPE_SELF then
            ModuleEvent.dispatchWithFixedArgs( ModuleConstant.SKILL_FIRE_SUCCESS, self.usingSkill,CharacterConstant.BEGIN_SKILL_CD)
        end
    end
end

function Player:clickCampDailySkillItem( clickIndex )
    -- body
	-- 嘲讽状态时无法使用技能
	if self.isTaunt then
		return
	end
    local skillType  = 0
    local skillId    = 0
    local skillLevel = 1

    if clickIndex == 1 then
        local data = CampDailyModel.getNormalHitData()
        if data and data.id ~= -1 then
            if self.skillCDKeys[data.id] ~= nil then -- 处于冷却中无法使用
                return
            end
            skillType = FightDefine.SKILLTYPE_SKILL
            skillId = data.id
            skillLevel = data.level
           -- GameLog("zls","big skillId:",skillId)
        else
            return
        end  
    elseif clickIndex == 2 then
        if self.avoidSkill == nil or self.skillCDKeys[self.avoidSkill.id] ~= nil then -- 处于冷却中无法使用
            return
        end
        skillType = FightDefine.SKILLTYPE_AVOID                             
    elseif clickIndex == 4 then
        local data = CampDailyModel.getSkillHitData()
        --GameLog("zls","small skillId:",data.id)
        if data and data.id ~= -1 then
            if self.skillCDKeys[data.id] ~= nil then -- 处于冷却中无法使用
                return
            end
            skillType = FightDefine.SKILLTYPE_SKILL
            skillId = data.id
            skillLevel = data.level        
        else
            return
        end       
    end   
	local firePos = CharacterManager:getMyCharac():getPosition()
	local firePosX = math.round(firePos.x, 2)
	local firePosZ = math.round(firePos.z, 2)
	-- 技能在严格同步模式下，当网络延迟过大时，在技能请求未响应时，禁止角色移动
	if EnvironmentHandler.isPvpClient and EnvironmentHandler.getSkillSyncType() == EnvironmentDef.SKILL_SYNC_STRICT then
		if EnvironmentHandler.getNetworkDelay() >= 500 then
			Joystick.disableFrames = 30
		end
	end
	--GameLog("zls","skillId:"..skillId.." skillLevel:"..skillLevel) 
    ModuleEvent.dispatch(ModuleConstant.SKILL_CLICKFIRE, {self.uniqueID, skillType, skillId, skillLevel, firePosX, firePosZ})
end

function Player:clickSkillItem( clickIndex )
    -- body
	-- 嘲讽状态时无法使用技能
	if self.isTaunt then
		return
	end
    local skillType  = 0
    local skillId    = 0
    local skillLevel = 1
    if clickIndex == 1 then
        skillType = FightDefine.SKILLTYPE_NORMAL
    elseif clickIndex == 2 then
        if self.avoidSkill == nil or self.skillCDKeys[self.avoidSkill.id] ~= nil then -- 处于冷却中无法使用
            return
        end
        skillType = FightDefine.SKILLTYPE_AVOID
    elseif clickIndex >= 3 and clickIndex <= 5 then
        if self.skillAttack == nil then
            return
        end
        self.skillIndex = clickIndex - 2
        if self.skillAttack[self.skillIndex] == nil or self.skillAttack[self.skillIndex] == 0 then
            return
        end
        skillType = FightDefine.SKILLTYPE_SKILL
        skillId = self.skillAttack[self.skillIndex].id
        skillLevel = self.skillAttack[self.skillIndex].level
        if self.skillCDKeys[skillId] ~= nil then -- 处于冷却中无法使用
            return
        end
    elseif clickIndex == 6 then
        if self.ultiAttack == nil or self.skillCDKeys[self.ultiAttack.id] ~= nil then -- 处于冷却中无法使用
            return
        end
        skillType = FightDefine.SKILLTYPE_ULTIMATE
    end
	local firePos = CharacterManager:getMyCharac():getPosition()
	local firePosX = math.round(firePos.x, 2)
	local firePosZ = math.round(firePos.z, 2)
	-- 技能在严格同步模式下，当网络延迟过大时，在技能请求未响应时，禁止角色移动
	if EnvironmentHandler.isPvpClient and EnvironmentHandler.getSkillSyncType() == EnvironmentDef.SKILL_SYNC_STRICT then
		if EnvironmentHandler.getNetworkDelay() >= 500 then
			Joystick.disableFrames = 30
		end
	end
    ModuleEvent.dispatch(ModuleConstant.SKILL_CLICKFIRE, {self.uniqueID, skillType, skillId, skillLevel, firePosX, firePosZ})    
end

function Player:clickPassiveSkill(skillId,skillLevel)
    local skillType = FightDefine.SKILLTYPE_SKILL
    local firePos = CharacterManager:getMyCharac():getPosition()
    local firePosX = math.round(firePos.x, 2)
    local firePosZ = math.round(firePos.z, 2)
    -- 技能在严格同步模式下，当网络延迟过大时，在技能请求未响应时，禁止角色移动
    if EnvironmentHandler.isPvpClient and EnvironmentHandler.getSkillSyncType() == EnvironmentDef.SKILL_SYNC_STRICT then
	    if EnvironmentHandler.getNetworkDelay() >= 500 then
		    Joystick.disableFrames = 30
	    end
    end
    ModuleEvent.dispatch(ModuleConstant.SKILL_CLICKFIRE, {self.uniqueID, skillType, skillId, skillLevel, firePosX, firePosZ})      
end

function Player:clickFire( skillType, skillId, skillLevel, firePos )
    -- body
	-- if firePos then
 --        -- 判断位置是否合法
 --        local legalDis = self.framespeed * 30 * 5 --以5秒为合法范围
 --        if CharacterUtil.sqrDistanceWithoutY( self:getPosition(), firePos ) >= (legalDis * legalDis) then
 --            return
 --        end
	-- 	self:setPosition(firePos)
	-- end
    if skillType == FightDefine.SKILLTYPE_NORMAL then
        self:fireNormal()
    elseif skillType == FightDefine.SKILLTYPE_AVOID then
        self:fireAvoid()
    elseif skillType == FightDefine.SKILLTYPE_SKILL then
        self:fireSkill( skillId, skillLevel )
    elseif skillType == FightDefine.SKILLTYPE_ULTIMATE then
        self:fireUlti()
    end
end

-- 普通攻击
function Player:fireNormal()
    if self.state == CharacterConstant.STATE_ATTACK and self.skillstate == CharacterConstant.SKILLSTATE_NONE then
        -- 普攻连招记录
        self.continueNormal = true
    else
        self.attackIndex = self.attackIndex or 1
        if self.normalAttack and self.normalAttack[self.attackIndex] then
            self:findTarget( self.normalAttack[self.attackIndex].id )
            self:requestFire( self.normalAttack[self.attackIndex].id, self.normalAttack[self.attackIndex].level, self.targetID ) 
        end
    end
end

-- 闪避
function Player:fireAvoid()
    -- body
    if self.avoidSkill ~= nil then
        if self.skillCDKeys[self.avoidSkill.id] ~= nil then -- 处于冷却中无法使用
            return
        end
        self:requestFire( self.avoidSkill.id, 1 ) 
        self.attackIndex = 1
    end
end

-- 技能攻击
function Player:fireSkill(skillId,skillLevel)
    -- body
    if self.skillAttack ~= nil then
        if (self.allskill and self.allskill[skillId] == nil ) and (self.configSkill and self.configSkill[skillId] == nil) then
            return --非法技能id
        end
        if self.skillCDKeys[skillId] ~= nil then -- 处于冷却中无法使用
            if EnvironmentHandler.isInServer then
                if self.skillCDEndTime[skillId] and (self.skillCDEndTime[skillId] - os.time()) > 2 then --差距小于2秒时可释放 大于则不可释放
                    return
                end
            else
                return
            end
        end
        self:findTarget( skillId )
        self:requestFire( skillId, skillLevel, self.targetID )
        self.attackIndex = 1
    end
end

function Player:fireUlti()
    -- body
    if self.ultiAttack ~= nil then
        if self.skillCDKeys[self.ultiAttack.id] ~= nil then -- 处于冷却中无法使用
            if EnvironmentHandler.isInServer then
                if self.skillCDEndTime[self.ultiAttack.id] and (self.skillCDEndTime[self.ultiAttack.id] - os.time()) > 2 then --差距小于2秒时可释放 大于则不可释放
                    return
                end
            else
                return
            end
            return
        end
        self:findTarget( self.ultiAttack.id )
        self:requestFire( self.ultiAttack.id, self.ultiAttack.level, self.targetID )
        self.attackIndex = 1
    end
end

function Player:unlockPassiveSkill(skillId)
    if self.pSkillHandler == nil then
        return
    end
    self.pSkillHandler:unlockSkill(skillId)
end

function Player:changeSkill(idx,skillId,skillLevel)
    if self.skillAttack == nil then
        return
    end
    if self.skillAttack[idx] == nil then
        return 
    end
    self.skillAttack[idx] = {id=skillId, level=skillLevel}
end

function Player:clearSkillCD( skillType )
    -- body
	if self.skillCDKeys == nil then return end
    if skillType == nil then
        for k,v in pairs( self.skillCDKeys ) do
            FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):removeLuaFunc( v )
            if self.characterType == CharacterConstant.TYPE_SELF then
                local data={}
                data.id=k
                ModuleEvent.dispatchWithFixedArgs( ModuleConstant.SKILL_FIRE_SUCCESS, data,CharacterConstant.CLEAR_SKILL_CD)
            end
        end
        self.skillCDKeys = {}
		self.skillFireSuccTime = {}
        self.skillCDEndTime = {}
    else
        if skillType == FightDefine.SKILLTYPE_ULTIMATE then
            if self.skillCDKeys[self.ultiAttack.id] ~= nil then
                FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):removeLuaFunc( self.skillCDKeys[self.ultiAttack.id] )
                self.skillCDKeys[self.ultiAttack.id] = nil
				self.skillFireSuccTime[self.ultiAttack.id] = nil
                self.skillCDEndTime[self.ultiAttack.id] = nil
                if self.characterType == CharacterConstant.TYPE_SELF then
                    local data={}
                    data.id=self.ultiAttack.id
                    ModuleEvent.dispatchWithFixedArgs( ModuleConstant.SKILL_FIRE_SUCCESS, data,CharacterConstant.CLEAR_SKILL_CD)
                end
            end
        end
    end
end

function Player:isSkillInCD( skillType )
    -- body
    if skillType == FightDefine.SKILLTYPE_ULTIMATE then
        return self.skillCDKeys[self.ultiAttack.id] ~= nil
    end
end

function Player:findSkill()
    local skillparam = nil
    if FightModel:getFightStageType() == ConstantData.STAGE_TYPE_CAMP_DAILY then
    	if #self.orderList == 1 then
    		return self.skillAttack[1]
	    end
        local index = self.orderList[self.orderIndex]
        skillparam = self.skillAttack[index]
	--[[
        EnvironmentHandler.sendLogToServer("self.skillAttack size:"..#self.skillAttack)
        for k,v in pairs(self.orderList) do
            EnvironmentHandler.sendLogToServer("self.orderList:"..tostring(v))            
        end
	]]
        self:increaseOrderIndex()
        return skillparam
    end
    local useNormal = false
	if self:isAbilityEnable(CharacterConstant.ABILITY.SKILL) == false then
		useNormal = true
    elseif self.orderList[self.orderIndex] == 0 then
        useNormal = true
    elseif self.orderList[self.orderIndex] == 4 then
        skillparam = self.ultiAttack
        if self.ultiAttack == nil then
            useNormal = true
        end
    elseif self.orderList[self.orderIndex] == 5 then
        skillparam = self.avoidSkill
        if self.avoidSkill == nil then
            useNormal = true
        end
    else
        self.attackIndex = nil
        self.skillIndex = self.orderList[self.orderIndex]

        if self.skillAttack == nil or self.skillAttack[self.skillIndex] == nil then
            useNormal = true
        else
            skillparam = self.skillAttack[self.skillIndex]
        end
    end

    if useNormal and self.normalAttack then
        self.attackIndex = 1
        skillparam = self.normalAttack[self.attackIndex]
        self:increaseOrderIndex()
    else
        self.attackIndex = nil
    end

    return skillparam
end

function Player:tryAttack( targetID )
    -- body
    local skillparam = nil
    local skillLvData = nil
    while true do
        skillparam = self:findSkill()
        if FightModel:getFightStageType() == ConstantData.STAGE_TYPE_CAMP_DAILY then
            --EnvironmentHandler.sendLogToServer("skillparam:"..skillparam.id.." level"..skillparam.level)
            if skillparam == nil or skillparam.id == -1 then
                return
            end
            if self.skillCDKeys[skillparam.id] ~= nil then
                return
            end
        end

        if skillparam == nil or skillparam.id == nil or self.skillCDKeys == nil or self.skillCDKeys[skillparam.id] == nil then
			-- 日常5v5无视技能和人物等级限制，都可以释放
			if FightModel:getFightStageType() == ConstantData.STAGE_TYPE_DAILY5V5_PVP then
                break
			elseif FightModel:getFightStageType() == ConstantData.STAGE_TYPE_CAMP_DAILY then
				break
			end
        	skillLvData = FightModel:getSkillLevelData( skillparam.id, skillparam.level )
			if skillLvData.reqlv and self.level and (skillLvData.reqlv <= self.level) then -- 处于冷却中或未到等级则使用普攻
				break
			end

        end
        self:increaseOrderIndex()
    end
    self:requestFire( skillparam.id, skillparam.level, targetID, 1 ) 
end

function Player:increaseOrderIndex()
    self.orderIndex = self.orderIndex + 1
    if self.orderIndex > self.maxOrder then
        self.orderIndex = 1
    end
end

function Player:switchAutoFight( flag )
    -- body
    if flag == nil then
        self.isAutoFight = not self.isAutoFight
    else
        self.isAutoFight = flag
    end
    self:swichAIState( self.isAutoFight )
    if self.isAutoFight == false and self.state == CharacterConstant.STATE_RUN then
        self:switchState( CharacterConstant.STATE_IDLE )
    end
end

function Player:swichAIState( flag )
    -- body
    if flag == true then
        if AIManager.hasAI(self.uniqueID) == false then
            ModuleEvent.dispatch(ModuleConstant.AI_CREATE, self.uniqueID)
        else
            ModuleEvent.dispatch(ModuleConstant.AI_START, self.uniqueID)
        end
    else
        if AIManager.hasAI(self.uniqueID) then
            ModuleEvent.dispatch(ModuleConstant.AI_STOP, self.uniqueID)
        end
		if self.resetKey ~= nil then
            FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):removeLuaFunc( self.resetKey )
            self.resetKey = nil
        end
    end
	ModuleEvent.dispatchWithFixedArgs(ModuleConstant.CHARAC_AUTOFIGHT_STATE, self.uniqueID, flag)
end

function Player:stateEnd()
    -- body
    if self.continueNormal == true then --普攻连招
        self.continueNormal = false
        if self.normalAttack[self.attackIndex] == nil then
            self.attackIndex = nil
            CharacterBase.stateEnd( self )
            return
        end
        self:requestFire( self.normalAttack[self.attackIndex].id,self.normalAttack[self.attackIndex].level, self.targetID ) 
        return
    end
    if self.attackIndex == nil or (self.isAutoFight == false and (not self.isTaunt)) then
        CharacterBase.stateEnd( self )
    else
        if self.attackIndex == 1 then --循环到1则代表普攻结束
            self.attackIndex = nil
            CharacterBase.stateEnd( self )
        else
            self:requestFire( self.normalAttack[self.attackIndex].id,self.normalAttack[self.attackIndex].level ) 
        end
    end
	ModuleEvent.dispatch(ModuleConstant.PLAYER_STATE_END, self.uniqueID)
end

function Player:revive()
    self:switchAutoFight(false)
    CharacterBase.revive(self)
end

function Player:dispose()
    -- body
    -- --GameLog("zls","Player:dispose:")
    self:resetRide(self.uniqueID,-1)
    self:clearSkillCD()
    self.skillCDKeys = nil
	self.skillFireSuccTime = nil
    self.skillCDEndTime = nil
    self:stopMove()
    self.skill = nil
    self.joystickDirec = nil
    self.deityWeaponId = nil
    self.isAuto = nil
    self.skillAttackSort = nil
    self.skillAttackSortNormal = nil
    self.skillAttack = nil
    CharacterBase.dispose(self)
end

function Player:resetRide(roleId,rideId,callBack)
    if self.view == nil then return end
    if rideId ~= -1 then
        local temp = RideControl.getRoleRide(rideId)
        if temp ~= nil then
            self.view:getOnRide(roleId, temp, callBack)--temp.model, temp.pose, temp.pose2, temp.namehigh, callBack
        end
    else
        self.view:getOffRide()
        if callBack then
            callBack()
        end
    end
end

function Player:resetFashion(roleId, jobId, fashionId)
    if self.view == nil then return end
    if fashionId ~= -1 then
        local temp = FashionControl:getFashion(jobId,fashionId)
        if temp ~= nil then
            self.view:putonFashion(fashionId,temp:getFashionNormalModel())
        end
    else
        self.view:putoffFashion(jobId)
    end
end

function Player:resetDeityweapon(jobId, fashionId,deityweaponType)
    if self.view == nil then return end
    self.view:putonWeapon(jobId, fashionId, deityweaponType, false);
end

function Player:resetDeityweaponByResourceId(resourceId,fashionId,deityWeaponId)
    if self.view==nil then return end
    self.view:putonWeaponByResourceId(resourceId,fashionId,deityWeaponId)
end

function Player:resetPlayerDynamicInfo(info)
   if self.view == nil or self.view.nameUI == nil then return end
   self.view.nameUI:setPlayerDynamicInfo(info)
end