-- region MainToCSharp.lua
-- Date    : 2015-5-15
-- Author : Jim
-- Description :从c#调用的初始化各种参数的接口
-- endregion

GameUtil = yh.GameUtil
UIUtil = yh.UIUtil
Application = UnityEngine.Application;
UrlMgr = yh.UrlMgr;
RuntimePlatform = UnityEngine.RuntimePlatform
MainPanel = yh.MainPanel
DockOption = yh.DockOption
NetWorkManager = Assets.script.netWorkManager.Manager.NetWorkManager
ServerConnect = Assets.script.netWorkManager.Manager.ServerConnect
GameObject = UnityEngine.GameObject
UIManager = yh.UIManager
NavigationUtil = yh.NavigationUtil
Obstacles = yh.Obstacles

LogMan = LogManager.Log
MainToCSharp={}

local loadIndex = 0

--初始化c#底层数据
local function initCsharpData()
	GameUtil.UI_WIDTH = 1334;
	GameUtil.UI_HEIGHT = 750;
	GameUtil.win_gap = 1000;

	GameUtil.isCamScaleAuto = false

	--初始化路径加载路径
	UrlMgr.skillTexturesPath = UrlManager.getLocalUrl (UrlManager.IMAGES,"",true,true) .. "skillTextures/"
	UrlMgr.imageRootPath = UrlManager.getImage("")
	UrlMgr.materialRootPath = UrlManager.getMaterial("")
	UrlMgr.animationRootPath = UrlManager.getAnimation("")
	UrlMgr.roleRootPath = UrlManager.getRoles(UrlManager.ROLES,"")
	UrlMgr.skillRootPath = UrlManager.getSkill("")
	UrlMgr.atlasRootPath = UrlManager.getAtlas("")
	UrlMgr.effectRootPath = UrlManager.getEffect("")
	UrlMgr.soundRootPath  = UrlManager.getSound ("")

	-- UIUtil.initPool()
	UIUtil.addAtlas("publicResources")
	UIUtil.addAtlas("item_icon")

	Application.runInBackground = true;
    Application.targetFrameRate = 30;

	--设置质量和log开关
	if GameUtil.isAndroidPlatform then
		LogManager.isOutLog = false;
		Debugger.useLog = false;
		GameUtil.isShowUIDebug = false;
		LuaHelper.setQualityLevel(0);
		PrinterName = nil;
	elseif GameUtil.isIosPlatform then
		LogManager.isOutLog = false;
		GameUtil.isShowUIDebug = false;
		Debugger.useLog = false;
		LuaHelper.setQualityLevel(1);
		PrinterName = nil;
	elseif GameUtil.isWindowPlayerPlatform then
		LogManager.isOutLog = false;
		GameUtil.isShowUIDebug = false;
		Debugger.useLog = false;
		LuaHelper.setQualityLevel(1);
		PrinterName = "zxg";
	else
		LogManager.isOutLog = true;
		GameUtil.isShowUIDebug = true;
		Debugger.useLog = true;
		LuaHelper.setQualityLevel(3);
		PrinterName = "zxg"
	end

	GameUtil.useLuaFrameManager = true
end

--检查是否健在完毕
local function checkLoadOver ()
	loadIndex = loadIndex + 1;
	if loadIndex >= 2 then 
		GameUtil.LuaScritptMgr:Start ()
	end
end

--加载公用图集错误处理
local function loadAtlasError (param)
	LogManager.LogError ("load atlas error="..param.requestURl);
	loadPublicAtlas ();
end

--加载公用图集完成处理
local function loadAtlasComplete (param)
	UIUtil.setPublicResources(param.assetbundle)
	checkLoadOver ();
end

--场景结构加载完毕初始化
local function onLoadComplete (param)
	local win = nil;
	local canvasGo = LuaHelper.InstantiateGameObjectFromAsset(param.assetbundle, "Canvas", "Canvas");
	MainPanel.uiRoot = canvasGo
	--设置参考分辨率;
	local tmpCanvasScaler = canvasGo:GetComponent("CanvasScaler");
	--使用当前机子的分辨率;
	LuaHelper.SetCanvasScalerReferenceWH(tmpCanvasScaler, -1, -1);
	local eventSystem = LuaHelper.InstantiateGameObjectFromAsset(param.assetbundle, "EventSystem", "EventSystem");
	local camera = LuaHelper.InstantiateGameObjectFromAsset(param.assetbundle, "Camera", "Camera");
	MainPanel.things = LuaHelper.InstantiateGameObjectFromAsset(param.assetbundle, "gameObject", "things");
	MainPanel.util = LuaHelper.InstantiateGameObjectFromAsset(param.assetbundle, "util", "util");
	MainPanel.soundGo = GameObject.New("SoundManager")
	GameObject.DontDestroyOnLoad(MainPanel.soundGo);

    -- 不销毁对象
    GameObject.DontDestroyOnLoad(camera)
    GameObject.DontDestroyOnLoad(canvasGo)
    GameObject.DontDestroyOnLoad(eventSystem)
    GameObject.DontDestroyOnLoad(MainPanel.things)
    GameObject.DontDestroyOnLoad(MainPanel.util)
    UIManager.getInstance():init()

	LuaHelper.removeResourceFromLoadManager(param.url);
	checkLoadOver ();
end

--加载场景结构失败处理
local function loadWinError (param)
	LogManager.LogError ("load mainUI error="..param.requestURl);
	loadMainUI ();
end

-- local function loadNumberEffectAtlasError (param)
	-- LogManager.LogError ("load atlas error="..param.requestURl);
-- end

--加载公用图集完成处理
-- local function loadNumberEffectAtlasComplete (param)
	-- UIUtil.publicResources2 = param.assetbundle;
-- end

local function loadNumberFontError (param)
	LogManager.LogError ("load number font error="..param.requestURl);
end

--加载公用图集完成处理
local function loadNumberFontComplete (param)
	UIUtil.publicResources3 = param.assetbundle;
	UIUtil.font2 = param.assetbundle:LoadAsset("number_font")
end

--加载公共图集
local function loadPublicAtlas()
	local url = UrlManager.getAtlas ("publicResources");
	LoadMgrLua.getInstance ():LoadSceneUnity3d (url, loadAtlasComplete, "",loadAtlasError);

	-- url = UrlManager.getAtlas ("numberEffect");
	-- LoadMgrLua.getInstance ():LoadSceneUnity3d (url, loadNumberEffectAtlasComplete, url,loadNumberEffectAtlasError);

	url = UrlManager.getAtlas ("number_font");
	LoadMgrLua.getInstance ():LoadSceneUnity3d (url, loadNumberFontComplete,"",loadNumberFontError);
end

--加载场景结构
local function loadMainUI ()
	local loadPath = UrlManager.getModule("main");
	LoadMgrLua.getInstance ():LoadSceneUnity3d(loadPath, onLoadComplete,"",loadWinError);
end

local function InitData()
	--初始化各种基础数据
	initCsharpData();

	--初始化tween
	GameUtil.mainGame:initTween(true,true,200,50);

	--开始初始化ui和公用资源
	loadMainUI();
	loadPublicAtlas();
end
require("game/util/UrlManager")
require("net/GameNet")
InitData()

