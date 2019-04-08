--------------------------------------------------------------
-- region Monster.lua
-- Date : 2016-6-20
-- Author : jjm
-- Description :怪物实体
-- endregion
---------------------------------------------------------------
Monster = {
  characterType = CharacterConstant.TYPE_MONSTER
}
Monster = createClassWithExtends(Monster,CharacterBase)

function Monster.create(monsterData)
  local monster = Monster:new()  
  CharacterBase.create(monster,monsterData)
  return monster
end

function Monster:setPosAndRot(monsterData)
    local rotation = Quaternion.Euler(0,monsterData.rotation, 0)
    self.bornPosition = PathFinder.samplePosition(monsterData.position)
    self.defaultPosition = self.bornPosition:Clone()
    self:setPosition( self.defaultPosition )
    self:setRotation( rotation )
    if self.view then
      self.view:setPosAndRon(self.defaultPosition,rotation,monsterData.scale)
      self.view:createDynamicBlock()
    end
end

function Monster:init(monsterData)
    monsterData.uiposition = monsterData.uiposition * monsterData.scale * 0.001
    self.model = monsterData.model
    self.monsterSpawnId = monsterData.monsterSpawnId;
    self.hatredmap = HatredMap:create()
    self.monsterType=monsterData.monsterType
    CharacterBase.init(self,monsterData)
    self:setPosAndRot(monsterData)
    if self.view and self.view.blood then
      self.view.blood:setMaxHp(self.maxhp)
      self.view.blood:setCurHp(self.hp, true)
    end
    if self.monsterType == CharacterConstant.MONSTER_TYPE.BOSS then
        if self.view and self.view.blood then
            self.view.blood:setVisible(false)
        end
    end
	if self.monsterSpawnId ~= nil then
		ModuleEvent.dispatch(ModuleConstant.AI_CREATE, self.uniqueID)
	end
end

function Monster:initSkill()
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

function Monster:doTalk()
    -- body
    if self.view then
        self.view:doTalk()
    end
end

function Monster:tryAttack( targetID )
    -- body
    local function doAttack( param )
        -- body
        local result = self:findSkill( param )
		if result == false then
			ModuleEvent.dispatchWithFixedArgs(ModuleConstant.AI_BREAK_STATE, self.uniqueID, 3)
		end
    end
    PressureDispatcher.orderMonsterFire( doAttack, targetID )
end

function Monster:findSkill( targetID )
  -- body
  local skillid = 0
  if self.orderType == FightDefine.MONSTERORDER_SEQUENCE then

  elseif self.orderType == FightDefine.MONSTERORDER_RAMDOM then
    self.orderIndex = math.ceil( math.LogicRandom( 0, self.maxOrder ) )
  else
      if self.orderAI == nil then --可能已销毁
        return false
      end
      self.orderIndex = self.orderAI:execute( targetID )
  end
  if self.orderIndex == nil then
    return false
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

    if skillid == 0 then
      return false
    end
    self:requestFire( skillid,1,targetID,1 ) 
	return true
end

function Monster:fireSuccess()
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

function Monster:changeProperty( key, value, isForce )
  -- body
    if key == ConstantData.ROLE_CONST_HP and value < 0 then
        value = 0
    end
    CharacterBase.changeProperty( self, key, value, isForce )
    if key == ConstantData.ROLE_CONST_HP then
      --如果血条隐藏，则不需要更新血量
      if self.view then
          self.view:updateBlood( value )
      end
      if self.monsterType == CharacterConstant.MONSTER_TYPE.BOSS or self.needDispatchHpInfo then
          ModuleEvent.dispatchWithFixedArgs(ModuleConstant.BOSS_HP_CHANGE,value, self.maxhp)
      end
	elseif key == ConstantData.ROLE_CONST_MAXHP then
		if self.view then
			self.view:updateMaxHp( value )
			self.view:updateBlood(self.hp)
		end
		if self.monsterType == CharacterConstant.MONSTER_TYPE.BOSS or self.needDispatchHpInfo then
			ModuleEvent.dispatchWithFixedArgs(ModuleConstant.BOSS_HP_CHANGE,self.hp or value, value)
		end
    end
end

function Monster:stateEnd()
  -- body
    CharacterBase.stateEnd(self)
    ModuleEvent.dispatch(ModuleConstant.PLAYER_STATE_END, self.uniqueID)
end

function Monster:setDeadEffect(param)
    if self.view then
        self.view:setDeadEffect(param)
    end
end

function Monster:dispose()
  -- body
  CharacterBase.dispose(self)
  if self.orderAI ~= nil then
      self.orderAI:dispose()
      self.orderAI = nil
  end
  self.orderList = nil
  self.needDispatchHpInfo = nil
  self.hitsound = nil
  self.deathsound = nil
  self.deadeffect = nil
  self.passSkill = nil
end