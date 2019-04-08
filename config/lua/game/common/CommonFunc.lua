-- region CommonFunc.lua
-- Date    : 2016-3-14
-- Author : panzhenfeng
-- Description : 公共函数, 目前的函数都为LuaHelper所转换过来的
-- endregion

CommonFunc = CommonFunc or {}

module("game/common/CommonFunc", package.seeall)

local Max = math.max;
local Ceil = math.ceil;
local temp = {};
local clamp = math.clamp;
local tempVector = Vector3.zero

--记录设置灰色了的go;

function CommonFunc.clearUserData()
    temp = {};
    temp.weakGrayDic = {};
    temp.psScaleDic = {};
end

do
    CommonFunc.clearUserData()
end

--销毁对象，并且销毁孩子
function CommonFunc.disposeObjAndChild(obj)
    if obj ~= nil then
        local allGos = ApplicationUtil.GetComponentsInChildren(obj, "Transform", true);
        if IsNil(allGos) == false then
            local itemObj = nil
            for i = 0, allGos.Length - 1 do
                itemObj = allGos[i]
                if IsNil(itemObj) == false then
                    GameObject.Destroy (itemObj.gameObject);
                end
            end
        end
        GameObject.Destroy (obj);
    end
end

--游戏退出
function CommonFunc.applicationExit()
    Application.Quit ();
end

function CommonFunc.replaceFashionAnimation (jodId,thingPlayer,resourceId)
    if IsNil(thingPlayer) or resourceId == nil then
        return
    end
    if jodId == 1 then
            --剑尊
            thingPlayer:setAnimtionCount(0,21)
            thingPlayer:replaceAnimation(resourceId,"appear","appear_empty")
            thingPlayer:replaceAnimation(resourceId,"attack1","attack1_empty")
            thingPlayer:replaceAnimation(resourceId,"attack2","attack2_empty")
            thingPlayer:replaceAnimation(resourceId,"attack3","attack3_empty")
            thingPlayer:replaceAnimation(resourceId,"avoidskill","avoidskill_empty")
            thingPlayer:replaceAnimation(resourceId,"dead","dead_empty")
            thingPlayer:replaceAnimation(resourceId,"die","die_empty")
            thingPlayer:replaceAnimation(resourceId,"fly","fly_empty")
            thingPlayer:replaceAnimation(resourceId,"standup","standup_empty")
            thingPlayer:replaceAnimation(resourceId,"hurt","hurt_empty")
            thingPlayer:replaceAnimation(resourceId,"skill1","skill1_empty")
            thingPlayer:replaceAnimation(resourceId,"skill2","skill2_empty")
            thingPlayer:replaceAnimation(resourceId,"skill3","skill3_empty")
            thingPlayer:replaceAnimation(resourceId,"skill4","skill4_empty")
            thingPlayer:replaceAnimation(resourceId,"skill5_1","skill5_1_empty")
            thingPlayer:replaceAnimation(resourceId,"skill5_2","skill5_2_empty")
            thingPlayer:replaceAnimation(resourceId,"skill5_3","skill5_3_empty")
            thingPlayer:replaceAnimation(resourceId,"skill6","skill6_empty")
            thingPlayer:replaceAnimation(resourceId,"stand","stand_empty")
            thingPlayer:replaceAnimation(resourceId,"ultskill","ultskill_empty")
            thingPlayer:replaceAnimation(resourceId,"ultskill2","ultskill2_empty")
    elseif jodId == 2 then
            --墨客
            thingPlayer:setAnimtionCount(0,18)
            thingPlayer:replaceAnimation(resourceId,"attack1","attack1_empty")
            thingPlayer:replaceAnimation(resourceId,"attack2","attack2_empty")
            thingPlayer:replaceAnimation(resourceId,"attack3","attack3_empty")
            thingPlayer:replaceAnimation(resourceId,"avoidskill","avoidskill_empty")
            thingPlayer:replaceAnimation(resourceId,"dead","dead_empty")
            thingPlayer:replaceAnimation(resourceId,"die","die_empty")
            thingPlayer:replaceAnimation(resourceId,"fly","fly_empty")
            thingPlayer:replaceAnimation(resourceId,"standup","standup_empty")
            thingPlayer:replaceAnimation(resourceId,"hurt","hurt_empty")
            thingPlayer:replaceAnimation(resourceId,"skill1","skill1_empty")
            thingPlayer:replaceAnimation(resourceId,"skill2","skill2_empty")
            thingPlayer:replaceAnimation(resourceId,"skill3","skill3_empty")
            thingPlayer:replaceAnimation(resourceId,"skill4","skill4_empty")
            thingPlayer:replaceAnimation(resourceId,"appear","appear_empty")
            -- thingPlayer:replaceAnimation(resourceId,"skill5","skill5_empty")
            thingPlayer:replaceAnimation(resourceId,"skill6","skill6_empty")
            thingPlayer:replaceAnimation(resourceId,"stand","stand_empty")
            thingPlayer:replaceAnimation(resourceId,"ultskill","ultskill_empty")
            thingPlayer:replaceAnimation(resourceId,"ultskill2","ultskill2_empty")
    elseif jodId == 3 then
            --魅影
            thingPlayer:setAnimtionCount(0,18)
            thingPlayer:replaceAnimation(resourceId,"appear","appear_empty")
            thingPlayer:replaceAnimation(resourceId,"attack1","attack1_empty")
            thingPlayer:replaceAnimation(resourceId,"attack2","attack2_empty")
            thingPlayer:replaceAnimation(resourceId,"attack3","attack3_empty")
            thingPlayer:replaceAnimation(resourceId,"avoidskill","avoidskill_empty")
            thingPlayer:replaceAnimation(resourceId,"dead","dead_empty")
            thingPlayer:replaceAnimation(resourceId,"die","die_empty")
            thingPlayer:replaceAnimation(resourceId,"fly","fly_empty")
            -- thingPlayer:replaceAnimation(resourceId,"fly1","fly1_empty")
            thingPlayer:replaceAnimation(resourceId,"hurt","hurt_empty")
            thingPlayer:replaceAnimation(resourceId,"skill1","skill1_empty")
            thingPlayer:replaceAnimation(resourceId,"skill2","skill2_empty")
            thingPlayer:replaceAnimation(resourceId,"skill3","skill3_empty")
            thingPlayer:replaceAnimation(resourceId,"skill4","skill4_empty")
            thingPlayer:replaceAnimation(resourceId,"skill5","skill5_empty")
            thingPlayer:replaceAnimation(resourceId,"skill6","skill6_empty")
            thingPlayer:replaceAnimation(resourceId,"stand","stand_empty")
            thingPlayer:replaceAnimation(resourceId,"ultskill","ultskill_empty")
            thingPlayer:replaceAnimation(resourceId,"ultskill2","ultskill2_empty")
    else
            --女萝
            thingPlayer:setAnimtionCount(0,21)
            thingPlayer:replaceAnimation(resourceId,"appear","appear_empty")
            thingPlayer:replaceAnimation(resourceId,"attack1","attack1_empty")
            thingPlayer:replaceAnimation(resourceId,"attack2","attack2_empty")
            thingPlayer:replaceAnimation(resourceId,"attack3","attack3_empty")
            thingPlayer:replaceAnimation(resourceId,"avoidskill","avoidskill_empty")
            thingPlayer:replaceAnimation(resourceId,"dead","dead_empty")
            thingPlayer:replaceAnimation(resourceId,"die","die_empty")
            thingPlayer:replaceAnimation(resourceId,"fly","fly_empty")
            thingPlayer:replaceAnimation(resourceId,"standup","standup_empty")
            thingPlayer:replaceAnimation(resourceId,"hurt","hurt_empty")
            thingPlayer:replaceAnimation(resourceId,"skill1","skill1_empty")
            thingPlayer:replaceAnimation(resourceId,"skill2","skill2_empty")
            thingPlayer:replaceAnimation(resourceId,"skill3","skill3_empty")
            thingPlayer:replaceAnimation(resourceId,"skill4","skill4_empty")
            thingPlayer:replaceAnimation(resourceId,"skill5","skill5_empty")
            thingPlayer:replaceAnimation(resourceId,"skill6","skill6_empty")
            thingPlayer:replaceAnimation(resourceId,"stand","stand_empty")
            thingPlayer:replaceAnimation(resourceId,"ultskill","ultskill_empty")
            thingPlayer:replaceAnimation(resourceId,"ultskill2_1","ultskill2_1_empty")
            thingPlayer:replaceAnimation(resourceId,"ultskill2_2","ultskill2_1_empty")
            thingPlayer:replaceAnimation(resourceId,"ultskill2_3","ultskill2_1_empty")
    end
