--region RuneObject.lua
--Date 2017/01/04
--Author zhouxiaogang
--Desc 场景上的神符对象
--endregion

RuneObject = {}
createClass(RuneObject)

function RuneObject:init(runeData, instanceId, pos)
	self.runeData = runeData
	self.position = pos
	self.instanceId = instanceId
	self.sqrRadius = self.runeData.pickuprage * 0.1
	self.sqrRadius = self.sqrRadius * self.sqrRadius

	if EnvironmentHandler.isInServer == false then
		if self.runeData.runeeffect and self.runeData.runeeffect ~= "0" then
			self.thing = StageManager.getThingPool():getThingCallback( self.runeData.runeeffect, UrlManager.SKILL, function(thing)
				if thing and thing.isAvatarValid then
					self.thing = thing
					self:loadCompleted()
				end 
			end)
		end
	end
end

function RuneObject:loadCompleted()
	if self.thing == nil or self.thing.isAvatarValid == false then
		return
	end
	self.thing:setActive(true)
	self.thing:setLocation(self.position.x, self.position.y, self.position.z)
	self.thing.avatarName = "runeobject_" .. tostring(self.runeData.runeid)
end

-- 检查玩家是否进入拾取范围
-- return 检查到有玩家进入时，返回进入者的uid，否则返回nil
function RuneObject:checkPlayerIsInArea()
	local players = CharacterManager:getCharacByType(CharacterConstant.TYPE_PLAYER)
	if players then
		for k,player in pairs(players) do
			if CharacterUtil.sqrDistance(player:getPosition(), self.position) <= self.sqrRadius then
				return player.uniqueID
			end
		end
	end
	
	local mainCharac = CharacterManager:getMyCharac()
	if mainCharac then
		if CharacterUtil.sqrDistance(mainCharac:getPosition(), self.position) <= self.sqrRadius then
			return mainCharac.roleId
		end
	end
	return nil
end

function RuneObject:dispose()
	self.runeData = nil
	self.spawnInfo = nil
	if self.thing then
		StageManager.getThingPool():poolThing(self.thing)
		self.thing = nil
	end
end

