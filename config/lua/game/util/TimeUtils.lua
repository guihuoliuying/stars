--region TimeUtlis.lua
--Author : zrc
--Date   : 2015/3/11
--脚本定时器管理类
TimeUtils = {

   
}
--TimeCount = 0
--TimerTable = {}

--function TimeUtils.TimerUpdate() --定时器调用
--  for i,v in pairs(TimerTable) do
--      if TimerTable[i] ~= nil then
--       TimerTable[i]()
--      end
--  end
--end



--function TimeUtils.insertTable(func) --将回调函数插入到定时器队列
--   TimeCount = TimeCount + 1
--   table.insert(TimerTable,TimeCount,func)
--   return TimeCount
--end


--function TimeUtils.removeTable(handle) -- 删除定时器队列回调函数
--    if handle > TimeCount or handle <= 0 then return end --容错处理
--    table.remove(TimerTable,handle)
--    TimeCount = TimeCount - 1
--end


--endregion

-- 返回格式为 HH:mm:ss 格式的时间字符串
function TimeUtils.GetTime()
    return os.date("%X");
end
-- 返回格式为 MM/dd/yy 格式的日期字符串
function TimeUtils.GetDate()
    return os.date("%x");
end
-- 返回格式为 MM/dd/yy HH:mm:ss  格式的日期字符串
function TimeUtils.GetDateTime()
    return os.date("%c");
end

-- 返回当前时间
function TimeUtils.getDateByStamp(timeStamp)
    return os.date("*t", tonumber(timeStamp))
end

-- 适用于把x秒显示为xx:yy
local second = nil
local minute = nil
local result = nil
function TimeUtils.formatTime( value )
    -- body
    if value <= 0 then
        return "0:00"
    end
    if value < 60 then
        if value < 10 then
          value = "0"..value
        end
        return "0:"..value
    else
        minute = math.floor( value/60 )
        second = value % 60
        if second < 10 then
          second = "0"..second
        end
        return minute..":"..second
    end
end

-- 适用于把个位数x显示为0x
function TimeUtils.formatTimeOne( value )
    -- body
    if value <= 0 then
        return "00"
    elseif value < 10 then
        return "0"..value
    else 
        return tostring(value)
    end 
end

-- 原始格式: dateStr:20160712  timeStr:235959
-- 目标格式：{year=2016,month=7,date=12, hour=23,minute=59,second=59}
function TimeUtils.parseDateTime(dateStr, timeStr)
	local result = {}
	result.year = tonumber(string.sub(dateStr, 1, 4))
	result.month = tonumber(string.sub(dateStr, 5, 6))
	result.day = tonumber(string.sub(dateStr, 7, 8))
	if timeStr then
		result.hour = tonumber(string.sub(timeStr, 1, 2))
		result.minute = tonumber(string.sub(timeStr, 3, 4))
		result.second = tonumber(string.sub(timeStr, 5, 6))
	end
	return result
end

-- 格式化时间数字
-- value 原始值(number)
-- digits 显示位数   如(1,2) 将显示成01  (1,4)将显示成0001
-- return string
function TimeUtils.formatTimeNumber(value, digits)
	local str = tostring(value)
	local len = string.len(str)
	if len < digits then
		for i = len + 1, digits do
			str = "0" .. str
		end
	end
	return str
end

-- 格式化秒成HH:mm:ss格式
function TimeUtils.formatTimeHHMMSS(seconds)
  	local hour = math.floor(seconds / 3600)
  	local minute = math.floor((seconds - (hour * 3600)) / 60)
  	local second = seconds - hour * 3600 - minute * 60
  	local timeStr = TimeUtils.formatTimeNumber(hour, 2) .. ":" .. TimeUtils.formatTimeNumber(minute, 2) .. ":" .. TimeUtils.formatTimeNumber(second, 2)
  	return timeStr
end

-- 格式化秒成HH:mm格式
function TimeUtils.formatTimeHHMM(seconds)
    local hour = math.floor(seconds / 3600)
    local minute = math.floor((seconds - (hour * 3600)) / 60)
    local timeStr = TimeUtils.formatTimeNumber(hour, 2) .. ":" .. TimeUtils.formatTimeNumber(minute, 2)
    return timeStr
end

