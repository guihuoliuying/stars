-----------------------------------------------------
-- region : Event.lua
-- Date   : 2016.3.11
-- Author ：dinglin
-- Desc   ：功能系统的事件
-- endregion
-----------------------------------------------------
Event = {

}

Event = createClass(Event)

function Event:create(obj)
	local event = Event:new()
	event.obj = obj
	event.events = {}
	return event
end

--注册事件
-- 使用ModuleEvent.dispatch()派发的事件，用该接口监听
function Event:attachEvent(evtId,callback,parseCallback)
	if self.events[evtId] then
		ModuleEvent.removeListener(evtId,self.events[evtId] )
	end
	local handler = function(evtData)
		if self.obj then
			callback(self.obj,evtData)
		else
			callback(evtData)
		end
	end
	local parser = nil
	if parseCallback ~= nil then
		parser = function(evtData)
			if self.obj then
				parseCallback(self.obj,evtData)
			else
				parseCallback(evtData)
			end
		end
	end
	self.events[evtId] = handler
	ModuleEvent.addListener(evtId, handler, parser)
end

-- 注册多参数事件
-- 使用ModuleEvent.dispatchWithFixedArgs()派发的事件，用该接口监听
-- !!!前后端同步的事件不能使用该接口
-- !!!前后端同步的事件不能使用该接口
-- !!!前后端同步的事件不能使用该接口
function Event:attachFixedArgsEvent(evtId, callback)
	if self.events[evtId] then
		ModuleEvent.removeListener(evtId,self.events[evtId] )
	end
	local handler = function(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9)
		if self.obj then
			callback(self.obj, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9)
		else
			callback(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9)
		end
	end
	self.events[evtId] = handler
	ModuleEvent.addListener(evtId, handler)
end

--移除全部事件
function Event:removeEvent()
	if self.events then
		for k,v in pairs(self.events) do
			ModuleEvent.removeListener(k,v)
		end
		self.events = {}
	end
end

-- 移除某个事件
function Event:removeEventByEvtId(evtId)
	if self.events and self.events[evtId] then
		ModuleEvent.removeListener(evtId, self.events[evtId])
		self.events[evtId] = nil
	end
end
