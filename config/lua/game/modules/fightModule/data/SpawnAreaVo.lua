--region SpawnAreaVo.lua
--Date 2016/07/06
--Author zhouxiaogang
-- 刷怪区域配置
--endregion

SpawnAreaVo = 
{
	spawnid = nil,		-- 唯一ID
	position = nil,		-- 区域中心点位置
	radius = nil,		-- 区域半径
	sqrradius = nil,	-- 区域半径的平方,客户端用于判断是否进入区域
}

createClass(SpawnAreaVo)

local function parseArea(areaStr)
	if areaStr == nil or areaStr == "" then
		return nil
	end
	local areaArr = StringUtils.split(areaStr, "+")
	local result = {}
	result.areaType = tonumber(areaArr[1]) or 0
	local x = tonumber(areaArr[2]) or 0
	local z = tonumber(areaArr[3]) or 0
	result.position = Vector3.New(x * 0.1, 0, z * 0.1)
	if result.areaType == FightDefine.SPAWNAREA_CIRCLE then
		result.radius = tonumber(areaArr[4]) or 0
		result.radius = result.radius * 0.1 + CharacterConstant.RUN_PRECISION
		result.sqrRadius = result.radius * result.radius
	else
		result.rotationY = tonumber(areaArr[4]) or 0
		local sizeX = tonumber(areaArr[5]) or 0
		local sizeZ = tonumber(areaArr[6]) or 0
		result.size = Vector2.New(sizeX * 0.1, sizeZ * 0.1)
		result.bounds = math.getRectBounds(result.position, result.size, result.rotationY)
	end
	return result
end

function SpawnAreaVo:read(conn)
	self.spawnid = conn:ReadInt()
	self.areaCode = conn:ReadString()
	self.area = parseArea(self.areaCode)
end

function SpawnAreaVo:write(conn)
	conn:WriteInt(self.spawnid)
	conn:WriteString(self.areaCode)
end

SpawnAreaVo.__tostring = function(self)
	return "SpawnAreaVo:" .. tableplus.formatstring(self,true)
end