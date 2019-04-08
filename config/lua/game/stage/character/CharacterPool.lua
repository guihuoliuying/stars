-- region
-- Date    : 2016-07-01
-- Author  : daiyaorong
-- Description :  角色池
-- endregion

CharacterPool = {
	pool = nil,
	poolLentList = nil,
}

local MAX_SIZE = 10

function CharacterPool:init()
	-- body
	self.pool = {}
	self.poolLentList = {}
end

function CharacterPool:getObject( cType )
	-- body
	if self.pool[cType] == nil then return end

	for k,v in ipairs( self.pool[cType] ) do
		if v.isUsed == false then
			v.isUsed = true
			return v.object
		end
	end
	return nil
end

function CharacterPool:poolObject( object )
	-- body
	local cType = object.characterType
	if self.poolLentList[cType] == nil then
		self.poolLentList[cType] = 0
	end
	if self.pool[cType] == nil then
		self.pool[cType] = {}
	end

	for k,v in ipairs( self.pool[cType] ) do
		if v.object == object then
			v.isUsed = false
			return
		end
	end

	if self.poolLentList[cType] >= MAX_SIZE then
		object = nil
		return
	end
	self.poolLentList[cType] = self.poolLentList[cType] + 1
	self.pool[cType][self.poolLentList[cType]] = { object=object, isUsed=false }
end

function CharacterPool:dispose()
	-- body
	self.pool = nil
	self.poolLentList = nil
end