end

--替换怪物shader为射线shader
function CommonFunc.replaceFashionShader (materials)
    --有透贴，所以不能直接替换By Jim
    if materials then
        local length=materials.Length
        local effMaterial
        local shaderName = nil
        for i=0,length-1 do
            effMaterial=materials[i]
            if IsNil(effMaterial) == false then
                shaderName = effMaterial.shader.name
                if shaderName == "YH02/monster" then
                    effMaterial.shader = MaterialsTexturePool.getShader ("YH02/rayRole");
                end
            end
        end
    end
end

function CommonFunc.replaceMonsterShader (materials)
    --有透贴，所以不能直接替换By Jim
    if materials then
        local length=materials.Length
        local effMaterial
        local shaderName = nil
        for i=0,length-1 do
            effMaterial=materials[i]
            if IsNil(effMaterial) == false then
                shaderName = effMaterial.shader.name
                if shaderName == "YH02/monster" then
                    effMaterial.shader = MaterialsTexturePool.getShader ("YH02/monster");
                end
            end
        end
    end
end

--世界坐标转换为屏幕坐标
function CommonFunc.worldToUIPoint (targetPos_)
    -- 避免不停生成新对象
    tempVector:Set(0,0,0)
    tempVector.x,tempVector.y = RoleControllerUtil.worldToUIPoint(targetPos_.x,targetPos_.y,targetPos_.z,tempVector.x,tempVector.y)
    return tempVector
end

-- <summary>
-- 添加Canvas组件，用于一些经常更新的text
-- </summary>
-- <param name="go_">Go_.</param>
function CommonFunc.addCanvas (go,order,isAddGraphicRaycaster)
    if go ~= nil then 
        local canvas = ApplicationUtil.AddMissComponent(go,"Canvas")
        canvas.overrideSorting = true;
        order = order or 0;
        canvas.sortingOrder = order;

        go.layer = LayerManager["UI"];

        if isAddGraphicRaycaster ~= nil and isAddGraphicRaycaster then
           local gr = ApplicationUtil.AddMissComponent(go,"GraphicRaycaster")
           gr.ignoreReversedGraphics = true
        end
    end
end

-- <summary>
-- 判断3d物体是否在屏幕内
-- </summary>
-- <param name="pos_">物体的世界坐标</param>
function CommonFunc.isInCamera (pos_)
    -- if GameUtil.mainCam == nil then 
    --     return false 
    -- else 
    --     pos_ = GameUtil.mainCam:WorldToScreenPoint (pos_)    
    --     if pos_.x >= -150 and pos_.x <= GameUtil.sceneWidth + 150 and pos_.y >= -150 and pos_.y <= GameUtil.sceneHeight + 150 then
    --         return true
    --     end
    -- end
    -- return false

    local p_pos = Vector3.zero
    p_pos.x,p_pos.y = RoleControllerUtil.worldToUIOrginPoint(pos_.x,pos_.y,pos_.z,p_pos.x,p_pos.y)
    return CameraManager.UIScreenRect:contaisXY(p_pos.x,p_pos.y)
end

-- <summary>
-- 判断3d物体是否在屏幕内，采用距离判断
-- </summary>
-- <param name="go_">Go_.</param>
function CommonFunc.isInCameraByDistance (pos_,centerPos_)
    local distance = Vector3.SqrMagnitudeWithoutY(pos_-centerPos_)
    return distance < 169
end

-- <summary>
-- 对窗口进行偏移以及fill模式
-- </summary>
-- <param name="go_">Go_.</param>
function CommonFunc.offectWinAndFill (go_)
    local p_rect = LuaHelper.addMissComponent(go_,"RectTransform");
    LuaHelper.UIDock (p_rect,DockOption.Fill);
    LuaHelper.UIRectOffset (p_rect,GameUtil.winOffectLeftRight, -GameUtil.winOffectLeftRight, -GameUtil.winOffectTopBottom, GameUtil.winOffectTopBottom);
end


function CommonFunc.CreateGameObject(name_) 
    if(name_ == nil)then
        return GameObject.New();
    else
        return GameObject.New(name_);
    end
end

-- <summary>
-- 对相机上的bloom效果进行增加或者禁用操作
-- </summary>
-- <returns>The bloom.</returns>
-- <param name="camGo">Cam go.</param>
-- <param name="isAdd">If set to <c>true</c> is add.</param>
function CommonFunc.opertationBloom(camGo_, isAdd_)
    local yh02Bloom = nil;
    if(isAdd_)then
        yh02Bloom = ApplicationUtil.AddMissComponent(camGo_, "YH02Bloom");
        -- LuaHelper.SetComponentEnable(yh02Bloom, false)
    else
        yh02Bloom = ApplicationUtil.GetComponent(camGo_, "YH02Bloom");
        if(yh02Bloom ~= nil)then
            -- LuaHelper.SetComponentEnable(yh02Bloom, false)
        end
    end
    return yh02Bloom;
end

-- <summary>
-- 技能大招的时候操作bloom强度
-- </summary>
-- <param name="lastTime">持续时间</param>
function CommonFunc.operationBloomBySkill(cameraObj_, lastTime_)
    local p_result = ApplicationUtil.GetComponent(cameraObj_, "YH02Bloom");
    if(p_result ~= nil)then
        p_result:operationBloomBySkill (lastTime_);
    end
end

function CommonFunc.resetBloom(cameraObj_, isRightNow_)
    local p_result = ApplicationUtil.GetComponent(cameraObj_, "YH02Bloom");
    if(p_result ~= nil)then
        p_result:resetBloom(isRightNow_);
    end
end


-- <summary>
-- 为挂有火的gameObject更换camera
-- </summary>
-- <param name="cam_">Cam_.</param>
function CommonFunc.changeCamForHuo(cam_)
    local p_lookCams = ApplicationUtil.GetComponentsInChildren(GameUtil.map.gameObject, "LookToCam", false);
    if p_lookCams then
        local len = p_lookCams.Length;
        for i = 0, len - 1 do
            local v = p_lookCams[i];
            v:setCam(cam_);
        end
    end
end


function CommonFunc.getEffectTimeLength(trans_)
    if(IsNil(trans_) == true)then
        return 0;
    end
    local p_particleSystems = ApplicationUtil.GetComponentsInChildren(trans_.gameObject, "ParticleSystem", false);
    local p_maxDuration = 0;
    local p_ps = nil;
    if p_particleSystems then
        local p_len = p_particleSystems.Length;
        for i = 0, p_len - 1 do
            p_ps = p_particleSystems[i];
            if(p_ps.enableEmission)then
                if(p_ps.loop)then
                    return -1;
                end
                local p_dunration = 0;
                if(p_ps.emissionRate <= 0)then
                    p_dunration = p_ps.startDelay + p_ps.startLifetime;
                else
                    p_dunration = p_ps.startDelay + Max(p_ps.duration, p_ps.startLifetime);
                end
                if(p_dunration > p_maxDuration)then
                    p_maxDuration = p_dunration;
                end
            end
        end
    end
    return p_maxDuration;
