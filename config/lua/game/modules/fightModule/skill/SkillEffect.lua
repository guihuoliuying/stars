-- region
-- Date    : 2016-06-21
-- Author  : daiyaorong
-- Description :  技能效果
-- endregion

SkillEffect = {
	
}

local Random = math.LogicRandom
local Abs = math.abs
local Min = math.min
local Ceil = math.ceil
local Sqrt = math.sqrt
local Max = math.max
local LocalRanNum = 0
local LocalAttrNum = 0
local zeorVector = Vector3.zero
local G = nil
local tempDirection = Vector3.zero
local numberEvtTb = nil
local effectEvtTb = nil
local hitflyHeight = nil

local function packNumberInfo(uid, numFlag, value, numType, sign, attackerUID, serialNo, delayFrame)
	if numberEvtTb == nil then
		numberEvtTb = {}
	end
	numberEvtTb[1] = uid
	numberEvtTb[2] = numFlag
	numberEvtTb[3] = value
	numberEvtTb[4] = numType
	numberEvtTb[5] = sign
	numberEvtTb[6] = attackerUID
	numberEvtTb[7] = serialNo
	numberEvtTb[8] = delayFrame
	return numberEvtTb
end

local function getSpeedX(skillData,effectIndex)
	if skillData.hiteffect == nil or skillData.hiteffect[effectIndex] == nil then 
		return 0
	end 
	local hitEff = skillData.hiteffect[effectIndex]
	if hitEff.type and hitEff.type == 1 then 
		if not hitEff.param then 
			return 0
		end 
		return hitEff.hitspeedX
	end 
	return 0
end

local function getHitFlySpeed(skillData,effectIndex)
	if skillData.hiteffect == nil or skillData.hiteffect[effectIndex] == nil then 
		return 0
	end 
	local hitEff = skillData.hiteffect[effectIndex]
	if hitEff.type and hitEff.type == 2 then 
		local param = hitEff.param
		if not param then 
			return 0
		end 
		local disX = param[1] --米
		local disY = param[2]  --米
		if G == nil then
			G = Abs(FightModel:getFightCommon(FightDefine.FIGHT_COMMON_G)) * 0.1 --米/二次方秒
		end
		local time = Sqrt((2*disY)/G) * 2 --秒
		local speedy = (time*0.5)*G --米/秒
		local speedx = disX/time --米/秒
		if param[2] < 0 then
			speedy = -speedy --符号与Y距离配置保持一致 
		end
		return time,speedx,speedy
	end 
	return 0,0,0
end

local function getDirection( hitterPos, hitterRot, target, param )
	-- body
    local direc = nil
	if param == FightDefine.HITDIREC_FORWARD then
		return hitterRot:Forward()
	elseif param == FightDefine.HITDIREC_TOATTACKER then
		local pos = target:getPosition()
		tempDirection.x = hitterPos.x - pos.x
		tempDirection.y = hitterPos.y - pos.y
		tempDirection.z = hitterPos.z - pos.z
		direc = tempDirection:Normalize()
	else
		local pos = target:getPosition()
		tempDirection.x = pos.x - hitterPos.x
		tempDirection.y = pos.y - hitterPos.y
		tempDirection.z = pos.z - hitterPos.z
		direc = tempDirection:Normalize()
	end
	direc.y = 0
    if direc:Equals(zeorVector) then
        return hitterRot:Forward()
    end
    return direc
end

local function getFaceDirection( moveDirection, param )
	-- body
	tempDirection.y = 0
	if param == FightDefine.HITDIREC_TOATTACKER then
		tempDirection:Set( moveDirection.x, 0, moveDirection.z )
	else
		tempDirection:Set( -moveDirection.x, 0, -moveDirection.z )
	end
	return Quaternion.LookRotation(tempDirection) 
end

