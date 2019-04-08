--------------------------------------------------------------
-- region PartnerAI.lua
-- Date : 2016-8-24
-- Author : jjm
-- Description : 伙伴AI类
-- endregion
--------------------------------------------------------------- 

PartnerAI = {
    awakeType = nil,--激活类型
    awakeParam = nil,--激活参数
}
PartnerAI = createClassWithExtends(PartnerAI,CharacterAI)

--[[--
初始化伙伴AI
@monster 伙伴实体对象
]]
function PartnerAI:init(character)
    if character.awake == "0" then
        self.awakeType = 0
    else
        local awakeTable = StringUtils.split(character.awake,"|")
        self.awakeType = tonumber(awakeTable[1]) or 0;
        self.awakeParam = tonumber(awakeTable[2])
    end
	self.followAI = PartnerFollowAI:create()
	self.followAI:init(character, false)
    CharacterAI.init(self,character)
	if self.pathType and self.pathType ~= -1 then
		 self.findPathAI = FindPathAI:new()
	end
	self.canFollow = false
end

--[[--
销毁AI
]]
function PartnerAI:dispose()
	self.followAI:dispose()
    CharacterAI.dispose(self)
end


--[[--
设置伙伴AI开启
]]
function PartnerAI:start()
    self.target:changeProperty( "invincible", 1 )
    if self.target.bornskill ~= 0 then
        self.currState = AIState.DO_SKILL
        self.target:onFire(self.target.bornskill)
    else
        self:setAwakeCondition()
    end
end

--[[--
    伙伴激活
]]
function PartnerAI:awake()
    if self.currState ~= AIState.NOTHING then return end
    if self.target.awakeskill ~= 0 then
		self.currState = AIState.DO_SKILL
        self.target:onFire(self.target.awakeskill)
    else
        self.followAI:doTalk()
        self.currState = AIState.IDLE
        self.target:changeProperty( "invincible", self.target.isinvincible )
    end
end

--[[--
设置激活条件
]]
function PartnerAI:setAwakeCondition()
    self.currState = AIState.NOTHING
    --类型0，默认激活，不需要填参数
    if self.awakeType == nil or self.awakeType == 0 then
        self:awake()
    --类型1，警戒激活，不需要填参数
    elseif self.awakeType == 1 then

    --类型2，死亡激活，参数：伙伴组
    elseif self.awakeType == 2 then

    --类型3，机关激活，参数：机关ID
    elseif self.awakeType == 3 then
    end
end

function PartnerAI:update()
	if self.canFollow and self.targetEnemy == nil then
		self.followAI:update()
	end
	CharacterAI.update(self)
end

--[[--
开始一个新回合的AI
]]
function PartnerAI:doRoundAI()
    self:stopCurrAI()
    self.currentCD = self.findEnemyFrame
    self:findEnemy()
end

--[[--
伙伴技能结束
]]
function PartnerAI:endAISkill(skillstate)
    if self.currState == AIState.NOTHING then return end
    if skillstate == CharacterConstant.SKILLSTATE_BORN then
        self:setAwakeCondition()
    elseif skillstate == CharacterConstant.SKILLSTATE_ACTIVE then
        self.followAI:doTalk()
        self.currState = AIState.IDLE
        self.target:changeProperty( "invincible", self.target.isinvincible )
    else
        if self.target then
            self.target:changeProperty( "invincible", self.target.isinvincible )
        end
        if self.currState ~= AIState.DO_SKILL then return end
        if self.stopFrame and self.stopFrame > 0 then
            self:stopTime()
        elseif self.currState ~= AIState.FIND_PATH then
            self:findPath()
        end
    end
end

--[[--
开始使用技能的时候清空上一次索敌间隔
]]
function PartnerAI:clearRoundTick()
    self.currState = AIState.DO_SKILL
end

--[[--
技能打断状态，用于处理角色为击退或击飞时候的处理
]]
function PartnerAI:breakState()
    if self.currState < AIState.DO_SKILL then
        self:doRoundAI()
    end
    CharacterAI.breakState(self)
end

function PartnerAI:findEnemy()
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
    self.canFollow = true
    local target = FindEnemyAI.getTarget(self.findEnemyType,self.target)
    if target then
        for uid, character in pairs(target) do
            self.currTarget = character
            self:doAISkill(uid)
            self.canFollow = false
            break
        end
    end
end

--[[--
伙伴AI停顿时间
]]
function PartnerAI:stopTime()
    self.currentCD = self.stopFrame
    self.currState = AIState.STOP_TIME
end

--[[--
寻路AI
]]
function PartnerAI:findPath()
    if self.attackintervalFrame == 0 then
		self.currState = AIState.IDLE
        return
    end
	if self.findPathAI then
		self.findPathAI:start(self.pathType,self.pathDis,self.target,self.currTarget)
	end
    self.currentCD = self.attackintervalFrame
    self.currState = AIState.FIND_PATH
end

--[[--
停止当前的AI行为
]]
function PartnerAI:stopCurrAI()
    if self.currState == AIState.FIND_PATH and self.findPathAI then
        self.findPathAI:stop()
    end
    CharacterAI.stopCurrAI(self)
end