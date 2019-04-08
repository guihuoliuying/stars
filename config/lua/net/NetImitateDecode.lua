--
-- Created by IntelliJ IDEA.
-- User: Administrator
-- Date: 15-7-21
-- Time: 下午5:54
-- 网络数据流解码模拟器，用于服务端运行lua
--

NetImitateDecode ={
    data = nil,
    beginIndex = 1,
    endIndex = 0,
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
local m_stringchar = string.char
local m_tonumber = tonumber
local m_concat = table.concat
local m_round = math.round

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
    return m_stringchar(m_unpack(res))
end

function NetImitateDecode:create(data)
    local obj = {}
    setmetatable(obj, self)
    self.__index = self
    self.data = data
	self:refreshDataLength()
    return obj
end

function NetImitateDecode:reflesh(data)
    self.__index = self
    self.data = data
	self:refreshDataLength()
    self.beginIndex = 1
    self.endIndex = 0
    return self
end

function NetImitateDecode:refreshDataLength()
	if self.data == nil then
		self.dataLength = 0
	else
		self.dataLength = m_stringlen(self.data)
	end
end

-- 是否还有可读取的内容
function NetImitateDecode:hasMoreData()
	return self.dataLength > self.beginIndex
end

function NetImitateDecode:initBuffer()
    self.__index = self
	self.buffer = {}
	self.beginIndex = 1
    return self
end

function NetImitateDecode:getSubStr(length)
    self.endIndex = self.endIndex+length
    local str = m_stringsub(self.data,self.beginIndex,self.endIndex)
    self.beginIndex = self.beginIndex+length
    return str
end

function NetImitateDecode:ReadSbyte()
    -- body
    return m_tonumber(bytes_to_int(self:getSubStr(ConstantData.ByteValueLen),DEFAULT_ENDIAN,DEFAULT_SIGHED))
end

function NetImitateDecode:ReadShort()
    -- body
    return m_tonumber(bytes_to_int(self:getSubStr(ConstantData.ShortValueLen),DEFAULT_ENDIAN,DEFAULT_SIGHED))
end

function NetImitateDecode:ReadInt()
    -- body
    return m_tonumber(bytes_to_int(self:getSubStr(ConstantData.IntValueLen),DEFAULT_ENDIAN,DEFAULT_SIGHED))
end

function NetImitateDecode:ReadFloat()
	local num = self:ReadInt()
	num = num * 0.01
	return num
end

function NetImitateDecode:ReadString()
    -- body
    local strlength = self:ReadShort()
    return self:getSubStr(strlength)
end

function NetImitateDecode:ReadProtocal()
    return self:ReadInt()
end

function NetImitateDecode:ReadByType(t)
	return readFuncDic[t](self)
end

function NetImitateDecode:WriteSbyte(data)
    self.buffer[self.beginIndex] = int_to_bytes(data, DEFAULT_ENDIAN, DEFAULT_SIGHED, ConstantData.ByteValueLen)
    self.beginIndex = self.beginIndex + 1
end

function NetImitateDecode:WriteShort(data)
    self.buffer[self.beginIndex] = int_to_bytes(data, DEFAULT_ENDIAN, DEFAULT_SIGHED, ConstantData.ShortValueLen)
    self.beginIndex = self.beginIndex + 1
end

function NetImitateDecode:WriteInt(data)
    self.buffer[self.beginIndex] = int_to_bytes(data, DEFAULT_ENDIAN, DEFAULT_SIGHED, ConstantData.IntValueLen)
    self.beginIndex = self.beginIndex + 1
end

function NetImitateDecode:WriteFloat(data)
	data = m_round(data, 2) * 100
	self:WriteInt(data)
end

function NetImitateDecode:WriteString(data)
    local len = m_stringlen(data)
    self:WriteShort(len)
	self.buffer[self.beginIndex] = data
	self.beginIndex = self.beginIndex + 1
end

function NetImitateDecode:WriteProtocol(eventID)
    self:WriteInt(eventID)
end

function NetImitateDecode:WriteByType(t, data)
	writeFuncDic[t](self, data)
end

function NetImitateDecode:GetPack()
    return m_concat(self.buffer)
end

--function NetImitateDecode:test()
--    self:WriteSbyte(-13)
--    self:WriteShort(-1024)
--    self:WriteInt(-4010011)
--    self:WriteString("-4546556412345")
--    self:WriteString("hello world")
--    self.buffer = table.concat(self.buffer)
--    self:test2(self.buffer)
--end

--function NetImitateDecode:test2(data)
--    self:reflesh(data)
--    print(self:ReadSbyte())
--    print(self:ReadShort())
--    print(self:ReadInt())
--    print(self:ReadString())
--    print(self:ReadString())
--end

do
    powerTable = {}
    for i = 1, 32 do
        powerTable[i] = 2 ^ i
    end
    powerTable[0] = 1

	readFuncDic = 
	{
		[ConstantData.LUA_TYPE_BYTE] = NetImitateDecode.ReadSbyte,
		[ConstantData.LUA_TYPE_SHORT] = NetImitateDecode.ReadShort,
		[ConstantData.LUA_TYPE_INT] = NetImitateDecode.ReadInt,
		[ConstantData.LUA_TYPE_FLOAT] = NetImitateDecode.ReadFloat,
		[ConstantData.LUA_TYPE_STRING] = NetImitateDecode.ReadString,
	}
	 writeFuncDic = 
	{
		[ConstantData.LUA_TYPE_BYTE] = NetImitateDecode.WriteSbyte,
		[ConstantData.LUA_TYPE_SHORT] = NetImitateDecode.WriteShort,
		[ConstantData.LUA_TYPE_INT] = NetImitateDecode.WriteInt,
		[ConstantData.LUA_TYPE_FLOAT] = NetImitateDecode.WriteFloat,
		[ConstantData.LUA_TYPE_STRING] = NetImitateDecode.WriteString,
	}
end