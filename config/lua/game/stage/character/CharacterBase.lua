-----------------------------------------------------
-- region : CharacterBase.lua
-- Date   : 2016.6.14
-- Author ：dinglin
-- Desc   ：角色基类
-- endregion
-----------------------------------------------------
CharacterBase = {
    uniqueID = nil,
    position = nil,
    bornPosition = nil,     --角色出生位置
    defaultPosition = nil,  --待机时位置
    rotation = nil,
    state = nil,
    stateMachine = nil,     --

    findPathState=nil,
    isCanMove = nil,        --是否允许移动
    movespeed = nil,
    view = nil,             --角色视图
    characterType = nil,    --角色类型
    isShowModelView = nil,  --是否显示角色模型视图，默认显示，周围玩家中会有不显示模型视图
    isActive = nil,         --是否显示
    event = nil,
    outShowInfo =nil,     --外显内容

    -- 战时属性
    usingSkill = nil,   --正在使用的技能
    motionInfo = nil,	--运动属性
    skillstate = nil,	--技能状态
    skillTime = nil,    --技能时长
    attackIndex = nil,	--普攻下标
    skillIndex = nil,	--技能下标
    superarmor = nil,	--霸体标记
    defaultArmor = nil, --原始霸体等级
	abilityState = nil,	--能力状态(启用/禁用)
	currentAttrib = nil, -- 角色当前的属性
	charaBuff = nil,	 -- 角色身上的buff控制器，在BuffManager中增加，默认为nil
    invincible = nil,   --无敌
    isIgnoreFilter = nil, --是否需要过滤不在镜头的物体(但true为不过滤，false过滤)
    isCanBeSelect = nil,--是否能够被选中
    delayShowDeadTime = nil, --延迟死亡展示
}
CharacterBase = createClass(CharacterBase)

--角色基类
function CharacterBase.create(self,param)

end

--初始化角色基类
function CharacterBase:init(param)
    self:setData( param )
    if self.view == nil then
        self.view = CharacterViewFactory.createView(self.characterType, self.uniqueID, param )
        if self.isActive~=nil then
            self.view:setActive(self.isActive , self.isShowModelView)
        end
    end
    self.rotation = Quaternion.New()
    self.bornPosition = Vector3.zero
    self.defaultPosition = Vector3.zero
	-- 默认给个位置
	if self.position == nil then
		self.position = self.bornPosition
	end
	-- 角色能力状态（1 是可用  0 是禁用）
	self.abilityState = {}
	for _,abilityIndex in pairs(CharacterConstant.ABILITY) do
		self.abilityState[abilityIndex] = 1
	end
    self.isCanMove = true

    -- 状态机初始化
    self.stateMachine = CharacterStateMachine:create()
    self.stateMachine:init( self )
    self:setFrameSpeed(self.movespeed)
    self.state = CharacterConstant.STATE_IDLE
    self.findPathState = CharacterConstant.FINDPATH_STATE_SUCCESS--寻路状态
    self:initEvent()
end

function CharacterBase:setData( data )
    -- body
    self:initFightData()
    for k,v in pairs( data ) do
		self:changeProperty(k,v)
    end
    self.defaultArmor = self.superarmor
    self.defaultTurnspeed = self.turnspeed
    self:initSkill()
end

-- 初始化战时属性
function CharacterBase:initFightData()
    -- body
    self.motionInfo = {curFrame = 0, stopMotionFrame = 0, collideState = 0 }
    self.attackIndex = 1
    self.skillIndex = 0
    self.skillstate = CharacterConstant.SKILLSTATE_NONE
    self.superarmor = self.superarmor or 0
    self.hatredmap:clearAllHatred()
    self.fireSerialNum = 0 --技能释放序号
end

-- 初始化技能配置
function CharacterBase:initSkill()
    -- body
    if self.passSkill ~= nil and self.passSkill ~= "0" and self.passSkill ~= "" then --被动技能
        self.passSkillList = StringUtils.split( self.passSkill, "|" )
        local tempList = {}
        for k,v in ipairs( self.passSkillList ) do
            tempList = StringUtils.split( v, "+", nil, tonumber )
            self.passSkillList[k] = { id=tempList[1], level=tempList[2] }
        end
	else
		self.passSkillList = nil
    end
