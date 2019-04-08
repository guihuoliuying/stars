--
-- Created by IntelliJ IDEA.
-- User: Simon
-- Date: 15-8-11
-- Time: 上午10:12
-- To change this template use File | Settings | File Templates.
-- 用于对源码的重写或加入新的代码（最后加载）


LuaReadNetData ={}
--解析数据包
function LuaReadNetData.read(packetType,data)
    print("----************************---"..packetType)
    NetImitateDecode:reflesh(data)
    PacketReciveArrayFunc[packetType]()
    print("----****************222********---"..packetType)
    --FrameTimeLua.setFighting(true)
end

--场景资源相关处理
function DugeonBase.cleanOutOfSceneData() end

--场景UI相关
function DugeonPVE:initUI() end

--引导处理处理
GuideController = {}
function GuideController.getInst()
    GuideConst.SPECIAL_SCRIPT= 
{
    SCENE_EVENT = "aaa",
}
    return GuideController
end
function GuideController:setScenePassState()
    
end

function GuideController.update(...)
   
end

function GuideController.cleanGuide( )
end

--兵法
-- function DugeonPVE:initJoiner()

-- end

--显示与隐藏地图分块
--SF.showMapPart('1,2,3')
function SF.showMapPart(...)

end

function SF.monsterTwinkle( id, x,y,z )
end

function SF.callShell( ... )
    -- body
end

function SF.dark( ... )
    -- body
end

function SF.bossOnStage( ... )
    -- body
end

NumberEffect = {}
function NumberEffect.pause() end

function NumberEffect.showCureNumber( ... )
    -- body
end
function NumberEffect.showDamageNumber( ... )
    -- body
end

function NumberEffect.showCritNumber( value, target )
end

-- [Comment]
-- 开始播放音乐，注意这个参数是音乐文件的名称！
-- 以下是扩展的参数顺序
-- startVolumn:淡入所需要的音量(0~1)
-- endVolumn:淡出所需要的音量(0~1)
-- fadeInRate:淡入的变化速率
-- fadeOutRate:淡出的变化速率
-- fadeInDelay:淡入的变化速率间隔时间
-- fadeOutDelay:淡出的变化速率间隔时间
-- continueTimeToEnd:淡出的持续时间(秒)
-- changed by: panzhenfeng 策划还需要自定义配置一些音乐淡入淡出调节参数
-- changed date: 2015-6-3
function SF.musicPlay() end

--设置雾气是否开启
function SF.setFogEnable(...) end

--设置柔光可用状态
function SF.setBloomEnable( ... )
    -- body
end

--显示天气效果
--SF.showWeather(1)显示下雨
--SF.showWeather(0)显示晴天
function SF.showWeather( ... )
    -- body
end

---- 控制在场景上指定名称的物体设置为显示/隐藏
function SF.resState(...) end

--显示对话（半身像剧情对话、冒泡、黑屏旁白）
function SF.dialog()
end

function SF.checkDialogEnd( ... )
    return true
end

-- 显示遮罩UI功能
MovieMaskUI = {}
function MovieMaskUI:show(isEase)
end

function MovieMaskUI:hide(isEase)
end

-- BOSS血条UI
BossBarWindow = {}
function BossBarWindow.Hide()
end

--- 显示场景图片。battleStart=战斗开始，boss1=boss来了
--- </summary>
--- <param name="imgName">图片名，【字符串】</param>
--- <param name="delay">【可选】延时多少秒进行显示，默认：0秒</param>
function SF.sceneImg(imgName, delay)
end

--显示boss血条或者隐藏
function SF.showOrHideBossBlood(type)
end

function SF.setCameraPath(...) end

-- ThingController
ThingController = {}
function ThingController:playCam( ... )
end
-- ThingController

-- SceneControl
function SceneControl:showFightRemind( ... )
end

function SceneControl.SceneBeforeEnd(isWin)
    local starStr = ""
    local temp = {}
    if isWin then
        SceneControl:countSceneStars()
        local sceneStars = SceneDataProxy:getSceneStarData()
        if sceneStars then
            for k,v in pairs(sceneStars) do
                if v.isFinish then
                    starStr  = starStr..tostring(v.pos)..','
                end
            end
        end
        temp.sceneId = SceneDataProxy:getSceneId()
        temp.isWin = isWin
        temp.starStr = starStr
    else
        temp.sceneId = SceneDataProxy:getSceneId()
        temp.isWin = isWin
        temp.starStr = starStr
    end
    FrameTimeLua.setFighting(false)
    -- getCheckResult(temp)
    SceneDataProxy:dispose()
end
-- SceneControl

RoleEffectUtil = {}
function RoleEffectUtil:getMoveChoosePlayerEffect( ... )
    
end

Application = {}
Application.targetFrameRate = 30

--- <summary>
--- 取消屏幕黑边
--- </summary>
--- <param name="smooth">是否平滑消失，1或空=是的，0=立刻消失</param>
function SF.unblack( ... )
end

-- [Comment]
-- 场景UI控制，显示或隐藏UI
function SF.sceneUI( ... )
end

