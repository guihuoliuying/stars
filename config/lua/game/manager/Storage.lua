--
-- Created by linzewei
-- User: linzewei
-- Date: 2015/4/17
-- Time: 10:21
-- To change this template use File | Settings | File Templates.
--
Storage = {}
local Item = {}
Item.__index = Item

function Item:create()
    -- body
    local item = { listenDic = {} }
    setmetatable(item, Item)
    return item
end

--字段改动回调处理
local function doChange(list, value)
    -- body
    for i = #list, 1, -1 do
        list[i](value)
    end
end

--设置字段数据
function Item:setData(key, value)
    -- body
    if( self.listenDic == nil)then
        return
    end
    self[key] = value
    if (self.listenDic[key]) then
        doChange(self.listenDic[key], value)
    end
end

--获取字段数据
function Item:getData(key)
    return self[key]
end

--移除监听
function Item:removeListener(key, callback)
    -- body
    if( self.listenDic == nil)then
        return
    end
    local callList = self.listenDic[key]
    if (callList ~= nil and callList[callback] ~= nil) then
        tableplus.remove(callList, callback)
    end
end

--添加监听，callIfNotNil表示该字段目前不为空，则立刻回调
function Item:addListener(key, callback, callIfNotNil)
    -- body
    if( self.listenDic == nil)then
        return
    end
    if (callIfNotNil and self[key] ~= nil) then
        callback(self[key])
    end

    local callList = self.listenDic[key]
    if (callList == nil) then
        callList = {}
        self.listenDic[key] = callList
    end
    if (callList[callback] == nil) then
        callList[callback] = true
        callList[#callList + 1] = callback
    end
end

--释放
function Item:dispose( )
    -- body
    self.listenDic = nil
end






--设置模块指定字段数据，会触发回调
function Storage.set(modulename, key, value)
    -- body
    local item = Storage[modulename]
    if (item == nil) then
        item = Item:create()
        Storage[modulename] = item
    end
    item:setData(key, value)
end

--获取对应模块指定字段的数据
function Storage.get(modulename, key)
    local item = Storage[modulename]
    if item ~= nil then
        return item[key]
    end
    return nil
end

--增加对某个模块某个字段的监听，字段改动回调。
--callIfNotNil表示监听时，如果字段有数据，那么立刻回调
function Storage.addListener(modulename, key, callback, callIfNotNil)
    -- body
    Storage.getModule(modulename):addListener(key, callback, callIfNotNil)
end

--移除对某个模块某个字段的监听
function Storage.removeListener(modulename, key, callback)
    -- body
    local item = Storage[modulename]
    if (item ~= nil) then
        item:removeListener(key, callback)
    end
end

--获取指定模块名的item
function Storage.getModule(modulename)
    -- body
    local item = Storage[modulename]
    if (item == nil) then
        item = Item:create()
        Storage[modulename] = item
    end
    return item
end

--清除指定模块名，包含数据和回调
function Storage.clearModule(modulename)
    -- body
    local item = Storage[modulename]
    if( item )then
        item:dispose()
        Storage[modulename] = nil
    end
end
