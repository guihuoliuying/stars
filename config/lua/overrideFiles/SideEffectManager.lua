SideEffectManager = {}
function SideEffectManager.getFreeTable() end
function SideEffectManager.poolFreeTable(node) end
function SideEffectManager:playAutoStopEffect( paras ) end
function SideEffectManager:playSideEffect( paras ) end
function SideEffectManager:playAutoEffectInWorldSpace(paras) end
function SideEffectManager:stopSideEffect( serialNo ) end
function SideEffectManager:activeSideEffect(param) end
function SideEffectManager:initEvent( ) end
function SideEffectManager:init( ) end
function SideEffectManager:clear( ) end
function SideEffectManager:addEffectRequest( paras ) end
function SideEffectManager:skillEffectRequest( paras ) end
function SideEffectManager:addAutoEffectRequest( paras ) end
function SideEffectManager:addWorldEffectRequest( paras ) end
function SideEffectManager:addUpdate( func ) end
function SideEffectManager:removeUpdate( funcSN ) end
function SideEffectManager:removeUpdateFunc( ) end
function SideEffectManager:update( ) end
function SideEffectManager:getSerialNo( ) end
function SideEffectManager:resetSerialNo( ) end
function SideEffectManager:resetFuncList( ) end
function SideEffectManager:initDelayList( ) end
function SideEffectManager:addFuncToDelayList( delayTime, delayFunc ) end
function SideEffectManager:clearDelayList( ) end