--显示boss脚底光圈
--speed：速度，原始速度是1
--scale：缩放大小，  eg:Vector3.New(2,2,2)
function Monster:showBossBottomLight( ... )
end

function Monster:showBossSkillBottomLight( ... )
    -- body
end

--- <summary>
--- 绑定BOSS实体到血条显示栏。
--- </summary>
--- <param name="bossCharacter">boss对象，包含了数据和实体等</param>
--- <param name="showdelay">延时多少秒显示，单位：秒，默认：0</param>
function BossBarWindow.Show( ... )
end

function IsNumber(num)
    return num ~= nil and type(num) == "number"
end

--只有玩家才需要更新怒气条界面
BattleSkillBar ={}
function BattleSkillBar:updateAngry(id, currentAngry, totalAngry)
end

function BattleSkillBar:updateHp( ... )
    -- body
end

function BattleSkillBar:dead( ... )
    -- body
end

function CameraTools.shake( ... )
    -- body
end

function CameraTools.lookAt( ... )
    -- body
end

function CameraTools.resetCamera( ... )
    -- body
end

PVPFightController = {}
function PVPFightController.deadHero( ... )
    -- body
end

function FenceManager:samplePosition( trans, targetPos, id )
    return PathFinder.samplePosition(targetPos, 30)
end

function NumberEffect.showCritNumber( ... )
end

ExtentionMethod = {}
function ExtentionMethod.GetChildByNameRecursive(transform, tName)
    return Transform.New()
end

function StateBase:normalizedTime( ... )           --获取当前动画播放进度
    -- body
    return 1
end

function StateBase:changeActSpeed( speed )
end

function RoleEffectUtil:hideMoveChooseAllEffect( ... )
end

--- <summary>
--- 启用或禁止掉落效果，0=不显示，1=显示
--- </summary>
--- <param name="isShow">0=不显示，1=显示</param>
function SF.dropEffect(isShow)
end

--- <summary>
--- 场景结束之前要执行的操作
--- </summary>
function SF.beforeEnd()
end

--- <summary>
--- 播放声效文件，如技能声效等
--- </summary>
--- <param name="name">【字符串】资源文件名</param>
--- <param name="...">资源名称，参考前面的格式</param>
function SF.sound(name, ...)
end

--- <summary>
--- 【镜头特写】让镜头对准目标，用于特写（放大之类的）
--- </summary>
--- <param name="deployId">布怪表ID</param>
--- <param name="distance">镜头距离目标的距离</param>
--- <param name="speed">镜头移动时间，浮点数，默认：0.12秒</param>
function SF.lookAt(deployId, distance, speed)
end

-----下面是镜头相关函数------
-- [Comment]
-- 时间慢放
-- 初始慢放倍数，增长倍数，增长时间
function SF.slow(a, b, c)
end

-- [Comment]
-- 重置摄像机
function SF.resetCamera()
end

--是否是过关斩将的场景;
function IsRushScene(sceneType)
    if sceneType ==ConstantData.SceneType.YIFANGHAOJIE or sceneType ==ConstantData.SceneType.QUDISHOUJI or sceneType ==ConstantData.SceneType.HUANGJINGSHUSHI or sceneType ==ConstantData.SceneType.YIFUDANGGUAN or sceneType ==ConstantData.SceneType.JINGUOXUMEI or sceneType ==ConstantData.SceneType.BAODONGTANXIAN then
        return true;
    end
    return false;
end

-- 获取地表Y值得
function FenceManager:getTerrinY( targetPos )
    return 4.5
end

SkillProcess = {}
function SkillProcess:new( o )
    o = o or {}
    setmetatable(o, self)
    self.__index = self

    return o
end

function SkillProcess.New( )
    local sp = SkillProcess:new()

    return sp
end
function SkillProcess:getInstance( ... )
    return SkillProcess.New()
end

function SkillProcess:setData( ... )
    
end

function SkillProcess:show( ... )
    return false
end

function SkillProcess:hide( ... )
    
end

function SkillProcess:block( ... )
    
end

function DugeonPVE:onSkillBegin( vo )
    -- body
    local character = vo[1]
    local skill = vo[2]
    if( skill.releaseType == 3)then --大招
        self.isSkillShowTime = true
        GlobalEvent.dispatch( EventConstant.POWERFUL_SKILL_START,character)
        SceneDataProxy.isShowTime = true
        self:setRunning(false)
    else
        self:getBase().onSkillBegin(self, vo)
    end
end

function DugeonPVE:onSkillEnd( vo )
    -- body
    local character = vo[1]
    local skill = vo[2]
    if( skill.releaseType == 3 and self.isSkillShowTime)then --大招
        self.isSkillShowTime = false
        GlobalEvent.dispatch(EventConstant.POWERFUL_SKILL_END, character)
        character.castSerialSkill = false
        SceneDataProxy.isShowTime = false
        self:setRunning( true )
    else
        self:getBase().onSkillEnd( self, vo )
    end
end


