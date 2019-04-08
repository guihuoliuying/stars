local floor = math.floor
local abs = math.abs
local sqrt = math.sqrt
local atan = math.atan
local ceil = math.ceil

local digits = {1,   10,  100,  1000,  10000,  100000,  1000000,  10000000,  100000000,  1000000000}
local fraction = {1, 0.1, 0.01, 0.001, 0.0001, 0.00001, 0.000001, 0.0000001, 0.00000001, 0.000000001}

local LEFT_TOP 		= 1
local RIGHT_TOP 	= 2
local RIGHT_BOTTOM 	= 3
local LEFT_BOTTOM 	= 4

-- 取小数点后几位数(四舍五入)
-- * 最多支持到小数点后9位
-- bit 表示要精确到的位数，0或nil时表示只保留整数部分
function math.round(num, bit)
	if num == nil then
		return num
	end
	bit = (bit or 0) + 1
	num = floor((num * digits[bit]) + 0.5) * fraction[bit]
	return num
end

function math.roundVector(pos, bit)
	pos.x = math.round(pos.x, bit)
	pos.y = math.round(pos.y, bit)
	pos.z = math.round(pos.z, bit)
	return pos
end

function math.sign(num)  
	if num > 0 then
		num = 1
	elseif num < 0 then
		num = -1
	else 
		num = 0
	end

	return num
end


--米单位坐标转换为分米
function math.mPos2DmPos( pos )
	local retPos = Vector3.New(pos.x, pos.y, pos.z)
	retPos.x = retPos.x * 10
	retPos.y = retPos.y * 10
	retPos.z = retPos.z * 10
	return retPos
end

--分米单位坐标转换为米
function math.dmPos2mPos( pos )
	local retPos = Vector3.New(pos.x, pos.y, pos.z)
	retPos.x = retPos.x * 0.1
	retPos.y = retPos.y * 0.1
	retPos.z = retPos.z * 0.1
	return retPos
end


function math.clamp(num, min, max)
	if num < min then
		num = min
	elseif num > max then
		num = max    
	end
	
	return num
end

local clamp = math.clamp

function math.lerp(from, to, t)
	return from + (to - from) * clamp(t, 0, 1)
end

function math.Repeat(t, length)    
	return t - (floor(t / length) * length)
end        

function math.LerpAngle(a, b, t)
	local num = math.Repeat(b - a, 360)

	if num > 180 then
		num = num - 360
	end

	return a + num * clamp(t, 0, 1)
end

function math.MoveTowards(current, target, maxDelta)
	if abs(target - current) <= maxDelta then
		return target
	end

	return current + mathf.sign(target - current) * maxDelta
end

function math.DeltaAngle(current, target)    
	local num = math.Repeat(target - current, 360)

	if num > 180 then
		num = num - 360
	end

	return num
end    

function math.MoveTowardsAngle(current, target, maxDelta)
	target = current + math.DeltaAngle(current, target)
	return math.MoveTowards(current, target, maxDelta)
end

function math.Approximately(a, b)
	return abs(b - a) < math.max(1e-6 * math.max(abs(a), abs(b)), 1.121039e-44)
end

function math.InverseLerp(from, to, value)
	if from < to then      
		if value < from then 
			return 0
		end

		if value > to then      
			return 1
		end

		value = value - from
		value = value/(to - from)
		return value
	end

	if from <= to then
		return 0
	end

	if value < to then
		return 1
	end

	if value > from then
        return 0
	end

	return 1.0 - ((value - to) / (from - to))
end

function math.PingPong(t, length)
    t = math.Repeat(t, length * 2)
    return length - abs(t - length)
end
 
math.deg2Rad = math.pi / 180
math.rad2Deg = 180 / math.pi
math.epsilon = 1.401298e-45


function math.Random(n, m)
	m = m or 0
	local range = m - n
	return math.random() * range + n
end

math.randomseed( os.time() )
local r = 29.2
local floor = math.floor
local function Rand(  )
    local base, u, v, p, temp1, temp2, temp3
    base = 256.1
    u = 17.4
    v = 139.7
    temp1 = u * r + v
    temp2 = floor(temp1 / base)
    temp3 = temp1 - temp2 * base
    r = temp3
    p = r / base

    return p
