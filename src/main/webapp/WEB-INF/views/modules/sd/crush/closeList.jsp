<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html style="overflow-x:hidden;overflow-y:auto;">
<head>
    <title>订单处理-突击单列表</title>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <style type="text/css">
        .table thead th, .table tbody td {text-align: center;vertical-align: middle;}
        .item_no{color: red;}
    </style>
    <script type="text/javascript">
        top.layer.closeAll();
        Order.rootUrl = "${ctx}";
        $(document).on("click", "#btnClearSearch", function () {
            $("#orderNo").val("");
            $("#crushNo").val("");
            $("#areaId").val("");
            $("#areaLevel").val("");
            $("#areaName").val("");
            $("#productCategoryId").val("0");
            $("#orderServiceType").val("0");
            $("#s2id_orderServiceType").find("span.select2-chosen").html('所有');
            search();
        });
    </script>
</head>
<body>
<ul id="navtabs" class="nav nav-tabs">
    <li><a href="${ctx}/sd/order/crush/list" title="处理中列表">处理中</a></li>
    <li class="active"><a href="javascript:;" title="已完成列表">已完成</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }"/>
<form:form id="searchForm" modelAttribute="orderCrush" method="post" class="form-inline">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div class="alert alert-block" style="padding-top:6px;padding-bottom:6px;">
        注:当用订单号,突击单号进行搜索查询时，不受发起日期、区域及品类等其他条件的限制。
    </div>
    <div class="control-group">
        <label class="label-search">订单号：</label>&nbsp;
        <input type=text class="input-small" id="orderNo" name="orderNo" maxlength="30" value="${orderCrush.orderNo}"/>
        &nbsp;&nbsp;
        <label class="label-search">突击单号：</label>&nbsp;
        <input type=text class="input-small" id="crushNo" name="crushNo" maxlength="30" value="${orderCrush.crushNo}"/>
        &nbsp;&nbsp;
        <label class="label-search">发起日期：</label>
        <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:95px;"
               maxlength="20" class="input-small Wdate" value="${fns:formatDate(orderCrush.beginDate,'yyyy-MM-dd')}"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        <label>~</label>&nbsp;&nbsp;&nbsp;
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:95px" maxlength="20"
               class="input-small Wdate" value="${fns:formatDate(orderCrush.endDate,'yyyy-MM-dd')}"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        &nbsp;&nbsp;
        <label class="label-search">处理人：</label>&nbsp;
        <input type=text class="input-small" id="closeByName" name="closeBy" maxlength="30" value="${orderCrush.closeBy}"/>
        &nbsp;&nbsp;
        <label>区域：</label>
        <%--
        <sys:treeselect id="area" name="area.id" value="${orderCrush.area.id}" levelValue="${orderCrush.areaLevel}" nodeLevel="true"
                        labelName="area.name" labelValue="${orderCrush.area.name }" title="区域"
                        url="/sys/area/treeData?kefu=${currentuser.id}" allowClear="true" nodesLevel="-1" nameLevel="3"/>
        --%>
        <sys:treeselectareanew id="area" name="area.id" value="${orderCrush.area.id}" levelValue="${orderCrush.areaLevel}"
                               labelName="area.name" labelValue="${orderCrush.area.name}" title="区域" clearIdValue="0"
                               url="/sys/area/treeDataNew?kefu=${currentuser.id}" allowClear="true" nodesLevel="-1" nameLevel="3"/>
        <label>品类：</label>
        <form:select path="productCategoryId" cssClass="input-small" cssStyle="width:125px;">
            <form:option value="0" label="所有"/>
            <form:options items="${categories}" itemLabel="name" itemValue="id" htmlEscape="false"/>
        </form:select>&nbsp;&nbsp;
        <label>订单类型：</label>
        <form:select path="orderServiceType" cssClass="input-small" cssStyle="width:125px;">
            <form:option value="0" label="所有"/>
            <form:options items="${fns:getDictExceptListFromMS('order_service_type',0)}"
                          itemLabel="label" itemValue="value" htmlEscape="false" />
        </form:select>&nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询"/>&nbsp;&nbsp;
        <input id="btnClearSearch" class="btn btn-primary" type="button" value="清除条件"/>&nbsp;&nbsp;
    </div>
