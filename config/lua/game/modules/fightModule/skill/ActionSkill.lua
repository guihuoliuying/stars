-- region
-- Date    : 2016-06-16
-- Author  : daiyaorong
-- Description :  
-- endregion

ActionSkill = {
	sType = FightDefine.POOLTYPE_CHARAC,
	bulletIndex = nil,		--弹道数据下标
}

ActionSkill = createClassWithExtends( ActionSkill, BaseSkill )

function ActionSkill:setData( targetID, skillID, index, skillLv, fireSerialNum )
	-- body
	BaseSkill.setData(self,targetID, skillID, index, skillLv, fireSerialNum)
	self.bulletIndex = 1
end

function ActionSkill:updateFunctions()
	-- body
	if self.skillData.effecttype == FightDefine.SKILLEFF_CHARAC then
		self:motionUpdate()
		self:checkHitTime()
		self:nextAction()
	end
	self:showWarning()
	self:showEffect()
	self:createBullt()
	self:checkResttime()
	self:playSound()
end

function ActionSkill:initMotionData()
	-- body
	if self.skillData.effecttype == FightDefine.SKILLEFF_BULLET then
		return
	end
	BaseSkill.initMotionData(self)
	if self.targetID ~= nil and self.skillData.skilltype == FightDefine.SKILLTYPE_NORMAL then
		self.motionData.needCollide = true
	else
		self.motionData.needCollide = false
	end
	local charaState = self.charac.state
	if charaState == CharacterConstant.STATE_HITFLY or charaState == CharacterConstant.STATE_DEAD then
		self.skillTime = self.motionData.totalFrame
	end
	if self.skillData["stateTime"] ~= nil then --击退需定制时间
		self.skillTime = self.skillData["stateTime"]
	end
	if charaState == CharacterConstant.STATE_HITFLY or
		charaState == CharacterConstant.STATE_HITBACK then
		if self.skillData["faceDirection"] ~= nil then	-- 被击飞击退时面朝方向
			self.charac:setRotation( self.skillData["faceDirection"] )
		end
	end
end

function ActionSkill:initEffect()
	-- body
	self.effectRecord = nil
	self.charac:removeBoneEffect()
	if self.skillData.effectinfo ~= nil then
		self.effectRecord = {}
	end
	self.warningRecord = nil
	if self.skillData.areawarning ~= nil then
		self.warningRecord = {}
	end
	self.actionTime = nil  --动作时间列表
	self.actionIndex = nil
	if self.skillData.action ~= nil and self.charac.state == CharacterConstant.STATE_ATTACK then --非攻击状态不走逻辑
		self.actionIndex = 1
		self.actionTime = { 0 }
		for k,v in ipairs( self.skillData.action ) do
			self.actionTime[k+1] = self.actionTime[k] + CFG.animatorClipArrTimeCheck.configs[self.charac.model][v] * ConstantData.FRAME_RATE
		end
	end
end

function ActionSkill:initSound()
	-- body
	if self.skillData.sound == nil then 
		self.soundData = nil
		return 
	end
	--这里声音会有多段
	self.soundData = {}
	for k,data in ipairs(self.skillData.sound) do 
		self.soundData[k] = data
	end 
end

function ActionSkill:showEffect()
	-- body
	if self.effectRecord ~= nil then
		for k,v in pairs( self.skillData.effectinfo ) do
			if self.effectRecord[k] ~= true and self.curSkillFrame >= v.beginTime then
				self.charac:setBoneEffect( v )
				self.effectRecord[k] = true
			end
		end
	end
	if self.actionTime ~= nil then
		if self.skillData.action[self.actionIndex] ~= nil and self.curSkillFrame >= self.actionTime[self.actionIndex] then
			self.charac:playAni( self.skillData.action[self.actionIndex], 0 )
			self.actionIndex = self.actionIndex + 1
		end
	end
end

