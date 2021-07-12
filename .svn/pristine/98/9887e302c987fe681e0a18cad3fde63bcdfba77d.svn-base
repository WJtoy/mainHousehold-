<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html style="overflow-x:hidden;overflow-y:auto;">
<head>
    <title>客户列表-好评</title>
    <meta name="description" content="好评">
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
    <script type="text/javascript">

        $(document).ready(function() {
            var praiseCount = ${praiseCount}
            var arrPraise = praiseCount.toString();
            for(var i=arrPraise.length-1;i>=0;i--){
                if(5>arrPraise.length){
                    var index = 5-arrPraise.length+i+1
                    $("#count"+index).text(arrPraise[i]);
                }else{
                    var index = i+1
                    $("#count"+index).text(arrPraise[i]);
                }
            }
        });
    </script>
    <style type="text/css">
        .yinyi {
            width:45px;
            height:55px;
            float:left;
            font-size:100%;
            background-color:#FFC412;
            margin-top:13px;
            margin-right:21px;
            text-align:center;
            line-height:55px;
            box-shadow: inset 1px 1px 8px 1px #848484;
            border-radius:3px;
        }

    </style>
</head>

<body>
<ul id="navtabs" class="nav nav-tabs">
    <li><a href="${ctx}/sd/order/customerNew/materialList" data-toggle="tooltipnorth" data-tooltip="&nbsp;&nbsp;&nbsp;&nbsp;要发配件的订单">待发配件</a></li>
    <li><a href="${ctx}/sd/order/customerNew/list" data-toggle="tooltipnorth" data-tooltip="&nbsp;&nbsp;处理中订单列表">处理中</a></li>
    <li><a href="${ctx}/sd/order/customerNew/finishlist" data-toggle="tooltipnorth" data-tooltip="已完成订单列表">已完成</a></li>
    <li><a href="${ctx}/sd/order/customerNew/cancellist" data-toggle="tooltipnorth" data-tooltip="取消单列表">取消单</a></li>
    <li><a href="${ctx}/sd/order/customerNew/returnlist" data-toggle="tooltipnorth" data-tooltip="退单列表">退单</a></li>
    <li><a href="${ctx}/sd/order/customerNew/alllist" data-toggle="tooltipnorth" data-tooltip="所有订单列表">所有</a></li>
    <li><a href="${ctx}/sd/order/customerNew/complainlist" data-toggle="tooltipnorth" data-tooltip="投诉列表">投诉</a></li>
    <c:if test="${reminderFlag==1}">
        <li><a href="${ctx}/sd/order/customerNew/reminderList" data-toggle="tooltipnorth" data-tooltip="催单列表">催单</a></li>
    </c:if>
    <li class="active"><a href="javascript:void(0);" data-toggle="tooltipnorth" data-tooltip="好评单列表">好评</a></li>
</ul>
<sys:message content="${message}"/>
<div style="width:100%;min-width:1100px;position: absolute;height:75%;min-height:580px";>
    <div style="width:100%;background-image:url('${ctxStatic}/images/guide.png');background-size:100%; padding-bottom: 42%;">
        <div style="float:left;width:7.2%;padding-bottom:35%;"></div>
        <div style="float:left;width:92.8%;padding-bottom:28%;"></div>
        <div style="width:590px;height:80px;color:#fff;font-size:30px;line-height: 80px;font-family:微软雅黑;background-color:rgba(255,255,255,0.2);border-radius:7px;float:left" >
            <span style="display:block;float:left;margin-left:13px;font-family:黑体;margin-right:14px;" >本月好评单数量:</span>
            <div class = "yinyi" id="count1">0</div>
            <div class = "yinyi" id="count2">0</div>
            <div class = "yinyi" id="count3">0</div>
            <div class = "yinyi" id="count4">0</div>
            <div class = "yinyi" style="margin-right:0px;" id="count5">0</div>
        </div>
    </div>
</div>
<script type="text/javascript">
</script>
</body>
</html>