end
-- 战斗逻辑中专用的随机数方法，用来保证双端验证的，请不要在UI或其他非逻辑中使用
function math.LogicRandom( n, m )
	m = m or 0
	local range = m - n
	return Rand() * range + n
end

function math.SetRandomSeed( seed )
	r = seed
end

function math.isNumberEqual( num1, num2 )
	-- body
	if not num1 or not num2 then return false end 
	return abs(num1-num2) <= CharacterConstant.RUN_PRECISION
end


-- isnan
function math.isnan(number)
	return not (number == number)
end

local PI = math.pi
local TWO_PI = 2 * math.pi
local HALF_PI = math.pi / 2 


function math.sin16(a) 
	local s

	if a < 0 or a >= TWO_PI then
		a = a - floor( a / TWO_PI ) * TWO_PI
	end

	if a < PI then
		if a > HALF_PI then
			a = PI - a
		end
	else 
		if a > PI + HALF_PI then
			a = a - TWO_PI
		else
			a = PI - a
		end
	end

	s = a * a
	return a * ( ( ( ( (-2.39e-8 * s + 2.7526e-6) * s - 1.98409e-4) * s + 8.3333315e-3) * s - 1.666666664e-1 ) * s + 1)
end


function math.atan16(a) 
	local s

	if abs( a ) > 1 then
		a = 1 / a
		s = a * a
		s = - ( ( ( ( ( ( ( ( ( 0.0028662257 * s - 0.0161657367 ) * s + 0.0429096138 ) * s - 0.0752896400 )
				* s + 0.1065626393 ) * s - 0.1420889944 ) * s + 0.1999355085 ) * s - 0.3333314528 ) * s ) + 1.0 ) * a
		if FLOATSIGNBITSET( a ) then
			return s - HALF_PI
		else 
			return s + HALF_PI
		end
	else 
		s = a * a
		return ( ( ( ( ( ( ( ( ( 0.0028662257 * s - 0.0161657367 ) * s + 0.0429096138 ) * s - 0.0752896400 )
			* s + 0.1065626393 ) * s - 0.1420889944 ) * s + 0.1999355085 ) * s - 0.3333314528 ) * s ) + 1 ) * a
	end
end

