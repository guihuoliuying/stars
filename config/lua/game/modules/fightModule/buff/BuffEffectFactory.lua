--region BuffEffectFactory.lua
--Date 2016/07/15
-- Author zhouxiaogang
-- 用于创建BUFF效果类
--endregion

BuffEffectFactory = {}

-- buff效果类
BuffEffectFactory.EFFECT_CLS = 
{
	[FightDefine.BUFF_EFFECT_TYPE.ATTRIB] = BuffEffectAttrib,
	[FightDefine.BUFF_EFFECT_TYPE.BEATBACK] = BuffEffectBeatback,
	[FightDefine.BUFF_EFFECT_TYPE.CTRL] = BuffEffectCTRL,
	[FightDefine.BUFF_EFFECT_TYPE.CURE] = BuffEffectCure,
	[FightDefine.BUFF_EFFECT_TYPE.DAMAGE] = BuffEffectDamage,
	[FightDefine.BUFF_EFFECT_TYPE.PHANTOM] = BuffEffectPhantom,
	[FightDefine.BUFF_EFFECT_TYPE.POISONING] = BuffEffectPoisoning,
	[FightDefine.BUFF_EFFECT_TYPE.SHIELD] = BuffEffectShield,
	[FightDefine.BUFF_EFFECT_TYPE.TAUNT] = BuffEffectTaunt,
    [FightDefine.BUFF_EFFECT_TYPE.POISONBOMB] = BuffEffectPoisonBomb,
	[FightDefine.BUFF_EFFECT_TYPE.SUPERARMOR] = BuffEffectSuperArmor,
	[FightDefine.BUFF_EFFECT_TYPE.INVINCIBLE] = BuffEffectInvincible,
	[FightDefine.BUFF_EFFECT_TYPE.HPDOWN] = BuffEffectHpDown,
	[FightDefine.BUFF_EFFECT_TYPE.DAMAGE_INTENSIFY] = BuffEffectDamageIntensify,
	[FightDefine.BUFF_EFFECT_TYPE.DAMAGE_ADD] = BuffEffectDamageAdd,
	[FightDefine.BUFF_EFFECT_TYPE.BUFF_CLEAN] = BuffEffectBuffClean,
	[FightDefine.BUFF_EFFECT_TYPE.HP_PERCENT] = BuffEffectHpPercent,
	[FightDefine.BUFF_EFFECT_TYPE.BUFF_DURATION_ADD] = BuffEffectDurationAdd,
	[FightDefine.BUFF_EFFECT_TYPE.BUFF_GETHP] = BuffEffectGetHp,
	[FightDefine.BUFF_EFFECT_TYPE.BUFF_GETDEMAGEHP] = BuffEffectGetDemageHp,
	[FightDefine.BUFF_EFFECT_TYPE.BUFF_NEWPOISONING] = BuffEffectNewPoisoning,
}

-- 创建一个buff效果
-- effectType 效果类型 FightDefine.BUFF_EFFECT_TYPE中定义
function BuffEffectFactory.createBuffEffect(effectType)
	local cls = BuffEffectFactory.EFFECT_CLS[effectType]
	local instance = nil
	if cls then
		instance = cls:create()
	end
	return instance
end