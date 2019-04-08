-- region
-- Date    : 2016-08-03
-- Author  : daiyaorong
-- Description :  被动技能处理器
-- endregion

PassSkillHandler = {
	character = nil,
	activeList = nil,
	activeRecord = nil,
    lockList = nil,
}

PassSkillHandler = createClass( PassSkillHandler )
local Random = math.LogicRandom

function PassSkillHandler:init( character ,lock)
	-- body
	self.character = character
	self.activeList = {}
	self.activeRecord = {}
    self.cdList = {}
    self.lockList = {}
	local passSkillList = self.character.passSkillList

	for k,v in ipairs( passSkillList ) do
        if v.id and v.level then
		    local skilldata = FightModel:getSkillLevelData( v.id, v.level )
			if skilldata then
				if skilldata.condition and skilldata.condition[1] ~= FightDefine.PASSACTIVE_NODE then
					self.activeList[skilldata.condition[1]] = self.activeList[skilldata.condition[1]] or {}
					table.insert( self.activeList[skilldata.condition[1]], skilldata)
                    if lock then
                        self.lockList[v.id] = true                  
                    end
				end
			end
        end
	end

	if EnvironmentHandler.isInServer == false and self.character then
		if self.character.characterType == CharacterConstant.TYPE_SELF then
			self:hpUpdate()
		else
			self:onFightStart() -- 可能model还没加载完 加个定时器
		end
	end
end

function PassSkillHandler:onFightStart()
	FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(ConstantData.FRAME_RATE, 1, function()
		if self.character then
			self:hpUpdate()
		end
	end)
end

local LocalRanNum = 1
local function isPercentOK(percent)
	-- body
	LocalRanNum = Random( 1, 1000 )
	if LocalRanNum < percent then
		return true
	end
	return false
end

--每次攻击都去做判断   目标类型+条件类型+参数，目标类型：1代表自己，2代表敌人  条件类型：1类型，血量判断，参数填目标类型百分比      2类型，buff判断，参数填buffid   3类型，buff类型判断，填1或者2,1代表增益类，2代表减益类    4类型，暴击判断，不用填参数。
local aimcharacter = nil
local isSkillEffect = nil
local backeffectInfo =nil  --这个参数转用于技能触发效果为3的情况
function PassSkillHandler:Hit(targetID,isCrit,isFocus)
	-- body
	if self.activeList and self.activeList[FightDefine.PASSACTIVE_HITS] ~= nil then
		for k,v in ipairs( self.activeList[FightDefine.PASSACTIVE_HITS] ) do
            if self:isLock(v.skillid) == false and self:isInCD(v) == false then
            	aimcharacter = nil 
			    if v.condition[2] ==1 then
			    	aimcharacter = self.character
			    elseif v.condition[2] ==2 then
			    	aimcharacter=CharacterManager:getCharacByUId(targetID)
			    end 
			    if aimcharacter==nil then
			    	return
			    end
                isSkillEffect=false
			    if v.condition[3] == 1 then
                    if v.condition[4] == 0 then
                        if aimcharacter.hp * 1000 / aimcharacter.maxhp < v.condition[5] then
                    	    isSkillEffect = true
                        end
                    elseif v.condition[4] == 1 then
                	    if aimcharacter.hp * 1000 / aimcharacter.maxhp > v.condition[5] then
                    	    isSkillEffect = true
                        end
                    end
			    elseif v.condition[3] == 2 then
			    	if BuffManager:characHasBuff(aimcharacter.uniqueID, v.condition[4]) then
			    		isSkillEffect = true
			    	end
			    elseif v.condition[3] == 3 then
			    	if BuffManager:getCharacterBuff(aimcharacter.uniqueID) and BuffManager:getCharacterBuff(aimcharacter.uniqueID):hasBuffType(v.condition[4]) then
			    		isSkillEffect = true
			    	end
			    elseif v.condition[3] == 4 then
			    	if (isCrit or isFocus) and isPercentOK(v.condition[4]) then
			    		isSkillEffect = true
			    	end
			    end

                if v.effectinfo["type"] == FightDefine.PASSEFF_DemagePercent then
                	if isSkillEffect==true then
                	    aimcharacter = nil
                        isSkillEffect = nil
                        backeffectInfo =nil
                        return v.effectinfo["param"][1]
                    end
                else
                	if isSkillEffect==true then
                		if v.effectinfo["param"][1] == FightDefine.PASSTARGET_SELF then
                            self:makeEffect( self.character.uniqueID, self.character.uniqueID, v, BuffManager.getInstanceId() )
				        else
                            self:makeEffect( self.character.uniqueID, targetID, v, BuffManager.getInstanceId() )
                        end
                	end
                	
                end

            end
		end
	end
	aimcharacter = nil
    isSkillEffect = nil
    backeffectInfo =nil
    return nil
