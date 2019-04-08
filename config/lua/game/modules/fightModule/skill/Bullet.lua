-- region
-- Date    : 2016-06-21
-- Author  : daiyaorong
-- Description :  飞行弹道类
-- endregion

Bullet = {
	
}

Bullet = createClass( Bullet )

function Bullet:init( fatherId, skillID, bulletIndex )
	self.fatherId	= fatherId
	self.skillID    = skillID
	self.bulletIndex = bulletIndex

	self.id 		= CharacterManager:getCharacId( )
	self.skillData 	= FightModel:getSkillData( self.skillID )
	
	if self.skillData and self.skillData.bulleteffectinfo and self.bulletIndex 
		and self.skillData.bulleteffectinfo[self.bulletIndex] then
		self.model 		= self.skillData.bulleteffectinfo[self.bulletIndex].model;
	else
		self.model 		=  "";
	end

	self.isPause 	= false

	local fatherEntity = CharacterManager:getCharacByUId( self.fatherId )
	self.motionInfo = {	bulletId = self.id,
						curFrame=0, 
						stopMotionFrame=fatherEntity.motionInfo.stopMotionFrame,
						targetInitPos = fatherEntity.motionInfo.targetInitPos:Clone(),
						targetInitRot = fatherEntity.motionInfo.targetInitRot:Clone(),
						}
	self.position = fatherEntity:getPosition():Clone()
	self.bornPosition = self.position
	self.defaultPosition = self.position
	self.rotation = fatherEntity:getRotation():Clone()
	self.camp = fatherEntity.camp
	self.pSkillHandler = fatherEntity.pSkillHandler

	--显示子弹view
	if EnvironmentHandler.isInServer == false then
		self:initView(fatherEntity)
	end
end

--子弹view
function Bullet:initView( fatherEntity )
	if( not StringUtils.isEmptyString( self.model ) )then
		self.view = BulletView:create()
		self.view:init( self.model )
		self.view:setPosition( self.position )
		self.view:setRotation( self.rotation )
		self.view:setScale( fatherEntity.view:getScale() )
	end
end

function Bullet:changeProperty( key, value )
	-- body
	if( key == "position")then
		self:setPosition( value )
	elseif( key == "rotation")then
		self:setRotation( value )
	end
	if self[key] == nil then
		if self.motionInfo[key] ~= nil then
			self.motionInfo[key] = value
		end 
	else
		self[key] = value
	end
end

function Bullet:setPosition(pos)
    self.position = pos
    if self.view ~= nil then
        self.view:setPosition(self.position)
        if self.view.isActive == false then
        	self.view:setActive( true )
        end
    end
end

function Bullet:getPosition()
	-- body
	return self.position
end

function Bullet:setRotation( value )
    -- body
    self.rotation = value
    if self.view ~= nil then
        self.view:setRotation( value )
    end
end

function Bullet:getRotation()
    return self.rotation
end

function Bullet:isAnimPaused()
	-- body
	if self.view ~= nil then
		return self.view:isAnimPaused()
	end
	return self.isPause
end

function Bullet:setActive( value )
	-- body
	if self.view then
		self.view:setActive( value )
	end
end

--恢复
function Bullet:resume( )
	if self.view ~= nil then
		self.view:resume( false )
	end
	self.isPause = false
end

--暂停
function Bullet:pause( )
	if self.view ~= nil then
		self.view:pause( true )
	end
	self.isPause = true
end

function Bullet:dispose( )
	if( self.view )then
		self.view:dispose()
		self.view = nil
	end
	self.skillData = nil
	self.skillID = nil
	self.motionInfo = nil
	self.bornPosition = nil
	self.defaultPosition = nil
end