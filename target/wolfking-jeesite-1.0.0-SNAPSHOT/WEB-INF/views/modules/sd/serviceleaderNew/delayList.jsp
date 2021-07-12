<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html style="overflow-x:hidden;overflow-y:auto;">
<head>
    <title>异常工单-延时列表</title>
    <meta name="description" content="下单超过1小时未接单">
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <!-- jquery.supgerTable -->
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            /*background: transparent;*/
        }
    </style>
    <script type="text/javascript">
        top.layer.closeAll();
        Order.rootUrl = "${ctx}";

        //覆盖分页前方法
        function beforePage() {
            var $btnSubmit = $("#btnSubmit");
            $btnSubmit.attr('disabled', 'disabled');
            // $btnSubmit.val("...");
            $("#btnClearSearch").attr('disabled', 'disabled');
            layerLoading("查询中...", true);
        }

        var clicktag = 0;
        $(document).on("click", "#btnSubmit", function () {
            if (clicktag == 0) {
                clicktag = 1;
                beforePage();
                setPage();
                this.form.submit();
            }
        });

        $(document).on("click", "#btnClearSearch", function () {
            $("#searchForm")[0].reset();
            $("#areaId").val("");
            $("#areaName").val("");
            $("#customerId").val("");
            $("#customerName").val("");
            $("#orderNo").val("");
            $("#dataSource").val("0");
            $("#s2id_dataSource").find("span.select2-chosen").html('所有');
            search();
        });
    </script>
</head>
<body>
<ul id="navtabs" class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);" title="下单超1小时未接单">延时</a></li>
    <li><a href="${ctx}/sd/order/serviceLeaderNew/reservationlist" title="预约时间两次及以上">爽约</a></li>
    <li><a href="${ctx}/sd/order/serviceLeaderNew/complainlist" title="被厂商或用户投诉的工单">投诉单</a></li>
    <li ><a href="${ctx}/sd/order/serviceLeaderNew/travellist" title="远程费超20的工单">远程单</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }"/>
<form:form id="searchForm" modelAttribute="order" action="${ctx}/sd/order/serviceLeaderNew/delaylist" method="post" class="form-inline">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <input id="searchType" name="searchType" type="hidden" value="delaylist" />
    <input id="repageFlag" name="repageFlag" type="hidden" value="false" />
    <div class="alert alert-block" style="padding-top:6px;padding-bottom:6px;">
        注:当用工单号和手机号码进行搜索查询时，不受当前时间、地区等其他条件的限制。
    </div>
    <div class="control-group">
        <label class="label-search">订单来源：</label>
        <form:select path="dataSource" class="input-small" style="width:125px;">
            <form:option value="0" label="所有"/>
            <form:options items="${fns:getDictListFromMS('order_data_source')}" itemLabel="label" itemValue="value" htmlEscape="false"/>
        </form:select>
        <label>订单号：</label>&nbsp;
        <input type=text class="input-small" id="orderNo" name="orderNo" value="${order.orderNo }" maxlength="20" />
        <label class="label-search">客户：</label>
       <%-- <sys:treeselect id="customer" name="customer.id" value="${order.customer.id}" labelName="customer.name"
                        labelValue="${order.customer.name}" cssStyle="width:211px;"
                        title="客户" url="/md/customer/treeData?kefu=${currentuser.id}"
                        cssClass="input-small" allowClear="true"/>--%>
        <form:select path="customer.id" style="width:250px;">
            <form:option value="0" label="所有"/>
            <form:options items="${fns:getMyCustomerList()}" itemLabel="name" itemValue="id"
                          htmlEscape="false"/>
        </form:select>
        <label>区域：</label>
        <sys:treeselect id="area" name="area.id" value="${order.area.id}" levelValue="${order.areaLevel}" nodeLevel="true"
                        labelName="area.name" labelValue="${order.area.name }" title="区域"
                        url="/sys/area/treeData?kefu=${currentuser.id}" allowClear="true" nodesLevel="-1" nameLevel="3"/>
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/>&nbsp;&nbsp;
        <input id="btnClearSearch" class="btn btn-primary" type="button" value="清除条件"/>&nbsp;&nbsp;
    </div>