-- 格式化秒成HH:mm:ss格式或者mm:ss
function TimeUtils.formatTimeMMSS(seconds)
    local hour = math.floor(seconds / 3600)
    local minute = math.floor((seconds - (hour * 3600)) / 60)
    local second = seconds - hour * 3600 - minute * 60
    local timeStr = ''
    if hour > 0 then
        timeStr = TimeUtils.formatTimeNumber(hour, 2) .. ":" .. TimeUtils.formatTimeNumber(minute, 2) .. ":" .. TimeUtils.formatTimeNumber(second, 2)
    else
        timeStr = TimeUtils.formatTimeNumber(minute, 2) .. ":" .. TimeUtils.formatTimeNumber(second, 2)
    end
    return timeStr
end

-- 格式化日期显示
-- timeStamp 时间戳
-- split 分割符
-- 如 2016-08-26 或 2016/08/26
function TimeUtils.formatFullDate(timeStamp, split)
	split = split or "-"
	local date = os.date("*t", timeStamp)
	local year = tostring(date.year)
	local month = TimeUtils.formatTimeNumber(date.month, 2)
	local day = TimeUtils.formatTimeNumber(date.day, 2)
	return (year .. split .. month .. split .. day)
end

-- 格式化时间显示
function TimeUtils.formatFullTime(timeStamp, split)
	split = split or ":"
	local date = os.date("*t", timeStamp)
	local hour = tostring(date.hour)
	local minute = TimeUtils.formatTimeNumber(date.min, 2)
	local second = TimeUtils.formatTimeNumber(date.sec, 2)
	return string.format("%s%s%s%s%s",hour,split,minute,split,second)    
end

function TimeUtils.formatYYMMDDHHMM(timeStamp, split1, split2)
    --print("显示 ："..tostring(timeStamp).." , "..debug.traceback());
    split1 = split1 or "-"
    split2 = split2 or ":"
    --print("AAAAAAAAA "..tostring(timeStamp));
    local date = os.date("*t", tonumber(timeStamp));
    --print("bbbbbbbbbbbb")
    local year = tostring(date.year)
    local month = TimeUtils.formatTimeNumber(date.month, 2)
    local day = TimeUtils.formatTimeNumber(date.day, 2)
    local hour = tostring(date.hour)
    local minute = TimeUtils.formatTimeNumber(date.min, 2)
    --print("vbbbbbbbbbbbbbb")
    return string.format("%s%s%s%s%s %s%s%s",year,split1,month,split1,day,hour,split2,minute);
end

function TimeUtils.formatYYMMDDHHMMSS(timeStamp, split1, split2)
    --print("显示 ："..tostring(timeStamp).." , "..debug.traceback());
    split1 = split1 or "-"
    split2 = split2 or ":"
    --print("AAAAAAAAA "..tostring(timeStamp));
    local date = os.date("*t", tonumber(timeStamp));
    --print("bbbbbbbbbbbb")
    local year = tostring(date.year)
    local month = TimeUtils.formatTimeNumber(date.month, 2)
    local day = TimeUtils.formatTimeNumber(date.day, 2)
    local hour = TimeUtils.formatTimeNumber(date.hour, 2)
    local minute = TimeUtils.formatTimeNumber(date.min, 2)
    local second = TimeUtils.formatTimeNumber(date.sec, 2)
    --print("vbbbbbbbbbbbbbb")
    return string.format("%s%s%s%s%s %s%s%s%s%s",year,split1,month,split1,day,hour,split2,minute,split2,second);
end

-- 格式化当天时间，以秒为单位
-- timeStamp 时间戳
-- 如 2016-08-26 19:20:10 返回 19*3600+20*60+10
function TimeUtils.formatCurrentTimeSec(timeStamp)
  if timeStamp == nil then return end
  local currentTime = os.date("*t" , timeStamp)
  return currentTime.hour*3600+currentTime.min*60+currentTime.sec
end

-- 显示输入时间戳到当前时间的相隔天数
-- timeStamp 时间戳（秒）
-- 输出:
--      0-59秒 == x（秒）               
--      60秒-59分钟59秒 == x（分）
--      1小时-23小时59分59秒 == x（小时）
--      1天-29天 == x （天)
--      30天 -- 12月 == x（月）
--      1年-无尽 == x（年）

