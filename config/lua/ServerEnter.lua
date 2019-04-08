-- region
-- Date    : 2016-09-08
-- Author  : daiyaorong
-- Description :  服务端lua入口
-- endregion

Project_Path = ""
ServerEnter = {}
function ServerEnter.setPath( path )
    Project_Path = path
    package.path = package.path..";"..Project_Path.."/config/lua/?.lua"
    require("ServerGlobal")
end