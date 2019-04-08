--------------------------------------------------------------
-- region Partner
-- Date : 2016-8-11
-- Author : jjm
-- Description :伙伴实体
-- endregion
--------------------------------------------------------------- 

Partner = {
    characterType = CharacterConstant.TYPE_PARTNER
}
Partner = createClassWithExtends(Partner,CharacterBase)
PartnerConst = {
    TalkPanel = "NpcTalk",
    TalkTime = "npc_talktime"
}

function Partner.create(uid,partnerData)
    local partner = Partner:new()
    CharacterBase.create(partner,partnerData)
    return partner
end

function Partner:init( partnerData )
    -- body
    self.hatredmap = HatredMap:create()
    self.canFollow = true
    CharacterBase.init(self,partnerData)
    self.master = CharacterManager:getCharacByUId( self.masterUId )   --伙伴的主人
  	if self.master == nil then
  		LogManager.LogError("create partner error[" .. tostring(self.masterUId) .. "] master not found(isStageReady:" .. tostring(StageManager.isStageReady()) .. ", stageId:" .. tostring(StageManager.getCurStageId()) .. ")")
  		return
  	end
    self:setPosition(self.master:getPosition())
    local scaleNum = partnerData.scale * 0.001
  	if self.view then
  		self.view:setScale(Vector3.New(scaleNum,scaleNum,scaleNum))
  	end
    self.followInterval = 0.5  --伙伴跟随的间隔时间
    self.followRadius   = 2    --跟随半径
    self.doActPercent   = 50   --播放动作或者行走的机率
    self.percentInterval= 2    --产生随机事件的间隔时间
    self.walkPosX       = 1    --walk移动X轴
    self.walkPosZ       = 1    --walk移动Z轴
  	if self.view then
  		self.view.onLoadModelCompleted = function()
        if self.view then
    			self.view:setScale(Vector3.New(scaleNum,scaleNum,scaleNum))
          if self.pose ~= nil then
    			   self.view.animator:CrossFade(self.pose,0)
          end
          self.view:enableBoxCollider(false)
        end
  		end
  	end
    if EnvironmentHandler.isInServer or StageManager.isInFightStage() then
        ModuleEvent.dispatch(ModuleConstant.AI_CREATE, self.uniqueID)
  	else
    		self.followAI = PartnerFollowAI:create()
    		self.followAI:init(self, true)
    end
end

function Partner:initSkill()
  -- body
  CharacterBase.initSkill( self )
    if self.skill ~= nil and self.skill ~= "" then
        self.normalAttack = self.skillTable[1]
        self.skillAttack = self.skillTable[2]
        self.ultiAttack = self.skillTable[3]
        self.avoidSkill = self.skillTable[4]
    end
  if self.skillorder ~= nil then
      local temp = self.skillorderTable
      self.orderType = temp[1]
      self.orderIndex = 1

      if self.orderType == FightDefine.MONSTERORDER_AI then
          self.orderAI = SkillAI:create()
          self.orderAI:init( self.uniqueID, temp )
      else
          self.orderList = temp[2]
          self.maxOrder = #self.orderList
      end
  end
  if self.passSkillList ~= nil then
      PassSkillManager.addHandler( self )
  end
end

function Partner:changeProperty(key,value,isForce)
    if key == ConstantData.ROLE_CONST_HP and value < 0 then
        value = 0
    end
    CharacterBase.changeProperty(self,key,value,isForce)
    if key == ConstantData.ROLE_CONST_HP then
        --如果血条隐藏，则不需要更新血量
        if self.view and self.view.blood and self.view.blood.visible then
            self.view:updateBlood( value )
        end
		ModuleEvent.dispatchWithFixedArgs(ModuleConstant.STAGE_FIGHT_PARTNER_HP,self.masterUId,value)
    end
end

function Partner:tryAttack( targetID )
    -- body
    local function doAttack( param )
        -- body
        self:findSkill( param )
    end
    PressureDispatcher.orderMonsterFire( doAttack, targetID )
end

function Partner:findSkill( targetID )
  -- body
    local skillid = 0
    if self.orderType == FightDefine.MONSTERORDER_SEQUENCE then

    elseif self.orderType == FightDefine.MONSTERORDER_RAMDOM then
      self.orderIndex = math.ceil( math.LogicRandom( 0, self.maxOrder ) )
    else
        self.orderIndex = self.orderAI:execute()
    end
    if self.orderIndex == nil then
      return
    end
    if self.charaBuff and self.charaBuff:hasBuffEffect(FightDefine.BUFF_EFFECT_TYPE.TAUNT) then
        self.orderIndex = 0
    end

    if self.orderIndex == 0 or (self.orderList ~= nil and self.orderList[self.orderIndex] == 0) then
      skillid = self.normalAttack[1] --怪物只有一招普攻
      self.attackIndex = 1
      self.skillIndex = nil
    else
      if self.orderList ~= nil then
        skillid = self.skillAttack[self.orderList[self.orderIndex]]
        self.skillIndex = self.orderList[self.orderIndex]
      else
        skillid = self.skillAttack[self.orderIndex]
        self.skillIndex = self.orderIndex
      end
    end
    local skilllevel = 1
    if self.allskill[skillid] then --寻找实际等级
        skilllevel = self.allskill[skillid].level
    end
    self:requestFire( skillid,skilllevel,targetID,1 ) 
end

function Partner:fireSuccess()
    -- body
    if self.usingSkill ~= nil then
        -- 出生或激活技能不处理
        local skillData = FightModel:getSkillData( self.usingSkill.id )
        if skillData.skilltype == FightDefine.SKILLTYPE_BORN or skillData.skilltype == FightDefine.SKILLTYPE_ACTIVE then
            return
        end
    end
    if self.orderType == FightDefine.MONSTERORDER_SEQUENCE then
        self.orderIndex = self.orderIndex + 1
        if self.orderIndex > self.maxOrder then
          self.orderIndex = 1
        end
    end
end

function Partner:switchAutoFight( flag )
    if EnvironmentHandler.isInServer or StageManager.isInFightStage() then
        ModuleEvent.dispatch(ModuleConstant.AI_CREATE, self.uniqueID)
    end
end

function Partner:stateEnd()
  -- body
    CharacterBase.stateEnd(self)
    ModuleEvent.dispatch(ModuleConstant.PLAYER_STATE_END, self.uniqueID)
end

function Partner:revive()
    -- body
    CharacterBase.revive(self)
    local master = CharacterManager:getCharacByUId(self.masterUId)
    if master and master:getPosition() then
        self:setPosition(master:getPosition())
    end
    self:switchAutoFight(true)
end

function Partner:dispose()
	if self.followAI then
		self.followAI:dispose()
	end
    CharacterBase.dispose(self)
end