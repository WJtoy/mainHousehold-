﻿ 
/**
 * 常用工具类，提供了大量静态常用方法。
 *
 */
var Utils = new Object();

/**
 * 判断对象是否为null
 * @param obj
 */
Utils.isNull = function(obj){
	return (obj == undefined || obj == null || typeof(obj) == "undefined")? true : false;
}

/**
 * 判断对象是否为对象
 * 
 * @param Object obj 欲判断的对象
 * @return boolean true - 是对象，false - 不是对象
 */
Utils.isObject = function(obj){
	return ( obj == null || typeof(obj) == "undefined" ) ? false : true;
}

/**
 * 判断输入是否为货币数值
 * 
 * @param float money
 * @return boolean
 */
Utils.isMoney = function(money){

//	return /^[1-9][0-9]*\.[0-9]{2}/.test(money) ||
//		   /^0\.[0-9]{2}/.test(money);
	return Utils.isFloatValue(money);
}

/**
 * 判断一个对象是不是函数对象
 * 
 * @param fun 对象
 * @return boolean
 */
Utils.isFunction = function(fun){
	return (fun != null && typeof(fun) == "function");
}

/**
 * 判断一个数值（或字符串）是否为整数，包含0和正负数。
 * 
 */
Utils.isInteger = function(num){
	return /^-?\d+$/.test(num);
}

/**
 * 判断一个数值（或字符串）是否为正整数，不包含0。
 * 
 */
Utils.isPositiveInteger = function(num){
	return /^[0-9]*[1-9][0-9]*/.test(num);
}

/**
 * 判断一个数值（或字符串）是否为非负数，包含0和正数。
 * 
 */
Utils.isNonNegativeInteger = function(num){
	
	return /^[1-9][0-9]*/.test(num) || (num == 0);
}

/**
 * 判断一个数值（或字符串）是否为非正数，包含0和负数。
 * 
 */
Utils.isNonPositiveInteger = function(num){
	
	return  /^((-\d+)|(0+))$/.test(num) || (num == 0);
}

/**
 * 验证是否为浮点数
 * 
 * @param String str 
 * @return boolean
 */
Utils.isFloatValue = function(floatValue){
    
    var reg = /(^((-|\+)?0\.)(\d*)$)|(^((-|\+)?[1-9])+\d*(\.\d*)?$)/;  
    
    return reg.test(floatValue);    
}

/**
 * 判断一个数值（或字符串）是否为浮点数。
 * 
 */
Utils.isFloat = function(num){
	return /^(-?\d+)(\.\d+)?$/.test(num);
}

/**
 * 判断一个数值（或字符串）是否为正浮点数，不包含0。
 * 
 */
Utils.isPositiveFloat = function(num){
	return /^(([0-9]+\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\.[0-9]+)|([0-9]*[1-9][0-9]*))$/.test(num);
}

/**
 * 判断一个数值（或字符串）是否为非负浮点数，包含0和正数。
 * 
 */
Utils.isNonNegativeFloat = function(num){
	return /^(-?\d+)(\.\d+)?$/.test(num);
}

/**
 * 判断一个数值（或字符串）是否为非正浮点数，包含0和负数。
 * 
 */
Utils.isNonPositiveFloat = function(num){
	return /^((-\d+(\.\d+)?)|(0+(\.0+)?))$/.test(num);
}

/**
 * 验证商品类别编号格式是否合法
 * 
 * @param code
 * @return boolean
 */
Utils.isWareCategoryCode = function(code){
    
    var reg = /^\w+$/;
    
    if (!reg.test(code)){
       return false;
    }
    
    return true;
}

Utils.isKHtml = /Konqueror|Safari|KHTML/i.test(navigator.userAgent);
// Utils.isIE = (/msie/i.test(navigator.userAgent) &&
// 		   	  !/opera/i.test(navigator.userAgent));

//判断是否是IE浏览器
Utils.isIE = function(){

        var userAgent = navigator.userAgent;//取得浏览器的userAgent字符串
        var isIE  = userAgent.indexOf("compatible") > -1 &&  userAgent.indexOf("MSIE") > -1
			&&  userAgent.indexOf("Opera") > -1;
        if(isIE)
        {
            return true;
        }
        else
        {
            return false;
        }
}