function TimeUtils.formatMDMSBeforeNow(timeStamp)
    if timeStamp == nil then return end
    local currTimeStamp = os.time()
    local currDate = os.date("*t", currTimeStamp)
    --GameLog("zls","year:",currDate.Year,"month:",currDate.Month,"day:",currDate.Day,"hour:",currDate.Hour,"min:",currDate.Minute,"second:",currDate.Second)
    local oldDate = os.date("*t", tonumber(timeStamp))
    --GameLog("zls","year:",oldDate.Year, "month:",oldDate.Month, "day:",oldDate.Day,"hour:",oldDate.Hour,"min:",oldDate.Minute,"second:",oldDate.Second)

    local year   = currDate.year   - oldDate.year
    local month  = currDate.month  - oldDate.month
    local day    = currDate.day    - oldDate.day
    local hour   = currDate.hour   - oldDate.hour
    local min    = currDate.min - oldDate.min
    local second = currDate.sec - oldDate.sec
    --GameLog("zls","year:",year,"month:",month,"day:",day,"hour:",hour,"min:",min,"second:",second)
    if year > 0 then 
        return 1,year
    elseif month > 0 then
        return 2,month
    elseif day > 0 then
        return 3,day
    elseif hour > 0 then
        return 4,hour
    elseif min > 0 then
        return 5,min
    elseif second > 0 then
        return 6,second
    end
    return 0,0
end

-- 显示输入时间戳所代表的具体时间段
-- timeStamp 时间戳（秒）
-- 输出:
--      A年B月C日D时E分F秒
local _maxYear = 356*24*60*60
local _maxMouth = 30*24*60*60
local _maxDay  = 24*60*60
local _maxHour = 60*60
local _maxMinute = 60
local _maxSecond = 1
function TimeUtils.formatStampToTime(timeStamp)
    local curYear = 0
    local curMouth = 0
    local curDay = 0
    local curHour = 0
    local curMinute = 0
    local curSecond = 0
    local stamp = timeStamp 
    while stamp > 0 do
        if stamp >= _maxYear then
            curYear = Mathf.Floor(Mathf.stamp/_maxYear)
            stamp = stamp - curYear * _maxYear
        elseif stamp >= _maxMouth then
            curMouth = Mathf.Floor(stamp/_maxMouth)
            stamp = stamp - curMouth * _maxMouth
        elseif stamp >= _maxDay then
            curDay = Mathf.Floor(stamp/_maxDay)
            stamp = stamp - curDay * _maxDay
        elseif stamp >= _maxHour then
            curHour = Mathf.Floor(stamp/_maxHour)
            stamp = stamp - curHour * _maxHour
        elseif stamp >= _maxMinute then
            curMinute = Mathf.Floor(stamp/_maxMinute)
            stamp = stamp - curMinute * _maxMinute
        elseif stamp >= _maxSecond then
            curSecond = Mathf.Floor(stamp/_maxSecond)
            stamp = stamp - curSecond * _maxSecond
        end
    end
    --GameLog("zls",curYear," ",curMouth," ",curDay, " ",curHour, " ", curMinute, " ",curSecond)
    local txtYear = ""
    local txtMouth = ""
    local txtDay = ""
    local txtHour = ""
    local txtMinute = ""
    local txtSecond = ""
    if curYear > 0 then txtYear = string.format("%s年",curYear) end
    if curMouth > 0 then txtMouth = string.format("%s月",curMouth) end
    if curDay > 0 then txtDay = string.format("%s天",curDay) end
    if curHour > 0 then txtHour = string.format("%s小时",curHour) end
    if curMinute > 0 then txtMinute = string.format("%s分",curMinute) end
    if curSecond > 0 then txtSecond = string.format("%s秒",curSecond) end
    return string.format("%s%s%s%s%s%s",txtYear,txtMouth,txtDay,txtHour,txtMinute,txtSecond)
end

--return true , next>pre;
function TimeUtils.compare(preHour, preMinute, preSecond, nextHour, nextMinute, nextSecond)
    local isBig = false;
    for i=1,1 do
        if(nextHour < preHour)then
            break;
        end
        if(nextHour == preHour and nextMinute < preMinute)then
            break;
        end
        if(nextHour == preHour and nextMinute == preMinute and nextSecond<preSecond)then
            break;
        end
        isBig = true;
    end
    return isBig;
end

function TimeUtils.getDiffSeconds(preHour, preMinute, preSecond, nextHour, nextMinute, nextSecond)
    local preSeconds = preHour*3600+preMinute*60+preSecond;
    local nextSeconds = nextHour*3600+nextMinute*60+nextSecond;
    return nextSeconds - preSeconds;
end
