<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>网点天猫预警</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <script src="${ctxStatic}/sd/ServicePointOrderService.js?_v=${OrderJsVersion}" type="text/javascript"></script>
</head>
<style type="text/css">
    .table thead th, .table tbody td {
        text-align: center;
        vertical-align: middle;
    }
</style>
<script type="text/javascript">
    Order.rootUrl = "${ctx}";
    ServicePointOrderService.rootUrl = "${ctx}";

    $(document).ready(function () {
        $('a[data-toggle=tooltip]').darkTooltip();
        $('a[data-toggle=tooltipnorth]').darkTooltip({gravity: 'north'});
        $('a[data-toggle=tooltipeast]').darkTooltip({gravity: 'east'});
    });

    function monitorFeedback(orderNo, monitorId, id) {
        top.layer.open({
            type: 2,
            id: 'layer_serviceMonitor',
            zIndex: 19891015,
            title: '天猫预警反馈[' + orderNo + ']',
            content: "${ctx}/sd/order/serviceMonitor/feedbackFrom?monitorId=" + monitorId + "&id=" + id,
            area: ['830px', '380px'],
            shade: 0.3,
            maxmin: false,
            success: function (layero, index) {
            },
            end: function () {
                var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                if (iframe != undefined) {
                    var repageFlag = $("#repageFlag", iframe.document).val();
                    if (repageFlag == "true") {
                        iframe.repage();
                    }
                }
            }
        });
    }
</script>

<body>
<ul class="nav nav-tabs">
    <li><a href="${ctx}/servicePoint/sd/processOrderList/reminderlist" title="催单的工单">催单</a></li>
    <li><a href="${ctx}/servicePoint/sd/processOrderList/planinglist" title="待接派的工单列表">待接派单</a></li>
    <li><a href="${ctx}/servicePoint/sd/processOrderList/noAppointmentList" title="未预约的工单">未预约</a></li>
    <li><a href="${ctx}/servicePoint/sd/processOrderList/arriveAppointmentList" title="预约到期的工单">预约到期</a></li>
    <li><a href="${ctx}/servicePoint/sd/processOrderList/passAppointmentList" title="预约超期的工单">预约超期</a></li>
    <li><a href="${ctx}/servicePoint/sd/processOrderList/servicedList" title="待回访的工单">待回访</a></li>
    <li><a href="${ctx}/servicePoint/sd/processOrderList/followUpFailList" title="回访失败的工单">回访失败</a></li>
    <li><a href="${ctx}/servicePoint/sd/processOrderList/tmallAnomalyList" title="天猫一键求助">求助</a></li>
    <li  class="active"><a href="javascript:void(0);" title="天猫预警">预警<span id="spn_order_count" class="badge badge-info">${page !=null ?page.count:''}</span></a></li>
    <li><a href="${ctx}/servicePoint/sd/processOrderList/rushinglist" title="突击中的工单">突击单</a></li>
    <li><a href="${ctx}/servicePoint/sd/processOrderList/complainlist" title="投诉的工单">投诉</a></li>
    <li><a href="${ctx}/servicePoint/sd/processOrderList/pendinglist" title="需要等待的工单">停滞</a></li>
    <li><a href="${ctx}/servicePoint/sd/processOrderList/uncompletedList" title="未完成的工单">未完成</a></li>
    <li><a href="${ctx}/servicePoint/sd/processOrderList/completedList" title="已完成的工单">已完成</a></li>
    <li><a href="${ctx}/servicePoint/sd/processOrderList/alllist" title="所有工单">所有</a></li>
</ul>
<form:form id="searchForm" modelAttribute="entity" action="${ctx}/servicePoint/sd/processOrderList/tmallServiceMonitorList" method="post"
           class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <input id="repageFlag" type="hidden" value="false">
    <div class="alert alert-block" style="padding-top:6px;padding-bottom:6px;">
        注:当用工单号进行搜索查询时，不受当前时间、状态、预警分类等其他条件的限制。
    </div>
    <div class="control-group">
        <label class="label-search">工单号：</label>&nbsp;
        <input type=text class="input-small" id="orderNo" name="orderNo" value="${entity.orderNo }" maxlength="20"/>
        <label class="label-search">预警等级：</label>
        <form:select path="level" class="input-small" style="width:125px;">
            <form:option value="0" label="所有"/>
            <form:options items="${fns:getDictListFromMS('OrderMonitorLevel')}" itemLabel="label" itemValue="value"
                          htmlEscape="false"/>
        </form:select>
        <label class="label-search">状态：</label>
        <form:select path="status" class="input-small" style="width:125px;">
            <form:option value="0" label="所有"/>
            <form:option value="1" label="未反馈"/>
            <form:option value="2" label="已反馈"/>
        </form:select>
        &nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
    </div>
</form:form>
<sys:message content="${message}"/>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="50">序号</th>
        <th width="100">订单号</th>
        <th width="145">区域</th>
        <th width="70">状态</th>
        <th width="100">预警等级</th>
        <th width="200">预警内容</th>
        <th width="100">预警时间</th>
        <th width="100">反馈内容</th>
        <th width="100">反馈时间</th>
        <th width="100">客服</th>
        <shiro:hasPermission name="sd:servicemonitor:feedback">
            <th width="40">操作</th>
        </shiro:hasPermission>
    </tr>
    </thead>
    <tbody>
    <c:set var="index" value="0"></c:set>
    <c:forEach items="${page.list}" var="vmodel">
        <tr>
            <c:set var="index" value="${index+1}"></c:set>
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>
                <a href="javascript:void(0);" onclick="ServicePointOrderService.showProcessOrderDetail('${vmodel.orderId}','${vmodel.quarter}',1);">
                    <abbr title="查看订单详情">${vmodel.orderNo}</abbr>
                </a>
            </td>
            <td>${vmodel.areaName}</td>
            <c:choose>
                <c:when test="${vmodel.status==1}">
                    <td><span class="label status_60">未反馈</span></td>
                </c:when>
                <c:otherwise>
                    <td><span class="label status_50">已反馈</span></td>
                </c:otherwise>
            </c:choose>
            <td>${fns:getDictLabelFromMS(vmodel.level,'OrderMonitorLevel','OrderMonitorLevel')}</td>
            <td>
                <c:choose>
                    <c:when test="${fn:length(vmodel.content)>100}">
                        <a href="javascript:void(0);" data-toggle="tooltip"
                           data-tooltip="${vmodel.content}">${fns:abbr(vmodel.content,150)}</a>
                    </c:when>
                    <c:otherwise>
                        ${vmodel.content}
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <fmt:formatDate value="${vmodel.gmtDate}" pattern="yyyy-MM-dd HH:mm"/>
            </td>
            <td>${vmodel.replyContent}</td>
            <td>
                <c:if test="${vmodel.status==2}">
                    <fmt:formatDate value="${vmodel.replyDate}" pattern="yyyy-MM-dd HH:mm"/>
                </c:if>
            </td>
            <td>${vmodel.replierName}</td>
            <shiro:hasPermission name="sd:servicemonitor:feedback">
                <td>
                    <c:if test="${vmodel.status==1}">
                        <a class="btn btn-mini btn-primary" href="javascript:void(0);"
                           onclick="monitorFeedback('${vmodel.orderNo}','${vmodel.monitorId}','${vmodel.id}')">反馈</a>
                    </c:if>
                </td>
            </shiro:hasPermission>
        </tr>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
</html>
