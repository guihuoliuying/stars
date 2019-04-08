--------------------------------------------------------------------------------
--      Copyright (c) 2015 - 2016 , 蒙占志(topameng) topameng@gmail.com
--      All rights reserved.
--      Use, modification and distribution are subject to the "MIT License"
--------------------------------------------------------------------------------
if jit then		
	if jit.opt then
		jit.opt.start(3)			
	end
	--print("jit", jit.status())
	--print(string.format("os: %s, arch: %s", jit.os, jit.arch))
end

--mobdebug 会调整jit
if DebugServerIp then
	ZBS = "D:/ZeroBraneStudio/";
	LuaPath = "E:/myWork/unity/simeiren/LuaSource/lua/"
	package.path = package.path..";./?.lua;"..ZBS.."lualibs/?/?.lua;"..ZBS.."lualibs/?.lua;"..LuaPath.."?.lua;"..ZBS.."bin/clibs/".."?.dll;"
  	require("mobdebug").start(DebugServerIp)
end

function tolua.initget( type )
	return {}
end

do
	if EnvironmentHandler and EnvironmentHandler.isInServer then
		UnityEngine = {}
		System = {}
		Mathf		= require "toLua/UnityEngine/Mathf"
		Vector3 	= require "toLua/UnityEngine/Vector3"
		Quaternion	= require "toLua/UnityEngine/Quaternion"
		Vector2		= require "toLua/UnityEngine/Vector2"
	else
		Mathf		= require "toLua/UnityEngine/Mathf"
		Vector3 	= require "toLua/UnityEngine/Vector3"
		Quaternion	= require "toLua/UnityEngine/Quaternion"
		Vector2		= require "toLua/UnityEngine/Vector2"
		Vector4		= require "toLua/UnityEngine/Vector4"
		Color		= require "toLua/UnityEngine/Color"
		Ray			= require "toLua/UnityEngine/Ray"
		Bounds		= require "toLua/UnityEngine/Bounds"
		RaycastHit	= require "toLua/UnityEngine/RaycastHit"
		Touch		= require "toLua/UnityEngine/Touch"
		LayerMask	= require "toLua/UnityEngine/LayerMask"
		Plane		= require "toLua/UnityEngine/Plane"
		Time		= require "toLua/UnityEngine/Time"

		list		= require "toLua/list"
		utf8		= require "toLua/misc/utf8"

		require "toLua/event"
		require "toLua/typeof"
		require "toLua/slot"
		require "toLua/System/Timer"
		require "toLua/System/coroutine"
		require "toLua/System/ValueType"
		require "toLua/System/Reflection/BindingFlags"
	end
end