--region BuffEffectDamage.lua
--Date 2016/07/15
--Author zhouxiaogang
-- BUFF效果：伤害类
--endregion

--[[
	effectInfo = 
	{
		effectType = nil,
		radius = nil,		-- 伤害范围
		rate = nil,			-- 伤害千分比
		value = nil,		-- 固定伤害值
		interval = nil,		-- 间隔时间
	}
]]

BuffEffectDamage = {}

createClassWithExtends(BuffEffectDamage, BuffEffectBase)

function BuffEffectDamage:onBuffStart()
	self.sourceAttack = self.refBuff.attacker:getCurrentAttrib().attack or 0
	local radius = self.effectInfo.radius * 0.1
	self.sqrRadius = radius * radius
end

--加buff
function BuffEffectDamage:onAddBuff()
	if self.effectInfo.childbuffInfo then
		local buffDatas = self.effectInfo.childbuffInfo[1]
		local selfCharaList = nil
		local enemyCharList = nil
		local tempVec = nil
		for _,buff in ipairs(buffDatas) do
			if buff.target == FightDefine.BUFF_TARGET.SELF then
				ModuleEvent.dispatch(ModuleConstant.ADD_BUFF, {self.refBuff.target.uniqueID, self.refBuff.target.uniqueID, buff.buffId,self.refBuff.buffData.buffLv,BuffManager.getInstanceId()})
			elseif buff.target == FightDefine.BUFF_TARGET.SELFSIDE then
				if selfCharaList == nil then
					selfCharaList = CharacterManager:getCharacByRelation( self.refBuff.target.uniqueID, self.refBuff.target.camp, CharacterConstant.RELATION_FRIEND )
				end
				if selfCharaList then
					for _,chara in pairs(selfCharaList) do
						tempVec = chara:getPosition() - self.refBuff.target:getPosition()
			            -- 友军在范围内
			            if tempVec:SqrMagnitudeWithoutY() <= self.sqrRadius then 
							ModuleEvent.dispatch(ModuleConstant.ADD_BUFF, {self.refBuff.target.uniqueID, chara.uniqueID, buff.buffId, self.refBuff.buffData.buffLv,BuffManager.getInstanceId()})
						end
					end
				end
			elseif buff.target == FightDefine.BUFF_TARGET.ENEMY then
				if enemyCharList == nil then
					enemyCharList = CharacterManager:getCharacByRelation( self.refBuff.target.uniqueID, self.refBuff.target.camp, CharacterConstant.RELATION_ENEMY )
				end
				if enemyCharList then
					for _,chara in pairs(enemyCharList) do
						tempVec = chara:getPosition() - self.refBuff.target:getPosition()
			            -- 敌人在范围内
			            if tempVec:SqrMagnitudeWithoutY() <= self.sqrRadius then 
							ModuleEvent.dispatch(ModuleConstant.ADD_BUFF, {self.refBuff.target.uniqueID, chara.uniqueID, buff.buffId, self.refBuff.buffData.buffLv,BuffManager.getInstanceId()})
						end
					end
				end

			elseif buff.target == FightDefine.BUFF_TARGET.MASTER then --伙伴给主人加
				local charac = CharacterManager:getCharacByUId( self.refBuff.target.uniqueID )
				if charac and charac.characterType == CharacterConstant.TYPE_PARTNER then
					ModuleEvent.dispatch(ModuleConstant.ADD_BUFF, {self.refBuff.target.uniqueID, charac.masterUId, buff.buffId, self.refBuff.buffData.buffLv,BuffManager.getInstanceId()})
				end
			end
		end
	end
end