end

-- <summary>
-- 设置粒子缩放
-- </summary>
-- <param name="go_">粒子对象</param>
-- <param name="f_">缩放倍数</param>
function CommonFunc.setEffectScaleTo(go_, f_)
    if(IsNil(go_) ~= nil)then
        local p_particleSystems = ApplicationUtil.GetComponentsInChildren(go_, "ParticleSystem", true);
        if p_particleSystems then
            local p_len = p_particleSystems.Length;
            local v = nil;
            local psShape = nil;
            local bakData = nil;
            local psUid = nil;
            for i = 0, p_len - 1 do
                v = p_particleSystems[i];
                psUid = v:GetInstanceID();
                psShape = v.shape;
                if(temp.psScaleDic[go_]==nil)then
                    temp.psScaleDic[go_] = {};
                end

                if(temp.psScaleDic[go_][psUid] == nil)then
                    bakData = {};
                    bakData.startSize = v.startSize; 
                    bakData.startSpeed = v.startSpeed; 
                    bakData.gravityModifier = v.gravityModifier;
                    bakData.shapeLength = psShape.length;
                    bakData.shapeRadius = psShape.radius;
                    bakData.shapeBox = Vector3.New(psShape.box.x, psShape.box.y, psShape.box.z);
                    temp.psScaleDic[go_][psUid] = bakData;
                end
                bakData = temp.psScaleDic[go_][psUid];
                v.startSize = bakData.startSize * f_;
                v.startSpeed = bakData.startSpeed * f_;
                v.gravityModifier = bakData.gravityModifier * f_;
                psShape.length = bakData.shapeLength * f_;
                psShape.radius = bakData.shapeRadius * f_;
                psShape.box = bakData.shapeBox * f_;
            end
        end
    end
end




-- <summary>
-- 设置窗口在最上层，并且自动计算距离
-- </summary>
-- <param name="win_">Window.</param>
function CommonFunc.setWinPosAtLast (win_)
    local p_count = MainPanel.uiMiddle.transform.childCount;
    local p_temp = nil;
    local p_result = Vector3.zero;
    for i=p_count-1,0,-1 do
        p_temp = MainPanel.uiMiddle.transform:GetChild(i);
        if(p_temp.gameObject.activeSelf and p_temp.name ~= win_.name and p_temp.name ~= "BgLayer" and p_temp.name ~= "CurrencyWindow")then
            p_result.z = p_temp.localPosition.z - GameUtil.win_gap * 2;
            break;
        end
    end
    win_.transform.localPosition = p_result;
end

-- <summary>
-- 设置物体层次
-- </summary>
-- <param name="transForm_">Trans form.</param>
-- <param name="laName_">La name.</param>
function CommonFunc.setLayerName(transForm_, laName_)
    CommonFunc.setThingLayer (transForm_, LayerMask.NameToLayer (laName_));
end

--设置特效层级
function CommonFunc.setEffectLayer( go,newLayerIndex )
    if IsNil(go) then return end
    local p_renders = ApplicationUtil.GetComponentsInChildren(go,"Renderer",true);
    local len = p_renders.Length
    for i = 0, len - 1 do
        p_renders[i].sortingOrder = newLayerIndex;
    end
end

--获得指定特效层级
function CommonFunc.getEffectLayer(go)
    if IsNil(go) then return end
    local p_renders = ApplicationUtil.GetComponentsInChildren(go,"Renderer",true);
    if p_renders[0] and p_renders[0].sortingOrder then
        return  p_renders[0].sortingOrder;
    end
end

-- <summary>
-- 设置物体层次
-- </summary>
-- <param name="transForm_">Trans form.</param>
-- <param name="laName">La name.</param>
function CommonFunc.setThingLayer(transForm_, laIndex_)
    if transForm_.gameObject.layer == laIndex_ then return end;
    local coms = ApplicationUtil.GetComponentsInChildren(transForm_.gameObject,"Transform",true)
    if coms then
        local len = coms.Length
        for i = 0, len - 1 do
            coms[i].gameObject.layer = laIndex_;
        end
    end
end

-- <summary>
-- 为ui组件或者特效增加深度控制组件
-- </summary>
-- <param name="go_">Go.</param>
-- <param name="isUI_">If set to <c>true</c> is U.</param>
-- <param name="depth_">Depth.</param>
function CommonFunc.addUIOrder(go_, isUI_, depth_)
    local p_uidepth = ApplicationUtil.GetComponent(go_, "UIOrder");
    if(p_uidepth == nil)then
        p_uidepth = ApplicationUtil.AddComponent(go_, "UIOrder");
    end
    p_uidepth.isUI = isUI_;
    p_uidepth.order = depth_;
    return p_uidepth;
end



-- <summary>
-- 从查找出来的对象执行不销毁操作
-- </summary>
-- <param name="name_">Name.</param>
function CommonFunc.DontDestroyOnLoadByFind (name_)
    CommonFunc.DontDestroyOnLoad(GameObject.Find (name_));
end

-- <summary>
-- 设置某个对象不销毁
-- </summary>
-- <param name="go_">Go.</param>
function CommonFunc.DontDestroyOnLoad (go_)
    GameObject.DontDestroyOnLoad(go_);
end

-- <summary>
-- image组件全屏
-- </summary>
-- <param name="bg_">图片对象</param>
-- <param name="isUI_">是否是ui缩放</param>
function CommonFunc.makeImgToFullScreen (bg_, isUI_)
    if(bg_ ~= nil)then
        local rectTrans = bg_:GetComponent("RectTransform")
        if rectTrans == nil then
            -- CmdLog("CommonFunc.makeImgToFullScreen rectTransform isNil ==  "..tostring(rectTrans.name));
            return
        end
        if(isUI_)then
            local p_bgSize = rectTrans.sizeDelta;
            local p_currentWidth = p_bgSize.x * GameUtil.winScale;
            local p_currentHeight = p_bgSize.y * GameUtil.winScale;
            local p_gapWidth = p_currentWidth / GameUtil.sceneWidth;
            local p_gapHeight = p_currentHeight / GameUtil.sceneHeight;
            local p_w = Ceil(p_bgSize.x / p_gapWidth) + 2;
            local p_h = Ceil(p_bgSize.y / p_gapHeight) + 2;
            rectTrans.sizeDelta = Vector2.New(p_w, p_h);
        else
            rectTrans.sizeDelta = Vector2.New(GameUtil.sceneWidth + 2, GameUtil.sceneHeight + 2);
        end
    end
end

-- 将图片等比例放大到全屏大小
-- rectTrans 要缩放的RectTransform
-- useWinScale 是否计算窗口缩放（界面内图片需要，未参与winScale计算的不需要）,默认为true
function CommonFunc.makeImgScaleToFullScreen(rectTrans, useWinScale)
	local bgSize = rectTrans.sizeDelta
	if useWinScale == nil or useWinScale then
		bgSize.x = bgSize.x * GameUtil.winScale
		bgSize.y = bgSize.y * GameUtil.winScale
	end
	local offsetX = Screen.width - bgSize.x
	local offsetY = Screen.height - bgSize.y
	local scale = 1
	if offsetX > 0 or offsetY > 0 then
		if offsetX > offsetY then
			scale = Screen.width / bgSize.x
		else
			scale = Screen.height / bgSize.y
		end
		rectTrans.localScale = Vector3.New(scale, scale, 1)
	end
end