end


--血量变化触发
function PassSkillHandler:hpUpdate()
	-- body
	if self.activeList and self.activeList[FightDefine.PASSACTIVE_HP] ~= nil then
		local percent = self.character.hp / self.character.maxhp
		for k,v in ipairs( self.activeList[FightDefine.PASSACTIVE_HP] ) do
            if self:isLock(v.skillid) == false and self:isInCD(v) == false then
			    if percent < v.condition[2] then
				    if self.activeRecord[v.skillLevelKey] == nil then
					    self.activeRecord[v.skillLevelKey] = BuffManager.getInstanceId()
				        self:makeEffect( self.character.uniqueID, self.character.uniqueID, v, self.activeRecord[v.skillLevelKey] )
                    end
			    else
				    if self.activeRecord[v.skillLevelKey] ~= nil then
                        self:stopEffect( self.character.uniqueID, self.activeRecord[v.skillLevelKey] )
					    self.activeRecord[v.skillLevelKey] = nil
				    end
			    end
            end
		end
	end
end

--特定技能命中触发
function PassSkillHandler:skillHit( skillid, targetID, hitIndex )
	-- body
	if self.activeList and self.activeList[FightDefine.PASSACTIVE_HIT] ~= nil then
		for k,v in ipairs( self.activeList[FightDefine.PASSACTIVE_HIT] ) do
            if self:isLock(v.skillid) == false and self:isInCD(v) == false and self:figureProbability(self.character.uniqueID, targetID, v) == true then
			    local skilldata = FightModel:getSkillData( skillid )
			    if skilldata == nil then
			    	return
		    	end
		    	local isOk = false
		    	local compareValue = skillid --默认匹配skillid
		    	if v.condition[4] == 2 then --匹配skilltype
		    		compareValue = skilldata.skilltype
	    		end
	    		for index, value in pairs(v.condition[3]) do --匹配
	    			if compareValue == value then
	    				isOk = true
	    				break
	    			end
    			end
	    		if isOk then
				    if v.effectinfo["param"][1] == FightDefine.PASSTARGET_SELF then
					    if hitIndex == 1 then
                            self:makeEffect( self.character.uniqueID, self.character.uniqueID, v, BuffManager.getInstanceId() )
                        end
				    else
                        self:makeEffect( self.character.uniqueID, targetID, v, BuffManager.getInstanceId() )
                    end
                end
            end
		end
	end
end

--命中触发
function PassSkillHandler:commonHit( targetID )
    if self and self.activeList and type(self.activeList) == "table" and self.activeList[FightDefine.PASSACTIVE_COMMONHIT] ~= nil then
		for k,v in ipairs( self.activeList[FightDefine.PASSACTIVE_COMMONHIT] ) do
            if self:isLock(v.skillid) == false and self:isInCD(v) == false then
			    if self:figureProbability(self.character.uniqueID, targetID, v) == true then
				    if v.effectinfo["param"][1] == FightDefine.PASSTARGET_SELF then
                        self:makeEffect( self.character.uniqueID, self.character.uniqueID, v, BuffManager.getInstanceId() )
				    else
                        self:makeEffect( self.character.uniqueID, targetID, v, BuffManager.getInstanceId() )
                    end
			    end
            end
		end
	end
end

--技能触发
function PassSkillHandler:fireSkill( targetID, skillid )
    if self.activeList and self.activeList[FightDefine.PASSACTIVE_SKILL] ~= nil then
		for k,v in ipairs( self.activeList[FightDefine.PASSACTIVE_SKILL] ) do
            if self:isLock(v.skillid) == false and self:isInCD(v) == false and self:figureProbability(self.character.uniqueID, targetID, v) == true then
            	local skilldata = FightModel:getSkillData( skillid )
			    if skilldata == nil then
			    	return
		    	end
		    	local isOk = false
		    	local compareValue = skillid --默认匹配skillid
		    	if v.condition[4] == 2 then --匹配skilltype
		    		compareValue = skilldata.skilltype
	    		end
	    		for index, value in pairs(v.condition[3]) do --匹配
	    			if compareValue == value then
	    				isOk = true
	    				break
	    			end
    			end
			    if isOk then
                    if v.effectinfo["param"][1] == FightDefine.PASSTARGET_SELF then
				        self:makeEffect( self.character.uniqueID, self.character.uniqueID, v, BuffManager.getInstanceId() )
                    else
                        self:makeEffect( self.character.uniqueID, targetID, v, BuffManager.getInstanceId() )
                    end
			    end
            end
		end
	end
