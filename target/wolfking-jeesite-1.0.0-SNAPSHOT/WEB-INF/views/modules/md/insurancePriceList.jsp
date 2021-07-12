<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>保险价格列表</title>
	<meta name="decorator" content="default"/>
	  <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
  </head>

  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">保险价格列表</a></li>
		<shiro:hasPermission name="md:insuranceprice:edit"><li><a href="${ctx}/md/insurancePrice/form">保险价格添加</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="insurancePrice" action="${ctx}/md/insurancePrice" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<label>产品品类名称 ：</label>
		<%--<form:input path="category.name" htmlEscape="false" maxlength="20" class="input-large"/>--%>
		<select id="category.id" name="category.id" class="input-small"	style="width:250px;">
			<option value=""
					<c:out value="${(empty insurancePrice.category.id)?'selected=selected':''}" />>所有</option>
			<c:forEach items="${productCategories}" var="dict">
				<option value="${dict.id}"
						<c:out value="${(insurancePrice.category.id eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
			</c:forEach>
		</select>
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<th width="300">产品品类名称</th>
				<th width="100">价格</th>
				<th width="150">创建时间</th>
				<th>描述</th>
				<shiro:hasPermission name="md:insuranceprice:edit"><th width="100">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="entity">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${entity.category.name}</td>
				<td>
					<fmt:formatNumber value="${entity.insurance}" pattern="0.00"></fmt:formatNumber>
				</td>
				<td><fmt:formatDate value="${entity.createDate}" pattern="yyyy-MM-dd HH:mm:ss"></fmt:formatDate></td>
				<td>${entity.remarks}</td>
				<shiro:hasPermission name="md:insuranceprice:edit"><td>
    				<a href="${ctx}/md/insurancePrice/form?id=${entity.id}">修改</a>
					<a href="${ctx}/md/insurancePrice/delete?id=${entity.id}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>
				</td></shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
  </body>
</html>