-- <summary>
-- 获取模型的高度和宽度
-- </summary>
-- <returns>The modle height width.</returns>
-- <param name="player_">Player.</param>
function CommonFunc.getModleHeightWidth(player_)
    local p_smr = ApplicationUtil.GetComponentInChildren(player_.gameObject, "SkinnedMeshRenderer");
    local p_result = Vector2.one;
    if (p_smr == nil)then
        return p_result;
    end
    p_result.x = (p_smr.bounds.size.x * player_.transform.localScale.x);
    p_result.y = (p_smr.bounds.size.y * player_.transform.localScale.y);
    return p_result;
end


function CommonFunc.setImageColor(image_, color_)
    image_.color = color_;
end


-- <summary>
-- 背景全屏适应
-- </summary>
-- <param name="bg_"></param>
function CommonFunc.setImgSize(bg_, w_, h_)
    if(bg_ ~= nil)then
        local p_image = ApplicationUtil.GetComponent(bg_, "Image");
        if(p_image ~= nil) then
            p_image.rectTransform.sizeDelta = Vector2.New(w_, h_);
        end
    end
end

function CommonFunc.GetParticleSystemsInChildren(obj_)
    if(IsNil(obj_)~=nil)then
        return ApplicationUtil.GetComponentsInChildren(obj_.gameObject, "ParticleSystem", true);
    end
    return nil;
end

-- <summary>
-- Instantiate Object
-- </summary>
-- <param name="original_"></param>
-- <returns></returns>
function CommonFunc.Instantiate (original_)
    if IsNil(original_) == false then
        return GameObject.Instantiate (original_);
    end
    return nil
end

-- <summary>
-- 设置父对象，保持孩子的值为原始值
-- </summary>
-- <param name="child_">Child.</param>
-- <param name="parent_">Parent.</param>
function CommonFunc.SetParent (child_, parent_)
    -- local p_tranformTa = child_.transform;
    -- local id = TransUtil.addObject(p_tranformTa)
    -- TransUtil.setParent(id,parent_)
    -- TransUtil.setLocation(id,0,0,0)
    -- TransUtil.setRotation(id,0,0,0)
    -- TransUtil.setScale(id,1,1,1)
    -- TransUtil.disposeObject(id)
    if IsNil(child_) then return end
    LuaHelper.setParent(child_.transform,parent_)
end

    
     
-- <summary>
-- 根据名称查找GameObject，如果需要查找不激活的物体，请使用完整路径。
-- </summary>
-- <param name="name_">如果不是如 aa/bb/cc 这样的路径，就直接用查找</param>
-- <returns></returns>
function CommonFunc.FindEx(name_)
    if(StringUtils.isEmptyString(name_))then
        return nil;
    end
    --如果没有路径，找不到隐藏物体的;
    if (not LuaHelper.isStringContains(name_, "/"))then
        return GameObject.Find(name_);
    end
    --尝试用路径查找;
    local findStartIndex = LuaHelper.getStringLastIndexOf(name_, "/");
    local p_path = LuaHelper.subString(name_, 0, findStartIndex);
    --找出父节点;
    local p_go = GameObject.Find (p_path);
    if(IsNil(p_go))then
        return p_go;
    end
    --抽取具体名称;
    local strLength = LuaHelper.getStrLen(name_);
    findStartIndex = findStartIndex + 1;
    name_ = LuaHelper.subString(name_, findStartIndex, strLength - findStartIndex);
    return p_go.transform:FindChild(name_).gameObject;
end


-- <summary>
-- 遍历直接孩子,并调用回调接口;
-- </summary>
-- <param name="parent_"></param>
-- <param name="eachFn_"></param>
function CommonFunc.ForeachChild(parent_, eachCallback_)
    local p_pr = parent_.transform;
    local count = p_pr.childCount;
    local p_child = nil;
    for i=0,count-1 do
        p_child = p_pr:GetChild(i);
        if(eachCallback_ ~= nil)then
            eachCallback_(i, p_child.gameObject);
        end
    end
end

-- <summary>
-- 对传入对象进行根据屏幕分辨率的缩放
-- </summary>
-- <param name="tran_"></param>
function CommonFunc.AutoScale(tran_)
    if(tran_ == nil)then
        return;
    end
    tran_.localScale = Vector3.New(GameUtil.winScale, GameUtil.winScale, GameUtil.winScale);
end


function CommonFunc.getGameObjectMaterial(go_)
    return ApplicationUtil.GetComponent(go_, "Renderer").sharedMaterial;
end


--缓存gameObject对应的特效组件, 对应接口setEffectIsPlaying;
local goEffectCompsDic = {};
setmetatable(goEffectCompsDic, {__mode="k"});

--设置特效是否在播放;
--支持Animator, ParticleSystem, ConrolShaderTime;
function CommonFunc.setEffectIsPlaying(go_, isPlaying_, isRestart_)
    if true then
        return 
    end
    if(IsNil(go_))then
        return;
    end
    if(isRestart_)then
        go_:SetActive(false);
        go_:SetActive(true);
    end
    local tmpComs = goEffectCompsDic[go_];
    if(tmpComs== nil)then
        tmpComs = {};
        tmpComs.psArr = ApplicationUtil.GetComponentsInChildren(go_, "ParticleSystem", true);
        tmpComs.aniArr = ApplicationUtil.GetComponentsInChildren(go_, "Animator", true);
        tmpComs.cstArr = ApplicationUtil.GetComponentsInChildren(go_, "ControlShaderTime", true);
        goEffectCompsDic[go_] = tmpComs;
    end
    local tmpValue = 1;
    if(isPlaying_==false)then
        tmpValue = 0;
    end
    local tmpItem = nil;
    --判断粒子系统;
    if tmpComs.psArr then
        for i = 0, tmpComs.psArr.Length - 1 do
            tmpComs.psArr[i].playbackSpeed = tmpValue;
        end
    end
    --判断动画系统;
    if tmpComs.aniArr then
        for i = 0, tmpComs.aniArr.Length - 1 do
            tmpComs.aniArr[i].speed = tmpValue;
        end
    end
    --判断ControlShaderTime;
    if tmpComs.cstArr then
        for i = 0, tmpComs.cstArr.Length - 1 do
            tmpComs.cstArr[i].isPlaying = isPlaying_;
        end
    end
end


--用于缓存go与ingoreTimeScale脚本的对应关系;
local goIngoreTimeScaleComDic = {};
setmetatable(goIngoreTimeScaleComDic, {__mode = "k"});

-- <summary>
-- 设置粒子系统/动画的播放状态;
-- </summary>
-- <param name="component">Component.</param>
-- <param name="isPlaying_">If set to <c>true</c> is playing.</param>
function CommonFunc.setIngoreTimeScalePlaying (go_, isPlaying_, isRestart_)
    local p_ignoreTimeScale = goIngoreTimeScaleComDic[go_];
    if(IsNil(p_ignoreTimeScale)==true)then
        p_ignoreTimeScale = ApplicationUtil.AddMissComponent(go_, "IngoreTimeScale");
        goIngoreTimeScaleComDic[go_] = p_ignoreTimeScale;
    end
    p_ignoreTimeScale:setIsPlaying(isPlaying_);
     if(isRestart_)then
         p_ignoreTimeScale:reStart();
     end
end


