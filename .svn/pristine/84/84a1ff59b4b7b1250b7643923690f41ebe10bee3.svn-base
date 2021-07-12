<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@ include file="/WEB-INF/views/include/treeview.jsp" %>
    <title>负责区域设置</title>
    <meta name="decorator" content="default"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <style type="text/css">
        #editBtn {
            position: fixed;
            left: 0px;
            bottom: 0;
            width: 100%;
            height: 60px;
            background: #fff;
            z-index: 10;
            border-top: 1px solid #e5e5e5;
        }

        #province div {
            width: 237px;
            height: 40px;
            float: left;
            cursor: pointer;
        }

        #city div div {
            width: 237px;
            height: 40px;
            float: left;
            cursor: pointer;
        }

        .current {
            width: 237px;
            height: 40px;
            color: black;
            background-color: #F2FBFF;
        }

        .area {
            width: 25%;
            height: 40px;
            float: left;
            /*text-overflow: ellipsis;*/
            overflow: hidden;
            white-space: nowrap;
        }

        .selected{
            margin-top: -25px;
            margin-left: 180px;
            padding: 2px 4px;
            background-color: #E9F3FF;
            width: 29px;
            color: #1D89FF;
        }
        .unselected{
            margin-top: -25px;
            margin-left: 180px;
            padding: 2px 4px;
            background-color: #E9F3FF;
            width: 29px;
            color: #1D89FF;
            display: none;
        }
    </style>
    <script type="text/javascript">
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
        var this_index = top.layer.index;

        function cancel() {
            top.layer.close(this_index);// 关闭本身
        }
        var clickTag = 0;
        $(document).ready(function () {
            $("#inputForm").validate({
                submitHandler: function (form) {
                    var loadingIndex = layerLoading('正在提交，请稍候...');

                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }

                    var userId = $("#userId").val();
                    $btnSubmit.prop("disabled", true);

                    var entity = {};

                    entity["subFlag"] = $("#subFlag").val();
                    entity["userId"] = userId;
                    $("input[type='checkbox'][name='area']:checkbox:checked").each(function (i, element) {
                        var areaId = $(this).val();
                        entity["regionList[" + i + "].provinceId"] = $(this).data("province");
                        entity["regionList[" + i + "].cityId"] = $(this).data("city");
                        entity["regionList[" + i + "].areaId"] = areaId;
                    });

                    $.ajax({
                        url: "${ctx}/sys/userKeFu/saveUserRegion",
                        type: "POST",
                        data: entity,
                        dataType: "json",
                        success: function (data) {
                            //提交后的回调函数
                            if (loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                            if (ajaxLogout(data)) {
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                return false;
                            }

                            if (data.success) {
                                if(userId != ''){
                                    layerMsg("保存成功");
                                }

                                if (parentIndex && parentIndex != undefined && parentIndex != '') {
                                    var layero = $("#layui-layer" + parentIndex, top.document);
                                    var iframeWin = top[layero.find('iframe')[0]['name']];
                                    iframeWin.refreshUserRegion(data.data);
                                }
                                top.layer.close(this_index);//关闭本身
                            } else {
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                layerError(data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data) {
                            if (loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            ajaxLogout(data, null, "数据保存错误，请重试!");
                        },
                        timeout: 30000               //限制请求的时间，当请求大于30秒后，跳出请求
                    });

                },
                errorContainer: "#messageBox",
                errorPlacement: function (error, element) {
                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox") || element.is(":radio") || element.parent().is(".input-append")) {
                        error.appendTo(element.parent().parent());
                    } else {
                        error.insertAfter(element);
                    }
                }
            });


            $("[data-selectAll=selectAll]").change(function () {
                var $check = $(this);
                var cityId = $(this).data("city");
                var provinceId = $(this).data("province");
                $("input[type=checkbox][data-province=" + provinceId + "][data-city=" + cityId + "]:enabled").each(function () {
                    if ($(this).val() != "on") {
                        if ($check.attr("checked") == "checked") {
                            $(this).attr("checked", true);
                        } else {
                            $(this).attr("checked", false);
                        }
                    }

                });
            });

            $("[name=area]").change(function () {
                var $check = $(this);
                var cityId = $(this).data("city");
                var provinceId = $(this).data("province");

                if (provinceId != 1) {
                    $("input[type=checkbox][data-province= " + 1 + "][data-city= " + 1 + "]").attr("checked", false); //取消全国的勾选
                    $("#selectedProvince_" + 1 +"").attr("class", "unselected");
                    $("#selectedCity_" + 1 +"").attr("class", "unselected");
                } else {
                    $("input[type=checkbox][data-province!= " + 1 + "][data-city!= " + 1 + "]").attr("checked", false); //取消全国以外的勾选
                    $("[id!=selectedProvince_" + provinceId +"][name=selectedProvince]").attr("class", "unselected");
                    $("[id!=selectedCity_" + provinceId +"][name=selectedCity]").attr("class", "unselected");
                }
                if (provinceId != cityId) {
                    $("input[type=checkbox][data-province= " + provinceId + "][data-city= " + provinceId + "]").attr("checked", false); //取消全省的勾选
                } else {
                    $("input[type=checkbox][data-province= " + provinceId + "][data-city!= " + provinceId + "]").attr("checked", false); //取消区的勾选
                    $("#city_" + provinceId+" p.selected" ).attr("class", "unselected");
                }


                var  areaSum = $("[name=areaSel][id=area_" + cityId +"]").children("div").length;

                var ids = $("input[type='checkbox'][data-province= " + provinceId + "][data-city= " + cityId + "]:checkbox:checked").length;

                var areas = $("input[type='checkbox'][data-province= " + provinceId + "][data-city= " + cityId + "][value!=0]:checkbox:checked").length;

                // if(areas == (areaSum-1)){
                //     $("input[name=area][data-selectAll=selectAll][data-city=" + cityId+"][data-province=" + provinceId+"]").attr("checked", true);
                // }else {
                //     $("input[name=area][data-selectAll=selectAll][data-city=" + cityId+"][data-province=" + provinceId+"]").attr("checked", false);
                // }
                if(areas != (areaSum-1)){
                    $("input[name=area][data-selectAll=selectAll][data-city=" + cityId+"][data-province=" + provinceId+"]").attr("checked", false);
                }
                if (ids > 0) {
                    $("#selectedCity_" +cityId +"").attr("class", "selected");
                    if(provinceId != cityId){
                        $("#selectedCity_" +provinceId +"").attr("class", "unselected");
                    }
                } else {
                    $("#selectedCity_" +cityId +"").attr("class", "unselected");
                }


                var citySum = $("#city_" + provinceId+" p.selected" ).length;
                if(citySum >0){
                    $("#selectedProvince_" + provinceId +"").attr("class", "selected");
                }else {
                    $("#selectedProvince_" + provinceId +"").attr("class", "unselected");
                }
            });
        });

        function editArea() {
            var userRegionAreaList = $("#userRegionAreaList").val();
            var userRegionAreas = ${fns:toJson(userRegionAreaList)};
            var province_sel = [];
            var city_sel = [];
            var strCity = [];
            var strArea = [];
            var area_sel = [];
            area_sel.push('<div name="areaSel" style="margin-left: 8px;margin-top: 12px" id="area_0"><<请选择市</div>');
            city_sel.push('<div name="citySel" style="margin-left: 8px;margin-top: 12px" id="city_0"><<请选择省</div>');

            for (var i = 0; i < userRegionAreas.length; i++) {
                var province = userRegionAreas[i];
                province_sel.push('<div name="province" id="' + province.provinceId + '"><p style="margin-top: 12px;color: #515A6E;margin-left: 8px;margin-bottom: 0px">' + province.areaName + '</p><p id="selectedProvince_'+ province.provinceId+'" name="selectedProvince" class="unselected">已选</p></div>');
                strCity = [];
                for (var j = 0; j < province.permissionDtoList.length; j++) {
                    var city = province.permissionDtoList[j];
                    strCity.push('<div id="' + city.cityId + '"><p style="margin-top: 12px;color: #515A6E;margin-left: 8px;margin-bottom: 0px">' + city.areaName + '</p><p id="selectedCity_'+ city.cityId +'" name="selectedCity" class="unselected">已选</p></div>');
                    strArea = [];
                    var subFlag = $("#subFlag").val();
                    if(subFlag == 3 || subFlag == 4){
                        if(city.cityId == 212 || city.cityId == 49 || city.cityId == 303 || city.cityId == 314){

                        }else {
                            strArea.push('<div class="area" style="width: 100%"><label style="margin-top: 12px"><input name="area" data-selectAll="selectAll" type="checkbox" name="area" data-province="' + province.provinceId + '" data-city="' + city.cityId + '" value="0" style="zoom: 1.4;margin-left: 7px">全选</label></input></div>');
                        }
                    }else {
                        strArea.push('<div class="area" style="width: 100%"><label style="margin-top: 12px"><input name="area" data-selectAll="selectAll" type="checkbox" name="area" data-province="' + province.provinceId + '" data-city="' + city.cityId + '" value="0" style="zoom: 1.4;margin-left: 7px">全选</label></input></div>');
                    }

                    for (var k = 0; k < city.permissionDtoList.length; k++) {
                        var area = province.permissionDtoList[j].permissionDtoList[k];
                        if (area.provinceId == area.cityId && area.cityId == area.areaId) {
                            strArea = [];
                        }
                        strArea.push('<div class="area"><label style="margin-top: 12px"><input type="checkbox" name="area" style="zoom: 1.4;margin-left: 7px" data-province="' + area.provinceId + '" data-city="' + area.cityId + '" value="' + area.areaId + '">' + area.areaName + '</label></input></div>');
                    }
                    area_sel.push('<div name="areaSel" style="display: none" id="area_' + city.cityId + '">' + strArea.join(' ') + '</div>')
                }

                city_sel.push('<div name="citySel" style="display: none" id="city_' + province.provinceId + '">' + strCity.join(' ') + '</div>')
            }
            $("#province").append(province_sel);
            $("#city").append(city_sel);
            $("#area").append(area_sel);

        }


        function editCity(id) {
            $("[name=citySel]").hide();
            document.getElementById("city_" + id + "").style.display = "";//显示
        }

        function selectArea(id) {
            $("[name=areaSel]").hide();
            document.getElementById("area_" + id + "").style.display = "";//显示
        }

        function load(){
            var loadingInd = top.layer.msg('正在加载，请稍等...', {
                icon: 16,
                time: 0,//不定时关闭
                shade: 0.3
            });
            var data = {
                "userId" : $("#userId").val()
            };
            $.ajax({
                url: "${ctx}/sys/userKeFu/getUserRegion",
                type: 'post',
                data : data,
                success : function (result) {
                    top.layer.close(loadingInd);
                    if (!result.success) {
                        layerError("数据加载失败:" + result.message, "错误提示");
                    } else {
                        var subRegionCheck = [];
                        if (result.data && result.data.length > 0) {
                            subRegionCheck = result.data;
                            for (var i in result.data) {
                                var entity = subRegionCheck[i];
                                if(entity.areaType == 1){
                                    $("#selectedCity_" +1 +"").attr("class", "selected");
                                    $("#selectedProvince_" +1 +"").attr("class", "selected");
                                    $("input[type=checkbox][data-province= " + 1 + "][data-city= " + 1 + "]").attr("checked", true); //勾选全国
                                }else if(entity.areaType == 2){
                                    $("#selectedCity_" +entity.provinceId +"").attr("class", "selected");
                                    $("#selectedProvince_" +entity.provinceId +"").attr("class", "selected");
                                    $("input[type=checkbox][data-province= " + entity.provinceId + "][data-city= " + entity.provinceId + "][value= " + entity.provinceId + "]").attr("checked", true); //勾选省
                                }else if(entity.areaType == 3){
                                    $("#selectedCity_" +entity.cityId +"").attr("class", "selected");
                                    $("#selectedProvince_" +entity.provinceId +"").attr("class", "selected");
                                    $("input[type=checkbox][data-province= " + entity.provinceId + "][data-city= " + entity.cityId + "]").attr("checked", true); //勾选市
                                }else if(entity.areaType == 4){
                                    $("#selectedCity_" +entity.cityId +"").attr("class", "selected");
                                    $("#selectedProvince_" +entity.provinceId +"").attr("class", "selected");
                                    $("input[type=checkbox][data-province= " + entity.provinceId + "][data-city= " + entity.cityId + "][value=" + entity.areaId + "]").attr("checked", true); //勾选区
                                }
                            }
                        }

                    }
                }
            });

        }
    </script>
</head>
<body>
<form:form id="inputForm" action="${ctx}/sys/userKeFu/userRegion" method="post" class="form-horizontal"
           cssStyle="margin-left: 0px;width: 100%">
    <sys:message content="${message}"/>
    <input type="hidden" id="userId" value="${userId}">
    <input type="hidden" id="subFlag" value="${subFlag}">
    <input type="hidden" value="${userRegionAreaList}" id="userRegionAreaList">
    <div style="width: 906px;height: 490px;margin: 24px 0px 24px 24px;border: 1px #DCDEE2 solid;">
        <div style="float: left;width: 239px;height: 40px;background-color:#F8F8F9;border-right-color: #E8EAEC;border-right-style: solid;border-right-width: 1px;">
            <p style="margin-top: 12px;color: #17233D;margin-left: 8px">省</p>
        </div>
        <div style="float: left;width: 239px;height: 40px;background-color:#F8F8F9;border-right-color: #E8EAEC;border-right-style: solid;border-right-width: 1px;">
            <p style="margin-top: 12px;color: #17233D;margin-left: 8px">市</p>
        </div>
        <div style="float: left;width: 426px;height: 40px;background-color:#F8F8F9;">
            <p style="margin-top: 12px;color: #17233D;margin-left: 8px">区</p>
        </div>

        <div id="province"
             style="float:left;width: 239px;height: 450px;border-right-color: #E8EAEC;border-right-style: solid;border-right-width: 1px;overflow-y: auto;overflow-x:hidden ">

        </div>

        <div id="city"
             style="float:left;width: 239px;height: 450px;border-right-color: #E8EAEC;border-right-style: solid;border-right-width: 1px;overflow-y: auto;overflow-x:hidden">

        </div>

        <div id="area" style="float:left;width: 426px;height: 450px;overflow-y: auto;overflow-x:hidden">

        </div>

    </div>

    <div id="editBtn" class="line-row" style="width: 100%;">

        <input id="btnSubmit" class="btn btn-primary" type="submit" value="保存"
               style="width: 96px;height: 40px;margin-top: 10px;margin-left: 723px"/>
        <input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()"
               style="width: 96px;height: 40px;margin-top: 10px;margin-left: 13px;"/>
    </div>


</form:form>

<script type="text/javascript">

    $(document).ready(function () {

    });

    editArea();
    //获取变量==>存变量==>给变量绑定属性
    $(function () {
        var $provinceDiv = $('#province div');//获取每一个标题和内容用变量存起来

        $provinceDiv.click(function () {
            var $this = $(this);//用变量把点击的每一个当前的div存起来
            $provinceDiv.removeClass();//因为默认是第一个显示  所以先移除
            $this.addClass('current');//再添加当前的
            editCity($this.prop("id"));
            selectArea(0);
        })
    });



    $(function () {
        var $cityDiv = $('#city div div');//获取每一个标题和内容用变量存起来
        $cityDiv.click(function () {
            var $this = $(this);//用变量把点击的每一个当前的div存起来
            $cityDiv.removeClass();//因为默认是第一个显示  所以先移除
            $this.addClass('current');//再添加当前的
            selectArea($this.prop("id"));
        })
    });


    load();//加载已选择区域
</script>
</body>
</html>
