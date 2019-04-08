--------------------------------------------------------------
-- region CharacterAI.lua
-- Date : 2016-7-30
-- Author : jjm
-- Description : 角色AI基类
-- endregion
--------------------------------------------------------------- 
AIState = {
    NOTHING = 0, --什么都没干
    IDLE = 1,--待机状态
    FIND_ENEMY = 2,--索敌
    CLOSETO_ENEMY = 3,--靠近敌人（技能寻路）
    DO_SKILL = 4,--使用技能
    STOP_TIME = 5,--停顿
    FIND_PATH = 6, --寻路
}

CharacterAI = {
    target = nil,--角色实体对象
    aiFrameKey = nil, --AI定时器
    currState = nil,--AI当前状态
    currTarget = nil,--当前目标
    targetEnemy = nil,--索敌目标
	currentCD = nil, -- 当前冷却时间，冷却时间内AI不运作
}
CharacterAI = createClass(CharacterAI)

--[[--
初始化角色AI
@monster 角色实体对象
]]
function CharacterAI:init(character)
    self.target = character
    self.findEnemyType = self.target.findenemyai[1]
    self.findEnemyFrame = self.target.findenemyai[2] * 0.001 * ConstantData.FRAME_RATE
    self.stopFrame = self.target.stopTime * 0.001 * ConstantData.FRAME_RATE
    self.attackintervalFrame = self.target.attackinterval * 0.001 * ConstantData.FRAME_RATE
    self.pathType = self.target.pathai[1]
    self.pathDis = self.target.pathai[2] * 0.1
	self.currentCD = 0
    self.moveEvtTable = {}
end

--[[--
销毁AI
]]
function CharacterAI:dispose()
    if self.aiFrameKey then
        FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_AI):removeCallback(self.aiFrameKey)
        self.aiFrameKey = nil
    end
    if self.findPathAI then
        self.findPathAI:stop()
        self.findPathAI = nil
    end
    self.target = nil
    self.currState = nil
    self.currTarget = nil
    self.targetEnemy = nil
end

--[[--
设置角色AI开启
]]
function CharacterAI:start()
    self:doRoundAI()
end

function CharacterAI:update()
	if self.currState == AIState.DO_SKILL or 
		self.currState == AIState.NOTHING then
		return
	end
	if self.findPathAI and self.findPathAI.isRunning then
		local pos, faceToCharaId = self.findPathAI:update()
		if pos ~= nil then
			self.moveEvtTable[1] = self.target.uniqueID
			self.moveEvtTable[2] = math.round(pos.x, 2)
			self.moveEvtTable[3] = math.round(pos.y, 2)
			self.moveEvtTable[4] = math.round(pos.z, 2)
			self.moveEvtTable[5] = faceToCharaId or "0"
			ModuleEvent.dispatch(ModuleConstant.AI_ACTION_MOVE, self.moveEvtTable)
		end
	end
	if self.currentCD and self.currentCD > 0 then
		self.currentCD = self.currentCD - FightDefine.AI_RUN_INTERVAL
		return
	end
	self:doRoundAI()
end

--[[--
强制设置索敌目标
]]
function CharacterAI:setTargetEnemy(character)
    self.targetEnemy = character
end

--[[--
设置角色AI停止
]]
function CharacterAI:stop()
    self:stopCurrAI()
end

--[[--
开始一个新回合的AI
]]
function CharacterAI:doRoundAI()
    --TODO 子类去实现回合的开始
end

--[[--
角色索敌
@return target
{
    uid 对象id
    character  实体对象
}
影响索敌的因素有
1、距离最近
2、生命值最低
3、仇恨最高
4、队伍编号最小
]]
function CharacterAI:findEnemy()
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
    if target then
        for uid,character in pairs (target) do
            self.currTarget = character
            self:doAISkill(uid)
            break
        end
    end
end

--[[--
执行技能AI
]]
function CharacterAI:doAISkill(uid)
    self.currState = AIState.CLOSETO_ENEMY
    self.target:tryAttack(uid)
end

--[[--
停止角色技能寻路（靠近敌人）
]]
function CharacterAI:stopCloseToEnemy()
    self.target:stopCurState()
end

--[[--
开始使用技能的时候清空上一次索敌间隔
]]
function CharacterAI:clearRoundTick()
    --TODO 子类去实现是否需要清空回合定时器
end

--[[--
角色技能结束
]]
function CharacterAI:endAISkill(skillstate)
   --TODO 子类去实现技能完成之后该做什么
end

--[[--
技能打断状态，用于处理角色为击退或击飞时候的处理
]]
function CharacterAI:breakState()
    if self.currState == AIState.DO_SKILL then
        self:endAISkill()
    end
end


--[[--
停止当前的AI行为
]]
function CharacterAI:stopCurrAI()
    if self.currState == AIState.CLOSETO_ENEMY then
        self:stopCloseToEnemy()
    end
    self.currState = AIState.NOTHING
end

--[[--
获取角色未激活状态下的待机动作
]]
function CharacterAI:getIdleAction()
    if self.target and self.currState == AIState.NOTHING and self.target.relaxaction then
        return self.target.relaxaction
    end
    return nil
end

--判断运行状态
function CharacterAI:isRunning()
    return (self.currState ~= AIState.NOTHING)
end 

function CharacterAI:changeProperty(key, val)
	if key == nil then
		return
	end
	self[key] = val
end