-- <summary>
-- 激活特效的矩形遮挡;
-- 注意: 在UI大小改变，位置改变时都需要进行一次调用;
-- useWinScale_： 以WindowManager打开的窗口设置为true,其他的若未手动设置UI缩放请设置为false
-- </summary>
-- <param name="transform_"></param>
function CommonFunc.setChildUIEffectMask(transform_, useWinScale_)
    local func = function()
        if IsNil(transform_) then return end
        --[[ 这个计算没有必要，因为游戏窗口已经按1136*640的比例进行了自适应
        local width = GameUtil.sceneWidth;
        local height = GameUtil.sceneHeight;
        local designWidth = GameUtil.UI_WIDTH;
        local designHeight = GameUtil.UI_HEIGHT;
        local s1 = designWidth / designHeight;
        local s2 = width / height;
        local contentScale = 1.0;
        if(s1 > s2)then
            contentScale = s1 / s2;
        end
        --]]
        local pos = transform_.position;
        local rendererArr = ApplicationUtil.GetComponentsInChildren(transform_.gameObject, "Renderer", true);
        local rectTransform = ApplicationUtil.GetComponent(transform_.gameObject, "RectTransform");
        local minX = 0;
        local minY = 0;
        local maxX = 0;
        local maxY = 0;
        local pixelScale = MainPanel.uiRoot.transform.localScale.x;
        if(useWinScale_)then
            pixelScale = pixelScale * GameUtil.winScale;
        end
        --GameLog("zls","pixelScale:",pixelScale)
        minX = pos.x - rectTransform.pivot.x * rectTransform.sizeDelta.x * pixelScale;
        minY = pos.y - rectTransform.pivot.y * rectTransform.sizeDelta.y * pixelScale;
        maxX = minX + rectTransform.rect.width * pixelScale;
        maxY = minY + rectTransform.rect.height * pixelScale;
        if rendererArr then
            local tmpMaterial = nil;
            local len = rendererArr.Length;

            for i = 0, len - 1 do
                local v = rendererArr[i];
                tmpMaterial = v.sharedMaterial;

                tmpMaterial:SetFloat ("_MinX", minX);
                tmpMaterial:SetFloat ("_MinY", minY);
                tmpMaterial:SetFloat ("_MaxX", maxX);
                tmpMaterial:SetFloat ("_MaxY", maxY);
                --[[
                tmpMaterial:SetFloat ("_MinX", minX / contentScale);
                tmpMaterial:SetFloat ("_MinY", minY / contentScale);
                tmpMaterial:SetFloat ("_MaxX", maxX / contentScale);
                tmpMaterial:SetFloat ("_MaxY", maxY / contentScale);
                --]]
            end
        end
    end
    -- 延时两帧是因为特效和材质球是分离的，第二次创建完成回调时材质还未赋值进来
    FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_UI):RegisterLuaCallBack(2, 1, func)
end

-- <summary>
-- 将某个UI设置为灰色
-- </summary>
-- <param name="go_">一个GameObject,GameObject下的所有图片、文本都会受影响</param>
-- <param name="isGray_">是否置灰</param>
-- <param name="hasMask_">是否为带有Mask组件对象的子级</param>
function CommonFunc.setUIGray (go_, isGray_, hasMask_)
    if IsNil(go_) then return end
    temp.weakGrayDic[go_] = isGray_;
    LuaHelper.setUIGray(go_, isGray_, hasMask_);
    --判断直接子对象是否有不可设置灰色的对象;
    local cannotgrayTrans = go_.transform:FindChild(ConstantData.CANNOTGRAYNAME);
    if(not IsNil(cannotgrayTrans))then
        LuaHelper.setUIGray(cannotgrayTrans.gameObject, false, hasMask_);
    end
end

--注意,这个是配合setUIGray使用的, 不推荐不确定的情况下去使用这个接口;
function CommonFunc.isUIGray(go_)
    return temp.weakGrayDic[go_] == true;
end


-- <summary>
-- 禁掉某个UI的点击、拖动等事件
-- </summary>
-- <param name="go_"></param>
-- <param name="isEnable_">是否禁掉事件</param>
function CommonFunc.setUIEnableClick (go_, isEnable_)
    local events = ApplicationUtil.GetComponentsInChildren(go_, "EventTriggerListener", true);
    if events then
        local len = events.Length;
        for i = 0, len - 1 do
            local v = events[i];
            LuaHelper.SetComponentEnable(v, isEnable_)
        end
    end
end

-- 开启或禁用UIScale效果
function CommonFunc.enableUIScale(go, isEnable)
    local scaleComps = ApplicationUtil.GetComponentsInChildren(go, "UScale", true)
    if scaleComps then
        local len = scaleComps.Length;
        for i = 0, len - 1 do
            local v = scaleComps[i];
            LuaHelper.SetComponentEnable(v, isEnable)
        end
    end
end


function CommonFunc.InstantiateLocal (original_, parent_, pos_)
    local p_tranformTa = original_.transform;
    if(pos_ == nil or pos_ == Vector3.zero)then
        pos_ = p_tranformTa.localPosition;
    end
    local _rota = p_tranformTa.localRotation;
    local scale = p_tranformTa.localScale;
    local _clone = GameObject.Instantiate (original_);
    local p_transform = _clone.transform;
    if(IsNil(parent_)==false)then
        _clone.transform:SetParent (parent_.transform);
    end
    p_transform.localPosition = pos_;
    p_transform.localScale = scale;
    p_transform.localRotation = _rota;
    return _clone;
end

-- <summary>
-- 
-- </summary>
-- <param name="original_"></param>
-- <param name="parent_"></param>
-- <returns></returns>
function CommonFunc.InstantiateGlobal(original_, parent_)
    local p_tranformTa = original_.transform;
    local p_pos = p_tranformTa.position;
    local p_rota = p_tranformTa.rotation;
    local p_scale = p_tranformTa.localScale;
    local p_clone = GameObject.Instantiate (original_);
    local p_transform = p_clone.transform;
     if(IsNil(parent_)==false)then
        p_clone.transform:SetParent (parent_.transform);
    end
    p_transform.position = p_pos;
    p_transform.localScale = p_scale;
    p_transform.rotation = p_rota;
    return p_clone;
end


-- <summary>
-- 设置父对象
-- </summary>
-- <param name="child_"></param>
-- <param name="parent_"></param>
function CommonFunc.SetParentForOldValue(child_, parent_)
    local p_tranformTa = child_.transform;
    local p_pos = p_tranformTa.localPosition;
    local p_rota = p_tranformTa.localRotation;
    local p_scale = p_tranformTa.localScale;
    child_.transform:SetParent(parent_.transform);
    p_tranformTa.localPosition = p_pos;
    p_tranformTa.localScale = p_scale;
    p_tranformTa.localRotation = p_rota;
end

-- 销毁trans_下的所有子gameObject, 不包括trans_自己
-- trans_:Transform
function CommonFunc.DestroyAllChildren(trans_)
    if trans_ == nil then return end
    for i = trans_.childCount - 1, 0, -1 do
        Object.Destroy(trans_:GetChild(i).gameObject)
    end
end

function CommonFunc.SetTimeScale(value_)
    --约束下值;
    if(value_ < 0)then
        value_ = 0;
    end
    if(value_ > 100)then
        value_ = 100;
    end
    LuaHelper.setTimeScale(value_);
end

-- 给UI添加一个长按事件
-- uiGo_:GameObject
-- callback_:长按回调,参数(GameObject, isPress)
-- pressTime_:按住多久算长按(秒), 可不传，默认为0.2秒
-- cancelDistance_:按住时uiGo_位置变化多大视为取消长按(像素)，一般用于可拖动的列表, 可不传，默认为0,即忽略这个条件
function CommonFunc.AddUIPressEvent(uiGo_, callback_, pressTime_, cancelDistance_)
    if uiGo_ == nil or callback_ == nil then
        return
    end
    pressTime_ = pressTime_ or 0.2
    cancelDistance_ = cancelDistance_ or 0
    local comp = ApplicationUtil.AddComponent(uiGo_, "UIPressEvent")
    comp:init(callback_, pressTime_, cancelDistance_);
    return comp
end

--Description：查找子节点;
--parentTrans_: 父亲的transform
--childPath_: 子节点的相对父亲路径
--typeName_: 类型名;
function CommonFunc.FindChild(parentTrans_, childPath_, typeName_)
    if(typeName_ == nil)then
        return parentTrans_:FindChild(childPath_);
    else
        return parentTrans_:FindChild(childPath_):GetComponent(typeName_);
    end
