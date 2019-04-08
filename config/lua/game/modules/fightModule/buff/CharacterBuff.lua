--region CharacterBuff.lua
--Date 2016/07/13
--Author zhouxiaogang
-- 单个角色的BUFF管理
--endregion

CharacterBuff = 
{
	buffList = nil,			-- 玩家的buff列表(数组)
	effectDic = nil,		-- 玩家buff的所有效果列表(效果类型 -> 效果脚本)
	target = nil,			-- 所属对象
	isDisplayEffect = nil,	-- 是否显示特效
	immuneBuffTypes = nil,	-- 当前免疫的BUFF类型（无法添加上）
	extraPercentBuffDuration = nil,	-- 固定增加buff的持续时间，秒
	extraFixedBuffDuration = nil,	-- 百分比增加buff的持续时间
	attrib = nil,			-- buff带来的额外属性加成
	controls = nil,			-- buff控制角色行为类型
	addDamageRate = nil,	-- buff带来的伤害加成百分比
	addDamageValue = nil,	-- buff带来的固定伤害值
	intensifyDamageRate = nil,	-- buff带来的固定伤害加深百分比
	intensifyDamageValue = nil,	-- buff带来的固定伤害加深值
	buffGetHpRate = nil ,      --buff吸血千分比
	buffGetHpValue = nil ,     --buff吸血固定值
	damageNumberType =nil,      --表示吸血飘字类型
	buffGetDemageHpRate = nil , --buff伤害减免之后吸血千分比
	damageNumberType2 =nil,     --免伤吸血后的飘字类型
	invincible = nil,	-- 无敌标记
	superArmor = nil,	-- buff带来的霸体等级
	targetSourceMaxHp = nil,	-- 用于记录角色最大生命上限
}

createClass(CharacterBuff)

function CharacterBuff:init(charUniqueId)
	self.target = CharacterManager:getCharacByUId(charUniqueId)
	self.buffList = {}
	self.effectDic = {}
end

-- 添加一个buff到角色身上
function CharacterBuff:addBuff(buffData, attackerUID,buffInstanceId)
	if buffData == nil or attackerUID == nil or type(buffData) ~= "table" then
		return
	end
	-- 当前免疫该类型的BUFF
	if self:isImmuneBuffType(buffData.buffType) ~= false then
		-- if BuffManager.isDebug then
		-- 	CmdLog("BUFF被免疫（清除）[target=" .. tostring(self.target.uniqueID) .. ",attacker=" .. tostring(attackerUID) .. ",buffid=" .. tostring(buffData.buffId) .. ",bufflv=" .. tostring(buffData.bufflv) .. "]")
		-- end
		return
	end
	-- if BuffManager.isDebug then
	-- 	CmdLog("添加BUFF[target=" .. tostring(self.target.uniqueID) .. ",attacker=" .. tostring(attackerUID) .. ",buffid=" .. tostring(buffData.buffId) .. ",bufflv=" .. tostring(buffData.bufflv) .. "]")
	-- end
	local sameBuff = self:tryGetSameBuff(buffData, attackerUID)
	if sameBuff then
		sameBuff:overlayBuff(buffData)
	else
        sameBuff = self:tryGetSameIdBuff(buffData)
		local buff = BuffBase:create()
		buff.instanceId = buffInstanceId
		local attacker = CharacterManager:getCharacByUId(attackerUID)
		buff:init(buffData, attacker, self.target)
		for _,eff in pairs(buff.effects) do
			if self.effectDic[eff.effectType] == nil then
				self.effectDic[eff.effectType] = {}
			end
			table.insert(self.effectDic[eff.effectType], eff)
		end
		table.insert(self.buffList, buff)
        if sameBuff then
            if buffData.buffLv >= sameBuff.buffData.buffLv then
				buff:startBuff(self.isDisplayEffect)
                self:removeBuff(sameBuff.instanceId)
            else
                --增加buffdata但不生效
            end
        else
            buff:startBuff(self.isDisplayEffect)
        end
	end
	self:recalcAllBuffEffect()
