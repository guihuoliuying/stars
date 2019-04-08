--------------
--desc:四叉树
--author:潘振峰
--date:2015-10-20
--TODO:增加点查询接口;
--TODO:增加矩形查询接口;
--TODO:修改check接口以根节点开始查找;
--------------
--每个节点所能包含物体的最大数量;
local MAX_OBJECTS_PER_NODE = 10;
--四叉树的最大层数,超过则不再划分;
local MAX_LEVELS = 20;

--整理的时候将类分散到文件中;
QuadTree = {};
QuadNode = { idx = 0,level = 0};
QuadGameCharacter = {};

--------------------用于测试的接口;
local testX = -250;
local testZ = -250;
local testWidth = 500;
local testHeight = 500;
local testY = 100;
local testCheckY = 100;
local produceNum = 10000;

local testTransforms = {};
local checkObjs = {};
local function testInitGameObjects(quadTree)
	local tmpTrans = nil;
	local tmpX, tmpZ;
	local fenceCubePrefab = Resources.Load("fordebug/fenceLine");
	for i=1,produceNum do
		table.insert(testTransforms, CommonFunc.Instantiate(fenceCubePrefab).transform);
		tmpX = math.Random(testX,testX+testWidth);
		tmpZ = math.Random(testZ,testZ+testHeight);
		testTransforms[i].localScale = Vector3.one;
		testTransforms[i].position = Vector3.New(tmpX, testY, tmpZ);
		testTransforms[i].name = "testQuadObj_"..i;
		testTransforms[i] = QuadGameCharacter:new(testTransforms[i]);
		quadTree:insert(testTransforms[i]);
	end
end

local function calcDistanceObjAndObj(curObj, compareObj)
	local curX,curY = curObj:getXZ();
	local compareX,compareY = compareObj:getXZ();
	local tmpX = curX - compareX;
	local tmpY = curY - compareY;
	return math.sqrt(tmpX*tmpX + tmpY*tmpY);
end

