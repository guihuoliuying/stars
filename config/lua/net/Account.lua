-- region Account.lua
-- Date    : 2015-6-2
-- Author :  daiyaorong
-- Description : 游戏账户相关业务
-- endregion

Account = {}

------------------------------------------------数据类型--------------------------------------------
Account.account = "account" 	--账户名
Account.password = "password" 	--账户密码
Account.connState = nil			--账户连接状态
Account.isConning = false		--是否正在登陆过程中