-- region
-- Date    : 2016-07-11
-- Author  : daiyaorong
-- Description :  主角类
-- endregion

MainCharacter = {
    characterType = CharacterConstant.TYPE_SELF,
    isAutoFight = nil,      --是否自动战斗
    skillCDKeys = nil,
    isPlayMoveSound = nil,  --是否播放跑动音效
    walkSound = nil,        --解析出来的跑动音效
    rideSound = nil,        --解析出来的跑动音效
    moveSoundKey = nil,     --跑动音效帧
}
MainCharacter = createClassWithExtends(MainCharacter, Player )

function MainCharacter.create( param )
	local instance = MainCharacter:new()
	CharacterBase.create(instance,param)
    instance:registerRideState() -- 注册玩家坐骑事件
    instance:registerFashionState() -- 注册
    instance:registerDeityweaponState() --注册神兵改变事件
	return instance
end

function MainCharacter:init(playerData)
     Player.init(self,playerData)
     self:resetMoveSpeed(0)
     -- self.event:attachEvent(ModuleConstant.SHOW_PLAYER_RANGE,self.showPlayerRange)
end

function MainCharacter:initFightData()
    -- body
    Player.initFightData( self )
    --self.hp = self.maxhp
    if self.view then
        self.view:stopBright()
    end
    self:initSkill()
    if self.passSkillList ~= nil then
        local stageType = FightModel:getFightStageType()
        if stageType == ConstantData.STAGE_TYPE_CAMP_DAILY then
            PassSkillManager.addHandler( self, true )
        else
            PassSkillManager.addHandler( self )
        end
    end
    self.defaultPosition = self.position
end

function MainCharacter:initSkill()
    -- body
    Player.initSkill(self)
    if FightModel:getFightStageType() == ConstantData.STAGE_TYPE_CAMP_DAILY then
        return
    end
    if self.skill ~= nil and self.skill ~= "" then
        local tempList = StringUtils.split( self.skill, "|" )
        self.skillAttack = {}
        self.ultiAttack = nil
        self.avoidSkill = nil
        for key,value in ipairs( tempList ) do
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

end

function MainCharacter:replaceSkills(skill)
	self.lastSkills = self.skill
	self.skill = skill
	self:initSkill()
end

function MainCharacter:clearFightData()
    -- body
    self.defaultPosition = nil
    self.ctrlState = FightDefine.CONTROL_DEFAULT
    self:setCanMove( true )
    self:clearSkillCD()
    self:stopMove()
    self.stopAngle = nil
	if self.lastSkills then
		self.skill = self.lastSkills
		self:initSkill()
		self.lastSkills = nil
	end
	if self.pSkillHandler ~= nil then
        PassSkillManager.removeHandler( self )
    end
end

function MainCharacter:updateProperties( data )
    for k,v in pairs( data ) do
        if k == ConstantData.ROLE_CONST_HP and self.view ~= nil  then
            self.view:updateBlood(v)
        elseif k == ConstantData.ROLE_CONST_MAXHP and self.view ~= nil then
            self.view:updateMaxHp(v)
        elseif k==ConstantData.ROLE_CONST_OUTSHOW then
            if self.view then
                self.view:ChangeOutShowInfo(v)
            end
        end
        self[k] = v
    end
	if data.maxhp and self.characBuff and self.characBuff.targetSourceMaxHp then
		if self.characBuff.targetSourceMaxHp < data.maxhp then
			self.characBuff.targetSourceMaxHp = data.maxhp
		end
	end
    if self.maxhp ~= nil then
        self.maxhp = self.maxhp
    end
	ModuleEvent.dispatch(ModuleConstant.STAGE_FIGHT_HPUPDATE)
    self.defaultArmor = self.superarmor
end

function MainCharacter:updateSkill( param )
    -- body
    if param ~= nil then
        for key, value in ipairs( param ) do
            if value.posIndex == 3 then
                self.ultiAttack = { id=value.skillId, level=value.skillLv }
            else
                self.skillAttack = self.skillAttack or {}
                self.skillAttack[value.posIndex+1] = { id=value.skillId, level=value.skillLv }
            end
        end
    end
end