Utils.isIE5 = (Utils.isIE && /msie 5\.0/i.test(navigator.userAgent));

/**
 * 获取某HTML元素的符合指定样式名称的第一个父元素。
 * 
 * @param HTMLElement 需要匹配的HTML元素
 * @param String className 匹配的样式名称
 * @return HTMLElement 返回符合条件的父元素，或返回空
 */
Utils.getParentByClassName = function(el, className){
	if (!el)
		return null;
		
	var e = el.parentNode;
	if (e.className == className)
		return e;
		
	Utils.getParentByClassName(e, className);
}

/**
 * 获取某HTMLElement下指定样式的子元素集合
 * 
 * @param HTMLElment el
 * @param String className
 * @return Array
 */
Utils.getChildrenByClassName = function(el, className){
	if (!el)
	var children = new Array();
	for (var i=0; i<el.childNodes.length; i++){
		if (el.childNodes[i].className == className)
			children.push(el.childNodes[i]);
	}
	return children;
}

/**
 * 
 */
Utils.ScriptFragmentRegExp = new RegExp('(?:<script.*?>)((\n|\r|.)*?)(?:<\/script>)', 'img');

/**
 * 在指定的字符串中执行其中包含的脚本。
 * 
 * @param String str
 */
Utils.evalScripts = function(str){
	var scripts = str.match(Utils.ScriptFragmentRegExp);
	
	if (scripts) {
      	match = new RegExp(Prototype.ScriptFragment, 'im');
      	setTimeout((function() {
       		for (var i = 0; i < scripts.length; i++)
          		eval(scripts[i].match(match)[1]);
      	}).bind(this), 10);
   }
}

/**
 * 删除指定字符串中的所有脚本块。
 * 
 * @param String str
 * @return String
 */
Utils.omitScriptFragment = function(str){
    return str.replace(Utils.ScriptFragmentRegExp, '');
}

Utils.getComputedStyle = function(str){
	var reg = /[0-9]+/i;
	var value = str.match(reg);
	
	return (value == null) ? 0 : parseInt(value);
}

/**
 * 获取某个元素的绝对坐标。
 * 
 * @param HMTLElement el
 * @return Object
 */
Utils.getAbsolutePos = function(el) {
	var SL = 0, ST = 0;
	var is_div = /^div$/i.test(el.tagName);
	if (is_div && el.scrollLeft)
		SL = el.scrollLeft;
	if (is_div && el.scrollTop)
		ST = el.scrollTop;
	var r = { x: el.offsetLeft - SL, y: el.offsetTop - ST };
	if (el.offsetParent) {
		var tmp = this.getAbsolutePos(el.offsetParent);
		r.x += tmp.x;
		r.y += tmp.y;
	}
	
	return r;
}

/**
 * HTML页面中比较龌龊的元素
 * 
 */
Utils.TERRIABLE_ELEMENTS = new Array("applet", "iframe", "select");

/**
 * 屏蔽HTML页面中比较龌龊的元素，如applet、iframe、select。
 * 由于Div在显示在这些元素之上，会出现破坏Div元素的情形，因此将在Div元素下的这些龌龊的元素隐藏。
 * 
 * @param HTMLEelement el 需要屏蔽龌龊元素的HTMLElement对象
 */
