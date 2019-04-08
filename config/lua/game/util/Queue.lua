
Queue = {}          --队列

function Queue:new()           --新建队列
    local o =  {first = 1,last = 0 }
    setmetatable(o,self)
    self.__index = self
    return o
end

function Queue:pushFirst(value)
    self.first = self.first - 1
    self[self.first] = value
end

--插入队列尾部
function Queue:add(value)
    self.last = self.last + 1
    self[self.last] = value
end

-- 基本队列操作，与pop对应，与add的功能一样
function Queue:push(value)
    self.last = self.last + 1
    self[self.last] = value
end

--抛出队头元素
function Queue:pop()
    if self.first > self.last then return nil end
    local value = self[self.first]
    self[self.first] = nil
    self.first = self.first +1
    return value
end

function Queue:popLast() 
    if self.first > self.last then return nil end
    local value = self[self.last]
    self[self.last] = nil
    self.last = self.last -1
    return value
end

function Queue:clear()
    for i = self.first ,self.last do
        self[i] = nil
    end
    self.first = 0
    self.last = -1
end

function Queue:getLast()
    return self[self.last]
end

function Queue:getFirst()
    return self[self.first]
end

function Queue:getSize()
    return  self.last-self.first +1
end

function Queue:getVal( index )
    return self[index]
end

function Queue:getHeadIndex()
    -- body
    return self.first
end

function Queue:getLastIndex()
    -- body
    return self.last
end