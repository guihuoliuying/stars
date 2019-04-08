--
-- Created by IntelliJ IDEA.
-- User: Administrator
-- Date: 15-7-21
-- Time: 下午5:54
-- 网络数据流解码模拟器，用于服务端运行lua
--

NetImitateDecodeServer ={
    data = nil,
    readBeginIndex = 1,
    readEndIndex = 0,
	writeBeginIndex = 1,
	dataLength = 0,
	readFuncDic = nil,
}
local DEFAULT_ENDIAN = "big"
local DEFAULT_SIGHED = true
local powerTable = nil
local readFuncDic = nil
local writeFuncDic = nil

local m_floor = math.floor
local m_assert = assert
local m_unpack = unpack
local m_stringlen = string.len
local m_stringsub = string.sub
local m_stringbyte = string.byte
local m_tonumber = tonumber
local m_round = math.round

local eventRecord = nil

local function bytes_to_int(str, endian, signed) -- use length of string to determine 8,16,32,64 bits
    local t = { m_stringbyte(str, 1, -1) }
    local tLen = #t
    if endian == "big" then --reverse bytes
        local tt = {}
        for k = 1, tLen do
            tt[tLen - k + 1] = t[k]
        end
        t = tt
    end
    local n = 0
    for k = 1, tLen do
        n = n + t[k] * powerTable[(k - 1) * 8]
    end
    if signed then
        n = (n > powerTable[tLen * 8-1] - 1) and (n - powerTable[tLen*8]) or n -- if last bit set, negative.
    end
    return n
end

local function int_to_bytes(num, endian, signed, byteNum)
	if num == nil or type(num) ~= "number" then
		EnvironmentHandler.sendLogToServer("int_to_bytes:" .. debug.traceback())
		num = 0
	end
	num = m_floor(num)
    if num < 0 and not signed then 
        num = -num 
        print"warning, dropping sign from number converting to unsigned" 
    end
    local res = {}
    local n = byteNum --math.ceil(select(2,math.frexp(num))/8) -- number of bytes to be used.
    if signed and num < 0 then
        num = num + powerTable[n * 8]
    end
    local mul = nil
    for k = n, 1, -1 do -- 256 = 2^8 bits per char.
        mul = powerTable[8*(k-1)]
        res[k] = m_floor(num/mul)
        num = num - res[k] * mul
    end
    m_assert(num==0)
    if endian == "big" then
        local t = {}
        for k = 1, n do
            t[k] = res[n-k+1]
        end
        res = t
    end
    return m_unpack(res)
end

function NetImitateDecodeServer:create(data)
    local obj = {}
    setmetatable(obj, self)
    self.__index = self
    self.data = data
	self:refreshDataLength()
    return obj
end

function NetImitateDecodeServer:reflesh(data)
    self.__index = self
    self.data = data
	self:refreshDataLength()
    self.readBeginIndex = 1
    self.readEndIndex = 0
    return self
end

function NetImitateDecodeServer:refreshDataLength()
	if self.data == nil then
		self.dataLength = 0
	else
		self.dataLength = m_stringlen(self.data)
	end
end

-- 是否还有可读取的内容
function NetImitateDecodeServer:hasMoreData()
	return self.dataLength > self.readBeginIndex
end

function NetImitateDecodeServer:initBuffer()
    self.__index = self
	self.buffer = {}
	self.writeBeginIndex = 1
    return self
end

function NetImitateDecodeServer:getSubStr(length)
    self.readEndIndex = self.readEndIndex+length
    local str = m_stringsub(self.data,self.readBeginIndex,self.readEndIndex)
    self.readBeginIndex = self.readBeginIndex+length
    return str
end

function NetImitateDecodeServer:ReadSbyte()
    -- body
    return m_tonumber(bytes_to_int(self:getSubStr(ConstantData.ByteValueLen),DEFAULT_ENDIAN,DEFAULT_SIGHED))
end

function NetImitateDecodeServer:ReadUnsignedSbyte()
    -- body
    return m_tonumber(bytes_to_int(self:getSubStr(ConstantData.ByteValueLen),DEFAULT_ENDIAN,false))
end

function NetImitateDecodeServer:ReadShort()
    -- body
    return m_tonumber(bytes_to_int(self:getSubStr(ConstantData.ShortValueLen),DEFAULT_ENDIAN,DEFAULT_SIGHED))
end

function NetImitateDecodeServer:ReadInt()
    -- body
    return m_tonumber(bytes_to_int(self:getSubStr(ConstantData.IntValueLen),DEFAULT_ENDIAN,DEFAULT_SIGHED))
end

function NetImitateDecodeServer:ReadFloat()
	local num = self:ReadShort()
	num = num * 0.1
	return num
end

function NetImitateDecodeServer:ReadString()
    -- body
    local strlength = self:ReadShort()
    return self:getSubStr(strlength)
end

function NetImitateDecodeServer:ReadProtocal()
    return self:ReadInt()
end