Utils.hideShowCovered = function(el){
	function getVisib(obj){
		var value = obj.style.visibility;
		if (!value) {
			if (document.defaultView && typeof (document.defaultView.getComputedStyle) == "function") { // Gecko, W3C
				if (!Utils.isKHtml)
					value = document.defaultView.
						getComputedStyle(obj, "").getPropertyValue("visibility");
				else
					value = '';
			} else if (obj.currentStyle) { // IE
				value = obj.currentStyle.visibility;
			} else
				value = '';
		}
		return value;
	};

	var p = Utils.getAbsolutePos(el);
	var EX1 = p.x;
	var EX2 = el.offsetWidth + EX1;
	var EY1 = p.y;
	var EY2 = el.offsetHeight + EY1;

	for (var k = Utils.TERRIABLE_ELEMENTS.length; k > 0; ) {
		var ar = document.getElementsByTagName(Utils.TERRIABLE_ELEMENTS[--k]);
		var cc = null;

		for (var i = ar.length; i > 0;) {
			cc = ar[--i];

			p = Utils.getAbsolutePos(cc);
			var CX1 = p.x;
			var CX2 = cc.offsetWidth + CX1;
			var CY1 = p.y;
			var CY2 = cc.offsetHeight + CY1;

			if (self.hidden || (CX1 > EX2) || (CX2 < EX1) || (CY1 > EY2) || (CY2 < EY1)) {
				if (!cc.__msh_save_visibility) {
					cc.__msh_save_visibility = getVisib(cc);
				}
				cc.style.visibility = cc.__msh_save_visibility;
			} else {
				if (!cc.__msh_save_visibility) {
					cc.__msh_save_visibility = getVisib(cc);
				}
				cc.style.visibility = "hidden";
			}
		}
	}
}

/**
 * 
 * 
 */
Utils.calculateStringWidth = function(str){
	if (Utils.isEmpty(str))
		return 0;
		
	var span = document.createElement("span");
	span.innerHTML = str;
	document.body.appendChild(span);
	
	var width = span.offsetWidth;
	document.body.removeChild(span);
	
	return width;
}

Utils.autoFitImage = function(img, width, height){
	
	if (!img || img.height == 0 || img.width == 0)
		return "&nbsp";
	
	var str = "<img src='" + img.src + "' ";
	
	if (img.width > width || img.height > height){
		var widthRate = img.width / width;
		var heightRate = img.height / height;
		if (widthRate > heightRate){
			str += "width='" + width + "' ";
			img.width = width;
		}else{
			str += "height='" + height + "' ";
			img.height = height;
		}
	}
		
	str += "/>";
	
	return str;
}

/*------------------- String ------------------------------*/


/**
 * 去除字符串中的头尾空白字符
 * 
 */
if (!String.prototype.trim){
	String.prototype.trim  =  function(){
    	return  this.replace(/(^\s*)|(\s*$)/g,  "");
	}
}

/**
 * 根据显示字符串的容器的宽度，自动截取字符串。
 * 
 * @param int containerWidth 显示字符串的容器的宽度
 * @return String
 */
String.prototype.substrByContainerWidth = function(len){
	/*var titleWidth = Utils.calculateStringWidth(this);
	if (titleWidth > containerWidth){
		var length = parseInt(this.length * (containerWidth/titleWidth));
		return this.substr(0, length-1)+'...'
	}else 
		return this;
	*/
	//length属性读出来的汉字长度为1
    if(this.length*2 <= len) {
        return this;
    }
    var strlen = 0;
    var s = "";
    for(var i = 0;i < this.length; i++) {
        s = s + this.charAt(i);
        if (this.charCodeAt(i) > 128) {
            strlen = strlen + 2;
            if(strlen >= len){
                return s.substring(0,s.length-1) + "...";
            }
        } else {
            strlen = strlen + 1;
            if(strlen >= len){
                return s.substring(0,s.length-2) + "...";
            }
        }
    }
    return s;
}

/**
 * 判断字符串是否以某个字符串开始
 * 
 * @param String prefix
 * @return boolean
 */
if (!String.startsWith){
	String.prototype.startsWith = function(prefix){
		if (Utils.isEmpty(prefix))
			return false;
			
		return (this.indexOf(prefix) > -1);
	}
}

/*----------------- StringUtils ----------------------------*/
/**
 * 去两边空格
 */
Utils.trim = function (str){

    var reg = new RegExp("^\\s+|\\s+$","g");
    reg = /^\s+|\s+$/g;

    return str.replace(reg,"");
}
/**
 * 去左边空格
 */
Utils.ltrim = function (str){
    return str.replace(/^\s+/,"");
}
/**
 * 去右边空格
 */
Utils.rtrim = function (str){
    return str.replace(/\s+$/,"");
}

/**
 * 判断是否为空字符串
 */
Utils.isEmpty = function(str){
	return (!Utils.isObject(str) || str.length == 0) ? true : false;
}

