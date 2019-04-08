-- region
-- Date    : 2016-08-18
-- Author  : daiyaorong
-- Description :
-- endregion

--function setIndex(name)
--end
CFG = {}

-- 根据路径加载脚本并设置索引
function loadByIndex(index, path)
--	--print("index===="..index)
	require(path);
    -- 设置索引
--    setIndex(index);
end

-- Util集合
loadByIndex(1, "game/modules/fightModule/figggConstantStr")
loadByIndex(2001, "game/util/Math");
loadByIndex(9, "game/util/Queue")

loadByIndex(3051,"game/manager/ModuleConstant")
loadByIndex(3052,"game/manager/ModuleEvent")
loadByIndex(3054, "CodeOverrideInclude")
-- 网络
loadByIndex(1, "net/PacketType")

loadByIndex(1, "game/modules/fightModule/environment/EnvironmentDef")
loadByIndex(1, "game/modules/fightModule/environment/EnvironmentHandler")
EnvironmentHandler.setEnv( EnvironmentDef.PVP_SERVER )
loadByIndex(5100, "toLua/tolua")
loadByIndex(6, "game/common/ConstantData")
loadByIndex(1, "game/modules/fightModule/environment/EventHandler")

loadByIndex(1, "game/modules/fightModule/environment/SyncFrequence")
--if GameUtil.useLuaFrameManager == true then
	loadByIndex(136,"game/base/FrameTimerManager")
--end
loadByIndex(1, "json/json");

loadByIndex(4, "game/util/LogUtils");

loadByIndex(10, "game/manager/Storage")

loadByIndex(19, "game/common/CommonFunc")

loadByIndex(23, "game/util/StringUtils")

loadByIndex(1, "game/util/CharacterUtil")

loadByIndex(998, "game/util/tableplus")

loadByIndex(5017, "ServerConfigInclude");

--寻路
loadByIndex(1,"game/base/PathFinder")

--commondefine
loadByIndex(1, "game/config/AnimatorClipArrTimeCheck")

--Event
loadByIndex(1, "game/common/Event")
loadByIndex(1, "game/common/AttrEnum")
loadByIndex(1, "game/common/Attribute")

--stage/character
loadByIndex(1, "game/stage/character/CharacterConstant")
loadByIndex(1, "game/stage/character/CharacterBase")
loadByIndex(1, "game/stage/character/Player")
loadByIndex(1, "game/stage/character/MainCharacter")
loadByIndex(1,"game/stage/character/Monster")
loadByIndex(1,"game/stage/character/Npc")
loadByIndex(20, "game/stage/character/model/CharacterModel")
loadByIndex(1,"game/stage/character/stateMachine/StateBase")
loadByIndex(2,"game/stage/character/stateMachine/IdleState")
loadByIndex(3,"game/stage/character/stateMachine/AttackState")
loadByIndex(4,"game/stage/character/stateMachine/HitbackState")
loadByIndex(5,"game/stage/character/stateMachine/HitflyState")
loadByIndex(6,"game/stage/character/stateMachine/StandupState")
loadByIndex(7,"game/stage/character/stateMachine/DeadState")
loadByIndex(8,"game/stage/character/stateMachine/RunState")
loadByIndex(1,"game/stage/character/stateMachine/CharacterStateMachine")
loadByIndex(1,"game/stage/character/CharacterPool")
loadByIndex(1,"game/stage/character/HatredMap")
loadByIndex(1,"game/stage/character/Partner")
loadByIndex(1, "game/modules/zmodules/stage/BabyCharacter") --宝宝
loadByIndex(1, "game/stage/character/view/CharacterViewFactory")

loadByIndex(1, "game/stage/character/CharacterManager")

-- fight from 5000
loadByIndex(5000, "game/modules/fightModule/core/FightServerInclude")

loadByIndex(1, "net/CodeRecover")
loadByIndex(199, "net/NetImitateDecodeServer")