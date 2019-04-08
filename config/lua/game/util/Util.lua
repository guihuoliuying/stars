GameObject = UnityEngine.GameObject
--- <summary>
--- 基本功能类
--- </summary>
Util = { };
-- 输出日志--
function log(str)
    print(str)
end

-- 查找对象--
function find(str)
    return GameObject.Find(str);
end

function destroy(obj)
    LuaHelper.Destroy(obj);
end

function newobject(prefab)
    return CommonFunc.Instantiate(prefab);
end

function child(str)
    return transform:FindChild(str);
end

function Substring(str, index)
    return str:Substring(index);
end

function removeCloneTag(str)
    return str:Replace("(Clone)", "");
end

function subGet(childNode, typeName)
    return child(childNode):GetComponent(typeName);
end

function makeHandle( self, cb )
    return function ( ... )
        cb(self, ... )
    end
end

function findPanel(str)
    local obj = find(str);
    if obj == nil then
        error(str .. " is null");
        return nil;
    end
    return obj:GetComponent("WindowBaseLua");
end

-- 清除所有子节点
-- 参数 go = Transform
function ClearChild(go)
    if (go == nil) then return; end
    for i = go.childCount - 1, 0, -1 do
        LuaHelper.Destroy(go:GetChild(i).gameObject);
    end
end

--- <summary>
--- 从uiMiddle 或者 uiTop 面板里面查找某个子窗口，优先从uiMiddle查找，
--- 找不到就从uiTop找，如果找到，返回GameObject，如果都找不到就返回 nil。
--- </summary>
--- <param name="winName">窗口名称</param>
function Util.FindWin(winName)
    if (string.IsNilOrEmpty(winName)) then return nil; end
    local win = MainPanel.uiMiddle.transform:FindChild(winName);
    if (win ~= nil) then
        -- --GameLog"ljh", "Util.FindWin->", winName, "-", win.gameObject.name);
        return win.gameObject;
    end
    win = MainPanel.uiTop.transform:FindChild(winName);
    if (win ~= nil) then
        -- --GameLog"ljh", "Util.FindWin->", winName, "-", win.gameObject.name);
        return win.gameObject;
    end
    return nil;
end

--- <summary>
--- 将某个窗口的父级设置到uiTop。
--- </summary>
--- <param name="winTran">窗口transform</param>
function Util.SetParentToTop(winTran)
    if IsNil(winTran) then return; end
    -- --GameLog"ljh", "Util.SetParentToTop->", winTran.gameObject.name);
    winTran:SetParent(MainPanel.uiTop.transform)
end

--- <summary>
--- 递归寻找子控件相对于指定父级的本地坐标
--- </summary>
--- <param name="childTrans">子控件Transform</param>
--- <param name="parentname">父级名称</param>
--- <param name="maxFindLayerCount">最大寻找的层数：默认10层</param>
--- <param name="innerUse">内部使用的计数，无需外部赋值</param>
function Util.GetRelativeParentLocalPosition(childTrans, parentname, maxFindLayerCount, innerUse)
    innerUse = innerUse or Vector3.New(0,0,0);
    maxFindLayerCount = maxFindLayerCount - 1;
    if maxFindLayerCount < 0 then
        return innerUse;
    end
    if childTrans.parent ~= nil then
        innerUse = innerUse + childTrans.localPosition;
        if childTrans.parent.name == parentname then
            return innerUse;
        else
            return Util.GetRelativeParentLocalPosition(childTrans.parent, parentname, maxFindLayerCount, innerUse);
        end
    else
        return Vector3.New(0,0,0);
    end
end

--加载地图
--mapId            地图id
--loadCompleteCB   加载完后的回调
function Util.loadMap(mapId,loadCompleteCB)
    MapManager.loadMap(mapId, loadCompleteCB)
end

--动态加载预设
--prefabName        预设名字
--loadCompleteCB    加载完成后回调
--assetName从assetbundle中加载的名字
--dontUnloadAsset 标记为不回收
--添加引用计数的数量
function Util.loadPrefab( prefabName, loadCompleteCB ,assetName, dontUnloadAsset, addRefCount)
    -- 加载路径
    local url = UrlManager.getModule(prefabName)
    -- 加载完毕回调
    local loadComplete = function(param)
        if (IsNil(param) or IsNil(param.assetbundle)) then
            return
        end
		if addRefCount then
			param:addReferenceAssetBundle(addRefCount)
		end
        if assetName == nil then assetName = prefabName end
		if dontUnloadAsset then
			param.canDestoty = false
		end
        local prefab = param.assetbundle:LoadAsset(assetName)
        if loadCompleteCB ~= nil then
            loadCompleteCB( prefab )
        end
    end
    -- 加载错误
    local loadError = function(param)
        LogManager.LogError("load " .. prefabName .. " error=" .. param.requestURl);
    end
    -- 开始异步加载
    LoadMgrLua.getInstance():LoadSceneUnity3d(url, loadComplete, nil, loadError)
end

