-- 本文件由工具自动生成
-- 请勿手动修改该文件，否则所有修改都会在重新导表时被覆盖掉
-----------------------------------------------------
--[[
表名:runespawn
对应数据源表名:runespawn
配置描述:神符刷新点配置表
配置字段描述：
runespawn = { 
	runespawnid = nil,    -- 刷新点的唯一ID
	stagemap = nil,    -- 刷新点特效
	position = nil,    -- 刷新点位置
	spawntime = nil,    -- 刷新时间，时+分+秒，24小时制
	rechargetime = nil,    -- 神符补充时间
	rune = nil,    -- 神符列表，神符1+权重,神符2+权重,...
}
--]]

CFG.runespawn = { }
CFG.runespawn.fileCount = 1
CFG.runespawn.defaultValues = 
{	a0 = "eff_common_empty",
	a1 = "410101+25,410201+150,410301+25,410401+650",
}
local D = CFG.runespawn.defaultValues
CFG.runespawn.configs = 
{
	[1]={runespawnid=1,stagemap=D.a0,position="37+5+0",spawntime="0",rechargetime=30,rune="11001+50,11002+50,11003+50",},
	[2]={runespawnid=2,stagemap=D.a0,position="37+5+120",spawntime="0",rechargetime=45,rune="11002+50,11003+50",},
	[3]={runespawnid=3,stagemap=D.a0,position="37+5+-120",spawntime="0",rechargetime=45,rune="11002+50,11003+50",},
	[4]={runespawnid=4,stagemap=D.a0,position="-28+37+-48",spawntime="0",rechargetime=45,rune="11001+50,11002+50,11003+50",},
	[5]={runespawnid=5,stagemap=D.a0,position="-28+37+53",spawntime="0",rechargetime=45,rune="11001+50,11002+50,11003+50",},
	[6]={runespawnid=6,stagemap=D.a0,position="36+3+0",spawntime="0",rechargetime=45,rune="11001+50,11002+50,11003+50",},
	[7]={runespawnid=7,stagemap=D.a0,position="42+37+-48",spawntime="0",rechargetime=45,rune="11001+50,11002+50,11003+50",},
	[8]={runespawnid=8,stagemap=D.a0,position="42+37+53",spawntime="0",rechargetime=45,rune="11001+50,11002+50,11003+50",},
	[9]={runespawnid=9,stagemap=D.a0,position="723+162+474",spawntime="0",rechargetime=15,rune=D.a1,},
	[10]={runespawnid=10,stagemap=D.a0,position="756+162+352",spawntime="0",rechargetime=15,rune=D.a1,},
	[11]={runespawnid=11,stagemap=D.a0,position="789+162+401",spawntime="0",rechargetime=15,rune=D.a1,},
	[12]={runespawnid=12,stagemap=D.a0,position="704+162+401",spawntime="0",rechargetime=15,rune=D.a1,},
	[13]={runespawnid=13,stagemap=D.a0,position="799+162+534",spawntime="0",rechargetime=15,rune=D.a1,},
	[14]={runespawnid=14,stagemap=D.a0,position="732+162+576",spawntime="0",rechargetime=15,rune=D.a1,},
	[15]={runespawnid=15,stagemap=D.a0,position="580+203+819",spawntime="0",rechargetime=15,rune=D.a1,},
	[16]={runespawnid=16,stagemap=D.a0,position="608+203+782",spawntime="0",rechargetime=15,rune=D.a1,},
	[17]={runespawnid=17,stagemap=D.a0,position="575+203+645",spawntime="0",rechargetime=15,rune=D.a1,},
	[18]={runespawnid=18,stagemap=D.a0,position="667+203+712",spawntime="0",rechargetime=15,rune=D.a1,},
	[19]={runespawnid=19,stagemap=D.a0,position="598+203+595",spawntime="0",rechargetime=15,rune=D.a1,},
	[20]={runespawnid=20,stagemap=D.a0,position="568+203+717",spawntime="0",rechargetime=15,rune=D.a1,},
	[21]={runespawnid=21,stagemap=D.a0,position="447+203+809",spawntime="0",rechargetime=15,rune=D.a1,},
	[22]={runespawnid=22,stagemap=D.a0,position="478+203+760",spawntime="0",rechargetime=15,rune=D.a1,},
	[23]={runespawnid=23,stagemap=D.a0,position="516+203+804",spawntime="0",rechargetime=15,rune=D.a1,},
	[24]={runespawnid=24,stagemap=D.a0,position="373+182+556",spawntime="0",rechargetime=15,rune=D.a1,},
	[25]={runespawnid=25,stagemap=D.a0,position="336+182+511",spawntime="0",rechargetime=15,rune=D.a1,},
	[26]={runespawnid=26,stagemap=D.a0,position="305+182+556",spawntime="0",rechargetime=15,rune=D.a1,},
	[27]={runespawnid=27,stagemap=D.a0,position="412+182+516",spawntime="0",rechargetime=15,rune=D.a1,},
	[28]={runespawnid=28,stagemap=D.a0,position="443+182+467",spawntime="0",rechargetime=15,rune=D.a1,},
	[29]={runespawnid=29,stagemap=D.a0,position="438+211+273",spawntime="0",rechargetime=15,rune=D.a1,},
	[30]={runespawnid=30,stagemap=D.a0,position="407+211+322",spawntime="0",rechargetime=15,rune=D.a1,},
	[31]={runespawnid=31,stagemap=D.a0,position="300+211+365",spawntime="0",rechargetime=15,rune=D.a1,},
	[32]={runespawnid=32,stagemap=D.a0,position="331+211+316",spawntime="0",rechargetime=15,rune=D.a1,},
	[33]={runespawnid=33,stagemap=D.a0,position="349+211+170",spawntime="0",rechargetime=15,rune=D.a1,},
	[34]={runespawnid=34,stagemap=D.a0,position="318+211+219",spawntime="0",rechargetime=15,rune=D.a1,},
	[35]={runespawnid=35,stagemap=D.a0,position="424+211+176",spawntime="0",rechargetime=15,rune=D.a1,},
	[36]={runespawnid=36,stagemap=D.a0,position="455+211+127",spawntime="0",rechargetime=15,rune=D.a1,},
	[37]={runespawnid=37,stagemap=D.a0,position="370+211+258",spawntime="0",rechargetime=15,rune=D.a1,},
	[38]={runespawnid=38,stagemap=D.a0,position="635+202+247",spawntime="0",rechargetime=15,rune=D.a1,},
	[39]={runespawnid=39,stagemap=D.a0,position="665+202+93",spawntime="0",rechargetime=15,rune=D.a1,},
	[40]={runespawnid=40,stagemap=D.a0,position="655+202+150",spawntime="0",rechargetime=15,rune=D.a1,},
	[41]={runespawnid=41,stagemap=D.a0,position="572+202+231",spawntime="0",rechargetime=15,rune=D.a1,},
	[42]={runespawnid=42,stagemap=D.a0,position="576+202+105",spawntime="0",rechargetime=15,rune=D.a1,},
	[43]={runespawnid=43,stagemap=D.a0,position="614+202+192",spawntime="0",rechargetime=15,rune=D.a1,},
	[44]={runespawnid=44,stagemap=D.a0,position="639+202+310",spawntime="0",rechargetime=15,rune=D.a1,},
	[45]={runespawnid=45,stagemap=D.a0,position="626+202+89",spawntime="0",rechargetime=15,rune=D.a1,},
	[46]={runespawnid=46,stagemap=D.a0,position="700+202+154",spawntime="0",rechargetime=15,rune=D.a1,},
	[47]={runespawnid=47,stagemap=D.a0,position="572+202+301",spawntime="0",rechargetime=15,rune=D.a1,},
}

