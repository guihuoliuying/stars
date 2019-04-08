--region BuffEffectPoisoning.lua
--Date 2016/07/15
--Author zhouxiaogang
-- BUFF效果：中毒类
--endregion

--[[
	effectInfo = 
	{
		effectType = nil
		rate = nil,		-- 伤害千分比
		value = nil,	-- 固定伤害值
		interval = nil,	-- 间隔时间（毫秒）
	}
]]

BuffEffectPoisoning = {}

createClassWithExtends(BuffEffectPoisoning, BuffEffectBase)

function BuffEffectPoisoning:onBuffStart()
	if self.refBuff.attacker then
		self.sourceAttack = self.refBuff.attacker[ATTR_KEY.ATTACK]
	else
		self.sourceAttack = 0
	end
end

-- 执行一次中毒效果
function BuffEffectPoisoning:doEffect()
	local damage = SkillEffect.calcDamage(self.sourceAttack, self.refBuff.target[ATTR_KEY.DEFENSE], self.effectInfo.rate * 0.001, self.effectInfo.value)
	damage = damage * self.refBuff.curLayer
	damage = math.ceil(damage)
	if damage < 1 then
		damage = 1
	end
    local target = self.refBuff.target
    local targetBuff = BuffManager:getCharacterBuff(target.uniqueID)
	if targetBuff and targetBuff.buffGetDemageHpRate then
        local changeHp = 0
        local newHp2 = 0
		changeHp = damage * targetBuff.buffGetDemageHpRate 
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
	local targetHp = self.refBuff.target.hp-damage
	if self.numberInfo == nil then
		self.numberInfo = {}
		self.numberInfo.numFlag = FightDefine.FIGHT_NUM_FLAG.POISONING
	end
	self.numberInfo.numType = self.refBuff.buffData.damageNumberType
	self:changeHp(self.refBuff.targetUID, damage, targetHp, 0, self.numberInfo)
end

-- 获取中毒伤害值
function BuffEffectPoisoning:getPoisonDamage()
    local damage = SkillEffect.calcDamage(self.sourceAttack or 0, self.refBuff.target[ATTR_KEY.DEFENSE], self.effectInfo.rate * 0.001, self.effectInfo.value)
    damage = damage * self.refBuff.curLayer * self:getRemainExecuteTimes()
    if damage < 1 then
		damage = 1
	end
    return damage
end

function BuffEffectPoisoning:dispose()
	BuffEffectBase.dispose(self)
	self.numberInfo = nil
end