end

function CommonFunc.setTextNotEnoughColor(label_, value_, max_, color_)
    color_ = color_ or "#ff0000";
    if(value_>=max_)then
        label_.text = value_.."/"..max_;
    else
        label_.text = "<color='"..color_.."'>"..value_.."</color>/"..max_;
    end
end

function CommonFunc.setTextEnoughOrNotColor(label_, value_, max_, color_NEnough, color_Enough)
    colorred_ = color_NEnough or "#ff0000";
    colorgreen_ = color_Enough or "#00ff00";
    if(value_>=max_)then
        label_.text = "<color='"..colorgreen_.."'>"..value_.."</color>/"..max_;
    else
        label_.text = "<color='"..colorred_.."'>"..value_.."</color>/"..max_;
    end
end

function CommonFunc.setTextIsNotEnoughColor(label_, value_, color_)
    color_ = color_ or "#ff0000";
    label_.text = "<color='"..color_.."'>"..value_.."</color>";
end

function CommonFunc.setTextColor(label_, text_, colorLevel_)
    label_.text = "<color="..CommonFunc.getQualityColor(colorLevel_)..">"..text_.."</color>";
end

function CommonFunc.changeStringtoColor(str)
    str=string.gsub(str,"#","")
    local count=string.len(str)/2
    local colorTable={}
    for i=0,count-1 do
        local color = tonumber("0x"..string.sub(str,i*2+1,i*2+2))
        colorTable[i+1]=color/255
    end
   
    local count=#colorTable
    if count==4 then
        return Color.New(colorTable[1],colorTable[2],colorTable[3],colorTable[4])
    else
        return Color.New(colorTable[1],colorTable[2],colorTable[3])
    end
end


-- 格式化显示大数值（用于货币、经验等之类的数值显示）
-- return xxx亿 or xxx万 or xxxx
function CommonFunc.formatBigNumber(value)
    if value >= 1000000000 then
        value = math.floor(value / 100000000)
        value = CFG.gametext:getFormatText("sourcenum_yi", tostring(value))
    elseif value >= 100000 then
        value = math.floor(value / 10000)
        value = CFG.gametext:getFormatText("sourcenum_wan", tostring(value))
    else
        value = tostring(value)
    end
    return value
end

-- 格式化显示大数值（用于货币、经验等之类的数值显示）
-- return xxx亿 or xxx万 or xxxx
function CommonFunc.formatBigNumber2(value)
    if value >= 1000000000 then
        value = math.floor(value / 100000000)
        value = CFG.gametext:getFormatText("sourcenum_yi", tostring(value))
    elseif value >= 10000 then
        value = math.floor(value / 10000)
        value = CFG.gametext:getFormatText("sourcenum_wan", tostring(value))
    else
        value = tostring(value)
    end
    return value
end

function CommonFunc.showOrHideAllWin(visible, exceptGo)
    local mainUITrans = MainPanel.uiMiddle.transform
    local position = mainUITrans.localPosition
    if visible then
        if position.x > -100000 then
            position.x = position.x - 100000
            mainUITrans.localPosition = position
            if exceptGo then
                exceptGo.transform.localPosition = exceptGo.transform.localPosition + Vector3.New(100000, 0, 0)
            end
            if CommonTips.window then
                CommonTips.window.transform.localPosition = CommonTips.window.transform.localPosition + Vector3.New(100000, 0, 0)
            end
        end
    else
        if position.x <= -100000 then
            position.x = position.x + 100000
            mainUITrans.localPosition = position
            if exceptGo then
                exceptGo.transform.localPosition = exceptGo.transform.localPosition + Vector3.New(-100000, 0, 0)
            end
            if CommonTips.window then
                CommonTips.window.transform.localPosition = CommonTips.window.transform.localPosition + Vector3.New(-100000, 0, 0)
            end
        end
    end
end

--获取品质对应的颜色值;
function CommonFunc.getQualityColor(quality_, needRgb_)
    if(temp.qualitycolor==nil)then
        temp.qualitycolor = {};
        local tmpValue = CFG.commondefine:get("qualitycolor").value;
        local tmpArr = StringUtils.split(tmpValue, "|");
        local itemArr = nil;
        for i=1,#tmpArr do
            itemArr = StringUtils.split(tmpArr[i], "+");
            temp.qualitycolor[tonumber(itemArr[1])] = itemArr[2];
        end
        temp.qualitycolorMin = 1;
        temp.qualitycolorMax = #tmpArr;
    end
    --约束范围;
    quality_ = clamp(quality_ or 1, temp.qualitycolorMin or 1, temp.qualitycolorMax or 1);
    local htmlColorStr = temp.qualitycolor[quality_];
    if(needRgb_)then
        return LuaHelper.convertHtmlStringToColor(htmlColorStr);
    end
    return htmlColorStr;
end

--返回带品质颜色的字符串
--text:字符串文本，quality:品质
function CommonFunc.getQualityColorText(text , quality)
    if text == nil or text == "" then return "" end;
    if(temp.qualitycolor==nil) then
        temp.qualitycolor = {};
        local tmpValue = CFG.commondefine:get("qualitycolor").value;
        local tmpArr = StringUtils.split(tmpValue, "|");
        local itemArr = nil;
        for i=1,#tmpArr do
            itemArr = StringUtils.split(tmpArr[i], "+");
            temp.qualitycolor[tonumber(itemArr[1])] = itemArr[2];
        end
        temp.qualitycolorMin = 1;
        temp.qualitycolorMax = #tmpArr;
    end
    if temp.qualitycolor[quality] then
        return "<color=" .. temp.qualitycolor[quality] .. ">" .. text .. "</color>"
    else
        return text
    end
end

function CommonFunc.isInRange(value_, min_, max_)
    if(value_ >= min_ and value_ <= max_)then
        return true;
    end
    return false;
end

--添加一个定时器
--timerType    : 帧定时器类型,可参照ConstantData.FRAME_xxx常量
--intervalTime : 定时器执行的间隔时间
--loopTimes    : 循环次数,填0表示无限次循环
--callback     : 定时回调函数
function CommonFunc.addFrameTimer(frameType,intervalTime,loopTimes,callback)
    frameType = frameType or ConstantData.FRAME_EVENT_UI
    intervalTime = (intervalTime <= 0) and 1 or intervalTime
    loopTimes = (loopTimes < 0) and 0 or loopTimes
    return FrameTimerManager.getInstance(frameType):RegisterLuaCallBack(intervalTime, loopTimes,callback)
end

--添加一个定时器
--timerType    : 帧定时器类型,可参照ConstantData.FRAME_xxx常量
--intervalTime : 定时器执行的间隔时间
--loopTimes    : 循环次数,填0表示无限次循环
--callback     : 定时回调函数
--endCallback  : 循环完成后的回调
function CommonFunc.addFrameTimerWithEnd(frameType,intervalTime,loopTimes,callback,endCallback)
    frameType = frameType or ConstantData.FRAME_EVENT_UI
    intervalTime = (intervalTime <= 0) and 1 or intervalTime
    loopTimes = (loopTimes < 0) and 0 or loopTimes
    return FrameTimerManager.getInstance(frameType):RegisterLuaCallBackWithEnd(intervalTime, loopTimes,callback,endCallback)
end

--添加一个UI定时器
--intervalTime : 定时器执行的间隔时间
--loopTimes    : 循环次数,填0表示无限次循环
--callback     : 定时回调函数
function CommonFunc.addUIFrameTimer(intervalTime,loopTimes,callback)
    intervalTime = (intervalTime <= 0) and 1 or intervalTime
    loopTimes = (loopTimes < 0) and 0 or loopTimes
    return FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_UI):RegisterLuaCallBack(intervalTime, loopTimes,callback)
end