function MainCharacter:changeProperty( key, value, isForce )
    -- body
    Player.changeProperty(self,key,value,isForce)
    if key==ConstantData.ROLE_CONST_HP or key == ConstantData.ROLE_CONST_MAXHP then--lyt 20161019 只有hp改变才发消息
        ModuleEvent.dispatch(ModuleConstant.STAGE_FIGHT_HPUPDATE)
    end
end

function MainCharacter:setPosition( value )
    -- body
    ModuleEvent.dispatch( ModuleConstant.FIGHT_SCENE_UPDATEGUIDE )
    return CharacterBase.setPosition( self, value )
end

function MainCharacter:setRotation(rotation)
    CharacterBase.setRotation(self,rotation)
end

function MainCharacter:tryAttack( targetID )
    -- body
    local skillparam = nil
    local skillLvData = nil
    while true do
        skillparam = self:findSkill()
        if skillparam then
            skillLvData = FightModel:getSkillLevelData( skillparam.id, skillparam.level )
            if (self.skillCDKeys == nil or skillparam.id == nil or self.skillCDKeys[skillparam.id] == nil) 
                and skillLvData and DungeonModel:isDungeonPass(skillLvData.reqdungeon) then 
                -- 处于冷却中或未到等级则使用普攻
                if skillLvData.reqlv and self.level and (skillLvData.reqlv <= self.level) then
                    break
                end
            end
        end
        self:increaseOrderIndex()
    end
    self:requestFire( skillparam.id, skillparam.level, targetID, 1 ) 
end

-- 主角进入关卡前重置
function MainCharacter:resetEnterStage()

    self:resetFashionMain()
    -- 改坐骑后需要放在更改时装之后，因为更换坐骑需要更改角色动作，假如
    -- 顺序调换可能会在改动作的时候替换模型，导致出错
    self:resetRideMain()
    ModuleEvent.dispatch(ModuleConstant.ROLE_DEITYWEAPON_SYNCSTATE);
end

function MainCharacter:resetMoveSpeed(Type)
    self:setFrameSpeed(self.movespeed * RideControl.getRideMoveSpeed(Type))
end

-- 主角色坐骑重置
function MainCharacter:resetRideMain()
    if StageManager.isInShowRideStage() then
        if RoleData.rideId ~= -1 then
            local temp = RideModel.getRideOrgInfoById(RoleData.rideId)--CFG.rideinfo:get(RoleData.rideId)
            if temp ~= nil then
                self:playerRideOn(temp)--temp.model,temp.pose,temp.pose2,temp.namehigh
                self:resetMoveSpeed()
            end
        end
    else
        -- 主角色下马必须更改相机焦点
        self:playerRideOff()
        self:resetCamera()
        self:resetMoveSpeed(1)
    end
end

--[[
    pose:剩上坐骑时战力动作
    pose2:剩上坐骑时坐骑移动动作
]]
function MainCharacter:playerRideOn(rideData)--modelId,pose,pose2,nameHigh
    if self.view ~= nil then
       self.view:getOnRide( RoleData:getRoleId(), rideData)
    end
end

function MainCharacter:playerRideOff()
    if self.view ~= nil then
        self.view:getOffRide()
    end
end

function MainCharacter:resetCamera()
    if self.view ~= nil and self.view.avatar then
        CameraManager:setTargetTransform(self.view.avatar.transform)
    end
end

function MainCharacter:registerRideState()
    ModuleEvent.addListener(ModuleConstant.RIDE_SYNC_STATE,function(table)
        if table == nil then
            --主角色下马必须更改相机焦点
            self:playerRideOff()
            self:resetCamera()
            self:resetMoveSpeed(1)
        else
            self:playerRideOn(table.rideData)--table.modelId,table.pose,table.pose2,table.namehigh
            self:resetMoveSpeed(0)
        end
    end)
    -- 乘上坐骑是因等待模型载入完成，所以相机焦点需要在此更改
    ModuleEvent.addListener(ModuleConstant.RIDE_CAMERA_TARGET,function()
        self:resetCamera()
    end)
    --打开界面时重新调整坐骑方向
    ModuleEvent.addListener(ModuleConstant.RIDE_INIT_STATE , function (table)
        if table then
            if self.view ~= nil then
                self.view.rideModel:setActive(false) --初始时先隐藏再重新创建显示
                self.view:getOnRide(RoleData:getRoleId(),table.rideData)--table.modelId,table.pose,table.pose2,table.namehigh
            end
            self:resetMoveSpeed(0)
        end
    end)
