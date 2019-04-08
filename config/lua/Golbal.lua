-- luanet.load_assembly("UnityEngine")

object			= System.Object
Type			= System.Type
DateTime		= System.DateTime
Object          = UnityEngine.Object
GameObject 		= UnityEngine.GameObject
Transform 		= UnityEngine.Transform
MonoBehaviour 	= UnityEngine.MonoBehaviour
Component		= UnityEngine.Component
Application		= UnityEngine.Application
SystemInfo		= UnityEngine.SystemInfo
Screen			= UnityEngine.Screen
Camera			= UnityEngine.Camera
Material 		= UnityEngine.Material
Renderer 		= UnityEngine.Renderer
AsyncOperation	= UnityEngine.AsyncOperation
Vector2         = UnityEngine.Vector2
Vector3         = UnityEngine.Vector3

CharacterController = UnityEngine.CharacterController
SkinnedMeshRenderer = UnityEngine.SkinnedMeshRenderer
Animation		= UnityEngine.Animation
AnimationClip	= UnityEngine.AnimationClip
AnimationEvent	= UnityEngine.AnimationEvent
AnimationState	= UnityEngine.AnimationState
Input			= UnityEngine.Input
KeyCode			= UnityEngine.KeyCode
AudioClip		= UnityEngine.AudioClip
AudioSource		= UnityEngine.AudioSource
Physics			= UnityEngine.Physics
Light			= UnityEngine.Light
LightType		= UnityEngine.LightType
ParticleEmitter	= UnityEngine.ParticleEmitter
Space			= UnityEngine.Space
CameraClearFlags= UnityEngine.CameraClearFlags
RenderSettings  = UnityEngine.RenderSettings
MeshRenderer	= UnityEngine.MeshRenderer
WrapMode		= UnityEngine.WrapMode
QueueMode		= UnityEngine.QueueMode
PlayMode		= UnityEngine.PlayMode
ParticleAnimator= UnityEngine.ParticleAnimator
TouchPhase 		= UnityEngine.TouchPhase
AnimationBlendMode = UnityEngine.AnimationBlendMode
TextAnchor      = UnityEngine.TextAnchor
Resources       = UnityEngine.Resources
Shader          = UnityEngine.Shader
WWW				= UnityEngine.WWW
MicrophoneCtrl	= yh.MicrophoneCtrl
VoiceObject		= yh.VoiceObject 

SceneMng    = UnityEngine.SceneManagement.SceneManager

MeshPlayer      = yh.MeshPlayer
MeshAnimManager = yh.MeshAnimManager
--Custom
Application =  UnityEngine.Application --使用所绑定的类 不使用ulua luanet加载方式
UIManager   =  yh.UIManager
RoleControllerUtil = yh.RoleControllerUtil
ConfigManager = yh.ConfigManager --配置相关

MainPanel = yh.MainPanel
FubenOverWindow = yh.FubenOverWindow
FubenOverData = yh.FubenOverData
Time = UnityEngine.Time
CamUtil = yh.CamUtil
Animator = UnityEngine.Animator
ThingData = yh.ThingData
ThingPlayer = yh.ThingPlayer
RoleConfig = yh.RoleConfig
MathCommon = yh.MathCommon
UIEventManager = yh.UIEventManager 
MainPanel = yh.MainPanel
UIUtil = yh.UIUtil
AnimationCurve = UnityEngine.AnimationCurve
AudioVo = yh.AudioVo
LightmapData = UnityEngine.LightmapData
AsyncOperation = UnityEngine.AsyncOperation
KeyboardEvent = yh.KeyboardEvent
GridLayoutGroup_Corner = UnityEngine.UI.GridLayoutGroup.Corner

function LoadAllLua()
	loadLua = require
    require("include") --加载包含的头文件
end

function print(...)
	local str = ""	
	local arg = {...}
	local n = select('#', ...)

	for i = 1, n do
		str = string.format("%s%s", str, tostring(arg[i]))
	end
	
	Debugger.Log(str)
end

function printf(format, ...)
	Debugger.Log(string.format(format, ...))
end

--require "game/util/Vector3"
--require "game/util/Vector2"

function traceback(msg)
	msg = debug.traceback(msg, 2)
	return msg
end

function LuaGC()
  local c = collectgarbage("count")
  Debugger.Log("Begin gc count = {0} kb", c)
  collectgarbage("collect")
  c = collectgarbage("count")
  Debugger.Log("End gc count = {0} kb", c)
end

--[[function ShowUI(name, OnLoad)
	UIBase.LoadUI(name, OnLoad)
end--]]

function RemoveTableItem(list, item, removeAll)
    local rmCount = 0

    for i = 1, #list do
        if list[i - rmCount] == item then
            table.remove(list, i - rmCount)

            if removeAll then
                rmCount = rmCount + 1
            else
                break
            end
        end
    end
end



--unity 对象判断为空, 如果你有些对象是在c#删掉了，lua 不知道
--判断这种对象为空时可以用下面这个函数。
function IsNil(uobj)
	return uobj == nil or uobj:Equals(nil)
end
--- <summary>
--- 判断一个传入的对象是否为数字
--- </summary>
function IsNumber(num)
    return num ~= nil and type(num) == "number";
end
--- <summary>
--- 判断一个传入的字符串是否为nil或者是为空
--- </summary>
function string.IsNilOrEmpty(str)
    return str == nil or tostring(str) == "";
end

-- isnan
function isnan(number)
	return not (number == number)
end

function changeObjctLayer( trans, layer )
	-- body
	trans.gameObject.layer = layer
	for i=0,trans.childCount - 1 do
		changeObjctLayer(trans:GetChild (i),layer)
	end
