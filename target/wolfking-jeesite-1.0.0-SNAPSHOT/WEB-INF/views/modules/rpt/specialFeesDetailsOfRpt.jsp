<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<html>
<head>
    <title>费用详情</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default" />
    <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" />
    <meta name="generator" content="1.0"/>
    <%@include file="/WEB-INF/views/include/dialog.jsp"%>
    <script src="${ctxStatic}/zeroClipboard/ZeroClipboard.js" type="text/javascript"></script>
    <script type="text/javascript" src="${ctxStatic}/zeroClipboard/jquery.zclip.min.js"></script>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <script src="${ctxStatic}/common/doT.min.js" type="text/javascript"></script>
    <link href="${ctxStatic}/bootstrap/2.3.1/bwizard/bwizard.min.css" type="text/css" rel="stylesheet" />

    <style type="text/css">
        .table thead th,.table tbody td {text-align: center;vertical-align: middle;}
        .tdlable {width:90px;text-align: right;}
        .tdbody {width:300px ;}
        .table th,.table td {padding: 4px;}
        .table thead th {text-align: center;vertical-align: middle;}
        .table .tdcenter {text-align: center;vertical-align: middle;}
        .alert {padding: 4px 5px 4px 4px; margin-right: 5px;}
        #toolbar{height: 40px;line-height: 40px;}
        .form-horizontal .control-label{width:90px;}
        .form-horizontal .controls {margin-left:120px;}
        i[class^="icon-"] {font-size:18px;}
    </style>
</head>
<body>
<div id="divGrid">
    <table id="contentTable" class="table table-bordered table-hover" style="margin-top:5px;margin-left:auto;margin-right:auto;width:98%" >
        <thead>
        <tr>
            <th width="40">序号</th>
            <th width="100">工单号</th>
            <th width="100">网点编号</th>
            <th width="100">网点名称</th>
            <th width="60">安维师傅</th>
            <th width="60">
                <c:if test="${chargeFlag == 1}">远程费用</c:if>
                <c:if test="${chargeFlag == 2}">其他费用</c:if>
            </th>
        </tr>
        </thead>
        <tbody>
        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${list}" var="item">
        <c:set var="rowIndex" value="${rowIndex+1}"/>
            <tr>
                <td>${rowIndex}</td>
                <td>${item.orderNo}</td>
                <td>${item.servicePointNo}</td>
                <td>${item.servicepointName}</td>
                <td>${item.engineerName}</td>
                <td>
                    <c:if test="${chargeFlag == 1}">${item.engineerTravelCharge}</c:if>
                    <c:if test="${chargeFlag == 2}">${item.engineerOtherCharge}</c:if>
                </td>

            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
</body>
</html>