end

--初始化事件
function CharacterBase:initEvent()
    self.event = Event:create( self )
end

--角色移动toPos:目标位置 callback:移动完的回调,可以为nil
function CharacterBase:moveTo(toPos,callback)
    if self.isCanMove == false or self.movespeed == 0 then
        return
    end
	if self:isAbilityEnable(CharacterConstant.ABILITY.MOVE) == false then
    	return
	end

    if CharacterUtil.sqrDistanceWithoutY(self:getPosition(), toPos) >= CharacterConstant.SQR_RUN_PRECISION then 
        self:switchState(CharacterConstant.STATE_RUN)
        self.stateMachine:invoke("moveTo",{toPos,callback})
	else
        self:setPosition(toPos)
		self:switchState(CharacterConstant.STATE_IDLE)
		if callback then
			callback()
		end
    end
end


--移动motion位移量，单位为vector3，doSample，是否同步到地表,用于RunState调用
function CharacterBase:move(motion,doSample)
    self.tempPos = self.tempPos or Vector3.zero
    self.tempPos.x = self.position.x + motion.x
    self.tempPos.y = self.position.y + motion.y
    self.tempPos.z = self.position.z + motion.z
    return self:setPosition(self.tempPos)
end

function CharacterBase:setCanMove( isActive )
    -- body
    self.isCanMove = isActive
end

function CharacterBase:changeProperty( key, value, isForce )
    -- body
    if key == ConstantData.ROLE_CONST_HP and value < 0 then
        value = 0
    end
    self[key] = value
    if key == ConstantData.ROLE_CONST_HP then
        if value > 0 and self.pSkillHandler ~= nil then
            self.pSkillHandler:hpUpdate()   --触发被动技能
        elseif value == 0 and self.state ~= CharacterConstant.STATE_HITFLY and self.state ~= CharacterConstant.STATE_DEAD 
                and FightModel:canControlDead() and FightModel:canControlMonsterDead(self.characterType) then
            if self.characterType == CharacterConstant.TYPE_MONSTER then
                if self.view then
                    self.view:removeBornEffect()
                end
            end
            self:switchState( CharacterConstant.STATE_DEAD )
        end
        if value == 0 and isForce ~= true and (FightModel:canControlDead() == false or FightModel:canControlMonsterDead(self.characterType) == false) then --客户端不能控制死亡
            self.hp = 1
            ModuleEvent.dispatchWithFixedArgs( ModuleConstant.AI_DESTROY, self.uniqueID, self.characterType)
        end
	elseif key == ConstantData.ROLE_CONST_INVINCIBLE then
		if value == 0 and self.charaBuff and self.charaBuff.invincible == 1 then
			value = 1
			self.invincible = value
		end
    elseif key==ConstantData.ROLE_CONST_OUTSHOW then
        if self.view then
            self.view:ChangeOutShowInfo(value)
        end
    end
    if( self.view) then
        if key == ConstantData.ROLE_CONST_INVINCIBLE then
			if self.characterType == CharacterConstant.TYPE_MONSTER or self.characterType == CharacterConstant.TYPE_PARTNER then
				if self.view.blood then
					self.view.blood:setVisible(value == 0 and true or false)
				end
			end
			if value == 0 then
				if self.charaBuff and self.charaBuff.invincible and self.charaBuff.invincible > 0 then
					self.invincible = 1
				end
			end
        end
    end
end

--简单直接设置位置, 不考虑是否可走区域;
function CharacterBase:setSuperPosition(pos)
    self.position = pos;
    if self.view ~= nil then
        self.view:setPosition(self.position)
    end
end