--添加一个角色定时器
--intervalTime : 定时器执行的间隔时间
--loopTimes    : 循环次数,填0表示无限次循环
--callback     : 定时回调函数
function CommonFunc.addCharacFrameTimer(intervalTime,loopTimes,callback)
    intervalTime = (intervalTime <= 0) and 1 or intervalTime
    loopTimes = (loopTimes < 0) and 0 or loopTimes
    return FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUACHARAC):RegisterLuaCallBack(intervalTime, loopTimes,callback)
end

--移除定时器
function CommonFunc.removeFrameTimer(frameType,funcKey)
    if funcKey then
        FrameTimerManager.getInstance(frameType):removeLuaFunc(funcKey)
    end
end

--迭代属性,保证输出的顺序和配置的一致性;
--attrArr 属性的englishName的组;
--callback 每次顺序迭代回调;callback(attrEnglishName)
--isInnerDecode 是否内部解析，如果是内部解析的话, 那么attrArr要写为字符串形式,支持"hp=1,mp=3.."这样的字符串
function CommonFunc.iteratorAttr(attrArr, callback, isInnerDecode)
    local count = RoleModel.getAttrLength();
    local attrEnglishName = "";
    --判断是否要求内部解析;
    local attrValueTb = nil;
    if(isInnerDecode)then
        local itemArr = StringUtils.split(attrArr, ",");
        local tmpArr = nil;
        attrArr = {};
        attrValueTb = {};
        for i=1,#itemArr do
            tmpArr = StringUtils.split(itemArr[i], "=");
            attrValueTb[tmpArr[1]] = tonumber(tmpArr[2]);
            table.insert(attrArr, tmpArr[1]);
        end
    end
    for i=1, count do
        attrEnglishName = RoleModel.getAttrEnglishNameByIndex(i);
        for k=1, #attrArr do
            if(attrEnglishName == attrArr[k])then
                if(attrValueTb)then
                    callback(attrEnglishName, attrValueTb[attrEnglishName]);
                else
                    callback(attrEnglishName);
                end
                break;
            end
        end
    end
end

function CommonFunc.iteratorAttrKvMap(attrMap, callback)
    local count = RoleModel.getAttrLength();
    local attrEnglishName = "";
    for i=1, count do
        attrEnglishName = RoleModel.getAttrEnglishNameByIndex(i);
        for k,v in pairs(attrMap) do
            if(attrEnglishName == k)then
                callback(attrEnglishName, v);
                break;
            end
        end
    end
end

function CommonFunc.createOneUIEffect(effectName, parent, param, isActive,loadOverCallback , autoDispose, is3D, dialog) --{effectScale=4, pos=self.vt.diceArr[i].localPosition}, false)
    local effectInfo = CFG.effectinfo:get(effectName);
    if(effectInfo)then
       if(effectInfo.effectresource == "0")then
           return nil;
       end
    end
    param = param or {}
    local pos = param.pos or Vector3(parent.localPosition.x,parent.localPosition.y,0)
    local effect = UIEffect:create()
    effect:init(effectName , is3D , dialog, autoDispose)
    effect:setLoadOverCallBack(function()
        if effect == nil or IsNil(parent) then
            if effect then
                effect:dispose()
                effect = nil
            end
            return
        end
        effect:setVisible(isActive, pos, parent)
        if(param.scale)then
            effect:setScale(Vector3(param.scale,param.scale,param.scale))
        end
        if(param.effectScale)then
            effect:setEffectScale(param.effectScale);
        end
    end)
    effect:setVisible(isActive, pos, parent)
    if(param.scale)then
        effect:setScale(Vector3(param.scale,param.scale,param.scale))
    end
    if(param.effectScale)then
        effect:setEffectScale(param.effectScale);
    end
    if loadOverCallback then
        effect:setLoadOverCallBack(loadOverCallback);
    end
    return effect;
end

--显示UIEffect特效
--effectList : 用于存特效的表，由模块自己管理
--effectName : ui特效名字
--parent     : ui特效的父节点
--param      : 参数表，结构为{pos=Vector3(x,y,z),scale=x} 可不传,默认param.pos的值为parent的localpostion,param.scale=1
--isActive   : 是否创建立即显示,可不传,默认为true
--is3D       : 是否需要创建3dui特效,可不传，默认为false，默认创建2d特效
function CommonFunc.createUIEffect(effectList,effectName,parent,param,isActive,is3D, loadOverCallback)
    if effectList == nil or effectName == nil or IsNil(parent) then return end
    param = param or {}
    local pos = param.pos or Vector3(parent.localPosition.x,parent.localPosition.y,0)
    local scale = param.scale or 1
    if isActive == nil then
        isActive = true
    end
    if is3D == nil then
        is3D = false
    end
    local effect = effectList[effectName]
    if effect == nil or not effect:isValid() then
        effect = UIEffect:create()
        effect:init(effectName,is3D)
        effect:setLoadOverCallBack(function()
            if effect ~= nil and IsNil(parent) == false then
                effect:setVisible(isActive, pos, parent)
                effect:setScale(Vector3(scale,scale,scale))
                effectList[effectName] = effect
            end
        end)
        effect:setVisible(isActive, pos, parent)
        effect:setScale(Vector3(scale,scale,scale))
        effectList[effectName] = effect
        effect:setLoadOverCallBack(loadOverCallback)
    else
        effect:setVisible(false,pos,parent)
        effect:setVisible(isActive,pos,parent)
        effect:setScale(Vector3(scale,scale,scale))
    end
    return effect
end

--移除ui特效
--effectList ：自己模块定义的特效列表
--effectName ：指定删除的特效，如果不传则表示将effectList全部清空
function CommonFunc.removeUIEffect(effectList,effectName)
    if effectList == nil then return end
    if effectName == nil then
        for k,v in pairs(effectList) do
            v:dispose()
        end
    else
        if effectList[effectName] then
            effectList[effectName]:dispose()
        end
    end
end

--获取特效时间
function CommonFunc.getEffTimeLen( effGameObj )
    if IsNil(effGameObj) == true then return end
    local com = effGameObj:GetComponent("ThingMono")
    if com ~= nil then
        --防止特效由于误差无法播放完毕, 这里加了个ConstantData.FRAME_DELTA_TIME, by:panzhenfeng;
        if(com.effectTimeLen>0)then
            return com.effectTimeLen + ConstantData.FRAME_DELTA_TIME;
        end
        return com.effectTimeLen;
    end
end

-- 给UI加上点击事件
-- uiGo ui对象
-- func 点击函数
-- clickGapTime 点击间隔时间
function CommonFunc.AddUIClickEvent(uiGo, func, clickGapTime)
    local event = EventTriggerListener.Get(uiGo)
    event.onClickLua = func
    if clickGapTime then
        event.clickGapTime = clickGapTime
    end
end