</form:form>
<sys:message content="${message}"/>
<c:set var="rowNumber" value="0"/>
<c:set var="crushview" value="0"/>
<c:set var="crushedit" value="0"/>
<shiro:hasPermission name="sd:orderCrush:edit">
    <c:set var="crushedit" value="1"/>
</shiro:hasPermission>
<shiro:lacksPermission name="sd:orderCrush:edit">
    <shiro:hasPermission name="sd:ordercrush:view">
        <c:set var="crushview" value="1"/>
    </shiro:hasPermission>
</shiro:lacksPermission>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="50">序号</th>
        <th width="100">突击单号</th>
        <th width="100">订单号</th>
        <th width="100">突击次数</th>
        <th width="100">订单类型</th>
        <th width="100">品类</th>
        <th width="50">状态</th>
        <th width="100">发起人</th>
        <th width="150">发起日期</th>
        <th width="200">发起说明</th>
        <th width="100">处理人</th>
        <th width="150">处理日期</th>
        <th>处理记录</th>
        <th width="200">用户地址</th>
        <th width="100">完成时效</th>
        <c:if test="${crushview eq 1 or crushedit eq 1}">
        <th width="100">操作</th>
        </c:if>
    </tr>
    </thead>
    <tbody>
    <c:set var="rowcnt" value="${page.list.size()}"/>
    <c:forEach items="${page.list}" var="entity">
        <c:set var="rowNumber" value="${rowNumber+1}"/>
        <tr>
            <td>${rowNumber}</td>
            <td>
                <c:choose>
                    <c:when test="${crushedit eq 1 or crushview eq 1}">
                        <a href="javascript:;" onclick="Order.crush_view('${entity.id}','${entity.quarter}')" class="${entity.itemNo>=2?'item_no':''}"> ${entity.crushNo}</a>
                    </c:when>
                    <c:otherwise><a href="javascript:;" class="${entity.itemNo>=2?'item_no':''}">${entity.crushNo}</a></c:otherwise>
                </c:choose>
            </td>
            <td>${entity.orderNo}</td>
            <td>第${entity.itemNo}次突击</td>
            <td>${entity.orderServiceTypeName}</td>
            <td>${entity.productCategoryName}</td>
            <td><span class="label status_80">已完成</span></td>
            <td>${entity.createBy.name}</td>
            <td>
                <fmt:formatDate value="${entity.createDate}" pattern="yyyy-MM-dd HH:mm"></fmt:formatDate>
            </td>
            <td>
                <c:choose>
                    <c:when test="${fn:length(entity.createRemark)>36}">
                        <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${entity.createRemark}">${fns:abbr(entity.createRemark,36)}</a>
                    </c:when>
                    <c:otherwise>
                        ${entity.createRemark}
                    </c:otherwise>
                </c:choose>
            </td>
            <td>${entity.closeBy.name}</td>
            <td>
                <fmt:formatDate value="${entity.closeDate}" pattern="yyyy-MM-dd HH:mm"></fmt:formatDate>
            </td>
            <td>
                <c:choose>
                    <c:when test="${fn:length(entity.closeRemark)>36}">
                        <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${entity.closeRemark}">${fns:abbr(entity.closeRemark,36)}</a>
                    </c:when>
                    <c:otherwise>
                        ${entity.closeRemark}
                    </c:otherwise>
                </c:choose>
            </td>
            <td>${entity.userAddress}</td>
            <td>${entity.timeLinessLabel}</td>
            <c:if test="${crushview eq 1 or crushedit eq 1}">
            <td>
                <a href="javascript:void(0);" class="btn btn-mini btn-warning" onclick="Order.crush_view('${entity.id}','${entity.quarter}');">查看</a>
            </td>
            </c:if>
        </tr>


    </c:forEach>
    </tbody>
</table>
<div id="pagination" class="pagination">${page}</div>
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