function CharacterBase:setPosition(pos)
    if self:isAbilityEnable(CharacterConstant.ABILITY.MOVE) == false then
        return false
    end
    if PathFinder.isPosValid(pos) == false then
        return false
    end
    local result = true
    local hitFlyOrDead = self.state == CharacterConstant.STATE_HITFLY or self.state == CharacterConstant.STATE_DEAD
    if PathFinder.isBlockValid(pos) == false or hitFlyOrDead then
        local hitHigh = pos.y --保存击飞高度
        if (hitFlyOrDead) and self.defaultPosition then
            pos.y = self.defaultPosition.y --避免在空中调sample
        end
        local samplePos = PathFinder.samplePositionValid(pos)--防止陷入地表
        local fixedPos = samplePos
        -- 宠物无视阻挡
        if self.characterType ~= CharacterConstant.TYPE_PARTNER then
            if self.position and DynamicBlockManager.hasActiveBlock() then 
                local isObstacles = 0
                isObstacles = NavigationUtil.checkObstacles(fixedPos.x,fixedPos.y,fixedPos.z,isObstacles)
                if isObstacles == 1 then
                    result = false
                    fixedPos = RoleControllerUtil.rayToObstacles2(self.position.x,self.position.y,self.position.z, fixedPos.x,fixedPos.y,fixedPos.z)
                end
            end
        end
        self.position = fixedPos
        self.position.y = samplePos.y
        if (self.state == CharacterConstant.STATE_HITFLY or self.state == CharacterConstant.STATE_DEAD) and hitHigh > self.position.y then--允许击飞时浮空
            if (hitHigh - self.position.y) >= 2 then --限制最高击飞高度
                self.position.y = self.position.y + 2
            else
                self.position.y = hitHigh
            end
        end
        if self.state == CharacterConstant.STATE_IDLE then --保存待机状态下的位置
            self.defaultPosition = self.position
        end
        if self.view ~= nil then
            self.view:setPosition(self.position)
        end
    end
    if result then
        if self.characterType==CharacterConstant.TYPE_PLAYER or self.characterType==CharacterConstant.TYPE_SELF then
            --通知角色移动了
            ModuleEvent.dispatchWithFixedArgs(ModuleConstant.PLAYER_MOVE,self.uniqueID,self.position)
        end
    end
    return result
end

function CharacterBase:getPosition()
    return self.position or Vector3(0,0,0)
end

function CharacterBase:lookAt(dir)
    if self.view then
        self.view:lookAt(dir)
    end
end

function CharacterBase:lookAtSmooth(dir,minAngles,speed)
    if self.view then
        self.view:lookAtSmooth(dir,minAngles,speed)
    end
end

function CharacterBase:lookAtSmoothByRotation( rot, minAngles, speed )
    if self.view then
        self.view:lookAtSmoothByRotation( rot, minAngles, speed )
    end
end

function CharacterBase:playAni(animaName,duration)
    if self.view then
        self.view:playAni(animaName,duration)
    end
end

function CharacterBase:setRotation( value )
    -- body
    if value == nil then return end
    self.rotation = value
    if self.view ~= nil then
        self.view:setRotation( value )
    end
end

function CharacterBase:getRotation()
    return self.rotation
end

function CharacterBase:faceToTarget()
    -- body
    if self.turnspeed == 0 then return end
    if self.view then
        self.view:stopSmoothTurn()
    end
    if self.targetID ~= nil then
        local target = CharacterManager:getCharacByUId( self.targetID )
        if target ~= nil then
            local direction = CharacterUtil.faceToInTween( self, target:getPosition() )
            if direction ~= nil then
                self:setRotation( direction )
            end
        end
    elseif self.joystickDirec ~= nil then
        self:setRotation( Quaternion.LookRotation(self.joystickDirec) )
    end
end

