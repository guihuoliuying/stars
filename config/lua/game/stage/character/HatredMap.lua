-- region
-- Date    : 2016-07-06
-- Author  : daiyaorong
-- Description :  角色仇恨类
-- endregion

HatredMap = {
	record = nil,
}
HatredMap = createClass(HatredMap)

function HatredMap:init()
	-- body
end

-- 增加对特定角色的仇恨值
function HatredMap:addHatred( characterID, value )
	-- body
	if self.record == nil then
		self.record = {}
	end
	if self.record[characterID] == nil then
		self.record[characterID] = 0
	end
	self.record[characterID] = self.record[characterID] + value
end

-- 获取仇恨值最高的角色ID列表
function HatredMap:getTopHatred()
	-- body
	if self.record == nil then return nil end

	local uidList = nil
	local topValue = nil
	for k,v in pairs( self.record ) do
		if topValue == nil then
			uidList = {}
			table.insert( uidList, k )
			topValue = v
		else
			if v > topValue then
				uidList = {}
				table.insert( uidList, k )
				topValue = v
			elseif v == topValue then
				table.insert( uidList, k )
			end
		end
	end
	return uidList
end

--获取单个目标仇恨值
function HatredMap:getHatredbyUid(uid)
    if not self.record then return 0 end
    return self.record[uid] 
end

-- 清除所有仇恨值
function HatredMap:clearAllHatred()
	-- body
	self.record = nil
end

function HatredMap:dispose()
	-- body
	self.record = nil
end