end

-- 给某个buff添加一个效果
function CharacterBuff:addEffectToBuff(buffInstanceId, effectInfo)
	local buffIndex = self:indexOfBuff(buffInstanceId)
	if buffIndex <= 0 then return end
	local buff = self.buffList[buffIndex]
	local eff = buff:addEffect(effectInfo)
	if eff then
		if self.effectDic[eff.effectType] == nil then
			self.effectDic[eff.effectType] = {}
		end
		table.insert(self.effectDic[eff.effectType], eff)
	end
	self:recalcAllBuffEffect()
end

-- 从角色身上移除一个buff
-- buffInstanceId 对象ID 
function CharacterBuff:removeBuff(buffInstanceId)
	if buffInstanceId == nil then return end
	local buffIndex = self:indexOfBuff(buffInstanceId)
	if buffIndex == 0 then
--		CmdLog("尝试删除一个不存在的BUFF:" .. tostring(self.target.uniqueID) .. "|" .. tostring(buffInstanceId))
		return
	end
	local buff = table.remove(self.buffList, buffIndex)
	if BuffManager.isDebug then
		CmdLog("移除BUFF[target=" .. tostring(self.target.uniqueID) .. ",attacker=" .. tostring(buff.attackerUID) .. ",buffid=" .. tostring(buff.buffData.buffId) .. ",bufflv=" .. tostring(buff.buffData.bufflv) .. "]")
	end
	local tempTb = nil
	for _,eff in pairs(buff.effects) do
		tempTb = self.effectDic[eff.effectType]
		if tempTb then
			for i = #tempTb, 1, -1 do 
				if tempTb[i] == eff then table.remove(tempTb, i) end
			end
		end
	end
	local restBuff = self:tryGetSameIdTopLvBuff(buff.buffData)
	if restBuff and self:isImmuneBuffType(restBuff.buffData.buffType) == false then
		restBuff:startBuff(self.isDisplayEffect)
	end
	-- CmdLog("销毁BUFF：owner:" .. tostring(self.target.uniqueID) .. "|buffId:" .. tostring(buff.buffData.buffId) .. "|" .. debug.traceback())
	buff:dispose()
	self:recalcAllBuffEffect()
end

-- 按buff类型取buff列表
-- buffType buff类型：FightDefine.BUFF_TYPE
-- return buff数组
function CharacterBuff:getBuffByType(buffType)
	if self.buffList == nil then return nil end
	local result = {}
	for _,buff in ipairs(self.buffList) do
		if buff.buffData.buffType == buffType then
			table.insert(result, buff)
		end
	end
	return result
end

function CharacterBuff:hasBuffType(buffType)
    local list = self:getBuffByType(buffType)
    if list and #list > 0 then
        return true
    else
        return false
    end
end

-- 按buff效果类型取效果列表
-- effectType 效果类型 FightDefine.BUFF_EFFECT_TYPE
-- return buff效果列表或空
function CharacterBuff:getBuffEffectByType(effectType)
	if self.effectDic == nil then return nil end
	return self.effectDic[effectType]
end

-- 是否存在某种类型的buff效果
function CharacterBuff:hasBuffEffect(effectType)
	local list = self:getBuffEffectByType(effectType)
	if list and #list > 0 then
		return true
	else
		return false
	end
end

-- 尝试获取一个相同的buff
-- buffData : BuffVo
-- attackerUID : 施加者的uniqueID
function CharacterBuff:tryGetSameBuff(buffData, attackerUID)
	local result = nil
	for _,buff in pairs(self.buffList) do
		if buff:isSameBuff(buffData, attackerUID) then
			result = buff;break;
		end
	end
	return result
end

-- 尝试获取一个相同id的buff
function CharacterBuff:tryGetSameIdBuff(buffData)
    local result = nil
    for _, buff in pairs(self.buffList) do
        if buff:isSameId(buffData) then
            result = buff;break;
        end
    end
    return result
