-- region
-- Date    : 2016-06-27
-- Author  : daiyaorong
-- Description :  死亡状态
-- endregion

DeadState = createClassWithExtends(DeadState, StateBase)

local DelayFrame = 15
-- local DelayShowDeadFrame = nil

function DeadState:onStateActive( preState )
	if self.target.delayShowDeadTime and self.target.delayShowDeadTime>0 then
		self.delayShowDeadFrame = ConstantData.FIX_FRAME_SECOND*self.target.delayShowDeadTime
	else
		self.delayShowDeadFrame=nil
		self:delayStateActive()
	end
end

function DeadState:delayStateActive()
	self.target:setCanMove( false )
	self.target:stopSearch()
	self.target.hp = 0
    if AIManager.hasAI(self.target.uniqueID) then
       ModuleEvent.dispatch(ModuleConstant.AI_STOP, self.target.uniqueID)
    end
  	self:initDeadEff()
	self.stateAni = CharacterConstant.ANIMATOR_DEAD
	self:crossFade( self.stateAni )
	self.curTime = 0

	self:playDeadSound()
	if EnvironmentHandler.isInServer == false then
		self.removeTime = self.stateTime + CharacterConstant.DEAD_REMOVEFRAME
	else
		self.removeTime = self.stateTime + 15
	end
	self.disposeTime = self.stateTime + DelayFrame --延迟执行消散效果
	ModuleEvent.dispatch( ModuleConstant.SKILL_DESTROY, self.target.uniqueID)
	ModuleEvent.dispatch( ModuleConstant.CHARACTER_DEAD,  self.target.uniqueID )

    if self.target.characterType == CharacterConstant.TYPE_SELF then
        if FightModel:canControlAI() then
            AIManager.disableAllAI()
        end
        AudioMgr.PlaySoundInfoFight('bgm_battle_kill')  --播放击杀音效
    elseif self.target.characterType == CharacterConstant.TYPE_MONSTER then
    	if(self.target.view)then
    		self.target.view.thingPlayer:replaceXiaoShanTexture();
    	end
    elseif self.target.characterType == CharacterConstant.TYPE_PARTNER then --伙伴
    	if FightModel:isPartnerCanRevive() then
    		if self.target.master.uniqueID == RoleData.roleId then --自己的伙伴死亡
    			FightModel:requestPartnerRevive(self.target.uniqueID)
    		end
    	end
    end
end

function DeadState:onStateUpdate()
	if self.delayShowDeadFrame then
		if self.delayShowDeadFrame>0 then
			self.delayShowDeadFrame=self.delayShowDeadFrame-1
			return
		else
			self:delayStateActive()
			self.delayShowDeadFrame=nil
		end
	end
	if self.curTime==nil then
		return
	end
	-- body
	self.curTime = self.curTime + 1

	if self.target.characterType == CharacterConstant.TYPE_MONSTER then
		self:playDeadEff()
		self:onBodyDown()
    elseif self.target.characterType == CharacterConstant.TYPE_PLAYER then
        self:onPlayerRemove()
	end
end

function DeadState:onNextState()
	-- body
	ModuleEvent.dispatch( ModuleConstant.SKILL_DESTROY, self.target.uniqueID)
	ModuleEvent.dispatchWithFixedArgs( ModuleConstant.AI_DESTROY, self.target.uniqueID, self.target.characterType)
	if self.stateAni == CharacterConstant.ANIMATOR_FLY then
		self.stateAni = CharacterConstant.ANIMATOR_DIE
		self:crossFade( self.stateAni )
		self.curTime = 0
		self.removeTime = self.stateTime + CharacterConstant.DEAD_REMOVEFRAME
		self.disposeTime = self.stateTime + DelayFrame
	end
end

function DeadState:initDeadEff()
	-- body
	self.effectRecord = nil
	if self.target.characterType == CharacterConstant.TYPE_MONSTER then
		if self.target.deadeffect ~= nil then
			self.effectRecord = {}
		end
		if self.target.view then
			self.target.view:enterDead()
		end
	end
end

function DeadState:playDeadSound()
	-- body
	-- 播放死亡音效
	local deathSound = self.target.deathsound
	if deathSound then
		local index = math.ceil( math.LogicRandom( 0, #deathSound ) )
		if deathSound[index] ~= "0" then
			local soudnObj = AudioMgr.isSoundPlaying(deathSound[index])
			if soudnObj then
				local percent = soudnObj:getAudioSourcePercent()
				if percent and percent <= 0.5 then
					return --避免短时间内重复播放
				end
			end
			AudioMgr.PlaySoundInfoFight( deathSound[index] )
		end
	end
end

function DeadState:playDeadEff()
	-- body
	if self.effectRecord ~= nil then
		local param = nil
		for k,v in ipairs( self.target.deadeffect ) do
			if self.effectRecord[k] ~= true and self.curTime >= v[3] then
				self.effectRecord[k] = true
				param = { id=v[1], bone=v[2], offset={0,0,0} }
				self.target:setDeadEffect( param )
			end
		end
	end
end

function DeadState:onBodyDown()
	-- body
	if self.removeTime ~= nil then
		if self.curTime >= self.disposeTime then
			if self.target.characterType == CharacterConstant.TYPE_MONSTER then
				--计算百分比;
				if(self.target.view)then
					self.target.view.thingPlayer:setFloat("_dissolve", (self.curTime-self.disposeTime)/CharacterConstant.DEAD_REMOVEFRAME);
				end
			end
			if self.curTime < self.removeTime then
				-- 尸体下沉
				if self.target.characterType ~= CharacterConstant.TYPE_MONSTER then
					self.target.position.y = self.target.position.y - self.target.downspeed * ConstantData.FRAME_DELTA_TIME
					if self.target.view ~= nil then
			            self.target.view:setPosition(self.target.position)
			        end
				end
			else
				if EnvironmentHandler.isInServer then
                    FightServerControl.setFrameDead( self.target.uniqueID, self.target:getPosition() )
                end
				ModuleEvent.dispatch( ModuleConstant.CHARAC_REMOVE, self.target.uniqueID)
			end
		end
	end
end

--处理玩家类角色移除
function DeadState:onPlayerRemove()
	local fightStageType = FightModel:getFightStageType()
    if fightStageType ~= ConstantData.STAGE_TYPE_TEAM and fightStageType ~= ConstantData.STAGE_TYPE_ELITE and fightStageType ~= ConstantData.STAGE_TYPE_COUPLE_DUNGEON then
        if EnvironmentHandler.isInServer == true or self.target.uniqueID ~= RoleData.roleId then
            if self.removeTime ~= nil and self.curTime >= self.removeTime then
                if EnvironmentHandler.isInServer == true then
                    FightServerControl.setFrameDead( self.target.uniqueID, self.target:getPosition() )
                end
			    ModuleEvent.dispatch(ModuleConstant.CHARAC_REMOVE, self.target.uniqueID)
            end
        end
    end
end

function DeadState:onDeadHitAcitve( hitState )
	-- body
	if hitState == CharacterConstant.STATE_HITFLY then
		self.stateAni = CharacterConstant.ANIMATOR_FLY
		self:crossFade( self.stateAni, 0 )
		self.effectRecord = nil
		self.curTime = 0
		self.removeTime = nil
	end
end