end


-- 主角色时装重置
function MainCharacter:resetFashionMain()
    if StageManager.isInFightStage() ~= true then
        if RoleData:getFashionModel() ~= nil or self.isReplaceModel == true then
            self.isReplaceModel = nil --当在阵营日常战斗时,主角需要使用弓箭手作为替代模型
            self:playerFashionOn(RoleData:getFashionId(),RoleData:getModel(RoleConstant.MODEL_TYPE.NORMAL,RoleData:getFashionId(),RoleData:getJobId()))
        end
    elseif StageManager.isInFightStage() then
        -- 主角脱去时装
        self:playerFashionOff()
        self:resetCamera()
    end
end

function MainCharacter:playerFashionOn(fashionId,modelId)
    if self.view ~= nil then
       self.model = modelId
       self.view:putonFashion(fashionId,modelId);
    end
end

function MainCharacter:playerFashionOff()
    if self.view ~= nil then
        if StageManager.getCurStageType() ~= ConstantData.STAGE_TYPE_CAMP_DAILY then
            self.model = RoleData:getBaseModel()
            self.view:putoffFashion(RoleData:getJobId())
        else
            self.isReplaceModel = true --当在阵营日常战斗时,主角需要使用弓箭手作为替代模型
            self.model = RoleData:getBaseModel(nil , 4)
            self.view:putoffFashion(4,true)
        end
    end
end

function MainCharacter:registerFashionState()
    ModuleEvent.addListener(ModuleConstant.ROLE_FASHION_SYNCSTATE,function(t)
        if t==nil or t.modelId == nil then
            --主角色脱下衣服
            self:playerFashionOff()
            self:resetCamera()
        else
            self:playerFashionOn(t.fashionId_,t.modelId);
        end
    end)
    -- 乘上坐骑是因等待模型载入完成，所以相机焦点需要在此更改
    ModuleEvent.addListener(ModuleConstant.ROLE_FASHION_CAMERA,function()
        self:resetCamera()
    end)
end



function MainCharacter:registerDeityweaponState()
    local onDeityweaponState = function(self)
        if StageManager.getCurStageType() ~= ConstantData.STAGE_TYPE_CAMP_DAILY  then -- 当处于阵营日常战斗中，不需要刷新兵器
            self:resetDeityweapon(RoleData.jobId, RoleData:getFashionId(), RoleData:getDeityweaponType());
        end
    end
    ModuleEvent.addListener(ModuleConstant.ROLE_DEITYWEAPON_SYNCSTATE, makeHandle(self, onDeityweaponState));
    onDeityweaponState(self);
end

function MainCharacter:beginMove( targetRot )
    Player.beginMove(self , targetRot )
    self:setCharacterMoveState(true)    --主角是否跑动设置
end

function MainCharacter:stopMove()
    Player.stopMove(self)
    self:setCharacterMoveState(false)   --主角是否跑动设置
end



--播放音效
function MainCharacter:playSound()
    if RoleData.rideId > 0 and StageManager.isInShowRideStage() then
        self.rideSound = self.rideSound or {}
        if self.rideSound[RoleData.rideId] == nil then --初始化解析音效
            local config = CFG.rideinfo:get(RoleData.rideId)
            if config == nil or config.walksound == nil or config.walksound == "0" then return end
            local info = StringUtils.split(config.walksound , "|")
            if info == nil or info[1] == nil or info[2] == nil then return end
            self.rideSound[RoleData.rideId] = {}
            self.rideSound[RoleData.rideId].loopTime = tonumber(info[1])
            self.rideSound[RoleData.rideId].soundInfo  = StringUtils.split(info[2] , "," , nil ,function (value)
                return StringUtils.split(value , "+")
            end)
        end
        self:loopPlaySound(self.rideSound[RoleData.rideId].loopTime , self.rideSound[RoleData.rideId].soundInfo)
    else
        self.walksound = self.walksound or {}
        if self.walksound[RoleData.jobId] == nil then --初始化解析音效
            local jobVo = CFG.job:get(RoleData.jobId);            
            local resourceVo = CFG.resource:get(jobVo.modelres);
            if resourceVo == nil or resourceVo.walksound == nil or resourceVo.walksound == "0" then return end
            local info = StringUtils.split(resourceVo.walksound , "|")
            if info == nil or info[1] == nil or info[2] == nil then return end

            self.walksound[RoleData.jobId] = {}
            self.walksound[RoleData.jobId].loopTime = tonumber(info[1])
            self.walksound[RoleData.jobId].soundInfo  = StringUtils.split(info[2] , "," , nil ,function (value)
                return StringUtils.split(value , "+")
            end)
        end
        self:loopPlaySound(self.walksound[RoleData.jobId].loopTime , self.walksound[RoleData.jobId].soundInfo)
    end
