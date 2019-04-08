-- Attribute
--除了战斗系统以外其他系统显示属性的对象
Attribute = {}

createClass(Attribute)

local powerRatios = nil

function Attribute:read(conn)
	-- 默认值
	for index = 1, ATTR_COUNT do
		self[AttrEnum[index]] = 0
	end
	for index = 1, EXTATTR_COUNT do
		self[EXT_ATTR[index]] = 0
	end
	-- 具体值
	local size = conn:ReadSbyte()
	local key = nil
	for index = 1, size do
		key = conn:ReadShort()+1 --服务端从0开始
		if key <= ATTR_COUNT then
			-- 主属性
			self[AttrEnum[key]]            = conn:ReadInt()
		else
			-- 扩展属性
			self[EXT_ATTR[key-ATTR_COUNT]] = conn:ReadInt()
		end
	end
end 

function Attribute:readConfig(configStr,split1,split2)
	if configStr==nil or configStr=="0" then
		return
	end
    
    split1 = split1 or ","
    split2 = split2 or "="
    -- 清空上一次的数据
    self:clear()

    local attrValue = nil
	local attr = string.split(configStr,split1)
    for i=1,table.getn(attr) do
        attrValue = StringUtils.split(attr[i],split2)
        self[attrValue[1]] = tonumber(attrValue[2])
    end
end

function Attribute:getFightScore()
    return Attribute.calcFightScore(self)
end

function Attribute.calcFightScore(attr)
	if powerRatios == nil then
		powerRatios = CFG.commondefine:getValue("battlepowerratio", function(val) 
			local arr = StringUtils.split(val, "|")
			local result = {}
			local tempRatio = nil
			for k,v in ipairs(arr) do
				tempRatio = StringUtils.split(v, "=")
				result[tempRatio[1]] = tonumber(tempRatio[2]) or 0
			end
			return result
		end)
	end
	if powerRatios == nil then return 0 end
	local totalFightScore = 0
	for attribKey, ratio in pairs(powerRatios) do
		if attr[attribKey] then
			totalFightScore = totalFightScore + math.ceil(attr[attribKey] * ratio)
		end
	end
	return totalFightScore
end

function Attribute:addOther(attrib)
	for k,v in pairs(attrib) do
		if self[k] then
			self[k] = self[k] + v
		else
			self[k] = v
		end
	end
end

function Attribute:clear()
	for k,v in pairs(self) do
		self[k] = 0
	end
end

function Attribute:clone()
	local attrib = Attribute:create()
	for k,v in pairs(self) do
		attrib[k] = v
	end
	return attrib
end

-- 取属性名展示在UI上的名称
function Attribute.getAttribDisplayName(attribKey)
	return CFG.gametext:getFormatText(attribKey .. "name")
end

Attribute.__tostring = function(self)
	local msg = "Attribute:{\n"
	for key,val in pairs(self) do
		msg = msg .. key .. "=" .. val .. ",\n"
	end
	msg = msg .. "}"
	return msg
end