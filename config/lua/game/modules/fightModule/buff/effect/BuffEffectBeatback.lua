--region BuffEffectBeatback.lua
--Date 2016/07/15
--Author zhouxiaogang
-- BUFF效果：反击类
--endregion

--[[
	effectInfo = 
	{
		effectType = nil,
		rate = nil,		-- 伤害千分比
		value = nil,	-- 固定伤害值
	}
]]

BuffEffectBeatback = {}
createClassWithExtends(BuffEffectBeatback, BuffEffectBase)

function BuffEffectBeatback:onBuffStart()
	self.sourceAttack = self.refBuff.target[ATTR_KEY.ATTACK]
end

function BuffEffectBeatback:doBeatback(attackerId, numType)
	local attacker = CharacterManager:getCharacByUId(attackerId)
	if attacker == nil then return end
	local damage = self.sourceAttack * (self.effectInfo.rate * 0.001) + self.effectInfo.value
	damage = damage * self.refBuff.curLayer
	damage = math.ceil(damage)
	if damage < 1 then damage = 1 end

    local attackerBuff = BuffManager:getCharacterBuff(attackerId)
	if attackerBuff and attackerBuff.buffGetDemageHpRate then
        local changeHp = 0
        local newHp2 = 0
		changeHp = damage * attackerBuff.buffGetDemageHpRate 
		newHp2 = attacker.hp+changeHp
		if newHp2 > attacker.maxhp then
            newHp2=attacker.maxhp
        end
                
        local numberType = attackerBuff.damageNumberType2
	    if self.numberInfo == nil then
		    self.numberInfo = {}
		    self.numberInfo.numFlag = FightDefine.FIGHT_NUM_FLAG.CURE
		    self.numberInfo.numSign = 1
	    end
	    self.numberInfo.numType = numberType
	    self:changeHp(attacker.uniqueID, changeHp, newHp2, 1, self.numberInfo)
	    return
    end
	local newHp = attacker.hp - damage
	if self.numberInfo == nil then
		self.numberInfo = {}
		self.numberInfo.numFlag = FightDefine.FIGHT_NUM_FLAG.DAMAGE
	end
	self.numberInfo.numType = numType
	self:changeHp(attackerId, damage, newHp, 0, self.numberInfo)
end

function BuffEffectBeatback:dispose()
	BuffEffectBase.dispose(self)
	self.numberInfo = nil
end