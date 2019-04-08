-- region
-- Date    : 2016-07-28
-- Author  : daiyaorong
-- Description :  技能对象缓存池
-- endregion

SkillPool = {}
local MAX_SIZE = 10
local pool = nil
local poolLentList = nil

function SkillPool.init()
	-- body
	pool = pool or {}
	poolLentList = poolLentList or {}
end

-- 获取一个空闲对象
function SkillPool.getObject( sType )
	-- body
	if pool[sType] == nil then return end
	for k,v in ipairs( pool[sType] ) do
		if v.isUsed == false then
			v.isUsed = true
			return v.object
		end
	end
	return nil
end

-- 存放一个空闲对象
function SkillPool.poolObject( object )
	-- body
	local sType = object.sType
	if poolLentList[sType] == nil then
		poolLentList[sType] = 0
	end
	if pool[sType] == nil then
		pool[sType] = {}
	end

	for k,v in ipairs( pool[sType] ) do
		if v.object == object then
			v.isUsed = false
			return
		end
	end	

	if poolLentList[sType] >= MAX_SIZE then
		object = nil
		return
	end
	poolLentList[sType] = poolLentList[sType] + 1
	pool[sType][poolLentList[sType]] = { object=object, isUsed=false }
end

function SkillPool.dispose()
	-- body
	pool = nil
	poolLentList = nil
end


-- 子弹池
BulletPool = {}
local bulletPool = nil
local bulletPoolLen = nil

function BulletPool.init()
	-- body
	bulletPool = bulletPool or {}
	bulletPoolLen = bulletPoolLen or 0
end

function BulletPool.getObject()
	-- body
	for k,v in ipairs( bulletPool ) do
		if v.isUsed == false then
			v.isUsed = true
			return v.object
		end
	end
	return nil
end

function BulletPool.poolObject( object )
	-- body
	for k,v in ipairs( bulletPool ) do
		if v.object == object then
			v.isUsed = false
			return
		end
	end	

	if bulletPoolLen >= MAX_SIZE then
		object = nil
		return
	end
	bulletPoolLen = bulletPoolLen + 1
	bulletPool[bulletPoolLen] = { object=object, isUsed=false }
end

function BulletPool.dispose()
	-- body
	bulletPool = nil
	bulletPoolLen = nil
end