local function damageEffect( hitterPos, hitterRot, target, skillData, effectIndex, hitLevel )
	-- body
	if target == nil or skillData == nil then
		return
	end
	-- 组队副本减少击飞击退计算
	if EnvironmentHandler.isInServer == false and StageManager.isTeamStage(FightModel:getFightStageType()) then
		if target.state == CharacterConstant.STATE_HITFLY or target.state == CharacterConstant.STATE_HITBACK then
			local runSkill = SkillManager:getRunningSkill(target.uniqueID)
			if runSkill and runSkill.curSkillFrame < 15 then
				return
			end
		end
	end

	-- 超高霸体值时不做受击行为
	if target:getSuperArmor() >= 100 then
		return
	end

	if skillData.hiteffect == nil or skillData.hiteffect[effectIndex] == nil then
		return
	end

	local hitEffectData = skillData.hiteffect[effectIndex]
	local skillID = FightModel:getSkillID()
	local skillConfig = CFG.skill.configs[skillID]
	if skillConfig == nil then
		skillConfig = SkillVo:create()
	end
	skillConfig["skillid"] = skillID
	skillConfig["skilltype"] = 0
	skillConfig["effecttype"] = FightDefine.SKILLEFF_CHARAC
	skillConfig["skilldistance"] = 0
	skillConfig["faceDirection"] = nil
	skillConfig["stateTime"] = nil
	if skillConfig["movement"] == nil then
		skillConfig["movement"] = {}
	end
	local movement = skillConfig["movement"]
	movement[1] = movement[1] or {}
	movement[1]["tracktype"] = FightDefine.TRACK_SPEEDSTRAIGHT
	movement[1]["startpositiontype"] = FightDefine.ACTION_ATTACKPOINT
	movement[1]["endpositiontype"] = FightDefine.ACTION_UNKNOWPOINT
	movement[1]["startposition"] = { x=0,y=0,z=0 }
	movement[1]["endposition"] = { x=0,y=0,z=0 }
	movement[1]["speedx"] = getSpeedX(skillData,effectIndex)
	movement[1]["speedy"] = 0
	movement[1]["time"] = 0
	movement[1]["starttime"] = 0
	movement[1]["endtime"] = 0

	--需要注意的是,这个endPosition在击飞状态时,不是位置信息,只是一个暂存的table
	--[1]:targetId
	--[2]:方向
	--[3]:击飞的初始高度,击飞转击飞则是0
	local stateID = 0
	local movement_1 = movement[1]
	if target.state == CharacterConstant.STATE_HITFLY then --处于击飞状态
		stateID = CharacterConstant.STATE_HITFLY
		movement_1["tracktype"] = FightDefine.TRACK_HITFLY
		movement_1["endposition"][1] = target.uniqueID
		movement_1["endposition"][3] = 0
		if hitEffectData.type == FightDefine.HITACTION_HITBACK then
			movement_1["speedy"] = FightModel:getFightCommon(FightDefine.FIGHT_COMMON_FLOATSPEEDY) * 0.1
			movement_1["endposition"][2] = getDirection( hitterPos, hitterRot, target, hitEffectData.param[4] )
			skillConfig["faceDirection"] = getFaceDirection( movement_1["endposition"][2], hitEffectData.param[4] )
		else 
			local time,speedx,speedy = getHitFlySpeed(skillData,effectIndex)
			movement_1["speedy"] = speedy
			movement_1["speedx"] = speedx
			movement_1["endposition"][2] = getDirection( hitterPos, hitterRot, target, hitEffectData.param[3] )
			skillConfig["faceDirection"] = getFaceDirection( movement_1["endposition"][2], hitEffectData.param[3] )
		end 
	else
		if hitEffectData.type == FightDefine.HITACTION_HITBACK then
			stateID = CharacterConstant.STATE_HITBACK  --击退
			movement_1["tracktype"] = FightDefine.TRACK_HITBACK
			movement_1["time"] = hitEffectData.param[2] * 0.001
			movement_1["speedy"] = 0
			movement_1["endposition"][2] = getDirection( hitterPos, hitterRot, target, hitEffectData.param[4] )
			skillConfig["faceDirection"] = getFaceDirection( movement_1["endposition"][2], hitEffectData.param[4] )
			skillConfig["stateTime"] = hitEffectData.param[3] * 0.001 * ConstantData.FRAME_RATE
		elseif hitEffectData.type == FightDefine.HITACTION_HITFLY then
			stateID = CharacterConstant.STATE_HITFLY --击飞
			movement_1["tracktype"] = FightDefine.TRACK_HITFLY
			movement_1["endposition"][1] = target.uniqueID
			movement_1["endposition"][2] = getDirection( hitterPos, hitterRot, target, hitEffectData.param[3] )
			skillConfig["faceDirection"] = getFaceDirection( movement_1["endposition"][2], hitEffectData.param[3] )
			if hitflyHeight == nil then
				hitflyHeight = FightModel:getFightCommon(FightDefine.FIGHT_COMMON_HITFLYHEIGHT)
			end
			movement_1["endposition"][3] = hitflyHeight * 0.1
			local time,speedx,speedy = getHitFlySpeed(skillData,effectIndex)
			movement_1["time"] = time
			movement_1["speedx"] = speedx
			movement_1["speedy"] = speedy
		end
	end
	skillConfig["movement"] = movement

	if stateID ~= 0 then
		if hitLevel <= target:getSuperArmor() then --打断等级不足以打断霸体
			if target.state == CharacterConstant.STATE_IDLE then
				target:stateInvoke( "onIdleHurt", skillConfig["movement"][1]["endposition"][2] )
			end
			return
		end

		skillConfig["action"] = { CharacterConstant.STATE_ANIMATOR[stateID] }
		CFG.skill.configs[skillID] = skillConfig
		-- print("技能 "..tableplus.formatstring(skillConfig,true))
		ModuleEvent.dispatchWithFixedArgs( ModuleConstant.SKILL_FIRE, target.uniqueID, target.uniqueID, skillID, 1, 1, stateID)
	end
