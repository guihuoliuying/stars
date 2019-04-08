-- StringUtils
-- To change this template use File | Settings | File Templates.
--

local T_INSERT = table.insert
local STR_FIND = string.find
local STR_SUB = string .sub

StringUtils = {}

-- 生成数组  如 [(-1)+1+1] -->{-1, 1, 1}
StringUtils.PATTERN_NUM_ARR = "[-]?%d+"




--计算包括中文字符的字符个数
function StringUtils.chineseLength(str)
    if  type(str) ~= "string"  then
        return 0
    end
    -- local _, n = str:gsub('[\128-\255]', '')
    -- return (#str - n) + n / 3
    local tmpByte = nil;
    local rtnCount = 0;
    for i=1, #str do
        tmpByte = string.byte(str, i);
        if(tmpByte > 127)then
            rtnCount = rtnCount + 1;
        end
    end
    return rtnCount/3;
end

function StringUtils.length(str)
    if  type(str) ~= "string"  then
        return 0
    end
    return string.len(str)
end

function StringUtils.stringWidth(str,fontSize)
    if str == nil or type(str) ~= 'string' then
        return 0
    end
    local byte = nil
    local strWidth = 0
    local isAllEn = true  --是否所有的都是英文
    for i = 1,#str do
        byte = string.byte(str, i)
        if byte > 0 and byte <= 127 then
            strWidth = strWidth + 1
        elseif byte >= 192 and byte < 233 then
            strWidth = strWidth + 1
            isAllEn = false
        elseif byte >= 224 and byte < 239 then
            strWidth = strWidth + 1
            isAllEn = false
        elseif byte >= 240 and byte <= 247 then
            strWidth = strWidth + 1
            isAllEn = false
        end
    end
    if isAllEn == true then
        fontSize = fontSize - 7
    end
    return strWidth * fontSize
end

--检测某个字符串存在与否
function StringUtils.isExist(str,pattern)
    if str == nil or pattern == nil or StringUtils.isEmptyString(str) then
        return false
    end
    return (string.find(str,pattern) ~= nil)
end


function StringUtils.isEmptyString(checkString)
    if checkString then
        local newString = string.gsub(checkString, " ", "")
        if #newString == 0 then
            return true
        end
    else
        return true;
    end
    return false;
end
--检测字符串是否无效
function StringUtils.isInVaildString(checkString)
    if checkString == nil or StringUtils.isEmptyString(checkString) or checkString == '0' then
        return true
    end
    return false
end

--检查字符是否以中文结尾
function StringUtils.checkChineseInLast(str)
    local lastChar = string.sub(str, -1)
    local index = string.find(lastChar, '[^\128-\255]')
    if index then
        return false
    end
    return true
end

--截取包含中文字符前缀
function StringUtils.subPrefixString(str, remainLenght)
    local len = StringUtils.length(str)
    while len > remainLenght do
        if StringUtils.checkChineseInLast(str) then
            str = string.sub(str, 1, -4)
        else
            str = string.sub(str, 1, -2)
        end
        len = len - 1
    end
    return str
end

function StringUtils.checkPrefix( str, prefixStr )
    assert(type(str) == "string")
    local pattern = "/^"..prefixStr.."*$/"
    if string.find(str, prefixStr) then
        return true
    end

    return false
end

--生成数组
function StringUtils.getArrayFromStrII( str, pattern, formatFunc )
    local array = {}
    if StringUtils.isEmptyString(str) or StringUtils.isEmptyString(pattern) then
        return
    end

    for k,v in string.gmatch(str, pattern) do
        local ret = k
        if formatFunc ~= nil then
            ret = formatFunc(k)
        end
        array[#array + 1] = ret
    end
    return array
end

--生成数字下标数组  如 格式：a;b;c;
function StringUtils.getArrayFromStr(str, splitStr)
    local array = {}
    local i = 1
    if str then
        for s in string.gmatch(str, "([^" .. splitStr .. "]+)" .. splitStr) do
            array[i] = s
            i = i + 1
        end
    end
    return array, i - 1
end

--字符串拆分
function StringUtils.split(str, delim, maxcount, formatFunc)
    str = tostring(str)
    -- LogManager.Log("splite"..debug.traceback())
    -- str = str..""
    local i, j, k
    local t = {}
    local subFun
    if( formatFunc == nil)then
        subFun = STR_SUB
    else
        subFun = function ( str, k ,i)
            -- body
            return formatFunc( STR_SUB(str, k,i))
        end
    end
    k = 1
    while true do
        i, j = STR_FIND(str, delim, k)
        if i == nil or (maxcount and maxcount - 1 <= #t) then
            T_INSERT(t, subFun(str, k))
            return t
        end
        T_INSERT(t, subFun(str, k, i - 1))
        k = j + 1
    end

end

-- 获取符合模式的列表
function StringUtils.getPattern( str, pattern )
    -- body
    local i, j, k
    local t = {}
    local subFun = string.sub
    k = 1
    while true do
        i, j = STR_FIND( str, pattern, k )
        if i == nil then
            T_INSERT( t, subFun(str,k) )
            return t
        end
        T_INSERT( t, subFun(str,i,j) )
        k = j + 1
    end
end

--去掉两边空格
function StringUtils.trim(s)
    assert(type(s) == "string")
    return s:match("^%s*(.-)%s*$")
end



function StringUtils.numberFormat(silver)
    local showSilverNum = 0
    local floatNum = 0
    if silver >= 1000000000 then
        showSilverNum,floatNum= math.modf(silver/100000000)
        if floatNum >= 0 then
            floatNum = floatNum * 10
            floatNum = math.floor(floatNum)
            if floatNum > 0 then
                showSilverNum = showSilverNum .. "." .. floatNum .. Res.String.tenMillion
            else
                showSilverNum=showSilverNum ..Res.String.tenMillion
            end
        end
    elseif silver >= 100000 then
        showSilverNum,floatNum= math.modf(silver/10000)
        if floatNum >0 then
            floatNum = floatNum * 10
            floatNum = math.floor(floatNum)
            if floatNum > 0 then
                showSilverNum = showSilverNum .. "." .. floatNum .. Res.String.tenThousand
            else
                showSilverNum=showSilverNum ..Res.String.tenThousand
            end
        else
            showSilverNum=showSilverNum ..Res.String.tenThousand
        end
    else
        showSilverNum =silver
    end
    return showSilverNum
end


function StringUtils.numberFormatWan(num)
    if num > 10000 then
        num = num /10000 - num/10000%0.1
        num =  num..Res.String.tenThousand
    end
    return num
end

--数值格式化为万，当num大于等于startNum时才格式化，
--bit为保留多少位小数,为0，为1时取整数，为0.1时取一位小数，0.01时取两位小数...
function StringUtils.numberFormatWanByBit(num , startNum , bit)
    if num >= startNum then
        if bit == 0 then bit = 1 end
        local temp = num*0.0001
        num = temp - temp%bit
        num = num..Res.String.tenThousand
    end
    return tostring(num) 
end

--数值格式化为亿，万，当num大于等于10亿或10万时才格式化，
--bit为保留多少位小数,为0，为1时取整数，为0.1时取一位小数，0.01时取两位小数...为空时保留一位小数
function StringUtils.numberFormatByBit(num , bit)
    if bit == nil then bit = 0.1 end
    if bit == 0 then bit = 1 end
    if num >= 1000000000 then
        local temp = num*0.00000001
        num = temp - temp%bit
        num = num.."亿"
    elseif num >= 100000  then
        local temp = num*0.0001
        num = temp - temp%bit
        num = num..Res.String.tenThousand
    end
    return tostring(num) 
end

function StringUtils.containsValue(table,value)
    if not table or not value then return false end
    for i,v in pairs(table) do
        if v == value then
            return true
        end
    end
    return false
end
function StringUtils.containsProperty(table,pro,value)
    if not table or not value then return false end
    for i,v in pairs(table) do
        if v[pro] == value then
            return true
        end
    end
    return false
end

function StringToTable(string,split1,split2,split3)
    local table=StringUtils.split(string,split1)
    if split2 then
        local table2={}
        for i,v in pairs(table) do
            local tmp = StringUtils.split(v,split2)
            if #tmp == 2 then
                if split3 then
                    local tmp2 = StringUtils.split(tmp[1],split3)
                    for i,v in pairs(tmp2) do
                        table2[v]=tmp[2]
                    end
                else
                    table2[tmp[1]]=tmp[2]
                end
                
            end
        end
        return table2
    end
    return table
end

function StringUtils.getString(replaceString, ...)
    local str = replaceString
	local arg={...}--lua新版不支持arg 所以用这个模拟
    for i = 1, #arg do
        str = string.gsub(str, "{"..i.."}", arg[i])
    end
    return str
end

--转换数字成中文形式;
--num:数字
local chineseSet = {'零','一','二','三','四','五','六','七','八','九','十'}
function StringUtils.convertNumToChinese(num)
    if num == nil then return 'none' end
    local chineseNum = ''
    if num <= 0 then
        chineseNum = chineseSet[1] 
    elseif num < 10 then
        chineseNum = chineseSet[num+1]
    elseif num < 100 then
        local sw = math.floor(num / 10)
        local gw = num % 10
        if sw == 1 and gw == 0 then
            chineseNum = '十'
        elseif gw == 0 then
            chineseNum = chineseSet[sw+1]..'十'
        else
            chineseNum = chineseSet[sw+1]..'十'..chineseSet[gw+1]
        end
    elseif num < 999 then
        chineseNum = num  --(有需要的可以扩展)
    end
    return chineseNum
end

--Description:将数字转换为对应类型的美术字;
--num:数字
--images:已经缓存了的UImage组
--type:参见ConstantData.NUMBER_ART_TYPE
function StringUtils.convertNumToArtNumbers(num, images, parent, firstX, firstY, type, scale)
    images = images or {};
    local perNumWidth = 0;
    scale = scale or 1;
    if type == ConstantData.NUMBER_ART_TYPE.FIGHT then 
        perNumWidth = ConstantData.NUMBER_ART_TYPE.FIGHT_WIDTH * scale;
    elseif type == ConstantData.NUMBER_ART_TYPE.LVL then
        perNumWidth = ConstantData.NUMBER_ART_TYPE.LVL_WIDTH * scale;
    end
    local tmpNum = tostring(num);
    tmpNum = StringUtils.trim(tmpNum);
    local tmpItemNum = 0;
    local tmpUImg = nil;
    local tmpImg = nil;
    local tmpNewInstantiate = nil;
    local numLength = string.len(tmpNum);
    for i=1,numLength do
        tmpItemNum = string.sub(tmpNum, i, i);
        tmpUImg = images[i];
        if IsNil(tmpUImg) == true then
            tmpNewInstantiate = GameObject.New(tostring(i));
            tmpNewInstantiate = tmpNewInstantiate:AddComponent("UImage");
            tmpUImg = tmpNewInstantiate;
            table.insert(images, tmpUImg);
        end
        tmpImg = tmpUImg.gameObject:GetComponent("Image");
        if tmpImg == nil then
           tmpImg = tmpUImg.gameObject:AddComponent("Image");
        end
        tmpUImg.gameObject:SetActive(true);
        tmpUImg:setSpriteName("publicResources", StringUtils.formatNumToArtNumberString(tmpItemNum, type));
        tmpUImg.transform:SetParent(parent)
        tmpUImg.transform.localScale = Vector3.one;
        tmpImg:SetNativeSize();
        local tmpSizeDelta = tmpUImg.gameObject.transform.sizeDelta;
        tmpSizeDelta = Vector2.New(tmpSizeDelta.x*scale, tmpSizeDelta.y*scale);
        tmpUImg.gameObject.transform.sizeDelta = tmpSizeDelta;
    end
    --去除多余的Images显示;
    for i=1,#images do
        if i>numLength then
            images[i].gameObject:SetActive(false);
        else
            images[i].transform.localPosition = Vector3.New(firstX + (i-1)*perNumWidth, firstY, 0);
        end
    end
    return images;
end

function StringUtils.formatNumToArtNumberString(num, type)
    if num == ":" then
        --GameLog"xyj","num == ")
        return ConstantData.NUMBER_ART_TYPE.LVL_DOT
    end

    if num == "x" then
        return ConstantData.NUMBER_ART_TYPE.LV_X;
    end

    if num == "/" then
        return ConstantData.NUMBER_ART_TYPE.LV_PAG;
    end

    if type == ConstantData.NUMBER_ART_TYPE.FIGHT then 
        return ConstantData.NUMBER_ART_TYPE.FIGHT_FORMAT..num;
    elseif type == ConstantData.NUMBER_ART_TYPE.LVL then
        return ConstantData.NUMBER_ART_TYPE.LVL_FORMAT..num;
    end

    return nil;
end

function StringUtils.stringToTable(str)
   if(str==nil)then
        return nil; 
   end
   local ret = loadstring("return "..str)()
   return ret
end

--分割数字到数组中;
function StringUtils.splitNumToArr(num_)
    local tmpNum = tostring(num_);
    tmpNum = StringUtils.trim(tmpNum);
    local tmpItemNum = 0;
    local numLength = string.len(tmpNum);
    local rtnNumTable = {};
    for i=1,numLength do
        tmpItemNum = string.sub(tmpNum, i, i);
        table.insert(rtnNumTable, tmpItemNum);
    end
    return rtnNumTable;
end

-- 将数字转成16进制的字符串
function StringUtils.numberToHexString(num)
	return "0x" .. string.format("%x", num)
end

-- 解析同步位置字符串
-- 格式：x*y*z
-- return Vector3
function StringUtils.decodeSyncVector3(posStr)
	if posStr == nil then return nil end
	 local arr = StringUtils.split(posStr, "*", nil, tonumber)
	 return Vector3.New(arr[1], arr[2], arr[3])
--    return LuaHelper.decodeSyncVector3(posStr)
end

-- 将Vector3转成同步字符串信息
function StringUtils.encodeSyncVector3(position)
	if position == nil then return "" end
	return math.round(position.x, 2) .. "*" .. math.round(position.y, 2) .. "*" .. math.round(position.z, 2)
end