-- 预加载UI预设
--prefabName        预设名字
--loadCompleteCB    加载完成后回调
--assetName从assetbundle中加载的名字
function Util.preLoadUI( prefabName, loadCompleteCB ,assetName)
    -- 加载完毕回调
	-- 加载路径
    local url = UrlManager.getModule(prefabName)    -- 开始异步加载
    local loadComplete = function(param)
		if (param.assetbundle == nil) then
			if loadCompleteCB ~= nil then
				loadCompleteCB()
			end
			LoadManager.getInstance ():removeResourceImmediately (param.url)
			return
		end
        if assetName == nil then assetName = prefabName end
		--WindowManager:addUIToPreLoadedList(url, assetName, param)
        if loadCompleteCB ~= nil then
            loadCompleteCB()
        end
    end
    -- 加载错误
    local loadError = function(param)
		if loadCompleteCB ~= nil then
			loadCompleteCB()
		end
        LogManager.LogError("load " .. prefabName .. " error=" .. param.requestURl);
    end
    LoadMgrLua.getInstance():LoadSceneUnity3d(url, loadComplete, "", loadError)
end

-- 预加载图片
--prefabName        预设名字
--loadCompleteCB    加载完成后回调
--assetName从assetbundle中加载的名字
function Util.preLoadTexture(assetName,loadCompleteCB)
     local url = UrlManager.getImage("mapTextures/"..assetName)    -- 开始异步加载
    local loadComplete = function(param)
        if loadCompleteCB ~= nil then
            loadCompleteCB()
        end
    end
    local loadError = function(param)
        if loadCompleteCB ~= nil then
            loadCompleteCB()
        end
    end
    LoadMgrLua.getInstance():LoadTextureFromAB(url, loadComplete, assetName, loadError)
end

function Util.loadMaterial(materialName, loadCompleteCB)
	local url = UrlManager.getMaterial(materialName)    -- 开始异步加载
    local loadComplete = function(param)
        if loadCompleteCB ~= nil then
            loadCompleteCB()
        end
    end
    local loadError = function(param)
        if loadCompleteCB ~= nil then
            loadCompleteCB()
        end
    end
    LoadMgrLua.getInstance():LoadMaterial(url, loadComplete, assetName, loadError)
end

--加载图集
--atlasName 图集名称
function Util.loadAtlas(atlasName,loadAtlasComplete)
    local _atlasPath = UrlMgr.getAtlas (atlasName)
    local function loadError(param)
        LogManager.LogError('load '..atlasName..' error='..param.requestURl)
    end
    LoadMgrLua.getInstance():LoadAtlas (_atlasPath, loadAtlasComplete,'',loadError)
end

--加载音效
--soundName  音效资源名
function Util.loadAudio(soundName,loadAudioComplete)
    local _soundPath = UrlMgr.getSound (soundName)
    local function loadError(param)
        LogManager.LogError('load '..soundName..' error='..param.requestURl)
    end
    LoadMgrLua.getInstance():LoadAudio(_soundPath, loadAudioComplete,soundName,loadError)
end


--血条显示隐藏
function Util.setBloodActive(isActive)
    local gameObject = MainPanel.uiBottom.transform.gameObject
    if IsNil(gameObject) then return end
    gameObject:SetActive(isActive)
end

function Util.isResource(itemId)
    if itemId == ConstantData.ROLE_MONEY_TYPE.GOLD
        or itemId == ConstantData.ROLE_MONEY_TYPE.MONEY
        or itemId == ConstantData.ROLE_MONEY_TYPE.BINDGOLD
        or itemId == ConstantData.ROLE_MONEY_TYPE.VIRGOR then
        return true
    end
    return false
end

--从gametext中读取text值
function Util.getTextFromCfg(indexName)
    local gametext = CFG.gametext:get(indexName)
    if gametext then
        return gametext.text,false
    end
    return '',true
end 

--从commondefine中读取value值
--isNumber：是否要转化为数值
function Util.getValueFromCfg(indexName,isNumber)
    local commonDef = CFG.commondefine:get(indexName)
    if commonDef then
        if isNumber == nil or isNumber == true then
            return tonumber(commonDef.value)
        else
            return commonDef.value
        end
    end
end
 
function Util.FunParamAhead(param, method)
    return function(...)
        return method(param, ...)
    end
end

function Util.hideItems(itemList,startIdx,endIdx)
    if itemList == nil or (startIdx > endIdx) then return end
    for i = startIdx,endIdx do
        if itemList[i] then
            itemList[i]:setActive(false)
        end
    end
end

function Util.disposeItems(itemList)
    if itemList then
        for k,v in pairs(itemList) do
            v:dispose()
        end
        itemList = nil
    end
end

function Util.ChangeDamageFormat(damageValue)
    local str=""
    if damageValue<100000 then
        str=tostring(damageValue)
    elseif damageValue>=100000 and damageValue<1000000000 then
        damageValue=math.floor(damageValue/10000)
        str=CFG.gametext:getFormatText("sourcenum_wan",tostring(damageValue))
    else
        damageValue=math.floor(damageValue/1000000000)
        str=CFG.gametext:getFormatText("sourcenum_yi",tostring(damageValue))
    end
    return str
end
