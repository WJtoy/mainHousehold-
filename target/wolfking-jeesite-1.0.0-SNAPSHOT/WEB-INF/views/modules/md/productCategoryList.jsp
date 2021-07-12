<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>产品品类</title>
	<meta name="decorator" content="default"/>
	  <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
  </head>

  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">产品品类列表</a></li>
		<shiro:hasPermission name="md:productcategory:edit"><li><a href="${ctx}/md/productcategory/form">产品品类添加</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="productCategory" action="${ctx}/md/productcategory" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<label>编码：</label><form:input path="code" htmlEscape="false" maxlength="10" class="input-small"/>
		&nbsp;
		<label>名称 ：</label><form:input path="name" htmlEscape="false" maxlength="20" class="input-small"/>
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50px">序号</th>
				<th>编码</th>
				<th>名称</th>
				<th>描述</th>
				<shiro:hasPermission name="md:productcategory:edit"><th width="65px">操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:set var="index" value="0"></c:set>
		<c:forEach items="${page.list}" var="productcategory">
			<tr>
				<c:set var="index" value="${index+1}"></c:set>
				<td>${index+(page.pageNo-1)*page.pageSize}</td>
				<td>${productcategory.code}</td>
				<td>${productcategory.name}</td>
				<td>${productcategory.remarks}</td>
				<shiro:hasPermission name="md:productcategory:edit"><td>
    				<a href="${ctx}/md/productcategory/form?id=${productcategory.id}">修改</a>
					<a href="${ctx}/md/productcategory/delete?id=${productcategory.id}" onclick="return confirmx('确认要删除 [${productcategory.name}] 产品分类吗？', this.href)">删除</a>
				</td></shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
  </body>
</html>
