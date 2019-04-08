-- region
-- Date    : 2016-07-04
-- Author  : daiyaorong
-- Description :  逻辑函数
-- endregion

JudgeLogic = {}

local function andLogic( param1, param2 )
	-- body
	return param1 and param2
end

local function orLogic( param1, param2 )
	-- body
	return param1 or param2
end

JudgeLogic["&"] = andLogic
JudgeLogic["$"] = orLogic