</form:form>
<sys:message content="${message}"/>
<c:set var="rowNumber" value="0"/>
<c:set var="processTimeout" value="${fns:getDictSingleValueFromMS('order_process_timeout', '48')}"/><%-- 切换为微服务 --%>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="40">序号</th>
        <th width="145">单号</th>
        <th width="40">状态</th>
        <th width="60">来源</th>
        <th width="100">品牌</th>
        <th width="150">产品</th>
        <th width="65">服务类型</th>
        <th width="45">数量</th>
        <th width="45">客服</th>
        <th width="65">用户</th>
        <th width="100">手机</th>
        <th>安装地址</th>
    </tr>
    </thead>
    <tbody>
    <c:set var="rowcnt" value="${page.list.size()}"/>
    <c:forEach items="${page.list}" var="order">
        <c:set var="rowNumber" value="${rowNumber+1}"/>
        <c:set var="rowspan" value="${order.items.size() eq 0?1:order.items.size() }"/>
        <tr>
            <td rowspan="${rowspan}">${rowNumber}</td>
            <td rowspan="${rowspan}">
                <a href="javascript:void(0);" onclick="Order.showOrderDetail('${order.id}','${order.quarter}');">
                    <abbr title="超1小时未接单接单">${order.orderNo}</abbr><c:if test="${not empty order.repeateNo}"><abbr title="疑似重复订单，相关单号:${order.repeateNo}" >(重)</abbr></c:if>
                </a>
                <%--<c:if test="${order.orderCondition.isComplained>0}">
                    <c:choose>
                        <c:when test="${order.complainFormStatus == null}">
                            <a class="label label-warning">投诉</a>
                        </c:when>
                        <c:when test="${order.complainFormStatus.value == '0'}">
                            <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-important">投诉</a>
                        </c:when>
                        <c:when test="${order.complainFormStatus.value == '1'}">
                            <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-success">投诉</a>
                        </c:when>
                        <c:when test="${order.complainFormStatus.value == '2'}">
                            <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label">投诉</a>
                        </c:when>
                        <c:otherwise>
                            <a class="label label-warning">投诉</a>
                        </c:otherwise>
                    </c:choose>
                </c:if>--%>
                <c:if test="${order.orderStatus.complainFlag>0}">
                    <c:choose>
                        <c:when test="${order.complainFormStatus == null}">
                            <a class="label label-warning">投诉</a>
                        </c:when>
                        <c:when test="${order.complainFormStatus.value == '0'}">
                            <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-important">投诉</a>
                        </c:when>
                        <c:when test="${order.complainFormStatus.value == '1'}">
                            <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label label-success">投诉</a>
                        </c:when>
                        <c:when test="${order.complainFormStatus.value == '2'}">
                            <a data-toggle="tooltip" data-tooltip="${order.complainFormStatus.label}" class="label">投诉</a>
                        </c:when>
                        <c:otherwise>
                            <a class="label label-warning">投诉</a>
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </td>
            <td rowspan="${rowspan}">
                <span class="label status_${order.orderCondition.status.value}">${order.orderCondition.status.label}</span>
            </td>
            <td rowspan="${rowspan}">${order.dataSource.label}</td>
            <c:set var="item" value="${order.items[0]}"/>
            <td>${item.brand }</td>
            <td>
                <a href="javascript:" data-toggle="tooltip" data-tooltip="${item.productSpec}">${item.product.name } </a>
            </td>
            <td>${item.serviceType.name }</td>
            <td>${item.qty}</td>
            <td rowspan="${rowspan}">${order.orderCondition.kefu.name}</td>
            <td rowspan="${rowspan}">${order.orderCondition.userName}</td>
            <td rowspan="${rowspan}">
                ${order.orderCondition.servicePhone}
                <c:if test="${!empty order.orderCondition.phone2}">
                    <br/>${order.orderCondition.phone2}
                </c:if>
            </td>
            <td rowspan="${rowspan}">
                    ${order.orderCondition.area.name}&nbsp;${order.orderCondition.serviceAddress}
                <%--<a href="javascript:void(0);" data-toggle="tooltip"--%>
                   <%--data-tooltip="${order.orderCondition.area.name}&nbsp;${order.orderCondition.serviceAddress}">${fns:abbr(order.orderCondition.area.name,40)}</a>--%>
            </td>
        </tr>
        <c:forEach items="${order.items}" var="item" varStatus="i" begin="1">
            <tr class="item">
                <td>${item.brand }</td>
                <td>
                    <a href="javascript:" data-toggle="tooltip" data-tooltip="${item.productSpec}">${item.product.name } </a>
                </td>
                <td>${item.serviceType.name }</td>
                <td>${item.qty}</td>
            </tr>
        </c:forEach>

    </c:forEach>
    </tbody>
</table>
<c:if test="${rowcnt > 0}">
    <div id="pagination" class="pagination">${page}</div>
</c:if>
<script type="text/javascript" language="javascript">
    $(document).ready(function () {
        $('a[data-toggle=tooltip]').darkTooltip();
        $('a[data-toggle=tooltipnorth]').darkTooltip({gravity : 'north'});
        $('a[data-toggle=tooltipeast]').darkTooltip({gravity : 'east'});
    });
</script>
<style type="text/css">
    .dropdown-menu {min-width: 80px;}
    .dropdown-menu > li > a {text-align: left;padding: 3px 10px;}
    .pagination {margin: 4px 0 0 4px;}
    .label-search {width: 70px;  text-align: right;}
    form {margin: 0 0 5px;}
</style>
</body>
</html>
