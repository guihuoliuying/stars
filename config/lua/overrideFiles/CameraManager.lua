CameraManager = {}
function CameraManager.setCameraBlackWhiteEnable(value, grayValue, rGrayValue, gGrayValue, bGrayValue, brightValue, saturationValue, compareValue) end
function CameraManager.setCameraEulerNeedRecordToLocal(value) end
function CameraManager.getCameraEulerRecordLocalValue() end
function CameraManager.clearUserData() end
function CameraManager.setCameaCtrlConfigParam(backwardDistance, eulerY, eulerX, offsetH, eulerMinX, eulerMaxX) end
function CameraManager:init() end
function CameraManager:getMainGoLocalPosition() end
function CameraManager:getMainGoLocalEuler() end
function CameraManager:setMainGoLocalPosition(locPos) end
function CameraManager:setMainGoLocalEuler(locEuler) end
function CameraManager.register() end
function CameraManager:setFieldView( value ) end
function CameraManager:resetFieldView() end
function CameraManager:initBloom() end
function CameraManager:disposeBloom() end
function CameraManager:setBloomOpen(isOpen) end
function CameraManager:setBloomEnable(enabled_) end
function CameraManager:initShadow() end
function CameraManager:loginWindowShader(value) end
function CameraManager:reInitShadow() end
function CameraManager:disposeShadow() end
function CameraManager:setLayerDistance(isAllCanSee) end
function CameraManager:setPosOff( offset ) end
function CameraManager:changeShadowSize() end
function CameraManager:updateShadowRotation(mapId,mainCharacterPos) end
function CameraManager:setShadowVisible(value) end
function CameraManager:getCameraTrans() end
function CameraManager:setRadialBlurEffect(blurPowerStartValue_, blurPowerEndValue_, duration_, completeFunc_) end
function CameraManager:slowAction(targetTimeScaleValue_, frag1Time_, frag2Time_, frag3Time_) end
function CameraManager.setIsCanDrag(value) end
function CameraManager:setTargetTransform(value_) end
function CameraManager:setCameraCtrlStop() end
function CameraManager:setCameraCtrlContinue() end
function CameraManager:initEvent() end
function CameraManager:setPosition(pos_, isDispatchMsg_) end
function CameraManager:setRotation(dir_) end
function CameraManager:getRotation() end
function CameraManager:getEulerAngles() end
function CameraManager:getPosByDirection(dir_) end
function CameraManager:getPosition() end
function CameraManager:LookAt(pos) end
function CameraManager:setActive(isActive_) end
function CameraManager:shakeCam( typeID ) end
function CameraManager:killPosMoveToEndTween() end
function CameraManager:killRotationMoveToEndTween() end
function CameraManager:setPosMoveToEnd(startPos_, endPos_, time_, isDispatchMsg_) end
function CameraManager:setRotationMoveToEnd(startRotation_, endRotation_, time_) end
function CameraManager.changeChangedOffsetPosByForwardDistance(startDistance_, targetDistance_, duration_, completeFunc_) end
function CameraManager.setCustomTweenMove(lookTargetTrans_, time_, backwardDistance, eulerY, eulerX, offsetH, eulerMinX, eulerMaxX) end
function CameraManager:dispose() end
