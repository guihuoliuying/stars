-- region
-- Date    : 2016-06-15
-- Author  : daiyaorong
-- Description :  技能基类
-- endregion

BaseSkill = {
	characId = nil,		--技能释放者的ID
	charac = nil,		--技能释放者
	skillID = nil,
	skillData = nil,
	index = nil,		--技能动作数据下标
	skillLv = nil,		--技能等级
	hitIndex = nil,		--打击效果数据下标
	isActive = nil,
}

local TABLE_REMOVE = table.remove
local MATH_RAN = math.LogicRandom
local MATH_CEIL = math.ceil

BaseSkill = createClass( BaseSkill )

function BaseSkill:init( characId )
	-- body
	self.characId = characId
	self.charac = CharacterManager:getCharacByUId( characId )
end

function BaseSkill:setData( targetID, skillID, index, skillLv, fireSerialNum )
	-- body
	self.targetID 	= targetID
	self.skillID 	= skillID
	self.index 		= index
	self.skillLv	= skillLv
    self.fireSerialNum = fireSerialNum

	self.target 	= CharacterManager:getCharacByUId( self.targetID )
	self.skillData	= FightModel:getSkillData( self.skillID )
	self.isFreeze   = false
	self.skillTime 	= self.charac:getSkillTime()
	self.curSkillFrame 	= 0
	self.hitIndex 	= 1
	self.motionData = self.charac.motionInfo
	if self.target ~= nil then --保存技能开始时目标所在点
		self.motionData.targetInitPos = self.target:getPosition():Clone()
		self.motionData.targetInitRot = self.target:getRotation():Clone()
	else
		self.motionData.targetInitPos = self.charac:getPosition():Clone()
		self.motionData.targetInitRot = self.charac:getRotation():Clone()
	end

	self:initMotionData()
	self:initEffect()
	self:initSound()

	self.isNeedRaycast = false
	self.isActive = true
end

function BaseSkill:initMotionData()
	-- body
	if self.skillData == nil or self.skillData.movement == nil then
		self.skillMove = nil
		return 
	end 
	self.skillMove	= self.skillData.movement[self.index]
	if self.skillMove ~= nil then
		self.motionData = MotionCalculator.configInitData( self.characId, self.targetID, self.skillMove, self.motionData)
		self.motionData.curFrame = 0
		self.motionData.collideState = 0
		if self.isNeedRaycast == false and self.skillMove.tracktype then
			self.isNeedRaycast = FightDefine.RAYCAST_TRACK[self.skillMove.tracktype]
		end
	end
end

function BaseSkill:initEffect()
	-- body
end

function BaseSkill:initSound()
	-- body 
end

function BaseSkill:update()
	-- body
	if self.isActive ~= true then return end
	self:updateTime()
	if self.isFreeze then 
		return 
	end 
	self:updateFunctions()
	self:checkLife()
end

-- 定义每帧需要更新的行为
function BaseSkill:updateFunctions()
	-- body
end

-- 更新时间
function BaseSkill:updateTime()
	if self.motionData and self.motionData.stopMotionFrame > 0 then --定身计数
		self.motionData.stopMotionFrame = self.motionData.stopMotionFrame - 1
		if self.charac:isAnimPaused() == false then
			self.charac:pause()
		end
		self.isFreeze = true
	else
		if self.charac:isAnimPaused() == true then
			self.charac:resume()
		end
		self.isFreeze = false
		self.curSkillFrame = self.curSkillFrame + 1
	end
end

-- 计算运动信息
function BaseSkill:motionUpdate()
	if not self.skillMove then 
		return 
	end 
	-- 计算位置 运动属性
	if self.curSkillFrame >= self.skillMove.starttime then
		if self.motionData.curFrame == 0 then
            self.motionData = MotionCalculator.configInitData( self.characId, self.targetID, self.skillMove, self.motionData)
			self.charac:setPosition( self.motionData.startWorldPoint )
			self.skillMove.faceDirec = nil
			if self.skillData.direction ~= nil and self.skillData.direction[self.index] ~= nil then
				self.skillMove.faceDirec = self.skillData.direction[self.index]
				if self.skillData.direction[self.index] == FightDefine.DIRECTION_SPEED and self.motionData.speedDirec ~= nil then
					self.charac:setRotation( self.motionData.speedDirec )
				end
			end
            if self.charac.ctrlState ~= nil then --特定轨迹类型时可以操控摇杆移动或者转向
                if self.skillMove.tracktype == FightDefine.TRACK_AUTOFOLLOW then
                    self.charac:changeProperty( "ctrlState",FightDefine.CONTROL_MOVE )
                    self.charac:setFrameSpeed( self.skillMove.speedx ) --保持与配置速度一致
                elseif self.skillMove.tracktype == FightDefine.TRACK_AUTOFACE then
                    self.charac:changeProperty( "ctrlState",FightDefine.CONTROL_TURN )
                    self.charac:changeProperty( "turnspeed", self.skillMove.speedx/ConstantData.FRAME_RATE ) --单位是度每帧
                elseif self.skillMove.tracktype == FightDefine.TRACK_AUTOFOLLOWTURN then
                     self.charac:changeProperty( "ctrlState",FightDefine.CONTROL_TURN_MOVE )
                    self.charac:setFrameSpeed( self.skillMove.speedx ) --保持与配置速度一致                   
                end
            end
		end
		local curPos = self.charac:getPosition()
		local updateData = TrackCalculator.getUpdateData( self.skillMove, self.motionData, curPos, self.targetID, self.characId, self.charac )
		-- if updateData[2] then
		-- 	self.motionData = updateData[2]
		-- end
		local dstPos = updateData[1]
		if self.isNeedRaycast and DynamicBlockManager.hasActiveBlock() then 
			dstPos = RoleControllerUtil.rayToObstacles2(curPos.x,curPos.y,curPos.z, dstPos.x, dstPos.y, dstPos.z)
		end
		if dstPos and (dstPos.x ~= curPos.x or dstPos.z ~= curPos.z or dstPos.y ~= curPos.y) then
			self.charac:setPosition( dstPos )
		end
		if updateData[3] ~= nil then
			self.charac:setRotation( updateData[3] )
		end
		self.motionData.curFrame = self.motionData.curFrame + 1
	end
