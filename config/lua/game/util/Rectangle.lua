----------Rectangle类

Rectangle = {}

Rectangle.LEFT_TOP 		= "leftTop"
Rectangle.RIGHT_TOP 	= "rightTop"
Rectangle.RIGHT_BOTTOM 	= "rightBottom"
Rectangle.LEFT_BOTTOM 	= "leftBottom"

function Rectangle:new(x, y, width, height)
	local o = {};
	setmetatable(o, {__index=self});
	o.x = x;
	o.y = y;
	o.width = width;
	o.height = height;
	o.rx = x + width
	o.ry = y + height
	return o;
end

function Rectangle:isInterSected( rect )
	-- body
	return ( self.x < rect.rx and self.y < rect.ry) and ( self.rx > rect.x and self.ry > rect.y )
end

function Rectangle:contains( pos )
	-- body
	return self.x < pos.x and self.y < pos.y and self.rx > pos.x and self.ry > pos.y
end

function Rectangle:contaisXY( x, y )
	-- body
	return self.x < x and self.y < y and self.rx > x and self.ry > y
end

--获取中心点;
function Rectangle:getMiddle()
	return self.x+self.width/2, self.y+self.height/2;
end

--与其他的中心点计算距离;
function Rectangle:calcMiddleDistance(otherRectangle)
	local curMiddleX, curMiddleY = self:getMiddle();
	local otherMiddleX, otherMiddleY = otherRectangle:getMiddle();
	return math.sqrt((curMiddleX - otherMiddleX)^2 + (curMiddleY - otherMiddleY)^2)
end
