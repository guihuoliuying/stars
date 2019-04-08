--
-- Created by IntelliJ IDEA.
-- User: Simon
-- Date: 15-8-7
-- Time: 下午5:39
-- To change this template use File | Settings | File Templates.
--用于重写覆盖Lua层不存在的对象，避免执行报错

function NewTable(self,o)
    o = o or {}
    setmetatable(o, self)
    self.__index = self
    return o
end


--网络通信
local decoder = nil
function GameNet.GetSocket()
    -- body
    if decoder == nil then
        decoder = NetImitateDecodeServer:create(data)
    end
    return decoder
end

function GameNet.GetServerTime()
	return os.time() * 1000;
end

function RoleControllerUtil.rayToObstacles2(x,y,z, posX,posY,posZ)
    if FightModel:getFightStageType() == ConstantData.STAGE_TYPE_FAMILY_WAR_ELITE_FIGHT 
        and FightServerControl.getFightState() == FightDefine.FIGHT_STATE_READY then
        local tempx1 = posX - FightDefine.FAMILYWAR_CIRCLE_A.x
        local tempz1 = posZ - FightDefine.FAMILYWAR_CIRCLE_A.z
        local dis1 = math.sqrt(tempx1 * tempx1 + tempz1 * tempz1)

        local tempx2 = posX - FightDefine.FAMILYWAR_CIRCLE_B.x
        local tempz2 = posZ - FightDefine.FAMILYWAR_CIRCLE_B.z
        local dis2 = math.sqrt(tempx2 * tempx2 + tempz2 * tempz2)
        if dis1 > FightDefine.FAMILYWAR_R and dis2 > FightDefine.FAMILYWAR_R then
            -- 距离两边原点都超出范围了则使用旧坐标
            return Vector3(x,y,z)
        end
    end

    local isObstacle = 0
    isObstacle = NavigationUtil.checkObstacles(posX,posY,posZ,isObstacle)
    if isObstacle == 1 then
        return Vector3(x,y,z)
    end

	return Vector3(posX,posY,posZ)
end

--点击获取信息
UnityEngine={}
function UnityEngine.CapsuleCollider()
    return {}
end

--资源加载
Resources ={}
function Resources.Load()
    return {}
end

WindowManager ={}
function WindowManager.removeWinOnChangeStage () end
function module(...)end

GameUtil={}
GameUtil.mainCamera ={}
function GameUtil.mainCamera:GetComponent(...)
        return {}
end
GameUtil.mainCamera.transform={}
function GameUtil.mainCamera.transform:GetComponent(...)
    return {}
end

function IsNil(uobj)
    return uobj == nil
end

-- LuaHelper={}
-- LuaHelper = {}
function LuaHelper.IsInRangeByTypeRectangle(pos, forwardStartPos, forwardEndPos, backStartPos, backEndPos)
    --print("--------!!!!!!!")
    local tmpValue = false;
    tmpValue = ((pos.x >= forwardStartPos.x) and (pos.x <= backEndPos.x)) and ((pos.z >= forwardStartPos.z) and (pos.z <= backEndPos.z))
    if (tmpValue == true) then
        return true
    end
    tmpValue = ((pos.x >= backStartPos.x) and (pos.x <= forwardEndPos.x)) and ((pos.z >= backStartPos.z) and (pos.z <= forwardEndPos.z));
    if (tmpValue == true) then
        return true
    end

    return false;
end

MainPanel={}
MainPanel.thingsTemp ={}


-- ThingData
ThingData = {
    id = 0,
    resourceId = nil,
    nickName = nil,
    kind = nil,
    px = 0,
    py = 0,
    pz = 0,
    dir = 0,
    dialog = nil,
    layerName = nil,
}
function ThingData:new( o )
    o = o or {}
    setmetatable(o, self)
    self.__index = self

    return o
end

function ThingData.New( )
    local data = ThingData:new()

    return data
end
-- ThingData

-- ThingPlayer
ThingPlayer = {
    data = nil, -- ThingData
    -- _trans = nil, -- Transform
    -- _currentPos = nil, -- Vector3
    avatar = nil, --GameObject
    luaLoadCallBack = nil,
    direction = nil,
}
function ThingPlayer:new( o )
    o = o or {}
    setmetatable(o, self)
    self.__index = self

    return o
end

function ThingPlayer.New( )
    local thing = ThingPlayer:new()
    thing.avatar = GameObject.New()

    return thing
end

function ThingPlayer:initAvator( )
    self.avatar.name = ""..self.data.kind..self.data.id
    --GameLog("pete3",self.data.dir.."++++++++++++++++++++++++"..self.avatar.name)
    self.direction = self.data.dir
    self.avatar.transform.rotation = Quaternion.Euler (0, self.data.dir, 0)
    if self.luaLoadCallBack then
        self.luaLoadCallBack()
    end
end

function ThingPlayer:setLayerNameInt(nlayerName )
    
end

function ThingPlayer:CrossFade( stateHash, transitionDuration )
    
end

function ThingPlayer:setActive( isActive )
    
end

function ThingPlayer:setScale( scale )
    
end

function ThingPlayer:getBonePoint( boneName )
    return self.avatar.transform
