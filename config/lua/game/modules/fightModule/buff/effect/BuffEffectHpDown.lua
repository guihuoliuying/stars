-- region
-- Date    : 2016-12-21
-- Author  : daiyaorong
-- Description :  血量下降 下降越多属性增幅越多
-- endregion

BuffEffectHpDown = {}
createClassWithExtends(BuffEffectHpDown, BuffEffectBase)

local m_floor = math.floor
local m_min = math.min

function BuffEffectHpDown:init(buff, effectInfo)
	BuffEffectBase.init(self, buff, effectInfo)
	self.attribValue = 0
end

function BuffEffectHpDown:onBuffStart()
	self.attribValue = 0
	self.sourceValue = self.refBuff.target[self.effectInfo.attribName] or 0
	self.preValue = 0 --保留旧数值以便做比较
	self.triggerLayer = 0
end

function BuffEffectHpDown:doEffect()
	local maxHp = self.refBuff.target.maxhp
	local downPercent = ((maxHp-self.refBuff.target.hp) / maxHp) * 1000
	self.triggerLayer = m_floor(downPercent / self.effectInfo.percent) --计算下降的层数
	self.triggerLayer = m_min(self.triggerLayer, self.effectInfo.limit) --限制最高层数
    self.attribValue = (self.sourceValue * (self.effectInfo.rate * 0.001) + self.effectInfo.value) * self.triggerLayer
	self.attribValue = self.attribValue * self.refBuff.curLayer
	if self.attribValue ~= self.preValue then --与之前不同时需调用接口使属性生效
    	self.refBuff.target.charaBuff:recalcAllBuffEffect()
	end
	self.preValue = self.attribValue
end

function BuffEffectHpDown:overlay()
	self:doEffect()
end

-- 计算某角色buff效果的属性加成
function BuffEffectHpDown.recalcAllEffect(charaBuff, effects)
	if charaBuff.attrib == nil then
		charaBuff.attrib = Attribute:create()
	end
	local attrib = charaBuff.attrib
	attrib:clear()
	if effects then
		local attribName = nil
		for _,eff in pairs(effects) do
			attribName = eff.effectInfo.attribName
			if attribName then
				if attrib[attribName] == nil then
					attrib[attribName] = eff.attribValue
				else
					attrib[attribName] = attrib[attribName] + eff.attribValue
				end
			end
		end
	end
end