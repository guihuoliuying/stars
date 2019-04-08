--[=[
    @@file
    @auth zhaowenshuo
    @date 2014/07/01 17:54
    @vers 1.0
    @desc 提供增强table操作的一些辅助函数
]=]

tableplus = {}
printTable = true
--[=[
    @@func
    @desc 浅复制table
    @para @type tabl @desc 需要复制的table
    @rtrn @type tabl @desc 返回已复制的table
]=]
function tableplus.shallowcopy(t)
    local nt = {}
    tableplus.foreach(t, function(k, v)
        rawset(nt, k, v)
    end)
    return nt
end

function tableplus.deepcopy(t)
    local nt = {}
    tableplus.foreach(t, function(k, v)
        if type(v) == "table" then
            rawset(nt, k, tableplus.deepcopy(v))
        else
            rawset(nt, k, v)
        end
    end)
    local meta = getmetatable(t)
    if( meta )then
        setmetatable(nt,meta)
    end
    return nt
end

-- 浅层比较
function tableplus.simpleCompare( tableA, tableB )
    -- body
    if tableA == nil or tableB == nil then
        return false
    end
    if #tableA ~= #tableB then
        return false
    end
    for k,v in pairs( tableA ) do
        if v ~= tableB[k] then
            return false
        end
    end
    return true
end

--[=[
    @@func
    @desc 将table的存储方式从hashmap转换成list
    @para @type tabl @desc 待转换的table
    @rtrn @type tabl @desc 存储方式为list的table
]=]
function tableplus.toarray(t)
    local nt = {}
    tableplus.foreach(t, function(_, v)
        table.insert(nt, v)
    end)
    return nt
end

--[=[
    @@func
    @desc 将table的存储方式从list转换成hashmap
    @para @type tabl @desc 待转换的table
    @rtrn @type tabl @desc 存储方式为hashmap的table
]=]
function tableplus.tohashmap(t, f)
    local nt = {}
    tableplus.foreach(t, function(k, v)
--        --print(k, v, f(v))
        rawset(nt, f(v), v)
    end)
    return nt
end

--[=[
    @@func
    @desc 返回table的字符串表示
    @para @type tabl @desc table
    @rtrn @type str @desc table的字符串表示
]=]
function tableplus.tostring(t, recursive)
    if PrinterName == nil then return "" end
    if (type(t) ~= "table") then
        return tostring(t)
    else
        local str = "{"
        tableplus.foreach(t, function(k, v)
            -- handle key
            if type(k) == "string" then
                str = str .. "['" .. k .. "']="
            else
                str = str .. "[" .. tostring(k) .. "]="
            end
            -- handle val
            if type(v) == "table" and recursive then
                str = str .. tableplus.tostring(v, recursive) .. ","
            elseif type(v) == "string" then
                str = str .. "'" .. v .. "',"
            else
                str = str .. tostring(v) .. ","
            end
        end)
        return str .. "}"
    end
    -- return ""
end

function tableplus.formatstring(t, recursive, indent)
    if PrinterName == nil then return "" end
    indent = indent or 1
    local padding = ""
    -- fill padding
    for i = 1, indent do
        padding = padding .. "    "
    end

    if (type(t) ~= "table") then
        return tostring(t)
    else
        local str = "{"
        tableplus.foreach(t, function(k, v)
            -- add padding
            str = str .. "\n" .. padding
            -- handle key
            if type(k) == "string" then
                str = str .. "['" .. k .. "']="
            else
                str = str .. "[" .. tostring(k) .. "]="
            end
            -- handle val
            if type(v) == "table" and recursive then
                str = str .. tableplus.formatstring(v, recursive, indent + 1) .. ","
            elseif type(v) == "string" then
                str = str .. "'" .. v .. "',"
            else
                str = str .. tostring(v) .. ","
            end
        end)
        return str .. "\n" .. string.sub(padding, 1, string.len(padding)-4) .. "}"
    end
    -- return ""
end

--[=[
    @@func
    @desc 过滤条件（f(k, v)）为假的元素
    @para @type func @desc 判定函数，作用于每一个元素
    @rtrn @type tabl @desc 返回已经过滤的table
]=]
function tableplus.filter(t, f)
    local nt = {}
    tableplus.foreach(t, function(k, v)
        if f(k, v) then rawset(nt, k, v) end
    end)
    return nt