function CharacterBase:requestFire( skillid, level, targetID, requestByAI )
    local skillData = FightModel:getSkillData( skillid )
    if skillData == nil then
        return
    end
    if self:checkState( skillData ) == false then
		return
	end
    if self.position == nil then
        return
    end
    self.targetID = targetID
	requestByAI = requestByAI or 0
    if skillData.skilltype == FightDefine.SKILLTYPE_AVOID then --闪避技能无需目标直接释放
        self.fireSerialNum = self.fireSerialNum + 1
		local firePosX = math.round(self.position.x, 2)
		local firePosZ = math.round(self.position.z, 2)
        ModuleEvent.dispatch(ModuleConstant.CHARAC_FIREREQUEST,{self.uniqueID, skillid, level, "0", firePosX, firePosZ, self.fireSerialNum, requestByAI})
        return
    end

    local target = CharacterManager:getCharacByUId( self.targetID )
    if self.targetID ~= nil and (target == nil or target.state == CharacterConstant.STATE_DEAD) then
        self:findTarget( skillid ) --目标不合法 重新寻找目标
    end
    if self:checkTarget( skillData, level ) == true then
        self.fireSerialNum = self.fireSerialNum + 1
		local firePosX = math.round(self.position.x, 2)
		local firePosZ = math.round(self.position.z, 2)
        ModuleEvent.dispatch(ModuleConstant.CHARAC_FIREREQUEST,{self.uniqueID, skillid, level, self.targetID or "0", firePosX, firePosZ, self.fireSerialNum, requestByAI})
    end
end

function CharacterBase:fire( skillid, level, targetID, pos, fireSerialNum, requestByAI )
    -- body
    local skillData = FightModel:getSkillData( skillid )
	if targetID == "0" then 
		targetID = nil 
	end
    self.targetID = targetID
    self.fireSerialNum = fireSerialNum
	if pos and EnvironmentHandler.isPvpClient then
		local sqrDis = CharacterUtil.sqrDistance(pos, self.position)
		-- 小于0.2 或 大于 8 米时释放技能时瞬移
		if sqrDis <= CharacterConstant.SQR_RUN_PRECISION or (sqrDis >= 64 and self.characterType ~= CharacterConstant.TYPE_MONSTER) then
			self:setPosition(pos)
		end
	end
    if self:checkTarget( skillData, level ) == false then
        return
    end
    if requestByAI == 1 then --AI触发的需要考虑寻路 玩家点击触发则直接释放
        if self:runToTarget( skillData, level ) == false then
            self:onFire( skillid, level )
        end
    else
        self:onFire( skillid, level )
    end
end

function CharacterBase:onFire( skillid, level )
    -- body
    self:stopSearch()
    self:faceToTarget()
    if AIManager.hasAI(self.uniqueID) then
        ModuleEvent.dispatch(ModuleConstant.AI_CLEARROUNDTIME, self.uniqueID)
    end
    if self.orderAI and self.skillIndex then
        self.orderAI:beginCDByIndex(self.skillIndex)
    end
	if self.cacheUsingSkillTb == nil then
		self.cacheUsingSkillTb = {}
	end
    self.usingSkill = self.cacheUsingSkillTb
	self.usingSkill.id = skillid
	self.usingSkill.level = level or 1
    ModuleEvent.dispatchWithFixedArgs( ModuleConstant.SKILL_FIRE, self.uniqueID, self.targetID, skillid, 1, level or 1, CharacterConstant.STATE_ATTACK, self.fireSerialNum )
end

-- 释放成功
function CharacterBase:fireSuccess()
    -- body
end

function CharacterBase:checkState( skillData )
    -- body
    if self.state == CharacterConstant.STATE_HITBACK or self.state == CharacterConstant.STATE_HITFLY then
        return false    -- 被击中时无法进入攻击状态
    end
    if self.state == CharacterConstant.STATE_STANDUP and skillData.skilltype ~= FightDefine.SKILLTYPE_AVOID then
        return false    -- 非闪避技能无法在起身状态使用
    end
    if self.skillstate and (self.skillstate == CharacterConstant.SKILLSTATE_BORN or self.skillstate == CharacterConstant.SKILLSTATE_ACTIVE) then
        return false    -- 出场或激活时不可被打断
    end

    if skillData.skilltype == FightDefine.SKILLTYPE_NORMAL then
        if self.skillstate ~= CharacterConstant.SKILLSTATE_NONE then
            -- 非待机或收招时无法普攻
            return false
        end
		-- 普攻被禁用
		if self:isAbilityEnable(CharacterConstant.ABILITY.NORMAL_SKILL) == false then
			return false
		end
    elseif skillData.skilltype == FightDefine.SKILLTYPE_SKILL then
        if self.skillstate == CharacterConstant.SKILLSTATE_AVOID or 
            self.skillstate == CharacterConstant.SKILLSTATE_ULTI or
            self.skillstate == CharacterConstant.SKILLSTATE_SKILL then
            -- 闪避或大招过程中无法使用技能
            return false
        end
		if self:isAbilityEnable(CharacterConstant.ABILITY.SKILL) == false then
			return false
		end
    elseif skillData.skilltype == FightDefine.SKILLTYPE_AVOID then
        if self.skillstate == CharacterConstant.SKILLSTATE_ULTI then
            -- 大招时无法闪避
            return false
        end
		if self:isAbilityEnable(CharacterConstant.ABILITY.SKILL) == false then
			return false
		end
	elseif skillData.skilltype == FightDefine.SKILLTYPE_ULTIMATE then
		if self:isAbilityEnable(CharacterConstant.ABILITY.SKILL) == false then
			return false
		end
    end
    return true
