-- region
-- Date    : 2016-07-04
-- Author  : daiyaorong
-- Description :  对比函数
-- endregion

JudgeCompare = {}
local Abs = math.abs
local PRECISION = FightDefine.PRECISION

local function greaterThan( a1,a2 )
	-- body
	return (a1 - a2) > PRECISION
end

local function lessThan( a1, a2 )
	-- body
	return (a2 - a1) > PRECISION
end

local function euqal( a1,a2 )
	-- body
	return Abs(a1 - a2) <= PRECISION
end

local function greaterThanOrEqual( a1,a2 )
	-- body
	return a1 >= a2
end

local function lessThanOrEqual( a1, a2 )
	-- body
	return (a1 - a2) <= PRECISION
end

local function unequal( a1,a2 )
	-- body
	return Abs(a1 - a2) > PRECISION
end

JudgeCompare["<"] = lessThan
JudgeCompare[">"] = greaterThan
JudgeCompare[">="] = greaterThanOrEqual
JudgeCompare["<="] = lessThanOrEqual
JudgeCompare["=="] = euqal
JudgeCompare["!="] = unequal