end

--受击触发
function PassSkillHandler:beHit( attackerID )


    if self.activeList and self.activeList[FightDefine.PASSACTIVE_BEHIT] ~= nil then
		local percent = self.character.hp / self.character.maxhp
		for k,v in ipairs( self.activeList[FightDefine.PASSACTIVE_BEHIT] ) do
            if self:isLock(v.skillid) == false and self:isInCD(v) == false then
			    if percent < v.condition[3] and self:figureProbability(self.character.uniqueID, attackerID, v) == true then
                    if v.effectinfo["param"][1] == FightDefine.PASSTARGET_SELF then

				        self:makeEffect( self.character.uniqueID, self.character.uniqueID, v, BuffManager.getInstanceId() )
                    else
                        self:makeEffect( self.character.uniqueID, attackerID, v, BuffManager.getInstanceId() )
                    end
			    end
            end
		end
	end
end

--计算概率
local randomNum = nil
function PassSkillHandler:figureProbability( attackerID, targetID, data )
    randomNum = math.ceil( math.LogicRandom(0, 1000) )
    if data.skillType == FightDefine.PASSTYPE_TRUMP then
        --根据法宝属性计算概率
        local triggerRate = 0
        local penet = 0
        local resis = 0
        local attrIndex = 0

        local attacker = CharacterManager:getCharacByUId( attackerID )

        if attacker.allskill[data.skillid] and attacker.allskill[data.skillid]["skillAttr"] ~= nil then
            triggerRate = attacker.allskill[data.skillid]["skillAttr"]["trumpRate"]
            attrIndex = attacker.allskill[data.skillid]["skillAttr"]["attrIndex"]
            penet = attacker[EXT_ATTR[attrIndex]]
        else
        	--当拿不到技能对应skillAttri属性时候，判断是否为符文副本，是的话走data.condition[2]值判断是否被动生效，解决预热法宝选择使用被动技能不生效问题
        	if StageManager.getCurStageType() == ConstantData.STAGE_TYPE_BENEFIT_TOKEN then
        		return randomNum <= data.condition[2]
        	end
        end

        local target = CharacterManager:getCharacByUId( targetID )
        if target ~= nil and attrIndex ~= 0 then
            resis = target[EXT_ATTR[attrIndex+10]]
        end

        local part2 = penet+resis

        if part2 == 0 then part2 = 1 end --分母不可为0
        return randomNum <= ( triggerRate * penet / part2 )
    else
        --根据配置概率data.condition[2]
        return randomNum <= data.condition[2]
    end
end

function PassSkillHandler:isInCD( data )
    return self.cdList[data.skillLevelKey] ~= nil and data.cooldown > 0
end

function PassSkillHandler:isLock(skillid)
    if self.lockList[skillid] ~= true then
        return false
    end
    return true
end

function PassSkillHandler:unlockSkill(skillid)
    if self.lockList[skillid] == true then
        self.lockList[skillid] = nil
    end 

end

function PassSkillHandler:makeEffect( attackerID, targetID, data, instantID )
    ModuleEvent.dispatch(ModuleConstant.ADD_BUFF, { attackerID, targetID, data.effectinfo["param"][2], data.level, instantID })
    if data.cooldown > 0 then
        local function CDFinish()
            FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):removeLuaFunc( self.cdList[data.skillLevelKey] )
            self.cdList[data.skillLevelKey] = nil
            ModuleEvent.dispatch(ModuleConstant.DRAGONBALL_SETVISIBLE, { attackerID, data.skillid, 1 })
        end
        self.cdList[data.skillLevelKey] = FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(data.cooldown,1,CDFinish)
        ModuleEvent.dispatch(ModuleConstant.DRAGONBALL_SETVISIBLE, { attackerID, data.skillid, 0 })
    end
end

function PassSkillHandler:stopEffect( attackerID, instantID )
    ModuleEvent.dispatch(ModuleConstant.REMOVE_BUFF, {attackerID, instantID})
end

function PassSkillHandler:dispose()
	-- body
	self.character = nil
	self.activeList = nil
	self.activeRecord = nil
    self.lockList = {}
    for k,v in pairs(self.cdList) do
        FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):removeLuaFunc(v)
    end
    self.cdList = nil
end