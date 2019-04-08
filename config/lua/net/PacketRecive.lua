-- auto: zrc
-- Data: 2015-03-09
-- 这里只处理一些公用的协议

PacketRecive = { 
	
}

local function execClientHeratBeat()
	local conn = GameNet.GetSocket()
    -- local str = conn:ReadString()
    -- local byet = conn:ReadSbyte()
	local serverTime = conn:ReadLong();
	GameState.isRecvFirstHeartBeat = true;
	if serverTime > 0 then
		GameNet.UpdateServerTime(serverTime)
	end
	GameNet.UpdateRecvPacketTime()
    --GameLog"zrc", str)
end
--处理获取text
local function getGametext(key)
    local text = CFG.gametext:get(key)
    if text ~= nil then
        return text
    end
    local data = {text = key}
    return data
end

local function execCommonTips()
	local conn = GameNet.GetSocket()
    --系统常量,即如ModuleConstant.EQUIPMENT_WINDOW;
    local systemConstant = conn:ReadInt();
    local key  = conn:ReadString()
    local size = conn:ReadSbyte()
    local tipsCfg  = CFG.gametext:get(key)
    local tipsText = ''
	local param  = {}
	if tipsCfg and tipsCfg.text then
		tipsText = tipsCfg.text
	else
		tipsText = key
	end
    for i=1,size do
        param[i] = conn:ReadString()    --参数
		local temp = CFG.gametext:get(param[i])
		if temp and temp.text then
			param[i] = temp.text
		end
    end
    local paramIdx = 1
    if size > 0 then
        tipsText = string.gsub(tipsText, "%%s", function(s)
            paramIdx = paramIdx + 1
            return param[paramIdx-1]
         end)
    end
	if key ~= "0" then
		CommonTips.showCommonFloatTips(tipsText)
	end
    if(systemConstant>0)then
        ModuleEvent.dispatch(ModuleConstant.BUSYINDICATE_STOP, systemConstant);
	else
		ModuleEvent.dispatch(ModuleConstant.BUSYINDICATE_STOP, {stopType = ConstantData.BUSY_TYPE_NETWORK})
    end
    --操作完成后停止忙碌指示器
    -- ModuleEvent.dispatch(ModuleConstant.BUSYINDICATORWINDOW_STOP)
end

--- <summary>
--- 在客户端显示滚动消息
--- </summary>
local function execClientRollTips()
	local conn = GameNet.GetSocket()
    --系统常量,即如ModuleConstant.EQUIPMENT_WINDOW;
    local systemConstant = conn:ReadInt();
    local key  = conn:ReadString()
    local size = conn:ReadSbyte()
    local tipsCfg  = CFG.gametext:get(key)

    local tipsText = ''
	local param  = {}
	if tipsCfg and tipsCfg.text then
		tipsText = tipsCfg.text
	else
		tipsText = key
	end
    for i=1,size do
        param[i] = conn:ReadString()    --参数
	    
    	local temp = CFG.gametext:get(param[i])
		if temp and temp.text then
			param[i] = temp.text
		end
    end
    local paramIdx = 1
    if size > 0 then
        tipsText = string.gsub(tipsText, "%%s", function(s)
            paramIdx = paramIdx + 1
            return param[paramIdx-1]
         end)
    end
	if key ~= "0" then
        ModuleEvent.dispatch(ModuleConstant.SHOW_ROLL_NOTICES, {tipsText,1});
	end
end


--此处为登录下发全局表中的数据，按顺序读取
local function execClientSysConfig( )
    local value = conn:UnpackInt() --第一个是怒气最大值
    ConstantData.MAX_ANGER = value
    --音效播放的上限
    local audioCount = conn:UnpackShort() 
    ConstantData.AUDIO_COUNT_MAX = audioCount;
end

local function register()
	GameNet.registerRecv(PacketType.ClientHeratBeat, execClientHeratBeat)
	GameNet.registerRecv(PacketType.ClientCommonTips, execCommonTips)
    GameNet.registerRecv(PacketType.ClientRollTips, execClientRollTips)
end

do	register() end