end

-- 获取相同id等级最高的buff
function CharacterBuff:tryGetSameIdTopLvBuff(buffData)
	local result = nil
	for _, buff in pairs(self.buffList) do
		if buff:isSameId(buffData) then
			if result == nil or result.buffData.buffLv < buff.buffData.buffLv then
				result = buff
			end
		end
	end
	return result
end

-- 取buff的索引
-- buffInstanceId : Buff对象的唯一ID
function CharacterBuff:indexOfBuff(buffInstanceId)
	if self.buffList == nil then return 0 end
	for k,buff in ipairs(self.buffList) do
		if buff.instanceId == buffInstanceId then
			return k
		end
	end
	return 0
end

-- 是否存在指定产品表buffId的buff
-- buffId buff表中的buffid字段(BuffVo中为buffId字段)
function CharacterBuff:hasBuff(buffId)
	if self.buffList == nil then return false end
	for k,buff in ipairs(self.buffList) do
		if buff.buffData.buffId == buffId then
			return true
		end
	end
	return false
end

-- 更新角色身上的所有buff
function CharacterBuff:update(deltaTime)
	for _,buff in pairs(self.buffList) do
		-- 循环过程中，列表内的buff效果可能导致角色死亡
		if self.target == nil then break end
		buff:update(deltaTime)
	end
end

-- 清掉角色身上所有buff
function CharacterBuff:removeAll()
	self.effectDic = {}
	for _,buff in ipairs(self.buffList) do
		buff:dispose()
	end
	self.buffList = {}
	for _,effectType in pairs(FightDefine.BUFF_EFFECT_TYPE) do
		BuffEffectFactory.EFFECT_CLS[effectType].recalcAllEffect(self, nil)
	end
end

-- 清除某一效果类型的buff
function CharacterBuff:removeByEffectType(effectType)
    if self.buffList == nil then return end
    local buffObj = nil
    for index = #self.buffList, 1, -1 do
        buffObj = self.buffList[index]
        for j = #buffObj.effects, 1, -1 do
            if buffObj.effects[j].effectType == effectType then
                buffObj:removeEffect(j)
            end
        end
        if #buffObj.effects == 0 then
            self:removeBuff( buffObj.instanceId )
        end
    end
    self.effectDic[effectType] = {}
end

-- 重算角色身上的buff效果(一般发生在往角色身上添加或删除buff时)
function CharacterBuff:recalcAllBuffEffect()
    if self.effectDic == nil then return end
	local isPVPClient = (EnvironmentHandler.isPvpClient)
	local effCls = nil
	for effectType, effects in pairs(self.effectDic) do
		effCls = BuffEffectFactory.EFFECT_CLS[effectType]
		if (not isPVPClient) or effCls.isForceRecalcAllEffect then
			effCls.recalcAllEffect(self, effects)
		end
	end
end

function CharacterBuff:enableBuffDisplayEffect()
	self.isDisplayEffect = true
	for _,buff in ipairs(self.buffList) do
		buff:playBuffEffect()
	end
end

function CharacterBuff:disableBuffDisplayEffect()
	self.isDisplayEffect = false
	for _,buff in ipairs(self.buffList) do
		buff:stopBuffEffect()
	end
end

-- 指定buff类型的buff是否被免疫
function CharacterBuff:isImmuneBuffType(buffType)
	if self.immuneBuffTypes == nil or buffType == nil or type(self.immuneBuffTypes) ~= "table" or self.immuneBuffTypes[buffType] == nil then
		return false
	end
	return self.immuneBuffTypes[buffType]
end

function CharacterBuff:dispose()
    
	self:removeAll()
	-- 将速度重置回来
	if self.target then
		self.target:setFrameSpeed(self.target.movespeed)
		self.target.isTaunt = nil
	end
	self.buffList = nil
	self.effectDic = nil
	self.target = nil
	self.immuneBuffTypes = nil
	self.attrib = nil
	self.controls = nil
	self.targetSourceMaxHp = nil
end


