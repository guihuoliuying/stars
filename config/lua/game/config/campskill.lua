-- 本文件由工具自动生成
-- 请勿手动修改该文件，否则所有修改都会在重新导表时被覆盖掉
-----------------------------------------------------
--[[
表名:campskill
对应数据源表名:campskill
配置描述:
配置字段描述：
campskill = { 
	id = nil,
	skillid = nil,    -- 技能id
	type = nil,    -- 技能类型
	desc = nil,    -- 技能获得时描述
	odds = nil,
	opencondition = nil,    -- 技能生效条件
	condition = nil,
}
--]]

CFG.campskill = { }
CFG.campskill.fileCount = 1
CFG.campskill.defaultValues = 
{	a0 = "1+1+999",
}
local D = CFG.campskill.defaultValues
CFG.campskill.configs = 
{
	[1]={id=1,skillid="400101|400102|400103|400104|400105",type=1,desc="camp_bowbattle_lvupdesc1|camp_bowbattle_lvupdesc2|camp_bowbattle_lvupdesc3|camp_bowbattle_lvupdesc4|camp_bowbattle_lvupdesc5",odds=50,opencondition=D.a0,condition=0,},
	[2]={id=2,skillid="400201|400202|400203",type=1,desc="camp_bowbattle_lvupdesc6|camp_bowbattle_lvupdesc7|camp_bowbattle_lvupdesc8",odds=50,opencondition=D.a0,condition=0,},
	[3]={id=3,skillid="400301|400302|400303",type=1,desc="camp_bowbattle_lvupdesc11|camp_bowbattle_lvupdesc12|camp_bowbattle_lvupdesc13",odds=50,opencondition=D.a0,condition=0,},
	[4]={id=4,skillid="400401|400402|400403",type=1,desc="camp_bowbattle_lvupdesc16|camp_bowbattle_lvupdesc17|camp_bowbattle_lvupdesc18",odds=50,opencondition=D.a0,condition=0,},
	[5]={id=5,skillid="400501|400502|400503",type=1,desc="camp_bowbattle_lvupdesc21|camp_bowbattle_lvupdesc22|camp_bowbattle_lvupdesc23",odds=50,opencondition=D.a0,condition=0,},
	[6]={id=6,skillid="400701|400702|400703",type=1,desc="camp_bowbattle_lvupdesc31|camp_bowbattle_lvupdesc32|camp_bowbattle_lvupdesc33",odds=50,opencondition=D.a0,condition=0,},
	[7]={id=7,skillid="400801|400802|400803",type=1,desc="camp_bowbattle_lvupdesc36|camp_bowbattle_lvupdesc37|camp_bowbattle_lvupdesc38",odds=50,opencondition=D.a0,condition=0,},
	[8]={id=8,skillid="400901|400902|400903",type=1,desc="camp_bowbattle_lvupdesc41|camp_bowbattle_lvupdesc42|camp_bowbattle_lvupdesc43",odds=50,opencondition=D.a0,condition=0,},
	[9]={id=9,skillid="420101|420102|420103",type=3,desc="camp_bowbattle_lvupdesc46|camp_bowbattle_lvupdesc47|camp_bowbattle_lvupdesc48",odds=50,opencondition=D.a0,condition=0,},
	[10]={id=10,skillid="420201|420202|420203",type=3,desc="camp_bowbattle_lvupdesc51|camp_bowbattle_lvupdesc52|camp_bowbattle_lvupdesc53",odds=50,opencondition=D.a0,condition=0,},
	[11]={id=11,skillid="420301|420302|420303",type=3,desc="camp_bowbattle_lvupdesc56|camp_bowbattle_lvupdesc57|camp_bowbattle_lvupdesc58",odds=50,opencondition=D.a0,condition=0,},
	[12]={id=12,skillid="420401|420402|420403",type=3,desc="camp_bowbattle_lvupdesc61|camp_bowbattle_lvupdesc62|camp_bowbattle_lvupdesc63",odds=50,opencondition=D.a0,condition=0,},
	[13]={id=13,skillid="420501|420502|420503",type=3,desc="camp_bowbattle_lvupdesc66|camp_bowbattle_lvupdesc67|camp_bowbattle_lvupdesc68",odds=50,opencondition=D.a0,condition=0,},
	[14]={id=14,skillid="420601|420602|420603",type=3,desc="camp_bowbattle_lvupdesc71|camp_bowbattle_lvupdesc72|camp_bowbattle_lvupdesc73",odds=50,opencondition=D.a0,condition=0,},
	[15]={id=15,skillid="420701|420702|420703",type=3,desc="camp_bowbattle_lvupdesc76|camp_bowbattle_lvupdesc77|camp_bowbattle_lvupdesc78",odds=50,opencondition=D.a0,condition=0,},
	[16]={id=16,skillid="420801|420802|420803",type=3,desc="camp_bowbattle_lvupdesc81|camp_bowbattle_lvupdesc82|camp_bowbattle_lvupdesc83",odds=50,opencondition=D.a0,condition=0,},
	[17]={id=17,skillid="420901|420902|420903",type=3,desc="camp_bowbattle_lvupdesc86|camp_bowbattle_lvupdesc87|camp_bowbattle_lvupdesc88",odds=50,opencondition=D.a0,condition=0,},
	[18]={id=18,skillid="421001|421002|421003",type=3,desc="camp_bowbattle_lvupdesc91|camp_bowbattle_lvupdesc92|camp_bowbattle_lvupdesc93",odds=50,opencondition=D.a0,condition=0,},
	[19]={id=19,skillid="421101",type=3,desc="camp_bowbattle_lvupdesc96|camp_bowbattle_lvupdesc97|camp_bowbattle_lvupdesc98",odds=50,opencondition=D.a0,condition=0,},
	[20]={id=20,skillid="421201",type=3,desc="camp_bowbattle_lvupdesc101|camp_bowbattle_lvupdesc102|camp_bowbattle_lvupdesc103",odds=50,opencondition=D.a0,condition=0,},
	[21]={id=21,skillid="421301",type=3,desc="camp_bowbattle_lvupdesc106|camp_bowbattle_lvupdesc107|camp_bowbattle_lvupdesc108",odds=50,opencondition=D.a0,condition=0,},
	[22]={id=22,skillid="421401",type=3,desc="camp_bowbattle_lvupdesc111|camp_bowbattle_lvupdesc112|camp_bowbattle_lvupdesc113",odds=50,opencondition=D.a0,condition=0,},
	[23]={id=23,skillid="421501|421502|421503",type=3,desc="camp_bowbattle_lvupdesc116|camp_bowbattle_lvupdesc117|camp_bowbattle_lvupdesc118",odds=50,opencondition=D.a0,condition=0,},
	[24]={id=24,skillid="421601|421602|421603",type=3,desc="camp_bowbattle_lvupdesc121|camp_bowbattle_lvupdesc122|camp_bowbattle_lvupdesc123",odds=50,opencondition=D.a0,condition=0,},
	[25]={id=25,skillid="421701|421702|421703",type=3,desc="camp_bowbattle_lvupdesc126|camp_bowbattle_lvupdesc127|camp_bowbattle_lvupdesc128",odds=50,opencondition=D.a0,condition=0,},
	[26]={id=26,skillid="421801|421802|421803",type=3,desc="camp_bowbattle_lvupdesc136|camp_bowbattle_lvupdesc137|camp_bowbattle_lvupdesc138",odds=50,opencondition=D.a0,condition=1,},
	[27]={id=27,skillid="421901|421902|421903",type=3,desc="camp_bowbattle_lvupdesc141|camp_bowbattle_lvupdesc142|camp_bowbattle_lvupdesc143",odds=50,opencondition=D.a0,condition=1,},
	[28]={id=28,skillid="440101",type=2,desc="camp_bowbattle_lvupdesc131",odds=50,opencondition=D.a0,condition=0,},
	[29]={id=29,skillid="440201",type=2,desc="camp_bowbattle_lvupdesc132",odds=50,opencondition=D.a0,condition=0,},
	[30]={id=30,skillid="440301",type=2,desc="camp_bowbattle_lvupdesc133",odds=50,opencondition=D.a0,condition=0,},
	[31]={id=31,skillid="440401",type=4,desc="camp_bowbattle_lvupdesc134",odds=50,opencondition=D.a0,condition=0,},
}

