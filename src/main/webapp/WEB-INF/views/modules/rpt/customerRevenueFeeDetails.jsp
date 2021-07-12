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

<div style="margin-top: 20px;overflow: auto;margin-right:auto;margin-left:auto;width: 963px;padding: 15px 0;box-sizing: border-box;border: 1px solid #eee">
<div style="float: left;margin-left: 14px;width: 180px">
    <div style="margin-bottom:15px;font-size: 12px;">客户费用</div>
    <div  style="font-size: 16px">
       <fmt:formatNumber value="${entity.receivableCharge}" pattern="0.00"/>元
    </div>
</div>
<div style="float: left;margin-left: 14px;width: 180px;padding-left:10px;border-left:1px solid #E8EAEC">
    <div style="margin-bottom:15px;font-size: 12px">网点费用</div>
    <div  style="font-size: 16px">
      <fmt:formatNumber value="${0-entity.payableCharge}" pattern="0.00"/>元
    </div>
</div>
<div style="float: left;margin-left: 14px;width: 150px;padding-left:10px;border-left:1px solid #E8EAEC">
    <div style="margin-bottom:15px;font-size: 12px">工单毛利</div>
    <div  style="font-size: 16px" ><fmt:formatNumber value="${entity.orderGrossProfit}" pattern="0.00"/>元
    </div>
</div>
<div style="float: left;margin-left: 14px;width: 150px;padding-left:10px;border-left:1px solid #E8EAEC">
    <div style="margin-bottom:15px;font-size: 12px">完成单量</div>
    <div  style="font-size: 16px" >${entity.finishOrder}单
    </div>
</div>
<div style="float: left;margin-left: 14px;width: 182px;padding-left:10px;border-left:1px solid #E8EAEC">
    <div style="margin-bottom:15px;font-size: 12px">每单毛利(工单毛利/完成单量)</div>
    <div  style="font-size: 16px"><fmt:formatNumber value="${entity.everySingleGrossProfit}" pattern="0.00"/>元/单
    </div>
</div>
</div>
<div style="margin-right:auto;margin-left:auto;width: 963px;padding: 3px ;background-color:#F8F8F9;box-sizing: border-box;border: 1px solid #eee;border-top: none">
    <span style="margin-left: 11px;color: red"> 注：网点费用含 应付网点费用：<fmt:formatNumber value="${0-(entity.engineerInsuranceCharge+entity.engineerDeposit+entity.payableCharge)}" pattern="0.00"/>元,
        互助基金：<fmt:formatNumber value="${0-entity.engineerInsuranceCharge}" pattern="0.00"/>元,
        质保金额：<fmt:formatNumber value="${0-entity.engineerDeposit}" pattern="0.00"/>元</span>
</div>


<div style="height: 20px"></div>

<table id="contentTablea" class="table table-striped table-bordered table-condensed table-hover" style="margin-top:-2px;margin-left:auto;margin-right:auto;width:963px" >
        <thead>
        <tr>
            <th>费用明细</th>
            <th>客户费用(元)</th>
            <th>网点费用(元)</th>
            <th>工单毛利(元)</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td>服务费</td>
            <td><fmt:formatNumber value="${entity.customerServiceCharge}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${0-entity.engineerServiceCharge}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${entity.customerServiceCharge-entity.engineerServiceCharge}" pattern="0.00"/></td>
        </tr>

        <tr>
            <td>快递费用</td>
            <td><fmt:formatNumber value="${entity.customerExpressCharge}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${0-entity.engineerExpressCharge}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${entity.customerExpressCharge-entity.engineerExpressCharge}" pattern="0.00"/></td>
        </tr>

        <tr>
            <td >远程费用</td>
            <td><fmt:formatNumber value="${entity.customerTravelCharge}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${0-entity.engineerTravelCharge}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${entity.customerTravelCharge-entity.engineerTravelCharge}" pattern="0.00"/></td>
        </tr>

        <tr>
            <td>配件费用</td>
            <td><fmt:formatNumber value="${entity.customerMaterialCharge}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${0-entity.engineerMaterialCharge}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${entity.customerMaterialCharge-entity.engineerMaterialCharge}" pattern="0.00"/></td>
        </tr>

        <tr>
            <td>时效费用</td>
            <td><fmt:formatNumber value="${entity.customerTimelinessCharge}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${0-entity.engineerTimelinessCharge}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${entity.customerTimelinessCharge-entity.engineerTimelinessCharge}" pattern="0.00"/></td>
        </tr>

        <tr>
            <td>加急费用</td>
            <td><fmt:formatNumber value="${entity.customerUrgentCharge}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${0-entity.engineerUrgentCharge}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${entity.customerUrgentCharge-entity.engineerUrgentCharge}" pattern="0.00"/></td>
        </tr>

        <tr>
            <td>其他费用</td>
            <td><fmt:formatNumber value="${entity.customerOtherCharge}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${0-entity.engineerOtherCharge}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${entity.customerOtherCharge-entity.engineerOtherCharge}" pattern="0.00"/></td>
        </tr>

        <tr>
            <td>好评费</td>
            <td><fmt:formatNumber value="${entity.customerPraiseFee}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${0-entity.engineerPraiseFee}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${entity.customerPraiseFee-entity.engineerPraiseFee}" pattern="0.00"/></td>
        </tr>

        <tr>
            <td>退补金额</td>
            <td><fmt:formatNumber value="${entity.customerWriteOffCharge}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${0-entity.engineerWriteOffCharge}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${entity.customerWriteOffCharge-entity.engineerWriteOffCharge}" pattern="0.00"/></td>
        </tr>

        <tr>
            <td>厂商时效费用</td>
            <td>-</td>
            <td><fmt:formatNumber value="${0-entity.engineerCustomerTimelinessCharge}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${0-entity.engineerCustomerTimelinessCharge}" pattern="0.00"/></td>
        </tr>

<%--        <tr>--%>
<%--            <td>互助基金</td>--%>
<%--            <td>-</td>--%>
<%--            <td><fmt:formatNumber value="${0-entity.engineerInsuranceCharge}" pattern="0.00"/></td>--%>
<%--            <td><fmt:formatNumber value="${0-entity.engineerInsuranceCharge}" pattern="0.00"/></td>--%>
<%--        </tr>--%>

        <tr>
            <td>扣点</td>
            <td>-</td>
            <td><fmt:formatNumber value="${0-entity.taxFee}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${0-entity.taxFee}" pattern="0.00"/></td>
        </tr>

        <tr>
            <td>平台服务费</td>
            <td>-</td>
            <td><fmt:formatNumber value="${0-entity.infoFee}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${0-entity.infoFee}" pattern="0.00"/></td>
        </tr>

<%--        <tr>--%>
<%--            <td>质保金额</td>--%>
<%--            <td>-</td>--%>
<%--            <td><fmt:formatNumber value="${0-entity.engineerDeposit}" pattern="0.00"/></td>--%>
<%--            <td><fmt:formatNumber value="${0-entity.engineerDeposit}" pattern="0.00"/></td>--%>
<%--        </tr>--%>

        <tr>
            <td>合计</td>
            <td><fmt:formatNumber value="${entity.receivableTotalCharge}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${0-entity.payableChargeTotalCharge}" pattern="0.00"/></td>
            <td><fmt:formatNumber value="${entity.receivableTotalCharge-entity.payableChargeTotalCharge}" pattern="0.00"/></td>
        </tr>
        </tbody>
    </table>
    <div style="margin-left: 18px">
      <span style="color: red">*</span><span>正数表示平台应收取费用，负数表示平台应支付费用</span>
    </div>
</body>
</html>