function BuffEffectDamage:doEffect()
	local target = self.refBuff.target
	if target == nil or target.hp <= 0 then
		return
	end
	local enemys = CharacterManager:getCharacByRelation( target.uniqueID, target.camp, CharacterConstant.RELATION_ENEMY )
	if enemys == nil then return end
	local targetPos = target:getPosition()
	local tempVec = nil
	local numType = self.refBuff.buffData.damageNumberType or 1
	if self.numberInfo == nil then
		self.numberInfo = {}
		self.numberInfo.numFlag = FightDefine.FIGHT_NUM_FLAG.DAMAGE
	end
	self.numberInfo.numType = numType
	if self.effectInfo.childbuffInfo then
        self:onAddBuff()
	end
	local hitSeriNum = 0
	for _,enemy in pairs(enemys) do
		if enemy.hp > 0 and (enemy.invincible == nil or enemy.invincible == 0) then
			tempVec = enemy:getPosition() - targetPos
			-- 敌人在范围内
			if tempVec:SqrMagnitudeWithoutY() <= self.sqrRadius then
				local tempDamage = SkillEffect.calcDamage(self.sourceAttack, enemy:getCurrentAttrib().defense, self.effectInfo.rate*0.001, self.effectInfo.value)
				tempDamage = tempDamage * self.refBuff.curLayer
				tempDamage = math.ceil(tempDamage)
				if tempDamage < 1 then tempDamage = 0 end
				--buff吸收伤害
				local enemyBuff = BuffManager:getCharacterBuff(enemy.uniqueID)
				if enemyBuff and enemyBuff.buffGetDemageHpRate then
                	local changeHp = 0
                	local newHp2 = 0
			        changeHp = tempDamage * enemyBuff.buffGetDemageHpRate 
			        newHp2 = enemy.hp+changeHp
		            if newHp2 > enemy.maxhp then
                        newHp2=enemy.maxhp
                    end
                
                    local numberType = enemyBuff.damageNumberType2
	                if self.numberInfo == nil then
		                self.numberInfo = {}
		                self.numberInfo.numFlag = FightDefine.FIGHT_NUM_FLAG.CURE
		                self.numberInfo.numSign = 1
	                end
	                self.numberInfo.numType = numberType
	                self:changeHp(enemy.uniqueID, changeHp, newHp2, 1, self.numberInfo)
	            else
	            	--buffer的吸血
	            	local newHp = enemy.hp - tempDamage
				    self:changeHp(enemy.uniqueID, tempDamage, newHp, 0, self.numberInfo)
                    local attackerBuff = BuffManager:getCharacterBuff(target.uniqueID)
                    if attackerBuff and attackerBuff.buffGetHpRate then
                	    local changeHp = 0
                	    local newHp2 = 0
			            changeHp = tempDamage * attackerBuff.buffGetHpRate + attackerBuff.buffGetHpValue
			            newHp2 = target.hp+changeHp
		                if newHp2 > target.maxhp then
                            newHp2=target.maxhp
                        end
                
                        local numberType = attackerBuff.damageNumberType
	                    if self.numberInfo == nil then
		                    self.numberInfo = {}
		                    self.numberInfo.numFlag = FightDefine.FIGHT_NUM_FLAG.CURE
		                    self.numberInfo.numSign = 1
	                    end
	                    self.numberInfo.numType = numberType
	                    self:changeHp(target.uniqueID, changeHp, newHp2, 1, self.numberInfo)
	                end

				    
				    hitSeriNum=hitSeriNum+1
			        if self.effectInfo.showSkillID and self.effectInfo.showSkillID~=0 then
			    	    self.effectEvtTb = self.effectEvtTb or {}
		                self.effectEvtTb[1] = self.refBuff.target.uniqueID
		                self.effectEvtTb[2] = enemy.uniqueID
		                self.effectEvtTb[3] = self.effectInfo.showSkillID
		                self.effectEvtTb[4] = 1
		                self.effectEvtTb[5] = tempDamage
		                self.effectEvtTb[6] = 0
		                self.effectEvtTb[7] = self.refBuff.target:getPosition()
		                self.effectEvtTb[8] = self.refBuff.target:getRotation()
                        self.effectEvtTb[9] =  0
		                self.effectEvtTb[10] = 0
		                self.effectEvtTb[11] = 1
		                self.effectEvtTb[12] = hitSeriNum or 1
		                self.effectEvtTb[13] = 0
		                SkillEffect.showSkillEffect(self.effectEvtTb,2)
			        end
	            end
			end
		end
	end
end

function BuffEffectDamage:dispose()
	BuffEffectBase.dispose(self)
	self.numberInfo = nil
end