end

function tableplus.distinct(t, f)
    local nt = {}
    tableplus.foreach(t, function(_, v)
        if not tableplus.matchany(nt, v, f) then
            table.insert(nt, v)
        end
    end)
    return nt
end

function tableplus.subarray(t, idx, len)
    local nt = {}
    for i = idx, idx + len - 1 do
        table.insert(nt, t[i])
    end
    return nt
end

function tableplus.foreach(t, f)
    if t == nil then return end
    for k, v in pairs(t) do
        if f(k, v) then break end
    end
end

function tableplus.iforeach(t, f)
    for i = 1, #t do
        if f(i, t[i]) then break end
    end
end

function tableplus.matchany(t, p, f)
    local flag = false
    tableplus.foreach(t, function(_, v)
        return f(p, v)
    end)
    return flag
end

function tableplus.matchall(t, p, f)
    local flag = true
    tableplus.foreach(t, function(_, v)
        flag = f(p, v) and flag
    end)
    return flag
end

function tableplus.reduce(t, f)
    local r
    tableplus.foreach(t, function(_, v) r = f(r, v) end)
    return r
end

function tableplus.count(t)
    local c = 0
    tableplus.foreach(t, function(_, _) c = c + 1 end)
    return c
end

function tableplus.index( t, o )
    -- body
    for i,v in ipairs(t) do
        if( o == v )then
            return i
        end
    end
    return -1
end

--移除无顺序table的item
function tableplus.remove( t, item )
    -- body
    local index = tableplus.index(t,item)
    if( index ~= -1)then
        if( index ~= #t)then
            t[index] = t[#t]
        end
        t[#t] = nil
    end
end

--- <summary>
--- 将一个表(t2)的所有字段复制到另外一个表(t1)，t2表的字段会覆盖t1表的。
--- </summary>
--- <param name="t1">合并后的表【被改变】</param>
--- <param name="t2">被合并的表【不会改变】</param>
function tableplus.combine(t1, t2)
    if (t2 == nil or type(t2) ~= "table" or t1 == nil or type(t1) ~= "table") then return t1; end
    tableplus.foreach(t2, function(k, v)
        rawset(t1, k, v)
    end )
    return t1;
end

function tableplus.combine2(t1,t2)
    if t1 == nil or t2 == nil or type(t1) ~= 'table' or type(t2) ~= 'table' then
        return {}
    end
    local t3 = {}
    for k,v in pairs(t1) do
        t3[k] = v
    end
    for k,v in pairs(t2) do
        t3[k] = v
    end
    return t3
end

--- 将t1和t2合并，排除相同元素
function tableplus.combine3(t1,t2, compareFunc)
    if t1 == nil or t2 == nil or type(t1) ~= 'table' or type(t2) ~= 'table' then
        return {}
    end
    compareFunc = compareFunc or tableplus.index
    local t3 = {}
    for k,v in ipairs(t1) do
        t3[#t3 + 1] = v
    end
    for k,v in ipairs(t2) do
        if compareFunc( t3, v ) == -1 then
            t3[#t3 + 1] = v
        end
    end
    return t3
end

-- 是否包含某个数值
function tableplus.containNum( t, num )
    for k,v in pairs(t) do
        if v == num then
            return true
        end
    end

    return false
end

-- {a, b, c} --> "a|b|c"
function tableplus.join( t, split )
    local ret = ""
    for i,v in ipairs(t) do
        ret = ret .. v
        if i ~= #t then
           ret = ret ..split
        end
    end
    return ret
end

--返回一个map类型table的长度
function tableplus.mapLength(table)
    if table == nil then return 0 end 
    local size = 0
    for k,v in pairs(table) do 
        size = size + 1 
    end 
    return size 
end 

--复制参数和值;
--t1_: 待复制的table;
--t2_: 目标table;
function tableplus.copyParamValue(t1_, t2_)
    if t1_ == nil then return end
    tableplus.foreach(t1_, function(k, v)
        rawset(t2_, k, v)
    end)
    return t2_
end