-- 根据key取配置
function CFG.runespawn:get(key)
	if CFG.isPrintNonExistLog and key ~= 0 and self.configs[key] == nil then
		TraceLog("配置runespawn[" .. key .. "]不存在！")
	end
	return self.configs[key]
end

-- 根据两对key来查找合并的结果
function CFG.runespawn:getByKeys(key1,val1,key2,val2)
	if key1 == nil and val1 == nil and key2 == nil and val2 == nil then
		return nil
	end
	local result = {}
	for k,v in pairs(CFG.runespawn.configs) do
		if v[key1] == val1 then
			if key2 == nil or v[key2] == val2 then
				table.insert(result,v)
			end
		end
	end
	return result;
end

-- 根据paramsTable中的参数来查找符合的配置如：{type = 1, sceneId = 2011}
function CFG.runespawn:getByParams(paramsTable)
	if paramsTable == nil then
		return CFG.runespawn.configs
	end
	local result = {}
	for k,v in pairs(CFG.runespawn.configs) do
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
function CFG.runespawn:getByConditionFunc(func)
	local result = {}
	for k,v in pairs(CFG.runespawn.configs) do
		if func(v) == true then
			table.insert(result,v)
		end
	end
	return result;
end

-- 取全部配置
function CFG.runespawn:getAll()
	return self.configs
end

-- 运行时添加新的配置进来
function CFG.runespawn:addConfig(key, config)
	if key == nil or config == nil then return end
	if self.configs[key] ~= nil then return end
	self.configs[key] = config
end

