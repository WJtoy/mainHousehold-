<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>品牌产品分类</title>
	<meta name="decorator" content="default"/>
	  <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
  </head>

  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">列表</a></li>
		<shiro:hasPermission name="md:brandcategory:edit"><li><a href="${ctx}/md/brandCategory/form">添加</a></li></shiro:hasPermission>
	</ul>
	<form:form id="searchForm" modelAttribute="brandCategory" action="${ctx}/md/brandCategory" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<label>产品分类:</label>
		<form:select path="category.id" cssClass="input-large">
			<form:option value="" label="请选择"></form:option>
			<form:options items="${categoryList}" itemValue="id" itemLabel="name"></form:options>
		</form:select>
		&nbsp;<label>品牌:</label>
		<form:select path="brand.id" cssClass="input-large">
			<form:option value="" label="请选择"></form:option>
			<form:options items="${brandList}" itemValue="id" itemLabel="name"></form:options>
		</form:select>
		&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
		<thead>
			<tr>
				<th width="50">序号</th>
				<th width="150">产品分类名称</th>
				<th>品牌名称</th>
				<shiro:hasPermission name="md:brandcategory:edit">
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
				<td>
					<a href="${ctx}/md/brandCategory/form?categoryId=${entity.category.id}">${entity.category.name}</a>
				</td>
				<td>${entity.brand.name}</td>
				<shiro:hasPermission name="md:brandcategory:edit">
                <td>
                    <a href="${ctx}/md/brandCategory/form?categoryId=${entity.category.id}">修改</a>
                    <a href="${ctx}/md/brandCategory/delete?id=${entity.id}" onclick="return confirmx('确认要删除吗？', this.href)" style="margin-left: 10px;">删除</a>
                </td>
                </shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
  </body>
</html>
