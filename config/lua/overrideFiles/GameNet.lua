GameNet = {}
function GameNet.CheckPacketTimeout() end
function GameNet.UpdateRecvPacketTime() end
function GameNet.getNetRate() end
function GameNet.ConnectServer(_serverType, _ip, _port, _callback, _failCallback) end
function GameNet.ReConnectServer(_callback, _failCallback) end
function GameNet.DisconnectServer() end
function GameNet.isValid() end
function GameNet.GetSocket() end
function GameNet.IsConnect() end
function GameNet.registerSend(protocalId, callback) end
function GameNet.registerRecv(protocalId, callback) end
function GameNet.sendPacket(protocalId, ...) end
function GameNet.recvPacket(protocalId, ...) end
function GameNet.registerMultiRecvFunc(func, ...) end
function GameNet.removeMultiRecvFunc(func) end
function GameNet:initNet() end
function GameNet:initTimer() end
function GameNet:getMainVersion() end
function GameNet:getVersionStr() end
function GameNet:enableHeartBeat(enable) end
function GameNet.GetServerTime() end
function GameNet.UpdateServerTime(newTimeStamp) end
function GameNet.setHeartBeatKey( key ) end
function GameNet:initCPPNet(serverType, ip, port, callback, failCallback) end
function GameNet:reInitCPPNet(serverType,ip,port,callback, failCallback) end
