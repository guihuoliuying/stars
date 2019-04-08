-- region
-- Date    : 2016-06-15
-- Author  : daiyaorong
-- Description : 技能管理器 
-- endregion

SkillManager = {
	runningSkill = nil,
	skillStartFrameRecords = nil,
}

-- 常用变量
local charac = nil
local skillData = nil
local enableSkillEffect = true	-- 是否启用技能效果（伤害，击飞击退等）
local tempVector = nil
local tempRot = nil

-- 检查攻击者
local function checkAttacker( characID, skillID, state )
	-- body
	charac = CharacterManager:getCharacByUId( characID)
	if charac == nil then
		return false
	end
	if charac.state == CharacterConstant.STATE_DEAD 
		and (state ~= CharacterConstant.STATE_HITBACK and state ~= CharacterConstant.STATE_HITFLY) then
		return false
	end
	return true
end

-- 获取技能动作时间
local function getSkillAniTime( model, actions )
	-- body
	local time = 0
	local timeConfig = CFG.animatorClipArrTimeCheck.configs[model]
	if timeConfig == nil then
		LogManager.LogError("模型没有动作时间配置 name="..tostring(model))
	end
	for k,v in ipairs( actions ) do
		if timeConfig[v] == nil then
			LogManager.LogError("模型没有动作时间配置 modelname="..tostring(model).."   action="..tostring(v))
			EnvironmentHandler.sendLogToServer("模型没有动作时间配置 modelname="..tostring(model).."   action="..tostring(v))
		end
		time = time + timeConfig[v] * ConstantData.FRAME_RATE
	end
	return time
end

function SkillManager:init()
	-- body
	enableSkillEffect = true
	self.runningSkill = {}
	self.skillStartFrameRecords = {}
	self:initEvent()
	SkillPool.init()
	BulletPool.init()
end

function SkillManager:initEvent( )
	self.event = Event:create( self )
	self.event:attachFixedArgsEvent(ModuleConstant.SKILL_FIRE, self.fireSkill )
	self.event:attachEvent(ModuleConstant.SKILL_FINISH, self.skillEnd )
	self.event:attachEvent(ModuleConstant.SKILL_DESTROY, self.skillDestroy )
	self.event:attachEvent(ModuleConstant.SKILL_MAKEEFFECT, self.makeEffect )
	self.event:attachFixedArgsEvent(ModuleConstant.SKILL_CREATEBULLET, self.createBullet)
	self.event:attachEvent(ModuleConstant.SKILL_SHOWEFFECT, self.showEffect, self.parseShowEffect )
end

function SkillManager:fireSkill( attackerID, targetID, skillID, index, skillLv, state, fireSerialNum)
	-- body
	local result = checkAttacker( attackerID, skillID, state )
	if result == true then
		-- 测试日志打印
		-- local logAtkId = attackerID
		-- if attackerID == RoleData.roleId then
		-- 	logAtkId = RoleData.name
		-- end
		-- local logTarId = targetID
		-- if targetID == RoleData.roleId then
		-- 	logTarId = RoleData.name
		-- end
		-- UILog("目标:"..tableplus.tostring(logTarId).." 攻击者:"..logAtkId.." 技能:"..skillID)

		-- 切换技能动作
		skillData = FightModel:getSkillData( skillID )
		if skillData == nil then
			--print("SkillManager:fireSkill():skillData is nill:attacker=" .. tostring(attackerID) .. ",skillid=" .. tostring(skillID))
			return
		end
		charac:changeProperty( "skillstate", skillData.skilltype )
		charac:changeProperty( "skillTime", getSkillAniTime( charac.model, skillData.action )  )
		if charac.state == CharacterConstant.STATE_DEAD then
			charac:stateInvoke( "onDeadHitAcitve", state )
		else
			charac:switchState( state )
		end
		
		local skill = self.runningSkill[attackerID]
		if skill == nil then
			skill = SkillPool.getObject(FightDefine.POOLTYPE_CHARAC)
			if skill == nil then
				skill = ActionSkill:create()
			end
			skill:init( attackerID )
			self.runningSkill[attackerID] = skill
		end
		skill:setData( targetID, skillID, index, skillLv, fireSerialNum )
		self:recordSkillStartFrame(attackerID, skill.fireSerialNum)
		ModuleEvent.dispatchWithFixedArgs(ModuleConstant.SKILL_REAL_START, attackerID, skill.fireSerialNum)
        if skillData.skilltype ~= 0 and charac.pSkillHandler then
            charac.pSkillHandler:fireSkill( targetID, skillID )
        end
	end
