<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>我的订单-已完成(网点)</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default" />
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
	<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
	<%@include file="/WEB-INF/views/include/treetable.jsp" %>
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
		$("#engineerName").val("");
		$("#isEngineerInvoiced").attr('checked', false);
		<%--$("#beginDate").val("${fns:getPastDate(-60,'yyyy-MM-dd')}");--%>
		<%--$("#endDate").val("${fns:getDate('yyyy-MM-dd')}");--%>
		$("#completeBeginDate").val();
		$("#completeEndDate").val();
		$("#phone").val("");
		$("#areaIdsId").val("");
		$("#areaIdsName").val("");
		search();
		//page(1, 10);
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
		<li><a href="${ctx}/sd/order/engineer/waitingAppointmentList" title="待预约的订单列表">待预约</a></li>
		<li><a href="${ctx}/sd/order/engineer/processingList" title="处理中的订单">处理中</a></li>
		<li><a href="${ctx}/sd/order/engineer/appointedList" title="预约中的订单列表">已预约</a></li>
		<li><a href="${ctx}/sd/order/engineer/waitingAccessoryList" title="等配件的订单列表">等配件</a></li>
		<li><a href="${ctx}/sd/order/engineer/pendingList" title="停滞的订单列表">停滞</a></li>
		<li class="active"><a href="javascript:void(0);">完成单</a></li>
		<li><a href="${ctx}/sd/order/engineer/returnlist" title="退单列表">退单</a></li>
		<li><a href="${ctx}/sd/order/engineer/allList" title="所有订单列表">所有</a></li>
	</ul>
	<c:set var="currentuser" value="${fns:getUser() }" />
	<form:form id="searchForm" modelAttribute="order"
		action="${ctx}/sd/order/engineer/finishlist" method="post"
		class="breadcrumb form-search">
		<input id="searchType" name="searchType" type="hidden"
			value="finished" />
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
		<input id="pageSize" name="pageSize" type="hidden"
			value="${page.pageSize}" />
		<div>
			<label>订单号：</label>&nbsp;<input type=text class="input-small"
				id="orderNo" name="orderNo" value="${order.orderNo}" maxlength="20" />
			<label>联系人：</label><input
				type=text class="input-small" id="userName" name="userName"
				value="${order.userName}" maxlength="20" />
			<label>电 话：</label><input type=text
				class="input-small" id="servicePhone" name="servicePhone" value="${order.servicePhone}" maxlength="20" />
			 <label>安维姓名：</label><input type=text
				class="input-small" id="engineerName" name="engineer.name" value="${order.engineer.name}" maxlength="20" />
			<c:set var="statusList" value="${fns:getDictListFromMS('engineer_charge_status')}" /><%-- 切换为微服务 --%>
			<label>状态：  </label><select id="engineerChargeStatus" name="engineerChargeStatus" style="width:135px;">
			<option value="" <c:out value="${(order.engineerChargeStatus == null)?'selected=selected':''}" />>所有</option>
			<c:forEach items="${statusList}" var="dict">
				<option value="${dict.value}" <c:out value="${(order.engineerChargeStatus.value eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
			</c:forEach>
		</select>
		</div>
		<div style="margin-top: 8px;">
			<label>接单时间：</label><input id="acceptBeginDate" name="acceptBeginDate"
				type="text" readonly="readonly" style="width:95px;margin-left:4px"
				maxlength="20" class="input-small Wdate" value="${fns:formatDate(order.acceptBeginDate,'yyyy-MM-dd')}"
				onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});" />
			<label>~</label>&nbsp;&nbsp;&nbsp;<input id="acceptEndDate" name="acceptEndDate"
				type="text" readonly="readonly" style="width:95px" maxlength="20"
				class="input-small Wdate" value="${fns:formatDate(order.acceptEndDate,'yyyy-MM-dd')}"
				onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});" />
			&nbsp;&nbsp; <label>完成时间：</label><input id="completeBegin"
				name="completeBegin" type="text" readonly="readonly"
				style="width:95px;margin-left:4px" maxlength="20"
				class="input-small Wdate" value="${fns:formatDate(order.completeBegin,'yyyy-MM-dd')}"
				onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});" />
			<label>~</label>&nbsp;&nbsp;&nbsp; <input id="completeEnd"
				name="completeEnd" type="text" readonly="readonly"
				style="width:95px" maxlength="20" class="input-small Wdate"
				value="${fns:formatDate(order.completeEnd,'yyyy-MM-dd')}"
				onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});" />
			&nbsp;&nbsp;
			<input id="btnSubmit" class="btn btn-primary"  type="submit" onclick="return setPage();" value="查询" /> &nbsp;&nbsp;
			<input id="btnClearSearch" class="btn btn-primary" type="button" value="清除条件" />
		</div>
	</form:form>

	<tags:message content="${message}" />
	<c:set var="rowNumber" value="0" />
	<table id="contentTable" class="table table-bordered table-condensed table-striped"
		style="table-layout:fixed;" cellspacing="0" width="100%">
		<thead>
			<tr>
				<th width="30">序号</th>
				<th width="110">单号</th>
				<th width="80">接单时间</th>
				<th width="80">完成时间</th>
				<th width="100">联系信息</th>
				<th width="200">地址</th>
				<th width="140">服务描述</th>
				<th width="70">安维姓名</th>
				<th width="100">费用明细</th>
				<th width="80">转入余额时间</th>
				<th width="60">完成照片</th>
				<th width="120">客服信息</th>
				<th width="160">实际服务明细</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach items="${page.list}" var="order">
				<c:set var="rowNumber" value="${rowNumber+1}" />
				<tr>
					<td>${rowNumber}</td>
					<td><a href="javascript:void(0);">
						<abbr>${order.orderNo}</abbr> </a><br>
							<c:choose>
								<c:when test="${order.orderCondition.status.value eq 50}"><label title="安维手机APP上完成，待客服回访，客评">待回访</label> </c:when>
								<c:when test="${order.orderCondition.status.value eq 80 && order.orderCondition.chargeFlag ==0}">
									<a href="javascript:" data-toggle="tooltip" data-tooltip="待财务审核" style="cursor: pointer;">
										<span class="label status_60">${order.orderCondition.status.label}</span>
									</a>
								</c:when>
								<c:otherwise><span class="label status_${order.orderCondition.status.value}">${order.orderCondition.status.label}</span></c:otherwise>
							</c:choose>
					</td>
					<td><fmt:formatDate value="${order.orderStatus.planDate}" pattern="yyyy-MM-dd" />
					</td>
					<td><fmt:formatDate value="${order.orderCondition.closeDate}" pattern="yyyy-MM-dd" />
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
					<td>${order.orderCondition.engineer.name}</td>
					<td>
							<a style="color: green;"><b>${order.orderFee.engineerTotalCharge}</b></a>元
							<br>
							<c:if test="${order.orderFee.engineerServiceCharge ne 0}">服务费：${order.orderFee.engineerServiceCharge}<br></c:if>
							<c:if test="${order.orderFee.engineerMaterialCharge ne 0}">配件费：${order.orderFee.engineerMaterialCharge}<br></c:if>
							<c:if test="${order.orderFee.engineerTravelCharge ne 0}">远程费：${order.orderFee.engineerTravelCharge}<br></c:if>
							<c:if test="${order.orderFee.engineerOtherCharge ne 0}">其他：${order.orderFee.engineerOtherCharge}<br></c:if>

					</td>
					<td><fmt:formatDate value="${order.orderStatus.engineerInvoiceDate}" pattern="yyyy-MM-dd" /></td>
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
						<a style="padding-left: 20px;" target="_blank" href="http://wpa.qq.com/msgrd?v=3&uin=${order.orderCondition.kefu.qq}&site=qq&menu=yes"><img border="0" src="http://wpa.qq.com/pa?p=2:572202493:52" alt="点这发消息" title="联系客服QQ ${order.orderCondition.kefu.qq}"/></a>
						<br>${order.orderCondition.kefu.phone }
					</td>
					<td>
						<c:forEach items="${order.detailList}" var="detail" varStatus="i" begin="0">
							${detail.brand}&nbsp;
							${detail.product.name }&nbsp;${detail.serviceType.name }&nbsp;&nbsp;数量:${detail.qty}<br />
						</c:forEach>
					</td>
				</tr>
			</c:forEach>

		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
</html>
