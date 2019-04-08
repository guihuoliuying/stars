--region DynamicBlockVo.lua
--Date 2016/07/06
--Author zhouxiaogang
-- 动态阻挡数据
--endregion

DynamicBlockVo = 
{
	blockid = nil,			-- 阻挡ID，唯一
	position = nil,			-- 动态阻挡位置
	rotation = nil,			-- 动态阻挡Y轴旋转
	state = nil,			-- 状态 0 关， 1 开
}

createClass(DynamicBlockVo)

function DynamicBlockVo:read(conn)
	self.blockid = conn:ReadString()
	local x = conn:ReadInt() * 0.1
	local y = conn:ReadInt() * 0.1
	local z = conn:ReadInt() * 0.1
	self.position = Vector3.New(x, y, z)
	self.rotation = conn:ReadInt()
	self.resourceName = conn:ReadString()
	self.sizeX = conn:ReadInt() * 0.1
	self.sizeY = conn:ReadInt() * 0.1
	self.state = conn:ReadSbyte()
end

function DynamicBlockVo:write(conn)
	conn:WriteString(self.blockid)
	conn:WriteInt(self.position.x * 10)
	conn:WriteInt(self.position.y * 10)
	conn:WriteInt(self.position.z * 10)
	conn:WriteInt(self.rotation)
	conn:WriteString(self.resourceName)
	conn:WriteInt(self.sizeX * 10)
	conn:WriteInt(self.sizeY * 10)
	conn:WriteSbyte(self.state)
end

DynamicBlockVo.__tostring = function(self)
	local str = "DynamicBlockVo[" .. self.blockid .. "]={";
	for k,v in pairs(self) do
		str = str .. k .. "=" .. tostring(v) .. ","
	end
	str = str .. "}"
	return str
end