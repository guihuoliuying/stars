-- 本文件由工具自动生成
-- 请勿手动修改该文件，否则所有修改都会在重新导表时被覆盖掉
-----------------------------------------------------
--[[
表名:fcd
对应数据源表名:fcd
配置描述:
配置字段描述：
fcd = { 
	parameter = nil,
	value = nil,
}
--]]

CFG.fcd = { }
CFG.fcd.fileCount = 1
CFG.fcd.defaultValues = 
{}
local D = CFG.fcd.defaultValues
CFG.fcd.configs = 
{
	["bosscoming"]={parameter="bosscoming",value="1000",},
	["bosscomingshake"]={parameter="bosscomingshake",value="2+100",},
	["bosscoming_music"]={parameter="bosscoming_music",value="sound_ui_switch",},
	["buddyposition"]={parameter="buddyposition",value="10",},
	["combofadetime"]={parameter="combofadetime",value="3000",},
	["combonumberfadetime"]={parameter="combonumberfadetime",value="500",},
	["combonumberpopsize"]={parameter="combonumberpopsize",value="1300",},
	["combonumberpoptime"]={parameter="combonumberpoptime",value="300",},
	["defaultcamera"]={parameter="defaultcamera",value="165+135+35+45+25+6",},
	["dragonballmaxdistance"]={parameter="dragonballmaxdistance",value="50",},
	["dragonballposition"]={parameter="dragonballposition",value="0+20+-10|10+20+-10,-10+20+-10|0+20+-10,10+15+-15,-10+15+-15|10+20+-10,-10+20+-10,20+15+-15,-20+15+-15|0+20+-10,10+15+-15,-10+15+-15,20+10+-20,-20+10+-20|10+20+-10,-10+20+-10,20+15+-15,-20+15+-15,30+10+-20,-30+10+-20",},
	["dragonballspeed"]={parameter="dragonballspeed",value="20",},
	["fightarea"]={parameter="fightarea",value="50",},
	["fightinterval"]={parameter="fightinterval",value="300",},
	["floatinghitspeedY"]={parameter="floatinghitspeedY",value="5",},
	["ghostshadow"]={parameter="ghostshadow",value="200+255+600",},
	["Gravitation"]={parameter="Gravitation",value="300",},
	["hiteffect"]={parameter="hiteffect",value="eff_common_hit",},
	["hitflash"]={parameter="hitflash",value="200",},
	["hitflyheight"]={parameter="hitflyheight",value="5",},
	["impactsound"]={parameter="impactsound",value="battle_swordhit",},
	["login_music"]={parameter="login_music",value="bgm_login_guzheng_dizi",},
	["losecontrol"]={parameter="losecontrol",value="1000",},
	["newguidedungeon"]={parameter="newguidedungeon",value="10101",},
	["shadowdamage"]={parameter="shadowdamage",value="100",},
	["slowmotion"]={parameter="slowmotion",value="100",},
	["tokenactivity"]={parameter="tokenactivity",value="0+10001+10002+10003+10004+10005|1+10001+10002+10003+10004+10005|2+10001+10002+10003+10004+10005",},
}

-- 根据key取配置
function CFG.fcd:get(key)
	if CFG.isPrintNonExistLog and key ~= '0' and self.configs[key] == nil then
		TraceLog("配置fcd[" .. key .. "]不存在！")
	end
	return self.configs[key]
end

-- 根据两对key来查找合并的结果
function CFG.fcd:getByKeys(key1,val1,key2,val2)
	if key1 == nil and val1 == nil and key2 == nil and val2 == nil then
		return nil
	end
	local result = {}
	for k,v in pairs(CFG.fcd.configs) do
		if v[key1] == val1 then
			if key2 == nil or v[key2] == val2 then
				table.insert(result,v)
			end
		end
	end
	return result;
end

-- 根据paramsTable中的参数来查找符合的配置如：{type = 1, sceneId = 2011}
function CFG.fcd:getByParams(paramsTable)
	if paramsTable == nil then
		return CFG.fcd.configs
	end
	local result = {}
	for k,v in pairs(CFG.fcd.configs) do
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
function CFG.fcd:getByConditionFunc(func)
	local result = {}
	for k,v in pairs(CFG.fcd.configs) do
		if func(v) == true then
			table.insert(result,v)
		end
	end
	return result;
end

-- 取全部配置
function CFG.fcd:getAll()
	return self.configs
end

-- 运行时添加新的配置进来
function CFG.fcd:addConfig(key, config)
	if key == nil or config == nil then return end
	if self.configs[key] ~= nil then return end
	self.configs[key] = config
end

