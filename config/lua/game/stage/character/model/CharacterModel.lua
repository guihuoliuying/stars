-- region
-- Date    : 2016-09-01
-- Author  : daiyaorong
-- Description :  角色数据模块
-- endregion

CharacterModel = {}

-- 阵营关系
local relationTable = nil
local CAMP_STR = nil

local function receiveRelation()
    -- body
    relationTable = {}
    local relation = nil
    local conn = GameNet.GetSocket()
    local size = conn:ReadSbyte()
    for index = 1, size do
        relation = RelationVo:create()
        relation:read( conn, size )
        relationTable[relation.starter] = relation
        CAMP_STR[index-1] = "camp"..(index-1)
    end
    -- --print("阵营关系表"..tableplus.formatstring(relationTable,true))
end

local function registerProtocal()
    -- body
    GameNet.registerRecv(PacketType.ClinetCampRelation, receiveRelation)
end

function CharacterModel.init()
	-- body
    CAMP_STR = {}
	registerProtocal()
    if EnvironmentHandler.isInServer == true then
        local totalCamp = 0
        for k,v in pairs(CFG.camp.configs) do
            totalCamp = totalCamp + 1
        end
        for index = 1, totalCamp do
            CAMP_STR[index-1] = "camp"..(index-1)
        end
    end
end

function CharacterModel.getRelation( campA, campB )
	-- body
    if EnvironmentHandler.isInServer == true then
        if CFG.camp.configs[campA] == nil then
            return 1
        else
            return CFG.camp.configs[campA][campB]
        end
    end
    if relationTable == nil or relationTable[campA]==nil then
        return CharacterConstant.RELATION_PEACE
    else
	   return relationTable[campA][campB]
    end
end

function CharacterModel.getCampStr(index)
    return CAMP_STR[index]
end