end

 -- 是否闪避
local function isMiss( attackerAttrib, targetAttrib )
	-- body
	LocalRanNum = Random( 1, 10000 )
	LocalAttrNum = ( (attackerAttrib.hit*4) / (attackerAttrib.hit*4 + targetAttrib.avoid) ) * 10000
	if LocalAttrNum < 3000 then
		LocalAttrNum = 3000
	end
	if LocalRanNum >= LocalAttrNum then
		return true
	end
	return false
end

-- 是否暴击
local function isCritHit( attackerAttrib, targetAttrib )
	-- body
	LocalRanNum = Random( 1, 10000 )
	LocalAttrNum = ( attackerAttrib.crit / (attackerAttrib.crit + targetAttrib.anticrit*4) ) * 10000
	if LocalRanNum < LocalAttrNum then
		return 1
	end
	return 0
end

---是否会心一击
local function isFocusHit(attackerAttrib,targetAttrib)

	-- body
	LocalRanNum = Random( 1, 10000 )
	if attackerAttrib.focusRate==0 and targetAttrib.focusReduce==0 then
		return 0
	end
	LocalAttrNum = ( attackerAttrib.focusRate / (4*attackerAttrib.focusRate + targetAttrib.focusReduce*8) ) * 10000
	if LocalRanNum < LocalAttrNum then
		return 1
	end
	return 0

