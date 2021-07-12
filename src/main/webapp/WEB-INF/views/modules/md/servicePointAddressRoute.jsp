<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>路径规划</title>
    <meta name="decorator" content="default"/>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no, width=device-width">
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script type="text/javascript" src="https://webapi.amap.com/maps?v=1.4.15&key=8101d6c8784be11a3d7d49b5fb6283f7&plugin=AMap.Driving"></script>
    <link href="${ctxStatic}/amap/AMap.CloudDataSearchRender1120.css" type="text/css" rel="stylesheet" />
</head>
<body>
<form:form id="searchForm" action="${ctx}/md/servicepoint/addressRoute" method="POST" class="breadcrumb form-search">
    <ul class="ul-form">
        <li>
            <label>出发地：</label>
            <input type="text" name="fromAddr" class="input-xlarge" value="${fromAddress}" maxlength="100">
        </li>
        <li>
            <label>目的地：</label>
            <input type="text" name="toAddr" class="input-xlarge" value="${toAddress}" maxlength="100">
        </li>
        <li class="btns"><input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" /></li>
        <li class="clearfix"></li>
    </ul>
    <ul class="ul-form">
        <li>
            <label>路径估算：</label>
            <span id="gusuan"></span>
        </li>
    </ul>
</form:form>
<div id="container" style="height: 89%;width:100%;position: absolute;overflow: hidden;"></div>
<script type="text/javascript">
    var map = new AMap.Map('container',{
        resizeEnable: true,
        zoom: 15,
        center:[${centerLng},${centerLat}]  // 定位
    });
    addToolbar();

    //构造路线导航类
    var driving = new AMap.Driving({
        map: map
    });
    // 根据起终点经纬度规划驾车导航路线
    driving.search(new AMap.LngLat(${centerLng}, ${centerLat}), new AMap.LngLat(${toLng}, ${toLat}), function(status, result) {
        if (status === 'complete') {
            //console.log(result);
            if (result != null && result.routes != null && result.routes.length >0) {
                var driveRoute = result.routes[0];
                var distance = parseFloat(driveRoute.distance/1000).toFixed(3).slice(0,-1);
                var time = parseInt(driveRoute.time/60);
                $("#gusuan").html(distance+" 千米，需耗时："+time+" 分钟");
                //console.log("共有："+distance+" 千米，需耗时:"+time+" 分钟");
            }
        } else {
            console.log('获取驾车数据失败：' + result)
        }
    });

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

</script>
</body>
</html>
