<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>客户产品型号</title>
	<meta name="decorator" content="default" />
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
</head>

<body>
	<ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">列表</a></li>
		<shiro:hasPermission name="md:customerproductmodel:edit">
			<li><a href="${ctx}/md/customerproductmodel/form">添加</a></li>
		</shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="customerProductModel" action="${ctx}/md/customerproductmodel/list" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
		<input id="pageSize" name="pageSize" type="hidden"
			value="${page.pageSize}" />&nbsp;
		<label>客户：</label>
		<select id="customerId" name="customer.id" class="input-small" style="width:250px;">
			<option value=""
					<c:out value="${(empty customerProductModel.customer.id)?'selected=selected':''}" />>所有</option>
			<c:forEach items="${fns:getMyCustomerList()}" var="dict">
				<option value="${dict.id}"
						<c:out value="${(customerProductModel.customer.id eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
			</c:forEach>
		</select>
		&nbsp;
		<label>产品：</label>
		<sys:treeselect id="product" name="product.id" value="${customerProductModel.product.id}" labelName="product.name" labelValue="${customerProductModel.product.name}"
						title="产品" url="/md/product/treeData" cssClass="input-small" allowClear="true" cssStyle="width:250px;"/>
		&nbsp;
		<label>型号:</label>
		<form:input path="customerModel" htmlEscape="false" maxlength="50" class="input-small"/>
		<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="top.$.jBox.tip('正在查询,请稍候...', 'loading');return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}" />
	<table id="treeTable"
		class="table table-bordered table-hover table-hover table-condensed ">
		<thead>
			<tr>
				<th width="30">序号</th>
				<th width="200">客户</th>
				<th width="150">产品</th>
				<th>型号</th>
				<th>客户产品名称</th>
				<th>备注</th>
				<shiro:hasPermission name="md:customerproductmodel:edit">
					<th width="200">操作</th>
				</shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
			<c:set var="index" value="0"></c:set>
			<c:forEach items="${page.list}" var="entity">
				<tr>
					<c:set var="index" value="${index+1}"></c:set>
					<td>${index+(page.pageNo-1)*page.pageSize}</td>
					<td>${entity.customer.name}</td>
					<td>${entity.product.name}</td>
					<td><a href="${ctx}/md/customerproductmodel/form?id=${entity.id}&customer.name=${fns:urlEncode(entity.customer.name)}&product.name=${fns:urlEncode(entity.product.name)}">${entity.customerModel}</a></td>
					<td>${entity.customerProductName}</td>
					<td>
					<c:choose>
						<c:when test="${fn:length(entity.remarks)>40}">
							<a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${entity.remarks}">${fns:abbr(entity.remarks,80)}</a>
						</c:when>
						<c:otherwise>
							${entity.remarks}
						</c:otherwise>
					</c:choose>
					</td>
					<shiro:hasPermission name="md:customerproductmodel:edit">
						<td><a href="${ctx}/md/customerproductmodel/form?id=${entity.id}&customer.name=${fns:urlEncode(entity.customer.name)}&product.name=${fns:urlEncode(entity.product.name)}">修改</a>
							<a style="margin-left: 6px;" href="${ctx}/md/customerproductmodel/delete?id=${entity.id}"
							onclick="return confirmx('确认要删除吗？', this.href)">删除</a>
						</td>
					</shiro:hasPermission>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
</html>
