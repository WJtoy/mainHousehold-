<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE HTML>
<head>
	<title>余额明细</title>
	<meta name="decorator" content="default"/>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<%@include file="/WEB-INF/views/include/treetable.jsp" %>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<style type="text/css">
		.table thead th,.table tbody td {
			text-align: center;
			vertical-align: middle;
			BackColor: Transparent;
		}
	</style>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#treeTable").treeTable({expandLevel : 5});

            $("#btnSubmit").on("click", function(){
                $("#pageNo").val(1);
                var val = $("#customer\\.id").val();
                if (val == undefined || val.length == 0) {
                    layerInfo("请选择客户!", "信息提示");
                    return false;
                }

                layerLoading('正在查询，请稍等...',true);
                var url = "${ctx}/customer/fi/customerCurrency/list";
                $("#searchForm").attr("action",url);
                $("#searchForm").submit();
                return false;
            });
		});
		function openjBox(url,title,width,height){
			top.$.jBox.open("iframe:" + url , title, width, height,{top:'10px',buttons:{}, loaded:function(h){$("#jbox-iframe",h).prop("height","98%");} });
		}
	</script>
</head>
<body>
<sys:message content="${message}"/>
	<ul class="nav nav-tabs">
        <shiro:hasPermission name="customer:fi:customeroffline:view"><li><a href="${ctx}/customer/fi/customerCurrency/customerofflinelist">线下充值明细</a></li></shiro:hasPermission>
		<shiro:hasPermission name="customer:fi:blockamountlist:view"><li><a href="${ctx}/customer/fi/customerCurrency/blockamountlist">冻结明细</a></li></shiro:hasPermission>
		<li class="active"><a href="javascript:void(0);">余额明细</a></li>
	</ul>
	<c:set var="currentuser" value="${fns:getUser() }" />
	<form:form id="searchForm" modelAttribute="customerCurrency" action="${ctx}/customer/fi/customerCurrency/list" method="post" class="breadcrumb form-search">
		<form:hidden path="firstSearch" />
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<div>
		<label><span class="red">*</span>客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
		<c:choose>
			<c:when test="${currentuser.isCustomer()}">
				<input type="text" readonly="true" id="customer.name" name="customer.name" value="${currentuser.customerAccountProfile.customer.name}" />
				<input type="hidden" readonly="true" id="customer.id" name="customer.id" value="${currentuser.customerAccountProfile.customer.id}" />
			</c:when>
			<c:otherwise>
			<select id="customer.id" name="customer.id" class="input-small"
				style="width:200px;">
                <option value=""
                        <c:out value="${(empty customerId)?'selected=selected':''}" />>所有</option>
				<c:forEach items="${fns:getMyCustomerListFromMS()}" var="dict">
					<option value="${dict.id}"
						<c:out value="${(customerCurrency.customer.id eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
				</c:forEach>
			</select>

			</c:otherwise>
		</c:choose>
		<c:set var="actionTypeList" value="${fns:getDictExceptListFromMS('CustomerActionType','60,70,80,10,20,30,40')}" /><!-- 切换为微服务 -->
			&nbsp;&nbsp;&nbsp;
		<label>变更类型：</label>
		<select id="actionType" name="actionType" style="width:200px;">
			<option value="" <c:out value="${(empty actionType)?'selected=selected':''}" />>所有</option>
			<c:forEach items="${actionTypeList}" var="dict">
				<option value="${dict.value}" <c:out value="${(actionType eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
			</c:forEach>
		</select>
			&nbsp;&nbsp;&nbsp;
		<label>变更时间：</label><input id="createDate" name="createDate" type="text" readonly="readonly" style="width:120px;margin-left:4px" maxlength="20" class="input-small Wdate"
			value="<fmt:formatDate value='${customerCurrency.createDate}' pattern='yyyy-MM-dd'/>" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
			<label>~</label>&nbsp;&nbsp;&nbsp;<input id="updateDate" name="updateDate" type="text" readonly="readonly" style="width:120px" maxlength="20" class="input-small Wdate"
				value="<fmt:formatDate value='${customerCurrency.updateDate}' pattern='yyyy-MM-dd'/>"  onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
			&nbsp;&nbsp;&nbsp;
		<label>相关单号：</label>
		<form:input path="currencyNo" class="input-medium" maxlength="20" cssStyle="width: 186px"/>
			&nbsp;&nbsp;&nbsp;
			<input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/>
		</div>
	</form:form>
	<c:set value="" var="customerId"/>
	<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
		<tr>
			<th>序号</th>
			<th>变更类型</th>
			<th>支付方式</th>
			<th>变更前余额</th>
			<th>变更金额</th>
			<th>余额</th>
			<th>相关单号</th>
			<th>描述</th>
			<th>创建时间</th>
			<th>修改时间</th>
		<tbody>
		<c:forEach items="${page.list}" var="customerCurrency">
			<c:set var="index" value="${index+1}" />
			<c:if test="${!(customerCurrency.customer.id eq customerId) }">
				<c:set value="${customerCurrency.customer.id}" var="customerId"/>
				<tr>
					<td style="text-align: left;padding-left: 10px" colspan="5">${customerCurrency.customer.name}</td><%--切换为微服务--%>
					<td>${fns:formatNum(customerCurrency.customerFinance.balance)}</td>
					<td style="border-left:0"></td>
					<td style="border-left:0"></td>
					<td style="border-left:0"></td>
					<td style="border-left:0"></td>
				</tr>
			</c:if>
			<tr>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${customerCurrency.actionTypeName}</td> <%-- 切换为微服务 --%>
				<c:choose>
					<c:when test="${customerCurrency.actionType eq 10 || customerCurrency.actionType eq 20 }">
						<td>${customerCurrency.paymentTypeName}</td><%--切换为微服务--%>
					</c:when>
					<c:otherwise>
						<td></td>
					</c:otherwise>
				</c:choose>
				<td><fmt:formatNumber pattern="0.00">${customerCurrency.beforeBalance}</fmt:formatNumber> </td>
				<td><fmt:formatNumber pattern="0.00">${customerCurrency.amount}</fmt:formatNumber></td>
				<td><fmt:formatNumber pattern="0.00">${customerCurrency.balance}</fmt:formatNumber></td>
				<td>${customerCurrency.currencyNo}</td>
				<td>${customerCurrency.remarks}</td>
				<td>${fns:formatDate(customerCurrency.createDate, 'yyyy-MM-dd HH:mm:ss')}</td>
				<td>${fns:formatDate(customerCurrency.updateDate, 'yyyy-MM-dd HH:mm:ss')}</td>

			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
</html>