function QuadTree:testCheck(index, radius)
	CmdLog("loop start Time:"..os.clock());
	local needCheckObj = testTransforms[index];
	local tmpRtnObjs = {};
	for i=1,#testTransforms do
		if(calcDistanceObjAndObj(testTransforms[i], needCheckObj)<radius)then
			table.insert(tmpRtnObjs, testTransforms[i]);
		end
	end
	CmdLog("loop end Time:"..os.clock());
	for i=1,#checkObjs do
		tmpPos = checkObjs[i].trans.position;
		tmpPos.y = testY;
		checkObjs[i].trans.position = tmpPos;
		testMaterial = CommonFunc.getGameObjectMaterial(checkObjs[i].trans.gameObject);
		testMaterial:SetColor("_echoGlowColor",Color.New(1,0,0,1));
	end
	CmdLog("开始check:"..os.clock());
	-- do return end;
	checkObjs = self:check(testTransforms[index], radius);
	CmdLog("结束check:"..os.clock().."   找到:"..#checkObjs.."个");
	local tmpPos = nil;
	local testMaterial = nil;
	for i=1,#checkObjs do
		-- TraceLog("find："..checkObjs[i].trans.name);
		tmpPos = checkObjs[i].trans.position;
		tmpPos.y = testCheckY;
		checkObjs[i].trans.position = tmpPos;
		testMaterial = CommonFunc.getGameObjectMaterial(checkObjs[i].trans.gameObject);
		testMaterial:SetColor("_echoGlowColor",Color.New(0,1,0,1));
	end
	testMaterial = CommonFunc.getGameObjectMaterial(testTransforms[index].trans.gameObject);
	testMaterial:SetColor("_echoGlowColor",Color.New(0,0,1,1));
end

function QuadTree:testCheck( index, radius )
	-- body
	-- TraceLog("loop start Time:"..os.clock());
	-- local needCheckObj = testTransforms[index];
	-- local tmpRtnObjs = {};
	-- for i=1,#testTransforms do
	-- 	if(calcDistanceObjAndObj(testTransforms[i], needCheckObj)<radius)then
	-- 		table.insert(tmpRtnObjs, testTransforms[i]);
	-- 	end
	-- end
	-- TraceLog("loop end Time:"..os.clock());
	-- for i=1,#checkObjs do
	-- 	tmpPos = checkObjs[i].trans.position;
	-- 	tmpPos.y = testY;
	-- 	checkObjs[i].trans.position = tmpPos;
	-- 	testMaterial = CommonFunc.getGameObjectMaterial(checkObjs[i].trans.gameObject);
	-- 	testMaterial:SetColor("_echoGlowColor",Color.New(1,0,0,1));
	-- end
	CmdLog("开始check:"..os.clock());
	-- do return end;
	checkObjs = self:checkWithRect(testTransforms[index].trans.position, radius);
	CmdLog("结束check:"..os.clock().."   找到:"..#checkObjs.."个");
	local tmpPos = nil;
	local testMaterial = nil;
	for i=1,#checkObjs do
		-- TraceLog("find："..checkObjs[i].trans.name);
		tmpPos = checkObjs[i].trans.position;
		tmpPos.y = testCheckY;
		checkObjs[i].trans.position = tmpPos;
		testMaterial = CommonFunc.getGameObjectMaterial(checkObjs[i].trans.gameObject);
		testMaterial:SetColor("_echoGlowColor",Color.New(0,1,0,1));
	end
	testMaterial = CommonFunc.getGameObjectMaterial(testTransforms[index].trans.gameObject);
	testMaterial:SetColor("_echoGlowColor",Color.New(0,0,1,1));
end

function QuadTree:testReset()
	local tmpPos = nil;
	local testMaterial = nil;
	for i=1,#testTransforms do
		tmpPos = testTransforms[i].trans.position;
		tmpPos.y = testY;
		testTransforms[i].trans.position = tmpPos;
		testMaterial = CommonFunc.getGameObjectMaterial(testTransforms[i].trans.gameObject);
		testMaterial:SetColor("_echoGlowColor",Color.New(1,0,0,1));
	end
end

function QuadTree:testNew()
	local o = QuadTree:new(testX, testZ, testWidth, testHeight);
	testInitGameObjects(o);
	return o;
end


--------------------用于测试的接口end;



----------四叉树;
function QuadTree:new(x, y, width, height)
	local o = {};
	setmetatable(o, {__index=self});
	o.rootNode = QuadNode:new(1, Rectangle:new(x, y, width, height), nil);
	return o;
end

--插入/刷新都用这个接口, 在物体创建的时候, 位置改变的时候需要调用这个接口;
function QuadTree:insert(quadObj)
	local bakQuadNode = quadObj.quadNode;
	self.rootNode:insert(quadObj);
	--如果和之前的所在的节点不一样了,需要将对象从当前节点进行移除;
	if((bakQuadNode~=nil) and (bakQuadNode~=quadObj.quadNode))then
		bakQuadNode:removeObject(quadObj);
	end
end

function QuadTree:clear()
	self.rootNode:clear();
end

--检查其他的节点, 并将找到的结果放入rtnObjs;
local function checkOtherNode(rtnObjs, otherNode, quadObj, radius)
	--先检查当前节点;
	local findTmpObjs = otherNode:getObjects(quadObj, radius);
	table.merge(rtnObjs, findTmpObjs, true);
	--开始检查子节点;
	for i=1,#otherNode.nodes do
		checkOtherNode(rtnObjs, otherNode.nodes[i], quadObj, radius);
	end
end


--寻找符合范围的对象集合;
local function findQuadObjects(rtnObjs, quadNode, quadObj, radius, ignoreDistanceFitler)
	--找到距离小于radius的节点先;
	if(quadNode:calcMiddleDistanceByObj(quadObj)<=radius)then
		--直接拿当前节点的父级进行获取对象集合;
		if(quadNode.parentNode~=nil)then
			quadNode = quadNode.parentNode;
		end
		checkOtherNode(rtnObjs, quadNode, quadObj, radius);
		return;
	end
	if(ignoreDistanceFitler==true)then
		for i=1,#quadNode.nodes do
			findQuadObjects(rtnObjs, quadNode.nodes[i], quadObj, radius);
		end
	else
		local tmpDistance = nil;
		local minDistance = 10000000;
		local minDistanceNode = nil;
		local needFindNodes = {};
		local tmpNode = nil;
		for i=1,#quadNode.nodes do
			tmpNode = quadNode.nodes[i];
			--找到距离最小的那个点;
			tmpDistance = tmpNode:calcMiddleDistanceByObj(quadObj);
			if(tmpDistance<minDistance)then
				minDistance = tmpDistance;
				minDistanceNode = tmpNode;
			end
			if(tmpDistance<=radius)then
				table.insert(needFindNodes, tmpNode);
			end
		end
		--判断是否已经有对应的最小距离节点了;
		local hasFind = false;
		for i=1,#needFindNodes do
			if(needFindNodes[i]==minDistanceNode)then
				hasFind = true;
				break;
			end
		end
		if(hasFind==false)then
			table.insert(needFindNodes, minDistanceNode);
		end
		for i=1,#needFindNodes do
			findQuadObjects(rtnObjs, needFindNodes[i], quadObj, radius);	
		end
	end
end

--检测对象在指定半径内的对象集合;
function QuadTree:check(quadObj, radius)
	--对象之前所在的位置节点;
	local tmpNode = quadObj.quadNode;
	if(tmpNode==nil)then
		CmdLog("QuadTree: can't find ["..quadObj.data.id.."] has in, confirm has invoked insert method");
		return;
	end
	local rtnObjs = {};
	--从根节点开始搜索
	findQuadObjects(rtnObjs, self.rootNode, quadObj, radius, true);
	return rtnObjs;
end

function QuadTree:checkWithRect( pos, radius )
	-- body
	local rect = Rectangle:new( pos.x - radius, pos.z - radius,radius * 2, radius * 2)
	local rtns = {}
	self.rootNode:getObjectsInRect( rect, rtns )
	return rtns
end

----------四叉树节点;
--注:当对象不在子节点的区域内时,会将对象保留在当前节点内;
--level：层级
--bounds: 范围
function QuadNode:new(level, bounds, parentNode, idx)
	local o = {};
	setmetatable(o, {__index=self});
	o:init(level, bounds, parentNode);
	o.idx = idx
	return o;
end

function QuadNode:init(level, bounds, parentNode)
	--当前层级;
	self.level = level;
	--包含的对象组;
	self.objects = {};
	--当前的范围;
	self.bounds = bounds;
	--子节点组;
	self.nodes = {};
	--父级节点,用于检索时不用从根节点开始查找;
	self.parentNode = parentNode;
end

--4象限分割,逆时针进行分割;
--2  1
--3  4
function QuadNode:split()
	local subWidth = self.bounds.width/2;
	local subHeight = self.bounds.height/2;
	local x = self.bounds.x;
	local y = self.bounds.y;
	self.nodes[1] = QuadNode:new(self.level + 1, Rectangle:new(x+subWidth, y+subHeight, subWidth, subHeight), self,1);
	self.nodes[2] = QuadNode:new(self.level + 1, Rectangle:new(x, y+subHeight, subWidth, subHeight), self,2);
	self.nodes[3] = QuadNode:new(self.level + 1, Rectangle:new(x, y, subWidth, subHeight), self,3);
	self.nodes[4] = QuadNode:new(self.level + 1, Rectangle:new(x+subWidth, y, subWidth, subHeight), self,4);
end


--获取位置所在象限索引;
function QuadNode:getIndex(x, y)
    local index = -1;
    local xMidPoint,yMidPoint = self.bounds:getMiddle();
    local isTop = y > yMidPoint;
    if(x < xMidPoint)then--在左象限;
        if(isTop==true)then
            index = 2;
        else
            index = 3;
        end
    elseif(x > xMidPoint)then--在右象限;
        if(isTop==true)then
            index = 1;
        else
            index = 4;
        end
    end
    return index;
end

--插入对象(每次场景中创建对象的时候执行);
function QuadNode:insert(quadObj)
	--如果已经在当前节点就不执行下面操作了;
	if(quadObj.quadNode == self)then
		return;
	end
	quadObj.quadNode = self;
	local index = 0;
	--判断是否有子节点;
	if(self.nodes[1] ~= nil)then
	   index = self:getIndex(quadObj:getXZ());
	   if(index > 0)then
	       self.nodes[index]:insert(quadObj);
		else
			--不在象限上时直接加入当前节点了;
			table.insert(self.objects, quadObj);
		end
		return;
	end
	table.insert(self.objects, quadObj);
	--当超过每个节点所能包容的最多对象数量时,就对当前节点进行切割;
	if((#self.objects > MAX_OBJECTS_PER_NODE) and (self.level < MAX_LEVELS))then
		self:split();
		local tmpObj = nil;
		for i=#self.objects,1,-1 do
			tmpObj = self.objects[i];
			index = self:getIndex(tmpObj:getXZ());
			if(index > 0)then
				self.nodes[index]:insert(tmpObj);
			    table.remove(self.objects, i);
			end
		end
	end
end

function QuadNode:getObjectsInRect( rect , rtnObjs )
	-- body
	for i=1,#self.objects do
		if( rect:contaisXY( self.objects[i]:getXZ()) )then
			rtnObjs[#rtnObjs + 1] = self.objects[i]
		end
	end
	for i = 1, #self.nodes do
		if( rect:isInterSected( self.nodes[i].bounds) )then
			self.nodes[i]:getObjectsInRect( rect, rtnObjs)
		end
	end
end

--获取当前节点的对象和当前的子节点的对象;
--radius:待检测的最大距离;
function QuadNode:getObjects(quadObj, radius)
	local rtnObjs = {};
	local tmpValue = 0;
	local tmpX1, tmpZ1 = quadObj:getXZ();
	local tmpX2, tmpZ2;
	for i=1,#self.objects do
		tmpX2, tmpZ2 = self.objects[i]:getXZ();
		tmpValue = math.sqrt((tmpX1 - tmpX2)^2 + (tmpZ1 - tmpZ2)^2);
		if(tmpValue<=radius)then
			table.insert(rtnObjs, self.objects[i]);
		end
	end
	return rtnObjs;
end

function QuadNode:getObjectsWithRect( rect )
	-- body
	local rtnObjs = {}
	for i=1,#self.objects do
		if( rect.contaisXY( self.objects[i]:getXZ()) )then
			rtnObjs[#rtnObjs + 1] = self.objects[i]
		end
	end
	return rtnObjs
end


function QuadNode:calcMiddleDistanceByNode(otherQuadNode)
	return self.bounds:calcMiddleDistance(otherQuadNode.bounds);
end

function QuadNode:calcMiddleDistanceByObj(quadObj)
	local curMiddleX, curMiddleY = self.bounds:getMiddle();
	local otherMiddleX, otherMiddleY = quadObj:getXZ();
	return math.sqrt((curMiddleX - otherMiddleX)^2 + (curMiddleY - otherMiddleY)^2)
end

function QuadNode:removeObject(obj)
	for i=1,#self.objects do
		if(self.objects[i]==obj)then
			table.remove(self.objects, i);
		end
	end
end


function QuadNode:clear()
	--包含的对象组;
	self.objects = {};
	--子节点组;
	self.nodes = {};
	self.parentNode = nil;
end



--------------四叉树的数据类----------------
function QuadGameCharacter:new(trans)
	local o = {};
	setmetatable(o, {__index=self});
	o:init(trans);
	return o;
end

function QuadGameCharacter:init(trans)
	--四叉树的节点,用于快速查找;
	self.quadNode = nil;
	self.trans = trans;
end

function QuadGameCharacter:getXZ()
	local tmpTransPos = self.trans.position;
	return tmpTransPos.x, tmpTransPos.z;
end



------其他的接口;
function table.merge(ta, tb, isMergeToTa)
	local rtnTb = {};
	if(isMergeToTa==true)then
		rtnTb = ta;
	else
		for i=1,#ta do
			table.insert(rtnTb, ta[i]);
		end
	end
	for i=1,#tb do
		table.insert(rtnTb, tb[i]);
	end
	return rtnTb;
end