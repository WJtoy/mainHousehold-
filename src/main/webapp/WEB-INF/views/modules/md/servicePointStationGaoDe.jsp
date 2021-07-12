<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <%--
    <link rel="stylesheet" href="https://a.amap.com/jsapi_demos/static/demo-center/css/demo-center.css" />
    --%>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>网点服务点管理</title>
    <meta name="decorator" content="default"/>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no, width=device-width">
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script type="text/javascript" src="https://webapi.amap.com/maps?v=1.4.14&key=8101d6c8784be11a3d7d49b5fb6283f7&plugin=AMap.CircleEditor"></script>
    <%--<link rel="stylesheet" href="https://cache.amap.com/lbs/static/AMap.CloudDataSearchRender1120.css" />--%>
    <link href="${ctxStatic}/amap/AMap.CloudDataSearchRender1120.css" type="text/css" rel="stylesheet" />
    <%--<script type="text/javascript" src="https://cache.amap.com/lbs/static/addToolbar.js"></script>--%>
    <style>
        .amap-marker-label {
            border: 0;
            background-color: transparent;
        }

        .info {
            position: relative;
            top: 0;
            right: 0;
            min-width: 0;
        }

        /*infowindow begin*/
        .content-window-card {
            position: relative;
            box-shadow: none;
            bottom: 0;
            left: 0;
            width: auto;
            padding: 0;
        }

        .content-window-card p {
            height: 2rem;
        }

        .custom-info {
            border: solid 1px silver;
        }

        div.info-top {
            position: relative;
            background: none repeat scroll 0 0 #F9F9F9;
            border-bottom: 1px solid #CCC;
            border-radius: 5px 5px 0 0;
        }

        div.info-top div {
            display: inline-block;
            color: #333333;
            font-size: 14px;
            font-weight: bold;
            line-height: 31px;
            padding: 0 10px;
        }

        div.info-top img {
            position: absolute;
            top: 10px;
            right: 10px;
            transition-duration: 0.25s;
        }

        div.info-top img:hover {
            box-shadow: 0px 0px 5px #000;
        }

        div.info-middle {
            font-size: 12px;
            padding: 10px 6px;
            line-height: 20px;
        }

        div.info-bottom {
            height: 0px;
            width: 100%;
            clear: both;
            text-align: center;
        }

        div.info-bottom img {
            position: relative;
            z-index: 104;
        }

        span {
            margin-left: 5px;
            font-size: 11px;
        }

        .info-middle img {
            float: left;
            margin-right: 6px;
        }
    </style>
    <style>
        #panel {
            position: absolute;
            background-color: white;
            max-height: 90%;
            overflow-y: auto;
            top: 100px;
            right: 10px;
            width: 200px;
        }
        #query {
            position: absolute;
            max-height: 90%;
            overflow-y: auto;
            top: 100px;
            left: 0px;
            width: 340px;
        }
    </style>
    <style type="text/css">
        .custom-content-marker {
            position: relative;
            width: 25px;
            height: 34px;
        }

        .custom-content-marker img {
            width: 100%;
            height: 100%;
        }

        .custom-content-marker .close-btn {
            position: absolute;
            top: -6px;
            right: -8px;
            width: 15px;
            height: 15px;
            font-size: 12px;
            background: #ccc;
            border-radius: 50%;
            color: #fff;
            text-align: center;
            line-height: 15px;
            box-shadow: -1px 1px 1px rgba(10, 10, 10, .2);
        }

        .custom-content-marker .close-btn:hover{
            background: #666;
        }
    </style>
    <script type="text/javascript">
        $(document).ready(function() {
            $(".poibox").on("click",function(e){
                // console.log("你点击我啦。");
                /*
                console.log(e);
                console.log(e.currentTarget.id);
                console.log($(e.currentTarget).find("input[name='lng']")[0].defaultValue);
                */
                var targetId = e.currentTarget.id;
                if (targetId) {
                    targetId = parseInt(targetId);
                }
                var overlays = map.getAllOverlays("marker");
                for(var i = 0; i < overlays.length; i++){
                    // 获取存在每个 extData 中的 id
                    var id = overlays[i].getExtData().spid;
                    if(id === targetId){
                        overlays[i].show();
                    } else {
                        overlays[i].hide();
                    }
                }
                var circles = map.getAllOverlays("circle");
                for(i = 0; i < circles.length; i++){
                    // 获取存在每个 extData 中的 id
                    var cid = circles[i].getExtData().spid;
                    if(cid === targetId){
                        circles[i].show();
                    } else {
                        circles[i].hide();
                    }
                }
            })

            $("#servicePointId").on("change",function(e){
                if (e.target.value == ""){
                    $("[id^='servicePointNo']").val("");
                }
            })
        });
        function pointSelect_callback(data){
            $("[id^='servicePointNo']").val(data.servicePointNo);
        }

    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li><a href="${ctx}/md/servicepoint/selectForStation">服务区域列表</a></li>
    <%--
    <shiro:hasPermission name="md:servicepointstation:edit">
        <li><a href="${ctx}/md/servicepointstation/form?servicePoint.id=${servicePointStation.servicePoint.id}">网点服务点添加</a></li>
    </shiro:hasPermission>
    --%>
    <li><a href="${ctx}/md/servicepointstation/areaStationList">区域服务点列表</a></li>
    <li class="active"><a href="${ctx}/md/servicepointstation/amap">地图</a></li>