-- commonfunc
--求2直线的交点;
--(P1X,P1Y) (P2X,P2Y)是第一条直接的点;
--(P3X,P3Y) (P4X,P4Y)是第二条直接的点;
function calc2LineCrossPoint(p1X, p1Y, p2X, p2Y, p3X, p3Y, p4X, p4Y)
    local resultX;
    local resultY;
    local left, right;
    left = (p2Y - p1Y) * (p4X - p3X) - (p4Y-p3Y)*(p2X - p1X);
    right = (p3Y - p1Y) * (p2X - p1X) * (p4X - p3X) + (p2Y - p1Y) * (p4X - p3X) * p1X - (p4Y - p3Y) * (p2X - p1X) * p3X;
    if(left==0)then
        return nil,nil;
    end
    resultX=right/left;
    left = (p2X - p1X) * (p4Y - p3Y) - (p4X - p3X) * (p2Y - p1Y);
    right = (p3X - p1X) * (p2Y - p1Y) * (p4Y - p3Y) + p1Y * (p2X - p1X) * (p4Y - p3Y) - p3Y*(p4X - p3X)*(p2Y - p1Y);
    if(left==0)then
        return nil,nil;
    end
    resultY=right/left;
    return resultX,resultY;
end

--计算点是否在线段上;
--checkX1, checkY1, 是待检测的点;
--(x1n, y1n)是第一条直接的点;
function calcPointIsInSegLine(checkX1, checkY1, x11, y11, x12, y12, ignoreCheckInLine, needLog)
    ignoreCheckInLine = ignoreCheckInLine or false;
    needLog = needLog or false;
    return LuaHelper.CalcPointIsInSegLine(checkX1, checkY1, x11, y11, x12, y12, ignoreCheckInLine, needLog);
end

--获取srcPoint点绕anchorPoint点转degree角度后的坐标
function pointRotateByPoint(srcPoint, anchorPoint, degree)
    local x = srcPoint.x;
    local y = srcPoint.y;

    local x0 = anchorPoint.x;
    local y0 = anchorPoint.y;
    degree = math.ceil(degree);
    local rtnX = (x-x0)*cosTable[degree]-(y-y0)*sinTable[degree]+x0;
    local rtnY = (x-x0)*sinTable[degree]+(y-y0)*cosTable[degree]+y0;
    return Vector2.New(rtnX, rtnY);
end
-- commonfunc



SceneImgWindow = {}
function SceneImgWindow.Dispose()
end

--人物胜利展示，集中
function DugeonBase:showVictory( ... )
    -- body
end

GameWinWindow = {}
--将评星结果传给胜利界面
function GameWinWindow.setStarNum( ... )
    -- body
end

-- 销毁所有掉落效果
ItemDropEffect = {}
function ItemDropEffect.Dispose()

end

--清除物品掉落信息
function ItemDropHelper.Dispose( ... )
    -- body
end

--清理飘字
function NumberEffect.clear( ... )
    -- body
end

--清除特效管理器
function EffectManager:clearAllEffect( ... )
    -- body
end

--清除摄像头信息
function CameraTools.clear( ... )
        -- body
end

BattleOccupyTownWindow = {}
function BattleOccupyTownWindow:updateAngry(...)

end

--退出并清除
function DugeonBase:exitAndClear( ... )
    -- body
    --self:clearCharacter()
    -- --GameLog'jx', "----------22222")
    --GameLog"pete3","-----------------")
    PathFinder.dispose()
    BattleFormation:clear()
    AIManager:clear()
    FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUASCENE):removeLuaFunc(funcKey)
    FrameTimeLua.setFighting(false)
end

--清除地图数据
-- SceneManager = {}
-- function SceneManager.getInstance()
--     return SceneManager
-- end
-- function SceneManager:clean()
--     -- body
-- end

CameraDragManager = {}


function GameCharacter:showSimpleBottomLight( ... )
    -- body
end

function GameCharacter:initCharacterMaterial()
end

function GameCharacter:changeAlpha(delta)
end

function GameCharacter:initBlood( )
end

function CharacterView:addBottomLight()
end

TeamCenter = {}
function TeamCenter.moveTo(...)
end

function TeamCenter.initPoint(centerPos)
end

function TeamCenter.setSpeed(speed)
end

function TeamCenter.resetSpeed()
end

--- <summary>
--- 镜像模糊效果
--- </summary>
function SF.fuzzy( ... )
    -- body
end

--清空qte相关数据
QteControl = {}
function QteControl.clean( ... )
    -- body
end

-- [Comment]
-- 检查击杀的怪物数量
function SF.checkNeedSkillMonsterNumms(num)
   local ctx = SF.getCtx();
   num = num or 1
   if(num<=ctx.killMonsterCount)then
        return true;
   end
   return false;
end

--击杀怪物数量的bar是否要显示;
--isshow:1显示,否则隐藏;
function SF.setNeedSkillMonsterNumsShow(isshow)
    
end


RoleData = {}
function RoleData.SetRoleData( roleId )
    RoleData.roleId = roleId
end

function RoleData.ClearRoleData( )
    RoleData = {}
end

function SkillEventMgr:onAddSkillView( data )

end

function SkillEventMgr:onAddSkillViewByVo( data )

end