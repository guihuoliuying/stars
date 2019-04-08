--region BuffEffectPoisonBomb.lua
--Date 2016/11/02
--Author daiyaorong
-- BUFF效果：毒爆类
--endregion

BuffEffectPoisonBomb = {}
createClassWithExtends(BuffEffectPoisonBomb, BuffEffectBase)

function BuffEffectPoisonBomb:onBuffStart()
	
end

-- 执行一次毒爆效果
function BuffEffectPoisonBomb:doEffect()
    self.refBuff.remainTime = 0--销毁自己
    local target = self.refBuff.target
	local poisonBuffList = target.charaBuff:getBuffEffectByType( FightDefine.BUFF_EFFECT_TYPE.POISONING )
    if poisonBuffList then
        local damNum = 0
        for k,v in ipairs( poisonBuffList ) do
            damNum = damNum + v:getPoisonDamage()--累积伤害值
        end
        target.charaBuff:removeByEffectType(FightDefine.BUFF_EFFECT_TYPE.POISONING)--删除中毒类buff
        damNum = damNum * self.effectInfo.rate * 0.001
        if damNum > 0 then
            damNum = math.ceil(damNum)


            local target = self.refBuff.target
            local targetBuff = BuffManager:getCharacterBuff(target.uniqueID)
            if targetBuff and targetBuff.buffGetDemageHpRate then
                local changeHp = 0
                local newHp2 = 0
                changeHp = damNum * targetBuff.buffGetDemageHpRate 
                newHp2 = target.hp+changeHp
                if newHp2 > target.maxhp then
                    newHp2=target.maxhp
                end
                
                local numberType = targetBuff.damageNumberType2
                if self.numberInfo == nil then
                    self.numberInfo = {}
                    self.numberInfo.numFlag = FightDefine.FIGHT_NUM_FLAG.CURE
                    self.numberInfo.numSign = 1
                end
                self.numberInfo.numType = numberType
                self:changeHp(target.uniqueID, changeHp, newHp2, 1, self.numberInfo)
                return
            end
            local targetHp = target.hp - damNum
            local numberType = self.refBuff.buffData.damageNumberType
			if self.numberInfo == nil then
				self.numberInfo = {}
				self.numberInfo.numFlag = FightDefine.FIGHT_NUM_FLAG.POISONING
			end
			self.numberInfo.numType = numberType
	        self:changeHp(self.refBuff.targetUID, damNum, targetHp, 0, self.numberInfo)
        end
    end
end