function NetImitateDecodeServer:ReadByType(t)
	return readFuncDic[t](self)
end

function NetImitateDecodeServer:WriteSbyte(data)
    self.buffer[self.writeBeginIndex] = int_to_bytes(data, DEFAULT_ENDIAN, DEFAULT_SIGHED, ConstantData.ByteValueLen)
    self.writeBeginIndex = self.writeBeginIndex + ConstantData.ByteValueLen
end

function NetImitateDecodeServer:WriteUnsignedSbyte(data)
    self.buffer[self.writeBeginIndex] = int_to_bytes(data, DEFAULT_ENDIAN, false, ConstantData.ByteValueLen)
    self.writeBeginIndex = self.writeBeginIndex + ConstantData.ByteValueLen
end

function NetImitateDecodeServer:WriteShort(data)
    local byte1, byte2 = int_to_bytes(data, DEFAULT_ENDIAN, DEFAULT_SIGHED, ConstantData.ShortValueLen)
	self.buffer[self.writeBeginIndex] = byte1
	self.buffer[self.writeBeginIndex + 1] = byte2
    self.writeBeginIndex = self.writeBeginIndex + ConstantData.ShortValueLen
end

function NetImitateDecodeServer:WriteInt(data)
    local byte1, byte2, byte3, byte4 = int_to_bytes(data, DEFAULT_ENDIAN, DEFAULT_SIGHED, ConstantData.IntValueLen)
	self.buffer[self.writeBeginIndex] = byte1
	self.buffer[self.writeBeginIndex + 1] = byte2  
	self.buffer[self.writeBeginIndex + 2] = byte3
	self.buffer[self.writeBeginIndex + 3] = byte4
    self.writeBeginIndex = self.writeBeginIndex + ConstantData.IntValueLen
end

function NetImitateDecodeServer:WriteFloat(data)
	data = m_round(data, 1) * 10
	self:WriteShort(data)
end

function NetImitateDecodeServer:WriteString(data)
    if data == nil then
        EnvironmentHandler.sendLogToServer("NetImitateDecodeServer WriteString false ==  "..tostring(eventRecord))
        return;
    end
    local len = m_stringlen(data)
    self:WriteShort(len)
	local bytes = {m_stringbyte(data, 1, -1)}
	for i = 1, len do
		self.buffer[self.writeBeginIndex] = bytes[i]
		self.writeBeginIndex = self.writeBeginIndex + 1
	end
end

function NetImitateDecodeServer:WriteProtocol(eventID)
    eventRecord = eventID
    self:WriteInt(eventID)
end

function NetImitateDecodeServer:WriteByType(t, data)
	writeFuncDic[t](self, data)
end

function NetImitateDecodeServer:GetPack()
    local tempBuffer = self.buffer
	self.buffer = nil
	return tempBuffer
end

--function NetImitateDecodeServer:test()
--    self:WriteSbyte(-13)
--    self:WriteShort(-1024)
--    self:WriteInt(-4010011)
--    self:WriteString("-4546556412345")
--    self:WriteString("hello world")
--    self.buffer = table.concat(self.buffer)
--    self:test2(self.buffer)
--end

--function NetImitateDecodeServer:test2(data)
--    self:reflesh(data)
--    --print(self:ReadSbyte())
--    --print(self:ReadShort())
--    --print(self:ReadInt())
--    --print(self:ReadString())
--    --print(self:ReadString())
--end

do
    powerTable = {}
    for i = 1, 32 do
        powerTable[i] = 2 ^ i
    end
    powerTable[0] = 1

	readFuncDic = 
	{
		[ConstantData.LUA_TYPE_BYTE] = NetImitateDecodeServer.ReadSbyte,
		[ConstantData.LUA_TYPE_SHORT] = NetImitateDecodeServer.ReadShort,
		[ConstantData.LUA_TYPE_INT] = NetImitateDecodeServer.ReadInt,
		[ConstantData.LUA_TYPE_FLOAT] = NetImitateDecodeServer.ReadFloat,
		[ConstantData.LUA_TYPE_STRING] = NetImitateDecodeServer.ReadString,
        [ConstantData.LUA_TYPE_BYTE_UNSIGNED] = NetImitateDecodeServer.ReadUnsignedSbyte,
	}
	 writeFuncDic = 
	{
		[ConstantData.LUA_TYPE_BYTE] = NetImitateDecodeServer.WriteSbyte,
		[ConstantData.LUA_TYPE_SHORT] = NetImitateDecodeServer.WriteShort,
		[ConstantData.LUA_TYPE_INT] = NetImitateDecodeServer.WriteInt,
		[ConstantData.LUA_TYPE_FLOAT] = NetImitateDecodeServer.WriteFloat,
		[ConstantData.LUA_TYPE_STRING] = NetImitateDecodeServer.WriteString,
        [ConstantData.LUA_TYPE_BYTE_UNSIGNED] = NetImitateDecodeServer.WriteUnsignedSbyte,
	}
end