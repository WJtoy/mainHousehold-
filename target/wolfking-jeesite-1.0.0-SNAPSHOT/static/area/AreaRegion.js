/**
 * 区域选择js
 * 依赖jquery
 */
var CountryAreaUtil = {
    init: function (staticserver) {
        this.rootUrl = '';
        this.provinces = [];//省
        this.province = {};
        this.city = {};
        this.cities = {};
        this.counties = [];
        this.document = null;
        this.callback = null;
        // this.bindEvent();
    },
    open: function (obj, event) {
        var self = this;
        self.callback = null;
        //event.stopPropagation();
        if ($(".provinceCityAll", self.document).length == 0) {
            var areahtml = '<div class="tabs clearfix">' +
                '<ul class=""><li><a href="javascript:" tb="provinceAll">省份</a></li><li><a href="javascript:" tb="cityAll" id="cityAll">城市</a></li></ul>' +
                '</div>' +
                '<div class="con"><div class="provinceAll invis"><div class="list"><ul></ul></div></div><div class="cityAll invis"><div class="list"><ul></ul></div></div><div class="countyAll invis"><div class="list"><ul></ul></div></div></div>';
            var areadiv = $(self.document.createElement('div')).html(areahtml).addClass('provinceCityAll');
            // areadiv.appendTo('body');
            areadiv.appendTo(self.document.body);
        }
        $(".provinceCityAll", self.document).hide();
        var txtcity = $(obj).prevAll("input").first();
        var o2 = txtcity.offset();
        var l2 = o2.left;
        var t2 = o2.top;
        var h2 = txtcity.height();
        $(".provinceCityAll", self.document).css("top", t2 + h2 + 10 - 1).css("left", l2).toggle();
        $(".provinceCityAll", self.document).click(function (event) {
            event.stopPropagation();
        });
        event.stopPropagation();
        $("html", self.document).click(function () {
            $(".provinceCityAll", self.document).hide();
            //如果没有选择到区县，清空让验证器报错
            if ($("input.current2", self.document).attr("data-checkselect") == "true" && $("input.current2").prev().val() == "") {
                $("input.current2", self.document).val("");
            }
        });
        $("input.proCitySelAll", self.document).removeClass("current2");
        txtcity.addClass("current2");
        if ($(".provinceCityAll", self.document).find(".tabs").find("a.current").length == 0) {
            $(".provinceCityAll", self.document).find(".tabs").find("a").removeClass("current");
            $(".provinceCityAll", self.document).find(".tabs").find("a[tb=provinceAll]").addClass("current");
            $(".provinceCityAll", self.document).find(".con").children().hide();
            $(".provinceCityAll", self.document).find(".con").find(".provinceAll").show();
        } else {
            $(".provinceCityAll", self.document).show();
        }

        if (self.provinces == null || self.provinces.length == 0) {
            self.loadProvinces();
        } else if ($(".provinceAll .list ul li", self.document).length == 0) {
            self.showProvince();
        }
        $(".provinceCityAll", self.document).find(".tabs").find("a").click(function () {
            if ($(this).attr("tb") == "cityAll" && $(".provinceAll .list .current", self.document).val() == null) {
                return;
            }
            ;
            if ($(this).attr("tb") == "countyAll" && $(".cityAll .list .current", self.document).val() == null) {
                return;
            }
            ;
            $(".provinceCityAll", self.document).find(".tabs").find("a").removeClass("current");
            $(this).addClass("current");
            var tb = $(this).attr("tb");
            $(".provinceCityAll", self.document).find(".con").children().hide();
            $(".provinceCityAll", self.document).find(".con").find("." + tb).show();
        });
    },
    opencallback: function (obj, event, callback) {
        var self = this;
        self.callback = callback;
        //event.stopPropagation();
        if ($(".provinceCityAll", self.document).length == 0) {
            var areahtml = '<div class="tabs clearfix">' +
                '<ul class=""><li><a href="javascript:" tb="provinceAll">省份</a></li><li><a href="javascript:" tb="cityAll" id="cityAll">城市</a></li></ul>' +
                '</div>' +
                '<div class="con"><div class="provinceAll invis"><div class="list"><ul></ul></div></div><div class="cityAll invis"><div class="list"><ul></ul></div></div><div class="countyAll invis"><div class="list"><ul></ul></div></div></div>';
            var areadiv = $(self.document.createElement('div')).html(areahtml).addClass('provinceCityAll');
            // areadiv.appendTo('body');
            areadiv.appendTo(self.document.body);
        }
        $(".provinceCityAll", self.document).hide();
        var txtcity = $(obj).prevAll("input").first();
        var o2 = txtcity.offset();
        var l2 = o2.left;
        var t2 = o2.top;
        var h2 = txtcity.height();
        $(".provinceCityAll", self.document).css("top", t2 + h2 + 10 - 1).css("left", l2).toggle();
        $(".provinceCityAll", self.document).click(function (event) {
            event.stopPropagation();
        });
        event.stopPropagation();
        $("html", self.document).click(function () {
            $(".provinceCityAll", self.document).hide();
            //如果没有选择到区县，清空让验证器报错
            if ($("input.current2", self.document).attr("data-checkselect") == "true" && $("input.current2").prev().val() == "") {
                $("input.current2", self.document).val("");
            }
        });
        $("input.proCitySelAll", self.document).removeClass("current2");
        txtcity.addClass("current2");
        if ($(".provinceCityAll", self.document).find(".tabs").find("a.current").length == 0) {
            $(".provinceCityAll", self.document).find(".tabs").find("a").removeClass("current");
            $(".provinceCityAll", self.document).find(".tabs").find("a[tb=provinceAll]").addClass("current");
            $(".provinceCityAll", self.document).find(".con").children().hide();
            $(".provinceCityAll", self.document).find(".con").find(".provinceAll").show();
        } else {
            $(".provinceCityAll", self.document).show();
        }

        if (self.provinces == null || self.provinces.length == 0) {
            self.loadProvinces();
        } else if ($(".provinceAll .list ul li", self.document).length == 0) {
            self.showProvince();
        }
        $(".provinceCityAll", self.document).find(".tabs").find("a").click(function () {
            if ($(this).attr("tb") == "cityAll" && $(".provinceAll .list .current", self.document).val() == null) {
                return;
            }
            ;
            if ($(this).attr("tb") == "countyAll" && $(".cityAll .list .current", self.document).val() == null) {
                return;
            }
            ;
            $(".provinceCityAll", self.document).find(".tabs").find("a").removeClass("current");
            $(this).addClass("current");
            var tb = $(this).attr("tb");
            $(".provinceCityAll", self.document).find(".con").children().hide();
            $(".provinceCityAll", self.document).find(".con").find("." + tb).show();
        });
    },
    //绑定事件
    bindEvent: function () {
        var self = this;
    },
    //装载省
    loadProvinces: function () {
        //var url = $("input.proCitySelAll").eq(0).attr("data-url");
        var self = this;
        $.ajax({
            type: "get",
            url: self.rootUrl + '/sys/area/service/arealist?type=2',
            //async: false,
            dataType: "json",
            success: function (data) {
                self.provinces = data;
                self.showProvince();
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                layerError("读取省份列表失败", "系统提示");
                //top.$.jBox.error(textStatus);
            }
        });
    },
    //读取省
    showProvince: function () {
        var self = this;
        $(".provinceAll .list ul li", self.document).remove();
        for (var i = 0; i < self.provinces.length; i++) {
            var p_id = self.provinces[i].id;
            var p_name = self.provinces[i].name;
            if (self.provinces[i].name == '内蒙古自治区') {
                p_name = '内蒙古';
            } else if (self.provinces[i].name == '黑龙江省') {
                p_name = '黑龙江';
            } else {
                p_name = self.provinces[i].name.substr(0, 2);
            }
            var li = $('<li><a style="background: none repeat scroll 0% 0% transparent; border: 0px none;" href="javascript:onclick=area.showCities(' + i + ');" id="' + p_id + '">' + p_name + '</a></li>');
            $(".provinceAll .list ul", self.document).append(li);
        }
    },
    //显示程式
    showCities: function (i) {
        //i:index
        var self = this;
        self.province = self.provinces[i];
        if ($("input.current2", self.document).attr("data-checkselect") == "true") {
            $("input.current2", self.document).prev().val("");
        } else {
            $("input.current2", self.document).prev().val(self.province.id);
        }
        $("input.current2", self.document).val(self.province.name);
        self.city = {id: null, name: ""};

        $(".provinceCityAll", self.document).find(".tabs").find("a").removeClass("current");
        $(".provinceCityAll .tabs", self.document).find("#cityAll").addClass("current");
        $(".con .provinceAll .list a", self.document).removeClass("current");
        $(".con .provinceAll .list a[id='" + self.province.id + "']", self.document).addClass("current");
        $(".provinceCityAll", self.document).find(".con").children().hide();
        $(".provinceCityAll", self.document).find(".con").find(".cityAll").show();
        $(".cityAll .list ul li", self.document).empty();
        $(".cityAll .list ul li", self.document).remove();
        var clist = null;
        if (self.cities.hasOwnProperty('' + self.province.id + '')) {
            clist = self.cities['' + self.province.id + ''];
        }
        if (clist == null || clist == undefined || clist.length == 0) {
            self.loadCities(self.province.id);
            clist = self.cities['' + self.province.id + ''];
            if (clist == null || clist == undefined || clist.length == 0) {
                // top.$.jBox.error("读取城市列表错误");
                layerError("读取城市列表失败", "系统提示");
                return false;
            }
        }
        //show
        /*for (var i = 0; i < clist.length; i++) {
            var c_id = clist[i].id;
            var cityName = clist[i].name.substr(0, 4);
            var li = $('<li><a href="javascript:onclick=area.showCounties(' + i + ')" id="' + c_id + '">' + cityName + '</a></li>');
            $(".cityAll .list ul", self.document).append(li);
        }*/
        for (var i = 0; i < clist.length; i++) {
            var c_id = clist[i].id;
            var cityName = clist[i].name.substr(0, 4);
            var li = $('<li><a href="javascript:onclick=area.addrInputAll(' + i+')" id="' + c_id + '">' + cityName + '</a></li>');
            $(".cityAll .list ul",self.document).append(li);
        }
    },
    //读取城市
    loadCities: function (provinceId) {
        var self = this;
        // var url = $("input.proCitySelAll").eq(0).attr("data-url");
        $(".cityAll .list ul", self.document).append("<div class='loading'><i></i><span>读取数据...</span></div>");
        $.ajax({
            type: "get",
            url: self.rootUrl + '/sys/area/service/arealist?type=3&id=' + provinceId,
            async: false,
            dataType: "json",
            success: function (data) {
                self.cities['' + provinceId + ''] = data;
                $(".cityAll .list ul", self.document).empty();
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                $(".cityAll .list ul", self.document).empty();
                //top.$.jBox.error(textStatus);
                layerError("装载城市失败", "系统提示");
                return false;
            }
        });
    },
    //显示区/县
    /*showCounties: function (i) {
        //i:index
        var self = this;
        self.city = self.cities['' + self.province.id + ''][i];
        $(".provinceCityAll", self.document).find(".tabs").find("a").removeClass("current");
        $(".provinceCityAll .tabs", self.document).find("#countyAll").addClass("current");
        $(".con .cityAll .list a", self.document).removeClass("current");
        $(".con .cityAll .list a[id='" + self.city.id + "']", self.document).addClass("current");
        $(".provinceCityAll", self.document).find(".con").children().hide();
        $(".provinceCityAll", self.document).find(".con").find(".countyAll").show();
        $(".countyAll .list ul li", self.document).empty();
        $(".countyAll .list ul li", self.document).remove();
        var tlist = null;
        if (self.counties.hasOwnProperty('' + self.city.id + '')) {
            tlist = self.counties['' + self.city.id + ''];
        }
        if (tlist == null || tlist == undefined || tlist.length == 0) {
            self.loadCouties(self.city.id);
            tlist = self.counties['' + self.city.id + ''];
            if (tlist == null || tlist == undefined || tlist.length == 0) {
                //top.$.jBox.error("读取区县列表错误");
                layerError("读取区县列表错误", "系统提示");
                return false;
            }
        }

        //show
        $("input.current2", self.document).removeClass("iGrays");
        $("input.current2", self.document).val(self.province.name + " " + self.city.name);
        $("input.current2", this.document).prev().val(self.city.id);
        for (var i = 0; i < tlist.length; i++) {
            var c_id = tlist[i].id;
            var countyName = tlist[i].name.substr(0, 4);
            var li = $('<li><a href="javascript:onclick=area.addrInputAll(' + i + ')" id="' + c_id + '">' + countyName + '</a></li>');
            $(".countyAll .list ul", self.document).append(li);
        }
    },*/
    //读取区县数据
    /*loadCouties: function (cityId) {
        var self = this;
        // var url = $("input.proCitySelAll").eq(0).attr("data-url");
        $(".countyAll .list ul").append("<div class='loading'><i></i><span>读取数据...</span></div>");
        $.ajax({
            type: "get",
            url: self.rootUrl + '/sys/area/service/arealist?type=4&id=' + cityId,
            async: false,
            dataType: "json",
            success: function (data) {
                self.counties['' + cityId + ''] = data;
                $(".countyAll .list ul").empty();
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                $(".countyAll .list ul").empty();
                // top.$.jBox.error(textStatus);
                layerError("装载区县失败", "系统提示");
                return false;
            }
        });

    },*/
    /*addrInputAll: function (cuntryId) {
        var country = this.counties['' + this.city.id + ''][cuntryId];
        $(".con .hotCityAll .list a input", this.document).removeClass("current");
        $(".con .hotCityAll .list a input[id='" + this.city.id + "']", this.document).addClass("current");
        $(".con .countyAll .list a", this.document).removeClass("current");
        $(".con .countyAll .list a[id='" + country.id + "']", this.document).addClass("current");

        var nameValue = $("input.current2", this.document);
        nameValue.removeClass("iGrays");
        $(".provinceCityAll", this.document).hide();
        var rtn = this.province.name + " " + this.city.name + " " + country.name;
        $("input.current2", this.document).val(rtn);
        $(".backifname", this.document).hide();
        $("input.current2", this.document).prev().val(country.id);
        if (this.callback && this.callback != "" && this.callback != null) {
            this.callback(country.id, rtn);
        }
    },*/
    addrInputAll:function(cityId){
        var country = this.cities[''+this.province.id+''][cityId];
        $(".con .hotCityAll .list a input",this.document).removeClass("current");
        $(".con .hotCityAll .list a input[id='" + this.city.id + "']",this.document).addClass("current");
        $(".con .countyAll .list a",this.document).removeClass("current");
        $(".con .countyAll .list a[id='" + country.id + "']",this.document).addClass("current");

        var nameValue = $("input.current2",this.document);
        nameValue.removeClass("iGrays");
        $(".provinceCityAll",this.document).hide();
        var rtn = this.province.name + " " + country.name;
        $("input.current2",this.document).val(rtn);
        $(".backifname",this.document).hide();
        $("input.current2",this.document).prev().val(country.id);
        if( this.callback && this.callback != "" && this.callback != null){
            this.callback(country.id,rtn);
        }
    },
    initComponent: function (document) {
        this.document = document;
        if ($(".provinceCityAll").length == 0) {
            var areahtml = '<div class="tabs clearfix">' +
                '<ul class=""><li><a href="javascript:" tb="provinceAll">省份</a></li><li><a href="javascript:" tb="cityAll" id="cityAll">城市</a></li></ul>' +
                '</div>' +
                '<div class="con"><div class="provinceAll invis"><div class="list"><ul></ul></div></div><div class="cityAll invis"><div class="list"><ul></ul></div></div><div class="countyAll invis"><div class="list"><ul></ul></div></div></div>';
            var areadiv = $(this.document.createElement('div')).html(areahtml).addClass('provinceCityAll');
            // areadiv.appendTo('body');
            areadiv.appendTo(this.document.body);
        }
        if (this.provinces == null || this.provinces.length == 0) {
            this.loadProvinces();
        }
        }
};

window.CountryAreaUtil = top.window.CountryAreaUtil || parent.window.CountryAreaUtil || CountryAreaUtil;
window.CountryAreaUtil.init();