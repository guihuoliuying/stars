-- 本文件由工具自动生成
-- 请勿手动修改该文件，否则所有修改都会在重新导表时被覆盖掉
-----------------------------------------------------
--[[
表名:resource
对应数据源表名:resource
配置描述:
配置字段描述：
resource = { 
	id = nil,
	model = nil,
	highmodel = nil,
	skill = nil,
	bornskill = nil,
	normalskill = nil,
	movespeed = nil,
	uiposition = nil,
	turnspeed = nil,
	headicon = nil,    -- 头像
	hitsound = nil,
	scale = nil,
	lmodel = nil,    -- 左手装备模型
	rmodel = nil,    -- 右手装备模型
	leffect = nil,    -- 左手装备特效
	reffect = nil,    -- 左手装备特效
	walksound = nil,
	walkeffect = nil,
}
--]]

CFG.resource = { }
CFG.resource.fileCount = 1
CFG.resource.defaultValues = 
{}
local D = CFG.resource.defaultValues
CFG.resource.configs = 
{
	[1]={id=1,model="hero_jianzun01",highmodel="hero_jianzun01_show",skill="101+102+103|111+112+113+114+115+116|131+132|121",bornskill="111+112+113+131+121",normalskill="101+102+103",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_jianzun01",hitsound="sound_hit_sword",scale=1000,lmodel="0",rmodel="hero_jianzun01_weapon_R01",leffect="0",reffect="0",walksound="1300|sound_walk_jianzun01+0,sound_walk_jianzun02+650",walkeffect="eff_jianzun01_run_smoke01",},
	[2]={id=2,model="hero_moke01",highmodel="hero_moke01_show",skill="201+202+203|211+212+213+214+215+216|231+232|221",bornskill="211+212+213+231+221",normalskill="201+202+203",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_moke01",hitsound="sound_hit_ink01+sound_hit_ink02+sound_hit_ink03",scale=1000,lmodel="0",rmodel="hero_moke01_weapon_R01",leffect="0",reffect="0",walksound="1300|sound_walk_moke01+0,sound_walk_moke02+650",walkeffect="eff_moke01_run_smoke01",},
	[3]={id=3,model="hero_meiying01",highmodel="hero_meiying01_show",skill="301+302+303|311+312+313+314+315+316|331+332|321",bornskill="311+312+313+331+321",normalskill="301+302+303",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_meiying01",hitsound="sound_hit_dagger01+sound_hit_dagger02+sound_hit_dagger03",scale=1000,lmodel="hero_meiying01_weapon_L01",rmodel="hero_meiying01_weapon_R01",leffect="0",reffect="0",walksound="1200|sound_walk_meiying01+0,sound_walk_meiying02+600",walkeffect="eff_meiying01_run_smoke01",},
	[4]={id=4,model="hero_nvluo01",highmodel="hero_nvluo01_show",skill="401+402+403|411+412+413+414+415+416|431+432|421",bornskill="411+412+413+431+421",normalskill="401+402+403",movespeed=60,uiposition=22,turnspeed=12,headicon="hero_nvluo01",hitsound="sound_hit_arrow_1+sound_hit_arrow_2+sound_hit_arrow_3",scale=1000,lmodel="0",rmodel="hero_nvluo01_weapon_R01",leffect="0",reffect="0",walksound="700|sound_walk_nvluo01+0,sound_walk_nvluo02+330",walkeffect="0",},
	[5]={id=5,model="hero_siming01",highmodel="hero_siming01_show",skill="501+502+503|511+512+513+514+515+516|531+532|521",bornskill="511+512+513+531+521",normalskill="501+502+503",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_siming01",hitsound="sound_skill_512_3",scale=1000,lmodel="hero_siming01_weapon_L01",rmodel="0",leffect="0",reffect="0",walksound="sound_walk_siming01+0,sound_walk_siming02+350",walkeffect="0",},
	[101]={id=101,model="hero_jianzun02",highmodel="hero_jianzun01_show",skill="101+102+103|111+112+113+114+115+116|131+132|121",bornskill="111+112+113+131+121",normalskill="101+102+103",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_jianzun01",hitsound="sound_hit_sword",scale=1000,lmodel="0",rmodel="hero_jianzun01_weapon_R01",leffect="0",reffect="0",walksound="1300|sound_walk_jianzun01+0,sound_walk_jianzun02+650",walkeffect="eff_jianzun01_run_smoke01",},
	[102]={id=102,model="hero_moke02",highmodel="hero_moke01_show",skill="201+202+203|211+212+213+214+215+216|231+232|221",bornskill="211+212+213+231+221",normalskill="201+202+203",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_moke01",hitsound="sound_hit_ink01+sound_hit_ink02+sound_hit_ink03",scale=1000,lmodel="0",rmodel="hero_moke01_weapon_R01",leffect="0",reffect="0",walksound="1300|sound_walk_moke01+0,sound_walk_moke02+650",walkeffect="eff_moke01_run_smoke01",},
	[103]={id=103,model="hero_meiying02",highmodel="hero_meiying01_show",skill="301+302+303|311+312+313+314+315+316|331+332|321",bornskill="311+312+313+331+321",normalskill="301+302+303",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_meiying01",hitsound="sound_hit_dagger01+sound_hit_dagger02+sound_hit_dagger03",scale=1000,lmodel="hero_meiying01_weapon_L01",rmodel="hero_meiying01_weapon_R01",leffect="0",reffect="0",walksound="1200|sound_walk_meiying01+0,sound_walk_meiying02+600",walkeffect="eff_meiying01_run_smoke01",},
	[104]={id=104,model="hero_nvluo02",highmodel="hero_nvluo01_show",skill="401+402+403|411+412+413+414+415+416|431+432|421",bornskill="411+412+413+431+421",normalskill="401+402+403",movespeed=60,uiposition=20,turnspeed=12,headicon="hero_nvluo01",hitsound="sound_hit_arrow_1+sound_hit_arrow_2+sound_hit_arrow_3",scale=1000,lmodel="0",rmodel="hero_nvluo01_weapon_R01",leffect="0",reffect="0",walksound="700|sound_walk_nvluo01+0,sound_walk_nvluo02+330",walkeffect="0",},
	[105]={id=105,model="hero_meiying02",highmodel="hero_siming01_show",skill="501+502+503|511+512+513+514+515+516|531+532|521",bornskill="511+512+513+531+521",normalskill="501+502+503",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_nvluo01",hitsound="sound_skill_512_3",scale=1000,lmodel="hero_siming01_weapon_L01",rmodel="0",leffect="0",reffect="0",walksound="sound_walk_siming01+0,sound_walk_siming02+350",walkeffect="0",},
	[111]={id=111,model="hero_jianzun03",highmodel="hero_jianzun01_show",skill="101+102+103|111+112+113+114+115+116|131+132|121",bornskill="111+112+113+131+121",normalskill="101+102+103",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_jianzun01",hitsound="sound_hit_sword",scale=1000,lmodel="0",rmodel="hero_jianzun01_weapon_R01",leffect="0",reffect="0",walksound="1300|sound_walk_jianzun01+0,sound_walk_jianzun02+650",walkeffect="eff_jianzun01_run_smoke01",},
	[112]={id=112,model="hero_moke03",highmodel="hero_moke01_show",skill="201+202+203|211+212+213+214+215+216|231+232|221",bornskill="211+212+213+231+221",normalskill="201+202+203",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_moke01",hitsound="sound_hit_ink01+sound_hit_ink02+sound_hit_ink03",scale=1000,lmodel="0",rmodel="hero_moke01_weapon_R01",leffect="0",reffect="0",walksound="1300|sound_walk_moke01+0,sound_walk_moke02+650",walkeffect="eff_moke01_run_smoke01",},
	[113]={id=113,model="hero_meiying03",highmodel="hero_meiying01_show",skill="301+302+303|311+312+313+314+315+316|331+332|321",bornskill="311+312+313+331+321",normalskill="301+302+303",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_meiying01",hitsound="sound_hit_dagger01+sound_hit_dagger02+sound_hit_dagger03",scale=1000,lmodel="hero_meiying01_weapon_L01",rmodel="hero_meiying01_weapon_R01",leffect="0",reffect="0",walksound="1200|sound_walk_meiying01+0,sound_walk_meiying02+600",walkeffect="eff_meiying01_run_smoke01",},
	[114]={id=114,model="hero_nvluo03",highmodel="hero_nvluo01_show",skill="401+402+403|411+412+413+414+415+416|431+432|421",bornskill="411+412+413+431+421",normalskill="401+402+403",movespeed=60,uiposition=20,turnspeed=12,headicon="hero_nvluo01",hitsound="sound_hit_arrow_1+sound_hit_arrow_2+sound_hit_arrow_3",scale=1000,lmodel="0",rmodel="hero_nvluo01_weapon_R01",leffect="0",reffect="0",walksound="700|sound_walk_nvluo01+0,sound_walk_nvluo02+330",walkeffect="0",},
	[115]={id=115,model="hero_meiying03",highmodel="hero_siming01_show",skill="501+502+503|511+512+513+514+515+516|531+532|521",bornskill="511+512+513+531+521",normalskill="501+502+503",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_nvluo01",hitsound="sound_skill_512_3",scale=1000,lmodel="hero_siming01_weapon_L01",rmodel="0",leffect="0",reffect="0",walksound="sound_walk_siming01+0,sound_walk_siming02+350",walkeffect="0",},
	[121]={id=121,model="hero_marriage02",highmodel="hero_jianzun01_show",skill="101+102+103|111+112+113+114+115+116|131+132|121",bornskill="111+112+113+131+121",normalskill="101+102+103",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_marriage02",hitsound="sound_hit_sword",scale=1000,lmodel="0",rmodel="hero_jianzun01_weapon_R01",leffect="0",reffect="0",walksound="1300|sound_walk_jianzun01+0,sound_walk_jianzun02+650",walkeffect="eff_jianzun01_run_smoke01",},
	[122]={id=122,model="hero_marriage02",highmodel="hero_moke01_show",skill="201+202+203|211+212+213+214+215+216|231+232|221",bornskill="211+212+213+231+221",normalskill="201+202+203",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_marriage02",hitsound="sound_hit_ink01+sound_hit_ink02+sound_hit_ink03",scale=1000,lmodel="0",rmodel="hero_moke01_weapon_R01",leffect="0",reffect="0",walksound="1300|sound_walk_moke01+0,sound_walk_moke02+650",walkeffect="eff_moke01_run_smoke01",},
	[123]={id=123,model="hero_marriage01",highmodel="hero_meiying01_show",skill="301+302+303|311+312+313+314+315+316|331+332|321",bornskill="311+312+313+331+321",normalskill="301+302+303",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_marriage01",hitsound="sound_hit_dagger01+sound_hit_dagger02+sound_hit_dagger03",scale=1000,lmodel="hero_meiying01_weapon_L01",rmodel="hero_meiying01_weapon_R01",leffect="0",reffect="0",walksound="1200|sound_walk_meiying01+0,sound_walk_meiying02+600",walkeffect="eff_meiying01_run_smoke01",},
	[124]={id=124,model="hero_marriage01",highmodel="hero_nvluo01_show",skill="401+402+403|411+412+413+414+415+416|431+432|421",bornskill="411+412+413+431+421",normalskill="401+402+403",movespeed=60,uiposition=20,turnspeed=12,headicon="hero_marriage01",hitsound="sound_hit_arrow_1+sound_hit_arrow_2+sound_hit_arrow_3",scale=1000,lmodel="0",rmodel="hero_nvluo01_weapon_R01",leffect="0",reffect="0",walksound="700|sound_walk_nvluo01+0,sound_walk_nvluo02+330",walkeffect="0",},
	[125]={id=125,model="hero_marriage01",highmodel="hero_siming01_show",skill="501+502+503|511+512+513+514+515+516|531+532|521",bornskill="511+512+513+531+521",normalskill="501+502+503",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_marriage01",hitsound="sound_skill_512_3",scale=1000,lmodel="hero_siming01_weapon_L01",rmodel="0",leffect="0",reffect="0",walksound="sound_walk_siming01+0,sound_walk_siming02+350",walkeffect="0",},
	[131]={id=131,model="hero_qixi02",highmodel="hero_jianzun01_show",skill="101+102+103|111+112+113+114+115+116|131+132|121",bornskill="111+112+113+131+121",normalskill="101+102+103",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_marriage02",hitsound="sound_hit_sword",scale=1000,lmodel="0",rmodel="hero_jianzun01_weapon_R01",leffect="0",reffect="0",walksound="1300|sound_walk_jianzun01+0,sound_walk_jianzun02+650",walkeffect="eff_jianzun01_run_smoke01",},
	[132]={id=132,model="hero_qixi02",highmodel="hero_moke01_show",skill="201+202+203|211+212+213+214+215+216|231+232|221",bornskill="211+212+213+231+221",normalskill="201+202+203",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_marriage02",hitsound="sound_hit_ink01+sound_hit_ink02+sound_hit_ink03",scale=1000,lmodel="0",rmodel="hero_moke01_weapon_R01",leffect="0",reffect="0",walksound="1300|sound_walk_moke01+0,sound_walk_moke02+650",walkeffect="eff_moke01_run_smoke01",},
	[133]={id=133,model="hero_qixi01",highmodel="hero_meiying01_show",skill="301+302+303|311+312+313+314+315+316|331+332|321",bornskill="311+312+313+331+321",normalskill="301+302+303",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_marriage01",hitsound="sound_hit_dagger01+sound_hit_dagger02+sound_hit_dagger03",scale=1000,lmodel="hero_meiying01_weapon_L01",rmodel="hero_meiying01_weapon_R01",leffect="0",reffect="0",walksound="1200|sound_walk_meiying01+0,sound_walk_meiying02+600",walkeffect="eff_meiying01_run_smoke01",},
	[134]={id=134,model="hero_qixi01",highmodel="hero_nvluo01_show",skill="401+402+403|411+412+413+414+415+416|431+432|421",bornskill="411+412+413+431+421",normalskill="401+402+403",movespeed=60,uiposition=20,turnspeed=12,headicon="hero_marriage01",hitsound="sound_hit_arrow_1+sound_hit_arrow_2+sound_hit_arrow_3",scale=1000,lmodel="0",rmodel="hero_nvluo01_weapon_R01",leffect="0",reffect="0",walksound="700|sound_walk_nvluo01+0,sound_walk_nvluo02+330",walkeffect="0",},
	[135]={id=135,model="hero_qixi01",highmodel="hero_siming01_show",skill="501+502+503|511+512+513+514+515+516|531+532|521",bornskill="511+512+513+531+521",normalskill="501+502+503",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_siming01",hitsound="sound_skill_512_3",scale=1000,lmodel="hero_siming01_weapon_L01",rmodel="0",leffect="0",reffect="0",walksound="sound_walk_siming01+0,sound_walk_siming02+350",walkeffect="0",},
	[141]={id=141,model="hero_jianzun04",highmodel="hero_jianzun01_show",skill="101+102+103|111+112+113+114+115+116|131+132|121",bornskill="111+112+113+131+121",normalskill="101+102+103",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_jianzun04",hitsound="sound_hit_sword",scale=1000,lmodel="0",rmodel="hero_jianzun01_weapon_R01",leffect="0",reffect="0",walksound="1300|sound_walk_jianzun01+0,sound_walk_jianzun02+650",walkeffect="eff_jianzun01_run_smoke01",},
	[142]={id=142,model="hero_jianzun04",highmodel="hero_moke01_show",skill="201+202+203|211+212+213+214+215+216|231+232|221",bornskill="211+212+213+231+221",normalskill="201+202+203",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_jianzun04",hitsound="sound_hit_ink01+sound_hit_ink02+sound_hit_ink03",scale=1000,lmodel="0",rmodel="hero_moke01_weapon_R01",leffect="0",reffect="0",walksound="1300|sound_walk_moke01+0,sound_walk_moke02+650",walkeffect="eff_moke01_run_smoke01",},
	[143]={id=143,model="hero_nvluo04",highmodel="hero_meiying01_show",skill="301+302+303|311+312+313+314+315+316|331+332|321",bornskill="311+312+313+331+321",normalskill="301+302+303",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_nvluo04",hitsound="sound_hit_dagger01+sound_hit_dagger02+sound_hit_dagger03",scale=1000,lmodel="hero_meiying01_weapon_L01",rmodel="hero_meiying01_weapon_R01",leffect="0",reffect="0",walksound="1200|sound_walk_meiying01+0,sound_walk_meiying02+600",walkeffect="eff_meiying01_run_smoke01",},
	[144]={id=144,model="hero_nvluo04",highmodel="hero_nvluo01_show",skill="401+402+403|411+412+413+414+415+416|431+432|421",bornskill="411+412+413+431+421",normalskill="401+402+403",movespeed=60,uiposition=20,turnspeed=12,headicon="hero_nvluo04",hitsound="sound_hit_arrow_1+sound_hit_arrow_2+sound_hit_arrow_3",scale=1000,lmodel="0",rmodel="hero_nvluo01_weapon_R01",leffect="0",reffect="0",walksound="700|sound_walk_nvluo01+0,sound_walk_nvluo02+330",walkeffect="0",},
	[145]={id=145,model="hero_nvluo04",highmodel="hero_siming01_show",skill="501+502+503|511+512+513+514+515+516|531+532|521",bornskill="511+512+513+531+521",normalskill="501+502+503",movespeed=60,uiposition=24,turnspeed=12,headicon="hero_nvluo04",hitsound="sound_skill_512_3",scale=1000,lmodel="hero_siming01_weapon_L01",rmodel="0",leffect="0",reffect="0",walksound="sound_walk_siming01+0,sound_walk_siming02+350",walkeffect="0",},
	[10004]={id=10004,model="hero_nvluo01",highmodel="hero_nvluo01_show",skill="401+402+403|411+412+413+414+415+416|431+432|421",bornskill="411+412+413+431+421",normalskill="401+402+403",movespeed=60,uiposition=22,turnspeed=12,headicon="hero_nvluo01",hitsound="sound_hit_arrow_1+sound_hit_arrow_2+sound_hit_arrow_3",scale=1000,lmodel="0",rmodel="hero_nvluo01_weapon_R01",leffect="0",reffect="0",walksound="700|sound_walk_nvluo01+0,sound_walk_nvluo02+330",walkeffect="0",},
}

-- 根据key取配置
function CFG.resource:get(key)
	if CFG.isPrintNonExistLog and key ~= 0 and self.configs[key] == nil then
		TraceLog("配置resource[" .. key .. "]不存在！")
	end
	return self.configs[key]
end

-- 根据两对key来查找合并的结果
function CFG.resource:getByKeys(key1,val1,key2,val2)
	if key1 == nil and val1 == nil and key2 == nil and val2 == nil then
		return nil
	end
	local result = {}
	for k,v in pairs(CFG.resource.configs) do
		if v[key1] == val1 then
			if key2 == nil or v[key2] == val2 then
				table.insert(result,v)
			end
		end
	end
	return result;
end

-- 根据paramsTable中的参数来查找符合的配置如：{type = 1, sceneId = 2011}
function CFG.resource:getByParams(paramsTable)
	if paramsTable == nil then
		return CFG.resource.configs
	end
	local result = {}
	for k,v in pairs(CFG.resource.configs) do
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
function CFG.resource:getByConditionFunc(func)
	local result = {}
	for k,v in pairs(CFG.resource.configs) do
		if func(v) == true then
			table.insert(result,v)
		end
	end
	return result;
end

-- 取全部配置
function CFG.resource:getAll()
	return self.configs
end

-- 运行时添加新的配置进来
function CFG.resource:addConfig(key, config)
	if key == nil or config == nil then return end
	if self.configs[key] ~= nil then return end
	self.configs[key] = config
end

