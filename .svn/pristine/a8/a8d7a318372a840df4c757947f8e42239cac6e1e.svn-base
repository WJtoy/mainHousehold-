(function() {
	$(".btnCity").click(function(event) {
		event.stopPropagation();
		if( $(".provinceCityAll").length == 0){
			var areahtml = '<div class="tabs clearfix">' +
			'<ul class=""><li><a href="javascript:" tb="provinceAll">省份</a></li><li><a href="javascript:" tb="cityAll" id="cityAll">城市</a></li><li><a href="javascript:" tb="countyAll" id="countyAll">区县</a></li></ul>'+
			'</div>' +
			'<div class="con"><div class="provinceAll invis"><div class="list"><ul></ul></div></div><div class="cityAll invis"><div class="list"><ul></ul></div></div><div class="countyAll invis"><div class="list"><ul></ul></div></div></div>';
			var areadiv = $(document.createElement('div')).html(areahtml).addClass('provinceCityAll');
			areadiv.appendTo('body');
		}
		//$(".provinceCity").hide();
		$(".provinceCityAll").hide();
		//var txtcity = $(this).prev();
		var txtcity = $(this).prevAll("input").first();
		var o2 = txtcity.offset();
		var l2 = o2.left;
		var t2 = o2.top;
		var h2 = txtcity.height();
		$(".provinceCityAll").css("top", t2 + h2 + 10 - 1).css("left", l2).toggle();
		$(".provinceCityAll").click(function(event) {
			event.stopPropagation();
		});
		event.stopPropagation();
		$("html").click(function() {
			$(".provinceCityAll").hide();
			//如果没有选择到区县，清空让验证器报错
			if( $("input.current2").attr("data-checkselect") == "true" && $("input.current2").prev().val() == ""){
				$("input.current2").val("");
			}
		});
		$("input.proCitySelAll").removeClass("current2");
		txtcity.addClass("current2");
		/*if ($("body").data("CitysAll") == null) {
			sendAllCitiesAjax();
		}*/
		if ( $(".provinceCityAll").find(".tabs").find("a.current").length == 0) {
			$(".provinceCityAll").find(".tabs").find("a").removeClass("current");
			$(".provinceCityAll").find(".tabs").find("a[tb=provinceAll]").addClass("current");
			$(".provinceCityAll").find(".con").children().hide();
			$(".provinceCityAll").find(".con").find(".provinceAll").show();
		} else {
			$(".provinceCityAll").show();	
		}
		
		if ($("body").data("allProvinces") == null) {
			sendAllProvinceAjax();
		}
		/*
		if ($("body").data("allCountys") == null) {
			sendAllCountiesAjax();
		}*/
		$(".provinceCityAll").find(".tabs").find("a").click(function() {
			if ($(this).attr("tb") == "cityAll" && $(".provinceAll .list .current").val() == null) {
				return;
			};
			if ($(this).attr("tb") == "countyAll" && $(".cityAll .list .current").val() == null ) {//&& $(".hotCityAll .list .current").val() == null
				return;
			};
			$(".provinceCityAll").find(".tabs").find("a").removeClass("current");
			$(this).addClass("current");
			var tb = $(this).attr("tb");
			$(".provinceCityAll").find(".con").children().hide();
			$(".provinceCityAll").find(".con").find("." + tb).show();
		});
	});
})();
function json2str(o) {
	var arr = [];
	var fmt = function(s) {
		if (typeof s == 'object' && s != null) return json2str(s);
		return /^(string|number)$/.test(typeof s) ? "'" + s + "'": s;
	};
	for (var i in o) arr.push("'" + i + "':" + fmt(o[i]));
	return '{' + arr.join(',') + '}';
}

var allProvinces = null;
var allCities = null;
var allAreas = null;
var allProId = null;
var cityIdAll = null;
var allCitys = null;

