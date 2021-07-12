<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>客戶管理</title>
	<meta name="decorator" content="default" />
	<style type="text/css">
	.table thead th,.table tbody td {
		text-align: center;
		vertical-align: middle;
		BackColor: Transparent;
	}
	</style>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
</head>
<body>
	<ul class="nav nav-tabs">
		<li class="active"><a href="${ctx}/md/customer">客戶列表</a>
		</li>
		<shiro:hasPermission name="md:customer:edit">
			<li><a href="${ctx}/md/customer/form?sort=10">客戶添加</a>
			</li>
		</shiro:hasPermission>
		<%--<shiro:hasPermission name="md:customer:edit">--%>
			<%--<li><a href="${ctx}/md/customer/approvelist">客户审核</a>--%>
			<%--</li>--%>
		<%--</shiro:hasPermission>--%>
	</ul>
	<form:form id="searchForm" modelAttribute="customer" action="${ctx}/md/customer" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
		<label>代码：</label>
		<form:input path="code" htmlEscape="false" maxlength="30" />
		<label>名称：</label>
		<form:input path="name" htmlEscape="false" maxlength="60" class="required" />
		&nbsp;&nbsp;&nbsp;
		<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}" />
	<table id="contentTable"
		class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th>序号</th>
				<th>代码</th>
				<th>名称</th>
				<th>负责人</th>
				<th>手机</th>
				<th>邮件</th>
				<th>技术人员</th>
				<th>技术人员电话</th>
				<th>默认品牌</th>
				<th>可下单</th>
				<th>短信发送</th>
				<th style="width:20%">描述</th>
				<shiro:hasPermission name="md:customer:edit">
					<th>操作</th>
				</shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
			<c:forEach items="${page.list}" var="customer">
				<c:set var="index" value="${index+1}" />
				<tr>
					<td>${index+(page.pageNo-1)*page.pageSize}</td>
					<td>${customer.code}</td>
					<td>${customer.name}</td>
					<td>${customer.master}</td>
					<td>${customer.phone}</td>
					<td>${customer.email}</td>
					<td>${customer.technologyOwner}</td>
					<td>${customer.technologyOwnerPhone}</td>
					<td>${customer.defaultBrand}</td>
					<td>${customer.effectFlag==0?'否':'是'}</td>
					<td>${customer.shortMessageFlag==1?'发送':'不发送'}</td>
					<td><a href="javascript:void(0);" title="${customer.remarks}">${fns:abbr(customer.remarks,40)}</a></td>
					<shiro:hasPermission name="md:customer:edit">
						<td><a href="${ctx}/md/customer/form?id=${customer.id}">修改</a>
							<shiro:hasPermission name="md:customer:detelete">
							<a href="${ctx}/md/customer/delete?id=${customer.id}"
							onclick="return confirmx('确认要删除该客戶吗？', this.href)">删除</a>
							</shiro:hasPermission>
						</td>
					</shiro:hasPermission>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
</html>