--解析物品的配置
--itemStr  : 'itemId+count,itemId+count,itemId+count...'
--返回结构 : {{itemId=10016,count=12},{itemId=10016,count=12},{itemId=10016,count=12}...}
function CommonFunc.parseItemStr(itemStr,split)
    if itemStr == nil or itemStr == '0' or itemStr =='' then
        return
    end
    if split == nil then split = ',' end
    local itemStrTb = StringUtils.split(itemStr,split)
    if itemStrTb == nil then return end
    local itemList = {}
    for i = 1,#itemStrTb do
        local itemTb = StringUtils.split(itemStrTb[i],'+')
        if itemTb == nil then return end
        local itemId = tonumber(itemTb[1] or 1)
        local count  = tonumber(itemTb[2] or 1)
        itemList[#itemList+1] = {itemId=itemId,count=count}
    end
    return itemList
end


--保证要打乱;
function CommonFunc.randomArray(arr, len, mustDifferent)
    if(mustDifferent == nil)then
        mustDifferent = true;
    end
    local cloneArr = tableplus.shallowcopy(arr);
    local dataLen = #arr;
    for i=1, len do
        local randomIndex1 = math.random(1, dataLen);
        local randomIndex2 = math.random(1, dataLen);
        local preValue = arr[randomIndex1];
        arr[randomIndex1] = arr[randomIndex2];
        arr[randomIndex2] = preValue;
    end
    --判断是否和原来还是一样的;
    if(mustDifferent)then
        for i=1, len do
            if(cloneArr[i] ~= arr[i])then
                return arr;
            end
        end
        return CommonFunc.randomArray(arr, len, mustDifferent);
    else
        return arr;
    end
end

--因为策划填是以左下角做判断;
function CommonFunc.convertLeftDownPosToTargetCoord(leftDownPos)
    local rtnPos = Vector3.New(leftDownPos.x - GameUtil.UI_WIDTH/2, leftDownPos.y - GameUtil.UI_HEIGHT/2, leftDownPos.z);
    return rtnPos;
end

--将UI坐标转换到左下角,让服务器可以进行验证;
function CommonFunc.convertTargetCoordPosToLeftDownPos(targetCoordPos)
    local rtnPos = Vector3.New(targetCoordPos.x + GameUtil.UI_WIDTH/2, targetCoordPos.y + GameUtil.UI_HEIGHT/2, 0);
    return rtnPos; 
end

function CommonFunc.convertActivityFlowConn(activityId, step)
    local activityflowArr = CFG.activityflow:getByKeys("activityid", activityId, "step", step);
    if(activityflowArr and #activityflowArr>0)then
        local activityflow = activityflowArr[1];
        local cronexprArr = StringUtils.split(activityflow.cronexpr, " ");
        local hour = tonumber(cronexprArr[3]);
        local min = tonumber(cronexprArr[2]);
        local str = "";
        str = str..hour.."点";
        if(min > 0)then
            str = str..min.."分";
        end
        return str;
    end
    return nil;
end

function CommonFunc.vec3MulVec3(a, b)
    local rtn = Vector3.one;
    rtn.x = a.x * b.x;
    rtn.y = a.y * b.y;
    rtn.z = a.z * b.z;
    return rtn;
end

--计算基础属性值
--attrBase : {'hp'=2,'mp'=0.5,....}属性名可查AttrEnum
function CommonFunc.calcaulateBaseFightScore(attrBase)
    if attrBase == nil then
        return 0
    else
        local config = StringUtils.split(CFG.commondefine:getValue("battlepowerratio") , "|" , nil , function (value)
            return StringUtils.split(value , "=")
        end)
        local configTab = {}
        if config then 
            for i,v in ipairs(config) do
                configTab[v[1]] = tonumber(v[2])
            end
        else 
            return 0
        end
        local total = 0
        for k,v in pairs(attrBase) do
            if v and configTab[k] then
                total = total + v * configTab[k]
            end
        end
        -- for i=1, ATTR_COUNT-1 do
        --     total = total + attrBase[AttrEnum[i]]*configTab[AttrEnum[i]]
        -- end
        return total
    end
end

--获取触摸点数
function CommonFunc.getTouchCount()
    local count = 0
    if GameUtil.isMobilePlatform then
        count = Input.touchCount
    else
        if Input.GetMouseButton(0) or Input.GetMouseButtonDown(0) then
            count = 1
        end
    end
    return count
end

--获取触摸位置
function CommonFunc.getTouchPoint()
    local touchPoint = Vector3(0,0,0)
    if GameUtil.isMobilePlatform then
        local count = CommonFunc.getTouchCount()
        for i = 0,count-1 do
            touchPoint = Input.GetTouch(i).position
            touchPoint = Vector3(touchPoint.x,touchPoint.y,0)
        end
    else
        if Input.GetMouseButton(0) or Input.GetMouseButtonDown(0) then
            touchPoint = Input.mousePosition
        end
    end
    return touchPoint
end

function CommonFunc.getPointByTouchId(touchId)
    local touchPoint = Vector3(0,0,0)
    if GameUtil.isMobilePlatform then
        local count = CommonFunc.getTouchCount()
        for i = 0,count-1 do
            local touch = Input.GetTouch(i)
            if touch.fingerId == touchId then
                touchPoint = Input.GetTouch(i).position
                touchPoint = Vector3(touchPoint.x,touchPoint.y,0)
            end
        end
    else
        if Input.GetMouseButton(0) or Input.GetMouseButtonDown(0) then
            touchPoint = Input.mousePosition
        end
    end
    return touchPoint
end

function CommonFunc.killTweenArr(comArr)
    for i=1, #comArr do
        if(not IsNil(comArr[i]))then
            GameTween.DOKill(comArr[i], false);    
        end
    end
end

--创建一个框框特效;
function CommonFunc.createBorderEffect(parent, effectName, subGoName)
    effectName = effectName or "inducteff_areamask";
    subGoName = subGoName or "1111111111 (0-00-00-00)";
    local createBorderSingleEffect = function(locPos, endPos, locRotation)
        local uiEffect = UIEffect:create()
        local loadCompleteCallback = function (params)
            if uiEffect then
                local effectGo = uiEffect:getGameObject()
                local effectTrans = effectGo.transform:FindChild(subGoName)
                uiEffect:setVisible(true , nil, parent);
                effectTrans.localPosition = params.locPos;
                effectTrans.localRotation = params.locRotation;
                uiEffect:setLocation(params.endPos);
            end
        end
        local params = {locPos=locPos, locRotation=locRotation, endPos=endPos};
        uiEffect:setLoadOverCallBack(makeHandle(params, loadCompleteCallback));
        uiEffect:init(effectName , false , nil , false);
        return uiEffect;
    end
    local sizeDelta = parent.transform.sizeDelta;
    local parentRectTrans = parent.transform:GetComponent("RectTransform");
    local posX = sizeDelta.x*parentRectTrans.pivot.x;
    local posY = sizeDelta.y*parentRectTrans.pivot.y;
    local effectArr = {};
    effectArr[1] = createBorderSingleEffect(Vector3.New(12 , -12 , 0), Vector3.New(-posX , posY , 0), Quaternion.Euler (0, 0, 0));
    effectArr[2] = createBorderSingleEffect(Vector3.New(-12 , -12 , 0), Vector3.New(posX , posY , 0), Quaternion.Euler (0, 0, 270));
    effectArr[3] = createBorderSingleEffect(Vector3.New(12 , 12 , 0), Vector3.New(-posX , -posY , 0), Quaternion.Euler (0, 0, 90));
    effectArr[4] = createBorderSingleEffect(Vector3.New(-12 , 12 , 0), Vector3.New(posX , -posY , 0), Quaternion.Euler (0, 0, 180));
    return effectArr;
end


function CommonFunc.setEffectIgnoreTimeScale(go, value)
    --控制动画;
    local animatorArr = ApplicationUtil.GetComponentsInChildren(go, "Animator", true);
    if animatorArr then
        local len = animatorArr.Length;
        local tmpAni = nil;
        local updateModeInt = 2;
        if(not value)then
            updateModeInt = 0;
        end
        for i = 0, len - 1 do
            tmpAni = animatorArr[i];
            LuaHelper.setAnimatorUpdateMode(tmpAni, updateModeInt);
        end
    end
    --控制粒子;
    local psArr = ApplicationUtil.GetComponentsInChildren(go, "ParticleSystem", true);
    if psArr then
        local len = psArr.Length;
        local tmpPs = nil;
        local psitsc = nil;
        for i = 0, len - 1 do
            tmpPs = psArr[i];
            psitsc = ApplicationUtil.AddMissComponent(tmpPs.gameObject, "ParticleSystemIngoreTimeScale");
            LuaHelper.SetComponentEnable(psitsc, value);
        end
    end
end