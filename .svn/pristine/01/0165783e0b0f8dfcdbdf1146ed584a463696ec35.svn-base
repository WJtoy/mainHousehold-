<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>定位上门地址</title>
    <meta name="decorator" content="default"/>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no, width=device-width">
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script type="text/javascript" src="https://webapi.amap.com/maps?v=1.4.14&key=8101d6c8784be11a3d7d49b5fb6283f7"></script>
    <link href="${ctxStatic}/amap/AMap.CloudDataSearchRender1120.css" type="text/css" rel="stylesheet" />
</head>
<body>
<div id="container" style="height: 100%;width:100%;position: absolute;overflow: hidden;"></div>
<script type="text/javascript">
    var map = new AMap.Map('container',{
        resizeEnable: true,
        zoom: 15,
        center:[${centerLng},${centerLat}]  // 定位
    });
    addToolbar();

    // 经纬度
    var lng = ${centerLng};
    var lat = ${centerLat};

    // 添加标记
    var queryAddressMarker = new AMap.Marker({
        map: map,
        position: new AMap.LngLat(lng, lat),   // 经纬度
    });
    queryAddressMarker.setTitle('${address}');

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