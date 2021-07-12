<!DOCTYPE HTML>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>上游客户退补处理</title>
	<meta name="decorator" content="default"/>
	<%@include file="/WEB-INF/views/include/treetable.jsp" %>
	  <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<style type="text/css">
		.table thead th,.table tbody td {
			text-align: center;
			vertical-align: middle;
			BackColor: Transparent;
		}
	</style>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#treeTable").treeTable({expandLevel : 1});
			$("a#aSave").fancybox({
                fitToView : false,
                width  : 700,
                height  : 450,
                autoSize : false,
                closeClick : false,
                type  : 'iframe',
                openEffect : 'none',
                closeEffect : 'none'
            });
		});

		function setConfirm(id, no){

	        var submit = function (v, h, f) {
	            if (v == 'ok') {
	    	 		$(this).attr("disabled", "disabled");
	    	 		$("#aSave").attr("href", "${ctx}/fi/customerchargewriteoff/writeoffform?id="+id);
	    	 		$("#aSave").val(id);
	    	 		$("#aSave").click();
	            }
	            else if (v == 'cancel') {
	                // 取消
	            }

	            return true; //close
	        };

	        top.$.jBox.confirm('您确定要开立退补单吗？', '上游客户退补处理', submit);

	    }

		function openjBox(url,title,width,height){
			top.$.jBox.open("iframe:" + url , title, width, height,{top:'5%',buttons:{}	});
		}
	</script>
  </head>

  <body>
  	<ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">上游客户对帐列表</a></li>
	</ul>
	<form:form id="searchForm" action="${ctx}/fi/customerchargewriteoff/list" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<a id="aSave" type="hidden"></a>
		<div>
			<label>客　　户：</label>
			<%--<sys:treeselect id="customer" name="customerId" value="${customerId}" labelName="customerName" labelValue="${customerName}"
				title="客户" url="/md/customer/treeData" allowClear="true"/>--%>
			<select name="customerId" class="input-large" style="width:258px;">
				<option value="">所有</option>
				<c:forEach items="${fns:getMyCustomerListFromMS()}" var="customer">
					<option value="${customer.id}"  <c:out value="${customer.id==customerId ?'selected':''}"/>>${customer.name}</option>
				</c:forEach>
			</select>
			<label class="control-label">产品类别：</label>
            <sys:treeselect id="category" name="productCategoryId" value="${productCategoryId}" labelName="productCategoryName" labelValue="${productCategoryName}"
			title="公司" url="/md/productcategory/treeData" cssStyle="width:120px;" allowClear="true"/>
		</div>
		<div style="margin-top:8px">
			<label>对帐日期：</label>
			<input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:98px;" maxlength="20" class="input-small Wdate"
				value="${beginDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
			<label>~</label>&nbsp;&nbsp;&nbsp;<input id="endDate" name="endDate" type="text" readonly="readonly" style="width:98px" maxlength="20" class="input-small Wdate"
				value="${endDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
			<label>订单编号：</label>
			<input type = "text" id="orderNo" name="orderNo" value="${orderNo}" maxlength="30" class="input-small"/>
			<label>累计上门：</label>
			<input type = "text" id="serviceTimes" name="serviceTimes" value="${serviceTimes}" maxlength="3" style="width:66px;"/>
		</div>
		<div style="margin-top:8px">
			<label>下单日期：</label>
			<input id="createBeginDate" name="createBeginDate" type="text" readonly="readonly" style="width:98px;" maxlength="20" class="input-small Wdate"
				value="${createBeginDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>
			<label>~</label>&nbsp;&nbsp;&nbsp;<input id="createEndDate" name="createEndDate" type="text" readonly="readonly" style="width:98px" maxlength="20" class="input-small Wdate"
				value="${createEndDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>
			<label>完成日期：</label>
			<input id="closeBeginDate" name="closeBeginDate" type="text" readonly="readonly" style="width:120px;" maxlength="20" class="input-small Wdate"
				value="${closeBeginDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>
			<label>~</label>&nbsp;&nbsp;&nbsp;<input id="closeEndDate" name="closeEndDate" type="text" readonly="readonly" style="width:120px" maxlength="20" class="input-small Wdate"
				value="${closeEndDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>
			&nbsp;&nbsp;<input id="btnSubmit" class="btn btn-primary" value="查询" type="submit" onclick="return setPage();" />
		</div>
	</form:form>
	<sys:message content="${message}"/>
	<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th>序号</th>
				<th>客户编码</th>
				<th>客户名称</th>
				<th>订单编号</th>
				<th>下单时间</th>
				<th>完成时间</th>
				<th>上门次数</th>
				<th>状态</th>
				<th>对帐时间</th>
				<th>服务费</th>
				<th>快递费</th>
				<th>远程费</th>
				<th>配件费</th>
				<th>时效费</th>
				<th>加急费</th>
				<th>好评费</th>
				<th>其他</th>
				<th>合计</th>
				<th>操作</th>
			</tr>
		</thead>
		<tbody>
		<%int i=0; %>
		<c:set var="totalCharge" value="0" />
		<c:forEach items="${page.list}" var="customerCharge">
			<tr id="${customerCharge.id}" pId="0">
				<%i++;%>
				<td><%=i%></td>
				<td>${customerCharge.customer.code}</td>
				<td>${customerCharge.customer.name}</td>
				<td><a href="javascript:void(0);" onclick="Order.viewOrderDetail('${customerCharge.orderId}');"  ><abbr title="点击查看订单详情">${customerCharge.orderNo}</abbr></a></td>
				<td><fmt:formatDate value="${customerCharge.condition.orderCreateDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
				<td><fmt:formatDate value="${customerCharge.condition.orderCloseDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
				<td style="color:blue;">${customerCharge.serviceTimes}</td>
				<td>
					<c:choose>
						<c:when test="${customerCharge.status eq 10}">
							<span class="label status_Completed">
						</c:when>
						<c:when test="${customerCharge.status eq 20}">
							<span class="label status_Returned">
						</c:when>
					<%--</c:choose>${fns:getDictLabel(customerCharge.status, 'customer_charge_status', '')}</span></td>--%>
					</c:choose>${customerCharge.statusName}</span></td> <%-- 切换为微服务 --%>
				<td><fmt:formatDate value="${customerCharge.createDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
				<td><c:if test="${customerCharge.serviceCharge ne 0}">${customerCharge.serviceCharge}</c:if></td>
				<td><c:if test="${customerCharge.expressCharge ne 0}">${customerCharge.expressCharge}</c:if></td>
				<td><c:if test="${customerCharge.travelCharge ne 0}">${customerCharge.travelCharge}</c:if></td>
				<td><c:if test="${customerCharge.materialCharge ne 0}">${customerCharge.materialCharge}</c:if></td>
				<td><c:if test="${customerCharge.timeLinessCharge ne 0}">${customerCharge.timeLinessCharge}</c:if></td>
				<td><c:if test="${customerCharge.urgentCharge ne 0}">${customerCharge.urgentCharge}</c:if></td>
				<td><c:if test="${customerCharge.praiseFee ne 0}">${customerCharge.praiseFee}</c:if></td>
				<td><c:if test="${customerCharge.otherCharge ne 0}">${customerCharge.otherCharge}</c:if></td>
				<td style="color:green"><B>${customerCharge.serviceCharge+customerCharge.expressCharge+customerCharge.travelCharge+
									customerCharge.materialCharge+customerCharge.timeLinessCharge+customerCharge.urgentCharge+
									customerCharge.praiseFee+customerCharge.otherCharge}</B></td>
				<td>
					<c:if test="${customerCharge.chargeOrderType eq 0}">
					<input type="Button" id="${customerCharge.id}" class="btn btn-mini btn-danger" value="退补" onclick="setConfirm('${customerCharge.id}','${customerCharge.orderNo}')"/>
					</c:if>
				</td>
				<c:set var="totalCharge" value="${totalCharge+customerCharge.serviceCharge+customerCharge.expressCharge+customerCharge.travelCharge+
													customerCharge.materialCharge+customerCharge.timeLinessCharge+customerCharge.urgentCharge+
													customerCharge.praiseFee+customerCharge.otherCharge}" />
			</tr>
			<c:if test="${customerCharge.chargeOrderType eq 1}">
			<tr>
				<td></td>
				<td style="background-color: #f2dede;">
					退补描述
				</td>
				<td colspan="16" style="background-color: #f2dede;">${customerCharge.remarks}</td>
				<td></td>
			</tr>
			</c:if>
		</c:forEach>
		<tr>
			<td style="text-align:right;" colspan="9" ><B>合计</B></td>
			<td style="text-align:right;color:green;padding-right:15px;" colspan="9"><B>${fns:formatNum(totalCharge)}</B></td>
			<td></td>
		</tr>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
  </body>
</html>