end
------------------------------------------------全局--------------------------------------------

 function SkillEffect.hitDamage( data )
 	-- body
	local attackerID 	= data[1]	--攻击方角色UID
 	local targetID 		= data[2]	--目标角色UID
    local fireSerialNum = data[3]   --攻击序号
 	local skillID 		= data[4]	--技能ID
 	local skillLv		= data[5]	--技能等级
 	local effectIndex	= data[6]	--技能效果编号
 	local attackerAttrib= data[7]	--攻击方施法瞬间属性
 	local hitter 		= data[8]	--触发打击者
	local secondDamRate	= data[9]	--二次攻击的伤害比率(现在是幻影BUFF造成)
	local numType		= data[10]	--飘字类型（比较坑爹，幻影类buff造成的伤害飘字要用buff配置的飘字）
	local hitSeriNum 	= data[11]  --打击中的排号 拿来判断是否播放打击音效

	if effectIndex == nil or effectIndex <= 0 then
		return
	end

	local target = CharacterManager:getCharacByUId( targetID )
	if target == nil or target.hp==nil or target.hp <= 0 then
		return
	end
	local attacker = CharacterManager:getCharacByUId( attackerID )
	if attackerAttrib == nil and attacker == nil then
		return
	end
	local targetAttrib = target:getCurrentAttrib()
	attackerAttrib = attackerAttrib or attacker:getCurrentAttrib()
	local targetBuff = BuffManager:getCharacterBuff(targetID)
	local attackerBuff = BuffManager:getCharacterBuff(attackerID)
	local skillData = FightModel:getSkillData(skillID)
	local skillLvData = FightModel:getSkillLevelData( skillID, skillLv )
	hitter = hitter or attacker --空值则为攻击方 非空则为子弹 主要拿来计算击退方向之类的表现数据

	if isMiss( attackerAttrib, targetAttrib ) == true then
		if EnvironmentHandler.isInServer then
			local passedFrame = SkillManager:getPassedFrameAfterSkillStart(attackerID, fireSerialNum)
			ModuleEvent.dispatch(ModuleConstant.SHOW_FIGHT_NUMBER, packNumberInfo(targetID, FightDefine.FIGHT_NUM_FLAG.MISS, 0, 0, 0, attackerID, fireSerialNum or 0, passedFrame))
		else
			ModuleEvent.dispatch(ModuleConstant.SHOW_FIGHT_NUMBER, packNumberInfo(targetID, FightDefine.FIGHT_NUM_FLAG.MISS))
		end
		-- 闪避
		return
	end



	SkillEffect.doBuff(attacker, target, skillData, skillLv, effectIndex)
    if attacker and attacker.pSkillHandler and type(attacker.pSkillHandler) == "function" then
        attacker.pSkillHandler:commonHit( targetID ) --命中触发被动技能
    end
	local damNum = 0
	local isFocus=0
	local isCrit = 0
	if skillLvData == nil or skillLvData.coefficient == nil then
		return
	end
	if skillLvData.coefficient ~= 0 then
		-- 伤害
		local effectNum = skillLvData.damage
		if attacker and attacker.allskill and attacker.allskill[skillID] then
			--包含全部附加伤害
			effectNum = attacker.allskill[skillID]["damage"]
		end
		damNum = SkillEffect.calcDamage(attackerAttrib.attack, targetAttrib.defense, skillLvData.coefficient, effectNum)
		-- 暴击伤害加成
		isFocus= isFocusHit(attackerAttrib,targetAttrib)
		if isFocus==1 then
			damNum = damNum * 3    ---会心伤害三倍伤害
		else
			isCrit = isCritHit( attackerAttrib, targetAttrib )
			if isCrit == 1 then
				local add = attackerAttrib.crithurtadd or 0
				local reduce = targetAttrib.crithurtreduce or 0
				local critNum = Max( 1, (2000+add-reduce)*0.001 )
				damNum = damNum * critNum
			end
		end
		if secondDamRate then
			damNum = damNum * secondDamRate
		end
		damNum = damNum + SkillEffect.calAddDamage( attacker, target, skillData.damageadd, damNum, isCrit)
		damNum = Ceil(damNum)	
        local hitPassPercent=nil
		if attacker and attacker.pSkillHandler then
			hitPassPercent=attacker.pSkillHandler:Hit(targetID,isCrit==1,isFocus==1)
		end
		-- 计算BUFF的伤害加成
		local buffDamageRate = 0
		local buffDamageValue = 0
		if targetBuff and targetBuff.intensifyDamageRate then
			buffDamageRate = buffDamageRate + targetBuff.intensifyDamageRate
			buffDamageValue = buffDamageValue + targetBuff.intensifyDamageValue
		end
		if attackerBuff and attackerBuff.addDamageRate then
			buffDamageRate = buffDamageRate + attackerBuff.addDamageRate
			buffDamageValue = buffDamageValue + attackerBuff.addDamageValue
		end
		damNum = damNum * (1 + buffDamageRate) + buffDamageValue
		if damNum < 1 then
			damNum = 1
		end
		if targetBuff then
			-- 目标有反击类buff
			local beatbackEffects = targetBuff:getBuffEffectByType(FightDefine.BUFF_EFFECT_TYPE.BEATBACK)
			if beatbackEffects and next(beatbackEffects) then
				local tempNumType = skillData.damagenumbertype[effectIndex]
				for _,effect in pairs(beatbackEffects) do
					effect:doBeatback(attackerID, tempNumType)
				end
			end
			-- 敌方带护盾类BUFF效果，伤害要减去护盾值
			local shieldEffects = targetBuff:getBuffEffectByType(FightDefine.BUFF_EFFECT_TYPE.SHIELD)
			if shieldEffects and next(shieldEffects) then
				for _,effect in pairs(shieldEffects) do
					damNum = effect:doShield(damNum)
					if damNum <= 0 then break end
				end
			end
		end
		if target.pSkillHandler then
			target.pSkillHandler:beHit( attackerID )
		end
        
        --特定情况下的被动技能被触发
        if hitPassPercent then
        	damNum=damNum*(hitPassPercent+1000)/1000
        end

		damNum = Ceil(damNum)
		if damNum < 1 then
			damNum = 1
		end
          

		-- if attackerID == RoleData.roleId then
		-- 	damNum = 8888888
		-- end

        -- 吸收
        if targetBuff and targetBuff.buffGetDemageHpRate then
        	local newHp = 0
        	local changeHp = 0
			changeHp = damNum * targetBuff.buffGetDemageHpRate
			newHp = target.hp+changeHp
			if newHp > target.maxhp then
                newHp=target.maxhp
            end
		    ModuleEvent.dispatch( ModuleConstant.CHARAC_UPDATE_ATTR, { target.uniqueID, ConstantData.ROLE_CONST_HP, newHp } )
	        if changeHp ~= 0 and EnvironmentHandler.isPvpClient == false then
		        if numberEvtTb == nil then numberEvtTb = {} end
		        numberEvtTb[1] = target.uniqueID
		        numberEvtTb[2] = FightDefine.FIGHT_NUM_FLAG.CURE
		        numberEvtTb[3] = changeHp
		        numberEvtTb[4] = targetBuff.damageNumberType2 or 1
		        numberEvtTb[5] = 1
		        numberEvtTb[6] = ""
		        numberEvtTb[7] = 0
		        numberEvtTb[8] = 0
		        ModuleEvent.dispatch(ModuleConstant.SHOW_FIGHT_NUMBER, numberEvtTb)
	        end
        	return   ---有吸收buff取消本次伤害
        end
        
		-- 吸血   吸血    吸血
        if attackerBuff and attackerBuff.buffGetHpRate then
        	local newHp = 0
        	local changeHp = 0
			changeHp = damNum * attackerBuff.buffGetHpRate + attackerBuff.buffGetHpValue
			changeHp = math.ceil(changeHp)
			newHp = attacker.hp+changeHp
			if newHp > attacker.maxhp then
                newHp=attacker.maxhp
            end
		    ModuleEvent.dispatch( ModuleConstant.CHARAC_UPDATE_ATTR, { attacker.uniqueID, ConstantData.ROLE_CONST_HP, newHp } )
	        if changeHp ~= 0 and EnvironmentHandler.isPvpClient == false then
		        if numberEvtTb == nil then numberEvtTb = {} end
		        numberEvtTb[1] = attacker.uniqueID
		        numberEvtTb[2] = FightDefine.FIGHT_NUM_FLAG.CURE
		        numberEvtTb[3] = changeHp
		        numberEvtTb[4] = attackerBuff.damageNumberType or 1
		        numberEvtTb[5] = 1
		        numberEvtTb[6] = ""
		        numberEvtTb[7] = 0
		        numberEvtTb[8] = 0
		        ModuleEvent.dispatch(ModuleConstant.SHOW_FIGHT_NUMBER, numberEvtTb)
	        end
		end
        

		local hpValue = target.hp - damNum
		if hpValue < 0 then
			hpValue = 0
		end

		-- 测试日志打印
	--	 local logAtkId = attackerID
	--	 if attackerID == RoleData.roleId then
	--	 	logAtkId = RoleData.name
	--	 end
	--	 local logTarId = targetID
	--	 if targetID == RoleData.roleId then
	--	 	logTarId = RoleData.name
	--	 end
		--UILog("目标:"..logTarId.." 前血量:"..target.hp.." 攻击者:"..logAtkId.." 技能:"..skillID.."  伤害值:"..damNum.."  剩余血量:"..hpValue)
		local stageType = FightModel:getFightStageType()
		if (stageType == ConstantData.STAGE_TYPE_DUNGEON or stageType == ConstantData.STAGE_TYPE_NEWGUIDE
			or stageType == ConstantData.STAGE_TYPE_POEM) and targetID == RoleData.roleId then
			if hpValue == 0 and (FightModel:canBeDead() == false or stageType == ConstantData.STAGE_TYPE_NEWGUIDE) then
				ModuleEvent.dispatch( ModuleConstant.CHARAC_UPDATE_ATTR, { targetID, ConstantData.ROLE_CONST_HP, target.maxhp } )
				return
			end
		end
		ModuleEvent.dispatch( ModuleConstant.CHARAC_UPDATE_ATTR, { targetID, ConstantData.ROLE_CONST_HP, hpValue } )
		target.hatredmap:addHatred( attackerID, damNum )
		if EnvironmentHandler.isInServer == false then --服务端环境不同步
			FixedTimeDispatcher.triggerDamageUpload(attackerID,targetID,0,damNum,hpValue)
			FixedTimeDispatcher.triggerDamageSave(attackerID,targetID,0,damNum)
		else
			FightServerControl.setFrameRecord( attackerID, targetID, damNum, hpValue )  
		end

		if attackerBuff and damNum > 0 and secondDamRate == nil then
			-- 攻击者携带魅影BUFF，给敌方造成二次伤害
			local phantomEffects = attackerBuff:getBuffEffectByType(FightDefine.BUFF_EFFECT_TYPE.PHANTOM)
			if phantomEffects and next(phantomEffects) then
				for _,effect in pairs(phantomEffects) do
					effect:doSecondDamage(targetID, tableplus.shallowcopy(data))
				end
			end
		end
	end

	local hitLevel = 1
	if skillData.hitlevel and skillData.hitlevel[effectIndex] then
		local hitLevelData = skillData.hitlevel[effectIndex]
		local hitLevelUp = SkillEffect.calHitlevelUp( attacker, target, hitLevelData, isCrit ) or 0;
		hitLevel = hitLevelData["level"] + hitLevelUp
	end
	if EnvironmentHandler.isInServer then
		-- 击飞击退
		damageEffect( hitter:getPosition(), hitter:getRotation(), target, skillData, effectIndex, hitLevel )
		-- 需要双端同步
		local hitpos = hitter:getPosition()
		local hitrot = hitter:getRotation():ToEulerAngles()
		local passedFrame = SkillManager:getPassedFrameAfterSkillStart(attackerID, fireSerialNum)
		if effectEvtTb == nil then effectEvtTb = {} end
		effectEvtTb[1] = attackerID
		effectEvtTb[2] = targetID
		effectEvtTb[3] = skillID
		effectEvtTb[4] = effectIndex
		effectEvtTb[5] = damNum
		effectEvtTb[6] = isCrit
		effectEvtTb[7] = math.round(hitpos.x, 2)
		effectEvtTb[8] = math.round(hitpos.y, 2)
		effectEvtTb[9] = math.round(hitpos.z, 2)
		effectEvtTb[10] = math.round(hitrot.y, 0)
        effectEvtTb[11] = fireSerialNum or 0
		effectEvtTb[12] = passedFrame
		effectEvtTb[13] = numType or 0
		effectEvtTb[14] = hitLevel
		effectEvtTb[15] = hitSeriNum or 1
		effectEvtTb[16] = isFocus
		ModuleEvent.dispatch(ModuleConstant.SKILL_SHOWEFFECT, effectEvtTb)
	else
		if effectEvtTb == nil then effectEvtTb = {} end
		effectEvtTb[1] = attackerID
		effectEvtTb[2] = targetID
		effectEvtTb[3] = skillID
		effectEvtTb[4] = effectIndex
		effectEvtTb[5] = damNum
		effectEvtTb[6] = isCrit
		effectEvtTb[7] = hitter:getPosition()
		effectEvtTb[8] = hitter:getRotation()
        effectEvtTb[9] = fireSerialNum or 0
		effectEvtTb[10] = numType or 0
		effectEvtTb[11] = hitLevel
		effectEvtTb[12] = hitSeriNum or 1
		effectEvtTb[13] = isFocus
		ModuleEvent.dispatch(ModuleConstant.SKILL_SHOWEFFECT, effectEvtTb)
	end
 end

 function SkillEffect.calcDamage(attackerAtk, targetDef, coefficient, fitDamage)
	local damage = (attackerAtk - targetDef) * coefficient + fitDamage
	local minDamage = 0.2 * attackerAtk * coefficient;
	if damage < minDamage then
		damage = minDamage
	end
	return damage
 end

 function SkillEffect.calAddDamage( attacker, target, config, damNum, isCrit )
    if config == nil then return 0 end
    local character = attacker
    if config.targetType == 2 then
        character = target
    end

    local charaBuff = BuffManager:getCharacterBuff(character.uniqueID)
    if config.conditionType == FightDefine.CONDITION_HP then
        local percent = (character.hp / character.maxhp) * 1000
        if percent >= config.param then
            return 0
        end
    elseif config.conditionType == FightDefine.CONDITION_BUFFEFF then
        if charaBuff == nil or charaBuff:hasBuff(config.param) == false then
            return 0
        end
    elseif config.conditionType == FightDefine.CONDITION_BUFFTYPE then
        if charaBuff == nil or charaBuff:hasBuffType(config.param) == false then
            return 0
        end
    elseif config.conditionType == FightDefine.CONDITION_CRIT then
        if isCrit ~= 1 then
            return 0
        end
    elseif config.conditionType == FightDefine.CONDITION_STATE then
        if character.state ~= config.param then
            return 0
        end
    end
    return Ceil( damNum * config.percent )
 end

 function SkillEffect.calHitlevelUp( attacker, target, config,isCrit )
 	if config == nil then return 0 end
 	if config.targetType == 0 then return 0 end
    local character = attacker
    if config.targetType == 2 then
        character = target
    end
    if character == nil then return 0 end
    
    local charaBuff = BuffManager:getCharacterBuff(character.uniqueID)
    if config.conditionType == FightDefine.CONDITION_HP then
        local percent = (character.hp / character.maxhp) * 1000
        if percent >= config.param then
            return 0
        end
    elseif config.conditionType == FightDefine.CONDITION_BUFFEFF then
        if charaBuff == nil or charaBuff:hasBuffEffect(config.param) == false then
            return 0
        end
    elseif config.conditionType == FightDefine.CONDITION_BUFFTYPE then
        if charaBuff == nil or charaBuff:hasBuffType(config.param) == false then
            return 0
        end
    elseif config.conditionType == FightDefine.CONDITION_CRIT then
        if isCrit ~= 1 then
            return 0
        end
    elseif config.conditionType == FightDefine.CONDITION_STATE then
        if character.state ~= config.param then
            return 0
        end  
    end

    return config.levelUp
 end

 function SkillEffect.doBuff(attacker, target, skillData, skillLv, effectIndex)
	if attacker == nil or target == nil or skillData == nil or effectIndex == nil then
		return
	end

	if skillData.buffInfo == nil or skillData.buffInfo[effectIndex] == nil then 
		return
	end
	local buffList = skillData.buffInfo[effectIndex]
	if buffList and next(buffList) then
		for _,buff in pairs(buffList) do
			if buff.target == FightDefine.BUFF_TARGET.ENEMY then
				ModuleEvent.dispatch(ModuleConstant.ADD_BUFF, {attacker.uniqueID, target.uniqueID, buff.buffId, skillLv, BuffManager.getInstanceId()})
			end
		end
	end
 end

