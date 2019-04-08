-- FrameTimerManager.lua
-- FrameTimeItem
FrameTimerPrefix = "FrameTimer+"
local TABLE_REMOVE = table.remove

FrameTimeItem = {
    delay = nil,
    repeatCount = nil,
    callback = nil,
    callback2 = nil,
    onOver = nil,
    frameRate = nil,
    lastTime = nil,
    luaCallBack = nil,
    luaCallBackOver = nil,
    itemIndex = nil
}

function FrameTimeItem:new( o )
    o = o or {}
    setmetatable(o, self)
    self.__index = self
    return o
end

-- 注册的都是lua回调方法
function FrameTimeItem.New( delay, repeatCount, luaCallBack, overCallback, curFrame )
    local item = FrameTimeItem:new()
    item.delay = delay
    item.repeatCount = repeatCount
    item.luaCallBack = luaCallBack
    item.luaCallBackOver = overCallback
    item.nextTime = curFrame + delay
    return item
end

local function FrameTimerItemBuilder( delay, repeatCount, luaCallBack, overCallback, curFrame)
    local item = {}
    local nextTime = curFrame + delay
    item.repeatCount = repeatCount
    item.luaCallBack = luaCallBack
    item.overCallback = overCallback
    item.exec = function ( nowTime )
        -- body
        if( nowTime >= nextTime )then
            nextTime = nowTime + delay
            if( luaCallBack )then
                luaCallBack()
            end
            return true
        end
        return false
    end
    return item
end

-- FrameTimer
FrameTimer = {
    timerType = FrameTimerPrefix,
    itemDic = {},
    callList = {},
    elapsedTime = 0,
}
FrameTimerIndex = 1

function FrameTimer:new( o )
    o = o or {}
    setmetatable(o, self)
    self.__index = self
    return o
end

function FrameTimer.New( name )
    local timer = FrameTimer:new()
    timer.itemDic = {}
    timer.callList = {}
    timer.callCount = 0
    timer.elapsedTime = 0
    timer.name = name
    timer.enabled = true
    local item = nil
    local enterFrameHandler = function ( )
        timer.elapsedTime = timer.elapsedTime + 1
        for i=timer.callCount,1,-1 do
            item = timer.callList[i]
            if( item and timer.itemDic[item.itemIndex] and item.luaCallBack )then
                if item.exec( timer.elapsedTime )then
                    if( item.repeatCount > 0)then
                        item.repeatCount = item.repeatCount - 1
                        if( item.repeatCount < 1)then
                            TABLE_REMOVE(timer.callList,i)
                            timer.callCount = timer.callCount - 1
                            timer.itemDic[item.itemIndex] = nil
                            if( item.overCallback ~= nil)then
                                item.overCallback()
                            end
                        end
                    end
                end
            else
                TABLE_REMOVE(timer.callList, i)
                timer.callCount = timer.callCount - 1
            end
        end
    end
    timer.enterFrameHandler = enterFrameHandler
    return timer
end

function FrameTimer:RegisterLuaCallBackWithEnd( delay, repeatCount, callback, overCallback )
    FrameTimerIndex = FrameTimerIndex + 1
    if self.itemDic[FrameTimerIndex] == nil then
        local item = FrameTimerItemBuilder(delay, repeatCount, callback, overCallback, self.elapsedTime )
        item.itemIndex = FrameTimerIndex
        self.callCount = self.callCount + 1
        self.callList[self.callCount] = item
        self.itemDic[ FrameTimerIndex ] = true
        return FrameTimerIndex
    end
end

function FrameTimer:RegisterLuaCallBack( delay, repeatCount, callback,isPrint )
    FrameTimerIndex = FrameTimerIndex + 1
    if self.itemDic[FrameTimerIndex] == nil then
        local item = FrameTimerItemBuilder(delay, repeatCount, callback, nil, self.elapsedTime )
        item.itemIndex = FrameTimerIndex
        self.callCount = self.callCount + 1
        self.callList[self.callCount] = item
        self.itemDic[ FrameTimerIndex ] = true
        return FrameTimerIndex
    end
end

function FrameTimer:hasFunction( itemIndex )
    if itemIndex==nil then
        return false
    end
    return self.itemDic[itemIndex]
end

function FrameTimer:removeCallback( itemIndex )
    if self:hasFunction(itemIndex) then
        self.itemDic[itemIndex] = nil
    end
end

function FrameTimer:removeLuaFunc( itemIndex )
    self:removeCallback(itemIndex)
end

function FrameTimer:dispose( )
    self.itemDic =  {}
    self.callList = {}
    self.callCount = 0
end

-- FrameTimerManager
FrameTimerManager = {
    dic = {},
    keyDic = {},
}

function FrameTimerManager:new( o )
    o = o or {}
    setmetatable(o, self)
    self.__index = self
    return o
end

function FrameTimerManager.New( )
    local FTM = FrameTimerManager:new()
    FTM.dic = {}
    FTM.keyDic = {}
    return FTM
end

FTMSingleton = FrameTimerManager.New()

local FrameInstance = {
    ConstantData.FRAME_EVENT_LUA,
    ConstantData.FRAME_EVENT_LUACHARAC,
    ConstantData.FRAME_EVENT_LUASCENE,
    ConstantData.FRAME_EVENT_AI,
    ConstantData.FRAME_EVENT_CAMERA,
    ConstantData.FRAME_EVENT_UI,
    ConstantData.FRAME_EVENT_DEFAULT,
}

function FrameTimerManager.getInstance( name )
    name = name or ConstantData.FRAME_EVENT_DEFAULT
    if FTMSingleton.dic[name] == nil then
        FTMSingleton.dic[name] = FrameTimer.New(name)
        FTMSingleton.keyDic[ #FTMSingleton.keyDic + 1] = FTMSingleton.dic[name]
    end
    return FTMSingleton.dic[name] 
end

function FrameTimerManager.pauseInstance( name )
    name = name or ConstantData.FRAME_EVENT_DEFAULT
    if FTMSingleton.dic[name] then
    	FTMSingleton.dic[name].enabled = false

    end
end

function FrameTimerManager.resumeInstance( name )
    name = name or ConstantData.FRAME_EVENT_DEFAULT
    if FTMSingleton.dic[name] then
    	FTMSingleton.dic[name].enabled = true
    end
end

function FrameTimerManager.freeInstance( name )
    name = name or ConstantData.FRAME_EVENT_DEFAULT
    if FTMSingleton.dic[name] then
        local timer = FTMSingleton.dic[name]
        timer:dispose()
    end

    FTMSingleton.dic[name] = nil
end

function FrameTimerManager.activeAllInstance(flag)
    for k,v in pairs(FTMSingleton.dic) do
        v.enabled = flag
    end
end

function FrameTimerManager.clear()
    FTMSingleton.dic = {}
    FTMSingleton.keyDic = {}
end

function FrameTimerManager.dispose()
    for k,v in pairs(FTMSingleton.dic) do
        v:dispose()
    end
    FrameTimerManager.clear()
end

function FrameTimerManager.frameHandle( )
    --先遍历特定顺序的定时器
	for i,timer in ipairs(FTMSingleton.keyDic) do
        if( timer.enabled )then
            timer.enterFrameHandler()
        end
	end
end

function FrameTimerManager.testLog( )
    for i,timer in ipairs(FTMSingleton.keyDic) do
        if( timer.enabled )then
            LogMan(timer.name..",count="..#timer.callList)
        end
    end
end

-- FrameTimerManager

function init( )
    for index, name in ipairs(FrameInstance) do
        FrameTimerManager.getInstance(name)
    end
end

init()