-- 碰撞判定
function ActionSkill:onCollision()
	-- body
	if EnvironmentHandler.isPvpClient then
		return
	end
	local allEnemy = CharacterManager:getCharacByRelation( self.charac.uniqueID, self.charac.camp, CharacterConstant.RELATION_ENEMY )
	if allEnemy == nil then return end
	local hitCount = 0
	local hitSeriNum = 0
	local collisionInfo = self.skillData.collision[self.hitIndex]
	for k,v in pairs( allEnemy ) do
		if CollisionTool.isCollide( self.charac, v.uniqueID, collisionInfo ) == true then
			hitSeriNum = hitSeriNum + 1
			ModuleEvent.dispatch( ModuleConstant.SKILL_MAKEEFFECT, { self.characId, v.uniqueID, self.fireSerialNum, self.skillID, self.skillLv, self.hitIndex, nil, nil, nil, nil, hitSeriNum } )
			if self.charac.pSkillHandler ~= nil then --触发被动技能
				hitCount = hitCount + 1
				self.charac.pSkillHandler:skillHit( self.skillID, v.uniqueID, hitCount )
			end
		end
	end
end

function ActionSkill:onSpecailEffect()
	-- body
	if self.skillData.specialeffect ~= nil then
		local specialEff = self.skillData.specialeffect[self.hitIndex]
		if specialEff ~= nil and specialEff ~= "0" then
			if EnvironmentHandler.isInServer then
				return
			end
			if self.charac.characterType ~= CharacterConstant.TYPE_SELF and self.charac.characterType ~= CharacterConstant.TYPE_MONSTER 
				and (self.charac.characterType ~= CharacterConstant.TYPE_PARTNER or self.charac.masterUId ~= RoleData.roleId)then
				return --主角及其伙伴或怪物才震动
			end
			if specialEff.actionShakeId ~= nil then --震动
				CameraManager:shakeCam( specialEff.actionShakeId )
			end
			-- if self.skillData.specialeffect[self.hitIndex].blurinfo ~= nil then --模糊
				-- CameraManager:setBloomEnable(false)
				-- ModuleEvent.dispatch(ModuleConstant.CAMERA_DISABLERADIALBLUREFFECT);
				-- local param = self.skillData.specialeffect[self.hitIndex].blurinfo
				-- CameraManager:setRadialBlurEffect( 0, param.inPower, param.inTime, function()
				-- 	CameraManager:setRadialBlurEffect( param.outPower, 0, param.outTime, function()
				-- 		ModuleEvent.dispatch(ModuleConstant.CAMERA_DISABLERADIALBLUREFFECT);
				-- 		CameraManager:setBloomEnable(true)
				-- 	end )
				-- end )
			-- end
		end
	end
end

function ActionSkill:createBullt()
	-- body
	if self.skillData.bulleteffectinfo ~= nil then
		if self.skillData.bulleteffectinfo[self.bulletIndex] ~= nil then
			if self.curSkillFrame >= self.skillData.bulleteffectinfo[self.bulletIndex].beginTime then
				ModuleEvent.dispatchWithFixedArgs( ModuleConstant.SKILL_CREATEBULLET, self.characId, self.targetID, self.skillID, self.bulletIndex, self.skillLv, self.fireSerialNum )
				self.bulletIndex = self.bulletIndex + 1
			end
		end
	end
end

-- 检查后续行为
function ActionSkill:nextAction()
	-- body
	if not self.skillMove or not self.skillMove.endtime then 
		return 
	end 
	if self.curSkillFrame < self.skillMove.endtime then
		return
    elseif self.curSkillFrame == self.skillMove.endtime and self.skillData.skilltype > 0 then
        if self.charac.ctrlState and self.charac.ctrlState ~= FightDefine.CONTROL_NONE then
            self.charac:changeProperty( "ctrlState", FightDefine.CONTROL_NONE ) --结束摇杆操控
            if self.skillMove.tracktype == FightDefine.TRACK_AUTOFOLLOW then
            	self.charac:setFrameSpeed( self.charac.movespeed ) --还原摇杆移动速度
        	elseif self.skillMove.tracktype == FightDefine.TRACK_AUTOFACE then
        		self.charac:changeProperty( "turnspeed", self.charac.defaultTurnspeed )
        	elseif self.skillMove.tracktype == FightDefine.TRACK_AUTOFOLLOWTURN then
        		self.charac:setFrameSpeed( self.charac.movespeed ) --还原摇杆移动速度
        		self.charac:changeProperty( "turnspeed", self.charac.defaultTurnspeed )
            end
        end
	end
	if self.skillData == nil or self.skillData.movement ==nil then 
		return 
	end 
	-- 检查下一动作
	if self.skillData.movement[self.index+1] ~= nil then
		self.index = self.index + 1
		self:initMotionData()
		return
	end