end

-- 检测目标是否适用
function CharacterBase:checkTarget( skillData, level )
    -- body
    if skillData.skilltarget ~= FightDefine.TARGETTYPE_NONE then
        if self.targetID == nil or (CharacterManager:getCharacByUId(self.targetID)==nil) or skillData.skilldistance == 0 then
            return false
        end
    end
    return true
end

function CharacterBase:runToTarget( skillData, level )
    if self.targetID ~= nil then
        local target = CharacterManager:getCharacByUId( self.targetID )
        if target and skillData.skilldistance ~= 0 then
            local targetPos = CharacterUtil.getTargetPos( self, target, skillData, self.framespeed*3 )
            if targetPos ~= nil then
                self:beginSearch( target, skillData, level )
                return true
            end
        end
    end
    return false
end

-- 寻找一个技能目标
function CharacterBase:findTarget( skillid )
    -- body
    local skillData = FightModel:getSkillData( skillid )
    if skillData.skilltarget == FightDefine.TARGETTYPE_NONE then
        self.targetID = CharacterUtil.getNearestEnemy( self, skillData.targetdistance )
    else
        if skillData.skilltarget == FightDefine.TARGETTYPE_NEAREST then
            self.targetID = CharacterUtil.getNearestEnemy( self, skillData.targetdistance )
        elseif skillData.skilltarget == FightDefine.TARGETTYPE_RANDOM then
            self.targetID = CharacterUtil.getRandomEnemy( self, skillData.targetdistance )
        end
    end
    if self.targetID ~= nil then
        if self.joystickDirec and skillData.joystickarea then
            local joystickTarget = CharacterUtil.getCloseJoystickEnemy( self, self.joystickDirec, skillData.joystickarea )
            self.targetID = joystickTarget or self.targetID
        end
    end
end

function CharacterBase:switchState( state )
    if self.stateMachine and (self.state ~= CharacterConstant.STATE_RUN or state ~= CharacterConstant.STATE_RUN) then
        self.stateMachine:stateTransition( state )
    end
end

-- 主动停止当前状态
function CharacterBase:stopCurState()
    -- body
    if self.state == CharacterConstant.STATE_HITBACK
        or self.state == CharacterConstant.STATE_HITFLY
        or self.state == CharacterConstant.STATE_STANDUP then
        -- 被打中时无法主动停止
        return
    end
    self:stopSearch()
end

-- 当前状态自然运行结束
function CharacterBase:stateEnd()
	-- body
	self.stateMachine:stateEnd()
end

function CharacterBase:stateInvoke( funcName, param )
    -- body
    self.stateMachine:invoke( funcName, param )
end

function CharacterBase:getSkillTime()
    -- body
    return self.skillTime
end

function CharacterBase:isAnimPaused()
    -- body
    if self.view ~= nil then
        return self.view:isAnimPaused()
    end
    return self.isPause
end

--恢复
function CharacterBase:resume( )
    if self.view ~= nil then
        self.view:resume( false )
    end
    self.isPause = false
end

--暂停
function CharacterBase:pause( )
    if self.view ~= nil then
        self.view:pause( true )
    end
    self.isPause = true
end

function CharacterBase:setActive(isActive)
    if self.isActive ~= isActive then
        self.isActive = isActive
        if self.view then
            self.view:setActive(self.isActive , self.isShowModelView)
        end
    end 