end

function SkillManager:skillEnd( uid )
	-- body
	charac = CharacterManager:getCharacByUId( uid )
	if charac == nil then return end
	charac:stateEnd()
end

function SkillManager:skillDestroy( uid )
	-- body
	if self.runningSkill[uid] ~= nil then
		local skill = self.runningSkill[uid]
		self.runningSkill[uid] = nil
		skill:dispose()
		SkillPool.poolObject( skill )
	end
end

function SkillManager:makeEffect( param )
	-- body
	if enableSkillEffect == false then
		return
	end
	if EnvironmentHandler.canCollide() == false then
 		return
	end
	PressureDispatcher.orderSkillEffect( SkillEffect.hitDamage, param )
end

function SkillManager:parseShowEffect( params )
	-- body
	if EnvironmentHandler.isPvpClient then
		local target = CharacterManager:getCharacByUId( params[2] )
	 	if target == nil then return end
		if target.uniqueID ~= RoleData.roleId then
			local targetPos = target:getPosition()
			if StageObjFilter.checkOnScreen( targetPos ) == false then --屏幕外不表现
				return
			end
		end
	end
	if tempVector == nil then
		tempVector = Vector3.zero
	end
	if tempRot == nil then
		tempRot = Quaternion.identity
	end
	tempVector:Set(params[7], params[8], params[9])
	tempRot:SetEuler(0, params[10], 0)
	params[7] = tempVector
	params[8] = tempRot
    params[9] = params[11]
	params[10] = params[13]
	params[11] = params[14]
	params[12] = params[15]
	params[13] = params[16]
    self:showEffect( params )
end

function SkillManager:showEffect( param )
	-- body
	SkillEffect.showSkillEffect( param )
end

function SkillManager:createBullet( attackerID, targetID, skillID, index, skillLv, fireSerialNum )
	-- body
	local bullet = BulletPool.getObject()
	if bullet == nil then
		bullet = Bullet:create()
	end
	local skill = SkillPool.getObject(FightDefine.POOLTYPE_BULLET)
	if skill == nil then
		skill = BulletSkill:create()
	end
	skill:init( attackerID, bullet )
	skill:setData( targetID, skillID, index, skillLv, fireSerialNum )
	self.runningSkill[bullet.id] = skill
end

-- 记录技能开始帧
function SkillManager:recordSkillStartFrame(attackerID, skillSerialNum)
	if skillSerialNum == nil then return end
	if self.skillStartFrameRecords[attackerID] == nil then
		self.skillStartFrameRecords[attackerID] = {}
	end
	self.skillStartFrameRecords[attackerID][skillSerialNum] = FightControlFactory.getControl().getCurrentFrame()
end

-- 取技能开始帧
function SkillManager:getSkillStartFrame(attackerID, skillSerialNum)
	if self.skillStartFrameRecords[attackerID] == nil then
		return nil
	end
	return self.skillStartFrameRecords[attackerID][skillSerialNum]
end

-- 取技能开始到现在经过的帧数
function SkillManager:getPassedFrameAfterSkillStart(attackerID, skillSerialNum)
	local startFrame = self:getSkillStartFrame(attackerID, skillSerialNum)
	if startFrame == nil then return 0 end
	return (FightControlFactory.getControl().getCurrentFrame() - startFrame)
end

function SkillManager:getRunningSkill(uid)
	-- body
	if self.runningSkill == nil then
		return
	end
	return self.runningSkill[uid]
end

function SkillManager:update()
	-- body
	if self.runningSkill == nil then return end
	for k,v in pairs(self.runningSkill) do
		v:update()
	end
end

function SkillManager:enableSkillEffect(isEnable)
	enableSkillEffect = isEnable
end

function SkillManager:dispose()
	-- body
	if self.runningSkill ~= nil then
		for k,v in pairs( self.runningSkill) do 
			v:dispose()
		end
		self.runningSkill = nil
	end
	self.skillStartFrameRecords = nil
	if self.event then
		self.event:removeEvent()
		self.event = nil
	end
	SkillPool.dispose()
	BulletPool.dispose()
end