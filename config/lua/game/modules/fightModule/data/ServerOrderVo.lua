-- region
-- Date    : 2016-12-01
-- Author  : daiyaorong
-- Description :  服务端指令
-- endregion

ServerOrderVo = {
	ordertype = nil,	--指令类型
	campid = nil,		--阵营id
	buffid = nil,		--buff配置id
	level = nil,		--等级
	instanceid = nil,	--实例id
}

createClass(ServerOrderVo)

function ServerOrderVo:read( conn )
	self.ordertype = conn:ReadSbyte()
	self:readByType(conn)
end

-- 阵营增加buff
local function AddCampBuff( vo, conn )
	vo.campid = conn:ReadSbyte()
	vo.charactertype = conn:ReadSbyte()
	local size = conn:ReadSbyte()
	vo.characIdList = {}
	for i = 1, size do
		vo.characIdList[i] = conn:ReadString()
	end
	vo.buffid = conn:ReadInt()
	vo.level = conn:ReadInt()
	vo.instanceid = tostring(conn:ReadInt())
end

-- 阵营移除buff
local function RemoveCampBuff( vo, conn )
	vo.campid = conn:ReadSbyte()
	local size = conn:ReadSbyte()
	vo.characIdList = {}
	for i = 1, size do
		vo.characIdList[i] = conn:ReadString()
	end
	vo.instanceid = tostring(conn:ReadInt())
end

-- 移动指定的角色
local function MoveCharacter(vo, conn)
	vo.uniqueID = conn:ReadString()
	vo.pos = Vector3.NewByStr(conn:ReadString())
	vo.pos = vo.pos * 0.1
end

-- 停止移动指定的角色
local function StopMoveCharacter(vo, conn)
	vo.uniqueID = conn:ReadString()
end

-- 更新战斗状态
local function UpdateFightState(vo, conn)
	vo.fightState = conn:ReadSbyte()
	vo.duration = conn:ReadInt()  -- 0 代表不限时
end

-- 重置角色状态（恢复）
local function ResetCharacState(vo, conn)
	local size = conn:ReadSbyte()
	vo.characIdList = {}
	for i = 1, size do
		vo.characIdList[i] = conn:ReadString()
	end
end

local function AddCharacBuff(vo, conn)
	local size = conn:ReadSbyte()
	vo.characIdList = {}
	for i = 1, size do
		vo.characIdList[i] = conn:ReadString()
	end
	vo.buffid = conn:ReadInt()
	vo.level = conn:ReadInt()
	vo.instanceid = tostring(conn:ReadInt())
end

local function SetCharacterAI(vo, conn)
	-- body
	local size = conn:ReadSbyte()
	vo.characIdList = {}
	for i = 1, size do
		vo.characIdList[i] = conn:ReadString()
	end
	vo.aiState = conn:ReadSbyte()
end

local ParseList = {
	[FightDefine.SORDER_ADDCAMPBUFF] = AddCampBuff,
	[FightDefine.SORDER_REMOVECAMPBUFF] = RemoveCampBuff,
	[FightDefine.SORDER_CHANGE_FIGHTSTATE] = UpdateFightState,
	[FightDefine.SORDER_MOVECHARAC] = MoveCharacter,
	[FightDefine.SORDER_RESET_CHARACS] = ResetCharacState,
	[FightDefine.SORDER_STOPCHARAC] = StopMoveCharacter,
	[FightDefine.SORDER_ADD_BUFF] = AddCharacBuff,
	[FightDefine.SORDER_SETAI] = SetCharacterAI,
}

function ServerOrderVo:readByType(conn)
	ParseList[self.ordertype](self,conn)
end