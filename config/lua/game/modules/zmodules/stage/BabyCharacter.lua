--------------------------------------------------------------
-- region BabyCharacter
-- Date : 2017-7-24
-- Author : tanguofeng
-- Description :宝宝实体
-- endregion
--------------------------------------------------------------- 

BabyCharacter = {
    characterType = CharacterConstant.TYPE_BABY
}
BabyCharacter = createClassWithExtends(BabyCharacter,CharacterBase)
-- PartnerConst = {
--     TalkPanel = "NpcTalk",
--     TalkTime = "npc_talktime"
-- }

function BabyCharacter.create(characData)
    local babyCharac = BabyCharacter:new()
    CharacterBase.create(babyCharac,characData)
    return babyCharac
end

function BabyCharacter:init( characData )
    -- body
    self.hatredmap = HatredMap:create()
    self.canFollow = true
    CharacterBase.init(self,characData)
    self.master = CharacterManager:getCharacByUId( self.masterUId )   --伙伴的主人
  	if self.master == nil then
  		LogManager.LogError("create babyCharac error[" .. tostring(self.masterUId) .. "] master not found(isStageReady:" .. tostring(StageManager.isStageReady()) .. ", stageId:" .. tostring(StageManager.getCurStageId()) .. ")")
  		return
  	end
    self:setPosition(self.master:getPosition())
    local scaleNum = characData.scale * 0.001
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
        -- ModuleEvent.dispatch(ModuleConstant.AI_CREATE, self.uniqueID)
  	else
    		self.followAI = PartnerFollowAI:create()
    		self.followAI:init(self, true)
    end
end




function BabyCharacter:tryAttack( targetID )

end

function BabyCharacter:findSkill( targetID )

end

function BabyCharacter:fireSuccess()

end

function BabyCharacter:switchAutoFight( flag )
    if EnvironmentHandler.isInServer or StageManager.isInFightStage() then
        -- ModuleEvent.dispatch(ModuleConstant.AI_CREATE, self.uniqueID)
    end
end

function BabyCharacter:revive()
    -- body
    CharacterBase.revive(self)
    local master = CharacterManager:getCharacByUId(self.masterUId)
    if master and master:getPosition() then
        self:setPosition(master:getPosition())
    end
    self:switchAutoFight(true)
end

function BabyCharacter:dispose()
	if self.followAI then
		  self.followAI:dispose()
	end
    CharacterBase.dispose(self)
end