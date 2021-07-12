<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>我的订单-待预约(网点)</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<meta name="decorator" content="default" />
	<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
	<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>

<script type="text/javascript">
    Order.rootUrl = "${ctx}";

	function openjBox(url, title, width, height)
	{
		top.$.jBox.open("iframe:" + url, title, width, height,
		{
			top : '5%',
			buttons : {},
			loaded : function(h)
			{
				$("#jbox-iframe", h).prop("height", "98%");
			}
		});
	}

	$(document).on("click", "#btnClearSearch", function()
	{
		$("#searchForm")[0].reset();
		$("#orderNo").val("");
		$("#userName").val("");
        $("#beginAcceptDate").val("");
        $("#endAcceptDate").val("");
		$("#servicePhone").val("");
		$("#engineerName").val("");
        search();
//		page(1, 10);
	});

	$(document).ready(function()
	{
		$('a[data-toggle=tooltip]').darkTooltip();
		$('a[data-toggle=tooltipnorth]').darkTooltip(
		{
			gravity : 'north'
		});
		$('a[data-toggle=tooltipeast]').darkTooltip(
		{
			gravity : 'east'
		});
	});
</script>
<style type="text/css">
.dropdown-menu {
	min-width: 80px;
}

.dropdown-menu>li>a {
	text-align: left;
	padding: 3px 10px;
}

.pagination {
	margin: 10px 0;
}

.label-search {
	width: 70px;
	text-align: right;
}

.td {
	word-break: break-all;
}

.table thead th,.table tbody td {
	text-align: center;
	vertical-align: middle;
	BackColor: Transparent;
}
</style>

</head>

