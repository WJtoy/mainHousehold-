<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
		<title>异常处理</title>
		<%@ include file="/WEB-INF/views/include/head.jsp" %>
		<meta name="decorator" content="default"/>
		<%@include file="/WEB-INF/views/include/treetable.jsp" %>
		<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
		<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
		<%@ include file="/WEB-INF/views/include/WdateLimitPicker.jsp" %>
	<style type="text/css">
		.table thead th {
			text-align: center;
    		vertical-align: middle;
		}
	</style>
	<script type="text/javascript">
		Order.rootUrl = "${ctx}";
		$(document).ready(function() {
			//oneYearDatePicker('completeBegin','completeEnd',false);
            customerLimitDatePicker('completeBegin','completeEnd',6,false);
			$("#treeTable").treeTable({expandLevel : 5});
		});

	</script>
</head>

  <body>
  <ul id="navtabs" class="nav nav-tabs">
	  <li class="active"><a href="javascript:void(0);" title="异常单处理">异常处理</a></li>
  </ul>
	<form:form id="searchForm" modelAttribute="order" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<div class="alert alert-block" style="padding-top:6px;padding-bottom:6px;">
			注:当用订单编号和用户电话进行搜索查询时，不受当前时间、产品等其他条件的限制。
		</div>
		<div>
			<label class="label-search">用户：</label>
			<input type=text class="input-mini" id="userName" name="userName" value="${order.userName}" placeholder="用户姓名" maxlength="20" />
			<label>用户电话：</label>
			<input type=text class="input-small" id="servicePhone" name="servicePhone" value="${order.servicePhone}" placeholder="用户电话 或 实际联络电话" maxlength="20" />
			<label>产品名称：</label>
			<input type = "text" id="productName" name="productName" value="${order.productName}" class="input-small" placeholder="产品名称" maxlength="30" />
			<label>数量：</label>
			<input type = "text" id="productQty" name="productQty" value="${order.productQty}" maxlength="3" class="input-small" style="width:33px;"/>
		</div>
		<div style="margin-top:8px">
			<label class="label-search">客户：</label><sys:treeselect id="customer" name="customer.id" value="${order.customer.id}" labelName="customer.name" labelValue="${order.customer.name}"
				title="客户" url="/md/customer/treeData" cssClass="input-small" allowClear="true"/>
			<label>订单编号：</label>
			<input type = "text" id="orderNo" name="orderNo" value="${order.orderNo}" class="input-small" placeholder="订单编号" maxlength="20" />
			<label class="control-label">产品类别：</label>
            <sys:treeselect id="category" name="category.id" value="${order.category.id}" labelName="category.name" labelValue="${order.category.name}"
			title="品类" url="/md/productcategory/treeData" cssClass="input-small" allowClear="true"/>
		</div>
		<div style="margin-top:8px">
			<label class="label-search">订单来源：</label>
			<form:select path="dataSource" class="input-small" style="width:125px;">
				<form:option value="0" label="所有"/>
				<form:options items="${fns:getDictListFromMS('order_data_source')}" itemLabel="label" itemValue="value" htmlEscape="false"/>
			</form:select>
			<label>完成日期：</label>
			<input id="completeBegin" name="completeBegin" type="text" readonly="readonly" style="width:98px;margin-left:4px" maxlength="20" class="input-small Wdate"
				value="${fns:formatDate(order.completeBegin,'yyyy-MM-dd')}" />
			<label>~</label>&nbsp;&nbsp;&nbsp;
			<input id="completeEnd" name="completeEnd" type="text" readonly="readonly" style="width:98px" maxlength="20" class="input-small Wdate"
				value="${fns:formatDate(order.completeEnd,'yyyy-MM-dd')}" />
				<c:set var="statusList" value="${fns:getDictListFromMS('order_status')}" /><%--切换为微服务--%>
			&nbsp;&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="top.$.jBox.tip('请稍候...', 'loading');return setPage();" value="查询" />
		</div>
	</form:form>
	<sys:message content="${message}"/>
	<table id="treeTable" class="table table-striped table-bordered table-condensed">
		<thead>
			<tr style="vertical-align: middle;">
				<th rowspan="2" width="30px">序号</th>
				<th rowspan="2" width="0px">订单单号</th>
				<th rowspan="2" width="50px">来源</th>
				<%--<th rowspan="2">客户<br/>编码</th>--%>
				<th rowspan="2">客户名称</th>
				<th rowspan="2">客服</th>
				<th rowspan="2">累计<br/>上门</th>
				<th rowspan="2">状态</th>
				<th rowspan="2">完成日期</th>

				<th colspan="6">应付款</th>
				<th rowspan="2">操作</th>
			</tr>
			<tr>
				<th>服务费</th>
				<th>拆机费</th>
				<th>远程费</th>
				<th>配件费</th>
				<th>其他</th>
				<th>合计</th>
			</tr>
		</thead>
		<tbody>
		<%int i=0; %>
		<c:set var="inTotalCharge" value="0" />
		<c:set var="outTotalCharge" value="0" />
		<c:set var="rowcnt" value="${page.list.size()}"/>
		<c:forEach items="${page.list}" var="order">
			<tr id="${order.id}" pId="0">
			<%i++;%>

				<td><%=i%>  <input type="hidden" id="cbox<%=i%>" value="${order.id}" name="checkedRecords"/>  </td>
				<td>
					<a href="javascript:void(0);" onclick="Order.showOrderDetail('${order.id}','${order.quarter}');" ><abbr title="点击查看订单详情">${order.orderNo}</abbr></a>
				</td>
				<td>${order.dataSource.label}</td>
				<%--<td>${order.orderCondition.customer.code}</td>--%>
				<td>${order.orderCondition.customer.name}</td>
				<td>${order.orderCondition.kefu.name}</td>
				<td style="color:blue">${order.orderCondition.serviceTimes}</td>
				<td>${order.orderCondition.status.label}</td>
				<td><fmt:formatDate value="${order.orderCondition.closeDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>

				<td>${order.orderFee.engineerServiceCharge}</td>
				<td>${order.orderFee.engineerExpressCharge}</td>
				<td>${order.orderFee.engineerTravelCharge}</td>
				<td>${order.orderFee.engineerMaterialCharge}</td>
				<td>${order.orderFee.engineerOtherCharge}</td>
				<td style="color:red"><B>${order.orderFee.engineerTotalCharge}</B></td>
				<td rowspan="${order.detailList.size()+1}">
					<shiro:hasPermission name="sd:pending:edit">
					<div class="btn-group">
						<a class="btn btn-primary dropdown-toggle" data-toggle="dropdown" href="#">操作
						<span class="caret"></span>
  						</a>
					  <ul class="dropdown-menu">
					  	<li><a href="javascript:void(0);" onclick="Order.addPending('${order.id}','${order.quarter}');" >修改</a></li>
						<li><a href="javascript:void(0);" onclick="Order.pendintTracking('${order.id}','${order.quarter}');" >进度</a></li>
					  </ul>
					</div>
					</shiro:hasPermission>
				</td>
			</tr>
			<c:forEach items="${order.detailList}" var="detail">
				<tr id="${detail.id}" pId="${order.id}">

					<td colspan="8">第<span style="color:blue">${detail.serviceTimes}</span>次&nbsp;&nbsp;
					服务类型:<span style="color:blue">${detail.serviceType.name}</span>&nbsp;&nbsp;
					数量:<span style="color:blue">${detail.qty}</span>&nbsp;&nbsp;
					产品:${detail.product.name}&nbsp;&nbsp;
					型号/规格:${detail.product.model}&nbsp;&nbsp;
					安维人:${detail.engineer.name}
					</td>

					<c:set var="inTotalCharge" value="${inTotalCharge+detail.charge+detail.materialCharge+detail.otherCharge}" />
					<td>${detail.engineerServiceCharge}</td>
					<td>${detail.engineerExpressCharge}</td>
					<td>${detail.engineerTravelCharge}</td>
					<td>${detail.engineerMaterialCharge}</td>
					<td>${detail.engineerOtherCharge}</td>
					<td><B>${detail.engineerServiceCharge+detail.engineerTravelCharge+detail.engineerMaterialCharge+detail.engineerOtherCharge}</B></td>
					<c:set var="outTotalCharge" value="${outTotalCharge+detail.engineerServiceCharge+detail.engineerTravelCharge+detail.engineerMaterialCharge+detail.engineerOtherCharge}" />
				</tr>
			</c:forEach>
		</c:forEach>
		<tr>
			<td style="text-align:right;" colspan="8" ><B>合计</B></td>

			<td style="text-align:right;color:red;padding-right:15px;" colspan="7"><B>${outTotalCharge}</B></td>
		</tr>
		</tbody>
	</table>
	<c:if test="${rowcnt > 0}">
		<div class="pagination">${page}</div>
	</c:if>
  </body>
</html>
