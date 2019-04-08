--region BuffEffectCure.lua
--Date 2016/07/15
--Author zhouxiaogang
-- BUFF效果：治疗类
--endregion

--[[
	effectInfo = 
	{
		effectType = nil,
		rate = nil,		-- 千分比
		value = nil,	-- 固定值
		interval = nil,	-- 间隔时间
	}
]]

BuffEffectCure = {}
createClassWithExtends(BuffEffectCure, BuffEffectBase)

function BuffEffectCure:onBuffStart()
	self.sourceAttack = self.refBuff.attacker[ATTR_KEY.ATTACK]
end

-- 执行一次治疗效果
function BuffEffectCure:doEffect()
	local target = self.refBuff.target
	if target == nil or target.hp <= 0 then
		return	-- 已死
	end
    local poisonBuffList = target.charaBuff:getBuffEffectByType( FightDefine.BUFF_EFFECT_TYPE.POISONING )
    if poisonBuffList then
        if #poisonBuffList > 0 then
            return  --中毒不能被治疗
        end
    end
	local cureHp = self.sourceAttack * (self.effectInfo.rate * 0.001) + self.effectInfo.value
	cureHp = cureHp * self.refBuff.curLayer
	cureHp = math.ceil(cureHp)
	if cureHp < 1 then
		cureHp = 1
	end
	local newHp = cureHp + target.hp
	if newHp > target.maxhp then
		newHp = target.maxhp
	end
	local numberType = self.refBuff.buffData.damageNumberType
	if self.numberInfo == nil then
		self.numberInfo = {}
		self.numberInfo.numFlag = FightDefine.FIGHT_NUM_FLAG.CURE
		self.numberInfo.numSign = 1
	end
	self.numberInfo.numType = numberType
	self:changeHp(target.uniqueID, cureHp, newHp, 1, self.numberInfo)
end

function BuffEffectCure:dispose()
	BuffEffectBase.dispose(self)
	self.numberInfo = nil
end