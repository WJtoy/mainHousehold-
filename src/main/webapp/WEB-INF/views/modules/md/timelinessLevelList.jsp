<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>加急等级费用</title>
	<meta name="decorator" content="default" />
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
</head>

<body>
	<ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">列表</a></li>
		<shiro:hasPermission name="md:timelinesslevel:edit">
			<li><a href="${ctx}/md/timelinesslevel/form">添加</a></li>
		</shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="timelinessLevel" action="${ctx}/md/timelinesslevel/list" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
		<input id="pageSize" name="pageSize" type="hidden"
			value="${page.pageSize}" />
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="top.$.jBox.tip('正在查询,请稍候...', 'loading');return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}" />
	<table id="treeTable"
		class="table table-bordered table-hover table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<th width="200">描述</th>
				<th width="100">收取</th>
				<th width="100">支付</th>
				<th width="100">排序</th>
				<th>备注</th>
				<shiro:hasPermission name="md:timelinesslevel:edit">
					<th width="100">操作</th>
				</shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
			<c:set var="index" value="0"></c:set>
			<c:forEach items="${page.list}" var="entity">
				<tr>
					<c:set var="index" value="${index+1}"></c:set>
					<td>${index+(page.pageNo-1)*page.pageSize}</td>
					<td>${entity.name}</td>
					<td style="color: green;"><b><fmt:formatNumber value="${entity.chargeIn}"
							pattern="0.00"></fmt:formatNumber></b></td>
					<td style="color: red;"><b><fmt:formatNumber value="${entity.chargeOut}"
																 pattern="0.00"></fmt:formatNumber></b>
					</td>
					<td>${entity.sort}</td>
					<td>${entity.remarks}</td>
					<shiro:hasPermission name="md:timelinesslevel:edit">
						<td><a href="${ctx}/md/timelinesslevel/form?id=${entity.id}">修改</a>
							<a href="${ctx}/md/timelinesslevel/delete?id=${entity.id}"
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
