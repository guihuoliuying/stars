-----------------------------------------------------
-- region : RunState.lua
-- Date   : 2016.6.14
-- Author ：dinglin
-- Desc   ：角色视图类、让视图与逻辑分离
-- endregion
-----------------------------------------------------
RunState = createClassWithExtends(RunState,StateBase)

local tempVector = Vector3.zero

--激活跑动状态
function RunState:onStateActive()
	self.stateAni   = CharacterConstant.ANIMATOR_MOVE
	self.moveTo     = self.onPosChange     --需要外部调用的方法赋值过来
	self.stopSearchPath = self.stopSearch  --终止寻路

	self.path = nil 			--重置路径
	self.isRunning = false
	self.toRot = nil
	self.toPos = nil
	self.isStopSearch = false
	self.markWaitForNext = false
	self.movement = Vector3.zero
	if self.target == CharacterManager:getMyCharac() then 	--主角是否跑动设置
		CharacterManager:getMyCharac():setCharacterMoveState(true)
	end
end

--跑动状态结束
function RunState:onStateDeative() 	
	self.path = nil 			--重置路径
	self.isRunning = false
	self.toRot = nil
	self.toPos = nil
	self.isWaiting = true
	self.markWaitForNext = false
	PathFinder.removePathFinder(self.target)
	if self.target == CharacterManager:getMyCharac() then 	--主角是否跑动设置
		CharacterManager:getMyCharac():setCharacterMoveState(false)
	end
end

--停止寻路
function RunState:stopSearch()
	if self.isStopSearch == false then
		self:onStateDeative()
		self.isStopSearch = true
	end
end

function RunState:onStateUpdate() 		--状态帧驱动
	if( self.path ~= nil ) then
		if self.isRunning  then 		--移动到路点
			self:loopMoving()
		else 							--下一路点
			self:moveNext()
			--找到下一路点后直接寻路一次，防止点击过快卡顿
			if self.moveLoopCount and self.moveLoopCount > 0 then
				self.target:move( self.movement , not self.ignoreMap)
				self.moveLoopCount = self.moveLoopCount - 1
			end
		end
	elseif self.isWaiting == false then 	--无路径且不等待寻路回调，则认为到目标点了
		self:onArrive()
	end
end

function RunState:loopMoving()
	if( self.moveLoopCount <= 0)then 	--移动到路点
		self.isRunning = false
		self.curIndex = self.curIndex + 1
	else
		local result = self.target:move( self.movement , not self.ignoreMap)
		if result then
			self.moveLoopCount = self.moveLoopCount - 1
		end
		local cutRot = self.target:getRotation()
		if self.toRot and cutRot then
			self.target:setRotation(Quaternion.Slerp(cutRot,self.toRot,0.5))
		end
		if result == false then
			self:stopSearch()
		end
	end
end

function RunState:moveNext()
	if( self.curIndex <= #self.path ) then
		local pathPos = self.path[self.curIndex]
		local tagretPos = self.target:getPosition() 
		tempVector.x = pathPos.x - tagretPos.x	--位移向量
		tempVector.y = pathPos.y - tagretPos.y
		tempVector.z = pathPos.z - tagretPos.z
		local tempMag = tempVector.magnitude
		if( tempMag > 0.2)then 		--向量长度大于
			tempVector:SetNormalize()
			self.movement.x = tempVector.x * self.machine.speed --计算每一帧的位移量
			self.movement.y = tempVector.y * self.machine.speed
			self.movement.z = tempVector.z * self.machine.speed
			self.moveLoopCount = tempMag / self.movement.magnitude --移动帧数
			self.toRot = CharacterUtil.faceToInTween(self.target,self.path[self.curIndex])  --更新面向
			self.isRunning = true
		else 			--距离过小，忽略直接取下一路点
			self.curIndex = self.curIndex + 1
		end
	else 		--到达目的点
		local result = self.target:setPosition( self.path[#self.path] )
		if result == false then
			self:stopSearch()
		else
			self:onArrive()
		end
	end
end

-- 移动速度变化后 重新计算移动信息
function RunState:onSpeedUpdate()
	if self.path then
		if( self.curIndex <= #self.path ) then 
			local pathPos = self.path[self.curIndex]
			local tagretPos = self.target:getPosition() 
			tempVector.x = pathPos.x - tagretPos.x 	--位移向量
			tempVector.y = pathPos.y - tagretPos.y
			tempVector.z = pathPos.z - tagretPos.z
			local tempMag = tempVector.magnitude
			if( tempMag > 0.2)then 		--向量长度大于
				tempVector:SetNormalize()
				self.movement.x = tempVector.x * self.machine.speed 	--计算每一帧的位移量
				self.movement.y = tempVector.y * self.machine.speed
				self.movement.z = tempVector.z * self.machine.speed
				self.moveLoopCount = tempMag / self.movement.magnitude --移动帧数
			end
		end
	end
end

--新的目标点到来
function RunState:onPosChange(param)
	local toPos = param[1]
	self.onArriveCB = param[2]
	self.markWaitForNext = false
	if toPos ~= nil then
		if self.toPos then
			if self.toPos.x == toPos.x and self.toPos.y == toPos.y and self.toPos.z == toPos.z then
				return
			end
			if CharacterUtil.sqrDistanceWithoutY(self.toPos,toPos) < CharacterConstant.SQR_RUN_PRECISION then
				return
			end
		end
		if self.toRot then
			self.target:setRotation(self.toRot)
			self.toRot = nil
		end
		self.toPos = toPos
		self:searchPath()
	end
end

--寻路
function RunState:searchPath()
	local function onPathFindComplete(result) --寻路完毕回调函数
		self:onPathSearched(result)
	end
	if CharacterUtil.sqrDistanceWithoutY(self.target:getPosition(), self.toPos) >= CharacterConstant.SQR_RUN_PRECISION then 	    --调用c#层寻路
		self.isWaiting = true
		PathFinder.luaAddPathFinder(self.target, self.toPos, onPathFindComplete)
		if self.target==CharacterManager:getMyCharac() then
		    self.target:playerBeginMove()
		end
	else
		self:onArrive()
	end
end

--路点找到
function RunState:onPathSearched( path ) --寻路完毕回调，path路点列表
	if self.target.state ~= CharacterConstant.STATE_RUN then return end
	self.path = path
	self.isWaiting = false
	self.isRunning = false
	self.curIndex = 1

	if CharacterUtil.sqrDistanceWithoutY(self.target:getPosition(), path[#path]) <= CharacterConstant.SQR_RUN_PRECISION then
		--寻路距离太短，寻路失败
		self.target.findPathState = CharacterConstant.FINDPATH_STATE_FAIL
		self:onArrive()
	else
		--寻路成功,播放跑步动作
		self.target.findPathState = CharacterConstant.FINDPATH_STATE_SUCCESS
		self:crossFade(self.stateAni)
	end
end

--到达寻路点
function RunState:onArrive()
	if self.toRot then
		self.target:setRotation(self.toRot)
		self.toRot = nil
	end
	if self.onArriveCB then
		self.markWaitForNext = true
		self.onArriveCB()
		if self.markWaitForNext then
			self.onArriveCB = nil
		end
	end
	if self.isWaiting == false then 			--没有新的目的点，则跳回待机状态
		self.machine:stateTransition(CharacterConstant.STATE_IDLE)   --切换到idle状态
	end
	self.toPos = nil
	self.path  = nil 			--重置路径
	self.isRunning = false
	if self.target==CharacterManager:getMyCharac() then
	    self.target:playerStopMove()
	end
end