--- <summary>
--- 从传入的table中，随机抽取其中一个元素，table必须有序，用数字索引。
--- </summary>
--- <param name="list"></param>
function math.randomElement(list)
    if list == nil or #list == 0 then return nil end
    return list[math.random(1, #list)]
end

--- 从传入的table中，随机抽取其中一个元素，table可以是无序的。
function math.randomUnit(list)
    if list == nil or next(list) == nil then return nil end
    local keyTable = {}
    for k,v in pairs (list) do
        keyTable[#keyTable+1] = k
    end
    local randomKey = keyTable[math.random(1, #keyTable)]
    return list[randomKey],randomKey
end

--标准化角度 返回一个0~360的值
local function standardAngle( angle )
	local ret = angle
	if ret < 0 or ret > 360 then
		ret = ret % 360
		if ret < 0 then
			ret = ret + 360
		end
	end
	return ceil(ret)
end
math.standardAngle = standardAngle

-- 获取矩形所有顶点，相对于矩形中心点坐标
-- 矩形宽rectWidth, 矩形高rectHeigh, 矩形相对于x轴偏转角度rectAngle,单位角度
local pointMapList = nil
local offsetAngleList = nil
local function getRectVertexList( rectWidth, rectHeigh, rectAngle )
	--计算记录顶点,预先分配4个元素，申请内存，效率更快
	if pointMapList == nil then
		pointMapList = {
			[LEFT_TOP] 		= Vector2.zero, 
			[RIGHT_TOP] 		= Vector2.zero, 
			[RIGHT_BOTTOM] 	= Vector2.zero, 
			[LEFT_BOTTOM] 	= Vector2.zero
		}
	end
	if offsetAngleList == nil then
		offsetAngleList = {
			[LEFT_TOP] 		= 0,
			[RIGHT_TOP] 		= 0, 
			[RIGHT_BOTTOM] 	= 0,
			[LEFT_BOTTOM] 	= 0,
		}
	end
	local halfRectWidth = rectWidth * 0.5
	local halfRectHeight = rectHeigh * 0.5
	if rectAngle == 0 then
		pointMapList[LEFT_TOP]:Set(-halfRectWidth, halfRectHeight)
		pointMapList[RIGHT_TOP]:Set(halfRectWidth, halfRectHeight)
		pointMapList[RIGHT_BOTTOM]:Set(halfRectWidth, -halfRectHeight)
		pointMapList[LEFT_BOTTOM]:Set(-halfRectWidth, -halfRectHeight)
		return pointMapList
	end
	
	local angle = math.rad2Deg * atan(rectHeigh / rectWidth)
	angle = ceil(angle)

	--矩形半径
	local rectR = sqrt(halfRectWidth * halfRectWidth + halfRectHeight * halfRectHeight)

	-- 算出每个顶点相关偏移角度，从左上角开始，顺时针顺序
	-- leftTopOffsetAngle ,rightTopOffsetAngle ,rightBottomOffsetAngle,leftBottomOffsetAngle
	offsetAngleList[LEFT_TOP] 		= 180 - angle - rectAngle
	offsetAngleList[RIGHT_TOP] 		= angle - rectAngle
	offsetAngleList[RIGHT_BOTTOM] 	= - angle - rectAngle
	offsetAngleList[LEFT_BOTTOM] 		= 180 + angle - rectAngle
	-- print("内部各点==="..tableplus.formatstring(offsetAngleList,true))
	local v = nil
	for i=1,4 do
		v = standardAngle(offsetAngleList[i])
		if v > 360 or v < 0 then
			v = 0
		end
		v = floor(v)
		pointMapList[i]:Set(rectR * cosTable[v], rectR * sinTable[v])
	end
	return pointMapList
end

-- 计算点p(x, y)到经过两点p1(x1, y1)和p2(x2, y2)的直线的距离
local function distanceFromPointToLine(p, p1, p2)
    local a = p2.y - p1.y;
    local b = p1.x - p2.x;
    local c = p2.x * p1.y - p1.x * p2.y;

    return abs(a * p.x + b * p.y + c) / sqrt(a * a + b * b)
end

--  判断点P(x, y)与有向直线P1P2的关系. 小于0表示点在直线左侧，等于0表示点在直线上，大于0表示点在直线右侧
local function evaluatePointToLine(p, p1, p2)
    local a = p2.y - p1.y;
    local b = p1.x - p2.x;
    local c = p2.x * p1.y - p1.x * p2.y;

    return (a * p.x + b * p.y + c);
end

local function determinant(v1, v2, v3, v4)
    return (v1 * v4 - v2 * v3);
end

--线段是否相交
local function lineIntersect(aa, bb, cc, dd)
    local delta = determinant(bb.x - aa.x, cc.x - dd.x, bb.y - aa.y, cc.y - dd.y);
    if (delta <= (1e-6) and delta >= -(1e-6)) then
        return false;
    end

    local namenda = determinant(cc.x - aa.x, cc.x - dd.x, cc.y - aa.y, cc.y - dd.y) / delta;
    if (namenda > 1 or namenda < 0) then
        return false;
    end

    local miu = determinant(bb.x - aa.x, cc.x - aa.x, bb.y - aa.y, cc.y - aa.y) / delta;
    if (miu > 1 or miu < 0) then
        return false;
    end

    return true;
end

-- 矩形线段是否相交
local function judgeRectIntersectLineSeg(aa, bb, cc, dd, lineP1, lineP2)
    return lineIntersect(aa, bb, lineP1, lineP2) or lineIntersect(bb, cc, lineP1, lineP2) or lineIntersect(cc, dd, lineP1, lineP2) or lineIntersect(aa, dd, lineP1, lineP2);
end


-- 圆与矩形碰撞检测，具体算法原理请看客户单文档svn中《碰撞检测.rar》文件
-- 圆心circleCenter, 半径r, 矩形中心rectCenter, 矩形宽rectWidth, 矩形高rectHeigh, 矩形相对于x轴偏转角度rectAngle,单位角度 vertexList矩形四个顶点列表，相对于矩形中心点，可不传
local rectRightCenter = nil
local rectTopCenter = nil
local vertexList = nil
function math.judgeCircleIntersectRectangle( circleCenter, r, rectCenter, rectWidth, rectHeigh, rectAngle )
	-- --print("矩形中心====="..tableplus.tostring(rectCenter,true))
	vertexList = getRectVertexList( rectWidth, rectHeigh, rectAngle )
	rectRightCenter = rectRightCenter or Vector2.zero
	rectTopCenter = rectTopCenter or Vector2.zero

	rectRightCenter.x = (vertexList[RIGHT_TOP].x + vertexList[RIGHT_BOTTOM].x) * 0.5 + rectCenter.x
	rectRightCenter.y = (vertexList[RIGHT_TOP].y + vertexList[RIGHT_BOTTOM].y) * 0.5 + rectCenter.y
	-- --print("右中心===="..tableplus.tostring(rectRightCenter,true))

	rectTopCenter.x = (vertexList[RIGHT_TOP].x + vertexList[LEFT_TOP].x) * 0.5 + rectCenter.x
	rectTopCenter.y = (vertexList[RIGHT_TOP].y + vertexList[LEFT_TOP].y) * 0.5 + rectCenter.y
	-- --print("上中心===="..tableplus.tostring(rectTopCenter,true))

    local w1 = rectWidth * 0.5
    local h1 = rectHeigh * 0.5

    local w2 = distanceFromPointToLine(circleCenter, rectCenter, rectTopCenter);
    local h2 = distanceFromPointToLine(circleCenter, rectCenter, rectRightCenter);

    if (w2 >= w1 + r) then
        return false
    end

    if (h2 >= h1 + r) then
        return false
    end

    if (w2 < w1) then
        return true
    end

    if (h2 < h1) then
        return true
    end
    local ret = ((w2 - w1) * (w2 - w1) + (h2 - h1) * (h2 - h1)) <= r * r;
    
    return ret
end

-- 判断矩形与扇形是否相交， 具体算法原理请看客户单文档svn中《碰撞检测.rar》文件
-- 矩形中心(rectCenter), 矩形宽rectWidth, 矩形高rectHeigh, 矩形相对于x轴偏转角度rectAngle, 
-- 扇形圆心sectorCenter,扇形半径sectorR， 扇形正前方最远点p2(x2, y2), 扇形夹角sectorAngle（0~180）, 扇形相对于x轴偏转角度sectorAngleOffset（0~360）
-- function math.judgeRectIntersectFan(rectCenter, rectWidth, rectHeigh, rectAngle, sectorCenter, sectorR, sectorAngle, sectorAngleOffset)
-- 	local vertexList = getRectVertexList( rectWidth, rectHeigh, rectAngle )

--     -- 矩形四个顶点
--     local ltVertex = vertexList[LEFT_TOP]
--     local rtVertex = vertexList[RIGHT_TOP]
--     local rbVertex = vertexList[RIGHT_BOTTOM]
--     local lbVertex = vertexList[LEFT_BOTTOM]

-- 	if sectorAngle <= 0 or sectorAngle >= 360 then
-- 		-- CmdLog("子弹判定 扇形碰撞检测配置角度异常!!")
-- 		return false
-- 	end

--     --  矩形不与扇形圆相交，则矩形与扇形必不相交
--     if (math.judgeCircleIntersectRectangle(sectorCenter, sectorR, rectCenter, rectWidth, rectHeigh, rectAngle, vertexList) == false) then
--     	-- print("矩形与扇形圆不相交");
--         return false;
-- 	end

-- 	--转换为全局坐标
--     ltVertex:Add(rectCenter)
--     rtVertex:Add(rectCenter)
--     rbVertex:Add(rectCenter)
--     lbVertex:Add(rectCenter)

--     -- 扇形圆心在矩形内，判断点和直线关系（左边，右边，直线上）
--     local e1 = evaluatePointToLine(sectorCenter, ltVertex, rtVertex);
--     local e2 = evaluatePointToLine(sectorCenter, rtVertex, rbVertex);
--     local e3 = evaluatePointToLine(sectorCenter, rbVertex, lbVertex);
--     local e4 = evaluatePointToLine(sectorCenter, lbVertex, ltVertex);
--     if (e1 >= 0 and e2 >= 0 and e3 >= 0 and e4 >= 0) then
--         -- print("矩形内");
--         return true;
--     end

--     --扇形最上和最下面的点(向上取整)
--     local halfAngle = ceil(sectorAngle * 0.5)
--     sectorAngleOffset = ceil(sectorAngleOffset)

--     local offsetAngle = standardAngle(sectorAngleOffset + halfAngle)
--     local sectorTop = Vector2.New(sectorR * cosTable[offsetAngle], sectorR * sinTable[offsetAngle]);
--     sectorTop:Add(sectorCenter) --转换为全局坐标

--     offsetAngle = standardAngle(sectorAngleOffset - halfAngle)
--     local sectorBottom = Vector2.New(sectorR * cosTable[offsetAngle], sectorR * sinTable[offsetAngle]);
--     sectorBottom:Add(sectorCenter) --转换为全局坐标

--     --  如果矩形中心在扇形两边夹角内，则必相交
--     local d1 = evaluatePointToLine(rectCenter, sectorCenter, sectorTop)
--     local d2 = evaluatePointToLine(rectCenter, sectorBottom, sectorCenter)
--     if sectorAngle > 180 and (d1 >= 0 or d2 >= 0) then
--         -- print("夹角内111");
--         return true;
--     end

--     if sectorAngle <= 180 and (d1 >= 0 and d2 >= 0) then
--         -- print("夹角内222");
--         return true;
--     end
--     --print("夹角外");

--     --  如果矩形与任一边相交，则必相交
--     if (judgeRectIntersectLineSeg(ltVertex, rtVertex, rbVertex, lbVertex, sectorCenter, sectorTop)) then
--         -- print("边相交111");
--         return true;
--     end
--     if (judgeRectIntersectLineSeg(ltVertex, rtVertex, rbVertex, lbVertex, sectorCenter, sectorBottom)) then
--         -- print("边相交222");
--         return true;
--     end
--     return false;
-- end

--判断矩形是否相交
-- function math.judgeRectIntersect(rectCenterL, rectWidthL, rectHeighL, rectAngleL, rectCenterR, rectWidthR, rectHeighR, rectAngleR)
-- 	local pointMapListL = getRectVertexList( rectWidthL, rectHeighL, rectAngleL )
-- 	local pointMapListR = getRectVertexList( rectWidthR, rectHeighR, rectAngleR )

-- 	for k,v in pairs(pointMapListL) do
-- 		v:Add(rectCenterL)
-- 	end
-- 	for k,v in pairs(pointMapListR) do
-- 		v:Add(rectCenterR)
-- 	end

-- 	--没有偏转角度，用简单算法
-- 	if rectAngleL == 0 and rectAngleR == 0 then
-- 		return ( pointMapListL[LEFT_BOTTOM].x < pointMapListR[RIGHT_TOP].x and 
-- 				pointMapListL[LEFT_BOTTOM].y < pointMapListR[RIGHT_TOP].y) and 
-- 			( pointMapListL[RIGHT_TOP].x > pointMapListR[LEFT_BOTTOM].x and 
-- 				pointMapListL[RIGHT_TOP].y > pointMapListR[LEFT_BOTTOM].y )
-- 	end
	
-- 	--圆心距离
-- 	local rDis = Vector2.Distance(rectCenterL, rectCenterR)

-- 	-- 判断矩形外接圆是否相交外接圆半径
-- 	local rectLR = sqrt(rectWidthL * rectWidthL * 0.5 * 0.5 + rectHeighL * rectHeighL * 0.5 * 0.5)
-- 	local rectRR = sqrt(rectWidthR * rectWidthR * 0.5 * 0.5 + rectHeighR * rectHeighR * 0.5 * 0.5)
-- 	if rectLR + rectRR < rDis then
-- 		return false
-- 	end

-- 	--判断最小内切圆相交、包含情况
-- 	--首先计算每个矩形最小内切圆半径
-- 	rectLR = rectWidthL
-- 	if rectWidthL > rectHeighL then
-- 		rectLR = rectHeighL
-- 	end
-- 	rectLR = rectLR * 0.5

-- 	rectRR = rectWidthR
-- 	if rectWidthR > rectHeighR then
-- 		rectRR = rectHeighR
-- 	end
-- 	rectRR = rectRR * 0.5
-- 	if rectLR + rectRR >= rDis then --包含或相交
-- 		return true
-- 	end

-- 	--判断矩形和边是否相交
-- 	local vertexIndexList = {LEFT_TOP, RIGHT_TOP, RIGHT_BOTTOM, LEFT_BOTTOM}
-- 	for i=1, 4 do
-- 		local curVertexIndex = vertexIndexList[i]
-- 		local nextVertexIndex = vertexIndexList[i + 1]
-- 		if nextVertexIndex == nil then
-- 			nextVertexIndex = vertexIndexList[1]
-- 		end
-- 		if judgeRectIntersectLineSeg(pointMapListL[LEFT_TOP], pointMapListL[RIGHT_TOP], 
-- 									pointMapListL[RIGHT_BOTTOM],	pointMapListL[LEFT_BOTTOM], 
-- 									pointMapListR[curVertexIndex], pointMapListR[nextVertexIndex]) then
-- 			return true
-- 		end	
-- 	end


-- 	return false
-- end

-- 判断圆与扇形是否相交
-- @param:圆点 圆半径 扇形点 扇形半径 扇形偏转角
function math.judgeCircleIntersectFan( circleCenter, circleRadius, sectorCenter, sectorRadius, sectorAngle, sectorTop, sectorBottom )
	-- body
	if sectorAngle <= 0 or sectorAngle >= 360 then
		return
	end

	if math.judgeCircleInterCircle(circleCenter, circleRadius, sectorCenter, sectorRadius) == false then
		return false
	end

	--扇形最上和最下面的点(向上取整)
    -- local halfAngle = ceil(sectorAngle * 0.5)
    -- sectorAngleOffset = ceil(sectorAngleOffset)

    -- local offsetAngle = standardAngle(sectorAngleOffset + halfAngle)
    -- local sectorTop = Vector2.New(sectorRadius * cosTable[offsetAngle], sectorRadius * sinTable[offsetAngle]);
    -- sectorTop:Add(sectorCenter) --转换为全局坐标

    -- offsetAngle = standardAngle(sectorAngleOffset - halfAngle)
    -- local sectorBottom = Vector2.New(sectorRadius * cosTable[offsetAngle], sectorRadius * sinTable[offsetAngle]);
    -- sectorBottom:Add(sectorCenter) --转换为全局坐标

     --  如果圆中心在扇形两边夹角内，则必相交
    local d1 = evaluatePointToLine(circleCenter, sectorCenter, sectorTop)
    local d2 = evaluatePointToLine(circleCenter, sectorCenter, sectorBottom)
    if sectorAngle >= 180 and (d1 >= 0 or d2 <= 0) then
        return true;
    end

    if sectorAngle < 180 and (d1 >= 0 and d2 <= 0) then
        return true;
    end

    if d1 < 0 and d2 > 0 then
        return false    --在左边界的左边 右边界的右边时必定不在扇形内
    end

    -- 判断圆是否与两边相交
    if (distanceFromPointToLine( circleCenter, sectorCenter, sectorTop ) <= circleRadius and d1 < 0) or
    	(distanceFromPointToLine( circleCenter, sectorCenter, sectorBottom ) <= circleRadius and d2 > 0) then
    	return true
    end

    return false;
end

-- 判断圆是否相交
function math.judgeCircleInterCircle( circleCenter, circleRadius, otherCircleC, otherCircleR )
	if otherCircleR == 0 or circleRadius == 0 then
		return false
	end
	local minX = circleCenter.x - otherCircleC.x
	local minY = circleCenter.y - otherCircleC.y
	local dis = circleRadius + otherCircleR
	return (minX * minX + minY * minY) <= (dis * dis)
end

--计算点是否在圆内;
--radiusePower2_: 半径的2次方;
function math.isPointInCircle(x_, y_, circleCenterX_, circleCenterY_, radiusePower2_)
	return ((circleCenterX_-x_) * (circleCenterX_-x_) + (circleCenterY_-y_) * (circleCenterY_-y_)) <= radiusePower2_;
end

-- 取矩形信息
-- centerPos 矩形中心点
-- size 矩形尺寸x,y
-- 矩形基于Y轴的旋转
function math.getRectBounds(centerPos, size, angleY)
	local bounds = {}
	angleY = -angleY + 360
    local cosVal = cosTable[angleY]
    local sinVal = sinTable[angleY]
	local hSizeX = size.x * 0.5
	local hSizeZ = size.y * 0.5
    local cosX = cosVal * hSizeX
    local cosZ = cosVal * hSizeZ
    local sinX = sinVal * hSizeX
    local sinZ = sinVal * hSizeZ
    local p0  = Vector3.New(-cosX - sinZ + centerPos.x, 0, cosZ - sinX + centerPos.z)
    local p1 = Vector3.New(cosX - sinZ + centerPos.x, 0, cosZ + sinX + centerPos.z)
    local p2 = Vector3.New(-cosX + sinZ + centerPos.x, 0, -cosZ - sinX + centerPos.z)
    local p3 = Vector3.New(cosX + sinZ + centerPos.x, 0, -cosZ + sinX + centerPos.z)
    bounds.tLeft = p0
    bounds.tRight = p1
    bounds.bLeft = p2
    bounds.bRight = p3
    bounds.e0 = Vector3.New(p1.x - p0.x, 0, p1.z - p0.z)
    bounds.e1 = Vector3.New(p2.x - p0.x, 0, p2.z - p0.z)
	bounds.e0SqrXZ = bounds.e0.x * bounds.e0.x + bounds.e0.z * bounds.e0.z
	bounds.e1SqrXZ = bounds.e1.x * bounds.e1.x + bounds.e1.z * bounds.e1.z
    return bounds
end

-- 点是否在矩形内
-- rectBounds 矩形信息,通过math.getRectBounds获得
-- point 位置点
function math.isPointInRect(rectBounds, point)
	local ptx = (point.x - rectBounds.tLeft.x) * rectBounds.e0.x + (point.z - rectBounds.tLeft.z) * rectBounds.e0.z
    local ptz = (point.x - rectBounds.tLeft.x) * rectBounds.e1.x + (point.z - rectBounds.tLeft.z) * rectBounds.e1.z
    local xflag = ptx > 0 and ptx < rectBounds.e0SqrXZ
    local zflag = ptz > 0 and ptz < rectBounds.e1SqrXZ
    local result = xflag and zflag
    return xflag and zflag
end


function math.isDotInCircle(pos, centerPos, radius)
  local distance = Vector2.Distance(centerPos, pos);
  if(distance < radius)then
    return true;
  end
  return false;
end

--判断线段是否交叉圆 2D;
function math.isSegLineInterectCircle(startPos, endPos, centerPos, radius)
  -- projectVec2Go = projectVec2Go or GameObject.Find("projectVec2");
  local segEndToStartVec2 = endPos - startPos;
  local segEndToStartVecDir = segEndToStartVec2:Normalize();
  local segCenterToStartVec2 = centerPos - startPos;
  local dotValue = Vector2.Dot(segCenterToStartVec2, segEndToStartVecDir);
  local projectVec2 = startPos+segEndToStartVecDir*dotValue;
  -- projectVec2Go.transform.localPosition = Vector3.New(projectVec2.x, projectVec2.y, 0);
  --判断是否在startPos, endPos范围内;
  -- print("projectVec2="..tostring(projectVec2).." "..tostring(segEndToStartVecDir).."  startPos="..tostring(startPos).."endPos="..tostring(endPos).."         projectVec2="..tostring(projectVec2));
  --在线段内;
  if((projectVec2.x>=startPos.x and projectVec2.x<=endPos.x and projectVec2.y>=startPos.y and projectVec2.y<=endPos.y)or
    (projectVec2.x>=startPos.x and projectVec2.x<=endPos.x and projectVec2.y>=endPos.y and projectVec2.y<=startPos.y)
    )then
    --判断距离是否在radius内;
    local distance = Vector2.Distance(centerPos, startPos);
    local compareDistance = distance*distance - dotValue*dotValue;
    if(compareDistance > radius*radius)then
      return false;
    end
    return true;
  end
  --判断是否在圆内;
  if(math.isDotInCircle(startPos, centerPos, radius))then
    return true;
  end
  if(math.isDotInCircle(endPos, centerPos, radius))then
    return true;
  end
  return false;
end
