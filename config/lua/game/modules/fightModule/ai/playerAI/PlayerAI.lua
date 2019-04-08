--------------------------------------------------------------
-- region PlayerAI.lua
-- Date : 2016-7-30
-- Author : jjm
-- Description : 玩家AI类
-- endregion
--------------------------------------------------------------- 

PlayerAI = {}
PlayerAI = createClassWithExtends(PlayerAI,CharacterAI)

--[[--
设置角色AI开启
]]
-- 玩家的AI在create时无需执行一次，避免状态异常
function PlayerAI:start( isCreate )
    if isCreate == nil or isCreate == false then
        self:doRoundAI()
    end
end

--[[--
开始一个新回合的AI
]]
function PlayerAI:doRoundAI()
    --TODO 子类去实现回合的开始
    if (not self.target.isAutoFight) and (not self.target.isTaunt) then
	    return
    end
    self:stopCurrAI()
    self.currentCD = self.findEnemyFrame
    self:findEnemy()
end

function PlayerAI:findEnemy()
    self.currState = AIState.FIND_ENEMY
    if self.targetEnemy then
        if self.targetEnemy.hp > 0 then
            self.currTarget = self.targetEnemy
            self:doAISkill(self.targetEnemy.uniqueID)
            return
        else
            self.targetEnemy = nil
        end
    end
    local target = FindEnemyAI.getTarget(self.findEnemyType,self.target)
    if target ~= nil then
        for uid,character in pairs (target) do
        	if character.state ~= CharacterConstant.STATE_DEAD then
	            self.currTarget = character
	            self:doAISkill(uid)
	            return
	        end
        end
    end
	self:findPath()
end

--[[--
寻路AI
]]
function PlayerAI:findPath()
    if EnvironmentHandler.isInServer == false then
        local guidePos = nil
        if StageManager.getCurStage() then
            guidePos=StageManager.getCurStage():getGuidePos()
        end
	    if guidePos ~= nil then
			self.moveEvtTable[1] = self.target.uniqueID
			self.moveEvtTable[2] = math.round(guidePos.x, 2)
			self.moveEvtTable[3] = math.round(guidePos.y, 2)
			self.moveEvtTable[4] = math.round(guidePos.z, 2)
			self.moveEvtTable[5] = "0"
		    ModuleEvent.dispatch(ModuleConstant.AI_ACTION_MOVE, self.moveEvtTable)
	    else
            if self.currState ~= AIState.IDLE then
        	    self.currState = AIState.IDLE
                if self.target.state == CharacterConstant.STATE_RUN then
        	       self.target:switchState(CharacterConstant.STATE_IDLE)
                end
            end
	    end
    end
end

--[[--
角色技能结束
]]
function PlayerAI:endAISkill(skillstate)
   --TODO 子类去实现技能完成之后该做什么
    if self.currState ~= AIState.DO_SKILL then return end
   	if self.target.stopTime > 0 then
        self:stopTime()
    elseif self.currState ~= AIState.CLOSETO_ENEMY then
        self:doRoundAI()
    end
end

--[[--
怪物AI停顿时间
]]
function PlayerAI:stopTime()
    self.currentCD = self.stopFrame
    self.currState = AIState.STOP_TIME
end

--[[--
开始使用技能的时候清空上一次索敌间隔
]]
function PlayerAI:clearRoundTick()
    if self.currState == AIState.NOTHING then return end
    self.currState = AIState.DO_SKILL
end