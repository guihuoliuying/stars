-- 本文件由工具自动生成
-- 请勿手动修改该文件，否则所有修改都会在重新导表时被覆盖掉
-----------------------------------------------------
--[[
表名:job
对应数据源表名:job
配置描述:
配置字段描述：
job = { 
	jobid = nil,
	modelres = nil,
	pose = nil,
	roleinfoscale = nil,
	passskill = nil,    -- 被动技能
	jobtitle = nil,
}
--]]

CFG.job = { }
CFG.job.fileCount = 1
CFG.job.defaultValues = 
{}
local D = CFG.job.defaultValues
CFG.job.configs = 
{
	[1]={jobid=1,modelres=1,pose="bidle",roleinfoscale=250,passskill="141+142+143+144",jobtitle="createrolename_01",},
	[2]={jobid=2,modelres=2,pose="bidle",roleinfoscale=250,passskill="241+242+243+244",jobtitle="createrolename_02",},
	[3]={jobid=3,modelres=3,pose="bidle",roleinfoscale=250,passskill="341+342+343+344",jobtitle="createrolename_03",},
	[4]={jobid=4,modelres=4,pose="bidle",roleinfoscale=250,passskill="441+442+443+444",jobtitle="createrolename_04",},
	[5]={jobid=5,modelres=5,pose="bidle",roleinfoscale=250,passskill="541+542+543+544",jobtitle="createrolename_05",},
}

-- 根据key取配置
function CFG.job:get(key)
	if CFG.isPrintNonExistLog and key ~= 0 and self.configs[key] == nil then
		TraceLog("配置job[" .. key .. "]不存在！")
	end
	return self.configs[key]
end

-- 根据两对key来查找合并的结果
function CFG.job:getByKeys(key1,val1,key2,val2)
	if key1 == nil and val1 == nil and key2 == nil and val2 == nil then
		return nil
	end
	local result = {}
	for k,v in pairs(CFG.job.configs) do
		if v[key1] == val1 then
			if key2 == nil or v[key2] == val2 then
				table.insert(result,v)
			end
		end
	end
	return result;
end

-- 根据paramsTable中的参数来查找符合的配置如：{type = 1, sceneId = 2011}
function CFG.job:getByParams(paramsTable)
	if paramsTable == nil then
		return CFG.job.configs
	end
	local result = {}
	for k,v in pairs(CFG.job.configs) do
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
function CFG.job:getByConditionFunc(func)
	local result = {}
	for k,v in pairs(CFG.job.configs) do
		if func(v) == true then
			table.insert(result,v)
		end
	end
	return result;
end

-- 取全部配置
function CFG.job:getAll()
	return self.configs
end

-- 运行时添加新的配置进来
function CFG.job:addConfig(key, config)
	if key == nil or config == nil then return end
	if self.configs[key] ~= nil then return end
	self.configs[key] = config
end