/**
 * html转码
 */
Utils.htmlEncode = function(html) {
    var s = "";
    if (html.length == 0) return "";
    s = html.replace(/&/g, "&amp;");
    s = s.replace(/</g, "&lt;");
    s = s.replace(/>/g, "&gt;");
    s = s.replace(/ /g, "&nbsp;");
    s = s.replace(/\'/g, "&#39;");
    s = s.replace(/\"/g, "&quot;");
    return s;
}

/**
 * html解码
 */
Utils.htmlDecode = function(html) {
    var s = "";
    if (html.length == 0) return "";
    s = html.replace(/&amp;/g,"&");
    s = s.replace(/&lt;/g,"<");
    s = s.replace(/&gt;/g,">");
    s = s.replace(/&nbsp;/g," ");
    s = s.replace(/&#39;/g,"\'");
    s = s.replace(/&quot;/g,"\"");
    return s;
}

/*----------------- DateUtil ----------------------------*/
/**
* 日期处理工具类
*/
var DateUtil = new Object();
/**
 * 判断闰年
 * @param date Date日期对象
 * @return boolean true 或false
 */
DateUtil.isLeapYear = function(date){
    return (0==date.getYear()%4&&((date.getYear()%100!=0)||(date.getYear()%400==0)));
}
 
/**
 * 日期对象转换为指定格式的字符串
 * @param f 日期格式,格式定义如下 yyyy-MM-dd HH:mm:ss
 * @param date Date日期对象, 如果缺省，则为当前时间
 *
 * YYYY/yyyy/YY/yy 表示年份 
 * MM/M 月份 
 * W/w 星期 
 * dd/DD/d/D 日期 
 * hh/HH/h/H 时间 
 * mm/m 分钟 
 * ss/SS/s/S 秒 
 * @return string 指定格式的时间字符串
 */
DateUtil.dateToStr = function(formatStr, date){
    formatStr = arguments[0] || "yyyy-MM-dd HH:mm:ss";
    date = arguments[1] || new Date();
    var str = formatStr;  
    var Week = ['日','一','二','三','四','五','六']; 
    str=str.replace(/yyyy|YYYY/,date.getFullYear());  
    str=str.replace(/yy|YY/,(date.getYear() % 100)>9?(date.getYear() % 100).toString():'0' + (date.getYear() % 100));  
    str=str.replace(/MM/,date.getMonth()>9?(date.getMonth() + 1):'0' + (date.getMonth() + 1));  
    str=str.replace(/M/g,date.getMonth());  
    str=str.replace(/w|W/g,Week[date.getDay()]);  
   
    str=str.replace(/dd|DD/,date.getDate()>9?date.getDate().toString():'0' + date.getDate());  
    str=str.replace(/d|D/g,date.getDate());  
   
    str=str.replace(/hh|HH/,date.getHours()>9?date.getHours().toString():'0' + date.getHours());  
    str=str.replace(/h|H/g,date.getHours());  
    str=str.replace(/mm/,date.getMinutes()>9?date.getMinutes().toString():'0' + date.getMinutes());  
    str=str.replace(/m/g,date.getMinutes());  
   
    str=str.replace(/ss|SS/,date.getSeconds()>9?date.getSeconds().toString():'0' + date.getSeconds());  
    str=str.replace(/s|S/g,date.getSeconds());  
       
    return str;  
}
 
     
    /**
* 日期计算 
* @param strInterval string  可选值 y 年 m月 d日 w星期 ww周 h时 n分 s秒 
* @param num int
* @param date Date 日期对象
* @return Date 返回日期对象
*/
DateUtil.dateAdd = function(strInterval, num, date){
    date =  arguments[2] || new Date();
    switch (strInterval) {
        case 's' :return new Date(date.getTime() + (1000 * num)); 
        case 'n' :return new Date(date.getTime() + (60000 * num)); 
        case 'h' :return new Date(date.getTime() + (3600000 * num)); 
        case 'd' :return new Date(date.getTime() + (86400000 * num)); 
        case 'w' :return new Date(date.getTime() + ((86400000 * 7) * num)); 
        case 'm' :return new Date(date.getFullYear(), (date.getMonth()) + num, date.getDate(), date.getHours(), date.getMinutes(), date.getSeconds()); 
        case 'y' :return new Date((date.getFullYear() + num), date.getMonth(), date.getDate(), date.getHours(), date.getMinutes(), date.getSeconds()); 
    } 
} 
 
/**
* 比较日期差 dtEnd 格式为日期型或者有效日期格式字符串
* @param strInterval string  可选值 y 年 m月 d日 w星期 ww周 h时 n分 s秒 
* @param dtStart Date  可选值 y 年 m月 d日 w星期 ww周 h时 n分 s秒
* @param dtEnd Date  可选值 y 年 m月 d日 w星期 ww周 h时 n分 s秒
*/
DateUtil.dateDiff = function(strInterval, dtStart, dtEnd) {  
    switch (strInterval) {  
        case 's' :return parseInt((dtEnd - dtStart) / 1000); 
        case 'n' :return parseInt((dtEnd - dtStart) / 60000); 
        case 'h' :return parseInt((dtEnd - dtStart) / 3600000); 
        case 'd' :return parseInt((dtEnd - dtStart) / 86400000); 
        case 'w' :return parseInt((dtEnd - dtStart) / (86400000 * 7)); 
        case 'm' :return (dtEnd.getMonth()+1)+((dtEnd.getFullYear()-dtStart.getFullYear())*12) - (dtStart.getMonth()+1); 
        case 'y' :return dtEnd.getFullYear() - dtStart.getFullYear(); 
    } 
}
 
    /**
* 字符串转换为日期对象
* @param date Date 格式为yyyy-MM-dd HH:mm:ss，必须按年月日时分秒的顺序，中间分隔符不限制
*/
DateUtil.strToDate = function(dateStr){
    var data = dateStr; 
    var reCat = /(\d{1,4})/gm;  
    var t = data.match(reCat);
    t[1] = t[1] - 1;
    eval('var d = new Date('+t.join(',')+');');
    return d;
}
 
    /**
* 把指定格式的字符串转换为日期对象yyyy-MM-dd HH:mm:ss
*
*/
DateUtil.strFormatToDate = function(formatStr, dateStr){
    var year = 0;
    var start = -1;
    var len = dateStr.length;
    if((start = formatStr.indexOf('yyyy')) > -1 && start < len){
        year = dateStr.substr(start, 4);
    }
    var month = 0;
    if((start = formatStr.indexOf('MM')) > -1  && start < len){
        month = parseInt(dateStr.substr(start, 2)) - 1;
    }
    var day = 0;
    if((start = formatStr.indexOf('dd')) > -1 && start < len){
        day = parseInt(dateStr.substr(start, 2));
    }
    var hour = 0;
    if( ((start = formatStr.indexOf('HH')) > -1 || (start = formatStr.indexOf('hh')) > 1) && start < len){
        hour = parseInt(dateStr.substr(start, 2));
    }
    var minute = 0;
    if((start = formatStr.indexOf('mm')) > -1  && start < len){
        minute = dateStr.substr(start, 2);
    }
    var second = 0;
    if((start = formatStr.indexOf('ss')) > -1  && start < len){
            second = dateStr.substr(start, 2);
    }
    return new Date(year, month, day, hour, minute, second);
}
 
 
    /**
* 日期对象转换为毫秒数
*/
DateUtil.dateToLong = function(date){
    return date.getTime();
}
 
    /**
* 毫秒转换为日期对象
* @param dateVal number 日期的毫秒数
*/
DateUtil.longToDate = function(dateVal){
    return new Date(dateVal);
}
 
    /**
* 判断字符串是否为日期格式
* @param str string 字符串
* @param formatStr string 日期格式， 如下 yyyy-MM-dd
*/
DateUtil.isDate = function(str, formatStr){
    if (formatStr == null){
        formatStr = "yyyyMMdd";   
    }
    var yIndex = formatStr.indexOf("yyyy");    
    if(yIndex==-1){
        return false;
    }
    var year = str.substring(yIndex,yIndex+4);    
    var mIndex = formatStr.indexOf("MM");    
    if(mIndex==-1){
        return false;
    }
    var month = str.substring(mIndex,mIndex+2);    
    var dIndex = formatStr.indexOf("dd");    
    if(dIndex==-1){
        return false;
    }
    var day = str.substring(dIndex,dIndex+2);    
    if(!isNumber(year)||year>"2100" || year< "1900"){
        return false;
    }
    if(!isNumber(month)||month>"12" || month< "01"){
        return false;
    }
    if(day>getMaxDay(year,month) || day< "01"){
        return false;
    }
    return true;  
}
 
DateUtil.getMaxDay = function(year,month) {    
    if(month==4||month==6||month==9||month==11)    
        return "30";    
    if(month==2)    
        if(year%4==0&&year%100!=0 || year%400==0)    
            return "29";    
        else    
            return "28";    
    return "31";    
}    
/**
*   变量是否为数字
*/
DateUtil.isNumber = function(str)
{
    var regExp = /^\d+$/g;
    return regExp.test(str);
}
 
/**
* 把日期分割成数组 [年、月、日、时、分、秒]
*/
DateUtil.toArray = function(myDate) 
{  
    myDate = arguments[0] || new Date();
    var myArray = Array(); 
    myArray[0] = myDate.getFullYear(); 
    myArray[1] = myDate.getMonth(); 
    myArray[2] = myDate.getDate(); 
    myArray[3] = myDate.getHours(); 
    myArray[4] = myDate.getMinutes(); 
    myArray[5] = myDate.getSeconds(); 
    return myArray; 
} 
 
/**
* 取得日期数据信息 
* 参数 interval 表示数据类型 
* y 年 M月 d日 w星期 ww周 h时 n分 s秒 
*/
DateUtil.datePart = function(interval, myDate) 
{  
    myDate = arguments[1] || new Date();
    var partStr=''; 
    var Week = ['日','一','二','三','四','五','六']; 
    switch (interval) 
    {  
        case 'y' :partStr = myDate.getFullYear();break; 
        case 'M' :partStr = myDate.getMonth()+1;break; 
        case 'd' :partStr = myDate.getDate();break; 
        case 'w' :partStr = Week[myDate.getDay()];break; 
        case 'ww' :partStr = myDate.WeekNumOfYear();break; 
        case 'h' :partStr = myDate.getHours();break; 
        case 'm' :partStr = myDate.getMinutes();break; 
        case 's' :partStr = myDate.getSeconds();break; 
    } 
    return partStr; 
} 
 
/**
* 取得当前日期所在月的最大天数 
*/
DateUtil.maxDayOfDate = function(date) 
{  
    date = arguments[0] || new Date();
    date.setDate(1);
    date.setMonth(date.getMonth() + 1);
    var time = date.getTime() - 24 * 60 * 60 * 1000;
    var newDate = new Date(time);
    return newDate.getDate();
}

/**
 * 分钟转字符串，返回：xx小时xx分钟
 */
DateUtil.minuteToString = function(minutes)
{
	var hour=parseInt(minutes/60);
	var min= parseInt(minutes - (hour*60));
	var str = "";
	if (hour>0)
	{
		str += hour + "小时";
	}
	if (min>0)
	{
		str += parseFloat(min) + "分";
	}
	return str;
}

// form
Utils.formToHiddenInputHtml = function(formId){
    var str = "";
    if (!formId)
        return str;
	var $form = $("#" + formId);
	if(!$form){
		return str;
	}
	var queryUrl = $form.serialize();
	//pageNo=1&pageSize=12&repageFlag=false&customer.id=1482&orderNo=&userPhone=&beginDate=2017-03-01&endDate=2019-06-10&materialType=1&applyType=0&pendingType=0
	var params = queryUrl.split("&");
	if(params.length == 0){
		return str;
	}
	var param;
	var keyVal;
    for(var j = 0,len = params.length; j < len; j++) {
        param = params[j];
        keyVal = param.split("=");
        if (keyVal.length == 2) {
            str += "<input type='hidden' id='" + keyVal[0] + "' name='" + keyVal[0] + "' value='" + keyVal[1] + "' />";
        }
    }

    return str;
}