</ul>
<form:form id="searchForm" modelAttribute="servicePoint" action="${ctx}/md/servicepointstation/amap" method="POST" class="breadcrumb form-search">
    <form:hidden path="firstSearch" />
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
    <ul class="ul-form">
        <li>
            <label>网点编号：</label>
            <form:input path="servicePointNo" htmlEscape="false" class="input-large" maxlength="20" style="width: 120px"/>
        </li>
        <li>
            <label>网点名称：</label>
            <%--<md:pointselectlayer id="servicePoint" name="id" value="${servicePoint.id}" labelName="name" labelValue="${servicePoint.name}"--%>
                                 <%--width="1200" height="780" callbackmethod="pointSelect_callback" title="选择网点" areaId="" cssClass="required" allowClear="true"/>--%>
            <form:input path="name" htmlEscape="false" maxlength="30" class="input-small"/>
        </li>
        <li>
            <label>网点电话：</label>
            <form:input path="contactInfo1" htmlEscape="false" class="input-small" maxlength="20" />
        </li>
        <li>
            <label style="margin-left: 10px">区域：</label>
            <%--
            <sys:treeselectarea id="area" name="area.id" value="${servicePoint.area.id}" labelName="area.name" labelValue="${servicePoint.area.fullName}" title="区域"
                            url="/sys/area/treeData" nodesLevel="2" nameLevel="3" cssStyle="width:140px;" cssClass="required" allowClear="true"/>
            --%>
            <%--<sys:treeselect id="area" name="area.id" value="${servicePoint.area.id}" labelName="area.name" labelValue="${servicePoint.area.fullName}" title="区域"--%>
                                <%--url="/sys/area/treeData" nodesLevel="2" nameLevel="3" cssStyle="width:140px;" cssClass="required" allowClear="true"/>--%>

            <sys:newtableareaselect id="area" name="area.id" value="${servicePoint.area.id}"
                                    labelValue="${servicePoint.area.fullName}" labelName="area.name"
                                    title="区域" mustSelectCounty="false" cssClass="required"> </sys:newtableareaselect>
        </li>

        <li>
            <label class="control-label">等级:</label>
            <form:select path="level.value" class="input-small">
                <form:option value="0" label="所有" />
                <form:options items="${fns:getDictListFromMS('ServicePointLevel')}" itemLabel="label" itemValue="value" htmlEscape="false" />
            </form:select>
        </li>
        <li>
            <label style="margin-left: 40px">自动派单：</label>
            <form:select path="autoPlanFlag" class="input-small">
                <form:option value="-1" label="所有" />
                <form:options items="${fns:getDictListFromMS('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false" />
            </form:select>
        </li>
        <li class="btns"><input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" /></li>
        <li class="clearfix"></li>
    </ul>