//Province
function sendAllProvinceAjax() {
	var url = $("input.proCitySelAll").eq(0).attr("data-url");
	$.ajax({
		type: "get",
		url: url+'/sys/area/service/arealist?type=2',
		//async: false,
		dataType: "json",
		success: function(data) {
			allProvinces = data;
			$("body").data("allProvinces", allProvinces);
			viewAllProvince();
		},
		error: function(XMLHttpRequest, textStatus, errorThrown)
		 {
		 	layerError("装载区域错误","错误提示");
			// top.$.jBox.error(textStatus);
		}
	});
}
//show
function viewAllProvince() {
	$(".provinceAll .list ul li").remove();
	for (var i = 0; i < allProvinces.length; i++) {
		var p_id = allProvinces[i].id;
		var p_name = allProvinces[i].name;
		if (allProvinces[i].name == '内蒙古自治区') {
			p_name = '内蒙古';
		} else if (allProvinces[i].name == '黑龙江省') {
			p_name = '黑龙江';
		} else {
			p_name = allProvinces[i].name.substr(0, 2);
		}
		var li = $('<li><a style="background: none repeat scroll 0% 0% transparent; border: 0px none;" href="javascript:onclick=viewCities(' + i + ');" id="' + p_id + '">' + p_name + '</a></li>');
		$(".provinceAll .list ul").append(li);
	}
}

//city
function viewCities(i) {
	allProId = allProvinces[i].id;
	if($("input.current2").attr("data-checkselect") == "true"){
		$("input.current2").prev().val("");
	}else{
		$("input.current2").prev().val(allProId);
	}
	$("input.current2").val( allProvinces[i].name);
	$("body").data("pname", allProvinces[i].name);
	$("body").data("pid", allProId);
	$("body").data("cname", "");
	$("body").data("cid", null);
	
	$(".provinceCityAll").find(".tabs").find("a").removeClass("current");
	$(".provinceCityAll .tabs").find("#cityAll").addClass("current");
	$(".con .provinceAll .list a").removeClass("current");
	$(".con .provinceAll .list a[id='" + allProId + "']").addClass("current");
	$(".provinceCityAll").find(".con").children().hide();
	$(".provinceCityAll").find(".con").find(".cityAll").show();
	$(".cityAll .list ul li").empty();
	$(".cityAll .list ul li").remove();
	
	var citys = $("body").data("cityofpro_"+allProId);
	if(citys == null){
		sendCitiesAjax(allProId);
	}
	citys = $("body").data("cityofpro_"+allProId);
	if(citys == null){
        layerError("装载城市列表错误","错误提示");
		//top.$.jBox.error("读取城市列表错误");
		return false;
	}
	
	//show
	
	for (var i = 0; i < citys.length; i++) {
		var c_id = citys[i].id;
		var cityName = citys[i].name.substr(0, 4);
		var li = $('<li><a href="javascript:onclick=viewCounties(' + i+')" id="' + c_id + '">' + cityName + '</a></li>');
		$(".cityAll .list ul").append(li);
	}
}

function sendCitiesAjax(proid) {
	var url = $("input.proCitySelAll").eq(0).attr("data-url");
	$(".cityAll .list ul").append("<div class='loading'><i></i><span>读取数据...</span></div>");
	$.ajax({
		type: "get",
		url: url+'/sys/area/service/arealist?type=3&id=' + proid,
		async: false,
		dataType: "json",
		success: function(data) {
			$("body").data("cityofpro_"+proid, data);
			$(".cityAll .list ul").empty();
		},
		error: function(XMLHttpRequest, textStatus, errorThrown)
		 {
			$(".cityAll .list ul").empty();
			//top.$.jBox.error(textStatus);
             layerError("装载区域错误","错误提示");
			return false;
		}
	});
}