end

--停止音效
function MainCharacter:stopSound()
    if self.moveSoundKey ~= nil then
        FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):removeLuaFunc( self.moveSoundKey )
        self.moveSoundKey = nil
    end
    if RoleData.rideId > 0 and StageManager.isInShowRideStage() then
        if self.rideSound and self.rideSound[RoleData.rideId] and self.rideSound[RoleData.rideId].soundInfo then
            for k,v in pairs(self.rideSound[RoleData.rideId].soundInfo) do
                AudioMgr.stopById(v[1])
            end
        end
    else
        if self.walkSound and self.walkSound[RoleData.jobId] and self.walkSound[RoleData.jobId].soundInfo then
            for k,v in pairs(self.walkSound[RoleData.jobId].soundInfo) do
                AudioMgr.stopById(v[1])
            end
        end
    end
end

--循环播放音效
function MainCharacter:loopPlaySound(loopTime , soundInfo)
    local curTime = 0
    local isPlaying = {}
    local playFunc = nil
    playFunc = function ()
        if curTime >= loopTime then
            curTime = 0
            isPlaying = {}
        end
        for i,v in ipairs(soundInfo) do
            if (not isPlaying[i]) and curTime >= tonumber(v[2]) then
                isPlaying[i] = true
                AudioMgr.Play(v[1])
            end
        end
        curTime = curTime + 30 --下一帧的时间
        self.moveSoundKey = FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(1,1,playFunc)
    end
    playFunc()
end

--设置主角移动状态，目前主要用于播放跑动音效
function MainCharacter:setCharacterMoveState(isMove)

    if StageManager.isInSafeStage() then --安全区才播放音效
        if isMove then
            if not self.isPlayMoveSound then
                self.isPlayMoveSound = true
                self:playSound()
                ----print("beginMove :")
            end
        else
            if self.isPlayMoveSound and (not Joystick:isJoystickUsing()) then
                self.isPlayMoveSound = nil
                self:stopSound()
                ----print("stopMove:")
            end
        end
    end
end

function MainCharacter:revive()
    -- body
    CharacterBase.revive(self)
    if FightModel:canControlAI() then
        AIManager.activeAllAI()
    end
    if EnvironmentHandler.environmentType == EnvironmentDef.DEFAULT or EnvironmentHandler.environmentType == EnvironmentDef.PVE_WEAKSYNC
        or EnvironmentHandler.environmentType == EnvironmentDef.PVP_CLIENT then
            if StageManager.getCurStageType() ~= ConstantData.STAGE_TYPE_CAMP_DAILY then
                --重新激活自动战斗
                ModuleEvent.dispatch(ModuleConstant.STAGE_FIGHT_AUTOFIGHT)
            end
    end
end

-- --显示周围玩家的范围，用于测试
-- function MainCharacter:showPlayerRange(radius)
--     if radius > 0 then
--         if self.view and (not IsNil(self.view.avatar)) then
--             if IsNil(self.rangeObj) then
--                 local rangeObj = GameObject.CreatePrimitive(UnityEngine.PrimitiveType.Cylinder)
--                 CommonFunc.SetParent(rangeObj, self.view.avatar.transform)          
--                 rangeObj.transform.localPosition = Vector3.New(0,0,0);
--                 rangeObj.transform.localRotation = Quaternion.Euler(Vector3.zero)
--             end
--             rangeObj.transform.localScale = Vector3.New(radius , 0.2 , radius);
--         end
        
--     else
--         if not IsNil(self.rangeObj) then
--             self.rangeObj:SetActive(false)
--         end
--     end
-- end
