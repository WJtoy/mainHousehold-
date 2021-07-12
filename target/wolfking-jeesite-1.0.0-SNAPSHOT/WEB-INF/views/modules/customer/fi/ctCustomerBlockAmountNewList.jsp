<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE HTML>
<head>
	<title>冻结查询</title>
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
                var url = "${ctx}/customer/fi/customerCurrency/blockamountlist";
                $("#searchForm").attr("action",url);
                $("#searchForm").submit();
                return false;
            });
		});
	</script>
</head>
<body>
<sys:message content="${message}"/>
	<ul class="nav nav-tabs">
        <shiro:hasPermission name="customer:fi:customeroffline:view"><li><a href="${ctx}/customer/fi/customerCurrency/customerofflinelist">线下充值明细</a></li></shiro:hasPermission>
		<li class="active"><a href="javascript:void(0);">冻结明细</a></li>
		<li><a href="${ctx}/customer/fi/customerCurrency/list">余额明细</a></li>
	</ul>
	<c:set var="currentuser" value="${fns:getUser() }" />
	<form:form id="searchForm" modelAttribute="searchEntity" action="${ctx}/customer/fi/customerCurrency/blockamountlist" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<form:hidden path="firstSearch" />
		<div>
			<label class="control-label"><span class="red">*</span>客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
			<c:choose>
				<c:when test="${currentuser.isCustomer()}">
					<input type="text" readonly="true" id="customer.name" name="customer.name" value="${currentuser.customerAccountProfile.customer.name}" />
					<input type="hidden" readonly="true" id="customer.id" name="customer.id" value="${currentuser.customerAccountProfile.customer.id}" />
				</c:when>
				<c:otherwise>
					<select id="customer.id" name="customer.id" class="input-small"
							style="width:200px;">
						<option value=""
								<c:out value="${(empty searchEntity.customer.id)?'selected=selected':''}" />>所有</option>
						<c:forEach items="${fns:getMyCustomerListFromMS()}" var="dict">
							<option value="${dict.id}"
									<c:out value="${(searchEntity.customer.id eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
						</c:forEach>
					</select>

				</c:otherwise>
			</c:choose>
			&nbsp;&nbsp;

			<c:set var="currencyTypeList" value="${fns:getDictExceptListFromMS('BlockCurrencyType','')}" /><!-- 切换为微服务 -->
			<label>变更类型：</label>
			<select id="currencyType" name="currencyType" style="width:200px;">
				<option value="" <c:out value="${(empty searchEntity.currencyType)?'selected=selected':''}" />>所有</option>
				<c:forEach items="${currencyTypeList}" var="dict">
					<option value="${dict.value}" <c:out value="${(searchEntity.currencyType eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
				</c:forEach>
			</select>
			&nbsp;&nbsp;
			<label>变更时间：</label>
			<input id="createDate" name="createDate" type="text" readonly="readonly" style="width:120px;margin-left:4px" maxlength="20" class="input-small Wdate"
				   value="<fmt:formatDate value='${searchEntity.createDate}' pattern='yyyy-MM-dd' type='date'/>" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
			<label>~</label>
			<input id="updateDate" name="updateDate" type="text" readonly="readonly" style="width:125px" maxlength="20" class="input-small Wdate"
				   value="<fmt:formatDate value='${searchEntity.updateDate}' pattern='yyyy-MM-dd' type='date'/>" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
			&nbsp;&nbsp;

			<label>相关单号：</label>
			<form:input path="currencyNo" class="input-medium" maxlength="20" cssStyle="width: 186px" />
			&nbsp;&nbsp;
			<input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/>
		</div>
	</form:form>
	<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
		<tr>
			<th>序号</th>
			<th>变更类型</th>
			<th>冻结金额</th>
			<th>相关单号</th>
			<th>描述</th>
			<th>创建时间</th>
			<th>修改时间</th>
		<tbody>
		<c:forEach items="${page.list}" var="item">
			<c:set var="index" value="${index+1}" />
			<c:if test="${!(item.customer.id eq customerId) }">
				<c:set var="customerId" value="${item.customer.id}"/>
				<tr id="${item.customer.id}" pId="">
					<td style="text-align: left;padding-left: 10px" colspan="2">${item.customer.name}</td>
					<td>${fns:formatNum(item.customerFinance.blockAmount)}</td>
					<td style="border-left:0"></td>
					<td style="border-left:0"></td>
					<td style="border-left:0"></td>
					<td style="border-left:0"></td>
				</tr>
			</c:if>
			<tr>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${item.currencyTypeName}</td><!-- 切换为微服务 -->
				<td><fmt:formatNumber pattern="0.00">${item.amount}</fmt:formatNumber></td>
				<td>${item.currencyNo}</td>
				<td>${item.remarks}</td>
				<td>${fns:formatDate(item.createDate, 'yyyy-MM-dd HH:mm:ss')}</td>
				<td>${fns:formatDate(item.updateDate, 'yyyy-MM-dd HH:mm:ss')}</td>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
</html>