-- 根据key取配置
function CFG.campskill:get(key)
	if CFG.isPrintNonExistLog and key ~= 0 and self.configs[key] == nil then
		TraceLog("配置campskill[" .. key .. "]不存在！")
	end
	return self.configs[key]
end

-- 根据两对key来查找合并的结果
function CFG.campskill:getByKeys(key1,val1,key2,val2)
	if key1 == nil and val1 == nil and key2 == nil and val2 == nil then
		return nil
	end
	local result = {}
	for k,v in pairs(CFG.campskill.configs) do
		if v[key1] == val1 then
			if key2 == nil or v[key2] == val2 then
				table.insert(result,v)
			end
		end
	end
	return result;
end

-- 根据paramsTable中的参数来查找符合的配置如：{type = 1, sceneId = 2011}
function CFG.campskill:getByParams(paramsTable)
	if paramsTable == nil then
		return CFG.campskill.configs
	end
	local result = {}
	for k,v in pairs(CFG.campskill.configs) do
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
function CFG.campskill:getByConditionFunc(func)
	local result = {}
	for k,v in pairs(CFG.campskill.configs) do
		if func(v) == true then
			table.insert(result,v)
		end
	end
	return result;
end

-- 取全部配置
function CFG.campskill:getAll()
	return self.configs
end

-- 运行时添加新的配置进来
function CFG.campskill:addConfig(key, config)
	if key == nil or config == nil then return end
	if self.configs[key] ~= nil then return end
	self.configs[key] = config
end

