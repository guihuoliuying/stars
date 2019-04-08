-- region
-- Date    : 2016-08-18
-- Author  : daiyaorong
-- Description :  环境常量定义
-- endregion

EnvironmentDef = {}

EnvironmentDef.DEFAULT = 0 			--默认环境（无同步PVE的客户端）
EnvironmentDef.PVE_WEAKSYNC = 1 	--弱同步副本
EnvironmentDef.PVE_CLIENT = 2 		--强同步PVE模式的客户端
EnvironmentDef.PVE_SERVER = 3 		--强同步PVE模式的服务端
EnvironmentDef.PVP_CLIENT = 4 		--强同步PVP模式的客户端
EnvironmentDef.PVP_SERVER = 5 		--强同步PVP模式的服务端


EnvironmentDef.SKILL_SYNC_STRICT = 1		-- 技能必须经由服务端验证之后才可释放
EnvironmentDef.SKILL_SYNC_NORMAL = 2		-- 技能先行表现，但技能效果由服务端产生并广播
EnvironmentDef.SKILL_SYNC_LOOSE = 3			-- 技能先行表现，技能效果由客户端自己计算，只有实际的属性变化由服务端控制