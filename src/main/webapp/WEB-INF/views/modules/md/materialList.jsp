<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>配件</title>
	<meta name="decorator" content="default" />
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
</head>

<body>
	<ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">配件列表</a></li>
		<shiro:hasPermission name="md:material:edit">
			<li><a href="${ctx}/md/material/form">配件添加</a></li>
		</shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="material" action="${ctx}/md/material/list" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
		<input id="pageSize" name="pageSize" type="hidden"
			value="${page.pageSize}" />
        <label>配件类别：</label>
        <form:select path="materialCategory.id" cssStyle="width: 200px;">
            <form:option value="" label="请选择"></form:option>
            <form:options items="${materialCategoryList}" itemLabel="name" itemValue="id"></form:options>
        </form:select>
        &nbsp;
		<label>配件名称：</label>
		<form:input path="name" htmlEscape="false" maxlength="30" value="${name}" class="input-small" />
		<label>备注：</label>
		<form:input path="remarks" htmlEscape="false" maxlength="50" class="input-small" />
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="top.$.jBox.tip('正在查询,请稍候...', 'loading');return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}" />
	<table id="treeTable"
		class="table table-bordered table-hover table-hover">
		<thead>
			<tr>
				<th width="50px">序号</th>
				<th width="150px">配件类别</th>
				<th>名称</th>
				<th>参考价</th>
				<th width="50px">是否返件</th>
				<th>备注</th>
				<shiro:hasPermission name="md:material:edit">
					<th width="65px">操作</th>
				</shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
			<c:set var="index" value="0"></c:set>
			<c:forEach items="${page.list}" var="material">
				<tr>
					<c:set var="index" value="${index+1}"></c:set>
					<td>${index+(page.pageNo-1)*page.pageSize}</td>
					<td>${material.materialCategory.name}</td>
					<td>${material.name}</td>
					<td style="color: red;"><b><fmt:formatNumber value="${material.price}"
							pattern="0.00"></fmt:formatNumber></b></td>
					<c:choose>
						<c:when test="${material.isReturn==0}">
							<td>否</td>
						</c:when>
						<c:when test="${material.isReturn==1}">
							<td>是</td>
						</c:when>
						<c:otherwise>
							<td>未知</td>
						</c:otherwise>
					</c:choose>
					<td>${material.remarks}</td>
					<shiro:hasPermission name="md:material:edit">
						<td><a href="${ctx}/md/material/form?id=${material.id}&materialCategory.name=${fns:urlEncode(material.materialCategory.name)}">修改</a>
							<a href="${ctx}/md/material/delete?id=${material.id}"
							onclick="return confirmx('确认要删除该配件吗？', this.href)">删除</a>
						</td>
					</shiro:hasPermission>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</body>
</html>