end

--设置模型显示，用于周围玩家
function CharacterBase:setModelViewShow(isShow)
    self.isShowModelView = isShow
    if self.view then
        --print("uid:" , self.uniqueID , " self.isActive:" , self.isActive , " isActive:" , self.view._isActive , " modelshow:" , self.isShowModelView)
        if self.isActive ~= false and self.view._isActive ~= self.isShowModelView then
            self.view:setActive(true , self.isShowModelView)
        end        
    end
end

function CharacterBase:setBoneEffect( param )
    -- body
    if self.view then
        self.view:setBoneEffect( param )
    end
end

function CharacterBase:removeBoneEffect()
    -- body
    if self.view then
        self.view:removeBoneEffect()
    end
end

function CharacterBase:showBright()
    -- body
    if self.view then
        self.view:showBright()
    end
end

-- 添加残影效果
-- interval 每个残影出现的间隔时间(秒)
-- duration 残影存在的部时间(秒)
-- perTime 每个残影从出现到消失的时间(秒)
-- alpha 残影的初始透明度(0-1)
function CharacterBase:showGhostShadow(interval, duration, perTime, alpha)
	if self.view then
		self.view:showGhostShadow(interval, duration, perTime, alpha)
	end
end

function CharacterBase:removeGhostShadow()
	if self.view then
		self.view:removeGhostShadow()
	end
end

function CharacterBase:beginSearch( target, skillData, level )
    -- body
    self:stopSearch()
    local targetPos = nil
    local function searchFunction()
        -- body		
        if target.uniqueID then
            targetPos = CharacterUtil.getTargetPos( self, target, skillData, self.framespeed*3 ) 
            local selfPos = self:getPosition()
            if type(selfPos) == "number" or targetPos == nil then --出现异常
                self:onFire( skillData.skillid, level )
            else
                self:moveTo( targetPos )
            end
        end
    end
    searchFunction()
    self.searchKey = FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(3,0,searchFunction)
end

function CharacterBase:stopSearch()
    -- body
    if self.searchKey ~= nil then
        FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):removeLuaFunc( self.searchKey )
        self.searchKey = nil
    end
end

-- 获取角色的某项能力是否可用
function CharacterBase:isAbilityEnable(abilityType)
	if abilityType == CharacterConstant.ABILITY.MOVE then
		-- 击飞击退不受移动能力状态控制
		if self.state == CharacterConstant.STATE_HITBACK or self.state == CharacterConstant.STATE_HITFLY then
			return true
		end
	end
	if self.abilityState[abilityType] == 0 then
		return false
	end
	if self.charaBuff and self.charaBuff.controls and self.charaBuff.controls[abilityType] == 0 then
		return false
	end
	return true
end

-- 开启或禁用角色某项能力
-- abilityType : CharacterConstant.ABILITY
-- enableFlag : 0 禁用  1 启用   默认为1
function CharacterBase:setAbilityEnable(abilityType, enableFlag)
	if self.abilityState[abilityType] then
		self.abilityState[abilityType] = enableFlag or 1
	end
end

-- 获取角色当前的属性列表
function CharacterBase:getCurrentAttrib()
	if self.currentAttrib == nil then
		self.currentAttrib = Attribute:create()
	end
	for _,attribKey in ipairs(AttrEnum) do
		self.currentAttrib[attribKey] = self[attribKey]
	end
	self.currentAttrib[ConstantData.ROLE_CONST_HP] = self.maxhp
	if self.charaBuff and self.charaBuff.attrib then
		for k,v in pairs(self.charaBuff.attrib) do
			if self.currentAttrib[k] then
				self.currentAttrib[k] = self.currentAttrib[k] + v
			else
				self.currentAttrib[k] = v
			end
		end
	end
	return self.currentAttrib
end

function CharacterBase:clearSkillCD()
    -- body
end

function CharacterBase:setFrameSpeed(moveSpeed)
	if moveSpeed == nil then return end
	self.framespeed = moveSpeed / ConstantData.FRAME_RATE
	if self.framespeed < 0 then
		self.framespeed = 0
	end
    if self.stateMachine ~= nil then
        self.stateMachine.speed = self.framespeed
        self.stateMachine:invoke("onSpeedUpdate")
    end
