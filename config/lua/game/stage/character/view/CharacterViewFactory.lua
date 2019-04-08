--region CharacterViewFactory.lua
--Date 2016/09/19
--Author zhouxiaogang
--Desc 角色View创建工厂类
--endregion

CharacterViewFactory = {}

local viewClsDic = 
{
	[CharacterConstant.TYPE_MONSTER] = MonsterView,
	[CharacterConstant.TYPE_NPC] = NpcView,
	[CharacterConstant.TYPE_PARTNER] = PartnerView,
	[CharacterConstant.TYPE_PLAYER] = PlayerView,
	[CharacterConstant.TYPE_SELF] = PlayerView,
	[CharacterConstant.TYPE_BABY] = BabyView,
}

function CharacterViewFactory.createView(characterType, uid, data)
	if EnvironmentHandler.isInServer then
		return nil	
	end
	if characterType == nil then
		return nil
	end
	if viewClsDic[characterType] then
		return viewClsDic[characterType].createView(uid, data)
	end
	return CharacterView.createView(uid, data)
end