</form:form>
<div id="container" style="height: 87%;width:100%;position: absolute;overflow: hidden;"></div>
<div id="query">
    <input type="text" id="queryaddress" class="input-large" style="margin-top: 0px;" placeholder="请输入地址"/>
    <input type="button" value="查询" class="btn btn-success" style="margin-top: -9px;" onclick="findAddress()"/>
    <input type="button" value="清空" class="btn btn-default" style="margin-top: -9px;" onclick="clearAddress()"/>
</div>
<div id="panel">
    <div class="amap_lib_cloudDataSearch">
        <div class="amap_lib_cloudDataSearch_list">
            <ul>
                <c:forEach items="${page.list}" var="servicePoint">
                    <c:set var="rowNumber" value="${rowNumber+1}"/>
                    <li class="poibox" id="${servicePoint.id}">
                        <c:choose>
                            <c:when test="${servicePoint.autoPlanFlag eq 1}">
                                <div title="已设置自动派单" class="amap_lib_cloudDataSearch_poi poibox-icon" style="background: url(https://webapi.amap.com/theme/v1.3/markers/n/mark_r.png) no-repeat;">${rowNumber+(page.pageNo-1)*page.pageSize}</div>
                            </c:when>
                            <c:otherwise>
                                <div title="不能自动派单" class="amap_lib_cloudDataSearch_poi poibox-icon">${rowNumber+(page.pageNo-1)*page.pageSize}</div>
                            </c:otherwise>
                        </c:choose>
                        <%--
                        <div class="amap_lib_cloudDataSearch_poi poibox-icon">${rowNumber+(page.pageNo-1)*page.pageSize}</div>
                        --%>
                        <h3 class="poi-title" style="height:19px;line-height:20px;">
                            <span class="poi-name">${servicePoint.servicePointNo}</span>
                        </h3>
                        <div class="poi-info">
                            <p class="poi-addr">网点名称:${servicePoint.name}</p>
                        </div>
                    </li>
                </c:forEach>
            </ul>
        </div>
    </div>
    <div class="amap_lib_cloudDataSearch_page">
        ${page.mapString}
    </div>