end

function CharacterBase:isInSkillState()
	if self.state == CharacterConstant.STATE_ATTACK or 
		self.state == CharacterConstant.STATE_HITBACK or 
		self.state == CharacterConstant.STATE_HITFLY then
		return true
	end
	return false
end

function CharacterBase:revive()
    if self.state ~= CharacterConstant.STATE_DEAD then
        return
    end
	self.state = CharacterConstant.STATE_IDLE
    self:changeProperty(ConstantData.ROLE_CONST_HP, self.maxhp )
    self:clearSkillCD()
	self.skillstate = CharacterConstant.SKILLSTATE_NONE
	self.ctrlState = FightDefine.CONTROL_DEFAULT
    self:setFrameSpeed( self.movespeed )
	self.attackIndex = nil
	self.isCanMove = true
    self:switchState( CharacterConstant.STATE_IDLE )
    if FightModel:getFightStageType() == ConstantData.STAGE_TYPE_TEAM or
        FightModel:getFightStageType() == ConstantData.STAGE_TYPE_LOOTTREASURE_PVP then
        self:setPosition( self.bornPosition ) --设置为出生位置
    end
    if self.isRobot and self.isRobot == true then
        self:switchAutoFight(true)
    end
end

-- 取自身总的霸体
function CharacterBase:getSuperArmor()
	if self.charaBuff and self.charaBuff.superArmor then
		return (self.superarmor + self.charaBuff.superArmor)
	end
	return self.superarmor
end

-- 按给定的一组路点移动
function CharacterBase:moveFollowPaths(pathPoints, callback, stepCallback)
	self.followPathPoints = pathPoints
	if self.followPathPoints == nil or #self.followPathPoints == 0 then
		if callback then callback(self) end
	else
		self.followPathIndex = 0
		local moveNext = nil
		moveNext = function()
			if self.followPathIndex > 0 then
				if stepCallback then stepCallback(self) end
			end
			self.followPathIndex = self.followPathIndex + 1
			if self.followPathPoints[self.followPathIndex] == nil then
				if callback then callback(self) end
				return
			end
			self:moveTo(self.followPathPoints[self.followPathIndex], moveNext)
		end
		moveNext()
	end
end

function CharacterBase:changeModel(model , borneffect)
    if self.view then
        self.view:changeModel(model , borneffect)
        self.model = model;
        self.borneffect = borneffect;
    end
end

function CharacterBase:dispose()
    if self.view then
        self.view:dispose()
        self.view = nil
    end
    if self.event then 
        self.event:removeEvent()
        self.event = nil
    end
    if self.stateMachine then
        self.stateMachine:dispose()
        self.stateMachine = nil
    end
    if self.hatredmap ~= nil then
        self.hatredmap:dispose()
        self.hatredmap = nil
    end
    if self.pSkillHandler ~= nil then
        PassSkillManager.removeHandler( self )
    end
    ModuleEvent.dispatch( ModuleConstant.SKILL_DESTROY, self.uniqueID)
	ModuleEvent.dispatchWithFixedArgs( ModuleConstant.AI_DESTROY, self.uniqueID, self.characterType)
    self:stopSearch()
	self.charaBuff = nil
    self.normalAttack = nil
    self.skillAttack = nil
    self.avoidSkill = nil
    self.ultiAttack = nil
	self.allskill = nil	
    self.passSkillList = nil
    self.hitsound = nil
	self.passSkill = nil
    self.isCanBeSelect=nil
    self.istrap = nil
    self.isIgnoreFilter = nil
    self.isActive=nil
    self.isShowModelView = nil
    self.deityweaponType=nil
    self.deityWeaponId=nil
    self.stopAngle = nil
    self.drop = nil
    self.isRobot = nil
    self.delayShowDeadTime = nil
    self.targetID = nil
	self.cacheUsingSkillTb = nil
    self.uniqueID = nil
    self.orderIndex = nil
    self.skillIndex = nil
    self.lastPathAngle = nil
end
