-- region
-- Date    : 2016-06-21
-- Author  : daiyaorong
-- Description :  
-- endregion

BulletSkill = {
	sType = FightDefine.POOLTYPE_BULLET,
	characId = nil,		--产生此子弹的父对象ID
	charac = nil,		--子弹对象
	index = nil,		--数据下标
}

BulletSkill = createClassWithExtends( BulletSkill, BaseSkill )

local hitcount = nil
local bulletHitIndex = nil

-- 判断与某个角色的碰撞
local function onJudgeCollision( self, targetID, allEnemy )
	-- body
	if CollisionTool.isCollide( self.charac, targetID, self.skillData.collision[self.index] ) == true then
		if self.bulletData.bulletType == FightDefine.BULLET_BOMB then  --爆炸型子弹是打击范围内的角色
			local hitSeriNum = 0
			for k,v in pairs( allEnemy ) do
				if CollisionTool.isCollideByCircle( self.charac, self.bulletData.hurtArea, v ) then
					hitSeriNum = hitSeriNum + 1
					ModuleEvent.dispatch( ModuleConstant.SKILL_MAKEEFFECT, { self.characId, v.uniqueID, self.fireSerialNum, self.skillID, self.skillLv, self.index, self.targetAttrib, self.charac, nil, nil, hitSeriNum } )
					if self.charac.pSkillHandler ~= nil then --触发被动技能
						hitcount = hitcount + 1
						self.charac.pSkillHandler:skillHit( self.skillID, targetID, hitcount )
					end
				end
			end
		else
			bulletHitIndex = bulletHitIndex + 1
			ModuleEvent.dispatch( ModuleConstant.SKILL_MAKEEFFECT, { self.characId, targetID, self.fireSerialNum, self.skillID, self.skillLv, self.index, self.targetAttrib, self.charac, nil, nil, bulletHitIndex } )
			if self.charac.pSkillHandler ~= nil then --触发被动技能
				hitcount = hitcount + 1
				self.charac.pSkillHandler:skillHit( self.skillID, targetID, hitcount )
			end
		end
		self.hasHit = self.hasHit + 1
		self.lastHit = self.curSkillFrame
		if self.bulletData.bulletType == FightDefine.BULLET_PIERCE then	--只有穿透型才保存命中记录
			self.hitRecord[targetID] = self.hitRecord[targetID] + 1
		end
	end
end

function BulletSkill:init( characId, bullet )
	-- body
	self.characId = characId
	self.charac = bullet
end

function BulletSkill:setData( targetID, skillID, index, skillLv, fireSerialNum )
	self.targetID 	= targetID
	self.skillID 	= skillID
	self.index 		= index
	self.skillLv 	= skillLv
    self.fireSerialNum = fireSerialNum
	self.charac:init( self.characId, self.skillID, self.index )

	self.target 	= CharacterManager:getCharacByUId( self.targetID )
	self.skillData	= FightModel:getSkillData( self.skillID )
	self.bulletData = self.skillData.bulleteffectinfo[self.index]
	self.beginHitFrame = nil
	self.isFreeze   = false
	self.skillTime 	= self.bulletData.lastTime
	self.curSkillFrame 	= 0
	self.lastHit 	= 0
	self.hasHit		= 0
	self.hitIndex   = self.index

	-- 子弹创建时就先确定玩家属性
	local bulletOwner = CharacterManager:getCharacByUId(self.characId)
	self.targetAttrib = bulletOwner:getCurrentAttrib():clone()
	self.motionData = self.charac.motionInfo
	-- 计算初始值
	self:initMotionData()
	self.isActive = true
end

-- 计算初始运动属性
function BulletSkill:initMotionData()
	if self.skillData.effecttype == FightDefine.SKILLEFF_BULLET then
		BaseSkill.initMotionData(self)
		if self.skillMove ~= nil then
			self.isNeedRaycast = false
			self.charac:setActive( false )
			if self.motionData.speedDirec ~= nil then
				self.charac:changeProperty( "rotation", self.motionData.speedDirec )
			end
		end
	end
end

function BulletSkill:updateFunctions()
	-- body
	if self.skillData.effecttype == FightDefine.SKILLEFF_BULLET then
		self:motionUpdate()
		self:checkHitTime()
	end
end

-- 检测打击帧
function BulletSkill:checkHitTime()
	-- body
	if self.beginHitFrame == nil then
		self.beginHitFrame = self.skillData.hittime[self.index]
	end
	-- 到了打击时间才开始检测
	if self.curSkillFrame < self.beginHitFrame then
		return
	end
	if self.curSkillFrame == self.beginHitFrame or self.curSkillFrame > (self.lastHit + self.bulletData.hitFrameGap) then 
		self:onAddBuff()
		self:onCollision()
		self.lastHit = self.curSkillFrame
	end
end

-- 碰撞检测
function BulletSkill:onCollision()
	-- body
	if EnvironmentHandler.isPvpClient and self.bulletData.bulletType ~= FightDefine.BULLET_BOMB then --pvp客户端不进行碰撞检测，放在服务进行判断
		return
	end
	local allEnemy = CharacterManager:getCharacByRelation( self.charac.uniqueID, self.charac.camp, CharacterConstant.RELATION_ENEMY )
	if allEnemy == nil then return end
	hitcount = 0
	bulletHitIndex = 0
	for k,v in pairs( allEnemy ) do
		if self.bulletData.bulletType == FightDefine.BULLET_PIERCE then
			self.hitRecord 	= self.hitRecord or {}	--命中记录
			self.hitRecord[v.uniqueID] = self.hitRecord[v.uniqueID] or 0
			if self.hitRecord[v.uniqueID] < self.bulletData.hurtLimit then
				onJudgeCollision( self, v.uniqueID, allEnemy )
			end
		else
			onJudgeCollision( self, v.uniqueID, allEnemy )
			if self.bulletData.bulletType == FightDefine.BULLET_BOMB and self.hasHit >= 1 then
				-- 爆炸型一命中就结束
				if EnvironmentHandler.isInServer == false then
					local effectNode = SideEffectManager.getFreeTable()
					effectNode.entityID = self.charac.id
					effectNode.effectResName = self.bulletData.bombeff
					effectNode.pos = self.charac:getPosition()
					effectNode.direction = self.charac:getRotation()
					ModuleEvent.dispatch(ModuleConstant.PLAY_AUTOEFFECT_IN_WORLDSPACE, effectNode)
				end
				break
			end
		end
	end
end

function BulletSkill:checkLife()
	-- body
	if self.curSkillFrame >= self.skillTime or self.hasHit >= self.bulletData.maxHit then
		ModuleEvent.dispatch( ModuleConstant.SKILL_DESTROY, self.charac.id)
	end
end

function BulletSkill:dispose( )
	BaseSkill.dispose( self )
	self.charac:dispose() --连带子弹也一起销毁
	BulletPool.poolObject( self.charac )
	self.charac = nil
	self.hitRecord = nil
end