</div>
<script type="text/javascript">
    var map = new AMap.Map('container',{
        resizeEnable: true,
        zoom: 11,
        center:[${centerLng},${centerLat}]  // 定位到佛山市
    });
    addToolbar();

    var circleEditor;
    var queryAddressMarker;

    addMarker();
    map.on('click',mapClick);

    var infoWindow = new AMap.InfoWindow({
        isCustom: true,  //使用自定义窗体
        //content: createInfoWindow(title, content.join("<br/>")),
        offset: new AMap.Pixel(16, -45)
    });

    function addMarker() {
        var stationList = ${servicePointStationList};
        if (!stationList){
            return false;
        }
        for (var i =0;i< stationList.length;i++) {
            var strInfo = "服务半径:"+ stationList[i].radius +"米";
            strInfo ="";
            //console.log(stationList[i].servicePoint);
            if (stationList[i].servicePoint.autoPlanFlag == 1) {
                strInfo += "<div class='amap_lib_cloudDataSearch_poi' style='background: url(https://webapi.amap.com/theme/v1.3/markers/n/mark_r.png) no-repeat;'>"+ (i+1) +"</div></div>";
            } else  {
                strInfo += "<div class='amap_lib_cloudDataSearch_poi'>"+ (i+1) +"</div></div>";
            }

            var marker = new AMap.Marker({
                position:[stationList[i].longtitude,stationList[i].latitude],
                map:map,
                content: strInfo,
                // icon: "https://webapi.amap.com/theme/v1.3/markers/n/mark_r.png",   //红色  //要移除掉
                offset: new AMap.Pixel(-13, -30),
                draggable: true,
                extData:{
                    id: stationList[i].id,
                    spid: stationList[i].servicePoint.id,
                    initposition:[stationList[i].longtitude,stationList[i].latitude]
                }
            });

            var strTitle = generateTitle(stationList[i].servicePoint.name,stationList[i].name,stationList[i].area.name)
            marker.setTitle(strTitle);


            var strInfo = "服务半径:"+ stationList[i].radius +"米";
            // 设置label标签
            // label默认蓝框白底左上角显示，样式className为：amap-marker-label
            marker.setLabel({
                content: "<div class='info'>"+strInfo+"</div>", //设置文本标注内容
                direction: 'top' //设置文本标注方位
            });

            marker.content = generateContent(stationList[i].radius,stationList[i].id);
            marker.on('click', markerClick);
            //marker.on('dragging',markerDragging);
            marker.on('dragend', markerDragend);

            var radius =  (stationList[i].radius == 0 || (stationList[i].radius == undefined))? 10000: stationList[i].radius ;

            var circle = new AMap.Circle({
                center: [stationList[i].longtitude,stationList[i].latitude],
                radius: radius, //半径
                borderWeight: 3,
                strokeColor: "#FF33FF",
                strokeOpacity: 1,
                strokeWeight: 1,
                strokeOpacity: 0.2,
                fillOpacity: 0.4,
                strokeStyle: 'solid',
                strokeDasharray: [0,0,0],
                // 线样式还支持 'dashed'
                fillColor: '#1791fc',
                zIndex: 50,
                extData:{
                    id: stationList[i].id,
                    spid: stationList[i].servicePoint.id
                }
            })
            circle.setMap(map);
            circle.on('click',circleClick);
        }
    }

    function mapClick(e) {
        // console.log("mapClick");
        if (circleEditor) {
            circleEditor.close();
            circleEditor = null;
        }
    }

    function circleClick(e) {
        if (circleEditor) {
            circleEditor.close();
            circleEditor = null;
        } else {
            circleEditor = new AMap.CircleEditor(map, e.target);
            circleEditor.open();
        }
    }

    //构建自定义信息窗体
    function createInfoWindow(title, content) {
        var info = document.createElement("div");
        info.className = "custom-info input-card content-window-card";

        //可以通过下面的方式修改自定义窗体的宽高
        //info.style.width = "400px";
        // 定义顶部标题
        var top = document.createElement("div");
        var titleD = document.createElement("div");
        var closeX = document.createElement("img");
        top.className = "info-top";
        titleD.innerHTML = title;
        closeX.src = "https://webapi.amap.com/images/close2.gif";
        closeX.onclick = closeInfoWindow;

        top.appendChild(titleD);
        top.appendChild(closeX);
        info.appendChild(top);

        // 定义中部内容
        var middle = document.createElement("div");
        middle.className = "info-middle";
        middle.style.backgroundColor = 'white';
        middle.innerHTML = content;
        info.appendChild(middle);

        // 定义底部内容
        var bottom = document.createElement("div");
        bottom.className = "info-bottom";
        bottom.style.position = 'relative';
        bottom.style.top = '0px';
        bottom.style.margin = '0 auto';
        var sharp = document.createElement("img");
        sharp.src = "https://webapi.amap.com/images/sharp.png";
        bottom.appendChild(sharp);
        info.appendChild(bottom);
        return info;
    }

    //关闭信息窗体
    function closeInfoWindow() {
        map.clearInfoWindow();
    }

    // 打开信息窗体
    function markerClick(e){
        var strcontent = createInfoWindow("信息",e.target.content);
        infoWindow.setContent(strcontent);
        infoWindow.open(map, e.target.getPosition());
    }

    function modifyRadius(targetId){
        if (!targetId) {
            return false;
        }
        var targetMarker;
        var targetCircle;
        var valRadius = $("#radius").val();

        $.ajax({
            cache : false,
            type : "POST",
            url : "${ctx}/md/servicepointstation/modifyRadius",
            data : {id:targetId,radius:valRadius},
            success : function(data)
            {
                if (data.success==false)
                {
                    layerError(data.message,"错误提示");
                    return false;
                } else {
                    var overlays = map.getAllOverlays("marker");
                    for(var i = 0; i < overlays.length; i++){
                        // 获取存在每个 extData 中的 id
                        var id = overlays[i].getExtData().id;
                        if(id === targetId){
                            targetMarker = overlays[i];

                            var strInfo = "服务半径:"+ valRadius +"米";
                            // 设置label标签
                            targetMarker.setLabel({
                                content: "<div class='info'>"+strInfo+"</div>", //设置文本标注内容
                                direction: 'top' //设置文本标注方位
                            });

                            targetMarker.content = generateContent(valRadius,id);
                            closeInfoWindow();
                            break;
                        }
                    }
                    var circles = map.getAllOverlays("circle");
                    for(i = 0; i < circles.length; i++){
                        // 获取存在每个 extData 中的 id
                        var cid = circles[i].getExtData().id;
                        if(cid === targetId){
                            targetCircle = circles[i];
                            targetCircle.setRadius(parseInt(valRadius));
                            closeInfoWindow();
                            break;
                        }
                    }
                }
            },
            error : function(xhr, ajaxOptions, thrownError)
            {
                top.layer.close(loadingIndex);
                layerError(thrownError.toString(),"错误提示");
            }
        });//end ajax
    }

    function generateContent(radius,id) {
        // var  content = [];
        // content.push("服务点名称:&nbsp;" + stationList[0].name+"&nbsp;&nbsp;");
        // content.push("服务区域:&nbsp;"+stationList[i].address+"&nbsp;&nbsp;");
        // content.push("服务半径:&nbsp;"+stationList[i].radius+"米&nbsp;&nbsp;");
        // content.push("<a href='javascript:void(0)' onclick='circleClick();'>编辑半径</a>");

        // 生成点标记显示内容
        var strContent = "";
        strContent ='<label style="font-size:10px;">服务半径(米):</label>';
        strContent +='<input id="radius" name="radius" type="number" maxlength="12" style="width:95px;margin-left:4px;" value="'+radius+'"class="number" />';
        strContent +='<div style="text-align:center;"><button  class="btn btn-suucess btn-xs" style="font-size:10px;" onclick="modifyRadius('+id+')">修改半径</button></div>';

        return strContent;
    }

    function generateTitle(servicePointName,servicePointStationName,servicePointStationAddress) {
        //var strTitle = "网点名称:"+stationList[i].servicePoint.name+"\r\n服务点名称:"+stationList[i].name+"\r\n服务区域:" + stationList[i].address +"\r\n服务半径:"+ stationList[i].radius +"米";
        var strTitle = "";
        strTitle +="网点名称:"+servicePointName+"\r\n服务点名称:"+servicePointStationName+"\r\n服务区域:" + servicePointStationAddress;
        return strTitle;
    }

    function markerDragend(e){
        //console.log("markerDraggend");
        //console.log(e);
        //console.log(e.target.getPosition().lng);
        //console.log(e.target.getPosition().lat);

        top.$.jBox.confirm("确认要修改服务点坐标吗？","系统提示",function(v,h,f){
            // console.log(v);
            if(v=="ok"){
                // console.log(e);
                var id = e.target.getExtData().id;
                if (!id) {
                    // console.log("can't find id.");
                    return false;
                }
                var spid = e.target.getExtData().spid;
                var circles = map.getAllOverlays("circle");
                var longitude = e.target.getPosition().lng;
                var latitude = e.target.getPosition().lat;
                for(var i = 0; i < circles.length; i++){
                    // 获取存在每个 extData 中的 id
                    var cid = circles[i].getExtData().id;
                    if (cid == id) {
                        circles[i].setCenter(new AMap.LngLat(longitude, latitude,false));
                        break;
                    }
                }

                // update to db
                $.post("${ctx}/md/servicepointstation/modifyPosition",{id:id,longitude:longitude,latitude:latitude},function(data){
                    if (data.success==false){
                        layerError(data.message,"错误提示");
                        return false;
                    } else {
                        // console.log("更新新坐标成功!");
                        var initposition = e.target.getPosition();
                        e.target.setExtData({id:id,spid:spid,initposition:initposition});
                    }
                });
            } else {
                // console.log(e);
                var initposition = e.target.getExtData().initposition;
                if (initposition) {
                    e.target.setPosition(initposition);
                }
            }
        },{buttonsFocus:1});
        top.$('.jbox-body .jbox-icon').css('top','55px');
    }

    function addToolbar() {
        AMap.plugin(['AMap.ToolBar'],
            function () {
                toolopt = {
                    offset: new AMap.Pixel(10, 50),//相对于地图容器左上角的偏移量，正数代表向右下偏移。默认为AMap.Pixel(10,10)
                    /*
                    *控件停靠位置
                    *LT:左上角;
                    *RT:右上角;
                    *LB:左下角;
                    *RB:右下角;
                    *默认位置：LT
                    */
                    position: 'LT',
                    ruler: true,//标尺键盘是否可见，默认为true
                    noIpLocate: false,//定位失败后，是否开启IP定位，默认为false
                    locate: true,//是否显示定位按钮，默认为false
                    liteStyle: false,//是否使用精简模式，默认为false
                    direction: true,//方向键盘是否可见，默认为true
                    autoPosition: false,//是否自动定位，即地图初始化加载完成后，是否自动定位的用户所在地，在支持HTML5的浏览器中有效，默认为false
                    locationMarker: AMap.Marker({map: map}),
                    /**
                     *是否使用高德定位sdk用来辅助优化定位效果，默认：false.
                     *仅供在使用了高德定位sdk的APP中，嵌入webview页面时使用
                     *注：如果要使用辅助定位的功能，除了需要将useNative属性设置为true以外，
                     *还需要调用高德定位idk中，AMapLocationClient类的startAssistantLocation()方法开启辅助H5定位功能；
                     *不用时，可以调用stopAssistantLocation()方法停止辅助H5定位功能。具体用法可参考定位SDK的参考手册
                     */
                    useNative: false
                }
                var toolbar = new AMap.ToolBar(toolopt);
                //toolbar.hide();//隐藏toolbar
                map.addControl(toolbar);
                //启动监听
                // toolbar.on('location', function () {
                //     alert(toolbar.getLocation());
                // })
            });
    }

    function findAddress() {
        var address = $("#queryaddress").val();
        if (address.length > 0) {
            markLocation(address);
        }
    }

    function clearAddress() {
        $("#queryaddress").val("");
    }

    function markLocation(address) {
        if (queryAddressMarker) {
            map.remove(queryAddressMarker);
        }
        AMap.plugin('AMap.Geocoder', function() {
            var geocoder = new AMap.Geocoder();
            geocoder.getLocation(address, function(status, result) {
                if (status === 'complete' && result.info === 'OK') {

                    // 经纬度
                    var lng = result.geocodes[0].location.lng;
                    var lat = result.geocodes[0].location.lat;

                    // 添加标记
                    queryAddressMarker = new AMap.Marker({
                        map: map,
                        position: new AMap.LngLat(lng, lat),   // 经纬度
                    });
                } else {
                    console.log('定位失败！');
                }
            });
        });
    }
</script>
</body>
</html>