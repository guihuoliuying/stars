-- region
-- Date    : 2016-08-18
-- Author  : daiyaorong
-- Description :  
-- endregion

function LoadAllLua()
	loadLua = require
    require("ServerInclude") --加载包含的头文件
end

--function print(...)
--	local str = ""
--	local arg = {...}
--	local n = select('#', ...)
--
--	for i = 1, n do
--		str = string.format("%s%s", str, tostring(arg[i]))
--	end
--
--	Debugger.Log(str)
--end

function printf(format, ...)
	Debugger.Log(string.format(format, ...))
end

function traceback(msg)
	msg = debug.traceback(msg, 2)
	return msg
end

local lastMemory = 0
local function onTimerCheckGc()
 	local currentMemory = collectgarbage("count")
 	EnvironmentHandler.sendLogToServer("lua current ===== "..currentMemory)
 	EnvironmentHandler.sendLogToServer("lua xiaohao ===== "..(currentMemory-lastMemory))
 	EnvironmentHandler.sendLogToServer("================= ")
 	lastMemory = currentMemory
end

function initLuaGC()
	collectgarbage("setpause",150)
  	collectgarbage("setstepmul",500)
  	-- lastMemory = collectgarbage("count")
  	-- FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(60, 0, onTimerCheckGc);
end

function LuaGC()
  -- local c = collectgarbage("count")
  -- Debugger.Log("Begin gc count = {0} kb", c)
  collectgarbage("collect")
  lastMemory = collectgarbage("count")
  -- c = collectgarbage("count")
  -- Debugger.Log("End gc count = {0} kb", c)
end

function RemoveTableItem(list, item, removeAll)
    local rmCount = 0

    for i = 1, #list do
        if list[i - rmCount] == item then
            table.remove(list, i - rmCount)

            if removeAll then
                rmCount = rmCount + 1
            else
                break
            end
        end
    end
end

--unity 对象判断为空, 如果你有些对象是在c#删掉了，lua 不知道
--判断这种对象为空时可以用下面这个函数。
function IsNil(uobj)
	return uobj == nil or uobj:Equals(nil)
end
--- <summary>
--- 判断一个传入的对象是否为数字
--- </summary>
function IsNumber(num)
    return num ~= nil and type(num) == "number";
end
--- <summary>
--- 判断一个传入的字符串是否为nil或者是为空
--- </summary>
function string.IsNilOrEmpty(str)
    return str == nil or tostring(str) == "";
end

-- isnan
function isnan(number)
	return not (number == number)
end

function changeObjctLayer( trans, layer )
	-- body
	trans.gameObject.layer = layer
	for i=0,trans.childCount - 1 do
		changeObjctLayer(trans:GetChild (i),layer)
	end
end

function string:split(sep)
	local sep, fields = sep or ",", {}
	local pattern = string.format("([^%s]+)", sep)
	self:gsub(pattern, function(c) table.insert(fields, c) end)
	return fields
end

function GetDir(path)
	return string.match(fullpath, ".*/")
end

function GetFileName(path)
	return string.match(fullpath, ".*/(.*)")
end

function table.contains(table, element)
  if table == nil then
        return false
  end

  for _, value in pairs(table) do
    if value == element then
      return true
    end
  end
  return false
end

function table.getCount(self)
	local count = 0
	
	for k, v in pairs(self) do
		count = count + 1	
	end
	
	return count
end

function DumpTable(t)
	for k,v in pairs(t) do
		if v ~= nil then
			Debugger.Log("Key: {0}, Value: {1}", tostring(k), tostring(v))
		else
			Debugger.Log("Key: {0}, Value nil", tostring(k))
		end
	end
end

 function PrintTable(tab)
    local str = {}

    local function internal(tab, str, indent)
        for k,v in pairs(tab) do
            if type(v) == "table" then
                table.insert(str, indent..tostring(k)..":\n")
                internal(v, str, indent..' ')
            else
                table.insert(str, indent..tostring(k)..": "..tostring(v).."\n")
            end
        end
    end

    internal(tab, str, '')
    return table.concat(str, '')
end

function PrintLua(name, lib)
	local m
	lib = lib or _G

	for w in string.gmatch(name, "%w+") do
       lib = lib[w]
     end

	 m = lib

	if (m == nil) then
		Debugger.Log("Lua Module {0} not exists", name)
		return
	end

	Debugger.Log("-----------------Dump Table {0}-----------------",name)
	if (type(m) == "table") then
		for k,v in pairs(m) do
			Debugger.Log("Key: {0}, Value: {1}", k, tostring(v))
		end
	end

	local meta = getmetatable(m)
	Debugger.Log("-----------------Dump meta {0}-----------------",name)

	while meta ~= nil and meta ~= m do
		for k,v in pairs(meta) do
			if k ~= nil then
			Debugger.Log("Key: {0}, Value: {1}", tostring(k), tostring(v))
			end

		end

		meta = getmetatable(meta)
	end

	Debugger.Log("-----------------Dump meta Over-----------------")
	Debugger.Log("-----------------Dump Table Over-----------------")
end

function stringToTable(str)
   if(str==nil)then
   		return nil;	
   end
   return loadstring("return "..str)()
end

function SetIndex(indexTable) --设置Index方法，面向对象的继承用到（提高访问效率）
	-- indexTable.__index = indexTable
    indexTable.__index =function(table,key)
        local value = indexTable[key]
        rawset(table,key,value)
        return value
    end
end

local function createFunc( self, o )
	o = o or {}
	setmetatable( o, self )
	return o
end

function createClass( o )
	-- body
	o = o or {}
	o.create = createFunc
	o.new = createFunc
	o.__index = o
	return o
end

function createClassWithExtends( o, parent )
	-- body
	o = o or {}
	o.__index = o
	setmetatable(o, parent )
	return o
end

function isRunningInServer( )
	return false
end


--注册模块, 用于初始化模块的命令，之后模块(注:mvc理解为同一个模块)之间调用公开的方法通过命令进行调用;
local function registerModules()
    FightModel.register() --暂时放这
	CharacterManager:init()
end

--注册协议;
local function registerProtocol()
	RoleProtocol.register();
end

LoadAllLua()
initLuaGC()
-- registerProtocol()
registerModules()
-- RoleModel.init()
-- RoleData.init();