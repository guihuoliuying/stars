-- region MotionCalculator.lua
-- Date    : 2015-11-17
-- Author : daiyaorong
-- Description : 运动计算器
-- endregion

MotionCalculator = {}

------------------------------------------------全局--------------------------------------------

-- 计算初始运动信息
-- params:运动所属实体ID 目标ID 运动配置数据 实体运动属性列表
function MotionCalculator.configInitData( entityID, targetID, config, motionInfo )
	-- body
	if config == nil or motionInfo == nil then
		return
	end
	if config.tracktype == nil then
		--CmdLog("=============config.tracktype = nil!!!!!!!!!!")
	end
	local pointType = config.startpositiontype
	local pointValue = config.startposition
	motionInfo.startWorldPoint = PointCalculator.getInitPoint( entityID, targetID, pointType, pointValue, motionInfo, motionInfo.startWorldPoint )

	pointType = config.endpositiontype
	pointValue = config.endposition
	motionInfo.endWorldPoint = PointCalculator.getInitPoint( entityID, targetID, pointType, pointValue, motionInfo, motionInfo.endWorldPoint )
	-- 修正结束位置
	if motionInfo.endWorldPoint and DynamicBlockManager.hasActiveBlock() and motionInfo.bulletId == nil then --子弹无需修正
		motionInfo.endWorldPoint = RoleControllerUtil.rayToObstacles2(motionInfo.startWorldPoint.x,motionInfo.startWorldPoint.y,motionInfo.startWorldPoint.z, motionInfo.endWorldPoint.x,motionInfo.endWorldPoint.y,motionInfo.endWorldPoint.z)
	end
	local initData = TrackCalculator.getTrackData( motionInfo.startWorldPoint, motionInfo.endWorldPoint, config, entityID)
	motionInfo.speedDirec = nil --重置
	for k,v in pairs( initData ) do
		motionInfo[k] = v
	end
	return motionInfo
end