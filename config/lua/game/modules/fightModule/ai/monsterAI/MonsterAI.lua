--------------------------------------------------------------
-- region MonsterAI.lua
-- Date : 2016-7-6
-- Author : jjm
-- Description : 怪物AI类
-- endregion
--------------------------------------------------------------- 

MonsterAI = {
    awakeType = nil,--激活类型
    awakeParam = nil,--激活参数
}
MonsterAI = createClassWithExtends(MonsterAI,CharacterAI)

--[[--
初始化怪物AI
@monster 怪物实体对象
]]
function MonsterAI:init(character)

    if character.awake == "0" then
        self.awakeType = 0
    else
        local awakeTable = StringUtils.split(character.awake,"|")
        self.awakeType = tonumber(awakeTable[1]) or 0;
        self.awakeParam = tonumber(awakeTable[2])
    end
    CharacterAI.init(self,character)
	if self.pathType and self.pathType ~= -1 then
		self.findPathAI = FindPathAI:new()
	end
end

--[[--
设置怪物AI开启
]]
function MonsterAI:start()
    self.target:changeProperty( "invincible", 1 )
    if self.target.bornskill and self.target.bornskill ~= 0 then
        self.target:changeProperty("targetID", nil)
        self.target:onFire(self.target.bornskill)
    else
        self:setAwakeCondition()
    end
end

--[[--
    怪物激活
]]
function MonsterAI:awake()
    if self.currState ~= AIState.NOTHING then return end
    if self.target.awakeskill and self.target.awakeskill ~= 0 then
        self.target:changeProperty("targetID", nil)
        self.target:onFire(self.target.awakeskill)
    else
        if self.target.monsterType == CharacterConstant.MONSTER_TYPE.BOSS then
            ModuleEvent.dispatch(ModuleConstant.BOSS_BLOOD_SHOW,self.target)
        end
        self.target:doTalk()
        self.currState = AIState.IDLE
		self.target:changeProperty( "invincible", self.target.isinvincible )
    end
end

--[[--
设置激活条件
]]
function MonsterAI:setAwakeCondition()
    self.currState = AIState.NOTHING
    if self.target.hp > 0 then
        self.target:switchState( CharacterConstant.STATE_IDLE )
    end
    --类型0，默认激活，不需要填参数
    if self.awakeType == 0 then
        self:awake()
    --类型1，警戒激活，不需要填参数
    elseif self.awakeType == 1 then
        MonsterAwakeControl.addPatrol(self.target)
    --类型2，死亡激活，参数：怪物组
    elseif self.awakeType == 2 then
        MonsterAwakeControl.addDeadListener(self.awakeParam, self.target)
    --类型3，机关激活，参数：机关ID
    elseif self.awakeType == 3 then
        
    end
end

--[[--
开始一个新回合的AI
]]
function MonsterAI:doRoundAI()
	if self.currState == AIState.STOP_TIME then
		self:findPath()
		return
	end
	self:stopCurrAI()
	self.currentCD = self.findEnemyFrame
	self:findEnemy()
end

--[[--
怪物技能结束
]]
function MonsterAI:endAISkill(skillstate)
    if self.currState == AIState.NOTHING then return end
    if skillstate == CharacterConstant.SKILLSTATE_BORN then
        self:setAwakeCondition()
    elseif skillstate == CharacterConstant.SKILLSTATE_ACTIVE then
        if self.target.monsterType == CharacterConstant.MONSTER_TYPE.BOSS then
            ModuleEvent.dispatch(ModuleConstant.BOSS_BLOOD_SHOW,self.target)
        end
        self.target:doTalk()
        self.currState = AIState.IDLE
        self.target:changeProperty( "invincible", self.target.isinvincible )
    else
        self.target:changeProperty( "invincible", self.target.isinvincible )
        if self.currState ~= AIState.DO_SKILL then return end
        if self.stopFrame > 0 then
            self:stopTime()
        elseif self.currState ~= AIState.FIND_PATH then
            self:findPath()
        end
    end
end

--[[--
开始使用技能的时候清空上一次索敌间隔
]]
function MonsterAI:clearRoundTick()
    self.currState = AIState.DO_SKILL
end

--[[--
技能打断状态，用于处理角色为击退或击飞时候的处理
]]
function MonsterAI:breakState()
    if self.currState == nil or self.currState == AIState.NOTHING then return end
    if self.currState < AIState.DO_SKILL then
        self:doRoundAI()
    end
    CharacterAI.breakState(self)
end

--[[--
怪物AI停顿时间
]]
function MonsterAI:stopTime()
	self.currentCD = self.stopFrame
    self.currState = AIState.STOP_TIME
end

--[[--
寻路AI
]]
function MonsterAI:findPath()
	self.currState = AIState.FIND_PATH
    self.currentCD = self.attackintervalFrame
    if self.attackintervalFrame > 0 and self.currTarget and self.findPathAI then
		self.findPathAI:start(self.pathType,self.pathDis,self.target,self.currTarget)
	else
		self.currState = AIState.IDLE
    end 
end

--[[--
停止当前的AI行为
]]
function MonsterAI:stopCurrAI()
    if self.currState == AIState.FIND_PATH and self.findPathAI then
        self.findPathAI:stop()
    end
    CharacterAI.stopCurrAI(self)
end