end

-- 打击帧判定
function BaseSkill:checkHitTime()
	-- body
	if self.skillData.hittime ~= nil and self.skillData.hittime[self.hitIndex] then
		if self.curSkillFrame >= self.skillData.hittime[self.hitIndex] then
			self:onSpecailEffect()
			self:onAddBuff()
			self:onCollision()
			self.hitIndex = self.hitIndex + 1
		end
	end
end

-- 碰撞判定
function BaseSkill:onCollision()
	-- body
end

--  特殊效果判定
function BaseSkill:onSpecailEffect()
	-- body
end

function BaseSkill:createBullt()
	-- body
end

-- 表现层效果
function BaseSkill:showEffect()
	-- body
end

function BaseSkill:playSound()
	-- body
	if self.soundData == nil then 
		return 
	end
	if EnvironmentHandler.isInServer then
		return
	end
	for k,soundData in pairs(self.soundData) do 
		if soundData and self.curSkillFrame >= soundData.delay then
			local index = MATH_CEIL( MATH_RAN( 0, soundData.soundNum ) )
			if soundData.soundList[index] ~= "0" then
				-- 主角及其伙伴或者怪物 才会播放音效
				if self.charac.characterType == CharacterConstant.TYPE_SELF or self.charac.characterType == CharacterConstant.TYPE_MONSTER
					or (self.charac.characterType == CharacterConstant.TYPE_PARTNER and self.charac.masterUId == RoleData.roleId) then
					AudioMgr.PlayFight( soundData.soundList[index], 0, 1 )
				end
			end
			self.soundData[k] = nil
		end
	end 
end

function BaseSkill:checkLife()
	-- body
	if self.curSkillFrame >= self.skillTime then
		-- 技能结束
		self.isActive = false
		self.charac:removeBoneEffect()
		ModuleEvent.dispatch( ModuleConstant.SKILL_FINISH, self.characId )
	end
end

-- 到打击帧时给自己或己方面员加buff
function BaseSkill:onAddBuff()
	if EnvironmentHandler.isPvpClient then
		return
	end
	if self.skillData.buffInfo and self.skillData.buffInfo[self.hitIndex] then
		local buffDatas = self.skillData.buffInfo[self.hitIndex]
		local selfCharaList = nil
		for _,buff in ipairs(buffDatas) do
			if buff.target == FightDefine.BUFF_TARGET.SELF then
				ModuleEvent.dispatch(ModuleConstant.ADD_BUFF, {self.characId, self.characId, buff.buffId, self.skillLv,BuffManager.getInstanceId()})
			elseif buff.target == FightDefine.BUFF_TARGET.SELFSIDE then
				if selfCharaList == nil then
					selfCharaList = CharacterManager:getCharacByRelation( self.characId, self.charac.camp, CharacterConstant.RELATION_FRIEND )
				end
				if selfCharaList then
					for _,chara in pairs(selfCharaList) do
						if CollisionTool.isCollide( self.charac, chara.uniqueID, self.skillData.collision[self.hitIndex], true ) == true then
							ModuleEvent.dispatch(ModuleConstant.ADD_BUFF, {self.characId, chara.uniqueID, buff.buffId, self.skillLv,BuffManager.getInstanceId()})
						end
					end
				end
			elseif buff.target == FightDefine.BUFF_TARGET.MASTER then --伙伴给主人加
				local charac = CharacterManager:getCharacByUId( self.characId )
				if charac and charac.characterType == CharacterConstant.TYPE_PARTNER then
					ModuleEvent.dispatch(ModuleConstant.ADD_BUFF, {self.characId, charac.masterUId, buff.buffId, self.skillLv,BuffManager.getInstanceId()})
				end
			elseif buff.target == FightDefine.BUFF_TARGET.SELFPLAYER then --己方玩家
				if selfCharaList == nil then
					selfCharaList = CharacterManager:getCharacByRelation( self.characId, self.charac.camp, CharacterConstant.RELATION_FRIEND )
				end
				if selfCharaList then
					for _,chara in pairs(selfCharaList) do
						if (chara.characterType == CharacterConstant.TYPE_PLAYER or chara.characterType == CharacterConstant.TYPE_SELF)
							and CollisionTool.isCollide( self.charac, chara.uniqueID, self.skillData.collision[self.hitIndex], true ) then
							ModuleEvent.dispatch(ModuleConstant.ADD_BUFF, {self.characId, chara.uniqueID, buff.buffId, self.skillLv,BuffManager.getInstanceId()})
						end
					end
				end
			end
		end
	end
end

function BaseSkill:dispose()
	-- body
end