end

function ThingPlayer:init( )
    self:initAvator()
end

-- ThingPlayer




-- Time
Time = {
    time = 0,
    -- deltaTime = ConstantData.FRAME_DELTA_TIME,
    -- fixedDeltaTime = ConstantData.FRAME_DELTA_TIME,

    deltaTime = 0.033,
    fixedDeltaTime = 0.033,
    realtimeSinceStartup = 0,
}

function Time.tick()
    if FrameTimeLua.isRunning( ) then
        Time.time = Time.time + Time.deltaTime
        Time.realtimeSinceStartup = Time.realtimeSinceStartup + Time.deltaTime
    else
        if funcKey ~= nil then
            FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUASCENE):removeLuaFunc(funcKey)
        end
    end
end
-- Time


FrameTimeLua ={}
local fighting = true --标志是否处于战斗中
local maxRunTime = 30*60*10 --最大的循环次数 = 一秒30帧*60秒*30分钟
--改变战斗状态
function FrameTimeLua.setFighting(fight)
    fighting = fight
end

function FrameTimeLua.isRunning( )
    return fighting
end

--帧驱动逻辑
function FrameTimeLua.run()
    --print('maxRunTime---------- '..maxRunTime)
    for i =1,maxRunTime do
        if not fighting then
            break
        end
        FrameTimerManager.frameHandle()
        Time.tick()
    end
end


NavigationUtil = {}

local hasBlock = nil
local dynamicBlockList = nil
local MATH_ABS = math.abs
-- 主要是家族战服务端拿来防止准备阶段超出范围
function NavigationUtil.checkObstacles(x,y,z,result)
    if (FightModel:getFightStageType() == ConstantData.STAGE_TYPE_FAMILY_WAR_ELITE_FIGHT or FightModel:getFightStageType() == ConstantData.STAGE_TYPE_DAILY5V5_PVP) then
        if FightServerControl.getFightState() == FightDefine.FIGHT_STATE_READY then
            local tempx = x - FightDefine.FAMILYWAR_CIRCLE_A.x
            local tempz = z - FightDefine.FAMILYWAR_CIRCLE_A.z
            local dis = nil
            dis = math.sqrt(tempx * tempx + tempz * tempz)
            if dis <= FightDefine.FAMILYWAR_R then
                return 0
            end

            tempx = x - FightDefine.FAMILYWAR_CIRCLE_B.x
            tempz = z - FightDefine.FAMILYWAR_CIRCLE_B.z
            dis = math.sqrt(tempx * tempx + tempz * tempz)
            if dis <= FightDefine.FAMILYWAR_R then
                return 0
            end
            return 1
        else
            return 0
        end
    end

    if hasBlock and dynamicBlockList then
        local num1 = nil
        local num2 = nil
        for k, v in pairs(dynamicBlockList) do
            if v.rotation == 0 then
                -- 无旋转
                num1 = MATH_ABS(v.position.x - x)
                num2 = MATH_ABS(v.position.z - z)
            else --带旋转
                num1 = MATH_ABS(v.position.x - (v.position.x + (x-v.position.x)*cosTable[v.rotation] - (z-v.position.z) * sinTable[v.rotation]))
                num2 = MATH_ABS(v.position.z - (v.position.z + (x-v.position.x)*sinTable[v.rotation] + (z-v.position.z) * cosTable[v.rotation]))
            end
            if (num1 < (v.sizeX*0.5)) and (num2 < (v.sizeY*0.5)) then
                return 1
            end
        end
    end

    return 0
end

DynamicBlockManager = {}

function DynamicBlockManager:init()
    -- body
    hasBlock = false
    dynamicBlockList = FightModel:getFightData():getData(FightDefine.DATA_DYNAMIC_BLOCK)
    if dynamicBlockList then
        for k,v in pairs(dynamicBlockList) do
            hasBlock = true
            break
        end
    end
end

function DynamicBlockManager.hasActiveBlock() 
    if (FightModel:getFightStageType() == ConstantData.STAGE_TYPE_FAMILY_WAR_ELITE_FIGHT or FightModel:getFightStageType() == ConstantData.STAGE_TYPE_DAILY5V5_PVP) then
        if FightServerControl.getFightState() == FightDefine.FIGHT_STATE_READY then --家族战moba模式处于准备阶段时
            return true
        end
        return false
    end
    return hasBlock 
end

LogManager = {}
LogManager.LogError = function(message)
	--print(message)
end

LogManager.Log = function(message) end

local function parseBuff()
    -- body
    local configs = CFG.buff:getAll()
    local newTable = {}
    for _, config in pairs(configs) do
        if newTable[config.buffid] == nil then
            newTable[config.buffid] = {}
        end
        newTable[config.buffid][config.bufflv] = config
    end
    CFG.buff.configs = newTable
end

local function parseSkillLevel()
    -- body
    local configs = CFG.skilllvup:getAll()
    local newTable = {}
    for _, config in pairs(configs) do
        if newTable[config.skillid] == nil then
            newTable[config.skillid] = {}
        end
        newTable[config.skillid][config.level] = config
    end
    CFG.skilllvup.configs = newTable
end

parseBuff()
parseSkillLevel()