-- 技能命中的各种表现效果   demagebuff会有这样的表现
-- 1-攻击者ID 2-目标ID 3-技能ID 4-技能效果下标 5-伤害值 6-是否暴击 7-打击者位置 8-打击者朝向 9-攻击序号 10-飘字类型 11-打断等级 12-打击序号
-- ，showtype 2表示受击buff
 function SkillEffect.showSkillEffect( param ,showtype)
 	-- body
 	local target = CharacterManager:getCharacByUId( param[2] )
 	if target == nil then
		return
	end
	local attacker = CharacterManager:getCharacByUId( param[1] )
	local skillData = FightModel:getSkillData(param[3])
	if skillData == nil then return end

	-- 击飞击退
	local hitPos = param[7]
	damageEffect( hitPos, param[8], target, skillData, param[4], param[11] )

	local effectName = FightModel:getFightCommon(FightDefine.FIGHT_COMMON_HITEFFCT)
	if skillData.specialeffect ~= nil and skillData.specialeffect[param[4]] ~= nil and skillData.specialeffect[param[4]] ~= "0" then
		local effectData = skillData.specialeffect[param[4]]
		-- 震屏
		if effectData.shakeId ~= nil and param[12] == 1 then
			if attacker then
				if attacker.characterType == CharacterConstant.TYPE_SELF or attacker.characterType == CharacterConstant.TYPE_MONSTER
					or (attacker.characterType == CharacterConstant.TYPE_PARTNER and attacker.masterUId == RoleData.roleId) then
					CameraManager:shakeCam( effectData.shakeId ) --主角及其伙伴或怪物才会震动
				end
			end
		end
		-- 屏幕特效
		if target.characterType == CharacterConstant.TYPE_SELF and param[12] == 1 then
			if effectData.screenEffect ~= nil then
				ModuleEvent.dispatch( ModuleConstant.STAGE_FIGHT_SCREEN, effectData.screenEffect )
			end
		end
		-- 特殊命中特效
		effectName = effectData.hitEffectName or effectName
	end
	local damNum = param[5]
	if EnvironmentHandler.isInServer == false and damNum > 0 then
		-- 命中特效
		local targetPos = target:getPosition()
		tempDirection.x = hitPos.x - targetPos.x
		tempDirection.y = hitPos.y - targetPos.y
		tempDirection.z = hitPos.z - targetPos.z
		local effectNode = SideEffectManager.getFreeTable()
		effectNode.entityID = target.uniqueID
		effectNode.effectResName = effectName
		effectNode.boneName = "Heart_Point00"
		effectNode.direction = Quaternion.LookRotation(tempDirection)
		ModuleEvent.dispatch(ModuleConstant.PLAY_AUTO_EFFECT, effectNode)
		-- 音效
		if attacker ~= nil and attacker.hitsound ~= nil and param[12] == 1 then
			local index = math.ceil( math.LogicRandom( 0, #attacker.hitsound ) )
			if attacker.hitsound[index] ~= "0"
				and (attacker.characterType == CharacterConstant.TYPE_SELF or attacker.characterType == CharacterConstant.TYPE_MONSTER) then
				AudioMgr.PlaySoundInfoFight( attacker.hitsound[index] )
			end
		end
		-- 高亮
		target:showBright()
		-- 连击
		if attacker ~= nil and attacker.characterType == CharacterConstant.TYPE_SELF then
			ModuleEvent.dispatch(ModuleConstant.COMBO_HIT_CHANGE)
		end
	end
	-- 飘字
	if damNum > 0 and (showtype==nil or showtype~=2 )then
		local numType = param[10]
		if numType == nil or numType == 0 then
			numType = skillData.damagenumbertype[param[4]]
		end
		if param[6] == 1 then
			ModuleEvent.dispatch( ModuleConstant.SHOW_FIGHT_NUMBER, packNumberInfo( param[2], FightDefine.FIGHT_NUM_FLAG.CRIT, damNum, numType))
		elseif param[13] ==1 then
			ModuleEvent.dispatch( ModuleConstant.SHOW_FIGHT_NUMBER, packNumberInfo( param[2], FightDefine.FIGHT_NUM_FLAG.FOCUS, damNum, numType))
		else
			ModuleEvent.dispatch( ModuleConstant.SHOW_FIGHT_NUMBER, packNumberInfo( param[2], FightDefine.FIGHT_NUM_FLAG.DAMAGE, damNum, numType))
		end
	end
 end