end

--[[function FindNode(transform, name)
	if transform == nil then
		error("invalide arguments to FindNode transform is nil")
		return nil
	elseif name == nil then
		error("invalide arguments to FindNode name is nil")
		return nil
	end
	
	local node = UnGfx.FindNode(transform, name)
	
	if node == nil then
		error(string.format("transform %s does not have child %s", transform.name, name))
	end
	
	return node
end--]]


function string:split(sep)
	local sep, fields = sep or ",", {}
	local pattern = string.format("([^%s]+)", sep)
	self:gsub(pattern, function(c) table.insert(fields, c) end)
	return fields
end

function GetDir(path)
	return string.match(fullpath, ".*/")
end

function GetFileName(path)
	return string.match(fullpath, ".*/(.*)")
end

-- Courtesy of lua-users.org
--[[function string:split(pat)
   local t = {}
   local fpat = "(.-)" .. pat
   local last_end = 1
   local s, e, cap = string.find(str, fpat, 1)

   while s do
      if s ~= 1 or cap ~= "" then
          table.insert(t,cap)
       end
      last_end = e+1
      s, e, cap = string.find(str, fpat, last_end)
   end

   if last_end <= string.len(str) then
      cap = string.sub(str, last_end)
      table.insert(t, cap)
   end

   return t
end]]

function table.contains(table, element)
  if table == nil then
        return false
  end

  for _, value in pairs(table) do
    if value == element then
      return true
    end
  end
  return false
end

function table.getCount(self)
	local count = 0
	
	for k, v in pairs(self) do
		count = count + 1	
	end
	
	return count
end

function DumpTable(t)
	for k,v in pairs(t) do
		if v ~= nil then
			Debugger.Log("Key: {0}, Value: {1}", tostring(k), tostring(v))
		else
			Debugger.Log("Key: {0}, Value nil", tostring(k))
		end
	end
end

 function PrintTable(tab)
    local str = {}

    local function internal(tab, str, indent)
        for k,v in pairs(tab) do
            if type(v) == "table" then
                table.insert(str, indent..tostring(k)..":\n")
                internal(v, str, indent..' ')
            else
                table.insert(str, indent..tostring(k)..": "..tostring(v).."\n")
            end
        end
    end

    internal(tab, str, '')
    return table.concat(str, '')
end

function PrintLua(name, lib)
	local m
	lib = lib or _G

	for w in string.gmatch(name, "%w+") do
       lib = lib[w]
     end

	 m = lib

	if (m == nil) then
		Debugger.Log("Lua Module {0} not exists", name)
		return
	end

	Debugger.Log("-----------------Dump Table {0}-----------------",name)
	if (type(m) == "table") then
		for k,v in pairs(m) do
			Debugger.Log("Key: {0}, Value: {1}", k, tostring(v))
		end
	end

	local meta = getmetatable(m)
	Debugger.Log("-----------------Dump meta {0}-----------------",name)

	while meta ~= nil and meta ~= m do
		for k,v in pairs(meta) do
			if k ~= nil then
			Debugger.Log("Key: {0}, Value: {1}", tostring(k), tostring(v))
			end

		end

		meta = getmetatable(meta)
	end

	Debugger.Log("-----------------Dump meta Over-----------------")
	Debugger.Log("-----------------Dump Table Over-----------------")
end

function stringToTable(str)
   if(str==nil)then
   		return nil;	
   end
   return loadstring("return "..str)()
end

function SetIndex(indexTable) --设置Index方法，面向对象的继承用到（提高访问效率）
	-- indexTable.__index = indexTable
    indexTable.__index =function(table,key)
        local value = indexTable[key]
        rawset(table,key,value)
        return value
    end
end

local function createFunc( self, o )
	o = o or {}
	setmetatable( o, self )
	return o
end

function createClass( o )
	-- body
	o = o or {}
	o.create = createFunc
	o.new = createFunc
	o.__index = o
	return o
end

function NewTable(self,o)
    o = o or {}
    setmetatable(o, self)
    self.__index = self
    return o
end

function createClassWithExtends( o, parent )
	-- body
	o = o or {}
	o.__index = o
	setmetatable(o, parent )
	return o
end

function isRunningInServer( )
	return false
end


--注册模块, 用于初始化模块的命令，之后模块(注:mvc理解为同一个模块)之间调用公开的方法通过命令进行调用;
local function registerModules()
	BusyIndicateWindow.register()
	LoginWindow.register() --登陆系统
	MainWindow:register()
    RoleWindow.register()
    BagWindow.register() --背包系统
    FightModel:init() --暂时放这
    FightAbilityWindow.register()
	
	DungeonMainWindow:register()
	CharacterManager:init()
    CameraManager:init()
	StageFightWindow.register()
	EquipmentCtrl.register()
	AwardShowCtrl.register()
	GetwayCtrl.register()
	BoxUseNotify.register()
	StageResultWindow.register()
	SweepWindow.register()
	LoadingWindow.register()
	TaskControl.register()
	EmailWindow.register()
	VigorControl.register()
	ChatControl.register()
	SkyTowerCtrl.register()	
	FriendController.Init()
	PlayerInfoController.Init()
	RankWindow.register()
	InductCtrl.register()
	FamilyControl.register()
end

--注册协议;
local function registerProtocol()
	RoleProtocol.register();
	EquipmentProtocol.register();
	LoginProtocol.register();
	SkyTowerProtocol.register();
end


LoadAllLua()
registerProtocol()
registerModules()
RoleModel.init()
RoleData.init();