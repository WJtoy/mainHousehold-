<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<head>
	<title>客评项目管理</title>
	<meta name="decorator" content="default"/>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<%@include file="/WEB-INF/views/include/treetable.jsp" %>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#treeTable").treeTable({expandLevel : 5});
		});
	</script>
</head>
<body>
	<ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">客评项目列表</a></li>
		<shiro:hasPermission name="md:grade:edit"><li><a href="${ctx}/md/grade/form">客评项目添加</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="grade" action="${ctx}/md/grade" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<label>名称 ：</label><form:input path="name" htmlEscape="false" maxlength="50" class="input-small"/>
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询"/>
	</form:form>
	<sys:message content="${message}"/>
	<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
		<tr>
			<th>名称</th>
			<th>分值</th>
			<th>关联字典类型</th>
			<th>描述</th>
			<th>排序</th>
			<shiro:hasPermission name="md:grade:edit"><th>操作</th></shiro:hasPermission>
		</tr>
		</thead>
		<tbody>
		<c:forEach items="${page.list}" var="grade">
			<tr id="${grade.id}" pId="0">
				<td>${grade.name}</td>
				<td>${grade.point}</td>
				<td>${grade.dictType}</td>
				<td>${grade.remarks}</td>
				<td>${grade.sort}</td>
				<shiro:hasPermission name="md:grade:edit"><td>
    				<a href="${ctx}/md/grade/form?type=grade&id=${grade.id}">修改</a>
					<a href="${ctx}/md/grade/delete?id=${grade.id}" onclick="return confirmx('确认要删除该客评项目及其评价标准吗？', this.href)">删除</a>
    				<a href="<c:url value='${fns:getAdminPath()}/md/grade/itemform?type=item&gradeId=${grade.id}'></c:url>">添加客评标准</a>
				</td></shiro:hasPermission>
			</tr>
			<c:forEach items="${grade.itemList}" var="item">
			<c:if test="${!(item.delFlag eq 1)}">
			<tr id="${item.id}" pId="${grade.id}">
				<td>${item.remarks}</td>
				<td>${item.point}</td>
				<td>${item.dictValue}</td>
				<td></td>
				<td></td>
				<shiro:hasPermission name="md:grade:edit"><td>
    				<a href="${ctx}/md/grade/itemform?type=item&gradeId=${grade.id}&id=${item.id}">修改</a>
					<a href="${ctx}/md/grade/deleteitem?type=item&gradeId=${grade.id}&id=${item.id}" onclick="return confirmx('确认要删除该客评标准吗？', this.href)">删除</a>
				</td></shiro:hasPermission>
			</tr>
			</c:if>
			</c:forEach>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
</html>
