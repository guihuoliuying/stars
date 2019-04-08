-- 本文件由工具自动生成
-- 请勿手动修改该文件，否则所有修改都会在重新导表时被覆盖掉
-----------------------------------------------------
--[[
表名:rune
对应数据源表名:rune
配置描述:神符表
配置字段描述：
rune = { 
	runeid = nil,    -- 神符ID
	runeeffect = nil,    -- 神符特效
	pickupeffect = nil,    -- 神符拾取特效
	pickuprage = nil,    -- 拾取半径，分米
	buffid = nil,    -- 拾取神符后获得的buff
}
--]]

CFG.rune = { }
CFG.rune.fileCount = 1
CFG.rune.defaultValues = 
{}
local D = CFG.rune.defaultValues
CFG.rune.configs = 
{
	[11001]={runeid=11001,runeeffect="eff_common_rune_attack",pickupeffect="eff_common_empty",pickuprage=10,buffid=110010,},
	[11002]={runeid=11002,runeeffect="eff_common_rune_defense",pickupeffect="eff_common_empty",pickuprage=10,buffid=110020,},
	[11003]={runeid=11003,runeeffect="eff_common_rune_heal",pickupeffect="eff_common_empty",pickuprage=10,buffid=110030,},
	[410101]={runeid=410101,runeeffect="eff_common_buff_movespeed",pickupeffect="eff_common_empty",pickuprage=10,buffid=4101010,},
	[410201]={runeid=410201,runeeffect="eff_common_rune_heal",pickupeffect="eff_common_empty",pickuprage=10,buffid=4102010,},
	[410301]={runeid=410301,runeeffect="eff_common_buff_bati",pickupeffect="eff_common_empty",pickuprage=10,buffid=4103010,},
	[410401]={runeid=410401,runeeffect="eff_common_buff_exp",pickupeffect="eff_common_empty",pickuprage=10,buffid=4104010,},
}

-- 根据key取配置
function CFG.rune:get(key)
	if CFG.isPrintNonExistLog and key ~= 0 and self.configs[key] == nil then
		TraceLog("配置rune[" .. key .. "]不存在！")
	end
	return self.configs[key]
end

-- 根据两对key来查找合并的结果
function CFG.rune:getByKeys(key1,val1,key2,val2)
	if key1 == nil and val1 == nil and key2 == nil and val2 == nil then
		return nil
	end
	local result = {}
	for k,v in pairs(CFG.rune.configs) do
		if v[key1] == val1 then
			if key2 == nil or v[key2] == val2 then
				table.insert(result,v)
			end
		end
	end
	return result;
end

-- 根据paramsTable中的参数来查找符合的配置如：{type = 1, sceneId = 2011}
function CFG.rune:getByParams(paramsTable)
	if paramsTable == nil then
		return CFG.rune.configs
	end
	local result = {}
	for k,v in pairs(CFG.rune.configs) do
		local hit = true
		for k1,v1 in pairs(paramsTable) do
			if v[k1] ~= v1 then
				hit = false
				break
			end
		end
		if hit then
			table.insert(result, v)
		end
	end
	return result;
end

-- 根据指定的条件函数来查找结果，条件函数返回true的将放入结果中
function CFG.rune:getByConditionFunc(func)
	local result = {}
	for k,v in pairs(CFG.rune.configs) do
		if func(v) == true then
			table.insert(result,v)
		end
	end
	return result;
end

-- 取全部配置
function CFG.rune:getAll()
	return self.configs
end

-- 运行时添加新的配置进来
function CFG.rune:addConfig(key, config)
	if key == nil or config == nil then return end
	if self.configs[key] ~= nil then return end
	self.configs[key] = config
end