//county
function viewCounties(i) {
	var pid =$("body").data("pid");
	var pname = $("body").data("pname");
	var citys = $("body").data("cityofpro_"+pid);
	var cid = citys[i].id;
	var cname = $.trim(citys[i].name);
	$("body").data("cid", cid);
	$("body").data("cname", cname);
	$(".provinceCityAll").find(".tabs").find("a").removeClass("current");
	$(".provinceCityAll .tabs").find("#countyAll").addClass("current");
	$(".con .cityAll .list a").removeClass("current");
	$(".con .cityAll .list a[id='" + cid + "']").addClass("current");
	$(".provinceCityAll").find(".con").children().hide();
	$(".provinceCityAll").find(".con").find(".countyAll").show();
	$(".countyAll .list ul li").empty();
	$(".countyAll .list ul li").remove();
	
	var counties = $("body").data("countyofcity_"+cid);
	if(counties == null){
		sendCoutiesAjax(cid);
	}
	counties = $("body").data("countyofcity_"+cid);
	if(counties == null){
		top.$.jBox.error("读取城市列表错误");
		return false;
	}
	
	//show
	$("input.current2").removeClass("iGrays");
	$("input.current2").val(pname + " " + cname);
	
	for (var i = 0; i < counties.length; i++) {
		var c_id = counties[i].id;
		var countyName = counties[i].name.substr(0, 4);;
		var li = $('<li><a href="javascript:onclick=addrInputAll(' + i + ')" id="' + c_id + '">' + countyName + '</a></li>');
		$(".countyAll .list ul").append(li);
	}
}

function sendCoutiesAjax(cid) {
	var url = $("input.proCitySelAll").eq(0).attr("data-url");
	$(".countyAll .list ul").append("<div class='loading'><i></i><span>读取数据...</span></div>");
	$.ajax({
		type: "get",
		url: url+'/sys/area/service/arealist?type=4&id=' + cid,
		async: false,
		dataType: "json",
		success: function(data) {
			$("body").data("countyofcity_"+cid, data);
			$(".countyAll .list ul").empty();
		},
		error: function(XMLHttpRequest, textStatus, errorThrown)
		 {
			$(".countyAll .list ul").empty();
			top.$.jBox.error(textStatus);
			return false;
		}
	});
}

function addrInputAll(i) {
	var pname = $("body").data("pname");
	var cname = $("body").data("cname");
	var cid = $("body").data("cid");
	var counties = $("body").data("countyofcity_"+cid);
	var tid = counties[i].id;
	var tname = $.trim(counties[i].name);
	$(".con .hotCityAll .list a input").removeClass("current");
	$(".con .hotCityAll .list a input[id='" + cid + "']").addClass("current");
	$(".con .countyAll .list a").removeClass("current");
	$(".con .countyAll .list a[id='" + tid + "']").addClass("current");
	
	
	var nameValue = $("input.current2");
	nameValue.removeClass("iGrays");
	$(".provinceCityAll").hide();
	var rtn = pname + " " + cname + " " + tname;
	$("input.current2").val(rtn);
	$(".backifname").hide();
	$("input.current2").prev().val(tid);
}
$(document).ready(function() {
	if( $(".provinceCityAll").length == 0){
		var areahtml = '<div class="tabs clearfix">' +
		'<ul class=""><li><a href="javascript:" tb="provinceAll">省份</a></li><li><a href="javascript:" tb="cityAll" id="cityAll">城市</a></li><li><a href="javascript:" tb="countyAll" id="countyAll">区县</a></li></ul>'+
		'</div>' +
		'<div class="con"><div class="provinceAll invis"><div class="list"><ul></ul></div></div><div class="cityAll invis"><div class="list"><ul></ul></div></div><div class="countyAll invis"><div class="list"><ul></ul></div></div></div>';
		var areadiv = $(document.createElement('div')).html(areahtml).addClass('provinceCityAll');
		areadiv.appendTo('body');
	}
	if ($("body").data("allProvinces") == null) {
		sendAllProvinceAjax();
	}
});