end

-- 检测是否普攻收招
function ActionSkill:checkResttime()
	-- body
	if self.skillData.skilltype ~= FightDefine.SKILLTYPE_NORMAL then
		return
	end
	if self.skillData.resttime == nil or self.skillData.resttime == 0 then
		return
	end
	if self.curSkillFrame >= self.skillData.resttime then
		self.charac:changeProperty( "skillstate", CharacterConstant.SKILLSTATE_NONE )
	end
end

-- 技能预警
function ActionSkill:showWarning()
	-- body
	if EnvironmentHandler.isInServer then
		return
	end
	if self.skillData.areawarning ~= nil and self.warningRecord ~= nil then
		for k,v in ipairs( self.skillData.areawarning ) do
			if self.warningRecord[k] == nil and self.curSkillFrame >= v.beginTime then
				self.warningRecord[k] = SideEffectManager:getSerialNo()
				local effectName = FightDefine.WARNING_EFF[v.areaInfo[1]]
				local scale = nil
				local circlraAngle = nil
				local sectorOff = 0
				if v.areaInfo[1] == 1 then	--圆形
					scale = { x=v.areaInfo[2]/FightDefine.WARN_CIRCLE_R,y=1,z=v.areaInfo[2]/FightDefine.WARN_CIRCLE_R }
					circlraAngle = 360
				elseif v.areaInfo[1] == 2 then --矩形
					scale = { x=v.areaInfo[2]/FightDefine.WARN_CIRCLE_R,y=1,z=v.areaInfo[3]/FightDefine.WARN_CIRCLE_R }
				elseif v.areaInfo[1] == 3 then --扇形
					scale = { x=v.areaInfo[2]/FightDefine.WARN_CIRCLE_R,y=1,z=v.areaInfo[2]/FightDefine.WARN_CIRCLE_R }
					circlraAngle = v.areaInfo[3]
					sectorOff = v.areaInfo[3] * 0.5
				end
				local direction = nil
				if v.posType ~= FightDefine.ACTION_WORLDPOINT and v.posType ~= FightDefine.ACTION_BORNPOS then
					direction = self.charac:getRotation():Clone()
					direction.x = 0
					direction.z = 0
					direction = direction * Quaternion.Euler( 0,v.angle-sectorOff,0 )
				else
					direction = Quaternion.Euler( 0,v.angle-sectorOff,0 )
				end
				local effectNode = SideEffectManager.getFreeTable()
				effectNode.serialNo = self.warningRecord[k]
				effectNode.entityID = self.charac.uniqueID
				effectNode.effectResName = effectName
				effectNode.pos = PointCalculator.getInitPoint( self.characId, self.targetID, v.posType, v.pos, self.motionData )
				effectNode.direction = direction
				effectNode.speed = StageManager.getThingPool():getEffTimeByName( effectName ) / v.lifeTime
				effectNode.scale = scale
				effectNode.angle = circlraAngle
				effectNode.time = 999
				ModuleEvent.dispatch(ModuleConstant.PLAY_AUTOEFFECT_IN_WORLDSPACE, effectNode)
			elseif self.warningRecord[k] ~= nil and self.curSkillFrame >= v.endTime then
				self:removeWarning( k )
			end
		end
	end
end

function ActionSkill:removeWarning(key)
	-- body
	if self.warningRecord[key] ~= nil and self.warningRecord[key] ~= false then
		ModuleEvent.dispatch(ModuleConstant.STOP_SIDE_EFFECT, self.warningRecord[key])
		self.warningRecord[key] = false
	end
end

function ActionSkill:dispose()
	-- body
	if self.charac ~= nil then
		self.charac:removeBoneEffect()
	end
	if self.warningRecord ~= nil then
		for k,v in pairs( self.warningRecord ) do
			self:removeWarning( k )
		end
		self.warningRecord = nil
	end
	self.effectRecord = nil
	self.actionTime = nil
end