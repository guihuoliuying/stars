local cSharpFinder = PathFinder

PathFinder = { }

local navHandle = NavigationHandler:shaderdNavigation()
local isLoadMapSuccess
local targetList = { }
local currentFinder = nil
local tempItem = nil
local findQueqe = { }
local isRunning = false
local funcKey = 0

local minPos = -1000
local maxPos = 1000

local function getPathList()
	-- body
	local pathList = { }
	local len = navHandle:getPathLen()
	if (len <= 0) then return nil end
	for i = 1, len do
		local pos = navHandle:getPopPath()
		pathList[#pathList + 1] = Vector3.New(pos.x, pos.y, pos.z)

	end
	-- pathList.Length = len
	return pathList
end

local function findPathComplete(path)
	-- body
	if (currentFinder == nil) then
		return
	end
	-- if( currentFinder.waiting == false)then
	-- end
	local finPath = { }
	if (path == nil or #path == 0) then
		-- finPath[1] = currentFinder.pos
		finPath[1] = currentFinder.target:getPosition()
	else
		finPath = path
		-- for i=0,path.Length - 1 do
		-- 	finPath[#finPath + 1] = path[i]
		-- end
	end
	currentFinder.path = finPath
	currentFinder.callback(finPath)
	currentFinder = nil
end


local function dtDistancePtSegSqr2D(pt, p, q)
	local pqx = q.x - p.x;
	local pqz = q.y - p.y;
	local dx = pt.x - p.x;
	local dz = pt.y - p.y;
	local d = pqx * pqx + pqz * pqz;
	local t = pqx * dx + pqz * dz;
	if (d > 0) then t = t / d end
	if (t < 0) then
		t = 0
	elseif (t > 1) then
		t = 1
	end
	dx = p.x + t * pqx - pt.x;
	dz = p.y + t * pqz - pt.y;
	return dx * dx + dz * dz;
end


-- 校正路点，插入路点避免没贴地
local function formatPath(path)
	-- body
	local targetPath = { }
	local num
	local curPos
	local nextPos
	local vec
	local normal
	local insertPos
	for i = 1, #path - 1 do
		curPos = path[i]
		nextPos = path[i + 1]
		targetPath[#targetPath + 1] = path[i]
		vec = nextPos - curPos
		num = math.floor(vec.magnitude)
		for j = 1, num do
			normal = vec.normalized
			insertPos = navHandle:samplePosition(0, curPos + normal)
			if (dtDistancePtSegSqr2D(insertPos, curPos, nextPos) < 0.01) then
				-- break
			else
				targetPath[#targetPath + 1] = insertPos
				curPos = insertPos
				vec = nextPos - curPos
			end
		end
	end
	targetPath[#targetPath + 1] = path[#path]
	return targetPath
end

local function isCorValid(vpos,toPos)
  if vpos ~= nil and vpos.x > minPos and vpos.x < maxPos and vpos.y > minPos and vpos.y < maxPos and vpos.z > minPos and vpos.z < maxPos then
      if toPos ~= nil and toPos.x > minPos and toPos.x < maxPos and toPos.y > minPos and toPos.y < maxPos and toPos.z > minPos and toPos.z < maxPos then
        return true
      end
  end
  return false
end

local function doFindPath()
	-- body
	if (currentFinder == nil) then
		if (#findQueqe > 0) then
			currentFinder = targetList[findQueqe[1]]
			table.remove(findQueqe, 1)
			if (currentFinder == nil) then
				return
			end
			currentFinder.waiting = false
			local vpos = currentFinder.target:getPosition()
			local toPos = currentFinder.pos
			-- vpos.z = -vpos.z
			-- toPos.z = -toPos.z
			-- PathFinder.samplePosition(toPos)
			if isCorValid(vpos,toPos) then
	          navHandle:findShortPath(0, vpos,toPos)
	      	else
	        	LogManager.LogError("findShortPath was too big")
	      	end
			local path = getPathList()
			findPathComplete(path)
			-- 		callback( path)

			-- if( currentFinder ~= nil)then
			-- 	currentFinder.waiting = false
			-- 	cSharpFinder.luaAddPathFinder( currentFinder.target, currentFinder.pos, findPathComplete )
			-- else
			-- 	table.remove( findQueqe,1)
			-- end
		end
	end
end

local function endPathFind()
	-- body
	FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUASCENE):removeLuaFunc(funcKey)
	findQueqe = { }
	targetList = { }
	currentFinder = nil
	tempItem = nil
	isRunning = false
end

local function initPathFind()
	-- body
	isRunning = true
	funcKey = FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUASCENE):RegisterLuaCallBack(1, 0, doFindPath)

end

function PathFinder.luaAddPathFinder(character, toPos, callback)
	tempItem = targetList[character]
	if (tempItem == nil) then
		tempItem = { }
		targetList[character] = tempItem
		tempItem.target = character
	end
	tempItem.pos = toPos
	tempItem.callback = callback
	tempItem.path = nil
	if (not tempItem.waiting) then
		if (currentFinder == tempItem) then
			currentFinder = nil
		else
			findQueqe[#findQueqe + 1] = character
			tempItem.waiting = true
		end
	end

	if (isRunning == false) then
		initPathFind()
	end
end

function PathFinder.samplePosition(pos)
	if PathFinder.isPosValid(pos) then
		local samlePos = navHandle:samplePosition(0, pos)
		samlePos = Vector3.New(samlePos.x, samlePos.y, samlePos.z)
		return samlePos
	end
	return pos
end

function PathFinder.samplePositionWithRange(pos, range)
	local extend = Vector3.New(range, range, range)
	local samplePos = navHandle:samplePosition(0, pos, extend)
	return samplePos
end

function PathFinder.isBlock(pos)
	if PathFinder.isPosValid(pos) then
		return navHandle:isBlock(0, pos)
	end
	return true
end

function PathFinder.isPosValid(vpos)
  if vpos ~= nil and vpos.x > minPos and vpos.x < maxPos and vpos.y > minPos and vpos.y < maxPos and vpos.z > minPos and vpos.z < maxPos then
      return true
  end
  LogManager.LogError("isblock postion was too big:"..tableplus.tostring(vpos,true))
  return false
end

--直接判断坐标，不判断是否非法，其他人不要调用这个接口（调用前要先调用PathFinder.isPosValid(vpos)）
function PathFinder.isBlockValid(pos)
	return navHandle:isBlock(0, pos)
end

--直接调用，其他人不要调用这个接口（调用前要先调用PathFinder.isPosValid(vpos)）
function PathFinder.samplePositionValid(pos)
	local samlePos = navHandle:samplePosition(0, pos)
	samlePos = Vector3.New(samlePos.x, samlePos.y, samlePos.z)
	return samlePos
end

function PathFinder.removePathFinder(target)
	-- body
	if (targetList and target) then
		targetList[target] = nil
	end
end

-- 简单版本的范围点，只找左边和右边
function PathFinder.samplePosWithRangeII(pos, rotate, posNum, range)
	local tempPos = pos
	if PathFinder.isBlock(tempPos) == false then
		return tempPos
	end
	posNum = posNum or 20
	range = range or 0.1
	for i = 1, posNum do
		tempPos = pos + rotate * Vector3(0, 0, range * i)

		if PathFinder.isBlock(tempPos) == false then
			return tempPos
		end
		tempPos = pos + rotate * Vector3(0, 0, - range * i)
		if PathFinder.isBlock(tempPos) == false then
			return tempPos
		end
	end
	return PathFinder.samplePosition(pos)
end

function PathFinder.dispose()
	endPathFind()
	if (navHandle) then
		navHandle:removeAllNav()
		navHandle:dispose()
		navHandle = nil
	end
end

function PathFinder.clearCache()
	endPathFind()
end

function PathFinder.addData(path, mapId)
	navHandle:removeAllNav()

	local function zipOverCallBack()
		isLoadMapSuccess = navHandle:loadNavMesh(path)
		if isLoadMapSuccess == false then
			navHandle:loadNavMesh(path)
		end
	end

	if LuaHelper.isFileExists(path) == false then
		LogManager.LogError("地图数据文件不存在=" .. path)
		if GameUtil.isAndroidPlatform then
			GameUtil.zipNavmesh(mapId, false, zipOverCallBack)
		end
	else
		isLoadMapSuccess = navHandle:loadNavMesh(path)
		if isLoadMapSuccess == false then
			LogManager.Log("地图数据文件加载失败，重试=" .. path)
			navHandle:loadNavMesh(path)
		end
	end
end
