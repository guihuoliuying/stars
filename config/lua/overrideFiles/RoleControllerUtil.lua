RoleControllerUtil = {}
function RoleControllerUtil.New() end
function RoleControllerUtil.getHitPosByPoint(pos_, layerIndex_, cam_) end
function RoleControllerUtil.samplePosition(pos_, distance_) end
function RoleControllerUtil.findPath(sPos_, tPos_, callBack_) end
function RoleControllerUtil.canMoveByPoint(pos_, navMeshAgent_) end
function RoleControllerUtil.getWalkablePoint(sPos_, tPos_) end
function RoleControllerUtil.twoPointDistance(pos1_, pos2_) end
function RoleControllerUtil.getRayByMousePosition() end
function RoleControllerUtil.worldToScreenPoint(pos_) end
function RoleControllerUtil.worldToUIPoint(x_, y_, z_, resultX_, resultY_) end
function RoleControllerUtil.worldToUIOrginPoint(x_, y_, z_, resultX_, resultY_) end
function RoleControllerUtil.isRayThing(hit_, ray_) end
function RoleControllerUtil.getWorldPointByMousePostion() end
function RoleControllerUtil.getWorldPointBySceneCenter() end
function RoleControllerUtil.getWorldPointByScenePoint(scenePoint_) end
function RoleControllerUtil.getTransformByScenePoint(scenePoint_) end
function RoleControllerUtil.getTransformByMousePostion() end
function RoleControllerUtil.getTransformByMousePostionByLayer(layerMask_) end
function RoleControllerUtil.CollectActorsOnCameraRay(rayOrigin_) end
function RoleControllerUtil.getTransformByNameAtMousePosition(thingName_) end
function RoleControllerUtil.getCapsuleColliderArray(array_) end
function RoleControllerUtil.getRayColliderPosByLayer(targetPos_, dir_, layerMask_) end
function RoleControllerUtil.rayToObstacles(targetPos_, dir_) end
function RoleControllerUtil.rayToObstacles2(fromX_, fromY_, fromZ_, toX_, toY_, toZ_) end
function RoleControllerUtil:ToString() end
function RoleControllerUtil:Equals(obj) end
function RoleControllerUtil:GetHashCode() end
function RoleControllerUtil:GetType() end
