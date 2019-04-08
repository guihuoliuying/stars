LuaHelper = {}
function LuaHelper.setLightmapSettingsMaps(lightMapDataArr_) end
function LuaHelper.setLightmapSettingsMapsNULL() end
function LuaHelper.getLightmapSettingsMaps() end
function LuaHelper.setLightmapSettingsMode(lightModeInt_) end
function LuaHelper.setLightmapDataFar(lightMapData_, dataFar_) end
function LuaHelper.removeResourceFromLoadManager(url_) end
function LuaHelper.SetComponentEnable(behaviour_, isEnable_) end
function LuaHelper.SetCanvasScalerReferenceWH(canvasScaler_, w_, h_) end
function LuaHelper.GetKeyCodeByKeyName(keyName_) end
function LuaHelper.setTransformLocalPos(com_, x_, y_, z_) end
function LuaHelper.setTransformLocalRotation(com_, x_, y_, z_) end
function LuaHelper.setTransformLocalSize(com_, x_, y_, z_) end
function LuaHelper.setTransformLocalData(com_, localPos_, localEulerAng_, localScale_) end
function LuaHelper.setTransformLocalDataPR(com_, localPos_, localEulerAng_) end
function LuaHelper.setTransformLocalDataPS(com_, localPos_, localScale_) end
function LuaHelper.setTransformLocalDataRS(com_, localEulerAng_, localScale_) end
function LuaHelper.setImageColorFillAmount(image_, color_, fillAmount_) end
function LuaHelper.setTimeScale(value_) end
function LuaHelper.GetInputTouchPosition(numIndex_) end
function LuaHelper.GetInputTouchDeltaPosition(numIndex_) end
function LuaHelper.IsInputTouchMoved(numIndex_) end
function LuaHelper.IsInputTouchBegan(numIndex_) end
function LuaHelper.IsInputTouchEnded(numIndex_) end
function LuaHelper.GetFontCharWidth(label_, text_, fontSize_) end
function LuaHelper.GetLabelWidth(label_, text_, fontSize_) end
function LuaHelper.ResourcesLoad(path_) end
function LuaHelper.consoleLog(content_) end
function LuaHelper.consoleLogWarn(content_) end
function LuaHelper.consoleLogError(content_) end
function LuaHelper.setUIGray(go_, isGray_, hasMask_) end
function LuaHelper.isVec3Equal(vec3_, targetVec3_, precision_) end
function LuaHelper.convertHtmlStringToColor(htmlStr_) end
function LuaHelper.GetTimeStamp(year_, month_, day_, hour_, minute_, second_) end
function LuaHelper.GetTimeStampNow() end
function LuaHelper.GetDateTimeByTimeStamp(timeStamp) end
function LuaHelper.getAddedDateTime(year, month, day, hour, minute, second, addedSeconds) end
function LuaHelper.setRaycastTargetEnable(graphic_, value_) end
function LuaHelper.getFirstGoByRaycast(position_) end
function LuaHelper.getUIPosFromRoot(targetTrans) end
function LuaHelper.bindEventListener(eventDefineInt, luaFunc) end
function LuaHelper.removeEventListener(eventDefineInt, luaFunc) end
function LuaHelper.isContainTypeWithFunc(eventDefineInt, luaFunc) end
function LuaHelper.clearCache() end
function LuaHelper.getTimeFormat() end
function LuaHelper.updateEnterGameProgress(current_) end
function LuaHelper.isFileExists(path_) end
function LuaHelper.getThingPlayer() end
function LuaHelper.setGraphicQuality(level_) end
function LuaHelper.md5(data_) end
function LuaHelper.setParent(child_, parent_) end
function LuaHelper.decodeSyncVector3(posStr_) end
function LuaHelper.equalNull(obj_) end
function LuaHelper.clearGLColor(clearDepth_, clearColor_) end
function LuaHelper.IsVectorInvalid(pos_) end
function LuaHelper.getCameraCullingMask(layerName_) end
function LuaHelper.ScreenToGUIPoint(targetRect_, position_, cam_) end
function LuaHelper.setCameraClearFlags(camera_, clearFlags_) end
function LuaHelper.setViewSize(x_, y_, scale_, customCam_) end
function LuaHelper.getTargetTransPos(target_, child_) end
function LuaHelper.setQualityLevel(index_) end
function LuaHelper.IsNumber(strNumber_) end
function LuaHelper.InstantiateFromAsset(assetbundle_, loadName_, parent_, objName_) end
function LuaHelper.InstantiateGameObjectFromAsset(assetbundle_, loadName_, objName_) end
function LuaHelper.InstantiateObj(assetbundle_, loadName_) end
function LuaHelper.GetKey(key_) end
function LuaHelper.setFog(isOpen_) end
function LuaHelper.GetInt(key_) end
function LuaHelper.HasKey(key_) end
function LuaHelper.SetInt(key_, value_) end
function LuaHelper.GetString(key_) end
function LuaHelper.SetString(key_, value_) end
function LuaHelper.RemoveData(key_) end
function LuaHelper.GetType(classname_) end
function LuaHelper.getListByValue(str_) end
function LuaHelper.resourcesLoad(fileName_) end
function LuaHelper.gc() end
function LuaHelper.tryTriggerGC() end
function LuaHelper.gcByCheck() end
function LuaHelper.Quit() end
function LuaHelper.getIndexInName(str_, index_) end
function LuaHelper.Action(func_) end
function LuaHelper.VoidDelegate(func_) end
function LuaHelper.Destroy(original_) end
function LuaHelper.DestroyImmediate(obj_, allowDestroyingAssets_) end
function LuaHelper.getStrLen(str_) end
function LuaHelper.getStringLastIndexOf(str_, value_) end
function LuaHelper.getStringIndexOf(str_, value_) end
function LuaHelper.isStringContains(str_, checkStr_) end
function LuaHelper.replaceString(str_, old_, new_) end
function LuaHelper.subString(str_, begin_, end_) end
function LuaHelper.RefreshShader(assetBundle_) end
function LuaHelper.UIFill(rect_) end
function LuaHelper.UIRectOffsetZero(rect_) end
function LuaHelper.UIRectOffset(rect_, left_, right_, top_, bottom_) end
function LuaHelper.UIDock2(rect_, option_) end
function LuaHelper.UIDock(rect_, option_) end
function LuaHelper.getEmptyMaterials(len_) end
function LuaHelper.setMaterialNull(materils_, index_) end
function LuaHelper.getSubStringCount(srcStr_, subString_) end
function LuaHelper.IsInfinity(value_) end
function LuaHelper.IsVector3HasFinity(vec3_) end
function LuaHelper.AddSortorderForMaskEffect(go_, sortOrder_, isUI_, needGraphicRaycaster_) end
function LuaHelper.RemoveSortorderForMaskEffect(go_) end
function LuaHelper.calcPointIsInLine(checkX1_, checkY1_, x11_, y11_, x12_, y12_) end
function LuaHelper.CalcPointIsInSegLine(checkX1_, checkY1_, x11_, y11_, x12_, y12_, ignoreCheckInLine_, needLog_) end
function LuaHelper.IsInRangeByTypeRectangle(pos_, forwardStartPos_, forwardEndPos_, backStartPos_, backEndPos_) end
function LuaHelper.GetChildByNameRecursive(obj_, tName_) end
function LuaHelper.GetChildCollection(obj_) end
function LuaHelper.GetRootParent(obj_) end
function LuaHelper.CopyComponent(origin_, target_) end
function LuaHelper.ChangeScriptTo(origin_, target_) end
function LuaHelper.GetComponentInParent(com_, parentLevel_, searchDepth_) end
function LuaHelper.addMissComponent(go_, type_) end
function LuaHelper.AddMissComponent(go_) end
function LuaHelper.getWorldRect(rectTransform_, width_, height_) end
function LuaHelper.disposeLuaFunction(luaFunc_) end
function LuaHelper.addAnimationCurveKeyFrame(animationCurve_, keyFrameInfo) end
function LuaHelper.setAnimatorUpdateMode(animator_, updateModeInt_) end
function LuaHelper:ToString() end
function LuaHelper:Equals(obj) end
function LuaHelper:GetHashCode() end
