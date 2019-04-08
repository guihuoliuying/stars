--author: zrc
--Data:  2015-03-09

IsPrintSelf = true --设置是否只打印自己的

EnableLog = true --如果觉得输出行数的方法影响性能，请置为false

local myLog = LogMan

-- if IsPrintSelf then -- 使用闭包屏蔽之前的打印函数
--  print = function() end
-- end
do
	if EnvironmentHandler.isInServer == false then
		UILog = UIDebug.Log
	end
end

local fileHandlers = {}
function LogToFile( name, filename, log )
    if(PrinterName==nil or PrinterName ~= name) then return; end

    if type(log) == "string" and type(filename) == "string" and string.len(filename) > 0 then
        local fullFileName = name.."-"..filename..".txt"
        local file = fileHandlers[fullFileName]
        if file == nil then
            file = io.open(fullFileName, "w")
            fileHandlers[fullFileName] = file
        end
        file:write(log.."\r\n")
    end
end

function EndLogToFile( )
    for k,v in pairs(fileHandlers) do
        v:close()
    end
    fileHandlers = {}
end

--个人打印方法，现在可以传入任意参数了
function GameLog(name, log, ...)
    if(PrinterName==nil or PrinterName ~= name) then return; end

    local args= {...};
    if(#args>0) then
        for i=1,#args do
            log=log .. tostring(args[i]);
        end
    end
   if (type(log) == "table") then 
       log =  tableplus.tostring(t, recursive)
   end
   myLog(log);
end

function Log(log,... )
    GameLog('dl', log, ...)
end

function GameNetLog(log)
    if PrinterName == nil then return ; end
    myLog(log);
end

function TraceLog(msg)
    if PrinterName == nil then return  end
    msg = debug.traceback(msg, 2)
    myLog(msg)
end

function DevLog( name,str )
    -- body
    if IsPrintSelf then
        if(PrinterName == name) then
            myLog(name.." log : "..str)
        end
    end
end

--转换cmd的参数为字符串;
local function convertCmdParams(...)
    local args= {...};
    if(#args<1) then return; end
    local log = TimeUtils.GetTime() .. "->";
    for i=1,#args do
        log=log .. tostring(args[i]);
    end
    return log;
end

--命令行窗口显示日志，日志将显示到命令行窗口里面，允许若干参数
--个人打印方法请用 GameLog。
function CmdLog(...)
    if PrinterName == nil then return ; end
    LuaHelper.consoleLog(convertCmdParams(...));
end

function CmdLogWarn(...)
    if PrinterName == nil then return ; end
    LuaHelper.consoleLogWarn(convertCmdParams(...));
end

function CmdLogError(...)
    if PrinterName == nil then return ; end
    LuaHelper.consoleLogError(convertCmdParams(...));
end

--显示文件名和行数的打印方法
function GMLog( name, something )
    if EnableLog ~= true then return end
    if(PrinterName~=nil and PrinterName ~= name) then return; end

    local info = debug.getinfo(2, "Sl")
    local paths =  StringUtils.split(info.short_src, "/")
    local filename = string.sub(paths[#paths],1,#paths[#paths]-2)
    local result =  something.."\n\n\n\n".."文件名#"..filename.. "#行数*".. info.currentline

    myLog(result)
end

all = {}
function AddLogToAll( something )
    -- all[#all + 1] = something.."\n"
end

function LogAll( )
    -- local filename
    -- if CodeForCSUtil.isRunningServer( ) then
    --     filename = "C:/Users/user/Desktop/2.txt"
    -- else
    --     filename = "C:/Users/user/Desktop/1.txt"
    -- end
    -- local file = io.open(filename, "w")
    -- file:write(tableplus.tostring(all))
    -- file:close()
end
