--------------------------------------------------------------
-- region Npc.lua
-- Date : 2016-6-23
-- Author : jjm
-- Description :Npc实体
-- endregion
--------------------------------------------------------------- 
Npc = {
    characterType = CharacterConstant.TYPE_NPC
}
Npc = createClassWithExtends(Npc,CharacterBase)
NpcConst = {
    SelectEffect = "npc_chooseeff",
    TalkPanel = "NpcTalk",
    TalkTime = "npc_talktime"
}

local function defaultFindToNpcCallback(self)
    if MarryControl:isMarryNpc(self.npcid) then
       MarryControl:reqOpenMarryNpc({type_=MarryConstant.ENTRANCE_TYPE.NPC})        
    elseif  BenefitGiftExchangeModel:isBenefitGiftExchangeNpc(self.npcid) then  
    	BenefitGiftExchangeCtrl:reqOpenDialogWnd(self.npcid)
    elseif TaskControl.isTaskInteractionTempNpc(self.npcid) then
        ModuleEvent.dispatch(ModuleConstant.SHOW_TASK_COLLECT, self.npcid)
    else
        local dialogData = TaskControl.getTaskDialogByNpc(self.npcid)
        if dialogData == nil then
            dialogData = DialogUtil.randomDialog(self.npcid, self.dialog)
        end
        if dialogData then
			dialogData.openFromNpc = self.npcid
			--播放NPC音效;
			if(dialogData.dialogs[1].soundId)then
				AudioMgr.PlaySoundInfoNpc(dialogData.dialogs[1].soundId);
			end
            ModuleEvent.dispatch(ModuleConstant.DIALOG_WINDOW, {ModuleConstant.UI_OPEN, dialogData})
        end
    end
    StageTweenCtrl.stopAutoWay()
end

local function cameraPositonChangeLocal()
	self:cameraPositonChange()
end

function Npc.create(npcData)
    local npc = Npc:new()
    CharacterBase.create(npc,npcData)
    return npc
end

function Npc:init( npcData )
    -- body
    CharacterBase.init(self,npcData)
    self:setPosAndRot( npcData )
    self:setFindToNpcCallback(defaultFindToNpcCallback);
end

function Npc:initFightData()
    -- body
end

function Npc:setPosAndRot(npcData)
	local vector = npcData.position
    if type(vector) == "string" then
        vector = Vector3.NewByStr(npcData.position) * 0.1
    end
    if PathFinder.isBlock(vector) == false then
        vector = PathFinder.samplePosition(vector)
    end
    local rotation = Quaternion.Euler(0,tonumber(npcData.rotation), 0)
	if self.view then
		self.view:setPosAndRon(vector,rotation,npcData.scale)
	end
	self:setPosition(vector)
	self:setRotation(rotation)
	if self.isActive == false then
		self:setActive(false)
	else
		self:setActive(true)
	end
end

--初始化事件
function Npc:initEvent()
    CharacterBase.initEvent(self)
end

function Npc:setActive(isActive)
    if self.view and self.view._isActive ~= isActive then 
		CharacterBase.setActive(self,isActive)
	    if isActive then
	        if self.view.animator then
	            self.view.animator:CrossFade(self.pose,0)
	        end
	        local param = CFG.commondefine:get(NpcConst.TalkTime).value
	        local paramTable = StringUtils.split(param,"+")
	        --npc说话的几率
	        self.talkOdds = tonumber(paramTable[2])
	        --npc每句话持续时间
	        self.talkTime = paramTable[3]
	        local doTalk = function()
	            self:doTalk()
	        end
	        self.talkFrameKey = FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_UI):RegisterLuaCallBack(paramTable[1]*30,0,doTalk)
	    else
	      if self.talkFrameKey then
	        FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_UI):removeCallback(self.talkFrameKey)
	        self.talkFrameKey = nil
	        self.view:talkOver()
	      end
	    end		
    else
    	CharacterBase.setActive(self,isActive)
    end    
end

function Npc:doTalk()
	if self.view == nil then return end
    local num = math.Random(1, 1000)
    if num <= self.talkOdds then
        self.view:doTalk(self)
    end
end

function Npc:setSelect(callback)
	--print("----------------Npc:setSelect")
    if self.isCanBeSelect==false then
        return
    end
	local stageType = StageManager.getCurStageType()
	if stageType == ConstantData.STAGE_TYPE_CARGO_TROOP then
		CargoControl.cargoNpcSelectHandler(self)
		return
	end
	if stageType == ConstantData.STAGE_TYPE_FAMILY_TRANSPORT and self.npcid ~= FamilyTransportConstant.TRANSPORT_NPC then
		FamilyTransportControl.onSelectCar(self)
		return
	end
	CharacterManager.selectNpcID=self.uniqueID
	if self.view then
		self.view:setSelect()
	end
    self:findToNpc(nil, callback)
end

--设置找到npc后的回调;
function Npc:setFindToNpcCallback(callback)
	-- print("Npc:setFindToNpcCallback:::::::::::", callback)
    self.onFindToNpcCallback = callback;
end

function Npc:getSuitablePosForStand(character, callback)
	if callback == nil or character == nil then return end
	local pos = self:getPosition()
	if pos then
		if type(pos) == "string" then
			pos = Vector3.NewByStr(pos) * 0.1
		else
			pos = pos:Clone()
		end
	else
		return
	end
	local range = self.range * 0.1
	local characPos = character:getPosition():Clone()
	if CharacterUtil.sqrDistanceWithoutY(pos, characPos) <= (range*range) then
		callback(nil)
		return;
	end
	PathFinder.luaAddPathFinder( character, pos, function(path) 
		if path == nil then 
			callback(nil) 
		end
		local lastPos = nil
		if #path == 1 then
			lastPos = characPos
		else
			lastPos = path[#path - 1]
		end
		local directionPos = Vector3(lastPos.x - pos.x,0,lastPos.z - pos.z)
        local standPos = pos + Vector3.Normalize(directionPos) * range
		callback(standPos)
	end)
end

function Npc:findToNpc(character, callback)
	self.gotoNpcCallback = callback
    if character == nil then
        character = CharacterManager:getMyCharac()
    end
	local standPos = nil
	local afterGetPos = function()
		if standPos and self.view then
			character:moveTo(standPos, makeHandle(self, function(self)
				self.onFindToNpcCallback(self);
				if self.gotoNpcCallback then 
					self.gotoNpcCallback(self)
					self.gotoNpcCallback = nil
				end
			end));
		else
			self.onFindToNpcCallback(self);
			if self.gotoNpcCallback then
				self.gotoNpcCallback(self)
				self.gotoNpcCallback = nil
			end
		end
	end
	self:getSuitablePosForStand(character, function(pos) 
		standPos = pos
		FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_LUA):RegisterLuaCallBack(1, 1, afterGetPos)
	end)
end

-- 告诉服务器我与npc互动了
function Npc:talkWithNpc()
	local conn = GameNet.GetSocket()
	conn:WriteProtocol(PacketType.ServerTalkWithNpc)
	conn:WriteInt(self.npcid)
	conn:SendData()
end

function Npc:dispose()
    if self.talkFrameKey then
        FrameTimerManager.getInstance(ConstantData.FRAME_EVENT_UI):removeCallback(self.talkFrameKey)
        self.talkFrameKey = nil
		if self.view then
			self.view:talkOver()
		end
    end
    CharacterBase.dispose(self)
end