<body>
	<ul id="navtabs" class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">待预约</a></li>
		<li><a href="${ctx}/sd/order/engineer/processingList" title="处理中的订单">处理中</a></li>
		<li><a href="${ctx}/sd/order/engineer/appointedList" title="预约中的订单列表">已预约</a></li>
		<li><a href="${ctx}/sd/order/engineer/waitingAccessoryList" title="等配件的订单列表">等配件</a></li>
		<li><a href="${ctx}/sd/order/engineer/pendingList" title="停滞的订单列表">停滞</a></li>
		<li><a href="${ctx}/sd/order/engineer/finishlist" title="完成的订单列表">完成单</a></li>
		<li><a href="${ctx}/sd/order/engineer/returnlist" title="退单列表">退单</a></li>
		<li><a href="${ctx}/sd/order/engineer/allList" title="所有订单列表">所有</a></li>
	</ul>
	<c:set var="currentuser" value="${fns:getUser() }" />
	<form:form id="searchForm" modelAttribute="order" action="${ctx}/sd/order/engineer/waitingAppointmentList" method="post" class="breadcrumb form-search">
		<input id="searchType" name="searchType" type="hidden" value="accepted" />
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
		<input id="pageSize" name="pageSize" type="hidden"
			value="${page.pageSize}" />
		<div>
			<label>订单编号：</label>&nbsp;
			<input type=text class="input-small" id="orderNo" name="orderNo" value="${order.orderNo}" maxlength="20" />
			<label>联系人：</label>
			<input type=text class="input-mini" id="userName" name="userName" value="${order.userName}" maxlength="20" />
			<label>电 话：</label>
			<input type=text class="input-small" id="userPhone" name="userPhone" value="${order.userPhone}" maxlength="20" />
			<label>安维姓名：</label>
			<input type=text class="input-mini" id="engineerName" name="engineerName" value="${order.engineerName}" maxlength="20" />
		</div>
		<div style="margin-top: 8px;">
			<label>接单时间：</label>
			<input id="beginAcceptDate" name="beginAcceptDate" type="text" readonly="readonly" style="width:120px;margin-left:4px"
				maxlength="20" class="input-small Wdate" value="${fns:formatDate(order.beginAcceptDate,'yyyy-MM-dd')}"
				onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});" />
			<label style="width:45px;text-align: center;"> ~ </label>&nbsp;&nbsp;&nbsp;
			<input id="endAcceptDate" name="endAcceptDate" type="text" readonly="readonly" style="width:120px" maxlength="20"
				class="input-small Wdate" value="${fns:formatDate(order.endAcceptDate,'yyyy-MM-dd')}"
				onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});" />
			&nbsp;&nbsp;
			<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" /> &nbsp;&nbsp;
			<input id="btnClearSearch" class="btn btn-primary" type="button" value="清除条件" />
		</div>
	</form:form>

	<sys:message content="${message}" />
	<c:set var="rowNumber" value="0" />
	<table id="contentTable" class="table table-bordered table-condensed table-striped"
		style="table-layout:fixed;" cellspacing="0" width="100%">
		<thead>
			<tr>
				<th width="30">序号</th>
				<th width="110">单号</th>
				<th width="80">接单时间</th>
				<th width="100">联系人</th>
				<th width="180">地址</th>
				<th width="120">服务描述</th>
				<th width="80">安维信息</th>
				<shiro:hasPermission name="sd:order:engineeraccept">
					<th width="60">操作</th>
				</shiro:hasPermission>
				<th width="80">跟踪进度</th>
				<th width="60">完成照片</th>
				<th width="120">客服信息</th>
				<th>服务明细</th>
			</tr>
		</thead>
		<tbody>
			<c:set var="rowcnt" value="${page.list.size()}"/>
			<c:forEach items="${page.list}" var="order">
				<c:set var="rowNumber" value="${rowNumber+1}" />
				<tr>
					<td>${rowNumber}</td>
					<td>
						<c:if test="${order.orderCondition.replyFlagKefu == 1 }">
							<a href="javascript:void(0);" style="color: red;"><abbr title="问题反馈/回复未处理">${order.orderNo}</abbr> </a>
						</c:if>
						<c:if test="${order.orderCondition.replyFlagKefu != 1}">
							<a href="javascript:void(0);">
							   <abbr>${order.orderNo}</abbr> </a>
						</c:if><br>
						<span class="label status_${order.orderCondition.status.value}">${order.orderCondition.status.label} </span>
						<c:if test="${order.orderCondition.pendingType != null || !order.isClosed()}">
							<c:if test="${order.orderCondition.pendingType.value ne '6' && order.orderCondition.pendingType.value ne '0'}">
								<label class="">${fns:abbr(order.orderCondition.pendingType.label,20)}</label>
							</c:if>
						</c:if>
						<c:if test="${order.orderCondition.isComplained == 2}">
							<span class="label label-warning">投诉</span>
						</c:if>
						<c:if test="${order.orderCondition.urgentLevel.id >0}">
							<a data-toggle="tooltip" data-tooltip="${order.orderCondition.urgentLevel.remarks}" class='label label-important'>加急</a>
						</c:if>
					</td>
					<td><fmt:formatDate value="${order.orderStatus.planDate}" pattern="yyyy-MM-dd" />
					</td>

					<td>${order.orderCondition.userName}<br>
						${order.orderCondition.servicePhone}
					</td>
					<td>
						<a href="javascript:" data-toggle="tooltip"
						   data-tooltip="${order.orderCondition.area.name}&nbsp;${order.orderCondition.serviceAddress}">${order.orderCondition.area.name}</a>
					</td>
					<td>
						<a href="javascript:" data-toggle="tooltip"
						   data-tooltip="${order.description}">${fns:abbr(order.description,20)}</a>
					</td>
					<td>
					<a href="javascript:" data-toggle="tooltip"
							data-tooltip="${order.orderCondition.servicePoint.contactInfo1} ${order.orderCondition.servicePoint.contactInfo2}">${order.orderCondition.engineer.name}</a>
					</td>
					<shiro:hasPermission name="sd:order:engineeraccept">
						<td>
							<c:if test="${ order.canPlanOrder()}">
								<%--<a href="javascript:void(0);" onclick="Order.plan(${order.id},'${order.orderNo}');"><i class="icon-user"></i>派单</a>--%>
								<a href="javascript:void(0);" class="btn btn-mini btn-warning"
									onclick="Order.servicePointPlan('${order.id}','${order.orderNo}','${order.quarter}');">派单</a>
							</c:if>
					</shiro:hasPermission>
					<td>
					<c:if test="${fns:hasServicePoint(order.orderCondition.trackingFlag)}">
						<a href="javascript:void(0);"
						   title="<fmt:formatDate value="${order.orderCondition.trackingDate}" pattern="MM-dd" />${order.orderCondition.trackingMessage}">${fns:abbr(order.orderCondition.trackingMessage,10)}</a>
					</c:if>
						<c:if test="${ order.canTracking()}">
							<a href="javascript:void(0);" onclick="Order.trackingEnginner('${order.id}','${order.quarter}');" class="btn btn-mini btn-primary">进度</a>
						</c:if>
					</td>
					<td>
							<%--<c:if test="${order.orderCondition.status.value eq 80}">--%>
						<c:if test="${order.orderCondition.finishPhotoQty > 0 }">
							<a href="javascript:void(0);" onclick="Order.photolistNew('${order.id}','${order.quarter}',${fns:isNewOrder(order.orderNo)});" class="btn btn-mini btn-primary">查看</a>
						</c:if>
						<c:if test="${order.orderCondition.finishPhotoQty eq 0}">
							<a href="javascript:void(0);" onclick="Order.photolistNew('${order.id}','${order.quarter}',${fns:isNewOrder(order.orderNo)});" class="btn btn-mini btn-primary">上传</a>
						</c:if>
							<%--</c:if>--%>
					</td>
					<td>
						${order.orderCondition.kefu.name }
						<a style="padding-left: 20px;" target="_blank" href="http://wpa.qq.com/msgrd?v=3&uin=${order.orderCondition.kefu.qq}&site=qq&menu=yes"><img border="0" src="http://wpa.qq.com/pa?p=2:572202493:52" alt="点这发消息" title="联系客服QQ${order.orderCondition.kefu.qq}"/></a>
						<br>${order.orderCondition.kefu.phone }
					</td>
					<td>
						<c:forEach items="${order.items}" var="item"
							varStatus="i" begin="0">
							${item.brand }&nbsp;${item.product.name }&nbsp;${item.serviceType.name }&nbsp;&nbsp;数量:${item.qty }<br />
						</c:forEach>
					</td>
				</tr>
			</c:forEach>

		</tbody>
	</table>
	<c:if test="${rowcnt > 0}">
		<div class="pagination">${page}</div>
	</c:if>
</body>
</html>
