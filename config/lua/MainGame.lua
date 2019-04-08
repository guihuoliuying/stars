if GameUtil.useLuaFrameManager == true then
    LuaFrameTimerManger.RegisterLuaFrameTimerManager(FrameTimerManager.frameHandle)
end

local function onShaderLoadOver()
    -- CommonFunc.opertationBloom (GameUtil.mainCamera, true);
end

--登录成功之后，进入游戏（这里是完成整个登录流程之后）
local function enterGame()
    Joystick:create()   --摇杆
    RoleExpBar:create() --角色经验条
    --请求角色装备信息;
    EquipmentProtocol.sendRequestAllEquipTishenInfo();
	GameNet.sendPacket(PacketType.ServerWorldView, DungeonConstant.SEND_WORLD_LIST)
end

local function InitData()
    CommonFunc.addCanvas(MainPanel.uiRoleBlood.gameObject)
end

local function initGameData()
        UIEventManager.getInstance():init()
		StageManager.init()
        AudioMgr.init()
        InitData()
        MaterialsTexturePool.init (onShaderLoadOver,UrlManager.getMaterial("public_shader"))
		WindowManager.showWin("CommonTips")
		ExtendConfigMethods.parseConfigAfterLoaded()
		ModuleEvent.addListener(ModuleConstant.LOGIN_SUCC, enterGame)
        ModuleEvent.dispatch(ModuleConstant.LOGIN_WINDOW, ModuleConstant.UI_OPEN);
		ModuleEvent.dispatch(ModuleConstant.BUSYINDICATE_SHOW, {ConstantData.BUSY_TYPE_TWEENNING, nil, 0.1})
		ModuleEvent.dispatch(ModuleConstant.LOADING_WINDOW, {ModuleConstant.UI_OPEN, "preload"})
        -- BgLayer:show(0)
        -- MaskLayer:show(0)
		if GameUtil.isMobilePlatform == false then
			--loadByIndex(1, "game/modules/debug/DebugCtrl")
			--DebugCtrl.init()
		end
        -- 加载loading
        ModuleEvent.dispatch(ModuleConstant.BUSYINDICATORWINDOW_SHOW, {ConstantData.BUSY_TYPE_LOADING, nil, 1, Res.String.Load.Loading});
end

local function InitGame()
    -- 游戏入口
    -- loadLua = require
    -- require("include") --加载包含的头文件
    -- LayerManager.InitLayerMask()
    initGameData() 
    GameNet:initNet()
    -- GameState.EnterGame()
    AppFacade.Instance:SendMsgCmd (NoticeConst.START_UP, CmdDefine.GAME_INIT_END)
end

--错误异常捕获接口;
function tryCatchErrorMsg(msg_)
    CmdLogError(msg_.."->"..debug.traceback());
end

xpcall(InitGame, tryCatchErrorMsg);


