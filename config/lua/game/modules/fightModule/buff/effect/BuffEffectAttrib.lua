--region BuffEffectAttrib.lua
--Date 2016/07/15
--Author zhouxiaogang
-- BUFF效果：属性类
--endregion

--[[
	effectInfo = 
	{
		effectType = nil,
		attribName = nil,	-- 属性名
		rate = nil,			-- 千分比
		value = nil,		-- 增加值
	}
]]

BuffEffectAttrib = {}

createClassWithExtends(BuffEffectAttrib, BuffEffectBase)

function BuffEffectAttrib:init(buff, effectInfo)
	BuffEffectBase.init(self, buff, effectInfo)
	self.attribValue = 0
end

function BuffEffectAttrib:onBuffStart()
	self.attribValue = 0
	if self.effectInfo.attribName == "maxhp" then
		local characBuff = self.refBuff.target.charaBuff
		if characBuff then
			if characBuff.targetSourceMaxHp == nil then
				characBuff.targetSourceMaxHp = self.refBuff.target.maxhp or 0
			end
			self.sourceValue = characBuff.targetSourceMaxHp
		else
			self.sourceValue = self.refBuff.target.maxhp or 0
		end
	else
		self.sourceValue = self.refBuff.target[self.effectInfo.attribName] or 0
	end
end

function BuffEffectAttrib:doEffect()
	if self.sourceValue == nil then return end
	self.attribValue = self.sourceValue * (self.effectInfo.rate * 0.001) + self.effectInfo.value
	self.attribValue = self.attribValue * self.refBuff.curLayer
	if self.effectInfo.attribName == "maxhp" then
		local target = self.refBuff.target
		if target and target.charaBuff then
			local newMaxHp = target.charaBuff.targetSourceMaxHp + self.attribValue
			local curHp = math.floor((target.hp / target.maxhp) * newMaxHp)
			if curHp <= 0 then curHp = 1 end
			ModuleEvent.dispatch( ModuleConstant.CHARAC_UPDATE_ATTR, { target.uniqueID, ConstantData.ROLE_CONST_HP, curHp } )
			--CmdLog(string.format("charac=%s, buffid=%s, maxhp=%s, hp=%s", target.uniqueID, self.refBuff.buffData.buffId, newMaxHp, curHp))
		end
	elseif self.effectInfo.attribName == "exp" then
        self.attribValue = self.effectInfo.value
    end
end

function BuffEffectAttrib:overlay()
	self:doEffect()
end


local msgRecords = nil
local function sendMaxHpMsgToClient(uid, percent)
	if msgRecords == nil then
		msgRecords = {}
	end
	-- 已经提示过就不再提示
	if msgRecords[uid] and msgRecords[uid] >= percent then
		return
	end
	msgRecords[uid] = percent
    local stageType = FightModel:getFightStageType() 
    if stageType == ConstantData.STAGE_TYPE_CAMP_DAILY then
    	EnvironmentHandler.sendMsgToClient(uid, 1, "campactivity2_tips_buffhpup", {percent})
    else
    	EnvironmentHandler.sendMsgToClient(uid, 1, "dailyfivepvp_vipskilltips_towersoul_addtips", {percent})
    end
end
-- 计算某角色属性类buff效果的总属性加成
function BuffEffectAttrib.recalcAllEffect(charaBuff, effects)

	if charaBuff.attrib == nil then
		charaBuff.attrib = Attribute:create()
	end
	local attrib = charaBuff.attrib
	attrib:clear()
        --EnvironmentHandler.sendLogToServer("BuffEffectAttrib.recalcAllEffect:"..tostring(effects))
	if effects then
		local attribName = nil
		for _,eff in pairs(effects) do
			attribName = eff.effectInfo.attribName
        --EnvironmentHandler.sendLogToServer("attribName:"..attribName)
			if attribName then
				if attrib[attribName] == nil then
					attrib[attribName] = eff.attribValue
				else
					attrib[attribName] = attrib[attribName] + eff.attribValue
				end
			end
		end
	end
    --EnvironmentHandler.sendLogToServer("attrib.exp:"..tostring(attrib.exp))
	if attrib.movespeed then
		local oldFrameSpeed = charaBuff.target.framespeed or 0
		local newMoveSpeed = charaBuff.target.movespeed + attrib.movespeed
		charaBuff.target:setFrameSpeed(newMoveSpeed)
		if oldFrameSpeed ~= charaBuff.target.framespeed then
			ModuleEvent.dispatch(ModuleConstant.CHARAC_UPDATE_ATTR, {charaBuff.target.uniqueID, "movespeed", math.round(newMoveSpeed * 100,0)})
		end
    else
		local oldFrameSpeed = charaBuff.target.framespeed or 0
		charaBuff.target:setFrameSpeed(charaBuff.target.movespeed)
		if oldFrameSpeed ~= charaBuff.target.framespeed and charaBuff.target.movespeed then
			ModuleEvent.dispatch(ModuleConstant.CHARAC_UPDATE_ATTR, {charaBuff.target.uniqueID, "movespeed", math.round(charaBuff.target.movespeed * 100, 0)})
		end
	end

    if attrib.exp then
        if attrib.exp > 0 then
	        FightServerControl.setFrameExpAdd(charaBuff.target.uniqueID,attrib.exp)
        end
    end

	-- 修改最大血量上限
	if charaBuff.targetSourceMaxHp then
		if attrib.maxhp and attrib.maxhp > 0 then
			local newMaxHp = attrib.maxhp + charaBuff.targetSourceMaxHp
			if newMaxHp ~= charaBuff.target.maxhp then
				if EnvironmentHandler.isInServer then
					local percent = math.round(newMaxHp / math.max(1, charaBuff.targetSourceMaxHp), 2) * 100
					sendMaxHpMsgToClient(charaBuff.target.uniqueID, percent)
				end
				ModuleEvent.dispatch(ModuleConstant.CHARAC_UPDATE_ATTR, {charaBuff.target.uniqueID, "maxhp", newMaxHp})
			end
		else
			if charaBuff.targetSourceMaxHp ~= charaBuff.target.maxhp then
				ModuleEvent.dispatch(ModuleConstant.CHARAC_UPDATE_ATTR, {charaBuff.target.uniqueID, "maxhp", charaBuff.targetSourceMaxHp})
			end
		end
	end
end